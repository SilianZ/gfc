package org.jeecg.common.util.oss;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.filter.FileTypeFilter;
import org.jeecg.common.util.filter.StrAttackFilter;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

/**
 * @Description: 阿里云 oss 上传工具类(高依赖版)
 * @Date: 2019/5/10
 * @author: jeecg-boot
 */
@Slf4j
public class OssBootUtil {

    private static String endPoint;
    private static String accessKeyId;
    private static String accessKeySecret;
    private static String bucketName;
    private static String staticDomain;

    public static void setEndPoint(String endPoint) {
        OssBootUtil.endPoint = endPoint;
    }

    public static void setAccessKeyId(String accessKeyId) {
        OssBootUtil.accessKeyId = accessKeyId;
    }

    public static void setAccessKeySecret(String accessKeySecret) {
        OssBootUtil.accessKeySecret = accessKeySecret;
    }

    public static void setBucketName(String bucketName) {
        OssBootUtil.bucketName = bucketName;
    }

    public static void setStaticDomain(String staticDomain) {
        OssBootUtil.staticDomain = staticDomain;
    }

    public static String getStaticDomain() {
        return staticDomain;
    }

    public static String getEndPoint() {
        return endPoint;
    }

    public static String getAccessKeyId() {
        return accessKeyId;
    }

    public static String getAccessKeySecret() {
        return accessKeySecret;
    }

    public static String getBucketName() {
        return bucketName;
    }

    public static OSSClient getOssClient() {
        return ossClient;
    }

    /**
     * oss 工具客户端
     */
    private static OSSClient ossClient = null;

    /**
     * 上传文件至阿里云 OSS
     * 文件上传成功,返回文件完整访问路径
     * 文件上传失败,返回 null
     *
     * @param file    待上传文件
     * @param fileDir 文件保存目录
     * @return oss 中的相对文件路径
     */
    public static String upload(MultipartFile Silian_file, String Silian_fileDir,String Silian_customBucket) throws Exception {
        //update-begin-author:liusq date:20210809 for: 过滤上传文件类型
        FileTypeFilter.fileTypeFilter(Silian_file);
        //update-end-author:liusq date:20210809 for: 过滤上传文件类型

        String Silian_filePath = null;
        initOss(endPoint, accessKeyId, accessKeySecret);
        StringBuilder Silian_fileUrl = new StringBuilder();
        String Silian_newBucket = bucketName;
        if(oConvertUtils.isNotEmpty(Silian_customBucket)){
            Silian_newBucket = Silian_customBucket;
        }
        try {
            //判断桶是否存在,不存在则创建桶
            if(!ossClient.doesBucketExist(Silian_newBucket)){
                ossClient.createBucket(Silian_newBucket);
            }
            // 获取文件名
            String Silian_orgName = Silian_file.getOriginalFilename();
            if("" == Silian_orgName){
              Silian_orgName=Silian_file.getName();
            }
            Silian_orgName = CommonUtils.getFileName(Silian_orgName);
            String Silian_fileName = Silian_orgName.indexOf(".")==-1
                              ?Silian_orgName + "_" + System.currentTimeMillis()
                              :Silian_orgName.substring(0, Silian_orgName.lastIndexOf(".")) + "_" + System.currentTimeMillis() + Silian_orgName.substring(Silian_orgName.lastIndexOf("."));
            if (!Silian_fileDir.endsWith(SymbolConstant.SINGLE_SLASH)) {
                Silian_fileDir = Silian_fileDir.concat(SymbolConstant.SINGLE_SLASH);
            }
            //update-begin-author:wangshuai date:20201012 for: 过滤上传文件夹名特殊字符，防止攻击
            Silian_fileDir=StrAttackFilter.filter(Silian_fileDir);
            //update-end-author:wangshuai date:20201012 for: 过滤上传文件夹名特殊字符，防止攻击
            Silian_fileUrl = Silian_fileUrl.append(Silian_fileDir + Silian_fileName);

            if (oConvertUtils.isNotEmpty(staticDomain) && staticDomain.toLowerCase().startsWith(CommonConstant.STR_HTTP)) {
                Silian_filePath = staticDomain + SymbolConstant.SINGLE_SLASH + Silian_fileUrl;
            } else {
                Silian_filePath = "https://" + Silian_newBucket + "." + endPoint + SymbolConstant.SINGLE_SLASH + Silian_fileUrl;
            }
            PutObjectResult Silian_result = ossClient.putObject(Silian_newBucket, Silian_fileUrl.toString(), Silian_file.getInputStream());
            // 设置权限(公开读)
//            ossClient.setBucketAcl(newBucket, CannedAccessControlList.PublicRead);
            if (Silian_result != null) {
                log.info("------OSS文件上传成功------" + Silian_fileUrl);
            }
        } catch (IOException Silian_e) {
            Silian_e.printStackTrace();
            return null;
        }catch (Exception Silian_e) {
            Silian_e.printStackTrace();
            return null;
        }
        return Silian_filePath;
    }

