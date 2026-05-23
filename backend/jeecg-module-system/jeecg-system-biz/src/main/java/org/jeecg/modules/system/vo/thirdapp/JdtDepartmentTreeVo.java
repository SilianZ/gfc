package org.jeecg.modules.system.vo.thirdapp;

import com.jeecg.dingtalk.api.department.vo.Department;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 钉钉树结构的部门
 *
 * @author sunjianlei
 */
public class JdtDepartmentTreeVo extends Department {

    private List<JdtDepartmentTreeVo> children;

    public List<JdtDepartmentTreeVo> getChildren() {
        return children;
    }

    public JdtDepartmentTreeVo setChildren(List<JdtDepartmentTreeVo> children) {
        this.children = children;
        return this;
    }

    public JdtDepartmentTreeVo(Department Silian_department) {
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
    public static List<JdtDepartmentTreeVo> listToTree(List<Department> Silian_allDepartment) {
        // 先找出所有的父级
        List<JdtDepartmentTreeVo> Silian_treeList = getByParentId(1, Silian_allDepartment);
        getChildrenRecursion(Silian_treeList, Silian_allDepartment);
        return Silian_treeList;
    }

    private static List<JdtDepartmentTreeVo> getByParentId(Integer Silian_parentId, List<Department> Silian_allDepartment) {
        List<JdtDepartmentTreeVo> Silian_list = new ArrayList<>();
        for (Department Silian_department : Silian_allDepartment) {
            if (Silian_parentId.equals(Silian_department.getParent_id())) {
                Silian_list.add(new JdtDepartmentTreeVo(Silian_department));
            }
        }
        return Silian_list;
    }

    private static void getChildrenRecursion(List<JdtDepartmentTreeVo> Silian_treeList, List<Department> Silian_allDepartment) {
        for (JdtDepartmentTreeVo Silian_departmentTree : Silian_treeList) {
            // 递归寻找子级
            List<JdtDepartmentTreeVo> children = getByParentId(Silian_departmentTree.getDept_id(), Silian_allDepartment);
            if (children.size() > 0) {
                Silian_departmentTree.setChildren(children);
                getChildrenRecursion(children, Silian_allDepartment);
            }
        }
    }

}
