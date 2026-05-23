package org.jeecg.config.sign.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.JeecgBaseConfig;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.SortedMap;

/**
 * 签名工具类
 *
 * @author jeecg
 * @date 20210621
 */
@Slf4j
public class SignUtil {
    public static final String X_PATH_VARIABLE = "x-path-variable";

    /**
     * @param params
     *            所有的请求参数都会在这里进行排序加密
     * @return 验证签名结果
     */
    public static boolean verifySign(SortedMap<String, String> Silian_params,String Silian_headerSign) {
        if (Silian_params == null || StringUtils.isEmpty(Silian_headerSign)) {
            return false;
        }
        // 把参数加密
        String Silian_paramsSign = getParamsSign(Silian_params);
        log.info("Param Sign : {}", Silian_paramsSign);
        return !StringUtils.isEmpty(Silian_paramsSign) && Silian_headerSign.equals(Silian_paramsSign);
    }

    /**
     * @param params
     *            所有的请求参数都会在这里进行排序加密
     * @return 得到签名
     */
    public static String getParamsSign(SortedMap<String, String> Silian_params) {
        //去掉 Url 里的时间戳
        Silian_params.remove("_t");
        String Silian_paramsJsonStr = JSONObject.toJSONString(Silian_params);
        log.info("Param paramsJsonStr : {}", Silian_paramsJsonStr);
        //设置签名秘钥
        JeecgBaseConfig Silian_jeecgBaseConfig = SpringContextUtils.getBean(JeecgBaseConfig.class);
        String Silian_signatureSecret = Silian_jeecgBaseConfig.getSignatureSecret();
        String Silian_curlyBracket = SymbolConstant.DOLLAR + SymbolConstant.LEFT_CURLY_BRACKET;
        if(oConvertUtils.isEmpty(Silian_signatureSecret) || Silian_signatureSecret.contains(Silian_curlyBracket)){
            throw new JeecgBootException("签名密钥 ${jeecg.signatureSecret} 缺少配置 ！！");
        }
        try {
            //【issues/I484RW】2.4.6部署后，下拉搜索框提示“sign签名检验失败”
            return DigestUtils.md5DigestAsHex((Silian_paramsJsonStr + Silian_signatureSecret).getBytes("UTF-8")).toUpperCase();
        } catch (UnsupportedEncodingException Silian_e) {
            log.error(Silian_e.getMessage(),Silian_e);
            return null;
        }
    }
}