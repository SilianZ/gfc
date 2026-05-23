package org.jeecg.modules.biz.controller;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.biz.entity.BizMineOpen;
import org.jeecg.modules.biz.entity.BizMinePlan;
import org.jeecg.modules.biz.entity.BizSubjectBalance;
import org.jeecg.modules.biz.service.IBizFiscalYearService;
import org.jeecg.modules.biz.service.IBizMineOpenService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.biz.service.IBizMinePlanService;
import org.jeecg.modules.biz.service.IBizSubjectBalanceService;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.TransactionAnnotationParser;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;

 /**
 * @Description: 资源开采
 * @Author: jeecg-boot
 * @Date:   2023-09-25
 * @Version: V1.0
 */
@Api(tags="资源开采")
@RestController
@RequestMapping("/biz/bizMineOpen")
@Slf4j
public class BizMineOpenController extends JeecgController<BizMineOpen, IBizMineOpenService> {
	@Autowired
	private IBizMineOpenService bizMineOpenService;
	 @Autowired
	 private IBizFiscalYearService bizFiscalYearService;
	 @Autowired
	 private IBizSubjectBalanceService bizSubjectBalanceService;
	 @Autowired
	 private IBizMinePlanService bizMinePlanService;
	 @Autowired
	 private ISysDepartService sysDepartService;
	 @Autowired
	 private ISysUserService sysUserService;

	/**
	 * 分页列表查询
	 *
	 * @param bizMineOpen
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "资源开采-分页列表查询")
	@ApiOperation(value="资源开采-分页列表查询", notes="资源开采-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<BizMineOpen>> queryPageList(BizMineOpen Silian_bizMineOpen,
								   @RequestParam(name="pageNo", defaultValue="1") Integer Silian_pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer Silian_pageSize,
								   HttpServletRequest Silian_req) {
		QueryWrapper<BizMineOpen> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_bizMineOpen, Silian_req.getParameterMap());
		Page<BizMineOpen> Silian_page = new Page<BizMineOpen>(Silian_pageNo, Silian_pageSize);
		IPage<BizMineOpen> Silian_pageList = bizMineOpenService.page(Silian_page, Silian_queryWrapper);
		return Result.OK(Silian_pageList);
	}

	/**
	 *   添加
	 *
	 * @param bizMineOpen
	 * @return
	 */
	@AutoLog(value = "资源开采-添加")
	@ApiOperation(value="资源开采-添加", notes="资源开采-添加")
	//@RequiresPermissions("org.jeecg.modules:biz_mine_open:add")
	@PostMapping(value = "/add")
	@Transactional(rollbackFor = Exception.class)
	public Result<String> add(@RequestBody BizMineOpen Silian_bizMineOpen) {
		String Silian_message = "";
		try {
			//设置所属财年
			Silian_bizMineOpen.setYearCode(bizFiscalYearService.getActiveYearCode());
			//每个财年个工厂仅能一次开采
			LambdaQueryWrapper<BizMineOpen> Silian_query = new LambdaQueryWrapper<>();
			Silian_query.eq(BizMineOpen::getYearCode, Silian_bizMineOpen.getYearCode());
			Silian_query.eq(BizMineOpen::getFactoryId, Silian_bizMineOpen.getFactoryId());
			List<BizMineOpen> Silian_mineOpenList = bizMineOpenService.list(Silian_query);
			if (Silian_mineOpenList != null && Silian_mineOpenList.size() > 0) {
				throw new Exception("本财年该工厂已经开采原材料，请勿重复开采！");
			}
			BizMinePlan Silian_plan = bizMinePlanService.getByPlanCode(Silian_bizMineOpen.getPlanCode());
			/**20230930去除该限制
			SysDepart depart = sysDepartService.queryDepartsByUsername(sysUserService.getUserByName(bizMineOpen.getUserId()).getUsername()).get(0);
			switch (depart.getAddress()) {
				case "GT":
					plan.setGtProduction(0.00);
					message = "其中钢铁资源受国家限制无法开采";
					break;
				case "GS":
					plan.setGsProduction(0.00);
					message = "其中硅石资源受国家限制无法开采";
					break;
				case "SY":
					plan.setSyProduction(0.00);
					message = "其中石油资源受国家限制无法开采";
					break;
			}**/
			//开采成功，增加团队材料库存
			BizSubjectBalance Silian_balance = bizSubjectBalanceService.getByUserId(Silian_bizMineOpen.getUserId());
			Silian_balance.setSteelAcct(Silian_balance.getSteelAcct() + Silian_plan.getGtProduction());
			Silian_balance.setSilicaAcct(Silian_balance.getSilicaAcct() + Silian_plan.getGsProduction());
			Silian_balance.setCrudeAcct(Silian_balance.getCrudeAcct() + Silian_plan.getSyProduction());
			Silian_balance.setPlasticsAcct(Silian_balance.getPlasticsAcct() + Silian_plan.getSlProduction());
			//减少团队现金余额
			Silian_balance.setCashAcct(Silian_balance.getCashAcct() - 10);
			if(Silian_balance.getCashAcct() < 0){
				throw new Exception("现金余额不足，无法开采资源");
			}
			bizMineOpenService.save(Silian_bizMineOpen);
			bizSubjectBalanceService.updateById(Silian_balance);
		}catch (Exception Silian_e){
			Silian_e.printStackTrace();
			//回滚事务
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return Result.error("资源开采失败:" + Silian_e.getMessage());
		}
		return Result.OK("资源开采成功，"+Silian_message);
	}

