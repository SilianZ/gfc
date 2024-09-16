package org.jeecg.modules.biz.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.biz.entity.BizFixedAssetsTrans;
import org.jeecg.modules.biz.entity.BizResourceRights;
import org.jeecg.modules.biz.entity.BizSubjectBalance;
import org.jeecg.modules.biz.entity.BizTeamResource;
import org.jeecg.modules.biz.service.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;

 /**
 * @Description: 固定资产交易
 * @Author: jeecg-boot
 * @Date:   2023-09-24
 * @Version: V1.0
 */
@Api(tags="固定资产交易")
@RestController
@RequestMapping("/biz/bizFixedAssetsTrans")
@Slf4j
public class BizFixedAssetsTransController extends JeecgController<BizFixedAssetsTrans, IBizFixedAssetsTransService> {
	@Autowired
	private IBizFixedAssetsTransService bizFixedAssetsTransService;
	@Autowired
	private IBizTeamResourceService bizTeamResourceService;
	@Autowired
	private IBizResourceRightsService bizResourceRightsService;
	@Autowired
	private IBizFiscalYearService bizFiscalYearService;
	@Autowired
	private IBizSubjectBalanceService bizSubjectBalanceService;
	 @Autowired
	 private IBizBankConfigService bizBankConfigService;
	 @Autowired
	 private ISysDepartService sysDepartService;
	 @Autowired
	 private ISysUserService sysUserService;
	
