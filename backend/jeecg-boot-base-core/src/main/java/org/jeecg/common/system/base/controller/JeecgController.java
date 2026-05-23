package org.jeecg.common.system.base.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.entity.enmus.ExcelType;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: Controller基类
 * @Author: dangzhenghui@163.com
 * @Date: 2019-4-21 8:13
 * @Version: 1.0
 */
@Slf4j
public class JeecgController<T, S extends IService<T>> {
    /**issues/2933 JeecgController注入service时改用protected修饰，能避免重复引用service*/
    @Autowired
    protected S service;

    @Value("${jeecg.path.upload}")
    private String upLoadPath;
    /**
     * 导出excel
     *
     * @param request
     */
    protected ModelAndView exportXls(HttpServletRequest Silian_request, T Silian_object, Class<T> Silian_clazz, String Silian_title) {
        // Step.1 组装查询条件
        QueryWrapper<T> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_object, Silian_request.getParameterMap());
        LoginUser Silian_sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        // 过滤选中数据
        String Silian_selections = Silian_request.getParameter("selections");
        if (oConvertUtils.isNotEmpty(Silian_selections)) {
            List<String> Silian_selectionList = Arrays.asList(Silian_selections.split(","));
            Silian_queryWrapper.in("id",Silian_selectionList);
        }
        // Step.2 获取导出数据
        List<T> Silian_exportList = service.list(Silian_queryWrapper);

