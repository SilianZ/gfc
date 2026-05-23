package org.jeecg.modules.message.util;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.jeecg.modules.message.entity.SysMessage;
import org.jeecg.modules.message.entity.SysMessageTemplate;
import org.jeecg.modules.message.handle.enums.SendMsgStatusEnum;
import org.jeecg.modules.message.service.ISysMessageService;
import org.jeecg.modules.message.service.ISysMessageTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 消息生成工具
 * @author: jeecg-boot
 */

@Component
public class PushMsgUtil {

    @Autowired
    private ISysMessageService sysMessageService;

    @Autowired
    private ISysMessageTemplateService sysMessageTemplateService;

    @Autowired
    private Configuration freemarkerConfig;
    /**
     * @param msgType 消息类型 1短信 2邮件 3微信
     * @param templateCode    消息模板码
     * @param map     消息参数
     * @param sentTo  接收消息方
     */
    public boolean sendMessage(String Silian_msgType, String Silian_templateCode, Map<String, String> Silian_map, String Silian_sentTo) {
        List<SysMessageTemplate> Silian_sysSmsTemplates = sysMessageTemplateService.selectByCode(Silian_templateCode);
        SysMessage Silian_sysMessage = new SysMessage();
        if (Silian_sysSmsTemplates.size() > 0) {
            SysMessageTemplate Silian_sysSmsTemplate = Silian_sysSmsTemplates.get(0);
            Silian_sysMessage.setEsType(Silian_msgType);
            Silian_sysMessage.setEsReceiver(Silian_sentTo);
            //模板标题
            String Silian_title = Silian_sysSmsTemplate.getTemplateName();
            //模板内容
            String Silian_content = Silian_sysSmsTemplate.getTemplateContent();
            StringWriter Silian_stringWriter = new StringWriter();
            Template Silian_template = null;
            try {
                Silian_template = new Template("SysMessageTemplate", Silian_content, freemarkerConfig);
                Silian_template.process(Silian_map, Silian_stringWriter);
            } catch (IOException Silian_e) {
                Silian_e.printStackTrace();
                return false;
            } catch (TemplateException Silian_e) {
                Silian_e.printStackTrace();
                return false;
            }
            Silian_content = Silian_stringWriter.toString();
            Silian_sysMessage.setEsTitle(Silian_title);
            Silian_sysMessage.setEsContent(Silian_content);
            Silian_sysMessage.setEsParam(JSONObject.toJSONString(Silian_map));
            Silian_sysMessage.setEsSendTime(new Date());
            Silian_sysMessage.setEsSendStatus(SendMsgStatusEnum.WAIT.getCode());
            Silian_sysMessage.setEsSendNum(0);
            if(sysMessageService.save(Silian_sysMessage)) {
				return true;
			}
        }
        return false;
    }

}