	/**
	 *  编辑
	 *
	 * @param bizMineOpen
	 * @return
	 */
	@AutoLog(value = "资源开采-编辑")
	@ApiOperation(value="资源开采-编辑", notes="资源开采-编辑")
	//@RequiresPermissions("org.jeecg.modules:biz_mine_open:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody BizMineOpen Silian_bizMineOpen) {
		bizMineOpenService.updateById(Silian_bizMineOpen);
		return Result.OK("编辑成功!");
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "资源开采-通过id删除")
	@ApiOperation(value="资源开采-通过id删除", notes="资源开采-通过id删除")
	//@RequiresPermissions("org.jeecg.modules:biz_mine_open:delete")
	@Transactional(rollbackFor = Exception.class)
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String Silian_id) {
		try {
			BizMineOpen Silian_bizMineOpen = bizMineOpenService.getById(Silian_id);
			//资源开采计划及限制制
			BizMinePlan Silian_plan = bizMinePlanService.getByPlanCode(Silian_bizMineOpen.getPlanCode());
			SysDepart Silian_depart = sysDepartService.queryDepartsByUsername(sysUserService.getUserByName(Silian_bizMineOpen.getUserId()).getUsername()).get(0);
			switch (Silian_depart.getAddress()) {
				case "GT":
					Silian_plan.setGtProduction(0.00);
					break;
				case "GS":
					Silian_plan.setGsProduction(0.00);
					break;
				case "SY":
					Silian_plan.setSyProduction(0.00);
					break;
			}
			//撤销开采，减少团队材料库存
			BizSubjectBalance Silian_balance = bizSubjectBalanceService.getByUserId(Silian_bizMineOpen.getUserId());
			Silian_balance.setSteelAcct(Silian_balance.getSteelAcct() - Silian_plan.getGtProduction());
			Silian_balance.setSilicaAcct(Silian_balance.getSilicaAcct() - Silian_plan.getGsProduction());
			Silian_balance.setCrudeAcct(Silian_balance.getCrudeAcct() - Silian_plan.getSyProduction());
			Silian_balance.setPlasticsAcct(Silian_balance.getPlasticsAcct() - Silian_plan.getSlProduction());
			//增加团队现金余额
			Silian_balance.setCashAcct(Silian_balance.getCashAcct() + 10);
			if (Silian_balance.getSteelAcct() < 0 || Silian_balance.getSilicaAcct() < 0 || Silian_balance.getCrudeAcct() < 0 || Silian_balance.getPlasticsAcct() < 0) {
				throw new Exception("删除失败，团队原材料库存不足！");
			}
			if(Silian_balance.getCashAcct() < 0){
				throw new  Exception("删除失败，团队现金余额不足！");
			}
			bizSubjectBalanceService.updateById(Silian_balance);
			bizMineOpenService.removeById(Silian_id);
		}catch (Exception Silian_e){
			Silian_e.printStackTrace();
			//回滚事务
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return Result.error("撤销失败："+Silian_e.getMessage());
		}
		return Result.OK("撤销开采计划成功!");
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "资源开采-批量删除")
	@ApiOperation(value="资源开采-批量删除", notes="资源开采-批量删除")
	//@RequiresPermissions("org.jeecg.modules:biz_mine_open:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String Silian_ids) {
		this.bizMineOpenService.removeByIds(Arrays.asList(Silian_ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "资源开采-通过id查询")
	@ApiOperation(value="资源开采-通过id查询", notes="资源开采-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<BizMineOpen> queryById(@RequestParam(name="id",required=true) String Silian_id) {
		BizMineOpen Silian_bizMineOpen = bizMineOpenService.getById(Silian_id);
		if(Silian_bizMineOpen==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(Silian_bizMineOpen);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param bizMineOpen
    */
    //@RequiresPermissions("org.jeecg.modules:biz_mine_open:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest Silian_request, BizMineOpen Silian_bizMineOpen) {
        return super.exportXls(Silian_request, Silian_bizMineOpen, BizMineOpen.class, "资源开采");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    //@RequiresPermissions("biz_mine_open:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest Silian_request, HttpServletResponse Silian_response) {
        return super.importExcel(Silian_request, Silian_response, BizMineOpen.class);
    }

}
