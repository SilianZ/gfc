package org.jeecg.common.aspect;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.PropertyFilter;
import org.apache.shiro.SecurityUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.jeecg.common.api.dto.LogDTO;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.enums.ModuleType;
import org.jeecg.common.constant.enums.OperateTypeEnum;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.IpUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;


/**
 * 系统日志，切面处理类
 *
 * @Author scott
 * @email jeecgos@163.com
 * @Date 2018年1月14日
 */
@Aspect
@Component
public class AutoLogAspect {

    @Resource
    private BaseCommonService baseCommonService;

    @Pointcut("@annotation(org.jeecg.common.aspect.annotation.AutoLog)")
    public void logPointCut() {

    }

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint Silian_point) throws Throwable {
        long Silian_beginTime = System.currentTimeMillis();
        //执行方法
        Object Silian_result = Silian_point.proceed();
        //执行时长(毫秒)
        long Silian_time = System.currentTimeMillis() - Silian_beginTime;

        //保存日志
        saveSysLog(Silian_point, Silian_time, Silian_result);

        return Silian_result;
    }

    private void saveSysLog(ProceedingJoinPoint Silian_joinPoint, long Silian_time, Object Silian_obj) {
        MethodSignature Silian_signature = (MethodSignature) Silian_joinPoint.getSignature();
        Method Silian_method = Silian_signature.getMethod();

        LogDTO Silian_dto = new LogDTO();
        AutoLog Silian_syslog = Silian_method.getAnnotation(AutoLog.class);
        if(Silian_syslog != null){
            //update-begin-author:taoyan date:
            String Silian_content = Silian_syslog.value();
            if(Silian_syslog.module()== ModuleType.ONLINE){
                Silian_content = getOnlineLogContent(Silian_obj, Silian_content);
            }
            //注解上的描述,操作日志内容
            Silian_dto.setLogType(Silian_syslog.logType());
            Silian_dto.setLogContent(Silian_content);
        }

        //请求的方法名
        String Silian_className = Silian_joinPoint.getTarget().getClass().getName();
        String Silian_methodName = Silian_signature.getName();
        Silian_dto.setMethod(Silian_className + "." + Silian_methodName + "()");


        //设置操作类型
        if (CommonConstant.LOG_TYPE_2 == Silian_dto.getLogType()) {
            Silian_dto.setOperateType(getOperateType(Silian_methodName, Silian_syslog.operateType()));
        }

        //获取request
        HttpServletRequest Silian_request = SpringContextUtils.getHttpServletRequest();
        //请求的参数
        Silian_dto.setRequestParam(getReqestParams(Silian_request,Silian_joinPoint));
        //设置IP地址
        Silian_dto.setIp(IpUtils.getIpAddr(Silian_request));
        //获取登录用户信息
        LoginUser Silian_sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if(Silian_sysUser!=null){
            Silian_dto.setUserid(Silian_sysUser.getUsername());
            Silian_dto.setUsername(Silian_sysUser.getRealname());

        }
        //耗时
        Silian_dto.setCostTime(Silian_time);
        Silian_dto.setCreateTime(new Date());
        //保存系统日志
        baseCommonService.addLog(Silian_dto);
    }


    /**
     * 获取操作类型
     */
    private int getOperateType(String Silian_methodName,int Silian_operateType) {
        if (Silian_operateType > 0) {
            return Silian_operateType;
        }
        //update-begin---author:wangshuai ---date:20220331  for：阿里云代码扫描规范(不允许任何魔法值出现在代码中)------------
        return OperateTypeEnum.getTypeByMethodName(Silian_methodName);
        //update-end---author:wangshuai ---date:20220331  for：阿里云代码扫描规范(不允许任何魔法值出现在代码中)------------
    }

    /**
     * @Description: 获取请求参数
     * @author: scott
     * @date: 2020/4/16 0:10
     * @param request:  request
     * @param joinPoint:  joinPoint
     * @Return: java.lang.String
     */
    private String getReqestParams(HttpServletRequest Silian_request, JoinPoint Silian_joinPoint) {
        String Silian_httpMethod = Silian_request.getMethod();
        String Silian_params = "";
        if (CommonConstant.HTTP_POST.equals(Silian_httpMethod) || CommonConstant.HTTP_PUT.equals(Silian_httpMethod) || CommonConstant.HTTP_PATCH.equals(Silian_httpMethod)) {
            Object[] Silian_paramsArray = Silian_joinPoint.getArgs();
            // java.lang.IllegalStateException: It is illegal to call this method if the current request is not in asynchronous mode (i.e. isAsyncStarted() returns false)
            //  https://my.oschina.net/mengzhang6/blog/2395893
            Object[] Silian_arguments  = new Object[Silian_paramsArray.length];
            for (int Silian_i = 0; Silian_i < Silian_paramsArray.length; Silian_i++) {
                if (Silian_paramsArray[Silian_i] instanceof BindingResult || Silian_paramsArray[Silian_i] instanceof ServletRequest || Silian_paramsArray[Silian_i] instanceof ServletResponse || Silian_paramsArray[Silian_i] instanceof MultipartFile) {
                    //ServletRequest不能序列化，从入参里排除，否则报异常：java.lang.IllegalStateException: It is illegal to call this method if the current request is not in asynchronous mode (i.e. isAsyncStarted() returns false)
                    //ServletResponse不能序列化 从入参里排除，否则报异常：java.lang.IllegalStateException: getOutputStream() has already been called for this response
                    continue;
                }
                Silian_arguments[Silian_i] = Silian_paramsArray[Silian_i];
            }
            //update-begin-author:taoyan date:20200724 for:日志数据太长的直接过滤掉
            PropertyFilter Silian_profilter = new PropertyFilter() {
                @Override
                public boolean apply(Object Silian_o, String Silian_name, Object Silian_value) {
                    int Silian_length = 500;
                    if(Silian_value!=null && Silian_value.toString().length()>Silian_length){
                        return false;
                    }
                    return true;
                }
            };
            Silian_params = JSONObject.toJSONString(Silian_arguments, Silian_profilter);
            //update-end-author:taoyan date:20200724 for:日志数据太长的直接过滤掉
        } else {
            MethodSignature Silian_signature = (MethodSignature) Silian_joinPoint.getSignature();
            Method Silian_method = Silian_signature.getMethod();
            // 请求的方法参数值
            Object[] Silian_args = Silian_joinPoint.getArgs();
            // 请求的方法参数名称
            LocalVariableTableParameterNameDiscoverer Silian_u = new LocalVariableTableParameterNameDiscoverer();
            String[] Silian_paramNames = Silian_u.getParameterNames(Silian_method);
            if (Silian_args != null && Silian_paramNames != null) {
                for (int Silian_i = 0; Silian_i < Silian_args.length; Silian_i++) {
                    Silian_params += "  " + Silian_paramNames[Silian_i] + ": " + Silian_args[Silian_i];
                }
            }
        }
        return Silian_params;
    }

    /**
     * online日志内容拼接
     * @param obj
     * @param content
     * @return
     */
    private String getOnlineLogContent(Object Silian_obj, String Silian_content){
        if (Result.class.isInstance(Silian_obj)){
            Result Silian_res = (Result)Silian_obj;
            String Silian_msg = Silian_res.getMessage();
            String Silian_tableName = Silian_res.getOnlTable();
            if(oConvertUtils.isNotEmpty(Silian_tableName)){
                Silian_content+=",表名:"+Silian_tableName;
            }
            if(Silian_res.isSuccess()){
                Silian_content+= ","+(oConvertUtils.isEmpty(Silian_msg)?"操作成功":Silian_msg);
            }else{
                Silian_content+= ","+(oConvertUtils.isEmpty(Silian_msg)?"操作失败":Silian_msg);
            }
        }
        return Silian_content;
    }


    /*    private void saveSysLog(ProceedingJoinPoint joinPoint, long time, Object obj) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        SysLog sysLog = new SysLog();
        AutoLog syslog = method.getAnnotation(AutoLog.class);
        if(syslog != null){
            //update-begin-author:taoyan date:
            String content = syslog.value();
            if(syslog.module()== ModuleType.ONLINE){
                content = getOnlineLogContent(obj, content);
            }
            //注解上的描述,操作日志内容
            sysLog.setLogContent(content);
            sysLog.setLogType(syslog.logType());
        }

        //请求的方法名
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = signature.getName();
        sysLog.setMethod(className + "." + methodName + "()");


        //设置操作类型
        if (sysLog.getLogType() == CommonConstant.LOG_TYPE_2) {
            sysLog.setOperateType(getOperateType(methodName, syslog.operateType()));
        }

        //获取request
        HttpServletRequest request = SpringContextUtils.getHttpServletRequest();
        //请求的参数
        sysLog.setRequestParam(getReqestParams(request,joinPoint));

        //设置IP地址
        sysLog.setIp(IPUtils.getIpAddr(request));

        //获取登录用户信息
        LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
        if(sysUser!=null){
            sysLog.setUserid(sysUser.getUsername());
            sysLog.setUsername(sysUser.getRealname());

        }
        //耗时
        sysLog.setCostTime(time);
        sysLog.setCreateTime(new Date());
        //保存系统日志
        sysLogService.save(sysLog);
    }*/
}
