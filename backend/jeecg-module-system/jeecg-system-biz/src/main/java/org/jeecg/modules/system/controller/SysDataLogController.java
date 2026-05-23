package org.jeecg.modules.system.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysDataLog;
import org.jeecg.modules.system.service.ISysDataLogService;
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
 * @Description: 系统数据日志
 * @author: jeecg-boot
 */
@RestController
@RequestMapping("/sys/dataLog")
@Slf4j
public class SysDataLogController {
	@Autowired
	private ISysDataLogService service;

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Result<IPage<SysDataLog>> queryPageList(SysDataLog Silian_dataLog,@RequestParam(name="pageNo", defaultValue="1") Integer Silian_pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer Silian_pageSize,HttpServletRequest Silian_req) {
		Result<IPage<SysDataLog>> Silian_result = new Result<IPage<SysDataLog>>();
		Silian_dataLog.setType(CommonConstant.DATA_LOG_TYPE_JSON);
		QueryWrapper<SysDataLog> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_dataLog, Silian_req.getParameterMap());
		Page<SysDataLog> Silian_page = new Page<SysDataLog>(Silian_pageNo, Silian_pageSize);
		IPage<SysDataLog> Silian_pageList = service.page(Silian_page, Silian_queryWrapper);
		log.info("查询当前页："+Silian_pageList.getCurrent());
		log.info("查询当前页数量："+Silian_pageList.getSize());
		log.info("查询结果数量："+Silian_pageList.getRecords().size());
		log.info("数据总数："+Silian_pageList.getTotal());
		Silian_result.setSuccess(true);
		Silian_result.setResult(Silian_pageList);
		return Silian_result;
	}

	/**
	 * 查询对比数据
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/queryCompareList", method = RequestMethod.GET)
	public Result<List<SysDataLog>> queryCompareList(HttpServletRequest Silian_req) {
		Result<List<SysDataLog>> Silian_result = new Result<>();
		String Silian_dataId1 = Silian_req.getParameter("dataId1");
		String Silian_dataId2 = Silian_req.getParameter("dataId2");
		List<String> Silian_idList = new ArrayList<String>();
		Silian_idList.add(Silian_dataId1);
		Silian_idList.add(Silian_dataId2);
		try {
			List<SysDataLog> Silian_list =  (List<SysDataLog>) service.listByIds(Silian_idList);
			Silian_result.setResult(Silian_list);
			Silian_result.setSuccess(true);
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(),Silian_e);
		}
		return Silian_result;
	}

	/**
	 * 查询版本信息
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/queryDataVerList", method = RequestMethod.GET)
	public Result<List<SysDataLog>> queryDataVerList(HttpServletRequest Silian_req) {
		Result<List<SysDataLog>> Silian_result = new Result<>();
		String Silian_dataTable = Silian_req.getParameter("dataTable");
		String Silian_dataId = Silian_req.getParameter("dataId");
		QueryWrapper<SysDataLog> Silian_queryWrapper = new QueryWrapper<SysDataLog>();
		Silian_queryWrapper.eq("data_table", Silian_dataTable);
		Silian_queryWrapper.eq("data_id", Silian_dataId);
		//update-begin-author:taoyan date:2022-7-26 for: 新增查询条件-type
		String Silian_type = Silian_req.getParameter("type");
		if (oConvertUtils.isNotEmpty(Silian_type)) {
			Silian_queryWrapper.eq("type", Silian_type);
		}
		// 按时间倒序排
		Silian_queryWrapper.orderByDesc("create_time");
		//update-end-author:taoyan date:2022-7-26 for:新增查询条件-type

		List<SysDataLog> Silian_list = service.list(Silian_queryWrapper);
		if(Silian_list==null||Silian_list.size()<=0) {
			Silian_result.error500("未找到版本信息");
		}else {
			Silian_result.setResult(Silian_list);
			Silian_result.setSuccess(true);
		}
		return Silian_result;
	}

}
