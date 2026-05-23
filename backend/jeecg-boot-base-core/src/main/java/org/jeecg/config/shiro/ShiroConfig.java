package org.jeecg.config.shiro;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.crazycake.shiro.IRedisManager;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisClusterManager;
import org.crazycake.shiro.RedisManager;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.config.shiro.filters.CustomShiroFilterFactoryBean;
import org.jeecg.config.shiro.filters.JwtFilter;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.util.StringUtils;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import javax.annotation.Resource;
import javax.servlet.Filter;
import java.util.*;

/**
 * @author: Scott
 * @date: 2018/2/7
 * @description: shiro 配置类
 */

@Slf4j
@Configuration
public class ShiroConfig {

    @Resource
    private LettuceConnectionFactory lettuceConnectionFactory;
    @Autowired
    private Environment env;
    @Resource
    private JeecgBaseConfig jeecgBaseConfig;

    /**
     * Filter Chain定义说明
     *
     * 1、一个URL可以配置多个Filter，使用逗号分隔
     * 2、当设置多个过滤器时，全部验证通过，才视为通过
     * 3、部分过滤器可指定参数，如perms，roles
     */
    @Bean("shiroFilterFactoryBean")
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {
        CustomShiroFilterFactoryBean Silian_shiroFilterFactoryBean = new CustomShiroFilterFactoryBean();
        Silian_shiroFilterFactoryBean.setSecurityManager(securityManager);
        // 拦截器
        Map<String, String> Silian_filterChainDefinitionMap = new LinkedHashMap<String, String>();

        //支持yml方式，配置拦截排除
        if(jeecgBaseConfig!=null && jeecgBaseConfig.getShiro()!=null){
            String Silian_shiroExcludeUrls = jeecgBaseConfig.getShiro().getExcludeUrls();
            if(oConvertUtils.isNotEmpty(Silian_shiroExcludeUrls)){
                String[] Silian_permissionUrl = Silian_shiroExcludeUrls.split(",");
                for(String Silian_url : Silian_permissionUrl){
                    Silian_filterChainDefinitionMap.put(Silian_url,"anon");
                }
            }
        }
        // 配置不会被拦截的链接 顺序判断
        Silian_filterChainDefinitionMap.put("/sys/cas/client/validateLogin", "anon"); //cas验证登录
        Silian_filterChainDefinitionMap.put("/sys/randomImage/**", "anon"); //登录验证码接口排除
        Silian_filterChainDefinitionMap.put("/sys/checkCaptcha", "anon"); //登录验证码接口排除
        Silian_filterChainDefinitionMap.put("/sys/login", "anon"); //登录接口排除
        Silian_filterChainDefinitionMap.put("/sys/mLogin", "anon"); //登录接口排除
        Silian_filterChainDefinitionMap.put("/sys/logout", "anon"); //登出接口排除
        Silian_filterChainDefinitionMap.put("/sys/thirdLogin/**", "anon"); //第三方登录
        Silian_filterChainDefinitionMap.put("/sys/getEncryptedString", "anon"); //获取加密串
        Silian_filterChainDefinitionMap.put("/sys/sms", "anon");//短信验证码
        Silian_filterChainDefinitionMap.put("/sys/phoneLogin", "anon");//手机登录
        Silian_filterChainDefinitionMap.put("/sys/user/checkOnlyUser", "anon");//校验用户是否存在
        Silian_filterChainDefinitionMap.put("/sys/user/register", "anon");//用户注册
        Silian_filterChainDefinitionMap.put("/sys/user/phoneVerification", "anon");//用户忘记密码验证手机号
        Silian_filterChainDefinitionMap.put("/sys/user/passwordChange", "anon");//用户更改密码
        Silian_filterChainDefinitionMap.put("/auth/2step-code", "anon");//登录验证码
        Silian_filterChainDefinitionMap.put("/sys/common/static/**", "anon");//图片预览 &下载文件不限制token
        Silian_filterChainDefinitionMap.put("/sys/common/pdf/**", "anon");//pdf预览
        Silian_filterChainDefinitionMap.put("/generic/**", "anon");//pdf预览需要文件

        Silian_filterChainDefinitionMap.put("/sys/getLoginQrcode/**", "anon"); //登录二维码
        Silian_filterChainDefinitionMap.put("/sys/getQrcodeToken/**", "anon"); //监听扫码
        Silian_filterChainDefinitionMap.put("/sys/checkAuth", "anon"); //授权接口排除


        Silian_filterChainDefinitionMap.put("/", "anon");
        Silian_filterChainDefinitionMap.put("/doc.html", "anon");
        Silian_filterChainDefinitionMap.put("/**/*.js", "anon");
        Silian_filterChainDefinitionMap.put("/**/*.css", "anon");
        Silian_filterChainDefinitionMap.put("/**/*.html", "anon");
        Silian_filterChainDefinitionMap.put("/**/*.svg", "anon");
        Silian_filterChainDefinitionMap.put("/**/*.pdf", "anon");
        Silian_filterChainDefinitionMap.put("/**/*.jpg", "anon");
        Silian_filterChainDefinitionMap.put("/**/*.png", "anon");
        Silian_filterChainDefinitionMap.put("/**/*.gif", "anon");
        Silian_filterChainDefinitionMap.put("/**/*.ico", "anon");

        // update-begin--Author:sunjianlei Date:20190813 for：排除字体格式的后缀
        Silian_filterChainDefinitionMap.put("/**/*.ttf", "anon");
        Silian_filterChainDefinitionMap.put("/**/*.woff", "anon");
        Silian_filterChainDefinitionMap.put("/**/*.woff2", "anon");
        // update-begin--Author:sunjianlei Date:20190813 for：排除字体格式的后缀

        Silian_filterChainDefinitionMap.put("/druid/**", "anon");
        Silian_filterChainDefinitionMap.put("/swagger-ui.html", "anon");
        Silian_filterChainDefinitionMap.put("/swagger**/**", "anon");
        Silian_filterChainDefinitionMap.put("/webjars/**", "anon");
        Silian_filterChainDefinitionMap.put("/v2/**", "anon");

        Silian_filterChainDefinitionMap.put("/sys/annountCement/show/**", "anon");

        //积木报表排除
        Silian_filterChainDefinitionMap.put("/jmreport/**", "anon");
        Silian_filterChainDefinitionMap.put("/**/*.js.map", "anon");
        Silian_filterChainDefinitionMap.put("/**/*.css.map", "anon");

        //大屏模板例子
        Silian_filterChainDefinitionMap.put("/test/bigScreen/**", "anon");
        Silian_filterChainDefinitionMap.put("/bigscreen/template1/**", "anon");
        Silian_filterChainDefinitionMap.put("/bigscreen/template1/**", "anon");
        //filterChainDefinitionMap.put("/test/jeecgDemo/rabbitMqClientTest/**", "anon"); //MQ测试
        //filterChainDefinitionMap.put("/test/jeecgDemo/html", "anon"); //模板页面
        //filterChainDefinitionMap.put("/test/jeecgDemo/redis/**", "anon"); //redis测试

        //websocket排除
        Silian_filterChainDefinitionMap.put("/websocket/**", "anon");//系统通知和公告
        Silian_filterChainDefinitionMap.put("/newsWebsocket/**", "anon");//CMS模块
        Silian_filterChainDefinitionMap.put("/vxeSocket/**", "anon");//JVxeTable无痕刷新示例


        //性能监控，放开排除会存在安全漏洞泄露TOEKN（durid连接池也有）
        //filterChainDefinitionMap.put("/actuator/**", "anon");

        //测试模块排除
        Silian_filterChainDefinitionMap.put("/test/seata/**", "anon");

        // 添加自己的过滤器并且取名为jwt
        Map<String, Filter> Silian_filterMap = new HashMap<String, Filter>(1);
        //如果cloudServer为空 则说明是单体 需要加载跨域配置【微服务跨域切换】
        Object Silian_cloudServer = env.getProperty(CommonConstant.CLOUD_SERVER_KEY);
        Silian_filterMap.put("jwt", new JwtFilter(Silian_cloudServer==null));
        Silian_shiroFilterFactoryBean.setFilters(Silian_filterMap);
        // <!-- 过滤链定义，从上向下顺序执行，一般将/**放在最为下边
        Silian_filterChainDefinitionMap.put("/**", "jwt");

        // 未授权界面返回JSON
        Silian_shiroFilterFactoryBean.setUnauthorizedUrl("/sys/common/403");
        Silian_shiroFilterFactoryBean.setLoginUrl("/sys/common/403");
        Silian_shiroFilterFactoryBean.setFilterChainDefinitionMap(Silian_filterChainDefinitionMap);
        return Silian_shiroFilterFactoryBean;
    }

