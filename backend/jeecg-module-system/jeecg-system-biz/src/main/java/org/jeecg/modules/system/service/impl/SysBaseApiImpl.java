package org.jeecg.modules.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.dto.DataLogDTO;
import org.jeecg.common.api.dto.OnlineAuthDTO;
import org.jeecg.common.api.dto.message.*;
import org.jeecg.common.aspect.UrlMatchEnum;
import org.jeecg.common.constant.*;
import org.jeecg.common.constant.enums.MessageTypeEnum;
import org.jeecg.common.desensitization.util.SensitiveInfoUtil;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.*;
import org.jeecg.common.util.*;
import org.jeecg.common.util.dynamic.db.FreemarkerParseFactory;
import org.jeecg.modules.message.entity.SysMessageTemplate;
import org.jeecg.modules.message.handle.impl.DdSendMsgHandle;
import org.jeecg.modules.message.handle.impl.EmailSendMsgHandle;
import org.jeecg.modules.message.handle.impl.QywxSendMsgHandle;
import org.jeecg.modules.message.handle.impl.SystemSendMsgHandle;
import org.jeecg.modules.message.service.ISysMessageTemplateService;
import org.jeecg.modules.message.websocket.WebSocket;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.mapper.*;
import org.jeecg.modules.system.service.*;
import org.jeecg.modules.system.util.SecurityUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * @Description: 底层共通业务API，提供其他独立模块调用
 * @Author: scott
 * @Date:2019-4-20
 * @Version:V1.0
 */
@Slf4j
@Service
public class SysBaseApiImpl implements ISysBaseAPI {
	/** 当前系统数据库类型 */
	private static String DB_TYPE = "";

	@Autowired
	private ISysMessageTemplateService sysMessageTemplateService;
	@Resource
	private SysUserMapper userMapper;
	@Resource
	private SysUserRoleMapper sysUserRoleMapper;
	@Autowired
	private ISysDepartService sysDepartService;
	@Autowired
	private ISysDictService sysDictService;
	@Resource
	private SysAnnouncementMapper sysAnnouncementMapper;
	@Resource
	private SysAnnouncementSendMapper sysAnnouncementSendMapper;
	@Resource
    private WebSocket webSocket;
	@Resource
	private SysRoleMapper roleMapper;
	@Resource
	private SysDepartMapper departMapper;
	@Resource
	private SysCategoryMapper categoryMapper;
	@Autowired
	private ISysDataSourceService dataSourceService;
	@Autowired
	private ISysUserDepartService sysUserDepartService;
	@Resource
	private SysPermissionMapper sysPermissionMapper;
	@Autowired
	private ISysPermissionDataRuleService sysPermissionDataRuleService;
	@Autowired
	private ThirdAppWechatEnterpriseServiceImpl wechatEnterpriseService;
	@Autowired
	private ThirdAppDingtalkServiceImpl dingtalkService;
	@Autowired
	ISysCategoryService sysCategoryService;
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ISysDataLogService sysDataLogService;
	@Autowired
	private ISysFilesService sysFilesService;

	@Override
	//@SensitiveDecode
	public LoginUser getUserByName(String Silian_username) {
		//update-begin-author:taoyan date:2022-6-6 for: VUEN-1276 【v3流程图】测试bug 1、通过我发起的流程或者流程实例，查看历史，流程图预览问题
		if (oConvertUtils.isEmpty(Silian_username)) {
			return null;
		}
		//update-end-author:taoyan date:2022-6-6 for: VUEN-1276 【v3流程图】测试bug 1、通过我发起的流程或者流程实例，查看历史，流程图预览问题
		LoginUser Silian_user = sysUserService.getEncodeUserInfo(Silian_username);

		//相同类中方法间调用时脱敏解密 Aop会失效，获取用户信息太重要，此处采用原生解密方法，不采用@SensitiveDecodeAble注解方式
		try {
			SensitiveInfoUtil.handlerObject(Silian_user, false);
		} catch (IllegalAccessException Silian_e) {
			Silian_e.printStackTrace();
		}

		return Silian_user;
	}

	@Override
	public String translateDictFromTable(String Silian_table, String Silian_text, String Silian_code, String Silian_key) {
		return sysDictService.queryTableDictTextByKey(Silian_table, Silian_text, Silian_code, Silian_key);
	}

	@Override
	public String translateDict(String Silian_code, String Silian_key) {
		return sysDictService.queryDictTextByKey(Silian_code, Silian_key);
	}

	@Override
	public List<SysPermissionDataRuleModel> queryPermissionDataRule(String Silian_component, String Silian_requestPath, String Silian_username) {
		List<SysPermission> Silian_currentSyspermission = null;
		if(oConvertUtils.isNotEmpty(Silian_component)) {
			//1.通过注解属性pageComponent 获取菜单
			LambdaQueryWrapper<SysPermission> Silian_query = new LambdaQueryWrapper<SysPermission>();
			Silian_query.eq(SysPermission::getDelFlag,0);
			Silian_query.eq(SysPermission::getComponent, Silian_component);
			Silian_currentSyspermission = sysPermissionMapper.selectList(Silian_query);
		}else {
			//1.直接通过前端请求地址查询菜单
			LambdaQueryWrapper<SysPermission> Silian_query = new LambdaQueryWrapper<SysPermission>();
			Silian_query.eq(SysPermission::getMenuType,2);
			Silian_query.eq(SysPermission::getDelFlag,0);
			Silian_query.eq(SysPermission::getUrl, Silian_requestPath);
			Silian_currentSyspermission = sysPermissionMapper.selectList(Silian_query);
			//2.未找到 再通过自定义匹配URL 获取菜单
			if(Silian_currentSyspermission==null || Silian_currentSyspermission.size()==0) {
				//通过自定义URL匹配规则 获取菜单（实现通过菜单配置数据权限规则，实际上针对获取数据接口进行数据规则控制）
				String Silian_userMatchUrl = UrlMatchEnum.getMatchResultByUrl(Silian_requestPath);
				LambdaQueryWrapper<SysPermission> Silian_queryQserMatch = new LambdaQueryWrapper<SysPermission>();
				// update-begin-author:taoyan date:20211027 for: online菜单如果配置成一级菜单 权限查询不到 取消menuType = 1
				//queryQserMatch.eq(SysPermission::getMenuType, 1);
				// update-end-author:taoyan date:20211027 for: online菜单如果配置成一级菜单 权限查询不到 取消menuType = 1
				Silian_queryQserMatch.eq(SysPermission::getDelFlag, 0);
				Silian_queryQserMatch.eq(SysPermission::getUrl, Silian_userMatchUrl);
				if(oConvertUtils.isNotEmpty(Silian_userMatchUrl)){
					Silian_currentSyspermission = sysPermissionMapper.selectList(Silian_queryQserMatch);
				}
			}
			//3.未找到 再通过正则匹配获取菜单
			if(Silian_currentSyspermission==null || Silian_currentSyspermission.size()==0) {
				//通过正则匹配权限配置
				String Silian_regUrl = getRegexpUrl(Silian_requestPath);
				if(Silian_regUrl!=null) {
					Silian_currentSyspermission = sysPermissionMapper.selectList(new LambdaQueryWrapper<SysPermission>().eq(SysPermission::getMenuType,2).eq(SysPermission::getUrl, Silian_regUrl).eq(SysPermission::getDelFlag,0));
				}
			}
		}
		if(Silian_currentSyspermission!=null && Silian_currentSyspermission.size()>0){
			List<SysPermissionDataRuleModel> Silian_dataRules = new ArrayList<SysPermissionDataRuleModel>();
			for (SysPermission Silian_sysPermission : Silian_currentSyspermission) {
				// update-begin--Author:scott Date:20191119 for：数据权限规则编码不规范，项目存在相同包名和类名 #722
				List<SysPermissionDataRule> Silian_temp = sysPermissionDataRuleService.queryPermissionDataRules(Silian_username, Silian_sysPermission.getId());
				if(Silian_temp!=null && Silian_temp.size()>0) {
					//dataRules.addAll(temp);
					Silian_dataRules = oConvertUtils.entityListToModelList(Silian_temp,SysPermissionDataRuleModel.class);
				}
				// update-end--Author:scott Date:20191119 for：数据权限规则编码不规范，项目存在相同包名和类名 #722
			}
			return Silian_dataRules;
		}
		return null;
	}

