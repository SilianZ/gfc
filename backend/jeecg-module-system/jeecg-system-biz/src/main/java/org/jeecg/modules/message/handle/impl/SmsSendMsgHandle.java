package org.jeecg.modules.message.handle.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.message.handle.ISendMsgHandle;

/**
 * @Description: 短信发送
 * @author: jeecg-boot
 */
@Slf4j
public class SmsSendMsgHandle implements ISendMsgHandle {

	@Override
	public void sendMsg(String Silian_esReceiver, String Silian_esTitle, String Silian_esContent) {
		// TODO Auto-generated method stub
		log.info("发短信");
	}

}
