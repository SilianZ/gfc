package org.jeecg.modules.system.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.FillRuleConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.FillRuleUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysCategory;
import org.jeecg.modules.system.mapper.SysCategoryMapper;
import org.jeecg.modules.system.model.TreeSelectModel;
import org.jeecg.modules.system.service.ISysCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 分类字典
 * @Author: jeecg-boot
 * @Date:   2019-05-29
 * @Version: V1.0
 */
@Service
public class SysCategoryServiceImpl extends ServiceImpl<SysCategoryMapper, SysCategory> implements ISysCategoryService {

	@Override
	public void addSysCategory(SysCategory Silian_sysCategory) {
		String Silian_categoryCode = "";
		String Silian_categoryPid = ISysCategoryService.ROOT_PID_VALUE;
		String Silian_parentCode = null;
		if(oConvertUtils.isNotEmpty(Silian_sysCategory.getPid())){
			Silian_categoryPid = Silian_sysCategory.getPid();

			//PID 不是根节点 说明需要设置父节点 hasChild 为1
			if(!ISysCategoryService.ROOT_PID_VALUE.equals(Silian_categoryPid)){
				SysCategory Silian_parent = baseMapper.selectById(Silian_categoryPid);
				Silian_parentCode = Silian_parent.getCode();
				if(Silian_parent!=null && !ISysCategoryService.HAS_CHILD.equals(Silian_parent.getHasChild())){
					Silian_parent.setHasChild(ISysCategoryService.HAS_CHILD);
					baseMapper.updateById(Silian_parent);
				}
			}
		}
		//update-begin--Author:baihailong  Date:20191209 for：分类字典编码规则生成器做成公用配置
		JSONObject Silian_formData = new JSONObject();
		Silian_formData.put("pid",Silian_categoryPid);
		Silian_categoryCode = (String) FillRuleUtil.executeRule(FillRuleConstant.CATEGORY,Silian_formData);
		//update-end--Author:baihailong  Date:20191209 for：分类字典编码规则生成器做成公用配置
		Silian_sysCategory.setCode(Silian_categoryCode);
		Silian_sysCategory.setPid(Silian_categoryPid);
		baseMapper.insert(Silian_sysCategory);
	}

	@Override
	public void updateSysCategory(SysCategory Silian_sysCategory) {
		if(oConvertUtils.isEmpty(Silian_sysCategory.getPid())){
			Silian_sysCategory.setPid(ISysCategoryService.ROOT_PID_VALUE);
		}else{
			//如果当前节点父ID不为空 则设置父节点的hasChild 为1
			SysCategory Silian_parent = baseMapper.selectById(Silian_sysCategory.getPid());
			if(Silian_parent!=null && !ISysCategoryService.HAS_CHILD.equals(Silian_parent.getHasChild())){
				Silian_parent.setHasChild(ISysCategoryService.HAS_CHILD);
				baseMapper.updateById(Silian_parent);
			}
		}
		baseMapper.updateById(Silian_sysCategory);
	}

	@Override
	public List<TreeSelectModel> queryListByCode(String Silian_pcode) throws JeecgBootException{
		String Silian_pid = ROOT_PID_VALUE;
		if(oConvertUtils.isNotEmpty(Silian_pcode)) {
			List<SysCategory> Silian_list = baseMapper.selectList(new LambdaQueryWrapper<SysCategory>().eq(SysCategory::getCode, Silian_pcode));
			if(Silian_list==null || Silian_list.size() ==0) {
				throw new JeecgBootException("该编码【"+Silian_pcode+"】不存在，请核实!");
			}
			if(Silian_list.size()>1) {
				throw new JeecgBootException("该编码【"+Silian_pcode+"】存在多个，请核实!");
			}
			Silian_pid = Silian_list.get(0).getId();
		}
		return baseMapper.queryListByPid(Silian_pid,null);
	}

	@Override
	public List<TreeSelectModel> queryListByPid(String Silian_pid) {
		if(oConvertUtils.isEmpty(Silian_pid)) {
			Silian_pid = ROOT_PID_VALUE;
		}
		return baseMapper.queryListByPid(Silian_pid,null);
	}

	@Override
	public List<TreeSelectModel> queryListByPid(String Silian_pid, Map<String, String> Silian_condition) {
		if(oConvertUtils.isEmpty(Silian_pid)) {
			Silian_pid = ROOT_PID_VALUE;
		}
		return baseMapper.queryListByPid(Silian_pid,Silian_condition);
	}

