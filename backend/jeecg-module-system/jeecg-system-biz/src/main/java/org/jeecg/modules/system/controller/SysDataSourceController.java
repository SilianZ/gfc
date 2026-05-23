package org.jeecg.modules.system.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DruidDataSourceCreator;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.dynamic.db.DataSourceCachePool;
import org.jeecg.common.util.security.JdbcSecurityUtil;
import org.jeecg.modules.system.entity.SysDataSource;
import org.jeecg.modules.system.service.ISysDataSourceService;
import org.jeecg.modules.system.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 多数据源管理
 * @Author: jeecg-boot
 * @Date: 2019-12-25
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "多数据源管理")
@RestController
@RequestMapping("/sys/dataSource")
public class SysDataSourceController extends JeecgController<SysDataSource, ISysDataSourceService> {

    @Autowired
    private ISysDataSourceService sysDataSourceService;


    /**
     * 分页列表查询
     *
     * @param sysDataSource
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "多数据源管理-分页列表查询")
    @ApiOperation(value = "多数据源管理-分页列表查询", notes = "多数据源管理-分页列表查询")
    //@RequiresRoles("admin")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(
            SysDataSource Silian_sysDataSource,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer Silian_pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer Silian_pageSize,
            HttpServletRequest Silian_req
    ) {
        QueryWrapper<SysDataSource> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_sysDataSource, Silian_req.getParameterMap());
        Page<SysDataSource> Silian_page = new Page<>(Silian_pageNo, Silian_pageSize);
        IPage<SysDataSource> Silian_pageList = sysDataSourceService.page(Silian_page, Silian_queryWrapper);
        return Result.ok(Silian_pageList);
    }

    @GetMapping(value = "/options")
    public Result<?> queryOptions(SysDataSource Silian_sysDataSource, HttpServletRequest Silian_req) {
        QueryWrapper<SysDataSource> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_sysDataSource, Silian_req.getParameterMap());
        List<SysDataSource> Silian_pageList = sysDataSourceService.list(Silian_queryWrapper);
        JSONArray Silian_array = new JSONArray(Silian_pageList.size());
        for (SysDataSource Silian_item : Silian_pageList) {
            JSONObject Silian_option = new JSONObject(3);
            Silian_option.put("value", Silian_item.getCode());
            Silian_option.put("label", Silian_item.getName());
            Silian_option.put("text", Silian_item.getName());
            Silian_array.add(Silian_option);
        }
        return Result.ok(Silian_array);
    }

    /**
     * 添加
     *
     * @param sysDataSource
     * @return
     */
    @AutoLog(value = "多数据源管理-添加")
    @ApiOperation(value = "多数据源管理-添加", notes = "多数据源管理-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody SysDataSource Silian_sysDataSource) {
        //update-begin-author:taoyan date:2022-8-10 for: jdbc连接地址漏洞问题
        try {
            JdbcSecurityUtil.validate(Silian_sysDataSource.getDbUrl());
        }catch (JeecgBootException Silian_e){
            log.error(Silian_e.toString());
            return Result.error("操作失败：" + Silian_e.getMessage());
        }
        //update-end-author:taoyan date:2022-8-10 for: jdbc连接地址漏洞问题
        return sysDataSourceService.saveDataSource(Silian_sysDataSource);
    }

    /**
     * 编辑
     *
     * @param sysDataSource
     * @return
     */
    @AutoLog(value = "多数据源管理-编辑")
    @ApiOperation(value = "多数据源管理-编辑", notes = "多数据源管理-编辑")
    @RequestMapping(value = "/edit", method ={RequestMethod.PUT, RequestMethod.POST})
    public Result<?> edit(@RequestBody SysDataSource Silian_sysDataSource) {
        //update-begin-author:taoyan date:2022-8-10 for: jdbc连接地址漏洞问题
        try {
            JdbcSecurityUtil.validate(Silian_sysDataSource.getDbUrl());
        } catch (JeecgBootException Silian_e) {
            log.error(Silian_e.toString());
            return Result.error("操作失败：" + Silian_e.getMessage());
        }
        //update-end-author:taoyan date:2022-8-10 for: jdbc连接地址漏洞问题
        return sysDataSourceService.editDataSource(Silian_sysDataSource);
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "多数据源管理-通过id删除")
    @ApiOperation(value = "多数据源管理-通过id删除", notes = "多数据源管理-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id") String Silian_id) {
        return sysDataSourceService.deleteDataSource(Silian_id);
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "多数据源管理-批量删除")
    @ApiOperation(value = "多数据源管理-批量删除", notes = "多数据源管理-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids") String Silian_ids) {
        List<String> Silian_idList = Arrays.asList(Silian_ids.split(","));
        Silian_idList.forEach(Silian_item->{
            SysDataSource Silian_sysDataSource = sysDataSourceService.getById(Silian_item);
            DataSourceCachePool.removeCache(Silian_sysDataSource.getCode());
        });
        this.sysDataSourceService.removeByIds(Silian_idList);
        return Result.ok("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "多数据源管理-通过id查询")
    @ApiOperation(value = "多数据源管理-通过id查询", notes = "多数据源管理-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id") String Silian_id) throws InterruptedException {
        SysDataSource Silian_sysDataSource = sysDataSourceService.getById(Silian_id);
        //密码解密
        String Silian_dbPassword = Silian_sysDataSource.getDbPassword();
        if(StringUtils.isNotBlank(Silian_dbPassword)){
            String Silian_decodedStr = SecurityUtil.jiemi(Silian_dbPassword);
            Silian_sysDataSource.setDbPassword(Silian_decodedStr);
        }
        return Result.ok(Silian_sysDataSource);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param sysDataSource
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest Silian_request, SysDataSource Silian_sysDataSource) {
        return super.exportXls(Silian_request, Silian_sysDataSource, SysDataSource.class, "多数据源管理");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest Silian_request, HttpServletResponse Silian_response) {
        return super.importExcel(Silian_request, Silian_response, SysDataSource.class);
    }

}