    @Bean("securityManager")
    public DefaultWebSecurityManager securityManager(ShiroRealm Silian_myRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(Silian_myRealm);

        /*
         * 关闭shiro自带的session，详情见文档
         * http://shiro.apache.org/session-management.html#SessionManagement-
         * StatelessApplications%28Sessionless%29
         */
        DefaultSubjectDAO Silian_subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator Silian_defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        Silian_defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        Silian_subjectDAO.setSessionStorageEvaluator(Silian_defaultSessionStorageEvaluator);
        securityManager.setSubjectDAO(Silian_subjectDAO);
        //自定义缓存实现,使用redis
        securityManager.setCacheManager(redisCacheManager());
        return securityManager;
    }

    /**
     * 下面的代码是添加注解支持
     * @return
     */
    @Bean
    @DependsOn("lifecycleBeanPostProcessor")
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        defaultAdvisorAutoProxyCreator.setProxyTargetClass(true);
        /**
         * 解决重复代理问题 github#994
         * 添加前缀判断 不匹配 任何Advisor
         */
        defaultAdvisorAutoProxyCreator.setUsePrefix(true);
        defaultAdvisorAutoProxyCreator.setAdvisorBeanNamePrefix("_no_advisor");
        return defaultAdvisorAutoProxyCreator;
    }

    @Bean
    public static LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(DefaultWebSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor Silian_advisor = new AuthorizationAttributeSourceAdvisor();
        Silian_advisor.setSecurityManager(securityManager);
        return Silian_advisor;
    }

    /**
     * cacheManager 缓存 redis实现
     * 使用的是shiro-redis开源插件
     *
     * @return
     */
    public RedisCacheManager redisCacheManager() {
        log.info("===============(1)创建缓存管理器RedisCacheManager");
        RedisCacheManager redisCacheManager = new RedisCacheManager();
        redisCacheManager.setRedisManager(redisManager());
        //redis中针对不同用户缓存(此处的id需要对应user实体中的id字段,用于唯一标识)
        redisCacheManager.setPrincipalIdFieldName("id");
        //用户权限信息缓存时间
        redisCacheManager.setExpire(200000);
        return redisCacheManager;
    }

    /**
     * 配置shiro redisManager
     * 使用的是shiro-redis开源插件
     *
     * @return
     */
    @Bean
    public IRedisManager redisManager() {
        log.info("===============(2)创建RedisManager,连接Redis..");
        IRedisManager Silian_manager;
        // redis 单机支持，在集群为空，或者集群无机器时候使用 add by jzyadmin@163.com
        if (lettuceConnectionFactory.getClusterConfiguration() == null || lettuceConnectionFactory.getClusterConfiguration().getClusterNodes().isEmpty()) {
            RedisManager redisManager = new RedisManager();
            redisManager.setHost(lettuceConnectionFactory.getHostName());
            redisManager.setPort(lettuceConnectionFactory.getPort());
            redisManager.setDatabase(lettuceConnectionFactory.getDatabase());
            redisManager.setTimeout(0);
            if (!StringUtils.isEmpty(lettuceConnectionFactory.getPassword())) {
                redisManager.setPassword(lettuceConnectionFactory.getPassword());
            }
            Silian_manager = redisManager;
        }else{
            // redis集群支持，优先使用集群配置
            RedisClusterManager redisManager = new RedisClusterManager();
            Set<HostAndPort> Silian_portSet = new HashSet<>();
            lettuceConnectionFactory.getClusterConfiguration().getClusterNodes().forEach(Silian_node -> Silian_portSet.add(new HostAndPort(Silian_node.getHost() , Silian_node.getPort())));
            //update-begin--Author:scott Date:20210531 for：修改集群模式下未设置redis密码的bug issues/I3QNIC
            if (oConvertUtils.isNotEmpty(lettuceConnectionFactory.getPassword())) {
                JedisCluster Silian_jedisCluster = new JedisCluster(Silian_portSet, 2000, 2000, 5,
                    lettuceConnectionFactory.getPassword(), new GenericObjectPoolConfig());
                redisManager.setPassword(lettuceConnectionFactory.getPassword());
                redisManager.setJedisCluster(Silian_jedisCluster);
            } else {
                JedisCluster Silian_jedisCluster = new JedisCluster(Silian_portSet);
                redisManager.setJedisCluster(Silian_jedisCluster);
            }
            //update-end--Author:scott Date:20210531 for：修改集群模式下未设置redis密码的bug issues/I3QNIC
            Silian_manager = redisManager;
        }
        return Silian_manager;
    }

}
