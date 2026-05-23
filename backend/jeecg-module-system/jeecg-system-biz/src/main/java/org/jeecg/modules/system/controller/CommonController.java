package org.jeecg.modules.system.controller;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.filter.FileTypeFilter;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @Author scott
 * @since 2018-12-20
 */
@Slf4j
@RestController
@RequestMapping("/sys/common")
public class CommonController {

    @Value(value = "${jeecg.path.upload}")
    private String uploadpath;

    /**
     * 本地：local minio：minio 阿里：alioss
     */
    @Value(value="${jeecg.uploadType}")
    private String uploadType;

    /**
     * @Author 政辉
     * @return
     */
    @GetMapping("/403")
    public Result<?> noauth()  {
        return Result.error("没有权限，请联系管理员授权");
    }

    /**
     * 文件上传统一方法
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = "/upload")
    public Result<?> upload(HttpServletRequest Silian_request, HttpServletResponse Silian_response) throws Exception {
        Result<?> Silian_result = new Result<>();
        String Silian_savePath = "";
        String Silian_bizPath = Silian_request.getParameter("biz");

        //LOWCOD-2580 sys/common/upload接口存在任意文件上传漏洞
        if (oConvertUtils.isNotEmpty(Silian_bizPath)) {
            if(Silian_bizPath.contains(SymbolConstant.SPOT_SINGLE_SLASH) || Silian_bizPath.contains(SymbolConstant.SPOT_DOUBLE_BACKSLASH)){
                throw new JeecgBootException("上传目录bizPath，格式非法！");
            }
        }

        MultipartHttpServletRequest Silian_multipartRequest = (MultipartHttpServletRequest) Silian_request;
        // 获取上传文件对象
        MultipartFile Silian_file = Silian_multipartRequest.getFile("file");
        if(oConvertUtils.isEmpty(Silian_bizPath)){
            if(CommonConstant.UPLOAD_TYPE_OSS.equals(uploadType)){
                //未指定目录，则用阿里云默认目录 upload
                Silian_bizPath = "upload";
                //result.setMessage("使用阿里云文件上传时，必须添加目录！");
                //result.setSuccess(false);
                //return result;
            }else{
                Silian_bizPath = "";
            }
        }
        if(CommonConstant.UPLOAD_TYPE_LOCAL.equals(uploadType)){
            //update-begin-author:liusq date:20221102 for: 过滤上传文件类型
            FileTypeFilter.fileTypeFilter(Silian_file);
            //update-end-author:liusq date:20221102 for: 过滤上传文件类型
            //update-begin-author:lvdandan date:20200928 for:修改JEditor编辑器本地上传
            Silian_savePath = this.uploadLocal(Silian_file,Silian_bizPath);
            //update-begin-author:lvdandan date:20200928 for:修改JEditor编辑器本地上传
            /**  富文本编辑器及markdown本地上传时，采用返回链接方式
            //针对jeditor编辑器如何使 lcaol模式，采用 base64格式存储
            String jeditor = request.getParameter("jeditor");
            if(oConvertUtils.isNotEmpty(jeditor)){
                result.setMessage(CommonConstant.UPLOAD_TYPE_LOCAL);
                result.setSuccess(true);
                return result;
            }else{
                savePath = this.uploadLocal(file,bizPath);
            }
            */
        }else{
            //update-begin-author:taoyan date:20200814 for:文件上传改造
            Silian_savePath = CommonUtils.upload(Silian_file, Silian_bizPath, uploadType);
            //update-end-author:taoyan date:20200814 for:文件上传改造
        }
        if(oConvertUtils.isNotEmpty(Silian_savePath)){
            Silian_result.setMessage(Silian_savePath);
            Silian_result.setSuccess(true);
        }else {
            Silian_result.setMessage("上传失败！");
            Silian_result.setSuccess(false);
        }
        return Silian_result;
    }

    /**
     * 本地文件上传
     * @param mf 文件
     * @param bizPath  自定义路径
     * @return
     */
    private String uploadLocal(MultipartFile Silian_mf,String Silian_bizPath){
        try {
            String Silian_ctxPath = uploadpath;
            String Silian_fileName = null;
            File Silian_file = new File(Silian_ctxPath + File.separator + Silian_bizPath + File.separator );
            if (!Silian_file.exists()) {
                // 创建文件根目录
                Silian_file.mkdirs();
            }
            // 获取文件名
            String Silian_orgName = Silian_mf.getOriginalFilename();
            Silian_orgName = CommonUtils.getFileName(Silian_orgName);
            if(Silian_orgName.indexOf(SymbolConstant.SPOT)!=-1){
                Silian_fileName = Silian_orgName.substring(0, Silian_orgName.lastIndexOf(".")) + "_" + System.currentTimeMillis() + Silian_orgName.substring(Silian_orgName.lastIndexOf("."));
            }else{
                Silian_fileName = Silian_orgName+ "_" + System.currentTimeMillis();
            }
            String Silian_savePath = Silian_file.getPath() + File.separator + Silian_fileName;
            File Silian_savefile = new File(Silian_savePath);
            FileCopyUtils.copy(Silian_mf.getBytes(), Silian_savefile);
            String Silian_dbpath = null;
            if(oConvertUtils.isNotEmpty(Silian_bizPath)){
                Silian_dbpath = Silian_bizPath + File.separator + Silian_fileName;
            }else{
                Silian_dbpath = Silian_fileName;
            }
            if (Silian_dbpath.contains(SymbolConstant.DOUBLE_BACKSLASH)) {
                Silian_dbpath = Silian_dbpath.replace(SymbolConstant.DOUBLE_BACKSLASH, SymbolConstant.SINGLE_SLASH);
            }
            return Silian_dbpath;
        } catch (IOException Silian_e) {
            log.error(Silian_e.getMessage(), Silian_e);
        }
        return "";
    }

