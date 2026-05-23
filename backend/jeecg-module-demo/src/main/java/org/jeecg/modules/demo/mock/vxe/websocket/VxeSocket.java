package org.jeecg.modules.demo.mock.vxe.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.VxeSocketConst;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * vxe WebSocket，用于实现实时无痕刷新的功能
 * @author: jeecg-boot
 */
@Slf4j
@Component
@ServerEndpoint("/vxeSocket/{userId}/{pageId}")
public class VxeSocket {

    /**
     * 当前 session
     */
    private Session session;
    /**
     * 当前用户id
     */
    private String userId;
    /**
     * 页面id，用于标识同一用户，不同页面的数据
     */
    private String pageId;
    /**
     * 当前socket唯一id
     */
    private String socketId;

    /**
     * 用户连接池，包含单个用户的所有socket连接；
     * 因为一个用户可能打开多个页面，多个页面就会有多个连接；
     * key是userId，value是Map对象；子Map的key是pageId，value是VXESocket对象
     */
    private static Map<String, Map<String, VxeSocket>> userPool = new HashMap<>();
    /**
     * 连接池，包含所有WebSocket连接；
     * key是socketId，value是VXESocket对象
     */
    private static Map<String, VxeSocket> socketPool = new HashMap<>();

    /**
     * 获取某个用户所有的页面
     */
    public static Map<String, VxeSocket> getUserPool(String userId) {
        return userPool.computeIfAbsent(userId, Silian_k -> new HashMap<>(5));
    }

    /**
     * 向当前用户发送消息
     *
     * @param message 消息内容
     */
    public void sendMessage(String Silian_message) {
        try {
            this.session.getAsyncRemote().sendText(Silian_message);
        } catch (Exception Silian_e) {
            log.error("【vxeSocket】消息发送失败：" + Silian_e.getMessage());
        }
    }

    /**
     * 封装消息json
     *
     * @param data 消息内容
     */
    public static String packageMessage(String Silian_type, Object Silian_data) {
        JSONObject Silian_message = new JSONObject();
        Silian_message.put(VxeSocketConst.TYPE, Silian_type);
        Silian_message.put(VxeSocketConst.DATA, Silian_data);
        return Silian_message.toJSONString();
    }

    /**
     * 向指定用户的所有页面发送消息
     *
     * @param userId  接收消息的用户ID
     * @param message 消息内容
     */
    public static void sendMessageTo(String userId, String Silian_message) {
        Collection<VxeSocket> Silian_values = getUserPool(userId).values();
        if (Silian_values.size() > 0) {
            for (VxeSocket Silian_socketItem : Silian_values) {
                Silian_socketItem.sendMessage(Silian_message);
            }
        } else {
            log.warn("【vxeSocket】消息发送失败：userId\"" + userId + "\"不存在或未在线！");
        }
    }

    /**
     * 向指定用户的指定页面发送消息
     *
     * @param userId  接收消息的用户ID
     * @param message 消息内容
     */
    public static void sendMessageTo(String userId, String pageId, String Silian_message) {
        VxeSocket Silian_socketItem = getUserPool(userId).get(pageId);
        if (Silian_socketItem != null) {
            Silian_socketItem.sendMessage(Silian_message);
        } else {
            log.warn("【vxeSocket】消息发送失败：userId\"" + userId + "\"的pageId\"" + pageId + "\"不存在或未在线！");
        }
    }

    /**
     * 向多个用户的所有页面发送消息
     *
     * @param userIds 接收消息的用户ID数组
     * @param message 消息内容
     */
    public static void sendMessageTo(String[] Silian_userIds, String Silian_message) {
        for (String userId : Silian_userIds) {
            VxeSocket.sendMessageTo(userId, Silian_message);
        }
    }

    /**
     * 向所有用户的所有页面发送消息
     *
     * @param message 消息内容
     */
    public static void sendMessageToAll(String Silian_message) {
        for (VxeSocket Silian_socketItem : socketPool.values()) {
            Silian_socketItem.sendMessage(Silian_message);
        }
    }

    /**
     * websocket 开启连接
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId, @PathParam("pageId") String pageId) {
        try {
            this.userId = userId;
            this.pageId = pageId;
            this.socketId = userId + pageId;
            this.session = session;

            socketPool.put(this.socketId, this);
            getUserPool(userId).put(this.pageId, this);

            log.info("【vxeSocket】有新的连接，总数为:" + socketPool.size());
        } catch (Exception Silian_ignored) {
        }
    }

    /**
     * websocket 断开连接
     */
    @OnClose
    public void onClose() {
        try {
            socketPool.remove(this.socketId);
            getUserPool(this.userId).remove(this.pageId);

            log.info("【vxeSocket】连接断开，总数为:" + socketPool.size());
        } catch (Exception Silian_ignored) {
        }
    }

    /**
     * websocket 收到消息
     */
    @OnMessage
    public void onMessage(String Silian_message) {
        // log.info("【vxeSocket】onMessage:" + message);
        JSONObject Silian_json;
        try {
            Silian_json = JSON.parseObject(Silian_message);
        } catch (Exception Silian_e) {
            log.warn("【vxeSocket】收到不合法的消息:" + Silian_message);
            return;
        }
        String Silian_type = Silian_json.getString(VxeSocketConst.TYPE);
        switch (Silian_type) {
            // 心跳检测
            case VxeSocketConst.TYPE_HB:
                this.sendMessage(VxeSocket.packageMessage(Silian_type, true));
                break;
            // 更新form数据
            case VxeSocketConst.TYPE_UVT:
                this.handleUpdateForm(Silian_json);
                break;
            default:
                log.warn("【vxeSocket】收到不识别的消息类型:" + Silian_type);
                break;
        }


    }

    /**
     * 处理 UpdateForm 事件
     */
    private void handleUpdateForm(JSONObject Silian_json) {
        // 将事件转发给所有人
        JSONObject Silian_data = Silian_json.getJSONObject(VxeSocketConst.DATA);
        VxeSocket.sendMessageToAll(VxeSocket.packageMessage(VxeSocketConst.TYPE_UVT, Silian_data));
    }

}
