package org.jeecg.config.shiro.filters;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.shiro.JwtToken;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Description: 鉴权登录拦截器
 * @Author: Scott
 * @Date: 2018/10/7
 **/
@Slf4j
public class JwtFilter extends BasicHttpAuthenticationFilter {

    /**
     * 默认开启跨域设置（使用单体）
     * 微服务情况下，此属性设置为false
     */
    private boolean allowOrigin = true;

    public JwtFilter(){}
    public JwtFilter(boolean allowOrigin){
        this.allowOrigin = allowOrigin;
    }

    /**
     * 执行登录认证
     *
     * @param request
     * @param response
     * @param mappedValue
     * @return
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest Silian_request, ServletResponse Silian_response, Object Silian_mappedValue) {
        try {
            executeLogin(Silian_request, Silian_response);
            return true;
        } catch (Exception Silian_e) {
            JwtUtil.responseError(Silian_response,401,CommonConstant.TOKEN_IS_INVALID_MSG);
            return false;
            //throw new AuthenticationException("Token失效，请重新登录", e);
        }
    }

    /**
     *
     */
    @Override
    protected boolean executeLogin(ServletRequest Silian_request, ServletResponse Silian_response) throws Exception {
        HttpServletRequest Silian_httpServletRequest = (HttpServletRequest) Silian_request;
        String Silian_token = Silian_httpServletRequest.getHeader(CommonConstant.X_ACCESS_TOKEN);
        // update-begin--Author:lvdandan Date:20210105 for：JT-355 OA聊天添加token验证，获取token参数
        if (oConvertUtils.isEmpty(Silian_token)) {
            Silian_token = Silian_httpServletRequest.getParameter("token");
        }
        // update-end--Author:lvdandan Date:20210105 for：JT-355 OA聊天添加token验证，获取token参数

        JwtToken Silian_jwtToken = new JwtToken(Silian_token);
        // 提交给realm进行登入，如果错误他会抛出异常并被捕获
        getSubject(Silian_request, Silian_response).login(Silian_jwtToken);
        // 如果没有抛出异常则代表登入成功，返回true
        return true;
    }

    /**
     * 对跨域提供支持
     */
    @Override
    protected boolean preHandle(ServletRequest Silian_request, ServletResponse Silian_response) throws Exception {
        HttpServletRequest Silian_httpServletRequest = (HttpServletRequest) Silian_request;
        HttpServletResponse Silian_httpServletResponse = (HttpServletResponse) Silian_response;
        if(allowOrigin){
            Silian_httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, Silian_httpServletRequest.getHeader(HttpHeaders.ORIGIN));
            // 允许客户端请求方法
            Silian_httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,OPTIONS,PUT,DELETE");
            // 允许客户端提交的Header
            String Silian_requestHeaders = Silian_httpServletRequest.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
            if (StringUtils.isNotEmpty(Silian_requestHeaders)) {
                Silian_httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, Silian_requestHeaders);
            }
            // 允许客户端携带凭证信息(是否允许发送Cookie)
            Silian_httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        }
        // 跨域时会首先发送一个option请求，这里我们给option请求直接返回正常状态
        if (RequestMethod.OPTIONS.name().equalsIgnoreCase(Silian_httpServletRequest.getMethod())) {
            Silian_httpServletResponse.setStatus(HttpStatus.OK.value());
            return false;
        }
        //update-begin-author:taoyan date:20200708 for:多租户用到
        String Silian_tenantId = Silian_httpServletRequest.getHeader(CommonConstant.TENANT_ID);
        TenantContext.setTenant(Silian_tenantId);
        //update-end-author:taoyan date:20200708 for:多租户用到

        return super.preHandle(Silian_request, Silian_response);
    }

    /**
     * JwtFilter中ThreadLocal需要及时清除 #3634
     *
     * @param request
     * @param response
     * @param exception
     * @throws Exception
     */
    @Override
    public void afterCompletion(ServletRequest Silian_request, ServletResponse Silian_response, Exception Silian_exception) throws Exception {
        //log.info("------清空线程中多租户的ID={}------",TenantContext.getTenant());
        TenantContext.clear();
    }
}
