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
import org.jeecg.modules.biz.entity.BizMaterialTrans;
import org.jeecg.modules.biz.entity.BizFinanceManagement;
import org.jeecg.modules.biz.entity.BizFiscalYear;
import org.jeecg.modules.biz.entity.BizSubjectBalance;
import org.jeecg.modules.biz.service.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysDepartService;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;
import lombok.Data;
/**
 * @Description: 理财
 * @Author: jeecg-boot
 * @Date: 2023-09-27
 * @Version: V1.0
 */
@Api(tags = "理财")
@RestController
@RequestMapping("/biz/bizFinanceManagement")
@Slf4j
public class BizFinanceManagementController
        extends JeecgController<BizFinanceManagement, IBizFinanceManagementService> {
    @Autowired
    private IBizFinanceManagementService bizFinanceManagementService;
    @Autowired
    private IBizSubjectBalanceService bizSubjectBalanceService;
    @Autowired
    private IBizFiscalYearService bizFiscalYearService;
    @Autowired
    private IBizBankConfigService bizBankConfigService;
    @Autowired
    private ISysDepartService sysDepartService;

    /**
     * 分页列表查询 Management
     *
     * @param bizFinanceManagement
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    // @AutoLog(value = "理财-分页列表查询")
    @ApiOperation(value = "理财-分页列表查询", notes = "理财-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<BizFinanceManagement>> queryPageList(BizFinanceManagement Silian_bizFinanceManagement,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer Silian_pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer Silian_pageSize,
            HttpServletRequest Silian_req) {
        QueryWrapper<BizFinanceManagement> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_bizFinanceManagement,
                Silian_req.getParameterMap());
        Page<BizFinanceManagement> Silian_page = new Page<BizFinanceManagement>(Silian_pageNo, Silian_pageSize);
        IPage<BizFinanceManagement> Silian_pageList = bizFinanceManagementService.page(Silian_page, Silian_queryWrapper);
        return Result.OK(Silian_pageList);
    }

    /**
     * 添加
     *
     * @param bizFinanceManagement
     * @return
     */
    @AutoLog(value = "理财-添加")
    @ApiOperation(value = "理财-添加", notes = "理财-添加")
    // @RequiresPermissions("org.jeecg.modules:biz_material_trans:add")
    @PostMapping(value = "/add")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> add(@RequestBody BizFinanceManagement Silian_bizFinanceManagement) {




        log.info("开始初始化查询条件构造器，搜索对象类型：{}", Silian_bizFinanceManagement);


        try {
            log.info("12356896074578697");
            Integer Silian_yearc=Silian_bizFinanceManagement.getYearCode();
            log.info("YearCode: {}", Silian_yearc);

            log.info("Start Year :::: {}", bizFiscalYearService.getById(Silian_yearc));
            log.info("Active Year :::: {}", bizFiscalYearService.getActiveYearCode());
            log.info("Max Year Code:::: {}", bizFiscalYearService.getMaxYearCode());
            log.info("Process Count :::: {}", bizFiscalYearService.getProcessCount());
            log.info("Use yearCode to query the Bizfiscal year: {}", bizFiscalYearService.getByYearCode(Silian_yearc));


            BizFiscalYear Silian_startyear=bizFiscalYearService.getByYearCode(Silian_yearc);
            // 检查是否在5分钟内投资
            Date Silian_starttime=Silian_startyear.getStartTime();

            Date Silian_now = new Date();

            boolean Silian_isWithinFiveMinutes = Silian_now.after(Silian_starttime) && Silian_now.before(new Date(Silian_starttime.getTime() + 5 * 60000));

            if(!Silian_isWithinFiveMinutes){
                throw new Exception("已过当前财年投资时间");
            }

            BizSubjectBalance Silian_buyerBalance = bizSubjectBalanceService.getByUserId(Silian_bizFinanceManagement.getSellerId());
            // 减少买方现金余额
            Silian_buyerBalance.setCashAcct((Silian_buyerBalance.getCashAcct() == null ? 0 : Silian_buyerBalance.getCashAcct())
                    - (Silian_bizFinanceManagement.getTransPrice() == null ? 0 : Silian_bizFinanceManagement.getTransPrice()));
            if (Silian_buyerBalance.getCashAcct() < 0) {
                throw new Exception("存入方现金余额不足");
            }
            bizSubjectBalanceService.updateById(Silian_buyerBalance);
            bizFinanceManagementService.save(Silian_bizFinanceManagement);
        } catch (Exception Silian_e) {
            Silian_e.printStackTrace();
            // 回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.error("交易：" + Silian_e.getMessage());
        }
        return Result.OK("添加成功！");
    }

    /**
     *   通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "理财-通过id提现")
    @ApiOperation(value="理财-通过id提现", notes="理财-通过id提现")
    //@RequiresPermissions("org.jeecg.modules:biz_finance_management:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name="id",required=true) String Silian_id) {

        log.info("Here is the FinanceManagement Process' id: {}", Silian_id);

        try{

            BizFinanceManagement Silian_bizFinanceManagement = bizFinanceManagementService.getById(Silian_id);

            log.info("Here is the FinanceManagement Process we need to delete: {}", Silian_bizFinanceManagement);

            //计算复利次数
            Integer Silian_period=bizFiscalYearService.getActiveYearCode()-Silian_bizFinanceManagement.getYearCode();

            log.info("Here is the ActiveYearCode: {}", bizFiscalYearService.getActiveYearCode());
            log.info("Here is the got YearCode: {}", Silian_bizFinanceManagement.getYearCode());
            log.info("Here is the period we need to calculate: {}", Silian_period);

            //增加投资方资金
            BizSubjectBalance Silian_buyerBalance = bizSubjectBalanceService.getByUserId(Silian_bizFinanceManagement.getSellerId());

            Silian_buyerBalance.setCashAcct(Silian_buyerBalance.getCashAcct()+Silian_bizFinanceManagement.getTransPrice() * Math.pow(1.04,Silian_period));

            bizSubjectBalanceService.updateById(Silian_buyerBalance);
            bizFinanceManagementService.removeById(Silian_id);

        }catch (Exception Silian_e){
            Silian_e.printStackTrace();
            //回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return  Result.error("提现失败：" + Silian_e.getMessage());
        }
        return Result.OK("提现成功!");
    }

}