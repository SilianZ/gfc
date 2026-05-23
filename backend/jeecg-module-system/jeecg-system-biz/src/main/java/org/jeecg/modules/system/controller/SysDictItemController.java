package org.jeecg.modules.system.controller;


import java.util.Arrays;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysDictItem;
import org.jeecg.modules.system.service.ISysDictItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-28
 */
@Api(tags = "数据字典")
@RestController
@RequestMapping("/sys/dictItem")
@Slf4j
public class SysDictItemController {

	@Autowired
	private ISysDictItemService sysDictItemService;

	/**
	 * @功能：查询字典数据
	 * @param sysDictItem
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Result<IPage<SysDictItem>> queryPageList(SysDictItem Silian_sysDictItem,@RequestParam(name="pageNo", defaultValue="1") Integer Silian_pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer Silian_pageSize,HttpServletRequest Silian_req) {
		Result<IPage<SysDictItem>> Silian_result = new Result<IPage<SysDictItem>>();
		QueryWrapper<SysDictItem> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_sysDictItem, Silian_req.getParameterMap());
		Silian_queryWrapper.orderByAsc("sort_order");
		Page<SysDictItem> Silian_page = new Page<SysDictItem>(Silian_pageNo, Silian_pageSize);
		IPage<SysDictItem> Silian_pageList = sysDictItemService.page(Silian_page, Silian_queryWrapper);
		Silian_result.setSuccess(true);
		Silian_result.setResult(Silian_pageList);
		return Silian_result;
	}

	/**
	 * @功能：新增
	 * @return
	 */
	//@RequiresRoles({"admin"})
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@CacheEvict(value= {CacheConstant.SYS_DICT_CACHE, CacheConstant.SYS_ENABLE_DICT_CACHE}, allEntries=true)
	public Result<SysDictItem> add(@RequestBody SysDictItem Silian_sysDictItem) {
		Result<SysDictItem> Silian_result = new Result<SysDictItem>();
		try {
			Silian_sysDictItem.setCreateTime(new Date());
			sysDictItemService.save(Silian_sysDictItem);
			Silian_result.success("保存成功！");
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(),Silian_e);
			Silian_result.error500("操作失败");
		}
		return Silian_result;
	}

	/**
	 * @功能：编辑
	 * @param sysDictItem
	 * @return
	 */
	//@RequiresRoles({"admin"})
	@RequestMapping(value = "/edit",  method = { RequestMethod.PUT,RequestMethod.POST })
	@CacheEvict(value={CacheConstant.SYS_DICT_CACHE, CacheConstant.SYS_ENABLE_DICT_CACHE}, allEntries=true)
	public Result<SysDictItem> edit(@RequestBody SysDictItem Silian_sysDictItem) {
		Result<SysDictItem> Silian_result = new Result<SysDictItem>();
		SysDictItem Silian_sysdict = sysDictItemService.getById(Silian_sysDictItem.getId());
		if(Silian_sysdict==null) {
			Silian_result.error500("未找到对应实体");
		}else {
			Silian_sysDictItem.setUpdateTime(new Date());
			boolean Silian_ok = sysDictItemService.updateById(Silian_sysDictItem);
			//TODO 返回false说明什么？
			if(Silian_ok) {
				Silian_result.success("编辑成功!");
			}
		}
		return Silian_result;
	}

	/**
	 * @功能：删除字典数据
	 * @param id
	 * @return
	 */
	//@RequiresRoles({"admin"})
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	@CacheEvict(value={CacheConstant.SYS_DICT_CACHE, CacheConstant.SYS_ENABLE_DICT_CACHE}, allEntries=true)
	public Result<SysDictItem> delete(@RequestParam(name="id",required=true) String Silian_id) {
		Result<SysDictItem> Silian_result = new Result<SysDictItem>();
		SysDictItem Silian_joinSystem = sysDictItemService.getById(Silian_id);
		if(Silian_joinSystem==null) {
			Silian_result.error500("未找到对应实体");
		}else {
			boolean Silian_ok = sysDictItemService.removeById(Silian_id);
			if(Silian_ok) {
				Silian_result.success("删除成功!");
			}
		}
		return Silian_result;
	}

	/**
	 * @功能：批量删除字典数据
	 * @param ids
	 * @return
	 */
	//@RequiresRoles({"admin"})
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
	@CacheEvict(value={CacheConstant.SYS_DICT_CACHE, CacheConstant.SYS_ENABLE_DICT_CACHE}, allEntries=true)
	public Result<SysDictItem> deleteBatch(@RequestParam(name="ids",required=true) String Silian_ids) {
		Result<SysDictItem> Silian_result = new Result<SysDictItem>();
		if(Silian_ids==null || "".equals(Silian_ids.trim())) {
			Silian_result.error500("参数不识别！");
		}else {
			this.sysDictItemService.removeByIds(Arrays.asList(Silian_ids.split(",")));
			Silian_result.success("删除成功!");
		}
		return Silian_result;
	}

	/**
	 * 字典值重复校验
	 * @param sysDictItem
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/dictItemCheck", method = RequestMethod.GET)
	@ApiOperation("字典重复校验接口")
	public Result<Object> doDictItemCheck(SysDictItem Silian_sysDictItem, HttpServletRequest Silian_request) {
		Long Silian_num = Long.valueOf(0);
		LambdaQueryWrapper<SysDictItem> Silian_queryWrapper = new LambdaQueryWrapper<SysDictItem>();
		Silian_queryWrapper.eq(SysDictItem::getItemValue,Silian_sysDictItem.getItemValue());
		Silian_queryWrapper.eq(SysDictItem::getDictId,Silian_sysDictItem.getDictId());
		if (StringUtils.isNotBlank(Silian_sysDictItem.getId())) {
			// 编辑页面校验
			Silian_queryWrapper.ne(SysDictItem::getId,Silian_sysDictItem.getId());
		}
		Silian_num = sysDictItemService.count(Silian_queryWrapper);
		if (Silian_num == 0) {
			// 该值可用
			return Result.ok("该值可用！");
		} else {
			// 该值不可用
			log.info("该值不可用，系统中已存在！");
			return Result.error("该值不可用，系统中已存在！");
		}
	}

}
