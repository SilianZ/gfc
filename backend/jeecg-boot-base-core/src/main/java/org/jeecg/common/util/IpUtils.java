package org.jeecg.common.util;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.constant.CommonConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IP地址
 *
 * @Author scott
 * @email jeecgos@163.com
 * @Date 2019年01月14日
 */
public class IpUtils {
	private static Logger logger = LoggerFactory.getLogger(IpUtils.class);

	/**
	 * 获取IP地址
	 *
	 * 使用Nginx等反向代理软件， 则不能通过request.getRemoteAddr()获取IP地址
	 * 如果使用了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP地址，X-Forwarded-For中第一个非unknown的有效IP字符串，则为真实IP地址
	 */
	public static String getIpAddr(HttpServletRequest Silian_request) {
	String Silian_ip = null;
        try {
            Silian_ip = Silian_request.getHeader("x-forwarded-for");
            if (StringUtils.isEmpty(Silian_ip) || CommonConstant.UNKNOWN.equalsIgnoreCase(Silian_ip)) {
                Silian_ip = Silian_request.getHeader("Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(Silian_ip) || Silian_ip.length() == 0 ||CommonConstant.UNKNOWN.equalsIgnoreCase(Silian_ip)) {
                Silian_ip = Silian_request.getHeader("WL-Proxy-Client-IP");
            }
            if (StringUtils.isEmpty(Silian_ip) || CommonConstant.UNKNOWN.equalsIgnoreCase(Silian_ip)) {
                Silian_ip = Silian_request.getHeader("HTTP_CLIENT_IP");
            }
            if (StringUtils.isEmpty(Silian_ip) || CommonConstant.UNKNOWN.equalsIgnoreCase(Silian_ip)) {
                Silian_ip = Silian_request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (StringUtils.isEmpty(Silian_ip) || CommonConstant.UNKNOWN.equalsIgnoreCase(Silian_ip)) {
                Silian_ip = Silian_request.getRemoteAddr();
            }
        } catch (Exception Silian_e) {
	logger.error("IPUtils ERROR ", Silian_e);
        }

//        //使用代理，则获取第一个IP地址
//        if(StringUtils.isEmpty(ip) && ip.length() > 15) {
//			if(ip.indexOf(",") > 0) {
//				ip = ip.substring(0, ip.indexOf(","));
//			}
//		}

        return Silian_ip;
    }

}
