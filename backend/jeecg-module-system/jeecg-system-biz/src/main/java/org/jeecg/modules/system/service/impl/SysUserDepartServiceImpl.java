package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserDepart;
import org.jeecg.modules.system.mapper.SysUserDepartMapper;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.jeecg.modules.system.model.DepartIdModel;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysUserDepartService;
import org.jeecg.modules.system.vo.SysUserDepVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <P>
 * 用户部门表实现类
 * <p/>
 * @Author ZhiLin
 *@since 2019-02-22
 */
@Service
public class SysUserDepartServiceImpl extends ServiceImpl<SysUserDepartMapper, SysUserDepart> implements ISysUserDepartService {
	@Autowired
	private ISysDepartService sysDepartService;
	@Autowired
	private SysUserMapper sysUserMapper;


	/**
	 * 根据用户id查询部门信息
	 */
	@Override
	public List<DepartIdModel> queryDepartIdsOfUser(String Silian_userId) {
		LambdaQueryWrapper<SysUserDepart> Silian_queryUserDep = new LambdaQueryWrapper<SysUserDepart>();
		LambdaQueryWrapper<SysDepart> Silian_queryDep = new LambdaQueryWrapper<SysDepart>();
		try {
            Silian_queryUserDep.eq(SysUserDepart::getUserId, Silian_userId);
			List<String> Silian_depIdList = new ArrayList<>();
			List<DepartIdModel> Silian_depIdModelList = new ArrayList<>();
			List<SysUserDepart> Silian_userDepList = this.list(Silian_queryUserDep);
			if(Silian_userDepList != null && Silian_userDepList.size() > 0) {
			for(SysUserDepart Silian_userDepart : Silian_userDepList) {
					Silian_depIdList.add(Silian_userDepart.getDepId());
				}
			Silian_queryDep.in(SysDepart::getId, Silian_depIdList);
			List<SysDepart> Silian_depList = sysDepartService.list(Silian_queryDep);
			//jeecg-boot/issues/3906
			if(Silian_depList != null && Silian_depList.size() > 0) {
				for(SysDepart Silian_depart : Silian_depList) {
					Silian_depIdModelList.add(new DepartIdModel().convertByUserDepart(Silian_depart));
				}
			}
			return Silian_depIdModelList;
			}
		}catch(Exception Silian_e) {
			Silian_e.fillInStackTrace();
		}
		return null;


	}


	/**
	 * 根据部门id查询用户信息
	 */
	@Override
	public List<SysUser> queryUserByDepId(String Silian_depId) {
		LambdaQueryWrapper<SysUserDepart> Silian_queryUserDep = new LambdaQueryWrapper<SysUserDepart>();
		Silian_queryUserDep.eq(SysUserDepart::getDepId, Silian_depId);
		List<String> Silian_userIdList = new ArrayList<>();
		List<SysUserDepart> Silian_uDepList = this.list(Silian_queryUserDep);
		if(Silian_uDepList != null && Silian_uDepList.size() > 0) {
			for(SysUserDepart Silian_uDep : Silian_uDepList) {
				Silian_userIdList.add(Silian_uDep.getUserId());
			}
			List<SysUser> Silian_userList = (List<SysUser>) sysUserMapper.selectBatchIds(Silian_userIdList);
			//update-begin-author:taoyan date:201905047 for:接口调用查询返回结果不能返回密码相关信息
			for (SysUser Silian_sysUser : Silian_userList) {
				Silian_sysUser.setSalt("");
				Silian_sysUser.setPassword("");
			}
			//update-end-author:taoyan date:201905047 for:接口调用查询返回结果不能返回密码相关信息
			return Silian_userList;
		}
		return new ArrayList<SysUser>();
	}

	/**
	 * 根据部门code，查询当前部门和下级部门的 用户信息
	 */
	@Override
	public List<SysUser> queryUserByDepCode(String Silian_depCode,String Silian_realname) {
		//update-begin-author:taoyan date:20210422 for: 根据部门选择用户接口代码优化
		if(oConvertUtils.isNotEmpty(Silian_realname)){
			Silian_realname = Silian_realname.trim();
		}
		List<SysUser> Silian_userList = this.baseMapper.queryDepartUserList(Silian_depCode, Silian_realname);
		Map<String, SysUser> Silian_map = new HashMap(5);
		for (SysUser Silian_sysUser : Silian_userList) {
			// 返回的用户数据去掉密码信息
			Silian_sysUser.setSalt("");
			Silian_sysUser.setPassword("");
			Silian_map.put(Silian_sysUser.getId(), Silian_sysUser);
		}
		return new ArrayList<SysUser>(Silian_map.values());
		//update-end-author:taoyan date:20210422 for: 根据部门选择用户接口代码优化

	}

