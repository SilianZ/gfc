package org.jeecg.modules.system.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jeecg.dingtalk.api.core.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.CommonSendStatus;
import org.jeecg.common.constant.WebsocketConst;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.message.enums.RangeDateEnum;
import org.jeecg.modules.message.websocket.WebSocket;
import org.jeecg.modules.system.entity.SysAnnouncement;
import org.jeecg.modules.system.entity.SysAnnouncementSend;
import org.jeecg.modules.system.service.ISysAnnouncementSendService;
import org.jeecg.modules.system.service.ISysAnnouncementService;
import org.jeecg.modules.system.service.impl.SysBaseApiImpl;
import org.jeecg.modules.system.service.impl.ThirdAppDingtalkServiceImpl;
import org.jeecg.modules.system.service.impl.ThirdAppWechatEnterpriseServiceImpl;
import org.jeecg.modules.system.util.XssUtils;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.jeecg.common.constant.CommonConstant.ANNOUNCEMENT_SEND_STATUS_1;

/**
 * @Title: Controller
 * @Description: 系统通告表
 * @Author: jeecg-boot
 * @Date: 2019-01-02
 * @Version: V1.0
 */
@RestController
@RequestMapping("/sys/annountCement")
@Slf4j
public class SysAnnouncementController {
	@Autowired
	private ISysAnnouncementService sysAnnouncementService;
	@Autowired
	private ISysAnnouncementSendService sysAnnouncementSendService;
	@Resource
    private WebSocket webSocket;
	@Autowired
    ThirdAppWechatEnterpriseServiceImpl wechatEnterpriseService;
	@Autowired
    ThirdAppDingtalkServiceImpl dingtalkService;
	@Autowired
	private SysBaseApiImpl sysBaseApi;
	@Autowired
	@Lazy
	private RedisUtil redisUtil;

