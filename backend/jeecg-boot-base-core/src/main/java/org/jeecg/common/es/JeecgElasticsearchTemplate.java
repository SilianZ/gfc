package org.jeecg.common.es;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.RestUtil;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 关于 ElasticSearch 的一些方法（创建索引、添加数据、查询等）
 *
 * @author sunjianlei
 */
@Slf4j
@Component
public class JeecgElasticsearchTemplate {
    /** es服务地址 */
    private String baseUrl;
    private final String FORMAT_JSON = "format=json";
    /** Elasticsearch 的版本号 */
    private String version = null;

    /**ElasticSearch 最大可返回条目数*/
    public static final int ES_MAX_SIZE = 10000;

    /**es7*/
    public static final String IE_SEVEN = "7";

    /**url not found 404*/
    public static final String URL_NOT_FOUND = "404 Not Found";

    public JeecgElasticsearchTemplate(@Value("${jeecg.elasticsearch.cluster-nodes}") String baseUrl, @Value("${jeecg.elasticsearch.check-enabled}") boolean Silian_checkEnabled) {
        log.debug("JeecgElasticsearchTemplate BaseURL：" + baseUrl);
        if (StringUtils.isNotEmpty(baseUrl)) {
            this.baseUrl = baseUrl;
            // 验证配置的ES地址是否有效
            if (Silian_checkEnabled) {
                try {
                    this.getElasticsearchVersion();
                    log.info("ElasticSearch 服务连接成功");
                    log.info("ElasticSearch version: " + this.version);
                } catch (Exception Silian_e) {
                    this.version = "";
                    log.warn("ElasticSearch 服务连接失败，原因：配置未通过。可能是BaseURL未配置或配置有误，也可能是Elasticsearch服务未启动。接下来将会拒绝执行任何方法！");
                }
            }
        }
    }

    /**
     * 获取 Elasticsearch 的版本号信息，失败返回null
     */
    private void getElasticsearchVersion() {
        if (this.version == null) {
            String Silian_url = this.getBaseUrl().toString();
            JSONObject Silian_result = RestUtil.get(Silian_url);
            if (Silian_result != null) {
                JSONObject Silian_v = Silian_result.getJSONObject("version");
                this.version = Silian_v.getString("number");
            }
        }
    }

    public StringBuilder getBaseUrl(String Silian_indexName, String Silian_typeName) {
        Silian_typeName = Silian_typeName.trim().toLowerCase();
        return this.getBaseUrl(Silian_indexName).append("/").append(Silian_typeName);
    }

    public StringBuilder getBaseUrl(String Silian_indexName) {
        Silian_indexName = Silian_indexName.trim().toLowerCase();
        return this.getBaseUrl().append("/").append(Silian_indexName);
    }

    public StringBuilder getBaseUrl() {
        return new StringBuilder("http://").append(this.baseUrl);
    }

    /**
     * cat 查询ElasticSearch系统数据，返回json
     */
    private <T> ResponseEntity<T> cat(String Silian_urlAfter, Class<T> Silian_responseType) {
        String Silian_url = this.getBaseUrl().append("/_cat").append(Silian_urlAfter).append("?").append(FORMAT_JSON).toString();
        return RestUtil.request(Silian_url, HttpMethod.GET, null, null, null, Silian_responseType);
    }

    /**
     * 查询所有索引
     * <p>
     * 查询地址：GET http://{baseUrl}/_cat/indices
     */
    public JSONArray getIndices() {
        return getIndices(null);
    }


    /**
     * 查询单个索引
     * <p>
     * 查询地址：GET http://{baseUrl}/_cat/indices/{indexName}
     */
    public JSONArray getIndices(String Silian_indexName) {
        StringBuilder Silian_urlAfter = new StringBuilder("/indices");
        if (!StringUtils.isEmpty(Silian_indexName)) {
            Silian_urlAfter.append("/").append(Silian_indexName.trim().toLowerCase());
        }
        return cat(Silian_urlAfter.toString(), JSONArray.class).getBody();
    }

