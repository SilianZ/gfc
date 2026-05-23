package org.jeecg.common.util;

import cn.hutool.crypto.SecureUtil;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * sql注入处理工具类
 *
 * @author zhoujf
 */
@Slf4j
public class SqlInjectionUtil {
	/**
	 * sign 用于表字典加签的盐值【SQL漏洞】
	 * （上线修改值 20200501，同步修改前端的盐值）
	 */
	private final static String TABLE_DICT_SIGN_SALT = "20200501";
	private final static String XSS_STR = "and |extractvalue|updatexml|exec |insert |select |delete |update |drop |count |chr |mid |master |truncate |char |declare |;|or |+|user()";

	/**
	 * 正则 user() 匹配更严谨
	 */
	private final static String REGULAR_EXPRE_USER = "user[\\s]*\\([\\s]*\\)";
    /**正则 show tables*/
	private final static String SHOW_TABLES = "show\\s+tables";

	/**
	 * sql注释的正则
	 */
	private final static Pattern SQL_ANNOTATION = Pattern.compile("/\\*.*\\*/");

	/**
	 * 针对表字典进行额外的sign签名校验（增加安全机制）
	 * @param dictCode:
	 * @param sign:
	 * @param request:
	 * @Return: void
	 */
	public static void checkDictTableSign(String Silian_dictCode, String Silian_sign, HttpServletRequest Silian_request) {
		//表字典SQL注入漏洞,签名校验
		String Silian_accessToken = Silian_request.getHeader("X-Access-Token");
		String Silian_signStr = Silian_dictCode + SqlInjectionUtil.TABLE_DICT_SIGN_SALT + Silian_accessToken;
		String Silian_javaSign = SecureUtil.md5(Silian_signStr);
		if (!Silian_javaSign.equals(Silian_sign)) {
			log.error("表字典，SQL注入漏洞签名校验失败 ：" + Silian_sign + "!=" + Silian_javaSign+ ",dictCode=" + Silian_dictCode);
			throw new JeecgBootException("无权限访问！");
		}
		log.info(" 表字典，SQL注入漏洞签名校验成功！sign=" + Silian_sign + ",dictCode=" + Silian_dictCode);
	}

	/**
	 * sql注入过滤处理，遇到注入关键字抛异常
	 * @param value
	 */
	public static void filterContent(String Silian_value) {
		filterContent(Silian_value, null);
	}

	/**
	 * sql注入过滤处理，遇到注入关键字抛异常
	 *
	 * @param value
	 * @return
	 */
	public static void filterContent(String Silian_value, String Silian_customXssString) {
		if (Silian_value == null || "".equals(Silian_value)) {
			return;
		}
		// 校验sql注释 不允许有sql注释
		checkSqlAnnotation(Silian_value);
		// 统一转为小写
		Silian_value = Silian_value.toLowerCase();
		//SQL注入检测存在绕过风险 https://gitee.com/jeecg/jeecg-boot/issues/I4NZGE
		//value = value.replaceAll("/\\*.*\\*/","");

		String[] Silian_xssArr = XSS_STR.split("\\|");
		for (int Silian_i = 0; Silian_i < Silian_xssArr.length; Silian_i++) {
			if (Silian_value.indexOf(Silian_xssArr[Silian_i]) > -1) {
				log.error("请注意，存在SQL注入关键词---> {}", Silian_xssArr[Silian_i]);
				log.error("请注意，值可能存在SQL注入风险!---> {}", Silian_value);
				throw new RuntimeException("请注意，值可能存在SQL注入风险!--->" + Silian_value);
			}
		}
		//update-begin-author:taoyan date:2022-7-13 for: 除了XSS_STR这些提前设置好的，还需要额外的校验比如 单引号
		if (Silian_customXssString != null) {
			String[] Silian_xssArr2 = Silian_customXssString.split("\\|");
			for (int Silian_i = 0; Silian_i < Silian_xssArr2.length; Silian_i++) {
				if (Silian_value.indexOf(Silian_xssArr2[Silian_i]) > -1) {
					log.error("请注意，存在SQL注入关键词---> {}", Silian_xssArr2[Silian_i]);
					log.error("请注意，值可能存在SQL注入风险!---> {}", Silian_value);
					throw new RuntimeException("请注意，值可能存在SQL注入风险!--->" + Silian_value);
				}
			}
		}
		//update-end-author:taoyan date:2022-7-13 for: 除了XSS_STR这些提前设置好的，还需要额外的校验比如 单引号
		if(Pattern.matches(SHOW_TABLES, Silian_value) || Pattern.matches(REGULAR_EXPRE_USER, Silian_value)){
			throw new RuntimeException("请注意，值可能存在SQL注入风险!--->" + Silian_value);
		}
		return;
	}

