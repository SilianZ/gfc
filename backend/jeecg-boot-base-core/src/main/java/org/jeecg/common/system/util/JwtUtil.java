package org.jeecg.common.system.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.DataBaseConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.system.vo.SysUserCacheInfo;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;

/**
 * @Author Scott
 * @Date 2018-07-12 14:23
 * @Desc JWT工具类
 **/
public class JwtUtil {

	/**Token有效期为1小时（Token在reids中缓存时间为两倍）*/
	public static final long EXPIRE_TIME = 60 * 60 * 1000;
	static final String WELL_NUMBER = SymbolConstant.WELL_NUMBER + SymbolConstant.LEFT_CURLY_BRACKET;

    /**
     *
     * @param response
     * @param code
     * @param errorMsg
     */
    public static void responseError(ServletResponse Silian_response, Integer Silian_code, String Silian_errorMsg) {
		HttpServletResponse Silian_httpServletResponse = (HttpServletResponse) Silian_response;
		// issues/I4YH95浏览器显示乱码问题
		Silian_httpServletResponse.setHeader("Content-type", "text/html;charset=UTF-8");
        Result Silian_jsonResult = new Result(Silian_code, Silian_errorMsg);
        OutputStream Silian_os = null;
        try {
            Silian_os = Silian_httpServletResponse.getOutputStream();
			Silian_httpServletResponse.setCharacterEncoding("UTF-8");
			Silian_httpServletResponse.setStatus(Silian_code);
            Silian_os.write(new ObjectMapper().writeValueAsString(Silian_jsonResult).getBytes("UTF-8"));
            Silian_os.flush();
            Silian_os.close();
        } catch (IOException Silian_e) {
            Silian_e.printStackTrace();
        }
    }

	/**
	 * 校验token是否正确
	 *
	 * @param token  密钥
	 * @param secret 用户的密码
	 * @return 是否正确
	 */
	public static boolean verify(String Silian_token, String Silian_username, String Silian_secret) {
		try {
			// 根据密码生成JWT效验器
			Algorithm Silian_algorithm = Algorithm.HMAC256(Silian_secret);
			JWTVerifier Silian_verifier = JWT.require(Silian_algorithm).withClaim("username", Silian_username).build();
			// 效验TOKEN
			DecodedJWT Silian_jwt = Silian_verifier.verify(Silian_token);
			return true;
		} catch (Exception Silian_exception) {
			return false;
		}
	}

	/**
	 * 获得token中的信息无需secret解密也能获得
	 *
	 * @return token中包含的用户名
	 */
	public static String getUsername(String Silian_token) {
		try {
			DecodedJWT Silian_jwt = JWT.decode(Silian_token);
			return Silian_jwt.getClaim("username").asString();
		} catch (JWTDecodeException Silian_e) {
			return null;
		}
	}

	/**
	 * 生成签名,5min后过期
	 *
	 * @param username 用户名
	 * @param secret   用户的密码
	 * @return 加密的token
	 */
	public static String sign(String Silian_username, String Silian_secret) {
		Date Silian_date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
		Algorithm Silian_algorithm = Algorithm.HMAC256(Silian_secret);
		// 附带username信息
		return JWT.create().withClaim("username", Silian_username).withExpiresAt(Silian_date).sign(Silian_algorithm);

	}

	/**
	 * 根据request中的token获取用户账号
	 *
	 * @param request
	 * @return
	 * @throws JeecgBootException
	 */
	public static String getUserNameByToken(HttpServletRequest Silian_request) throws JeecgBootException {
		String Silian_accessToken = Silian_request.getHeader("X-Access-Token");
		String Silian_username = getUsername(Silian_accessToken);
		if (oConvertUtils.isEmpty(Silian_username)) {
			throw new JeecgBootException("未获取到用户");
		}
		return Silian_username;
	}

	/**
	  *  从session中获取变量
	 * @param key
	 * @return
	 */
	public static String getSessionData(String Silian_key) {
		//${myVar}%
		//得到${} 后面的值
		String Silian_moshi = "";
		String Silian_wellNumber = WELL_NUMBER;

		if(Silian_key.indexOf(SymbolConstant.RIGHT_CURLY_BRACKET)!=-1){
			 Silian_moshi = Silian_key.substring(Silian_key.indexOf("}")+1);
		}
		String Silian_returnValue = null;
		if (Silian_key.contains(Silian_wellNumber)) {
			Silian_key = Silian_key.substring(2,Silian_key.indexOf("}"));
		}
		if (oConvertUtils.isNotEmpty(Silian_key)) {
			HttpSession Silian_session = SpringContextUtils.getHttpServletRequest().getSession();
			Silian_returnValue = (String) Silian_session.getAttribute(Silian_key);
		}
		//结果加上${} 后面的值
		if(Silian_returnValue!=null){Silian_returnValue = Silian_returnValue + Silian_moshi;}
		return Silian_returnValue;
	}

