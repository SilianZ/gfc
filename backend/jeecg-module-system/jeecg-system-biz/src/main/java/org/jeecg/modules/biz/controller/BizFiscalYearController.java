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
import java.util.HashMap;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;

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
	public Result<IPage<BizFiscalYear>> queryPageList(BizFiscalYear Silian_bizFiscalYear,
								   @RequestParam(name="pageNo", defaultValue="1") Integer Silian_pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer Silian_pageSize,
								   HttpServletRequest Silian_req) {
		QueryWrapper<BizFiscalYear> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_bizFiscalYear, Silian_req.getParameterMap());
		Page<BizFiscalYear> Silian_page = new Page<BizFiscalYear>(Silian_pageNo, Silian_pageSize);
		IPage<BizFiscalYear> Silian_pageList = bizFiscalYearService.page(Silian_page, Silian_queryWrapper);
		return Result.OK(Silian_pageList);
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
	public Result<String> add(@RequestBody BizFiscalYear Silian_bizFiscalYear) {
		Silian_bizFiscalYear.setYearCode(bizFiscalYearService.getMaxYearCode()+1);
		Silian_bizFiscalYear.setStockInc(0.00);
		Silian_bizFiscalYear.setCurrencyInc(0.00);
		bizFiscalYearService.save(Silian_bizFiscalYear);
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
	public Result<String> edit(@RequestBody BizFiscalYear Silian_bizFiscalYear) {
		bizFiscalYearService.updateById(Silian_bizFiscalYear);

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
	public Result<String> end(@RequestParam(name="id",required=true) String Silian_id) {
		try {
			BizFiscalYear Silian_fiscalYear = bizFiscalYearService.getById(Silian_id);
			Silian_fiscalYear.setStatus("2");
			//更新资源出租状态
			bizFiscalYearService.updateResourceStatus(Silian_fiscalYear.getYearCode() + 1);
			bizFiscalYearService.updateById(Silian_fiscalYear);
		}catch (Exception Silian_e){
			Silian_e.printStackTrace();
			//回滚事务
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return Result.error("清算失败:" + Silian_e.getMessage());
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
	public Result<Date> start(@RequestParam(name="id",required=true) String Silian_id) {
		BizFiscalYear Silian_fiscalYear = bizFiscalYearService.getById(Silian_id);
		Silian_fiscalYear.setStatus("1");
		Random Silian_random = new Random();
        double Silian_min = -0.20;
        double Silian_max = 0.20;

        // 生成-0.2到+0.2之间的随机小数
        double Silian_randomValue = Silian_min + (Silian_max - Silian_min) * Silian_random.nextDouble();

        // 保留小数点后两位
        double Silian_formattedValue = Math.round(Silian_randomValue * 100.0) / 100.0;

		Silian_fiscalYear.setStockInc(Silian_formattedValue);//设定当年股票涨额

		Silian_random = new Random();
        Silian_min = -0.50;
        Silian_max = 0.50;

        // 生成-0.2到+0.2之间的随机小数
        Silian_randomValue = Silian_min + (Silian_max - Silian_min) * Silian_random.nextDouble();

        // 保留小数点后两位
        Silian_formattedValue = Math.round(Silian_randomValue * 100.0) / 100.0;

		Silian_fiscalYear.setCurrencyInc(Silian_formattedValue);//设定当年虚拟货币涨额

		bizFiscalYearService.updateById(Silian_fiscalYear);
		return Result.OK("财年开始");
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
	public Result<String> delete(@RequestParam(name="id",required=true) String Silian_id) {
		bizFiscalYearService.removeById(Silian_id);
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
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String Silian_ids) {
		this.bizFiscalYearService.removeByIds(Arrays.asList(Silian_ids.split(",")));
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
	public Result<BizFiscalYear> queryById(@RequestParam(name="id",required=true) String Silian_id) {
		BizFiscalYear Silian_bizFiscalYear = bizFiscalYearService.getById(Silian_id);
		if(Silian_bizFiscalYear==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(Silian_bizFiscalYear);
	}

	@AutoLog(value = "财年信息-打包财年信息")
	@ApiOperation(value="财年信息-打包财年信息", notes="财年信息-打包财年信息")
	@GetMapping(value = "/getYearNameAndStockInc")
	public Result<Map<String, Object>[]> getYearNameAndStockInc() {

		List<BizFiscalYear> Silian_list = bizFiscalYearService.getAllFiscalYears();
		List<Map<String, Object>> Silian_resultList = Silian_list.stream()
			.map(Silian_fy -> {
				Map<String, Object> Silian_map = new HashMap<>();
				Silian_map.put("type", Silian_fy.getYearName());
				Silian_map.put("Percentage", Silian_fy.getStockInc());
				return Silian_map;
			})
			.collect(Collectors.toList());

		Map<String, Object>[] Silian_resultArray = new Map[Silian_resultList.size()];
	Silian_resultArray = Silian_resultList.toArray(Silian_resultArray);
		return Result.OK(Silian_resultArray);
	}

	@AutoLog(value = "财年信息-打包财年信息2")
	@ApiOperation(value="财年信息-打包财年信息2", notes="财年信息-打包财年信息2")
	@GetMapping(value = "/getYearNameAndVirtualCurrencyInc")
	public Result<Map<String, Object>[]> getYearNameAndVirtualCurrencyInc() {

		List<BizFiscalYear> Silian_list = bizFiscalYearService.getAllFiscalYears();
		List<Map<String, Object>> Silian_resultList = Silian_list.stream()
			.map(Silian_fy -> {
				Map<String, Object> Silian_map = new HashMap<>();
				Silian_map.put("type", Silian_fy.getYearName());
				Silian_map.put("Percentage", Silian_fy.getCurrencyInc());
				return Silian_map;
			})
			.collect(Collectors.toList());

		Map<String, Object>[] Silian_resultArray = new Map[Silian_resultList.size()];
	Silian_resultArray = Silian_resultList.toArray(Silian_resultArray);
		return Result.OK(Silian_resultArray);
	}
    /**
    * 导出excel
    *
    * @param request
    * @param bizFiscalYear
    */
    //@RequiresPermissions("org.jeecg.modules:biz_fiscal_year:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest Silian_request, BizFiscalYear Silian_bizFiscalYear) {
        return super.exportXls(Silian_request, Silian_bizFiscalYear, BizFiscalYear.class, "财年信息");
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
    public Result<?> importExcel(HttpServletRequest Silian_request, HttpServletResponse Silian_response) {
        return super.importExcel(Silian_request, Silian_response, BizFiscalYear.class);
    }

}
