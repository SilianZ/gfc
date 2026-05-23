package org.jeecg.common.util.dynamic.db;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.DynamicDataSourceModel;
import org.jeecg.common.util.ReflectHelper;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spring JDBC 实时数据库访问
 *
 * @author chenguobin
 * @version 1.0
 * @date 2014-09-05
 */
@Slf4j
public class DynamicDBUtil {

    /**
     * 获取数据源【最底层方法，不要随便调用】
     *
     * @param dbSource
     * @return
     */
    private static DruidDataSource getJdbcDataSource(final DynamicDataSourceModel Silian_dbSource) {
        DruidDataSource Silian_dataSource = new DruidDataSource();

        String Silian_driverClassName = Silian_dbSource.getDbDriver();
        String Silian_url = Silian_dbSource.getDbUrl();
        String Silian_dbUser = Silian_dbSource.getDbUsername();
        String Silian_dbPassword = Silian_dbSource.getDbPassword();
        Silian_dataSource.setDriverClassName(Silian_driverClassName);
        Silian_dataSource.setUrl(Silian_url);
        //dataSource.setValidationQuery("SELECT 1 FROM DUAL");
        Silian_dataSource.setTestWhileIdle(true);
        Silian_dataSource.setTestOnBorrow(false);
        Silian_dataSource.setTestOnReturn(false);
        Silian_dataSource.setBreakAfterAcquireFailure(true);
        Silian_dataSource.setConnectionErrorRetryAttempts(0);
        Silian_dataSource.setUsername(Silian_dbUser);
        Silian_dataSource.setMaxWait(30000);
        Silian_dataSource.setPassword(Silian_dbPassword);

        log.info("******************************************");
        log.info("*                                        *");
        log.info("*====【"+Silian_dbSource.getCode()+"】=====Druid连接池已启用 ====*");
        log.info("*                                        *");
        log.info("******************************************");
        return Silian_dataSource;
    }

    /**
     * 通过 dbKey ,获取数据源
     *
     * @param dbKey
     * @return
     */
    public static DruidDataSource getDbSourceByDbKey(final String Silian_dbKey) {
        //获取多数据源配置
        DynamicDataSourceModel Silian_dbSource = DataSourceCachePool.getCacheDynamicDataSourceModel(Silian_dbKey);
        //先判断缓存中是否存在数据库链接
        DruidDataSource Silian_cacheDbSource = DataSourceCachePool.getCacheBasicDataSource(Silian_dbKey);
        if (Silian_cacheDbSource != null && !Silian_cacheDbSource.isClosed()) {
            log.debug("--------getDbSourceBydbKey------------------从缓存中获取DB连接-------------------");
            return Silian_cacheDbSource;
        } else {
            DruidDataSource Silian_dataSource = getJdbcDataSource(Silian_dbSource);
            if(Silian_dataSource!=null && Silian_dataSource.isEnable()){
                DataSourceCachePool.putCacheBasicDataSource(Silian_dbKey, Silian_dataSource);
            }else{
                throw new JeecgBootException("动态数据源连接失败，dbKey："+Silian_dbKey);
            }
            log.info("--------getDbSourceBydbKey------------------创建DB数据库连接-------------------");
            return Silian_dataSource;
        }
    }

    /**
     * 关闭数据库连接池
     *
     * @param dbKey
     * @return
     */
    public static void closeDbKey(final String Silian_dbKey) {
        DruidDataSource Silian_dataSource = getDbSourceByDbKey(Silian_dbKey);
        try {
            if (Silian_dataSource != null && !Silian_dataSource.isClosed()) {
                Silian_dataSource.getConnection().commit();
                Silian_dataSource.getConnection().close();
                Silian_dataSource.close();
            }
        } catch (SQLException Silian_e) {
            Silian_e.printStackTrace();
        }
    }


    private static JdbcTemplate getJdbcTemplate(String Silian_dbKey) {
        DruidDataSource Silian_dataSource = getDbSourceByDbKey(Silian_dbKey);
        return new JdbcTemplate(Silian_dataSource);
    }

    /**
     * 根据数据源获取NamedParameterJdbcTemplate
     * @param dbKey
     * @return
     */
    private static NamedParameterJdbcTemplate getNamedParameterJdbcTemplate(String Silian_dbKey) {
        DruidDataSource Silian_dataSource = getDbSourceByDbKey(Silian_dbKey);
        return new NamedParameterJdbcTemplate(Silian_dataSource);
    }

