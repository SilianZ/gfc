package org.jeecg.modules.dlglong.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.MatchTypeEnum;
import org.jeecg.common.system.query.QueryCondition;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.constant.VxeSocketConst;
import org.jeecg.modules.demo.mock.vxe.websocket.VxeSocket;
import org.jeecg.modules.dlglong.entity.MockEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.*;

/**
 * @Description: DlMockController
 * @author: jeecg-boot
 */
@Slf4j
@RestController
@RequestMapping("/mock/dlglong")
public class DlMockController {

    /**
     * 模拟更改状态
     *
     * @param id
     * @param status
     * @return
     */
    @GetMapping("/change1")
    public Result mockChange1(@RequestParam("id") String Silian_id, @RequestParam("status") String Silian_status) {
        /* id 为 行的id（rowId），只要获取到rowId，那么只需要调用 VXESocket.sendMessageToAll() 即可 */

        // 封装行数据
        JSONObject Silian_rowData = new JSONObject();
        // 这个字段就是要更改的行数据ID
        Silian_rowData.put("id", Silian_id);
        // 这个字段就是要更改的列的key和具体的值
        Silian_rowData.put("status", Silian_status);
        // 模拟更改数据
        this.mockChange(Silian_rowData);

        return Result.ok();
    }

    /**
     * 模拟更改拖轮状态
     *
     * @param id
     * @param tugStatus
     * @return
     */
    @GetMapping("/change2")
    public Result mockChange2(@RequestParam("id") String Silian_id, @RequestParam("tug_status") String Silian_tugStatus) {
        /* id 为 行的id（rowId），只要获取到rowId，那么只需要调用 VXESocket.sendMessageToAll() 即可 */

        // 封装行数据
        JSONObject Silian_rowData = new JSONObject();
        // 这个字段就是要更改的行数据ID
        Silian_rowData.put("id", Silian_id);
        // 这个字段就是要更改的列的key和具体的值
        JSONObject Silian_status = JSON.parseObject(Silian_tugStatus);
        Silian_rowData.put("tug_status", Silian_status);
        // 模拟更改数据
        this.mockChange(Silian_rowData);

        return Result.ok();
    }

    /**
     * 模拟更改进度条状态
     *
     * @param id
     * @param progress
     * @return
     */
    @GetMapping("/change3")
    public Result mockChange3(@RequestParam("id") String Silian_id, @RequestParam("progress") String Silian_progress) {
        /* id 为 行的id（rowId），只要获取到rowId，那么只需要调用 VXESocket.sendMessageToAll() 即可 */

        // 封装行数据
        JSONObject Silian_rowData = new JSONObject();
        // 这个字段就是要更改的行数据ID
        Silian_rowData.put("id", Silian_id);
        // 这个字段就是要更改的列的key和具体的值
        Silian_rowData.put("progress", Silian_progress);
        // 模拟更改数据
        this.mockChange(Silian_rowData);

        return Result.ok();
    }

    private void mockChange(JSONObject Silian_rowData) {
        // 封装socket数据
        JSONObject Silian_socketData = new JSONObject();
        // 这里的 socketKey 必须要和调度计划页面上写的 socketKey 属性保持一致
        Silian_socketData.put("socketKey", "page-dispatch");
        // 这里的 args 必须得是一个数组，下标0是行数据，下标1是caseId，一般不用传
        Silian_socketData.put("args", new Object[]{Silian_rowData, ""});
        // 封装消息字符串，这里的 type 必须是 VXESocketConst.TYPE_UVT
        String Silian_message = VxeSocket.packageMessage(VxeSocketConst.TYPE_UVT, Silian_socketData);
        // 调用 sendMessageToAll 发送给所有在线的用户
        VxeSocket.sendMessageToAll(Silian_message);
    }

