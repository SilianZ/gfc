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
import org.jeecg.modules.biz.entity.BizProductionTrans;
import org.jeecg.modules.biz.entity.BizSubjectBalance;
import org.jeecg.modules.biz.service.IBizBankConfigService;
import org.jeecg.modules.biz.service.IBizFiscalYearService;
import org.jeecg.modules.biz.service.IBizProductionTransService;
import org.jeecg.modules.biz.service.IBizSubjectBalanceService;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysDepartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * @Description: 成品交易
 * @Author: jeecg-boot
 * @Date:   2023-09-28
 * @Version: V1.0
 */
@Api(tags="成品交易")
@RestController
@RequestMapping("/biz/bizProductionTrans")
@Slf4j
public class BizProductionTransController extends JeecgController<BizProductionTrans, IBizProductionTransService> {
	@Autowired
	private IBizProductionTransService bizProductionTransService;
	 @Autowired
	 private IBizSubjectBalanceService bizSubjectBalanceService;
	 @Autowired
	 private IBizFiscalYearService bizFiscalYearService;
	@Autowired
	private IBizBankConfigService bizBankConfigService;
	@Autowired
	private ISysDepartService sysDepartService;

	/**
	 * 分页列表查询
	 *
	 * @param bizProductionTrans
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "成品交易-分页列表查询")
	@ApiOperation(value="成品交易-分页列表查询", notes="成品交易-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<BizProductionTrans>> queryPageList(BizProductionTrans Silian_bizProductionTrans,
								   @RequestParam(name="pageNo", defaultValue="1") Integer Silian_pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer Silian_pageSize,
								   HttpServletRequest Silian_req) {
		QueryWrapper<BizProductionTrans> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_bizProductionTrans, Silian_req.getParameterMap());
		Page<BizProductionTrans> Silian_page = new Page<BizProductionTrans>(Silian_pageNo, Silian_pageSize);
		IPage<BizProductionTrans> Silian_pageList = bizProductionTransService.page(Silian_page, Silian_queryWrapper);
		return Result.OK(Silian_pageList);
	}

	/**
	 *   添加
	 *
	 * @param bizProductionTrans
	 * @return
	 */
	@AutoLog(value = "成品交易-添加")
	@ApiOperation(value="成品交易-添加", notes="成品交易-添加")
	//@RequiresPermissions("org.jeecg.modules:biz_production_trans:add")
	@PostMapping(value = "/add")
	@Transactional(rollbackFor = Exception.class)
	public Result<String> add(@RequestBody BizProductionTrans Silian_bizProductionTrans) {
		try {
			//设置所属财年
			Silian_bizProductionTrans.setYearCode(bizFiscalYearService.getActiveYearCode());
			//扣减卖方成品库存
			BizSubjectBalance Silian_sellerBalance = bizSubjectBalanceService.getByUserId(Silian_bizProductionTrans.getSellerId());
			Silian_sellerBalance.setChipAcct((Silian_sellerBalance.getChipAcct() == null ? 0 : Silian_sellerBalance.getChipAcct()) - (Silian_bizProductionTrans.getChipNumber() == null ? 0 : Silian_bizProductionTrans.getChipNumber()));
			Silian_sellerBalance.setCardAcct((Silian_sellerBalance.getCardAcct() == null ? 0 : Silian_sellerBalance.getCardAcct()) - (Silian_bizProductionTrans.getCardNumber() == null ? 0 : Silian_bizProductionTrans.getCardNumber()));
			Silian_sellerBalance.setEnergyAcct((Silian_sellerBalance.getEnergyAcct() == null ? 0 : Silian_sellerBalance.getEnergyAcct()) - (Silian_bizProductionTrans.getEnergyNumber() == null ? 0 : Silian_bizProductionTrans.getEnergyNumber()));
			Silian_sellerBalance.setToyAcct((Silian_sellerBalance.getToyAcct() == null ? 0 : Silian_sellerBalance.getToyAcct()) - (Silian_bizProductionTrans.getToyNumber() == null ? 0 : Silian_bizProductionTrans.getToyNumber()));
			//扣除交易所得税
			SysDepart Silian_depart = sysDepartService.queryDepartsByUsername(Silian_bizProductionTrans.getSellerId()).get(0);
			Double Silian_taxAmount = bizBankConfigService.collectTaxes(Silian_bizProductionTrans.getTransPrice(), Silian_bizProductionTrans.getIsTransnational().equals("Y"), Silian_depart.getId());
			//增加卖方现金余额
			Silian_sellerBalance.setCashAcct((Silian_sellerBalance.getCashAcct() == null ? 0 : Silian_sellerBalance.getCashAcct()) + (Silian_bizProductionTrans.getTransPrice() == null ? 0 : Silian_bizProductionTrans.getTransPrice() - Silian_taxAmount));
			//增加买方成品库存
			BizSubjectBalance Silian_buyerBalance = bizSubjectBalanceService.getByUserId(Silian_bizProductionTrans.getBuyerId());
			Silian_buyerBalance.setChipAcct((Silian_buyerBalance.getChipAcct() == null ? 0 : Silian_buyerBalance.getChipAcct()) + (Silian_bizProductionTrans.getChipNumber() == null ? 0 : Silian_bizProductionTrans.getChipNumber()));
			Silian_buyerBalance.setCardAcct((Silian_buyerBalance.getCardAcct() == null ? 0 : Silian_buyerBalance.getCardAcct()) + (Silian_bizProductionTrans.getCardNumber() == null ? 0 : Silian_bizProductionTrans.getCardNumber()));
			Silian_buyerBalance.setEnergyAcct((Silian_buyerBalance.getEnergyAcct() == null ? 0 : Silian_buyerBalance.getEnergyAcct()) + (Silian_bizProductionTrans.getEnergyNumber() == null ? 0 : Silian_bizProductionTrans.getEnergyNumber()));
			Silian_buyerBalance.setToyAcct((Silian_buyerBalance.getToyAcct() == null ? 0 : Silian_buyerBalance.getToyAcct()) + (Silian_bizProductionTrans.getToyNumber() == null ? 0 : Silian_bizProductionTrans.getToyNumber()));
			//减少买方现金余额
			Silian_buyerBalance.setCashAcct((Silian_buyerBalance.getCashAcct() == null ? 0 : Silian_buyerBalance.getCashAcct()) - (Silian_bizProductionTrans.getTransPrice() == null ? 0 : Silian_bizProductionTrans.getTransPrice()));
			if(Silian_sellerBalance.getChipAcct() < 0 ||  Silian_sellerBalance.getCardAcct() < 0 || Silian_sellerBalance.getEnergyAcct() < 0 || Silian_sellerBalance.getToyAcct() < 0 ){
				throw new Exception("卖家成品库存不足");
			}
			if(Silian_buyerBalance.getCashAcct() < 0){
				throw new Exception("买家现金余额不足");
			}
			bizSubjectBalanceService.updateById(Silian_sellerBalance);
			bizSubjectBalanceService.updateById(Silian_buyerBalance);
			bizProductionTransService.save(Silian_bizProductionTrans);
		}catch (Exception Silian_e){
			Silian_e.printStackTrace();
			//回滚事务
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return Result.error("交易失败：" + Silian_e.getMessage());
		}
		return Result.OK("交易成功！");
	}