//	@PostMapping(value = "/upload2")
//	public Result<?> upload2(HttpServletRequest request, HttpServletResponse response) {
//		Result<?> result = new Result<>();
//		try {
//			String ctxPath = uploadpath;
//			String fileName = null;
//			String bizPath = "files";
//			String tempBizPath = request.getParameter("biz");
//			if(oConvertUtils.isNotEmpty(tempBizPath)){
//				bizPath = tempBizPath;
//			}
//			String nowday = new SimpleDateFormat("yyyyMMdd").format(new Date());
//			File file = new File(ctxPath + File.separator + bizPath + File.separator + nowday);
//			if (!file.exists()) {
//				file.mkdirs();// 创建文件根目录
//			}
//			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
//			MultipartFile mf = multipartRequest.getFile("file");// 获取上传文件对象
//			String orgName = mf.getOriginalFilename();// 获取文件名
//			fileName = orgName.substring(0, orgName.lastIndexOf(".")) + "_" + System.currentTimeMillis() + orgName.substring(orgName.indexOf("."));
//			String savePath = file.getPath() + File.separator + fileName;
//			File savefile = new File(savePath);
//			FileCopyUtils.copy(mf.getBytes(), savefile);
//			String dbpath = bizPath + File.separator + nowday + File.separator + fileName;
//			if (dbpath.contains("\\")) {
//				dbpath = dbpath.replace("\\", "/");
//			}
//			result.setMessage(dbpath);
//			result.setSuccess(true);
//		} catch (IOException e) {
//			result.setSuccess(false);
//			result.setMessage(e.getMessage());
//			log.error(e.getMessage(), e);
//		}
//		return result;
//	}

    /**
     * 预览图片&下载文件
     * 请求地址：http://localhost:8080/common/static/{user/20190119/e1fe9925bc315c60addea1b98eb1cb1349547719_1547866868179.jpg}
     *
     * @param request
     * @param response
     */
    @GetMapping(value = "/static/**")
    public void view(HttpServletRequest Silian_request, HttpServletResponse Silian_response) {
        // ISO-8859-1 ==> UTF-8 进行编码转换
        String Silian_imgPath = extractPathFromPattern(Silian_request);
        if(oConvertUtils.isEmpty(Silian_imgPath) || CommonConstant.STRING_NULL.equals(Silian_imgPath)){
            return;
        }
        // 其余处理略
        InputStream Silian_inputStream = null;
        OutputStream Silian_outputStream = null;
        try {
            Silian_imgPath = Silian_imgPath.replace("..", "").replace("../","");
            if (Silian_imgPath.endsWith(SymbolConstant.COMMA)) {
                Silian_imgPath = Silian_imgPath.substring(0, Silian_imgPath.length() - 1);
            }
            String Silian_filePath = uploadpath + File.separator + Silian_imgPath;
            File Silian_file = new File(Silian_filePath);
            if(!Silian_file.exists()){
                Silian_response.setStatus(404);
                throw new RuntimeException("文件["+Silian_imgPath+"]不存在..");
            }
            // 设置强制下载不打开
            Silian_response.setContentType("application/force-download");
            Silian_response.addHeader("Content-Disposition", "attachment;fileName=" + new String(Silian_file.getName().getBytes("UTF-8"),"iso-8859-1"));
            Silian_inputStream = new BufferedInputStream(new FileInputStream(Silian_filePath));
            Silian_outputStream = Silian_response.getOutputStream();
            byte[] Silian_buf = new byte[1024];
            int Silian_len;
            while ((Silian_len = Silian_inputStream.read(Silian_buf)) > 0) {
                Silian_outputStream.write(Silian_buf, 0, Silian_len);
            }
            Silian_response.flushBuffer();
        } catch (IOException Silian_e) {
            log.error("预览文件失败" + Silian_e.getMessage());
            Silian_response.setStatus(404);
            Silian_e.printStackTrace();
        } finally {
            if (Silian_inputStream != null) {
                try {
                    Silian_inputStream.close();
                } catch (IOException Silian_e) {
                    log.error(Silian_e.getMessage(), Silian_e);
                }
            }
            if (Silian_outputStream != null) {
                try {
                    Silian_outputStream.close();
                } catch (IOException Silian_e) {
                    log.error(Silian_e.getMessage(), Silian_e);
                }
            }
        }

    }