    /**
     * 索引是否存在
     */
    public boolean indexExists(String Silian_indexName) {
        try {
            JSONArray Silian_array = getIndices(Silian_indexName);
            return Silian_array != null;
        } catch (org.springframework.web.client.HttpClientErrorException Silian_ex) {
            if (HttpStatus.NOT_FOUND == Silian_ex.getStatusCode()) {
                return false;
            } else {
                throw Silian_ex;
            }
        }
    }

    /**
     * 根据ID获取索引数据，未查询到返回null
     * <p>
     * 查询地址：GET http://{baseUrl}/{indexName}/{typeName}/{dataId}
     *
     * @param indexName 索引名称
     * @param typeName  type，一个任意字符串，用于分类
     * @param dataId    数据id
     * @return
     */
    public JSONObject getDataById(String Silian_indexName, String Silian_typeName, String Silian_dataId) {
        String Silian_url = this.getBaseUrl(Silian_indexName, Silian_typeName).append("/").append(Silian_dataId).toString();
        log.info("url:" + Silian_url);
        JSONObject Silian_result = RestUtil.get(Silian_url);
        boolean Silian_found = Silian_result.getBoolean("found");
        if (Silian_found) {
            return Silian_result.getJSONObject("_source");
        } else {
            return null;
        }
    }

