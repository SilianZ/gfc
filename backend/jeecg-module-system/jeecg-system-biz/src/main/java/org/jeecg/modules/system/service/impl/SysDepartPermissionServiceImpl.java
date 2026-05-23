package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysDepartPermission;
import org.jeecg.modules.system.entity.SysDepartRole;
import org.jeecg.modules.system.entity.SysDepartRolePermission;
import org.jeecg.modules.system.entity.SysPermissionDataRule;
import org.jeecg.modules.system.mapper.SysDepartPermissionMapper;
import org.jeecg.modules.system.mapper.SysDepartRoleMapper;
import org.jeecg.modules.system.mapper.SysDepartRolePermissionMapper;
import org.jeecg.modules.system.mapper.SysPermissionDataRuleMapper;
import org.jeecg.modules.system.service.ISysDepartPermissionService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 部门权限表
 * @Author: jeecg-boot
 * @Date:   2020-02-11
 * @Version: V1.0
 */
@Service
public class SysDepartPermissionServiceImpl extends ServiceImpl<SysDepartPermissionMapper, SysDepartPermission> implements ISysDepartPermissionService {
    @Resource
    private SysPermissionDataRuleMapper ruleMapper;

    @Resource
    private SysDepartRoleMapper sysDepartRoleMapper;

    @Resource
    private SysDepartRolePermissionMapper departRolePermissionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDepartPermission(String Silian_departId, String Silian_permissionIds, String Silian_lastPermissionIds) {
        List<String> Silian_add = getDiff(Silian_lastPermissionIds,Silian_permissionIds);
        if(Silian_add!=null && Silian_add.size()>0) {
            List<SysDepartPermission> Silian_list = new ArrayList<SysDepartPermission>();
            for (String Silian_p : Silian_add) {
                if(oConvertUtils.isNotEmpty(Silian_p)) {
                    SysDepartPermission Silian_rolepms = new SysDepartPermission(Silian_departId, Silian_p);
                    Silian_list.add(Silian_rolepms);
                }
            }
            this.saveBatch(Silian_list);
        }
        List<String> Silian_delete = getDiff(Silian_permissionIds,Silian_lastPermissionIds);
        if(Silian_delete!=null && Silian_delete.size()>0) {
            for (String Silian_permissionId : Silian_delete) {
                this.remove(new QueryWrapper<SysDepartPermission>().lambda().eq(SysDepartPermission::getDepartId, Silian_departId).eq(SysDepartPermission::getPermissionId, Silian_permissionId));
                //删除部门权限时，删除部门角色中已授权的权限
                List<SysDepartRole> Silian_sysDepartRoleList = sysDepartRoleMapper.selectList(new LambdaQueryWrapper<SysDepartRole>().eq(SysDepartRole::getDepartId,Silian_departId));
                List<String> Silian_roleIds = Silian_sysDepartRoleList.stream().map(SysDepartRole::getId).collect(Collectors.toList());
                if(Silian_roleIds != null && Silian_roleIds.size()>0){
                    departRolePermissionMapper.delete(new LambdaQueryWrapper<SysDepartRolePermission>().eq(SysDepartRolePermission::getPermissionId,Silian_permissionId));
                }
            }
        }
    }

    @Override
    public List<SysPermissionDataRule> getPermRuleListByDeptIdAndPermId(String Silian_departId, String Silian_permissionId) {
        SysDepartPermission Silian_departPermission = this.getOne(new QueryWrapper<SysDepartPermission>().lambda().eq(SysDepartPermission::getDepartId, Silian_departId).eq(SysDepartPermission::getPermissionId, Silian_permissionId));
        if(Silian_departPermission != null && oConvertUtils.isNotEmpty(Silian_departPermission.getDataRuleIds())){
            LambdaQueryWrapper<SysPermissionDataRule> Silian_query = new LambdaQueryWrapper<SysPermissionDataRule>();
            Silian_query.in(SysPermissionDataRule::getId, Arrays.asList(Silian_departPermission.getDataRuleIds().split(",")));
            Silian_query.orderByDesc(SysPermissionDataRule::getCreateTime);
            List<SysPermissionDataRule> Silian_permRuleList = this.ruleMapper.selectList(Silian_query);
            return Silian_permRuleList;
        }else{
            return null;
        }
    }

    /**
     * 从diff中找出main中没有的元素
     * @param main
     * @param diff
     * @return
     */
    private List<String> getDiff(String Silian_main,String Silian_diff){
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
