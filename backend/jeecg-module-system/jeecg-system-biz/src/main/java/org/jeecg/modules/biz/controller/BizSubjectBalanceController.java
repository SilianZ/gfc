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
import org.jeecg.modules.biz.entity.BizSubjectBalance;
import org.jeecg.modules.biz.service.IBizFiscalYearService;
import org.jeecg.modules.biz.service.IBizSubjectBalanceService;

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
 * @Description: 主体资源余额
 * @Author: jeecg-boot
 * @Date:   2023-09-24
 * @Version: V1.0
 */
@Api(tags="主体资源余额")
@RestController
@RequestMapping("/biz/bizSubjectBalance")
@Slf4j
public class BizSubjectBalanceController extends JeecgController<BizSubjectBalance, IBizSubjectBalanceService> {
	@Autowired
	private IBizSubjectBalanceService bizSubjectBalanceService;
	 @Autowired
	 private IBizFiscalYearService bizFiscalYearService;

	/**
	 * 分页列表查询
	 *
	 * @param bizSubjectBalance
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "主体资源余额-分页列表查询")
	@ApiOperation(value="主体资源余额-分页列表查询", notes="主体资源余额-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<BizSubjectBalance>> queryPageList(BizSubjectBalance Silian_bizSubjectBalance,
								   @RequestParam(name="pageNo", defaultValue="1") Integer Silian_pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer Silian_pageSize,
								   HttpServletRequest Silian_req) {
		QueryWrapper<BizSubjectBalance> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_bizSubjectBalance, Silian_req.getParameterMap());
		Page<BizSubjectBalance> Silian_page = new Page<BizSubjectBalance>(Silian_pageNo, Silian_pageSize);
		IPage<BizSubjectBalance> Silian_pageList = bizSubjectBalanceService.page(Silian_page, Silian_queryWrapper);
		return Result.OK(Silian_pageList);
	}

	/**
	 *   添加
	 *
	 * @param bizSubjectBalance
	 * @return
	 */
	@AutoLog(value = "主体资源余额-添加")
	@ApiOperation(value="主体资源余额-添加", notes="主体资源余额-添加")
	//@RequiresPermissions("org.jeecg.modules:biz_subject_balance:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody BizSubjectBalance Silian_bizSubjectBalance) {
		bizSubjectBalanceService.save(Silian_bizSubjectBalance);
		return Result.OK("添加成功！");
	}

	/**
	 *  编辑
	 *
	 * @param bizSubjectBalance
	 * @return
	 */
	@AutoLog(value = "主体资源余额-编辑")
	@ApiOperation(value="主体资源余额-编辑", notes="主体资源余额-编辑")
	//@RequiresPermissions("org.jeecg.modules:biz_subject_balance:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody BizSubjectBalance Silian_bizSubjectBalance) {
		bizSubjectBalanceService.updateById(Silian_bizSubjectBalance);
		return Result.OK("编辑成功!");
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "主体资源余额-通过id删除")
	@ApiOperation(value="主体资源余额-通过id删除", notes="主体资源余额-通过id删除")
	//@RequiresPermissions("org.jeecg.modules:biz_subject_balance:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String Silian_id) {
		bizSubjectBalanceService.removeById(Silian_id);
		return Result.OK("删除成功!");
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "主体资源余额-批量删除")
	@ApiOperation(value="主体资源余额-批量删除", notes="主体资源余额-批量删除")
	//@RequiresPermissions("org.jeecg.modules:biz_subject_balance:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String Silian_ids) {
		this.bizSubjectBalanceService.removeByIds(Arrays.asList(Silian_ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "主体资源余额-通过id查询")
	@ApiOperation(value="主体资源余额-通过id查询", notes="主体资源余额-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<BizSubjectBalance> queryById(@RequestParam(name="id",required=true) String Silian_id) {
		BizSubjectBalance Silian_bizSubjectBalance = bizSubjectBalanceService.getById(Silian_id);
		if(Silian_bizSubjectBalance==null) {
			return Result.error("未找到该主体账户数据");
		}
		return Result.OK(Silian_bizSubjectBalance);
	}

	/**
	 * 通过userId查询
	 *
	 * @param userId
	 * @return
	 */
	//@AutoLog(value = "主体资源余额-通过userId查询")
	@ApiOperation(value="主体资源余额-通过userId查询", notes="主体资源余额-通过userId查询")
	@GetMapping(value = "/queryByUserId")
	public Result<BizSubjectBalance> queryByUserId(@RequestParam(name="userId",required=true) String Silian_userId) {
		BizSubjectBalance Silian_bizSubjectBalance = bizSubjectBalanceService.getByUserId(Silian_userId);
		Silian_bizSubjectBalance.setYearCode(bizFiscalYearService.getActiveYearCode());
		if(Silian_bizSubjectBalance==null) {
			return Result.error("未找到该主体账户数据");
		}
		return Result.OK(Silian_bizSubjectBalance);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param bizSubjectBalance
    */
    //@RequiresPermissions("org.jeecg.modules:biz_subject_balance:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest Silian_request, BizSubjectBalance Silian_bizSubjectBalance) {
        return super.exportXls(Silian_request, Silian_bizSubjectBalance, BizSubjectBalance.class, "主体资源余额");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    //@RequiresPermissions("biz_subject_balance:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest Silian_request, HttpServletResponse Silian_response) {
        return super.importExcel(Silian_request, Silian_response, BizSubjectBalance.class);
    }

}
