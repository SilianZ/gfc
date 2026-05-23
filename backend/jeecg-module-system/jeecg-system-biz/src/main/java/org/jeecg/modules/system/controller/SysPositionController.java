package org.jeecg.modules.system.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.quartz.service.IQuartzJobService;
import org.jeecg.modules.system.entity.SysPosition;
import org.jeecg.modules.system.service.ISysPositionService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: 职务表
 * @Author: jeecg-boot
 * @Date: 2019-09-19
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "职务表")
@RestController
@RequestMapping("/sys/position")
public class SysPositionController {

    @Autowired
    private ISysPositionService sysPositionService;

    /**
     * 分页列表查询
     *
     * @param sysPosition
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "职务表-分页列表查询")
    @ApiOperation(value = "职务表-分页列表查询", notes = "职务表-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<SysPosition>> queryPageList(SysPosition Silian_sysPosition,
                                                    @RequestParam(name = "pageNo", defaultValue = "1") Integer Silian_pageNo,
                                                    @RequestParam(name = "pageSize", defaultValue = "10") Integer Silian_pageSize,
                                                    HttpServletRequest Silian_req) {
        Result<IPage<SysPosition>> Silian_result = new Result<IPage<SysPosition>>();
        QueryWrapper<SysPosition> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_sysPosition, Silian_req.getParameterMap());
        Page<SysPosition> Silian_page = new Page<SysPosition>(Silian_pageNo, Silian_pageSize);
        IPage<SysPosition> Silian_pageList = sysPositionService.page(Silian_page, Silian_queryWrapper);
        Silian_result.setSuccess(true);
        Silian_result.setResult(Silian_pageList);
        return Silian_result;
    }

    /**
     * 添加
     *
     * @param sysPosition
     * @return
     */
    @AutoLog(value = "职务表-添加")
    @ApiOperation(value = "职务表-添加", notes = "职务表-添加")
    @PostMapping(value = "/add")
    public Result<SysPosition> add(@RequestBody SysPosition Silian_sysPosition) {
        Result<SysPosition> Silian_result = new Result<SysPosition>();
        try {
            sysPositionService.save(Silian_sysPosition);
            Silian_result.success("添加成功！");
        } catch (Exception Silian_e) {
            log.error(Silian_e.getMessage(), Silian_e);
            Silian_result.error500("操作失败");
        }
        return Silian_result;
    }

    /**
     * 编辑
     *
     * @param sysPosition
     * @return
     */
    @AutoLog(value = "职务表-编辑")
    @ApiOperation(value = "职务表-编辑", notes = "职务表-编辑")
    @RequestMapping(value = "/edit", method ={RequestMethod.PUT, RequestMethod.POST})
    public Result<SysPosition> edit(@RequestBody SysPosition Silian_sysPosition) {
        Result<SysPosition> Silian_result = new Result<SysPosition>();
        SysPosition Silian_sysPositionEntity = sysPositionService.getById(Silian_sysPosition.getId());
        if (Silian_sysPositionEntity == null) {
            Silian_result.error500("未找到对应实体");
        } else {
            boolean Silian_ok = sysPositionService.updateById(Silian_sysPosition);
            //TODO 返回false说明什么？
            if (Silian_ok) {
                Silian_result.success("修改成功!");
            }
        }

        return Silian_result;
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "职务表-通过id删除")
    @ApiOperation(value = "职务表-通过id删除", notes = "职务表-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String Silian_id) {
        try {
            sysPositionService.removeById(Silian_id);
        } catch (Exception Silian_e) {
            log.error("删除失败", Silian_e.getMessage());
            return Result.error("删除失败!");
        }
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "职务表-批量删除")
    @ApiOperation(value = "职务表-批量删除", notes = "职务表-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<SysPosition> deleteBatch(@RequestParam(name = "ids", required = true) String Silian_ids) {
        Result<SysPosition> Silian_result = new Result<SysPosition>();
        if (Silian_ids == null || "".equals(Silian_ids.trim())) {
            Silian_result.error500("参数不识别！");
        } else {
            this.sysPositionService.removeByIds(Arrays.asList(Silian_ids.split(",")));
            Silian_result.success("删除成功!");
        }
        return Silian_result;
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "职务表-通过id查询")
    @ApiOperation(value = "职务表-通过id查询", notes = "职务表-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<SysPosition> queryById(@RequestParam(name = "id", required = true) String Silian_id) {
        Result<SysPosition> Silian_result = new Result<SysPosition>();
        SysPosition Silian_sysPosition = sysPositionService.getById(Silian_id);
        if (Silian_sysPosition == null) {
            Silian_result.error500("未找到对应实体");
        } else {
            Silian_result.setResult(Silian_sysPosition);
            Silian_result.setSuccess(true);
        }
        return Silian_result;
    }