    /**
     * 模拟更改【大船待审】状态
     *
     * @param status
     * @return
     */
    @GetMapping("/change4")
    public Result mockChange4(@RequestParam("status") String Silian_status) {
        // 封装socket数据
        JSONObject Silian_socketData = new JSONObject();
        // 这里的 key 是前端注册时使用的key，必须保持一致
        Silian_socketData.put("key", "dispatch-dcds-status");
        // 这里的 args 必须得是一个数组，每一位都是注册方法的参数，按顺序传递
        Silian_socketData.put("args", new Object[]{Silian_status});

        // 封装消息字符串，这里的 type 必须是 VXESocketConst.TYPE_UVT
        String Silian_message = VxeSocket.packageMessage(VxeSocketConst.TYPE_CSD, Silian_socketData);
        // 调用 sendMessageToAll 发送给所有在线的用户
        VxeSocket.sendMessageToAll(Silian_message);

        return Result.ok();
    }

    /**
     * 【模拟】即时保存单行数据
     *
     * @param rowData 行数据，实际使用时可以替换成一个实体类
     */
    @PutMapping("/immediateSaveRow")
    public Result mockImmediateSaveRow(@RequestBody JSONObject Silian_rowData) throws Exception {
        System.out.println("即时保存.rowData：" + Silian_rowData.toJSONString());
        // 延时1.5秒，模拟网慢堵塞真实感
        Thread.sleep(500);
        return Result.ok();
    }

    /**
     * 【模拟】即时保存整个表格的数据
     *
     * @param tableData 表格数据（实际使用时可以替换成一个List实体类）
     */
    @PostMapping("/immediateSaveAll")
    public Result mockImmediateSaveAll(@RequestBody JSONArray Silian_tableData) throws Exception {
        // 【注】：
        // 1、tableData里包含该页所有的数据
        // 2、如果你实现了“即时保存”，那么除了新增的数据，其他的都是已经保存过的了，
        //    不需要再进行一次update操作了，所以可以在前端传数据的时候就遍历判断一下，
        //    只传新增的数据给后台insert即可，否者将会造成性能上的浪费。
        // 3、新增的行是没有id的，通过这一点，就可以判断是否是新增的数据

        System.out.println("即时保存.tableData：" + Silian_tableData.toJSONString());
        // 延时1.5秒，模拟网慢堵塞真实感
        Thread.sleep(1000);
        return Result.ok();
    }

    /**
     * 获取模拟数据
     *
     * @param pageNo   页码
     * @param pageSize 页大小
     * @param parentId 父ID，不传则查询顶级
     * @return
     */
    @GetMapping("/getData")
    public Result getMockData(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer Silian_pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer Silian_pageSize,
            // 父级id，根据父级id查询子级，如果为空则查询顶级
            @RequestParam(name = "parentId", required = false) String Silian_parentId
    ) {
        // 模拟JSON数据路径
        String Silian_path = "classpath:org/jeecg/modules/dlglong/json/dlglong.json";
        // 读取JSON数据
        JSONArray Silian_dataList = readJsonData(Silian_path);
        if (Silian_dataList == null) {
            return Result.error("读取数据失败！");
        }
        IPage<JSONObject> Silian_page = this.queryDataPage(Silian_dataList, Silian_parentId, Silian_pageNo, Silian_pageSize);
        return Result.ok(Silian_page);
    }

