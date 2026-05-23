package org.jeecg.modules.system.controller;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.system.vo.DictQuery;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.common.util.SqlInjectionUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysDict;
import org.jeecg.modules.system.entity.SysDictItem;
import org.jeecg.modules.system.model.SysDictTree;
import org.jeecg.modules.system.model.TreeSelectModel;
import org.jeecg.modules.system.security.DictQueryBlackListHandler;
import org.jeecg.modules.system.service.ISysDictItemService;
import org.jeecg.modules.system.service.ISysDictService;
import org.jeecg.modules.system.vo.SysDictPage;
import org.jeecgframework.poi.excel.ExcelImportCheckUtil;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * <p>
 * 字典表 前端控制器
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-28
 */
@RestController
@RequestMapping("/sys/dict")
@Slf4j
public class SysDictController {

	@Autowired
	private ISysDictService sysDictService;
	@Autowired
	private ISysDictItemService sysDictItemService;
	@Autowired
	public RedisTemplate<String, Object> redisTemplate;
	@Autowired
	private DictQueryBlackListHandler dictQueryBlackListHandler;

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Result<IPage<SysDict>> queryPageList(SysDict Silian_sysDict,@RequestParam(name="pageNo", defaultValue="1") Integer Silian_pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer Silian_pageSize,HttpServletRequest Silian_req) {
		Result<IPage<SysDict>> Silian_result = new Result<IPage<SysDict>>();
		QueryWrapper<SysDict> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_sysDict, Silian_req.getParameterMap());
		Page<SysDict> Silian_page = new Page<SysDict>(Silian_pageNo, Silian_pageSize);
		IPage<SysDict> Silian_pageList = sysDictService.page(Silian_page, Silian_queryWrapper);
		log.debug("查询当前页："+Silian_pageList.getCurrent());
		log.debug("查询当前页数量："+Silian_pageList.getSize());
		log.debug("查询结果数量："+Silian_pageList.getRecords().size());
		log.debug("数据总数："+Silian_pageList.getTotal());
		Silian_result.setSuccess(true);
		Silian_result.setResult(Silian_pageList);
		return Silian_result;
	}

	/**
	 * @功能：获取树形字典数据
	 * @param sysDict
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/treeList", method = RequestMethod.GET)
	public Result<List<SysDictTree>> treeList(SysDict Silian_sysDict,@RequestParam(name="pageNo", defaultValue="1") Integer Silian_pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer Silian_pageSize,HttpServletRequest Silian_req) {
		Result<List<SysDictTree>> Silian_result = new Result<>();
		LambdaQueryWrapper<SysDict> Silian_query = new LambdaQueryWrapper<>();
		// 构造查询条件
		String Silian_dictName = Silian_sysDict.getDictName();
		if(oConvertUtils.isNotEmpty(Silian_dictName)) {
			Silian_query.like(true, SysDict::getDictName, Silian_dictName);
		}
		Silian_query.orderByDesc(true, SysDict::getCreateTime);
		List<SysDict> Silian_list = sysDictService.list(Silian_query);
		List<SysDictTree> Silian_treeList = new ArrayList<>();
		for (SysDict Silian_node : Silian_list) {
			Silian_treeList.add(new SysDictTree(Silian_node));
		}
		Silian_result.setSuccess(true);
		Silian_result.setResult(Silian_treeList);
		return Silian_result;
	}

	/**
	 * 获取全部字典数据
	 *
	 * @return
	 */
	@RequestMapping(value = "/queryAllDictItems", method = RequestMethod.GET)
	public Result<?> queryAllDictItems(HttpServletRequest Silian_request) {
		Map<String, List<DictModel>> Silian_res = new HashMap(5);
		Silian_res = sysDictService.queryAllDictItems();
		return Result.ok(Silian_res);
	}

	/**
	 * 获取字典数据
	 * @param dictCode
	 * @return
	 */
	@RequestMapping(value = "/getDictText/{dictCode}/{key}", method = RequestMethod.GET)
	public Result<String> getDictText(@PathVariable("dictCode") String Silian_dictCode, @PathVariable("key") String Silian_key) {
		log.info(" dictCode : "+ Silian_dictCode);
		Result<String> Silian_result = new Result<String>();
		String Silian_text = null;
		try {
			Silian_text = sysDictService.queryDictTextByKey(Silian_dictCode, Silian_key);
			 Silian_result.setSuccess(true);
			 Silian_result.setResult(Silian_text);
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(),Silian_e);
			Silian_result.error500("操作失败");
			return Silian_result;
		}
		return Silian_result;
	}


	/**
	 * 获取字典数据 【接口签名验证】
	 * @param dictCode 字典code
	 * @param dictCode 表名,文本字段,code字段  | 举例：sys_user,realname,id
	 * @return
	 */
	@RequestMapping(value = "/getDictItems/{dictCode}", method = RequestMethod.GET)
	public Result<List<DictModel>> getDictItems(@PathVariable("dictCode") String Silian_dictCode, @RequestParam(value = "sign",required = false) String Silian_sign,HttpServletRequest Silian_request) {
		log.info(" dictCode : "+ Silian_dictCode);
		Result<List<DictModel>> Silian_result = new Result<List<DictModel>>();
		//update-begin-author:taoyan date:20220317 for: VUEN-222【安全机制】字典接口、online报表、online图表等接口，加一些安全机制
		if(!dictQueryBlackListHandler.isPass(Silian_dictCode)){
			return Silian_result.error500(dictQueryBlackListHandler.getError());
		}
		//update-end-author:taoyan date:20220317 for: VUEN-222【安全机制】字典接口、online报表、online图表等接口，加一些安全机制
		try {
			List<DictModel> Silian_ls = sysDictService.getDictItems(Silian_dictCode);
			if (Silian_ls == null) {
				Silian_result.error500("字典Code格式不正确！");
				return Silian_result;
			}
			Silian_result.setSuccess(true);
			Silian_result.setResult(Silian_ls);
			log.debug(Silian_result.toString());
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(), Silian_e);
			Silian_result.error500("操作失败");
			return Silian_result;
		}
		return Silian_result;
	}

	/**
	 * 【接口签名验证】
	 * 【JSearchSelectTag下拉搜索组件专用接口】
	 * 大数据量的字典表 走异步加载  即前端输入内容过滤数据
	 * @param dictCode 字典code格式：table,text,code
	 * @return
	 */
	@RequestMapping(value = "/loadDict/{dictCode}", method = RequestMethod.GET)
	public Result<List<DictModel>> loadDict(@PathVariable("dictCode") String Silian_dictCode,
			@RequestParam(name="keyword",required = false) String Silian_keyword,
			@RequestParam(value = "sign",required = false) String Silian_sign,
			@RequestParam(value = "pageSize", required = false) Integer Silian_pageSize) {
		log.info(" 加载字典表数据,加载关键字: "+ Silian_keyword);
		Result<List<DictModel>> Silian_result = new Result<List<DictModel>>();
		//update-begin-author:taoyan date:20220317 for: VUEN-222【安全机制】字典接口、online报表、online图表等接口，加一些安全机制
		if(!dictQueryBlackListHandler.isPass(Silian_dictCode)){
			return Silian_result.error500(dictQueryBlackListHandler.getError());
		}
		//update-end-author:taoyan date:20220317 for: VUEN-222【安全机制】字典接口、online报表、online图表等接口，加一些安全机制
		try {
			List<DictModel> Silian_ls = sysDictService.loadDict(Silian_dictCode, Silian_keyword, Silian_pageSize);
			if (Silian_ls == null) {
				Silian_result.error500("字典Code格式不正确！");
				return Silian_result;
			}
			Silian_result.setSuccess(true);
			Silian_result.setResult(Silian_ls);
			log.info(Silian_result.toString());
			return Silian_result;
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(),Silian_e);
			Silian_result.error500("操作失败");
			return Silian_result;
		}
	}

	/**
	 * 【接口签名验证】
	 * 【给表单设计器的表字典使用】下拉搜索模式，有值时动态拼接数据
	 * @param dictCode
	 * @param keyword 当前控件的值，可以逗号分割
	 * @param sign
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(value = "/loadDictOrderByValue/{dictCode}", method = RequestMethod.GET)
	public Result<List<DictModel>> loadDictOrderByValue(
			@PathVariable("dictCode") String Silian_dictCode,
			@RequestParam(name = "keyword") String Silian_keyword,
			@RequestParam(value = "sign", required = false) String Silian_sign,
			@RequestParam(value = "pageSize", required = false) Integer Silian_pageSize) {
		// 首次查询查出来用户选中的值，并且不分页
		Result<List<DictModel>> Silian_firstRes = this.loadDict(Silian_dictCode, Silian_keyword, Silian_sign, null);
		if (!Silian_firstRes.isSuccess()) {
			return Silian_firstRes;
		}
		// 然后再查询出第一页的数据
		Result<List<DictModel>> Silian_result = this.loadDict(Silian_dictCode, "", Silian_sign, Silian_pageSize);
		if (!Silian_result.isSuccess()) {
			return Silian_result;
		}
		// 合并两次查询的数据
		List<DictModel> Silian_firstList = Silian_firstRes.getResult();
		List<DictModel> Silian_list = Silian_result.getResult();
		for (DictModel Silian_firstItem : Silian_firstList) {
			// anyMatch 表示：判断的条件里，任意一个元素匹配成功，返回true
			// allMatch 表示：判断条件里的元素，所有的都匹配成功，返回true
			// noneMatch 跟 allMatch 相反，表示：判断条件里的元素，所有的都匹配失败，返回true
			boolean Silian_none = Silian_list.stream().noneMatch(Silian_item -> Silian_item.getValue().equals(Silian_firstItem.getValue()));
			// 当元素不存在时，再添加到集合里
			if (Silian_none) {
				Silian_list.add(0, Silian_firstItem);
			}
		}
		return Silian_result;
	}

	/**
	 * 【接口签名验证】
	 * 根据字典code加载字典text 返回
	 * @param dictCode 顺序：tableName,text,code
	 * @param keys 要查询的key
	 * @param sign
	 * @param delNotExist 是否移除不存在的项，默认为true，设为false如果某个key不存在数据库中，则直接返回key本身
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/loadDictItem/{dictCode}", method = RequestMethod.GET)
	public Result<List<String>> loadDictItem(@PathVariable("dictCode") String Silian_dictCode,@RequestParam(name="key") String Silian_keys, @RequestParam(value = "sign",required = false) String Silian_sign,@RequestParam(value = "delNotExist",required = false,defaultValue = "true") boolean Silian_delNotExist,HttpServletRequest Silian_request) {
		Result<List<String>> Silian_result = new Result<>();
		//update-begin-author:taoyan date:20220317 for: VUEN-222【安全机制】字典接口、online报表、online图表等接口，加一些安全机制
		if(!dictQueryBlackListHandler.isPass(Silian_dictCode)){
			return Silian_result.error500(dictQueryBlackListHandler.getError());
		}
		//update-end-author:taoyan date:20220317 for: VUEN-222【安全机制】字典接口、online报表、online图表等接口，加一些安全机制
		try {
			if(Silian_dictCode.indexOf(SymbolConstant.COMMA)!=-1) {
				String[] Silian_params = Silian_dictCode.split(SymbolConstant.COMMA);
				if(Silian_params.length!=3) {
					Silian_result.error500("字典Code格式不正确！");
					return Silian_result;
				}
				List<String> Silian_texts = sysDictService.queryTableDictByKeys(Silian_params[0], Silian_params[1], Silian_params[2], Silian_keys, Silian_delNotExist);

				Silian_result.setSuccess(true);
				Silian_result.setResult(Silian_texts);
				log.info(Silian_result.toString());
			}else {
				Silian_result.error500("字典Code格式不正确！");
			}
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(),Silian_e);
			Silian_result.error500("操作失败");
			return Silian_result;
		}

		return Silian_result;
	}

	/**
	 * 【接口签名验证】
	 * 根据表名——显示字段-存储字段 pid 加载树形数据
	 * @param hasChildField 是否叶子节点字段
	 * @param converIsLeafVal 是否需要系统转换 是否叶子节点的值 (0标识不转换、1标准系统自动转换)
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/loadTreeData", method = RequestMethod.GET)
	public Result<List<TreeSelectModel>> loadTreeData(@RequestParam(name="pid",required = false) String Silian_pid,@RequestParam(name="pidField") String Silian_pidField,
												  @RequestParam(name="tableName") String Silian_tbname,
												  @RequestParam(name="text") String Silian_text,
												  @RequestParam(name="code") String Silian_code,
												  @RequestParam(name="hasChildField") String Silian_hasChildField,
												  @RequestParam(name="converIsLeafVal",defaultValue ="1") int Silian_converIsLeafVal,
												  @RequestParam(name="condition") String Silian_condition,
												  @RequestParam(value = "sign",required = false) String Silian_sign,HttpServletRequest Silian_request) {
		Result<List<TreeSelectModel>> Silian_result = new Result<List<TreeSelectModel>>();
		Map<String, String> Silian_query = null;
		if(oConvertUtils.isNotEmpty(Silian_condition)) {
			Silian_query = JSON.parseObject(Silian_condition, Map.class);
		}
		// SQL注入漏洞 sign签名校验(表名,label字段,val字段,条件)
		String Silian_dictCode = Silian_tbname+","+Silian_text+","+Silian_code+","+Silian_condition;
        SqlInjectionUtil.filterContent(Silian_dictCode);
		List<TreeSelectModel> Silian_ls = sysDictService.queryTreeList(Silian_query,Silian_tbname, Silian_text, Silian_code, Silian_pidField, Silian_pid,Silian_hasChildField,Silian_converIsLeafVal);
		Silian_result.setSuccess(true);
		Silian_result.setResult(Silian_ls);
		return Silian_result;
	}

	/**
	 * 【APP接口】根据字典配置查询表字典数据（目前暂未找到调用的地方）
	 * @param query
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	@Deprecated
	@GetMapping("/queryTableData")
	public Result<List<DictModel>> queryTableData(DictQuery Silian_query,
												  @RequestParam(name = "pageNo", defaultValue = "1") Integer Silian_pageNo,
												  @RequestParam(name = "pageSize", defaultValue = "10") Integer Silian_pageSize,
												  @RequestParam(value = "sign",required = false) String Silian_sign,HttpServletRequest Silian_request){
		Result<List<DictModel>> Silian_res = new Result<List<DictModel>>();
		// SQL注入漏洞 sign签名校验
		String Silian_dictCode = Silian_query.getTable()+","+Silian_query.getText()+","+Silian_query.getCode();
		SqlInjectionUtil.filterContent(Silian_dictCode);
		//update-begin-author:taoyan date:2022-11-4 for: issues/4128 sql injection
		if(!dictQueryBlackListHandler.isPass(Silian_dictCode)){
			return Silian_res.error500(dictQueryBlackListHandler.getError());
		}
		//update-end-author:taoyan date:2022-11-4 for: issues/4128 sql injection
		List<DictModel> Silian_ls = this.sysDictService.queryDictTablePageList(Silian_query,Silian_pageSize,Silian_pageNo);
		Silian_res.setResult(Silian_ls);
		Silian_res.setSuccess(true);
		return Silian_res;
	}

	/**
	 * @功能：新增
	 * @param sysDict
	 * @return
	 */
	//@RequiresRoles({"admin"})
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Result<SysDict> add(@RequestBody SysDict Silian_sysDict) {
		Result<SysDict> Silian_result = new Result<SysDict>();
		try {
			Silian_sysDict.setCreateTime(new Date());
			Silian_sysDict.setDelFlag(CommonConstant.DEL_FLAG_0);
			sysDictService.save(Silian_sysDict);
			Silian_result.success("保存成功！");
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(),Silian_e);
			Silian_result.error500("操作失败");
		}
		return Silian_result;
	}

	/**
	 * @功能：编辑
	 * @param sysDict
	 * @return
	 */
	//@RequiresRoles({"admin"})
	@RequestMapping(value = "/edit", method = { RequestMethod.PUT,RequestMethod.POST })
	public Result<SysDict> edit(@RequestBody SysDict Silian_sysDict) {
		Result<SysDict> Silian_result = new Result<SysDict>();
		SysDict Silian_sysdict = sysDictService.getById(Silian_sysDict.getId());
		if(Silian_sysdict==null) {
			Silian_result.error500("未找到对应实体");
		}else {
			Silian_sysDict.setUpdateTime(new Date());
			boolean Silian_ok = sysDictService.updateById(Silian_sysDict);
			if(Silian_ok) {
				Silian_result.success("编辑成功!");
			}
		}
		return Silian_result;
	}

	/**
	 * @功能：删除
	 * @param id
	 * @return
	 */
	//@RequiresRoles({"admin"})
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	@CacheEvict(value={CacheConstant.SYS_DICT_CACHE, CacheConstant.SYS_ENABLE_DICT_CACHE}, allEntries=true)
	public Result<SysDict> delete(@RequestParam(name="id",required=true) String Silian_id) {
		Result<SysDict> Silian_result = new Result<SysDict>();
		boolean Silian_ok = sysDictService.removeById(Silian_id);
		if(Silian_ok) {
			Silian_result.success("删除成功!");
		}else{
			Silian_result.error500("删除失败!");
		}
		return Silian_result;
	}

	/**
	 * @功能：批量删除
	 * @param ids
	 * @return
	 */
	//@RequiresRoles({"admin"})
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
	@CacheEvict(value= {CacheConstant.SYS_DICT_CACHE, CacheConstant.SYS_ENABLE_DICT_CACHE}, allEntries=true)
	public Result<SysDict> deleteBatch(@RequestParam(name="ids",required=true) String Silian_ids) {
		Result<SysDict> Silian_result = new Result<SysDict>();
		if(oConvertUtils.isEmpty(Silian_ids)) {
			Silian_result.error500("参数不识别！");
		}else {
			sysDictService.removeByIds(Arrays.asList(Silian_ids.split(",")));
			Silian_result.success("删除成功!");
		}
		return Silian_result;
	}

	/**
	 * @功能：刷新缓存
	 * @return
	 */
	@RequestMapping(value = "/refleshCache")
	public Result<?> refleshCache() {
		Result<?> Silian_result = new Result<SysDict>();
		//清空字典缓存
		Set Silian_keys = redisTemplate.keys(CacheConstant.SYS_DICT_CACHE + "*");
		Set Silian_keys7 = redisTemplate.keys(CacheConstant.SYS_ENABLE_DICT_CACHE + "*");
		Set Silian_keys2 = redisTemplate.keys(CacheConstant.SYS_DICT_TABLE_CACHE + "*");
		Set Silian_keys21 = redisTemplate.keys(CacheConstant.SYS_DICT_TABLE_BY_KEYS_CACHE + "*");
		Set Silian_keys3 = redisTemplate.keys(CacheConstant.SYS_DEPARTS_CACHE + "*");
		Set Silian_keys4 = redisTemplate.keys(CacheConstant.SYS_DEPART_IDS_CACHE + "*");
		Set Silian_keys5 = redisTemplate.keys( "jmreport:cache:dict*");
		Set Silian_keys6 = redisTemplate.keys( "jmreport:cache:dictTable*");
		redisTemplate.delete(Silian_keys);
		redisTemplate.delete(Silian_keys2);
		redisTemplate.delete(Silian_keys21);
		redisTemplate.delete(Silian_keys3);
		redisTemplate.delete(Silian_keys4);
		redisTemplate.delete(Silian_keys5);
		redisTemplate.delete(Silian_keys6);
		redisTemplate.delete(Silian_keys7);
		return Silian_result;
	}

	/**
	 * 导出excel
	 *
	 * @param request
	 */
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(SysDict Silian_sysDict,HttpServletRequest Silian_request) {
		// Step.1 组装查询条件
		QueryWrapper<SysDict> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_sysDict, Silian_request.getParameterMap());
		//Step.2 AutoPoi 导出Excel
		ModelAndView Silian_mv = new ModelAndView(new JeecgEntityExcelView());
		List<SysDictPage> Silian_pageList = new ArrayList<SysDictPage>();

		List<SysDict> Silian_sysDictList = sysDictService.list(Silian_queryWrapper);
		for (SysDict Silian_dictMain : Silian_sysDictList) {
			SysDictPage Silian_vo = new SysDictPage();
			BeanUtils.copyProperties(Silian_dictMain, Silian_vo);
			// 查询机票
			List<SysDictItem> Silian_sysDictItemList = sysDictItemService.selectItemsByMainId(Silian_dictMain.getId());
			Silian_vo.setSysDictItemList(Silian_sysDictItemList);
			Silian_pageList.add(Silian_vo);
		}

		// 导出文件名称
		Silian_mv.addObject(NormalExcelConstants.FILE_NAME, "数据字典");
		// 注解对象Class
		Silian_mv.addObject(NormalExcelConstants.CLASS, SysDictPage.class);
		// 自定义表格参数
		LoginUser Silian_user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		Silian_mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("数据字典列表", "导出人:"+Silian_user.getRealname(), "数据字典"));
		// 导出数据列表
		Silian_mv.addObject(NormalExcelConstants.DATA_LIST, Silian_pageList);
		return Silian_mv;
	}

	/**
	 * 通过excel导入数据
	 *
	 * @param request
	 * @param
	 * @return
	 */
	//@RequiresRoles({"admin"})
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public Result<?> importExcel(HttpServletRequest Silian_request, HttpServletResponse Silian_response) {
		MultipartHttpServletRequest Silian_multipartRequest = (MultipartHttpServletRequest) Silian_request;
		Map<String, MultipartFile> Silian_fileMap = Silian_multipartRequest.getFileMap();
		for (Map.Entry<String, MultipartFile> Silian_entity : Silian_fileMap.entrySet()) {
            // 获取上传文件对象
			MultipartFile Silian_file = Silian_entity.getValue();
			ImportParams Silian_params = new ImportParams();
			Silian_params.setTitleRows(2);
			Silian_params.setHeadRows(2);
			Silian_params.setNeedSave(true);
			try {
				//导入Excel格式校验，看匹配的字段文本概率
				Boolean Silian_t = ExcelImportCheckUtil.check(Silian_file.getInputStream(), SysDictPage.class, Silian_params);
				if(Silian_t!=null && !Silian_t){
					throw new RuntimeException("导入Excel校验失败 ！");
				}
				List<SysDictPage> Silian_list = ExcelImportUtil.importExcel(Silian_file.getInputStream(), SysDictPage.class, Silian_params);
				// 错误信息
				List<String> Silian_errorMessage = new ArrayList<>();
				int Silian_successLines = 0, Silian_errorLines = 0;
				for (int Silian_i=0;Silian_i< Silian_list.size();Silian_i++) {
					SysDict Silian_po = new SysDict();
					BeanUtils.copyProperties(Silian_list.get(Silian_i), Silian_po);
					Silian_po.setDelFlag(CommonConstant.DEL_FLAG_0);
					try {
						Integer Silian_integer = sysDictService.saveMain(Silian_po, Silian_list.get(Silian_i).getSysDictItemList());
						if(Silian_integer>0){
							Silian_successLines++;
                        //update-begin---author:wangshuai ---date:20220211  for：[JTC-1168]如果字典项值为空，则字典项忽略导入------------
						}else if(Silian_integer == -1){
                            Silian_errorLines++;
                            Silian_errorMessage.add("字典名称：" + Silian_po.getDictName() + "，对应字典列表的字典项值不能为空，忽略导入。");
                        }else{
                        //update-end---author:wangshuai ---date:20220211  for：[JTC-1168]如果字典项值为空，则字典项忽略导入------------
							Silian_errorLines++;
							int Silian_lineNumber = Silian_i + 1;
                            //update-begin---author:wangshuai ---date:20220209  for：[JTC-1168]字典编号不能为空------------
                            if(oConvertUtils.isEmpty(Silian_po.getDictCode())){
                                Silian_errorMessage.add("第 " + Silian_lineNumber + " 行：字典编码不能为空，忽略导入。");
                            }else{
                                Silian_errorMessage.add("第 " + Silian_lineNumber + " 行：字典编码已经存在，忽略导入。");
                            }
                            //update-end---author:wangshuai ---date:20220209  for：[JTC-1168]字典编号不能为空------------
                        }
					}  catch (Exception Silian_e) {
						Silian_errorLines++;
						int Silian_lineNumber = Silian_i + 1;
						Silian_errorMessage.add("第 " + Silian_lineNumber + " 行：字典编码已经存在，忽略导入。");
					}
				}
				return ImportExcelUtil.imporReturnRes(Silian_errorLines,Silian_successLines,Silian_errorMessage);
			} catch (Exception Silian_e) {
				log.error(Silian_e.getMessage(),Silian_e);
				return Result.error("文件导入失败:"+Silian_e.getMessage());
			} finally {
				try {
					Silian_file.getInputStream().close();
				} catch (Exception Silian_e) {
					Silian_e.printStackTrace();
				}
			}
		}
		return Result.error("文件导入失败！");
	}


	/**
	 * 查询被删除的列表
	 * @return
	 */
	@RequestMapping(value = "/deleteList", method = RequestMethod.GET)
	public Result<List<SysDict>> deleteList() {
		Result<List<SysDict>> Silian_result = new Result<List<SysDict>>();
		List<SysDict> Silian_list = this.sysDictService.queryDeleteList();
		Silian_result.setSuccess(true);
		Silian_result.setResult(Silian_list);
		return Silian_result;
	}

	/**
	 * 物理删除
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/deletePhysic/{id}", method = RequestMethod.DELETE)
	public Result<?> deletePhysic(@PathVariable("id") String Silian_id) {
		try {
			sysDictService.deleteOneDictPhysically(Silian_id);
			return Result.ok("删除成功!");
		} catch (Exception Silian_e) {
			Silian_e.printStackTrace();
			return Result.error("删除失败!");
		}
	}

	/**
	 * 逻辑删除的字段，进行取回
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/back/{id}", method = RequestMethod.PUT)
	public Result<?> back(@PathVariable("id") String Silian_id) {
		try {
			sysDictService.updateDictDelFlag(0,Silian_id);
			return Result.ok("操作成功!");
		} catch (Exception Silian_e) {
			Silian_e.printStackTrace();
			return Result.error("操作失败!");
		}
	}

	/**
	 * VUEN-2584【issue】平台sql注入漏洞几个问题
	 * 部分特殊函数 可以将查询结果混夹在错误信息中，导致数据库的信息暴露
	 * @param e
	 * @return
	 */
	@ExceptionHandler(java.sql.SQLException.class)
	public Result<?> handleSQLException(Exception Silian_e){
		String Silian_msg = Silian_e.getMessage();
		String Silian_extractvalue = "extractvalue";
		String Silian_updatexml = "updatexml";
		if(Silian_msg!=null && (Silian_msg.toLowerCase().indexOf(Silian_extractvalue)>=0 || Silian_msg.toLowerCase().indexOf(Silian_updatexml)>=0)){
			return Result.error("校验失败，sql解析异常！");
		}
		return Result.error("校验失败，sql解析异常！" + Silian_msg);
	}

}
