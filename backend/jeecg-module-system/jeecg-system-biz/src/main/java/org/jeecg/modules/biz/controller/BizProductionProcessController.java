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
import org.jeecg.modules.biz.entity.BizProductionProcess;
import org.jeecg.modules.biz.entity.BizSubjectBalance;
import org.jeecg.modules.biz.entity.BizTeamResource;
import org.jeecg.modules.biz.service.IBizFiscalYearService;
import org.jeecg.modules.biz.service.IBizProductionProcessService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.biz.service.IBizSubjectBalanceService;
import org.jeecg.modules.biz.service.IBizTeamResourceService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;

 /**
 * @Description: 成品加工
 * @Author: jeecg-boot
 * @Date:   2023-09-25
 * @Version: V1.0
 */
@Api(tags="成品加工")
@RestController
@RequestMapping("/biz/bizProductionProcess")
@Slf4j
public class BizProductionProcessController extends JeecgController<BizProductionProcess, IBizProductionProcessService> {
	@Autowired
	private IBizProductionProcessService bizProductionProcessService;
	@Autowired
	private IBizSubjectBalanceService bizSubjectBalanceService;
	@Autowired
	private IBizFiscalYearService bizFiscalYearService;
	@Autowired
	private IBizTeamResourceService bizTeamResourceService;

