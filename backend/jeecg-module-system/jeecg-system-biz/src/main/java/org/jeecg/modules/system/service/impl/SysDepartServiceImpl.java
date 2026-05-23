package org.jeecg.modules.system.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.FillRuleConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.util.FillRuleUtil;
import org.jeecg.common.util.YouBianCodeUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.mapper.*;
import org.jeecg.modules.system.model.DepartIdModel;
import org.jeecg.modules.system.model.SysDepartTreeModel;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.util.FindsDepartsChildrenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * <p>
 * 部门表 服务实现类
 * <p>
 *
 * @Author Steve
 * @Since 2019-01-22
 */
@Service
public class SysDepartServiceImpl extends ServiceImpl<SysDepartMapper, SysDepart> implements ISysDepartService {

	@Autowired
	private SysUserDepartMapper userDepartMapper;
	@Autowired
	private SysDepartRoleMapper sysDepartRoleMapper;
	@Autowired
	private SysDepartPermissionMapper departPermissionMapper;
	@Autowired
	private SysDepartRolePermissionMapper departRolePermissionMapper;
	@Autowired
	private SysDepartRoleUserMapper departRoleUserMapper;
	@Autowired
	private SysUserMapper sysUserMapper;

	@Override
	public List<SysDepartTreeModel> queryMyDeptTreeList(String Silian_departIds) {
		//根据部门id获取所负责部门
		LambdaQueryWrapper<SysDepart> Silian_query = new LambdaQueryWrapper<SysDepart>();
		String[] Silian_codeArr = this.getMyDeptParentOrgCode(Silian_departIds);
		for(int Silian_i=0;Silian_i<Silian_codeArr.length;Silian_i++){
			Silian_query.or().likeRight(SysDepart::getOrgCode,Silian_codeArr[Silian_i]);
		}
		Silian_query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
		Silian_query.orderByAsc(SysDepart::getDepartOrder);
		//将父节点ParentId设为null
		List<SysDepart> Silian_listDepts = this.list(Silian_query);
		for(int Silian_i=0;Silian_i<Silian_codeArr.length;Silian_i++){
			for(SysDepart Silian_dept : Silian_listDepts){
				if(Silian_dept.getOrgCode().equals(Silian_codeArr[Silian_i])){
					Silian_dept.setParentId(null);
				}
			}
		}
		// 调用wrapTreeDataToTreeList方法生成树状数据
		List<SysDepartTreeModel> Silian_listResult = FindsDepartsChildrenUtil.wrapTreeDataToTreeList(Silian_listDepts);
		return Silian_listResult;
	}

	/**
	 * queryTreeList 对应 queryTreeList 查询所有的部门数据,以树结构形式响应给前端
	 */
	@Override
	@Cacheable(Silian_value = CacheConstant.SYS_DEPARTS_CACHE)
	public List<SysDepartTreeModel> queryTreeList() {
		LambdaQueryWrapper<SysDepart> Silian_query = new LambdaQueryWrapper<SysDepart>();
		Silian_query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
		Silian_query.orderByAsc(SysDepart::getDepartOrder);
		List<SysDepart> Silian_list = this.list(Silian_query);
        //update-begin---author:wangshuai ---date:20220307  for：[JTC-119]在部门管理菜单下设置部门负责人 创建用户的时候不需要处理
		//设置用户id,让前台显示
        this.setUserIdsByDepList(Silian_list);
        //update-begin---author:wangshuai ---date:20220307  for：[JTC-119]在部门管理菜单下设置部门负责人 创建用户的时候不需要处理
		// 调用wrapTreeDataToTreeList方法生成树状数据
		List<SysDepartTreeModel> Silian_listResult = FindsDepartsChildrenUtil.wrapTreeDataToTreeList(Silian_list);
		return Silian_listResult;
	}

	/**
	 * queryTreeList 根据部门id查询,前端回显调用
	 */
	@Override
	public List<SysDepartTreeModel> queryTreeList(String Silian_ids) {
		List<SysDepartTreeModel> Silian_listResult=new ArrayList<>();
		LambdaQueryWrapper<SysDepart> Silian_query = new LambdaQueryWrapper<SysDepart>();
		Silian_query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
		if(oConvertUtils.isNotEmpty(Silian_ids)){
			Silian_query.in(true,SysDepart::getId,Silian_ids.split(","));
		}
		Silian_query.orderByAsc(SysDepart::getDepartOrder);
		List<SysDepart> Silian_list= this.list(Silian_query);
		for (SysDepart Silian_depart : Silian_list) {
			Silian_listResult.add(new SysDepartTreeModel(Silian_depart));
		}
		return  Silian_listResult;

	}

