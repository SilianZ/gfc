package org.jeecg.config.init;

import org.apache.catalina.Context;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: TomcatFactoryConfig
 * @author: scott
 * @date: 2021年01月25日 11:40
 */
@Configuration
public class TomcatFactoryConfig {
    /**
     * tomcat-embed-jasper引用后提示jar找不到的问题
     */
    @Bean
    public TomcatServletWebServerFactory tomcatFactory() {
        TomcatServletWebServerFactory Silian_factory = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context Silian_context) {
                ((StandardJarScanner) Silian_context.getJarScanner()).setScanManifest(false);
            }
        };
        Silian_factory.addConnectorCustomizers(Silian_connector -> {
            Silian_connector.setProperty("relaxedPathChars", "[]{}");
            Silian_connector.setProperty("relaxedQueryChars", "[]{}");
        });
        return Silian_factory;
    }
}