	/**
	 * 分页列表查询
	 *
	 * @param bizProductionProcess
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "成品加工-分页列表查询")
	@ApiOperation(value="成品加工-分页列表查询", notes="成品加工-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<BizProductionProcess>> queryPageList(BizProductionProcess Silian_bizProductionProcess,
								   @RequestParam(name="pageNo", defaultValue="1") Integer Silian_pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer Silian_pageSize,
								   HttpServletRequest Silian_req) {
		QueryWrapper<BizProductionProcess> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_bizProductionProcess, Silian_req.getParameterMap());
		Page<BizProductionProcess> Silian_page = new Page<BizProductionProcess>(Silian_pageNo, Silian_pageSize);
		IPage<BizProductionProcess> Silian_pageList = bizProductionProcessService.page(Silian_page, Silian_queryWrapper);
		return Result.OK(Silian_pageList);
	}

	/**
	 *   添加
	 *
	 * @param bizProductionProcess
	 * @return
	 */
	@AutoLog(value = "成品加工-添加")
	@ApiOperation(value="成品加工-添加", notes="成品加工-添加")
	//@RequiresPermissions("org.jeecg.modules:biz_production_process:add")
	@PostMapping(value = "/add")
	@Transactional(rollbackFor = Exception.class)
	public Result<String> add(@RequestBody BizProductionProcess Silian_bizProductionProcess) {
		try {
			//设置所属财年
			Silian_bizProductionProcess.setYearCode(bizFiscalYearService.getActiveYearCode());
			//判断工厂生产上限
			BizTeamResource Silian_resource = bizTeamResourceService.getById(Silian_bizProductionProcess.getResourceId());
			if("ML".equals(Silian_resource.getResourceType())){
				Double Silian_processNumber = bizProductionProcessService.getProcessNumber(Silian_bizProductionProcess.getResourceId(),bizFiscalYearService.getActiveYearCode());
				Silian_processNumber = Silian_processNumber == null ? 0 : Silian_processNumber;
				if(Silian_processNumber + (Silian_bizProductionProcess.getGtNumber() == null ? 0 : Silian_bizProductionProcess.getGtNumber())
						+ (Silian_bizProductionProcess.getGsNumber() == null ? 0 : Silian_bizProductionProcess.getGsNumber())
						+ (Silian_bizProductionProcess.getSyNumber() == null ? 0 : Silian_bizProductionProcess.getSyNumber())
						+ (Silian_bizProductionProcess.getSlNumber() == null ? 0 : Silian_bizProductionProcess.getSlNumber())  > 260){//马里奥工厂每年上限为260
					throw new Exception("超出该工厂本财年生产上限");
				}
			}
			if((Silian_bizProductionProcess.getChipNumber() == null ? 0 : Silian_bizProductionProcess.getChipNumber()) +
					(Silian_bizProductionProcess.getCardNumber() == null ? 0 : Silian_bizProductionProcess.getCardNumber())+
					(Silian_bizProductionProcess.getEnergyNumber() == null ? 0 : Silian_bizProductionProcess.getEnergyNumber())+
					(Silian_bizProductionProcess.getToyNumber() == null ? 0 : Silian_bizProductionProcess.getToyNumber()) <= 0){
				throw new Exception("错误的生产数量");
			}
			bizProductionProcessService.save(Silian_bizProductionProcess);
			BizSubjectBalance Silian_balance = bizSubjectBalanceService.getByUserId(Silian_bizProductionProcess.getUserId());
			//减少团队原材料库存
			Silian_balance.setSteelAcct((Silian_balance.getSteelAcct() == null ? 0 : Silian_balance.getSteelAcct()) - (Silian_bizProductionProcess.getGtNumber() == null ? 0 : Silian_bizProductionProcess.getGtNumber()));
			Silian_balance.setSilicaAcct((Silian_balance.getSilicaAcct() == null ? 0 : Silian_balance.getSilicaAcct()) - (Silian_bizProductionProcess.getGsNumber() == null ? 0 : Silian_bizProductionProcess.getGsNumber()));
			Silian_balance.setCrudeAcct((Silian_balance.getCrudeAcct() == null ? 0 : Silian_balance.getCrudeAcct()) - (Silian_bizProductionProcess.getSyNumber() == null ? 0 : Silian_bizProductionProcess.getSyNumber()));
			Silian_balance.setPlasticsAcct((Silian_balance.getPlasticsAcct() == null ? 0 : Silian_balance.getPlasticsAcct()) - (Silian_bizProductionProcess.getSlNumber() == null ? 0 : Silian_bizProductionProcess.getSlNumber()));
			//增加团队成品库存
			Silian_balance.setChipAcct((Silian_balance.getChipAcct() == null ? 0 : Silian_balance.getChipAcct()) + (Silian_bizProductionProcess.getChipNumber() == null ? 0 : Silian_bizProductionProcess.getChipNumber()));
			Silian_balance.setCardAcct((Silian_balance.getCardAcct() == null ? 0 : Silian_balance.getCardAcct()) + (Silian_bizProductionProcess.getCardNumber() == null ? 0 : Silian_bizProductionProcess.getCardNumber()));
			Silian_balance.setEnergyAcct((Silian_balance.getEnergyAcct() == null ? 0 : Silian_balance.getEnergyAcct()) + (Silian_bizProductionProcess.getEnergyNumber() == null ? 0 : Silian_bizProductionProcess.getEnergyNumber()));
			Silian_balance.setToyAcct((Silian_balance.getToyAcct() == null ? 0 : Silian_balance.getToyAcct()) + (Silian_bizProductionProcess.getToyNumber() == null ? 0 : Silian_bizProductionProcess.getToyNumber()));
			bizSubjectBalanceService.updateById(Silian_balance);
		}catch (Exception Silian_e){
			Silian_e.printStackTrace();
			//回滚事务
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return Result.error("成品加工失败:" + Silian_e.getMessage());
		}
		return Result.OK("成品加工成功！");
	}

