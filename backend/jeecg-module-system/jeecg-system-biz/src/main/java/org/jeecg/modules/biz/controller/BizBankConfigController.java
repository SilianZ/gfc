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

import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysDepartService;
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
import org.jeecg.modules.biz.entity.BizSubjectBalance;
import org.jeecg.modules.biz.entity.BizBankConfig;
import org.jeecg.modules.biz.vo.BizBankConfigPage;
import org.jeecg.modules.biz.service.IBizBankConfigService;
import org.jeecg.modules.biz.service.IBizSubjectBalanceService;
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
 * @Description: 银行管理
 * @Author: jeecg-boot
 * @Date:   2023-09-28
 * @Version: V1.0
 */
@Api(tags="银行管理")
@RestController
@RequestMapping("/biz/bizBankConfig")
@Slf4j
public class BizBankConfigController {
	@Autowired
	private IBizBankConfigService bizBankConfigService;
	@Autowired
	private IBizSubjectBalanceService bizSubjectBalanceService;
	@Autowired
	private ISysDepartService sysDepartService;

	/**
	 * 分页列表查询
	 *
	 * @param bizBankConfig
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "银行管理-分页列表查询")
	@ApiOperation(value="银行管理-分页列表查询", notes="银行管理-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<BizBankConfig>> queryPageList(BizBankConfig Silian_bizBankConfig,
								   @RequestParam(name="pageNo", defaultValue="1") Integer Silian_pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer Silian_pageSize,
								   HttpServletRequest Silian_req) {
		QueryWrapper<BizBankConfig> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_bizBankConfig, Silian_req.getParameterMap());
		Page<BizBankConfig> Silian_page = new Page<BizBankConfig>(Silian_pageNo, Silian_pageSize);
		IPage<BizBankConfig> Silian_pageList = bizBankConfigService.page(Silian_page, Silian_queryWrapper);
		return Result.OK(Silian_pageList);
	}

	/**
	 *   添加
	 *
	 * @param bizBankConfigPage
	 * @return
	 */
	@AutoLog(value = "银行管理-添加")
	@ApiOperation(value="银行管理-添加", notes="银行管理-添加")
    //@RequiresPermissions("org.jeecg.modules:biz_bank_config:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody BizBankConfigPage Silian_bizBankConfigPage) {
		BizBankConfig Silian_bizBankConfig = new BizBankConfig();
		BeanUtils.copyProperties(Silian_bizBankConfigPage, Silian_bizBankConfig);
		bizBankConfigService.saveMain(Silian_bizBankConfig, Silian_bizBankConfigPage.getBizSubjectBalanceList());
		return Result.OK("添加成功！");
	}

	/**
	 *  编辑
	 *
	 * @param bizBankConfigPage
	 * @return
	 */
	@AutoLog(value = "银行管理-编辑")
	@ApiOperation(value="银行管理-编辑", notes="银行管理-编辑")
    //@RequiresPermissions("org.jeecg.modules:biz_bank_config:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody BizBankConfigPage Silian_bizBankConfigPage) {
		BizBankConfig Silian_bizBankConfig = new BizBankConfig();
		BeanUtils.copyProperties(Silian_bizBankConfigPage, Silian_bizBankConfig);
		BizBankConfig Silian_bizBankConfigEntity = bizBankConfigService.getById(Silian_bizBankConfig.getId());
		if(Silian_bizBankConfigEntity==null) {
			return Result.error("未找到对应数据");
		}
		bizBankConfigService.updateMain(Silian_bizBankConfig, Silian_bizBankConfigPage.getBizSubjectBalanceList());
		return Result.OK("编辑成功!");
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "银行管理-通过id删除")
	@ApiOperation(value="银行管理-通过id删除", notes="银行管理-通过id删除")
    //@RequiresPermissions("org.jeecg.modules:biz_bank_config:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String Silian_id) {
		bizBankConfigService.delMain(Silian_id);
		return Result.OK("删除成功!");
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "银行管理-批量删除")
	@ApiOperation(value="银行管理-批量删除", notes="银行管理-批量删除")
    //@RequiresPermissions("org.jeecg.modules:biz_bank_config:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String Silian_ids) {
		this.bizBankConfigService.delBatchMain(Arrays.asList(Silian_ids.split(",")));
		return Result.OK("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "银行管理-通过id查询")
	@ApiOperation(value="银行管理-通过id查询", notes="银行管理-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<BizBankConfig> queryById(@RequestParam(name="id",required=true) String Silian_id) {
		BizBankConfig Silian_bizBankConfig = bizBankConfigService.getById(Silian_id);
		if(Silian_bizBankConfig==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(Silian_bizBankConfig);

	}

	/**
	 * 查询税率和税额
	 *
	 * @return
	 */
	//@AutoLog(value = "银行管理-查询税率和税额")
	@ApiOperation(value="银行管理-查询税率和税额", notes="银行管理-查询税率和税额")
	@GetMapping(value = "/queryTaxs")
		public Result<BizBankConfig> queryTaxes(@RequestParam(name="userId",required=true) String Silian_userId,
												@RequestParam(name="taxAmount",required=true) Double Silian_taxAmount,
												@RequestParam(name="isTransnational",required=true) Boolean Silian_isTransnational) {
		SysDepart Silian_depart = sysDepartService.queryDepartsByUsername(Silian_userId).get(0);
		BizBankConfig Silian_config = bizBankConfigService.queryTaxes(Silian_taxAmount, Silian_depart.getId(), Silian_isTransnational);
		if(Silian_config==null) {
			return Result.error("未找到对应税率信息");
		}
		return Result.OK(Silian_config);

	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "账户余额通过主表ID查询")
	@ApiOperation(value="账户余额主表ID查询", notes="账户余额-通主表ID查询")
	@GetMapping(value = "/queryBizSubjectBalanceByMainId")
	public Result<List<BizSubjectBalance>> queryBizSubjectBalanceListByMainId(@RequestParam(name="id",required=true) String Silian_id) {
		List<BizSubjectBalance> Silian_bizSubjectBalanceList = bizSubjectBalanceService.selectByMainId(Silian_id);
		return Result.OK(Silian_bizSubjectBalanceList);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param bizBankConfig
    */
    //@RequiresPermissions("org.jeecg.modules:biz_bank_config:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest Silian_request, BizBankConfig Silian_bizBankConfig) {
      // Step.1 组装查询条件查询数据
      QueryWrapper<BizBankConfig> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_bizBankConfig, Silian_request.getParameterMap());
      LoginUser Silian_sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

      //配置选中数据查询条件
      String Silian_selections = Silian_request.getParameter("selections");
      if(oConvertUtils.isNotEmpty(Silian_selections)) {
         List<String> Silian_selectionList = Arrays.asList(Silian_selections.split(","));
         Silian_queryWrapper.in("id",Silian_selectionList);
      }
      //Step.2 获取导出数据
      List<BizBankConfig> Silian_bizBankConfigList = bizBankConfigService.list(Silian_queryWrapper);

      // Step.3 组装pageList
      List<BizBankConfigPage> Silian_pageList = new ArrayList<BizBankConfigPage>();
      for (BizBankConfig Silian_main : Silian_bizBankConfigList) {
          BizBankConfigPage Silian_vo = new BizBankConfigPage();
          BeanUtils.copyProperties(Silian_main, Silian_vo);
          List<BizSubjectBalance> Silian_bizSubjectBalanceList = bizSubjectBalanceService.selectByMainId(Silian_main.getId());
          Silian_vo.setBizSubjectBalanceList(Silian_bizSubjectBalanceList);
          Silian_pageList.add(Silian_vo);
      }

      // Step.4 AutoPoi 导出Excel
      ModelAndView Silian_mv = new ModelAndView(new JeecgEntityExcelView());
      Silian_mv.addObject(NormalExcelConstants.FILE_NAME, "银行管理列表");
      Silian_mv.addObject(NormalExcelConstants.CLASS, BizBankConfigPage.class);
      Silian_mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("银行管理数据", "导出人:"+Silian_sysUser.getRealname(), "银行管理"));
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
    //@RequiresPermissions("org.jeecg.modules:biz_bank_config:importExcel")
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
              List<BizBankConfigPage> Silian_list = ExcelImportUtil.importExcel(Silian_file.getInputStream(), BizBankConfigPage.class, Silian_params);
              for (BizBankConfigPage Silian_page : Silian_list) {
                  BizBankConfig Silian_po = new BizBankConfig();
                  BeanUtils.copyProperties(Silian_page, Silian_po);
                  bizBankConfigService.saveMain(Silian_po, Silian_page.getBizSubjectBalanceList());
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
