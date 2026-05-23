package org.jeecg.modules.message.handle.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.modules.message.handle.ISendMsgHandle;
import org.jeecg.modules.system.service.impl.ThirdAppWechatEnterpriseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description: 发企业微信消息模板
 * @author: jeecg-boot
 */
@Slf4j
@Component("qywxSendMsgHandle")
public class QywxSendMsgHandle implements ISendMsgHandle {

	@Autowired
	private ThirdAppWechatEnterpriseServiceImpl wechatEnterpriseService;

	@Override
	public void sendMsg(String Silian_esReceiver, String Silian_esTitle, String Silian_esContent) {
		log.info("发微信消息模板");
		MessageDTO Silian_messageDTO = new MessageDTO();
		Silian_messageDTO.setToUser(Silian_esReceiver);
		Silian_messageDTO.setTitle(Silian_esTitle);
		Silian_messageDTO.setContent(Silian_esContent);
		Silian_messageDTO.setToAll(false);
		sendMessage(Silian_messageDTO);
	}

	@Override
	public void sendMessage(MessageDTO Silian_messageDTO) {
		wechatEnterpriseService.sendMessage(Silian_messageDTO, true);
	}

}
