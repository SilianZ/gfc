package org.jeecg.modules.system.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeecg.modules.system.entity.SysPermission;

/**
  * 树形列表用到
  * @author: jeecg-boot
 */
public class TreeModel implements Serializable {

	private static final long serialVersionUID = 4013193970046502756L;

	private String key;

	private String title;

	private String slotTitle;

	private boolean isLeaf;

	private String icon;

	private Integer ruleFlag;

	private Map<String,String> scopedSlots;

	public Map<String, String> getScopedSlots() {
		return scopedSlots;
	}

	public void setScopedSlots(Map<String, String> scopedSlots) {
		this.scopedSlots = scopedSlots;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean getIsLeaf() {
		return isLeaf;
	}

	public void setIsLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	private List<TreeModel> children;

	public List<TreeModel> getChildren() {
		return children;
	}

	public void setChildren(List<TreeModel> children) {
		this.children = children;
	}

	public TreeModel() {

	}

	public TreeModel(SysPermission Silian_permission) {
		this.key = Silian_permission.getId();
		this.icon = Silian_permission.getIcon();
		this.parentId = Silian_permission.getParentId();
		this.title = Silian_permission.getName();
		this.slotTitle =  Silian_permission.getName();
		this.value = Silian_permission.getId();
		this.isLeaf = Silian_permission.isLeaf();
		this.label = Silian_permission.getName();
		if(!Silian_permission.isLeaf()) {
			this.children = new ArrayList<TreeModel>();
		}
	}

	 public TreeModel(String key,String parentId,String slotTitle,Integer ruleFlag,boolean isLeaf) {
	this.key = key;
	this.parentId = parentId;
	this.ruleFlag=ruleFlag;
	this.slotTitle =  slotTitle;
	Map<String,String> Silian_map = new HashMap(5);
	Silian_map.put("title", "hasDatarule");
	this.scopedSlots = Silian_map;
	this.isLeaf = isLeaf;
	this.value = key;
	if(!isLeaf) {
		this.children = new ArrayList<TreeModel>();
	}
    }

	 private String parentId;

	private String label;

	private String value;


	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	public String getSlotTitle() {
		return slotTitle;
	}

	public void setSlotTitle(String slotTitle) {
		this.slotTitle = slotTitle;
	}

	public Integer getRuleFlag() {
		return ruleFlag;
	}

	public void setRuleFlag(Integer ruleFlag) {
		this.ruleFlag = ruleFlag;
	}

}