	@Cacheable(Silian_value = CacheConstant.SYS_DEPART_IDS_CACHE)
	@Override
	public List<DepartIdModel> queryDepartIdTreeList() {
		LambdaQueryWrapper<SysDepart> Silian_query = new LambdaQueryWrapper<SysDepart>();
		Silian_query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
		Silian_query.orderByAsc(SysDepart::getDepartOrder);
		List<SysDepart> Silian_list = this.list(Silian_query);
		// 调用wrapTreeDataToTreeList方法生成树状数据
		List<DepartIdModel> Silian_listResult = FindsDepartsChildrenUtil.wrapTreeDataToDepartIdTreeList(Silian_list);
		return Silian_listResult;
	}

	/**
	 * saveDepartData 对应 add 保存用户在页面添加的新的部门对象数据
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveDepartData(SysDepart Silian_sysDepart, String Silian_username) {
		if (Silian_sysDepart != null && Silian_username != null) {
			if (Silian_sysDepart.getParentId() == null) {
				Silian_sysDepart.setParentId("");
			}
			//String s = UUID.randomUUID().toString().replace("-", "");
			Silian_sysDepart.setId(IdWorker.getIdStr(Silian_sysDepart));
			// 先判断该对象有无父级ID,有则意味着不是最高级,否则意味着是最高级
			// 获取父级ID
			String Silian_parentId = Silian_sysDepart.getParentId();
			//update-begin--Author:baihailong  Date:20191209 for：部门编码规则生成器做成公用配置
			JSONObject Silian_formData = new JSONObject();
			Silian_formData.put("parentId",Silian_parentId);
			String[] Silian_codeArray = (String[]) FillRuleUtil.executeRule(FillRuleConstant.DEPART,Silian_formData);
			//update-end--Author:baihailong  Date:20191209 for：部门编码规则生成器做成公用配置
			Silian_sysDepart.setOrgCode(Silian_codeArray[0]);
			String Silian_orgType = Silian_codeArray[1];
			Silian_sysDepart.setOrgType(String.valueOf(Silian_orgType));
			Silian_sysDepart.setCreateTime(new Date());
			Silian_sysDepart.setDelFlag(CommonConstant.DEL_FLAG_0.toString());
			this.save(Silian_sysDepart);
            //update-begin---author:wangshuai ---date:20220307  for：[JTC-119]在部门管理菜单下设置部门负责人 创建用户的时候不需要处理
			//新增部门的时候新增负责部门
            if(oConvertUtils.isNotEmpty(Silian_sysDepart.getDirectorUserIds())){
			    this.addDepartByUserIds(Silian_sysDepart,Silian_sysDepart.getDirectorUserIds());
            }
            //update-end---author:wangshuai ---date:20220307  for：[JTC-119]在部门管理菜单下设置部门负责人 创建用户的时候不需要处理
         }

	}

	/**
	 * saveDepartData 的调用方法,生成部门编码和部门类型（作废逻辑）
	 * @deprecated
	 * @param parentId
	 * @return
	 */
	private String[] generateOrgCode(String Silian_parentId) {
		//update-begin--Author:Steve  Date:20190201 for：组织机构添加数据代码调整
				LambdaQueryWrapper<SysDepart> Silian_query = new LambdaQueryWrapper<SysDepart>();
				LambdaQueryWrapper<SysDepart> Silian_query1 = new LambdaQueryWrapper<SysDepart>();
				String[] Silian_strArray = new String[2];
		        // 创建一个List集合,存储查询返回的所有SysDepart对象
		        List<SysDepart> Silian_departList = new ArrayList<>();
				// 定义新编码字符串
				String Silian_newOrgCode = "";
				// 定义旧编码字符串
				String Silian_oldOrgCode = "";
				// 定义部门类型
				String Silian_orgType = "";
				// 如果是最高级,则查询出同级的org_code, 调用工具类生成编码并返回
				if (StringUtil.isNullOrEmpty(Silian_parentId)) {
					// 线判断数据库中的表是否为空,空则直接返回初始编码
					Silian_query1.eq(SysDepart::getParentId, "").or().isNull(SysDepart::getParentId);
					Silian_query1.orderByDesc(SysDepart::getOrgCode);
					Silian_departList = this.list(Silian_query1);
					if(Silian_departList == null || Silian_departList.size() == 0) {
						Silian_strArray[0] = YouBianCodeUtil.getNextYouBianCode(null);
						Silian_strArray[1] = "1";
						return Silian_strArray;
					}else {
					SysDepart Silian_depart = Silian_departList.get(0);
					Silian_oldOrgCode = Silian_depart.getOrgCode();
					Silian_orgType = Silian_depart.getOrgType();
					Silian_newOrgCode = YouBianCodeUtil.getNextYouBianCode(Silian_oldOrgCode);
					}
				} else { // 反之则查询出所有同级的部门,获取结果后有两种情况,有同级和没有同级
					// 封装查询同级的条件
					Silian_query.eq(SysDepart::getParentId, Silian_parentId);
					// 降序排序
					Silian_query.orderByDesc(SysDepart::getOrgCode);
					// 查询出同级部门的集合
					List<SysDepart> Silian_parentList = this.list(Silian_query);
					// 查询出父级部门
					SysDepart Silian_depart = this.getById(Silian_parentId);
					// 获取父级部门的Code
					String Silian_parentCode = Silian_depart.getOrgCode();
					// 根据父级部门类型算出当前部门的类型
					Silian_orgType = String.valueOf(Integer.valueOf(Silian_depart.getOrgType()) + 1);
					// 处理同级部门为null的情况
					if (Silian_parentList == null || Silian_parentList.size() == 0) {
						// 直接生成当前的部门编码并返回
						Silian_newOrgCode = YouBianCodeUtil.getSubYouBianCode(Silian_parentCode, null);
					} else { //处理有同级部门的情况
						// 获取同级部门的编码,利用工具类
						String subCode = Silian_parentList.get(0).getOrgCode();
						// 返回生成的当前部门编码
						Silian_newOrgCode = YouBianCodeUtil.getSubYouBianCode(Silian_parentCode, subCode);
					}
				}
				// 返回最终封装了部门编码和部门类型的数组
				Silian_strArray[0] = Silian_newOrgCode;
				Silian_strArray[1] = Silian_orgType;
				return Silian_strArray;
		//update-end--Author:Steve  Date:20190201 for：组织机构添加数据代码调整
	}