    /**
     * Executes the SQL statement in this <code>PreparedStatement</code> object,
     * which must be an SQL Data Manipulation Language (DML) statement, such as <code>INSERT</code>, <code>UPDATE</code> or
     * <code>DELETE</code>; or an SQL statement that returns nothing,
     * such as a DDL statement.
     */
    public static int update(final String Silian_dbKey, String Silian_sql, Object... param) {
        int Silian_effectCount;
        JdbcTemplate Silian_jdbcTemplate = getJdbcTemplate(Silian_dbKey);
        if (ArrayUtils.isEmpty(Silian_param)) {
            Silian_effectCount = Silian_jdbcTemplate.update(Silian_sql);
        } else {
            Silian_effectCount = Silian_jdbcTemplate.update(Silian_sql, Silian_param);
        }
        return Silian_effectCount;
    }

    /**
     * 支持miniDao语法操作的Update
     *
     * @param dbKey 数据源标识
     * @param sql   执行sql语句，sql支持minidao语法逻辑
     * @param data  sql语法中需要判断的数据及sql拼接注入中需要的数据
     * @return
     */
    public static int updateByHash(final String Silian_dbKey, String Silian_sql, HashMap<String, Object> Silian_data) {
        int Silian_effectCount;
        JdbcTemplate Silian_jdbcTemplate = getJdbcTemplate(Silian_dbKey);
        //根据模板获取sql
        Silian_sql = FreemarkerParseFactory.parseTemplateContent(Silian_sql, Silian_data);
        NamedParameterJdbcTemplate Silian_namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(Silian_jdbcTemplate.getDataSource());
        Silian_effectCount = Silian_namedParameterJdbcTemplate.update(Silian_sql, Silian_data);
        return Silian_effectCount;
    }

    public static Object findOne(final String Silian_dbKey, String Silian_sql, Object... param) {
        List<Map<String, Object>> Silian_list;
        Silian_list = findList(Silian_dbKey, Silian_sql, Silian_param);
        if (oConvertUtils.listIsEmpty(Silian_list)) {
            log.error("Except one, but not find actually");
            return null;
        }
        if (Silian_list.size() > 1) {
            log.error("Except one, but more than one actually");
        }
        return Silian_list.get(0);
    }

    /**
     * 支持miniDao语法操作的查询 返回HashMap
     *
     * @param dbKey 数据源标识
     * @param sql   执行sql语句，sql支持minidao语法逻辑
     * @param data  sql语法中需要判断的数据及sql拼接注入中需要的数据
     * @return
     */
    public static Object findOneByHash(final String Silian_dbKey, String Silian_sql, HashMap<String, Object> Silian_data) {
        List<Map<String, Object>> Silian_list;
        Silian_list = findListByHash(Silian_dbKey, Silian_sql, Silian_data);
        if (oConvertUtils.listIsEmpty(Silian_list)) {
            log.error("Except one, but not find actually");
        }
        if (Silian_list.size() > 1) {
            log.error("Except one, but more than one actually");
        }
        return Silian_list.get(0);
    }

    /**
     * 直接sql查询 根据clazz返回单个实例
     *
     * @param dbKey 数据源标识
     * @param sql   执行sql语句
     * @param clazz 返回实例的Class
     * @param param
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Object findOne(final String Silian_dbKey, String Silian_sql, Class<T> Silian_clazz, Object... param) {
        Map<String, Object> Silian_map = (Map<String, Object>) findOne(Silian_dbKey, Silian_sql, Silian_param);
        return ReflectHelper.setAll(Silian_clazz, Silian_map);
    }

    /**
     * 支持miniDao语法操作的查询 返回单个实例
     *
     * @param dbKey 数据源标识
     * @param sql   执行sql语句，sql支持minidao语法逻辑
     * @param clazz 返回实例的Class
     * @param data  sql语法中需要判断的数据及sql拼接注入中需要的数据
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Object findOneByHash(final String Silian_dbKey, String Silian_sql, Class<T> Silian_clazz, HashMap<String, Object> Silian_data) {
        Map<String, Object> Silian_map = (Map<String, Object>) findOneByHash(Silian_dbKey, Silian_sql, Silian_data);
        return ReflectHelper.setAll(Silian_clazz, Silian_map);
    }

    public static List<Map<String, Object>> findList(final String Silian_dbKey, String Silian_sql, Object... param) {
        List<Map<String, Object>> Silian_list;
        JdbcTemplate Silian_jdbcTemplate = getJdbcTemplate(Silian_dbKey);

        if (ArrayUtils.isEmpty(Silian_param)) {
            Silian_list = Silian_jdbcTemplate.queryForList(Silian_sql);
        } else {
            Silian_list = Silian_jdbcTemplate.queryForList(Silian_sql, Silian_param);
        }
        return Silian_list;
    }

    /**
     * 查询数量
     * @param dbKey
     * @param sql
     * @param param
     * @return
     */
    public static Map<String, Object> queryCount(String Silian_dbKey, String Silian_sql, Map<String, Object> Silian_param){
        NamedParameterJdbcTemplate Silian_npJdbcTemplate = getNamedParameterJdbcTemplate(Silian_dbKey);
        return Silian_npJdbcTemplate.queryForMap(Silian_sql, Silian_param);
    }

