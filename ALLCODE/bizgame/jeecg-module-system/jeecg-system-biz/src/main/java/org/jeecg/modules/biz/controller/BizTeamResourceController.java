package org.jeecg.modules.biz.controller;

import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeecg.modules.biz.entity.BizBankConfig;
import org.jeecg.modules.biz.service.IBizBankConfigService;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.vo.LoginUser;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.biz.entity.BizResourceRights;
import org.jeecg.modules.biz.entity.BizTeamResource;
import org.jeecg.modules.biz.vo.BizTeamResourcePage;
import org.jeecg.modules.biz.service.IBizTeamResourceService;
import org.jeecg.modules.biz.service.IBizResourceRightsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;

 /**
 * @Description: 团队资源
 * @Author: jeecg-boot
 * @Date:   2023-09-28
 * @Version: V1.0
 */
@Api(tags="团队资源")
@RestController
@RequestMapping("/biz/bizTeamResource")
@Slf4j
public class BizTeamResourceController {
	@Autowired
	private IBizTeamResourceService bizTeamResourceService;
	@Autowired
	private IBizResourceRightsService bizResourceRightsService;
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private IBizBankConfigService bizBankConfigService;

	/**
	 * 分页列表查询
	 *
	 * @param bizTeamResource
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "团队资源-分页列表查询")
	@ApiOperation(value="团队资源-分页列表查询", notes="团队资源-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<BizTeamResource>> queryPageList(BizTeamResource bizTeamResource,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<BizTeamResource> queryWrapper = QueryGenerator.initQueryWrapper(bizTeamResource, req.getParameterMap());
		queryWrapper.ne("resource_type", "LS");
		Page<BizTeamResource> page = new Page<BizTeamResource>(pageNo, pageSize);
		IPage<BizTeamResource> pageList = bizTeamResourceService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param bizTeamResourcePage
	 * @return
	 */
	@AutoLog(value = "团队资源-添加")
	@ApiOperation(value="团队资源-添加", notes="团队资源-添加")
    //@RequiresPermissions("org.jeecg.modules:biz_team_resource:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody BizTeamResourcePage bizTeamResourcePage) {
		BizTeamResource bizTeamResource = new BizTeamResource();
		BeanUtils.copyProperties(bizTeamResourcePage, bizTeamResource);
		bizTeamResourceService.saveMain(bizTeamResource, bizTeamResourcePage.getBizResourceRightsList());
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param bizTeamResourcePage
	 * @return
	 */
	@AutoLog(value = "团队资源-编辑")
	@ApiOperation(value="团队资源-编辑", notes="团队资源-编辑")
    //@RequiresPermissions("org.jeecg.modules:biz_team_resource:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody BizTeamResourcePage bizTeamResourcePage) {
		BizTeamResource bizTeamResource = new BizTeamResource();
		BeanUtils.copyProperties(bizTeamResourcePage, bizTeamResource);
		BizTeamResource bizTeamResourceEntity = bizTeamResourceService.getById(bizTeamResource.getId());
		if(bizTeamResourceEntity==null) {
			return Result.error("未找到对应数据");
		}
		bizTeamResourceService.updateMain(bizTeamResource, bizTeamResourcePage.getBizResourceRightsList());
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "团队资源-通过id删除")
	@ApiOperation(value="团队资源-通过id删除", notes="团队资源-通过id删除")
    //@RequiresPermissions("org.jeecg.modules:biz_team_resource:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		bizTeamResourceService.delMain(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "团队资源-批量删除")
	@ApiOperation(value="团队资源-批量删除", notes="团队资源-批量删除")
    //@RequiresPermissions("org.jeecg.modules:biz_team_resource:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.bizTeamResourceService.delBatchMain(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功！");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "团队资源-通过id查询")
	@ApiOperation(value="团队资源-通过id查询", notes="团队资源-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<BizTeamResource> queryById(@RequestParam(name="id",required=true) String id) {
		BizTeamResource bizTeamResource = bizTeamResourceService.getById(id);
		if(bizTeamResource.getResourceType().equals("LS")){//老式工厂，效率为0.5，需考虑银行投资加成
			bizTeamResource.setProductRate(0.50);
			SysUser belongUser = sysUserService.getUserByName(bizTeamResource.getUserId());
			BizBankConfig bizBankConfig = bizBankConfigService.getByDeptId(belongUser.getDepartIds().split(",")[0]);
			if(bizBankConfig != null && "JGC".equals(bizBankConfig.getInvestPlan())){
				bizTeamResource.setIsBankRate(true);
				bizTeamResource.setBankRate(0.65);
			}
		}else{
			if(bizTeamResource.getResourceType().equals("JM")){//伽马工厂，效率为1.5
				bizTeamResource.setProductRate(1.50);
			}else{//马里奥小岛工厂，效率为1.0
				bizTeamResource.setProductRate(1.00);
			}
			bizTeamResource.setIsBankRate(false);
		}
		if(bizTeamResource==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(bizTeamResource);

	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "使用权通过主表ID查询")
	@ApiOperation(value="使用权主表ID查询", notes="使用权-通主表ID查询")
	@GetMapping(value = "/queryBizResourceRightsByMainId")
	public Result<List<BizResourceRights>> queryBizResourceRightsListByMainId(@RequestParam(name="id",required=true) String id) {
		List<BizResourceRights> bizResourceRightsList = bizResourceRightsService.selectByMainId(id);
		return Result.OK(bizResourceRightsList);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param bizTeamResource
    */
    //@RequiresPermissions("org.jeecg.modules:biz_team_resource:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, BizTeamResource bizTeamResource) {
      // Step.1 组装查询条件查询数据
      QueryWrapper<BizTeamResource> queryWrapper = QueryGenerator.initQueryWrapper(bizTeamResource, request.getParameterMap());
      LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

      //配置选中数据查询条件
      String selections = request.getParameter("selections");
      if(oConvertUtils.isNotEmpty(selections)) {
         List<String> selectionList = Arrays.asList(selections.split(","));
         queryWrapper.in("id",selectionList);
      }
      //Step.2 获取导出数据
      List<BizTeamResource> bizTeamResourceList = bizTeamResourceService.list(queryWrapper);

      // Step.3 组装pageList
      List<BizTeamResourcePage> pageList = new ArrayList<BizTeamResourcePage>();
      for (BizTeamResource main : bizTeamResourceList) {
          BizTeamResourcePage vo = new BizTeamResourcePage();
          BeanUtils.copyProperties(main, vo);
          List<BizResourceRights> bizResourceRightsList = bizResourceRightsService.selectByMainId(main.getId());
          vo.setBizResourceRightsList(bizResourceRightsList);
          pageList.add(vo);
      }

      // Step.4 AutoPoi 导出Excel
      ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
      mv.addObject(NormalExcelConstants.FILE_NAME, "团队资源列表");
      mv.addObject(NormalExcelConstants.CLASS, BizTeamResourcePage.class);
      mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("团队资源数据", "导出人:"+sysUser.getRealname(), "团队资源"));
      mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
      return mv;
    }

    /**
    * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    //@RequiresPermissions("org.jeecg.modules:biz_team_resource:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
      MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
      Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
      for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
          // 获取上传文件对象
          MultipartFile file = entity.getValue();
          ImportParams params = new ImportParams();
          params.setTitleRows(2);
          params.setHeadRows(1);
          params.setNeedSave(true);
          try {
              List<BizTeamResourcePage> list = ExcelImportUtil.importExcel(file.getInputStream(), BizTeamResourcePage.class, params);
              for (BizTeamResourcePage page : list) {
                  BizTeamResource po = new BizTeamResource();
                  BeanUtils.copyProperties(page, po);
                  bizTeamResourceService.saveMain(po, page.getBizResourceRightsList());
              }
              return Result.OK("文件导入成功！数据行数:" + list.size());
          } catch (Exception e) {
              log.error(e.getMessage(),e);
              return Result.error("文件导入失败:"+e.getMessage());
          } finally {
              try {
                  file.getInputStream().close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
      }
      return Result.OK("文件导入失败！");
    }

}
