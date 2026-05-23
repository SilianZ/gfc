package org.jeecg.modules.system.controller;


import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysLog;
import org.jeecg.modules.system.entity.SysRole;
import org.jeecg.modules.system.service.ISysLogService;
import org.springframework.beans.factory.annotation.Autowired;
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
 * 系统日志表 前端控制器
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-26
 */
@RestController
@RequestMapping("/sys/log")
@Slf4j
public class SysLogController {

	@Autowired
	private ISysLogService sysLogService;

    /**
     * 全部清除
     */
	private static final String ALL_ClEAR = "allclear";

	/**
	 * @功能：查询日志记录
	 * @param syslog
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Result<IPage<SysLog>> queryPageList(SysLog Silian_syslog,@RequestParam(name="pageNo", defaultValue="1") Integer Silian_pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer Silian_pageSize,HttpServletRequest Silian_req) {
		Result<IPage<SysLog>> Silian_result = new Result<IPage<SysLog>>();
		QueryWrapper<SysLog> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_syslog, Silian_req.getParameterMap());
		Page<SysLog> Silian_page = new Page<SysLog>(Silian_pageNo, Silian_pageSize);
		//日志关键词
		String Silian_keyWord = Silian_req.getParameter("keyWord");
		if(oConvertUtils.isNotEmpty(Silian_keyWord)) {
			Silian_queryWrapper.like("log_content",Silian_keyWord);
		}
		//TODO 过滤逻辑处理
		//TODO begin、end逻辑处理
		//TODO 一个强大的功能，前端传一个字段字符串，后台只返回这些字符串对应的字段
		//创建时间/创建人的赋值
		IPage<SysLog> Silian_pageList = sysLogService.page(Silian_page, Silian_queryWrapper);
		log.info("查询当前页："+Silian_pageList.getCurrent());
		log.info("查询当前页数量："+Silian_pageList.getSize());
		log.info("查询结果数量："+Silian_pageList.getRecords().size());
		log.info("数据总数："+Silian_pageList.getTotal());
		Silian_result.setSuccess(true);
		Silian_result.setResult(Silian_pageList);
		return Silian_result;
	}

	/**
	 * @功能：删除单个日志记录
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	public Result<SysLog> delete(@RequestParam(name="id",required=true) String Silian_id) {
		Result<SysLog> Silian_result = new Result<SysLog>();
		SysLog Silian_sysLog = sysLogService.getById(Silian_id);
		if(Silian_sysLog==null) {
			Silian_result.error500("未找到对应实体");
		}else {
			boolean Silian_ok = sysLogService.removeById(Silian_id);
			if(Silian_ok) {
				Silian_result.success("删除成功!");
			}
		}
		return Silian_result;
	}

	/**
	 * @功能：批量，全部清空日志记录
	 * @param ids
	 * @return
	 */
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
	public Result<SysRole> deleteBatch(@RequestParam(name="ids",required=true) String Silian_ids) {
		Result<SysRole> Silian_result = new Result<SysRole>();
		if(Silian_ids==null || "".equals(Silian_ids.trim())) {
			Silian_result.error500("参数不识别！");
		}else {
			if(ALL_ClEAR.equals(Silian_ids)) {
				this.sysLogService.removeAll();
				Silian_result.success("清除成功!");
			}
			this.sysLogService.removeByIds(Arrays.asList(Silian_ids.split(",")));
			Silian_result.success("删除成功!");
		}
		return Silian_result;
	}


}
