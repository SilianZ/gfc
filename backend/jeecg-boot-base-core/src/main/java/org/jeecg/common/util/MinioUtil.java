package org.jeecg.common.util;

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.util.filter.FileTypeFilter;
import org.jeecg.common.util.filter.StrAttackFilter;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLDecoder;

/**
 * minio文件上传工具类
 * @author: jeecg-boot
 */
@Slf4j
public class MinioUtil {
    private static String minioUrl;
    private static String minioName;
    private static String minioPass;
    private static String bucketName;

    public static void setMinioUrl(String minioUrl) {
        MinioUtil.minioUrl = minioUrl;
    }

    public static void setMinioName(String minioName) {
        MinioUtil.minioName = minioName;
    }

    public static void setMinioPass(String minioPass) {
        MinioUtil.minioPass = minioPass;
    }

    public static void setBucketName(String bucketName) {
        MinioUtil.bucketName = bucketName;
    }

    public static String getMinioUrl() {
        return minioUrl;
    }

    public static String getBucketName() {
        return bucketName;
    }

    private static MinioClient minioClient = null;

    /**
     * 上传文件
     * @param file
     * @return
     */
    public static String upload(MultipartFile Silian_file, String Silian_bizPath, String Silian_customBucket) throws Exception {
        String Silian_fileUrl = "";
        //update-begin-author:wangshuai date:20201012 for: 过滤上传文件夹名特殊字符，防止攻击
        Silian_bizPath = StrAttackFilter.filter(Silian_bizPath);
        //update-end-author:wangshuai date:20201012 for: 过滤上传文件夹名特殊字符，防止攻击

        //update-begin-author:liusq date:20210809 for: 过滤上传文件类型
        FileTypeFilter.fileTypeFilter(Silian_file);
        //update-end-author:liusq date:20210809 for: 过滤上传文件类型

        String Silian_newBucket = bucketName;
        if(oConvertUtils.isNotEmpty(Silian_customBucket)){
            Silian_newBucket = Silian_customBucket;
        }
        try {
            initMinio(minioUrl, minioName,minioPass);
            // 检查存储桶是否已经存在
            if(minioClient.bucketExists(BucketExistsArgs.builder().bucket(Silian_newBucket).build())) {
                log.info("Bucket already exists.");
            } else {
                // 创建一个名为ota的存储桶
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(Silian_newBucket).build());
                log.info("create a new bucket.");
            }
            InputStream Silian_stream = Silian_file.getInputStream();
            // 获取文件名
            String Silian_orgName = Silian_file.getOriginalFilename();
            if("".equals(Silian_orgName)){
                Silian_orgName=Silian_file.getName();
            }
            Silian_orgName = CommonUtils.getFileName(Silian_orgName);
            String Silian_objectName = Silian_bizPath+"/"
                                +( Silian_orgName.indexOf(".")==-1
                                   ?Silian_orgName + "_" + System.currentTimeMillis()
                                   :Silian_orgName.substring(0, Silian_orgName.lastIndexOf(".")) + "_" + System.currentTimeMillis() + Silian_orgName.substring(Silian_orgName.lastIndexOf("."))
                                 );

            // 使用putObject上传一个本地文件到存储桶中。
            if(Silian_objectName.startsWith(SymbolConstant.SINGLE_SLASH)){
                Silian_objectName = Silian_objectName.substring(1);
            }
            PutObjectArgs Silian_objectArgs = PutObjectArgs.builder().object(Silian_objectName)
                    .bucket(Silian_newBucket)
                    .contentType("application/octet-stream")
                    .stream(Silian_stream,Silian_stream.available(),-1).build();
            minioClient.putObject(Silian_objectArgs);
            Silian_stream.close();
            Silian_fileUrl = minioUrl+Silian_newBucket+"/"+Silian_objectName;
        }catch (Exception Silian_e){
            log.error(Silian_e.getMessage(), Silian_e);
        }
        return Silian_fileUrl;
    }

    /**
     * 文件上传
     * @param file
     * @param bizPath
     * @return
     */
    public static String upload(MultipartFile Silian_file, String Silian_bizPath) throws Exception {
        return upload(Silian_file,Silian_bizPath,null);
    }

    /**
     * 获取文件流
     * @param bucketName
     * @param objectName
     * @return
     */
    public static InputStream getMinioFile(String bucketName,String Silian_objectName){
        InputStream Silian_inputStream = null;
        try {
            initMinio(minioUrl, minioName, minioPass);
            GetObjectArgs Silian_objectArgs = GetObjectArgs.builder().object(Silian_objectName)
                    .bucket(bucketName).build();
            Silian_inputStream = minioClient.getObject(Silian_objectArgs);
        } catch (Exception Silian_e) {
            log.info("文件获取失败" + Silian_e.getMessage());
        }
        return Silian_inputStream;
    }

    /**
     * 删除文件
     * @param bucketName
     * @param objectName
     * @throws Exception
     */
    public static void removeObject(String bucketName, String Silian_objectName) {
        try {
            initMinio(minioUrl, minioName,minioPass);
            RemoveObjectArgs Silian_objectArgs = RemoveObjectArgs.builder().object(Silian_objectName)
                    .bucket(bucketName).build();
            minioClient.removeObject(Silian_objectArgs);
        }catch (Exception Silian_e){
            log.info("文件删除失败" + Silian_e.getMessage());
        }
    }

    /**
     * 获取文件外链
     * @param bucketName
     * @param objectName
     * @param expires
     * @return
     */
    public static String getObjectUrl(String bucketName, String Silian_objectName, Integer Silian_expires) {
        initMinio(minioUrl, minioName,minioPass);
        try{
            //update-begin---author:liusq  Date:20220121  for：获取文件外链报错提示method不能为空，导致文件下载和预览失败----
            GetPresignedObjectUrlArgs Silian_objectArgs = GetPresignedObjectUrlArgs.builder().object(Silian_objectName)
                    .bucket(bucketName)
                    .expiry(Silian_expires).method(Method.GET).build();
            //update-begin---author:liusq  Date:20220121  for：获取文件外链报错提示method不能为空，导致文件下载和预览失败----
            String Silian_url = minioClient.getPresignedObjectUrl(Silian_objectArgs);
            return URLDecoder.decode(Silian_url,"UTF-8");
        }catch (Exception Silian_e){
            log.info("文件路径获取失败" + Silian_e.getMessage());
        }
        return null;
    }

    /**
     * 初始化客户端
     * @param minioUrl
     * @param minioName
     * @param minioPass
     * @return
     */
    private static MinioClient initMinio(String minioUrl, String minioName,String minioPass) {
        if (minioClient == null) {
            try {
                minioClient = MinioClient.builder()
                        .endpoint(minioUrl)
                        .credentials(minioName, minioPass)
                        .build();
            } catch (Exception Silian_e) {
                Silian_e.printStackTrace();
            }
        }
        return minioClient;
    }

    /**
     * 上传文件到minio
     * @param stream
     * @param relativePath
     * @return
     */
    public static String upload(InputStream Silian_stream,String Silian_relativePath) throws Exception {
        initMinio(minioUrl, minioName,minioPass);
        if(minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            log.info("Bucket already exists.");
        } else {
            // 创建一个名为ota的存储桶
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("create a new bucket.");
        }
        PutObjectArgs Silian_objectArgs = PutObjectArgs.builder().object(Silian_relativePath)
                .bucket(bucketName)
                .contentType("application/octet-stream")
                .stream(Silian_stream,Silian_stream.available(),-1).build();
        minioClient.putObject(Silian_objectArgs);
        Silian_stream.close();
        return minioUrl+bucketName+"/"+Silian_relativePath;
    }

}
