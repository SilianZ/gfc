package org.jeecg.modules.biz.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.biz.entity.BizMinePlan;
import org.jeecg.modules.biz.service.IBizMinePlanService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;

 /**
 * @Description: 原材料开采计划
 * @Author: jeecg-boot
 * @Date:   2023-09-25
 * @Version: V1.0
 */
@Api(tags="原材料开采计划")
@RestController
@RequestMapping("/biz/bizMinePlan")
@Slf4j
public class BizMinePlanController extends JeecgController<BizMinePlan, IBizMinePlanService> {
	@Autowired
	private IBizMinePlanService bizMinePlanService;
	
	/**
	 * 分页列表查询
	 *
	 * @param bizMinePlan
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "原材料开采计划-分页列表查询")
	@ApiOperation(value="原材料开采计划-分页列表查询", notes="原材料开采计划-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<BizMinePlan>> queryPageList(BizMinePlan bizMinePlan,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<BizMinePlan> queryWrapper = QueryGenerator.initQueryWrapper(bizMinePlan, req.getParameterMap());
		Page<BizMinePlan> page = new Page<BizMinePlan>(pageNo, pageSize);
		IPage<BizMinePlan> pageList = bizMinePlanService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param bizMinePlan
	 * @return
	 */
	@AutoLog(value = "原材料开采计划-添加")
	@ApiOperation(value="原材料开采计划-添加", notes="原材料开采计划-添加")
	//@RequiresPermissions("org.jeecg.modules:biz_mine_plan:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody BizMinePlan bizMinePlan) {
		bizMinePlanService.save(bizMinePlan);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param bizMinePlan
	 * @return
	 */
	@AutoLog(value = "原材料开采计划-编辑")
	@ApiOperation(value="原材料开采计划-编辑", notes="原材料开采计划-编辑")
	//@RequiresPermissions("org.jeecg.modules:biz_mine_plan:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody BizMinePlan bizMinePlan) {
		bizMinePlanService.updateById(bizMinePlan);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "原材料开采计划-通过id删除")
	@ApiOperation(value="原材料开采计划-通过id删除", notes="原材料开采计划-通过id删除")
	//@RequiresPermissions("org.jeecg.modules:biz_mine_plan:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		bizMinePlanService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "原材料开采计划-批量删除")
	@ApiOperation(value="原材料开采计划-批量删除", notes="原材料开采计划-批量删除")
	//@RequiresPermissions("org.jeecg.modules:biz_mine_plan:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.bizMinePlanService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "原材料开采计划-通过id查询")
	@ApiOperation(value="原材料开采计划-通过id查询", notes="原材料开采计划-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<BizMinePlan> queryById(@RequestParam(name="id",required=true) String id) {
		BizMinePlan bizMinePlan = bizMinePlanService.getById(id);
		if(bizMinePlan==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(bizMinePlan);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param bizMinePlan
    */
    //@RequiresPermissions("org.jeecg.modules:biz_mine_plan:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, BizMinePlan bizMinePlan) {
        return super.exportXls(request, bizMinePlan, BizMinePlan.class, "原材料开采计划");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    //@RequiresPermissions("biz_mine_plan:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, BizMinePlan.class);
    }

}