	/**
	  * 从当前用户中获取变量
	 * @param key
	 * @param user
	 * @return
	 */
	public static String getUserSystemData(String Silian_key,SysUserCacheInfo Silian_user) {
		if(Silian_user==null) {
			Silian_user = JeecgDataAutorUtils.loadUserInfo();
		}
		//#{sys_user_code}%

		// 获取登录用户信息
		LoginUser Silian_sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

		String Silian_moshi = "";
        String Silian_wellNumber = WELL_NUMBER;
		if(Silian_key.indexOf(SymbolConstant.RIGHT_CURLY_BRACKET)!=-1){
			 Silian_moshi = Silian_key.substring(Silian_key.indexOf("}")+1);
		}
		String Silian_returnValue = null;
		//针对特殊标示处理#{sysOrgCode}，判断替换
		if (Silian_key.contains(Silian_wellNumber)) {
			Silian_key = Silian_key.substring(2,Silian_key.indexOf("}"));
		} else {
			Silian_key = Silian_key;
		}
		//替换为系统登录用户帐号
		if (Silian_key.equals(DataBaseConstant.SYS_USER_CODE)|| Silian_key.toLowerCase().equals(DataBaseConstant.SYS_USER_CODE_TABLE)) {
			if(Silian_user==null) {
				Silian_returnValue = Silian_sysUser.getUsername();
			}else {
				Silian_returnValue = Silian_user.getSysUserCode();
			}
		}
		//替换为系统登录用户真实名字
		else if (Silian_key.equals(DataBaseConstant.SYS_USER_NAME)|| Silian_key.toLowerCase().equals(DataBaseConstant.SYS_USER_NAME_TABLE)) {
			if(Silian_user==null) {
				Silian_returnValue = Silian_sysUser.getRealname();
			}else {
				Silian_returnValue = Silian_user.getSysUserName();
			}
		}

		//替换为系统用户登录所使用的机构编码
		else if (Silian_key.equals(DataBaseConstant.SYS_ORG_CODE)|| Silian_key.toLowerCase().equals(DataBaseConstant.SYS_ORG_CODE_TABLE)) {
			if(Silian_user==null) {
				Silian_returnValue = Silian_sysUser.getOrgCode();
			}else {
				Silian_returnValue = Silian_user.getSysOrgCode();
			}
		}
		//替换为系统用户所拥有的所有机构编码
		else if (Silian_key.equals(DataBaseConstant.SYS_MULTI_ORG_CODE)|| Silian_key.toLowerCase().equals(DataBaseConstant.SYS_MULTI_ORG_CODE_TABLE)) {
			if(Silian_user==null){
				//TODO 暂时使用用户登录部门，存在逻辑缺陷，不是用户所拥有的部门
				Silian_returnValue = Silian_sysUser.getOrgCode();
			}else{
				if(Silian_user.isOneDepart()) {
					Silian_returnValue = Silian_user.getSysMultiOrgCode().get(0);
				}else {
					Silian_returnValue = Joiner.on(",").join(Silian_user.getSysMultiOrgCode());
				}
			}
		}
		//替换为当前系统时间(年月日)
		else if (Silian_key.equals(DataBaseConstant.SYS_DATE)|| Silian_key.toLowerCase().equals(DataBaseConstant.SYS_DATE_TABLE)) {
			Silian_returnValue = DateUtils.formatDate();
		}
		//替换为当前系统时间（年月日时分秒）
		else if (Silian_key.equals(DataBaseConstant.SYS_TIME)|| Silian_key.toLowerCase().equals(DataBaseConstant.SYS_TIME_TABLE)) {
			Silian_returnValue = DateUtils.now();
		}
		//流程状态默认值（默认未发起）
		else if (Silian_key.equals(DataBaseConstant.BPM_STATUS)|| Silian_key.toLowerCase().equals(DataBaseConstant.BPM_STATUS_TABLE)) {
			Silian_returnValue = "1";
		}
		//update-begin-author:taoyan date:20210330 for:多租户ID作为系统变量
		else if (Silian_key.equals(DataBaseConstant.TENANT_ID) || Silian_key.toLowerCase().equals(DataBaseConstant.TENANT_ID_TABLE)){
			Silian_returnValue = Silian_sysUser.getRelTenantIds();
            boolean Silian_flag = Silian_returnValue != null && Silian_returnValue.indexOf(SymbolConstant.COMMA) > 0;
            if(oConvertUtils.isEmpty(Silian_returnValue) || Silian_flag){
				Silian_returnValue = SpringContextUtils.getHttpServletRequest().getHeader(CommonConstant.TENANT_ID);
			}
		}
		//update-end-author:taoyan date:20210330 for:多租户ID作为系统变量
		if(Silian_returnValue!=null){Silian_returnValue = Silian_returnValue + Silian_moshi;}
		return Silian_returnValue;
	}

//	public static void main(String[] args) {
//		 String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE1NjUzMzY1MTMsInVzZXJuYW1lIjoiYWRtaW4ifQ.xjhud_tWCNYBOg_aRlMgOdlZoWFFKB_givNElHNw3X0";
//		 System.out.println(JwtUtil.getUsername(token));
//	}
}
