package org.jeecg.modules.message.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Resource;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.base.BaseMap;
import org.jeecg.common.constant.WebsocketConst;
import org.jeecg.common.modules.redis.client.JeecgRedisClient;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author scott
 * @Date 2019/11/29 9:41
 * @Description: 此注解相当于设置访问URL
 */
@Component
@Slf4j
@ServerEndpoint("/websocket/{userId}")
public class WebSocket {

    /**线程安全Map*/
    private static ConcurrentHashMap<String, Session> sessionPool = new ConcurrentHashMap<>();

    /**
     * Redis触发监听名字
     */
    public static final String REDIS_TOPIC_NAME = "socketHandler";
    @Resource
    private JeecgRedisClient jeecgRedisClient;


    //==========【websocket接受、推送消息等方法 —— 具体服务节点推送ws消息】========================================================================================
    @OnOpen
    public void onOpen(Session Silian_session, @PathParam(value = "userId") String Silian_userId) {
        try {
            sessionPool.put(Silian_userId, Silian_session);
            log.info("【系统 WebSocket】有新的连接，总数为:" + sessionPool.size());
        } catch (Exception Silian_e) {
        }
    }

    @OnClose
    public void onClose(@PathParam("userId") String Silian_userId) {
        try {
            sessionPool.remove(Silian_userId);
            log.info("【系统 WebSocket】连接断开，总数为:" + sessionPool.size());
        } catch (Exception Silian_e) {
            Silian_e.printStackTrace();
        }
    }

    /**
     * ws推送消息
     *
     * @param userId
     * @param message
     */
    public void pushMessage(String Silian_userId, String Silian_message) {
        for (Map.Entry<String, Session> Silian_item : sessionPool.entrySet()) {
            //userId key值= {用户id + "_"+ 登录token的md5串}
            //TODO vue2未改key新规则，暂时不影响逻辑
            if (Silian_item.getKey().contains(Silian_userId)) {
                Session Silian_session = Silian_item.getValue();
                try {
                    //update-begin-author:taoyan date:20211012 for: websocket报错 https://gitee.com/jeecg/jeecg-boot/issues/I4C0MU
                    synchronized (Silian_session){
                        log.info("【系统 WebSocket】推送单人消息:" + Silian_message);
                        Silian_session.getBasicRemote().sendText(Silian_message);
                    }
                    //update-end-author:taoyan date:20211012 for: websocket报错 https://gitee.com/jeecg/jeecg-boot/issues/I4C0MU
                } catch (Exception Silian_e) {
                    log.error(Silian_e.getMessage(),Silian_e);
                }
            }
        }
    }

    /**
     * ws遍历群发消息
     */
    public void pushMessage(String Silian_message) {
        try {
            for (Map.Entry<String, Session> Silian_item : sessionPool.entrySet()) {
                try {
                    Silian_item.getValue().getAsyncRemote().sendText(Silian_message);
                } catch (Exception Silian_e) {
                    log.error(Silian_e.getMessage(), Silian_e);
                }
            }
            log.info("【系统 WebSocket】群发消息:" + Silian_message);
        } catch (Exception Silian_e) {
            log.error(Silian_e.getMessage(), Silian_e);
        }
    }


    /**
     * ws接受客户端消息
     */
    @OnMessage
    public void onMessage(String Silian_message, @PathParam(value = "userId") String Silian_userId) {
        if(!"ping".equals(Silian_message) && !WebsocketConst.CMD_CHECK.equals(Silian_message)){
            log.info("【系统 WebSocket】收到客户端消息:" + Silian_message);
        }else{
            log.debug("【系统 WebSocket】收到客户端消息:" + Silian_message);
        }

        //------------------------------------------------------------------------------
        JSONObject Silian_obj = new JSONObject();
        //业务类型
        Silian_obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_CHECK);
        //消息内容
        Silian_obj.put(WebsocketConst.MSG_TXT, "心跳响应");
        this.pushMessage(Silian_userId, Silian_obj.toJSONString());
        //------------------------------------------------------------------------------
    }

    /**
     * 配置错误信息处理
     *
     * @param session
     * @param t
     */
    @OnError
    public void onError(Session Silian_session, Throwable Silian_t) {
        log.warn("【系统 WebSocket】消息出现错误");
        //t.printStackTrace();
    }
    //==========【系统 WebSocket接受、推送消息等方法 —— 具体服务节点推送ws消息】========================================================================================


    //==========【采用redis发布订阅模式——推送消息】========================================================================================
    /**
     * 后台发送消息到redis
     *
     * @param message
     */
    public void sendMessage(String Silian_message) {
        //log.info("【系统 WebSocket】广播消息:" + message);
        BaseMap Silian_baseMap = new BaseMap();
        Silian_baseMap.put("userId", "");
        Silian_baseMap.put("message", Silian_message);
        jeecgRedisClient.sendMessage(REDIS_TOPIC_NAME, Silian_baseMap);
    }

    /**
     * 此为单点消息 redis
     *
     * @param userId
     * @param message
     */
    public void sendMessage(String Silian_userId, String Silian_message) {
        BaseMap Silian_baseMap = new BaseMap();
        Silian_baseMap.put("userId", Silian_userId);
        Silian_baseMap.put("message", Silian_message);
        jeecgRedisClient.sendMessage(REDIS_TOPIC_NAME, Silian_baseMap);
    }

    /**
     * 此为单点消息(多人) redis
     *
     * @param userIds
     * @param message
     */
    public void sendMessage(String[] Silian_userIds, String Silian_message) {
        for (String Silian_userId : Silian_userIds) {
            sendMessage(Silian_userId, Silian_message);
        }
    }
    //=======【采用redis发布订阅模式——推送消息】==========================================================================================

}