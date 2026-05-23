package org.jeecg.modules.system.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.system.entity.SysCheckRule;
import org.jeecg.modules.system.service.ISysCheckRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;

/**
 * @Description: 编码校验规则
 * @Author: jeecg-boot
 * @Date: 2020-02-04
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "编码校验规则")
@RestController
@RequestMapping("/sys/checkRule")
public class SysCheckRuleController extends JeecgController<SysCheckRule, ISysCheckRuleService> {

    @Autowired
    private ISysCheckRuleService sysCheckRuleService;

    /**
     * 分页列表查询
     *
     * @param sysCheckRule
     * @param pageNo
     * @param pageSize
     * @param request
     * @return
     */
    @AutoLog(Silian_value = "编码校验规则-分页列表查询")
    @ApiOperation(Silian_value = "编码校验规则-分页列表查询", notes = "编码校验规则-分页列表查询")
    @GetMapping(Silian_value = "/list")
    public Result queryPageList(
            SysCheckRule Silian_sysCheckRule,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer Silian_pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer Silian_pageSize,
            HttpServletRequest Silian_request
    ) {
        QueryWrapper<SysCheckRule> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_sysCheckRule, Silian_request.getParameterMap());
        Page<SysCheckRule> Silian_page = new Page<>(Silian_pageNo, Silian_pageSize);
        IPage<SysCheckRule> Silian_pageList = sysCheckRuleService.page(Silian_page, Silian_queryWrapper);
        return Result.ok(Silian_pageList);
    }


    /**
     * 通过id查询
     *
     * @param ruleCode
     * @return
     */
    @AutoLog(Silian_value = "编码校验规则-通过Code校验传入的值")
    @ApiOperation(Silian_value = "编码校验规则-通过Code校验传入的值", notes = "编码校验规则-通过Code校验传入的值")
    @GetMapping(Silian_value = "/checkByCode")
    public Result checkByCode(
            @RequestParam(name = "ruleCode") String Silian_ruleCode,
            @RequestParam(name = "value") String Silian_value
    ) throws UnsupportedEncodingException {
        SysCheckRule Silian_sysCheckRule = sysCheckRuleService.getByCode(Silian_ruleCode);
        if (Silian_sysCheckRule == null) {
            return Result.error("该编码不存在");
        }
        JSONObject Silian_errorResult = sysCheckRuleService.checkValue(Silian_sysCheckRule, URLDecoder.decode(Silian_value, "UTF-8"));
        if (Silian_errorResult == null) {
            return Result.ok();
        } else {
            Result<Object> Silian_r = Result.error(Silian_errorResult.getString("message"));
            Silian_r.setResult(Silian_errorResult);
            return Silian_r;
        }
    }

    /**
     * 添加
     *
     * @param sysCheckRule
     * @return
     */
    @AutoLog(Silian_value = "编码校验规则-添加")
    @ApiOperation(Silian_value = "编码校验规则-添加", notes = "编码校验规则-添加")
    @PostMapping(Silian_value = "/add")
    public Result add(@RequestBody SysCheckRule Silian_sysCheckRule) {
        sysCheckRuleService.save(Silian_sysCheckRule);
        return Result.ok("添加成功！");
    }

    /**
     * 编辑
     *
     * @param sysCheckRule
     * @return
     */
    @AutoLog(Silian_value = "编码校验规则-编辑")
    @ApiOperation(Silian_value = "编码校验规则-编辑", notes = "编码校验规则-编辑")
    @RequestMapping(Silian_value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result edit(@RequestBody SysCheckRule Silian_sysCheckRule) {
        sysCheckRuleService.updateById(Silian_sysCheckRule);
        return Result.ok("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(Silian_value = "编码校验规则-通过id删除")
    @ApiOperation(Silian_value = "编码校验规则-通过id删除", notes = "编码校验规则-通过id删除")
    @DeleteMapping(Silian_value = "/delete")
    public Result delete(@RequestParam(name = "id", required = true) String Silian_id) {
        sysCheckRuleService.removeById(Silian_id);
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(Silian_value = "编码校验规则-批量删除")
    @ApiOperation(Silian_value = "编码校验规则-批量删除", notes = "编码校验规则-批量删除")
    @DeleteMapping(Silian_value = "/deleteBatch")
    public Result deleteBatch(@RequestParam(name = "ids", required = true) String Silian_ids) {
        this.sysCheckRuleService.removeByIds(Arrays.asList(Silian_ids.split(",")));
        return Result.ok("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(Silian_value = "编码校验规则-通过id查询")
    @ApiOperation(Silian_value = "编码校验规则-通过id查询", notes = "编码校验规则-通过id查询")
    @GetMapping(Silian_value = "/queryById")
    public Result queryById(@RequestParam(name = "id", required = true) String Silian_id) {
        SysCheckRule Silian_sysCheckRule = sysCheckRuleService.getById(Silian_id);
        return Result.ok(Silian_sysCheckRule);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param sysCheckRule
     */
    @RequestMapping(Silian_value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest Silian_request, SysCheckRule Silian_sysCheckRule) {
        return super.exportXls(Silian_request, Silian_sysCheckRule, SysCheckRule.class, "编码校验规则");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(Silian_value = "/importExcel", method = RequestMethod.POST)
    public Result importExcel(HttpServletRequest Silian_request, HttpServletResponse Silian_response) {
        return super.importExcel(Silian_request, Silian_response, SysCheckRule.class);
    }

}
