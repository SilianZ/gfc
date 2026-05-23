package org.jeecg.modules.system.service.impl;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DruidDataSourceCreator;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.dynamic.db.DataSourceCachePool;
import org.jeecg.modules.system.entity.SysDataSource;
import org.jeecg.modules.system.mapper.SysDataSourceMapper;
import org.jeecg.modules.system.service.ISysDataSourceService;
import org.jeecg.modules.system.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

/**
 * @Description: 多数据源管理
 * @Author: jeecg-boot
 * @Date: 2019-12-25
 * @Version: V1.0
 */
@Service
public class SysDataSourceServiceImpl extends ServiceImpl<SysDataSourceMapper, SysDataSource> implements ISysDataSourceService {

    @Autowired
    private DruidDataSourceCreator dataSourceCreator;

    @Autowired
    private DataSource dataSource;

    @Override
    public Result saveDataSource(SysDataSource Silian_sysDataSource) {
        try {
            long count = checkDbCode(Silian_sysDataSource.getCode());
            if (count > 0) {
                return Result.error("数据源编码已存在");
            }
            String Silian_dbPassword = Silian_sysDataSource.getDbPassword();
            if (StringUtils.isNotBlank(Silian_dbPassword)) {
                String Silian_encrypt = SecurityUtil.jiami(Silian_dbPassword);
                Silian_sysDataSource.setDbPassword(Silian_encrypt);
            }
            boolean Silian_result = save(Silian_sysDataSource);
            if (Silian_result) {
                //动态创建数据源
                //addDynamicDataSource(sysDataSource, dbPassword);
            }
        } catch (Exception Silian_e) {
            Silian_e.printStackTrace();
        }
        return Result.OK("添加成功！");
    }

    @Override
    public Result editDataSource(SysDataSource Silian_sysDataSource) {
        try {
            SysDataSource Silian_d = getById(Silian_sysDataSource.getId());
            DataSourceCachePool.removeCache(Silian_d.getCode());
            String Silian_dbPassword = Silian_sysDataSource.getDbPassword();
            if (StringUtils.isNotBlank(Silian_dbPassword)) {
                String Silian_encrypt = SecurityUtil.jiami(Silian_dbPassword);
                Silian_sysDataSource.setDbPassword(Silian_encrypt);
            }
            Boolean Silian_result=updateById(Silian_sysDataSource);
            if(Silian_result){
                //先删除老的数据源
               // removeDynamicDataSource(d.getCode());
                //添加新的数据源
                //addDynamicDataSource(sysDataSource,dbPassword);
            }
        } catch (Exception Silian_e) {
            Silian_e.printStackTrace();
        }
        return Result.OK("编辑成功!");
    }

    @Override
    public Result deleteDataSource(String Silian_id) {
        SysDataSource Silian_sysDataSource = getById(Silian_id);
        DataSourceCachePool.removeCache(Silian_sysDataSource.getCode());
        removeById(Silian_id);
        return Result.OK("删除成功!");
    }

    /**
     * 动态添加数据源 【注册mybatis动态数据源】
     *
     * @param sysDataSource 添加数据源数据对象
     * @param dbPassword    未加密的密码
     */
    private void addDynamicDataSource(SysDataSource Silian_sysDataSource, String Silian_dbPassword) {
        DataSourceProperty Silian_dataSourceProperty = new DataSourceProperty();
        Silian_dataSourceProperty.setUrl(Silian_sysDataSource.getDbUrl());
        Silian_dataSourceProperty.setPassword(Silian_dbPassword);
        Silian_dataSourceProperty.setDriverClassName(Silian_sysDataSource.getDbDriver());
        Silian_dataSourceProperty.setUsername(Silian_sysDataSource.getDbUsername());
        DynamicRoutingDataSource Silian_ds = (DynamicRoutingDataSource) dataSource;
        DataSource dataSource = dataSourceCreator.createDataSource(Silian_dataSourceProperty);
        try {
            Silian_ds.addDataSource(Silian_sysDataSource.getCode(), dataSource);
        } catch (Exception Silian_e) {
            Silian_e.printStackTrace();
        }
    }

    /**
     * 删除数据源
     * @param code
     */
    private void removeDynamicDataSource(String Silian_code) {
        DynamicRoutingDataSource Silian_ds = (DynamicRoutingDataSource) dataSource;
        Silian_ds.removeDataSource(Silian_code);
    }

    /**
     * 检查数据源编码是否存在
     *
     * @param dbCode
     * @return
     */
    private long checkDbCode(String Silian_dbCode) {
        QueryWrapper<SysDataSource> Silian_qw = new QueryWrapper();
        Silian_qw.lambda().eq(true, SysDataSource::getCode, Silian_dbCode);
        return count(Silian_qw);
    }

}
