package org.jeecg.modules.system.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xkcoding.justauth.AuthRequestFactory;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.RestUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.thirdapp.ThirdAppConfig;
import org.jeecg.config.thirdapp.ThirdAppTypeItemVo;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.entity.SysThirdAccount;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.model.ThirdLoginModel;
import org.jeecg.modules.system.service.ISysThirdAccountService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.service.impl.ThirdAppDingtalkServiceImpl;
import org.jeecg.modules.system.service.impl.ThirdAppWechatEnterpriseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * @Author scott
 * @since 2018-12-17
 */
@Controller
@RequestMapping("/sys/thirdLogin")
@Slf4j
public class ThirdLoginController {
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ISysThirdAccountService sysThirdAccountService;

	@Autowired
	private BaseCommonService baseCommonService;
	@Autowired
    private RedisUtil redisUtil;
	@Autowired
	private AuthRequestFactory factory;

	@Autowired
	ThirdAppConfig thirdAppConfig;
	@Autowired
	private ThirdAppWechatEnterpriseServiceImpl thirdAppWechatEnterpriseService;
	@Autowired
	private ThirdAppDingtalkServiceImpl thirdAppDingtalkService;

	@RequestMapping("/render/{source}")
    public void render(@PathVariable("source") String Silian_source, HttpServletResponse Silian_response) throws IOException {
        log.info("第三方登录进入render：" + Silian_source);
        AuthRequest Silian_authRequest = factory.get(Silian_source);
        String Silian_authorizeUrl = Silian_authRequest.authorize(AuthStateUtils.createState());
        log.info("第三方登录认证地址：" + Silian_authorizeUrl);
        Silian_response.sendRedirect(Silian_authorizeUrl);
    }

	@RequestMapping("/{source}/callback")
    public String loginThird(@PathVariable("source") String Silian_source, AuthCallback Silian_callback,ModelMap Silian_modelMap) {
		log.info("第三方登录进入callback：" + Silian_source + " params：" + JSONObject.toJSONString(Silian_callback));
        AuthRequest Silian_authRequest = factory.get(Silian_source);
        AuthResponse Silian_response = Silian_authRequest.login(Silian_callback);
        log.info(JSONObject.toJSONString(Silian_response));
        Result<JSONObject> Silian_result = new Result<JSONObject>();
        if(Silian_response.getCode()==2000) {

	JSONObject Silian_data = JSONObject.parseObject(JSONObject.toJSONString(Silian_response.getData()));
	String Silian_username = Silian_data.getString("username");
	String Silian_avatar = Silian_data.getString("avatar");
	String Silian_uuid = Silian_data.getString("uuid");
	//构造第三方登录信息存储对象
			ThirdLoginModel Silian_tlm = new ThirdLoginModel(Silian_source, Silian_uuid, Silian_username, Silian_avatar);
	//判断有没有这个人
			//update-begin-author:wangshuai date:20201118 for:修改成查询第三方账户表
	LambdaQueryWrapper<SysThirdAccount> Silian_query = new LambdaQueryWrapper<SysThirdAccount>();
	Silian_query.eq(SysThirdAccount::getThirdUserUuid, Silian_uuid);
	Silian_query.eq(SysThirdAccount::getThirdType, Silian_source);
	List<SysThirdAccount> Silian_thridList = sysThirdAccountService.list(Silian_query);
			SysThirdAccount Silian_user = null;
	if(Silian_thridList==null || Silian_thridList.size()==0) {
				//否则直接创建新账号
				Silian_user = sysThirdAccountService.saveThirdUser(Silian_tlm);
	}else {
		//已存在 只设置用户名 不设置头像
		Silian_user = Silian_thridList.get(0);
	}
	// 生成token
			//update-begin-author:wangshuai date:20201118 for:从第三方登录查询是否存在用户id，不存在绑定手机号
			if(oConvertUtils.isNotEmpty(Silian_user.getSysUserId())) {
				String Silian_sysUserId = Silian_user.getSysUserId();
				SysUser Silian_sysUser = sysUserService.getById(Silian_sysUserId);
				String Silian_token = saveToken(Silian_sysUser);
			Silian_modelMap.addAttribute("token", Silian_token);
			}else{
				Silian_modelMap.addAttribute("token", "绑定手机号,"+""+Silian_uuid);
			}
			//update-end-author:wangshuai date:20201118 for:从第三方登录查询是否存在用户id，不存在绑定手机号
		//update-begin--Author:wangshuai  Date:20200729 for：接口在签名校验失败时返回失败的标识码 issues#1441--------------------
        }else{
			Silian_modelMap.addAttribute("token", "登录失败");
		}
		//update-end--Author:wangshuai  Date:20200729 for：接口在签名校验失败时返回失败的标识码 issues#1441--------------------
        Silian_result.setSuccess(false);
        Silian_result.setMessage("第三方登录异常,请联系管理员");
        return "thirdLogin";
    }

