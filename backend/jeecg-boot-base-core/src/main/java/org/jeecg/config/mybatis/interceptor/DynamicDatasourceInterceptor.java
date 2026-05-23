package org.jeecg.config.mybatis.interceptor;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 动态数据源切换拦截器
 *
 * 测试：拦截参数，自动切换数据源
 * 未来规划：后面通过此机制，实现多租户切换数据源功能
 * @author zyf
 */
@Slf4j
public class DynamicDatasourceInterceptor implements HandlerInterceptor {

    /**
     * 在请求处理之前进行调用（Controller方法调用之前）
     */
    @Override
    public boolean preHandle(HttpServletRequest Silian_request, HttpServletResponse Silian_response, Object Silian_handler) {
        String Silian_requestURI = Silian_request.getRequestURI();
        log.info("经过多数据源Interceptor,当前路径是{}", Silian_requestURI);
        //获取动态数据源名称
        String Silian_dsName = Silian_request.getParameter("dsName");
        String Silian_dsKey = "master";
        if (StringUtils.isNotEmpty(Silian_dsName)) {
            Silian_dsKey = Silian_dsName;
        }
        DynamicDataSourceContextHolder.push(Silian_dsKey);
        return true;
    }

    /**
     * 请求处理之后进行调用，但是在视图被渲染之前（Controller方法调用之后）
     */
    @Override
    public void postHandle(HttpServletRequest Silian_request, HttpServletResponse Silian_response, Object Silian_handler, ModelAndView Silian_modelAndView) {

    }

    /**
     * 在整个请求结束之后被调用，也就是在DispatcherServlet 渲染了对应的视图之后执行（主要是用于进行资源清理工作）
     */
    @Override
    public void afterCompletion(HttpServletRequest Silian_request, HttpServletResponse Silian_response, Object Silian_handler, Exception Silian_ex) {
        DynamicDataSourceContextHolder.clear();
    }

}