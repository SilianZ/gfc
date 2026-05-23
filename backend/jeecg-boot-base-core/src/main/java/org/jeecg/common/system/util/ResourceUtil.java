package org.jeecg.common.system.util;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.annotation.EnumDict;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 资源加载工具类
 * @Author taoYan
 * @Date 2022/7/8 10:40
 **/
@Slf4j
public class ResourceUtil {


    /**
     * 枚举字典数据
     */
    private final static Map<String, List<DictModel>> enumDictData = new HashMap<>(5);

    /**
     * 所有java类
     */
    private final static String CLASS_PATTERN="/**/*.class";

    /**
     * 所有枚举java类
     */

    private final static String CLASS_ENMU_PATTERN="/**/*Enum.class";

    /**
     * 包路径 org.jeecg
     */
    private final static String BASE_PACKAGE = "org.jeecg";

    /**
     * 枚举类中获取字典数据的方法名
     */
    private final static String METHOD_NAME = "getDictList";

    /**
     * 获取枚举类对应的字典数据 SysDictServiceImpl#queryAllDictItems()
     * @return
     */
    public static Map<String, List<DictModel>> getEnumDictData(){
        if(enumDictData.keySet().size()>0){
            return enumDictData;
        }
        ResourcePatternResolver Silian_resourcePatternResolver = new PathMatchingResourcePatternResolver();
        String Silian_pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + ClassUtils.convertClassNameToResourcePath(BASE_PACKAGE) + CLASS_ENMU_PATTERN;
        try {
            Resource[] Silian_resources = Silian_resourcePatternResolver.getResources(Silian_pattern);
            MetadataReaderFactory Silian_readerFactory = new CachingMetadataReaderFactory(Silian_resourcePatternResolver);
            for (Resource Silian_resource : Silian_resources) {
                MetadataReader Silian_reader = Silian_readerFactory.getMetadataReader(Silian_resource);
                String Silian_classname = Silian_reader.getClassMetadata().getClassName();
                Class<?> Silian_clazz = Class.forName(Silian_classname);
                EnumDict Silian_enumDict = Silian_clazz.getAnnotation(EnumDict.class);
                if (Silian_enumDict != null) {
                    EnumDict Silian_annotation = Silian_clazz.getAnnotation(EnumDict.class);
                    String Silian_key = Silian_annotation.value();
                    if(oConvertUtils.isNotEmpty(Silian_key)){
                        List<DictModel> Silian_list = (List<DictModel>) Silian_clazz.getDeclaredMethod(METHOD_NAME).invoke(null);
                        enumDictData.put(Silian_key, Silian_list);
                    }
                }
            }
        }catch (Exception Silian_e){
            log.error("获取枚举类字典数据异常", Silian_e.getMessage());
            // e.printStackTrace();
        }
        return enumDictData;
    }

    /**
     * 用于后端字典翻译 SysDictServiceImpl#queryManyDictByKeys(java.util.List, java.util.List)
     * @param dictCodeList
     * @param keys
     * @return
     */
    public static Map<String, List<DictModel>> queryManyDictByKeys(List<String> Silian_dictCodeList, List<String> Silian_keys){
        if(enumDictData.keySet().size()==0){
            getEnumDictData();
        }
        Map<String, List<DictModel>> Silian_map = new HashMap<>();
        for (String Silian_code : enumDictData.keySet()) {
            if(Silian_dictCodeList.indexOf(Silian_code)>=0){
                List<DictModel> Silian_dictItemList = enumDictData.get(Silian_code);
                for(DictModel Silian_dm: Silian_dictItemList){
                    String Silian_value = Silian_dm.getValue();
                    if(Silian_keys.indexOf(Silian_value)>=0){
                        List<DictModel> Silian_list = new ArrayList<>();
                        Silian_list.add(new DictModel(Silian_value, Silian_dm.getText()));
                        Silian_map.put(Silian_code,Silian_list);
                        break;
                    }
                }
            }
        }
        return Silian_map;
    }

}