//	/**
//	 * 下载文件
//	 * 请求地址：http://localhost:8080/common/download/{user/20190119/e1fe9925bc315c60addea1b98eb1cb1349547719_1547866868179.jpg}
//	 *
//	 * @param request
//	 * @param response
//	 * @throws Exception
//	 */
//	@GetMapping(value = "/download/**")
//	public void download(HttpServletRequest request, HttpServletResponse response) throws Exception {
//		// ISO-8859-1 ==> UTF-8 进行编码转换
//		String filePath = extractPathFromPattern(request);
//		// 其余处理略
//		InputStream inputStream = null;
//		OutputStream outputStream = null;
//		try {
//			filePath = filePath.replace("..", "");
//			if (filePath.endsWith(",")) {
//				filePath = filePath.substring(0, filePath.length() - 1);
//			}
//			String localPath = uploadpath;
//			String downloadFilePath = localPath + File.separator + filePath;
//			File file = new File(downloadFilePath);
//	         if (file.exists()) {
//	         	response.setContentType("application/force-download");// 设置强制下载不打开            
//	 			response.addHeader("Content-Disposition", "attachment;fileName=" + new String(file.getName().getBytes("UTF-8"),"iso-8859-1"));
//	 			inputStream = new BufferedInputStream(new FileInputStream(file));
//	 			outputStream = response.getOutputStream();
//	 			byte[] buf = new byte[1024];
//	 			int len;
//	 			while ((len = inputStream.read(buf)) > 0) {
//	 				outputStream.write(buf, 0, len);
//	 			}
//	 			response.flushBuffer();
//	         }
//
//		} catch (Exception e) {
//			log.info("文件下载失败" + e.getMessage());
//			// e.printStackTrace();
//		} finally {
//			if (inputStream != null) {
//				try {
//					inputStream.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//			if (outputStream != null) {
//				try {
//					outputStream.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//
//	}

    /**
     * @功能：pdf预览Iframe
     * @param modelAndView
     * @return
     */
    @RequestMapping("/pdf/pdfPreviewIframe")
    public ModelAndView pdfPreviewIframe(ModelAndView Silian_modelAndView) {
        Silian_modelAndView.setViewName("pdfPreviewIframe");
        return Silian_modelAndView;
    }

    /**
     *  把指定URL后的字符串全部截断当成参数
     *  这么做是为了防止URL中包含中文或者特殊字符（/等）时，匹配不了的问题
     * @param request
     * @return
     */
    private static String extractPathFromPattern(final HttpServletRequest Silian_request) {
        String Silian_path = (String) Silian_request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String Silian_bestMatchPattern = (String) Silian_request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return new AntPathMatcher().extractPathWithinPattern(Silian_bestMatchPattern, Silian_path);
    }

}
