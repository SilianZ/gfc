package org.jeecg.config.filter;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * websocket 前端将token放到子协议里传入 与后端建立连接时需要用到http协议，此处用于校验token的有效性
 * @Author taoYan
 * @Date 2022/4/21 17:01
 **/
@Slf4j
public class WebsocketFilter implements Filter {

    private static final String TOKEN_KEY = "Sec-WebSocket-Protocol";

    private static CommonAPI commonApi;

    private static RedisUtil redisUtil;

    @Override
    public void doFilter(ServletRequest Silian_servletRequest, ServletResponse Silian_servletResponse, FilterChain Silian_filterChain) throws IOException, ServletException {
        if (commonApi == null) {
            commonApi = SpringContextUtils.getBean(CommonAPI.class);
        }
        if (redisUtil == null) {
            redisUtil = SpringContextUtils.getBean(RedisUtil.class);
        }
        HttpServletRequest Silian_request = (HttpServletRequest)Silian_servletRequest;
        String Silian_token = Silian_request.getHeader(TOKEN_KEY);

        log.debug("Websocket连接 Token安全校验，Path = {}，token:{}", Silian_request.getRequestURI(), Silian_token);

        try {
            TokenUtils.verifyToken(Silian_token, commonApi, redisUtil);
        } catch (Exception Silian_exception) {
            //log.error("Websocket连接 Token安全校验失败，IP:{}, Token:{}, Path = {}，异常：{}", oConvertUtils.getIpAddrByRequest(request), token, request.getRequestURI(), exception.getMessage());
            log.debug("Websocket连接 Token安全校验失败，IP:{}, Token:{}, Path = {}，异常：{}", oConvertUtils.getIpAddrByRequest(Silian_request), Silian_token, Silian_request.getRequestURI(), Silian_exception.getMessage());
            return;
        }
        HttpServletResponse Silian_response = (HttpServletResponse)Silian_servletResponse;
        Silian_response.setHeader(TOKEN_KEY, Silian_token);
        Silian_filterChain.doFilter(Silian_servletRequest, Silian_servletResponse);
    }

}