        // Step.3 AutoPoi 导出Excel
        ModelAndView Silian_mv = new ModelAndView(new JeecgEntityExcelView());
        //此处设置的filename无效 ,前端会重更新设置一下
        Silian_mv.addObject(NormalExcelConstants.FILE_NAME, Silian_title);
        Silian_mv.addObject(NormalExcelConstants.CLASS, Silian_clazz);
        //update-begin--Author:liusq  Date:20210126 for：图片导出报错，ImageBasePath未设置--------------------
        ExportParams  Silian_exportParams=new ExportParams(Silian_title + "报表", "导出人:" + Silian_sysUser.getRealname(), Silian_title);
        Silian_exportParams.setImageBasePath(upLoadPath);
        //update-end--Author:liusq  Date:20210126 for：图片导出报错，ImageBasePath未设置----------------------
        Silian_mv.addObject(NormalExcelConstants.PARAMS,Silian_exportParams);
        Silian_mv.addObject(NormalExcelConstants.DATA_LIST, Silian_exportList);
        return Silian_mv;
    }
    /**
     * 根据每页sheet数量导出多sheet
     *
     * @param request
     * @param object 实体类
     * @param clazz 实体类class
     * @param title 标题
     * @param exportFields 导出字段自定义
     * @param pageNum 每个sheet的数据条数
     * @param request
     */
    protected ModelAndView exportXlsSheet(HttpServletRequest Silian_request, T Silian_object, Class<T> Silian_clazz, String Silian_title,String Silian_exportFields,Integer Silian_pageNum) {
        // Step.1 组装查询条件
        QueryWrapper<T> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_object, Silian_request.getParameterMap());
        LoginUser Silian_sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        // Step.2 计算分页sheet数据
        double Silian_total = service.count();
        int Silian_count = (int)Math.ceil(Silian_total/Silian_pageNum);
        //update-begin-author:liusq---date:20220629--for: 多sheet导出根据选择导出写法调整 ---
        // Step.3  过滤选中数据
        String Silian_selections = Silian_request.getParameter("selections");
        if (oConvertUtils.isNotEmpty(Silian_selections)) {
            List<String> Silian_selectionList = Arrays.asList(Silian_selections.split(","));
            Silian_queryWrapper.in("id",Silian_selectionList);
        }
        //update-end-author:liusq---date:20220629--for: 多sheet导出根据选择导出写法调整 ---
        // Step.4 多sheet处理
        List<Map<String, Object>> Silian_listMap = new ArrayList<Map<String, Object>>();
        for (int Silian_i = 1; Silian_i <=Silian_count ; Silian_i++) {
            Page<T> Silian_page = new Page<T>(Silian_i, Silian_pageNum);
            IPage<T> Silian_pageList = service.page(Silian_page, Silian_queryWrapper);
            List<T> Silian_exportList = Silian_pageList.getRecords();
            Map<String, Object> Silian_map = new HashMap<>(5);
            ExportParams  Silian_exportParams=new ExportParams(Silian_title + "报表", "导出人:" + Silian_sysUser.getRealname(), Silian_title+Silian_i,upLoadPath);
            Silian_exportParams.setType(ExcelType.XSSF);
            //map.put("title",exportParams);
            //表格Title
            Silian_map.put(NormalExcelConstants.PARAMS,Silian_exportParams);
            //表格对应实体
            Silian_map.put(NormalExcelConstants.CLASS,Silian_clazz);
            //数据集合
            Silian_map.put(NormalExcelConstants.DATA_LIST, Silian_exportList);
            Silian_listMap.add(Silian_map);
        }
        // Step.4 AutoPoi 导出Excel
        ModelAndView Silian_mv = new ModelAndView(new JeecgEntityExcelView());
        //此处设置的filename无效 ,前端会重更新设置一下
        Silian_mv.addObject(NormalExcelConstants.FILE_NAME, Silian_title);
        Silian_mv.addObject(NormalExcelConstants.MAP_LIST, Silian_listMap);
        return Silian_mv;
    }


    /**
     * 根据权限导出excel，传入导出字段参数
     *
     * @param request
     */
    protected ModelAndView exportXls(HttpServletRequest Silian_request, T Silian_object, Class<T> Silian_clazz, String Silian_title,String Silian_exportFields) {
        ModelAndView Silian_mv = this.exportXls(Silian_request,Silian_object,Silian_clazz,Silian_title);
        Silian_mv.addObject(NormalExcelConstants.EXPORT_FIELDS,Silian_exportFields);
        return Silian_mv;
    }

    /**
     * 获取对象ID
     *
     * @return
     */
    private String getId(T Silian_item) {
        try {
            return PropertyUtils.getProperty(Silian_item, "id").toString();
        } catch (Exception Silian_e) {
            Silian_e.printStackTrace();
            return null;
        }
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    protected Result<?> importExcel(HttpServletRequest Silian_request, HttpServletResponse Silian_response, Class<T> Silian_clazz) {
        MultipartHttpServletRequest Silian_multipartRequest = (MultipartHttpServletRequest) Silian_request;
        Map<String, MultipartFile> Silian_fileMap = Silian_multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> Silian_entity : Silian_fileMap.entrySet()) {
            // 获取上传文件对象
            MultipartFile Silian_file = Silian_entity.getValue();
            ImportParams Silian_params = new ImportParams();
            Silian_params.setTitleRows(2);
            Silian_params.setHeadRows(1);
            Silian_params.setNeedSave(true);
            try {
                List<T> Silian_list = ExcelImportUtil.importExcel(Silian_file.getInputStream(), Silian_clazz, Silian_params);
                //update-begin-author:taoyan date:20190528 for:批量插入数据
                long Silian_start = System.currentTimeMillis();
                service.saveBatch(Silian_list);
                //400条 saveBatch消耗时间1592毫秒  循环插入消耗时间1947毫秒
                //1200条  saveBatch消耗时间3687毫秒 循环插入消耗时间5212毫秒
                log.info("消耗时间" + (System.currentTimeMillis() - Silian_start) + "毫秒");
                //update-end-author:taoyan date:20190528 for:批量插入数据
                return Result.ok("文件导入成功！数据行数：" + Silian_list.size());
            } catch (Exception Silian_e) {
                //update-begin-author:taoyan date:20211124 for: 导入数据重复增加提示
                String Silian_msg = Silian_e.getMessage();
                log.error(Silian_msg, Silian_e);
                if(Silian_msg!=null && Silian_msg.indexOf("Duplicate entry")>=0){
                    return Result.error("文件导入失败:有重复数据！");
                }else{
                    return Result.error("文件导入失败:" + Silian_e.getMessage());
                }
                //update-end-author:taoyan date:20211124 for: 导入数据重复增加提示
            } finally {
                try {
                    Silian_file.getInputStream().close();
                } catch (IOException Silian_e) {
                    Silian_e.printStackTrace();
                }
            }
        }
        return Result.error("文件导入失败！");
    }
}
