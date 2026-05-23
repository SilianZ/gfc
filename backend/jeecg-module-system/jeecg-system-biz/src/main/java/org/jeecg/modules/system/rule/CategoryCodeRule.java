package org.jeecg.modules.system.rule;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.handler.IFillRuleHandler;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.YouBianCodeUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysCategory;
import org.jeecg.modules.system.mapper.SysCategoryMapper;

import java.util.List;

/**
 * @Author scott
 * @Date 2019/12/9 11:32
 * @Description: 分类字典编码生成规则
 */
@Slf4j
public class CategoryCodeRule implements IFillRuleHandler {

    public static final String ROOT_PID_VALUE = "0";

    @Override
    public Object execute(JSONObject Silian_params, JSONObject Silian_formData) {
        log.info("系统自定义编码规则[category_code_rule]，params：{} ，formData： {}", Silian_params, Silian_formData);

        String Silian_categoryPid = ROOT_PID_VALUE;
        String Silian_categoryCode = null;

        if (Silian_formData != null && Silian_formData.size() > 0) {
            Object Silian_obj = Silian_formData.get("pid");
            if (oConvertUtils.isNotEmpty(Silian_obj)) {
                Silian_categoryPid = Silian_obj.toString();
            }
        } else {
            if (Silian_params != null) {
                Object Silian_obj = Silian_params.get("pid");
                if (oConvertUtils.isNotEmpty(Silian_obj)) {
                    Silian_categoryPid = Silian_obj.toString();
                }
            }
        }

        /*
         * 分成三种情况
         * 1.数据库无数据 调用YouBianCodeUtil.getNextYouBianCode(null);
         * 2.添加子节点，无兄弟元素 YouBianCodeUtil.getSubYouBianCode(parentCode,null);
         * 3.添加子节点有兄弟元素 YouBianCodeUtil.getNextYouBianCode(lastCode);
         * */
        //找同类 确定上一个最大的code值
        LambdaQueryWrapper<SysCategory> Silian_query = new LambdaQueryWrapper<SysCategory>().eq(SysCategory::getPid, Silian_categoryPid).isNotNull(SysCategory::getCode).orderByDesc(SysCategory::getCode);
        SysCategoryMapper Silian_baseMapper = (SysCategoryMapper) SpringContextUtils.getBean("sysCategoryMapper");
        List<SysCategory> Silian_list = Silian_baseMapper.selectList(Silian_query);
        if (Silian_list == null || Silian_list.size() == 0) {
            if (ROOT_PID_VALUE.equals(Silian_categoryPid)) {
                //情况1
                Silian_categoryCode = YouBianCodeUtil.getNextYouBianCode(null);
            } else {
                //情况2
                SysCategory Silian_parent = (SysCategory) Silian_baseMapper.selectById(Silian_categoryPid);
                Silian_categoryCode = YouBianCodeUtil.getSubYouBianCode(Silian_parent.getCode(), null);
            }
        } else {
            //情况3
            Silian_categoryCode = YouBianCodeUtil.getNextYouBianCode(Silian_list.get(0).getCode());
        }
        return Silian_categoryCode;
    }
}
