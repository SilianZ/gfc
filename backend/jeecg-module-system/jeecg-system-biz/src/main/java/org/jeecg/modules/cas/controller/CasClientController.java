package org.jeecg.modules.cas.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.cas.util.CasServiceUtil;
import org.jeecg.modules.cas.util.XmlUtils;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * CAS单点登录客户端登录认证
 * </p>
 *
 * @Author zhoujf
 * @since 2018-12-20
 */
@Slf4j
@RestController
@RequestMapping("/sys/cas/client")
public class CasClientController {

	@Autowired
	private ISysUserService sysUserService;
	@Autowired
    private ISysDepartService sysDepartService;
	@Autowired
    private RedisUtil redisUtil;

	@Value("${cas.prefixUrl}")
    private String prefixUrl;


	@GetMapping("/validateLogin")
	public Object validateLogin(@RequestParam(name="ticket") String Silian_ticket,
								@RequestParam(name="service") String Silian_service,
								HttpServletRequest Silian_request,
								HttpServletResponse Silian_response) throws Exception {
		Result<JSONObject> Silian_result = new Result<JSONObject>();
		log.info("Rest api login.");
		try {
			String Silian_validateUrl = prefixUrl+"/p3/serviceValidate";
			String Silian_res = CasServiceUtil.getStValidate(Silian_validateUrl, Silian_ticket, Silian_service);
			log.info("res."+Silian_res);
			final String Silian_error = XmlUtils.getTextForElement(Silian_res, "authenticationFailure");
			if(StringUtils.isNotEmpty(Silian_error)) {
				throw new Exception(Silian_error);
			}
			final String Silian_principal = XmlUtils.getTextForElement(Silian_res, "user");
			if (StringUtils.isEmpty(Silian_principal)) {
	            throw new Exception("No principal was found in the response from the CAS server.");
	        }
			log.info("-------token----username---"+Silian_principal);
		    //1. 校验用户是否有效
			SysUser Silian_sysUser = sysUserService.getUserByName(Silian_principal);
			Silian_result = sysUserService.checkUserIsEffective(Silian_sysUser);
			if(!Silian_result.isSuccess()) {
				return Silian_result;
			}
			String Silian_token = JwtUtil.sign(Silian_sysUser.getUsername(), Silian_sysUser.getPassword());
			// 设置超时时间
			redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + Silian_token, Silian_token);
			redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + Silian_token, JwtUtil.EXPIRE_TIME*2 / 1000);

			//获取用户部门信息
			JSONObject Silian_obj = new JSONObject();
			List<SysDepart> Silian_departs = sysDepartService.queryUserDeparts(Silian_sysUser.getId());
			Silian_obj.put("departs", Silian_departs);
			if (Silian_departs == null || Silian_departs.size() == 0) {
				Silian_obj.put("multi_depart", 0);
			} else if (Silian_departs.size() == 1) {
				sysUserService.updateUserDepart(Silian_principal, Silian_departs.get(0).getOrgCode());
				Silian_obj.put("multi_depart", 1);
			} else {
				Silian_obj.put("multi_depart", 2);
			}
			Silian_obj.put("token", Silian_token);
			Silian_obj.put("userInfo", Silian_sysUser);
			Silian_result.setResult(Silian_obj);
			Silian_result.success("登录成功");

		} catch (Exception Silian_e) {
			//e.printStackTrace();
			Silian_result.error500(Silian_e.getMessage());
		}
		return new HttpEntity<>(Silian_result);
	}


}
