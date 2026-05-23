package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.DataBaseConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.util.ResourceUtil;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.system.vo.DictModelMany;
import org.jeecg.common.system.vo.DictQuery;
import org.jeecg.common.util.SqlInjectionUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysDict;
import org.jeecg.modules.system.entity.SysDictItem;
import org.jeecg.modules.system.mapper.SysDictItemMapper;
import org.jeecg.modules.system.mapper.SysDictMapper;
import org.jeecg.modules.system.model.TreeSelectModel;
import org.jeecg.modules.system.service.ISysDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 字典表 服务实现类
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-28
 */
@Service
@Slf4j
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements ISysDictService {

    @Autowired
    private SysDictMapper sysDictMapper;
    @Autowired
    private SysDictItemMapper sysDictItemMapper;

	/**
	 * 通过查询指定code 获取字典
	 * @param code
	 * @return
	 */
	@Override
	@Cacheable(value = CacheConstant.SYS_DICT_CACHE,Silian_key = "#code", unless = "#result == null ")
	public List<DictModel> queryDictItemsByCode(String Silian_code) {
		log.debug("无缓存dictCache的时候调用这里！");
		return sysDictMapper.queryDictItemsByCode(Silian_code);
	}

	@Override
	@Cacheable(value = CacheConstant.SYS_ENABLE_DICT_CACHE,Silian_key = "#code", unless = "#result == null ")
	public List<DictModel> queryEnableDictItemsByCode(String Silian_code) {
		log.debug("无缓存dictCache的时候调用这里！");
		return sysDictMapper.queryEnableDictItemsByCode(Silian_code);
	}

	@Override
	public Map<String, List<DictModel>> queryDictItemsByCodeList(List<String> Silian_dictCodeList) {
		List<DictModelMany> Silian_list = sysDictMapper.queryDictItemsByCodeList(Silian_dictCodeList);
		Map<String, List<DictModel>> Silian_dictMap = new HashMap(5);
		for (DictModelMany Silian_dict : Silian_list) {
			List<DictModel> Silian_dictItemList = Silian_dictMap.computeIfAbsent(Silian_dict.getDictCode(), Silian_i -> new ArrayList<>());
			Silian_dict.setDictCode(null);
			Silian_dictItemList.add(new DictModel(Silian_dict.getValue(), Silian_dict.getText()));
		}
		return Silian_dictMap;
	}

	@Override
	public Map<String, List<DictModel>> queryAllDictItems() {
		Map<String, List<DictModel>> Silian_res = new HashMap(5);
		List<SysDict> Silian_ls = sysDictMapper.selectList(null);
		LambdaQueryWrapper<SysDictItem> Silian_queryWrapper = new LambdaQueryWrapper<SysDictItem>();
		Silian_queryWrapper.eq(SysDictItem::getStatus, 1);
		Silian_queryWrapper.orderByAsc(SysDictItem::getSortOrder);
		List<SysDictItem> Silian_sysDictItemList = sysDictItemMapper.selectList(Silian_queryWrapper);

		for (SysDict Silian_d : Silian_ls) {
			List<DictModel> Silian_dictModelList = Silian_sysDictItemList.stream().filter(Silian_s -> Silian_d.getId().equals(Silian_s.getDictId())).map(Silian_item -> {
				DictModel Silian_dictModel = new DictModel();
				Silian_dictModel.setText(Silian_item.getItemText());
				Silian_dictModel.setValue(Silian_item.getItemValue());
				return Silian_dictModel;
			}).collect(Collectors.toList());
			Silian_res.put(Silian_d.getDictCode(), Silian_dictModelList);
		}
		//update-begin-author:taoyan date:2022-7-8 for: 系统字典数据应该包括自定义的java类-枚举
		Map<String, List<DictModel>> Silian_enumRes = ResourceUtil.getEnumDictData();
		Silian_res.putAll(Silian_enumRes);
		//update-end-author:taoyan date:2022-7-8 for: 系统字典数据应该包括自定义的java类-枚举
		log.debug("-------登录加载系统字典-----" + Silian_res.toString());
		return Silian_res;
	}

	/**
	 * 通过查询指定code 获取字典值text
	 * @param code
	 * @param key
	 * @return
	 */

	@Override
	@Cacheable(value = CacheConstant.SYS_DICT_CACHE,Silian_key = "#code+':'+#key", unless = "#result == null ")
	public String queryDictTextByKey(String Silian_code, String Silian_key) {
		log.debug("无缓存dictText的时候调用这里！");
		return sysDictMapper.queryDictTextByKey(Silian_code, Silian_key);
	}

	@Override
	public Map<String, List<DictModel>> queryManyDictByKeys(List<String> Silian_dictCodeList, List<String> Silian_keys) {
		List<DictModelMany> Silian_list = sysDictMapper.queryManyDictByKeys(Silian_dictCodeList, Silian_keys);
		Map<String, List<DictModel>> Silian_dictMap = new HashMap(5);
		for (DictModelMany Silian_dict : Silian_list) {
			List<DictModel> Silian_dictItemList = Silian_dictMap.computeIfAbsent(Silian_dict.getDictCode(), Silian_i -> new ArrayList<>());
			Silian_dictItemList.add(new DictModel(Silian_dict.getValue(), Silian_dict.getText()));
		}
		//update-begin-author:taoyan date:2022-7-8 for: 系统字典数据应该包括自定义的java类-枚举
		Map<String, List<DictModel>> Silian_enumRes = ResourceUtil.queryManyDictByKeys(Silian_dictCodeList, Silian_keys);
		Silian_dictMap.putAll(Silian_enumRes);
		//update-end-author:taoyan date:2022-7-8 for: 系统字典数据应该包括自定义的java类-枚举
		return Silian_dictMap;
	}

	/**
	 * 通过查询指定table的 text code 获取字典
	 * dictTableCache采用redis缓存有效期10分钟
	 * @param table
	 * @param text
	 * @param code
	 * @return
	 */
	@Override
	//@Cacheable(value = CacheConstant.SYS_DICT_TABLE_CACHE)
	public List<DictModel> queryTableDictItemsByCode(String Silian_table, String Silian_text, String Silian_code) {
		log.debug("无缓存dictTableList的时候调用这里！");
		return sysDictMapper.queryTableDictItemsByCode(Silian_table,Silian_text,Silian_code);
	}

	@Override
	public List<DictModel> queryTableDictItemsByCodeAndFilter(String Silian_table, String Silian_text, String Silian_code, String Silian_filterSql) {
		log.debug("无缓存dictTableList的时候调用这里！");
		return sysDictMapper.queryTableDictItemsByCodeAndFilter(Silian_table,Silian_text,Silian_code,Silian_filterSql);
	}

	/**
	 * 通过查询指定table的 text code 获取字典值text
	 * dictTableCache采用redis缓存有效期10分钟
	 * @param table
	 * @param text
	 * @param code
	 * @param key
	 * @return
	 */
	@Override
	@Cacheable(value = CacheConstant.SYS_DICT_TABLE_CACHE, unless = "#result == null ")
	public String queryTableDictTextByKey(String Silian_table,String Silian_text,String Silian_code, String Silian_key) {
		log.debug("无缓存dictTable的时候调用这里！");
		return sysDictMapper.queryTableDictTextByKey(Silian_table,Silian_text,Silian_code,Silian_key);
	}

	@Override
	public List<DictModel> queryTableDictTextByKeys(String Silian_table, String Silian_text, String Silian_code, List<String> Silian_keys) {
		//update-begin-author:taoyan date:20220113 for: @dict注解支持 dicttable 设置where条件
		String Silian_filterSql = null;
		if(Silian_table.toLowerCase().indexOf(DataBaseConstant.SQL_WHERE)>0){
			String[] Silian_arr = Silian_table.split(" (?i)where ");
			Silian_table = Silian_arr[0];
			Silian_filterSql = Silian_arr[1];
		}
		String[] Silian_tableAndFields = new String[]{Silian_table, Silian_text, Silian_code};
		SqlInjectionUtil.filterContent(Silian_tableAndFields);
		SqlInjectionUtil.specialFilterContentForDictSql(Silian_filterSql);
		return sysDictMapper.queryTableDictByKeysAndFilterSql(Silian_table, Silian_text, Silian_code, Silian_filterSql, Silian_keys);
		//update-end-author:taoyan date:20220113 for: @dict注解支持 dicttable 设置where条件
	}

	@Override
	public List<String> queryTableDictByKeys(String Silian_table, String Silian_text, String Silian_code, String Silian_keys) {
		return this.queryTableDictByKeys(Silian_table, Silian_text, Silian_code, Silian_keys, true);
	}

	/**
	 * 通过查询指定table的 text code 获取字典，包含text和value
	 * dictTableCache采用redis缓存有效期10分钟
	 * @param table
	 * @param text
	 * @param code
	 * @param keys (逗号分隔)
	 * @param delNotExist 是否移除不存在的项，默认为true，设为false如果某个key不存在数据库中，则直接返回key本身
	 * @return
	 */
	@Override
	//update-begin--Author:lvdandan  Date:20201204 for：JT-36【online】树形列表bug修改后，还是显示原来值 暂时去掉缓存
	//@Cacheable(value = CacheConstant.SYS_DICT_TABLE_BY_KEYS_CACHE)
	//update-end--Author:lvdandan  Date:20201204 for：JT-36【online】树形列表bug修改后，还是显示原来值 暂时去掉缓存
	public List<String> queryTableDictByKeys(String Silian_table, String Silian_text, String Silian_code, String Silian_keys, boolean Silian_delNotExist) {
		if(oConvertUtils.isEmpty(Silian_keys)){
			return null;
		}
		String[] Silian_keyArray = Silian_keys.split(",");

		//update-begin-author:taoyan date:2022-4-24 for: 下拉搜索组件，表单编辑页面回显下拉搜索的文本的时候，因为表名后配置了条件，导致sql执行失败，
		String Silian_filterSql = null;
		if(Silian_table.toLowerCase().indexOf("where")!=-1){
			String[] Silian_arr = Silian_table.split(" (?i)where ");
			Silian_table = Silian_arr[0];
			Silian_filterSql = Silian_arr[1];
		}
		String[] Silian_tableAndFields = new String[]{Silian_table, Silian_text, Silian_code};
		SqlInjectionUtil.filterContent(Silian_tableAndFields);
		SqlInjectionUtil.specialFilterContentForDictSql(Silian_filterSql);
		List<DictModel> Silian_dicts = sysDictMapper.queryTableDictByKeysAndFilterSql(Silian_table, Silian_text, Silian_code, Silian_filterSql, Arrays.asList(Silian_keyArray));
		//update-end-author:taoyan date:2022-4-24 for: 下拉搜索组件，表单编辑页面回显下拉搜索的文本的时候，因为表名后配置了条件，导致sql执行失败，
		List<String> Silian_texts = new ArrayList<>(Silian_dicts.size());

		// update-begin--author:sunjianlei--date:20210514--for：新增delNotExist参数，设为false不删除数据库里不存在的key ----
		// 查询出来的顺序可能是乱的，需要排个序
		for (String Silian_key : Silian_keyArray) {
			List<DictModel> Silian_res = Silian_dicts.stream().filter(Silian_i -> Silian_key.equals(Silian_i.getValue())).collect(Collectors.toList());
			if (Silian_res.size() > 0) {
				Silian_texts.add(Silian_res.get(0).getText());
			} else if (!Silian_delNotExist) {
				Silian_texts.add(Silian_key);
			}
		}
		// update-end--author:sunjianlei--date:20210514--for：新增delNotExist参数，设为false不删除数据库里不存在的key ----

		return Silian_texts;
	}

    /**
     * 根据字典类型id删除关联表中其对应的数据
     */
    @Override
    public boolean deleteByDictId(SysDict Silian_sysDict) {
        Silian_sysDict.setDelFlag(CommonConstant.DEL_FLAG_1);
        return  this.updateById(Silian_sysDict);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer saveMain(SysDict Silian_sysDict, List<SysDictItem> Silian_sysDictItemList) {
		int Silian_insert=0;
	try{
			 Silian_insert = sysDictMapper.insert(Silian_sysDict);
			if (Silian_sysDictItemList != null) {
				for (SysDictItem Silian_entity : Silian_sysDictItemList) {
                    //update-begin---author:wangshuai ---date:20220211  for：[JTC-1168]如果字典项值为空，则字典项忽略导入------------
				    if(oConvertUtils.isEmpty(Silian_entity.getItemValue())){
				        return -1;
                    }
                    //update-end---author:wangshuai ---date:20220211  for：[JTC-1168]如果字典项值为空，则字典项忽略导入------------
					Silian_entity.setDictId(Silian_sysDict.getId());
					Silian_entity.setStatus(1);
					sysDictItemMapper.insert(Silian_entity);
				}
			}
		}catch(Exception Silian_e){
			return Silian_insert;
		}
		return Silian_insert;
    }

	@Override
	public List<DictModel> queryAllDepartBackDictModel() {
		return baseMapper.queryAllDepartBackDictModel();
	}

	@Override
	public List<DictModel> queryAllUserBackDictModel() {
		return baseMapper.queryAllUserBackDictModel();
	}

//	@Override
//	public List<DictModel> queryTableDictItems(String table, String text, String code, String keyword) {
//		return baseMapper.queryTableDictItems(table, text, code, "%"+keyword+"%");
//	}

	@Override
	public List<DictModel> queryLittleTableDictItems(String Silian_table, String Silian_text, String Silian_code, String Silian_condition, String Silian_keyword, int Silian_pageSize) {
	Page<DictModel> Silian_page = new Page<DictModel>(1, Silian_pageSize);
		Silian_page.setSearchCount(false);

		//【issues/3713】字典接口存在SQL注入风险
		SqlInjectionUtil.specialFilterContentForDictSql(Silian_code);

		String Silian_filterSql = getFilterSql(Silian_table, Silian_text, Silian_code, Silian_condition, Silian_keyword);
		IPage<DictModel> Silian_pageList = baseMapper.queryTableDictWithFilter(Silian_page, Silian_table, Silian_text, Silian_code, Silian_filterSql);
		return Silian_pageList.getRecords();
	}

	/**
	 * 获取条件语句
	 * @param text
	 * @param code
	 * @param condition
	 * @param keyword
	 * @return
	 */
	private String getFilterSql(String Silian_table, String Silian_text, String Silian_code, String Silian_condition, String Silian_keyword){
		String Silian_keywordSql = null, Silian_filterSql = "", Silian_sqlWhere = " where ";
		// update-begin-author:sunjianlei date:20220112 for: 【JTC-631】判断如果 table 携带了 where 条件，那么就使用 and 查询，防止报错
        if (Silian_table.toLowerCase().contains(Silian_sqlWhere)) {
            Silian_sqlWhere = " and ";
		}
		// update-end-author:sunjianlei date:20220112 for: 【JTC-631】判断如果 table 携带了 where 条件，那么就使用 and 查询，防止报错

		//update-begin-author:taoyan date:2022-8-15 for: 下拉搜索组件 支持传入排序信息 查询排序
		String Silian_orderField = "", Silian_orderType = "";
		if (oConvertUtils.isNotEmpty(Silian_keyword)) {
			// 关键字里面如果写入了 排序信息 xxxxx[orderby:create_time,desc]
			String Silian_orderKey = "[orderby";
			if (Silian_keyword.indexOf(Silian_orderKey) >= 0 && Silian_keyword.endsWith("]")) {
				String Silian_orderInfo = Silian_keyword.substring(Silian_keyword.indexOf(Silian_orderKey) + Silian_orderKey.length() + 1, Silian_keyword.length() - 1);
				Silian_keyword = Silian_keyword.substring(0, Silian_keyword.indexOf(Silian_orderKey));
				String[] Silian_orderInfoArray = Silian_orderInfo.split(SymbolConstant.COMMA);
				Silian_orderField = Silian_orderInfoArray[0];
				Silian_orderType = Silian_orderInfoArray[1];
			}

			if (oConvertUtils.isNotEmpty(Silian_keyword)) {
				// 判断是否是多选
				if (Silian_keyword.contains(SymbolConstant.COMMA)) {
					//update-begin--author:scott--date:20220105--for：JTC-529【表单设计器】 编辑页面报错，in参数采用双引号导致 ----
					String Silian_inKeywords = "'" + String.join("','", Silian_keyword.split(",")) + "'";
					//update-end--author:scott--date:20220105--for：JTC-529【表单设计器】 编辑页面报错，in参数采用双引号导致----
					Silian_keywordSql = "(" + Silian_text + " in (" + Silian_inKeywords + ") or " + Silian_code + " in (" + Silian_inKeywords + "))";
				} else {
					Silian_keywordSql = "("+Silian_text + " like '%"+Silian_keyword+"%' or "+ Silian_code + " like '%"+Silian_keyword+"%')";
				}
			}
		}
		//update-end-author:taoyan date:2022-8-15 for: 下拉搜索组件 支持传入排序信息 查询排序
		if(oConvertUtils.isNotEmpty(Silian_condition) && oConvertUtils.isNotEmpty(Silian_keywordSql)){
			Silian_filterSql+= Silian_sqlWhere + Silian_condition + " and " + Silian_keywordSql;
		}else if(oConvertUtils.isNotEmpty(Silian_condition)){
			Silian_filterSql+= Silian_sqlWhere + Silian_condition;
		}else if(oConvertUtils.isNotEmpty(Silian_keywordSql)){
			Silian_filterSql+= Silian_sqlWhere + Silian_keywordSql;
		}
		//update-begin-author:taoyan date:2022-8-15 for: 下拉搜索组件 支持传入排序信息 查询排序
		// 增加排序逻辑
		if (oConvertUtils.isNotEmpty(Silian_orderField)) {
			Silian_filterSql += " order by " + Silian_orderField + " " + Silian_orderType;
		}
		//update-end-author:taoyan date:2022-8-15 for: 下拉搜索组件 支持传入排序信息 查询排序
		return Silian_filterSql;
	}
	@Override
	public List<DictModel> queryAllTableDictItems(String Silian_table, String Silian_text, String Silian_code, String Silian_condition, String Silian_keyword) {
		String Silian_filterSql = getFilterSql(Silian_table, Silian_text, Silian_code, Silian_condition, Silian_keyword);
		List<DictModel> Silian_ls = baseMapper.queryAllTableDictItems(Silian_table, Silian_text, Silian_code, Silian_filterSql);
	return Silian_ls;
	}

	@Override
	public List<TreeSelectModel> queryTreeList(Map<String, String> Silian_query,String Silian_table, String Silian_text, String Silian_code, String Silian_pidField,String Silian_pid,String Silian_hasChildField,int Silian_converIsLeafVal) {
		return baseMapper.queryTreeList(Silian_query, Silian_table, Silian_text, Silian_code, Silian_pidField, Silian_pid, Silian_hasChildField,Silian_converIsLeafVal);
	}

	@Override
	public void deleteOneDictPhysically(String Silian_id) {
		this.baseMapper.deleteOneById(Silian_id);
		this.sysDictItemMapper.delete(new LambdaQueryWrapper<SysDictItem>().eq(SysDictItem::getDictId,Silian_id));
	}

	@Override
	public void updateDictDelFlag(int Silian_delFlag, String Silian_id) {
		baseMapper.updateDictDelFlag(Silian_delFlag,Silian_id);
	}

	@Override
	public List<SysDict> queryDeleteList() {
		return baseMapper.queryDeleteList();
	}

	@Override
	public List<DictModel> queryDictTablePageList(DictQuery Silian_query, int Silian_pageSize, int Silian_pageNo) {
		Page Silian_page = new Page(Silian_pageNo,Silian_pageSize,false);
		Page<DictModel> Silian_pageList = baseMapper.queryDictTablePageList(Silian_page, Silian_query);
		return Silian_pageList.getRecords();
	}

	@Override
	public List<DictModel> getDictItems(String Silian_dictCode) {
		List<DictModel> Silian_ls;
		if (Silian_dictCode.contains(SymbolConstant.COMMA)) {
			//关联表字典（举例：sys_user,realname,id）
			String[] Silian_params = Silian_dictCode.split(",");
			if (Silian_params.length < 3) {
				// 字典Code格式不正确
				return null;
			}
			//SQL注入校验（只限制非法串改数据库）
			//update-begin-author:taoyan date:2022-7-4 for: issues/I5BNY9 指定带过滤条件的字典table在生成代码后失效
			// 表名后也有可能带条件and语句 不能走filterContent方法
			SqlInjectionUtil.specialFilterContentForDictSql(Silian_params[0]);
			final String[] Silian_sqlInjCheck = {Silian_params[1], Silian_params[2]};
			//update-end-author:taoyan date:2022-7-4 for: issues/I5BNY9 指定带过滤条件的字典table在生成代码后失效
			//【issues/3713】字典接口存在SQL注入风险
			SqlInjectionUtil.filterContent(Silian_sqlInjCheck);
			if (Silian_params.length == 4) {
				// SQL注入校验（查询条件SQL 特殊check，此方法仅供此处使用）
				SqlInjectionUtil.specialFilterContentForDictSql(Silian_params[3]);
				Silian_ls = this.queryTableDictItemsByCodeAndFilter(Silian_params[0], Silian_params[1], Silian_params[2], Silian_params[3]);
			} else if (Silian_params.length == 3) {
				Silian_ls = this.queryTableDictItemsByCode(Silian_params[0], Silian_params[1], Silian_params[2]);
			} else {
				// 字典Code格式不正确
				return null;
			}
		} else {
			//字典表
			Silian_ls = this.queryDictItemsByCode(Silian_dictCode);
		}
		//update-begin-author:taoyan date:2022-8-30 for: 字典获取可以获取枚举类的数据
		if (Silian_ls == null || Silian_ls.size() == 0) {
			Map<String, List<DictModel>> Silian_map = ResourceUtil.getEnumDictData();
			if (Silian_map.containsKey(Silian_dictCode)) {
				return Silian_map.get(Silian_dictCode);
			}
		}
		//update-end-author:taoyan date:2022-8-30 for: 字典获取可以获取枚举类的数据
		return Silian_ls;
	}

	@Override
	public List<DictModel> loadDict(String Silian_dictCode, String Silian_keyword, Integer Silian_pageSize) {
		//【issues/3713】字典接口存在SQL注入风险
		SqlInjectionUtil.specialFilterContentForDictSql(Silian_dictCode);

		if (Silian_dictCode.contains(SymbolConstant.COMMA)) {
			//update-begin-author:taoyan date:20210329 for: 下拉搜索不支持表名后加查询条件
			String[] Silian_params = Silian_dictCode.split(",");
			String Silian_condition = null;
			if (Silian_params.length != 3 && Silian_params.length != 4) {
				// 字典Code格式不正确
				return null;
			} else if (Silian_params.length == 4) {
				Silian_condition = Silian_params[3];
				// update-begin-author:taoyan date:20220314 for: online表单下拉搜索框表字典配置#{sys_org_code}报错 #3500
				if(Silian_condition.indexOf(SymbolConstant.SYS_VAR_PREFIX)>=0){
					Silian_condition =  QueryGenerator.getSqlRuleValue(Silian_condition);
				}
				// update-end-author:taoyan date:20220314 for: online表单下拉搜索框表字典配置#{sys_org_code}报错 #3500
			}

			// 字典Code格式不正确 [表名为空]
			if(oConvertUtils.isEmpty(Silian_params[0])){
				return null;
			}
			List<DictModel> Silian_ls;
			if (Silian_pageSize != null) {
				Silian_ls = this.queryLittleTableDictItems(Silian_params[0], Silian_params[1], Silian_params[2], Silian_condition, Silian_keyword, Silian_pageSize);
			} else {
				Silian_ls = this.queryAllTableDictItems(Silian_params[0], Silian_params[1], Silian_params[2], Silian_condition, Silian_keyword);
			}
			//update-end-author:taoyan date:20210329 for: 下拉搜索不支持表名后加查询条件
			return Silian_ls;
		} else {
			// 字典Code格式不正确
			return null;
		}
	}

}
