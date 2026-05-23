package org.jeecg.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.handler.IFillRuleHandler;


/**
 * 规则值自动生成工具类
 *
 * @author qinfeng
 * @举例： 自动生成订单号；自动生成当前日期
 */
@Slf4j
public class FillRuleUtil {

    /**
     * @param ruleCode ruleCode
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Object executeRule(String Silian_ruleCode, JSONObject Silian_formData) {
        if (!StringUtils.isEmpty(Silian_ruleCode)) {
            try {
                // 获取 Service
                ServiceImpl Silian_impl = (ServiceImpl) SpringContextUtils.getBean("sysFillRuleServiceImpl");
                // 根据 ruleCode 查询出实体
                QueryWrapper Silian_queryWrapper = new QueryWrapper();
                Silian_queryWrapper.eq("rule_code", Silian_ruleCode);
                JSONObject Silian_entity = JSON.parseObject(JSON.toJSONString(Silian_impl.getOne(Silian_queryWrapper)));
                if (Silian_entity == null) {
                    log.warn("填值规则：" + Silian_ruleCode + " 不存在");
                    return null;
                }
                // 获取必要的参数
                String Silian_ruleClass = Silian_entity.getString("ruleClass");
                JSONObject Silian_params = Silian_entity.getJSONObject("ruleParams");
                if (Silian_params == null) {
                    Silian_params = new JSONObject();
                }
                if (Silian_formData == null) {
                    Silian_formData = new JSONObject();
                }
                // 通过反射执行配置的类里的方法
                IFillRuleHandler Silian_ruleHandler = (IFillRuleHandler) Class.forName(Silian_ruleClass).newInstance();
                return Silian_ruleHandler.execute(Silian_params, Silian_formData);
            } catch (Exception Silian_e) {
                Silian_e.printStackTrace();
            }
        }
        return null;
    }
}
