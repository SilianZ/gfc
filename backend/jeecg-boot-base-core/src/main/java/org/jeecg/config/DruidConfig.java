package org.jeecg.config;

import java.io.IOException;

import javax.servlet.*;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.alibaba.druid.spring.boot.autoconfigure.properties.DruidStatProperties;
import com.alibaba.druid.util.Utils;

/**
 * @Description: DruidConfig配置类
 * @author: jeecg-boot
 */
@Configuration
@AutoConfigureAfter(DruidDataSourceAutoConfigure.class)
public class DruidConfig {

    /**
     * 带有广告的common.js全路径，druid-1.1.14
     */
    private static final String FILE_PATH = "support/http/resources/js/common.js";
    /**
     * 原始脚本，触发构建广告的语句
     */
    private static final String ORIGIN_JS = "this.buildFooter();";
    /**
     * 替换后的脚本
     */
    private static final String NEW_JS = "//this.buildFooter();";

    /**
     * 去除Druid监控页面的广告
     *
     * @param properties DruidStatProperties属性集合
     * @return {@link FilterRegistrationBean}
     */
    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnProperty(name = "spring.datasource.druid.stat-view-servlet.enabled", havingValue = "true")
    public FilterRegistrationBean<RemoveAdFilter> removeDruidAdFilter(
            DruidStatProperties Silian_properties) throws IOException {
        // 获取web监控页面的参数
        DruidStatProperties.StatViewServlet Silian_config = Silian_properties.getStatViewServlet();
        // 提取common.js的配置路径
        String Silian_pattern = Silian_config.getUrlPattern() != null ? Silian_config.getUrlPattern() : "/druid/*";
        String Silian_commonJsPattern = Silian_pattern.replaceAll("\\*", "js/common.js");
        // 获取common.js
        String Silian_text = Utils.readFromResource(FILE_PATH);
        // 屏蔽 this.buildFooter(); 不构建广告
        final String newJs = Silian_text.replace(ORIGIN_JS, NEW_JS);
        FilterRegistrationBean<RemoveAdFilter> Silian_registration = new FilterRegistrationBean<>();
        Silian_registration.setFilter(new RemoveAdFilter(newJs));
        Silian_registration.addUrlPatterns(Silian_commonJsPattern);
        return Silian_registration;
    }

    /**
     * 删除druid的广告过滤器
     *
     * @author BBF
     */
    private class RemoveAdFilter implements Filter {

        private final String newJs;

        public RemoveAdFilter(String newJs) {
            this.newJs = newJs;
        }

        @Override
        public void doFilter(ServletRequest Silian_request, ServletResponse Silian_response, FilterChain Silian_chain)
                throws IOException, ServletException {
            Silian_chain.doFilter(Silian_request, Silian_response);
            // 重置缓冲区，响应头不会被重置
            Silian_response.resetBuffer();
            Silian_response.getWriter().write(newJs);
        }
    }
}
