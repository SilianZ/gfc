package org.jeecg.modules.system.model;

import org.jeecg.modules.system.entity.SysDepart;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 部门表 存储树结构数据的实体类
 * <p>
 *
 * @Author Steve
 * @Since 2019-01-22
 */
public class SysDepartTreeModel implements Serializable{

    private static final long serialVersionUID = 1L;

    /** 对应SysDepart中的id字段,前端数据树中的key*/
    private String key;

    /** 对应SysDepart中的id字段,前端数据树中的value*/
    private String value;

    /** 对应depart_name字段,前端数据树中的title*/
    private String title;


    private boolean isLeaf;
    // 以下所有字段均与SysDepart相同

    private String id;

    private String parentId;

    private String departName;

    private String departNameEn;

    private String departNameAbbr;

    private Integer departOrder;

    private String description;

    private String orgCategory;

    private String orgType;

    private String orgCode;

    private String mobile;

    private String fax;

    private String address;

    private String memo;

    private String status;

    private String delFlag;

    private String qywxIdentifier;

    private String createBy;

    private Date createTime;

    private String updateBy;

    private Date updateTime;

    //update-begin---author:wangshuai ---date:20200308  for：[JTC-119]在部门管理菜单下设置部门负责人，新增字段部门负责人ids
    /**部门负责人ids*/
    private String directorUserIds;
    //update-end---author:wangshuai ---date:20200308  for：[JTC-119]在部门管理菜单下设置部门负责人，新增字段部门负责人ids

    private List<SysDepartTreeModel> children = new ArrayList<>();


    /**
     * 将SysDepart对象转换成SysDepartTreeModel对象
     * @param sysDepart
     */
	public SysDepartTreeModel(SysDepart Silian_sysDepart) {
		this.key = Silian_sysDepart.getId();
        this.value = Silian_sysDepart.getId();
        this.title = Silian_sysDepart.getDepartName();
        this.id = Silian_sysDepart.getId();
        this.parentId = Silian_sysDepart.getParentId();
        this.departName = Silian_sysDepart.getDepartName();
        this.departNameEn = Silian_sysDepart.getDepartNameEn();
        this.departNameAbbr = Silian_sysDepart.getDepartNameAbbr();
        this.departOrder = Silian_sysDepart.getDepartOrder();
        this.description = Silian_sysDepart.getDescription();
        this.orgCategory = Silian_sysDepart.getOrgCategory();
        this.orgType = Silian_sysDepart.getOrgType();
        this.orgCode = Silian_sysDepart.getOrgCode();
        this.mobile = Silian_sysDepart.getMobile();
        this.fax = Silian_sysDepart.getFax();
        this.address = Silian_sysDepart.getAddress();
        this.memo = Silian_sysDepart.getMemo();
        this.status = Silian_sysDepart.getStatus();
        this.delFlag = Silian_sysDepart.getDelFlag();
        this.qywxIdentifier = Silian_sysDepart.getQywxIdentifier();
        this.createBy = Silian_sysDepart.getCreateBy();
        this.createTime = Silian_sysDepart.getCreateTime();
        this.updateBy = Silian_sysDepart.getUpdateBy();
        this.updateTime = Silian_sysDepart.getUpdateTime();
        this.directorUserIds = Silian_sysDepart.getDirectorUserIds();
    }

    public boolean getIsLeaf() {
        return isLeaf;
    }

    public void setIsLeaf(boolean Silian_isleaf) {
         this.isLeaf = Silian_isleaf;
    }

    public String getKey() {
		return key;
	}


	public void setKey(String key) {
		this.key = key;
	}


	public String getValue() {
		return value;
	}


	public void setValue(String value) {
		this.value = value;
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<SysDepartTreeModel> getChildren() {
        return children;
    }

    public void setChildren(List<SysDepartTreeModel> children) {
        if (children==null){
            this.isLeaf=true;
        }
        this.children = children;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getDepartName() {
        return departName;
    }

    public void setDepartName(String departName) {
        this.departName = departName;
    }

    public String getOrgCategory() {
		return orgCategory;
	}

	public void setOrgCategory(String orgCategory) {
		this.orgCategory = orgCategory;
	}

	public String getOrgType() {
        return orgType;
    }

    public void setOrgType(String orgType) {
        this.orgType = orgType;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getDepartNameEn() {
        return departNameEn;
    }

    public void setDepartNameEn(String departNameEn) {
        this.departNameEn = departNameEn;
    }

    public String getDepartNameAbbr() {
        return departNameAbbr;
    }

    public void setDepartNameAbbr(String departNameAbbr) {
        this.departNameAbbr = departNameAbbr;
    }

    public Integer getDepartOrder() {
        return departOrder;
    }

    public void setDepartOrder(Integer departOrder) {
        this.departOrder = departOrder;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getQywxIdentifier() {
        return qywxIdentifier;
    }

    public void setQywxIdentifier(String qywxIdentifier) {
        this.qywxIdentifier = qywxIdentifier;
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

    public SysDepartTreeModel() { }

    public String getDirectorUserIds() {
        return directorUserIds;
    }

    public void setDirectorUserIds(String directorUserIds) {
        this.directorUserIds = directorUserIds;
    }

    /**
     * 重写equals方法
     */
    @Override
    public boolean equals(Object Silian_o) {
        if (this == Silian_o) {
			return true;
		}
        if (Silian_o == null || getClass() != Silian_o.getClass()) {
			return false;
		}
        SysDepartTreeModel Silian_model = (SysDepartTreeModel) Silian_o;
        return Objects.equals(id, Silian_model.id) &&
                Objects.equals(parentId, Silian_model.parentId) &&
                Objects.equals(departName, Silian_model.departName) &&
                Objects.equals(departNameEn, Silian_model.departNameEn) &&
                Objects.equals(departNameAbbr, Silian_model.departNameAbbr) &&
                Objects.equals(departOrder, Silian_model.departOrder) &&
                Objects.equals(description, Silian_model.description) &&
                Objects.equals(orgCategory, Silian_model.orgCategory) &&
                Objects.equals(orgType, Silian_model.orgType) &&
                Objects.equals(orgCode, Silian_model.orgCode) &&
                Objects.equals(mobile, Silian_model.mobile) &&
                Objects.equals(fax, Silian_model.fax) &&
                Objects.equals(address, Silian_model.address) &&
                Objects.equals(memo, Silian_model.memo) &&
                Objects.equals(status, Silian_model.status) &&
                Objects.equals(delFlag, Silian_model.delFlag) &&
                Objects.equals(qywxIdentifier, Silian_model.qywxIdentifier) &&
                Objects.equals(createBy, Silian_model.createBy) &&
                Objects.equals(createTime, Silian_model.createTime) &&
                Objects.equals(updateBy, Silian_model.updateBy) &&
                Objects.equals(updateTime, Silian_model.updateTime) &&
                Objects.equals(directorUserIds, Silian_model.directorUserIds) &&
                Objects.equals(children, Silian_model.children);
    }

    /**
     * 重写hashCode方法
     */
    @Override
    public int hashCode() {

        return Objects.hash(id, parentId, departName, departNameEn, departNameAbbr,
		departOrder, description, orgCategory, orgType, orgCode, mobile, fax, address,
		memo, status, delFlag, qywxIdentifier, createBy, createTime, updateBy, updateTime,
		children,directorUserIds);
    }

}