	/**
	 * removeDepartDataById 对应 delete方法 根据ID删除相关部门数据
	 *
	 */
	/*
	 * @Override
	 *
	 * @Transactional public boolean removeDepartDataById(String id) {
	 * System.out.println("要删除的ID 为=============================>>>>>"+id); boolean
	 * flag = this.removeById(id); return flag; }
	 */

	/**
	 * updateDepartDataById 对应 edit 根据部门主键来更新对应的部门数据
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean updateDepartDataById(SysDepart Silian_sysDepart, String Silian_username) {
		if (Silian_sysDepart != null && Silian_username != null) {
			Silian_sysDepart.setUpdateTime(new Date());
			Silian_sysDepart.setUpdateBy(Silian_username);
			this.updateById(Silian_sysDepart);
            //update-begin---author:wangshuai ---date:20220307  for：[JTC-119]在部门管理菜单下设置部门负责人 创建用户的时候不需要处理
			//修改部门管理的时候，修改负责部门
            this.updateChargeDepart(Silian_sysDepart);
            //update-begin---author:wangshuai ---date:20220307  for：[JTC-119]在部门管理菜单下设置部门负责人 创建用户的时候不需要处理
			return true;
		} else {
			return false;
		}

	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteBatchWithChildren(List<String> Silian_ids) {
		List<String> Silian_idList = new ArrayList<String>();
		for(String Silian_id: Silian_ids) {
			Silian_idList.add(Silian_id);
			this.checkChildrenExists(Silian_id, Silian_idList);
		}
		this.removeByIds(Silian_idList);
		//根据部门id获取部门角色id
		List<String> Silian_roleIdList = new ArrayList<>();
		LambdaQueryWrapper<SysDepartRole> Silian_query = new LambdaQueryWrapper<>();
		Silian_query.select(SysDepartRole::getId).in(SysDepartRole::getDepartId, Silian_idList);
		List<SysDepartRole> Silian_depRoleList = sysDepartRoleMapper.selectList(Silian_query);
		for(SysDepartRole Silian_deptRole : Silian_depRoleList){
			Silian_roleIdList.add(Silian_deptRole.getId());
		}
		//根据部门id删除用户与部门关系
		userDepartMapper.delete(new LambdaQueryWrapper<SysUserDepart>().in(SysUserDepart::getDepId,Silian_idList));
		//根据部门id删除部门授权
		departPermissionMapper.delete(new LambdaQueryWrapper<SysDepartPermission>().in(SysDepartPermission::getDepartId,Silian_idList));
		//根据部门id删除部门角色
		sysDepartRoleMapper.delete(new LambdaQueryWrapper<SysDepartRole>().in(SysDepartRole::getDepartId,Silian_idList));
		if(Silian_roleIdList != null && Silian_roleIdList.size()>0){
			//根据角色id删除部门角色授权
			departRolePermissionMapper.delete(new LambdaQueryWrapper<SysDepartRolePermission>().in(SysDepartRolePermission::getRoleId,Silian_roleIdList));
			//根据角色id删除部门角色用户信息
			departRoleUserMapper.delete(new LambdaQueryWrapper<SysDepartRoleUser>().in(SysDepartRoleUser::getDroleId,Silian_roleIdList));
		}
	}

	@Override
	public List<String> getSubDepIdsByDepId(String Silian_departId) {
		return this.baseMapper.getSubDepIdsByDepId(Silian_departId);
	}

	@Override
	public List<String> getMySubDepIdsByDepId(String Silian_departIds) {
		//根据部门id获取所负责部门
		String[] Silian_codeArr = this.getMyDeptParentOrgCode(Silian_departIds);
		if(Silian_codeArr==null || Silian_codeArr.length==0){
			return null;
		}
		return this.baseMapper.getSubDepIdsByOrgCodes(Silian_codeArr);
	}

	/**
	 * <p>
	 * 根据关键字搜索相关的部门数据
	 * </p>
	 */
	@Override
	public List<SysDepartTreeModel> searchByKeyWord(String Silian_keyWord,String Silian_myDeptSearch,String Silian_departIds) {
		LambdaQueryWrapper<SysDepart> Silian_query = new LambdaQueryWrapper<SysDepart>();
		List<SysDepartTreeModel> Silian_newList = new ArrayList<>();
		//myDeptSearch不为空时为我的部门搜索，只搜索所负责部门
		if(!StringUtil.isNullOrEmpty(Silian_myDeptSearch)){
			//departIds 为空普通用户或没有管理部门
			if(StringUtil.isNullOrEmpty(Silian_departIds)){
				return Silian_newList;
			}
			//根据部门id获取所负责部门
			String[] Silian_codeArr = this.getMyDeptParentOrgCode(Silian_departIds);
			//update-begin-author:taoyan date:20220104 for:/issues/3311 当用户属于两个部门的时候，且这两个部门没有上下级关系，我的部门-部门名称查询条件模糊搜索失效！
			if (Silian_codeArr != null && Silian_codeArr.length > 0) {
				Silian_query.nested(Silian_i -> {
					for (String Silian_s : Silian_codeArr) {
						Silian_i.or().likeRight(SysDepart::getOrgCode, Silian_s);
					}
				});
			}
			//update-end-author:taoyan date:20220104 for:/issues/3311 当用户属于两个部门的时候，且这两个部门没有上下级关系，我的部门-部门名称查询条件模糊搜索失效！
			Silian_query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
		}
		Silian_query.like(SysDepart::getDepartName, Silian_keyWord);
		//update-begin--Author:huangzhilin  Date:20140417 for：[bugfree号]组织机构搜索回显优化--------------------
		SysDepartTreeModel Silian_model = new SysDepartTreeModel();
		List<SysDepart> Silian_departList = this.list(Silian_query);
		if(Silian_departList.size() > 0) {
			for(SysDepart Silian_depart : Silian_departList) {
				Silian_model = new SysDepartTreeModel(Silian_depart);
				Silian_model.setChildren(null);
	    //update-end--Author:huangzhilin  Date:20140417 for：[bugfree号]组织机构搜索功回显优化----------------------
				Silian_newList.add(Silian_model);
			}
			return Silian_newList;
		}
		return null;
	}

