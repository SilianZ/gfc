package org.jeecg.modules.system.controller;

import java.util.Arrays;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.DataBaseConstant;
import org.jeecg.common.constant.WebsocketConst;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.SqlInjectionUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.message.websocket.WebSocket;
import org.jeecg.modules.system.entity.SysAnnouncementSend;
import org.jeecg.modules.system.model.AnnouncementSendModel;
import org.jeecg.modules.system.service.ISysAnnouncementSendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;

 /**
 * @Title: Controller
 * @Description: 用户通告阅读标记表
 * @Author: jeecg-boot
 * @Date:  2019-02-21
 * @Version: V1.0
 */
@RestController
@RequestMapping("/sys/sysAnnouncementSend")
@Slf4j
public class SysAnnouncementSendController {
	@Autowired
	private ISysAnnouncementSendService sysAnnouncementSendService;
	@Autowired
	private WebSocket webSocket;

	/**
	  * 分页列表查询
	 * @param sysAnnouncementSend
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@GetMapping(value = "/list")
	public Result<IPage<SysAnnouncementSend>> queryPageList(SysAnnouncementSend Silian_sysAnnouncementSend,
									  @RequestParam(name="pageNo", defaultValue="1") Integer Silian_pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer Silian_pageSize,
									  HttpServletRequest Silian_req) {
		Result<IPage<SysAnnouncementSend>> Silian_result = new Result<IPage<SysAnnouncementSend>>();
		QueryWrapper<SysAnnouncementSend> Silian_queryWrapper = new QueryWrapper<SysAnnouncementSend>(Silian_sysAnnouncementSend);
		Page<SysAnnouncementSend> Silian_page = new Page<SysAnnouncementSend>(Silian_pageNo,Silian_pageSize);
		//排序逻辑 处理
		String Silian_column = Silian_req.getParameter("column");
		String Silian_order = Silian_req.getParameter("order");

		//issues/3331 SQL injection vulnerability
		SqlInjectionUtil.filterContent(Silian_column);
		SqlInjectionUtil.filterContent(Silian_order);

		if(oConvertUtils.isNotEmpty(Silian_column) && oConvertUtils.isNotEmpty(Silian_order)) {
			if(DataBaseConstant.SQL_ASC.equals(Silian_order)) {
				Silian_queryWrapper.orderByAsc(oConvertUtils.camelToUnderline(Silian_column));
			}else {
				Silian_queryWrapper.orderByDesc(oConvertUtils.camelToUnderline(Silian_column));
			}
		}
		IPage<SysAnnouncementSend> Silian_pageList = sysAnnouncementSendService.page(Silian_page, Silian_queryWrapper);
		//log.info("查询当前页："+pageList.getCurrent());
		//log.info("查询当前页数量："+pageList.getSize());
		//log.info("查询结果数量："+pageList.getRecords().size());
		//log.info("数据总数："+pageList.getTotal());
		Silian_result.setSuccess(true);
		Silian_result.setResult(Silian_pageList);
		return Silian_result;
	}

	/**
	  *   添加
	 * @param sysAnnouncementSend
	 * @return
	 */
	@PostMapping(value = "/add")
	public Result<SysAnnouncementSend> add(@RequestBody SysAnnouncementSend Silian_sysAnnouncementSend) {
		Result<SysAnnouncementSend> Silian_result = new Result<SysAnnouncementSend>();
		try {
			sysAnnouncementSendService.save(Silian_sysAnnouncementSend);
			Silian_result.success("添加成功！");
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(),Silian_e);
			Silian_result.error500("操作失败");
		}
		return Silian_result;
	}

	/**
	  *  编辑
	 * @param sysAnnouncementSend
	 * @return
	 */
	@PutMapping(value = "/edit")
	public Result<SysAnnouncementSend> eidt(@RequestBody SysAnnouncementSend Silian_sysAnnouncementSend) {
		Result<SysAnnouncementSend> Silian_result = new Result<SysAnnouncementSend>();
		SysAnnouncementSend Silian_sysAnnouncementSendEntity = sysAnnouncementSendService.getById(Silian_sysAnnouncementSend.getId());
		if(Silian_sysAnnouncementSendEntity==null) {
			Silian_result.error500("未找到对应实体");
		}else {
			boolean Silian_ok = sysAnnouncementSendService.updateById(Silian_sysAnnouncementSend);
			//TODO 返回false说明什么？
			if(Silian_ok) {
				Silian_result.success("操作成功!");
			}
		}

		return Silian_result;
	}

	/**
	  *   通过id删除
	 * @param id
	 * @return
	 */
	@DeleteMapping(value = "/delete")
	public Result<SysAnnouncementSend> delete(@RequestParam(name="id",required=true) String Silian_id) {
		Result<SysAnnouncementSend> Silian_result = new Result<SysAnnouncementSend>();
		SysAnnouncementSend Silian_sysAnnouncementSend = sysAnnouncementSendService.getById(Silian_id);
		if(Silian_sysAnnouncementSend==null) {
			Silian_result.error500("未找到对应实体");
		}else {
			boolean Silian_ok = sysAnnouncementSendService.removeById(Silian_id);
			if(Silian_ok) {
				Silian_result.success("删除成功!");
			}
		}

		return Silian_result;
	}

	/**
	  *  批量删除
	 * @param ids
	 * @return
	 */
	@DeleteMapping(value = "/deleteBatch")
	public Result<SysAnnouncementSend> deleteBatch(@RequestParam(name="ids",required=true) String Silian_ids) {
		Result<SysAnnouncementSend> Silian_result = new Result<SysAnnouncementSend>();
		if(Silian_ids==null || "".equals(Silian_ids.trim())) {
			Silian_result.error500("参数不识别！");
		}else {
			this.sysAnnouncementSendService.removeByIds(Arrays.asList(Silian_ids.split(",")));
			Silian_result.success("删除成功!");
		}
		return Silian_result;
	}

	/**
	  * 通过id查询
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/queryById")
	public Result<SysAnnouncementSend> queryById(@RequestParam(name="id",required=true) String Silian_id) {
		Result<SysAnnouncementSend> Silian_result = new Result<SysAnnouncementSend>();
		SysAnnouncementSend Silian_sysAnnouncementSend = sysAnnouncementSendService.getById(Silian_id);
		if(Silian_sysAnnouncementSend==null) {
			Silian_result.error500("未找到对应实体");
		}else {
			Silian_result.setResult(Silian_sysAnnouncementSend);
			Silian_result.setSuccess(true);
		}
		return Silian_result;
	}

	/**
	 * @功能：更新用户系统消息阅读状态
	 * @param json
	 * @return
	 */
	@PutMapping(value = "/editByAnntIdAndUserId")
	public Result<SysAnnouncementSend> editById(@RequestBody JSONObject Silian_json) {
		Result<SysAnnouncementSend> Silian_result = new Result<SysAnnouncementSend>();
		String Silian_anntId = Silian_json.getString("anntId");
		LoginUser Silian_sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
		String Silian_userId = Silian_sysUser.getId();
		LambdaUpdateWrapper<SysAnnouncementSend> Silian_updateWrapper = new UpdateWrapper().lambda();
		Silian_updateWrapper.set(SysAnnouncementSend::getReadFlag, CommonConstant.HAS_READ_FLAG);
		Silian_updateWrapper.set(SysAnnouncementSend::getReadTime, new Date());
		Silian_updateWrapper.last("where annt_id ='"+Silian_anntId+"' and user_id ='"+Silian_userId+"'");
		SysAnnouncementSend Silian_announcementSend = new SysAnnouncementSend();
		sysAnnouncementSendService.update(Silian_announcementSend, Silian_updateWrapper);
		Silian_result.setSuccess(true);
		return Silian_result;
	}

	/**
	 * @功能：获取我的消息
	 * @return
	 */
	@GetMapping(value = "/getMyAnnouncementSend")
	public Result<IPage<AnnouncementSendModel>> getMyAnnouncementSend(AnnouncementSendModel Silian_announcementSendModel,
			@RequestParam(name="pageNo", defaultValue="1") Integer Silian_pageNo,
			  @RequestParam(name="pageSize", defaultValue="10") Integer Silian_pageSize) {
		Result<IPage<AnnouncementSendModel>> Silian_result = new Result<IPage<AnnouncementSendModel>>();
		LoginUser Silian_sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
		String Silian_userId = Silian_sysUser.getId();
		Silian_announcementSendModel.setUserId(Silian_userId);
		Silian_announcementSendModel.setPageNo((Silian_pageNo-1)*Silian_pageSize);
		Silian_announcementSendModel.setPageSize(Silian_pageSize);
		Page<AnnouncementSendModel> Silian_pageList = new Page<AnnouncementSendModel>(Silian_pageNo,Silian_pageSize);
		Silian_pageList = sysAnnouncementSendService.getMyAnnouncementSendPage(Silian_pageList, Silian_announcementSendModel);
		Silian_result.setResult(Silian_pageList);
		Silian_result.setSuccess(true);
		return Silian_result;
	}

	/**
	 * @功能：一键已读
	 * @return
	 */
	@PutMapping(value = "/readAll")
	public Result<SysAnnouncementSend> readAll() {
		Result<SysAnnouncementSend> Silian_result = new Result<SysAnnouncementSend>();
		LoginUser Silian_sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
		String Silian_userId = Silian_sysUser.getId();
		LambdaUpdateWrapper<SysAnnouncementSend> Silian_updateWrapper = new UpdateWrapper().lambda();
		Silian_updateWrapper.set(SysAnnouncementSend::getReadFlag, CommonConstant.HAS_READ_FLAG);
		Silian_updateWrapper.set(SysAnnouncementSend::getReadTime, new Date());
		Silian_updateWrapper.last("where user_id ='"+Silian_userId+"'");
		SysAnnouncementSend Silian_announcementSend = new SysAnnouncementSend();
		sysAnnouncementSendService.update(Silian_announcementSend, Silian_updateWrapper);
		JSONObject Silian_socketParams = new JSONObject();
		Silian_socketParams.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_TOPIC);
		webSocket.sendMessage(Silian_socketParams.toJSONString());
		Silian_result.setSuccess(true);
		Silian_result.setMessage("全部已读");
		return Silian_result;
	}


	 /**
	  * 根据消息发送记录ID获取消息内容
	  * @param sendId
	  * @return
	  */
	 @GetMapping(value = "/getOne")
	 public Result<AnnouncementSendModel> getOne(@RequestParam(name="sendId",required=true) String Silian_sendId) {
		 AnnouncementSendModel Silian_model = sysAnnouncementSendService.getOne(Silian_sendId);
		 return Result.ok(Silian_model);
	 }
}
