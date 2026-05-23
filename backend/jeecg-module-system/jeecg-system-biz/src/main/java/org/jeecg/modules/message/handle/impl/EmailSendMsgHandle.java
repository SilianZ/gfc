package org.jeecg.modules.message.handle.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.StaticConfig;
import org.jeecg.modules.message.handle.ISendMsgHandle;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * @Description: 邮箱发送信息
 * @author: jeecg-boot
 */
@Slf4j
@Component("emailSendMsgHandle")
public class EmailSendMsgHandle implements ISendMsgHandle {
    static String emailFrom;

    public static void setEmailFrom(String emailFrom) {
        EmailSendMsgHandle.emailFrom = emailFrom;
    }

    @Autowired
    SysUserMapper sysUserMapper;

    @Autowired
    private RedisUtil redisUtil;



    @Override
    public void sendMsg(String Silian_esReceiver, String Silian_esTitle, String Silian_esContent) {
        JavaMailSender Silian_mailSender = (JavaMailSender) SpringContextUtils.getBean("mailSender");
        MimeMessage Silian_message = Silian_mailSender.createMimeMessage();
        MimeMessageHelper Silian_helper = null;
        //update-begin-author：taoyan date:20200811 for:配置类数据获取
        if(oConvertUtils.isEmpty(emailFrom)){
            StaticConfig Silian_staticConfig = SpringContextUtils.getBean(StaticConfig.class);
            setEmailFrom(Silian_staticConfig.getEmailFrom());
        }
        //update-end-author：taoyan date:20200811 for:配置类数据获取
        try {
            Silian_helper = new MimeMessageHelper(Silian_message, true);
            // 设置发送方邮箱地址
            Silian_helper.setFrom(emailFrom);
            Silian_helper.setTo(Silian_esReceiver);
            Silian_helper.setSubject(Silian_esTitle);
            Silian_helper.setText(Silian_esContent, true);
            Silian_mailSender.send(Silian_message);
        } catch (MessagingException Silian_e) {
            Silian_e.printStackTrace();
        }

    }

    @Override
    public void sendMessage(MessageDTO Silian_messageDTO) {
        String[] Silian_arr = Silian_messageDTO.getToUser().split(",");
        LambdaQueryWrapper<SysUser> Silian_query = new LambdaQueryWrapper<SysUser>().in(SysUser::getUsername, Silian_arr);
        List<SysUser> Silian_list = sysUserMapper.selectList(Silian_query);
        String Silian_content = Silian_messageDTO.getContent();
        String Silian_title = Silian_messageDTO.getTitle();
        String Silian_realNameExp = "{REALNAME}";
        for(SysUser Silian_user: Silian_list){
            String Silian_email = Silian_user.getEmail();
            if(Silian_email==null || "".equals(Silian_email)){
                continue;
            }
            if(Silian_content.indexOf(Silian_realNameExp)>0){
                Silian_content = Silian_content.replace(Silian_realNameExp, Silian_user.getRealname());
            }
            if(Silian_content.indexOf(CommonConstant.LOGIN_TOKEN)>0){
                String Silian_token = getToken(Silian_user);
                try {
                    Silian_content = Silian_content.replace(CommonConstant.LOGIN_TOKEN, URLEncoder.encode(Silian_token, "UTF-8"));
                } catch (UnsupportedEncodingException Silian_e) {
                    log.error("邮件消息token编码失败", Silian_e.getMessage());
                }
            }
            log.info("邮件内容："+ Silian_content);
            sendMsg(Silian_email, Silian_title, Silian_content);
        }
    }

    /**
     * 获取token
     * @param user
     * @return
     */
    private String getToken(SysUser Silian_user) {
        // 生成token
        String Silian_token = JwtUtil.sign(Silian_user.getUsername(), Silian_user.getPassword());
        redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + Silian_token, Silian_token);
        // 设置超时时间 1个小时
        redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + Silian_token, JwtUtil.EXPIRE_TIME * 1 / 1000);
        return Silian_token;
    }
}