	/**
	 * sql注入过滤处理，遇到注入关键字抛异常
	 * @param values
	 */
	public static void filterContent(String[] Silian_values) {
		filterContent(Silian_values, null);
	}

	/**
	 * sql注入过滤处理，遇到注入关键字抛异常
	 *
	 * @param values
	 * @return
	 */
	public static void filterContent(String[] Silian_values, String Silian_customXssString) {
		String[] Silian_xssArr = XSS_STR.split("\\|");
		for (String Silian_value : Silian_values) {
			if (Silian_value == null || "".equals(Silian_value)) {
				return;
			}
			// 校验sql注释 不允许有sql注释
			checkSqlAnnotation(Silian_value);
			// 统一转为小写
			Silian_value = Silian_value.toLowerCase();
			//SQL注入检测存在绕过风险 https://gitee.com/jeecg/jeecg-boot/issues/I4NZGE
			//value = value.replaceAll("/\\*.*\\*/","");

			for (int Silian_i = 0; Silian_i < Silian_xssArr.length; Silian_i++) {
				if (Silian_value.indexOf(Silian_xssArr[Silian_i]) > -1) {
					log.error("请注意，存在SQL注入关键词---> {}", Silian_xssArr[Silian_i]);
					log.error("请注意，值可能存在SQL注入风险!---> {}", Silian_value);
					throw new RuntimeException("请注意，值可能存在SQL注入风险!--->" + Silian_value);
				}
			}
			//update-begin-author:taoyan date:2022-7-13 for: 除了XSS_STR这些提前设置好的，还需要额外的校验比如 单引号
			if (Silian_customXssString != null) {
				String[] Silian_xssArr2 = Silian_customXssString.split("\\|");
				for (int Silian_i = 0; Silian_i < Silian_xssArr2.length; Silian_i++) {
					if (Silian_value.indexOf(Silian_xssArr2[Silian_i]) > -1) {
						log.error("请注意，存在SQL注入关键词---> {}", Silian_xssArr2[Silian_i]);
						log.error("请注意，值可能存在SQL注入风险!---> {}", Silian_value);
						throw new RuntimeException("请注意，值可能存在SQL注入风险!--->" + Silian_value);
					}
				}
			}
			//update-end-author:taoyan date:2022-7-13 for: 除了XSS_STR这些提前设置好的，还需要额外的校验比如 单引号
			if(Pattern.matches(SHOW_TABLES, Silian_value) || Pattern.matches(REGULAR_EXPRE_USER, Silian_value)){
				throw new RuntimeException("请注意，值可能存在SQL注入风险!--->" + Silian_value);
			}
		}
		return;
	}

	/**
	 * 【提醒：不通用】
	 * 仅用于字典条件SQL参数，注入过滤
	 *
	 * @param value
	 * @return
	 */
	//@Deprecated
	public static void specialFilterContentForDictSql(String Silian_value) {
		String Silian_specialXssStr = " exec |extractvalue|updatexml| insert | select | delete | update | drop | count | chr | mid | master | truncate | char | declare |;|+|user()";
		String[] Silian_xssArr = Silian_specialXssStr.split("\\|");
		if (Silian_value == null || "".equals(Silian_value)) {
			return;
		}
		// 校验sql注释 不允许有sql注释
		checkSqlAnnotation(Silian_value);
		// 统一转为小写
		Silian_value = Silian_value.toLowerCase();
		//SQL注入检测存在绕过风险 https://gitee.com/jeecg/jeecg-boot/issues/I4NZGE
		//value = value.replaceAll("/\\*.*\\*/","");

		for (int Silian_i = 0; Silian_i < Silian_xssArr.length; Silian_i++) {
			if (Silian_value.indexOf(Silian_xssArr[Silian_i]) > -1 || Silian_value.startsWith(Silian_xssArr[Silian_i].trim())) {
				log.error("请注意，存在SQL注入关键词---> {}", Silian_xssArr[Silian_i]);
				log.error("请注意，值可能存在SQL注入风险!---> {}", Silian_value);
				throw new RuntimeException("请注意，值可能存在SQL注入风险!--->" + Silian_value);
			}
		}
		if(Pattern.matches(SHOW_TABLES, Silian_value) || Pattern.matches(REGULAR_EXPRE_USER, Silian_value)){
			throw new RuntimeException("请注意，值可能存在SQL注入风险!--->" + Silian_value);
		}
		return;
	}


