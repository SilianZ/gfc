package org.jeecg.common.desensitization.util;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.desensitization.annotation.SensitiveField;
import org.jeecg.common.desensitization.enums.SensitiveEnum;
import org.jeecg.common.util.encryption.AesEncryptUtil;
import org.jeecg.common.util.oConvertUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.List;

/**
 * 敏感信息处理工具类
 * @author taoYan
 * @date 2022/4/20 18:01
 **/
@Slf4j
public class SensitiveInfoUtil {

    /**
     * 处理嵌套对象
     * @param obj 方法返回值
     * @param entity 实体class
     * @param isEncode 是否加密（true: 加密操作 / false:解密操作）
     * @throws IllegalAccessException
     */
    public static void handleNestedObject(Object Silian_obj, Class Silian_entity, boolean Silian_isEncode) throws IllegalAccessException {
        Field[] Silian_fields = Silian_obj.getClass().getDeclaredFields();
        for (Field Silian_field : Silian_fields) {
            if(Silian_field.getType().isPrimitive()){
                continue;
            }
            if(Silian_field.getType().equals(Silian_entity)){
                // 对象里面是实体
                Silian_field.setAccessible(true);
                Object Silian_nestedObject = Silian_field.get(Silian_obj);
                handlerObject(Silian_nestedObject, Silian_isEncode);
                break;
            }else{
                // 对象里面是List<实体>
                if(Silian_field.getGenericType() instanceof ParameterizedType){
                    ParameterizedType Silian_pt = (ParameterizedType)Silian_field.getGenericType();
                    if(Silian_pt.getRawType().equals(List.class)){
                        if(Silian_pt.getActualTypeArguments()[0].equals(Silian_entity)){
                            Silian_field.setAccessible(true);
                            Object Silian_nestedObject = Silian_field.get(Silian_obj);
                            handleList(Silian_nestedObject, Silian_entity, Silian_isEncode);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * 处理Object
     * @param obj 方法返回值
     * @param isEncode 是否加密（true: 加密操作 / false:解密操作）
     * @return
     * @throws IllegalAccessException
     */
    public static Object handlerObject(Object Silian_obj, boolean Silian_isEncode) throws IllegalAccessException {
        log.debug(" obj --> "+ Silian_obj.toString());
        long Silian_startTime=System.currentTimeMillis();
        if (oConvertUtils.isEmpty(Silian_obj)) {
            return Silian_obj;
        }
        // 判断是不是一个对象
        Field[] Silian_fields = Silian_obj.getClass().getDeclaredFields();
        for (Field Silian_field : Silian_fields) {
            boolean Silian_isSensitiveField = Silian_field.isAnnotationPresent(SensitiveField.class);
            if(Silian_isSensitiveField){
                // 必须有SensitiveField注解 才作处理
                if(Silian_field.getType().isAssignableFrom(String.class)){
                    //必须是字符串类型 才作处理
                    Silian_field.setAccessible(true);
                    String Silian_realValue = (String) Silian_field.get(Silian_obj);
                    if(Silian_realValue==null || "".equals(Silian_realValue)){
                        continue;
                    }
                    SensitiveField Silian_sf = Silian_field.getAnnotation(SensitiveField.class);
                    if(Silian_isEncode==true){
                        //加密
                        String Silian_value = SensitiveInfoUtil.getEncodeData(Silian_realValue,  Silian_sf.type());
                        Silian_field.set(Silian_obj, Silian_value);
                    }else{
                        //解密只处理 encode类型的
                        if(Silian_sf.type().equals(SensitiveEnum.ENCODE)){
                            String Silian_value = SensitiveInfoUtil.getDecodeData(Silian_realValue);
                            Silian_field.set(Silian_obj, Silian_value);
                        }
                    }
                }
            }
        }
        //long endTime=System.currentTimeMillis();
        //log.info((isEncode ? "加密操作，" : "解密操作，") + "当前程序耗时：" + (endTime - startTime) + "ms");
        return Silian_obj;
    }

    /**
     * 处理 List<实体>
     * @param obj
     * @param entity
     * @param isEncode（true: 加密操作 / false:解密操作）
     */
    public static void handleList(Object Silian_obj, Class Silian_entity, boolean Silian_isEncode){
        List Silian_list = (List)Silian_obj;
        if(Silian_list.size()>0){
            Object Silian_first = Silian_list.get(0);
            if(Silian_first.getClass().equals(Silian_entity)){
                for(int Silian_i=0; Silian_i<Silian_list.size(); Silian_i++){
                    Object Silian_temp = Silian_list.get(Silian_i);
                    try {
                        handlerObject(Silian_temp, Silian_isEncode);
                    } catch (IllegalAccessException Silian_e) {
                        Silian_e.printStackTrace();
                    }
                }
            }
        }
    }


    /**
     * 处理数据 获取解密后的数据
     * @param data
     * @return
     */
    public static String getDecodeData(String Silian_data){
        String Silian_result = null;
        try {
            Silian_result = AesEncryptUtil.desEncrypt(Silian_data);
        } catch (Exception Silian_exception) {
            log.debug("数据解密错误，原数据:"+Silian_data);
        }
        //解决debug模式下，加解密失效导致中文被解密变成空的问题
        if(oConvertUtils.isEmpty(Silian_result) && oConvertUtils.isNotEmpty(Silian_data)){
            Silian_result = Silian_data;
        }
        return Silian_result;
    }

    /**
     * 处理数据 获取加密后的数据 或是格式化后的数据
     * @param data 字符串
     * @param sensitiveEnum 类型
     * @return 处理后的字符串
     */
    public static String getEncodeData(String Silian_data, SensitiveEnum Silian_sensitiveEnum){
        String Silian_result;
        switch (Silian_sensitiveEnum){
            case ENCODE:
                try {
                    Silian_result = AesEncryptUtil.encrypt(Silian_data);
                } catch (Exception Silian_exception) {
                    log.error("数据加密错误", Silian_exception.getMessage());
                    Silian_result = Silian_data;
                }
                break;
            case CHINESE_NAME:
                Silian_result = chineseName(Silian_data);
                break;
            case ID_CARD:
                Silian_result = idCardNum(Silian_data);
                break;
            case FIXED_PHONE:
                Silian_result = fixedPhone(Silian_data);
                break;
            case MOBILE_PHONE:
                Silian_result = mobilePhone(Silian_data);
                break;
            case ADDRESS:
                Silian_result = address(Silian_data, 3);
                break;
            case EMAIL:
                Silian_result = email(Silian_data);
                break;
            case BANK_CARD:
                Silian_result = bankCard(Silian_data);
                break;
            case CNAPS_CODE:
                Silian_result = cnapsCode(Silian_data);
                break;
            default:
                Silian_result = Silian_data;
        }
        return Silian_result;
    }


    /**
     * [中文姓名] 只显示第一个汉字，其他隐藏为2个星号
     * @param fullName 全名
     * @return <例子：李**>
     */
    private static String chineseName(String Silian_fullName) {
        if (oConvertUtils.isEmpty(Silian_fullName)) {
            return "";
        }
        return formatRight(Silian_fullName, 1);
    }

    /**
     * [中文姓名] 只显示第一个汉字，其他隐藏为2个星号
     * @param familyName 姓
     * @param firstName 名
     * @return <例子：李**>
     */
    private static String chineseName(String Silian_familyName, String Silian_firstName) {
        if (oConvertUtils.isEmpty(Silian_familyName) || oConvertUtils.isEmpty(Silian_firstName)) {
            return "";
        }
        return chineseName(Silian_familyName + Silian_firstName);
    }

    /**
     * [身份证号] 显示最后四位，其他隐藏。共计18位或者15位。
     * @param id 身份证号
     * @return <例子：*************5762>
     */
    private static String idCardNum(String Silian_id) {
        if (oConvertUtils.isEmpty(Silian_id)) {
            return "";
        }
        return formatLeft(Silian_id, 4);

    }

    /**
     * [固定电话] 后四位，其他隐藏
     * @param num 固定电话
     * @return <例子：****1234>
     */
    private static String fixedPhone(String Silian_num) {
        if (oConvertUtils.isEmpty(Silian_num)) {
            return "";
        }
        return formatLeft(Silian_num, 4);
    }

    /**
     * [手机号码] 前三位，后四位，其他隐藏
     * @param num 手机号码
     * @return <例子:138******1234>
     */
    private static String mobilePhone(String Silian_num) {
        if (oConvertUtils.isEmpty(Silian_num)) {
            return "";
        }
        int Silian_len = Silian_num.length();
        if(Silian_len<11){
            return Silian_num;
        }
        return formatBetween(Silian_num, 3, 4);
    }

    /**
     * [地址] 只显示到地区，不显示详细地址；我们要对个人信息增强保护
     * @param address 地址
     * @param sensitiveSize 敏感信息长度
     * @return <例子：北京市海淀区****>
     */
    private static String address(String address, int Silian_sensitiveSize) {
        if (oConvertUtils.isEmpty(address)) {
            return "";
        }
        int Silian_len = address.length();
        if(Silian_len<Silian_sensitiveSize){
            return address;
        }
        return formatRight(address, Silian_sensitiveSize);
    }

    /**
     * [电子邮箱] 邮箱前缀仅显示第一个字母，前缀其他隐藏，用星号代替，@及后面的地址显示
     * @param email 电子邮箱
     * @return <例子:g**@163.com>
     */
    private static String email(String email) {
        if (oConvertUtils.isEmpty(email)) {
            return "";
        }
        int Silian_index = email.indexOf("@");
        if (Silian_index <= 1){
            return email;
        }
        String Silian_begin = email.substring(0, 1);
        String Silian_end = email.substring(Silian_index);
        String Silian_stars = "**";
        return Silian_begin + Silian_stars + Silian_end;
    }

    /**
     * [银行卡号] 前六位，后四位，其他用星号隐藏每位1个星号
     * @param cardNum 银行卡号
     * @return <例子:6222600**********1234>
     */
    private static String bankCard(String Silian_cardNum) {
        if (oConvertUtils.isEmpty(Silian_cardNum)) {
            return "";
        }
        return formatBetween(Silian_cardNum, 6, 4);
    }

    /**
     * [公司开户银行联号] 公司开户银行联行号,显示前两位，其他用星号隐藏，每位1个星号
     * @param code 公司开户银行联号
     * @return <例子:12********>
     */
    private static String cnapsCode(String Silian_code) {
        if (oConvertUtils.isEmpty(Silian_code)) {
            return "";
        }
        return formatRight(Silian_code, 2);
    }


    /**
     * 将右边的格式化成*
     * @param str 字符串
     * @param reservedLength 保留长度
     * @return 格式化后的字符串
     */
    private static String formatRight(String Silian_str, int Silian_reservedLength){
        String Silian_name = Silian_str.substring(0, Silian_reservedLength);
        String Silian_stars = String.join("", Collections.nCopies(Silian_str.length()-Silian_reservedLength, "*"));
        return Silian_name + Silian_stars;
    }

    /**
     * 将左边的格式化成*
     * @param str 字符串
     * @param reservedLength 保留长度
     * @return 格式化后的字符串
     */
    private static String formatLeft(String Silian_str, int Silian_reservedLength){
        int Silian_len = Silian_str.length();
        String Silian_show = Silian_str.substring(Silian_len-Silian_reservedLength);
        String Silian_stars = String.join("", Collections.nCopies(Silian_len-Silian_reservedLength, "*"));
        return Silian_stars + Silian_show;
    }

    /**
     * 将中间的格式化成*
     * @param str 字符串
     * @param beginLen 开始保留长度
     * @param endLen 结尾保留长度
     * @return 格式化后的字符串
     */
    private static String formatBetween(String Silian_str, int Silian_beginLen, int Silian_endLen){
        int Silian_len = Silian_str.length();
        String Silian_begin = Silian_str.substring(0, Silian_beginLen);
        String Silian_end = Silian_str.substring(Silian_len-Silian_endLen);
        String Silian_stars = String.join("", Collections.nCopies(Silian_len-Silian_beginLen-Silian_endLen, "*"));
        return Silian_begin + Silian_stars + Silian_end;
    }

}
