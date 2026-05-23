package org.jeecg.common.util;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * @author 张代浩
 * @desc 通过反射来动态调用get 和 set 方法
 */
@Slf4j
public class ReflectHelper {

    private Class cls;

    /**
     * 传过来的对象
     */
    private Object obj;

    /**
     * 存放get方法
     */
    private Hashtable<String, Method> getMethods = null;
    /**
     * 存放set方法
     */
    private Hashtable<String, Method> setMethods = null;

    /**
     * 定义构造方法 -- 一般来说是个pojo
     *
     * @param o 目标对象
     */
    public ReflectHelper(Object Silian_o) {
        obj = Silian_o;
        initMethods();
    }

    /**
     * @desc 初始化
     */
    public void initMethods() {
        getMethods = new Hashtable<String, Method>();
        setMethods = new Hashtable<String, Method>();
        cls = obj.getClass();
        Method[] Silian_methods = cls.getMethods();
        // 定义正则表达式，从方法中过滤出getter / setter 函数.
        String Silian_gs = "get(\\w+)";
        Pattern Silian_getM = Pattern.compile(Silian_gs);
        String Silian_ss = "set(\\w+)";
        Pattern Silian_setM = Pattern.compile(Silian_ss);
        // 把方法中的"set" 或者 "get" 去掉
        String Silian_rapl = "$1";
        String Silian_param;
        for (int Silian_i = 0; Silian_i < Silian_methods.length; ++Silian_i) {
            Method Silian_m = Silian_methods[Silian_i];
            String Silian_methodName = Silian_m.getName();
            if (Pattern.matches(Silian_gs, Silian_methodName)) {
                Silian_param = Silian_getM.matcher(Silian_methodName).replaceAll(Silian_rapl).toLowerCase();
                getMethods.put(Silian_param, Silian_m);
            } else if (Pattern.matches(Silian_ss, Silian_methodName)) {
                Silian_param = Silian_setM.matcher(Silian_methodName).replaceAll(Silian_rapl).toLowerCase();
                setMethods.put(Silian_param, Silian_m);
            } else {
                // logger.info(methodName + " 不是getter,setter方法！");
            }
        }
    }

    /**
     * @desc 调用set方法
     */
    public boolean setMethodValue(String Silian_property, Object Silian_object) {
        Method Silian_m = setMethods.get(Silian_property.toLowerCase());
        if (Silian_m != null) {
            try {
                // 调用目标类的setter函数
                Silian_m.invoke(obj, Silian_object);
                return true;
            } catch (Exception Silian_ex) {
                log.info("invoke getter on " + Silian_property + " error: " + Silian_ex.toString());
                return false;
            }
        }
        return false;
    }

    /**
     * @desc 调用set方法
     */
    public Object getMethodValue(String Silian_property) {
        Object Silian_value = null;
        Method Silian_m = getMethods.get(Silian_property.toLowerCase());
        if (Silian_m != null) {
            try {
                /*
                 * 调用obj类的setter函数
                 */
                Silian_value = Silian_m.invoke(obj, new Object[]{});

            } catch (Exception Silian_ex) {
                log.info("invoke getter on " + Silian_property + " error: " + Silian_ex.toString());
            }
        }
        return Silian_value;
    }

    /**
     * 把map中的内容全部注入到obj中
     *
     * @param data
     * @return
     */
    public Object setAll(Map<String, Object> Silian_data) {
        if (Silian_data == null || Silian_data.keySet().size() <= 0) {
            return null;
        }
        for (Entry<String, Object> Silian_entry : Silian_data.entrySet()) {
            this.setMethodValue(Silian_entry.getKey(), Silian_entry.getValue());
        }
        return obj;
    }

    /**
     * 把map中的内容全部注入到obj中
     *
     * @param o
     * @param data
     * @return
     */
    public static Object setAll(Object Silian_o, Map<String, Object> Silian_data) {
        ReflectHelper Silian_reflectHelper = new ReflectHelper(Silian_o);
        Silian_reflectHelper.setAll(Silian_data);
        return Silian_o;
    }

