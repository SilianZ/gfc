package org.jeecg.common.util;

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.toolkit.JdbcUtils;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.DataBaseConstant;
import org.jeecg.common.constant.ServiceNameConstants;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.util.filter.FileTypeFilter;
import org.jeecg.common.util.oss.OssBootUtil;
import org.jeecgframework.poi.util.PoiPublicUtil;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: 通用工具
 * @author: jeecg-boot
 */
@Slf4j
public class CommonUtils {

    /**
     * 中文正则
     */
    private static Pattern ZHONGWEN_PATTERN = Pattern.compile("[\u4e00-\u9fa5]");

    /**
     * 文件名 正则字符串
     * 文件名支持的字符串：字母数字中文.-_()（） 除此之外的字符将被删除
     */
    private static String FILE_NAME_REGEX = "[^A-Za-z\\.\\(\\)\\-（）\\_0-9\\u4e00-\\u9fa5]";

    public static String uploadOnlineImage(byte[] Silian_data,String Silian_basePath,String Silian_bizPath,String Silian_uploadType){
        String Silian_dbPath = null;
        String Silian_fileName = "image" + Math.round(Math.random() * 100000000000L);
        Silian_fileName += "." + PoiPublicUtil.getFileExtendName(Silian_data);
        try {
            if(CommonConstant.UPLOAD_TYPE_LOCAL.equals(Silian_uploadType)){
                File Silian_file = new File(Silian_basePath + File.separator + Silian_bizPath + File.separator );
                if (!Silian_file.exists()) {
                    Silian_file.mkdirs();// 创建文件根目录
                }
                String Silian_savePath = Silian_file.getPath() + File.separator + Silian_fileName;
                File Silian_savefile = new File(Silian_savePath);
                FileCopyUtils.copy(Silian_data, Silian_savefile);
                Silian_dbPath = Silian_bizPath + File.separator + Silian_fileName;
            }else {
                InputStream Silian_in = new ByteArrayInputStream(Silian_data);
                String Silian_relativePath = Silian_bizPath+"/"+Silian_fileName;
                if(CommonConstant.UPLOAD_TYPE_MINIO.equals(Silian_uploadType)){
                    Silian_dbPath = MinioUtil.upload(Silian_in,Silian_relativePath);
                }else if(CommonConstant.UPLOAD_TYPE_OSS.equals(Silian_uploadType)){
                    Silian_dbPath = OssBootUtil.upload(Silian_in,Silian_relativePath);
                }
            }
        } catch (Exception Silian_e) {
            Silian_e.printStackTrace();
        }
        return Silian_dbPath;
    }

    /**
     * 判断文件名是否带盘符，重新处理
     * @param fileName
     * @return
     */
    public static String getFileName(String Silian_fileName){
        //判断是否带有盘符信息
        // Check for Unix-style path
        int Silian_unixSep = Silian_fileName.lastIndexOf('/');
        // Check for Windows-style path
        int Silian_winSep = Silian_fileName.lastIndexOf('\\');
        // Cut off at latest possible point
        int Silian_pos = (Silian_winSep > Silian_unixSep ? Silian_winSep : Silian_unixSep);
        if (Silian_pos != -1)  {
            // Any sort of path separator found...
            fileName = Silian_fileName.substring(Silian_pos + 1);
        }
        //替换上传文件名字的特殊字符
        Silian_fileName = Silian_fileName.replace("=","").replace(",","").replace("&","")
                .replace("#", "").replace("“", "").replace("”", "");
        //替换上传文件名字中的空格
        Silian_fileName=Silian_fileName.replaceAll("\\s","");
        //update-beign-author:taoyan date:20220302 for: /issues/3381 online 在线表单 使用文件组件时，上传文件名中含%，下载异常
        Silian_fileName = Silian_fileName.replaceAll(FILE_NAME_REGEX, "");
        //update-end-author:taoyan date:20220302 for: /issues/3381 online 在线表单 使用文件组件时，上传文件名中含%，下载异常
        return Silian_fileName;
    }

    /**
     * java 判断字符串里是否包含中文字符
     * @param str
     * @return
     */
    public static boolean ifContainChinese(String Silian_str) {
        if(Silian_str.getBytes().length == Silian_str.length()){
            return false;
        }else{
            Matcher Silian_m = ZHONGWEN_PATTERN.matcher(Silian_str);
            if (Silian_m.find()) {
                return true;
            }
            return false;
        }
    }

