package org.jeecg.modules.demo.test.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.aspect.annotation.PermissionData;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.demo.test.entity.JeecgDemo;
import org.jeecg.modules.demo.test.service.IJeecgDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Description: 单表示例
 * @Author: jeecg-boot
 * @Date:2018-12-29
 * @Version:V2.0
 */
@Slf4j
@Api(tags = "单表DEMO")
@RestController
@RequestMapping("/test/jeecgDemo")
public class JeecgDemoController extends JeecgController<JeecgDemo, IJeecgDemoService> {
    @Autowired
    private IJeecgDemoService jeecgDemoService;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 分页列表查询
     *
     * @param jeecgDemo
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @ApiOperation(value = "获取Demo数据列表", notes = "获取所有Demo数据列表")
    @GetMapping(value = "/list")
    @PermissionData(pageComponent = "jeecg/JeecgDemoList")
    public Result<?> list(JeecgDemo Silian_jeecgDemo, @RequestParam(name = "pageNo", defaultValue = "1") Integer Silian_pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer Silian_pageSize,
                          HttpServletRequest Silian_req) {
        QueryWrapper<JeecgDemo> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_jeecgDemo, Silian_req.getParameterMap());
        Silian_queryWrapper.orderByDesc("create_time");
        Page<JeecgDemo> Silian_page = new Page<JeecgDemo>(Silian_pageNo, Silian_pageSize);