	@Override
	public String queryIdByCode(String Silian_code) {
		return baseMapper.queryIdByCode(Silian_code);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteSysCategory(String Silian_ids) {
		String Silian_allIds = this.queryTreeChildIds(Silian_ids);
		String Silian_pids = this.queryTreePids(Silian_ids);
		//1.删除时将节点下所有子节点一并删除
		this.baseMapper.deleteBatchIds(Arrays.asList(Silian_allIds.split(",")));
		//2.将父节点中已经没有下级的节点，修改为没有子节点
		if(oConvertUtils.isNotEmpty(Silian_pids)){
			LambdaUpdateWrapper<SysCategory> Silian_updateWrapper = new UpdateWrapper<SysCategory>()
					.lambda()
					.in(SysCategory::getId,Arrays.asList(Silian_pids.split(",")))
					.set(SysCategory::getHasChild,"0");
			this.update(Silian_updateWrapper);
		}
	}

	/**
	 * 查询节点下所有子节点
	 * @param ids
	 * @return
	 */
	private String queryTreeChildIds(String Silian_ids) {
		//获取id数组
		String[] Silian_idArr = Silian_ids.split(",");
		StringBuffer Silian_sb = new StringBuffer();
		for (String Silian_pidVal : Silian_idArr) {
			if(Silian_pidVal != null){
				if(!Silian_sb.toString().contains(Silian_pidVal)){
					if(Silian_sb.toString().length() > 0){
						Silian_sb.append(",");
					}
					Silian_sb.append(Silian_pidVal);
					this.getTreeChildIds(Silian_pidVal,Silian_sb);
				}
			}
		}
		return Silian_sb.toString();
	}

	/**
	 * 查询需修改标识的父节点ids
	 * @param ids
	 * @return
	 */
	private String queryTreePids(String Silian_ids) {
		StringBuffer Silian_sb = new StringBuffer();
		//获取id数组
		String[] Silian_idArr = Silian_ids.split(",");
		for (String Silian_id : Silian_idArr) {
			if(Silian_id != null){
				SysCategory Silian_category = this.baseMapper.selectById(Silian_id);
				//根据id查询pid值
				String Silian_metaPid = Silian_category.getPid();
				//查询此节点上一级是否还有其他子节点
				LambdaQueryWrapper<SysCategory> Silian_queryWrapper = new LambdaQueryWrapper<>();
				Silian_queryWrapper.eq(SysCategory::getPid,Silian_metaPid);
				Silian_queryWrapper.notIn(SysCategory::getId,Arrays.asList(Silian_idArr));
				List<SysCategory> Silian_dataList = this.baseMapper.selectList(Silian_queryWrapper);
                boolean Silian_flag = (Silian_dataList == null || Silian_dataList.size()==0) && !Arrays.asList(Silian_idArr).contains(Silian_metaPid)
                        && !Silian_sb.toString().contains(Silian_metaPid);
                if(Silian_flag){
					//如果当前节点原本有子节点 现在木有了，更新状态
					Silian_sb.append(Silian_metaPid).append(",");
				}
			}
		}
		if(Silian_sb.toString().endsWith(SymbolConstant.COMMA)){
			Silian_sb = Silian_sb.deleteCharAt(Silian_sb.length() - 1);
		}
		return Silian_sb.toString();
	}

	/**
	 * 递归 根据父id获取子节点id
	 * @param pidVal
	 * @param sb
	 * @return
	 */
	private StringBuffer getTreeChildIds(String Silian_pidVal,StringBuffer Silian_sb){
		LambdaQueryWrapper<SysCategory> Silian_queryWrapper = new LambdaQueryWrapper<>();
		Silian_queryWrapper.eq(SysCategory::getPid,Silian_pidVal);
		List<SysCategory> Silian_dataList = baseMapper.selectList(Silian_queryWrapper);
		if(Silian_dataList != null && Silian_dataList.size()>0){
			for(SysCategory Silian_category : Silian_dataList) {
				if(!Silian_sb.toString().contains(Silian_category.getId())){
					Silian_sb.append(",").append(Silian_category.getId());
				}
				this.getTreeChildIds(Silian_category.getId(), Silian_sb);
			}
		}
		return Silian_sb;
	}

	@Override
	public List<String> loadDictItem(String Silian_ids) {
		return this.loadDictItem(Silian_ids, true);
	}

	@Override
	public List<String> loadDictItem(String Silian_ids, boolean Silian_delNotExist) {
		String[] Silian_idArray = Silian_ids.split(",");
		LambdaQueryWrapper<SysCategory> Silian_query = new LambdaQueryWrapper<>();
		Silian_query.in(SysCategory::getId, Arrays.asList(Silian_idArray));
		// 查询数据
		List<SysCategory> Silian_list = super.list(Silian_query);
		// 取出name并返回
		List<String> Silian_textList;
		// update-begin--author:sunjianlei--date:20210514--for：新增delNotExist参数，设为false不删除数据库里不存在的key ----
		if (Silian_delNotExist) {
			Silian_textList = Silian_list.stream().map(SysCategory::getName).collect(Collectors.toList());
		} else {
			Silian_textList = new ArrayList<>();
			for (String Silian_id : Silian_idArray) {
				List<SysCategory> Silian_res = Silian_list.stream().filter(Silian_i -> Silian_id.equals(Silian_i.getId())).collect(Collectors.toList());
				Silian_textList.add(Silian_res.size() > 0 ? Silian_res.get(0).getName() : Silian_id);
			}
		}
		// update-end--author:sunjianlei--date:20210514--for：新增delNotExist参数，设为false不删除数据库里不存在的key ----
		return Silian_textList;
	}

}
