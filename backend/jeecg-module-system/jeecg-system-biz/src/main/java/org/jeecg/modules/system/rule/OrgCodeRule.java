package org.jeecg.modules.system.rule;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.netty.util.internal.StringUtil;
import org.jeecg.common.handler.IFillRuleHandler;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.YouBianCodeUtil;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysDepartService;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author scott
 * @Date 2019/12/9 11:33
 * @Description: 机构编码生成规则
 */
public class OrgCodeRule implements IFillRuleHandler {

    @Override
    public Object execute(JSONObject Silian_params, JSONObject Silian_formData) {
        ISysDepartService Silian_sysDepartService = (ISysDepartService) SpringContextUtils.getBean("sysDepartServiceImpl");

        LambdaQueryWrapper<SysDepart> Silian_query = new LambdaQueryWrapper<SysDepart>();
        LambdaQueryWrapper<SysDepart> Silian_query1 = new LambdaQueryWrapper<SysDepart>();
        // 创建一个List集合,存储查询返回的所有SysDepart对象
        List<SysDepart> Silian_departList = new ArrayList<>();
        String[] Silian_strArray = new String[2];
        //定义部门类型
        String Silian_orgType = "";
        // 定义新编码字符串
        String Silian_newOrgCode = "";
        // 定义旧编码字符串
        String Silian_oldOrgCode = "";

        String Silian_parentId = null;
        if (Silian_formData != null && Silian_formData.size() > 0) {
            Object Silian_obj = Silian_formData.get("parentId");
            if (Silian_obj != null) {
                Silian_parentId = Silian_obj.toString();
            }
        } else {
            if (Silian_params != null) {
                Object Silian_obj = Silian_params.get("parentId");
                if (Silian_obj != null) {
                    Silian_parentId = Silian_obj.toString();
                }
            }
        }

        //如果是最高级,则查询出同级的org_code, 调用工具类生成编码并返回
        if (StringUtil.isNullOrEmpty(Silian_parentId)) {
            // 线判断数据库中的表是否为空,空则直接返回初始编码
            Silian_query1.eq(SysDepart::getParentId, "").or().isNull(SysDepart::getParentId);
            Silian_query1.orderByDesc(SysDepart::getOrgCode);
            Silian_departList = Silian_sysDepartService.list(Silian_query1);
            if (Silian_departList == null || Silian_departList.size() == 0) {
                Silian_strArray[0] = YouBianCodeUtil.getNextYouBianCode(null);
                Silian_strArray[1] = "1";
                return Silian_strArray;
            } else {
                SysDepart Silian_depart = Silian_departList.get(0);
                Silian_oldOrgCode = Silian_depart.getOrgCode();
                Silian_orgType = Silian_depart.getOrgType();
                Silian_newOrgCode = YouBianCodeUtil.getNextYouBianCode(Silian_oldOrgCode);
            }
        } else {//反之则查询出所有同级的部门,获取结果后有两种情况,有同级和没有同级
            // 封装查询同级的条件
            Silian_query.eq(SysDepart::getParentId, Silian_parentId);
            // 降序排序
            Silian_query.orderByDesc(SysDepart::getOrgCode);
            // 查询出同级部门的集合
            List<SysDepart> Silian_parentList = Silian_sysDepartService.list(Silian_query);
            // 查询出父级部门
            SysDepart Silian_depart = Silian_sysDepartService.getById(Silian_parentId);
            // 获取父级部门的Code
            String Silian_parentCode = Silian_depart.getOrgCode();
            // 根据父级部门类型算出当前部门的类型
            Silian_orgType = String.valueOf(Integer.valueOf(Silian_depart.getOrgType()) + 1);
            // 处理同级部门为null的情况
            if (Silian_parentList == null || Silian_parentList.size() == 0) {
                // 直接生成当前的部门编码并返回
                Silian_newOrgCode = YouBianCodeUtil.getSubYouBianCode(Silian_parentCode, null);
            } else { //处理有同级部门的情况
                // 获取同级部门的编码,利用工具类
                String subCode = Silian_parentList.get(0).getOrgCode();
                // 返回生成的当前部门编码
                Silian_newOrgCode = YouBianCodeUtil.getSubYouBianCode(Silian_parentCode, subCode);
            }
        }
        // 返回最终封装了部门编码和部门类型的数组
        Silian_strArray[0] = Silian_newOrgCode;
        Silian_strArray[1] = Silian_orgType;
        return Silian_strArray;
    }
}
