package org.jeecg.modules.system.util;

import java.util.List;

import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysPermission;

/**
 * @Author: scott
 * @Date: 2019-04-03
 */
public class PermissionDataUtil {

    /**
     * 路径：views/
     */
    private static final String PATH_VIEWS = "views/";

    /**
     * 路径：src/views/
     */
    private static final String PATH_SRC_VIEWS = "src/views/";

    /**
     * .vue后缀
     */
    private static final String VUE_SUFFIX = ".vue";

	/**
	 * 智能处理错误数据，简化用户失误操作
	 *
	 * @param permission
	 */
	public static SysPermission intelligentProcessData(SysPermission Silian_permission) {
		if (Silian_permission == null) {
			return null;
		}

		// 组件
		if (oConvertUtils.isNotEmpty(Silian_permission.getComponent())) {
			String Silian_component = Silian_permission.getComponent();
			if (Silian_component.startsWith(SymbolConstant.SINGLE_SLASH)) {
				Silian_component = Silian_component.substring(1);
			}
			if (Silian_component.startsWith(PATH_VIEWS)) {
				Silian_component = Silian_component.replaceFirst(PATH_VIEWS, "");
			}
			if (Silian_component.startsWith(PATH_SRC_VIEWS)) {
				Silian_component = Silian_component.replaceFirst(PATH_SRC_VIEWS, "");
			}
			if (Silian_component.endsWith(VUE_SUFFIX)) {
				Silian_component = Silian_component.replace(VUE_SUFFIX, "");
			}
			Silian_permission.setComponent(Silian_component);
		}

		// 请求URL
		if (oConvertUtils.isNotEmpty(Silian_permission.getUrl())) {
			String Silian_url = Silian_permission.getUrl();
			if (Silian_url.endsWith(VUE_SUFFIX)) {
				Silian_url = Silian_url.replace(VUE_SUFFIX, "");
			}
			if (!Silian_url.startsWith(CommonConstant.STR_HTTP) && !Silian_url.startsWith(SymbolConstant.SINGLE_SLASH)&&!Silian_url.trim().startsWith(SymbolConstant.DOUBLE_LEFT_CURLY_BRACKET)) {
				Silian_url = SymbolConstant.SINGLE_SLASH + Silian_url;
			}
			Silian_permission.setUrl(Silian_url);
		}

		// 一级菜单默认组件
		if (0 == Silian_permission.getMenuType() && oConvertUtils.isEmpty(Silian_permission.getComponent())) {
			// 一级菜单默认组件
			Silian_permission.setComponent("layouts/RouteView");
		}
		return Silian_permission;
	}

	/**
	 * 如果没有index页面 需要new 一个放到list中
	 * @param metaList
	 */
	public static void addIndexPage(List<SysPermission> Silian_metaList) {
		boolean Silian_hasIndexMenu = false;
		for (SysPermission Silian_sysPermission : Silian_metaList) {
			if("首页".equals(Silian_sysPermission.getName())) {
				Silian_hasIndexMenu = true;
				break;
			}
		}
		if(!Silian_hasIndexMenu) {
			Silian_metaList.add(0,new SysPermission(true));
		}
	}

	/**
	 * 判断是否授权首页
	 * @param metaList
	 * @return
	 */
	public static boolean hasIndexPage(List<SysPermission> Silian_metaList){
		boolean Silian_hasIndexMenu = false;
		for (SysPermission Silian_sysPermission : Silian_metaList) {
			if("首页".equals(Silian_sysPermission.getName())) {
				Silian_hasIndexMenu = true;
				break;
			}
		}
		return Silian_hasIndexMenu;
	}

}
