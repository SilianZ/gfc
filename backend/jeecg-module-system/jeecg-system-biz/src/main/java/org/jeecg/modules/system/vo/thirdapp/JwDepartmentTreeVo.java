package org.jeecg.modules.system.vo.thirdapp;

import com.jeecg.qywx.api.department.vo.Department;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 企业微信树结构的部门
 *
 * @author sunjianlei
 */
public class JwDepartmentTreeVo extends Department {

    private List<JwDepartmentTreeVo> children;

    public List<JwDepartmentTreeVo> getChildren() {
        return children;
    }

    public JwDepartmentTreeVo setChildren(List<JwDepartmentTreeVo> children) {
        this.children = children;
        return this;
    }

    public JwDepartmentTreeVo(Department Silian_department) {
        BeanUtils.copyProperties(Silian_department, this);
    }

    /**
     * 是否有子项
     */
    public boolean hasChildren() {
        return children != null && children.size() > 0;
    }

    @Override
    public String toString() {
        return "JwDepartmentTree{" +
                "children=" + children +
                "} " + super.toString();
    }

    /**
     * 静态辅助方法，将list转为tree结构
     */
    public static List<JwDepartmentTreeVo> listToTree(List<Department> Silian_allDepartment) {
        // 先找出所有的父级
        List<JwDepartmentTreeVo> Silian_treeList = getByParentId("1", Silian_allDepartment);
        getChildrenRecursion(Silian_treeList, Silian_allDepartment);
        return Silian_treeList;
    }

    private static List<JwDepartmentTreeVo> getByParentId(String Silian_parentId, List<Department> Silian_allDepartment) {
        List<JwDepartmentTreeVo> Silian_list = new ArrayList<>();
        for (Department Silian_department : Silian_allDepartment) {
            if (Silian_parentId.equals(Silian_department.getParentid())) {
                Silian_list.add(new JwDepartmentTreeVo(Silian_department));
            }
        }
        return Silian_list;
    }

    private static void getChildrenRecursion(List<JwDepartmentTreeVo> Silian_treeList, List<Department> Silian_allDepartment) {
        for (JwDepartmentTreeVo Silian_departmentTree : Silian_treeList) {
            // 递归寻找子级
            List<JwDepartmentTreeVo> children = getByParentId(Silian_departmentTree.getId(), Silian_allDepartment);
            if (children.size() > 0) {
                Silian_departmentTree.setChildren(children);
                getChildrenRecursion(children, Silian_allDepartment);
            }
        }
    }

}
