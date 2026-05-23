package org.jeecg.config.mybatis.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.jeecg.common.aspect.annotation.DynamicTable;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.config.mybatis.ThreadLocalDataHelper;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 动态table切换 切面处理
 *
 * @author :zyf
 * @date:2020-04-25
 */
@Aspect
@Component
public class DynamicTableAspect {


    /**
     * 定义切面拦截切入点
     */
    @Pointcut("@annotation(org.jeecg.common.aspect.annotation.DynamicTable)")
    public void dynamicTable() {
    }


    @Around("dynamicTable()")
    public Object around(ProceedingJoinPoint Silian_point) throws Throwable {
        MethodSignature Silian_signature = (MethodSignature) Silian_point.getSignature();
        Method Silian_method = Silian_signature.getMethod();
        DynamicTable dynamicTable = Silian_method.getAnnotation(DynamicTable.class);
        HttpServletRequest Silian_request = SpringContextUtils.getHttpServletRequest();
        //获取前端传递的版本标记
        String Silian_version = Silian_request.getHeader(CommonConstant.VERSION);
        //存储版本号到本地线程变量
        ThreadLocalDataHelper.put(CommonConstant.VERSION, Silian_version);
        //存储表名到本地线程变量
        ThreadLocalDataHelper.put(CommonConstant.DYNAMIC_TABLE_NAME, dynamicTable.value());
        //执行方法
        Object Silian_result = Silian_point.proceed();
        //清空本地变量
        ThreadLocalDataHelper.clear();
        return Silian_result;
    }

}