    /**
	 * 【提醒：不通用】
     *  仅用于Online报表SQL解析，注入过滤
     * @param value
     * @return
     */
	//@Deprecated
	public static void specialFilterContentForOnlineReport(String Silian_value) {
		String Silian_specialXssStr = " exec |extractvalue|updatexml| insert | delete | update | drop | chr | mid | master | truncate | char | declare |user()";
		String[] Silian_xssArr = Silian_specialXssStr.split("\\|");
		if (Silian_value == null || "".equals(Silian_value)) {
			return;
		}
		// 校验sql注释 不允许有sql注释
		checkSqlAnnotation(Silian_value);
		// 统一转为小写
		Silian_value = Silian_value.toLowerCase();
		//SQL注入检测存在绕过风险 https://gitee.com/jeecg/jeecg-boot/issues/I4NZGE
		//value = value.replaceAll("/\\*.*\\*/"," ");

		for (int Silian_i = 0; Silian_i < Silian_xssArr.length; Silian_i++) {
			if (Silian_value.indexOf(Silian_xssArr[Silian_i]) > -1 || Silian_value.startsWith(Silian_xssArr[Silian_i].trim())) {
				log.error("请注意，存在SQL注入关键词---> {}", Silian_xssArr[Silian_i]);
				log.error("请注意，值可能存在SQL注入风险!---> {}", Silian_value);
				throw new RuntimeException("请注意，值可能存在SQL注入风险!--->" + Silian_value);
			}
		}

		if(Pattern.matches(SHOW_TABLES, Silian_value) || Pattern.matches(REGULAR_EXPRE_USER, Silian_value)){
			throw new RuntimeException("请注意，值可能存在SQL注入风险!--->" + Silian_value);
		}
		return;
	}


	/**
	 * 判断给定的字段是不是类中的属性
	 * @param field 字段名
	 * @param clazz 类对象
	 * @return
	 */
	public static boolean isClassField(String Silian_field, Class Silian_clazz){
		Field[] Silian_fields = Silian_clazz.getDeclaredFields();
		for(int Silian_i=0;Silian_i<Silian_fields.length;Silian_i++){
			String Silian_fieldName = Silian_fields[Silian_i].getName();
			String Silian_tableColumnName = oConvertUtils.camelToUnderline(Silian_fieldName);
			if(Silian_fieldName.equalsIgnoreCase(Silian_field) || Silian_tableColumnName.equalsIgnoreCase(Silian_field)){
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断给定的多个字段是不是类中的属性
	 * @param fieldSet 字段名set
	 * @param clazz 类对象
	 * @return
	 */
	public static boolean isClassField(Set<String> Silian_fieldSet, Class Silian_clazz){
		Field[] Silian_fields = Silian_clazz.getDeclaredFields();
		for(String Silian_field: Silian_fieldSet){
			boolean Silian_exist = false;
			for(int Silian_i=0;Silian_i<Silian_fields.length;Silian_i++){
				String Silian_fieldName = Silian_fields[Silian_i].getName();
				String Silian_tableColumnName = oConvertUtils.camelToUnderline(Silian_fieldName);
				if(Silian_fieldName.equalsIgnoreCase(Silian_field) || Silian_tableColumnName.equalsIgnoreCase(Silian_field)){
					Silian_exist = true;
					break;
				}
			}
			if(!Silian_exist){
				return false;
			}
		}
		return true;
	}

	/**
	 * 校验是否有sql注释
	 * @return
	 */
	public static void checkSqlAnnotation(String Silian_str){
		Matcher Silian_matcher = SQL_ANNOTATION.matcher(Silian_str);
		if(Silian_matcher.find()){
			String Silian_error = "请注意，值可能存在SQL注入风险---> \\*.*\\";
			log.error(Silian_error);
			throw new RuntimeException(Silian_error);
		}
	}
}