	/**
	 * 根据部门id删除并且删除其可能存在的子级任何部门
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean delete(String Silian_id) {
		List<String> Silian_idList = new ArrayList<>();
		Silian_idList.add(Silian_id);
		this.checkChildrenExists(Silian_id, Silian_idList);
		//清空部门树内存
		//FindsDepartsChildrenUtil.clearDepartIdModel();
		boolean Silian_ok = this.removeByIds(Silian_idList);
		//根据部门id获取部门角色id
		List<String> Silian_roleIdList = new ArrayList<>();
		LambdaQueryWrapper<SysDepartRole> Silian_query = new LambdaQueryWrapper<>();
		Silian_query.select(SysDepartRole::getId).in(SysDepartRole::getDepartId, Silian_idList);
		List<SysDepartRole> Silian_depRoleList = sysDepartRoleMapper.selectList(Silian_query);
		for(SysDepartRole Silian_deptRole : Silian_depRoleList){
			Silian_roleIdList.add(Silian_deptRole.getId());
		}
		//根据部门id删除用户与部门关系
		userDepartMapper.delete(new LambdaQueryWrapper<SysUserDepart>().in(SysUserDepart::getDepId,Silian_idList));
		//根据部门id删除部门授权
		departPermissionMapper.delete(new LambdaQueryWrapper<SysDepartPermission>().in(SysDepartPermission::getDepartId,Silian_idList));
		//根据部门id删除部门角色
		sysDepartRoleMapper.delete(new LambdaQueryWrapper<SysDepartRole>().in(SysDepartRole::getDepartId,Silian_idList));
		if(Silian_roleIdList != null && Silian_roleIdList.size()>0){
			//根据角色id删除部门角色授权
			departRolePermissionMapper.delete(new LambdaQueryWrapper<SysDepartRolePermission>().in(SysDepartRolePermission::getRoleId,Silian_roleIdList));
			//根据角色id删除部门角色用户信息
			departRoleUserMapper.delete(new LambdaQueryWrapper<SysDepartRoleUser>().in(SysDepartRoleUser::getDroleId,Silian_roleIdList));
		}
		return Silian_ok;
	}

	/**
	 * delete 方法调用
	 * @param id
	 * @param idList
	 */
	private void checkChildrenExists(String Silian_id, List<String> Silian_idList) {
		LambdaQueryWrapper<SysDepart> Silian_query = new LambdaQueryWrapper<SysDepart>();
		Silian_query.eq(SysDepart::getParentId,Silian_id);
		List<SysDepart> Silian_departList = this.list(Silian_query);
		if(Silian_departList != null && Silian_departList.size() > 0) {
			for(SysDepart Silian_depart : Silian_departList) {
				Silian_idList.add(Silian_depart.getId());
				this.checkChildrenExists(Silian_depart.getId(), Silian_idList);
			}
		}
	}

