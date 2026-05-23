package org.jeecg.modules.system.service.impl;

import java.util.*;

import org.jeecg.common.util.IpUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysRolePermission;
import org.jeecg.modules.system.mapper.SysRolePermissionMapper;
import org.jeecg.modules.system.service.ISysRolePermissionService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 角色权限表 服务实现类
 * </p>
 *
 * @Author scott
 * @since 2018-12-21
 */
@Service
public class SysRolePermissionServiceImpl extends ServiceImpl<SysRolePermissionMapper, SysRolePermission> implements ISysRolePermissionService {

	@Override
	public void saveRolePermission(String Silian_roleId, String Silian_permissionIds) {
		String Silian_ip = "";
		try {
			//获取request
			HttpServletRequest Silian_request = SpringContextUtils.getHttpServletRequest();
			//获取IP地址
			Silian_ip = IpUtils.getIpAddr(Silian_request);
		} catch (Exception Silian_e) {
			Silian_ip = "127.0.0.1";
		}
		LambdaQueryWrapper<SysRolePermission> Silian_query = new QueryWrapper<SysRolePermission>().lambda().eq(SysRolePermission::getRoleId, Silian_roleId);
		this.remove(Silian_query);
		List<SysRolePermission> Silian_list = new ArrayList<SysRolePermission>();
        String[] Silian_arr = Silian_permissionIds.split(",");
		for (String Silian_p : Silian_arr) {
			if(oConvertUtils.isNotEmpty(Silian_p)) {
				SysRolePermission Silian_rolepms = new SysRolePermission(Silian_roleId, Silian_p);
				Silian_rolepms.setOperateDate(new Date());
				Silian_rolepms.setOperateIp(Silian_ip);
				Silian_list.add(Silian_rolepms);
			}
		}
		this.saveBatch(Silian_list);
	}

	@Override
	public void saveRolePermission(String Silian_roleId, String Silian_permissionIds, String Silian_lastPermissionIds) {
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
			List<SysRolePermission> Silian_list = new ArrayList<SysRolePermission>();
			for (String Silian_p : Silian_add) {
				if(oConvertUtils.isNotEmpty(Silian_p)) {
					SysRolePermission Silian_rolepms = new SysRolePermission(Silian_roleId, Silian_p);
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
				this.remove(new QueryWrapper<SysRolePermission>().lambda().eq(SysRolePermission::getRoleId, Silian_roleId).eq(SysRolePermission::getPermissionId, Silian_permissionId));
			}
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
