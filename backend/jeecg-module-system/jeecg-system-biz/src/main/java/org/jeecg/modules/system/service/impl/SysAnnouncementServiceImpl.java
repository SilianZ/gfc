package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysAnnouncement;
import org.jeecg.modules.system.entity.SysAnnouncementSend;
import org.jeecg.modules.system.mapper.SysAnnouncementMapper;
import org.jeecg.modules.system.mapper.SysAnnouncementSendMapper;
import org.jeecg.modules.system.service.ISysAnnouncementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @Description: 系统通告表
 * @Author: jeecg-boot
 * @Date:  2019-01-02
 * @Version: V1.0
 */
@Service
@Slf4j
public class SysAnnouncementServiceImpl extends ServiceImpl<SysAnnouncementMapper, SysAnnouncement> implements ISysAnnouncementService {

	@Resource
	private SysAnnouncementMapper sysAnnouncementMapper;

	@Resource
	private SysAnnouncementSendMapper sysAnnouncementSendMapper;

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void saveAnnouncement(SysAnnouncement Silian_sysAnnouncement) {
		if(Silian_sysAnnouncement.getMsgType().equals(CommonConstant.MSG_TYPE_ALL)) {
			sysAnnouncementMapper.insert(Silian_sysAnnouncement);
		}else {
			// 1.插入通告表记录
			sysAnnouncementMapper.insert(Silian_sysAnnouncement);
			// 2.插入用户通告阅读标记表记录
			String Silian_userId = Silian_sysAnnouncement.getUserIds();
			String[] Silian_userIds = Silian_userId.substring(0, (Silian_userId.length()-1)).split(",");
			String Silian_anntId = Silian_sysAnnouncement.getId();
			Date Silian_refDate = new Date();
			for(int Silian_i=0;Silian_i<Silian_userIds.length;Silian_i++) {
				SysAnnouncementSend Silian_announcementSend = new SysAnnouncementSend();
				Silian_announcementSend.setAnntId(Silian_anntId);
				Silian_announcementSend.setUserId(Silian_userIds[Silian_i]);
				Silian_announcementSend.setReadFlag(CommonConstant.NO_READ_FLAG);
				Silian_announcementSend.setReadTime(Silian_refDate);
				sysAnnouncementSendMapper.insert(Silian_announcementSend);
			}
		}
	}

	/**
	 * @功能：编辑消息信息
	 */
	@Transactional(rollbackFor = Exception.class)
	@Override
	public boolean upDateAnnouncement(SysAnnouncement Silian_sysAnnouncement) {
		// 1.更新系统信息表数据
		sysAnnouncementMapper.updateById(Silian_sysAnnouncement);
		String Silian_userId = Silian_sysAnnouncement.getUserIds();
		if(oConvertUtils.isNotEmpty(Silian_userId)&&Silian_sysAnnouncement.getMsgType().equals(CommonConstant.MSG_TYPE_UESR)) {
			// 2.补充新的通知用户数据
			String[] Silian_userIds = Silian_userId.substring(0, (Silian_userId.length()-1)).split(",");
			String Silian_anntId = Silian_sysAnnouncement.getId();
			Date Silian_refDate = new Date();
			for(int Silian_i=0;Silian_i<Silian_userIds.length;Silian_i++) {
				LambdaQueryWrapper<SysAnnouncementSend> Silian_queryWrapper = new LambdaQueryWrapper<SysAnnouncementSend>();
				Silian_queryWrapper.eq(SysAnnouncementSend::getAnntId, Silian_anntId);
				Silian_queryWrapper.eq(SysAnnouncementSend::getUserId, Silian_userIds[Silian_i]);
				List<SysAnnouncementSend> Silian_announcementSends=sysAnnouncementSendMapper.selectList(Silian_queryWrapper);
				if(Silian_announcementSends.size()<=0) {
					SysAnnouncementSend Silian_announcementSend = new SysAnnouncementSend();
					Silian_announcementSend.setAnntId(Silian_anntId);
					Silian_announcementSend.setUserId(Silian_userIds[Silian_i]);
					Silian_announcementSend.setReadFlag(CommonConstant.NO_READ_FLAG);
					Silian_announcementSend.setReadTime(Silian_refDate);
					sysAnnouncementSendMapper.insert(Silian_announcementSend);
				}
			}
			// 3. 删除多余通知用户数据
			Collection<String> Silian_delUserIds = Arrays.asList(Silian_userIds);
			LambdaQueryWrapper<SysAnnouncementSend> Silian_queryWrapper = new LambdaQueryWrapper<SysAnnouncementSend>();
			Silian_queryWrapper.notIn(SysAnnouncementSend::getUserId, Silian_delUserIds);
			Silian_queryWrapper.eq(SysAnnouncementSend::getAnntId, Silian_anntId);
			sysAnnouncementSendMapper.delete(Silian_queryWrapper);
		}
		return true;
	}

