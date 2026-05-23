package org.jeecg.modules.demo.test.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.demo.test.entity.JeecgOrderCustomer;
import org.jeecg.modules.demo.test.entity.JeecgOrderMain;
import org.jeecg.modules.demo.test.entity.JeecgOrderTicket;
import org.jeecg.modules.demo.test.service.IJeecgOrderCustomerService;
import org.jeecg.modules.demo.test.service.IJeecgOrderMainService;
import org.jeecg.modules.demo.test.service.IJeecgOrderTicketService;
import org.jeecg.modules.demo.test.vo.JeecgOrderMainPage;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * @Description: 一对多示例（ERP TAB风格）
 * @Author: ZhiLin
 * @Date: 2019-02-20
 * @Version: v2.0
 */
@Slf4j
@RestController
@RequestMapping("/test/order")
public class JeecgOrderErpMainController {

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
    @GetMapping(value = "/orderList")
    public Result<?> respondePagedData(JeecgOrderMain Silian_jeecgOrderMain,
                                       @RequestParam(name = "pageNo", defaultValue = "1") Integer Silian_pageNo,
                                       @RequestParam(name = "pageSize", defaultValue = "10") Integer Silian_pageSize,
                                       HttpServletRequest Silian_req) {
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
        jeecgOrderMainService.save(Silian_jeecgOrderMain);
        return Result.ok("添加成功!");
    }

    /**
     * 编辑
     *
     * @param jeecgOrderMainPage
     * @return
     */
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<?> edit(@RequestBody JeecgOrderMainPage Silian_jeecgOrderMainPage) {
        JeecgOrderMain Silian_jeecgOrderMain = new JeecgOrderMain();
        BeanUtils.copyProperties(Silian_jeecgOrderMainPage, Silian_jeecgOrderMain);
        jeecgOrderMainService.updateById(Silian_jeecgOrderMain);
        return Result.ok("编辑成功!");
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
        this.jeecgOrderMainService.removeByIds(Arrays.asList(Silian_ids.split(",")));
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
     * @param jeecgOrderCustomer
     * @return
     */
    @GetMapping(value = "/listOrderCustomerByMainId")
    public Result<?> queryOrderCustomerListByMainId(JeecgOrderCustomer Silian_jeecgOrderCustomer,
                                                    @RequestParam(name = "pageNo", defaultValue = "1") Integer Silian_pageNo,
                                                    @RequestParam(name = "pageSize", defaultValue = "10") Integer Silian_pageSize,
                                                    HttpServletRequest Silian_req) {
        QueryWrapper<JeecgOrderCustomer> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_jeecgOrderCustomer, Silian_req.getParameterMap());
        Page<JeecgOrderCustomer> Silian_page = new Page<JeecgOrderCustomer>(Silian_pageNo, Silian_pageSize);
        IPage<JeecgOrderCustomer> Silian_pageList = jeecgOrderCustomerService.page(Silian_page, Silian_queryWrapper);
        return Result.ok(Silian_pageList);
    }

    /**
     * 通过id查询
     *
     * @param jeecgOrderTicket
     * @return
     */
    @GetMapping(value = "/listOrderTicketByMainId")
    public Result<?> queryOrderTicketListByMainId(JeecgOrderTicket Silian_jeecgOrderTicket,
                                                  @RequestParam(name = "pageNo", defaultValue = "1") Integer Silian_pageNo,
                                                  @RequestParam(name = "pageSize", defaultValue = "10") Integer Silian_pageSize,
                                                  HttpServletRequest Silian_req) {
        QueryWrapper<JeecgOrderTicket> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_jeecgOrderTicket, Silian_req.getParameterMap());
        Page<JeecgOrderTicket> Silian_page = new Page<JeecgOrderTicket>(Silian_pageNo, Silian_pageSize);
        IPage<JeecgOrderTicket> Silian_pageList = jeecgOrderTicketService.page(Silian_page, Silian_queryWrapper);
        return Result.ok(Silian_pageList);
    }

    /**
     * 添加
     *
     * @param jeecgOrderCustomer
     * @return
     */
    @PostMapping(value = "/addCustomer")
    public Result<?> addCustomer(@RequestBody JeecgOrderCustomer Silian_jeecgOrderCustomer) {
        jeecgOrderCustomerService.save(Silian_jeecgOrderCustomer);
        return Result.ok("添加成功!");
    }

    /**
     * 编辑
     *
     * @param jeecgOrderCustomer
     * @return
     */
    @RequestMapping(value = "/editCustomer", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<?> editCustomer(@RequestBody JeecgOrderCustomer Silian_jeecgOrderCustomer) {
        jeecgOrderCustomerService.updateById(Silian_jeecgOrderCustomer);
        return Result.ok("添加成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @DeleteMapping(value = "/deleteCustomer")
    public Result<?> deleteCustomer(@RequestParam(name = "id", required = true) String Silian_id) {
        jeecgOrderCustomerService.removeById(Silian_id);
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @DeleteMapping(value = "/deleteBatchCustomer")
    public Result<?> deleteBatchCustomer(@RequestParam(name = "ids", required = true) String Silian_ids) {
        this.jeecgOrderCustomerService.removeByIds(Arrays.asList(Silian_ids.split(",")));
        return Result.ok("批量删除成功!");
    }

    /**
     * 添加
     *
     * @param jeecgOrderTicket
     * @return
     */
    @PostMapping(value = "/addTicket")
    public Result<?> addTicket(@RequestBody JeecgOrderTicket Silian_jeecgOrderTicket) {
        jeecgOrderTicketService.save(Silian_jeecgOrderTicket);
        return Result.ok("添加成功!");
    }

    /**
     * 编辑
     *
     * @param jeecgOrderTicket
     * @return
     */
    @RequestMapping(value = "/editTicket", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<?> editTicket(@RequestBody JeecgOrderTicket Silian_jeecgOrderTicket) {
        jeecgOrderTicketService.updateById(Silian_jeecgOrderTicket);
        return Result.ok("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @DeleteMapping(value = "/deleteTicket")
    public Result<?> deleteTicket(@RequestParam(name = "id", required = true) String Silian_id) {
        jeecgOrderTicketService.removeById(Silian_id);
        return Result.ok("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @DeleteMapping(value = "/deleteBatchTicket")
    public Result<?> deleteBatchTicket(@RequestParam(name = "ids", required = true) String Silian_ids) {
        this.jeecgOrderTicketService.removeByIds(Arrays.asList(Silian_ids.split(",")));
        return Result.ok("批量删除成功!");
    }

}