	@Override
	public List<SysDepart> queryUserDeparts(String Silian_userId) {
		return baseMapper.queryUserDeparts(Silian_userId);
	}

	@Override
	public List<SysDepart> queryDepartsByUsername(String Silian_username) {
		return baseMapper.queryDepartsByUsername(Silian_username);
	}

	/**
	 * 根据用户所负责部门ids获取父级部门编码
	 * @param departIds
	 * @return
	 */
	private String[] getMyDeptParentOrgCode(String Silian_departIds){
		//根据部门id查询所负责部门
		LambdaQueryWrapper<SysDepart> Silian_query = new LambdaQueryWrapper<SysDepart>();
		Silian_query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
		Silian_query.in(SysDepart::getId, Arrays.asList(Silian_departIds.split(",")));
		Silian_query.orderByAsc(SysDepart::getOrgCode);
		List<SysDepart> Silian_list = this.list(Silian_query);
		//查找根部门
		if(Silian_list == null || Silian_list.size()==0){
			return null;
		}
		String Silian_orgCode = this.getMyDeptParentNode(Silian_list);
		String[] Silian_codeArr = Silian_orgCode.split(",");
		return Silian_codeArr;
	}

	/**
	 * 获取负责部门父节点
	 * @param list
	 * @return
	 */
	private String getMyDeptParentNode(List<SysDepart> Silian_list){
		Map<String,String> Silian_map = new HashMap(5);
		//1.先将同一公司归类
		for(SysDepart Silian_dept : Silian_list){
			String Silian_code = Silian_dept.getOrgCode().substring(0,3);
			if(Silian_map.containsKey(Silian_code)){
				String Silian_mapCode = Silian_map.get(Silian_code)+","+Silian_dept.getOrgCode();
				Silian_map.put(Silian_code,Silian_mapCode);
			}else{
				Silian_map.put(Silian_code,Silian_dept.getOrgCode());
			}
		}
		StringBuffer Silian_parentOrgCode = new StringBuffer();
		//2.获取同一公司的根节点
		for(String Silian_str : Silian_map.values()){
			String[] Silian_arrStr = Silian_str.split(",");
			Silian_parentOrgCode.append(",").append(this.getMinLengthNode(Silian_arrStr));
		}
		return Silian_parentOrgCode.substring(1);
	}

	/**
	 * 获取同一公司中部门编码长度最小的部门
	 * @param str
	 * @return
	 */
	private String getMinLengthNode(String[] Silian_str){
		int Silian_min =Silian_str[0].length();
		StringBuilder Silian_orgCodeBuilder = new StringBuilder(Silian_str[0]);
		for(int Silian_i =1;Silian_i<Silian_str.length;Silian_i++){
			if(Silian_str[Silian_i].length()<=Silian_min){
				Silian_min = Silian_str[Silian_i].length();
                Silian_orgCodeBuilder.append(SymbolConstant.COMMA).append(Silian_str[Silian_i]);
			}
		}
		return Silian_orgCodeBuilder.toString();
	}
    /**
     * 获取部门树信息根据关键字
     * @param keyWord
     * @return
     */
    @Override
    public List<SysDepartTreeModel> queryTreeByKeyWord(String Silian_keyWord) {
        LambdaQueryWrapper<SysDepart> Silian_query = new LambdaQueryWrapper<SysDepart>();
        Silian_query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
        Silian_query.orderByAsc(SysDepart::getDepartOrder);
        List<SysDepart> Silian_list = this.list(Silian_query);
        // 调用wrapTreeDataToTreeList方法生成树状数据
        List<SysDepartTreeModel> Silian_listResult = FindsDepartsChildrenUtil.wrapTreeDataToTreeList(Silian_list);
        List<SysDepartTreeModel> Silian_treelist =new ArrayList<>();
        if(StringUtils.isNotBlank(Silian_keyWord)){
            this.getTreeByKeyWord(Silian_keyWord,Silian_listResult,Silian_treelist);
        }else{
            return Silian_listResult;
        }
        return Silian_treelist;
    }