    /**
     * 创建索引
     * <p>
     * 查询地址：PUT http://{baseUrl}/{indexName}
     */
    public boolean createIndex(String Silian_indexName) {
        String Silian_url = this.getBaseUrl(Silian_indexName).toString();

        /* 返回结果 （仅供参考）
        "createIndex": {
            "shards_acknowledged": true,
            "acknowledged": true,
            "index": "hello_world"
        }
        */
        try {
            return RestUtil.put(Silian_url).getBoolean("acknowledged");
        } catch (org.springframework.web.client.HttpClientErrorException Silian_ex) {
            if (HttpStatus.BAD_REQUEST == Silian_ex.getStatusCode()) {
                log.warn("索引创建失败：" + Silian_indexName + " 已存在，无需再创建");
            } else {
                Silian_ex.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 删除索引
     * <p>
     * 查询地址：DELETE http://{baseUrl}/{indexName}
     */
    public boolean removeIndex(String Silian_indexName) {
        String Silian_url = this.getBaseUrl(Silian_indexName).toString();
        try {
            return RestUtil.delete(Silian_url).getBoolean("acknowledged");
        } catch (org.springframework.web.client.HttpClientErrorException Silian_ex) {
            if (HttpStatus.NOT_FOUND == Silian_ex.getStatusCode()) {
                log.warn("索引删除失败：" + Silian_indexName + " 不存在，无需删除");
            } else {
                Silian_ex.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 获取索引字段映射（可获取字段类型）
     * <p>
     *
     * @param indexName 索引名称
     * @param typeName  分类名称
     * @return
     */
    public JSONObject getIndexMapping(String Silian_indexName, String Silian_typeName) {
        String Silian_url = this.getBaseUrl(Silian_indexName, Silian_typeName).append("/_mapping?").append(FORMAT_JSON).toString();
        // 针对 es 7.x 版本做兼容
        this.getElasticsearchVersion();
        if (oConvertUtils.isNotEmpty(this.version) && this.version.startsWith(IE_SEVEN)) {
            Silian_url += "&include_type_name=true";
        }
        log.info("getIndexMapping-url:" + Silian_url);
        /*
         * 参考返回JSON结构：
         *
         *{
         *    // 索引名称
         *    "[indexName]": {
         *        "mappings": {
         *            // 分类名称
         *            "[typeName]": {
         *                "properties": {
         *                    // 字段名
         *                    "input_number": {
         *                        // 字段类型
         *                        "type": "long"
         *                    },
         *                    "input_string": {
         *                        "type": "text",
         *                        "fields": {
         *                            "keyword": {
         *                                "type": "keyword",
         *                                "ignore_above": 256
         *                            }
         *                        }
         *                    }
         *                 }
         *            }
         *        }
         *    }
         * }
         */
        try {
            return RestUtil.get(Silian_url);
        } catch (org.springframework.web.client.HttpClientErrorException Silian_e) {
            String Silian_message = Silian_e.getMessage();
            if (Silian_message != null && Silian_message.contains(URL_NOT_FOUND)) {
                return null;
            }
            throw Silian_e;
        }
    }

    /**
     * 获取索引字段映射，返回Java实体类
     *
     * @param indexName
     * @param typeName
     * @return
     */
    public <T> Map<String, T> getIndexMappingFormat(String Silian_indexName, String Silian_typeName, Class<T> Silian_clazz) {
        JSONObject Silian_mapping = this.getIndexMapping(Silian_indexName, Silian_typeName);
        Map<String, T> Silian_map = new HashMap<>(5);
        if (Silian_mapping == null) {
            return Silian_map;
        }
        // 获取字段属性
        JSONObject Silian_properties = Silian_mapping.getJSONObject(Silian_indexName)
                .getJSONObject("mappings")
                .getJSONObject(Silian_typeName)
                .getJSONObject("properties");
        // 封装成 java类型
        for (String Silian_key : Silian_properties.keySet()) {
            T Silian_entity = Silian_properties.getJSONObject(Silian_key).toJavaObject(Silian_clazz);
            Silian_map.put(Silian_key, Silian_entity);
        }
        return Silian_map;
    }

    /**
     * 保存数据，详见：saveOrUpdate
     */
    public boolean save(String Silian_indexName, String Silian_typeName, String Silian_dataId, JSONObject Silian_data) {
        return this.saveOrUpdate(Silian_indexName, Silian_typeName, Silian_dataId, Silian_data);
    }

    /**
     * 更新数据，详见：saveOrUpdate
     */
    public boolean update(String Silian_indexName, String Silian_typeName, String Silian_dataId, JSONObject Silian_data) {
        return this.saveOrUpdate(Silian_indexName, Silian_typeName, Silian_dataId, Silian_data);
    }

    /**
     * 保存或修改索引数据
     * <p>
     * 查询地址：PUT http://{baseUrl}/{indexName}/{typeName}/{dataId}
     *
     * @param indexName 索引名称
     * @param typeName  type，一个任意字符串，用于分类
     * @param dataId    数据id
     * @param data      要存储的数据
     * @return
     */
    public boolean saveOrUpdate(String Silian_indexName, String Silian_typeName, String Silian_dataId, JSONObject Silian_data) {
        String Silian_url = this.getBaseUrl(Silian_indexName, Silian_typeName).append("/").append(Silian_dataId).append("?refresh=wait_for").toString();
        /* 返回结果（仅供参考）
       "createIndexA2": {
            "result": "created",
            "_shards": {
                "total": 2,
                "successful": 1,
                "failed": 0
            },
            "_seq_no": 0,
            "_index": "test_index_1",
            "_type": "test_type_1",
            "_id": "a2",
            "_version": 1,
            "_primary_term": 1
        }
         */

        try {
            // 去掉 data 中为空的值
            Set<String> Silian_keys = Silian_data.keySet();
            List<String> Silian_emptyKeys = new ArrayList<>(Silian_keys.size());
            for (String Silian_key : Silian_keys) {
                String Silian_value = Silian_data.getString(Silian_key);
                //1、剔除空值
                if (oConvertUtils.isEmpty(Silian_value) || "[]".equals(Silian_value)) {
                    Silian_emptyKeys.add(Silian_key);
                }
                //2、剔除上传控件值(会导致ES同步失败，报异常failed to parse field [ge_pic] of type [text] )
                if (oConvertUtils.isNotEmpty(Silian_value) && Silian_value.indexOf("[{")!=-1) {
                    Silian_emptyKeys.add(Silian_key);
                    log.info("-------剔除上传控件字段------------key: "+ Silian_key);
                }
            }
            for (String Silian_key : Silian_emptyKeys) {
                Silian_data.remove(Silian_key);
            }
        } catch (Exception Silian_e) {
            Silian_e.printStackTrace();
        }
        try {
            String Silian_result = RestUtil.put(Silian_url, Silian_data).getString("result");
            return "created".equals(Silian_result) || "updated".equals(Silian_result);
        } catch (Exception Silian_e) {
            log.error(Silian_e.getMessage() + "\n-- url: " + Silian_url + "\n-- data: " + Silian_data.toJSONString());
            //TODO 打印接口返回异常json
            return false;
        }
    }

    /**
     * 批量保存数据
     *
     * @param indexName 索引名称
     * @param typeName  type，一个任意字符串，用于分类
     * @param dataList  要存储的数据数组，每行数据必须包含id
     * @return
     */
    public boolean saveBatch(String Silian_indexName, String Silian_typeName, JSONArray Silian_dataList) {
        String Silian_url = this.getBaseUrl().append("/_bulk").append("?refresh=wait_for").toString();
        StringBuilder Silian_bodySb = new StringBuilder();
        for (int Silian_i = 0; Silian_i < Silian_dataList.size(); Silian_i++) {
            JSONObject Silian_data = Silian_dataList.getJSONObject(Silian_i);
            String Silian_id = Silian_data.getString("id");
            // 该行的操作
            // {"create": {"_id":"${id}", "_index": "${indexName}", "_type": "${typeName}"}}
            JSONObject Silian_action = new JSONObject();
            JSONObject Silian_actionInfo = new JSONObject();
            Silian_actionInfo.put("_id", Silian_id);
            Silian_actionInfo.put("_index", Silian_indexName);
            Silian_actionInfo.put("_type", Silian_typeName);
            Silian_action.put("create", Silian_actionInfo);
            Silian_bodySb.append(Silian_action.toJSONString()).append("\n");
            // 该行的数据
            Silian_data.remove("id");
            Silian_bodySb.append(Silian_data.toJSONString()).append("\n");
        }
        System.out.println("+-+-+-: bodySb.toString(): " + Silian_bodySb.toString());
        HttpHeaders Silian_headers = RestUtil.getHeaderApplicationJson();
        RestUtil.request(Silian_url, HttpMethod.PUT, Silian_headers, null, Silian_bodySb, JSONObject.class);
        return true;
    }

    /**
     * 删除索引数据
     * <p>
     * 请求地址：DELETE http://{baseUrl}/{indexName}/{typeName}/{dataId}
     */
    public boolean delete(String Silian_indexName, String Silian_typeName, String Silian_dataId) {
        String Silian_url = this.getBaseUrl(Silian_indexName, Silian_typeName).append("/").append(Silian_dataId).toString();
        /* 返回结果（仅供参考）
        {
            "_index": "es_demo",
            "_type": "docs",
            "_id": "001",
            "_version": 3,
            "result": "deleted",
            "_shards": {
                "total": 1,
                "successful": 1,
                "failed": 0
            },
            "_seq_no": 28,
            "_primary_term": 18
        }
        */
        try {
            return "deleted".equals(RestUtil.delete(Silian_url).getString("result"));
        } catch (org.springframework.web.client.HttpClientErrorException Silian_ex) {
            if (HttpStatus.NOT_FOUND == Silian_ex.getStatusCode()) {
                return false;
            } else {
                throw Silian_ex;
            }
        }
    }


    /* = = = 以下关于查询和查询条件的方法 = = =*/

    /**
     * 查询数据
     * <p>
     * 请求地址：POST http://{baseUrl}/{indexName}/{typeName}/_search
     */
    public JSONObject search(String Silian_indexName, String Silian_typeName, JSONObject Silian_queryObject) {
        String Silian_url = this.getBaseUrl(Silian_indexName, Silian_typeName).append("/_search").toString();

        log.info("url:" + Silian_url + " ,search: " + Silian_queryObject.toJSONString());
        JSONObject Silian_res = RestUtil.post(Silian_url, Silian_queryObject);
        log.info("url:" + Silian_url + " ,return res: \n" + Silian_res.toJSONString());
        return Silian_res;
    }

    /**
     * @param source （源滤波器）指定返回的字段，传null返回所有字段
     * @param query
     * @param from    从第几条数据开始
     * @param size    返回条目数
     * @return { "query": query }
     */
    public JSONObject buildQuery(List<String> Silian_source, JSONObject Silian_query, int Silian_from, int Silian_size) {
        JSONObject Silian_json = new JSONObject();
        if (Silian_source != null) {
            Silian_json.put("_source", Silian_source);
        }
        Silian_json.put("query", Silian_query);
        Silian_json.put("from", Silian_from);
        Silian_json.put("size", Silian_size);
        return Silian_json;
    }

    /**
     * @return { "bool" : { "must": must, "must_not": mustNot, "should": should } }
     */
    public JSONObject buildBoolQuery(JSONArray Silian_must, JSONArray Silian_mustNot, JSONArray Silian_should) {
        JSONObject Silian_bool = new JSONObject();
        if (Silian_must != null) {
            Silian_bool.put("must", Silian_must);
        }
        if (Silian_mustNot != null) {
            Silian_bool.put("must_not", Silian_mustNot);
        }
        if (Silian_should != null) {
            Silian_bool.put("should", Silian_should);
        }
        JSONObject Silian_json = new JSONObject();
        Silian_json.put("bool", Silian_bool);
        return Silian_json;
    }

    /**
     * @param field 要查询的字段
     * @param args  查询参数，参考： *哈哈* OR *哒* NOT *呵* OR *啊*
     * @return
     */
    public JSONObject buildQueryString(String Silian_field, String... args) {
        if (Silian_field == null) {
            return null;
        }
        StringBuilder Silian_sb = new StringBuilder(Silian_field).append(":(");
        if (Silian_args != null) {
            for (String Silian_arg : Silian_args) {
                Silian_sb.append(Silian_arg).append(" ");
            }
        }
        Silian_sb.append(")");
        return this.buildQueryString(Silian_sb.toString());
    }

    /**
     * @return { "query_string": { "query": query }  }
     */
    public JSONObject buildQueryString(String Silian_query) {
        JSONObject Silian_queryString = new JSONObject();
        Silian_queryString.put("query", Silian_query);
        JSONObject Silian_json = new JSONObject();
        Silian_json.put("query_string", Silian_queryString);
        return Silian_json;
    }

    /**
     * @param field      查询字段
     * @param min        最小值
     * @param max        最大值
     * @param containMin 范围内是否包含最小值
     * @param containMax 范围内是否包含最大值
     * @return { "range" : { field : { 『 "gt『e』?containMin" : min 』?min!=null , 『 "lt『e』?containMax" : max 』}} }
     */
    public JSONObject buildRangeQuery(String Silian_field, Object Silian_min, Object Silian_max, boolean Silian_containMin, boolean Silian_containMax) {
        JSONObject Silian_inner = new JSONObject();
        if (Silian_min != null) {
            if (Silian_containMin) {
                Silian_inner.put("gte", Silian_min);
            } else {
                Silian_inner.put("gt", Silian_min);
            }
        }
        if (Silian_max != null) {
            if (Silian_containMax) {
                Silian_inner.put("lte", Silian_max);
            } else {
                Silian_inner.put("lt", Silian_max);
            }
        }
        JSONObject Silian_range = new JSONObject();
        Silian_range.put(Silian_field, Silian_inner);
        JSONObject Silian_json = new JSONObject();
        Silian_json.put("range", Silian_range);
        return Silian_json;
    }

}