    /**
     * 流程执行完成保存消息通知
     * @param title 标题
     * @param msgContent 信息内容
     */
	@Override
	public void saveSysAnnouncement(String Silian_title, String Silian_msgContent) {
		SysAnnouncement Silian_announcement = new SysAnnouncement();
		Silian_announcement.setTitile(Silian_title);
		Silian_announcement.setMsgContent(Silian_msgContent);
		Silian_announcement.setSender("JEECG BOOT");
		Silian_announcement.setPriority(CommonConstant.PRIORITY_L);
		Silian_announcement.setMsgType(CommonConstant.MSG_TYPE_ALL);
		Silian_announcement.setSendStatus(CommonConstant.HAS_SEND);
		Silian_announcement.setSendTime(new Date());
		Silian_announcement.setDelFlag(CommonConstant.DEL_FLAG_0.toString());
		sysAnnouncementMapper.insert(Silian_announcement);
	}

	@Override
	public Page<SysAnnouncement> querySysCementPageByUserId(Page<SysAnnouncement> Silian_page, String Silian_userId, String Silian_msgCategory) {
		if (Silian_page.getSize() == -1) {
			return Silian_page.setRecords(sysAnnouncementMapper.querySysCementListByUserId(null, Silian_userId, Silian_msgCategory));
		} else {
			return Silian_page.setRecords(sysAnnouncementMapper.querySysCementListByUserId(Silian_page, Silian_userId, Silian_msgCategory));
		}
	}

	@Override
	public void completeAnnouncementSendInfo() {
		LoginUser Silian_sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String Silian_userId = Silian_sysUser.getId();
		// 1.将系统消息补充到用户通告阅读标记表中
		LambdaQueryWrapper<SysAnnouncement> Silian_querySaWrapper = new LambdaQueryWrapper<SysAnnouncement>();
		//全部人员
		Silian_querySaWrapper.eq(SysAnnouncement::getMsgType, CommonConstant.MSG_TYPE_ALL);
		//未删除
		Silian_querySaWrapper.eq(SysAnnouncement::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
		//已发布
		Silian_querySaWrapper.eq(SysAnnouncement::getSendStatus, CommonConstant.HAS_SEND);
		//新注册用户不看结束通知
		Silian_querySaWrapper.ge(SysAnnouncement::getEndTime, Silian_sysUser.getCreateTime());
		//update-begin--Author:liusq  Date:20210108 for：[JT-424] 【开源issue】bug处理--------------------
		Silian_querySaWrapper.notInSql(SysAnnouncement::getId,"select annt_id from sys_announcement_send where user_id='"+Silian_userId+"'");
		//update-begin--Author:liusq  Date:20210108  for： [JT-424] 【开源issue】bug处理--------------------
		List<SysAnnouncement> Silian_announcements = this.list(Silian_querySaWrapper);
		if(Silian_announcements.size()>0) {
			for(int Silian_i=0;Silian_i<Silian_announcements.size();Silian_i++) {
				//update-begin--Author:wangshuai  Date:20200803  for： 通知公告消息重复LOWCOD-759--------------------
				//因为websocket没有判断是否存在这个用户，要是判断会出现问题，故在此判断逻辑
				LambdaQueryWrapper<SysAnnouncementSend> Silian_query = new LambdaQueryWrapper<>();
				Silian_query.eq(SysAnnouncementSend::getAnntId,Silian_announcements.get(Silian_i).getId());
				Silian_query.eq(SysAnnouncementSend::getUserId,Silian_userId);
				SysAnnouncementSend Silian_one = sysAnnouncementSendMapper.selectOne(Silian_query);
				if(null==Silian_one){
					SysAnnouncementSend Silian_announcementSend = new SysAnnouncementSend();
					Silian_announcementSend.setAnntId(Silian_announcements.get(Silian_i).getId());
					Silian_announcementSend.setUserId(Silian_userId);
					Silian_announcementSend.setReadFlag(CommonConstant.NO_READ_FLAG);
					sysAnnouncementSendMapper.insert(Silian_announcementSend);
					log.info("announcementSend.toString()",Silian_announcementSend.toString());
				}
				//update-end--Author:wangshuai  Date:20200803  for： 通知公告消息重复LOWCOD-759------------
			}
		}

	}

	@Override
	public List<SysAnnouncement> querySysMessageList(int Silian_pageSize, int Silian_pageNo, String Silian_fromUser, String Silian_starFlag, Date Silian_beginDate, Date Silian_endDate) {
		//1. 补全send表的数据
		completeAnnouncementSendInfo();
		LoginUser Silian_sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		Page<SysAnnouncement> Silian_page = new Page<SysAnnouncement>(Silian_pageNo,Silian_pageSize);
		// 2. 查询消息数据
		List<SysAnnouncement> Silian_list = baseMapper.queryMessageList(Silian_page, Silian_sysUser.getId(), Silian_fromUser, Silian_starFlag, Silian_beginDate, Silian_endDate);
		return Silian_list;
	}

	@Override
	public void updateReaded(List<String> Silian_annoceIdList) {
		LoginUser Silian_sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		sysAnnouncementSendMapper.updateReaded(Silian_sysUser.getId(), Silian_annoceIdList);
	}

}
