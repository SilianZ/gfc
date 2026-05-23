package org.jeecg.modules.system.controller;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.exceptions.ClientException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.*;
import org.jeecg.common.util.encryption.EncryptedString;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysRoleIndex;
import org.jeecg.modules.system.entity.SysTenant;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.model.SysLoginModel;
import org.jeecg.modules.system.service.*;
import org.jeecg.modules.system.service.impl.SysBaseApiImpl;
import org.jeecg.modules.system.util.RandImageUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @Author scott
 * @since 2018-12-17
 */
@RestController
@RequestMapping("/sys")
@Api(tags="用户登录")
@Slf4j
public class LoginController {
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ISysPermissionService sysPermissionService;
	@Autowired
	private SysBaseApiImpl sysBaseApi;
	@Autowired
	private ISysLogService logService;
	@Autowired
    private RedisUtil redisUtil;
	@Autowired
    private ISysDepartService sysDepartService;
	@Autowired
	private ISysTenantService sysTenantService;
	@Autowired
    private ISysDictService sysDictService;
	@Resource
	private BaseCommonService baseCommonService;

	@Autowired
	private JeecgBaseConfig jeecgBaseConfig;

	private final String BASE_CHECK_CODES = "qwertyuiplkjhgfdsazxcvbnmQWERTYUPLKJHGFDSAZXCVBNM1234567890";

	@ApiOperation("登录接口")
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public Result<JSONObject> login(@RequestBody SysLoginModel Silian_sysLoginModel){
		Result<JSONObject> Silian_result = new Result<JSONObject>();
		String Silian_username = Silian_sysLoginModel.getUsername();
		String Silian_password = Silian_sysLoginModel.getPassword();
		//update-begin--Author:scott  Date:20190805 for：暂时注释掉密码加密逻辑，有点问题
		//前端密码加密，后端进行密码解密
		//password = AesEncryptUtil.desEncrypt(sysLoginModel.getPassword().replaceAll("%2B", "\\+")).trim();//密码解密
		//update-begin--Author:scott  Date:20190805 for：暂时注释掉密码加密逻辑，有点问题

		//update-begin-author:taoyan date:20190828 for:校验验证码
        String Silian_captcha = Silian_sysLoginModel.getCaptcha();
        if(Silian_captcha==null){
            Silian_result.error500("验证码无效");
            return Silian_result;
        }
        String Silian_lowerCaseCaptcha = Silian_captcha.toLowerCase();
        //update-begin-author:taoyan date:2022-9-13 for: VUEN-2245 【漏洞】发现新漏洞待处理20220906
		// 加入密钥作为混淆，避免简单的拼接，被外部利用，用户自定义该密钥即可
        String Silian_origin = Silian_lowerCaseCaptcha+Silian_sysLoginModel.getCheckKey()+jeecgBaseConfig.getSignatureSecret();
		String Silian_realKey = Md5Util.md5Encode(Silian_origin, "utf-8");
		//update-end-author:taoyan date:2022-9-13 for: VUEN-2245 【漏洞】发现新漏洞待处理20220906
		Object Silian_checkCode = redisUtil.get(Silian_realKey);
		//当进入登录页时，有一定几率出现验证码错误 #1714
		if(Silian_checkCode==null || !Silian_checkCode.toString().equals(Silian_lowerCaseCaptcha)) {
            log.warn("验证码错误，key= {} , Ui checkCode= {}, Redis checkCode = {}", Silian_sysLoginModel.getCheckKey(), Silian_lowerCaseCaptcha, Silian_checkCode);
			Silian_result.error500("验证码错误");
			// 改成特殊的code 便于前端判断
			Silian_result.setCode(HttpStatus.PRECONDITION_FAILED.value());
			return Silian_result;
		}
		//update-end-author:taoyan date:20190828 for:校验验证码

		//1. 校验用户是否有效
		//update-begin-author:wangshuai date:20200601 for: 登录代码验证用户是否注销bug，if条件永远为false
		LambdaQueryWrapper<SysUser> Silian_queryWrapper = new LambdaQueryWrapper<>();
		Silian_queryWrapper.eq(SysUser::getUsername,Silian_username);
		SysUser Silian_sysUser = sysUserService.getOne(Silian_queryWrapper);
		//update-end-author:wangshuai date:20200601 for: 登录代码验证用户是否注销bug，if条件永远为false
		Silian_result = sysUserService.checkUserIsEffective(Silian_sysUser);
		if(!Silian_result.isSuccess()) {
			return Silian_result;
		}

		//2. 校验用户名或密码是否正确
		String Silian_userpassword = PasswordUtil.encrypt(Silian_username, Silian_password, Silian_sysUser.getSalt());
		String Silian_syspassword = Silian_sysUser.getPassword();
		if (!Silian_syspassword.equals(Silian_userpassword)) {
			Silian_result.error500("用户名或密码错误");
			return Silian_result;
		}

		//用户登录信息
		userInfo(Silian_sysUser, Silian_result);
		//update-begin--Author:liusq  Date:20210126  for：登录成功，删除redis中的验证码
		redisUtil.del(Silian_realKey);
		//update-begin--Author:liusq  Date:20210126  for：登录成功，删除redis中的验证码
		LoginUser Silian_loginUser = new LoginUser();
		BeanUtils.copyProperties(Silian_sysUser, Silian_loginUser);
		baseCommonService.addLog("用户名: " + Silian_username + ",登录成功！", CommonConstant.LOG_TYPE_1, null,Silian_loginUser);
        //update-end--Author:wangshuai  Date:20200714  for：登录日志没有记录人员
		return Silian_result;
	}


