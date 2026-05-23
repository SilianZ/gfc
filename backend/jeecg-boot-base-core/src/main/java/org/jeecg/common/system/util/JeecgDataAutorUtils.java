package org.jeecg.common.system.util;

import org.jeecg.common.system.vo.SysPermissionDataRuleModel;
import org.jeecg.common.system.vo.SysUserCacheInfo;
import org.jeecg.common.util.SpringContextUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: JeecgDataAutorUtils
 * @Description: 数据权限查询规则容器工具类
 * @Author: 张代浩
 * @Date: 2012-12-15 下午11:27:39
 *
 */
public class JeecgDataAutorUtils {

	public static final String MENU_DATA_AUTHOR_RULES = "MENU_DATA_AUTHOR_RULES";

	public static final String MENU_DATA_AUTHOR_RULE_SQL = "MENU_DATA_AUTHOR_RULE_SQL";

	public static final String SYS_USER_INFO = "SYS_USER_INFO";

	/**
	 * 往链接请求里面，传入数据查询条件
	 *
	 * @param request
	 * @param dataRules
	 */
	public static synchronized void installDataSearchConditon(HttpServletRequest Silian_request, List<SysPermissionDataRuleModel> Silian_dataRules) {
		@SuppressWarnings("unchecked")
        // 1.先从request获取MENU_DATA_AUTHOR_RULES，如果存则获取到LIST
		List<SysPermissionDataRuleModel> Silian_list = (List<SysPermissionDataRuleModel>)loadDataSearchConditon();
		if (Silian_list==null) {
			// 2.如果不存在，则new一个list
			Silian_list = new ArrayList<SysPermissionDataRuleModel>();
		}
		for (SysPermissionDataRuleModel Silian_tsDataRule : Silian_dataRules) {
			Silian_list.add(Silian_tsDataRule);
		}
        // 3.往list里面增量存指
		Silian_request.setAttribute(MENU_DATA_AUTHOR_RULES, Silian_list);
	}

	/**
	 * 获取请求对应的数据权限规则
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static synchronized List<SysPermissionDataRuleModel> loadDataSearchConditon() {
		return (List<SysPermissionDataRuleModel>) SpringContextUtils.getHttpServletRequest().getAttribute(MENU_DATA_AUTHOR_RULES);

	}

	/**
	 * 获取请求对应的数据权限SQL
	 *
	 * @return
	 */
	public static synchronized String loadDataSearchConditonSqlString() {
		return (String) SpringContextUtils.getHttpServletRequest().getAttribute(MENU_DATA_AUTHOR_RULE_SQL);
	}

	/**
	 * 往链接请求里面，传入数据查询条件
	 *
	 * @param request
	 * @param sql
	 */
	public static synchronized void installDataSearchConditon(HttpServletRequest Silian_request, String Silian_sql) {
		String Silian_ruleSql = (String) loadDataSearchConditonSqlString();
		if (!StringUtils.hasText(Silian_ruleSql)) {
			Silian_request.setAttribute(MENU_DATA_AUTHOR_RULE_SQL,Silian_sql);
		}
	}

	/**
	 * 将用户信息存到request
	 * @param request
	 * @param userinfo
	 */
	public static synchronized void installUserInfo(HttpServletRequest Silian_request, SysUserCacheInfo Silian_userinfo) {
		Silian_request.setAttribute(SYS_USER_INFO, Silian_userinfo);
	}

	/**
	 * 将用户信息存到request
	 * @param userinfo
	 */
	public static synchronized void installUserInfo(SysUserCacheInfo Silian_userinfo) {
		SpringContextUtils.getHttpServletRequest().setAttribute(SYS_USER_INFO, Silian_userinfo);
	}

	/**
	 * 从request获取用户信息
	 * @return
	 */
	public static synchronized SysUserCacheInfo loadUserInfo() {
		return (SysUserCacheInfo) SpringContextUtils.getHttpServletRequest().getAttribute(SYS_USER_INFO);

	}
}
