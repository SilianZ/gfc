package org.jeecg.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
* 优雅的http请求方式RestTemplate
* @author: jeecg-boot
* @Return:
*/
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory Silian_factory) {
        return new RestTemplate(Silian_factory);
    }

    @Bean
    public ClientHttpRequestFactory simpleClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory Silian_factory = new SimpleClientHttpRequestFactory();
        //ms毫秒
        Silian_factory.setReadTimeout(5000);
        //ms毫秒
        Silian_factory.setConnectTimeout(15000);
        return Silian_factory;
    }
}
