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
    public Result<IPage<BizMaterialTrans>> queryPageList(BizMaterialTrans bizMaterialTrans,
                                   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<BizMaterialTrans> queryWrapper = QueryGenerator.initQueryWrapper(bizMaterialTrans, req.getParameterMap());
        Page<BizMaterialTrans> page = new Page<BizMaterialTrans>(pageNo, pageSize);
        IPage<BizMaterialTrans> pageList = bizMaterialTransService.page(page, queryWrapper);
        return Result.OK(pageList);
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
    public Result<String> add(@RequestBody BizMaterialTrans bizMaterialTrans) {
        try {
            //设置所属财年
            bizMaterialTrans.setYearCode(bizFiscalYearService.getActiveYearCode());
            //扣减卖方材料库存
            BizSubjectBalance sellerBalance = bizSubjectBalanceService.getByUserId(bizMaterialTrans.getSellerId());
            sellerBalance.setSteelAcct((sellerBalance.getSteelAcct() == null ? 0 : sellerBalance.getSteelAcct()) - (bizMaterialTrans.getGtNumber() == null ? 0 : bizMaterialTrans.getGtNumber()));
            sellerBalance.setSilicaAcct((sellerBalance.getSilicaAcct() == null ? 0 : sellerBalance.getSilicaAcct()) - (bizMaterialTrans.getGsNumber() == null ? 0 : bizMaterialTrans.getGsNumber()));
            sellerBalance.setCrudeAcct((sellerBalance.getCrudeAcct() == null ? 0 : sellerBalance.getCrudeAcct()) - (bizMaterialTrans.getSyNumber() == null ? 0 : bizMaterialTrans.getSyNumber()));
            sellerBalance.setPlasticsAcct((sellerBalance.getPlasticsAcct() == null ? 0 : sellerBalance.getPlasticsAcct()) - (bizMaterialTrans.getSlNumber() == null ? 0 : bizMaterialTrans.getSlNumber()));
            //扣除交易所得税
            SysDepart depart = sysDepartService.queryDepartsByUsername(bizMaterialTrans.getSellerId()).get(0);
            Double taxAmount = bizBankConfigService.collectTaxes(bizMaterialTrans.getTransPrice(), bizMaterialTrans.getIsTransnational().equals("Y"), depart.getId());
            //增加卖方现金余额
            sellerBalance.setCashAcct((sellerBalance.getCashAcct() == null ? 0 : sellerBalance.getCashAcct()) + (bizMaterialTrans.getTransPrice() == null ? 0 : bizMaterialTrans.getTransPrice() - taxAmount));
            //增加买方材料库存
            BizSubjectBalance buyerBalance = bizSubjectBalanceService.getByUserId(bizMaterialTrans.getBuyerId());
            buyerBalance.setSteelAcct((buyerBalance.getSteelAcct() == null ? 0 : buyerBalance.getSteelAcct()) + (bizMaterialTrans.getGtNumber() == null ? 0 : bizMaterialTrans.getGtNumber()));
            buyerBalance.setSilicaAcct((buyerBalance.getSilicaAcct() == null ? 0 : buyerBalance.getSilicaAcct()) + (bizMaterialTrans.getGsNumber() == null ? 0 : bizMaterialTrans.getGsNumber()));
            buyerBalance.setCrudeAcct((buyerBalance.getCrudeAcct() == null ? 0 : buyerBalance.getCrudeAcct()) + (bizMaterialTrans.getSyNumber() == null ? 0 : bizMaterialTrans.getSyNumber()));
            buyerBalance.setPlasticsAcct((buyerBalance.getPlasticsAcct() == null ? 0 : buyerBalance.getPlasticsAcct()) + (bizMaterialTrans.getSlNumber() == null ? 0 : bizMaterialTrans.getSlNumber()));
            //减少买方现金余额
            buyerBalance.setCashAcct((buyerBalance.getCashAcct() == null ? 0 : buyerBalance.getCashAcct()) - (bizMaterialTrans.getTransPrice() == null ? 0 : bizMaterialTrans.getTransPrice()));
            if(sellerBalance.getSteelAcct() < 0 ||  sellerBalance.getSilicaAcct() < 0 || sellerBalance.getCrudeAcct() < 0 || sellerBalance.getPlasticsAcct() < 0 ){
                throw new Exception("卖家材料库存不足");
            }
            if(buyerBalance.getCashAcct() < 0){
                throw new Exception("买家现金余额不足");
            }
            bizSubjectBalanceService.updateById(sellerBalance);
            bizSubjectBalanceService.updateById(buyerBalance);
            bizMaterialTransService.save(bizMaterialTrans);
        }catch (Exception e){
            e.printStackTrace();
            //回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.error("交易失败：" + e.getMessage());
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
    public Result<String> edit(@RequestBody BizMaterialTrans bizMaterialTrans) {
        bizMaterialTransService.updateById(bizMaterialTrans);
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
    public Result<String> delete(@RequestParam(name="id",required=true) String id) {
        try{
            BizMaterialTrans bizMaterialTrans = bizMaterialTransService.getById(id);
            //增加卖方材料库存
            BizSubjectBalance sellerBalance = bizSubjectBalanceService.getByUserId(bizMaterialTrans.getSellerId());
            sellerBalance.setSteelAcct((sellerBalance.getSteelAcct() == null ? 0 : sellerBalance.getSteelAcct()) + (bizMaterialTrans.getGtNumber() == null ? 0 : bizMaterialTrans.getGtNumber()));
            sellerBalance.setSilicaAcct((sellerBalance.getSilicaAcct() == null ? 0 : sellerBalance.getSilicaAcct()) + (bizMaterialTrans.getGsNumber() == null ? 0 : bizMaterialTrans.getGsNumber()));
            sellerBalance.setCrudeAcct((sellerBalance.getCrudeAcct() == null ? 0 : sellerBalance.getCrudeAcct()) + (bizMaterialTrans.getSyNumber() == null ? 0 : bizMaterialTrans.getSyNumber()));
            sellerBalance.setPlasticsAcct((sellerBalance.getPlasticsAcct() == null ? 0 : sellerBalance.getPlasticsAcct()) + (bizMaterialTrans.getSlNumber() == null ? 0 : bizMaterialTrans.getSlNumber()));
            //冲销交易所得税
            SysDepart depart = sysDepartService.queryDepartsByUsername(bizMaterialTrans.getSellerId()).get(0);
            Double taxAmount = bizBankConfigService.offTaxes(bizMaterialTrans.getTransPrice(), bizMaterialTrans.getIsTransnational().equals("Y"), depart.getId());
            //减少卖方现金余额
            sellerBalance.setCashAcct((sellerBalance.getCashAcct() == null ? 0 : sellerBalance.getCashAcct()) - (bizMaterialTrans.getTransPrice() == null ? 0 : bizMaterialTrans.getTransPrice()) + taxAmount);
            //减少买方材料库存
            BizSubjectBalance buyerBalance = bizSubjectBalanceService.getByUserId(bizMaterialTrans.getBuyerId());
            buyerBalance.setSteelAcct((buyerBalance.getSteelAcct() == null ? 0 : buyerBalance.getSteelAcct()) - (bizMaterialTrans.getGtNumber() == null ? 0 : bizMaterialTrans.getGtNumber()));
            buyerBalance.setSilicaAcct((buyerBalance.getSilicaAcct() == null ? 0 : buyerBalance.getSilicaAcct()) - (bizMaterialTrans.getGsNumber() == null ? 0 : bizMaterialTrans.getGsNumber()));
            buyerBalance.setCrudeAcct((buyerBalance.getCrudeAcct() == null ? 0 : buyerBalance.getCrudeAcct()) - (bizMaterialTrans.getSyNumber() == null ? 0 : bizMaterialTrans.getSyNumber()));
            buyerBalance.setPlasticsAcct((buyerBalance.getPlasticsAcct() == null ? 0 : buyerBalance.getPlasticsAcct()) - (bizMaterialTrans.getSlNumber() == null ? 0 : bizMaterialTrans.getSlNumber()));
            //增加买方现金余额
            buyerBalance.setCashAcct((buyerBalance.getCashAcct() == null ? 0 : buyerBalance.getCashAcct()) + (bizMaterialTrans.getTransPrice() == null ? 0 : bizMaterialTrans.getTransPrice()));
            if(buyerBalance.getCashAcct() < 0){
                throw new Exception("卖家现金余额不足");                
            }
            if(sellerBalance.getSteelAcct() < 0 ||  sellerBalance.getSilicaAcct() < 0 || sellerBalance.getCrudeAcct() < 0 || sellerBalance.getPlasticsAcct() < 0){
                throw new Exception("卖家材料库存不足");                
            }
            bizSubjectBalanceService.updateById(sellerBalance);
            bizSubjectBalanceService.updateById(buyerBalance);
            bizMaterialTransService.removeById(id);
        }catch (Exception e){
            e.printStackTrace();
            //回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return  Result.error("撤销失败：" + e.getMessage());
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
    public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
        this.bizMaterialTransService.removeByIds(Arrays.asList(ids.split(",")));
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
    public Result<BizMaterialTrans> queryById(@RequestParam(name="id",required=true) String id) {
        BizMaterialTrans bizMaterialTrans = bizMaterialTransService.getById(id);
        if(bizMaterialTrans==null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(bizMaterialTrans);
    }

    /**
    * 导出excel
    *
    * @param request
    * @param bizMaterialTrans
    */
    //@RequiresPermissions("org.jeecg.modules:biz_material_trans:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, BizMaterialTrans bizMaterialTrans) {
        return super.exportXls(request, bizMaterialTrans, BizMaterialTrans.class, "原材交易");
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
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, BizMaterialTrans.class);
    }

}
