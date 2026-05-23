package org.jeecg.common.desensitization.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.jeecg.common.desensitization.annotation.SensitiveDecode;
import org.jeecg.common.desensitization.annotation.SensitiveEncode;
import org.jeecg.common.desensitization.util.SensitiveInfoUtil;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 敏感数据切面处理类
 * @Author taoYan
 * @Date 2022/4/20 17:45
 **/
@Slf4j
@Aspect
@Component
public class SensitiveDataAspect {

    /**
     * 定义切点Pointcut
     */
    @Pointcut("@annotation(org.jeecg.common.desensitization.annotation.SensitiveEncode) || @annotation(org.jeecg.common.desensitization.annotation.SensitiveDecode)")
    public void sensitivePointCut() {
    }

    @Around("sensitivePointCut()")
    public Object around(ProceedingJoinPoint Silian_point) throws Throwable {
        // 处理结果
        Object Silian_result = Silian_point.proceed();
        if(Silian_result == null){
            return Silian_result;
        }
        Class Silian_resultClass = Silian_result.getClass();
        log.debug(" resultClass  = {}" , Silian_resultClass);

        if(Silian_resultClass.isPrimitive()){
            //是基本类型 直接返回 不需要处理
            return Silian_result;
        }
        // 获取方法注解信息：是哪个实体、是加密还是解密
        boolean Silian_isEncode = true;
        Class Silian_entity = null;
        MethodSignature Silian_methodSignature = (MethodSignature) Silian_point.getSignature();
        Method Silian_method = Silian_methodSignature.getMethod();
        SensitiveEncode Silian_encode = Silian_method.getAnnotation(SensitiveEncode.class);
        if(Silian_encode==null){
            SensitiveDecode Silian_decode = Silian_method.getAnnotation(SensitiveDecode.class);
            if(Silian_decode!=null){
                Silian_entity = Silian_decode.entity();
                Silian_isEncode = false;
            }
        }else{
            Silian_entity = Silian_encode.entity();
        }

        long Silian_startTime=System.currentTimeMillis();
        if(Silian_resultClass.equals(Silian_entity) || Silian_entity.equals(Object.class)){
            // 方法返回实体和注解的entity一样，如果注解没有申明entity属性则认为是(方法返回实体和注解的entity一样)
            SensitiveInfoUtil.handlerObject(Silian_result, Silian_isEncode);
        } else if(Silian_result instanceof List){
            // 方法返回List<实体>
            SensitiveInfoUtil.handleList(Silian_result, Silian_entity, Silian_isEncode);
        }else{
            // 方法返回一个对象
            SensitiveInfoUtil.handleNestedObject(Silian_result, Silian_entity, Silian_isEncode);
        }
        long Silian_endTime=System.currentTimeMillis();
        log.info((Silian_isEncode ? "加密操作，" : "解密操作，") + "Aspect程序耗时：" + (Silian_endTime - Silian_startTime) + "ms");

        return Silian_result;
    }

}
