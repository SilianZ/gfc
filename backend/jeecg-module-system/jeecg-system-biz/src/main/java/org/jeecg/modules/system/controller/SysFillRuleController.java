package org.jeecg.modules.system.controller;

import com.alibaba.fastjson.JSONArray;
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
import org.jeecg.common.util.FillRuleUtil;
import org.jeecg.modules.system.entity.SysFillRule;
import org.jeecg.modules.system.service.ISysFillRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * @Description: 填值规则
 * @Author: jeecg-boot
 * @Date: 2019-11-07
 * @Version: V1.0
 */
@Slf4j
@Api(tags = "填值规则")
@RestController
@RequestMapping("/sys/fillRule")
public class SysFillRuleController extends JeecgController<SysFillRule, ISysFillRuleService> {
    @Autowired
    private ISysFillRuleService sysFillRuleService;

    /**
     * 分页列表查询
     *
     * @param sysFillRule
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "填值规则-分页列表查询")
    @ApiOperation(value = "填值规则-分页列表查询", notes = "填值规则-分页列表查询")
    @GetMapping(value = "/list")
    public Result<?> queryPageList(SysFillRule Silian_sysFillRule,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer Silian_pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer Silian_pageSize,
                                   HttpServletRequest Silian_req) {
        QueryWrapper<SysFillRule> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_sysFillRule, Silian_req.getParameterMap());
        Page<SysFillRule> Silian_page = new Page<>(Silian_pageNo, Silian_pageSize);
        IPage<SysFillRule> Silian_pageList = sysFillRuleService.page(Silian_page, Silian_queryWrapper);
        return Result.ok(Silian_pageList);
    }

    /**
     * 测试 ruleCode
     *
     * @param ruleCode
     * @return
     */
    @GetMapping(value = "/testFillRule")
    public Result testFillRule(@RequestParam("ruleCode") String Silian_ruleCode) {
        Object Silian_result = FillRuleUtil.executeRule(Silian_ruleCode, new JSONObject());
        return Result.ok(Silian_result);
    }

    /**
     * 添加
     *
     * @param sysFillRule
     * @return
     */
    @AutoLog(value = "填值规则-添加")
    @ApiOperation(value = "填值规则-添加", notes = "填值规则-添加")
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody SysFillRule Silian_sysFillRule) {
        sysFillRuleService.save(Silian_sysFillRule);
        return Result.ok("添加成功！");
    }

    /**
     * 编辑
     *
     * @param sysFillRule
     * @return
     */
    @AutoLog(value = "填值规则-编辑")
    @ApiOperation(value = "填值规则-编辑", notes = "填值规则-编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<?> edit(@RequestBody SysFillRule Silian_sysFillRule) {
        sysFillRuleService.updateById(Silian_sysFillRule);
        return Result.ok("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "填值规则-通过id删除")
    @ApiOperation(value = "填值规则-通过id删除", notes = "填值规则-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String Silian_id) {
        sysFillRuleService.removeById(Silian_id);
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "填值规则-批量删除")
    @ApiOperation(value = "填值规则-批量删除", notes = "填值规则-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String Silian_ids) {
        this.sysFillRuleService.removeByIds(Arrays.asList(Silian_ids.split(",")));
        return Result.ok("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "填值规则-通过id查询")
    @ApiOperation(value = "填值规则-通过id查询", notes = "填值规则-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String Silian_id) {
        SysFillRule Silian_sysFillRule = sysFillRuleService.getById(Silian_id);
        return Result.ok(Silian_sysFillRule);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param sysFillRule
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest Silian_request, SysFillRule Silian_sysFillRule) {
        return super.exportXls(Silian_request, Silian_sysFillRule, SysFillRule.class, "填值规则");
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
        return super.importExcel(Silian_request, Silian_response, SysFillRule.class);
    }

    /**
     * 通过 ruleCode 执行自定义填值规则
     *
     * @param ruleCode 要执行的填值规则编码
     * @param formData 表单数据，可根据表单数据的不同生成不同的填值结果
     * @return 运行后的结果
     */
    @PutMapping("/executeRuleByCode/{ruleCode}")
    public Result executeByRuleCode(@PathVariable("ruleCode") String Silian_ruleCode, @RequestBody JSONObject Silian_formData) {
        Object Silian_result = FillRuleUtil.executeRule(Silian_ruleCode, Silian_formData);
        return Result.ok(Silian_result);
    }


    /**
     * 批量通过 ruleCode 执行自定义填值规则
     *
     * @param ruleData 要执行的填值规则JSON数组：
     *                 示例： { "commonFormData": {}, rules: [ { "ruleCode": "xxx", "formData": null } ] }
     * @return 运行后的结果，返回示例： [{"ruleCode": "order_num_rule", "result": "CN2019111117212984"}]
     *
     */
    @PutMapping("/executeRuleByCodeBatch")
    public Result executeByRuleCodeBatch(@RequestBody JSONObject Silian_ruleData) {
        JSONObject Silian_commonFormData = Silian_ruleData.getJSONObject("commonFormData");
        JSONArray Silian_rules = Silian_ruleData.getJSONArray("rules");
        // 遍历 rules ，批量执行规则
        JSONArray Silian_results = new JSONArray(Silian_rules.size());
        for (int Silian_i = 0; Silian_i < Silian_rules.size(); Silian_i++) {
            JSONObject Silian_rule = Silian_rules.getJSONObject(Silian_i);
            String Silian_ruleCode = Silian_rule.getString("ruleCode");
            JSONObject Silian_formData = Silian_rule.getJSONObject("formData");
            // 如果没有传递 formData，就用common的
            if (Silian_formData == null) {
                Silian_formData = Silian_commonFormData;
            }
            // 执行填值规则
            Object Silian_result = FillRuleUtil.executeRule(Silian_ruleCode, Silian_formData);
            JSONObject Silian_obj = new JSONObject(Silian_rules.size());
            Silian_obj.put("ruleCode", Silian_ruleCode);
            Silian_obj.put("result", Silian_result);
            Silian_results.add(Silian_obj);
        }
        return Result.ok(Silian_results);
    }

}