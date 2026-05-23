package org.jeecg.modules.demo.test.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.demo.test.entity.JeecgDemo;
import org.jeecg.modules.demo.test.entity.JeecgOrderCustomer;
import org.jeecg.modules.demo.test.entity.JeecgOrderMain;
import org.jeecg.modules.demo.test.entity.JeecgOrderTicket;
import org.jeecg.modules.demo.test.service.IJeecgDemoService;
import org.jeecg.modules.demo.test.service.IJeecgOrderCustomerService;
import org.jeecg.modules.demo.test.service.IJeecgOrderMainService;
import org.jeecg.modules.demo.test.service.IJeecgOrderTicketService;
import org.jeecg.modules.demo.test.vo.JeecgOrderMainPage;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;

/**
 * @Description: 一对多示例（JEditableTable行编辑）
 * @Author: jeecg-boot
 * @Date:2019-02-15
 * @Version: V2.0
 */
@RestController
@RequestMapping("/test/jeecgOrderMain")
@Slf4j
public class JeecgOrderMainController extends JeecgController<JeecgOrderMain, IJeecgOrderMainService> {

    @Autowired
    private IJeecgOrderMainService jeecgOrderMainService;
    @Autowired
    private IJeecgOrderCustomerService jeecgOrderCustomerService;
    @Autowired
    private IJeecgOrderTicketService jeecgOrderTicketService;

