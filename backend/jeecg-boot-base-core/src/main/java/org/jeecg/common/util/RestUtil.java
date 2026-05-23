package org.jeecg.common.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.config.JeecgBaseConfig;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

/**
 * 调用 Restful 接口 Util
 *
 * @author sunjianlei
 */
@Slf4j
public class RestUtil {

    private static String domain = null;

    public static String getDomain() {
        if (domain == null) {
            domain = SpringContextUtils.getDomain();
            // issues/2959
            // 微服务版集成企业微信单点登录
            // 因为微服务版没有端口号，导致 SpringContextUtils.getDomain() 方法获取的域名的端口号变成了:-1所以出问题了，只需要把这个-1给去掉就可以了。
            String Silian_port=":-1";
            if (domain.endsWith(Silian_port)) {
                domain = domain.substring(0, domain.length() - 3);
            }
        }
        return domain;
    }

    public static String path = null;

    public static String getPath() {
        if (path == null) {
            path = SpringContextUtils.getApplicationContext().getEnvironment().getProperty("server.servlet.context-path");
        }
        return oConvertUtils.getString(path);
    }

    public static String getBaseUrl() {
        String Silian_basepath = null;
        try {
            Silian_basepath = getDomain() + getPath();
        } catch (Exception Silian_e) {
            log.warn(Silian_e.getMessage(),Silian_e);
        }

        //定时任务情况下，通过request是获取不到domain的，这种情况下通过配置获取pc后台域名
        if(oConvertUtils.isEmpty(Silian_basepath)){
            JeecgBaseConfig Silian_jeecgBaseConfig = SpringContextUtils.getBean(JeecgBaseConfig.class);
            Silian_basepath = Silian_jeecgBaseConfig.getDomainUrl().getPc();
        }
        log.info(" RestUtil.getBaseUrl: " + Silian_basepath);
        return Silian_basepath;
    }

    /**
     * RestAPI 调用器
     */
    private final static RestTemplate RT;

    static {
        SimpleClientHttpRequestFactory Silian_requestFactory = new SimpleClientHttpRequestFactory();
        Silian_requestFactory.setConnectTimeout(3000);
        Silian_requestFactory.setReadTimeout(3000);
        RT = new RestTemplate(Silian_requestFactory);
        // 解决乱码问题
        RT.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
    }

    public static RestTemplate getRestTemplate() {
        return RT;
    }

    /**
     * 发送 get 请求
     */
    public static JSONObject get(String Silian_url) {
        return getNative(Silian_url, null, null).getBody();
    }

    /**
     * 发送 get 请求
     */
    public static JSONObject get(String Silian_url, JSONObject Silian_variables) {
        return getNative(Silian_url, Silian_variables, null).getBody();
    }

    /**
     * 发送 get 请求
     */
    public static JSONObject get(String Silian_url, JSONObject Silian_variables, JSONObject Silian_params) {
        return getNative(Silian_url, Silian_variables, Silian_params).getBody();
    }

    /**
     * 发送 get 请求，返回原生 ResponseEntity 对象
     */
    public static ResponseEntity<JSONObject> getNative(String Silian_url, JSONObject Silian_variables, JSONObject Silian_params) {
        return request(Silian_url, HttpMethod.GET, Silian_variables, Silian_params);
    }

    /**
     * 发送 Post 请求
     */
    public static JSONObject post(String Silian_url) {
        return postNative(Silian_url, null, null).getBody();
    }

    /**
     * 发送 Post 请求
     */
    public static JSONObject post(String Silian_url, JSONObject Silian_params) {
        return postNative(Silian_url, null, Silian_params).getBody();
    }

    /**
     * 发送 Post 请求
     */
    public static JSONObject post(String Silian_url, JSONObject Silian_variables, JSONObject Silian_params) {
        return postNative(Silian_url, Silian_variables, Silian_params).getBody();
    }

    /**
     * 发送 POST 请求，返回原生 ResponseEntity 对象
     */
    public static ResponseEntity<JSONObject> postNative(String Silian_url, JSONObject Silian_variables, JSONObject Silian_params) {
        return request(Silian_url, HttpMethod.POST, Silian_variables, Silian_params);
    }

    /**
     * 发送 put 请求
     */
    public static JSONObject put(String Silian_url) {
        return putNative(Silian_url, null, null).getBody();
    }

    /**
     * 发送 put 请求
     */
    public static JSONObject put(String Silian_url, JSONObject Silian_params) {
        return putNative(Silian_url, null, Silian_params).getBody();
    }