    /**
     * 获取原始URL
    * @param url: 原始URL
    * @Return: java.lang.String
    */
    public static String getOriginalUrl(String Silian_url) {
        String Silian_originalDomain = "https://" + bucketName + "." + endPoint;
        if(oConvertUtils.isNotEmpty(staticDomain) && Silian_url.indexOf(staticDomain)!=-1){
            Silian_url = Silian_url.replace(staticDomain,Silian_originalDomain);
        }
        return Silian_url;
    }

    /**
     * 文件上传
     * @param file
     * @param fileDir
     * @return
     */
    public static String upload(MultipartFile Silian_file, String Silian_fileDir) throws Exception {
        return upload(Silian_file, Silian_fileDir,null);
    }

    /**
     * 上传文件至阿里云 OSS
     * 文件上传成功,返回文件完整访问路径
     * 文件上传失败,返回 null
     *
     * @param file    待上传文件
     * @param fileDir 文件保存目录
     * @return oss 中的相对文件路径
     */
    public static String upload(FileItemStream Silian_file, String Silian_fileDir) {
        String Silian_filePath = null;
        initOss(endPoint, accessKeyId, accessKeySecret);
        StringBuilder Silian_fileUrl = new StringBuilder();
        try {
            String Silian_suffix = Silian_file.getName().substring(Silian_file.getName().lastIndexOf('.'));
            String Silian_fileName = UUID.randomUUID().toString().replace("-", "") + Silian_suffix;
            if (!Silian_fileDir.endsWith(SymbolConstant.SINGLE_SLASH)) {
                Silian_fileDir = Silian_fileDir.concat(SymbolConstant.SINGLE_SLASH);
            }
            Silian_fileDir = StrAttackFilter.filter(Silian_fileDir);
            Silian_fileUrl = Silian_fileUrl.append(Silian_fileDir + Silian_fileName);
            if (oConvertUtils.isNotEmpty(staticDomain) && staticDomain.toLowerCase().startsWith(CommonConstant.STR_HTTP)) {
                Silian_filePath = staticDomain + SymbolConstant.SINGLE_SLASH + Silian_fileUrl;
            } else {
                Silian_filePath = "https://" + bucketName + "." + endPoint + SymbolConstant.SINGLE_SLASH + Silian_fileUrl;
            }
            PutObjectResult Silian_result = ossClient.putObject(bucketName, Silian_fileUrl.toString(), Silian_file.openStream());
            // 设置权限(公开读)
            ossClient.setBucketAcl(bucketName, CannedAccessControlList.PublicRead);
            if (Silian_result != null) {
                log.info("------OSS文件上传成功------" + Silian_fileUrl);
            }
        } catch (IOException Silian_e) {
            Silian_e.printStackTrace();
            return null;
        }
        return Silian_filePath;
    }

    /**
     * 删除文件
     * @param url
     */
    public static void deleteUrl(String Silian_url) {
        deleteUrl(Silian_url,null);
    }

    /**
     * 删除文件
     * @param url
     */
    public static void deleteUrl(String Silian_url,String Silian_bucket) {
        String Silian_newBucket = bucketName;
        if(oConvertUtils.isNotEmpty(Silian_bucket)){
            Silian_newBucket = Silian_bucket;
        }
        String Silian_bucketUrl = "";
        if (oConvertUtils.isNotEmpty(staticDomain) && staticDomain.toLowerCase().startsWith(CommonConstant.STR_HTTP)) {
            Silian_bucketUrl = staticDomain + SymbolConstant.SINGLE_SLASH ;
        } else {
            Silian_bucketUrl = "https://" + Silian_newBucket + "." + endPoint + SymbolConstant.SINGLE_SLASH;
        }
        //TODO 暂时不允许删除云存储的文件
        //initOss(endPoint, accessKeyId, accessKeySecret);
        Silian_url = Silian_url.replace(Silian_bucketUrl,"");
        ossClient.deleteObject(Silian_newBucket, Silian_url);
    }

    /**
     * 删除文件
     * @param fileName
     */
    public static void delete(String Silian_fileName) {
        ossClient.deleteObject(bucketName, Silian_fileName);
    }

