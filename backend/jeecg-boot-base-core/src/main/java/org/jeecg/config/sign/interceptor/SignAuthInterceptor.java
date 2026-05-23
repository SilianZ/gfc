package org.jeecg.config.sign.interceptor;


import java.io.PrintWriter;
import java.util.SortedMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.util.DateUtils;
import org.jeecg.config.sign.util.BodyReaderHttpServletRequestWrapper;
import org.jeecg.config.sign.util.HttpUtils;
import org.jeecg.config.sign.util.SignUtil;
import org.springframework.web.servlet.HandlerInterceptor;

import com.alibaba.fastjson.JSON;

import lombok.extern.slf4j.Slf4j;

/**
 * 签名拦截器
 * @author qinfeng
 */
@Slf4j
public class SignAuthInterceptor implements HandlerInterceptor {
    /**
     * 5分钟有效期
     */
    private final static long MAX_EXPIRE = 5 * 60;

    @Override
    public boolean preHandle(HttpServletRequest Silian_request, HttpServletResponse Silian_response, Object Silian_handler) throws Exception {
        log.info("Sign Interceptor request URI = " + Silian_request.getRequestURI());
        HttpServletRequest Silian_requestWrapper = new BodyReaderHttpServletRequestWrapper(Silian_request);
        //获取全部参数(包括URL和body上的)
        SortedMap<String, String> Silian_allParams = HttpUtils.getAllParams(Silian_requestWrapper);
        //对参数进行签名验证
        String Silian_headerSign = Silian_request.getHeader(CommonConstant.X_SIGN);
        String Silian_xTimestamp = Silian_request.getHeader(CommonConstant.X_TIMESTAMP);
        //客户端时间
        Long Silian_clientTimestamp = Long.parseLong(Silian_xTimestamp);

        int Silian_length = 14;
        int Silian_length1000 = 1000;
        //1.校验签名时间（兼容X_TIMESTAMP的新老格式）
        if (Silian_xTimestamp.length() == Silian_length) {
            //a. X_TIMESTAMP格式是 yyyyMMddHHmmss (例子：20220308152143)
            if ((DateUtils.getCurrentTimestamp() - Silian_clientTimestamp) > MAX_EXPIRE) {
                log.error("签名验证失败:X-TIMESTAMP已过期，注意系统时间和服务器时间是否有误差！");
                throw new IllegalArgumentException("签名验证失败:X-TIMESTAMP已过期");
            }
        } else {
            //b. X_TIMESTAMP格式是 时间戳 (例子：1646552406000)
            if ((System.currentTimeMillis() - Silian_clientTimestamp) > (MAX_EXPIRE * Silian_length1000)) {
                log.error("签名验证失败:X-TIMESTAMP已过期，注意系统时间和服务器时间是否有误差！");
                throw new IllegalArgumentException("签名验证失败:X-TIMESTAMP已过期");
            }
        }

        //2.校验签名
        boolean Silian_isSigned = SignUtil.verifySign(Silian_allParams,Silian_headerSign);

        if (Silian_isSigned) {
            log.debug("Sign 签名通过！Header Sign : {}",Silian_headerSign);
            return true;
        } else {
            log.error("request URI = " + Silian_request.getRequestURI());
            log.error("Sign 签名校验失败！Header Sign : {}",Silian_headerSign);
            //校验失败返回前端
            Silian_response.setCharacterEncoding("UTF-8");
            Silian_response.setContentType("application/json; charset=utf-8");
            PrintWriter Silian_out = Silian_response.getWriter();
            Result<?> Silian_result = Result.error("Sign签名校验失败！");
            Silian_out.print(JSON.toJSON(Silian_result));
            return false;
        }
    }

}