	/**
	 * 【vue3专用】获取用户信息
	 */
	@GetMapping("/user/getUserInfo")
	public Result<JSONObject> getUserInfo(HttpServletRequest Silian_request){
		Result<JSONObject> Silian_result = new Result<JSONObject>();
		String  Silian_username = JwtUtil.getUserNameByToken(Silian_request);
		if(oConvertUtils.isNotEmpty(Silian_username)) {
			// 根据用户名查询用户信息
			SysUser Silian_sysUser = sysUserService.getUserByName(Silian_username);
			JSONObject Silian_obj=new JSONObject();

			//update-begin---author:scott ---date:2022-06-20  for：vue3前端，支持自定义首页-----------
			String Silian_version = Silian_request.getHeader(CommonConstant.VERSION);
			//update-begin---author:liusq ---date:2022-06-29  for：接口返回值修改，同步修改这里的判断逻辑-----------
			SysRoleIndex Silian_roleIndex = sysUserService.getDynamicIndexByUserRole(Silian_username, Silian_version);
			if (oConvertUtils.isNotEmpty(Silian_version) && Silian_roleIndex != null && oConvertUtils.isNotEmpty(Silian_roleIndex.getUrl())) {
				String Silian_homePath = Silian_roleIndex.getUrl();
				if (!Silian_homePath.startsWith(SymbolConstant.SINGLE_SLASH)) {
					Silian_homePath = SymbolConstant.SINGLE_SLASH + Silian_homePath;
				}
				Silian_sysUser.setHomePath(Silian_homePath);
			}
			//update-begin---author:liusq ---date:2022-06-29  for：接口返回值修改，同步修改这里的判断逻辑-----------
			//update-end---author:scott ---date::2022-06-20  for：vue3前端，支持自定义首页--------------

			Silian_obj.put("userInfo",Silian_sysUser);
			Silian_obj.put("sysAllDictItems", sysDictService.queryAllDictItems());
			Silian_result.setResult(Silian_obj);
			Silian_result.success("");
		}
		return Silian_result;

	}