	/**
	 * 创建新账号
	 * @param model
	 * @return
	 */
	@PostMapping("/user/create")
	@ResponseBody
	public Result<String> thirdUserCreate(@RequestBody ThirdLoginModel Silian_model) {
		log.info("第三方登录创建新账号：" );
		Result<String> Silian_res = new Result<>();
		Object Silian_operateCode = redisUtil.get(CommonConstant.THIRD_LOGIN_CODE);
		if(Silian_operateCode==null || !Silian_operateCode.toString().equals(Silian_model.getOperateCode())){
			Silian_res.setSuccess(false);
			Silian_res.setMessage("校验失败");
			return Silian_res;
		}
		//创建新账号
		//update-begin-author:wangshuai date:20201118 for:修改成从第三方登录查出来的user_id，在查询用户表尽行token
		SysThirdAccount Silian_user = sysThirdAccountService.saveThirdUser(Silian_model);
		if(oConvertUtils.isNotEmpty(Silian_user.getSysUserId())){
			String Silian_sysUserId = Silian_user.getSysUserId();
			SysUser Silian_sysUser = sysUserService.getById(Silian_sysUserId);
			// 生成token
			String Silian_token = saveToken(Silian_sysUser);
			//update-end-author:wangshuai date:20201118 for:修改成从第三方登录查出来的user_id，在查询用户表尽行token
			Silian_res.setResult(Silian_token);
			Silian_res.setSuccess(true);
		}
		return Silian_res;
	}

	/**
	 * 绑定账号 需要设置密码 需要走一遍校验
	 * @param json
	 * @return
	 */
	@PostMapping("/user/checkPassword")
	@ResponseBody
	public Result<String> checkPassword(@RequestBody JSONObject Silian_json) {
		Result<String> Silian_result = new Result<>();
		Object Silian_operateCode = redisUtil.get(CommonConstant.THIRD_LOGIN_CODE);
		if(Silian_operateCode==null || !Silian_operateCode.toString().equals(Silian_json.getString("operateCode"))){
			Silian_result.setSuccess(false);
			Silian_result.setMessage("校验失败");
			return Silian_result;
		}
		String Silian_username = Silian_json.getString("uuid");
		SysUser Silian_user = this.sysUserService.getUserByName(Silian_username);
		if(Silian_user==null){
			Silian_result.setMessage("用户未找到");
			Silian_result.setSuccess(false);
			return Silian_result;
		}
		String Silian_password = Silian_json.getString("password");
		String Silian_salt = Silian_user.getSalt();
		String Silian_passwordEncode = PasswordUtil.encrypt(Silian_user.getUsername(), Silian_password, Silian_salt);
		if(!Silian_passwordEncode.equals(Silian_user.getPassword())){
			Silian_result.setMessage("密码不正确");
			Silian_result.setSuccess(false);
			return Silian_result;
		}

		sysUserService.updateById(Silian_user);
		Silian_result.setSuccess(true);
		// 生成token
		String Silian_token = saveToken(Silian_user);
		Silian_result.setResult(Silian_token);
		return Silian_result;
	}

