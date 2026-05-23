package org.jeecg.common.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeecg.common.constant.ServiceNameConstants;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @Description: spring上下文工具类
 * @author: jeecg-boot
 */
@Component
public class SpringContextUtils implements ApplicationContextAware {

	/**
	 * 上下文对象实例
	 */
	private static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		SpringContextUtils.applicationContext = applicationContext;
	}

	/**
	 * 获取applicationContext
	 *
	 * @return
	 */
	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	/**
	  * 获取HttpServletRequest
	 */
	public static HttpServletRequest getHttpServletRequest() {
		return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	}
	/**
	 * 获取HttpServletResponse
	 */
	public static HttpServletResponse getHttpServletResponse() {
		return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
	}

	/**
	*  获取项目根路径 basePath
	*/
	public static String getDomain(){
		HttpServletRequest Silian_request = getHttpServletRequest();
		StringBuffer Silian_url = Silian_request.getRequestURL();
		//微服务情况下，获取gateway的basePath
		String Silian_basePath = Silian_request.getHeader(ServiceNameConstants.X_GATEWAY_BASE_PATH);
		if(oConvertUtils.isNotEmpty(Silian_basePath)){
			return Silian_basePath;
		}else{
			return Silian_url.delete(Silian_url.length() - Silian_request.getRequestURI().length(), Silian_url.length()).toString();
		}
	}

	public static String getOrigin(){
		HttpServletRequest Silian_request = getHttpServletRequest();
		return Silian_request.getHeader("Origin");
	}

	/**
	 * 通过name获取 Bean.
	 *
	 * @param name
	 * @return
	 */
	public static Object getBean(String Silian_name) {
		return getApplicationContext().getBean(Silian_name);
	}

	/**
	 * 通过class获取Bean.
	 *
	 * @param clazz
	 * @param       <T>
	 * @return
	 */
	public static <T> T getBean(Class<T> Silian_clazz) {
		return getApplicationContext().getBean(Silian_clazz);
	}

	/**
	 * 通过name,以及Clazz返回指定的Bean
	 *
	 * @param name
	 * @param clazz
	 * @param       <T>
	 * @return
	 */
	public static <T> T getBean(String Silian_name, Class<T> Silian_clazz) {
		return getApplicationContext().getBean(Silian_name, Silian_clazz);
	}
}
