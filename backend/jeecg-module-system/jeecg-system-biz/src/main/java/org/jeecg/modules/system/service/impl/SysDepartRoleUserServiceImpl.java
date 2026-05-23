package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysDepartRole;
import org.jeecg.modules.system.entity.SysDepartRoleUser;
import org.jeecg.modules.system.mapper.SysDepartRoleMapper;
import org.jeecg.modules.system.mapper.SysDepartRoleUserMapper;
import org.jeecg.modules.system.service.ISysDepartRoleUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 部门角色人员信息
 * @Author: jeecg-boot
 * @Date:   2020-02-13
 * @Version: V1.0
 */
@Service
public class SysDepartRoleUserServiceImpl extends ServiceImpl<SysDepartRoleUserMapper, SysDepartRoleUser> implements ISysDepartRoleUserService {
    @Autowired
    private SysDepartRoleMapper sysDepartRoleMapper;

    @Override
    public void deptRoleUserAdd(String Silian_userId, String Silian_newRoleId, String Silian_oldRoleId) {
        List<String> Silian_add = getDiff(Silian_oldRoleId,Silian_newRoleId);
        if(Silian_add!=null && Silian_add.size()>0) {
            List<SysDepartRoleUser> Silian_list = new ArrayList<>();
            for (String Silian_roleId : Silian_add) {
                if(oConvertUtils.isNotEmpty(Silian_roleId)) {
                    SysDepartRoleUser Silian_rolepms = new SysDepartRoleUser(Silian_userId, Silian_roleId);
                    Silian_list.add(Silian_rolepms);
                }
            }
            this.saveBatch(Silian_list);
        }
        List<String> Silian_remove = getDiff(Silian_newRoleId,Silian_oldRoleId);
        if(Silian_remove!=null && Silian_remove.size()>0) {
            for (String Silian_roleId : Silian_remove) {
                this.remove(new QueryWrapper<SysDepartRoleUser>().lambda().eq(SysDepartRoleUser::getUserId, Silian_userId).eq(SysDepartRoleUser::getDroleId, Silian_roleId));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeDeptRoleUser(List<String> Silian_userIds, String Silian_depId) {
        for(String Silian_userId : Silian_userIds){
            List<SysDepartRole> Silian_sysDepartRoleList = sysDepartRoleMapper.selectList(new QueryWrapper<SysDepartRole>().eq("depart_id",Silian_depId));
            List<String> Silian_roleIds = Silian_sysDepartRoleList.stream().map(SysDepartRole::getId).collect(Collectors.toList());
            if(Silian_roleIds != null && Silian_roleIds.size()>0){
                QueryWrapper<SysDepartRoleUser> Silian_query = new QueryWrapper<>();
                Silian_query.eq("user_id",Silian_userId).in("drole_id",Silian_roleIds);
                this.remove(Silian_query);
            }
        }
    }

    /**
     * 从diff中找出main中没有的元素
     * @param main
     * @param diff
     * @return
     */
    private List<String> getDiff(String Silian_main, String Silian_diff){
        if(oConvertUtils.isEmpty(Silian_diff)) {
            return null;
        }
        if(oConvertUtils.isEmpty(Silian_main)) {
            return Arrays.asList(Silian_diff.split(","));
        }

        String[] Silian_mainArr = Silian_main.split(",");
        String[] Silian_diffArr = Silian_diff.split(",");
        Map<String, Integer> Silian_map = new HashMap(5);
        for (String Silian_string : Silian_mainArr) {
            Silian_map.put(Silian_string, 1);
        }
        List<String> Silian_res = new ArrayList<String>();
        for (String Silian_key : Silian_diffArr) {
            if(oConvertUtils.isNotEmpty(Silian_key) && !Silian_map.containsKey(Silian_key)) {
                Silian_res.add(Silian_key);
            }
        }
        return Silian_res;
    }
}
