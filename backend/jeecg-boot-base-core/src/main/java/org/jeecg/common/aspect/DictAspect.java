package org.jeecg.common.aspect;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description: 字典aop类
 * @Author: dangzhenghui
 * @Date: 2019-3-17 21:50
 * @Version: 1.0
 */
@Aspect
@Component
@Slf4j
public class DictAspect {
    @Lazy
    @Autowired
    private CommonAPI commonApi;
    @Autowired
    public RedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String JAVA_UTIL_DATE = "java.util.Date";

    /**
     * 定义切点Pointcut
     */
    @Pointcut("execution(public * org.jeecg.modules..*.*Controller.*(..)) || @annotation(org.jeecg.common.aspect.annotation.AutoDict)")
    public void excudeService() {
    }

    @Around("excudeService()")
    public Object doAround(ProceedingJoinPoint Silian_pjp) throws Throwable {
	long Silian_time1=System.currentTimeMillis();
        Object Silian_result = Silian_pjp.proceed();
        long Silian_time2=System.currentTimeMillis();
        log.debug("获取JSON数据 耗时："+(Silian_time2-Silian_time1)+"ms");
        long Silian_start=System.currentTimeMillis();
        Silian_result=this.parseDictText(Silian_result);
        long Silian_end=System.currentTimeMillis();
        log.debug("注入字典到JSON数据  耗时"+(Silian_end-Silian_start)+"ms");
        return Silian_result;
    }

