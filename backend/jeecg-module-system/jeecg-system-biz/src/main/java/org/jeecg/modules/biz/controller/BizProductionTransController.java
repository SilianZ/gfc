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
	public Result<IPage<BizProductionTrans>> queryPageList(BizProductionTrans bizProductionTrans,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<BizProductionTrans> queryWrapper = QueryGenerator.initQueryWrapper(bizProductionTrans, req.getParameterMap());
		Page<BizProductionTrans> page = new Page<BizProductionTrans>(pageNo, pageSize);
		IPage<BizProductionTrans> pageList = bizProductionTransService.page(page, queryWrapper);
		return Result.OK(pageList);
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
	public Result<String> add(@RequestBody BizProductionTrans bizProductionTrans) {
		try {
			//设置所属财年
			bizProductionTrans.setYearCode(bizFiscalYearService.getActiveYearCode());
			//扣减卖方成品库存
			BizSubjectBalance sellerBalance = bizSubjectBalanceService.getByUserId(bizProductionTrans.getSellerId());
			sellerBalance.setChipAcct((sellerBalance.getChipAcct() == null ? 0 : sellerBalance.getChipAcct()) - (bizProductionTrans.getChipNumber() == null ? 0 : bizProductionTrans.getChipNumber()));
			sellerBalance.setCardAcct((sellerBalance.getCardAcct() == null ? 0 : sellerBalance.getCardAcct()) - (bizProductionTrans.getCardNumber() == null ? 0 : bizProductionTrans.getCardNumber()));
			sellerBalance.setEnergyAcct((sellerBalance.getEnergyAcct() == null ? 0 : sellerBalance.getEnergyAcct()) - (bizProductionTrans.getEnergyNumber() == null ? 0 : bizProductionTrans.getEnergyNumber()));
			sellerBalance.setToyAcct((sellerBalance.getToyAcct() == null ? 0 : sellerBalance.getToyAcct()) - (bizProductionTrans.getToyNumber() == null ? 0 : bizProductionTrans.getToyNumber()));
			//扣除交易所得税
			SysDepart depart = sysDepartService.queryDepartsByUsername(bizProductionTrans.getSellerId()).get(0);
			Double taxAmount = bizBankConfigService.collectTaxes(bizProductionTrans.getTransPrice(), bizProductionTrans.getIsTransnational().equals("Y"), depart.getId());
			//增加卖方现金余额
			sellerBalance.setCashAcct((sellerBalance.getCashAcct() == null ? 0 : sellerBalance.getCashAcct()) + (bizProductionTrans.getTransPrice() == null ? 0 : bizProductionTrans.getTransPrice() - taxAmount));
			//增加买方成品库存
			BizSubjectBalance buyerBalance = bizSubjectBalanceService.getByUserId(bizProductionTrans.getBuyerId());
			buyerBalance.setChipAcct((buyerBalance.getChipAcct() == null ? 0 : buyerBalance.getChipAcct()) + (bizProductionTrans.getChipNumber() == null ? 0 : bizProductionTrans.getChipNumber()));
			buyerBalance.setCardAcct((buyerBalance.getCardAcct() == null ? 0 : buyerBalance.getCardAcct()) + (bizProductionTrans.getCardNumber() == null ? 0 : bizProductionTrans.getCardNumber()));
			buyerBalance.setEnergyAcct((buyerBalance.getEnergyAcct() == null ? 0 : buyerBalance.getEnergyAcct()) + (bizProductionTrans.getEnergyNumber() == null ? 0 : bizProductionTrans.getEnergyNumber()));
			buyerBalance.setToyAcct((buyerBalance.getToyAcct() == null ? 0 : buyerBalance.getToyAcct()) + (bizProductionTrans.getToyNumber() == null ? 0 : bizProductionTrans.getToyNumber()));
			//减少买方现金余额
			buyerBalance.setCashAcct((buyerBalance.getCashAcct() == null ? 0 : buyerBalance.getCashAcct()) - (bizProductionTrans.getTransPrice() == null ? 0 : bizProductionTrans.getTransPrice()));
			if(sellerBalance.getChipAcct() < 0 ||  sellerBalance.getCardAcct() < 0 || sellerBalance.getEnergyAcct() < 0 || sellerBalance.getToyAcct() < 0 ){
				throw new Exception("卖家成品库存不足");
			}
			if(buyerBalance.getCashAcct() < 0){
				throw new Exception("买家现金余额不足");
			}
			bizSubjectBalanceService.updateById(sellerBalance);
			bizSubjectBalanceService.updateById(buyerBalance);
			bizProductionTransService.save(bizProductionTrans);
		}catch (Exception e){
			e.printStackTrace();
			//回滚事务
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return Result.error("交易失败：" + e.getMessage());
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
	public Result<String> edit(@RequestBody BizProductionTrans bizProductionTrans) {
		bizProductionTransService.updateById(bizProductionTrans);
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
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		try{
			BizProductionTrans bizProductionTrans = bizProductionTransService.getById(id);
			//增加卖方成品库存
			BizSubjectBalance sellerBalance = bizSubjectBalanceService.getByUserId(bizProductionTrans.getSellerId());
			sellerBalance.setChipAcct((sellerBalance.getChipAcct() == null ? 0 : sellerBalance.getChipAcct()) + (bizProductionTrans.getChipNumber() == null ? 0 : bizProductionTrans.getChipNumber()));
			sellerBalance.setCardAcct((sellerBalance.getCardAcct() == null ? 0 : sellerBalance.getCardAcct()) + (bizProductionTrans.getCardNumber() == null ? 0 : bizProductionTrans.getCardNumber()));
			sellerBalance.setEnergyAcct((sellerBalance.getEnergyAcct() == null ? 0 : sellerBalance.getEnergyAcct()) + (bizProductionTrans.getEnergyNumber() == null ? 0 : bizProductionTrans.getEnergyNumber()));
			sellerBalance.setToyAcct((sellerBalance.getToyAcct() == null ? 0 : sellerBalance.getToyAcct()) + (bizProductionTrans.getToyNumber() == null ? 0 : bizProductionTrans.getToyNumber()));
			//冲销交易所得税
			SysDepart depart = sysDepartService.queryDepartsByUsername(bizProductionTrans.getSellerId()).get(0);
			Double taxAmount = bizBankConfigService.offTaxes(bizProductionTrans.getTransPrice(), bizProductionTrans.getIsTransnational().equals("Y"), depart.getId());
			//减少卖方现金余额
			sellerBalance.setCashAcct((sellerBalance.getCashAcct() == null ? 0 : sellerBalance.getCashAcct()) - (bizProductionTrans.getTransPrice() == null ? 0 : bizProductionTrans.getTransPrice()) + taxAmount);
			//减少买方陈皮库存
			BizSubjectBalance buyerBalance = bizSubjectBalanceService.getByUserId(bizProductionTrans.getBuyerId());
			buyerBalance.setChipAcct((buyerBalance.getChipAcct() == null ? 0 : buyerBalance.getChipAcct()) - (bizProductionTrans.getChipNumber() == null ? 0 : bizProductionTrans.getChipNumber()));
			buyerBalance.setCardAcct((buyerBalance.getCardAcct() == null ? 0 : buyerBalance.getCardAcct()) - (bizProductionTrans.getCardNumber() == null ? 0 : bizProductionTrans.getCardNumber()));
			buyerBalance.setEnergyAcct((buyerBalance.getEnergyAcct() == null ? 0 : buyerBalance.getEnergyAcct()) - (bizProductionTrans.getEnergyNumber() == null ? 0 : bizProductionTrans.getEnergyNumber()));
			buyerBalance.setToyAcct((buyerBalance.getToyAcct() == null ? 0 : buyerBalance.getToyAcct()) - (bizProductionTrans.getToyNumber() == null ? 0 : bizProductionTrans.getToyNumber()));
			//增加买方现金余额
			buyerBalance.setCashAcct((buyerBalance.getCashAcct() == null ? 0 : buyerBalance.getCashAcct()) + (bizProductionTrans.getTransPrice() == null ? 0 : bizProductionTrans.getTransPrice()));
			if(buyerBalance.getCashAcct() < 0){
				throw new Exception("卖家现金余额不足");
			}
			if(sellerBalance.getChipAcct() < 0 ||  sellerBalance.getCardAcct() < 0 || sellerBalance.getEnergyAcct() < 0 || sellerBalance.getToyAcct() < 0){
				throw new Exception("卖家成品库存不足");
			}
			bizSubjectBalanceService.updateById(sellerBalance);
			bizSubjectBalanceService.updateById(buyerBalance);
			bizProductionTransService.removeById(id);
		}catch (Exception e){
			e.printStackTrace();
			//回滚事务
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return  Result.error("撤销失败：" + e.getMessage());
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
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.bizProductionTransService.removeByIds(Arrays.asList(ids.split(",")));
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
	public Result<BizProductionTrans> queryById(@RequestParam(name="id",required=true) String id) {
		BizProductionTrans bizProductionTrans = bizProductionTransService.getById(id);
		if(bizProductionTrans==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(bizProductionTrans);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param bizProductionTrans
    */
    //@RequiresPermissions("org.jeecg.modules:biz_production_trans:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, BizProductionTrans bizProductionTrans) {
        return super.exportXls(request, bizProductionTrans, BizProductionTrans.class, "成品交易");
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
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, BizProductionTrans.class);
    }

}