	/**
	 * 匹配前端传过来的地址 匹配成功返回正则地址
	 * AntPathMatcher匹配地址
	 *()* 匹配0个或多个字符
	 *()**匹配0个或多个目录
	 */
	private String getRegexpUrl(String Silian_url) {
		List<String> Silian_list = sysPermissionMapper.queryPermissionUrlWithStar();
		if(Silian_list!=null && Silian_list.size()>0) {
			for (String Silian_p : Silian_list) {
				PathMatcher Silian_matcher = new AntPathMatcher();
				if(Silian_matcher.match(Silian_p, Silian_url)) {
					return Silian_p;
				}
			}
		}
		return null;
	}

	@Override
	public SysUserCacheInfo getCacheUser(String Silian_username) {
		SysUserCacheInfo Silian_info = new SysUserCacheInfo();
		Silian_info.setOneDepart(true);
		LoginUser Silian_user = this.getUserByName(Silian_username);

//		try {
//			//相同类中方法间调用时脱敏@SensitiveDecodeAble解密 Aop失效处理
//			SensitiveInfoUtil.handlerObject(user, false);
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		}

		if(Silian_user!=null) {
			Silian_info.setSysUserCode(Silian_user.getUsername());
			Silian_info.setSysUserName(Silian_user.getRealname());
			Silian_info.setSysOrgCode(Silian_user.getOrgCode());
		}else{
			return null;
		}
		//多部门支持in查询
		List<SysDepart> Silian_list = departMapper.queryUserDeparts(Silian_user.getId());
		List<String> Silian_sysMultiOrgCode = new ArrayList<String>();
		if(Silian_list==null || Silian_list.size()==0) {
			//当前用户无部门
			//sysMultiOrgCode.add("0");
		}else if(Silian_list.size()==1) {
			Silian_sysMultiOrgCode.add(Silian_list.get(0).getOrgCode());
		}else {
			Silian_info.setOneDepart(false);
			for (SysDepart Silian_dpt : Silian_list) {
				Silian_sysMultiOrgCode.add(Silian_dpt.getOrgCode());
			}
		}
		Silian_info.setSysMultiOrgCode(Silian_sysMultiOrgCode);
		return Silian_info;
	}

	@Override
	public LoginUser getUserById(String Silian_id) {
		if(oConvertUtils.isEmpty(Silian_id)) {
			return null;
		}
		LoginUser Silian_loginUser = new LoginUser();
		SysUser Silian_sysUser = userMapper.selectById(Silian_id);
		if(Silian_sysUser==null) {
			return null;
		}
		BeanUtils.copyProperties(Silian_sysUser, Silian_loginUser);
		return Silian_loginUser;
	}

	@Override
	public List<String> getRolesByUsername(String Silian_username) {
		return sysUserRoleMapper.getRoleByUserName(Silian_username);
	}

	@Override
	public List<String> getDepartIdsByUsername(String Silian_username) {
		List<SysDepart> Silian_list = sysDepartService.queryDepartsByUsername(Silian_username);
		List<String> Silian_result = new ArrayList<>(Silian_list.size());
		for (SysDepart Silian_depart : Silian_list) {
			Silian_result.add(Silian_depart.getId());
		}
		return Silian_result;
	}

	@Override
	public List<String> getDepartNamesByUsername(String Silian_username) {
		List<SysDepart> Silian_list = sysDepartService.queryDepartsByUsername(Silian_username);
		List<String> Silian_result = new ArrayList<>(Silian_list.size());
		for (SysDepart Silian_depart : Silian_list) {
			Silian_result.add(Silian_depart.getDepartName());
		}
		return Silian_result;
	}

	@Override
	public DictModel getParentDepartId(String Silian_departId) {
		SysDepart Silian_depart = departMapper.getParentDepartId(Silian_departId);
		DictModel Silian_model = new DictModel(Silian_depart.getId(),Silian_depart.getParentId());
		return Silian_model;
	}

	@Override
	@Cacheable(value = CacheConstant.SYS_DICT_CACHE,Silian_key = "#code", unless = "#result == null ")
	public List<DictModel> queryDictItemsByCode(String Silian_code) {
		return sysDictService.queryDictItemsByCode(Silian_code);
	}

	@Override
	@Cacheable(value = CacheConstant.SYS_ENABLE_DICT_CACHE,Silian_key = "#code", unless = "#result == null ")
	public List<DictModel> queryEnableDictItemsByCode(String Silian_code) {
		return sysDictService.queryEnableDictItemsByCode(Silian_code);
	}

	@Override
	public List<DictModel> queryTableDictItemsByCode(String Silian_table, String Silian_text, String Silian_code) {
		//update-begin-author:taoyan date:20200820 for:【Online+系统】字典表加权限控制机制逻辑，想法不错 LOWCOD-799
		if(Silian_table.indexOf(SymbolConstant.SYS_VAR_PREFIX)>=0){
			Silian_table = QueryGenerator.getSqlRuleValue(Silian_table);
		}
		//update-end-author:taoyan date:20200820 for:【Online+系统】字典表加权限控制机制逻辑，想法不错 LOWCOD-799
		String[] Silian_arr = new String[]{Silian_text, Silian_code};
		SqlInjectionUtil.filterContent(Silian_arr);
		SqlInjectionUtil.specialFilterContentForDictSql(Silian_table);
		return sysDictService.queryTableDictItemsByCode(Silian_table, Silian_text, Silian_code);
	}

	@Override
	public List<DictModel> queryAllDepartBackDictModel() {
		return sysDictService.queryAllDepartBackDictModel();
	}

	@Override
	public void sendSysAnnouncement(MessageDTO Silian_message) {
		this.sendSysAnnouncement(Silian_message.getFromUser(),
				Silian_message.getToUser(),
				Silian_message.getTitle(),
				Silian_message.getContent(),
				Silian_message.getCategory());
		try {
			// 同步发送第三方APP消息
			wechatEnterpriseService.sendMessage(Silian_message, true);
			dingtalkService.sendMessage(Silian_message, true);
		} catch (Exception Silian_e) {
			log.error("同步发送第三方APP消息失败！", Silian_e);
		}
	}

	@Override
	public void sendBusAnnouncement(BusMessageDTO Silian_message) {
		sendBusAnnouncement(Silian_message.getFromUser(),
				Silian_message.getToUser(),
				Silian_message.getTitle(),
				Silian_message.getContent(),
				Silian_message.getCategory(),
				Silian_message.getBusType(),
				Silian_message.getBusId());
		try {
			// 同步发送第三方APP消息
			wechatEnterpriseService.sendMessage(Silian_message, true);
			dingtalkService.sendMessage(Silian_message, true);
		} catch (Exception Silian_e) {
			log.error("同步发送第三方APP消息失败！", Silian_e);
		}
	}

	@Override
	public void sendTemplateAnnouncement(TemplateMessageDTO Silian_message) {
		String Silian_templateCode = Silian_message.getTemplateCode();
		String Silian_title = Silian_message.getTitle();
		Map<String,String> Silian_map = Silian_message.getTemplateParam();
		String Silian_fromUser = Silian_message.getFromUser();
		String Silian_toUser = Silian_message.getToUser();

		List<SysMessageTemplate> Silian_sysSmsTemplates = sysMessageTemplateService.selectByCode(Silian_templateCode);
		if(Silian_sysSmsTemplates==null||Silian_sysSmsTemplates.size()==0){
			throw new JeecgBootException("消息模板不存在，模板编码："+Silian_templateCode);
		}
		SysMessageTemplate Silian_sysSmsTemplate = Silian_sysSmsTemplates.get(0);
		//模板标题
		Silian_title = Silian_title==null?Silian_sysSmsTemplate.getTemplateName():Silian_title;
		//模板内容
		String Silian_content = Silian_sysSmsTemplate.getTemplateContent();
		if(Silian_map!=null) {
			for (Map.Entry<String, String> Silian_entry : Silian_map.entrySet()) {
				String Silian_str = "${" + Silian_entry.getKey() + "}";
				if(oConvertUtils.isNotEmpty(Silian_title)){
					Silian_title = Silian_title.replace(Silian_str, Silian_entry.getValue());
				}
				Silian_content = Silian_content.replace(Silian_str, Silian_entry.getValue());
			}
		}

		SysAnnouncement Silian_announcement = new SysAnnouncement();
		Silian_announcement.setTitile(Silian_title);
		Silian_announcement.setMsgContent(Silian_content);
		Silian_announcement.setSender(Silian_fromUser);
		Silian_announcement.setPriority(CommonConstant.PRIORITY_M);
		Silian_announcement.setMsgType(CommonConstant.MSG_TYPE_UESR);
		Silian_announcement.setSendStatus(CommonConstant.HAS_SEND);
		Silian_announcement.setSendTime(new Date());
		Silian_announcement.setMsgCategory(CommonConstant.MSG_CATEGORY_2);
		Silian_announcement.setDelFlag(String.valueOf(CommonConstant.DEL_FLAG_0));
		sysAnnouncementMapper.insert(Silian_announcement);
		// 2.插入用户通告阅读标记表记录
		String Silian_userId = Silian_toUser;
		String[] Silian_userIds = Silian_userId.split(",");
		String Silian_anntId = Silian_announcement.getId();
		for(int Silian_i=0;Silian_i<Silian_userIds.length;Silian_i++) {
			if(oConvertUtils.isNotEmpty(Silian_userIds[Silian_i])) {
				SysUser Silian_sysUser = userMapper.getUserByName(Silian_userIds[Silian_i]);
				if(Silian_sysUser==null) {
					continue;
				}
				SysAnnouncementSend Silian_announcementSend = new SysAnnouncementSend();
				Silian_announcementSend.setAnntId(Silian_anntId);
				Silian_announcementSend.setUserId(Silian_sysUser.getId());
				Silian_announcementSend.setReadFlag(CommonConstant.NO_READ_FLAG);
				sysAnnouncementSendMapper.insert(Silian_announcementSend);
				JSONObject Silian_obj = new JSONObject();
				Silian_obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
				Silian_obj.put(WebsocketConst.MSG_USER_ID, Silian_sysUser.getId());
				Silian_obj.put(WebsocketConst.MSG_ID, Silian_announcement.getId());
				Silian_obj.put(WebsocketConst.MSG_TXT, Silian_announcement.getTitile());
				webSocket.sendMessage(Silian_sysUser.getId(), Silian_obj.toJSONString());
			}
		}
		try {
			// 同步企业微信、钉钉的消息通知
			dingtalkService.sendActionCardMessage(Silian_announcement, true);
			wechatEnterpriseService.sendTextCardMessage(Silian_announcement, true);
		} catch (Exception Silian_e) {
			log.error("同步发送第三方APP消息失败！", Silian_e);
		}

	}

	@Override
	public void sendBusTemplateAnnouncement(BusTemplateMessageDTO Silian_message) {
		String Silian_templateCode = Silian_message.getTemplateCode();
		String Silian_title = Silian_message.getTitle();
		Map<String,String> Silian_map = Silian_message.getTemplateParam();
		String Silian_fromUser = Silian_message.getFromUser();
		String Silian_toUser = Silian_message.getToUser();
		String Silian_busId = Silian_message.getBusId();
		String Silian_busType = Silian_message.getBusType();

		List<SysMessageTemplate> Silian_sysSmsTemplates = sysMessageTemplateService.selectByCode(Silian_templateCode);
		if(Silian_sysSmsTemplates==null||Silian_sysSmsTemplates.size()==0){
			throw new JeecgBootException("消息模板不存在，模板编码："+Silian_templateCode);
		}
		SysMessageTemplate Silian_sysSmsTemplate = Silian_sysSmsTemplates.get(0);
		//模板标题
		Silian_title = Silian_title==null?Silian_sysSmsTemplate.getTemplateName():Silian_title;
		//模板内容
		String Silian_content = Silian_sysSmsTemplate.getTemplateContent();
		if(Silian_map!=null) {
			for (Map.Entry<String, String> Silian_entry : Silian_map.entrySet()) {
				String Silian_str = "${" + Silian_entry.getKey() + "}";
				Silian_title = Silian_title.replace(Silian_str, Silian_entry.getValue());
				Silian_content = Silian_content.replace(Silian_str, Silian_entry.getValue());
			}
		}
		SysAnnouncement Silian_announcement = new SysAnnouncement();
		Silian_announcement.setTitile(Silian_title);
		Silian_announcement.setMsgContent(Silian_content);
		Silian_announcement.setSender(Silian_fromUser);
		Silian_announcement.setPriority(CommonConstant.PRIORITY_M);
		Silian_announcement.setMsgType(CommonConstant.MSG_TYPE_UESR);
		Silian_announcement.setSendStatus(CommonConstant.HAS_SEND);
		Silian_announcement.setSendTime(new Date());
		Silian_announcement.setMsgCategory(CommonConstant.MSG_CATEGORY_2);
		Silian_announcement.setDelFlag(String.valueOf(CommonConstant.DEL_FLAG_0));
		Silian_announcement.setBusId(Silian_busId);
		Silian_announcement.setBusType(Silian_busType);
		Silian_announcement.setOpenType(SysAnnmentTypeEnum.getByType(Silian_busType).getOpenType());
		Silian_announcement.setOpenPage(SysAnnmentTypeEnum.getByType(Silian_busType).getOpenPage());
		sysAnnouncementMapper.insert(Silian_announcement);
		// 2.插入用户通告阅读标记表记录
		String Silian_userId = Silian_toUser;
		String[] Silian_userIds = Silian_userId.split(",");
		String Silian_anntId = Silian_announcement.getId();
		for(int Silian_i=0;Silian_i<Silian_userIds.length;Silian_i++) {
			if(oConvertUtils.isNotEmpty(Silian_userIds[Silian_i])) {
				SysUser Silian_sysUser = userMapper.getUserByName(Silian_userIds[Silian_i]);
				if(Silian_sysUser==null) {
					continue;
				}
				SysAnnouncementSend Silian_announcementSend = new SysAnnouncementSend();
				Silian_announcementSend.setAnntId(Silian_anntId);
				Silian_announcementSend.setUserId(Silian_sysUser.getId());
				Silian_announcementSend.setReadFlag(CommonConstant.NO_READ_FLAG);
				sysAnnouncementSendMapper.insert(Silian_announcementSend);
				JSONObject Silian_obj = new JSONObject();
				Silian_obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
				Silian_obj.put(WebsocketConst.MSG_USER_ID, Silian_sysUser.getId());
				Silian_obj.put(WebsocketConst.MSG_ID, Silian_announcement.getId());
				Silian_obj.put(WebsocketConst.MSG_TXT, Silian_announcement.getTitile());
				webSocket.sendMessage(Silian_sysUser.getId(), Silian_obj.toJSONString());
			}
		}
		try {
			// 同步企业微信、钉钉的消息通知
			dingtalkService.sendActionCardMessage(Silian_announcement, true);
			wechatEnterpriseService.sendTextCardMessage(Silian_announcement, true);
		} catch (Exception Silian_e) {
			log.error("同步发送第三方APP消息失败！", Silian_e);
		}

	}

	@Override
	public String parseTemplateByCode(TemplateDTO Silian_templateDTO) {
		String Silian_templateCode = Silian_templateDTO.getTemplateCode();
		Map<String, String> Silian_map = Silian_templateDTO.getTemplateParam();
		List<SysMessageTemplate> Silian_sysSmsTemplates = sysMessageTemplateService.selectByCode(Silian_templateCode);
		if(Silian_sysSmsTemplates==null||Silian_sysSmsTemplates.size()==0){
			throw new JeecgBootException("消息模板不存在，模板编码："+Silian_templateCode);
		}
		SysMessageTemplate Silian_sysSmsTemplate = Silian_sysSmsTemplates.get(0);
		//模板内容
		String Silian_content = Silian_sysSmsTemplate.getTemplateContent();
		if(Silian_map!=null) {
			for (Map.Entry<String, String> Silian_entry : Silian_map.entrySet()) {
				String Silian_str = "${" + Silian_entry.getKey() + "}";
				Silian_content = Silian_content.replace(Silian_str, Silian_entry.getValue());
			}
		}
		return Silian_content;
	}

	@Override
	public void updateSysAnnounReadFlag(String Silian_busType, String Silian_busId) {
		SysAnnouncement Silian_announcement = sysAnnouncementMapper.selectOne(new QueryWrapper<SysAnnouncement>().eq("bus_type",Silian_busType).eq("bus_id",Silian_busId));
		if(Silian_announcement != null){
			LoginUser Silian_sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
			String Silian_userId = Silian_sysUser.getId();
			LambdaUpdateWrapper<SysAnnouncementSend> Silian_updateWrapper = new UpdateWrapper().lambda();
			Silian_updateWrapper.set(SysAnnouncementSend::getReadFlag, CommonConstant.HAS_READ_FLAG);
			Silian_updateWrapper.set(SysAnnouncementSend::getReadTime, new Date());
			Silian_updateWrapper.last("where annt_id ='"+Silian_announcement.getId()+"' and user_id ='"+Silian_userId+"'");
			SysAnnouncementSend Silian_announcementSend = new SysAnnouncementSend();
			sysAnnouncementSendMapper.update(Silian_announcementSend, Silian_updateWrapper);
		}
	}

	/**
	 * 获取数据库类型
	 * @param dataSource
	 * @return
	 * @throws SQLException
	 */
	private String getDatabaseTypeByDataSource(DataSource Silian_dataSource) throws SQLException{
		if("".equals(DB_TYPE)) {
			Connection Silian_connection = Silian_dataSource.getConnection();
			try {
				DatabaseMetaData Silian_md = Silian_connection.getMetaData();
				String Silian_dbType = Silian_md.getDatabaseProductName().toLowerCase();
				if(Silian_dbType.indexOf(DataBaseConstant.DB_TYPE_MYSQL.toLowerCase())>=0) {
					DB_TYPE = DataBaseConstant.DB_TYPE_MYSQL;
				}else if(Silian_dbType.indexOf(DataBaseConstant.DB_TYPE_ORACLE.toLowerCase())>=0) {
					DB_TYPE = DataBaseConstant.DB_TYPE_ORACLE;
				}else if(Silian_dbType.indexOf(DataBaseConstant.DB_TYPE_SQLSERVER.toLowerCase())>=0||Silian_dbType.indexOf(DataBaseConstant.DB_TYPE_SQL_SERVER_BLANK)>=0) {
					DB_TYPE = DataBaseConstant.DB_TYPE_SQLSERVER;
				}else if(Silian_dbType.indexOf(DataBaseConstant.DB_TYPE_POSTGRESQL.toLowerCase())>=0) {
					DB_TYPE = DataBaseConstant.DB_TYPE_POSTGRESQL;
				}else if(Silian_dbType.indexOf(DataBaseConstant.DB_TYPE_MARIADB.toLowerCase())>=0) {
					DB_TYPE = DataBaseConstant.DB_TYPE_MARIADB;
				}else {
					log.error("数据库类型:[" + Silian_dbType + "]不识别!");
					//throw new JeecgBootException("数据库类型:["+dbType+"]不识别!");
				}
			} catch (Exception Silian_e) {
				log.error(Silian_e.getMessage(), Silian_e);
			}finally {
				Silian_connection.close();
			}
		}
		return DB_TYPE;

	}

	@Override
	public List<DictModel> queryAllDict() {
		// 查询并排序
		QueryWrapper<SysDict> Silian_queryWrapper = new QueryWrapper<SysDict>();
		Silian_queryWrapper.orderByAsc("create_time");
		List<SysDict> Silian_dicts = sysDictService.list(Silian_queryWrapper);
		// 封装成 model
		List<DictModel> Silian_list = new ArrayList<DictModel>();
		for (SysDict Silian_dict : Silian_dicts) {
			Silian_list.add(new DictModel(Silian_dict.getDictCode(), Silian_dict.getDictName()));
		}

		return Silian_list;
	}

	@Override
	public List<SysCategoryModel> queryAllSysCategory() {
		List<SysCategory> Silian_ls = categoryMapper.selectList(null);
		List<SysCategoryModel> Silian_res = oConvertUtils.entityListToModelList(Silian_ls,SysCategoryModel.class);
		return Silian_res;
	}

	@Override
	public List<DictModel> queryFilterTableDictInfo(String Silian_table, String Silian_text, String Silian_code, String Silian_filterSql) {
		return sysDictService.queryTableDictItemsByCodeAndFilter(Silian_table,Silian_text,Silian_code,Silian_filterSql);
	}

	@Override
	public List<String> queryTableDictByKeys(String Silian_table, String Silian_text, String Silian_code, String[] Silian_keyArray) {
		return sysDictService.queryTableDictByKeys(Silian_table,Silian_text,Silian_code,Joiner.on(",").join(Silian_keyArray));
	}

	@Override
	public List<ComboModel> queryAllUserBackCombo() {
		List<ComboModel> Silian_list = new ArrayList<ComboModel>();
		List<SysUser> Silian_userList = userMapper.selectList(new QueryWrapper<SysUser>().eq("status",1).eq("del_flag",0));
		for(SysUser Silian_user : Silian_userList){
			ComboModel Silian_model = new ComboModel();
			Silian_model.setTitle(Silian_user.getRealname());
			Silian_model.setId(Silian_user.getId());
			Silian_model.setUsername(Silian_user.getUsername());
			Silian_list.add(Silian_model);
		}
		return Silian_list;
	}

	@Override
	public JSONObject queryAllUser(String Silian_userIds, Integer Silian_pageNo, Integer Silian_pageSize) {
		JSONObject Silian_json = new JSONObject();
		QueryWrapper<SysUser> Silian_queryWrapper = new QueryWrapper<SysUser>().eq("status",1).eq("del_flag",0);
		List<ComboModel> Silian_list = new ArrayList<ComboModel>();
		Page<SysUser> Silian_page = new Page<SysUser>(Silian_pageNo, Silian_pageSize);
		IPage<SysUser> Silian_pageList = userMapper.selectPage(Silian_page, Silian_queryWrapper);
		for(SysUser Silian_user : Silian_pageList.getRecords()){
			ComboModel Silian_model = new ComboModel();
			Silian_model.setUsername(Silian_user.getUsername());
			Silian_model.setTitle(Silian_user.getRealname());
			Silian_model.setId(Silian_user.getId());
			Silian_model.setEmail(Silian_user.getEmail());
			if(oConvertUtils.isNotEmpty(Silian_userIds)){
				String[] Silian_temp = Silian_userIds.split(",");
				for(int Silian_i = 0; Silian_i<Silian_temp.length;Silian_i++){
					if(Silian_temp[Silian_i].equals(Silian_user.getId())){
						Silian_model.setChecked(true);
					}
				}
			}
			Silian_list.add(Silian_model);
		}
		Silian_json.put("list",Silian_list);
		Silian_json.put("total",Silian_pageList.getTotal());
		return Silian_json;
	}

	@Override
	public List<ComboModel> queryAllRole() {
		List<ComboModel> Silian_list = new ArrayList<ComboModel>();
		List<SysRole> Silian_roleList = roleMapper.selectList(new QueryWrapper<SysRole>());
		for(SysRole Silian_role : Silian_roleList){
			ComboModel Silian_model = new ComboModel();
			Silian_model.setTitle(Silian_role.getRoleName());
			Silian_model.setId(Silian_role.getId());
			Silian_list.add(Silian_model);
		}
		return Silian_list;
	}

    @Override
    public List<ComboModel> queryAllRole(String[] Silian_roleIds) {
        List<ComboModel> Silian_list = new ArrayList<ComboModel>();
        List<SysRole> Silian_roleList = roleMapper.selectList(new QueryWrapper<SysRole>());
        for(SysRole Silian_role : Silian_roleList){
            ComboModel Silian_model = new ComboModel();
            Silian_model.setTitle(Silian_role.getRoleName());
            Silian_model.setId(Silian_role.getId());
            Silian_model.setRoleCode(Silian_role.getRoleCode());
            if(oConvertUtils.isNotEmpty(Silian_roleIds)) {
                for (int Silian_i = 0; Silian_i < Silian_roleIds.length; Silian_i++) {
                    if (Silian_roleIds[Silian_i].equals(Silian_role.getId())) {
                        Silian_model.setChecked(true);
                    }
                }
            }
            Silian_list.add(Silian_model);
        }
        return Silian_list;
    }

	@Override
	public List<String> getRoleIdsByUsername(String Silian_username) {
		return sysUserRoleMapper.getRoleIdByUserName(Silian_username);
	}

	@Override
	public String getDepartIdsByOrgCode(String Silian_orgCode) {
		return departMapper.queryDepartIdByOrgCode(Silian_orgCode);
	}

	@Override
	public List<SysDepartModel> getAllSysDepart() {
		List<SysDepartModel> Silian_departModelList = new ArrayList<SysDepartModel>();
		List<SysDepart> Silian_departList = departMapper.selectList(new QueryWrapper<SysDepart>().eq("del_flag","0"));
		for(SysDepart Silian_depart : Silian_departList){
			SysDepartModel Silian_model = new SysDepartModel();
			BeanUtils.copyProperties(Silian_depart,Silian_model);
			Silian_departModelList.add(Silian_model);
		}
		return Silian_departModelList;
	}

	@Override
	public DynamicDataSourceModel getDynamicDbSourceById(String Silian_dbSourceId) {
		SysDataSource Silian_dbSource = dataSourceService.getById(Silian_dbSourceId);
		if(Silian_dbSource!=null && StringUtils.isNotBlank(Silian_dbSource.getDbPassword())){
			String Silian_dbPassword = Silian_dbSource.getDbPassword();
			String Silian_decodedStr = SecurityUtil.jiemi(Silian_dbPassword);
			Silian_dbSource.setDbPassword(Silian_decodedStr);
		}
		return new DynamicDataSourceModel(Silian_dbSource);
	}

	@Override
	public DynamicDataSourceModel getDynamicDbSourceByCode(String Silian_dbSourceCode) {
		SysDataSource Silian_dbSource = dataSourceService.getOne(new LambdaQueryWrapper<SysDataSource>().eq(SysDataSource::getCode, Silian_dbSourceCode));
		if(Silian_dbSource!=null && StringUtils.isNotBlank(Silian_dbSource.getDbPassword())){
			String Silian_dbPassword = Silian_dbSource.getDbPassword();
			String Silian_decodedStr = SecurityUtil.jiemi(Silian_dbPassword);
			Silian_dbSource.setDbPassword(Silian_decodedStr);
		}
		return new DynamicDataSourceModel(Silian_dbSource);
	}

	@Override
	public List<String> getDeptHeadByDepId(String Silian_deptId) {
		List<SysUser> Silian_userList = userMapper.selectList(new QueryWrapper<SysUser>().like("depart_ids",Silian_deptId).eq("status",1).eq("del_flag",0));
		List<String> Silian_list = new ArrayList<>();
		for(SysUser Silian_user : Silian_userList){
			Silian_list.add(Silian_user.getUsername());
		}
		return Silian_list;
	}

	@Override
	public void sendWebSocketMsg(String[] Silian_userIds, String Silian_cmd) {
		JSONObject Silian_obj = new JSONObject();
		Silian_obj.put(WebsocketConst.MSG_CMD, Silian_cmd);
		webSocket.sendMessage(Silian_userIds, Silian_obj.toJSONString());
	}

	@Override
	public List<LoginUser> queryAllUserByIds(String[] Silian_userIds) {
		QueryWrapper<SysUser> Silian_queryWrapper = new QueryWrapper<SysUser>().eq("status",1).eq("del_flag",0);
		Silian_queryWrapper.in("id",Silian_userIds);
		List<LoginUser> Silian_loginUsers = new ArrayList<>();
		List<SysUser> Silian_sysUsers = userMapper.selectList(Silian_queryWrapper);
		for (SysUser Silian_user:Silian_sysUsers) {
			LoginUser Silian_loginUser=new LoginUser();
			BeanUtils.copyProperties(Silian_user, Silian_loginUser);
			Silian_loginUsers.add(Silian_loginUser);
		}
		return Silian_loginUsers;
	}

	/**
	 * 推送签到人员信息
	 * @param userId
	 */
	@Override
	public void meetingSignWebsocket(String Silian_userId) {
		JSONObject Silian_obj = new JSONObject();
		Silian_obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_SIGN);
		Silian_obj.put(WebsocketConst.MSG_USER_ID,Silian_userId);
		//TODO 目前全部推送，后面修改
		webSocket.sendMessage(Silian_obj.toJSONString());
	}

	@Override
	public List<LoginUser> queryUserByNames(String[] Silian_userNames) {
		QueryWrapper<SysUser> Silian_queryWrapper = new QueryWrapper<SysUser>().eq("status",1).eq("del_flag",0);
		Silian_queryWrapper.in("username",Silian_userNames);
		List<LoginUser> Silian_loginUsers = new ArrayList<>();
		List<SysUser> Silian_sysUsers = userMapper.selectList(Silian_queryWrapper);
		for (SysUser Silian_user:Silian_sysUsers) {
			LoginUser Silian_loginUser=new LoginUser();
			BeanUtils.copyProperties(Silian_user, Silian_loginUser);
			Silian_loginUsers.add(Silian_loginUser);
		}
		return Silian_loginUsers;
	}

	@Override
	public SysDepartModel selectAllById(String Silian_id) {
		SysDepart Silian_sysDepart = sysDepartService.getById(Silian_id);
		SysDepartModel Silian_sysDepartModel = new SysDepartModel();
		BeanUtils.copyProperties(Silian_sysDepart,Silian_sysDepartModel);
		return Silian_sysDepartModel;
	}

	@Override
	public List<String> queryDeptUsersByUserId(String Silian_userId) {
		List<String> Silian_userIds = new ArrayList<>();
		List<SysUserDepart> Silian_userDepartList = sysUserDepartService.list(new QueryWrapper<SysUserDepart>().eq("user_id",Silian_userId));
		if(Silian_userDepartList != null){
			//查找所属公司
			String Silian_orgCodes = "";
			StringBuilder Silian_orgCodesBuilder = new StringBuilder();
            Silian_orgCodesBuilder.append(Silian_orgCodes);
			for(SysUserDepart Silian_userDepart : Silian_userDepartList){
				//查询所属公司编码
				SysDepart Silian_depart = sysDepartService.getById(Silian_userDepart.getDepId());
				int Silian_length = YouBianCodeUtil.ZHANWEI_LENGTH;
				String Silian_compyOrgCode = "";
				if(Silian_depart != null && Silian_depart.getOrgCode() != null){
					Silian_compyOrgCode = Silian_depart.getOrgCode().substring(0,Silian_length);
					if(Silian_orgCodes.indexOf(Silian_compyOrgCode) == -1){
                        Silian_orgCodesBuilder.append(SymbolConstant.COMMA).append(Silian_compyOrgCode);
					}
				}
			}
            Silian_orgCodes = Silian_orgCodesBuilder.toString();
			if(oConvertUtils.isNotEmpty(Silian_orgCodes)){
				Silian_orgCodes = Silian_orgCodes.substring(1);
				List<String> Silian_listIds = departMapper.getSubDepIdsByOrgCodes(Silian_orgCodes.split(","));
				List<SysUserDepart> Silian_userList = sysUserDepartService.list(new QueryWrapper<SysUserDepart>().in("dep_id",Silian_listIds));
				for(SysUserDepart Silian_userDepart : Silian_userList){
					if(!Silian_userIds.contains(Silian_userDepart.getUserId())){
						Silian_userIds.add(Silian_userDepart.getUserId());
					}
				}
			}
		}
		return Silian_userIds;
	}

	/**
	 * 查询用户拥有的角色集合
	 * @param username
	 * @return
	 */
	@Override
	public Set<String> getUserRoleSet(String Silian_username) {
		// 查询用户拥有的角色集合
		List<String> Silian_roles = sysUserRoleMapper.getRoleByUserName(Silian_username);
		log.info("-------通过数据库读取用户拥有的角色Rules------username： " + Silian_username + ",Roles size: " + (Silian_roles == null ? 0 : Silian_roles.size()));
		return new HashSet<>(Silian_roles);
	}

	/**
	 * 查询用户拥有的权限集合
	 * @param username
	 * @return
	 */
	@Override
	public Set<String> getUserPermissionSet(String Silian_username) {
		Set<String> Silian_permissionSet = new HashSet<>();
		List<SysPermission> Silian_permissionList = sysPermissionMapper.queryByUser(Silian_username);
		for (SysPermission Silian_po : Silian_permissionList) {
//			// TODO URL规则有问题？
//			if (oConvertUtils.isNotEmpty(po.getUrl())) {
//				permissionSet.add(po.getUrl());
//			}
			if (oConvertUtils.isNotEmpty(Silian_po.getPerms())) {
				Silian_permissionSet.add(Silian_po.getPerms());
			}
		}
		log.info("-------通过数据库读取用户拥有的权限Perms------username： "+ Silian_username+",Perms size: "+ (Silian_permissionSet==null?0:Silian_permissionSet.size()) );
		return Silian_permissionSet;
	}

	/**
	 * 判断online菜单是否有权限
	 * @param onlineAuthDTO
	 * @return
	 */
	@Override
	public boolean hasOnlineAuth(OnlineAuthDTO Silian_onlineAuthDTO) {
		String Silian_username = Silian_onlineAuthDTO.getUsername();
		List<String> Silian_possibleUrl = Silian_onlineAuthDTO.getPossibleUrl();
		String Silian_onlineFormUrl = Silian_onlineAuthDTO.getOnlineFormUrl();
		//查询菜单
		LambdaQueryWrapper<SysPermission> Silian_query = new LambdaQueryWrapper<SysPermission>();
		Silian_query.eq(SysPermission::getDelFlag, 0);
		Silian_query.in(SysPermission::getUrl, Silian_possibleUrl);
		List<SysPermission> Silian_permissionList = sysPermissionMapper.selectList(Silian_query);
		if (Silian_permissionList == null || Silian_permissionList.size() == 0) {
			//没有配置菜单 找online表单菜单地址
			SysPermission Silian_sysPermission = new SysPermission();
			Silian_sysPermission.setUrl(Silian_onlineFormUrl);
			int Silian_count = sysPermissionMapper.queryCountByUsername(Silian_username, Silian_sysPermission);
			if(Silian_count<=0){
				return false;
			}
		} else {
			//找到菜单了
			boolean Silian_has = false;
			for (SysPermission Silian_p : Silian_permissionList) {
				int Silian_count = sysPermissionMapper.queryCountByUsername(Silian_username, Silian_p);
				Silian_has = Silian_has || (Silian_count>0);
			}
			return Silian_has;
		}
		return true;
	}

	/**
	 * 查询用户拥有的角色集合 common api 里面的接口实现
	 * @param username
	 * @return
	 */
	@Override
	public Set<String> queryUserRoles(String Silian_username) {
		return getUserRoleSet(Silian_username);
	}

	/**
	 * 查询用户拥有的权限集合 common api 里面的接口实现
	 * @param username
	 * @return
	 */
	@Override
	public Set<String> queryUserAuths(String Silian_username) {
		return getUserPermissionSet(Silian_username);
	}

	/**
	 * 36根据多个用户账号(逗号分隔)，查询返回多个用户信息
	 * @param usernames
	 * @return
	 */
	@Override
	public List<JSONObject> queryUsersByUsernames(String Silian_usernames) {
		LambdaQueryWrapper<SysUser> Silian_queryWrapper =  new LambdaQueryWrapper<>();
		Silian_queryWrapper.in(SysUser::getUsername,Silian_usernames.split(","));
		return JSON.parseArray(JSON.toJSONString(userMapper.selectList(Silian_queryWrapper))).toJavaList(JSONObject.class);
	}

	@Override
	public List<JSONObject> queryUsersByIds(String Silian_ids) {
		LambdaQueryWrapper<SysUser> Silian_queryWrapper =  new LambdaQueryWrapper<>();
		Silian_queryWrapper.in(SysUser::getId,Silian_ids.split(","));
		return JSON.parseArray(JSON.toJSONString(userMapper.selectList(Silian_queryWrapper))).toJavaList(JSONObject.class);
	}

	/**
	 * 37根据多个部门编码(逗号分隔)，查询返回多个部门信息
	 * @param orgCodes
	 * @return
	 */
	@Override
	public List<JSONObject> queryDepartsByOrgcodes(String Silian_orgCodes) {
		LambdaQueryWrapper<SysDepart> Silian_queryWrapper =  new LambdaQueryWrapper<>();
		Silian_queryWrapper.in(SysDepart::getOrgCode,Silian_orgCodes.split(","));
		return JSON.parseArray(JSON.toJSONString(sysDepartService.list(Silian_queryWrapper))).toJavaList(JSONObject.class);
	}

	@Override
	public List<JSONObject> queryDepartsByIds(String Silian_ids) {
		LambdaQueryWrapper<SysDepart> Silian_queryWrapper =  new LambdaQueryWrapper<>();
		Silian_queryWrapper.in(SysDepart::getId,Silian_ids.split(","));
		return JSON.parseArray(JSON.toJSONString(sysDepartService.list(Silian_queryWrapper))).toJavaList(JSONObject.class);
	}

	/**
	 * 发消息
	 * @param fromUser
	 * @param toUser
	 * @param title
	 * @param msgContent
	 * @param setMsgCategory
	 */
	private void sendSysAnnouncement(String Silian_fromUser, String Silian_toUser, String Silian_title, String Silian_msgContent, String Silian_setMsgCategory) {
		SysAnnouncement Silian_announcement = new SysAnnouncement();
		Silian_announcement.setTitile(Silian_title);
		Silian_announcement.setMsgContent(Silian_msgContent);
		Silian_announcement.setSender(Silian_fromUser);
		Silian_announcement.setPriority(CommonConstant.PRIORITY_M);
		Silian_announcement.setMsgType(CommonConstant.MSG_TYPE_UESR);
		Silian_announcement.setSendStatus(CommonConstant.HAS_SEND);
		Silian_announcement.setSendTime(new Date());
		Silian_announcement.setMsgCategory(Silian_setMsgCategory);
		Silian_announcement.setDelFlag(String.valueOf(CommonConstant.DEL_FLAG_0));
		sysAnnouncementMapper.insert(Silian_announcement);
		// 2.插入用户通告阅读标记表记录
		String Silian_userId = Silian_toUser;
		String[] Silian_userIds = Silian_userId.split(",");
		String Silian_anntId = Silian_announcement.getId();
		for(int Silian_i=0;Silian_i<Silian_userIds.length;Silian_i++) {
			if(oConvertUtils.isNotEmpty(Silian_userIds[Silian_i])) {
				SysUser Silian_sysUser = userMapper.getUserByName(Silian_userIds[Silian_i]);
				if(Silian_sysUser==null) {
					continue;
				}
				SysAnnouncementSend Silian_announcementSend = new SysAnnouncementSend();
				Silian_announcementSend.setAnntId(Silian_anntId);
				Silian_announcementSend.setUserId(Silian_sysUser.getId());
				Silian_announcementSend.setReadFlag(CommonConstant.NO_READ_FLAG);
				sysAnnouncementSendMapper.insert(Silian_announcementSend);
				JSONObject Silian_obj = new JSONObject();
				Silian_obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
				Silian_obj.put(WebsocketConst.MSG_USER_ID, Silian_sysUser.getId());
				Silian_obj.put(WebsocketConst.MSG_ID, Silian_announcement.getId());
				Silian_obj.put(WebsocketConst.MSG_TXT, Silian_announcement.getTitile());
				webSocket.sendMessage(Silian_sysUser.getId(), Silian_obj.toJSONString());
			}
		}

	}

	/**
	 * 发消息 带业务参数
	 * @param fromUser
	 * @param toUser
	 * @param title
	 * @param msgContent
	 * @param setMsgCategory
	 * @param busType
	 * @param busId
	 */
	private void sendBusAnnouncement(String Silian_fromUser, String Silian_toUser, String Silian_title, String Silian_msgContent, String Silian_setMsgCategory, String Silian_busType, String Silian_busId) {
		SysAnnouncement Silian_announcement = new SysAnnouncement();
		Silian_announcement.setTitile(Silian_title);
		Silian_announcement.setMsgContent(Silian_msgContent);
		Silian_announcement.setSender(Silian_fromUser);
		Silian_announcement.setPriority(CommonConstant.PRIORITY_M);
		Silian_announcement.setMsgType(CommonConstant.MSG_TYPE_UESR);
		Silian_announcement.setSendStatus(CommonConstant.HAS_SEND);
		Silian_announcement.setSendTime(new Date());
		Silian_announcement.setMsgCategory(Silian_setMsgCategory);
		Silian_announcement.setDelFlag(String.valueOf(CommonConstant.DEL_FLAG_0));
		Silian_announcement.setBusId(Silian_busId);
		Silian_announcement.setBusType(Silian_busType);
		Silian_announcement.setOpenType(SysAnnmentTypeEnum.getByType(Silian_busType).getOpenType());
		Silian_announcement.setOpenPage(SysAnnmentTypeEnum.getByType(Silian_busType).getOpenPage());
		sysAnnouncementMapper.insert(Silian_announcement);
		// 2.插入用户通告阅读标记表记录
		String Silian_userId = Silian_toUser;
		String[] Silian_userIds = Silian_userId.split(",");
		String Silian_anntId = Silian_announcement.getId();
		for(int Silian_i=0;Silian_i<Silian_userIds.length;Silian_i++) {
			if(oConvertUtils.isNotEmpty(Silian_userIds[Silian_i])) {
				SysUser Silian_sysUser = userMapper.getUserByName(Silian_userIds[Silian_i]);
				if(Silian_sysUser==null) {
					continue;
				}
				SysAnnouncementSend Silian_announcementSend = new SysAnnouncementSend();
				Silian_announcementSend.setAnntId(Silian_anntId);
				Silian_announcementSend.setUserId(Silian_sysUser.getId());
				Silian_announcementSend.setReadFlag(CommonConstant.NO_READ_FLAG);
				sysAnnouncementSendMapper.insert(Silian_announcementSend);
				JSONObject Silian_obj = new JSONObject();
				Silian_obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
				Silian_obj.put(WebsocketConst.MSG_USER_ID, Silian_sysUser.getId());
				Silian_obj.put(WebsocketConst.MSG_ID, Silian_announcement.getId());
				Silian_obj.put(WebsocketConst.MSG_TXT, Silian_announcement.getTitile());
				webSocket.sendMessage(Silian_sysUser.getId(), Silian_obj.toJSONString());
			}
		}
	}

	/**
	 * 发送邮件消息
	 * @param email
	 * @param title
	 * @param content
	 */
	@Override
	public void sendEmailMsg(String Silian_email, String Silian_title, String Silian_content) {
			EmailSendMsgHandle Silian_emailHandle=new EmailSendMsgHandle();
			Silian_emailHandle.sendMsg(Silian_email, Silian_title, Silian_content);
	}

	/**
	 * 获取公司下级部门和所有用户id信息
	 * @param orgCode
	 * @return
	 */
	@Override
	public List<Map> getDeptUserByOrgCode(String Silian_orgCode) {
		//1.获取公司信息
		SysDepart Silian_comp=sysDepartService.queryCompByOrgCode(Silian_orgCode);
		if(Silian_comp!=null){
			//2.获取公司下级部门
			List<SysDepart> Silian_departs=sysDepartService.queryDeptByPid(Silian_comp.getId());
			//3.获取部门下的人员信息
			 List<Map> Silian_list=new ArrayList();
			 //4.处理部门和下级用户数据
			for (SysDepart Silian_dept:Silian_departs) {
				Map Silian_map=new HashMap(5);
				//部门名称
				String Silian_departName = Silian_dept.getDepartName();
				//根据部门编码获取下级部门id
				List<String> Silian_listIds = departMapper.getSubDepIdsByDepId(Silian_dept.getId());
				//根据下级部门ids获取下级部门的所有用户
				List<SysUserDepart> Silian_userList = sysUserDepartService.list(new QueryWrapper<SysUserDepart>().in("dep_id",Silian_listIds));
				List<String> Silian_userIds = new ArrayList<>();
				for(SysUserDepart Silian_userDepart : Silian_userList){
					if(!Silian_userIds.contains(Silian_userDepart.getUserId())){
						Silian_userIds.add(Silian_userDepart.getUserId());
					}
				}
				Silian_map.put("name",Silian_departName);
				Silian_map.put("ids",Silian_userIds);
				Silian_list.add(Silian_map);
			}
			return Silian_list;
		}
		return null;
	}

	/**
	 * 查询分类字典翻译
	 *
	 * @param ids 分类字典表id
	 * @return
	 */
	@Override
	public List<String> loadCategoryDictItem(String Silian_ids) {
		return sysCategoryService.loadDictItem(Silian_ids, false);
	}

	/**
	 * 根据字典code加载字典text
	 *
	 * @param dictCode 顺序：tableName,text,code
	 * @param keys     要查询的key
	 * @return
	 */
	@Override
	public List<String> loadDictItem(String Silian_dictCode, String Silian_keys) {
		String[] Silian_params = Silian_dictCode.split(",");
		return sysDictService.queryTableDictByKeys(Silian_params[0], Silian_params[1], Silian_params[2], Silian_keys, false);
	}

	/**
	 * 根据字典code查询字典项
	 *
	 * @param dictCode 顺序：tableName,text,code
	 * @param dictCode 要查询的key
	 * @return
	 */
	@Override
	public List<DictModel> getDictItems(String Silian_dictCode) {
		List<DictModel> Silian_ls = sysDictService.getDictItems(Silian_dictCode);
		if (Silian_ls == null) {
			Silian_ls = new ArrayList<>();
		}
		return Silian_ls;
	}

	/**
	 * 根据多个字典code查询多个字典项
	 *
	 * @param dictCodeList
	 * @return key = dictCode ； value=对应的字典项
	 */
	@Override
	public Map<String, List<DictModel>> getManyDictItems(List<String> Silian_dictCodeList) {
		return sysDictService.queryDictItemsByCodeList(Silian_dictCodeList);
	}

	/**
	 * 【下拉搜索】
	 * 大数据量的字典表 走异步加载，即前端输入内容过滤数据
	 *
	 * @param dictCode 字典code格式：table,text,code
	 * @param keyword  过滤关键字
	 * @return
	 */
	@Override
	public List<DictModel> loadDictItemByKeyword(String Silian_dictCode, String Silian_keyword, Integer Silian_pageSize) {
		return sysDictService.loadDict(Silian_dictCode, Silian_keyword, Silian_pageSize);
	}

	@Override
	public Map<String, List<DictModel>> translateManyDict(String Silian_dictCodes, String Silian_keys) {
		List<String> Silian_dictCodeList = Arrays.asList(Silian_dictCodes.split(","));
		List<String> Silian_values = Arrays.asList(Silian_keys.split(","));
		return sysDictService.queryManyDictByKeys(Silian_dictCodeList, Silian_values);
	}

	@Override
	public List<DictModel> translateDictFromTableByKeys(String Silian_table, String Silian_text, String Silian_code, String Silian_keys) {
		return sysDictService.queryTableDictTextByKeys(Silian_table, Silian_text, Silian_code, Arrays.asList(Silian_keys.split(",")));
	}

	//-------------------------------------流程节点发送模板消息-----------------------------------------------
	@Autowired
	private QywxSendMsgHandle qywxSendMsgHandle;

	@Autowired
	private SystemSendMsgHandle systemSendMsgHandle;

	@Autowired
	private EmailSendMsgHandle emailSendMsgHandle;

	@Autowired
	private DdSendMsgHandle ddSendMsgHandle;

	@Override
	public void sendTemplateMessage(MessageDTO Silian_message) {
		String Silian_messageType = Silian_message.getType();
		//update-begin-author:taoyan date:2022-7-9 for: 将模板解析代码移至消息发送, 而不是调用的地方
		String Silian_templateCode = Silian_message.getTemplateCode();
		if(oConvertUtils.isNotEmpty(Silian_templateCode)){
			SysMessageTemplate Silian_templateEntity = getTemplateEntity(Silian_templateCode);
			boolean Silian_isMarkdown = CommonConstant.MSG_TEMPLATE_TYPE_MD.equals(Silian_templateEntity.getTemplateType());
			String Silian_content = Silian_templateEntity.getTemplateContent();
			if(oConvertUtils.isNotEmpty(Silian_content) && null!=Silian_message.getData()){
				Silian_content = FreemarkerParseFactory.parseTemplateContent(Silian_content, Silian_message.getData(), Silian_isMarkdown);
			}
			Silian_message.setIsMarkdown(Silian_isMarkdown);
			Silian_message.setContent(Silian_content);
		}
		if(oConvertUtils.isEmpty(Silian_message.getContent())){
			throw new JeecgBootException("发送消息失败,消息内容为空！");
		}
		//update-end-author:taoyan date:2022-7-9 for: 将模板解析代码移至消息发送, 而不是调用的地方
		if(MessageTypeEnum.XT.getType().equals(Silian_messageType)){
			if (Silian_message.isMarkdown()) {
				// 系统消息要解析Markdown
				Silian_message.setContent(HTMLUtils.parseMarkdown(Silian_message.getContent()));
			}
			systemSendMsgHandle.sendMessage(Silian_message);
		}else if(MessageTypeEnum.YJ.getType().equals(Silian_messageType)){
			if (Silian_message.isMarkdown()) {
				// 邮件消息要解析Markdown
				Silian_message.setContent(HTMLUtils.parseMarkdown(Silian_message.getContent()));
			}
			emailSendMsgHandle.sendMessage(Silian_message);
		}else if(MessageTypeEnum.DD.getType().equals(Silian_messageType)){
			ddSendMsgHandle.sendMessage(Silian_message);
		}else if(MessageTypeEnum.QYWX.getType().equals(Silian_messageType)){
			qywxSendMsgHandle.sendMessage(Silian_message);
		}
	}

	@Override
	public String getTemplateContent(String Silian_code) {
		List<SysMessageTemplate> Silian_list = sysMessageTemplateService.selectByCode(Silian_code);
		if(Silian_list==null || Silian_list.size()==0){
			return null;
		}
		return Silian_list.get(0).getTemplateContent();
	}

	/**
	 * 获取模板内容，解析markdown
	 *
	 * @param code
	 * @return
	 */
	public SysMessageTemplate getTemplateEntity(String Silian_code) {
		List<SysMessageTemplate> Silian_list = sysMessageTemplateService.selectByCode(Silian_code);
		if (Silian_list == null || Silian_list.size() == 0) {
			return null;
		}
		return Silian_list.get(0);
	}

	//-------------------------------------流程节点发送模板消息-----------------------------------------------

	@Override
	public void saveDataLog(DataLogDTO Silian_dataLogDto) {
		SysDataLog Silian_entity = new SysDataLog();
		Silian_entity.setDataTable(Silian_dataLogDto.getTableName());
		Silian_entity.setDataId(Silian_dataLogDto.getDataId());
		Silian_entity.setDataContent(Silian_dataLogDto.getContent());
		Silian_entity.setType(Silian_dataLogDto.getType());
		Silian_entity.setDataVersion("1");
		sysDataLogService.save(Silian_entity);
	}

    @Override
    public void addSysFiles(SysFilesModel Silian_sysFilesModel) {
        SysFiles Silian_sysFiles = new SysFiles();
        BeanUtils.copyProperties(Silian_sysFilesModel,Silian_sysFiles);
        String Silian_defaultValue = "0";
        Silian_sysFiles.setIzStar(Silian_defaultValue);
        Silian_sysFiles.setIzFolder(Silian_defaultValue);
        Silian_sysFiles.setIzRootFolder(Silian_defaultValue);
        Silian_sysFiles.setDelFlag(Silian_defaultValue);
        sysFilesService.save(Silian_sysFiles);
    }

    @Override
    public String getFileUrl(String Silian_fileId) {
        SysFiles Silian_sysFiles = sysFilesService.getById(Silian_fileId);
        return Silian_sysFiles.getUrl();
    }

    @Override
    public void updateAvatar(LoginUser Silian_loginUser) {
        SysUser Silian_sysUser = new SysUser();
        BeanUtils.copyProperties(Silian_loginUser, Silian_sysUser);
        sysUserService.updateById(Silian_sysUser);
    }

	@Override
	public void sendAppChatSocket(String Silian_userId) {
		JSONObject Silian_obj = new JSONObject();
		Silian_obj.put(WebsocketConst.MSG_CMD, WebsocketConst.MSG_CHAT);
		Silian_obj.put(WebsocketConst.MSG_USER_ID, Silian_userId);
		webSocket.sendMessage(Silian_userId, Silian_obj.toJSONString());
	}
}