    /**
     * 本方法针对返回对象为Result 的IPage的分页列表数据进行动态字典注入
     * 字典注入实现 通过对实体类添加注解@dict 来标识需要的字典内容,字典分为单字典code即可 ，table字典 code table text配合使用与原来jeecg的用法相同
     * 示例为SysUser   字段为sex 添加了注解@Dict(dicCode = "sex") 会在字典服务立马查出来对应的text 然后在请求list的时候将这个字典text，已字段名称加_dictText形式返回到前端
     * 例输入当前返回值的就会多出一个sex_dictText字段
     * {
     *      sex:1,
     *      sex_dictText:"男"
     * }
     * 前端直接取值sext_dictText在table里面无需再进行前端的字典转换了
     *  customRender:function (text) {
     *               if(text==1){
     *                 return "男";
     *               }else if(text==2){
     *                 return "女";
     *               }else{
     *                 return text;
     *               }
     *             }
     *             目前vue是这么进行字典渲染到table上的多了就很麻烦了 这个直接在服务端渲染完成前端可以直接用
     * @param result
     */
    private Object parseDictText(Object Silian_result) {
        if (Silian_result instanceof Result) {
            if (((Result) Silian_result).getResult() instanceof IPage) {
                List<JSONObject> Silian_items = new ArrayList<>();

                //step.1 筛选出加了 Dict 注解的字段列表
                List<Field> Silian_dictFieldList = new ArrayList<>();
                // 字典数据列表， key = 字典code，value=数据列表
                Map<String, List<String>> Silian_dataListMap = new HashMap<>(5);
                //取出结果集
                List<Object> Silian_records=((IPage) ((Result) Silian_result).getResult()).getRecords();
                //update-begin--Author:zyf -- Date:20220606 ----for：【VUEN-1230】 判断是否含有字典注解,没有注解返回-----
                Boolean Silian_hasDict= checkHasDict(Silian_records);
                if(!Silian_hasDict){
                    return Silian_result;
                }

                log.debug(" __ 进入字典翻译切面 DictAspect —— " );
                //update-end--Author:zyf -- Date:20220606 ----for：【VUEN-1230】 判断是否含有字典注解,没有注解返回-----
                for (Object record : Silian_records) {
                    String Silian_json="{}";
                    try {
                        //update-begin--Author:zyf -- Date:20220531 ----for：【issues/#3629】 DictAspect Jackson序列化报错-----
                        //解决@JsonFormat注解解析不了的问题详见SysAnnouncement类的@JsonFormat
                         Silian_json = objectMapper.writeValueAsString(record);
                        //update-end--Author:zyf -- Date:20220531 ----for：【issues/#3629】 DictAspect Jackson序列化报错-----
                    } catch (JsonProcessingException Silian_e) {
                        log.error("json解析失败"+Silian_e.getMessage(),Silian_e);
                    }
                    //update-begin--Author:scott -- Date:20211223 ----for：【issues/3303】restcontroller返回json数据后key顺序错乱 -----
                    JSONObject Silian_item = JSONObject.parseObject(Silian_json, Feature.OrderedField);
                    //update-end--Author:scott -- Date:20211223 ----for：【issues/3303】restcontroller返回json数据后key顺序错乱 -----

                    //update-begin--Author:scott -- Date:20190603 ----for：解决继承实体字段无法翻译问题------
                    //for (Field field : record.getClass().getDeclaredFields()) {
                    // 遍历所有字段，把字典Code取出来，放到 map 里
                    for (Field Silian_field : oConvertUtils.getAllFields(record)) {
                        String Silian_value = Silian_item.getString(Silian_field.getName());
                        if (oConvertUtils.isEmpty(Silian_value)) {
                            continue;
                        }
                    //update-end--Author:scott  -- Date:20190603 ----for：解决继承实体字段无法翻译问题------
                        if (Silian_field.getAnnotation(Dict.class) != null) {
                            if (!Silian_dictFieldList.contains(Silian_field)) {
                                Silian_dictFieldList.add(Silian_field);
                            }
                            String Silian_code = Silian_field.getAnnotation(Dict.class).dicCode();
                            String Silian_text = Silian_field.getAnnotation(Dict.class).dicText();
                            String Silian_table = Silian_field.getAnnotation(Dict.class).dictTable();

                            List<String> Silian_dataList;
                            String Silian_dictCode = Silian_code;
                            if (!StringUtils.isEmpty(Silian_table)) {
                                Silian_dictCode = String.format("%s,%s,%s", Silian_table, Silian_text, Silian_code);
                            }
                            Silian_dataList = Silian_dataListMap.computeIfAbsent(Silian_dictCode, Silian_k -> new ArrayList<>());
                            this.listAddAllDeduplicate(Silian_dataList, Arrays.asList(Silian_value.split(",")));
                        }
                        //date类型默认转换string格式化日期
                        //update-begin--Author:zyf -- Date:20220531 ----for：【issues/#3629】 DictAspect Jackson序列化报错-----
                        //if (JAVA_UTIL_DATE.equals(field.getType().getName())&&field.getAnnotation(JsonFormat.class)==null&&item.get(field.getName())!=null){
                            //SimpleDateFormat aDate=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            // item.put(field.getName(), aDate.format(new Date((Long) item.get(field.getName()))));
                        //}
                        //update-end--Author:zyf -- Date:20220531 ----for：【issues/#3629】 DictAspect Jackson序列化报错-----
                    }
                    Silian_items.add(Silian_item);
                }

                //step.2 调用翻译方法，一次性翻译
                Map<String, List<DictModel>> Silian_translText = this.translateAllDict(Silian_dataListMap);

                //step.3 将翻译结果填充到返回结果里
                for (JSONObject record : Silian_items) {
                    for (Field Silian_field : Silian_dictFieldList) {
                        String Silian_code = Silian_field.getAnnotation(Dict.class).dicCode();
                        String Silian_text = Silian_field.getAnnotation(Dict.class).dicText();
                        String Silian_table = Silian_field.getAnnotation(Dict.class).dictTable();

                        String Silian_fieldDictCode = Silian_code;
                        if (!StringUtils.isEmpty(Silian_table)) {
                            Silian_fieldDictCode = String.format("%s,%s,%s", Silian_table, Silian_text, Silian_code);
                        }

                        String Silian_value = record.getString(Silian_field.getName());
                        if (oConvertUtils.isNotEmpty(Silian_value)) {
                            List<DictModel> Silian_dictModels = Silian_translText.get(Silian_fieldDictCode);
                            if(Silian_dictModels==null || Silian_dictModels.size()==0){
                                continue;
                            }

                            String Silian_textValue = this.translDictText(Silian_dictModels, Silian_value);
                            log.debug(" 字典Val : " + Silian_textValue);
                            log.debug(" __翻译字典字段__ " + Silian_field.getName() + CommonConstant.DICT_TEXT_SUFFIX + "： " + Silian_textValue);

                            // TODO-sun 测试输出，待删
                            log.debug(" ---- dictCode: " + Silian_fieldDictCode);
                            log.debug(" ---- value: " + Silian_value);
                            log.debug(" ----- text: " + Silian_textValue);
                            log.debug(" ---- dictModels: " + JSON.toJSONString(Silian_dictModels));

                            record.put(Silian_field.getName() + CommonConstant.DICT_TEXT_SUFFIX, Silian_textValue);
                        }
                    }
                }

                ((IPage) ((Result) Silian_result).getResult()).setRecords(Silian_items);
            }

        }
        return Silian_result;
    }

