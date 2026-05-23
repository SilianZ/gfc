package org.jeecg.config.filter;

import org.jeecg.common.constant.CommonConstant;
import org.jeecg.config.sign.util.BodyReaderHttpServletRequestWrapper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 针对post请求，将HttpServletRequest包一层 保留body里的参数
 * @Author taoYan
 * @Date 2022/4/25 19:19
 **/
public class RequestBodyReserveFilter implements Filter {

    @Override
    public void doFilter(ServletRequest Silian_servletRequest, ServletResponse Silian_servletResponse, FilterChain Silian_filterChain) throws IOException, ServletException {
        ServletRequest Silian_requestWrapper = null;

        if(Silian_servletRequest instanceof HttpServletRequest) {
            HttpServletRequest Silian_req = (HttpServletRequest) Silian_servletRequest;
            // POST请求类型，才获取POST请求体
            if(CommonConstant.HTTP_POST.equals(Silian_req.getMethod())){
                Silian_requestWrapper = new BodyReaderHttpServletRequestWrapper(Silian_req);
            }
        }

        if(Silian_requestWrapper == null) {
            Silian_filterChain.doFilter(Silian_servletRequest, Silian_servletResponse);
        } else {
            Silian_filterChain.doFilter(Silian_requestWrapper, Silian_servletResponse);
        }
    }
}