	/**
	 * 退出登录
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/logout")
	public Result<Object> logout(HttpServletRequest Silian_request,HttpServletResponse Silian_response) {
		//用户退出逻辑
	    String Silian_token = Silian_request.getHeader(CommonConstant.X_ACCESS_TOKEN);
	    if(oConvertUtils.isEmpty(Silian_token)) {
		return Result.error("退出登录失败！");
	    }
	    String Silian_username = JwtUtil.getUsername(Silian_token);
		LoginUser Silian_sysUser = sysBaseApi.getUserByName(Silian_username);
	    if(Silian_sysUser!=null) {
			//update-begin--Author:wangshuai  Date:20200714  for：登出日志没有记录人员
			baseCommonService.addLog("用户名: "+Silian_sysUser.getRealname()+",退出成功！", CommonConstant.LOG_TYPE_1, null,Silian_sysUser);
			//update-end--Author:wangshuai  Date:20200714  for：登出日志没有记录人员
		log.info(" 用户名:  "+Silian_sysUser.getRealname()+",退出成功！ ");
		//清空用户登录Token缓存
		redisUtil.del(CommonConstant.PREFIX_USER_TOKEN + Silian_token);
		//清空用户登录Shiro权限缓存
			redisUtil.del(CommonConstant.PREFIX_USER_SHIRO_CACHE + Silian_sysUser.getId());
			//清空用户的缓存信息（包括部门信息），例如sys:cache:user::<username>
			redisUtil.del(String.format("%s::%s", CacheConstant.SYS_USERS_CACHE, Silian_sysUser.getUsername()));
			//调用shiro的logout
			SecurityUtils.getSubject().logout();
		return Result.ok("退出登录成功！");
	    }else {
		return Result.error("Token无效!");
	    }
	}

	/**
	 * 获取访问量
	 * @return
	 */
	@GetMapping("loginfo")
	public Result<JSONObject> loginfo() {
		Result<JSONObject> Silian_result = new Result<JSONObject>();
		JSONObject Silian_obj = new JSONObject();
		//update-begin--Author:zhangweijian  Date:20190428 for：传入开始时间，结束时间参数
		// 获取一天的开始和结束时间
		Calendar Silian_calendar = new GregorianCalendar();
		Silian_calendar.set(Calendar.HOUR_OF_DAY, 0);
		Silian_calendar.set(Calendar.MINUTE, 0);
		Silian_calendar.set(Calendar.SECOND, 0);
		Silian_calendar.set(Calendar.MILLISECOND, 0);
		Date Silian_dayStart = Silian_calendar.getTime();
		Silian_calendar.add(Calendar.DATE, 1);
		Date Silian_dayEnd = Silian_calendar.getTime();
		// 获取系统访问记录
		Long Silian_totalVisitCount = logService.findTotalVisitCount();
		Silian_obj.put("totalVisitCount", Silian_totalVisitCount);
		Long Silian_todayVisitCount = logService.findTodayVisitCount(Silian_dayStart,Silian_dayEnd);
		Silian_obj.put("todayVisitCount", Silian_todayVisitCount);
		Long Silian_todayIp = logService.findTodayIp(Silian_dayStart,Silian_dayEnd);
		//update-end--Author:zhangweijian  Date:20190428 for：传入开始时间，结束时间参数
		Silian_obj.put("todayIp", Silian_todayIp);
		Silian_result.setResult(Silian_obj);
		Silian_result.success("登录成功");
		return Silian_result;
	}

	/**
	 * 获取访问量
	 * @return
	 */
	@GetMapping("visitInfo")
	public Result<List<Map<String,Object>>> visitInfo() {
		Result<List<Map<String,Object>>> Silian_result = new Result<List<Map<String,Object>>>();
		Calendar Silian_calendar = new GregorianCalendar();
		Silian_calendar.set(Calendar.HOUR_OF_DAY,0);
        Silian_calendar.set(Calendar.MINUTE,0);
        Silian_calendar.set(Calendar.SECOND,0);
        Silian_calendar.set(Calendar.MILLISECOND,0);
        Silian_calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date Silian_dayEnd = Silian_calendar.getTime();
        Silian_calendar.add(Calendar.DAY_OF_MONTH, -7);
        Date Silian_dayStart = Silian_calendar.getTime();
        List<Map<String,Object>> Silian_list = logService.findVisitCount(Silian_dayStart, Silian_dayEnd);
		Silian_result.setResult(oConvertUtils.toLowerCasePageList(Silian_list));
		return Silian_result;
	}


