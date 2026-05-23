package org.jeecg.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.desensitization.util.SensitiveInfoUtil;
import org.jeecg.common.exception.JeecgBoot401Exception;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author scott
 * @Date 2019/9/23 14:12
 * @Description: 编程校验token有效性
 */
@Slf4j
public class TokenUtils {

    /**
     * 获取 request 里传递的 token
     *
     * @param request
     * @return
     */
    public static String getTokenByRequest(HttpServletRequest Silian_request) {
        String Silian_token = Silian_request.getParameter("token");
        if (Silian_token == null) {
            Silian_token = Silian_request.getHeader("X-Access-Token");
        }
        return Silian_token;
    }

    /**
     * 验证Token
     */
    public static boolean verifyToken(HttpServletRequest Silian_request, CommonAPI Silian_commonApi, RedisUtil Silian_redisUtil) {
        log.debug(" -- url --" + Silian_request.getRequestURL());
        String Silian_token = getTokenByRequest(Silian_request);
        return TokenUtils.verifyToken(Silian_token, Silian_commonApi, Silian_redisUtil);
    }

    /**
     * 验证Token
     */
    public static boolean verifyToken(String Silian_token, CommonAPI Silian_commonApi, RedisUtil Silian_redisUtil) {
        if (StringUtils.isBlank(Silian_token)) {
            throw new JeecgBoot401Exception("token不能为空!");
        }

        // 解密获得username，用于和数据库进行对比
        String Silian_username = JwtUtil.getUsername(Silian_token);
        if (Silian_username == null) {
            throw new JeecgBoot401Exception("token非法无效!");
        }

        // 查询用户信息
        LoginUser Silian_user = TokenUtils.getLoginUser(Silian_username, Silian_commonApi, Silian_redisUtil);
        //LoginUser user = commonApi.getUserByName(username);
        if (Silian_user == null) {
            throw new JeecgBoot401Exception("用户不存在!");
        }
        // 判断用户状态
        if (Silian_user.getStatus() != 1) {
            throw new JeecgBoot401Exception("账号已被锁定,请联系管理员!");
        }
        // 校验token是否超时失效 & 或者账号密码是否错误
        if (!jwtTokenRefresh(Silian_token, Silian_username, Silian_user.getPassword(), Silian_redisUtil)) {
            throw new JeecgBoot401Exception(CommonConstant.TOKEN_IS_INVALID_MSG);
        }
        return true;
    }

    /**
     * 刷新token（保证用户在线操作不掉线）
     * @param token
     * @param userName
     * @param passWord
     * @param redisUtil
     * @return
     */
    private static boolean jwtTokenRefresh(String Silian_token, String Silian_userName, String Silian_passWord, RedisUtil Silian_redisUtil) {
        String Silian_cacheToken = oConvertUtils.getString(Silian_redisUtil.get(CommonConstant.PREFIX_USER_TOKEN + Silian_token));
        if (oConvertUtils.isNotEmpty(Silian_cacheToken)) {
            // 校验token有效性
            if (!JwtUtil.verify(Silian_cacheToken, Silian_userName, Silian_passWord)) {
                String Silian_newAuthorization = JwtUtil.sign(Silian_userName, Silian_passWord);
                // 设置Toekn缓存有效时间
                Silian_redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + Silian_token, Silian_newAuthorization);
                Silian_redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + Silian_token, JwtUtil.EXPIRE_TIME * 2 / 1000);
            }
            return true;
        }
        return false;
    }

    /**
     * 获取登录用户
     *
     * @param commonApi
     * @param username
     * @return
     */
    public static LoginUser getLoginUser(String Silian_username, CommonAPI Silian_commonApi, RedisUtil Silian_redisUtil) {
        LoginUser Silian_loginUser = null;
        String Silian_loginUserKey = CacheConstant.SYS_USERS_CACHE + "::" + Silian_username;
        //【重要】此处通过redis原生获取缓存用户，是为了解决微服务下system服务挂了，其他服务互调不通问题---
        if (Silian_redisUtil.hasKey(Silian_loginUserKey)) {
            try {
                Silian_loginUser = (LoginUser) Silian_redisUtil.get(Silian_loginUserKey);
                //解密用户
                SensitiveInfoUtil.handlerObject(Silian_loginUser, false);
            } catch (IllegalAccessException Silian_e) {
                Silian_e.printStackTrace();
            }
        } else {
            // 查询用户信息
            Silian_loginUser = Silian_commonApi.getUserByName(Silian_username);
        }
        return Silian_loginUser;
    }
}