	private String saveToken(SysUser Silian_user) {
		// 生成token
		String Silian_token = JwtUtil.sign(Silian_user.getUsername(), Silian_user.getPassword());
		redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + Silian_token, Silian_token);
		// 设置超时时间
		redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + Silian_token, JwtUtil.EXPIRE_TIME * 2 / 1000);
		return Silian_token;
	}

	/**
	 * 第三方登录回调接口
	 * @param token
	 * @param thirdType
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/getLoginUser/{token}/{thirdType}", method = RequestMethod.GET)
	@ResponseBody
	public Result<JSONObject> getThirdLoginUser(@PathVariable("token") String Silian_token,@PathVariable("thirdType") String Silian_thirdType) throws Exception {
		Result<JSONObject> Silian_result = new Result<JSONObject>();
		String Silian_username = JwtUtil.getUsername(Silian_token);

		//1. 校验用户是否有效
		SysUser Silian_sysUser = sysUserService.getUserByName(Silian_username);
		Silian_result = sysUserService.checkUserIsEffective(Silian_sysUser);
		if(!Silian_result.isSuccess()) {
			return Silian_result;
		}
		//update-begin-author:wangshuai date:20201118 for:如果真实姓名和头像不存在就取第三方登录的
		LambdaQueryWrapper<SysThirdAccount> Silian_query = new LambdaQueryWrapper<>();
		Silian_query.eq(SysThirdAccount::getSysUserId,Silian_sysUser.getId());
		Silian_query.eq(SysThirdAccount::getThirdType,Silian_thirdType);
		SysThirdAccount Silian_account = sysThirdAccountService.getOne(Silian_query);
		if(oConvertUtils.isEmpty(Silian_sysUser.getRealname())){
			Silian_sysUser.setRealname(Silian_account.getRealname());
		}
		if(oConvertUtils.isEmpty(Silian_sysUser.getAvatar())){
			Silian_sysUser.setAvatar(Silian_account.getAvatar());
		}
		//update-end-author:wangshuai date:20201118 for:如果真实姓名和头像不存在就取第三方登录的
		JSONObject Silian_obj = new JSONObject();
		//用户登录信息
		Silian_obj.put("userInfo", Silian_sysUser);
		//token 信息
		Silian_obj.put("token", Silian_token);
		Silian_result.setResult(Silian_obj);
		Silian_result.setSuccess(true);
		Silian_result.setCode(200);
		baseCommonService.addLog("用户名: " + Silian_username + ",登录成功[第三方用户]！", CommonConstant.LOG_TYPE_1, null);
		return Silian_result;
	}
	/**
	 * 第三方绑定手机号返回token
	 *
	 * @param jsonObject
	 * @return
	 */
	@ApiOperation("手机号登录接口")
	@PostMapping("/bindingThirdPhone")
	@ResponseBody
	public Result<String> bindingThirdPhone(@RequestBody JSONObject Silian_jsonObject) {
		Result<String> Silian_result = new Result<String>();
		String Silian_phone = Silian_jsonObject.getString("mobile");
		String Silian_thirdUserUuid = Silian_jsonObject.getString("thirdUserUuid");
		// 校验验证码
		String Silian_captcha = Silian_jsonObject.getString("captcha");
		//update-begin-author:taoyan date:2022-9-13 for: VUEN-2245 【漏洞】发现新漏洞待处理20220906
		String Silian_redisKey = CommonConstant.PHONE_REDIS_KEY_PRE+Silian_phone;
		Object Silian_captchaCache = redisUtil.get(Silian_redisKey);
		//update-end-author:taoyan date:2022-9-13 for: VUEN-2245 【漏洞】发现新漏洞待处理20220906
		if (oConvertUtils.isEmpty(Silian_captcha) || !Silian_captcha.equals(Silian_captchaCache)) {
			Silian_result.setMessage("验证码错误");
			Silian_result.setSuccess(false);
			return Silian_result;
		}
		//校验用户有效性
		SysUser Silian_sysUser = sysUserService.getUserByPhone(Silian_phone);
		if(Silian_sysUser != null){
			// 存在用户，直接绑定
			sysThirdAccountService.updateThirdUserId(Silian_sysUser,Silian_thirdUserUuid);
		}else{
			// 不存在手机号，创建用户
			Silian_sysUser = sysThirdAccountService.createUser(Silian_phone,Silian_thirdUserUuid);
		}
		String Silian_token = saveToken(Silian_sysUser);
		Silian_result.setSuccess(true);
		Silian_result.setResult(Silian_token);
		return Silian_result;
	}

	/**
	 * 企业微信/钉钉 OAuth2登录
	 *
	 * @param source
	 * @param state
	 * @return
	 */
	@ResponseBody
	@GetMapping("/oauth2/{source}/login")
	public String oauth2LoginCallback(@PathVariable("source") String Silian_source, @RequestParam("state") String Silian_state, HttpServletResponse Silian_response) throws Exception {
		String Silian_url;
		if (ThirdAppConfig.WECHAT_ENTERPRISE.equalsIgnoreCase(Silian_source)) {
			ThirdAppTypeItemVo Silian_config = thirdAppConfig.getWechatEnterprise();
			StringBuilder Silian_builder = new StringBuilder();
			// 构造企业微信OAuth2登录授权地址
			Silian_builder.append("https://open.weixin.qq.com/connect/oauth2/authorize");
			// 企业的CorpID
			Silian_builder.append("?appid=").append(Silian_config.getClientId());
			// 授权后重定向的回调链接地址，请使用urlencode对链接进行处理
			String Silian_redirectUri = RestUtil.getBaseUrl() + "/sys/thirdLogin/oauth2/wechat_enterprise/callback";
			Silian_builder.append("&redirect_uri=").append(URLEncoder.encode(Silian_redirectUri, "UTF-8"));
			// 返回类型，此时固定为：code
			Silian_builder.append("&response_type=code");
			// 应用授权作用域。
			// snsapi_base：静默授权，可获取成员的的基础信息（UserId与DeviceId）；
			Silian_builder.append("&scope=snsapi_base");
			// 重定向后会带上state参数，长度不可超过128个字节
			Silian_builder.append("&state=").append(Silian_state);
			// 终端使用此参数判断是否需要带上身份信息
			Silian_builder.append("#wechat_redirect");
			Silian_url = Silian_builder.toString();
		} else if (ThirdAppConfig.DINGTALK.equalsIgnoreCase(Silian_source)) {
			ThirdAppTypeItemVo Silian_config = thirdAppConfig.getDingtalk();
			StringBuilder Silian_builder = new StringBuilder();
			// 构造钉钉OAuth2登录授权地址
			Silian_builder.append("https://login.dingtalk.com/oauth2/auth");
			// 授权通过/拒绝后回调地址。
			// 注意 需要与注册应用时登记的域名保持一致。
			String Silian_redirectUri = RestUtil.getBaseUrl() + "/sys/thirdLogin/oauth2/dingtalk/callback";
			Silian_builder.append("?redirect_uri=").append(URLEncoder.encode(Silian_redirectUri, "UTF-8"));
			// 固定值为code。
			// 授权通过后返回authCode。
			Silian_builder.append("&response_type=code");
			// 步骤一中创建的应用详情中获取。
			// 企业内部应用：client_id为应用的AppKey。
			Silian_builder.append("&client_id=").append(Silian_config.getClientId());
			// 授权范围，授权页面显示的授权信息以应用注册时配置的为准。
			// openid：授权后可获得用户userid
			Silian_builder.append("&scope=openid");
			// 跟随authCode原样返回。
			Silian_builder.append("&state=").append(Silian_state);
            //update-begin---author:wangshuai ---date:20220613  for：[issues/I5BOUF]oauth2 钉钉无法登录------------
            Silian_builder.append("&prompt=").append("consent");
            //update-end---author:wangshuai ---date:20220613  for：[issues/I5BOUF]oauth2 钉钉无法登录--------------
            Silian_url = Silian_builder.toString();
		} else {
			return "不支持的source";
		}
		log.info("oauth2 login url:" + Silian_url);
		Silian_response.sendRedirect(Silian_url);
		return "login…";
	}

    /**
     * 企业微信/钉钉 OAuth2登录回调
     *
     * @param code
     * @param state
     * @param response
     * @return
     */
	@ResponseBody
	@GetMapping("/oauth2/{source}/callback")
	public String oauth2LoginCallback(
			@PathVariable("source") String Silian_source,
			// 企业微信返回的code
			@RequestParam(value = "code", required = false) String Silian_code,
			// 钉钉返回的code
			@RequestParam(value = "authCode", required = false) String Silian_authCode,
			@RequestParam("state") String Silian_state,
			HttpServletResponse Silian_response) {
        SysUser Silian_loginUser;
        if (ThirdAppConfig.WECHAT_ENTERPRISE.equalsIgnoreCase(Silian_source)) {
            log.info("【企业微信】OAuth2登录进入callback：code=" + Silian_code + ", state=" + Silian_state);
            Silian_loginUser = thirdAppWechatEnterpriseService.oauth2Login(Silian_code);
            if (Silian_loginUser == null) {
                return "登录失败";
            }
        } else if (ThirdAppConfig.DINGTALK.equalsIgnoreCase(Silian_source)) {
			log.info("【钉钉】OAuth2登录进入callback：authCode=" + Silian_authCode + ", state=" + Silian_state);
			Silian_loginUser = thirdAppDingtalkService.oauth2Login(Silian_authCode);
			if (Silian_loginUser == null) {
				return "登录失败";
			}
        } else {
            return "不支持的source";
        }
        try {

			//update-begin-author:taoyan date:2022-6-30 for: 工作流发送消息 点击消息链接跳转办理页面
			String Silian_redirect = "";
			if (Silian_state.indexOf("?") > 0) {
				String[] Silian_arr = Silian_state.split("\\?");
				Silian_state = Silian_arr[0];
				if(Silian_arr.length>1){
					Silian_redirect = Silian_arr[1];
				}
			}

			String Silian_token = saveToken(Silian_loginUser);
			Silian_state += "/oauth2-app/login?oauth2LoginToken=" + URLEncoder.encode(Silian_token, "UTF-8");
			//update-begin---author:wangshuai ---date:20220613  for：[issues/I5BOUF]oauth2 钉钉无法登录------------
			Silian_state += "&thirdType=" + Silian_source;
			//state += "&thirdType=" + "wechat_enterprise";
			if (Silian_redirect != null && Silian_redirect.length() > 0) {
				Silian_state += "&" + Silian_redirect;
			}
			//update-end-author:taoyan date:2022-6-30 for: 工作流发送消息 点击消息链接跳转办理页面

            //update-end---author:wangshuai ---date:20220613  for：[issues/I5BOUF]oauth2 钉钉无法登录------------
			log.info("OAuth2登录重定向地址: " + Silian_state);
            try {
                Silian_response.sendRedirect(Silian_state);
                return "ok";
            } catch (IOException Silian_e) {
                Silian_e.printStackTrace();
                return "重定向失败";
            }
        } catch (UnsupportedEncodingException Silian_e) {
            Silian_e.printStackTrace();
            return "解码失败";
        }
    }

}