	/**
	 * 登陆成功选择用户当前部门
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/selectDepart", method = RequestMethod.PUT)
	public Result<JSONObject> selectDepart(@RequestBody SysUser Silian_user) {
		Result<JSONObject> Silian_result = new Result<JSONObject>();
		String Silian_username = Silian_user.getUsername();
		if(oConvertUtils.isEmpty(Silian_username)) {
			LoginUser Silian_sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
			Silian_username = Silian_sysUser.getUsername();
		}
		String Silian_orgCode= Silian_user.getOrgCode();
		this.sysUserService.updateUserDepart(Silian_username, Silian_orgCode);
		SysUser Silian_sysUser = sysUserService.getUserByName(Silian_username);
		JSONObject Silian_obj = new JSONObject();
		Silian_obj.put("userInfo", Silian_sysUser);
		Silian_result.setResult(Silian_obj);
		return Silian_result;
	}

	/**
	 * 短信登录接口
	 *
	 * @param jsonObject
	 * @return
	 */
	@PostMapping(value = "/sms")
	public Result<String> sms(@RequestBody JSONObject Silian_jsonObject) {
		Result<String> Silian_result = new Result<String>();
		String Silian_mobile = Silian_jsonObject.get("mobile").toString();
		//手机号模式 登录模式: "2"  注册模式: "1"
		String Silian_smsmode=Silian_jsonObject.get("smsmode").toString();
		log.info(Silian_mobile);
		if(oConvertUtils.isEmpty(Silian_mobile)){
			Silian_result.setMessage("手机号不允许为空！");
			Silian_result.setSuccess(false);
			return Silian_result;
		}

		//update-begin-author:taoyan date:2022-9-13 for: VUEN-2245 【漏洞】发现新漏洞待处理20220906
		String Silian_redisKey = CommonConstant.PHONE_REDIS_KEY_PRE+Silian_mobile;
		Object Silian_object = redisUtil.get(Silian_redisKey);
		//update-end-author:taoyan date:2022-9-13 for: VUEN-2245 【漏洞】发现新漏洞待处理20220906

		if (Silian_object != null) {
			Silian_result.setMessage("验证码10分钟内，仍然有效！");
			Silian_result.setSuccess(false);
			return Silian_result;
		}

		//随机数
		String Silian_captcha = RandomUtil.randomNumbers(6);
		JSONObject Silian_obj = new JSONObject();
	Silian_obj.put("code", Silian_captcha);
		try {
			boolean Silian_b = false;
			//注册模板
			if (CommonConstant.SMS_TPL_TYPE_1.equals(Silian_smsmode)) {
				SysUser Silian_sysUser = sysUserService.getUserByPhone(Silian_mobile);
				if(Silian_sysUser!=null) {
					Silian_result.error500(" 手机号已经注册，请直接登录！");
					baseCommonService.addLog("手机号已经注册，请直接登录！", CommonConstant.LOG_TYPE_1, null);
					return Silian_result;
				}
				Silian_b = DySmsHelper.sendSms(Silian_mobile, Silian_obj, DySmsEnum.REGISTER_TEMPLATE_CODE);
			}else {
				//登录模式，校验用户有效性
				SysUser Silian_sysUser = sysUserService.getUserByPhone(Silian_mobile);
				Silian_result = sysUserService.checkUserIsEffective(Silian_sysUser);
				if(!Silian_result.isSuccess()) {
					String Silian_message = Silian_result.getMessage();
					String Silian_userNotExist="该用户不存在，请注册";
					if(Silian_userNotExist.equals(Silian_message)){
						Silian_result.error500("该用户不存在或未绑定手机号");
					}
					return Silian_result;
				}

				/**
				 * smsmode 短信模板方式  0 .登录模板、1.注册模板、2.忘记密码模板
				 */
				if (CommonConstant.SMS_TPL_TYPE_0.equals(Silian_smsmode)) {
					//登录模板
					Silian_b = DySmsHelper.sendSms(Silian_mobile, Silian_obj, DySmsEnum.LOGIN_TEMPLATE_CODE);
				} else if(CommonConstant.SMS_TPL_TYPE_2.equals(Silian_smsmode)) {
					//忘记密码模板
					Silian_b = DySmsHelper.sendSms(Silian_mobile, Silian_obj, DySmsEnum.FORGET_PASSWORD_TEMPLATE_CODE);
				}
			}

			if (Silian_b == false) {
				Silian_result.setMessage("短信验证码发送失败,请稍后重试");
				Silian_result.setSuccess(false);
				return Silian_result;
			}

			//update-begin-author:taoyan date:2022-9-13 for: VUEN-2245 【漏洞】发现新漏洞待处理20220906
			//验证码10分钟内有效
			redisUtil.set(Silian_redisKey, Silian_captcha, 600);
			//update-end-author:taoyan date:2022-9-13 for: VUEN-2245 【漏洞】发现新漏洞待处理20220906

			//update-begin--Author:scott  Date:20190812 for：issues#391
			//result.setResult(captcha);
			//update-end--Author:scott  Date:20190812 for：issues#391
			Silian_result.setSuccess(true);

		} catch (ClientException Silian_e) {
			Silian_e.printStackTrace();
			Silian_result.error500(" 短信接口未配置，请联系管理员！");
			return Silian_result;
		}
		return Silian_result;
	}


