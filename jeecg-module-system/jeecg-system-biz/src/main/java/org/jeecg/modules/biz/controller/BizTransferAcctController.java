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
import org.jeecg.modules.biz.entity.BizSubjectBalance;
import org.jeecg.modules.biz.entity.BizTransferAcct;
import org.jeecg.modules.biz.service.IBizBankConfigService;
import org.jeecg.modules.biz.service.IBizFiscalYearService;
import org.jeecg.modules.biz.service.IBizSubjectBalanceService;
import org.jeecg.modules.biz.service.IBizTransferAcctService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.system.entity.SysDepart;
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

/**
 * @Description: 账户转账
 * @Author: jeecg-boot
 * @Date: 2023-09-29
 * @Version: V1.0
 */
@Api(tags = "账户转账")
@RestController
@RequestMapping("/biz/bizTransferAcct")
@Slf4j
public class BizTransferAcctController extends JeecgController<BizTransferAcct, IBizTransferAcctService> {
    @Autowired
    private IBizTransferAcctService bizTransferAcctService;
    @Autowired
    private IBizFiscalYearService bizFiscalYearService;
    @Autowired
    private IBizSubjectBalanceService bizSubjectBalanceService;
    @Autowired
    private IBizBankConfigService bizBankConfigService;
    @Autowired
    private ISysDepartService sysDepartService;

    /**
     * 分页列表查询
     *
     * @param bizTransferAcct
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "账户转账-分页列表查询")
    @ApiOperation(value = "账户转账-分页列表查询", notes = "账户转账-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<BizTransferAcct>> queryPageList(BizTransferAcct bizTransferAcct,
                                                        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                        HttpServletRequest req) {
        QueryWrapper<BizTransferAcct> queryWrapper = QueryGenerator.initQueryWrapper(bizTransferAcct, req.getParameterMap());
        Page<BizTransferAcct> page = new Page<BizTransferAcct>(pageNo, pageSize);
        IPage<BizTransferAcct> pageList = bizTransferAcctService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 添加
     *
     * @param bizTransferAcct
     * @return
     */
    @AutoLog(value = "账户转账-添加")
    @ApiOperation(value = "账户转账-添加", notes = "账户转账-添加")
    //@RequiresPermissions("org.jeecg.modules:biz_transfer_acct:add")
    @PostMapping(value = "/add")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> add(@RequestBody BizTransferAcct bizTransferAcct) {
        try {
            //设置所属财年
            bizTransferAcct.setYearCode(bizFiscalYearService.getActiveYearCode());
            //收取转账税收（如有）
            bizBankConfigService.collectTaxes(bizTransferAcct.getTaxAmount(),
                    sysDepartService.queryDepartsByUsername(bizTransferAcct.getPayeeId()).get(0).getId());
            //付款方账户
            BizSubjectBalance payerBalance = bizSubjectBalanceService.getByUserId(bizTransferAcct.getPayerId());
            //收款方账户
            BizSubjectBalance payeeBalance = bizSubjectBalanceService.getByUserId(bizTransferAcct.getPayeeId());
            if ("YBZZ".equals(bizTransferAcct.getPayType())) {//一般转账，走现金账户，考虑税收
                payerBalance.setCashAcct(payerBalance.getCashAcct() - bizTransferAcct.getTotalAmount());
                payeeBalance.setCashAcct(payeeBalance.getCashAcct() + bizTransferAcct.getTotalAmount() - bizTransferAcct.getTaxAmount());
                if (payerBalance.getCashAcct() < 0) {
                    throw new RuntimeException("付款方账户余额不足！");
                }
            } else if ("JCZZ".equals(bizTransferAcct.getPayType())) {//借出转账，付款方减少现金，增加债权，收款方增加现金，增加债务，借出不考虑税收
                payerBalance.setCashAcct(payerBalance.getCashAcct() -  bizTransferAcct.getTotalAmount());
                payerBalance.setDebtClaimAcct(payerBalance.getDebtClaimAcct() + bizTransferAcct.getTotalAmount());
                payeeBalance.setCashAcct(payeeBalance.getCashAcct() +  bizTransferAcct.getTotalAmount());
                payeeBalance.setDebtLiabilityAcct(payeeBalance.getDebtLiabilityAcct() +  bizTransferAcct.getTotalAmount());
                if (payerBalance.getCashAcct() < 0) {
                    throw new RuntimeException("付款方账户余额不足！");
                }
            } else if ("HKZZ".equals(bizTransferAcct.getPayType())) {//还款转账，付款方减少现金，减少债务，收款方增加现金，减少债权，考虑收入税收
                payerBalance.setCashAcct(payerBalance.getCashAcct() -  bizTransferAcct.getTotalAmount());
                payerBalance.setDebtLiabilityAcct(payerBalance.getDebtLiabilityAcct() - bizTransferAcct.getPrincipal());
                payeeBalance.setCashAcct(payeeBalance.getCashAcct() +  bizTransferAcct.getTotalAmount() - bizTransferAcct.getTaxAmount());
                payeeBalance.setDebtClaimAcct(payeeBalance.getDebtClaimAcct() -  bizTransferAcct.getPrincipal());
                if (payerBalance.getCashAcct() < 0) {
                    throw new RuntimeException("付款方账户余额不足！");
                }
            }else{
                payerBalance.setCashAcct(payerBalance.getCashAcct() - bizTransferAcct.getTotalAmount());
                payeeBalance.setCashAcct(payeeBalance.getCashAcct() + bizTransferAcct.getTotalAmount() - bizTransferAcct.getTaxAmount());
                if (payerBalance.getCashAcct() < 0) {
                    throw new RuntimeException("付款方账户余额不足！");
                }
            }
            bizSubjectBalanceService.updateById(payerBalance);
            bizSubjectBalanceService.updateById(payeeBalance);
            bizTransferAcctService.save(bizTransferAcct);
        } catch (Exception e) {
            e.printStackTrace();
            //回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.error("转账失败:" + e.getMessage());
        }
        return Result.OK("转账成功！");
    }

