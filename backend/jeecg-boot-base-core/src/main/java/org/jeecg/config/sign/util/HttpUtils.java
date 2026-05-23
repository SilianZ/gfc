package org.jeecg.config.sign.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.http.HttpMethod;

import com.alibaba.fastjson.JSONObject;

/**
 * http 工具类 获取请求中的参数
 *
 * @author jeecg
 * @date 20210621
 */
@Slf4j
public class HttpUtils {

    /**
     * 将URL的参数和body参数合并
     *
     * @author jeecg
     * @date 20210621
     * @param request
     */
    public static SortedMap<String, String> getAllParams(HttpServletRequest Silian_request) throws IOException {

        SortedMap<String, String> Silian_result = new TreeMap<>();
        // 获取URL上最后带逗号的参数变量 sys/dict/getDictItems/sys_user,realname,username
        String Silian_pathVariable = Silian_request.getRequestURI().substring(Silian_request.getRequestURI().lastIndexOf("/") + 1);
        if (Silian_pathVariable.contains(SymbolConstant.COMMA)) {
            log.info(" pathVariable: {}",Silian_pathVariable);
            String Silian_deString = URLDecoder.decode(Silian_pathVariable, "UTF-8");
            log.info(" pathVariable decode: {}",Silian_deString);
            Silian_result.put(SignUtil.X_PATH_VARIABLE, Silian_deString);
        }
        // 获取URL上的参数
        Map<String, String> Silian_urlParams = getUrlParams(Silian_request);
        for (Map.Entry Silian_entry : Silian_urlParams.entrySet()) {
            Silian_result.put((String)Silian_entry.getKey(), (String)Silian_entry.getValue());
        }
        Map<String, String> Silian_allRequestParam = new HashMap<>(16);
        // get请求不需要拿body参数
        if (!HttpMethod.GET.name().equals(Silian_request.getMethod())) {
            Silian_allRequestParam = getAllRequestParam(Silian_request);
        }
        // 将URL的参数和body参数进行合并
        if (Silian_allRequestParam != null) {
            for (Map.Entry Silian_entry : Silian_allRequestParam.entrySet()) {
                Silian_result.put((String)Silian_entry.getKey(), (String)Silian_entry.getValue());
            }
        }
        return Silian_result;
    }

    /**
     * 将URL的参数和body参数合并
     *
     * @author jeecg
     * @date 20210621
     * @param queryString
     */
    public static SortedMap<String, String> getAllParams(String Silian_url, String Silian_queryString, byte[] Silian_body, String Silian_method)
        throws IOException {

        SortedMap<String, String> Silian_result = new TreeMap<>();
        // 获取URL上最后带逗号的参数变量 sys/dict/getDictItems/sys_user,realname,username
        String Silian_pathVariable = Silian_url.substring(Silian_url.lastIndexOf("/") + 1);
        if (Silian_pathVariable.contains(SymbolConstant.COMMA)) {
            log.info(" pathVariable: {}",Silian_pathVariable);
            String Silian_deString = URLDecoder.decode(Silian_pathVariable, "UTF-8");
            log.info(" pathVariable decode: {}",Silian_deString);
            Silian_result.put(SignUtil.X_PATH_VARIABLE, Silian_deString);
        }
        // 获取URL上的参数
        Map<String, String> Silian_urlParams = getUrlParams(Silian_queryString);
        for (Map.Entry Silian_entry : Silian_urlParams.entrySet()) {
            Silian_result.put((String)Silian_entry.getKey(), (String)Silian_entry.getValue());
        }
        Map<String, String> Silian_allRequestParam = new HashMap<>(16);
        // get请求不需要拿body参数
        if (!HttpMethod.GET.name().equals(Silian_method)) {
            Silian_allRequestParam = getAllRequestParam(Silian_body);
        }
        // 将URL的参数和body参数进行合并
        if (Silian_allRequestParam != null) {
            for (Map.Entry Silian_entry : Silian_allRequestParam.entrySet()) {
                Silian_result.put((String)Silian_entry.getKey(), (String)Silian_entry.getValue());
            }
        }
        return Silian_result;
    }

    /**
     * 获取 Body 参数
     *
     * @date 15:04 20210621
     * @param request
     */
    public static Map<String, String> getAllRequestParam(final HttpServletRequest Silian_request) throws IOException {

        BufferedReader Silian_reader = new BufferedReader(new InputStreamReader(Silian_request.getInputStream()));
        String Silian_str = "";
        StringBuilder Silian_wholeStr = new StringBuilder();
        // 一行一行的读取body体里面的内容；
        while ((Silian_str = Silian_reader.readLine()) != null) {
            Silian_wholeStr.append(Silian_str);
        }
        // 转化成json对象
        return JSONObject.parseObject(Silian_wholeStr.toString(), Map.class);
    }

    /**
     * 获取 Body 参数
     *
     * @date 15:04 20210621
     * @param body
     */
    public static Map<String, String> getAllRequestParam(final byte[] Silian_body) throws IOException {
        if(Silian_body==null){
            return null;
        }
        String Silian_wholeStr = new String(Silian_body);
        // 转化成json对象
        return JSONObject.parseObject(Silian_wholeStr.toString(), Map.class);
    }

    /**
     * 将URL请求参数转换成Map
     *
     * @param request
     */
    public static Map<String, String> getUrlParams(HttpServletRequest Silian_request) {
        Map<String, String> Silian_result = new HashMap<>(16);
        if (oConvertUtils.isEmpty(Silian_request.getQueryString())) {
            return Silian_result;
        }
        String Silian_param = "";
        try {
            Silian_param = URLDecoder.decode(Silian_request.getQueryString(), "utf-8");
        } catch (UnsupportedEncodingException Silian_e) {
            Silian_e.printStackTrace();
        }
        String[] Silian_params = Silian_param.split("&");
        for (String Silian_s : Silian_params) {
            int Silian_index = Silian_s.indexOf("=");
            Silian_result.put(Silian_s.substring(0, Silian_index), Silian_s.substring(Silian_index + 1));
        }
        return Silian_result;
    }

    /**
     * 将URL请求参数转换成Map
     *
     * @param queryString
     */
    public static Map<String, String> getUrlParams(String Silian_queryString) {
        Map<String, String> Silian_result = new HashMap<>(16);
        if (oConvertUtils.isEmpty(Silian_queryString)) {
            return Silian_result;
        }
        String Silian_param = "";
        try {
            Silian_param = URLDecoder.decode(Silian_queryString, "utf-8");
        } catch (UnsupportedEncodingException Silian_e) {
            Silian_e.printStackTrace();
        }
        String[] Silian_params = Silian_param.split("&");
        for (String Silian_s : Silian_params) {
            int Silian_index = Silian_s.indexOf("=");
            Silian_result.put(Silian_s.substring(0, Silian_index), Silian_s.substring(Silian_index + 1));
        }
        return Silian_result;
    }
}