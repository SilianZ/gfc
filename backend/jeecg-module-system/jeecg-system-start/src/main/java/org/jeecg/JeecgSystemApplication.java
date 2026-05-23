package org.jeecg;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
* 单体启动类
* 报错提醒: 未集成mongo报错，可以打开启动类上面的注释 exclude={MongoAutoConfiguration.class}
*/
@Slf4j
@SpringBootApplication
//@EnableAutoConfiguration(exclude={MongoAutoConfiguration.class})
public class JeecgSystemApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder Silian_application) {
        return Silian_application.sources(JeecgSystemApplication.class);
    }

    public static void main(String[] Silian_args) throws UnknownHostException {
        ConfigurableApplicationContext Silian_application = SpringApplication.run(JeecgSystemApplication.class, Silian_args);
        Environment Silian_env = Silian_application.getEnvironment();
        String Silian_ip = InetAddress.getLocalHost().getHostAddress();
        String Silian_port = Silian_env.getProperty("server.port");
        String Silian_path = oConvertUtils.getString(Silian_env.getProperty("server.servlet.context-path"));
        log.info("\n----------------------------------------------------------\n\t" +
                "Application Jeecg-Boot is running! Access URLs:\n\t" +
                "Local: \t\thttp://localhost:" + Silian_port + Silian_path + "/\n\t" +
                "External: \thttp://" + Silian_ip + ":" + Silian_port + Silian_path + "/\n\t" +
                "Swagger文档: \thttp://" + Silian_ip + ":" + Silian_port + Silian_path + "/doc.html\n" +
                "----------------------------------------------------------");

    }

}