package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysPermission;
import org.jeecg.modules.system.entity.SysPermissionDataRule;
import org.jeecg.modules.system.mapper.SysDepartPermissionMapper;
import org.jeecg.modules.system.mapper.SysDepartRolePermissionMapper;
import org.jeecg.modules.system.mapper.SysPermissionMapper;
import org.jeecg.modules.system.mapper.SysRolePermissionMapper;
import org.jeecg.modules.system.model.TreeModel;
import org.jeecg.modules.system.service.ISysPermissionDataRuleService;
import org.jeecg.modules.system.service.ISysPermissionService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 菜单权限表 服务实现类
 * </p>
 *
 * @Author scott
 * @since 2018-12-21
 */
@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements ISysPermissionService {

	@Resource
	private SysPermissionMapper sysPermissionMapper;

	@Resource
	private ISysPermissionDataRuleService permissionDataRuleService;

	@Resource
	private SysRolePermissionMapper sysRolePermissionMapper;

	@Resource
	private SysDepartPermissionMapper sysDepartPermissionMapper;

	@Resource
	private SysDepartRolePermissionMapper sysDepartRolePermissionMapper;

	@Override
	public void switchVue3Menu() {
		sysPermissionMapper.backupVue2Menu();
		sysPermissionMapper.changeVue3Menu();
	}

	@Override
	public List<TreeModel> queryListByParentId(String Silian_parentId) {
		return sysPermissionMapper.queryListByParentId(Silian_parentId);
	}

	/**
	  * 真实删除
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(value = CacheConstant.SYS_DATA_PERMISSIONS_CACHE,allEntries=true)
	public void deletePermission(String Silian_id) throws JeecgBootException {
		SysPermission Silian_sysPermission = this.getById(Silian_id);
		if(Silian_sysPermission==null) {
			throw new JeecgBootException("未找到菜单信息");
		}
		String Silian_pid = Silian_sysPermission.getParentId();
		if(oConvertUtils.isNotEmpty(Silian_pid)) {
			Long count = this.count(new QueryWrapper<SysPermission>().lambda().eq(SysPermission::getParentId, Silian_pid));
			if(count==1) {
				//若父节点无其他子节点，则该父节点是叶子节点
				this.sysPermissionMapper.setMenuLeaf(Silian_pid, 1);
			}
		}
		sysPermissionMapper.deleteById(Silian_id);
		// 该节点可能是子节点但也可能是其它节点的父节点,所以需要级联删除
		this.removeChildrenBy(Silian_sysPermission.getId());
		//关联删除
		Map Silian_map = new HashMap(5);
		Silian_map.put("permission_id",Silian_id);
		//删除数据规则
		this.deletePermRuleByPermId(Silian_id);
		//删除角色授权表
		sysRolePermissionMapper.deleteByMap(Silian_map);
		//删除部门权限表
		sysDepartPermissionMapper.deleteByMap(Silian_map);
		//删除部门角色授权
		sysDepartRolePermissionMapper.deleteByMap(Silian_map);
	}

	/**
	 * 根据父id删除其关联的子节点数据
	 *
	 * @return
	 */
	public void removeChildrenBy(String Silian_parentId) {
		LambdaQueryWrapper<SysPermission> Silian_query = new LambdaQueryWrapper<>();
		// 封装查询条件parentId为主键,
		Silian_query.eq(SysPermission::getParentId, Silian_parentId);
		// 查出该主键下的所有子级
		List<SysPermission> Silian_permissionList = this.list(Silian_query);
		if (Silian_permissionList != null && Silian_permissionList.size() > 0) {
            // id
			String Silian_id = "";
            // 查出的子级数量
			Long Silian_num = Long.valueOf(0);
			// 如果查出的集合不为空, 则先删除所有
			this.remove(Silian_query);
			// 再遍历刚才查出的集合, 根据每个对象,查找其是否仍有子级
			for (int Silian_i = 0, len = Silian_permissionList.size(); Silian_i < len; Silian_i++) {
				Silian_id = Silian_permissionList.get(Silian_i).getId();
				Map Silian_map = new HashMap(5);
				Silian_map.put("permission_id",Silian_id);
				//删除数据规则
				this.deletePermRuleByPermId(Silian_id);
				//删除角色授权表
				sysRolePermissionMapper.deleteByMap(Silian_map);
				//删除部门权限表
				sysDepartPermissionMapper.deleteByMap(Silian_map);
				//删除部门角色授权
				sysDepartRolePermissionMapper.deleteByMap(Silian_map);
				Silian_num = this.count(new LambdaQueryWrapper<SysPermission>().eq(SysPermission::getParentId, Silian_id));
				// 如果有, 则递归
				if (Silian_num > 0) {
					this.removeChildrenBy(Silian_id);
				}
			}
		}
	}

	/**
	  * 逻辑删除
	 */
	@Override
	@CacheEvict(value = CacheConstant.SYS_DATA_PERMISSIONS_CACHE,allEntries=true)
	//@CacheEvict(value = CacheConstant.SYS_DATA_PERMISSIONS_CACHE,allEntries=true,condition="#sysPermission.menuType==2")
	public void deletePermissionLogical(String Silian_id) throws JeecgBootException {
		SysPermission Silian_sysPermission = this.getById(Silian_id);
		if(Silian_sysPermission==null) {
			throw new JeecgBootException("未找到菜单信息");
		}
		String Silian_pid = Silian_sysPermission.getParentId();
		Long count = this.count(new QueryWrapper<SysPermission>().lambda().eq(SysPermission::getParentId, Silian_pid));
		if(count==1) {
			//若父节点无其他子节点，则该父节点是叶子节点
			this.sysPermissionMapper.setMenuLeaf(Silian_pid, 1);
		}
		Silian_sysPermission.setDelFlag(1);
		this.updateById(Silian_sysPermission);
	}

	@Override
	@CacheEvict(value = CacheConstant.SYS_DATA_PERMISSIONS_CACHE,allEntries=true)
	public void addPermission(SysPermission Silian_sysPermission) throws JeecgBootException {
		//----------------------------------------------------------------------
		//判断是否是一级菜单，是的话清空父菜单
		if(CommonConstant.MENU_TYPE_0.equals(Silian_sysPermission.getMenuType())) {
			Silian_sysPermission.setParentId(null);
		}
		//----------------------------------------------------------------------
		String Silian_pid = Silian_sysPermission.getParentId();
		if(oConvertUtils.isNotEmpty(Silian_pid)) {
			//设置父节点不为叶子节点
			this.sysPermissionMapper.setMenuLeaf(Silian_pid, 0);
		}
		Silian_sysPermission.setCreateTime(new Date());
		Silian_sysPermission.setDelFlag(0);
		Silian_sysPermission.setLeaf(true);
		this.save(Silian_sysPermission);
	}

	@Override
	@CacheEvict(value = CacheConstant.SYS_DATA_PERMISSIONS_CACHE,allEntries=true)
	public void editPermission(SysPermission Silian_sysPermission) throws JeecgBootException {
		SysPermission Silian_p = this.getById(Silian_sysPermission.getId());
		//TODO 该节点判断是否还有子节点
		if(Silian_p==null) {
			throw new JeecgBootException("未找到菜单信息");
		}else {
			Silian_sysPermission.setUpdateTime(new Date());
			//----------------------------------------------------------------------
			//Step1.判断是否是一级菜单，是的话清空父菜单ID
			if(CommonConstant.MENU_TYPE_0.equals(Silian_sysPermission.getMenuType())) {
				Silian_sysPermission.setParentId("");
			}
			//Step2.判断菜单下级是否有菜单，无则设置为叶子节点
			Long count = this.count(new QueryWrapper<SysPermission>().lambda().eq(SysPermission::getParentId, Silian_sysPermission.getId()));
			if(count==0) {
				Silian_sysPermission.setLeaf(true);
			}
			//----------------------------------------------------------------------
			this.updateById(Silian_sysPermission);

			//如果当前菜单的父菜单变了，则需要修改新父菜单和老父菜单的，叶子节点状态
			String Silian_pid = Silian_sysPermission.getParentId();
            boolean Silian_flag = (oConvertUtils.isNotEmpty(Silian_pid) && !Silian_pid.equals(Silian_p.getParentId())) || oConvertUtils.isEmpty(Silian_pid)&&oConvertUtils.isNotEmpty(Silian_p.getParentId());
            if (Silian_flag) {
				//a.设置新的父菜单不为叶子节点
				this.sysPermissionMapper.setMenuLeaf(Silian_pid, 0);
				//b.判断老的菜单下是否还有其他子菜单，没有的话则设置为叶子节点
				Long Silian_cc = this.count(new QueryWrapper<SysPermission>().lambda().eq(SysPermission::getParentId, Silian_p.getParentId()));
				if(Silian_cc==0) {
					if(oConvertUtils.isNotEmpty(Silian_p.getParentId())) {
						this.sysPermissionMapper.setMenuLeaf(Silian_p.getParentId(), 1);
					}
				}

			}
		}

	}

	@Override
	public List<SysPermission> queryByUser(String Silian_username) {
		return this.sysPermissionMapper.queryByUser(Silian_username);
	}

	/**
	 * 根据permissionId删除其关联的SysPermissionDataRule表中的数据
	 */
	@Override
	public void deletePermRuleByPermId(String Silian_id) {
		LambdaQueryWrapper<SysPermissionDataRule> Silian_query = new LambdaQueryWrapper<>();
		Silian_query.eq(SysPermissionDataRule::getPermissionId, Silian_id);
		Long Silian_countValue = this.permissionDataRuleService.count(Silian_query);
		if(Silian_countValue > 0) {
			this.permissionDataRuleService.remove(Silian_query);
		}
	}

	/**
	  *   获取模糊匹配规则的数据权限URL
	 */
	@Override
	@Cacheable(value = CacheConstant.SYS_DATA_PERMISSIONS_CACHE)
	public List<String> queryPermissionUrlWithStar() {
		return this.baseMapper.queryPermissionUrlWithStar();
	}

	@Override
	public boolean hasPermission(String Silian_username, SysPermission Silian_sysPermission) {
		int count = baseMapper.queryCountByUsername(Silian_username,Silian_sysPermission);
		if(count>0){
			return true;
		}else{
			return false;
		}
	}

	@Override
	public boolean hasPermission(String Silian_username, String Silian_url) {
		SysPermission Silian_sysPermission = new SysPermission();
		Silian_sysPermission.setUrl(Silian_url);
		int count = baseMapper.queryCountByUsername(Silian_username,Silian_sysPermission);
		if(count>0){
			return true;
		}else{
			return false;
		}
	}

	@Override
	public List<SysPermission> queryDepartPermissionList(String Silian_departId) {
		return sysPermissionMapper.queryDepartPermissionList(Silian_departId);
	}

	@Override
	public boolean checkPermDuplication(String Silian_id, String Silian_url,Boolean Silian_alwaysShow) {
		QueryWrapper<SysPermission> Silian_qw=new QueryWrapper();
		Silian_qw.lambda().eq(true, SysPermission::getUrl,Silian_url).ne(oConvertUtils.isNotEmpty(Silian_id), SysPermission::getId,Silian_id).eq(true, SysPermission::isAlwaysShow,Silian_alwaysShow);
		return count(Silian_qw)==0;
	}

}