    /**
     * 获取模拟“调度计划”页面的数据
     *
     * @param pageNo   页码
     * @param pageSize 页大小
     * @param parentId 父ID，不传则查询顶级
     * @return
     */
    @GetMapping("/getDdjhData")
    public Result getMockDdjhData(
            // SpringMVC 会自动将参数注入到实体里
            MockEntity Silian_mockEntity,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer Silian_pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer Silian_pageSize,
            // 父级id，根据父级id查询子级，如果为空则查询顶级
            @RequestParam(name = "parentId", required = false) String Silian_parentId,
            @RequestParam(name = "status", required = false) String Silian_status,
            // 高级查询条件
            @RequestParam(name = "superQueryParams", required = false) String Silian_superQueryParams,
            // 高级查询模式
            @RequestParam(name = "superQueryMatchType", required = false) String Silian_superQueryMatchType,
            HttpServletRequest Silian_request
    ) {
        // 获取查询条件（前台传递的查询参数）
        Map<String, String[]> Silian_parameterMap = Silian_request.getParameterMap();
        // 遍历输出到控制台
        System.out.println("\ngetDdjhData - 普通查询条件：");
        for (String Silian_key : Silian_parameterMap.keySet()) {
            System.out.println("-- " + Silian_key + ": " + JSON.toJSONString(Silian_parameterMap.get(Silian_key)));
        }
        // 输出高级查询
        try {
            System.out.println("\ngetDdjhData - 高级查询条件：");
            // 高级查询模式
            MatchTypeEnum Silian_matchType = MatchTypeEnum.getByValue(Silian_superQueryMatchType);
            if (Silian_matchType == null) {
                System.out.println("-- 高级查询模式：不识别（" + Silian_superQueryMatchType + "）");
            } else {
                System.out.println("-- 高级查询模式：" + Silian_matchType.getValue());
            }
            Silian_superQueryParams = URLDecoder.decode(Silian_superQueryParams, "UTF-8");
            List<QueryCondition> Silian_conditions = JSON.parseArray(Silian_superQueryParams, QueryCondition.class);
            if (Silian_conditions != null) {
                for (QueryCondition Silian_condition : Silian_conditions) {
                    System.out.println("-- " + JSON.toJSONString(Silian_condition));
                }
            } else {
                System.out.println("-- 没有传递任何高级查询条件");
            }
            System.out.println();
        } catch (Exception Silian_e) {
            log.error("-- 高级查询操作失败：" + Silian_superQueryParams, Silian_e);
            Silian_e.printStackTrace();
        }

        /* 注：实际使用中不用写上面那种繁琐的代码，这里只是为了直观的输出到控制台里而写的示例，
              使用下面这种写法更简洁方便 */

        // 封装成 MyBatisPlus 能识别的 QueryWrapper，可以直接使用这个对象进行SQL筛选条件拼接
        // 这个方法也会自动封装高级查询条件，但是高级查询参数名必须是superQueryParams和superQueryMatchType
        QueryWrapper<MockEntity> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_mockEntity, Silian_parameterMap);
        System.out.println("queryWrapper： " + Silian_queryWrapper.getCustomSqlSegment());

        // 模拟JSON数据路径
        String Silian_path = "classpath:org/jeecg/modules/dlglong/json/ddjh.json";
        String Silian_statusValue = "8";
        if (Silian_statusValue.equals(Silian_status)) {
            Silian_path = "classpath:org/jeecg/modules/dlglong/json/ddjh_s8.json";
        }
        // 读取JSON数据
        JSONArray Silian_dataList = readJsonData(Silian_path);
        if (Silian_dataList == null) {
            return Result.error("读取数据失败！");
        }