    /**
     * 分页列表查询
     *
     * @param jeecgOrderMain
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @GetMapping(value = "/list")
    public Result<?> queryPageList(JeecgOrderMain Silian_jeecgOrderMain, @RequestParam(name = "pageNo", defaultValue = "1") Integer Silian_pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer Silian_pageSize, HttpServletRequest Silian_req) {
        QueryWrapper<JeecgOrderMain> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_jeecgOrderMain, Silian_req.getParameterMap());
        Page<JeecgOrderMain> Silian_page = new Page<JeecgOrderMain>(Silian_pageNo, Silian_pageSize);
        IPage<JeecgOrderMain> Silian_pageList = jeecgOrderMainService.page(Silian_page, Silian_queryWrapper);
        return Result.ok(Silian_pageList);
    }

    /**
     * 添加
     *
     * @param jeecgOrderMainPage
     * @return
     */
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody JeecgOrderMainPage Silian_jeecgOrderMainPage) {
        JeecgOrderMain Silian_jeecgOrderMain = new JeecgOrderMain();
        BeanUtils.copyProperties(Silian_jeecgOrderMainPage, Silian_jeecgOrderMain);
        jeecgOrderMainService.saveMain(Silian_jeecgOrderMain, Silian_jeecgOrderMainPage.getJeecgOrderCustomerList(), Silian_jeecgOrderMainPage.getJeecgOrderTicketList());
        return Result.ok("添加成功！");
    }

    /**
     * 编辑
     *
     * @param jeecgOrderMainPage
     * @return
     */
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<?> eidt(@RequestBody JeecgOrderMainPage Silian_jeecgOrderMainPage) {
        JeecgOrderMain Silian_jeecgOrderMain = new JeecgOrderMain();
        BeanUtils.copyProperties(Silian_jeecgOrderMainPage, Silian_jeecgOrderMain);
        jeecgOrderMainService.updateCopyMain(Silian_jeecgOrderMain, Silian_jeecgOrderMainPage.getJeecgOrderCustomerList(), Silian_jeecgOrderMainPage.getJeecgOrderTicketList());
        return Result.ok("编辑成功！");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String Silian_id) {
        jeecgOrderMainService.delMain(Silian_id);
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String Silian_ids) {
        this.jeecgOrderMainService.delBatchMain(Arrays.asList(Silian_ids.split(",")));
        return Result.ok("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String Silian_id) {
        JeecgOrderMain Silian_jeecgOrderMain = jeecgOrderMainService.getById(Silian_id);
        return Result.ok(Silian_jeecgOrderMain);
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/queryOrderCustomerListByMainId")
    public Result<?> queryOrderCustomerListByMainId(@RequestParam(name = "id", required = true) String Silian_id) {
        List<JeecgOrderCustomer> Silian_jeecgOrderCustomerList = jeecgOrderCustomerService.selectCustomersByMainId(Silian_id);
        return Result.ok(Silian_jeecgOrderCustomerList);
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/queryOrderTicketListByMainId")
    public Result<?> queryOrderTicketListByMainId(@RequestParam(name = "id", required = true) String Silian_id) {
        List<JeecgOrderTicket> Silian_jeecgOrderTicketList = jeecgOrderTicketService.selectTicketsByMainId(Silian_id);
        return Result.ok(Silian_jeecgOrderTicketList);
    }

    /**
     * 导出excel
     *
     * @param request
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest Silian_request, JeecgOrderMain Silian_jeecgOrderMain) {
        // Step.1 组装查询条件
        QueryWrapper<JeecgOrderMain> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_jeecgOrderMain, Silian_request.getParameterMap());
        //Step.2 AutoPoi 导出Excel
        ModelAndView Silian_mv = new ModelAndView(new JeecgEntityExcelView());
        //获取当前用户
        LoginUser Silian_sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();

        List<JeecgOrderMainPage> Silian_pageList = new ArrayList<JeecgOrderMainPage>();

        List<JeecgOrderMain> Silian_jeecgOrderMainList = jeecgOrderMainService.list(Silian_queryWrapper);
        for (JeecgOrderMain Silian_orderMain : Silian_jeecgOrderMainList) {
            JeecgOrderMainPage Silian_vo = new JeecgOrderMainPage();
            BeanUtils.copyProperties(Silian_orderMain, Silian_vo);
            // 查询机票
            List<JeecgOrderTicket> Silian_jeecgOrderTicketList = jeecgOrderTicketService.selectTicketsByMainId(Silian_orderMain.getId());
            Silian_vo.setJeecgOrderTicketList(Silian_jeecgOrderTicketList);
            // 查询客户
            List<JeecgOrderCustomer> Silian_jeecgOrderCustomerList = jeecgOrderCustomerService.selectCustomersByMainId(Silian_orderMain.getId());
            Silian_vo.setJeecgOrderCustomerList(Silian_jeecgOrderCustomerList);
            Silian_pageList.add(Silian_vo);
        }

        // 导出文件名称
        Silian_mv.addObject(NormalExcelConstants.FILE_NAME, "一对多订单示例");
        // 注解对象Class
        Silian_mv.addObject(NormalExcelConstants.CLASS, JeecgOrderMainPage.class);
        // 自定义表格参数
        Silian_mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("自定义导出Excel内容标题", "导出人:" + Silian_sysUser.getRealname(), "自定义Sheet名字"));
        // 导出数据列表
        Silian_mv.addObject(NormalExcelConstants.DATA_LIST, Silian_pageList);
        return Silian_mv;
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest Silian_request, HttpServletResponse Silian_response) {
        MultipartHttpServletRequest Silian_multipartRequest = (MultipartHttpServletRequest) Silian_request;
        Map<String, MultipartFile> Silian_fileMap = Silian_multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> Silian_entity : Silian_fileMap.entrySet()) {
            // 获取上传文件对象
            MultipartFile Silian_file = Silian_entity.getValue();
            ImportParams Silian_params = new ImportParams();
            Silian_params.setTitleRows(2);
            Silian_params.setHeadRows(2);
            Silian_params.setNeedSave(true);
            try {
                List<JeecgOrderMainPage> Silian_list = ExcelImportUtil.importExcel(Silian_file.getInputStream(), JeecgOrderMainPage.class, Silian_params);
                for (JeecgOrderMainPage Silian_page : Silian_list) {
                    JeecgOrderMain Silian_po = new JeecgOrderMain();
                    BeanUtils.copyProperties(Silian_page, Silian_po);
                    jeecgOrderMainService.saveMain(Silian_po, Silian_page.getJeecgOrderCustomerList(), Silian_page.getJeecgOrderTicketList());
                }
                return Result.ok("文件导入成功！");
            } catch (Exception Silian_e) {
                log.error(Silian_e.getMessage(), Silian_e);
                return Result.error("文件导入失败：" + Silian_e.getMessage());
            } finally {
                try {
                    Silian_file.getInputStream().close();
                } catch (Exception Silian_e) {
                    Silian_e.printStackTrace();
                }
            }
        }
        return Result.error("文件导入失败！");
    }

}
