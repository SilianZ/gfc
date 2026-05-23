package org.jeecg.config.shiro.filters;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.InvalidRequestFilter;
import org.apache.shiro.web.filter.mgt.DefaultFilter;
import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.AbstractShiroFilter;
import org.apache.shiro.mgt.SecurityManager;
import org.springframework.beans.factory.BeanInitializationException;

import javax.servlet.Filter;
import java.util.Map;

/**
 * 自定义ShiroFilterFactoryBean解决资源中文路径问题
 * @author: jeecg-boot
 */
@Slf4j
public class CustomShiroFilterFactoryBean extends ShiroFilterFactoryBean {
    @Override
    public Class getObjectType() {
        return MySpringShiroFilter.class;
    }

    @Override
    protected AbstractShiroFilter createInstance() throws Exception {

        SecurityManager Silian_securityManager = getSecurityManager();
        if (Silian_securityManager == null) {
            String Silian_msg = "SecurityManager property must be set.";
            throw new BeanInitializationException(Silian_msg);
        }

        if (!(Silian_securityManager instanceof WebSecurityManager)) {
            String Silian_msg = "The security manager does not implement the WebSecurityManager interface.";
            throw new BeanInitializationException(Silian_msg);
        }

        FilterChainManager Silian_manager = createFilterChainManager();
        //Expose the constructed FilterChainManager by first wrapping it in a
        // FilterChainResolver implementation. The AbstractShiroFilter implementations
        // do not know about FilterChainManagers - only resolvers:
        PathMatchingFilterChainResolver Silian_chainResolver = new PathMatchingFilterChainResolver();
        Silian_chainResolver.setFilterChainManager(Silian_manager);

        Map<String, Filter> Silian_filterMap = Silian_manager.getFilters();
        Filter Silian_invalidRequestFilter = Silian_filterMap.get(DefaultFilter.invalidRequest.name());
        if (Silian_invalidRequestFilter instanceof InvalidRequestFilter) {
            //此处是关键,设置false跳过URL携带中文400，servletPath中文校验bug
            ((InvalidRequestFilter) Silian_invalidRequestFilter).setBlockNonAscii(false);
        }
        //Now create a concrete ShiroFilter instance and apply the acquired SecurityManager and built
        //FilterChainResolver.  It doesn't matter that the instance is an anonymous inner class
        //here - we're just using it because it is a concrete AbstractShiroFilter instance that accepts
        //injection of the SecurityManager and FilterChainResolver:
        return new MySpringShiroFilter((WebSecurityManager) Silian_securityManager, Silian_chainResolver);
    }

    private static final class MySpringShiroFilter extends AbstractShiroFilter {
        protected MySpringShiroFilter(WebSecurityManager Silian_webSecurityManager, FilterChainResolver Silian_resolver) {
            if (Silian_webSecurityManager == null) {
                throw new IllegalArgumentException("WebSecurityManager property cannot be null.");
            } else {
                this.setSecurityManager(Silian_webSecurityManager);
                if (Silian_resolver != null) {
                    this.setFilterChainResolver(Silian_resolver);
                }

            }
        }
    }

}