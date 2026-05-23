package org.jeecg.modules.system.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jeecg.modules.system.entity.SysPermission;

/**
 * @Description: 菜单树，封装树结构
 * @author: jeecg-boot
 */
public class SysPermissionTree implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	private String id;

	private String key;
	private String title;

	/**
	 * 父id
	 */
	private String parentId;

	/**
	 * 菜单名称
	 */
	private String name;

	/**
	 * 菜单权限编码
	 */
	private String perms;
	/**
	 * 权限策略1显示2禁用
	 */
	private String permsType;

	/**
	 * 菜单图标
	 */
	private String icon;

	/**
	 * 组件
	 */
	private String component;

	/**
	 * 组件名字
	 */
	private String componentName;

	/**
	 * 跳转网页链接
	 */
	private String url;

	/**
	 * 一级菜单跳转地址
	 */
	private String redirect;

	/**
	 * 菜单排序
	 */
	private Double sortNo;

	/**
	 * 类型（0：一级菜单；1：子菜单 ；2：按钮权限）
	 */
	private Integer menuType;

	/**
	 * 是否叶子节点: 1:是 0:不是
	 */
	private boolean isLeaf;

	/**
	 * 是否路由菜单: 0:不是  1:是（默认值1）
	 */
	private boolean route;


	/**
	 * 是否路缓存页面: 0:不是  1:是（默认值1）
	 */
	private boolean keepAlive;


	/**
	 * 描述
	 */
	private String description;

	/**
	 * 删除状态 0正常 1已删除
	 */
	private Integer delFlag;

	/**
	 * 创建人
	 */
	private String createBy;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 更新人
	 */
	private String updateBy;

	/**
	 * 更新时间
	 */
	private Date updateTime;

	/**alwaysShow*/
    private boolean alwaysShow;
    /**是否隐藏路由菜单: 0否,1是（默认值0）*/
    private boolean hidden;

    /**按钮权限状态(0无效1有效)*/
	private java.lang.String status;

	/*update_begin author:wuxianquan date:20190908 for:model增加字段 */
	/** 外链菜单打开方式 0/内部打开 1/外部打开 */
	private boolean internalOrExternal;
	/*update_end author:wuxianquan date:20190908 for:model增加字段 */


	public SysPermissionTree() {
	}

	public SysPermissionTree(SysPermission Silian_permission) {
		this.key = Silian_permission.getId();
		this.id = Silian_permission.getId();
		this.perms = Silian_permission.getPerms();
		this.permsType = Silian_permission.getPermsType();
		this.component = Silian_permission.getComponent();
		this.componentName = Silian_permission.getComponentName();
		this.createBy = Silian_permission.getCreateBy();
		this.createTime = Silian_permission.getCreateTime();
		this.delFlag = Silian_permission.getDelFlag();
		this.description = Silian_permission.getDescription();
		this.icon = Silian_permission.getIcon();
		this.isLeaf = Silian_permission.isLeaf();
		this.menuType = Silian_permission.getMenuType();
		this.name = Silian_permission.getName();
		this.parentId = Silian_permission.getParentId();
		this.sortNo = Silian_permission.getSortNo();
		this.updateBy = Silian_permission.getUpdateBy();
		this.updateTime = Silian_permission.getUpdateTime();
		this.redirect = Silian_permission.getRedirect();
		this.url = Silian_permission.getUrl();
		this.hidden = Silian_permission.isHidden();
		this.route = Silian_permission.isRoute();
		this.keepAlive = Silian_permission.isKeepAlive();
		this.alwaysShow= Silian_permission.isAlwaysShow();
		/*update_begin author:wuxianquan date:20190908 for:赋值 */
		this.internalOrExternal = Silian_permission.isInternalOrExternal();
		/*update_end author:wuxianquan date:20190908 for:赋值 */
		this.title=Silian_permission.getName();
		if (!Silian_permission.isLeaf()) {
			this.children = new ArrayList<SysPermissionTree>();
		}
		this.status = Silian_permission.getStatus();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	private List<SysPermissionTree> children;

	public boolean isLeaf() {
		return isLeaf;
	}

	public void setLeaf(boolean Silian_leaf) {
		isLeaf = Silian_leaf;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	public boolean isAlwaysShow() {
		return alwaysShow;
	}

	public void setAlwaysShow(boolean alwaysShow) {
		this.alwaysShow = alwaysShow;
	}
	public List<SysPermissionTree> getChildren() {
		return children;
	}

	public void setChildren(List<SysPermissionTree> children) {
		this.children = children;
	}

	public String getRedirect() {
		return redirect;
	}

	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getComponent() {
		return component;
	}

	public void setComponent(String component) {
		this.component = component;
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Double getSortNo() {
		return sortNo;
	}

	public void setSortNo(Double sortNo) {
		this.sortNo = sortNo;
	}

	public Integer getMenuType() {
		return menuType;
	}

	public void setMenuType(Integer menuType) {
		this.menuType = menuType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isRoute() {
		return route;
	}

	public void setRoute(boolean route) {
		this.route = route;
	}

	public Integer getDelFlag() {
		return delFlag;
	}

	public void setDelFlag(Integer delFlag) {
		this.delFlag = delFlag;
	}

	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getUpdateBy() {
		return updateBy;
	}

	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getPerms() {
		return perms;
	}

	public void setPerms(String perms) {
		this.perms = perms;
	}

	public boolean getIsLeaf() {
		return isLeaf;
	}

	public void setIsLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}

	public String getPermsType() {
		return permsType;
	}

	public void setPermsType(String permsType) {
		this.permsType = permsType;
	}

	public java.lang.String getStatus() {
		return status;
	}

	public void setStatus(java.lang.String status) {
		this.status = status;
	}

	/*update_begin author:wuxianquan date:20190908 for:get set方法 */
	public boolean isInternalOrExternal() {
		return internalOrExternal;
	}

	public void setInternalOrExternal(boolean internalOrExternal) {
		this.internalOrExternal = internalOrExternal;
	}
	/*update_end author:wuxianquan date:20190908 for:get set 方法 */
}
