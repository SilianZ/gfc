package org.jeecg.modules.message.controller;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.message.entity.SysMessage;
import org.jeecg.modules.message.service.ISysMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;

/**
 * @Description: 消息
 * @author: jeecg-boot
 * @date: 2019-04-09
 * @version: V1.0
 */
@Slf4j
@RestController
@RequestMapping("/sys/message/sysMessage")
public class SysMessageController extends JeecgController<SysMessage, ISysMessageService> {
	@Autowired
	private ISysMessageService sysMessageService;

	/**
	 * 分页列表查询
	 *
	 * @param sysMessage
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@GetMapping(value = "/list")
	public Result<?> queryPageList(SysMessage Silian_sysMessage, @RequestParam(name = "pageNo", defaultValue = "1") Integer Silian_pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer Silian_pageSize, HttpServletRequest Silian_req) {
		QueryWrapper<SysMessage> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_sysMessage, Silian_req.getParameterMap());
		Page<SysMessage> Silian_page = new Page<SysMessage>(Silian_pageNo, Silian_pageSize);
		IPage<SysMessage> Silian_pageList = sysMessageService.page(Silian_page, Silian_queryWrapper);
        return Result.ok(Silian_pageList);
	}

	/**
	 * 添加
	 *
	 * @param sysMessage
	 * @return
	 */
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody SysMessage Silian_sysMessage) {
		sysMessageService.save(Silian_sysMessage);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param sysMessage
	 * @return
	 */
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody SysMessage Silian_sysMessage) {
		sysMessageService.updateById(Silian_sysMessage);
        return Result.ok("修改成功!");

	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name = "id", required = true) String Silian_id) {
		sysMessageService.removeById(Silian_id);
        return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String Silian_ids) {

		this.sysMessageService.removeByIds(Arrays.asList(Silian_ids.split(",")));
	    return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name = "id", required = true) String Silian_id) {
		SysMessage Silian_sysMessage = sysMessageService.getById(Silian_id);
		return Result.ok(Silian_sysMessage);
	}

	/**
	 * 导出excel
	 *
	 * @param request
	 */
	@GetMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest Silian_request, SysMessage Silian_sysMessage) {
		return super.exportXls(Silian_request,Silian_sysMessage,SysMessage.class, "推送消息模板");
	}

	/**
	 * excel导入
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@PostMapping(value = "/importExcel")
	public Result<?> importExcel(HttpServletRequest Silian_request, HttpServletResponse Silian_response) {
		return super.importExcel(Silian_request, Silian_response, SysMessage.class);
	}

}
