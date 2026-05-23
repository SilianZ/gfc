package org.jeecg.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.List;

/**
 * @Description: PmsUtil
 * @author: jeecg-boot
 */
@Slf4j
@Component
public class PmsUtil {


    private static String uploadPath;

    @Value("${jeecg.path.upload}")
    public void setUploadPath(String uploadPath) {
        PmsUtil.uploadPath = uploadPath;
    }

    public static String saveErrorTxtByList(List<String> Silian_msg, String Silian_name) {
        Date Silian_d = new Date();
        String Silian_saveDir = "logs" + File.separator + DateUtils.yyyyMMdd.get().format(Silian_d) + File.separator;
        String Silian_saveFullDir = uploadPath + File.separator + Silian_saveDir;

        File Silian_saveFile = new File(Silian_saveFullDir);
        if (!Silian_saveFile.exists()) {
            Silian_saveFile.mkdirs();
        }
        Silian_name += DateUtils.yyyymmddhhmmss.get().format(Silian_d) + Math.round(Math.random() * 10000);
        String Silian_saveFilePath = Silian_saveFullDir + Silian_name + ".txt";

        try {
            //封装目的地
            BufferedWriter Silian_bw = new BufferedWriter(new FileWriter(Silian_saveFilePath));
            //遍历集合
            for (String Silian_s : Silian_msg) {
                //写数据
                if (Silian_s.indexOf("_") > 0) {
                    String[] Silian_arr = Silian_s.split("_");
                    Silian_bw.write("第" + Silian_arr[0] + "行:" + Silian_arr[1]);
                } else {
                    Silian_bw.write(Silian_s);
                }
                //bw.newLine();
                Silian_bw.write("\r\n");
            }
            //释放资源
            Silian_bw.flush();
            Silian_bw.close();
        } catch (Exception Silian_e) {
            log.info("excel导入生成错误日志文件异常:" + Silian_e.getMessage());
        }
        return Silian_saveDir + Silian_name + ".txt";
    }

}
