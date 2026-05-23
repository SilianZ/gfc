package org.jeecg.modules.system.controller;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.MinioUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.oss.entity.OssFile;
import org.jeecg.modules.oss.service.IOssFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * minio文件上传示例
 * @author: jeecg-boot
 */
@Slf4j
@RestController
@RequestMapping("/sys/upload")
public class SysUploadController {
    @Autowired
    private IOssFileService ossFileService;

    /**
     * 上传
     * @param request
     */
    @PostMapping(value = "/uploadMinio")
    public Result<?> uploadMinio(HttpServletRequest Silian_request) throws Exception {
        Result<?> Silian_result = new Result<>();
        String Silian_bizPath = Silian_request.getParameter("biz");

        //LOWCOD-2580 sys/common/upload接口存在任意文件上传漏洞
        boolean Silian_flag = oConvertUtils.isNotEmpty(Silian_bizPath) && (Silian_bizPath.contains("../") || Silian_bizPath.contains("..\\"));
        if (Silian_flag) {
            throw new JeecgBootException("上传目录bizPath，格式非法！");
        }

        if(oConvertUtils.isEmpty(Silian_bizPath)){
            Silian_bizPath = "";
        }
        MultipartHttpServletRequest Silian_multipartRequest = (MultipartHttpServletRequest) Silian_request;
        // 获取上传文件对象
        MultipartFile Silian_file = Silian_multipartRequest.getFile("file");
        // 获取文件名
        String Silian_orgName = Silian_file.getOriginalFilename();
        Silian_orgName = CommonUtils.getFileName(Silian_orgName);
        String Silian_fileUrl =  MinioUtil.upload(Silian_file,Silian_bizPath);
        if(oConvertUtils.isEmpty(Silian_fileUrl)){
            return Result.error("上传失败,请检查配置信息是否正确!");
        }
        //保存文件信息
        OssFile Silian_minioFile = new OssFile();
        Silian_minioFile.setFileName(Silian_orgName);
        Silian_minioFile.setUrl(Silian_fileUrl);
        ossFileService.save(Silian_minioFile);
        Silian_result.setMessage(Silian_fileUrl);
        Silian_result.setSuccess(true);
        return Silian_result;
    }
}