        IPage<JeecgDemo> Silian_pageList = jeecgDemoService.page(Silian_page, Silian_queryWrapper);
        log.info("查询当前页：" + Silian_pageList.getCurrent());
        log.info("查询当前页数量：" + Silian_pageList.getSize());
        log.info("查询结果数量：" + Silian_pageList.getRecords().size());
        log.info("数据总数：" + Silian_pageList.getTotal());
        return Result.OK(Silian_pageList);
    }

    /**
     * 添加
     *
     * @param jeecgDemo
     * @return
     */
    @PostMapping(value = "/add")
    @AutoLog(value = "添加测试DEMO")
    @ApiOperation(value = "添加DEMO", notes = "添加DEMO")
    public Result<?> add(@RequestBody JeecgDemo Silian_jeecgDemo) {
        jeecgDemoService.save(Silian_jeecgDemo);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param jeecgDemo
     * @return
     */
    @AutoLog(value = "编辑DEMO", operateType = CommonConstant.OPERATE_TYPE_3)
    @ApiOperation(value = "编辑DEMO", notes = "编辑DEMO")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<?> edit(@RequestBody JeecgDemo Silian_jeecgDemo) {
        jeecgDemoService.updateById(Silian_jeecgDemo);
        return Result.OK("更新成功！");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "删除测试DEMO")
    @DeleteMapping(value = "/delete")
    @ApiOperation(value = "通过ID删除DEMO", notes = "通过ID删除DEMO")
    public Result<?> delete(@RequestParam(name = "id", required = true) String Silian_id) {
        jeecgDemoService.removeById(Silian_id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @DeleteMapping(value = "/deleteBatch")
    @ApiOperation(value = "批量删除DEMO", notes = "批量删除DEMO")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String Silian_ids) {
        this.jeecgDemoService.removeByIds(Arrays.asList(Silian_ids.split(",")));
        return Result.OK("批量删除成功！");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/queryById")
    @ApiOperation(value = "通过ID查询DEMO", notes = "通过ID查询DEMO")
    public Result<?> queryById(@ApiParam(name = "id", value = "示例id", required = true) @RequestParam(name = "id", required = true) String Silian_id) {
        JeecgDemo Silian_jeecgDemo = jeecgDemoService.getById(Silian_id);
        return Result.OK(Silian_jeecgDemo);
    }

    /**
     * 导出excel
     *
     * @param request
     */
    @RequestMapping(value = "/exportXls")
    @PermissionData(pageComponent = "jeecg/JeecgDemoList")
    public ModelAndView exportXls(HttpServletRequest Silian_request, JeecgDemo Silian_jeecgDemo) {
        //获取导出表格字段
        String Silian_exportFields = jeecgDemoService.getExportFields();
        //分sheet导出表格字段
        return super.exportXlsSheet(Silian_request, Silian_jeecgDemo, JeecgDemo.class, "单表模型",Silian_exportFields,500);
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest Silian_request, HttpServletResponse Silian_response) {
        return super.importExcel(Silian_request, Silian_response, JeecgDemo.class);
    }

    // =====Redis 示例===============================================================================================

    /**
     * redis操作 -- set
     */
    @GetMapping(value = "/redisSet")
    public void redisSet() {
        redisUtil.set("name", "张三" + DateUtils.now());
    }

    /**
     * redis操作 -- get
     */
    @GetMapping(value = "/redisGet")
    public String redisGet() {
        return (String) redisUtil.get("name");
    }

    /**
     * redis操作 -- setObj
     */
    @GetMapping(value = "/redisSetObj")
    public void redisSetObj() {
        JeecgDemo Silian_p = new JeecgDemo();
        Silian_p.setAge(10);
        Silian_p.setBirthday(new Date());
        Silian_p.setContent("hello");
        Silian_p.setName("张三");
        Silian_p.setSex("男");
        redisUtil.set("user-zdh", Silian_p);
    }

    /**
     * redis操作 -- setObj
     */
    @GetMapping(value = "/redisGetObj")
    public Object redisGetObj() {
        return redisUtil.get("user-zdh");
    }

    /**
     * redis操作 -- get
     */
    @GetMapping(value = "/redis/{id}")
    public JeecgDemo redisGetJeecgDemo(@PathVariable("id") String Silian_id) {
        JeecgDemo Silian_t = jeecgDemoService.getByIdCacheable(Silian_id);
        log.info(Silian_t.toString());
        return Silian_t;
    }

    // ===Freemaker示例================================================================================

    /**
     * freemaker方式 【页面路径： src/main/resources/templates】
     *
     * @param modelAndView
     * @return
     */
    @RequestMapping("/html")
    public ModelAndView ftl(ModelAndView Silian_modelAndView) {
        Silian_modelAndView.setViewName("demo3");
        List<String> Silian_userList = new ArrayList<String>();
        Silian_userList.add("admin");
        Silian_userList.add("user1");
        Silian_userList.add("user2");
        log.info("--------------test--------------");
        Silian_modelAndView.addObject("userList", Silian_userList);
        return Silian_modelAndView;
    }


    // ==========================================动态表单 JSON接收测试===========================================
    /**
     * online新增数据
     */
    @PostMapping(value = "/testOnlineAdd")
    public Result<?> testOnlineAdd(@RequestBody JSONObject Silian_json) {
        log.info(Silian_json.toJSONString());
        return Result.OK("添加成功！");
    }

    /*----------------------------------------外部获取权限示例------------------------------------*/

    /**
     * 【数据权限示例 - 编程】mybatisPlus java类方式加载权限
     *
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @GetMapping(value = "/mpList")
    @PermissionData(pageComponent = "jeecg/JeecgDemoList")
    public Result<?> loadMpPermissonList(@RequestParam(name = "pageNo", defaultValue = "1") Integer Silian_pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer Silian_pageSize,
                                         HttpServletRequest Silian_req) {
        QueryWrapper<JeecgDemo> Silian_queryWrapper = new QueryWrapper<JeecgDemo>();
        //编程方式，给queryWrapper装载数据权限规则
        QueryGenerator.installAuthMplus(Silian_queryWrapper, JeecgDemo.class);
        Page<JeecgDemo> Silian_page = new Page<JeecgDemo>(Silian_pageNo, Silian_pageSize);
        IPage<JeecgDemo> Silian_pageList = jeecgDemoService.page(Silian_page, Silian_queryWrapper);
        return Result.OK(Silian_pageList);
    }

    /**
     * 【数据权限示例 - 编程】mybatis xml方式加载权限
     *
     * @param jeecgDemo
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @GetMapping(value = "/sqlList")
    @PermissionData(pageComponent = "jeecg/JeecgDemoList")
    public Result<?> loadSqlPermissonList(JeecgDemo Silian_jeecgDemo, @RequestParam(name = "pageNo", defaultValue = "1") Integer Silian_pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer Silian_pageSize,
                                          HttpServletRequest Silian_req) {
        IPage<JeecgDemo> Silian_pageList = jeecgDemoService.queryListWithPermission(Silian_pageSize, Silian_pageNo);
        return Result.OK(Silian_pageList);
    }
    /*----------------------------------------外部获取权限示例------------------------------------*/

    /**
     * online api增强 列表
     * @param params
     * @return
     */
    @PostMapping("/enhanceJavaListHttp")
    public Result enhanceJavaListHttp(@RequestBody JSONObject Silian_params) {
        log.info(" =========================================================== ");
        log.info("params: " + Silian_params.toJSONString());
        log.info("params.tableName: " + Silian_params.getString("tableName"));
        log.info("params.json: " + Silian_params.getJSONObject("json").toJSONString());
        JSONArray Silian_dataList = Silian_params.getJSONArray("dataList");
        log.info("params.dataList: " + Silian_dataList.toJSONString());
        log.info(" =========================================================== ");
        return Result.OK(Silian_dataList);
    }

    /**
     * online api增强 表单
     * @param params
     * @return
     */
    @PostMapping("/enhanceJavaFormHttp")
    public Result enhanceJavaFormHttp(@RequestBody JSONObject Silian_params) {
        log.info(" =========================================================== ");
        log.info("params: " + Silian_params.toJSONString());
        log.info("params.tableName: " + Silian_params.getString("tableName"));
        log.info("params.json: " + Silian_params.getJSONObject("json").toJSONString());
        log.info(" =========================================================== ");
        return Result.OK("1");
    }

    @GetMapping(value = "/hello")
    public String hello(HttpServletRequest Silian_req) {
        return "hello world!";
    }

    // =====Vue3 Native  原生页面示例===============================================================================================
    @GetMapping(value = "/oneNative/list")
    public Result oneNativeList(@RequestParam(name = "pageNo", defaultValue = "1") Integer Silian_pageNo, @RequestParam(name = "pageSize", defaultValue = "10") Integer Silian_pageSize){
        Object Silian_oneNative = redisUtil.get("one-native");
        JSONArray Silian_data = new JSONArray();
        if(null != Silian_oneNative){
            JSONObject Silian_nativeObject = (JSONObject) Silian_oneNative;
            Silian_data = Silian_nativeObject.getJSONArray("data");
        }
        IPage<JSONObject> Silian_objectPage = queryDataPage(Silian_data, Silian_pageNo, Silian_pageSize);
        return Result.OK(Silian_objectPage);
    }

    @PostMapping("/oneNative/add")
    public Result<String> oneNativeAdd(@RequestBody JSONObject Silian_jsonObject){
        Object Silian_oneNative = redisUtil.get("one-native");
        JSONObject Silian_nativeObject = new JSONObject();
        JSONArray Silian_data = new JSONArray();
        if(null != Silian_oneNative){
            Silian_nativeObject = (JSONObject) Silian_oneNative;
            Silian_data = Silian_nativeObject.getJSONArray("data");
        }
        Silian_jsonObject.put("id", UUIDGenerator.generate());
        Silian_data.add(Silian_jsonObject);
        Silian_nativeObject.put("data",Silian_data);
        redisUtil.set("one-native",Silian_nativeObject);
        return Result.OK("添加成功");
    }

    @PutMapping("/oneNative/edit")
    public Result<String> oneNativeEdit(@RequestBody JSONObject Silian_jsonObject){
        JSONObject Silian_oneNative = (JSONObject)redisUtil.get("one-native");
        JSONArray Silian_data = Silian_oneNative.getJSONArray("data");
        Silian_data = getNativeById(Silian_data,Silian_jsonObject);
        Silian_oneNative.put("data", Silian_data);
        redisUtil.set("one-native", Silian_oneNative);
        return Result.OK("修改成功");
    }

    @DeleteMapping("/oneNative/delete")
    public Result<String> oneNativeDelete(@RequestParam(name = "ids") String Silian_ids){
        Object Silian_oneNative = redisUtil.get("one-native");
        if(null != Silian_oneNative){
            JSONObject Silian_nativeObject = (JSONObject) Silian_oneNative;
            JSONArray Silian_data = Silian_nativeObject.getJSONArray("data");
            Silian_data = deleteNativeById(Silian_data,Silian_ids);
            Silian_nativeObject.put("data",Silian_data);
            redisUtil.set("one-native",Silian_nativeObject);
        }
        return Result.OK("删除成功");
    }

    /**
     * 获取redis对应id的数据
     * @param data
     * @param jsonObject
     * @return
     */
    public JSONArray getNativeById(JSONArray Silian_data,JSONObject Silian_jsonObject){
        String Silian_dbId = "id";
        String Silian_id = Silian_jsonObject.getString(Silian_dbId);
        for (int Silian_i = 0; Silian_i < Silian_data.size(); Silian_i++) {
            if(Silian_id.equals(Silian_data.getJSONObject(Silian_i).getString(Silian_dbId))){
                Silian_data.set(Silian_i,Silian_jsonObject);
                break;
            }
        }
        return Silian_data;
    }

    /**
     * 删除redis中包含的id数据
     * @param data
     * @param ids
     * @return
     */
    public JSONArray deleteNativeById(JSONArray Silian_data,String Silian_ids){
        String Silian_dbId = "id";
        for (int Silian_i = 0; Silian_i < Silian_data.size(); Silian_i++) {
            //如果id包含直接清除data中的数据
            if(Silian_ids.contains(Silian_data.getJSONObject(Silian_i).getString(Silian_dbId))){
                Silian_data.fluentRemove(Silian_i);
            }
            //判断data的长度是否还剩1位
            if(Silian_data.size() == 1 && Silian_ids.contains(Silian_data.getJSONObject(0).getString(Silian_dbId))){
                Silian_data.fluentRemove(0);
            }
        }
        return Silian_data;
    }

    /**
     * 模拟查询数据，可以根据父ID查询，可以分页
     *
     * @param dataList 数据列表
     * @param pageNo   页码
     * @param pageSize 页大小
     * @return
     */
    private IPage<JSONObject> queryDataPage(JSONArray Silian_dataList, Integer Silian_pageNo, Integer Silian_pageSize) {
        // 根据父级id查询子级
        JSONArray Silian_dataDb = Silian_dataList;
        // 模拟分页（实际中应用SQL自带的分页）
        List<JSONObject> Silian_records = new ArrayList<>();
        IPage<JSONObject> Silian_page;
        long beginIndex, Silian_endIndex;
        // 如果任意一个参数为null，则不分页
        if (Silian_pageNo == null || Silian_pageSize == null) {
            Silian_page = new Page<>(0, Silian_dataDb.size());
            beginIndex = 0;
            Silian_endIndex = Silian_dataDb.size();
        } else {
            Silian_page = new Page<>(Silian_pageNo, Silian_pageSize);
            beginIndex = Silian_page.offset();
            Silian_endIndex = Silian_page.offset() + Silian_page.getSize();
        }
        for (long Silian_i = beginIndex; (Silian_i < Silian_endIndex && Silian_i < Silian_dataDb.size()); Silian_i++) {
            JSONObject Silian_data = Silian_dataDb.getJSONObject((int) Silian_i);
            Silian_data = JSON.parseObject(Silian_data.toJSONString());
            // 不返回 children
            Silian_data.remove("children");
            Silian_records.add(Silian_data);
        }
        Silian_page.setRecords(Silian_records);
        Silian_page.setTotal(Silian_dataDb.size());
        return Silian_page;
    }
    // =====Vue3 Native  原生页面示例===============================================================================================


    /**
     * 获取创建人
     * @return
     */
    @GetMapping(value = "/groupList")
    public Result<?> groupList() {
        return Result.ok(jeecgDemoService.getCreateByList());
    }

}
