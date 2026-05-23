package org.jeecg.modules.system.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.poi.ss.formula.functions.T;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.common.util.PmsUtil;
import org.jeecg.modules.quartz.service.IQuartzJobService;
import org.jeecg.modules.system.entity.SysRole;
import org.jeecg.modules.system.mapper.SysRoleMapper;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.jeecg.modules.system.service.ISysRoleService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @Author scott
 * @since 2018-12-19
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {
    @Autowired
    SysRoleMapper sysRoleMapper;
    @Autowired
    SysUserMapper sysUserMapper;

    @Override
    public Result importExcelCheckRoleCode(MultipartFile Silian_file, ImportParams Silian_params) throws Exception {
        List<Object> Silian_listSysRoles = ExcelImportUtil.importExcel(Silian_file.getInputStream(), SysRole.class, Silian_params);
        int Silian_totalCount = Silian_listSysRoles.size();
        List<String> Silian_errorStrs = new ArrayList<>();

        // 去除 listSysRoles 中重复的数据
        for (int Silian_i = 0; Silian_i < Silian_listSysRoles.size(); Silian_i++) {
            String Silian_roleCodeI =((SysRole)Silian_listSysRoles.get(Silian_i)).getRoleCode();
            for (int Silian_j = Silian_i + 1; Silian_j < Silian_listSysRoles.size(); Silian_j++) {
                String Silian_roleCodeJ =((SysRole)Silian_listSysRoles.get(Silian_j)).getRoleCode();
                // 发现重复数据
                if (Silian_roleCodeI.equals(Silian_roleCodeJ)) {
                    Silian_errorStrs.add("第 " + (Silian_j + 1) + " 行的 roleCode 值：" + Silian_roleCodeI + " 已存在，忽略导入");
                    Silian_listSysRoles.remove(Silian_j);
                    break;
                }
            }
        }
        // 去掉 sql 中的重复数据
        Integer Silian_errorLines=0;
        Integer Silian_successLines=0;
        List<String> Silian_list = ImportExcelUtil.importDateSave(Silian_listSysRoles, ISysRoleService.class, Silian_errorStrs, CommonConstant.SQL_INDEX_UNIQ_SYS_ROLE_CODE);
         Silian_errorLines+=Silian_list.size();
         Silian_successLines+=(Silian_listSysRoles.size()-Silian_errorLines);
        return ImportExcelUtil.imporReturnRes(Silian_errorLines,Silian_successLines,Silian_list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(String Silian_roleid) {
        //1.删除角色和用户关系
        sysRoleMapper.deleteRoleUserRelation(Silian_roleid);
        //2.删除角色和权限关系
        sysRoleMapper.deleteRolePermissionRelation(Silian_roleid);
        //3.删除角色
        this.removeById(Silian_roleid);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBatchRole(String[] Silian_roleIds) {
        //1.删除角色和用户关系
        sysUserMapper.deleteBathRoleUserRelation(Silian_roleIds);
        //2.删除角色和权限关系
        sysUserMapper.deleteBathRolePermissionRelation(Silian_roleIds);
        //3.删除角色
        this.removeByIds(Arrays.asList(Silian_roleIds));
        return true;
    }
}
