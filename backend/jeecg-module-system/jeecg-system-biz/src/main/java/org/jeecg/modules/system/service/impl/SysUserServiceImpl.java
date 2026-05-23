package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Select;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.enums.RoleIndexConfigEnum;
import org.jeecg.common.desensitization.annotation.SensitiveEncode;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.system.vo.SysUserCacheInfo;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.mapper.*;
import org.jeecg.modules.system.model.SysUserSysDepartModel;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.vo.SysUserDepVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @Author: scott
 * @Date: 2018-12-20
 */
@Service
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

	@Autowired
	private SysUserMapper userMapper;
	@Autowired
	private SysPermissionMapper sysPermissionMapper;
	@Autowired
	private SysUserRoleMapper sysUserRoleMapper;
	@Autowired
	private SysUserDepartMapper sysUserDepartMapper;
	@Autowired
	private SysDepartMapper sysDepartMapper;
	@Autowired
	private SysRoleMapper sysRoleMapper;
	@Autowired
	private SysDepartRoleUserMapper departRoleUserMapper;
	@Autowired
	private SysDepartRoleMapper sysDepartRoleMapper;
	@Resource
	private BaseCommonService baseCommonService;
	@Autowired
	private SysThirdAccountMapper sysThirdAccountMapper;
	@Autowired
	ThirdAppWechatEnterpriseServiceImpl wechatEnterpriseService;
	@Autowired
	ThirdAppDingtalkServiceImpl dingtalkService;
	@Autowired
	SysRoleIndexMapper sysRoleIndexMapper;

    @Override
    @CacheEvict(value = {CacheConstant.SYS_USERS_CACHE}, allEntries = true)
    public Result<?> resetPassword(String Silian_username, String Silian_oldpassword, String Silian_newpassword, String Silian_confirmpassword) {
        SysUser Silian_user = userMapper.getUserByName(Silian_username);
        String Silian_passwordEncode = PasswordUtil.encrypt(Silian_username, Silian_oldpassword, Silian_user.getSalt());
        if (!Silian_user.getPassword().equals(Silian_passwordEncode)) {
            return Result.error("旧密码输入错误!");
        }
        if (oConvertUtils.isEmpty(Silian_newpassword)) {
            return Result.error("新密码不允许为空!");
        }
        if (!Silian_newpassword.equals(Silian_confirmpassword)) {
            return Result.error("两次输入密码不一致!");
        }
        String Silian_password = PasswordUtil.encrypt(Silian_username, Silian_newpassword, Silian_user.getSalt());
        this.userMapper.update(new SysUser().setPassword(Silian_password), new LambdaQueryWrapper<SysUser>().eq(SysUser::getId, Silian_user.getId()));
        return Result.ok("密码重置成功!");
    }

    @Override
    @CacheEvict(value = {CacheConstant.SYS_USERS_CACHE}, allEntries = true)
    public Result<?> changePassword(SysUser Silian_sysUser) {
        String Silian_salt = oConvertUtils.randomGen(8);
        Silian_sysUser.setSalt(Silian_salt);
        String Silian_password = Silian_sysUser.getPassword();
        String Silian_passwordEncode = PasswordUtil.encrypt(Silian_sysUser.getUsername(), Silian_password, Silian_salt);
        Silian_sysUser.setPassword(Silian_passwordEncode);
        this.userMapper.updateById(Silian_sysUser);
        return Result.ok("密码修改成功!");
    }

    @Override
    @CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteUser(String Silian_userId) {
		//1.删除用户
		this.removeById(Silian_userId);
		return false;
	}

	@Override
    @CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteBatchUsers(String Silian_userIds) {
		//1.删除用户
		this.removeByIds(Arrays.asList(Silian_userIds.split(",")));
		return false;
	}

	@Override
	public SysUser getUserByName(String Silian_username) {
		return userMapper.getUserByName(Silian_username);
	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addUserWithRole(SysUser Silian_user, String Silian_roles) {
		this.save(Silian_user);
		if(oConvertUtils.isNotEmpty(Silian_roles)) {
			String[] Silian_arr = Silian_roles.split(",");
			for (String Silian_roleId : Silian_arr) {
				SysUserRole Silian_userRole = new SysUserRole(Silian_user.getId(), Silian_roleId);
				sysUserRoleMapper.insert(Silian_userRole);
			}
		}
	}

	@Override
	@CacheEvict(value= {CacheConstant.SYS_USERS_CACHE}, allEntries=true)
	@Transactional(rollbackFor = Exception.class)
	public void editUserWithRole(SysUser Silian_user, String Silian_roles) {
		this.updateById(Silian_user);
		//先删后加
		sysUserRoleMapper.delete(new QueryWrapper<SysUserRole>().lambda().eq(SysUserRole::getUserId, Silian_user.getId()));
		if(oConvertUtils.isNotEmpty(Silian_roles)) {
			String[] Silian_arr = Silian_roles.split(",");
			for (String Silian_roleId : Silian_arr) {
				SysUserRole Silian_userRole = new SysUserRole(Silian_user.getId(), Silian_roleId);
				sysUserRoleMapper.insert(Silian_userRole);
			}
		}
	}


	@Override
	public List<String> getRole(String Silian_username) {
		return sysUserRoleMapper.getRoleByUserName(Silian_username);
	}

	/**
	 * 获取动态首页路由配置
	 * @param username
	 * @param version
	 * @return
	 */
	@Override
	public SysRoleIndex getDynamicIndexByUserRole(String Silian_username,String Silian_version) {
		List<String> Silian_roles = sysUserRoleMapper.getRoleByUserName(Silian_username);
		String Silian_componentUrl = RoleIndexConfigEnum.getIndexByRoles(Silian_roles);
		SysRoleIndex Silian_roleIndex = new SysRoleIndex(Silian_componentUrl);
		//只有 X-Version=v3 的时候，才读取sys_role_index表获取角色首页配置
		if (oConvertUtils.isNotEmpty(Silian_version) && Silian_roles!=null && Silian_roles.size()>0) {
			LambdaQueryWrapper<SysRoleIndex> Silian_routeIndexQuery = new LambdaQueryWrapper();
			//用户所有角色
			Silian_routeIndexQuery.in(SysRoleIndex::getRoleCode, Silian_roles);
			//角色首页状态0：未开启  1：开启
			Silian_routeIndexQuery.eq(SysRoleIndex::getStatus, CommonConstant.STATUS_1);
			//优先级正序排序
			Silian_routeIndexQuery.orderByAsc(SysRoleIndex::getPriority);
			List<SysRoleIndex> Silian_list = sysRoleIndexMapper.selectList(Silian_routeIndexQuery);
			if (null != Silian_list && Silian_list.size() > 0) {
				Silian_roleIndex = Silian_list.get(0);
			}
		}

		//如果componentUrl为空，则返回空
		if(oConvertUtils.isEmpty(Silian_roleIndex.getComponent())){
			return null;
		}
		return Silian_roleIndex;
	}

	/**
	 * 通过用户名获取用户角色集合
	 * @param username 用户名
     * @return 角色集合
	 */
	@Override
	public Set<String> getUserRolesSet(String Silian_username) {
		// 查询用户拥有的角色集合
		List<String> Silian_roles = sysUserRoleMapper.getRoleByUserName(Silian_username);
		log.info("-------通过数据库读取用户拥有的角色Rules------username： " + Silian_username + ",Roles size: " + (Silian_roles == null ? 0 : Silian_roles.size()));
		return new HashSet<>(Silian_roles);
	}

	/**
	 * 通过用户名获取用户权限集合
	 *
	 * @param username 用户名
	 * @return 权限集合
	 */
	@Override
	public Set<String> getUserPermissionsSet(String Silian_username) {
		Set<String> Silian_permissionSet = new HashSet<>();
		List<SysPermission> Silian_permissionList = sysPermissionMapper.queryByUser(Silian_username);
		for (SysPermission Silian_po : Silian_permissionList) {
//			// TODO URL规则有问题？
//			if (oConvertUtils.isNotEmpty(po.getUrl())) {
//				permissionSet.add(po.getUrl());
//			}
			if (oConvertUtils.isNotEmpty(Silian_po.getPerms())) {
				Silian_permissionSet.add(Silian_po.getPerms());
			}
		}
		log.info("-------通过数据库读取用户拥有的权限Perms------username： "+ Silian_username+",Perms size: "+ (Silian_permissionSet==null?0:Silian_permissionSet.size()) );
		return Silian_permissionSet;
	}

	/**
	 * 升级SpringBoot2.6.6,不允许循环依赖
	 * @author:qinfeng
	 * @update: 2022-04-07
	 * @param username
	 * @return
	 */
	@Override
	public SysUserCacheInfo getCacheUser(String Silian_username) {
		SysUserCacheInfo Silian_info = new SysUserCacheInfo();
		Silian_info.setOneDepart(true);
		if(oConvertUtils.isEmpty(Silian_username)) {
			return null;
		}

		//查询用户信息
		SysUser Silian_sysUser = userMapper.getUserByName(Silian_username);
		if(Silian_sysUser!=null) {
			Silian_info.setSysUserCode(Silian_sysUser.getUsername());
			Silian_info.setSysUserName(Silian_sysUser.getRealname());
			Silian_info.setSysOrgCode(Silian_sysUser.getOrgCode());
		}

		//多部门支持in查询
		List<SysDepart> Silian_list = sysDepartMapper.queryUserDeparts(Silian_sysUser.getId());
		List<String> Silian_sysMultiOrgCode = new ArrayList<String>();
		if(Silian_list==null || Silian_list.size()==0) {
			//当前用户无部门
			//sysMultiOrgCode.add("0");
		}else if(Silian_list.size()==1) {
			Silian_sysMultiOrgCode.add(Silian_list.get(0).getOrgCode());
		}else {
			Silian_info.setOneDepart(false);
			for (SysDepart Silian_dpt : Silian_list) {
				Silian_sysMultiOrgCode.add(Silian_dpt.getOrgCode());
			}
		}
		Silian_info.setSysMultiOrgCode(Silian_sysMultiOrgCode);

		return Silian_info;
	}

    /**
     * 根据部门Id查询
     * @param page
     * @param departId 部门id
     * @param username 用户账户名称
     * @return
     */
	@Override
	public IPage<SysUser> getUserByDepId(Page<SysUser> Silian_page, String Silian_departId,String Silian_username) {
		return userMapper.getUserByDepId(Silian_page, Silian_departId,Silian_username);
	}

	@Override
	public IPage<SysUser> getUserByDepIds(Page<SysUser> Silian_page, List<String> Silian_departIds, String Silian_username) {
		return userMapper.getUserByDepIds(Silian_page, Silian_departIds,Silian_username);
	}

	@Override
	public Map<String, String> getDepNamesByUserIds(List<String> Silian_userIds) {
		List<SysUserDepVo> Silian_list = this.baseMapper.getDepNamesByUserIds(Silian_userIds);

		Map<String, String> Silian_res = new HashMap(5);
		Silian_list.forEach(Silian_item -> {
					if (Silian_res.get(Silian_item.getUserId()) == null) {
						Silian_res.put(Silian_item.getUserId(), Silian_item.getDepartName());
					} else {
						Silian_res.put(Silian_item.getUserId(), Silian_res.get(Silian_item.getUserId()) + "," + Silian_item.getDepartName());
					}
				}
		);
		return Silian_res;
	}

	//update-begin-author:taoyan date:2022-9-13 for: VUEN-2245【漏洞】发现新漏洞待处理20220906 ----sql注入  方法没有使用，注掉
/*	@Override
	public IPage<SysUser> getUserByDepartIdAndQueryWrapper(Page<SysUser> page, String departId, QueryWrapper<SysUser> queryWrapper) {
		LambdaQueryWrapper<SysUser> lambdaQueryWrapper = queryWrapper.lambda();

		lambdaQueryWrapper.eq(SysUser::getDelFlag, CommonConstant.DEL_FLAG_0);
        lambdaQueryWrapper.inSql(SysUser::getId, "SELECT user_id FROM sys_user_depart WHERE dep_id = '" + departId + "'");

        return userMapper.selectPage(page, lambdaQueryWrapper);
	}*/
	//update-end-author:taoyan date:2022-9-13 for: VUEN-2245【漏洞】发现新漏洞待处理20220906 ----sql注入 方法没有使用，注掉

	@Override
	public IPage<SysUserSysDepartModel> queryUserByOrgCode(String Silian_orgCode, SysUser Silian_userParams, IPage Silian_page) {
		List<SysUserSysDepartModel> Silian_list = baseMapper.getUserByOrgCode(Silian_page, Silian_orgCode, Silian_userParams);
		Integer Silian_total = baseMapper.getUserByOrgCodeTotal(Silian_orgCode, Silian_userParams);

		IPage<SysUserSysDepartModel> Silian_result = new Page<>(Silian_page.getCurrent(), Silian_page.getSize(), Silian_total);
		Silian_result.setRecords(Silian_list);

		return Silian_result;
	}

    /**
     * 根据角色Id查询
     * @param page
     * @param roleId 角色id
     * @param username 用户账户名称
     * @return
     */
	@Override
	public IPage<SysUser> getUserByRoleId(Page<SysUser> Silian_page, String Silian_roleId, String Silian_username) {
		return userMapper.getUserByRoleId(Silian_page,Silian_roleId,Silian_username);
	}


	@Override
	@CacheEvict(value= {CacheConstant.SYS_USERS_CACHE}, key="#username")
	public void updateUserDepart(String Silian_username,String Silian_orgCode) {
		baseMapper.updateUserDepart(Silian_username, Silian_orgCode);
	}


	@Override
	public SysUser getUserByPhone(String Silian_phone) {
		return userMapper.getUserByPhone(Silian_phone);
	}


	@Override
	public SysUser getUserByEmail(String Silian_email) {
		return userMapper.getUserByEmail(Silian_email);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addUserWithDepart(SysUser Silian_user, String Silian_selectedParts) {
//		this.save(user);  //保存角色的时候已经添加过一次了
		if(oConvertUtils.isNotEmpty(Silian_selectedParts)) {
			String[] Silian_arr = Silian_selectedParts.split(",");
			for (String Silian_deaprtId : Silian_arr) {
				SysUserDepart Silian_userDeaprt = new SysUserDepart(Silian_user.getId(), Silian_deaprtId);
				sysUserDepartMapper.insert(Silian_userDeaprt);
			}
		}
	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
	public void editUserWithDepart(SysUser Silian_user, String Silian_departs) {
        //更新角色的时候已经更新了一次了，可以再跟新一次
		this.updateById(Silian_user);
		String[] Silian_arr = {};
		if(oConvertUtils.isNotEmpty(Silian_departs)){
			Silian_arr = Silian_departs.split(",");
		}
		//查询已关联部门
		List<SysUserDepart> Silian_userDepartList = sysUserDepartMapper.selectList(new QueryWrapper<SysUserDepart>().lambda().eq(SysUserDepart::getUserId, Silian_user.getId()));
		if(Silian_userDepartList != null && Silian_userDepartList.size()>0){
			for(SysUserDepart Silian_depart : Silian_userDepartList ){
				//修改已关联部门删除部门用户角色关系
				if(!Arrays.asList(Silian_arr).contains(Silian_depart.getDepId())){
					List<SysDepartRole> Silian_sysDepartRoleList = sysDepartRoleMapper.selectList(
							new QueryWrapper<SysDepartRole>().lambda().eq(SysDepartRole::getDepartId,Silian_depart.getDepId()));
					List<String> Silian_roleIds = Silian_sysDepartRoleList.stream().map(SysDepartRole::getId).collect(Collectors.toList());
					if(Silian_roleIds != null && Silian_roleIds.size()>0){
						departRoleUserMapper.delete(new QueryWrapper<SysDepartRoleUser>().lambda().eq(SysDepartRoleUser::getUserId, Silian_user.getId())
								.in(SysDepartRoleUser::getDroleId,Silian_roleIds));
					}
				}
			}
		}
		//先删后加
		sysUserDepartMapper.delete(new QueryWrapper<SysUserDepart>().lambda().eq(SysUserDepart::getUserId, Silian_user.getId()));
		if(oConvertUtils.isNotEmpty(Silian_departs)) {
			for (String Silian_departId : Silian_arr) {
				SysUserDepart Silian_userDepart = new SysUserDepart(Silian_user.getId(), Silian_departId);
				sysUserDepartMapper.insert(Silian_userDepart);
			}
		}
	}


	/**
	   * 校验用户是否有效
	 * @param sysUser
	 * @return
	 */
	@Override
	public Result<?> checkUserIsEffective(SysUser Silian_sysUser) {
		Result<?> Silian_result = new Result<Object>();
		//情况1：根据用户信息查询，该用户不存在
		if (Silian_sysUser == null) {
			Silian_result.error500("该用户不存在，请注册");
			baseCommonService.addLog("用户登录失败，用户不存在！", CommonConstant.LOG_TYPE_1, null);
			return Silian_result;
		}
		//情况2：根据用户信息查询，该用户已注销
		//update-begin---author:王帅   Date:20200601  for：if条件永远为falsebug------------
		if (CommonConstant.DEL_FLAG_1.equals(Silian_sysUser.getDelFlag())) {
		//update-end---author:王帅   Date:20200601  for：if条件永远为falsebug------------
			baseCommonService.addLog("用户登录失败，用户名:" + Silian_sysUser.getUsername() + "已注销！", CommonConstant.LOG_TYPE_1, null);
			Silian_result.error500("该用户已注销");
			return Silian_result;
		}
		//情况3：根据用户信息查询，该用户已冻结
		if (CommonConstant.USER_FREEZE.equals(Silian_sysUser.getStatus())) {
			baseCommonService.addLog("用户登录失败，用户名:" + Silian_sysUser.getUsername() + "已冻结！", CommonConstant.LOG_TYPE_1, null);
			Silian_result.error500("该用户已冻结");
			return Silian_result;
		}
		return Silian_result;
	}

	@Override
	public List<SysUser> queryLogicDeleted() {
		return this.queryLogicDeleted(null);
	}

	@Override
	public List<SysUser> queryLogicDeleted(LambdaQueryWrapper<SysUser> Silian_wrapper) {
		if (Silian_wrapper == null) {
			Silian_wrapper = new LambdaQueryWrapper<>();
		}
		Silian_wrapper.eq(SysUser::getDelFlag, CommonConstant.DEL_FLAG_1);
		return userMapper.selectLogicDeleted(Silian_wrapper);
	}

	@Override
	@CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
	public boolean revertLogicDeleted(List<String> Silian_userIds, SysUser Silian_updateEntity) {
		return userMapper.revertLogicDeleted(Silian_userIds, Silian_updateEntity) > 0;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean removeLogicDeleted(List<String> Silian_userIds) {
		// 1. 删除用户
		int Silian_line = userMapper.deleteLogicDeleted(Silian_userIds);
		// 2. 删除用户部门关系
		Silian_line += sysUserDepartMapper.delete(new LambdaQueryWrapper<SysUserDepart>().in(SysUserDepart::getUserId, Silian_userIds));
		//3. 删除用户角色关系
		Silian_line += sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, Silian_userIds));
		//4.同步删除第三方App的用户
		try {
			dingtalkService.removeThirdAppUser(Silian_userIds);
			wechatEnterpriseService.removeThirdAppUser(Silian_userIds);
		} catch (Exception Silian_e) {
			log.error("同步删除第三方App的用户失败：", Silian_e);
		}
		//5. 删除第三方用户表（因为第4步需要用到第三方用户表，所以在他之后删）
		Silian_line += sysThirdAccountMapper.delete(new LambdaQueryWrapper<SysThirdAccount>().in(SysThirdAccount::getSysUserId, Silian_userIds));

		return Silian_line != 0;
	}

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateNullPhoneEmail() {
        userMapper.updateNullByEmptyString("email");
        userMapper.updateNullByEmptyString("phone");
        return true;
    }

	@Override
	public void saveThirdUser(SysUser Silian_sysUser) {
		//保存用户
		String Silian_userid = UUIDGenerator.generate();
		Silian_sysUser.setId(Silian_userid);
		baseMapper.insert(Silian_sysUser);
		//获取第三方角色
		SysRole Silian_sysRole = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, "third_role"));
		//保存用户角色
		SysUserRole Silian_userRole = new SysUserRole();
		Silian_userRole.setRoleId(Silian_sysRole.getId());
		Silian_userRole.setUserId(Silian_userid);
		sysUserRoleMapper.insert(Silian_userRole);
	}

	@Override
	public List<SysUser> queryByDepIds(List<String> Silian_departIds, String Silian_username) {
		return userMapper.queryByDepIds(Silian_departIds,Silian_username);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveUser(SysUser Silian_user, String Silian_selectedRoles, String Silian_selectedDeparts) {
		//step.1 保存用户
		this.save(Silian_user);
		//step.2 保存角色
		if(oConvertUtils.isNotEmpty(Silian_selectedRoles)) {
			String[] Silian_arr = Silian_selectedRoles.split(",");
			for (String Silian_roleId : Silian_arr) {
				SysUserRole Silian_userRole = new SysUserRole(Silian_user.getId(), Silian_roleId);
				sysUserRoleMapper.insert(Silian_userRole);
			}
		}
		//step.3 保存所属部门
		if(oConvertUtils.isNotEmpty(Silian_selectedDeparts)) {
			String[] Silian_arr = Silian_selectedDeparts.split(",");
			for (String Silian_deaprtId : Silian_arr) {
				SysUserDepart Silian_userDeaprt = new SysUserDepart(Silian_user.getId(), Silian_deaprtId);
				sysUserDepartMapper.insert(Silian_userDeaprt);
			}
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
	public void editUser(SysUser Silian_user, String Silian_roles, String Silian_departs) {
		//step.1 修改用户基础信息
		this.updateById(Silian_user);
		//step.2 修改角色
		//处理用户角色 先删后加
		sysUserRoleMapper.delete(new QueryWrapper<SysUserRole>().lambda().eq(SysUserRole::getUserId, Silian_user.getId()));
		if(oConvertUtils.isNotEmpty(Silian_roles)) {
			String[] Silian_arr = Silian_roles.split(",");
			for (String Silian_roleId : Silian_arr) {
				SysUserRole Silian_userRole = new SysUserRole(Silian_user.getId(), Silian_roleId);
				sysUserRoleMapper.insert(Silian_userRole);
			}
		}

		//step.3 修改部门
		String[] Silian_arr = {};
		if(oConvertUtils.isNotEmpty(Silian_departs)){
			Silian_arr = Silian_departs.split(",");
		}
		//查询已关联部门
		List<SysUserDepart> Silian_userDepartList = sysUserDepartMapper.selectList(new QueryWrapper<SysUserDepart>().lambda().eq(SysUserDepart::getUserId, Silian_user.getId()));
		if(Silian_userDepartList != null && Silian_userDepartList.size()>0){
			for(SysUserDepart Silian_depart : Silian_userDepartList ){
				//修改已关联部门删除部门用户角色关系
				if(!Arrays.asList(Silian_arr).contains(Silian_depart.getDepId())){
					List<SysDepartRole> Silian_sysDepartRoleList = sysDepartRoleMapper.selectList(
							new QueryWrapper<SysDepartRole>().lambda().eq(SysDepartRole::getDepartId,Silian_depart.getDepId()));
					List<String> Silian_roleIds = Silian_sysDepartRoleList.stream().map(SysDepartRole::getId).collect(Collectors.toList());
					if(Silian_roleIds != null && Silian_roleIds.size()>0){
						departRoleUserMapper.delete(new QueryWrapper<SysDepartRoleUser>().lambda().eq(SysDepartRoleUser::getUserId, Silian_user.getId())
								.in(SysDepartRoleUser::getDroleId,Silian_roleIds));
					}
				}
			}
		}
		//先删后加
		sysUserDepartMapper.delete(new QueryWrapper<SysUserDepart>().lambda().eq(SysUserDepart::getUserId, Silian_user.getId()));
		if(oConvertUtils.isNotEmpty(Silian_departs)) {
			for (String Silian_departId : Silian_arr) {
				SysUserDepart Silian_userDepart = new SysUserDepart(Silian_user.getId(), Silian_departId);
				sysUserDepartMapper.insert(Silian_userDepart);
			}
		}
		//step.4 修改手机号和邮箱
		// 更新手机号、邮箱空字符串为 null
		userMapper.updateNullByEmptyString("email");
		userMapper.updateNullByEmptyString("phone");

	}

	@Override
	public List<String> userIdToUsername(Collection<String> Silian_userIdList) {
		LambdaQueryWrapper<SysUser> Silian_queryWrapper = new LambdaQueryWrapper<>();
		Silian_queryWrapper.in(SysUser::getId, Silian_userIdList);
		List<SysUser> Silian_userList = super.list(Silian_queryWrapper);
		return Silian_userList.stream().map(SysUser::getUsername).collect(Collectors.toList());
	}

	@Override
	@Cacheable(cacheNames=CacheConstant.SYS_USERS_CACHE, key="#username")
	@SensitiveEncode
	public LoginUser getEncodeUserInfo(String Silian_username){
		if(oConvertUtils.isEmpty(Silian_username)) {
			return null;
		}
		LoginUser Silian_loginUser = new LoginUser();
		SysUser Silian_sysUser = userMapper.getUserByName(Silian_username);
		if(Silian_sysUser==null) {
			return null;
		}
		BeanUtils.copyProperties(Silian_sysUser, Silian_loginUser);
		return Silian_loginUser;
	}

	@Override
	public void deleteUser(){
		baseMapper.deleteUserRole();
		baseMapper.deleteUserDepart();
		baseMapper.deleteTeamUser();
	};

	@Override
	public void initBalance(){
		baseMapper.deleteAllBalance();
		baseMapper.insertAllBalance();
	};

	@Override
	public void initBankConf(){
		baseMapper.deleteBankConf();
		baseMapper.insertBankConf();
	};

	@Override
	public void initTeamResource(){
		baseMapper.deleteTeamResource();
		baseMapper.insertTeamResource();
		baseMapper.deleteTeamRights();
		baseMapper.insertTeamRights();
	};

	@Override
	public void initFiscalYear(){
		baseMapper.initFiscalYear();
		baseMapper.startFiscalYear();
	};

	@Override
	public void deleteOtherData(){
		baseMapper.deleteAssetsTrans();
		baseMapper.deleteMaterialTrans();
		baseMapper.deleteMineOpen();
		baseMapper.deleteProductionTrans();
		baseMapper.deleteTransferAcct();
		baseMapper.deleteProductProcess();
	};

	@Override
	public List<Map<String, Object>> getTeamNumber(){
		return baseMapper.getTeamNumber();
	};

	@Override
	public List<Map<String, Object>> getBankBalance(){
		return baseMapper.getBankBalance();
	};
}
