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
	public Result<IPage<BizFixedAssetsTrans>> queryPageList(BizFixedAssetsTrans Silian_bizFixedAssetsTrans,
								   @RequestParam(name="pageNo", defaultValue="1") Integer Silian_pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer Silian_pageSize,
								   HttpServletRequest Silian_req) {
		QueryWrapper<BizFixedAssetsTrans> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_bizFixedAssetsTrans, Silian_req.getParameterMap());
		Page<BizFixedAssetsTrans> Silian_page = new Page<BizFixedAssetsTrans>(Silian_pageNo, Silian_pageSize);
		IPage<BizFixedAssetsTrans> Silian_pageList = bizFixedAssetsTransService.page(Silian_page, Silian_queryWrapper);
		return Result.OK(Silian_pageList);
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
	public Result<String> add(@RequestBody BizFixedAssetsTrans Silian_bizFixedAssetsTrans) {
		try {
			//判断买方可用余额
			LambdaQueryWrapper<BizSubjectBalance> Silian_query1 = new LambdaQueryWrapper<>();
			Silian_query1.eq(BizSubjectBalance::getUserId, Silian_bizFixedAssetsTrans.getBuyerId());
			BizSubjectBalance Silian_balance1 = bizSubjectBalanceService.getOne(Silian_query1);
			if (Silian_balance1 == null || Silian_balance1.getCashAcct() < Silian_bizFixedAssetsTrans.getTransPrice()) {
				throw new Exception("买家可用余额不足，交易失败！");
			}
			//扣减买家余额
			Silian_balance1.setCashAcct(Silian_balance1.getCashAcct() - Silian_bizFixedAssetsTrans.getTransPrice());
			//如果卖方不为主席团、国家政府和银行，扣除交易所得税
			SysUser Silian_seller = sysUserService.getUserByName(Silian_bizFixedAssetsTrans.getSellerId());
			Double Silian_taxAmount = 0.00;
			if(!"GOV".equals(Silian_seller.getPost()) && !"ZXT".equals(Silian_seller.getPost()) && !"BNK".equals(Silian_seller.getPost())){
				SysDepart Silian_depart = sysDepartService.queryDepartsByUsername(Silian_bizFixedAssetsTrans.getSellerId()).get(0);
				Silian_taxAmount = bizBankConfigService.collectTaxes(Silian_bizFixedAssetsTrans.getTransPrice(), isTransnational(Silian_bizFixedAssetsTrans.getSellerId(), Silian_bizFixedAssetsTrans.getBuyerId()), Silian_depart.getId());
			}
			//增加卖方余额
			LambdaQueryWrapper<BizSubjectBalance> Silian_query2 = new LambdaQueryWrapper<>();
			Silian_query2.eq(BizSubjectBalance::getUserId, Silian_bizFixedAssetsTrans.getSellerId());
			BizSubjectBalance Silian_balance2 = bizSubjectBalanceService.getOne(Silian_query2);
			Silian_balance2.setCashAcct(Silian_balance2.getCashAcct() + Silian_bizFixedAssetsTrans.getTransPrice() - Silian_taxAmount);
			//设置活动财年
			Silian_bizFixedAssetsTrans.setYearCode(bizFiscalYearService.getActiveYearCode());
			bizFixedAssetsTransService.save(Silian_bizFixedAssetsTrans);
			if ("SYOU".equals(Silian_bizFixedAssetsTrans.getRightType())) {//所有权变更
				BizTeamResource teamResource = bizTeamResourceService.getById(Silian_bizFixedAssetsTrans.getTransObject());
				//如果是伽马工厂（国家->团队），需扣除建造材料
				if(Silian_seller.getPost().equals("GOV") && teamResource.getResourceType().equals("JM")){
					Silian_balance1.setSteelAcct(Silian_balance1.getSteelAcct()==null?0:(Silian_balance1.getSteelAcct() - 400));
					Silian_balance1.setSilicaAcct(Silian_balance1.getSilicaAcct()==null?0:Silian_balance1.getSilicaAcct() - 400);
					Silian_balance1.setCrudeAcct(Silian_balance1.getCrudeAcct()==null?0:Silian_balance1.getCrudeAcct() - 400);
					Silian_balance1.setPlasticsAcct(Silian_balance1.getPlasticsAcct()==null?0:Silian_balance1.getPlasticsAcct() - 400);
				}
				teamResource.setPrice(Silian_bizFixedAssetsTrans.getTransPrice());
				teamResource.setUserId(Silian_bizFixedAssetsTrans.getBuyerId());
				bizTeamResourceService.updateById(teamResource);
				//变更后一并修改后续使用权
				List<BizResourceRights> Silian_rights = bizResourceRightsService.selectByMainId(Silian_bizFixedAssetsTrans.getTransObject());
				List<BizResourceRights> Silian_updateList = new ArrayList<>();
				for (BizResourceRights Silian_right : Silian_rights) {
					if (Silian_right.getYearCode() >= Silian_bizFixedAssetsTrans.getYearCode()) {
						Silian_right.setUserId(Silian_bizFixedAssetsTrans.getBuyerId());
						Silian_updateList.add(Silian_right);
					}
				}
				bizResourceRightsService.updateBatchById(Silian_updateList);
			} else {//使用权变更
				if (Silian_bizFixedAssetsTrans.getYearLimit() == null || Silian_bizFixedAssetsTrans.getYearLimit() == 0) {
					throw new Exception("使用权交易，需填写年限！");
				} else {
					List<BizResourceRights> Silian_rights = bizResourceRightsService.selectByMainId(Silian_bizFixedAssetsTrans.getTransObject());
					List<BizResourceRights> Silian_updateList = new ArrayList<>();
					for (BizResourceRights Silian_right : Silian_rights) {
						if (Silian_right.getYearCode() >= Silian_bizFixedAssetsTrans.getYearCode()
								&& Silian_right.getYearCode() < Silian_bizFixedAssetsTrans.getYearCode() + Silian_bizFixedAssetsTrans.getYearLimit()) {
							Silian_right.setUserId(Silian_bizFixedAssetsTrans.getBuyerId());
							Silian_updateList.add(Silian_right);
						}
					}
					bizResourceRightsService.updateBatchById(Silian_updateList);
				}
			}
			if(Silian_balance1.getCashAcct() < 0){
				throw new Exception("买家可用余额不足！");
			}
			if(Silian_balance1.getSteelAcct() < 0 || Silian_balance1.getSilicaAcct() < 0 || Silian_balance1.getCrudeAcct() < 0 || Silian_balance1.getPlasticsAcct() < 0){
				throw new Exception("买家材料库存不足！");
			}
			bizSubjectBalanceService.updateById(Silian_balance1);
			bizSubjectBalanceService.updateById(Silian_balance2);
		}catch (Exception Silian_e){
			Silian_e.printStackTrace();
			//回滚事务
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return Result.error("交易失败:" + Silian_e.getMessage());
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
	public Result<String> edit(@RequestBody BizFixedAssetsTrans Silian_bizFixedAssetsTrans) {
		bizFixedAssetsTransService.updateById(Silian_bizFixedAssetsTrans);
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
	public Result<String> delete(@RequestParam(name="id",required=true) String Silian_id) {
		try {
			BizFixedAssetsTrans Silian_trans = bizFixedAssetsTransService.getById(Silian_id);
			//恢复买方可用余额
			LambdaQueryWrapper<BizSubjectBalance> Silian_query1 = new LambdaQueryWrapper<>();
			Silian_query1.eq(BizSubjectBalance::getUserId, Silian_trans.getBuyerId());
			BizSubjectBalance Silian_balance1 = bizSubjectBalanceService.getOne(Silian_query1);
			Silian_balance1.setCashAcct(Silian_balance1.getCashAcct() + Silian_trans.getTransPrice());
			//如果卖方不为主席团、国家政府和银行，冲销交易所得税
			SysUser Silian_seller = sysUserService.getUserByName(Silian_trans.getSellerId());
			Double Silian_taxAmount = 0.00;
			if(!"GOV".equals(Silian_seller.getPost()) && !"ZXT".equals(Silian_seller.getPost()) && !"BNK".equals(Silian_seller.getPost())){
				SysDepart Silian_depart = sysDepartService.queryDepartsByUsername(Silian_trans.getSellerId()).get(0);
				Silian_taxAmount = bizBankConfigService.offTaxes(Silian_trans.getTransPrice(), isTransnational(Silian_trans.getSellerId(), Silian_trans.getBuyerId()), Silian_depart.getId());
			}
			//减少卖方余额
			LambdaQueryWrapper<BizSubjectBalance> Silian_query2 = new LambdaQueryWrapper<>();
			Silian_query2.eq(BizSubjectBalance::getUserId, Silian_trans.getSellerId());
			BizSubjectBalance Silian_balance2 = bizSubjectBalanceService.getOne(Silian_query2);
			if (Silian_balance2.getCashAcct() < Silian_trans.getTransPrice()) {
				throw new Exception("卖方余额不足，无法退回并删除交易！");
			}
			Silian_balance2.setCashAcct(Silian_balance2.getCashAcct() - Silian_trans.getTransPrice() + Silian_taxAmount);

			if ("SYOU".equals(Silian_trans.getRightType())) {//所有权变更
				BizTeamResource teamResource = bizTeamResourceService.getById(Silian_trans.getTransObject());
				//如果是伽马工厂（国家->团队），需加回已扣除的建造材料
				if(Silian_seller.getPost().equals("GOV") && teamResource.getResourceType().equals("JM")){
					Silian_balance1.setSteelAcct(Silian_balance1.getSteelAcct()==null?0:(Silian_balance1.getSteelAcct() + 400));
					Silian_balance1.setSilicaAcct(Silian_balance1.getSilicaAcct()==null?0:Silian_balance1.getSilicaAcct() + 400);
					Silian_balance1.setCrudeAcct(Silian_balance1.getCrudeAcct()==null?0:Silian_balance1.getCrudeAcct() + 400);
					Silian_balance1.setPlasticsAcct(Silian_balance1.getPlasticsAcct()==null?0:Silian_balance1.getPlasticsAcct() + 400);
				}
				teamResource.setUserId(Silian_trans.getSellerId());
				bizTeamResourceService.updateById(teamResource);
				//变更后一并修改后续使用权
				List<BizResourceRights> Silian_rights = bizResourceRightsService.selectByMainId(Silian_trans.getTransObject());
				List<BizResourceRights> Silian_updateList = new ArrayList<>();
				for (BizResourceRights Silian_right : Silian_rights) {
					if (Silian_right.getYearCode() >= Silian_trans.getYearCode()) {
						Silian_right.setUserId(Silian_trans.getSellerId());
						Silian_updateList.add(Silian_right);
					}
				}
				bizResourceRightsService.updateBatchById(Silian_updateList);
			} else {//使用权变更
				if (Silian_trans.getYearLimit() == null || Silian_trans.getYearLimit() == 0) {
					throw new Exception("无效的交易数据！");
				} else {
					List<BizResourceRights> Silian_rights = bizResourceRightsService.selectByMainId(Silian_trans.getTransObject());
					List<BizResourceRights> Silian_updateList = new ArrayList<>();
					for (BizResourceRights Silian_right : Silian_rights) {
						if (Silian_right.getYearCode() >= Silian_trans.getYearCode()
								&& Silian_right.getYearCode() < Silian_trans.getYearCode() + Silian_trans.getYearLimit()) {
							Silian_right.setUserId(Silian_trans.getSellerId());
							Silian_updateList.add(Silian_right);
						}
					}
					bizResourceRightsService.updateBatchById(Silian_updateList);
				}
			}
			bizSubjectBalanceService.updateById(Silian_balance1);
			bizSubjectBalanceService.updateById(Silian_balance2);
			bizFixedAssetsTransService.removeById(Silian_id);
		}catch (Exception Silian_e){
			Silian_e.printStackTrace();
			//回滚事务
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return Result.error("交易撤销失败:" + Silian_e.getMessage());
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
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String Silian_ids) {
		this.bizFixedAssetsTransService.removeByIds(Arrays.asList(Silian_ids.split(",")));
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
	public Result<BizFixedAssetsTrans> queryById(@RequestParam(name="id",required=true) String Silian_id) {
		BizFixedAssetsTrans Silian_bizFixedAssetsTrans = bizFixedAssetsTransService.getById(Silian_id);
		if(Silian_bizFixedAssetsTrans==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(Silian_bizFixedAssetsTrans);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param bizFixedAssetsTrans
    */
    //@RequiresPermissions("org.jeecg.modules:biz_fixed_assets_trans:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest Silian_request, BizFixedAssetsTrans Silian_bizFixedAssetsTrans) {
        return super.exportXls(Silian_request, Silian_bizFixedAssetsTrans, BizFixedAssetsTrans.class, "固定资产交易");
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
    public Result<?> importExcel(HttpServletRequest Silian_request, HttpServletResponse Silian_response) {
        return super.importExcel(Silian_request, Silian_response, BizFixedAssetsTrans.class);
    }

    public boolean isTransnational(String Silian_sellerId, String Silian_buyerId){
		SysDepart Silian_sellerDepart = sysDepartService.queryDepartsByUsername(Silian_sellerId).get(0);
		SysDepart Silian_buyerDepart = sysDepartService.queryDepartsByUsername(Silian_buyerId).get(0);
		if(Silian_sellerDepart.getId().equals(Silian_buyerDepart.getId())){
			return false;
		}else{
			return true;
		}
	}
}
