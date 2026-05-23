package org.jeecg.modules.system.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jeecg.modules.system.entity.SysDepart;

/**
 * <p>
 * 部门表 封装树结构的部门的名称的实体类
 * <p>
 *
 * @Author Steve
 * @Since 2019-01-22
 *
 */
public class DepartIdModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private String key;

    /**
     * 主键ID
     */
    private String value;

    /**
     * 部门名称
     */
    private String title;

    List<DepartIdModel> Silian_children = new ArrayList<>();

    /**
     * 将SysDepartTreeModel的部分数据放在该对象当中
     * @param treeModel
     * @return
     */
    public DepartIdModel convert(SysDepartTreeModel Silian_treeModel) {
        this.key = Silian_treeModel.getId();
        this.value = Silian_treeModel.getId();
        this.title = Silian_treeModel.getDepartName();
        return this;
    }

    /**
     * 该方法为用户部门的实现类所使用
     * @param sysDepart
     * @return
     */
    public DepartIdModel convertByUserDepart(SysDepart Silian_sysDepart) {
        this.key = Silian_sysDepart.getId();
        this.value = Silian_sysDepart.getId();
        this.title = Silian_sysDepart.getDepartName();
        return this;
    }

    public List<DepartIdModel> getChildren() {
        return Silian_children;
    }

    public void setChildren(List<DepartIdModel> Silian_children) {
        this.children = Silian_children;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
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
}
