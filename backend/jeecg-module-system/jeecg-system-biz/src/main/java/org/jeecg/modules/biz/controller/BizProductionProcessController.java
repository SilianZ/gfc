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
	public Result<IPage<BizProductionProcess>> queryPageList(BizProductionProcess bizProductionProcess,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<BizProductionProcess> queryWrapper = QueryGenerator.initQueryWrapper(bizProductionProcess, req.getParameterMap());
		Page<BizProductionProcess> page = new Page<BizProductionProcess>(pageNo, pageSize);
		IPage<BizProductionProcess> pageList = bizProductionProcessService.page(page, queryWrapper);
		return Result.OK(pageList);
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
	public Result<String> add(@RequestBody BizProductionProcess bizProductionProcess) {
		try {
			//设置所属财年
			bizProductionProcess.setYearCode(bizFiscalYearService.getActiveYearCode());
			//判断工厂生产上限
			BizTeamResource resource = bizTeamResourceService.getById(bizProductionProcess.getResourceId());
			if("ML".equals(resource.getResourceType())){
				Double processNumber = bizProductionProcessService.getProcessNumber(bizProductionProcess.getResourceId(),bizFiscalYearService.getActiveYearCode());
				processNumber = processNumber == null ? 0 : processNumber;
				if(processNumber + (bizProductionProcess.getGtNumber() == null ? 0 : bizProductionProcess.getGtNumber())
						+ (bizProductionProcess.getGsNumber() == null ? 0 : bizProductionProcess.getGsNumber())
						+ (bizProductionProcess.getSyNumber() == null ? 0 : bizProductionProcess.getSyNumber())
						+ (bizProductionProcess.getSlNumber() == null ? 0 : bizProductionProcess.getSlNumber())  > 260){//马里奥工厂每年上限为260
					throw new Exception("超出该工厂本财年生产上限");
				}
			}
			if((bizProductionProcess.getChipNumber() == null ? 0 : bizProductionProcess.getChipNumber()) +
					(bizProductionProcess.getCardNumber() == null ? 0 : bizProductionProcess.getCardNumber())+
					(bizProductionProcess.getEnergyNumber() == null ? 0 : bizProductionProcess.getEnergyNumber())+
					(bizProductionProcess.getToyNumber() == null ? 0 : bizProductionProcess.getToyNumber()) <= 0){
				throw new Exception("错误的生产数量");
			}
			bizProductionProcessService.save(bizProductionProcess);
			BizSubjectBalance balance = bizSubjectBalanceService.getByUserId(bizProductionProcess.getUserId());
			//减少团队原材料库存
			balance.setSteelAcct((balance.getSteelAcct() == null ? 0 : balance.getSteelAcct()) - (bizProductionProcess.getGtNumber() == null ? 0 : bizProductionProcess.getGtNumber()));
			balance.setSilicaAcct((balance.getSilicaAcct() == null ? 0 : balance.getSilicaAcct()) - (bizProductionProcess.getGsNumber() == null ? 0 : bizProductionProcess.getGsNumber()));
			balance.setCrudeAcct((balance.getCrudeAcct() == null ? 0 : balance.getCrudeAcct()) - (bizProductionProcess.getSyNumber() == null ? 0 : bizProductionProcess.getSyNumber()));
			balance.setPlasticsAcct((balance.getPlasticsAcct() == null ? 0 : balance.getPlasticsAcct()) - (bizProductionProcess.getSlNumber() == null ? 0 : bizProductionProcess.getSlNumber()));
			//增加团队成品库存
			balance.setChipAcct((balance.getChipAcct() == null ? 0 : balance.getChipAcct()) + (bizProductionProcess.getChipNumber() == null ? 0 : bizProductionProcess.getChipNumber()));
			balance.setCardAcct((balance.getCardAcct() == null ? 0 : balance.getCardAcct()) + (bizProductionProcess.getCardNumber() == null ? 0 : bizProductionProcess.getCardNumber()));
			balance.setEnergyAcct((balance.getEnergyAcct() == null ? 0 : balance.getEnergyAcct()) + (bizProductionProcess.getEnergyNumber() == null ? 0 : bizProductionProcess.getEnergyNumber()));
			balance.setToyAcct((balance.getToyAcct() == null ? 0 : balance.getToyAcct()) + (bizProductionProcess.getToyNumber() == null ? 0 : bizProductionProcess.getToyNumber()));
			bizSubjectBalanceService.updateById(balance);
		}catch (Exception e){
			e.printStackTrace();
			//回滚事务
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return Result.error("成品加工失败:" + e.getMessage());
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
	public Result<String> edit(@RequestBody BizProductionProcess bizProductionProcess) {
		bizProductionProcessService.updateById(bizProductionProcess);
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
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		try{
			BizProductionProcess bizProductionProcess = bizProductionProcessService.getById(id);
			BizSubjectBalance balance = bizSubjectBalanceService.getByUserId(bizProductionProcess.getUserId());
			// 更新团队原材料库存
			balance.setSteelAcct((balance.getSteelAcct() != null ? balance.getSteelAcct() : 0) + (bizProductionProcess.getGtNumber() != null ? bizProductionProcess.getGtNumber() : 0));
			balance.setSilicaAcct((balance.getSilicaAcct() != null ? balance.getSilicaAcct() : 0) + (bizProductionProcess.getGsNumber() != null ? bizProductionProcess.getGsNumber() : 0));
			balance.setCrudeAcct((balance.getCrudeAcct() != null ? balance.getCrudeAcct() : 0) + (bizProductionProcess.getSyNumber() != null ? bizProductionProcess.getSyNumber() : 0));
			balance.setPlasticsAcct((balance.getPlasticsAcct() != null ? balance.getPlasticsAcct() : 0) + (bizProductionProcess.getSlNumber() != null ? bizProductionProcess.getSlNumber() : 0));
			// 更新团队成品库存
			balance.setChipAcct((balance.getChipAcct() != null ? balance.getChipAcct() : 0) - (bizProductionProcess.getChipNumber() != null ? bizProductionProcess.getChipNumber() : 0));
			balance.setCardAcct((balance.getCardAcct() != null ? balance.getCardAcct() : 0) - (bizProductionProcess.getCardNumber() != null ? bizProductionProcess.getCardNumber() : 0));
			balance.setEnergyAcct((balance.getEnergyAcct() != null ? balance.getEnergyAcct() : 0) - (bizProductionProcess.getEnergyNumber() != null ? bizProductionProcess.getEnergyNumber() : 0));
			balance.setToyAcct((balance.getToyAcct() != null ? balance.getToyAcct() : 0) - (bizProductionProcess.getToyNumber() != null ? bizProductionProcess.getToyNumber() : 0));

			if(balance.getChipAcct() < 0 || balance.getCardAcct() < 0 || balance.getEnergyAcct() < 0 || balance.getToyAcct() < 0){
				throw new Exception("该用户成品库存不足，无法撤销");
			}
			bizSubjectBalanceService.updateById(balance);
			bizProductionProcessService.removeById(id);
		}catch (Exception e){
			e.printStackTrace();
			//回滚事务
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return Result.error("撤销失败："+e.getMessage());
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
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.bizProductionProcessService.removeByIds(Arrays.asList(ids.split(",")));
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
	public Result<BizProductionProcess> queryById(@RequestParam(name="id",required=true) String id) {
		BizProductionProcess bizProductionProcess = bizProductionProcessService.getById(id);
		if(bizProductionProcess==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(bizProductionProcess);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param bizProductionProcess
    */
    //@RequiresPermissions("org.jeecg.modules:biz_production_process:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, BizProductionProcess bizProductionProcess) {
        return super.exportXls(request, bizProductionProcess, BizProductionProcess.class, "成品加工");
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
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, BizProductionProcess.class);
    }

}