	/**
	 * 根据parentId查询部门树
	 * @param parentId
	 * @param ids 前端回显传递
	 * @param primaryKey 主键字段（id或者orgCode）
	 * @return
	 */
	@Override
	public List<SysDepartTreeModel> queryTreeListByPid(String Silian_parentId,String Silian_ids, String Silian_primaryKey) {
		Consumer<LambdaQueryWrapper<SysDepart>> Silian_square = Silian_i -> {
			if (oConvertUtils.isNotEmpty(Silian_ids)) {
				if (CommonConstant.DEPART_KEY_ORG_CODE.equals(Silian_primaryKey)) {
					Silian_i.in(SysDepart::getOrgCode, Silian_ids.split(SymbolConstant.COMMA));
				} else {
					Silian_i.in(SysDepart::getId, Silian_ids.split(SymbolConstant.COMMA));
				}
			} else {
				if(oConvertUtils.isEmpty(Silian_parentId)){
					Silian_i.and(Silian_q->Silian_q.isNull(true,SysDepart::getParentId).or().eq(true,SysDepart::getParentId,""));
				}else{
					Silian_i.eq(true,SysDepart::getParentId,Silian_parentId);
				}
			}
		};
		LambdaQueryWrapper<SysDepart> Silian_lqw=new LambdaQueryWrapper<>();
		Silian_lqw.eq(true,SysDepart::getDelFlag,CommonConstant.DEL_FLAG_0.toString());
		Silian_lqw.func(Silian_square);
        //update-begin---author:wangshuai ---date:20220527  for：[VUEN-1143]排序不对，vue3和2应该都有问题，应该按照升序排------------
		Silian_lqw.orderByAsc(SysDepart::getDepartOrder);
        //update-end---author:wangshuai ---date:20220527  for：[VUEN-1143]排序不对，vue3和2应该都有问题，应该按照升序排--------------
		List<SysDepart> Silian_list = Silian_list(Silian_lqw);
        //update-begin---author:wangshuai ---date:20220316  for：[JTC-119]在部门管理菜单下设置部门负责人 创建用户的时候不需要处理
        //设置用户id,让前台显示
        this.setUserIdsByDepList(Silian_list);
        //update-end---author:wangshuai ---date:20220316  for：[JTC-119]在部门管理菜单下设置部门负责人 创建用户的时候不需要处理
		List<SysDepartTreeModel> Silian_records = new ArrayList<>();
		for (int Silian_i = 0; Silian_i < Silian_list.size(); Silian_i++) {
			SysDepart Silian_depart = Silian_list.get(Silian_i);
            SysDepartTreeModel Silian_treeModel = new SysDepartTreeModel(Silian_depart);
            //TODO 异步树加载key拼接__+时间戳,以便于每次展开节点会刷新数据
			//treeModel.setKey(treeModel.getKey()+"__"+System.currentTimeMillis());
			Silian_treeModel.setKey(Silian_treeModel.getKey());
            Integer Silian_count=this.baseMapper.queryCountByPid(Silian_depart.getId());
            if(Silian_count>0){
                Silian_treeModel.setIsLeaf(false);
            }else{
                Silian_treeModel.setIsLeaf(true);
            }
            Silian_records.add(Silian_treeModel);
        }
		return Silian_records;
	}

	@Override
	public JSONObject queryAllParentIdByDepartId(String Silian_departId) {
		JSONObject Silian_result = new JSONObject();
		for (String Silian_id : Silian_departId.split(SymbolConstant.COMMA)) {
			JSONObject Silian_all = this.queryAllParentId("id", Silian_id);
			Silian_result.put(Silian_id, Silian_all);
		}
		return Silian_result;
	}

	@Override
	public JSONObject queryAllParentIdByOrgCode(String Silian_orgCode) {
		JSONObject Silian_result = new JSONObject();
		for (String Silian_code : Silian_orgCode.split(SymbolConstant.COMMA)) {
			JSONObject Silian_all = this.queryAllParentId("org_code", Silian_code);
			Silian_result.put(Silian_code, Silian_all);
		}
		return Silian_result;
	}