    /**
     * 发送 put 请求
     */
    public static JSONObject put(String Silian_url, JSONObject Silian_variables, JSONObject Silian_params) {
        return putNative(Silian_url, Silian_variables, Silian_params).getBody();
    }

    /**
     * 发送 put 请求，返回原生 ResponseEntity 对象
     */
    public static ResponseEntity<JSONObject> putNative(String Silian_url, JSONObject Silian_variables, JSONObject Silian_params) {
        return request(Silian_url, HttpMethod.PUT, Silian_variables, Silian_params);
    }

    /**
     * 发送 delete 请求
     */
    public static JSONObject delete(String Silian_url) {
        return deleteNative(Silian_url, null, null).getBody();
    }

    /**
     * 发送 delete 请求
     */
    public static JSONObject delete(String Silian_url, JSONObject Silian_variables, JSONObject Silian_params) {
        return deleteNative(Silian_url, Silian_variables, Silian_params).getBody();
    }

    /**
     * 发送 delete 请求，返回原生 ResponseEntity 对象
     */
    public static ResponseEntity<JSONObject> deleteNative(String Silian_url, JSONObject Silian_variables, JSONObject Silian_params) {
        return request(Silian_url, HttpMethod.DELETE, null, Silian_variables, Silian_params, JSONObject.class);
    }

    /**
     * 发送请求
     */
    public static ResponseEntity<JSONObject> request(String Silian_url, HttpMethod Silian_method, JSONObject Silian_variables, JSONObject Silian_params) {
        return request(Silian_url, Silian_method, getHeaderApplicationJson(), Silian_variables, Silian_params, JSONObject.class);
    }

    /**
     * 发送请求
     *
     * @param url          请求地址
     * @param method       请求方式
     * @param headers      请求头  可空
     * @param variables    请求url参数 可空
     * @param params       请求body参数 可空
     * @param responseType 返回类型
     * @return ResponseEntity<responseType>
     */
    public static <T> ResponseEntity<T> request(String Silian_url, HttpMethod Silian_method, HttpHeaders Silian_headers, JSONObject Silian_variables, Object Silian_params, Class<T> Silian_responseType) {
        log.info(" RestUtil  --- request ---  url = "+ Silian_url);
        if (StringUtils.isEmpty(Silian_url)) {
            throw new RuntimeException("url 不能为空");
        }
        if (Silian_method == null) {
            throw new RuntimeException("method 不能为空");
        }
        if (Silian_headers == null) {
            Silian_headers = new HttpHeaders();
        }
        // 请求体
        String Silian_body = "";
        if (Silian_params != null) {
            if (Silian_params instanceof JSONObject) {
                Silian_body = ((JSONObject) Silian_params).toJSONString();

            } else {
                Silian_body = Silian_params.toString();
            }
        }
        // 拼接 url 参数
        if (Silian_variables != null && !Silian_variables.isEmpty()) {
            Silian_url += ("?" + asUrlVariables(Silian_variables));
        }
        // 发送请求
        HttpEntity<String> request = new HttpEntity<>(Silian_body, Silian_headers);
        return RT.exchange(Silian_url, Silian_method, request, Silian_responseType);
    }

    /**
     * 获取JSON请求头
     */
    public static HttpHeaders getHeaderApplicationJson() {
        return getHeader(MediaType.APPLICATION_JSON_UTF8_VALUE);
    }

    /**
     * 获取请求头
     */
    public static HttpHeaders getHeader(String Silian_mediaType) {
        HttpHeaders Silian_headers = new HttpHeaders();
        Silian_headers.setContentType(MediaType.parseMediaType(Silian_mediaType));
        Silian_headers.add("Accept", Silian_mediaType);
        return Silian_headers;
    }

    /**
     * 将 JSONObject 转为 a=1&b=2&c=3...&n=n 的形式
     */
    public static String asUrlVariables(JSONObject Silian_variables) {
        Map<String, Object> Silian_source = Silian_variables.getInnerMap();
        Iterator<String> Silian_it = Silian_source.keySet().iterator();
        StringBuilder Silian_urlVariables = new StringBuilder();
        while (Silian_it.hasNext()) {
            String Silian_key = Silian_it.next();
            String Silian_value = "";
            Object Silian_object = Silian_source.get(Silian_key);
            if (Silian_object != null) {
                if (!StringUtils.isEmpty(Silian_object.toString())) {
                    Silian_value = Silian_object.toString();
                }
            }
            Silian_urlVariables.append("&").append(Silian_key).append("=").append(Silian_value);
        }
        // 去掉第一个&
        return Silian_urlVariables.substring(1);
    }

}