    /**
     * 导出excel
     *
     * @param request
     * @param response
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest Silian_request, HttpServletResponse Silian_response) {
        // Step.1 组装查询条件
        QueryWrapper<SysPosition> Silian_queryWrapper = null;
        try {
            String Silian_paramsStr = Silian_request.getParameter("paramsStr");
            if (oConvertUtils.isNotEmpty(Silian_paramsStr)) {
                String Silian_deString = URLDecoder.decode(Silian_paramsStr, "UTF-8");
                SysPosition Silian_sysPosition = JSON.parseObject(Silian_deString, SysPosition.class);
                Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_sysPosition, Silian_request.getParameterMap());
            }
        } catch (UnsupportedEncodingException Silian_e) {
            Silian_e.printStackTrace();
        }

        //Step.2 AutoPoi 导出Excel
        ModelAndView Silian_mv = new ModelAndView(new JeecgEntityExcelView());
        List<SysPosition> Silian_pageList = sysPositionService.list(Silian_queryWrapper);
        LoginUser Silian_user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        //导出文件名称
        Silian_mv.addObject(NormalExcelConstants.FILE_NAME, "职务表列表");
        Silian_mv.addObject(NormalExcelConstants.CLASS, SysPosition.class);
        Silian_mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("职务表列表数据", "导出人:"+Silian_user.getRealname(),"导出信息"));
        Silian_mv.addObject(NormalExcelConstants.DATA_LIST, Silian_pageList);
        return Silian_mv;
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest Silian_request, HttpServletResponse Silian_response)throws IOException {
        MultipartHttpServletRequest Silian_multipartRequest = (MultipartHttpServletRequest) Silian_request;
        Map<String, MultipartFile> Silian_fileMap = Silian_multipartRequest.getFileMap();
        // 错误信息
        List<String> Silian_errorMessage = new ArrayList<>();
        int Silian_successLines = 0, Silian_errorLines = 0;
        for (Map.Entry<String, MultipartFile> Silian_entity : Silian_fileMap.entrySet()) {
            // 获取上传文件对象
            MultipartFile Silian_file = Silian_entity.getValue();
            ImportParams Silian_params = new ImportParams();
            Silian_params.setTitleRows(2);
            Silian_params.setHeadRows(1);
            Silian_params.setNeedSave(true);
            try {
                List<Object>  Silian_listSysPositions = ExcelImportUtil.importExcel(Silian_file.getInputStream(), SysPosition.class, Silian_params);
                List<String> Silian_list = ImportExcelUtil.importDateSave(Silian_listSysPositions, ISysPositionService.class, Silian_errorMessage,CommonConstant.SQL_INDEX_UNIQ_CODE);
                Silian_errorLines+=Silian_list.size();
                Silian_successLines+=(Silian_listSysPositions.size()-Silian_errorLines);
            } catch (Exception Silian_e) {
                log.error(Silian_e.getMessage(), Silian_e);
                return Result.error("文件导入失败:" + Silian_e.getMessage());
            } finally {
                try {
                    Silian_file.getInputStream().close();
                } catch (IOException Silian_e) {
                    Silian_e.printStackTrace();
                }
            }
        }
        return ImportExcelUtil.imporReturnRes(Silian_errorLines,Silian_successLines,Silian_errorMessage);
    }

    /**
     * 通过code查询
     *
     * @param code
     * @return
     */
    @AutoLog(value = "职务表-通过code查询")
    @ApiOperation(value = "职务表-通过code查询", notes = "职务表-通过code查询")
    @GetMapping(value = "/queryByCode")
    public Result<SysPosition> queryByCode(@RequestParam(name = "code", required = true) String Silian_code) {
        Result<SysPosition> Silian_result = new Result<SysPosition>();
        QueryWrapper<SysPosition> Silian_queryWrapper = new QueryWrapper<SysPosition>();
        Silian_queryWrapper.eq("code",Silian_code);
        SysPosition Silian_sysPosition = sysPositionService.getOne(Silian_queryWrapper);
        if (Silian_sysPosition == null) {
            Silian_result.error500("未找到对应实体");
        } else {
            Silian_result.setResult(Silian_sysPosition);
            Silian_result.setSuccess(true);
        }
        return Silian_result;
    }
}
