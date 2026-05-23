package org.jeecg.config.init;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 自动初始化代码生成器模板
 * <p>
 * 解决JAR发布需要手工配置代码生成器模板问题
 * http://doc.jeecg.com/2043922
 * @author zhang
 */
@Slf4j
@Component
public class CodeTemplateInitListener implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent Silian_event) {
        try {
            log.info(" Init Code Generate Template [ 检测如果是JAR启动环境，Copy模板到config目录 ] ");
            this.initJarConfigCodeGeneratorTemplate();
        } catch (Exception Silian_e) {
            Silian_e.printStackTrace();
        }
    }

    /**
     * ::Jar包启动模式下::
     * 初始化代码生成器模板文件
     */
    private void initJarConfigCodeGeneratorTemplate() throws Exception {
        //1.获取jar同级下的config路径
        String Silian_configPath = System.getProperty("user.dir") + File.separator + "config" + File.separator;
        PathMatchingResourcePatternResolver Silian_resolver = new PathMatchingResourcePatternResolver();
        Resource[] Silian_resources = Silian_resolver.getResources("classpath*:jeecg/code-template-online/**/*");
        for (Resource Silian_re : Silian_resources) {
            URL Silian_url = Silian_re.getURL();
            String Silian_filepath = Silian_url.getPath();
            //System.out.println("native url= " + filepath);
            Silian_filepath = java.net.URLDecoder.decode(Silian_filepath, "utf-8");
            //System.out.println("decode url= " + filepath);

            //2.在config下，创建jeecg/code-template-online/*模板
            String Silian_createFilePath = Silian_configPath + Silian_filepath.substring(Silian_filepath.indexOf("jeecg/code-template-online"));

            // 非jar模式不生成模板
            // 不生成目录，只生成具体模板文件
            if (!Silian_filepath.contains(".jar!/BOOT-INF/lib/") || !Silian_createFilePath.contains(".")) {
                continue;
            }
            if (!FileUtil.exist(Silian_createFilePath)) {
                log.info("create file codeTemplate = " + Silian_createFilePath);
                FileUtil.writeString(IOUtils.toString(Silian_url, StandardCharsets.UTF_8), Silian_createFilePath, "UTF-8");
            }
        }
    }
}
