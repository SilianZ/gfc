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
    public Result<IPage<BizFinanceManagement>> queryPageList(BizFinanceManagement bizFinanceManagement,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest req) {
        QueryWrapper<BizFinanceManagement> queryWrapper = QueryGenerator.initQueryWrapper(bizFinanceManagement,
                req.getParameterMap());
        Page<BizFinanceManagement> page = new Page<BizFinanceManagement>(pageNo, pageSize);
        IPage<BizFinanceManagement> pageList = bizFinanceManagementService.page(page, queryWrapper);
        return Result.OK(pageList);
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
    public Result<String> add(@RequestBody BizFinanceManagement bizFinanceManagement) {
        

        try {
            BizFiscalYear startyear=bizFiscalYearService.getById(bizFinanceManagement.getYearCode());
            // 检查是否在5分钟内投资
            Date starttime=startyear.getStartTime();

            Date now = new Date();

            boolean isWithinFiveMinutes = now.after(starttime) && now.before(new Date(starttime.getTime() + 5 * 60000));

            log.info("The add of New Finance management object can be run through this statement");
            if(!isWithinFiveMinutes){
                throw new Exception("已过当前财年投资时间");
            }

            BizSubjectBalance buyerBalance = bizSubjectBalanceService.getByUserId(bizFinanceManagement.getBuyerId());
            // 减少买方现金余额
            buyerBalance.setCashAcct((buyerBalance.getCashAcct() == null ? 0 : buyerBalance.getCashAcct())
                    - (bizFinanceManagement.getTransPrice() == null ? 0 : bizFinanceManagement.getTransPrice()));
            if (buyerBalance.getCashAcct() < 0) {
                throw new Exception("存入方现金余额不足");
            }
            bizSubjectBalanceService.updateById(buyerBalance);
            bizFinanceManagementService.save(bizFinanceManagement);
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
    @AutoLog(value = "理财-通过id提现")
    @ApiOperation(value="理财-通过id提现", notes="理财-通过id提现")
    //@RequiresPermissions("org.jeecg.modules:biz_finance_management:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name="id",required=true) String id) {
        try{
            BizFinanceManagement bizFinanceManagement = bizFinanceManagementService.getById(id);
            //计算复利次数
            Integer period=bizFiscalYearService.getActiveYearCode()-bizFinanceManagement.getYearCode();
            
            //增加投资方资金
            BizSubjectBalance buyerBalance = bizSubjectBalanceService.getByUserId(bizFinanceManagement.getBuyerId());

            buyerBalance.setCashAcct(bizFinanceManagement.getTransPrice() * Math.pow(1.04,period));

            bizSubjectBalanceService.updateById(buyerBalance);
            bizFinanceManagementService.save(bizFinanceManagement);

        }catch (Exception e){
            e.printStackTrace();
            //回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return  Result.error("提现失败：" + e.getMessage());
        }
        return Result.OK("提现成功!");
    }

}