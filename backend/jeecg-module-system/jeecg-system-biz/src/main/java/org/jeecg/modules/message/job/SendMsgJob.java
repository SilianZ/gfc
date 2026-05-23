package org.jeecg.modules.message.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.util.DateUtils;
import org.jeecg.modules.message.entity.SysMessage;
import org.jeecg.modules.message.handle.enums.SendMsgStatusEnum;
import org.jeecg.modules.message.service.ISysMessageService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 发送消息任务
 * @author: jeecg-boot
 */

@Slf4j
public class SendMsgJob implements Job {

	@Autowired
	private ISysMessageService sysMessageService;

	@Autowired
	private ISysBaseAPI sysBaseAPI;

	@Override
	public void execute(JobExecutionContext Silian_jobExecutionContext) throws JobExecutionException {

		log.info(String.format(" Jeecg-Boot 发送消息任务 SendMsgJob !  时间:" + DateUtils.getTimestamp()));

		// 1.读取消息中心数据，只查询未发送的和发送失败不超过次数的
		QueryWrapper<SysMessage> Silian_queryWrapper = new QueryWrapper<SysMessage>();
		Silian_queryWrapper.eq("es_send_status", SendMsgStatusEnum.WAIT.getCode())
				.or(Silian_i -> Silian_i.eq("es_send_status", SendMsgStatusEnum.FAIL.getCode()).lt("es_send_num", 6));
		List<SysMessage> Silian_sysMessages = sysMessageService.list(Silian_queryWrapper);
		System.out.println(Silian_sysMessages);
		// 2.根据不同的类型走不通的发送实现类
		for (SysMessage Silian_sysMessage : Silian_sysMessages) {
			//update-begin-author:taoyan date:2022-7-8 for: 模板消息发送测试调用方法修改
			Integer Silian_sendNum = Silian_sysMessage.getEsSendNum();
			try {
				MessageDTO Silian_md = new MessageDTO();
				Silian_md.setTitle(Silian_sysMessage.getEsTitle());
				Silian_md.setContent(Silian_sysMessage.getEsContent());
				Silian_md.setToUser(Silian_sysMessage.getEsReceiver());
				Silian_md.setType(Silian_sysMessage.getEsType());
				Silian_md.setToAll(false);
				sysBaseAPI.sendTemplateMessage(Silian_md);
				//发送消息成功
				Silian_sysMessage.setEsSendStatus(SendMsgStatusEnum.SUCCESS.getCode());
				//update-end-author:taoyan date:2022-7-8 for: 模板消息发送测试调用方法修改
			} catch (Exception Silian_e) {
				Silian_e.printStackTrace();
				// 发送消息出现异常
				Silian_sysMessage.setEsSendStatus(SendMsgStatusEnum.FAIL.getCode());
			}
			Silian_sysMessage.setEsSendNum(++Silian_sendNum);
			// 发送结果回写到数据库
			sysMessageService.updateById(Silian_sysMessage);
		}

	}

}