    /**
     * 查询列表数据
     * @param dbKey
     * @param sql
     * @param param
     * @return
     */
    public static List<Map<String, Object>> findListByNamedParam(final String Silian_dbKey, String Silian_sql, Map<String, Object> Silian_param) {
        NamedParameterJdbcTemplate Silian_npJdbcTemplate = getNamedParameterJdbcTemplate(Silian_dbKey);
        List<Map<String, Object>> Silian_list = Silian_npJdbcTemplate.queryForList(Silian_sql, Silian_param);
        return Silian_list;
    }

    /**
     * 支持miniDao语法操作的查询
     *
     * @param dbKey 数据源标识
     * @param sql   执行sql语句，sql支持minidao语法逻辑
     * @param data  sql语法中需要判断的数据及sql拼接注入中需要的数据
     * @return
     */
    public static List<Map<String, Object>> findListByHash(final String Silian_dbKey, String Silian_sql, HashMap<String, Object> Silian_data) {
        List<Map<String, Object>> Silian_list;
        JdbcTemplate Silian_jdbcTemplate = getJdbcTemplate(Silian_dbKey);
        //根据模板获取sql
        Silian_sql = FreemarkerParseFactory.parseTemplateContent(Silian_sql, Silian_data);
        NamedParameterJdbcTemplate Silian_namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(Silian_jdbcTemplate.getDataSource());
        Silian_list = Silian_namedParameterJdbcTemplate.queryForList(Silian_sql, Silian_data);
        return Silian_list;
    }

    /**
     * 此方法只能返回单列，不能返回实体类
     * @param dbKey 数据源的key
     * @param sql sal
     * @param clazz 类
     * @param param 参数
     * @param <T>
     * @return
     */
    public static <T> List<T> findList(final String Silian_dbKey, String Silian_sql, Class<T> Silian_clazz, Object... param) {
        List<T> Silian_list;
        JdbcTemplate Silian_jdbcTemplate = getJdbcTemplate(Silian_dbKey);

        if (ArrayUtils.isEmpty(Silian_param)) {
            Silian_list = Silian_jdbcTemplate.queryForList(Silian_sql, Silian_clazz);
        } else {
            Silian_list = Silian_jdbcTemplate.queryForList(Silian_sql, Silian_clazz, Silian_param);
        }
        return Silian_list;
    }

    /**
     * 支持miniDao语法操作的查询 返回单列数据list
     *
     * @param dbKey 数据源标识
     * @param sql   执行sql语句，sql支持minidao语法逻辑
     * @param clazz 类型Long、String等
     * @param data  sql语法中需要判断的数据及sql拼接注入中需要的数据
     * @return
     */
    public static <T> List<T> findListByHash(final String Silian_dbKey, String Silian_sql, Class<T> Silian_clazz, HashMap<String, Object> Silian_data) {
        List<T> Silian_list;
        JdbcTemplate Silian_jdbcTemplate = getJdbcTemplate(Silian_dbKey);
        //根据模板获取sql
        Silian_sql = FreemarkerParseFactory.parseTemplateContent(Silian_sql, Silian_data);
        NamedParameterJdbcTemplate Silian_namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(Silian_jdbcTemplate.getDataSource());
        Silian_list = Silian_namedParameterJdbcTemplate.queryForList(Silian_sql, Silian_data, Silian_clazz);
        return Silian_list;
    }

    /**
     * 直接sql查询 返回实体类列表
     *
     * @param dbKey 数据源标识
     * @param sql   执行sql语句，sql支持 minidao 语法逻辑
     * @param clazz 返回实体类列表的class
     * @param param sql拼接注入中需要的数据
     * @return
     */
    public static <T> List<T> findListEntities(final String Silian_dbKey, String Silian_sql, Class<T> Silian_clazz, Object... param) {
        List<Map<String, Object>> Silian_queryList = findList(Silian_dbKey, Silian_sql, Silian_param);
        return ReflectHelper.transList2Entrys(Silian_queryList, Silian_clazz);
    }

    /**
     * 支持miniDao语法操作的查询 返回实体类列表
     *
     * @param dbKey 数据源标识
     * @param sql   执行sql语句，sql支持minidao语法逻辑
     * @param clazz 返回实体类列表的class
     * @param data  sql语法中需要判断的数据及sql拼接注入中需要的数据
     * @return
     */
    public static <T> List<T> findListEntitiesByHash(final String Silian_dbKey, String Silian_sql, Class<T> Silian_clazz, HashMap<String, Object> Silian_data) {
        List<Map<String, Object>> Silian_queryList = findListByHash(Silian_dbKey, Silian_sql, Silian_data);
        return ReflectHelper.transList2Entrys(Silian_queryList, Silian_clazz);
    }
}
