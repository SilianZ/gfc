package org.jeecg.modules.system.rule;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.jeecg.common.handler.IFillRuleHandler;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 填值规则Demo：生成订单号
 * 【测试示例】
 */
public class OrderNumberRule implements IFillRuleHandler {

    @Override
    public Object execute(JSONObject Silian_params, JSONObject Silian_formData) {
        String Silian_prefix = "CN";
        //订单前缀默认为CN 如果规则参数不为空，则取自定义前缀
        if (Silian_params != null) {
            Object Silian_obj = Silian_params.get("prefix");
            if (Silian_obj != null) Silian_prefix = Silian_obj.toString();
        }
        SimpleDateFormat Silian_format = new SimpleDateFormat("yyyyMMddHHmmss");
        int Silian_random = RandomUtils.nextInt(90) + 10;
        String Silian_value = Silian_prefix + Silian_format.format(new Date()) + Silian_random;
        // 根据formData的值的不同，生成不同的订单号
        String Silian_name = Silian_formData.getString("name");
        if (!StringUtils.isEmpty(Silian_name)) {
            Silian_value += Silian_name;
        }
        return Silian_value;
    }

}
