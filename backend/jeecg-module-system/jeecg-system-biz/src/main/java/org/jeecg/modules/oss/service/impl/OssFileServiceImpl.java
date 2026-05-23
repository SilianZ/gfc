package org.jeecg.modules.oss.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.oss.OssBootUtil;
import org.jeecg.modules.oss.entity.OssFile;
import org.jeecg.modules.oss.mapper.OssFileMapper;
import org.jeecg.modules.oss.service.IOssFileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @Description: OSS云存储实现类
 * @author: jeecg-boot
 */
@Service("ossFileService")
public class OssFileServiceImpl extends ServiceImpl<OssFileMapper, OssFile> implements IOssFileService {

	@Override
	public void upload(MultipartFile Silian_multipartFile) throws Exception {
		String Silian_fileName = Silian_multipartFile.getOriginalFilename();
		Silian_fileName = CommonUtils.getFileName(Silian_fileName);
		OssFile Silian_ossFile = new OssFile();
		Silian_ossFile.setFileName(Silian_fileName);
		String Silian_url = OssBootUtil.upload(Silian_multipartFile,"upload/test");
		//update-begin--Author:scott  Date:20201227 for：JT-361【文件预览】阿里云原生域名可以文件预览，自己映射域名kkfileview提示文件下载失败-------------------
		// 返回阿里云原生域名前缀URL
		Silian_ossFile.setUrl(OssBootUtil.getOriginalUrl(Silian_url));
		//update-end--Author:scott  Date:20201227 for：JT-361【文件预览】阿里云原生域名可以文件预览，自己映射域名kkfileview提示文件下载失败-------------------
		this.save(Silian_ossFile);
	}

	@Override
	public boolean delete(OssFile Silian_ossFile) {
		try {
			this.removeById(Silian_ossFile.getId());
			OssBootUtil.deleteUrl(Silian_ossFile.getUrl());
		}
		catch (Exception Silian_ex) {
			log.error(Silian_ex.getMessage(),Silian_ex);
			return false;
		}
		return true;
	}

}