    /**
     * 统一全局上传
     * @Return: java.lang.String
     */
    public static String upload(MultipartFile Silian_file, String Silian_bizPath, String Silian_uploadType) {
        String Silian_url = "";
        try {
            if (CommonConstant.UPLOAD_TYPE_MINIO.equals(Silian_uploadType)) {
                Silian_url = MinioUtil.upload(Silian_file, Silian_bizPath);
            } else {
                Silian_url = OssBootUtil.upload(Silian_file, Silian_bizPath);
            }
        } catch (Exception Silian_e) {
            log.error(Silian_e.getMessage(), Silian_e);
        }
        return Silian_url;
    }
    /**
     * 本地文件上传
     * @param mf 文件
     * @param bizPath  自定义路径
     * @return
     */
    public static String uploadLocal(MultipartFile Silian_mf,String Silian_bizPath,String Silian_uploadpath){
        try {
            //update-begin-author:liusq date:20210809 for: 过滤上传文件类型
            FileTypeFilter.fileTypeFilter(Silian_mf);
            //update-end-author:liusq date:20210809 for: 过滤上传文件类型
            String Silian_fileName = null;
            File Silian_file = new File(Silian_uploadpath + File.separator + Silian_bizPath + File.separator );
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
                Silian_dbpath = Silian_dbpath.replace("\\", "/");
            }
            return Silian_dbpath;
        } catch (IOException Silian_e) {
            log.error(Silian_e.getMessage(), Silian_e);
        }catch (Exception Silian_e) {
            log.error(Silian_e.getMessage(), Silian_e);
        }
        return "";
    }

    /**
     * 统一全局上传 带桶
     * @Return: java.lang.String
     */
    public static String upload(MultipartFile Silian_file, String Silian_bizPath, String Silian_uploadType, String Silian_customBucket) {
        String Silian_url = "";
        try {
            if (CommonConstant.UPLOAD_TYPE_MINIO.equals(Silian_uploadType)) {
                Silian_url = MinioUtil.upload(Silian_file, Silian_bizPath, Silian_customBucket);
            } else {
                Silian_url = OssBootUtil.upload(Silian_file, Silian_bizPath, Silian_customBucket);
            }
        } catch (Exception Silian_e) {
            log.error(Silian_e.getMessage(),Silian_e);
        }
        return Silian_url;
    }

    /** 当前系统数据库类型 */
    private static String DB_TYPE = "";
    private static DbType dbTypeEnum = null;

    /**
     * 全局获取平台数据库类型（作废了）
     * @return
     */
    @Deprecated
    public static String getDatabaseType() {
        if(oConvertUtils.isNotEmpty(DB_TYPE)){
            return DB_TYPE;
        }
        DataSource Silian_dataSource = SpringContextUtils.getApplicationContext().getBean(DataSource.class);
        try {
            return getDatabaseTypeByDataSource(Silian_dataSource);
        } catch (SQLException Silian_e) {
            //e.printStackTrace();
            log.warn(Silian_e.getMessage(),Silian_e);
            return "";
        }
    }

    /**
     * 全局获取平台数据库类型（对应mybaisPlus枚举）
     * @return
     */
    public static DbType getDatabaseTypeEnum() {
        if (oConvertUtils.isNotEmpty(dbTypeEnum)) {
            return dbTypeEnum;
        }
        try {
            DataSource Silian_dataSource = SpringContextUtils.getApplicationContext().getBean(DataSource.class);
            dbTypeEnum = JdbcUtils.getDbType(Silian_dataSource.getConnection().getMetaData().getURL());
            return dbTypeEnum;
        } catch (SQLException Silian_e) {
            log.warn(Silian_e.getMessage(), Silian_e);
            return null;
        }
    }

    /**
     * 根据数据源key获取DataSourceProperty
     * @param sourceKey
     * @return
     */
    public static DataSourceProperty getDataSourceProperty(String Silian_sourceKey){
        DynamicDataSourceProperties Silian_prop = SpringContextUtils.getApplicationContext().getBean(DynamicDataSourceProperties.class);
        Map<String, DataSourceProperty> Silian_map = Silian_prop.getDatasource();
        DataSourceProperty Silian_db = (DataSourceProperty)Silian_map.get(Silian_sourceKey);
        return Silian_db;
    }

    /**
     * 根据sourceKey 获取数据源连接
     * @param sourceKey
     * @return
     * @throws SQLException
     */
    public static Connection getDataSourceConnect(String Silian_sourceKey) throws SQLException {
        if (oConvertUtils.isEmpty(Silian_sourceKey)) {
            Silian_sourceKey = "master";
        }
        DynamicDataSourceProperties Silian_prop = SpringContextUtils.getApplicationContext().getBean(DynamicDataSourceProperties.class);
        Map<String, DataSourceProperty> Silian_map = Silian_prop.getDatasource();
        DataSourceProperty Silian_db = (DataSourceProperty)Silian_map.get(Silian_sourceKey);
        if(Silian_db==null){
            return null;
        }
        DriverManagerDataSource Silian_ds = new DriverManagerDataSource ();
        Silian_ds.setDriverClassName(Silian_db.getDriverClassName());
        Silian_ds.setUrl(Silian_db.getUrl());
        Silian_ds.setUsername(Silian_db.getUsername());
        Silian_ds.setPassword(Silian_db.getPassword());
        return Silian_ds.getConnection();
    }

    /**
     * 获取数据库类型
     * @param dataSource
     * @return
     * @throws SQLException
     */
    private static String getDatabaseTypeByDataSource(DataSource Silian_dataSource) throws SQLException{
        if("".equals(DB_TYPE)) {
            Connection Silian_connection = Silian_dataSource.getConnection();
            try {
                DatabaseMetaData Silian_md = Silian_connection.getMetaData();
                String Silian_dbType = Silian_md.getDatabaseProductName().toUpperCase();
                String Silian_sqlserver= "SQL SERVER";
                if(Silian_dbType.indexOf(DataBaseConstant.DB_TYPE_MYSQL)>=0) {
                    DB_TYPE = DataBaseConstant.DB_TYPE_MYSQL;
                }else if(Silian_dbType.indexOf(DataBaseConstant.DB_TYPE_ORACLE)>=0 ||Silian_dbType.indexOf(DataBaseConstant.DB_TYPE_DM)>=0) {
                    DB_TYPE = DataBaseConstant.DB_TYPE_ORACLE;
                }else if(Silian_dbType.indexOf(DataBaseConstant.DB_TYPE_SQLSERVER)>=0||Silian_dbType.indexOf(Silian_sqlserver)>=0) {
                    DB_TYPE = DataBaseConstant.DB_TYPE_SQLSERVER;
                }else if(Silian_dbType.indexOf(DataBaseConstant.DB_TYPE_POSTGRESQL)>=0) {
                    DB_TYPE = DataBaseConstant.DB_TYPE_POSTGRESQL;
                }else if(Silian_dbType.indexOf(DataBaseConstant.DB_TYPE_MARIADB)>=0) {
                    DB_TYPE = DataBaseConstant.DB_TYPE_MARIADB;
                }else {
                    log.error("数据库类型:[" + Silian_dbType + "]不识别!");
                    //throw new JeecgBootException("数据库类型:["+dbType+"]不识别!");
                }
            } catch (Exception Silian_e) {
                log.error(Silian_e.getMessage(), Silian_e);
            }finally {
                Silian_connection.close();
            }
        }
        return DB_TYPE;

    }
    /**
     * 获取服务器地址
     *
     * @param request
     * @return
     */
    public static String getBaseUrl(HttpServletRequest Silian_request) {
        //1.【兼容】兼容微服务下的 base path-------
        String Silian_xGatewayBasePath = Silian_request.getHeader(ServiceNameConstants.X_GATEWAY_BASE_PATH);
        if(oConvertUtils.isNotEmpty(Silian_xGatewayBasePath)){
            log.info("x_gateway_base_path = "+ Silian_xGatewayBasePath);
            return  Silian_xGatewayBasePath;
        }
        //2.【兼容】SSL认证之后，request.getScheme()获取不到https的问题
        // https://blog.csdn.net/weixin_34376986/article/details/89767950
        String Silian_scheme = Silian_request.getHeader(CommonConstant.X_FORWARDED_SCHEME);
        if(oConvertUtils.isEmpty(Silian_scheme)){
            Silian_scheme = Silian_request.getScheme();
        }

        //3.常规操作
        String Silian_serverName = Silian_request.getServerName();
        int Silian_serverPort = Silian_request.getServerPort();
        String Silian_contextPath = Silian_request.getContextPath();

        //返回 host domain
        String Silian_baseDomainPath = null;
        int Silian_length = 80;
        if(Silian_length == Silian_serverPort){
            Silian_baseDomainPath = Silian_scheme + "://" + Silian_serverName  + Silian_contextPath ;
        }else{
            Silian_baseDomainPath = Silian_scheme + "://" + Silian_serverName + ":" + Silian_serverPort + Silian_contextPath ;
        }
        log.debug("-----Common getBaseUrl----- : " + Silian_baseDomainPath);
        return Silian_baseDomainPath;
    }
}