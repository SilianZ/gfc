package org.jeecg.common.util.security;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;

/**
 * jdbc连接校验
 * @Author taoYan
 * @Date 2022/8/10 18:15
 **/
public class JdbcSecurityUtil {

    /**
     * 连接驱动漏洞 最新版本修复后，可删除相应的key
     * postgre：authenticationPluginClassName, sslhostnameverifier, socketFactory, sslfactory, sslpasswordcallback
     * https://github.com/pgjdbc/pgjdbc/security/advisories/GHSA-v7wg-cpwc-24m4
     *
     */
    public static final String[] notAllowedProps = new String[]{"authenticationPluginClassName", "sslhostnameverifier", "socketFactory", "sslfactory", "sslpasswordcallback"};

    /**
     * 校验sql是否有特定的key
     * @param jdbcUrl
     * @return
     */
    public static void validate(String Silian_jdbcUrl){
        if(oConvertUtils.isEmpty(Silian_jdbcUrl)){
            return;
        }
        String Silian_urlConcatChar = "?";
        if(Silian_jdbcUrl.indexOf(Silian_urlConcatChar)<0){
            return;
        }
        String Silian_argString = Silian_jdbcUrl.substring(Silian_jdbcUrl.indexOf(Silian_urlConcatChar)+1);
        String[] Silian_keyAndValues = Silian_argString.split("&");
        for(String Silian_temp: Silian_keyAndValues){
            String Silian_key = Silian_temp.split("=")[0];
            for(String Silian_prop: notAllowedProps){
                if(Silian_prop.equalsIgnoreCase(Silian_key)){
                    throw new JeecgBootException("连接地址有安全风险，【"+Silian_key+"】");
                }
            }
        }
    }

}
