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
	public Result<IPage<BizTeamResource>> queryPageList(BizTeamResource Silian_bizTeamResource,
								   @RequestParam(name="pageNo", defaultValue="1") Integer Silian_pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer Silian_pageSize,
								   HttpServletRequest Silian_req) {
		QueryWrapper<BizTeamResource> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_bizTeamResource, Silian_req.getParameterMap());
		Silian_queryWrapper.ne("resource_type", "LS");
		Page<BizTeamResource> Silian_page = new Page<BizTeamResource>(Silian_pageNo, Silian_pageSize);
		IPage<BizTeamResource> Silian_pageList = bizTeamResourceService.page(Silian_page, Silian_queryWrapper);
		return Result.OK(Silian_pageList);
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
	public Result<String> add(@RequestBody BizTeamResourcePage Silian_bizTeamResourcePage) {
		BizTeamResource Silian_bizTeamResource = new BizTeamResource();
		BeanUtils.copyProperties(Silian_bizTeamResourcePage, Silian_bizTeamResource);
		bizTeamResourceService.saveMain(Silian_bizTeamResource, Silian_bizTeamResourcePage.getBizResourceRightsList());
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
	public Result<String> edit(@RequestBody BizTeamResourcePage Silian_bizTeamResourcePage) {
		BizTeamResource Silian_bizTeamResource = new BizTeamResource();
		BeanUtils.copyProperties(Silian_bizTeamResourcePage, Silian_bizTeamResource);
		BizTeamResource Silian_bizTeamResourceEntity = bizTeamResourceService.getById(Silian_bizTeamResource.getId());
		if(Silian_bizTeamResourceEntity==null) {
			return Result.error("未找到对应数据");
		}
		bizTeamResourceService.updateMain(Silian_bizTeamResource, Silian_bizTeamResourcePage.getBizResourceRightsList());
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
	public Result<String> delete(@RequestParam(name="id",required=true) String Silian_id) {
		bizTeamResourceService.delMain(Silian_id);
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
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String Silian_ids) {
		this.bizTeamResourceService.delBatchMain(Arrays.asList(Silian_ids.split(",")));
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
	public Result<BizTeamResource> queryById(@RequestParam(name="id",required=true) String Silian_id) {
		BizTeamResource Silian_bizTeamResource = bizTeamResourceService.getById(Silian_id);
		if(Silian_bizTeamResource.getResourceType().equals("LS")){//老式工厂，效率为0.5，需考虑银行投资加成
			Silian_bizTeamResource.setProductRate(0.50);
			SysUser Silian_belongUser = sysUserService.getUserByName(Silian_bizTeamResource.getUserId());
			BizBankConfig Silian_bizBankConfig = bizBankConfigService.getByDeptId(Silian_belongUser.getDepartIds().split(",")[0]);
			if(Silian_bizBankConfig != null && "JGC".equals(Silian_bizBankConfig.getInvestPlan())){
				Silian_bizTeamResource.setIsBankRate(true);
				Silian_bizTeamResource.setBankRate(0.65);
			}
		}else{
			if(Silian_bizTeamResource.getResourceType().equals("JM")){//伽马工厂，效率为1.5
				Silian_bizTeamResource.setProductRate(1.50);
			}else{//马里奥小岛工厂，效率为1.0
				Silian_bizTeamResource.setProductRate(1.00);
			}
			Silian_bizTeamResource.setIsBankRate(false);
		}
		if(Silian_bizTeamResource==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(Silian_bizTeamResource);

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
	public Result<List<BizResourceRights>> queryBizResourceRightsListByMainId(@RequestParam(name="id",required=true) String Silian_id) {
		List<BizResourceRights> Silian_bizResourceRightsList = bizResourceRightsService.selectByMainId(Silian_id);
		return Result.OK(Silian_bizResourceRightsList);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param bizTeamResource
    */
    //@RequiresPermissions("org.jeecg.modules:biz_team_resource:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest Silian_request, BizTeamResource Silian_bizTeamResource) {
      // Step.1 组装查询条件查询数据
      QueryWrapper<BizTeamResource> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_bizTeamResource, Silian_request.getParameterMap());
      LoginUser Silian_sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

      //配置选中数据查询条件
      String Silian_selections = Silian_request.getParameter("selections");
      if(oConvertUtils.isNotEmpty(Silian_selections)) {
         List<String> Silian_selectionList = Arrays.asList(Silian_selections.split(","));
         Silian_queryWrapper.in("id",Silian_selectionList);
      }
      //Step.2 获取导出数据
      List<BizTeamResource> Silian_bizTeamResourceList = bizTeamResourceService.list(Silian_queryWrapper);

      // Step.3 组装pageList
      List<BizTeamResourcePage> Silian_pageList = new ArrayList<BizTeamResourcePage>();
      for (BizTeamResource Silian_main : Silian_bizTeamResourceList) {
          BizTeamResourcePage Silian_vo = new BizTeamResourcePage();
          BeanUtils.copyProperties(Silian_main, Silian_vo);
          List<BizResourceRights> Silian_bizResourceRightsList = bizResourceRightsService.selectByMainId(Silian_main.getId());
          Silian_vo.setBizResourceRightsList(Silian_bizResourceRightsList);
          Silian_pageList.add(Silian_vo);
      }

      // Step.4 AutoPoi 导出Excel
      ModelAndView Silian_mv = new ModelAndView(new JeecgEntityExcelView());
      Silian_mv.addObject(NormalExcelConstants.FILE_NAME, "团队资源列表");
      Silian_mv.addObject(NormalExcelConstants.CLASS, BizTeamResourcePage.class);
      Silian_mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("团队资源数据", "导出人:"+Silian_sysUser.getRealname(), "团队资源"));
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
    //@RequiresPermissions("org.jeecg.modules:biz_team_resource:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest Silian_request, HttpServletResponse Silian_response) {
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
              List<BizTeamResourcePage> Silian_list = ExcelImportUtil.importExcel(Silian_file.getInputStream(), BizTeamResourcePage.class, Silian_params);
              for (BizTeamResourcePage Silian_page : Silian_list) {
                  BizTeamResource Silian_po = new BizTeamResource();
                  BeanUtils.copyProperties(Silian_page, Silian_po);
                  bizTeamResourceService.saveMain(Silian_po, Silian_page.getBizResourceRightsList());
              }
              return Result.OK("文件导入成功！数据行数:" + Silian_list.size());
          } catch (Exception Silian_e) {
              log.error(Silian_e.getMessage(),Silian_e);
              return Result.error("文件导入失败:"+Silian_e.getMessage());
          } finally {
              try {
                  Silian_file.getInputStream().close();
              } catch (IOException Silian_e) {
                  Silian_e.printStackTrace();
              }
          }
      }
      return Result.OK("文件导入失败！");
    }

}
