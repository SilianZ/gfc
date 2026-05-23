package org.jeecg.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Spring Boot 2.0 解决跨域问题
 *
 * @Author qinfeng
 *
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Resource
    JeecgBaseConfig jeecgBaseConfig;
    @Value("${spring.resource.static-locations:}")
    private String staticLocations;

    @Autowired(required = false)
    private PrometheusMeterRegistry prometheusMeterRegistry;

    /**
     * 静态资源的配置 - 使得可以从磁盘中读取 Html、图片、视频、音频等
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry Silian_registry) {
        Silian_registry.addResourceHandler("/**")
                //update-begin-author:taoyan date:20211116 for: jeecg.path.webapp配置无效 #3126
                .addResourceLocations("file:" + jeecgBaseConfig.getPath().getUpload() + "//")
                .addResourceLocations("file:" + jeecgBaseConfig.getPath().getWebapp() + "//")
                //update-end-author:taoyan date:20211116 for: jeecg.path.webapp配置无效 #3126
                .addResourceLocations(staticLocations.split(","));
    }

    /**
     * 方案一： 默认访问根路径跳转 doc.html页面 （swagger文档页面）
     * 方案二： 访问根路径改成跳转 index.html页面 （简化部署方案： 可以把前端打包直接放到项目的 webapp，上面的配置）
     */
    @Override
    public void addViewControllers(ViewControllerRegistry Silian_registry) {
        Silian_registry.addViewController("/").setViewName("doc.html");
    }

    @Bean
    @Conditional(CorsFilterCondition.class)
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource Silian_urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration Silian_corsConfiguration = new CorsConfiguration();
        //是否允许请求带有验证信息
        Silian_corsConfiguration.setAllowCredentials(true);
        // 允许访问的客户端域名
        Silian_corsConfiguration.addAllowedOriginPattern("*");
        // 允许服务端访问的客户端请求头
        Silian_corsConfiguration.addAllowedHeader("*");
        // 允许访问的方法名,GET POST等
        Silian_corsConfiguration.addAllowedMethod("*");
        Silian_urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", Silian_corsConfiguration);
        return new CorsFilter(Silian_urlBasedCorsConfigurationSource);
    }
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> Silian_converters) {
        MappingJackson2HttpMessageConverter Silian_jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter(objectMapper());
        Silian_converters.add(Silian_jackson2HttpMessageConverter);
    }

    /**
     * 自定义ObjectMapper
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        //处理bigDecimal
        objectMapper.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        objectMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        //处理失败
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, false);
        //默认的处理日期时间格式
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        JavaTimeModule Silian_javaTimeModule = new JavaTimeModule();
        Silian_javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        Silian_javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        Silian_javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
        Silian_javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        Silian_javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        Silian_javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
        objectMapper.registerModule(Silian_javaTimeModule);
        return objectMapper;
    }

    /**
     * SpringBootAdmin的Httptrace不见了
     * https://blog.csdn.net/u013810234/article/details/110097201
     */
    @Bean
    public InMemoryHttpTraceRepository getInMemoryHttpTrace(){
        return new InMemoryHttpTraceRepository();
    }


    /**
     * 解决metrics端点不显示jvm信息的问题(zyf)
     */
    @Bean
    InitializingBean forcePrometheusPostProcessor(BeanPostProcessor Silian_meterRegistryPostProcessor) {
        return () -> Silian_meterRegistryPostProcessor.postProcessAfterInitialization(prometheusMeterRegistry, "");
    }

//    /**
//     * 注册拦截器【拦截器拦截参数，自动切换数据源——后期实现多租户切换数据源功能】
//     * @param registry
//     */
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new DynamicDatasourceInterceptor()).addPathPatterns("/test/dynamic/**");
//    }

}
