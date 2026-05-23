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
import org.jeecg.modules.biz.entity.VTeamResource;
import org.jeecg.modules.biz.service.IBizFiscalYearService;
import org.jeecg.modules.biz.service.IVTeamResourceService;

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
 * @Description: v_team_resource
 * @Author: jeecg-boot
 * @Date:   2023-09-27
 * @Version: V1.0
 */
@Api(tags="v_team_resource")
@RestController
@RequestMapping("/biz/vTeamResource")
@Slf4j
public class VTeamResourceController extends JeecgController<VTeamResource, IVTeamResourceService> {
	@Autowired
	private IVTeamResourceService vTeamResourceService;
	 @Autowired
	 private IBizFiscalYearService bizFiscalYearService;

	/**
	 * 分页列表查询
	 *
	 * @param vTeamResource
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "v_team_resource-分页列表查询")
	@ApiOperation(value="v_team_resource-分页列表查询", notes="v_team_resource-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<VTeamResource>> queryPageList(VTeamResource Silian_vTeamResource,
								   @RequestParam(name="pageNo", defaultValue="1") Integer Silian_pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer Silian_pageSize,
								   HttpServletRequest Silian_req) {
		Integer Silian_yearCode = bizFiscalYearService.getActiveYearCode();
		//获取指定userId的状态为正常的所有ID
		QueryWrapper<VTeamResource> Silian_query = new QueryWrapper<VTeamResource>();
		Silian_query.eq("data_type", "SYOU");
		Silian_query.eq("status", "ZC");
		Silian_query.eq("user_id", Silian_vTeamResource.getUserId());
		List Silian_ids = vTeamResourceService.list(Silian_query).stream().map(VTeamResource::getId).collect(Collectors.toList());
		if(Silian_ids.isEmpty()) Silian_ids.add("-1");
		QueryWrapper<VTeamResource> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_vTeamResource, Silian_req.getParameterMap()).eq("data_type", "SYOU");
		Silian_queryWrapper.or(Silian_wrapper -> Silian_wrapper.eq("user_id", Silian_vTeamResource.getUserId()).eq("data_type", "SYON").notIn("id", Silian_ids));
		Page<VTeamResource> Silian_page = new Page<VTeamResource>(Silian_pageNo, Silian_pageSize);
		IPage<VTeamResource> Silian_pageList = vTeamResourceService.page(Silian_page, Silian_queryWrapper);
		return Result.OK(Silian_pageList);
	}

	/**
	 * 分页列表查询通道
	 *
	 * @param vTeamResource
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "v_team_resource-分页列表查询通道")
	@ApiOperation(value="v_team_resource-分页列表查询通道", notes="v_team_resource-分页列表查询通道")
	@GetMapping(value = "/listChannel")
	public Result<IPage<VTeamResource>> queryChannelPageList(VTeamResource Silian_vTeamResource,
								   @RequestParam(name="pageNo", defaultValue="1") Integer Silian_pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer Silian_pageSize,
								   HttpServletRequest Silian_req) {
		//获取指定userId的状态为正常的所有ID
		QueryWrapper<VTeamResource> Silian_query = new QueryWrapper<VTeamResource>();
		Silian_query.eq("data_type", "SYOU");
		Silian_query.eq("resource_type", "CN");
		Silian_query.eq("status", "ZC");
		Silian_query.eq("user_id", Silian_vTeamResource.getUserId());
		List Silian_ids = vTeamResourceService.list(Silian_query).stream().map(VTeamResource::getId).collect(Collectors.toList());
		if(Silian_ids.isEmpty()) Silian_ids.add("-1");
		QueryWrapper<VTeamResource> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_vTeamResource, Silian_req.getParameterMap()).eq("data_type", "SYOU");
		Silian_queryWrapper.or(Silian_wrapper -> Silian_wrapper.eq("resource_type", "CN").eq("user_id", Silian_vTeamResource.getUserId()).eq("data_type", "SYON").notIn("id", Silian_ids));
		Page<VTeamResource> Silian_page = new Page<VTeamResource>(Silian_pageNo, Silian_pageSize);
		IPage<VTeamResource> Silian_pageList = vTeamResourceService.page(Silian_page, Silian_queryWrapper);
		return Result.OK(Silian_pageList);
	}

	/**
	 *   添加
	 *
	 * @param vTeamResource
	 * @return
	 */
	@AutoLog(value = "v_team_resource-添加")
	@ApiOperation(value="v_team_resource-添加", notes="v_team_resource-添加")
	//@RequiresPermissions("org.jeecg.modules:v_team_resource:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody VTeamResource Silian_vTeamResource) {
		vTeamResourceService.save(Silian_vTeamResource);
		return Result.OK("添加成功！");
	}

	/**
	 *  编辑
	 *
	 * @param vTeamResource
	 * @return
	 */
	@AutoLog(value = "v_team_resource-编辑")
	@ApiOperation(value="v_team_resource-编辑", notes="v_team_resource-编辑")
	//@RequiresPermissions("org.jeecg.modules:v_team_resource:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody VTeamResource Silian_vTeamResource) {
		vTeamResourceService.updateById(Silian_vTeamResource);
		return Result.OK("编辑成功!");
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "v_team_resource-通过id删除")
	@ApiOperation(value="v_team_resource-通过id删除", notes="v_team_resource-通过id删除")
	//@RequiresPermissions("org.jeecg.modules:v_team_resource:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String Silian_id) {
		vTeamResourceService.removeById(Silian_id);
		return Result.OK("删除成功!");
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "v_team_resource-批量删除")
	@ApiOperation(value="v_team_resource-批量删除", notes="v_team_resource-批量删除")
	//@RequiresPermissions("org.jeecg.modules:v_team_resource:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String Silian_ids) {
		this.vTeamResourceService.removeByIds(Arrays.asList(Silian_ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "v_team_resource-通过id查询")
	@ApiOperation(value="v_team_resource-通过id查询", notes="v_team_resource-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<VTeamResource> queryById(@RequestParam(name="id",required=true) String Silian_id) {
		VTeamResource Silian_vTeamResource = vTeamResourceService.getById(Silian_id);
		if(Silian_vTeamResource==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(Silian_vTeamResource);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param vTeamResource
    */
    //@RequiresPermissions("org.jeecg.modules:v_team_resource:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest Silian_request, VTeamResource Silian_vTeamResource) {
        return super.exportXls(Silian_request, Silian_vTeamResource, VTeamResource.class, "v_team_resource");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    //@RequiresPermissions("v_team_resource:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest Silian_request, HttpServletResponse Silian_response) {
        return super.importExcel(Silian_request, Silian_response, VTeamResource.class);
    }

}
