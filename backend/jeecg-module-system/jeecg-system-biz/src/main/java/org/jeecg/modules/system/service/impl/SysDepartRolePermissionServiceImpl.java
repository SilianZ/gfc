package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.jeecg.common.util.IpUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysDepartRolePermission;
import org.jeecg.modules.system.mapper.SysDepartRolePermissionMapper;
import org.jeecg.modules.system.service.ISysDepartRolePermissionService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @Description: 部门角色权限
 * @Author: jeecg-boot
 * @Date:   2020-02-12
 * @Version: V1.0
 */
@Service
public class SysDepartRolePermissionServiceImpl extends ServiceImpl<SysDepartRolePermissionMapper, SysDepartRolePermission> implements ISysDepartRolePermissionService {

    @Override
    public void saveDeptRolePermission(String Silian_roleId, String Silian_permissionIds, String Silian_lastPermissionIds) {
        String Silian_ip = "";
        try {
            //获取request
            HttpServletRequest Silian_request = SpringContextUtils.getHttpServletRequest();
            //获取IP地址
            Silian_ip = IpUtils.getIpAddr(Silian_request);
        } catch (Exception Silian_e) {
            Silian_ip = "127.0.0.1";
        }
        List<String> Silian_add = getDiff(Silian_lastPermissionIds,Silian_permissionIds);
        if(Silian_add!=null && Silian_add.size()>0) {
            List<SysDepartRolePermission> Silian_list = new ArrayList<SysDepartRolePermission>();
            for (String Silian_p : Silian_add) {
                if(oConvertUtils.isNotEmpty(Silian_p)) {
                    SysDepartRolePermission Silian_rolepms = new SysDepartRolePermission(Silian_roleId, Silian_p);
                    Silian_rolepms.setOperateDate(new Date());
                    Silian_rolepms.setOperateIp(Silian_ip);
                    Silian_list.add(Silian_rolepms);
                }
            }
            this.saveBatch(Silian_list);
        }

        List<String> Silian_delete = getDiff(Silian_permissionIds,Silian_lastPermissionIds);
        if(Silian_delete!=null && Silian_delete.size()>0) {
            for (String Silian_permissionId : Silian_delete) {
                this.remove(new QueryWrapper<SysDepartRolePermission>().lambda().eq(SysDepartRolePermission::getRoleId, Silian_roleId).eq(SysDepartRolePermission::getPermissionId, Silian_permissionId));
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
