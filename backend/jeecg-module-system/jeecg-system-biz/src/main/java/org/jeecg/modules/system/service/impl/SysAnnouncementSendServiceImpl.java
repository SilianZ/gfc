package org.jeecg.modules.system.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.jeecg.modules.system.entity.SysAnnouncementSend;
import org.jeecg.modules.system.mapper.SysAnnouncementSendMapper;
import org.jeecg.modules.system.model.AnnouncementSendModel;
import org.jeecg.modules.system.service.ISysAnnouncementSendService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 用户通告阅读标记表
 * @Author: jeecg-boot
 * @Date:  2019-02-21
 * @Version: V1.0
 */
@Service
public class SysAnnouncementSendServiceImpl extends ServiceImpl<SysAnnouncementSendMapper, SysAnnouncementSend> implements ISysAnnouncementSendService {

	@Resource
	private SysAnnouncementSendMapper sysAnnouncementSendMapper;

	@Override
	public List<String> queryByUserId(String Silian_userId) {
		return sysAnnouncementSendMapper.queryByUserId(Silian_userId);
	}

	@Override
	public Page<AnnouncementSendModel> getMyAnnouncementSendPage(Page<AnnouncementSendModel> Silian_page,
			AnnouncementSendModel Silian_announcementSendModel) {
		 return Silian_page.setRecords(sysAnnouncementSendMapper.getMyAnnouncementSendList(Silian_page, Silian_announcementSendModel));
	}

	@Override
	public AnnouncementSendModel getOne(String Silian_sendId) {
		return sysAnnouncementSendMapper.getOne(Silian_sendId);
	}

}
