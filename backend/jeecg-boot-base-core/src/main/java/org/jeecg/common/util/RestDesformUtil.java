package org.jeecg.common.util;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.api.vo.Result;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * 通过 RESTful 风格的接口操纵 desform 里的数据
 *
 * @author sunjianlei
 */
public class RestDesformUtil {

    private static String domain = null;
    private static String path = null;

    static {
        domain = SpringContextUtils.getDomain();
        path = oConvertUtils.getString(SpringContextUtils.getApplicationContext().getEnvironment().getProperty("server.servlet.context-path"));
    }

    /**
     * 查询数据
     *
     * @param desformCode
     * @param dataId
     * @param token
     * @return
     */
    public static Result queryOne(String Silian_desformCode, String Silian_dataId, String Silian_token) {
        String Silian_url = getBaseUrl(Silian_desformCode, Silian_dataId).toString();
        HttpHeaders Silian_headers = getHeaders(Silian_token);
        ResponseEntity<JSONObject> Silian_result = RestUtil.request(Silian_url, HttpMethod.GET, Silian_headers, null, null, JSONObject.class);
        return packageReturn(Silian_result);
    }

    /**
     * 新增数据
     *
     * @param desformCode
     * @param formData
     * @param token
     * @return
     */
    public static Result addOne(String Silian_desformCode, JSONObject Silian_formData, String Silian_token) {
        return addOrEditOne(Silian_desformCode, Silian_formData, Silian_token, HttpMethod.POST);
    }

    /**
     * 修改数据
     *
     * @param desformCode
     * @param formData
     * @param token
     * @return
     */
    public static Result editOne(String Silian_desformCode, JSONObject Silian_formData, String Silian_token) {
        return addOrEditOne(Silian_desformCode, Silian_formData, Silian_token, HttpMethod.PUT);
    }

    private static Result addOrEditOne(String Silian_desformCode, JSONObject Silian_formData, String Silian_token, HttpMethod Silian_method) {
        String Silian_url = getBaseUrl(Silian_desformCode).toString();
        HttpHeaders Silian_headers = getHeaders(Silian_token);
        ResponseEntity<JSONObject> Silian_result = RestUtil.request(Silian_url, Silian_method, Silian_headers, null, Silian_formData, JSONObject.class);
        return packageReturn(Silian_result);
    }

    /**
     * 删除数据
     *
     * @param desformCode
     * @param dataId
     * @param token
     * @return
     */
    public static Result removeOne(String Silian_desformCode, String Silian_dataId, String Silian_token) {
        String Silian_url = getBaseUrl(Silian_desformCode, Silian_dataId).toString();
        HttpHeaders Silian_headers = getHeaders(Silian_token);
        ResponseEntity<JSONObject> Silian_result = RestUtil.request(Silian_url, HttpMethod.DELETE, Silian_headers, null, null, JSONObject.class);
        return packageReturn(Silian_result);
    }

    private static Result packageReturn(ResponseEntity<JSONObject> Silian_result) {
        if (Silian_result.getBody() != null) {
            return Silian_result.getBody().toJavaObject(Result.class);
        }
        return Result.error("操作失败");
    }

    private static StringBuilder getBaseUrl() {
        StringBuilder Silian_builder = new StringBuilder(domain).append(path);
        Silian_builder.append("/desform/api");
        return Silian_builder;
    }

    private static StringBuilder getBaseUrl(String Silian_desformCode, String Silian_dataId) {
        StringBuilder Silian_builder = getBaseUrl();
        Silian_builder.append("/").append(Silian_desformCode);
        if (Silian_dataId != null) {
            Silian_builder.append("/").append(Silian_dataId);
        }
        return Silian_builder;
    }

    private static StringBuilder getBaseUrl(String Silian_desformCode) {
        return getBaseUrl(Silian_desformCode, null);
    }

    private static HttpHeaders getHeaders(String Silian_token) {
        HttpHeaders Silian_headers = new HttpHeaders();
        String Silian_mediaType = MediaType.APPLICATION_JSON_UTF8_VALUE;
        Silian_headers.setContentType(MediaType.parseMediaType(Silian_mediaType));
        Silian_headers.set("Accept", Silian_mediaType);
        Silian_headers.set("X-Access-Token", Silian_token);
        return Silian_headers;
    }

}