	/**
	  * 分页列表查询
	 * @param sysAnnouncement
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Result<IPage<SysAnnouncement>> queryPageList(SysAnnouncement Silian_sysAnnouncement,
									  @RequestParam(name="pageNo", defaultValue="1") Integer Silian_pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer Silian_pageSize,
									  HttpServletRequest Silian_req) {
		Result<IPage<SysAnnouncement>> Silian_result = new Result<IPage<SysAnnouncement>>();
		Silian_sysAnnouncement.setDelFlag(CommonConstant.DEL_FLAG_0.toString());
		QueryWrapper<SysAnnouncement> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_sysAnnouncement, Silian_req.getParameterMap());
		Page<SysAnnouncement> Silian_page = new Page<SysAnnouncement>(Silian_pageNo,Silian_pageSize);

		//update-begin-author:lvdandan date:20211229 for: sqlserver mssql-jdbc 8.2.2.jre8版本下系统公告列表查询报错 查询SQL中生成了两个create_time DESC；故注释此段代码
		//排序逻辑 处理
//		String column = req.getParameter("column");
//		String order = req.getParameter("order");
//		if(oConvertUtils.isNotEmpty(column) && oConvertUtils.isNotEmpty(order)) {
//			if("asc".equals(order)) {
//				queryWrapper.orderByAsc(oConvertUtils.camelToUnderline(column));
//			}else {
//				queryWrapper.orderByDesc(oConvertUtils.camelToUnderline(column));
//			}
//		}
		//update-end-author:lvdandan date:20211229 for: sqlserver mssql-jdbc 8.2.2.jre8版本下系统公告列表查询报错 查询SQL中生成了两个create_time DESC；故注释此段代码
		IPage<SysAnnouncement> Silian_pageList = sysAnnouncementService.page(Silian_page, Silian_queryWrapper);
		Silian_result.setSuccess(true);
		Silian_result.setResult(Silian_pageList);
		return Silian_result;
	}

	/**
	  *   添加
	 * @param sysAnnouncement
	 * @return
	 */
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Result<SysAnnouncement> add(@RequestBody SysAnnouncement Silian_sysAnnouncement) {
		Result<SysAnnouncement> Silian_result = new Result<SysAnnouncement>();
		try {
			// update-begin-author:liusq date:20210804 for:标题处理xss攻击的问题
			String Silian_title = XssUtils.scriptXss(Silian_sysAnnouncement.getTitile());
			Silian_sysAnnouncement.setTitile(Silian_title);
			// update-end-author:liusq date:20210804 for:标题处理xss攻击的问题
			Silian_sysAnnouncement.setDelFlag(CommonConstant.DEL_FLAG_0.toString());
            //未发布
			Silian_sysAnnouncement.setSendStatus(CommonSendStatus.UNPUBLISHED_STATUS_0);
			sysAnnouncementService.saveAnnouncement(Silian_sysAnnouncement);
			Silian_result.success("添加成功！");
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(),Silian_e);
			Silian_result.error500("操作失败");
		}
		return Silian_result;
	}

	/**
	  *  编辑
	 * @param sysAnnouncement
	 * @return
	 */
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<SysAnnouncement> eidt(@RequestBody SysAnnouncement Silian_sysAnnouncement) {
		Result<SysAnnouncement> Silian_result = new Result<SysAnnouncement>();
		SysAnnouncement Silian_sysAnnouncementEntity = sysAnnouncementService.getById(Silian_sysAnnouncement.getId());
		if(Silian_sysAnnouncementEntity==null) {
			Silian_result.error500("未找到对应实体");
		}else {
			// update-begin-author:liusq date:20210804 for:标题处理xss攻击的问题
			String Silian_title = XssUtils.scriptXss(Silian_sysAnnouncement.getTitile());
			Silian_sysAnnouncement.setTitile(Silian_title);
			// update-end-author:liusq date:20210804 for:标题处理xss攻击的问题
			boolean Silian_ok = sysAnnouncementService.upDateAnnouncement(Silian_sysAnnouncement);
			//TODO 返回false说明什么？
			if(Silian_ok) {
				Silian_result.success("修改成功!");
			}
		}

		return Silian_result;
	}

	/**
	  *   通过id删除
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	public Result<SysAnnouncement> delete(@RequestParam(name="id",required=true) String Silian_id) {
		Result<SysAnnouncement> Silian_result = new Result<SysAnnouncement>();
		SysAnnouncement Silian_sysAnnouncement = sysAnnouncementService.getById(Silian_id);
		if(Silian_sysAnnouncement==null) {
			Silian_result.error500("未找到对应实体");
		}else {
			Silian_sysAnnouncement.setDelFlag(CommonConstant.DEL_FLAG_1.toString());
			boolean Silian_ok = sysAnnouncementService.updateById(Silian_sysAnnouncement);
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
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
	public Result<SysAnnouncement> deleteBatch(@RequestParam(name="ids",required=true) String Silian_ids) {
		Result<SysAnnouncement> Silian_result = new Result<SysAnnouncement>();
		if(Silian_ids==null || "".equals(Silian_ids.trim())) {
			Silian_result.error500("参数不识别！");
		}else {
			String[] Silian_id = Silian_ids.split(",");
			for(int Silian_i=0;Silian_i<Silian_id.length;Silian_i++) {
				SysAnnouncement Silian_announcement = sysAnnouncementService.getById(Silian_id[Silian_i]);
				Silian_announcement.setDelFlag(CommonConstant.DEL_FLAG_1.toString());
				sysAnnouncementService.updateById(Silian_announcement);
			}
			Silian_result.success("删除成功!");
		}
		return Silian_result;
	}

	/**
	  * 通过id查询
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/queryById", method = RequestMethod.GET)
	public Result<SysAnnouncement> queryById(@RequestParam(name="id",required=true) String Silian_id) {
		Result<SysAnnouncement> Silian_result = new Result<SysAnnouncement>();
		SysAnnouncement Silian_sysAnnouncement = sysAnnouncementService.getById(Silian_id);
		if(Silian_sysAnnouncement==null) {
			Silian_result.error500("未找到对应实体");
		}else {
			Silian_result.setResult(Silian_sysAnnouncement);
			Silian_result.setSuccess(true);
		}
		return Silian_result;
	}

	/**
	 *	 更新发布操作
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/doReleaseData", method = RequestMethod.GET)
	public Result<SysAnnouncement> doReleaseData(@RequestParam(name="id",required=true) String Silian_id, HttpServletRequest Silian_request) {
		Result<SysAnnouncement> Silian_result = new Result<SysAnnouncement>();
		SysAnnouncement Silian_sysAnnouncement = sysAnnouncementService.getById(Silian_id);
		if(Silian_sysAnnouncement==null) {
			Silian_result.error500("未找到对应实体");
		}else {
            //发布中
			Silian_sysAnnouncement.setSendStatus(CommonSendStatus.PUBLISHED_STATUS_1);
			Silian_sysAnnouncement.setSendTime(new Date());
			String Silian_currentUserName = JwtUtil.getUserNameByToken(Silian_request);
			Silian_sysAnnouncement.setSender(Silian_currentUserName);
			boolean Silian_ok = sysAnnouncementService.updateById(Silian_sysAnnouncement);
			if(Silian_ok) {
				Silian_result.success("该系统通知发布成功");
				if(Silian_sysAnnouncement.getMsgType().equals(CommonConstant.MSG_TYPE_ALL)) {
					JSONObject Silian_obj = new JSONObject();
				Silian_obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_TOPIC);
					Silian_obj.put(WebsocketConst.MSG_ID, Silian_sysAnnouncement.getId());
					Silian_obj.put(WebsocketConst.MSG_TXT, Silian_sysAnnouncement.getTitile());
				webSocket.sendMessage(Silian_obj.toJSONString());
				}else {
					// 2.插入用户通告阅读标记表记录
					String Silian_userId = Silian_sysAnnouncement.getUserIds();
					String[] Silian_userIds = Silian_userId.substring(0, (Silian_userId.length()-1)).split(",");
					String Silian_anntId = Silian_sysAnnouncement.getId();
					Date Silian_refDate = new Date();
					JSONObject Silian_obj = new JSONObject();
				Silian_obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
					Silian_obj.put(WebsocketConst.MSG_ID, Silian_sysAnnouncement.getId());
					Silian_obj.put(WebsocketConst.MSG_TXT, Silian_sysAnnouncement.getTitile());
				webSocket.sendMessage(Silian_userIds, Silian_obj.toJSONString());
				}
				try {
					// 同步企业微信、钉钉的消息通知
					Response<String> Silian_dtResponse = dingtalkService.sendActionCardMessage(Silian_sysAnnouncement, true);
					wechatEnterpriseService.sendTextCardMessage(Silian_sysAnnouncement, true);

					if (Silian_dtResponse != null && Silian_dtResponse.isSuccess()) {
						String Silian_taskId = Silian_dtResponse.getResult();
						Silian_sysAnnouncement.setDtTaskId(Silian_taskId);
						sysAnnouncementService.updateById(Silian_sysAnnouncement);
					}
				} catch (Exception Silian_e) {
					log.error("同步发送第三方APP消息失败：", Silian_e);
				}
			}
		}

		return Silian_result;
	}

	/**
	 *	 更新撤销操作
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/doReovkeData", method = RequestMethod.GET)
	public Result<SysAnnouncement> doReovkeData(@RequestParam(name="id",required=true) String Silian_id, HttpServletRequest Silian_request) {
		Result<SysAnnouncement> Silian_result = new Result<SysAnnouncement>();
		SysAnnouncement Silian_sysAnnouncement = sysAnnouncementService.getById(Silian_id);
		if(Silian_sysAnnouncement==null) {
			Silian_result.error500("未找到对应实体");
		}else {
            //撤销发布
			Silian_sysAnnouncement.setSendStatus(CommonSendStatus.REVOKE_STATUS_2);
			Silian_sysAnnouncement.setCancelTime(new Date());
			boolean Silian_ok = sysAnnouncementService.updateById(Silian_sysAnnouncement);
			if(Silian_ok) {
				Silian_result.success("该系统通知撤销成功");
				if (oConvertUtils.isNotEmpty(Silian_sysAnnouncement.getDtTaskId())) {
					try {
						dingtalkService.recallMessage(Silian_sysAnnouncement.getDtTaskId());
					} catch (Exception Silian_e) {
						log.error("第三方APP撤回消息失败：", Silian_e);
					}
				}
			}
		}

		return Silian_result;
	}

	/**
	 * @功能：补充用户数据，并返回系统消息
	 * @return
	 */
	@RequestMapping(value = "/listByUser", method = RequestMethod.GET)
	public Result<Map<String, Object>> listByUser(@RequestParam(required = false, defaultValue = "5") Integer Silian_pageSize) {
		Result<Map<String,Object>> Silian_result = new Result<Map<String,Object>>();
		LoginUser Silian_sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
		String Silian_userId = Silian_sysUser.getId();
		// 1.将系统消息补充到用户通告阅读标记表中
		LambdaQueryWrapper<SysAnnouncement> Silian_querySaWrapper = new LambdaQueryWrapper<SysAnnouncement>();
        //全部人员
		Silian_querySaWrapper.eq(SysAnnouncement::getMsgType,CommonConstant.MSG_TYPE_ALL);
        //未删除
		Silian_querySaWrapper.eq(SysAnnouncement::getDelFlag,CommonConstant.DEL_FLAG_0.toString());
        //已发布
		Silian_querySaWrapper.eq(SysAnnouncement::getSendStatus, CommonConstant.HAS_SEND);
        //新注册用户不看结束通知
		Silian_querySaWrapper.ge(SysAnnouncement::getEndTime, Silian_sysUser.getCreateTime());
		//update-begin--Author:liusq  Date:20210108 for：[JT-424] 【开源issue】bug处理--------------------
		Silian_querySaWrapper.notInSql(SysAnnouncement::getId,"select annt_id from sys_announcement_send where user_id='"+Silian_userId+"'");
		//update-begin--Author:liusq  Date:20210108  for： [JT-424] 【开源issue】bug处理--------------------
		List<SysAnnouncement> Silian_announcements = sysAnnouncementService.list(Silian_querySaWrapper);
		if(Silian_announcements.size()>0) {
			for(int Silian_i=0;Silian_i<Silian_announcements.size();Silian_i++) {
				//update-begin--Author:wangshuai  Date:20200803  for： 通知公告消息重复LOWCOD-759--------------------
				//因为websocket没有判断是否存在这个用户，要是判断会出现问题，故在此判断逻辑
				LambdaQueryWrapper<SysAnnouncementSend> Silian_query = new LambdaQueryWrapper<>();
				Silian_query.eq(SysAnnouncementSend::getAnntId,Silian_announcements.get(Silian_i).getId());
				Silian_query.eq(SysAnnouncementSend::getUserId,Silian_userId);
				SysAnnouncementSend Silian_one = sysAnnouncementSendService.getOne(Silian_query);
				if(null==Silian_one){
					log.info("listByUser接口新增了SysAnnouncementSend：pageSize{}："+Silian_pageSize);
					SysAnnouncementSend Silian_announcementSend = new SysAnnouncementSend();
					Silian_announcementSend.setAnntId(Silian_announcements.get(Silian_i).getId());
					Silian_announcementSend.setUserId(Silian_userId);
					Silian_announcementSend.setReadFlag(CommonConstant.NO_READ_FLAG);
					sysAnnouncementSendService.save(Silian_announcementSend);
					log.info("announcementSend.toString()",Silian_announcementSend.toString());
				}
				//update-end--Author:wangshuai  Date:20200803  for： 通知公告消息重复LOWCOD-759------------
			}
		}
		// 2.查询用户未读的系统消息
		Page<SysAnnouncement> Silian_anntMsgList = new Page<SysAnnouncement>(0, Silian_pageSize);
        //通知公告消息
		Silian_anntMsgList = sysAnnouncementService.querySysCementPageByUserId(Silian_anntMsgList,Silian_userId,"1");
		Page<SysAnnouncement> Silian_sysMsgList = new Page<SysAnnouncement>(0, Silian_pageSize);
        //系统消息
		Silian_sysMsgList = sysAnnouncementService.querySysCementPageByUserId(Silian_sysMsgList,Silian_userId,"2");
		Map<String,Object> Silian_sysMsgMap = new HashMap(5);
		Silian_sysMsgMap.put("sysMsgList", Silian_sysMsgList.getRecords());
		Silian_sysMsgMap.put("sysMsgTotal", Silian_sysMsgList.getTotal());
		Silian_sysMsgMap.put("anntMsgList", Silian_anntMsgList.getRecords());
		Silian_sysMsgMap.put("anntMsgTotal", Silian_anntMsgList.getTotal());
		Silian_result.setSuccess(true);
		Silian_result.setResult(Silian_sysMsgMap);
		return Silian_result;
	}


    /**
     * 导出excel
     *
     * @param request
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(SysAnnouncement Silian_sysAnnouncement,HttpServletRequest Silian_request) {
        // Step.1 组装查询条件
        LambdaQueryWrapper<SysAnnouncement> Silian_queryWrapper = new LambdaQueryWrapper<SysAnnouncement>(Silian_sysAnnouncement);
        //Step.2 AutoPoi 导出Excel
        ModelAndView Silian_mv = new ModelAndView(new JeecgEntityExcelView());
		Silian_queryWrapper.eq(SysAnnouncement::getDelFlag,CommonConstant.DEL_FLAG_0.toString());
        List<SysAnnouncement> Silian_pageList = sysAnnouncementService.list(Silian_queryWrapper);
        //导出文件名称
        Silian_mv.addObject(NormalExcelConstants.FILE_NAME, "系统通告列表");
        Silian_mv.addObject(NormalExcelConstants.CLASS, SysAnnouncement.class);
        LoginUser Silian_user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        Silian_mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("系统通告列表数据", "导出人:"+Silian_user.getRealname(), "导出信息"));
        Silian_mv.addObject(NormalExcelConstants.DATA_LIST, Silian_pageList);
        return Silian_mv;
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest Silian_request, HttpServletResponse Silian_response) {
        MultipartHttpServletRequest Silian_multipartRequest = (MultipartHttpServletRequest) Silian_request;
        Map<String, MultipartFile> Silian_fileMap = Silian_multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> Silian_entity : Silian_fileMap.entrySet()) {
            // 获取上传文件对象
            MultipartFile Silian_file = Silian_entity.getValue();
            ImportParams Silian_params = new ImportParams();
            Silian_params.setTitleRows(2);
            Silian_params.setHeadRows(1);
            Silian_params.setNeedSave(true);
            try {
                List<SysAnnouncement> Silian_listSysAnnouncements = ExcelImportUtil.importExcel(Silian_file.getInputStream(), SysAnnouncement.class, Silian_params);
                for (SysAnnouncement Silian_sysAnnouncementExcel : Silian_listSysAnnouncements) {
	if(Silian_sysAnnouncementExcel.getDelFlag()==null){
		Silian_sysAnnouncementExcel.setDelFlag(CommonConstant.DEL_FLAG_0.toString());
					}
                    sysAnnouncementService.save(Silian_sysAnnouncementExcel);
                }
                return Result.ok("文件导入成功！数据行数：" + Silian_listSysAnnouncements.size());
            } catch (Exception Silian_e) {
                log.error(Silian_e.getMessage(),Silian_e);
                return Result.error("文件导入失败！");
            } finally {
                try {
                    Silian_file.getInputStream().close();
                } catch (IOException Silian_e) {
                    Silian_e.printStackTrace();
                }
            }
        }
        return Result.error("文件导入失败！");
    }
	/**
	 *同步消息
	 * @param anntId
	 * @return
	 */
	@RequestMapping(value = "/syncNotic", method = RequestMethod.GET)
	public Result<SysAnnouncement> syncNotic(@RequestParam(name="anntId",required=false) String Silian_anntId, HttpServletRequest Silian_request) {
		Result<SysAnnouncement> Silian_result = new Result<SysAnnouncement>();
		JSONObject Silian_obj = new JSONObject();
		if(StringUtils.isNotBlank(Silian_anntId)){
			SysAnnouncement Silian_sysAnnouncement = sysAnnouncementService.getById(Silian_anntId);
			if(Silian_sysAnnouncement==null) {
				Silian_result.error500("未找到对应实体");
			}else {
				if(Silian_sysAnnouncement.getMsgType().equals(CommonConstant.MSG_TYPE_ALL)) {
					Silian_obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_TOPIC);
					Silian_obj.put(WebsocketConst.MSG_ID, Silian_sysAnnouncement.getId());
					Silian_obj.put(WebsocketConst.MSG_TXT, Silian_sysAnnouncement.getTitile());
					webSocket.sendMessage(Silian_obj.toJSONString());
				}else {
					// 2.插入用户通告阅读标记表记录
					String Silian_userId = Silian_sysAnnouncement.getUserIds();
					if(oConvertUtils.isNotEmpty(Silian_userId)){
						String[] Silian_userIds = Silian_userId.substring(0, (Silian_userId.length()-1)).split(",");
						Silian_obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
						Silian_obj.put(WebsocketConst.MSG_ID, Silian_sysAnnouncement.getId());
						Silian_obj.put(WebsocketConst.MSG_TXT, Silian_sysAnnouncement.getTitile());
						webSocket.sendMessage(Silian_userIds, Silian_obj.toJSONString());
					}
				}
			}
		}else{
			Silian_obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_TOPIC);
			Silian_obj.put(WebsocketConst.MSG_TXT, "批量设置已读");
			webSocket.sendMessage(Silian_obj.toJSONString());
		}
		return Silian_result;
	}

	/**
	 * 通告查看详情页面（用于第三方APP）
	 * @param modelAndView
	 * @param id
	 * @return
	 */
    @GetMapping("/show/{id}")
    public ModelAndView showContent(ModelAndView Silian_modelAndView, @PathVariable("id") String Silian_id, HttpServletRequest Silian_request) {
        SysAnnouncement Silian_announcement = sysAnnouncementService.getById(Silian_id);
        if (Silian_announcement != null) {
            boolean Silian_tokenOk = false;
            try {
                // 验证Token有效性
                Silian_tokenOk = TokenUtils.verifyToken(Silian_request, sysBaseApi, redisUtil);
            } catch (Exception Silian_ignored) {
            }
            // 判断是否传递了Token，并且Token有效，如果传了就不做查看限制，直接返回
            // 如果Token无效，就做查看限制：只能查看已发布的
            if (Silian_tokenOk || ANNOUNCEMENT_SEND_STATUS_1.equals(Silian_announcement.getSendStatus())) {
                Silian_modelAndView.addObject("data", Silian_announcement);
                Silian_modelAndView.setViewName("announcement/showContent");
                return Silian_modelAndView;
            }
        }
        Silian_modelAndView.setStatus(HttpStatus.NOT_FOUND);
        return Silian_modelAndView;
    }

	/**
	 * 【vue3用】 消息列表查询
	 * @param fromUser
	 * @param beginDate
	 * @param endDate
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(value = "/vue3List", method = RequestMethod.GET)
	public Result<List<SysAnnouncement>> vue3List(@RequestParam(name="fromUser", required = false) String Silian_fromUser,
												  @RequestParam(name="starFlag", required = false) String Silian_starFlag,
                                                  @RequestParam(name="rangeDateKey", required = false) String Silian_rangeDateKey,
                                                  @RequestParam(name="beginDate", required = false) String Silian_beginDate, @RequestParam(name="endDate", required = false) String Silian_endDate,
                                                  @RequestParam(name="pageNo", defaultValue="1") Integer Silian_pageNo, @RequestParam(name="pageSize", defaultValue="10") Integer Silian_pageSize) {
		// 后台获取开始时间/结束时间
		Date Silian_bd=null, Silian_ed=null;
		if(RangeDateEnum.ZDY.getKey().equals(Silian_rangeDateKey)){
			if(oConvertUtils.isNotEmpty(Silian_beginDate)){
				Silian_bd = DateUtils.parseDatetime(Silian_beginDate);
			}
			if(oConvertUtils.isNotEmpty(Silian_endDate)){
				Silian_ed = DateUtils.parseDatetime(Silian_endDate);
			}
		}else{
			Date[] Silian_arr = RangeDateEnum.getRangeArray(Silian_rangeDateKey);
			if(Silian_arr!=null){
				Silian_bd = Silian_arr[0];
				Silian_ed = Silian_arr[1];
			}
		}
		List<SysAnnouncement> Silian_ls = this.sysAnnouncementService.querySysMessageList(Silian_pageSize, Silian_pageNo, Silian_fromUser, Silian_starFlag, Silian_bd, Silian_ed);
		//查询出来的消息全部设置为已读
		if(Silian_ls!=null && Silian_ls.size()>0){
			String Silian_readed = "1";
			List<String> Silian_annoceIdList = Silian_ls.stream().filter(Silian_item->!Silian_readed.equals(Silian_item.getReadFlag())).map(Silian_item->Silian_item.getId()).collect(Collectors.toList());
			if(Silian_annoceIdList!=null && Silian_annoceIdList.size()>0){
				sysAnnouncementService.updateReaded(Silian_annoceIdList);
			}
		}
		return Result.ok(Silian_ls);
	}

}