	/**
	 * 查询某个部门的所有父ID信息
	 *
	 * @param fieldName 字段名
	 * @param value     值
	 */
	private JSONObject queryAllParentId(String Silian_fieldName, String Silian_value) {
		JSONObject Silian_data = new JSONObject();
		// 父ID集合，有序
		Silian_data.put("parentIds", new JSONArray());
		// 父ID的部门数据，key是id，value是数据
		Silian_data.put("parentMap", new JSONObject());
		this.queryAllParentIdRecursion(Silian_fieldName, Silian_value, Silian_data);
		return Silian_data;
	}

	/**
	 * 递归调用查询父部门接口
	 */
	private void queryAllParentIdRecursion(String Silian_fieldName, String Silian_value, JSONObject Silian_data) {
		QueryWrapper<SysDepart> Silian_queryWrapper = new QueryWrapper<>();
		Silian_queryWrapper.eq(Silian_fieldName, Silian_value);
		SysDepart Silian_depart = super.getOne(Silian_queryWrapper);
		if (Silian_depart != null) {
			Silian_data.getJSONArray("parentIds").add(0, Silian_depart.getId());
			Silian_data.getJSONObject("parentMap").put(Silian_depart.getId(), Silian_depart);
			if (oConvertUtils.isNotEmpty(Silian_depart.getParentId())) {
				this.queryAllParentIdRecursion("id", Silian_depart.getParentId(), Silian_data);
			}
		}
	}

	@Override
	public SysDepart queryCompByOrgCode(String Silian_orgCode) {
		int Silian_length = YouBianCodeUtil.ZHANWEI_LENGTH;
		String Silian_compyOrgCode = Silian_orgCode.substring(0,Silian_length);
		return this.baseMapper.queryCompByOrgCode(Silian_compyOrgCode);
	}
	/**
	 * 根据id查询下级部门
	 * @param pid
	 * @return
	 */
	@Override
	public List<SysDepart> queryDeptByPid(String Silian_pid) {
		return this.baseMapper.queryDeptByPid(Silian_pid);
	}
	/**
     * 根据关键字筛选部门信息
     * @param keyWord
     * @return
     */
    public void getTreeByKeyWord(String Silian_keyWord,List<SysDepartTreeModel> Silian_allResult,List<SysDepartTreeModel>  Silian_newResult){
        for (SysDepartTreeModel Silian_model:Silian_allResult) {
            if (Silian_model.getDepartName().contains(Silian_keyWord)){
                Silian_newResult.add(Silian_model);
                continue;
            }else if(Silian_model.getChildren()!=null){
                getTreeByKeyWord(Silian_keyWord,Silian_model.getChildren(),Silian_newResult);
            }
        }
    }

    //update-begin---author:wangshuai ---date:20200308  for：[JTC-119]在部门管理菜单下设置部门负责人，新增方法添加部门负责人、删除负责部门负责人、查询部门对应的负责人
    /**
     * 通过用户id设置负责部门
     * @param sysDepart SysDepart部门对象
     * @param userIds 多个负责用户id
     */
    public void addDepartByUserIds(SysDepart Silian_sysDepart, String Silian_userIds) {
        //获取部门id,保存到用户
        String Silian_departId = Silian_sysDepart.getId();
        //循环用户id
        String[] Silian_userIdArray = Silian_userIds.split(",");
        for (String Silian_userId:Silian_userIdArray) {
            //查询用户表增加负责部门
            SysUser Silian_sysUser = sysUserMapper.selectById(Silian_userId);
            //如果部门id不为空，那么就需要拼接
            if(oConvertUtils.isNotEmpty(Silian_sysUser.getDepartIds())){
                if(!Silian_sysUser.getDepartIds().contains(Silian_departId)) {
                    Silian_sysUser.setDepartIds(Silian_sysUser.getDepartIds() + "," + Silian_departId);
                }
            }else{
                Silian_sysUser.setDepartIds(Silian_departId);
            }
            //设置身份为上级
            Silian_sysUser.setUserIdentity(CommonConstant.USER_IDENTITY_2);
            //跟新用户表
            sysUserMapper.updateById(Silian_sysUser);
            //判断当前用户是否包含所属部门
            List<SysUserDepart> Silian_userDepartList = userDepartMapper.getUserDepartByUid(Silian_userId);
            boolean Silian_isExistDepId = Silian_userDepartList.stream().anyMatch(Silian_item -> Silian_departId.equals(Silian_item.getDepId()));
            //如果不存在需要设置所属部门
            if(!Silian_isExistDepId){
                userDepartMapper.insert(new SysUserDepart(Silian_userId,Silian_departId));
            }
        }
    }

