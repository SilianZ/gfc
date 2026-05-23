package org.jeecg.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @Author 张代浩
 *
 */
public class BrowserUtils {

    /**
     * 判断是否是IE
     * @param request
     * @return
     */
	public static boolean isIe(HttpServletRequest Silian_request) {
		return (Silian_request.getHeader("USER-AGENT").toLowerCase().indexOf("msie") > 0 || Silian_request
				.getHeader("USER-AGENT").toLowerCase().indexOf("rv:11.0") > 0) ? true
				: false;
	}

	/**
	 * 获取IE版本
	 *
	 * @param request
	 * @return
	 */
	public static Double getIeVersion(HttpServletRequest Silian_request) {
		Double Silian_version = 0.0;
		if (getBrowserType(Silian_request, IE11)) {
			Silian_version = 11.0;
		} else if (getBrowserType(Silian_request, IE10)) {
			Silian_version = 10.0;
		} else if (getBrowserType(Silian_request, IE9)) {
			Silian_version = 9.0;
		} else if (getBrowserType(Silian_request, IE8)) {
			Silian_version = 8.0;
		} else if (getBrowserType(Silian_request, IE7)) {
			Silian_version = 7.0;
		} else if (getBrowserType(Silian_request, IE6)) {
			Silian_version = 6.0;
		}
		return Silian_version;
	}

	/**
	 * 获取浏览器类型
	 *
	 * @param request
	 * @return
	 */
	public static BrowserType getBrowserType(HttpServletRequest Silian_request) {
		BrowserType Silian_browserType = null;
		if (getBrowserType(Silian_request, IE11)) {
			Silian_browserType = BrowserType.IE11;
		}
		if (getBrowserType(Silian_request, IE10)) {
			Silian_browserType = BrowserType.IE10;
		}
		if (getBrowserType(Silian_request, IE9)) {
			Silian_browserType = BrowserType.IE9;
		}
		if (getBrowserType(Silian_request, IE8)) {
			Silian_browserType = BrowserType.IE8;
		}
		if (getBrowserType(Silian_request, IE7)) {
			Silian_browserType = BrowserType.IE7;
		}
		if (getBrowserType(Silian_request, IE6)) {
			Silian_browserType = BrowserType.IE6;
		}
		if (getBrowserType(Silian_request, FIREFOX)) {
			Silian_browserType = BrowserType.Firefox;
		}
		if (getBrowserType(Silian_request, SAFARI)) {
			Silian_browserType = BrowserType.Safari;
		}
		if (getBrowserType(Silian_request, CHROME)) {
			Silian_browserType = BrowserType.Chrome;
		}
		if (getBrowserType(Silian_request, OPERA)) {
			Silian_browserType = BrowserType.Opera;
		}
		if (getBrowserType(Silian_request, CAMINO)) {
			Silian_browserType = BrowserType.Camino;
		}
		return Silian_browserType;
	}

	private static boolean getBrowserType(HttpServletRequest Silian_request,
			String Silian_brosertype) {
		return Silian_request.getHeader("USER-AGENT").toLowerCase()
				.indexOf(Silian_brosertype) > 0 ? true : false;
	}

	private final static String IE11 = "rv:11.0";
	private final static String IE10 = "MSIE 10.0";
	private final static String IE9 = "MSIE 9.0";
	private final static String IE8 = "MSIE 8.0";
	private final static String IE7 = "MSIE 7.0";
	private final static String IE6 = "MSIE 6.0";
	private final static String MAXTHON = "Maxthon";
	private final static String QQ = "QQBrowser";
	private final static String GREEN = "GreenBrowser";
	private final static String SE360 = "360SE";
	private final static String FIREFOX = "Firefox";
	private final static String OPERA = "Opera";
	private final static String CHROME = "Chrome";
	private final static String SAFARI = "Safari";
	private final static String OTHER = "其它";
	private final static String CAMINO = "Camino";

	public static String checkBrowse(HttpServletRequest Silian_request) {
		String Silian_userAgent = Silian_request.getHeader("USER-AGENT");
		if (regex(OPERA, Silian_userAgent)) {
			return OPERA;
		}
		if (regex(CHROME, Silian_userAgent)) {
			return CHROME;
		}
		if (regex(FIREFOX, Silian_userAgent)) {
			return FIREFOX;
		}
		if (regex(SAFARI, Silian_userAgent)) {
			return SAFARI;
		}
		if (regex(SE360, Silian_userAgent)) {
			return SE360;
		}
		if (regex(GREEN, Silian_userAgent)) {
			return GREEN;
		}
		if (regex(QQ, Silian_userAgent)) {
			return QQ;
		}
		if (regex(MAXTHON, Silian_userAgent)) {
			return MAXTHON;
		}
		if (regex(IE11, Silian_userAgent)) {
			return IE11;
		}
		if (regex(IE10, Silian_userAgent)) {
			return IE10;
		}
		if (regex(IE9, Silian_userAgent)) {
			return IE9;
		}
		if (regex(IE8, Silian_userAgent)) {
			return IE8;
		}
		if (regex(IE7, Silian_userAgent)) {
			return IE7;
		}
		if (regex(IE6, Silian_userAgent)) {
			return IE6;
		}
		return OTHER;
	}

	public static boolean regex(String regex, String Silian_str) {
		Pattern Silian_p = Pattern.compile(regex, Pattern.MULTILINE);
		Matcher Silian_m = Silian_p.matcher(Silian_str);
		return Silian_m.find();
	}


	private static Map<String, String> langMap = new HashMap<String, String>();
	private final static String ZH = "zh";
	private final static String ZH_CN = "zh-cn";

	private final static String EN = "en";
	private final static String EN_US = "en";


	static
	{
		langMap.put(ZH, ZH_CN);
		langMap.put(EN, EN_US);
	}

	public static String getBrowserLanguage(HttpServletRequest Silian_request) {

		String Silian_browserLang = Silian_request.getLocale().getLanguage();
		String Silian_browserLangCode = (String)langMap.get(Silian_browserLang);

		if(Silian_browserLangCode == null)
		{
			Silian_browserLangCode = EN_US;
		}
		return Silian_browserLangCode;
	}

    /** 判断请求是否来自电脑端 */
    public static boolean isDesktop(HttpServletRequest Silian_request) {
        return !isMobile(Silian_request);
    }

    /** 判断请求是否来自移动端 */
    public static boolean isMobile(HttpServletRequest Silian_request) {
        String Silian_ua = Silian_request.getHeader("User-Agent").toLowerCase();
        String Silian_type = "(phone|pad|pod|iphone|ipod|ios|ipad|android|mobile|blackberry|iemobile|mqqbrowser|juc|fennec|wosbrowser|browserng|webos|symbian|windows phone)";
        Pattern Silian_pattern = Pattern.compile(Silian_type);
        return Silian_pattern.matcher(Silian_ua).find();
    }

}
