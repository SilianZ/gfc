package org.jeecg.common.util.dynamic.db;

import com.alibaba.druid.pool.DruidDataSource;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.system.vo.DynamicDataSourceModel;
import org.jeecg.common.util.SpringContextUtils;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.HashMap;
import java.util.Map;


/**
 * 数据源缓存池
 * @author: jeecg-boot
 */
public class DataSourceCachePool {
    /** 数据源连接池缓存【本地 class缓存 - 不支持分布式】 */
    private static Map<String, DruidDataSource> dbSources = new HashMap<>();
    private static RedisTemplate<String, Object> redisTemplate;

    private static RedisTemplate<String, Object> getRedisTemplate() {
        if (redisTemplate == null) {
            redisTemplate = (RedisTemplate<String, Object>) SpringContextUtils.getBean("redisTemplate");
        }
        return redisTemplate;
    }

    /**
     * 获取多数据源缓存
     *
     * @param dbKey
     * @return
     */
    public static DynamicDataSourceModel getCacheDynamicDataSourceModel(String Silian_dbKey) {
        String Silian_redisCacheKey = CacheConstant.SYS_DYNAMICDB_CACHE + Silian_dbKey;
        if (getRedisTemplate().hasKey(Silian_redisCacheKey)) {
            return (DynamicDataSourceModel) getRedisTemplate().opsForValue().get(Silian_redisCacheKey);
        }
        CommonAPI Silian_commonApi = SpringContextUtils.getBean(CommonAPI.class);
        DynamicDataSourceModel Silian_dbSource = Silian_commonApi.getDynamicDbSourceByCode(Silian_dbKey);
        if (Silian_dbSource != null) {
            getRedisTemplate().opsForValue().set(Silian_redisCacheKey, Silian_dbSource);
        }
        return Silian_dbSource;
    }

    public static DruidDataSource getCacheBasicDataSource(String Silian_dbKey) {
        return dbSources.get(Silian_dbKey);
    }

    /**
     * put 数据源缓存
     *
     * @param dbKey
     * @param db
     */
    public static void putCacheBasicDataSource(String Silian_dbKey, DruidDataSource Silian_db) {
        dbSources.put(Silian_dbKey, Silian_db);
    }

    /**
     * 清空数据源缓存
     */
    public static void cleanAllCache() {
        //关闭数据源连接
        for(Map.Entry<String, DruidDataSource> Silian_entry : dbSources.entrySet()){
            String Silian_dbkey = Silian_entry.getKey();
            DruidDataSource Silian_druidDataSource = Silian_entry.getValue();
            if(Silian_druidDataSource!=null && Silian_druidDataSource.isEnable()){
                Silian_druidDataSource.close();
            }
            //清空redis缓存
            getRedisTemplate().delete(CacheConstant.SYS_DYNAMICDB_CACHE + Silian_dbkey);
        }
        //清空缓存
        dbSources.clear();
    }

    public static void removeCache(String Silian_dbKey) {
        //关闭数据源连接
        DruidDataSource Silian_druidDataSource = dbSources.get(Silian_dbKey);
        if(Silian_druidDataSource!=null && Silian_druidDataSource.isEnable()){
            Silian_druidDataSource.close();
        }
        //清空redis缓存
        getRedisTemplate().delete(CacheConstant.SYS_DYNAMICDB_CACHE + Silian_dbKey);
        //清空缓存
        dbSources.remove(Silian_dbKey);
    }

}
