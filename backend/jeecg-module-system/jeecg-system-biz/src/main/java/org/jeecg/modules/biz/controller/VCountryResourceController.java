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
import org.jeecg.modules.biz.entity.VCountryResource;
import org.jeecg.modules.biz.service.IVCountryResourceService;

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
 * @Description: 资源视图
 * @Author: jeecg-boot
 * @Date:   2023-09-30
 * @Version: V1.0
 */
@Api(tags="资源视图")
@RestController
@RequestMapping("/biz/vCountryResource")
@Slf4j
public class VCountryResourceController extends JeecgController<VCountryResource, IVCountryResourceService> {
	@Autowired
	private IVCountryResourceService vCountryResourceService;

	/**
	 * 分页列表查询
	 *
	 * @param vCountryResource
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "资源视图-分页列表查询")
	@ApiOperation(value="资源视图-分页列表查询", notes="资源视图-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<VCountryResource>> queryPageList(VCountryResource Silian_vCountryResource,
								   @RequestParam(name="pageNo", defaultValue="1") Integer Silian_pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer Silian_pageSize,
								   HttpServletRequest Silian_req) {
		QueryWrapper<VCountryResource> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_vCountryResource, Silian_req.getParameterMap());
		Silian_queryWrapper.eq("post", "TEM");
		Page<VCountryResource> Silian_page = new Page<VCountryResource>(Silian_pageNo, Silian_pageSize);
		IPage<VCountryResource> Silian_pageList = vCountryResourceService.page(Silian_page, Silian_queryWrapper);
		return Result.OK(Silian_pageList);
	}

	/**
	 *   添加
	 *
	 * @param vCountryResource
	 * @return
	 */
	@AutoLog(value = "资源视图-添加")
	@ApiOperation(value="资源视图-添加", notes="资源视图-添加")
	//@RequiresPermissions("org.jeecg.modules:v_country_resource:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody VCountryResource Silian_vCountryResource) {
		vCountryResourceService.save(Silian_vCountryResource);
		return Result.OK("添加成功！");
	}

	/**
	 *  编辑
	 *
	 * @param vCountryResource
	 * @return
	 */
	@AutoLog(value = "资源视图-编辑")
	@ApiOperation(value="资源视图-编辑", notes="资源视图-编辑")
	//@RequiresPermissions("org.jeecg.modules:v_country_resource:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody VCountryResource Silian_vCountryResource) {
		vCountryResourceService.updateById(Silian_vCountryResource);
		return Result.OK("编辑成功!");
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "资源视图-通过id删除")
	@ApiOperation(value="资源视图-通过id删除", notes="资源视图-通过id删除")
	//@RequiresPermissions("org.jeecg.modules:v_country_resource:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String Silian_id) {
		vCountryResourceService.removeById(Silian_id);
		return Result.OK("删除成功!");
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "资源视图-批量删除")
	@ApiOperation(value="资源视图-批量删除", notes="资源视图-批量删除")
	//@RequiresPermissions("org.jeecg.modules:v_country_resource:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String Silian_ids) {
		this.vCountryResourceService.removeByIds(Arrays.asList(Silian_ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "资源视图-通过id查询")
	@ApiOperation(value="资源视图-通过id查询", notes="资源视图-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<VCountryResource> queryById(@RequestParam(name="id",required=true) String Silian_id) {
		VCountryResource Silian_vCountryResource = vCountryResourceService.getById(Silian_id);
		if(Silian_vCountryResource==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(Silian_vCountryResource);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param vCountryResource
    */
    //@RequiresPermissions("org.jeecg.modules:v_country_resource:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest Silian_request, VCountryResource Silian_vCountryResource) {
        return super.exportXls(Silian_request, Silian_vCountryResource, VCountryResource.class, "资源视图");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    //@RequiresPermissions("v_country_resource:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest Silian_request, HttpServletResponse Silian_response) {
        return super.importExcel(Silian_request, Silian_response, VCountryResource.class);
    }

}
