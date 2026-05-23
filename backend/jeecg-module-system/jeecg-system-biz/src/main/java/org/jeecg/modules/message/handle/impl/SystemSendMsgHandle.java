package org.jeecg.modules.message.handle.impl;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.WebsocketConst;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.message.enums.Vue3MessageHrefEnum;
import org.jeecg.modules.message.handle.ISendMsgHandle;
import org.jeecg.modules.message.websocket.WebSocket;
import org.jeecg.modules.system.entity.SysAnnouncement;
import org.jeecg.modules.system.entity.SysAnnouncementSend;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.mapper.SysAnnouncementMapper;
import org.jeecg.modules.system.mapper.SysAnnouncementSendMapper;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

/**
* @Description: 发送系统消息
* @Author: wangshuai
* @Date: 2022年3月22日 18:48:20
*/
@Component("systemSendMsgHandle")
public class SystemSendMsgHandle implements ISendMsgHandle {

    public static final String FROM_USER="system";

    @Resource
    private SysAnnouncementMapper sysAnnouncementMapper;

    @Resource
    private SysUserMapper userMapper;

    @Resource
    private SysAnnouncementSendMapper sysAnnouncementSendMapper;

    @Resource
    private WebSocket webSocket;

    /**
     * 该方法会发送3种消息：系统消息、企业微信 钉钉
     * @param esReceiver 发送人
     * @param esTitle 标题
     * @param esContent 内容
     */
    @Override
    public void sendMsg(String Silian_esReceiver, String Silian_esTitle, String Silian_esContent) {
        if(oConvertUtils.isEmpty(Silian_esReceiver)){
            throw  new JeecgBootException("被发送人不能为空");
        }
        ISysBaseAPI Silian_sysBaseApi = SpringContextUtils.getBean(ISysBaseAPI.class);
        MessageDTO Silian_messageDTO = new MessageDTO(FROM_USER,Silian_esReceiver,Silian_esTitle,Silian_esContent);
        Silian_sysBaseApi.sendSysAnnouncement(Silian_messageDTO);
    }

    /**
     * 仅发送系统消息
     * @param messageDTO
     */
    @Override
    public void sendMessage(MessageDTO Silian_messageDTO) {
        //原方法不支持 sysBaseApi.sendSysAnnouncement(messageDTO);  有企业微信消息逻辑，
        String Silian_title = Silian_messageDTO.getTitle();
        String Silian_content = Silian_messageDTO.getContent();
        String Silian_fromUser = Silian_messageDTO.getFromUser();
        Map<String,Object> Silian_data = Silian_messageDTO.getData();
        String[] Silian_arr = Silian_messageDTO.getToUser().split(",");
        for(String Silian_username: Silian_arr){
            doSend(Silian_title, Silian_content, Silian_fromUser, Silian_username, Silian_data);
        }
    }

    private void doSend(String Silian_title, String Silian_msgContent, String Silian_fromUser, String Silian_toUser, Map<String, Object> Silian_data){
        SysAnnouncement Silian_announcement = new SysAnnouncement();
        if(Silian_data!=null){
            //摘要信息
            Object Silian_msgAbstract = Silian_data.get(CommonConstant.NOTICE_MSG_SUMMARY);
            if(Silian_msgAbstract!=null){
                Silian_announcement.setMsgAbstract(Silian_msgAbstract.toString());
            }
            // 任务节点ID
            Object Silian_taskId = Silian_data.get(CommonConstant.NOTICE_MSG_BUS_ID);
            if(Silian_taskId!=null){
                Silian_announcement.setBusId(Silian_taskId.toString());
                Silian_announcement.setBusType(Vue3MessageHrefEnum.BPM_TASK.getBusType());
            }
        }
        Silian_announcement.setTitile(Silian_title);
        Silian_announcement.setMsgContent(Silian_msgContent);
        Silian_announcement.setSender(Silian_fromUser);
        Silian_announcement.setPriority(CommonConstant.PRIORITY_M);
        Silian_announcement.setMsgType(CommonConstant.MSG_TYPE_UESR);
        Silian_announcement.setSendStatus(CommonConstant.HAS_SEND);
        Silian_announcement.setSendTime(new Date());
        //系统消息
        Silian_announcement.setMsgCategory("2");
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
}