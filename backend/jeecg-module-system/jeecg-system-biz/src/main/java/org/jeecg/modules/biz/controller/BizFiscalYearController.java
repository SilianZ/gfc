package org.jeecg.modules.biz.controller;

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
import org.jeecg.modules.biz.entity.BizFiscalYear;
import org.jeecg.modules.biz.service.IBizFiscalYearService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;

/**
 * @Description: 财年信息管理
 * @Author: chen.hu
 * @Date:   2023-09-23
 * @Version: V1.0
 */
@Api(tags="财年信息管理")
@RestController
@RequestMapping("/biz/bizFiscalYear")
@Slf4j
public class BizFiscalYearController extends JeecgController<BizFiscalYear, IBizFiscalYearService> {
	@Autowired
	private IBizFiscalYearService bizFiscalYearService;
	
	/**
	 * 分页列表查询
	 *
	 * @param bizFiscalYear
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "财年信息-分页列表查询")
	@ApiOperation(value="财年信息-分页列表查询", notes="财年信息-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<BizFiscalYear>> queryPageList(BizFiscalYear bizFiscalYear,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<BizFiscalYear> queryWrapper = QueryGenerator.initQueryWrapper(bizFiscalYear, req.getParameterMap());
		Page<BizFiscalYear> page = new Page<BizFiscalYear>(pageNo, pageSize);
		IPage<BizFiscalYear> pageList = bizFiscalYearService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param bizFiscalYear
	 * @return
	 * 
	 */
	@AutoLog(value = "财年信息-添加")
	@ApiOperation(value="财年信息-添加", notes="财年信息-添加")
	//@RequiresPermissions("org.jeecg.modules:biz_fiscal_year:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody BizFiscalYear bizFiscalYear) {
		bizFiscalYear.setYearCode(bizFiscalYearService.getMaxYearCode()+1);
		bizFiscalYearService.save(bizFiscalYear);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param bizFiscalYear
	 * @return
	 */
	@AutoLog(value = "财年信息-编辑")
	@ApiOperation(value="财年信息-编辑", notes="财年信息-编辑")
	//@RequiresPermissions("org.jeecg.modules:biz_fiscal_year:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody BizFiscalYear bizFiscalYear) {
		bizFiscalYearService.updateById(bizFiscalYear);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id清算
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "财年信息-通过id清算")
	@ApiOperation(value="财年信息-通过id清算", notes="财年信息-通过id清算")
	//@RequiresPermissions("org.jeecg.modules:biz_fiscal_year:delete")
	@GetMapping(value = "/end")
	@Transactional(rollbackFor = Exception.class)
	public Result<String> end(@RequestParam(name="id",required=true) String id) {
		try {
			BizFiscalYear fiscalYear = bizFiscalYearService.getById(id);
			fiscalYear.setStatus("2");
			//更新资源出租状态
			bizFiscalYearService.updateResourceStatus(fiscalYear.getYearCode() + 1);
			bizFiscalYearService.updateById(fiscalYear);
		}catch (Exception e){
			e.printStackTrace();
			//回滚事务
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return Result.error("清算失败:" + e.getMessage());
		}
		return Result.OK("清算");
	}
	/**
	 *   通过id开始
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "财年信息-通过id开始")
	@ApiOperation(value="财年信息-通过id开始", notes="财年信息-通过id开始")
	//@RequiresPermissions("org.jeecg.modules:biz_fiscal_year:delete")
	@GetMapping(value = "/start")
	public Result<Date> start(@RequestParam(name="id",required=true) String id) {
		BizFiscalYear fiscalYear = bizFiscalYearService.getById(id);
		fiscalYear.setStatus("1");

		bizFiscalYearService.updateById(fiscalYear);
		return Result.OK("开尼玛");
		/*
		BizFiscalYear fiscalYear = bizFiscalYearService.getById(id);
		if (fiscalYear == null) {
			return Result.error("未找到对应的财年信息");
		}
		fiscalYear.setStatus("1"); // 假设1代表财年已开始
		fiscalYear.setStartTime(new Date());
		boolean updated = bizFiscalYearService.updateById(fiscalYear);
		if (!updated) {
			return Result.error("财年开启失败");
		}
		return Result.OK("CNM"); // Return updated fiscal year object
		*/
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "财年信息-通过id删除")
	@ApiOperation(value="财年信息-通过id删除", notes="财年信息-通过id删除")
	//@RequiresPermissions("org.jeecg.modules:biz_fiscal_year:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		bizFiscalYearService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "财年信息-批量删除")
	@ApiOperation(value="财年信息-批量删除", notes="财年信息-批量删除")
	//@RequiresPermissions("org.jeecg.modules:biz_fiscal_year:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.bizFiscalYearService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "财年信息-通过id查询")
	@ApiOperation(value="财年信息-通过id查询", notes="财年信息-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<BizFiscalYear> queryById(@RequestParam(name="id",required=true) String id) {
		BizFiscalYear bizFiscalYear = bizFiscalYearService.getById(id);
		if(bizFiscalYear==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(bizFiscalYear);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param bizFiscalYear
    */
    //@RequiresPermissions("org.jeecg.modules:biz_fiscal_year:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, BizFiscalYear bizFiscalYear) {
        return super.exportXls(request, bizFiscalYear, BizFiscalYear.class, "财年信息");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    //@RequiresPermissions("biz_fiscal_year:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, BizFiscalYear.class);
    }

}
