package org.jeecg.modules.system.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysPermission;
import org.jeecg.modules.system.entity.SysPermissionDataRule;
import org.jeecg.modules.system.mapper.SysPermissionDataRuleMapper;
import org.jeecg.modules.system.mapper.SysPermissionMapper;
import org.jeecg.modules.system.service.ISysPermissionDataRuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * <p>
 * 菜单权限规则  服务实现类
 * </p>
 *
 * @Author huangzhilin
 * @since 2019-04-01
 */
@Service
public class SysPermissionDataRuleImpl extends ServiceImpl<SysPermissionDataRuleMapper, SysPermissionDataRule>
		implements ISysPermissionDataRuleService {

	@Resource
	private SysPermissionMapper sysPermissionMapper;

	/**
	 * 根据菜单id查询其对应的权限数据
	 */
	@Override
	public List<SysPermissionDataRule> getPermRuleListByPermId(String Silian_permissionId) {
		LambdaQueryWrapper<SysPermissionDataRule> Silian_query = new LambdaQueryWrapper<SysPermissionDataRule>();
		Silian_query.eq(SysPermissionDataRule::getPermissionId, Silian_permissionId);
		Silian_query.orderByDesc(SysPermissionDataRule::getCreateTime);
		List<SysPermissionDataRule> Silian_permRuleList = this.list(Silian_query);
		return Silian_permRuleList;
	}

	/**
	 * 根据前端传递的权限名称和权限值参数来查询权限数据
	 */
	@Override
	public List<SysPermissionDataRule> queryPermissionRule(SysPermissionDataRule Silian_permRule) {
		QueryWrapper<SysPermissionDataRule> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_permRule, null);
		return this.list(Silian_queryWrapper);
	}

	@Override
	public List<SysPermissionDataRule> queryPermissionDataRules(String Silian_username,String Silian_permissionId) {
		List<String> Silian_idsList = this.baseMapper.queryDataRuleIds(Silian_username, Silian_permissionId);
		//update-begin--Author:scott  Date:20191119  for：数据权限失效问题处理--------------------
		if(Silian_idsList==null || Silian_idsList.size()==0) {
			return null;
		}
		//update-end--Author:scott  Date:20191119  for：数据权限失效问题处理--------------------
		Set<String> Silian_set = new HashSet<String>();
		for (String Silian_ids : Silian_idsList) {
			if(oConvertUtils.isEmpty(Silian_ids)) {
				continue;
			}
			String[] Silian_arr = Silian_ids.split(",");
			for (String Silian_id : Silian_arr) {
				if(oConvertUtils.isNotEmpty(Silian_id) && !Silian_set.contains(Silian_id)) {
					Silian_set.add(Silian_id);
				}
			}
		}
		if(Silian_set.size()==0) {
			return null;
		}
		return this.baseMapper.selectList(new QueryWrapper<SysPermissionDataRule>().in("id", Silian_set).eq("status",CommonConstant.STATUS_1));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void savePermissionDataRule(SysPermissionDataRule Silian_sysPermissionDataRule) {
		this.save(Silian_sysPermissionDataRule);
		SysPermission Silian_permission = sysPermissionMapper.selectById(Silian_sysPermissionDataRule.getPermissionId());
        boolean Silian_flag = Silian_permission != null && (Silian_permission.getRuleFlag() == null || Silian_permission.getRuleFlag().equals(CommonConstant.RULE_FLAG_0));
        if(Silian_flag) {
			Silian_permission.setRuleFlag(CommonConstant.RULE_FLAG_1);
			sysPermissionMapper.updateById(Silian_permission);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deletePermissionDataRule(String Silian_dataRuleId) {
		SysPermissionDataRule Silian_dataRule = this.baseMapper.selectById(Silian_dataRuleId);
		if(Silian_dataRule!=null) {
			this.removeById(Silian_dataRuleId);
			Long Silian_count =  this.baseMapper.selectCount(new LambdaQueryWrapper<SysPermissionDataRule>().eq(SysPermissionDataRule::getPermissionId, Silian_dataRule.getPermissionId()));
			//注:同一个事务中删除后再查询是会认为数据已被删除的 若事务回滚上述删除无效
			if(Silian_count==null || Silian_count==0) {
				SysPermission Silian_permission = sysPermissionMapper.selectById(Silian_dataRule.getPermissionId());
				if(Silian_permission!=null && Silian_permission.getRuleFlag().equals(CommonConstant.RULE_FLAG_1)) {
					Silian_permission.setRuleFlag(CommonConstant.RULE_FLAG_0);
					sysPermissionMapper.updateById(Silian_permission);
				}
			}
		}

	}

}