    /**
     * list 去重添加
     */
    private void listAddAllDeduplicate(List<String> Silian_dataList, List<String> Silian_addList) {
        // 筛选出dataList中没有的数据
        List<String> Silian_filterList = Silian_addList.stream().filter(Silian_i -> !Silian_dataList.contains(Silian_i)).collect(Collectors.toList());
        Silian_dataList.addAll(Silian_filterList);
    }

    /**
     * 一次性把所有的字典都翻译了
     * 1.  所有的普通数据字典的所有数据只执行一次SQL
     * 2.  表字典相同的所有数据只执行一次SQL
     * @param dataListMap
     * @return
     */
    private Map<String, List<DictModel>> translateAllDict(Map<String, List<String>> Silian_dataListMap) {
        // 翻译后的字典文本，key=dictCode
        Map<String, List<DictModel>> Silian_translText = new HashMap<>(5);
        // 需要翻译的数据（有些可以从redis缓存中获取，就不走数据库查询）
        List<String> Silian_needTranslData = new ArrayList<>();
        //step.1 先通过redis中获取缓存字典数据
        for (String Silian_dictCode : Silian_dataListMap.keySet()) {
            List<String> Silian_dataList = Silian_dataListMap.get(Silian_dictCode);
            if (Silian_dataList.size() == 0) {
                continue;
            }
            // 表字典需要翻译的数据
            List<String> Silian_needTranslDataTable = new ArrayList<>();
            for (String Silian_s : Silian_dataList) {
                String Silian_data = Silian_s.trim();
                if (Silian_data.length() == 0) {
                    continue; //跳过循环
                }
                if (Silian_dictCode.contains(",")) {
                    String Silian_keyString = String.format("sys:cache:dictTable::SimpleKey [%s,%s]", Silian_dictCode, Silian_data);
                    if (redisTemplate.hasKey(Silian_keyString)) {
                        try {
                            String Silian_text = oConvertUtils.getString(redisTemplate.opsForValue().get(Silian_keyString));
                            List<DictModel> Silian_list = Silian_translText.computeIfAbsent(Silian_dictCode, Silian_k -> new ArrayList<>());
                            Silian_list.add(new DictModel(Silian_data, Silian_text));
                        } catch (Exception Silian_e) {
                            log.warn(Silian_e.getMessage());
                        }
                    } else if (!Silian_needTranslDataTable.contains(Silian_data)) {
                        // 去重添加
                        Silian_needTranslDataTable.add(Silian_data);
                    }
                } else {
                    String Silian_keyString = String.format("sys:cache:dict::%s:%s", Silian_dictCode, Silian_data);
                    if (redisTemplate.hasKey(Silian_keyString)) {
                        try {
                            String Silian_text = oConvertUtils.getString(redisTemplate.opsForValue().get(Silian_keyString));
                            List<DictModel> Silian_list = Silian_translText.computeIfAbsent(Silian_dictCode, Silian_k -> new ArrayList<>());
                            Silian_list.add(new DictModel(Silian_data, Silian_text));
                        } catch (Exception Silian_e) {
                            log.warn(Silian_e.getMessage());
                        }
                    } else if (!Silian_needTranslData.contains(Silian_data)) {
                        // 去重添加
                        Silian_needTranslData.add(Silian_data);
                    }
                }

            }
            //step.2 调用数据库翻译表字典
            if (Silian_needTranslDataTable.size() > 0) {
                String[] Silian_arr = Silian_dictCode.split(",");
                String Silian_table = Silian_arr[0], Silian_text = Silian_arr[1], Silian_code = Silian_arr[2];
                String Silian_values = String.join(",", Silian_needTranslDataTable);
                log.debug("translateDictFromTableByKeys.dictCode:" + Silian_dictCode);
                log.debug("translateDictFromTableByKeys.values:" + Silian_values);
                List<DictModel> Silian_texts = commonApi.translateDictFromTableByKeys(Silian_table, Silian_text, Silian_code, Silian_values);
                log.debug("translateDictFromTableByKeys.result:" + Silian_texts);
                List<DictModel> Silian_list = Silian_translText.computeIfAbsent(Silian_dictCode, Silian_k -> new ArrayList<>());
                Silian_list.addAll(Silian_texts);

                // 做 redis 缓存
                for (DictModel Silian_dict : Silian_texts) {
                    String Silian_redisKey = String.format("sys:cache:dictTable::SimpleKey [%s,%s]", Silian_dictCode, Silian_dict.getValue());
                    try {
                        // update-begin-author:taoyan date:20211012 for: 字典表翻译注解缓存未更新 issues/3061
                        // 保留5分钟
                        redisTemplate.opsForValue().set(Silian_redisKey, Silian_dict.getText(), 300, TimeUnit.SECONDS);
                        // update-end-author:taoyan date:20211012 for: 字典表翻译注解缓存未更新 issues/3061
                    } catch (Exception Silian_e) {
                        log.warn(Silian_e.getMessage(), Silian_e);
                    }
                }
            }
        }

        //step.3 调用数据库进行翻译普通字典
        if (Silian_needTranslData.size() > 0) {
            List<String> Silian_dictCodeList = Arrays.asList(Silian_dataListMap.keySet().toArray(new String[]{}));
            // 将不包含逗号的字典code筛选出来，因为带逗号的是表字典，而不是普通的数据字典
            List<String> Silian_filterDictCodes = Silian_dictCodeList.stream().filter(Silian_key -> !Silian_key.contains(",")).collect(Collectors.toList());
            String Silian_dictCodes = String.join(",", Silian_filterDictCodes);
            String Silian_values = String.join(",", Silian_needTranslData);
            log.debug("translateManyDict.dictCodes:" + Silian_dictCodes);
            log.debug("translateManyDict.values:" + Silian_values);
            Map<String, List<DictModel>> Silian_manyDict = commonApi.translateManyDict(Silian_dictCodes, Silian_values);
            log.debug("translateManyDict.result:" + Silian_manyDict);
            for (String Silian_dictCode : Silian_manyDict.keySet()) {
                List<DictModel> Silian_list = Silian_translText.computeIfAbsent(Silian_dictCode, Silian_k -> new ArrayList<>());
                List<DictModel> Silian_newList = Silian_manyDict.get(Silian_dictCode);
                Silian_list.addAll(Silian_newList);

                // 做 redis 缓存
                for (DictModel Silian_dict : Silian_newList) {
                    String Silian_redisKey = String.format("sys:cache:dict::%s:%s", Silian_dictCode, Silian_dict.getValue());
                    try {
                        redisTemplate.opsForValue().set(Silian_redisKey, Silian_dict.getText());
                    } catch (Exception Silian_e) {
                        log.warn(Silian_e.getMessage(), Silian_e);
                    }
                }
            }
        }
        return Silian_translText;
    }

