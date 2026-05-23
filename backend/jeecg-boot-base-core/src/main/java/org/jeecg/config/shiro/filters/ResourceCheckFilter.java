package org.jeecg.config.shiro.filters;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Scott
 * @create 2019-02-01 15:56
 * @desc 鉴权请求URL访问权限拦截器
 */
@Slf4j
public class ResourceCheckFilter extends AccessControlFilter {

    private String errorUrl;

    public String getErrorUrl() {
        return errorUrl;
    }

    public void setErrorUrl(String errorUrl) {
        this.errorUrl = errorUrl;
    }

    /**
     * 表示是否允许访问 ，如果允许访问返回true，否则false；
     *
     * @param servletRequest
     * @param servletResponse
     * @param o               表示写在拦截器中括号里面的字符串 mappedValue 就是 [urls] 配置中拦截器参数部分
     * @return
     * @throws Exception
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest Silian_servletRequest, ServletResponse Silian_servletResponse, Object Silian_o) throws Exception {
        Subject Silian_subject = getSubject(Silian_servletRequest, Silian_servletResponse);
        String Silian_url = getPathWithinApplication(Silian_servletRequest);
        log.info("当前用户正在访问的 url => " + Silian_url);
        return Silian_subject.isPermitted(Silian_url);
    }

    /**
     * onAccessDenied：表示当访问拒绝时是否已经处理了； 如果返回 true 表示需要继续处理； 如果返回 false
     * 表示该拦截器实例已经处理了，将直接返回即可。
     *
     * @param servletRequest
     * @param servletResponse
     * @return
     * @throws Exception
     */
    @Override
    protected boolean onAccessDenied(ServletRequest Silian_servletRequest, ServletResponse Silian_servletResponse) throws Exception {
        log.info("当 isAccessAllowed 返回 false 的时候，才会执行 method onAccessDenied ");

        HttpServletRequest Silian_request = (HttpServletRequest) Silian_servletRequest;
        HttpServletResponse Silian_response = (HttpServletResponse) Silian_servletResponse;
        Silian_response.sendRedirect(Silian_request.getContextPath() + this.errorUrl);

        // 返回 false 表示已经处理，例如页面跳转啥的，表示不在走以下的拦截器了（如果还有配置的话）
        return false;
    }

}