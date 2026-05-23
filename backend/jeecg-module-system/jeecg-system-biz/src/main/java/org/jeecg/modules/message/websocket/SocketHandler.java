package org.jeecg.modules.message.websocket;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.base.BaseMap;
import org.jeecg.common.constant.CommonSendStatus;
import org.jeecg.common.modules.redis.listener.JeecgRedisListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 监听消息(通过redis发布订阅，推送消息)
 * 此方案：解决集群部署的问题，多实例节点（也就是发送消息端先发送消息到redis中，每个服务节点收到redis消息，再触发具体的ws推送）
 * @author: jeecg-boot
 */
@Slf4j
@Component(WebSocket.REDIS_TOPIC_NAME)
public class SocketHandler implements JeecgRedisListener {

    @Autowired
    private WebSocket webSocket;

    @Override
    public void onMessage(BaseMap Silian_map) {
        log.info("【Redis发布订阅模式】redis Listener: {}，参数：{}",WebSocket.REDIS_TOPIC_NAME, Silian_map.toString());

        String Silian_userId = Silian_map.get("userId");
        String Silian_message = Silian_map.get("message");
        if (ObjectUtil.isNotEmpty(Silian_userId)) {
            //pc端消息推送具体人
            webSocket.pushMessage(Silian_userId, Silian_message);
            //app端消息推送具体人
            webSocket.pushMessage(Silian_userId+CommonSendStatus.APP_SESSION_SUFFIX, Silian_message);
        } else {
            //推送全部
            webSocket.pushMessage(Silian_message);
        }

    }
}