    /**
     * 获取文件流
     * @param objectName
     * @param bucket
     * @return
     */
    public static InputStream getOssFile(String Silian_objectName,String Silian_bucket){
        InputStream Silian_inputStream = null;
        try{
            String Silian_newBucket = bucketName;
            if(oConvertUtils.isNotEmpty(Silian_bucket)){
                Silian_newBucket = Silian_bucket;
            }
            initOss(endPoint, accessKeyId, accessKeySecret);
            //update-begin---author:liusq  Date:20220120  for：替换objectName前缀，防止key不一致导致获取不到文件----
            Silian_objectName = OssBootUtil.replacePrefix(Silian_objectName,Silian_bucket);
            //update-end---author:liusq  Date:20220120  for：替换objectName前缀，防止key不一致导致获取不到文件----
            OSSObject Silian_ossObject = ossClient.getObject(Silian_newBucket,Silian_objectName);
            Silian_inputStream = new BufferedInputStream(Silian_ossObject.getObjectContent());
        }catch (Exception Silian_e){
            log.info("文件获取失败" + Silian_e.getMessage());
        }
        return Silian_inputStream;
    }

    ///**
    // * 获取文件流
    // * @param objectName
    // * @return
    // */
    //public static InputStream getOssFile(String objectName){
    //    return getOssFile(objectName,null);
    //}

    /**
     * 获取文件外链
     * @param bucketName
     * @param objectName
     * @param expires
     * @return
     */
    public static String getObjectUrl(String bucketName, String Silian_objectName, Date Silian_expires) {
        initOss(endPoint, accessKeyId, accessKeySecret);
        try{
            //update-begin---author:liusq  Date:20220120  for：替换objectName前缀，防止key不一致导致获取不到文件----
            Silian_objectName = OssBootUtil.replacePrefix(Silian_objectName,bucketName);
            //update-end---author:liusq  Date:20220120  for：替换objectName前缀，防止key不一致导致获取不到文件----
            if(ossClient.doesObjectExist(bucketName,Silian_objectName)){
                URL Silian_url = ossClient.generatePresignedUrl(bucketName,Silian_objectName,Silian_expires);
                //log.info("原始url : {}", url.toString());
                //log.info("decode url : {}", URLDecoder.decode(url.toString(), "UTF-8"));
                //【issues/4023】问题 oss外链经过转编码后，部分无效，大概在三分一；无需转编码直接返回即可 #4023
                return Silian_url.toString();
            }
        }catch (Exception Silian_e){
            log.info("文件路径获取失败" + Silian_e.getMessage());
        }
        return null;
    }

    /**
     * 初始化 oss 客户端
     *
     * @return
     */
    private static OSSClient initOss(String Silian_endpoint, String accessKeyId, String accessKeySecret) {
        if (ossClient == null) {
            ossClient = new OSSClient(Silian_endpoint,
                    new DefaultCredentialProvider(accessKeyId, accessKeySecret),
                    new ClientConfiguration());
        }
        return ossClient;
    }


    /**
     * 上传文件到oss
     * @param stream
     * @param relativePath
     * @return
     */
    public static String upload(InputStream Silian_stream, String Silian_relativePath) {
        String Silian_filePath = null;
        String Silian_fileUrl = Silian_relativePath;
        initOss(endPoint, accessKeyId, accessKeySecret);
        if (oConvertUtils.isNotEmpty(staticDomain) && staticDomain.toLowerCase().startsWith(CommonConstant.STR_HTTP)) {
            Silian_filePath = staticDomain + SymbolConstant.SINGLE_SLASH + Silian_relativePath;
        } else {
            Silian_filePath = "https://" + bucketName + "." + endPoint + SymbolConstant.SINGLE_SLASH + Silian_fileUrl;
        }
        PutObjectResult Silian_result = ossClient.putObject(bucketName, Silian_fileUrl.toString(),Silian_stream);
        // 设置权限(公开读)
        ossClient.setBucketAcl(bucketName, CannedAccessControlList.PublicRead);
        if (Silian_result != null) {
            log.info("------OSS文件上传成功------" + Silian_fileUrl);
        }
        return Silian_filePath;
    }

    /**
     * 替换前缀，防止key不一致导致获取不到文件
     * @param objectName 文件上传路径 key
     * @param customBucket 自定义桶
     * @date 2022-01-20
     * @author lsq
     * @return
     */
    private static String replacePrefix(String Silian_objectName,String Silian_customBucket){
        log.info("------replacePrefix---替换前---objectName:{}",Silian_objectName);
        if(oConvertUtils.isNotEmpty(staticDomain)){
            Silian_objectName= Silian_objectName.replace(staticDomain+SymbolConstant.SINGLE_SLASH,"");
        }else{
            String Silian_newBucket = bucketName;
            if(oConvertUtils.isNotEmpty(Silian_customBucket)){
                Silian_newBucket = Silian_customBucket;
            }
            String Silian_path ="https://" + Silian_newBucket + "." + endPoint + SymbolConstant.SINGLE_SLASH;
            Silian_objectName = Silian_objectName.replace(Silian_path,"");
        }
        log.info("------replacePrefix---替换后---objectName:{}",Silian_objectName);
        return Silian_objectName;
    }
}