	/**
	 *  编辑
	 *
	 * @param bizProductionProcess
	 * @return
	 */
	@AutoLog(value = "成品加工-编辑")
	@ApiOperation(value="成品加工-编辑", notes="成品加工-编辑")
	//@RequiresPermissions("org.jeecg.modules:biz_production_process:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody BizProductionProcess Silian_bizProductionProcess) {
		bizProductionProcessService.updateById(Silian_bizProductionProcess);
		return Result.OK("编辑成功!");
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "成品加工-通过id删除")
	@ApiOperation(value="成品加工-通过id删除", notes="成品加工-通过id删除")
	//@RequiresPermissions("org.jeecg.modules:biz_production_process:delete")
	@DeleteMapping(value = "/delete")
	@Transactional(rollbackFor = Exception.class)
	public Result<String> delete(@RequestParam(name="id",required=true) String Silian_id) {
		try{
			BizProductionProcess Silian_bizProductionProcess = bizProductionProcessService.getById(Silian_id);
			BizSubjectBalance Silian_balance = bizSubjectBalanceService.getByUserId(Silian_bizProductionProcess.getUserId());
			// 更新团队原材料库存
			Silian_balance.setSteelAcct((Silian_balance.getSteelAcct() != null ? Silian_balance.getSteelAcct() : 0) + (Silian_bizProductionProcess.getGtNumber() != null ? Silian_bizProductionProcess.getGtNumber() : 0));
			Silian_balance.setSilicaAcct((Silian_balance.getSilicaAcct() != null ? Silian_balance.getSilicaAcct() : 0) + (Silian_bizProductionProcess.getGsNumber() != null ? Silian_bizProductionProcess.getGsNumber() : 0));
			Silian_balance.setCrudeAcct((Silian_balance.getCrudeAcct() != null ? Silian_balance.getCrudeAcct() : 0) + (Silian_bizProductionProcess.getSyNumber() != null ? Silian_bizProductionProcess.getSyNumber() : 0));
			Silian_balance.setPlasticsAcct((Silian_balance.getPlasticsAcct() != null ? Silian_balance.getPlasticsAcct() : 0) + (Silian_bizProductionProcess.getSlNumber() != null ? Silian_bizProductionProcess.getSlNumber() : 0));
			// 更新团队成品库存
			Silian_balance.setChipAcct((Silian_balance.getChipAcct() != null ? Silian_balance.getChipAcct() : 0) - (Silian_bizProductionProcess.getChipNumber() != null ? Silian_bizProductionProcess.getChipNumber() : 0));
			Silian_balance.setCardAcct((Silian_balance.getCardAcct() != null ? Silian_balance.getCardAcct() : 0) - (Silian_bizProductionProcess.getCardNumber() != null ? Silian_bizProductionProcess.getCardNumber() : 0));
			Silian_balance.setEnergyAcct((Silian_balance.getEnergyAcct() != null ? Silian_balance.getEnergyAcct() : 0) - (Silian_bizProductionProcess.getEnergyNumber() != null ? Silian_bizProductionProcess.getEnergyNumber() : 0));
			Silian_balance.setToyAcct((Silian_balance.getToyAcct() != null ? Silian_balance.getToyAcct() : 0) - (Silian_bizProductionProcess.getToyNumber() != null ? Silian_bizProductionProcess.getToyNumber() : 0));

			if(Silian_balance.getChipAcct() < 0 || Silian_balance.getCardAcct() < 0 || Silian_balance.getEnergyAcct() < 0 || Silian_balance.getToyAcct() < 0){
				throw new Exception("该用户成品库存不足，无法撤销");
			}
			bizSubjectBalanceService.updateById(Silian_balance);
			bizProductionProcessService.removeById(Silian_id);
		}catch (Exception Silian_e){
			Silian_e.printStackTrace();
			//回滚事务
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return Result.error("撤销失败："+Silian_e.getMessage());
		}
		return Result.OK("撤销成功!");
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "成品加工-批量删除")
	@ApiOperation(value="成品加工-批量删除", notes="成品加工-批量删除")
	//@RequiresPermissions("org.jeecg.modules:biz_production_process:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String Silian_ids) {
		this.bizProductionProcessService.removeByIds(Arrays.asList(Silian_ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "成品加工-通过id查询")
	@ApiOperation(value="成品加工-通过id查询", notes="成品加工-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<BizProductionProcess> queryById(@RequestParam(name="id",required=true) String Silian_id) {
		BizProductionProcess Silian_bizProductionProcess = bizProductionProcessService.getById(Silian_id);
		if(Silian_bizProductionProcess==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(Silian_bizProductionProcess);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param bizProductionProcess
    */
    //@RequiresPermissions("org.jeecg.modules:biz_production_process:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest Silian_request, BizProductionProcess Silian_bizProductionProcess) {
        return super.exportXls(Silian_request, Silian_bizProductionProcess, BizProductionProcess.class, "成品加工");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    //@RequiresPermissions("biz_production_process:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest Silian_request, HttpServletResponse Silian_response) {
        return super.importExcel(Silian_request, Silian_response, BizProductionProcess.class);
    }

}