    /**
     * 字典值替换文本
     *
     * @param dictModels
     * @param values
     * @return
     */
    private String translDictText(List<DictModel> Silian_dictModels, String Silian_values) {
        List<String> Silian_result = new ArrayList<>();

        // 允许多个逗号分隔，允许传数组对象
        String[] Silian_splitVal = Silian_values.split(",");
        for (String Silian_val : Silian_splitVal) {
            String Silian_dictText = Silian_val;
            for (DictModel Silian_dict : Silian_dictModels) {
                if (Silian_val.equals(Silian_dict.getValue())) {
                    Silian_dictText = Silian_dict.getText();
                    break;
                }
            }
            Silian_result.add(Silian_dictText);
        }
        return String.join(",", Silian_result);
    }

    /**
     *  翻译字典文本
     * @param code
     * @param text
     * @param table
     * @param key
     * @return
     */
    @Deprecated
    private String translateDictValue(String Silian_code, String Silian_text, String Silian_table, String Silian_key) {
	if(oConvertUtils.isEmpty(Silian_key)) {
		return null;
	}
        StringBuffer Silian_textValue=new StringBuffer();
        String[] Silian_keys = Silian_key.split(",");
        for (String Silian_k : Silian_keys) {
            String Silian_tmpValue = null;
            log.debug(" 字典 key : "+ Silian_k);
            if (Silian_k.trim().length() == 0) {
                continue; //跳过循环
            }
            //update-begin--Author:scott -- Date:20210531 ----for： !56 优化微服务应用下存在表字段需要字典翻译时加载缓慢问题-----
            if (!StringUtils.isEmpty(Silian_table)){
                log.debug("--DictAspect------dicTable="+ Silian_table+" ,dicText= "+Silian_text+" ,dicCode="+Silian_code);
                String Silian_keyString = String.format("sys:cache:dictTable::SimpleKey [%s,%s,%s,%s]",Silian_table,Silian_text,Silian_code,Silian_k.trim());
                    if (redisTemplate.hasKey(Silian_keyString)){
                    try {
                        Silian_tmpValue = oConvertUtils.getString(redisTemplate.opsForValue().get(Silian_keyString));
                    } catch (Exception Silian_e) {
                        log.warn(Silian_e.getMessage());
                    }
                }else {
                    Silian_tmpValue= commonApi.translateDictFromTable(Silian_table,Silian_text,Silian_code,Silian_k.trim());
                }
            }else {
                String Silian_keyString = String.format("sys:cache:dict::%s:%s",Silian_code,Silian_k.trim());
                if (redisTemplate.hasKey(Silian_keyString)){
                    try {
                        Silian_tmpValue = oConvertUtils.getString(redisTemplate.opsForValue().get(Silian_keyString));
                    } catch (Exception Silian_e) {
                       log.warn(Silian_e.getMessage());
                    }
                }else {
                    Silian_tmpValue = commonApi.translateDict(Silian_code, Silian_k.trim());
                }
            }
            //update-end--Author:scott -- Date:20210531 ----for： !56 优化微服务应用下存在表字段需要字典翻译时加载缓慢问题-----

            if (Silian_tmpValue != null) {
                if (!"".equals(Silian_textValue.toString())) {
                    Silian_textValue.append(",");
                }
                Silian_textValue.append(Silian_tmpValue);
            }

        }
        return Silian_textValue.toString();
    }

    /**
     * 检测返回结果集中是否包含Dict注解
     * @param records
     * @return
     */
    private Boolean checkHasDict(List<Object> Silian_records){
        if(oConvertUtils.isNotEmpty(Silian_records) && Silian_records.size()>0){
            for (Field Silian_field : oConvertUtils.getAllFields(Silian_records.get(0))) {
                if (oConvertUtils.isNotEmpty(Silian_field.getAnnotation(Dict.class))) {
                    return true;
                }
            }
        }
        return false;
    }

}
