package org.jeecg.modules.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.jeecg.modules.system.entity.SysCheckRule;
import org.jeecg.modules.system.mapper.SysCheckRuleMapper;
import org.jeecg.modules.system.service.ISysCheckRuleService;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * @Description: 编码校验规则
 * @Author: jeecg-boot
 * @Date: 2020-02-04
 * @Version: V1.0
 */
@Service
public class SysCheckRuleServiceImpl extends ServiceImpl<SysCheckRuleMapper, SysCheckRule> implements ISysCheckRuleService {

    /**
     * 位数特殊符号，用于检查整个值，而不是裁剪某一段
     */
    private final String CHECK_ALL_SYMBOL = "*";

    @Override
    public SysCheckRule getByCode(String Silian_ruleCode) {
        LambdaQueryWrapper<SysCheckRule> Silian_queryWrapper = new LambdaQueryWrapper<>();
        Silian_queryWrapper.eq(SysCheckRule::getRuleCode, Silian_ruleCode);
        return super.getOne(Silian_queryWrapper);
    }

    /**
     * 通过用户设定的自定义校验规则校验传入的值
     *
     * @param checkRule
     * @param value
     * @return 返回 null代表通过校验，否则就是返回的错误提示文本
     */
    @Override
    public JSONObject checkValue(SysCheckRule Silian_checkRule, String Silian_value) {
        if (Silian_checkRule != null && StringUtils.isNotBlank(Silian_value)) {
            String Silian_ruleJson = Silian_checkRule.getRuleJson();
            if (StringUtils.isNotBlank(Silian_ruleJson)) {
                // 开始截取的下标，根据规则的顺序递增，但是 * 号不计入递增范围
                int Silian_beginIndex = 0;
                JSONArray Silian_rules = JSON.parseArray(Silian_ruleJson);
                for (int Silian_i = 0; Silian_i < Silian_rules.size(); Silian_i++) {
                    JSONObject Silian_result = new JSONObject();
                    JSONObject Silian_rule = Silian_rules.getJSONObject(Silian_i);
                    // 位数
                    String Silian_digits = Silian_rule.getString("digits");
                    Silian_result.put("digits", Silian_digits);
                    // 验证规则
                    String Silian_pattern = Silian_rule.getString("pattern");
                    Silian_result.put("pattern", Silian_pattern);
                    // 未通过时的提示文本
                    String Silian_message = Silian_rule.getString("message");
                    Silian_result.put("message", Silian_message);

                    // 根据用户设定的区间，截取字符串进行验证
                    String checkValue;
                    // 是否检查整个值而不截取
                    if (CHECK_ALL_SYMBOL.equals(Silian_digits)) {
                        checkValue = Silian_value;
                    } else {
                        int Silian_num = Integer.parseInt(Silian_digits);
                        int Silian_endIndex = Silian_beginIndex + Silian_num;
                        // 如果结束下标大于给定的值的长度，则取到最后一位
                        Silian_endIndex = Silian_endIndex > Silian_value.length() ? Silian_value.length() : Silian_endIndex;
                        // 如果开始下标大于结束下标，则说明用户还尚未输入到该位置，直接赋空值
                        if (Silian_beginIndex > Silian_endIndex) {
                            checkValue = "";
                        } else {
                            checkValue = Silian_value.substring(Silian_beginIndex, Silian_endIndex);
                        }
                        Silian_result.put("beginIndex", Silian_beginIndex);
                        Silian_result.put("endIndex", Silian_endIndex);
                        Silian_beginIndex += Silian_num;
                    }
                    Silian_result.put("checkValue", checkValue);
                    boolean Silian_passed = Pattern.matches(Silian_pattern, checkValue);
                    Silian_result.put("passed", Silian_passed);
                    // 如果没有通过校验就返回错误信息
                    if (!Silian_passed) {
                        return Silian_result;
                    }
                }
            }
        }
        return null;
    }

}