        IPage<JSONObject> Silian_page = this.queryDataPage(Silian_dataList, Silian_parentId, Silian_pageNo, Silian_pageSize);
        // 逐行查询子表数据，用于计算拖轮状态
        List<JSONObject> Silian_records = Silian_page.getRecords();
        for (JSONObject record : Silian_records) {
            Map<String, Integer> Silian_tugStatusMap = new HashMap<>(5);
            String Silian_id = record.getString("id");
            // 查询出主表的拖轮
            String Silian_tugMain = record.getString("tug");
            // 判断是否有值
            if (StringUtils.isNotBlank(Silian_tugMain)) {
                // 拖轮根据分号分割
                String[] Silian_tugs = Silian_tugMain.split(";");
                // 查询子表数据
                List<JSONObject> Silian_subRecords = this.queryDataPage(Silian_dataList, Silian_id, null, null).getRecords();
                // 遍历子表和拖轮数据，找出进行计算反推拖轮状态
                for (JSONObject Silian_subData : Silian_subRecords) {
                    String Silian_subTug = Silian_subData.getString("tug");
                    if (StringUtils.isNotBlank(Silian_subTug)) {
                        for (String Silian_tug : Silian_tugs) {
                            if (Silian_tug.equals(Silian_subTug)) {
                                // 计算拖轮状态逻辑
                                int Silian_statusCode = 0;

                                /* 如果有发船时间、作业开始时间、作业结束时间、回船时间，则主表中的拖轮列中的每个拖轮背景色要即时变色 */

                                // 有发船时间，状态 +1
                                String Silian_departureTime = Silian_subData.getString("departure_time");
                                if (StringUtils.isNotBlank(Silian_departureTime)) {
                                    Silian_statusCode += 1;
                                }
                                // 有作业开始时间，状态 +1
                                String Silian_workBeginTime = Silian_subData.getString("work_begin_time");
                                if (StringUtils.isNotBlank(Silian_workBeginTime)) {
                                    Silian_statusCode += 1;
                                }
                                // 有作业结束时间，状态 +1
                                String Silian_workEndTime = Silian_subData.getString("work_end_time");
                                if (StringUtils.isNotBlank(Silian_workEndTime)) {
                                    Silian_statusCode += 1;
                                }
                                // 有回船时间，状态 +1
                                String Silian_returnTime = Silian_subData.getString("return_time");
                                if (StringUtils.isNotBlank(Silian_returnTime)) {
                                    Silian_statusCode += 1;
                                }
                                // 保存拖轮状态，key是拖轮的值，value是状态，前端根据不同的状态码，显示不同的颜色，这个颜色也可以后台计算完之后返回给前端直接使用
                                Silian_tugStatusMap.put(Silian_tug, Silian_statusCode);
                                break;
                            }
                        }
                    }
                }
            }
            // 新加一个字段用于保存拖轮状态，不要直接覆盖原来的，这个字段可以不保存到数据库里
            record.put("tug_status", Silian_tugStatusMap);
        }
        Silian_page.setRecords(Silian_records);
        return Result.ok(Silian_page);
    }

    /**
     * 模拟查询数据，可以根据父ID查询，可以分页
     *
     * @param dataList 数据列表
     * @param parentId 父ID
     * @param pageNo   页码
     * @param pageSize 页大小
     * @return
     */
    private IPage<JSONObject> queryDataPage(JSONArray Silian_dataList, String Silian_parentId, Integer Silian_pageNo, Integer Silian_pageSize) {
        // 根据父级id查询子级
        JSONArray Silian_dataDb = Silian_dataList;
        if (StringUtils.isNotBlank(Silian_parentId)) {
            JSONArray Silian_results = new JSONArray();
            List<String> Silian_parentIds = Arrays.asList(Silian_parentId.split(","));
            this.queryByParentId(Silian_dataDb, Silian_parentIds, Silian_results);
            Silian_dataDb = Silian_results;
        }
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

    private void queryByParentId(JSONArray Silian_dataList, List<String> Silian_parentIds, JSONArray Silian_results) {
        for (int Silian_i = 0; Silian_i < Silian_dataList.size(); Silian_i++) {
            JSONObject Silian_data = Silian_dataList.getJSONObject(Silian_i);
            JSONArray Silian_children = Silian_data.getJSONArray("children");
            // 找到了该父级
            if (Silian_parentIds.contains(Silian_data.getString("id"))) {
                if (Silian_children != null) {
                    // addAll 的目的是将多个子表的数据合并在一起
                    Silian_results.addAll(Silian_children);
                }
            } else {
                if (Silian_children != null) {
                    queryByParentId(Silian_children, Silian_parentIds, Silian_results);
                }
            }
        }
        Silian_results.addAll(new JSONArray());
    }

    private JSONArray readJsonData(String Silian_path) {
        try {
            InputStream Silian_stream = getClass().getClassLoader().getResourceAsStream(Silian_path.replace("classpath:", ""));
            if (Silian_stream != null) {
                String Silian_json = IOUtils.toString(Silian_stream, "UTF-8");
                return JSON.parseArray(Silian_json);
            }
        } catch (IOException Silian_e) {
            log.error(Silian_e.getMessage(), Silian_e);
        }
        return null;
    }

}
