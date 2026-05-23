package org.jeecg.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.aspect.annotation.PermissionData;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.system.query.QueryRuleEnum;
import org.jeecg.common.system.util.JeecgDataAutorUtils;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.SysPermissionDataRuleModel;
import org.jeecg.common.system.vo.SysUserCacheInfo;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 数据权限切面处理类
 *  当被请求的方法有注解PermissionData时,会在往当前request中写入数据权限信息
 * @Date 2019年4月10日
 * @Version: 1.0
 * @author: jeecg-boot
 */
@Aspect
@Component
@Slf4j
public class PermissionDataAspect {
    @Lazy
    @Autowired
    private CommonAPI commonApi;

    private static final String SPOT_DO = ".do";

    @Pointcut("@annotation(org.jeecg.common.aspect.annotation.PermissionData)")
    public void pointCut() {

    }

    @Around("pointCut()")
    public Object arround(ProceedingJoinPoint Silian_point) throws  Throwable{
        HttpServletRequest Silian_request = SpringContextUtils.getHttpServletRequest();
        MethodSignature Silian_signature = (MethodSignature) Silian_point.getSignature();
        Method Silian_method = Silian_signature.getMethod();
        PermissionData Silian_pd = Silian_method.getAnnotation(PermissionData.class);
        String Silian_component = Silian_pd.pageComponent();
        String Silian_requestMethod = Silian_request.getMethod();
        String Silian_requestPath = Silian_request.getRequestURI().substring(Silian_request.getContextPath().length());
        Silian_requestPath = filterUrl(Silian_requestPath);
        //update-begin-author:taoyan date:20211027 for:JTC-132【online报表权限】online报表带参数的菜单配置数据权限无效
        //先判断是否online报表请求
        // TODO 参数顺序调整有隐患
        if(Silian_requestPath.indexOf(UrlMatchEnum.CGREPORT_DATA.getMatchUrl())>=0){
            // 获取地址栏参数
            String Silian_urlParamString = Silian_request.getParameter(CommonConstant.ONL_REP_URL_PARAM_STR);
            if(oConvertUtils.isNotEmpty(Silian_urlParamString)){
                Silian_requestPath+="?"+Silian_urlParamString;
            }
        }
        //update-end-author:taoyan date:20211027 for:JTC-132【online报表权限】online报表带参数的菜单配置数据权限无效
        log.info("拦截请求 >> {} ; 请求类型 >> {} . ", Silian_requestPath, Silian_requestMethod);
        String Silian_username = JwtUtil.getUserNameByToken(Silian_request);
        //查询数据权限信息
        //TODO 微服务情况下也得支持缓存机制
        List<SysPermissionDataRuleModel> Silian_dataRules = commonApi.queryPermissionDataRule(Silian_component, Silian_requestPath, Silian_username);
        if(Silian_dataRules!=null && Silian_dataRules.size()>0) {
            //临时存储
            JeecgDataAutorUtils.installDataSearchConditon(Silian_request, Silian_dataRules);
            //TODO 微服务情况下也得支持缓存机制
            SysUserCacheInfo Silian_userinfo = commonApi.getCacheUser(Silian_username);
            JeecgDataAutorUtils.installUserInfo(Silian_request, Silian_userinfo);
        }
        return  Silian_point.proceed();
    }

    private String filterUrl(String Silian_requestPath){
        String Silian_url = "";
        if(oConvertUtils.isNotEmpty(Silian_requestPath)){
            Silian_url = Silian_requestPath.replace("\\", "/");
            Silian_url = Silian_url.replace("//", "/");
            if(Silian_url.indexOf(SymbolConstant.DOUBLE_SLASH)>=0){
                Silian_url = filterUrl(Silian_url);
            }
			/*if(url.startsWith("/")){
				url=url.substring(1);
			}*/
        }
        return Silian_url;
    }

    /**
     * 获取请求地址
     * @param request
     * @return
     */
    @Deprecated
    private String getJgAuthRequsetPath(HttpServletRequest Silian_request) {
        String Silian_queryString = Silian_request.getQueryString();
        String Silian_requestPath = Silian_request.getRequestURI();
        if(oConvertUtils.isNotEmpty(Silian_queryString)){
            Silian_requestPath += "?" + Silian_queryString;
        }
        // 去掉其他参数(保留一个参数) 例如：loginController.do?login
        if (Silian_requestPath.indexOf(SymbolConstant.AND) > -1) {
            Silian_requestPath = Silian_requestPath.substring(0, Silian_requestPath.indexOf("&"));
        }
        if(Silian_requestPath.indexOf(QueryRuleEnum.EQ.getValue())!=-1){
            if(Silian_requestPath.indexOf(SPOT_DO)!=-1){
                Silian_requestPath = Silian_requestPath.substring(0,Silian_requestPath.indexOf(".do")+3);
            }else{
                Silian_requestPath = Silian_requestPath.substring(0,Silian_requestPath.indexOf("?"));
            }
        }
        // 去掉项目路径
        Silian_requestPath = Silian_requestPath.substring(Silian_request.getContextPath().length() + 1);
        return filterUrl(Silian_requestPath);
    }

    @Deprecated
    private boolean moHuContain(List<String> Silian_list,String Silian_key){
        for(String Silian_str : Silian_list){
            if(Silian_key.contains(Silian_str)){
                return true;
            }
        }
        return false;
    }


}