	/**
	 * 分页列表查询
	 *
	 * @param bizFixedAssetsTrans
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "固定资产交易-分页列表查询")
	@ApiOperation(value="固定资产交易-分页列表查询", notes="固定资产交易-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<BizFixedAssetsTrans>> queryPageList(BizFixedAssetsTrans bizFixedAssetsTrans,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<BizFixedAssetsTrans> queryWrapper = QueryGenerator.initQueryWrapper(bizFixedAssetsTrans, req.getParameterMap());
		Page<BizFixedAssetsTrans> page = new Page<BizFixedAssetsTrans>(pageNo, pageSize);
		IPage<BizFixedAssetsTrans> pageList = bizFixedAssetsTransService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param bizFixedAssetsTrans
	 * @return
	 */
	@AutoLog(value = "固定资产交易-添加")
	@ApiOperation(value="固定资产交易-添加", notes="固定资产交易-添加")
	//@RequiresPermissions("org.jeecg.modules:biz_fixed_assets_trans:add")
	@PostMapping(value = "/add")
	@Transactional(rollbackFor = Exception.class)
	public Result<String> add(@RequestBody BizFixedAssetsTrans bizFixedAssetsTrans) {
		try {
			//判断买方可用余额
			LambdaQueryWrapper<BizSubjectBalance> query1 = new LambdaQueryWrapper<>();
			query1.eq(BizSubjectBalance::getUserId, bizFixedAssetsTrans.getBuyerId());
			BizSubjectBalance balance1 = bizSubjectBalanceService.getOne(query1);
			if (balance1 == null || balance1.getCashAcct() < bizFixedAssetsTrans.getTransPrice()) {
				throw new Exception("买家可用余额不足，交易失败！");
			}
			//扣减买家余额
			balance1.setCashAcct(balance1.getCashAcct() - bizFixedAssetsTrans.getTransPrice());
			//如果卖方不为主席团、国家政府和银行，扣除交易所得税
			SysUser seller = sysUserService.getUserByName(bizFixedAssetsTrans.getSellerId());
			Double taxAmount = 0.00;
			if(!"GOV".equals(seller.getPost()) && !"ZXT".equals(seller.getPost()) && !"BNK".equals(seller.getPost())){
				SysDepart depart = sysDepartService.queryDepartsByUsername(bizFixedAssetsTrans.getSellerId()).get(0);
				taxAmount = bizBankConfigService.collectTaxes(bizFixedAssetsTrans.getTransPrice(), isTransnational(bizFixedAssetsTrans.getSellerId(), bizFixedAssetsTrans.getBuyerId()), depart.getId());
			}
			//增加卖方余额
			LambdaQueryWrapper<BizSubjectBalance> query2 = new LambdaQueryWrapper<>();
			query2.eq(BizSubjectBalance::getUserId, bizFixedAssetsTrans.getSellerId());
			BizSubjectBalance balance2 = bizSubjectBalanceService.getOne(query2);
			balance2.setCashAcct(balance2.getCashAcct() + bizFixedAssetsTrans.getTransPrice() - taxAmount);
			//设置活动财年
			bizFixedAssetsTrans.setYearCode(bizFiscalYearService.getActiveYearCode());
			bizFixedAssetsTransService.save(bizFixedAssetsTrans);
			if ("SYOU".equals(bizFixedAssetsTrans.getRightType())) {//所有权变更
				BizTeamResource teamResource = bizTeamResourceService.getById(bizFixedAssetsTrans.getTransObject());
				//如果是伽马工厂（国家->团队），需扣除建造材料
				if(seller.getPost().equals("GOV") && teamResource.getResourceType().equals("JM")){
					balance1.setSteelAcct(balance1.getSteelAcct()==null?0:(balance1.getSteelAcct() - 400));
					balance1.setSilicaAcct(balance1.getSilicaAcct()==null?0:balance1.getSilicaAcct() - 400);
					balance1.setCrudeAcct(balance1.getCrudeAcct()==null?0:balance1.getCrudeAcct() - 400);
					balance1.setPlasticsAcct(balance1.getPlasticsAcct()==null?0:balance1.getPlasticsAcct() - 400);
				}
				teamResource.setPrice(bizFixedAssetsTrans.getTransPrice());
				teamResource.setUserId(bizFixedAssetsTrans.getBuyerId());
				bizTeamResourceService.updateById(teamResource);
				//变更后一并修改后续使用权
				List<BizResourceRights> rights = bizResourceRightsService.selectByMainId(bizFixedAssetsTrans.getTransObject());
				List<BizResourceRights> updateList = new ArrayList<>();
				for (BizResourceRights right : rights) {
					if (right.getYearCode() >= bizFixedAssetsTrans.getYearCode()) {
						right.setUserId(bizFixedAssetsTrans.getBuyerId());
						updateList.add(right);
					}
				}
				bizResourceRightsService.updateBatchById(updateList);
			} else {//使用权变更
				if (bizFixedAssetsTrans.getYearLimit() == null || bizFixedAssetsTrans.getYearLimit() == 0) {
					throw new Exception("使用权交易，需填写年限！");
				} else {
					List<BizResourceRights> rights = bizResourceRightsService.selectByMainId(bizFixedAssetsTrans.getTransObject());
					List<BizResourceRights> updateList = new ArrayList<>();
					for (BizResourceRights right : rights) {
						if (right.getYearCode() >= bizFixedAssetsTrans.getYearCode()
								&& right.getYearCode() < bizFixedAssetsTrans.getYearCode() + bizFixedAssetsTrans.getYearLimit()) {
							right.setUserId(bizFixedAssetsTrans.getBuyerId());
							updateList.add(right);
						}
					}
					bizResourceRightsService.updateBatchById(updateList);
				}
			}
			if(balance1.getCashAcct() < 0){
				throw new Exception("买家可用余额不足！");
			}
			if(balance1.getSteelAcct() < 0 || balance1.getSilicaAcct() < 0 || balance1.getCrudeAcct() < 0 || balance1.getPlasticsAcct() < 0){
				throw new Exception("买家材料库存不足！");
			}
			bizSubjectBalanceService.updateById(balance1);
			bizSubjectBalanceService.updateById(balance2);
		}catch (Exception e){
			e.printStackTrace();
			//回滚事务
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return Result.error("交易失败:" + e.getMessage());
		}
		return Result.OK("交易成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param bizFixedAssetsTrans
	 * @return
	 */
	@AutoLog(value = "固定资产交易-编辑")
	@ApiOperation(value="固定资产交易-编辑", notes="固定资产交易-编辑")
	//@RequiresPermissions("org.jeecg.modules:biz_fixed_assets_trans:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody BizFixedAssetsTrans bizFixedAssetsTrans) {
		bizFixedAssetsTransService.updateById(bizFixedAssetsTrans);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "固定资产交易-通过id删除")
	@ApiOperation(value="固定资产交易-通过id删除", notes="固定资产交易-通过id删除")
	//@RequiresPermissions("org.jeecg.modules:biz_fixed_assets_trans:delete")
	@DeleteMapping(value = "/delete")
	@Transactional(rollbackFor = Exception.class)
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		try {
			BizFixedAssetsTrans trans = bizFixedAssetsTransService.getById(id);
			//恢复买方可用余额
			LambdaQueryWrapper<BizSubjectBalance> query1 = new LambdaQueryWrapper<>();
			query1.eq(BizSubjectBalance::getUserId, trans.getBuyerId());
			BizSubjectBalance balance1 = bizSubjectBalanceService.getOne(query1);
			balance1.setCashAcct(balance1.getCashAcct() + trans.getTransPrice());
			//如果卖方不为主席团、国家政府和银行，冲销交易所得税
			SysUser seller = sysUserService.getUserByName(trans.getSellerId());
			Double taxAmount = 0.00;
			if(!"GOV".equals(seller.getPost()) && !"ZXT".equals(seller.getPost()) && !"BNK".equals(seller.getPost())){
				SysDepart depart = sysDepartService.queryDepartsByUsername(trans.getSellerId()).get(0);
				taxAmount = bizBankConfigService.offTaxes(trans.getTransPrice(), isTransnational(trans.getSellerId(), trans.getBuyerId()), depart.getId());
			}
			//减少卖方余额
			LambdaQueryWrapper<BizSubjectBalance> query2 = new LambdaQueryWrapper<>();
			query2.eq(BizSubjectBalance::getUserId, trans.getSellerId());
			BizSubjectBalance balance2 = bizSubjectBalanceService.getOne(query2);
			if (balance2.getCashAcct() < trans.getTransPrice()) {
				throw new Exception("卖方余额不足，无法退回并删除交易！");
			}
			balance2.setCashAcct(balance2.getCashAcct() - trans.getTransPrice() + taxAmount);

			if ("SYOU".equals(trans.getRightType())) {//所有权变更
				BizTeamResource teamResource = bizTeamResourceService.getById(trans.getTransObject());
				//如果是伽马工厂（国家->团队），需加回已扣除的建造材料
				if(seller.getPost().equals("GOV") && teamResource.getResourceType().equals("JM")){
					balance1.setSteelAcct(balance1.getSteelAcct()==null?0:(balance1.getSteelAcct() + 400));
					balance1.setSilicaAcct(balance1.getSilicaAcct()==null?0:balance1.getSilicaAcct() + 400);
					balance1.setCrudeAcct(balance1.getCrudeAcct()==null?0:balance1.getCrudeAcct() + 400);
					balance1.setPlasticsAcct(balance1.getPlasticsAcct()==null?0:balance1.getPlasticsAcct() + 400);
				}
				teamResource.setUserId(trans.getSellerId());
				bizTeamResourceService.updateById(teamResource);
				//变更后一并修改后续使用权
				List<BizResourceRights> rights = bizResourceRightsService.selectByMainId(trans.getTransObject());
				List<BizResourceRights> updateList = new ArrayList<>();
				for (BizResourceRights right : rights) {
					if (right.getYearCode() >= trans.getYearCode()) {
						right.setUserId(trans.getSellerId());
						updateList.add(right);
					}
				}
				bizResourceRightsService.updateBatchById(updateList);
			} else {//使用权变更
				if (trans.getYearLimit() == null || trans.getYearLimit() == 0) {
					throw new Exception("无效的交易数据！");
				} else {
					List<BizResourceRights> rights = bizResourceRightsService.selectByMainId(trans.getTransObject());
					List<BizResourceRights> updateList = new ArrayList<>();
					for (BizResourceRights right : rights) {
						if (right.getYearCode() >= trans.getYearCode()
								&& right.getYearCode() < trans.getYearCode() + trans.getYearLimit()) {
							right.setUserId(trans.getSellerId());
							updateList.add(right);
						}
					}
					bizResourceRightsService.updateBatchById(updateList);
				}
			}
			bizSubjectBalanceService.updateById(balance1);
			bizSubjectBalanceService.updateById(balance2);
			bizFixedAssetsTransService.removeById(id);
		}catch (Exception e){
			e.printStackTrace();
			//回滚事务
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return Result.error("交易撤销失败:" + e.getMessage());
		}
		return Result.OK("交易撤销成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "固定资产交易-批量删除")
	@ApiOperation(value="固定资产交易-批量删除", notes="固定资产交易-批量删除")
	//@RequiresPermissions("org.jeecg.modules:biz_fixed_assets_trans:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.bizFixedAssetsTransService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "固定资产交易-通过id查询")
	@ApiOperation(value="固定资产交易-通过id查询", notes="固定资产交易-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<BizFixedAssetsTrans> queryById(@RequestParam(name="id",required=true) String id) {
		BizFixedAssetsTrans bizFixedAssetsTrans = bizFixedAssetsTransService.getById(id);
		if(bizFixedAssetsTrans==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(bizFixedAssetsTrans);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param bizFixedAssetsTrans
    */
    //@RequiresPermissions("org.jeecg.modules:biz_fixed_assets_trans:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, BizFixedAssetsTrans bizFixedAssetsTrans) {
        return super.exportXls(request, bizFixedAssetsTrans, BizFixedAssetsTrans.class, "固定资产交易");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    //@RequiresPermissions("biz_fixed_assets_trans:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, BizFixedAssetsTrans.class);
    }

    public boolean isTransnational(String sellerId, String buyerId){
		SysDepart sellerDepart = sysDepartService.queryDepartsByUsername(sellerId).get(0);
		SysDepart buyerDepart = sysDepartService.queryDepartsByUsername(buyerId).get(0);
		if(sellerDepart.getId().equals(buyerDepart.getId())){
			return false;
		}else{
			return true;
		}
	}
}
