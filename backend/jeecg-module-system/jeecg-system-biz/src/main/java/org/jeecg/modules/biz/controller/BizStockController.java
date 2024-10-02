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
import org.jeecg.modules.biz.entity.BizStock;
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
 * @Description: 股票投资
 * @Author: jeecg-boot
 * @Date: 2023-09-27
 * @Version: V1.0
 */
@Api(tags = "股票投资")
@RestController
@RequestMapping("/biz/bizStock")
@Slf4j
public class BizStockController
        extends JeecgController<BizStock, IBizStockService> {
    @Autowired
    private IBizStockService bizStockService;
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
     * @param bizStock
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    // @AutoLog(value = "股票投资-分页列表查询")
    @ApiOperation(value = "股票投资-分页列表查询", notes = "股票投资-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<BizStock>> queryPageList(BizStock bizStock,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest req) {
        QueryWrapper<BizStock> queryWrapper = QueryGenerator.initQueryWrapper(bizStock,
                req.getParameterMap());
        Page<BizStock> page = new Page<BizStock>(pageNo, pageSize);
        IPage<BizStock> pageList = bizStockService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param bizStock
     * @return
     */
    @AutoLog(value = "股票投资-添加")
    @ApiOperation(value = "股票投资-添加", notes = "股票投资-添加")
    // @RequiresPermissions("org.jeecg.modules:biz_material_trans:add")
    @PostMapping(value = "/add")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> add(@RequestBody BizStock bizStock) {




        log.info("开始初始化查询条件构造器，搜索对象类型：{}", bizStock);


        try {
            log.info("12356896074578697");
            Integer yearc=bizStock.getYearCode();
            log.info("YearCode: {}", yearc);
            
            log.info("Start Year :::: {}", bizFiscalYearService.getById(yearc));
            log.info("Active Year :::: {}", bizFiscalYearService.getActiveYearCode());
            log.info("Max Year Code:::: {}", bizFiscalYearService.getMaxYearCode());
            log.info("Process Count :::: {}", bizFiscalYearService.getProcessCount());
            log.info("Use yearCode to query the Bizfiscal year: {}", bizFiscalYearService.getByYearCode(yearc));

            
            BizFiscalYear startyear=bizFiscalYearService.getByYearCode(yearc);
            // 检查是否在5分钟内投资
            Date starttime=startyear.getStartTime();

            Date now = new Date();

            boolean isWithinFiveMinutes = now.after(starttime) && now.before(new Date(starttime.getTime() + 5 * 60000));

            if(!isWithinFiveMinutes){
                throw new Exception("已过当前财年投资时间");
            }

            BizSubjectBalance buyerBalance = bizSubjectBalanceService.getByUserId(bizStock.getSellerId());
            //判断投资总额是否超标
            Double acct=buyerBalance.getCashAcct();
            if(acct * 0.10 < bizStock.getTransPrice()){
                throw new Exception("投资金额超过10%");
            }

            // 减少买方现金余额
            buyerBalance.setCashAcct((buyerBalance.getCashAcct() == null ? 0 : buyerBalance.getCashAcct())
                    - (bizStock.getTransPrice() == null ? 0 : bizStock.getTransPrice()));
            if (buyerBalance.getCashAcct() < 0) {
                throw new Exception("存入方现金余额不足");
            }

            bizSubjectBalanceService.updateById(buyerBalance);
            bizStockService.save(bizStock);

        } catch (Exception e) {
            e.printStackTrace();
            // 回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.error("交易：" + e.getMessage());
        }
        return Result.OK("添加成功！");
    }



    /**
     *   通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "股票投资-通过id提现")
    @ApiOperation(value="股票投资-通过id提现", notes="股票投资-通过id提现")
    //@RequiresPermissions("org.jeecg.modules:biz_finance_management:delete")
    @DeleteMapping(value = "/delete")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> delete(@RequestParam(name="id",required=true) String id) {

        try{

            BizStock bizStock = bizStockService.getById(id);

            Integer now=bizFiscalYearService.getActiveYearCode();//得到当前财年的yearCode
            
            if(now==bizStock.getYearCode()){//无法在第0财年提款
                throw new Exception("股票投资为t+1模式,无法在当前财年提款");
            }

            //计算收益
            Double Inc=bizFiscalYearService.getByYearCode(now).getStockInc();

            log.info("Here is the Increase: {}", Inc);
            
            //增加投资方资金
            BizSubjectBalance buyerBalance = bizSubjectBalanceService.getByUserId(bizStock.getSellerId());

            buyerBalance.setCashAcct(buyerBalance.getCashAcct() + bizStock.getTransPrice() * (1.00 + Inc));

            bizSubjectBalanceService.updateById(buyerBalance);
            bizStockService.removeById(id);

        }catch (Exception e){
            
            e.printStackTrace();
            //回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return  Result.error("提现失败：" + e.getMessage());

        }
        return Result.OK("提现成功!");
    }
}