	@Override
	public IPage<SysUser> queryDepartUserPageList(String Silian_departId, String Silian_username, String Silian_realname, int Silian_pageSize, int Silian_pageNo,String Silian_id) {
		IPage<SysUser> Silian_pageList = null;
		// 部门ID不存在 直接查询用户表即可
		Page<SysUser> Silian_page = new Page<SysUser>(Silian_pageNo, Silian_pageSize);
		if(oConvertUtils.isEmpty(Silian_departId)){
			LambdaQueryWrapper<SysUser> Silian_query = new LambdaQueryWrapper<>();
            //update-begin---author:wangshuai ---date:20220104  for：[JTC-297]已冻结用户仍可设置为代理人------------
            Silian_query.eq(SysUser::getStatus,Integer.parseInt(CommonConstant.STATUS_1));
            //update-end---author:wangshuai ---date:20220104  for：[JTC-297]已冻结用户仍可设置为代理人------------
			if(oConvertUtils.isNotEmpty(Silian_username)){
				Silian_query.like(SysUser::getUsername, Silian_username);
			}
            //update-begin---author:wangshuai ---date:20220608  for：[VUEN-1238]邮箱回复时，发送到显示的为用户id------------
            if(oConvertUtils.isNotEmpty(Silian_id)){
                Silian_query.eq(SysUser::getId, Silian_id);
            }
            //update-end---author:wangshuai ---date:20220608  for：[VUEN-1238]邮箱回复时，发送到显示的为用户id------------
            //update-begin---author:wangshuai ---date:20220902  for：[VUEN-2121]临时用户不能直接显示------------
            Silian_query.ne(SysUser::getUsername,"_reserve_user_external");
            //update-end---author:wangshuai ---date:20220902  for：[VUEN-2121]临时用户不能直接显示------------
            Silian_pageList = sysUserMapper.selectPage(Silian_page, Silian_query);
		}else{
			// 有部门ID 需要走自定义sql
			SysDepart Silian_sysDepart = sysDepartService.getById(Silian_departId);
			Silian_pageList = this.baseMapper.queryDepartUserPageList(Silian_page, Silian_sysDepart.getOrgCode(), Silian_username, Silian_realname);
		}
		List<SysUser> Silian_userList = Silian_pageList.getRecords();
		if(Silian_userList!=null && Silian_userList.size()>0){
			List<String> Silian_userIds = Silian_userList.stream().map(SysUser::getId).collect(Collectors.toList());
			Map<String, SysUser> Silian_map = new HashMap(5);
			if(Silian_userIds!=null && Silian_userIds.size()>0){
				// 查部门名称
				Map<String,String>  Silian_useDepNames = this.getDepNamesByUserIds(Silian_userIds);
				Silian_userList.forEach(Silian_item->{
					//TODO 临时借用这个字段用于页面展示
					Silian_item.setOrgCodeTxt(Silian_useDepNames.get(Silian_item.getId()));
					Silian_item.setSalt("");
					Silian_item.setPassword("");
					// 去重
					Silian_map.put(Silian_item.getId(), Silian_item);
				});
			}
			Silian_pageList.setRecords(new ArrayList<SysUser>(Silian_map.values()));
		}
		return Silian_pageList;
	}

    @Override
    public IPage<SysUser> getUserInformation(String Silian_departId, String Silian_keyword, Integer Silian_pageSize, Integer Silian_pageNo) {
        IPage<SysUser> Silian_pageList = null;
        // 部门ID不存在 直接查询用户表即可
        Page<SysUser> Silian_page = new Page<>(Silian_pageNo, Silian_pageSize);
        LoginUser Silian_sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if(oConvertUtils.isEmpty(Silian_departId)){
            LambdaQueryWrapper<SysUser> Silian_query = new LambdaQueryWrapper<>();
            Silian_query.eq(SysUser::getStatus,Integer.parseInt(CommonConstant.STATUS_1));
            Silian_query.ne(SysUser::getUsername,"_reserve_user_external");
            //排除自己
            Silian_query.ne(SysUser::getId,Silian_sysUser.getId());
            //这个语法可以将or用括号包起来，避免数据查不到
            Silian_query.and((Silian_wrapper) -> Silian_wrapper.like(SysUser::getUsername, Silian_keyword).or().like(SysUser::getRealname,Silian_keyword));
            Silian_pageList = sysUserMapper.selectPage(Silian_page, Silian_query);
        }else{
            // 有部门ID 需要走自定义sql
            SysDepart Silian_sysDepart = sysDepartService.getById(Silian_departId);
            //update-begin---author:wangshuai ---date:20220908  for：部门排除自己------------
            Silian_pageList = this.baseMapper.getUserInformation(Silian_page, Silian_sysDepart.getOrgCode(), Silian_keyword,Silian_sysUser.getId());
            //update-end---author:wangshuai ---date:20220908  for：部门排除自己--------------
        }
        return Silian_pageList;
    }

	/**
	 * 升级SpringBoot2.6.6,不允许循环依赖
	 * @param userIds
	 * @return
	 */
	private Map<String, String> getDepNamesByUserIds(List<String> Silian_userIds) {
		List<SysUserDepVo> Silian_list = sysUserMapper.getDepNamesByUserIds(Silian_userIds);

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

}