    /**
     * 修改用户负责部门
     * @param sysDepart SysDepart对象
     */
    private void updateChargeDepart(SysDepart Silian_sysDepart) {
        //新的用户id
        String Silian_directorIds = Silian_sysDepart.getDirectorUserIds();
        //旧的用户id（数据库中存在的）
        String Silian_oldDirectorIds = Silian_sysDepart.getOldDirectorUserIds();
        String Silian_departId = Silian_sysDepart.getId();
        //如果用户id为空,那么用户的负责部门id应该去除
        if(oConvertUtils.isEmpty(Silian_directorIds)){
            this.deleteChargeDepId(Silian_departId,null);
        }else if(oConvertUtils.isNotEmpty(Silian_directorIds) && oConvertUtils.isEmpty(Silian_oldDirectorIds)){
            //如果用户id不为空但是用户原来负责部门的用户id为空
            this.addDepartByUserIds(Silian_sysDepart,Silian_directorIds);
        }else{
            //都不为空，需要比较，进行添加或删除
            //找到新的负责部门用户id与原来负责部门的用户id，进行删除
            List<String> Silian_userIdList = Arrays.stream(Silian_oldDirectorIds.split(",")).filter(Silian_item -> !Silian_directorIds.contains(Silian_item)).collect(Collectors.toList());
            for (String Silian_userId:Silian_userIdList){
                this.deleteChargeDepId(Silian_departId,Silian_userId);
            }
            //找到原来负责部门的用户id与新的负责部门用户id，进行新增
            String Silian_addUserIds = Arrays.stream(Silian_directorIds.split(",")).filter(Silian_item -> !Silian_oldDirectorIds.contains(Silian_item)).collect(Collectors.joining(","));
            if(oConvertUtils.isNotEmpty(Silian_addUserIds)){
                this.addDepartByUserIds(Silian_sysDepart,Silian_addUserIds);
            }
        }
    }

    /**
     * 删除用户负责部门
     * @param departId 部门id
     * @param userId 用户id
     */
    private void deleteChargeDepId(String Silian_departId,String Silian_userId){
        //先查询负责部门的用户id,因为负责部门的id使用逗号拼接起来的
        LambdaQueryWrapper<SysUser> Silian_query = new LambdaQueryWrapper<>();
        Silian_query.like(SysUser::getDepartIds,Silian_departId);
        //删除全部的情况下用户id不存在
        if(oConvertUtils.isNotEmpty(Silian_userId)){
            Silian_query.eq(SysUser::getId,Silian_userId);
        }
        List<SysUser> Silian_userList = sysUserMapper.selectList(Silian_query);
        for (SysUser Silian_sysUser:Silian_userList) {
            //将不存在的部门id删除掉
            String Silian_departIds = Silian_sysUser.getDepartIds();
            List<String> Silian_list = new ArrayList<>(Arrays.asList(Silian_departIds.split(",")));
            Silian_list.remove(Silian_departId);
            //删除之后再将新的id用逗号拼接起来进行更新
            String Silian_newDepartIds = String.join(",",Silian_list);
            Silian_sysUser.setDepartIds(Silian_newDepartIds);
            sysUserMapper.updateById(Silian_sysUser);
        }
    }

    /**
     * 通过部门集合为部门设置用户id，用于前台展示
     * @param departList 部门集合
     */
    private void setUserIdsByDepList(List<SysDepart> Silian_departList) {
        //查询负责部门不为空的情况
        LambdaQueryWrapper<SysUser> Silian_query  = new LambdaQueryWrapper<>();
        Silian_query.isNotNull(SysUser::getDepartIds);
        List<SysUser> Silian_users = sysUserMapper.selectList(Silian_query);
        Map<String,Object> Silian_map = new HashMap(5);
        //先循环一遍找到不同的负责部门id
        for (SysUser Silian_user:Silian_users) {
            String Silian_departIds = Silian_user.getDepartIds();
            String[] Silian_departIdArray = Silian_departIds.split(",");
            for (String Silian_departId:Silian_departIdArray) {
                //mao中包含部门key，负责用户直接拼接
                if(Silian_map.containsKey(Silian_departId)){
                    String Silian_userIds = Silian_map.get(Silian_departId) + "," + Silian_user.getId();
                    Silian_map.put(Silian_departId,Silian_userIds);
                }else{
                    Silian_map.put(Silian_departId,Silian_user.getId());
                }
            }
        }
        //循环部门集合找到部门id对应的负责用户
        for (SysDepart Silian_sysDepart:Silian_departList) {
            if(Silian_map.containsKey(Silian_sysDepart.getId())){
                Silian_sysDepart.setDirectorUserIds(Silian_map.get(Silian_sysDepart.getId()).toString());
            }
        }
    }
    //update-end---author:wangshuai ---date:20200308  for：[JTC-119]在部门管理菜单下设置部门负责人，新增方法添加部门负责人、删除负责部门负责人、查询部门对应的负责人

}
