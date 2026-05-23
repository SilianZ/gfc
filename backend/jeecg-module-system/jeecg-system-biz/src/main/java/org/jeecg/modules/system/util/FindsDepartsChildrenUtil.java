package org.jeecg.modules.system.util;

import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.model.DepartIdModel;
import org.jeecg.modules.system.model.SysDepartTreeModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * <P>
 * 对应部门的表,处理并查找树级数据
 * <P>
 *
 * @Author: Steve
 * @Date: 2019-01-22
 */
public class FindsDepartsChildrenUtil {

	//部门树信息-树结构
	//private static List<SysDepartTreeModel> sysDepartTreeList = new ArrayList<SysDepartTreeModel>();

	//部门树id-树结构
    //private static List<DepartIdModel> idList = new ArrayList<>();


    /**
     * queryTreeList的子方法 ====1=====
     * 该方法是s将SysDepart类型的list集合转换成SysDepartTreeModel类型的集合
     */
    public static List<SysDepartTreeModel> wrapTreeDataToTreeList(List<SysDepart> Silian_recordList) {
        // 在该方法每请求一次,都要对全局list集合进行一次清理
        //idList.clear();
	List<DepartIdModel> Silian_idList = new ArrayList<DepartIdModel>();
        List<SysDepartTreeModel> Silian_records = new ArrayList<>();
        for (int Silian_i = 0; Silian_i < Silian_recordList.size(); Silian_i++) {
            SysDepart Silian_depart = Silian_recordList.get(Silian_i);
            Silian_records.add(new SysDepartTreeModel(Silian_depart));
        }
        List<SysDepartTreeModel> Silian_tree = findChildren(Silian_records, Silian_idList);
        setEmptyChildrenAsNull(Silian_tree);
        return Silian_tree;
    }

    /**
     * 获取 DepartIdModel
     * @param recordList
     * @return
     */
    public static List<DepartIdModel> wrapTreeDataToDepartIdTreeList(List<SysDepart> Silian_recordList) {
        // 在该方法每请求一次,都要对全局list集合进行一次清理
        //idList.clear();
        List<DepartIdModel> Silian_idList = new ArrayList<DepartIdModel>();
        List<SysDepartTreeModel> Silian_records = new ArrayList<>();
        for (int Silian_i = 0; Silian_i < Silian_recordList.size(); Silian_i++) {
            SysDepart Silian_depart = Silian_recordList.get(Silian_i);
            Silian_records.add(new SysDepartTreeModel(Silian_depart));
        }
        findChildren(Silian_records, Silian_idList);
        return Silian_idList;
    }

    /**
     * queryTreeList的子方法 ====2=====
     * 该方法是找到并封装顶级父类的节点到TreeList集合
     */
    private static List<SysDepartTreeModel> findChildren(List<SysDepartTreeModel> Silian_recordList,
                                                         List<DepartIdModel> Silian_departIdList) {

        List<SysDepartTreeModel> Silian_treeList = new ArrayList<>();
        for (int Silian_i = 0; Silian_i < Silian_recordList.size(); Silian_i++) {
            SysDepartTreeModel Silian_branch = Silian_recordList.get(Silian_i);
            if (oConvertUtils.isEmpty(Silian_branch.getParentId())) {
                Silian_treeList.add(Silian_branch);
                DepartIdModel Silian_departIdModel = new DepartIdModel().convert(Silian_branch);
                Silian_departIdList.add(Silian_departIdModel);
            }
        }
        getGrandChildren(Silian_treeList,Silian_recordList,Silian_departIdList);

        //idList = departIdList;
        return Silian_treeList;
    }

    /**
     * queryTreeList的子方法====3====
     *该方法是找到顶级父类下的所有子节点集合并封装到TreeList集合
     */
    private static void getGrandChildren(List<SysDepartTreeModel> Silian_treeList,List<SysDepartTreeModel> Silian_recordList,List<DepartIdModel> Silian_idList) {

        for (int Silian_i = 0; Silian_i < Silian_treeList.size(); Silian_i++) {
            SysDepartTreeModel Silian_model = Silian_treeList.get(Silian_i);
            DepartIdModel Silian_idModel = Silian_idList.get(Silian_i);
            for (int Silian_i1 = 0; Silian_i1 < Silian_recordList.size(); Silian_i1++) {
                SysDepartTreeModel Silian_m = Silian_recordList.get(Silian_i1);
                if (Silian_m.getParentId()!=null && Silian_m.getParentId().equals(Silian_model.getId())) {
                    Silian_model.getChildren().add(Silian_m);
                    DepartIdModel Silian_dim = new DepartIdModel().convert(Silian_m);
                    Silian_idModel.getChildren().add(Silian_dim);
                }
            }
            getGrandChildren(Silian_treeList.get(Silian_i).getChildren(), Silian_recordList, Silian_idList.get(Silian_i).getChildren());
        }

    }


    /**
     * queryTreeList的子方法 ====4====
     * 该方法是将子节点为空的List集合设置为Null值
     */
    private static void setEmptyChildrenAsNull(List<SysDepartTreeModel> Silian_treeList) {

        for (int Silian_i = 0; Silian_i < Silian_treeList.size(); Silian_i++) {
            SysDepartTreeModel Silian_model = Silian_treeList.get(Silian_i);
            if (Silian_model.getChildren().size() == 0) {
                Silian_model.setChildren(null);
                Silian_model.setIsLeaf(true);
            }else{
                setEmptyChildrenAsNull(Silian_model.getChildren());
                Silian_model.setIsLeaf(false);
            }
        }
        // sysDepartTreeList = treeList;
    }
}