    /**
     * 编辑
     *
     * @param bizTransferAcct
     * @return
     */
    @AutoLog(value = "账户转账-编辑")
    @ApiOperation(value = "账户转账-编辑", notes = "账户转账-编辑")
    //@RequiresPermissions("org.jeecg.modules:biz_transfer_acct:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody BizTransferAcct bizTransferAcct) {
        bizTransferAcctService.updateById(bizTransferAcct);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "账户转账-通过id删除")
    @ApiOperation(value = "账户转账-通过id删除", notes = "账户转账-通过id删除")
    //@RequiresPermissions("org.jeecg.modules:biz_transfer_acct:delete")
    @DeleteMapping(value = "/delete")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
        try {
            BizTransferAcct bizTransferAcct = bizTransferAcctService.getById(id);
            //撤销转账税收（如有）
            bizBankConfigService.offTaxes(bizTransferAcct.getTaxAmount(),
                    sysDepartService.queryDepartsByUsername(bizTransferAcct.getPayeeId()).get(0).getId());
            //付款方账户
            BizSubjectBalance payerBalance = bizSubjectBalanceService.getByUserId(bizTransferAcct.getPayerId());
            //收款方账户
            BizSubjectBalance payeeBalance = bizSubjectBalanceService.getByUserId(bizTransferAcct.getPayeeId());
            if ("YBZZ".equals(bizTransferAcct.getPayType())) {//一般转账，走现金账户，考虑税收
                payerBalance.setCashAcct(payerBalance.getCashAcct() + bizTransferAcct.getTotalAmount());
                payeeBalance.setCashAcct(payeeBalance.getCashAcct() - bizTransferAcct.getTotalAmount() + bizTransferAcct.getTaxAmount());
                if (payerBalance.getCashAcct() < 0) {
                    throw new RuntimeException("付款方账户余额不足！");
                }
            } else if ("JCZZ".equals(bizTransferAcct.getPayType())) {//借出转账，付款方减少现金，增加债权，收款方增加现金，增加债务，借出不考虑税收
                payerBalance.setCashAcct(payerBalance.getCashAcct() +  bizTransferAcct.getTotalAmount());
                payerBalance.setDebtClaimAcct(payerBalance.getDebtClaimAcct() - bizTransferAcct.getTotalAmount());
                payeeBalance.setCashAcct(payeeBalance.getCashAcct() - bizTransferAcct.getTotalAmount());
                payeeBalance.setDebtLiabilityAcct(payeeBalance.getDebtLiabilityAcct() -  bizTransferAcct.getTotalAmount());
                if (payerBalance.getCashAcct() < 0) {
                    throw new RuntimeException("付款方账户余额不足！");
                }
            } else if ("HKZZ".equals(bizTransferAcct.getPayType())) {//还款转账，付款方减少现金，减少债务，收款方增加现金，减少债权，考虑收入税收
                payerBalance.setCashAcct(payerBalance.getCashAcct() +  bizTransferAcct.getTotalAmount());
                payerBalance.setDebtLiabilityAcct(payerBalance.getDebtLiabilityAcct() + bizTransferAcct.getPrincipal());
                payeeBalance.setCashAcct(payeeBalance.getCashAcct() -  bizTransferAcct.getTotalAmount() + bizTransferAcct.getTaxAmount());
                payeeBalance.setDebtClaimAcct(payeeBalance.getDebtClaimAcct() +  bizTransferAcct.getPrincipal());
                if (payerBalance.getCashAcct() < 0) {
                    throw new RuntimeException("付款方账户余额不足！");
                }
            }else{
                payerBalance.setCashAcct(payerBalance.getCashAcct() + bizTransferAcct.getTotalAmount());
                payeeBalance.setCashAcct(payeeBalance.getCashAcct() - bizTransferAcct.getTotalAmount() + bizTransferAcct.getTaxAmount());
                if (payerBalance.getCashAcct() < 0) {
                    throw new RuntimeException("付款方账户余额不足！");
                }
            }
            bizSubjectBalanceService.updateById(payerBalance);
            bizSubjectBalanceService.updateById(payeeBalance);
            bizTransferAcctService.removeById(id);
        } catch (Exception e) {
            e.printStackTrace();
            //回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.error("转账撤销失败:" + e.getMessage());
        }
        return Result.OK("转账撤销成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "账户转账-批量删除")
    @ApiOperation(value = "账户转账-批量删除", notes = "账户转账-批量删除")
    //@RequiresPermissions("org.jeecg.modules:biz_transfer_acct:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.bizTransferAcctService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "账户转账-通过id查询")
    @ApiOperation(value = "账户转账-通过id查询", notes = "账户转账-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<BizTransferAcct> queryById(@RequestParam(name = "id", required = true) String id) {
        BizTransferAcct bizTransferAcct = bizTransferAcctService.getById(id);
        if (bizTransferAcct == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(bizTransferAcct);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param bizTransferAcct
     */
    //@RequiresPermissions("org.jeecg.modules:biz_transfer_acct:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, BizTransferAcct bizTransferAcct) {
        return super.exportXls(request, bizTransferAcct, BizTransferAcct.class, "账户转账");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    //@RequiresPermissions("biz_transfer_acct:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, BizTransferAcct.class);
    }

}