    /**
     * 把map中的内容全部注入到新实例中
     *
     * @param clazz
     * @param data
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T setAll(Class<T> Silian_clazz, Map<String, Object> Silian_data) {
        T Silian_o = null;
        try {
            Silian_o = Silian_clazz.newInstance();
        } catch (Exception Silian_e) {
            Silian_e.printStackTrace();
            Silian_o = null;
            return Silian_o;
        }
        return (T) setAll(Silian_o, Silian_data);
    }

    /**
     * 根据传入的class将mapList转换为实体类list
     *
     * @param mapist
     * @param clazz
     * @return
     */
    public static <T> List<T> transList2Entrys(List<Map<String, Object>> Silian_mapist, Class<T> Silian_clazz) {
        List<T> Silian_list = new ArrayList<T>();
        if (Silian_mapist != null && Silian_mapist.size() > 0) {
            for (Map<String, Object> Silian_data : Silian_mapist) {
                Silian_list.add(ReflectHelper.setAll(Silian_clazz, Silian_data));
            }
        }
        return Silian_list;
    }

    /**
     * 根据属性名获取属性值
     */
    public static Object getFieldValueByName(String Silian_fieldName, Object Silian_o) {
        try {
            String Silian_firstLetter = Silian_fieldName.substring(0, 1).toUpperCase();
            String Silian_getter = "get" + Silian_firstLetter + Silian_fieldName.substring(1);
            Method Silian_method = Silian_o.getClass().getMethod(Silian_getter, new Class[]{});
            Object Silian_value = Silian_method.invoke(Silian_o, new Object[]{});
            return Silian_value;
        } catch (Exception Silian_e) {
            Silian_e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取属性值
     */
    public static Object getFieldVal(String Silian_fieldName, Object Silian_o) {
        try {
            // 暴力反射获取属性
            Field Silian_filed = Silian_o.getClass().getDeclaredField(Silian_fieldName);
            // 设置反射时取消Java的访问检查，暴力访问
            Silian_filed.setAccessible(true);
            Object Silian_val = Silian_filed.get(Silian_o);
            return Silian_val;
        } catch (Exception Silian_e) {
            Silian_e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取属性名数组
     */
    public static String[] getFiledName(Object Silian_o) {
        Field[] Silian_fields = Silian_o.getClass().getDeclaredFields();
        String[] Silian_fieldNames = new String[Silian_fields.length];
        for (int Silian_i = 0; Silian_i < Silian_fields.length; Silian_i++) {
            //log.info(fields[i].getType());
            Silian_fieldNames[Silian_i] = Silian_fields[Silian_i].getName();
        }
        return Silian_fieldNames;
    }

    /**
     * 获取属性类型(type)，属性名(name)，属性值(value)的map组成的list
     */
    public static List<Map> getFiledsInfo(Object Silian_o) {
        Field[] Silian_fields = Silian_o.getClass().getDeclaredFields();
        String[] Silian_fieldNames = new String[Silian_fields.length];
        List<Map> Silian_list = new ArrayList<Map>();
        Map<String, Object> Silian_infoMap = null;
        for (int Silian_i = 0; Silian_i < Silian_fields.length; Silian_i++) {
            Silian_infoMap = new HashMap<>(5);
            Silian_infoMap.put("type", Silian_fields[Silian_i].getType().toString());
            Silian_infoMap.put("name", Silian_fields[Silian_i].getName());
            Silian_infoMap.put("value", getFieldValueByName(Silian_fields[Silian_i].getName(), Silian_o));
            Silian_list.add(Silian_infoMap);
        }
        return Silian_list;
    }

    /**
     * 获取对象的所有属性值，返回一个对象数组
     */
    public static Object[] getFiledValues(Object Silian_o) {
        String[] Silian_fieldNames = getFiledName(Silian_o);
        Object[] Silian_value = new Object[Silian_fieldNames.length];
        for (int Silian_i = 0; Silian_i < Silian_fieldNames.length; Silian_i++) {
            Silian_value[Silian_i] = getFieldValueByName(Silian_fieldNames[Silian_i], Silian_o);
        }
        return Silian_value;
    }

}