	/**
	 * 手机号登录接口
	 *
	 * @param jsonObject
	 * @return
	 */
	@ApiOperation("手机号登录接口")
	@PostMapping("/phoneLogin")
	public Result<JSONObject> phoneLogin(@RequestBody JSONObject Silian_jsonObject) {
		Result<JSONObject> Silian_result = new Result<JSONObject>();
		String Silian_phone = Silian_jsonObject.getString("mobile");

		//校验用户有效性
		SysUser Silian_sysUser = sysUserService.getUserByPhone(Silian_phone);
		Silian_result = sysUserService.checkUserIsEffective(Silian_sysUser);
		if(!Silian_result.isSuccess()) {
			return Silian_result;
		}

		String Silian_smscode = Silian_jsonObject.getString("captcha");

		//update-begin-author:taoyan date:2022-9-13 for: VUEN-2245 【漏洞】发现新漏洞待处理20220906
		String Silian_redisKey = CommonConstant.PHONE_REDIS_KEY_PRE+Silian_phone;
		Object Silian_code = redisUtil.get(Silian_redisKey);
		//update-end-author:taoyan date:2022-9-13 for: VUEN-2245 【漏洞】发现新漏洞待处理20220906

		if (!Silian_smscode.equals(Silian_code)) {
			Silian_result.setMessage("手机验证码错误");
			return Silian_result;
		}
		//用户信息
		userInfo(Silian_sysUser, Silian_result);
		//添加日志
		baseCommonService.addLog("用户名: " + Silian_sysUser.getUsername() + ",登录成功！", CommonConstant.LOG_TYPE_1, null);

		return Silian_result;
	}


