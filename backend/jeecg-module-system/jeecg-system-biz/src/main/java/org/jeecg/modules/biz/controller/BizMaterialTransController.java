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

 /**
 * @Description: 原材交易
 * @Author: jeecg-boot
 * @Date:   2023-09-27
 * @Version: V1.0
 */
@Api(tags="原材交易")
@RestController
@RequestMapping("/biz/bizMaterialTrans")
@Slf4j
public class BizMaterialTransController extends JeecgController<BizMaterialTrans, IBizMaterialTransService> {
    @Autowired
    private IBizMaterialTransService bizMaterialTransService;
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
     * @param bizMaterialTrans
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    //@AutoLog(value = "原材交易-分页列表查询")
    @ApiOperation(value="原材交易-分页列表查询", notes="原材交易-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<BizMaterialTrans>> queryPageList(BizMaterialTrans Silian_bizMaterialTrans,
                                   @RequestParam(name="pageNo", defaultValue="1") Integer Silian_pageNo,
                                   @RequestParam(name="pageSize", defaultValue="10") Integer Silian_pageSize,
                                   HttpServletRequest Silian_req) {
        QueryWrapper<BizMaterialTrans> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_bizMaterialTrans, Silian_req.getParameterMap());
        Page<BizMaterialTrans> Silian_page = new Page<BizMaterialTrans>(Silian_pageNo, Silian_pageSize);
        IPage<BizMaterialTrans> Silian_pageList = bizMaterialTransService.page(Silian_page, Silian_queryWrapper);
        return Result.OK(Silian_pageList);
    }
    /**
     *   添加
     *
     * @param bizMaterialTrans
     * @return
     */
    @AutoLog(value = "原材交易-添加")
    @ApiOperation(value="原材交易-添加", notes="原材交易-添加")
    //@RequiresPermissions("org.jeecg.modules:biz_material_trans:add")
    @PostMapping(value = "/add")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> add(@RequestBody BizMaterialTrans Silian_bizMaterialTrans) {
        try {
            //设置所属财年
            Silian_bizMaterialTrans.setYearCode(bizFiscalYearService.getActiveYearCode());
            //扣减卖方材料库存
            BizSubjectBalance Silian_sellerBalance = bizSubjectBalanceService.getByUserId(Silian_bizMaterialTrans.getSellerId());
            Silian_sellerBalance.setSteelAcct((Silian_sellerBalance.getSteelAcct() == null ? 0 : Silian_sellerBalance.getSteelAcct()) - (Silian_bizMaterialTrans.getGtNumber() == null ? 0 : Silian_bizMaterialTrans.getGtNumber()));
            Silian_sellerBalance.setSilicaAcct((Silian_sellerBalance.getSilicaAcct() == null ? 0 : Silian_sellerBalance.getSilicaAcct()) - (Silian_bizMaterialTrans.getGsNumber() == null ? 0 : Silian_bizMaterialTrans.getGsNumber()));
            Silian_sellerBalance.setCrudeAcct((Silian_sellerBalance.getCrudeAcct() == null ? 0 : Silian_sellerBalance.getCrudeAcct()) - (Silian_bizMaterialTrans.getSyNumber() == null ? 0 : Silian_bizMaterialTrans.getSyNumber()));
            Silian_sellerBalance.setPlasticsAcct((Silian_sellerBalance.getPlasticsAcct() == null ? 0 : Silian_sellerBalance.getPlasticsAcct()) - (Silian_bizMaterialTrans.getSlNumber() == null ? 0 : Silian_bizMaterialTrans.getSlNumber()));
            //扣除交易所得税
            SysDepart Silian_depart = sysDepartService.queryDepartsByUsername(Silian_bizMaterialTrans.getSellerId()).get(0);
            Double Silian_taxAmount = bizBankConfigService.collectTaxes(Silian_bizMaterialTrans.getTransPrice(), Silian_bizMaterialTrans.getIsTransnational().equals("Y"), Silian_depart.getId());
            //增加卖方现金余额
            Silian_sellerBalance.setCashAcct((Silian_sellerBalance.getCashAcct() == null ? 0 : Silian_sellerBalance.getCashAcct()) + (Silian_bizMaterialTrans.getTransPrice() == null ? 0 : Silian_bizMaterialTrans.getTransPrice() - Silian_taxAmount));
            //增加买方材料库存
            BizSubjectBalance Silian_buyerBalance = bizSubjectBalanceService.getByUserId(Silian_bizMaterialTrans.getBuyerId());
            Silian_buyerBalance.setSteelAcct((Silian_buyerBalance.getSteelAcct() == null ? 0 : Silian_buyerBalance.getSteelAcct()) + (Silian_bizMaterialTrans.getGtNumber() == null ? 0 : Silian_bizMaterialTrans.getGtNumber()));
            Silian_buyerBalance.setSilicaAcct((Silian_buyerBalance.getSilicaAcct() == null ? 0 : Silian_buyerBalance.getSilicaAcct()) + (Silian_bizMaterialTrans.getGsNumber() == null ? 0 : Silian_bizMaterialTrans.getGsNumber()));
            Silian_buyerBalance.setCrudeAcct((Silian_buyerBalance.getCrudeAcct() == null ? 0 : Silian_buyerBalance.getCrudeAcct()) + (Silian_bizMaterialTrans.getSyNumber() == null ? 0 : Silian_bizMaterialTrans.getSyNumber()));
            Silian_buyerBalance.setPlasticsAcct((Silian_buyerBalance.getPlasticsAcct() == null ? 0 : Silian_buyerBalance.getPlasticsAcct()) + (Silian_bizMaterialTrans.getSlNumber() == null ? 0 : Silian_bizMaterialTrans.getSlNumber()));
            //减少买方现金余额
            Silian_buyerBalance.setCashAcct((Silian_buyerBalance.getCashAcct() == null ? 0 : Silian_buyerBalance.getCashAcct()) - (Silian_bizMaterialTrans.getTransPrice() == null ? 0 : Silian_bizMaterialTrans.getTransPrice()));
            if(Silian_sellerBalance.getSteelAcct() < 0 ||  Silian_sellerBalance.getSilicaAcct() < 0 || Silian_sellerBalance.getCrudeAcct() < 0 || Silian_sellerBalance.getPlasticsAcct() < 0 ){
                throw new Exception("卖家材料库存不足");
            }
            if(Silian_buyerBalance.getCashAcct() < 0){
                throw new Exception("买家现金余额不足");
            }
            bizSubjectBalanceService.updateById(Silian_sellerBalance);
            bizSubjectBalanceService.updateById(Silian_buyerBalance);
            bizMaterialTransService.save(Silian_bizMaterialTrans);
        }catch (Exception Silian_e){
            Silian_e.printStackTrace();
            //回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.error("交易失败：" + Silian_e.getMessage());
        }
        return Result.OK("添加成功！");
    }

    /**
     *  编辑
     *
     * @param bizMaterialTrans
     * @return
     */
    @AutoLog(value = "原材交易-编辑")
    @ApiOperation(value="原材交易-编辑", notes="原材交易-编辑")
    //@RequiresPermissions("org.jeecg.modules:biz_material_trans:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<String> edit(@RequestBody BizMaterialTrans Silian_bizMaterialTrans) {
        bizMaterialTransService.updateById(Silian_bizMaterialTrans);
        return Result.OK("编辑成功!");
    }

    /**
     *   通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "原材交易-通过id删除")
    @ApiOperation(value="原材交易-通过id删除", notes="原材交易-通过id删除")
    //@RequiresPermissions("org.jeecg.modules:biz_material_trans:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name="id",required=true) String Silian_id) {
        try{
            BizMaterialTrans Silian_bizMaterialTrans = bizMaterialTransService.getById(Silian_id);
            //增加卖方材料库存
            BizSubjectBalance Silian_sellerBalance = bizSubjectBalanceService.getByUserId(Silian_bizMaterialTrans.getSellerId());
            Silian_sellerBalance.setSteelAcct((Silian_sellerBalance.getSteelAcct() == null ? 0 : Silian_sellerBalance.getSteelAcct()) + (Silian_bizMaterialTrans.getGtNumber() == null ? 0 : Silian_bizMaterialTrans.getGtNumber()));
            Silian_sellerBalance.setSilicaAcct((Silian_sellerBalance.getSilicaAcct() == null ? 0 : Silian_sellerBalance.getSilicaAcct()) + (Silian_bizMaterialTrans.getGsNumber() == null ? 0 : Silian_bizMaterialTrans.getGsNumber()));
            Silian_sellerBalance.setCrudeAcct((Silian_sellerBalance.getCrudeAcct() == null ? 0 : Silian_sellerBalance.getCrudeAcct()) + (Silian_bizMaterialTrans.getSyNumber() == null ? 0 : Silian_bizMaterialTrans.getSyNumber()));
            Silian_sellerBalance.setPlasticsAcct((Silian_sellerBalance.getPlasticsAcct() == null ? 0 : Silian_sellerBalance.getPlasticsAcct()) + (Silian_bizMaterialTrans.getSlNumber() == null ? 0 : Silian_bizMaterialTrans.getSlNumber()));
            //冲销交易所得税
            SysDepart Silian_depart = sysDepartService.queryDepartsByUsername(Silian_bizMaterialTrans.getSellerId()).get(0);
            Double Silian_taxAmount = bizBankConfigService.offTaxes(Silian_bizMaterialTrans.getTransPrice(), Silian_bizMaterialTrans.getIsTransnational().equals("Y"), Silian_depart.getId());
            //减少卖方现金余额
            Silian_sellerBalance.setCashAcct((Silian_sellerBalance.getCashAcct() == null ? 0 : Silian_sellerBalance.getCashAcct()) - (Silian_bizMaterialTrans.getTransPrice() == null ? 0 : Silian_bizMaterialTrans.getTransPrice()) + Silian_taxAmount);
            //减少买方材料库存
            BizSubjectBalance Silian_buyerBalance = bizSubjectBalanceService.getByUserId(Silian_bizMaterialTrans.getBuyerId());
            Silian_buyerBalance.setSteelAcct((Silian_buyerBalance.getSteelAcct() == null ? 0 : Silian_buyerBalance.getSteelAcct()) - (Silian_bizMaterialTrans.getGtNumber() == null ? 0 : Silian_bizMaterialTrans.getGtNumber()));
            Silian_buyerBalance.setSilicaAcct((Silian_buyerBalance.getSilicaAcct() == null ? 0 : Silian_buyerBalance.getSilicaAcct()) - (Silian_bizMaterialTrans.getGsNumber() == null ? 0 : Silian_bizMaterialTrans.getGsNumber()));
            Silian_buyerBalance.setCrudeAcct((Silian_buyerBalance.getCrudeAcct() == null ? 0 : Silian_buyerBalance.getCrudeAcct()) - (Silian_bizMaterialTrans.getSyNumber() == null ? 0 : Silian_bizMaterialTrans.getSyNumber()));
            Silian_buyerBalance.setPlasticsAcct((Silian_buyerBalance.getPlasticsAcct() == null ? 0 : Silian_buyerBalance.getPlasticsAcct()) - (Silian_bizMaterialTrans.getSlNumber() == null ? 0 : Silian_bizMaterialTrans.getSlNumber()));
            //增加买方现金余额
            Silian_buyerBalance.setCashAcct((Silian_buyerBalance.getCashAcct() == null ? 0 : Silian_buyerBalance.getCashAcct()) + (Silian_bizMaterialTrans.getTransPrice() == null ? 0 : Silian_bizMaterialTrans.getTransPrice()));
            if(Silian_buyerBalance.getCashAcct() < 0){
                throw new Exception("卖家现金余额不足");
            }
            if(Silian_sellerBalance.getSteelAcct() < 0 ||  Silian_sellerBalance.getSilicaAcct() < 0 || Silian_sellerBalance.getCrudeAcct() < 0 || Silian_sellerBalance.getPlasticsAcct() < 0){
                throw new Exception("卖家材料库存不足");
            }
            bizSubjectBalanceService.updateById(Silian_sellerBalance);
            bizSubjectBalanceService.updateById(Silian_buyerBalance);
            bizMaterialTransService.removeById(Silian_id);
        }catch (Exception Silian_e){
            Silian_e.printStackTrace();
            //回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return  Result.error("撤销失败：" + Silian_e.getMessage());
        }
        return Result.OK("撤销成功!");
    }

    /**
     *  批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "原材交易-批量删除")
    @ApiOperation(value="原材交易-批量删除", notes="原材交易-批量删除")
    //@RequiresPermissions("org.jeecg.modules:biz_material_trans:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String Silian_ids) {
        this.bizMaterialTransService.removeByIds(Arrays.asList(Silian_ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "原材交易-通过id查询")
    @ApiOperation(value="原材交易-通过id查询", notes="原材交易-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<BizMaterialTrans> queryById(@RequestParam(name="id",required=true) String Silian_id) {
        BizMaterialTrans Silian_bizMaterialTrans = bizMaterialTransService.getById(Silian_id);
        if(Silian_bizMaterialTrans==null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(Silian_bizMaterialTrans);
    }

    /**
    * 导出excel
    *
    * @param request
    * @param bizMaterialTrans
    */
    //@RequiresPermissions("org.jeecg.modules:biz_material_trans:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest Silian_request, BizMaterialTrans Silian_bizMaterialTrans) {
        return super.exportXls(Silian_request, Silian_bizMaterialTrans, BizMaterialTrans.class, "原材交易");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    //@RequiresPermissions("biz_material_trans:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest Silian_request, HttpServletResponse Silian_response) {
        return super.importExcel(Silian_request, Silian_response, BizMaterialTrans.class);
    }

}
