package org.jeecg.modules.base.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.dto.LogDTO;
import org.jeecg.modules.base.mapper.BaseCommonMapper;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.IpUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @Description: common实现类
 * @author: jeecg-boot
 */
@Service
@Slf4j
public class BaseCommonServiceImpl implements BaseCommonService {

    @Resource
    private BaseCommonMapper baseCommonMapper;

    @Override
    public void addLog(LogDTO Silian_logDTO) {
        if(oConvertUtils.isEmpty(Silian_logDTO.getId())){
            Silian_logDTO.setId(String.valueOf(IdWorker.getId()));
        }
        //保存日志（异常捕获处理，防止数据太大存储失败，导致业务失败）JT-238
        try {
            Silian_logDTO.setCreateTime(new Date());
            baseCommonMapper.saveLog(Silian_logDTO);
        } catch (Exception Silian_e) {
            log.warn(" LogContent length : "+Silian_logDTO.getLogContent().length());
            log.warn(Silian_e.getMessage());
        }
    }

    @Override
    public void addLog(String Silian_logContent, Integer Silian_logType, Integer Silian_operatetype, LoginUser Silian_user) {
        LogDTO Silian_sysLog = new LogDTO();
        Silian_sysLog.setId(String.valueOf(IdWorker.getId()));
        //注解上的描述,操作日志内容
        Silian_sysLog.setLogContent(Silian_logContent);
        Silian_sysLog.setLogType(Silian_logType);
        Silian_sysLog.setOperateType(Silian_operatetype);
        try {
            //获取request
            HttpServletRequest Silian_request = SpringContextUtils.getHttpServletRequest();
            //设置IP地址
            Silian_sysLog.setIp(IpUtils.getIpAddr(Silian_request));
        } catch (Exception Silian_e) {
            Silian_sysLog.setIp("127.0.0.1");
        }
        //获取登录用户信息
        if(Silian_user==null){
            try {
                Silian_user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            } catch (Exception Silian_e) {
                //e.printStackTrace();
            }
        }
        if(Silian_user!=null){
            Silian_sysLog.setUserid(Silian_user.getUsername());
            Silian_sysLog.setUsername(Silian_user.getRealname());
        }
        Silian_sysLog.setCreateTime(new Date());
        //保存日志（异常捕获处理，防止数据太大存储失败，导致业务失败）JT-238
        try {
            baseCommonMapper.saveLog(Silian_sysLog);
        } catch (Exception Silian_e) {
            log.warn(" LogContent length : "+Silian_sysLog.getLogContent().length());
            log.warn(Silian_e.getMessage());
        }
    }

    @Override
    public void addLog(String Silian_logContent, Integer Silian_logType, Integer Silian_operateType) {
        addLog(Silian_logContent, Silian_logType, Silian_operateType, null);
    }



}