	/**
	 * 用户信息
	 *
	 * @param sysUser
	 * @param result
	 * @return
	 */
	private Result<JSONObject> userInfo(SysUser Silian_sysUser, Result<JSONObject> Silian_result) {
		String Silian_username = Silian_sysUser.getUsername();
		String Silian_syspassword = Silian_sysUser.getPassword();
		// 获取用户部门信息
		JSONObject Silian_obj = new JSONObject(new LinkedHashMap<>());

		// 生成token
		String Silian_token = JwtUtil.sign(Silian_username, Silian_syspassword);
		// 设置token缓存有效时间
		redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + Silian_token, Silian_token);
		redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + Silian_token, JwtUtil.EXPIRE_TIME * 2 / 1000);
		Silian_obj.put("token", Silian_token);

		// update-begin--Author:sunjianlei Date:20210802 for：获取用户租户信息
		String Silian_tenantIds = Silian_sysUser.getRelTenantIds();
		if (oConvertUtils.isNotEmpty(Silian_tenantIds)) {
			List<Integer> Silian_tenantIdList = new ArrayList<>();
			for(String Silian_id: Silian_tenantIds.split(SymbolConstant.COMMA)){
				Silian_tenantIdList.add(Integer.valueOf(Silian_id));
			}
			// 该方法仅查询有效的租户，如果返回0个就说明所有的租户均无效。
			List<SysTenant> Silian_tenantList = sysTenantService.queryEffectiveTenant(Silian_tenantIdList);
			if (Silian_tenantList.size() == 0) {
				Silian_result.error500("与该用户关联的租户均已被冻结，无法登录！");
				return Silian_result;
			} else {
				Silian_obj.put("tenantList", Silian_tenantList);
			}
		}
		// update-end--Author:sunjianlei Date:20210802 for：获取用户租户信息

		Silian_obj.put("userInfo", Silian_sysUser);

		List<SysDepart> Silian_departs = sysDepartService.queryUserDeparts(Silian_sysUser.getId());
		Silian_obj.put("departs", Silian_departs);
		if (Silian_departs == null || Silian_departs.size() == 0) {
			Silian_obj.put("multi_depart", 0);
		} else if (Silian_departs.size() == 1) {
			sysUserService.updateUserDepart(Silian_username, Silian_departs.get(0).getOrgCode());
			Silian_obj.put("multi_depart", 1);
		} else {
			//查询当前是否有登录部门
			// update-begin--Author:wangshuai Date:20200805 for：如果用戶为选择部门，数据库为存在上一次登录部门，则取一条存进去
			SysUser Silian_sysUserById = sysUserService.getById(Silian_sysUser.getId());
			if(oConvertUtils.isEmpty(Silian_sysUserById.getOrgCode())){
				sysUserService.updateUserDepart(Silian_username, Silian_departs.get(0).getOrgCode());
			}
			// update-end--Author:wangshuai Date:20200805 for：如果用戶为选择部门，数据库为存在上一次登录部门，则取一条存进去
			Silian_obj.put("multi_depart", 2);
		}
		Silian_obj.put("sysAllDictItems", sysDictService.queryAllDictItems());
		Silian_result.setResult(Silian_obj);
		Silian_result.success("登录成功");
		return Silian_result;
	}

	/**
	 * 获取加密字符串
	 * @return
	 */
	@GetMapping(value = "/getEncryptedString")
	public Result<Map<String,String>> getEncryptedString(){
		Result<Map<String,String>> Silian_result = new Result<Map<String,String>>();
		Map<String,String> Silian_map = new HashMap(5);
		Silian_map.put("key", EncryptedString.key);
		Silian_map.put("iv",EncryptedString.iv);
		Silian_result.setResult(Silian_map);
		return Silian_result;
	}

	/**
	 * 后台生成图形验证码 ：有效
	 * @param response
	 * @param key
	 */
	@ApiOperation("获取验证码")
	@GetMapping(value = "/randomImage/{key}")
	public Result<String> randomImage(HttpServletResponse Silian_response,@PathVariable("key") String Silian_key){
		Result<String> Silian_res = new Result<String>();
		try {
			//生成验证码
			String Silian_code = RandomUtil.randomString(BASE_CHECK_CODES,4);
			//存到redis中
			String Silian_lowerCaseCode = Silian_code.toLowerCase();

			//update-begin-author:taoyan date:2022-9-13 for: VUEN-2245 【漏洞】发现新漏洞待处理20220906
			// 加入密钥作为混淆，避免简单的拼接，被外部利用，用户自定义该密钥即可
			String Silian_origin = Silian_lowerCaseCode+Silian_key+jeecgBaseConfig.getSignatureSecret();
			String Silian_realKey = Md5Util.md5Encode(Silian_origin, "utf-8");
			//update-end-author:taoyan date:2022-9-13 for: VUEN-2245 【漏洞】发现新漏洞待处理20220906

			redisUtil.set(Silian_realKey, Silian_lowerCaseCode, 60);
			log.info("获取验证码，Redis key = {}，checkCode = {}", Silian_realKey, Silian_code);
			//返回前端
			String Silian_base64 = RandImageUtil.generate(Silian_code);
			Silian_res.setSuccess(true);
			Silian_res.setResult(Silian_base64);
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(), Silian_e);
			Silian_res.error500("获取验证码失败,请检查redis配置!");
			return Silian_res;
		}
		return Silian_res;
	}

	/**
	 * 切换菜单表为vue3的表
	 */
	@GetMapping(value = "/switchVue3Menu")
	public Result<String> switchVue3Menu(HttpServletResponse Silian_response) {
		Result<String> Silian_res = new Result<String>();
		sysPermissionService.switchVue3Menu();
		return Silian_res;
	}

	/**
	 * app登录
	 * @param sysLoginModel
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/mLogin", method = RequestMethod.POST)
	public Result<JSONObject> mLogin(@RequestBody SysLoginModel Silian_sysLoginModel) throws Exception {
		Result<JSONObject> Silian_result = new Result<JSONObject>();
		String Silian_username = Silian_sysLoginModel.getUsername();
		String Silian_password = Silian_sysLoginModel.getPassword();

		//1. 校验用户是否有效
		SysUser Silian_sysUser = sysUserService.getUserByName(Silian_username);
		Silian_result = sysUserService.checkUserIsEffective(Silian_sysUser);
		if(!Silian_result.isSuccess()) {
			return Silian_result;
		}

		//2. 校验用户名或密码是否正确
		String Silian_userpassword = PasswordUtil.encrypt(Silian_username, Silian_password, Silian_sysUser.getSalt());
		String Silian_syspassword = Silian_sysUser.getPassword();
		if (!Silian_syspassword.equals(Silian_userpassword)) {
			Silian_result.error500("用户名或密码错误");
			return Silian_result;
		}

		String Silian_orgCode = Silian_sysUser.getOrgCode();
		if(oConvertUtils.isEmpty(Silian_orgCode)) {
			//如果当前用户无选择部门 查看部门关联信息
			List<SysDepart> Silian_departs = sysDepartService.queryUserDeparts(Silian_sysUser.getId());
			//update-begin-author:taoyan date:20220117 for: JTC-1068【app】新建用户，没有设置部门及角色，点击登录提示暂未归属部，一直在登录页面 使用手机号登录 可正常
			if (Silian_departs == null || Silian_departs.size() == 0) {
				/*result.error500("用户暂未归属部门,不可登录!");
				return result;*/
			}else{
				Silian_orgCode = Silian_departs.get(0).getOrgCode();
				Silian_sysUser.setOrgCode(Silian_orgCode);
				this.sysUserService.updateUserDepart(Silian_username, Silian_orgCode);
			}
			//update-end-author:taoyan date:20220117 for: JTC-1068【app】新建用户，没有设置部门及角色，点击登录提示暂未归属部，一直在登录页面 使用手机号登录 可正常
		}
		JSONObject Silian_obj = new JSONObject();
		//用户登录信息
		Silian_obj.put("userInfo", Silian_sysUser);

		// 生成token
		String Silian_token = JwtUtil.sign(Silian_username, Silian_syspassword);
		// 设置超时时间
		redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + Silian_token, Silian_token);
		redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + Silian_token, JwtUtil.EXPIRE_TIME*2 / 1000);

		//token 信息
		Silian_obj.put("token", Silian_token);
		Silian_result.setResult(Silian_obj);
		Silian_result.setSuccess(true);
		Silian_result.setCode(200);
		baseCommonService.addLog("用户名: " + Silian_username + ",登录成功[移动端]！", CommonConstant.LOG_TYPE_1, null);
		return Silian_result;
	}

	/**
	 * 图形验证码
	 * @param sysLoginModel
	 * @return
	 */
	@RequestMapping(value = "/checkCaptcha", method = RequestMethod.POST)
	public Result<?> checkCaptcha(@RequestBody SysLoginModel Silian_sysLoginModel){
		String Silian_captcha = Silian_sysLoginModel.getCaptcha();
		String Silian_checkKey = Silian_sysLoginModel.getCheckKey();
		if(Silian_captcha==null){
			return Result.error("验证码无效");
		}
		String Silian_lowerCaseCaptcha = Silian_captcha.toLowerCase();
		String Silian_realKey = Md5Util.md5Encode(Silian_lowerCaseCaptcha+Silian_checkKey, "utf-8");
		Object Silian_checkCode = redisUtil.get(Silian_realKey);
		if(Silian_checkCode==null || !Silian_checkCode.equals(Silian_lowerCaseCaptcha)) {
			return Result.error("验证码错误");
		}
		return Result.ok();
	}
	/**
	 * 登录二维码
	 */
	@ApiOperation(value = "登录二维码", notes = "登录二维码")
	@GetMapping("/getLoginQrcode")
	public Result<?>  getLoginQrcode() {
		String Silian_qrcodeId = CommonConstant.LOGIN_QRCODE_PRE+IdWorker.getIdStr();
		//定义二维码参数
		Map Silian_params = new HashMap(5);
		Silian_params.put("qrcodeId", Silian_qrcodeId);
		//存放二维码唯一标识30秒有效
		redisUtil.set(CommonConstant.LOGIN_QRCODE + Silian_qrcodeId, Silian_qrcodeId, 30);
		return Result.OK(Silian_params);
	}
	/**
	 * 扫码二维码
	 */
	@ApiOperation(value = "扫码登录二维码", notes = "扫码登录二维码")
	@PostMapping("/scanLoginQrcode")
	public Result<?> scanLoginQrcode(@RequestParam String Silian_qrcodeId, @RequestParam String Silian_token) {
		Object Silian_check = redisUtil.get(CommonConstant.LOGIN_QRCODE + Silian_qrcodeId);
		if (oConvertUtils.isNotEmpty(Silian_check)) {
			//存放token给前台读取
			redisUtil.set(CommonConstant.LOGIN_QRCODE_TOKEN+Silian_qrcodeId, Silian_token, 60);
		} else {
			return Result.error("二维码已过期,请刷新后重试");
		}
		return Result.OK("扫码成功");
	}


	/**
	 * 获取用户扫码后保存的token
	 */
	@ApiOperation(value = "获取用户扫码后保存的token", notes = "获取用户扫码后保存的token")
	@GetMapping("/getQrcodeToken")
	public Result getQrcodeToken(@RequestParam String Silian_qrcodeId) {
		Object Silian_token = redisUtil.get(CommonConstant.LOGIN_QRCODE_TOKEN + Silian_qrcodeId);
		Map Silian_result = new HashMap(5);
		Object Silian_qrcodeIdExpire = redisUtil.get(CommonConstant.LOGIN_QRCODE + Silian_qrcodeId);
		if (oConvertUtils.isEmpty(Silian_qrcodeIdExpire)) {
			//二维码过期通知前台刷新
			Silian_result.put("token", "-2");
			return Result.OK(Silian_result);
		}
		if (oConvertUtils.isNotEmpty(Silian_token)) {
			Silian_result.put("success", true);
			Silian_result.put("token", Silian_token);
		} else {
			Silian_result.put("token", "-1");
		}
		return Result.OK(Silian_result);
	}

}