	/**
	 *  编辑
	 *
	 * @param bizProductionTrans
	 * @return
	 */
	@AutoLog(value = "成品交易-编辑")
	@ApiOperation(value="成品交易-编辑", notes="成品交易-编辑")
	//@RequiresPermissions("org.jeecg.modules:biz_production_trans:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody BizProductionTrans Silian_bizProductionTrans) {
		bizProductionTransService.updateById(Silian_bizProductionTrans);
		return Result.OK("编辑成功!");
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "成品交易-通过id删除")
	@ApiOperation(value="成品交易-通过id删除", notes="成品交易-通过id删除")
	//@RequiresPermissions("org.jeecg.modules:biz_production_trans:delete")
	@DeleteMapping(value = "/delete")
	@Transactional(rollbackFor = Exception.class)
	public Result<String> delete(@RequestParam(name="id",required=true) String Silian_id) {
		try{
			BizProductionTrans Silian_bizProductionTrans = bizProductionTransService.getById(Silian_id);
			//增加卖方成品库存
			BizSubjectBalance Silian_sellerBalance = bizSubjectBalanceService.getByUserId(Silian_bizProductionTrans.getSellerId());
			Silian_sellerBalance.setChipAcct((Silian_sellerBalance.getChipAcct() == null ? 0 : Silian_sellerBalance.getChipAcct()) + (Silian_bizProductionTrans.getChipNumber() == null ? 0 : Silian_bizProductionTrans.getChipNumber()));
			Silian_sellerBalance.setCardAcct((Silian_sellerBalance.getCardAcct() == null ? 0 : Silian_sellerBalance.getCardAcct()) + (Silian_bizProductionTrans.getCardNumber() == null ? 0 : Silian_bizProductionTrans.getCardNumber()));
			Silian_sellerBalance.setEnergyAcct((Silian_sellerBalance.getEnergyAcct() == null ? 0 : Silian_sellerBalance.getEnergyAcct()) + (Silian_bizProductionTrans.getEnergyNumber() == null ? 0 : Silian_bizProductionTrans.getEnergyNumber()));
			Silian_sellerBalance.setToyAcct((Silian_sellerBalance.getToyAcct() == null ? 0 : Silian_sellerBalance.getToyAcct()) + (Silian_bizProductionTrans.getToyNumber() == null ? 0 : Silian_bizProductionTrans.getToyNumber()));
			//冲销交易所得税
			SysDepart Silian_depart = sysDepartService.queryDepartsByUsername(Silian_bizProductionTrans.getSellerId()).get(0);
			Double Silian_taxAmount = bizBankConfigService.offTaxes(Silian_bizProductionTrans.getTransPrice(), Silian_bizProductionTrans.getIsTransnational().equals("Y"), Silian_depart.getId());
			//减少卖方现金余额
			Silian_sellerBalance.setCashAcct((Silian_sellerBalance.getCashAcct() == null ? 0 : Silian_sellerBalance.getCashAcct()) - (Silian_bizProductionTrans.getTransPrice() == null ? 0 : Silian_bizProductionTrans.getTransPrice()) + Silian_taxAmount);
			//减少买方陈皮库存
			BizSubjectBalance Silian_buyerBalance = bizSubjectBalanceService.getByUserId(Silian_bizProductionTrans.getBuyerId());
			Silian_buyerBalance.setChipAcct((Silian_buyerBalance.getChipAcct() == null ? 0 : Silian_buyerBalance.getChipAcct()) - (Silian_bizProductionTrans.getChipNumber() == null ? 0 : Silian_bizProductionTrans.getChipNumber()));
			Silian_buyerBalance.setCardAcct((Silian_buyerBalance.getCardAcct() == null ? 0 : Silian_buyerBalance.getCardAcct()) - (Silian_bizProductionTrans.getCardNumber() == null ? 0 : Silian_bizProductionTrans.getCardNumber()));
			Silian_buyerBalance.setEnergyAcct((Silian_buyerBalance.getEnergyAcct() == null ? 0 : Silian_buyerBalance.getEnergyAcct()) - (Silian_bizProductionTrans.getEnergyNumber() == null ? 0 : Silian_bizProductionTrans.getEnergyNumber()));
			Silian_buyerBalance.setToyAcct((Silian_buyerBalance.getToyAcct() == null ? 0 : Silian_buyerBalance.getToyAcct()) - (Silian_bizProductionTrans.getToyNumber() == null ? 0 : Silian_bizProductionTrans.getToyNumber()));
			//增加买方现金余额
			Silian_buyerBalance.setCashAcct((Silian_buyerBalance.getCashAcct() == null ? 0 : Silian_buyerBalance.getCashAcct()) + (Silian_bizProductionTrans.getTransPrice() == null ? 0 : Silian_bizProductionTrans.getTransPrice()));
			if(Silian_buyerBalance.getCashAcct() < 0){
				throw new Exception("卖家现金余额不足");
			}
			if(Silian_sellerBalance.getChipAcct() < 0 ||  Silian_sellerBalance.getCardAcct() < 0 || Silian_sellerBalance.getEnergyAcct() < 0 || Silian_sellerBalance.getToyAcct() < 0){
				throw new Exception("卖家成品库存不足");
			}
			bizSubjectBalanceService.updateById(Silian_sellerBalance);
			bizSubjectBalanceService.updateById(Silian_buyerBalance);
			bizProductionTransService.removeById(Silian_id);
		}catch (Exception Silian_e){
			Silian_e.printStackTrace();
			//回滚事务
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return  Result.error("撤销失败：" + Silian_e.getMessage());
		}
		return Result.OK("删除成功!");
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "成品交易-批量删除")
	@ApiOperation(value="成品交易-批量删除", notes="成品交易-批量删除")
	//@RequiresPermissions("org.jeecg.modules:biz_production_trans:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String Silian_ids) {
		this.bizProductionTransService.removeByIds(Arrays.asList(Silian_ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "成品交易-通过id查询")
	@ApiOperation(value="成品交易-通过id查询", notes="成品交易-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<BizProductionTrans> queryById(@RequestParam(name="id",required=true) String Silian_id) {
		BizProductionTrans Silian_bizProductionTrans = bizProductionTransService.getById(Silian_id);
		if(Silian_bizProductionTrans==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(Silian_bizProductionTrans);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param bizProductionTrans
    */
    //@RequiresPermissions("org.jeecg.modules:biz_production_trans:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest Silian_request, BizProductionTrans Silian_bizProductionTrans) {
        return super.exportXls(Silian_request, Silian_bizProductionTrans, BizProductionTrans.class, "成品交易");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    //@RequiresPermissions("biz_production_trans:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest Silian_request, HttpServletResponse Silian_response) {
        return super.importExcel(Silian_request, Silian_response, BizProductionTrans.class);
    }

}
