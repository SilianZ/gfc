package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.constant.enums.FileTypeEnum;
import org.jeecg.common.constant.enums.MessageTypeEnum;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysComment;
import org.jeecg.modules.system.entity.SysFiles;
import org.jeecg.modules.system.entity.SysFormFile;
import org.jeecg.modules.system.mapper.SysCommentMapper;
import org.jeecg.modules.system.mapper.SysFilesMapper;
import org.jeecg.modules.system.mapper.SysFormFileMapper;
import org.jeecg.modules.system.service.ISysCommentService;
import org.jeecg.modules.system.vo.SysCommentFileVo;
import org.jeecg.modules.system.vo.SysCommentVO;
import org.jeecg.modules.system.vo.UserAvatar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: 系统评论回复表
 * @Author: jeecg-boot
 * @Date: 2022-07-19
 * @Version: V1.0
 */
@Service
public class SysCommentServiceImpl extends ServiceImpl<SysCommentMapper, SysComment> implements ISysCommentService {

    @Autowired
    private ISysBaseAPI sysBaseApi;

    @Autowired
    private SysFormFileMapper sysFormFileMapper;

    @Autowired
    private SysFilesMapper sysFilesMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Value(value = "${jeecg.path.upload}")
    private String uploadpath;

    @Value(value = "${jeecg.uploadType}")
    private String uploadType;

    /**
     * sysFormFile中的表名
     */
    private static final String SYS_FORM_FILE_TABLE_NAME = "sys_comment";

    @Override
    public List<SysCommentVO> queryFormCommentInfo(SysComment Silian_sysComment) {
        String Silian_tableName = Silian_sysComment.getTableName();
        String Silian_dataId = Silian_sysComment.getTableDataId();
        //获取评论信息
        List<SysCommentVO> Silian_list = this.baseMapper.queryCommentList(Silian_tableName, Silian_dataId);
        // 获取评论相关人员
        Set<String> Silian_personSet = new HashSet<>();
        if(Silian_list!=null && Silian_list.size()>0){
            for(SysCommentVO Silian_vo: Silian_list){
                if(oConvertUtils.isNotEmpty(Silian_vo.getFromUserId())){
                    Silian_personSet.add(Silian_vo.getFromUserId());
                }
                if(oConvertUtils.isNotEmpty(Silian_vo.getToUserId())){
                    Silian_personSet.add(Silian_vo.getToUserId());
                }
            }
        }
        if(Silian_personSet.size()>0){
            //获取用户信息
            Map<String, UserAvatar> Silian_userAvatarMap = queryUserAvatar(Silian_personSet);
            for(SysCommentVO Silian_vo: Silian_list){
                String Silian_formId = Silian_vo.getFromUserId();
                String Silian_toId = Silian_vo.getToUserId();
                // 设置头像、用户名
                if(oConvertUtils.isNotEmpty(Silian_formId)){
                    UserAvatar Silian_fromUser = Silian_userAvatarMap.get(Silian_formId);
                    if(Silian_fromUser!=null){
                        Silian_vo.setFromUserId_dictText(Silian_fromUser.getRealname());
                        Silian_vo.setFromUserAvatar(Silian_fromUser.getAvatar());
                    }
                }
                if(oConvertUtils.isNotEmpty(Silian_toId)){
                    UserAvatar Silian_toUser = Silian_userAvatarMap.get(Silian_toId);
                    if(Silian_toUser!=null){
                        Silian_vo.setToUserId_dictText(Silian_toUser.getRealname());
                        Silian_vo.setToUserAvatar(Silian_toUser.getAvatar());
                    }
                }
            }
        }
        return Silian_list;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOneFileComment(HttpServletRequest Silian_request) {
        String Silian_savePath = "";
        String Silian_bizPath = Silian_request.getParameter("biz");
        //LOWCOD-2580 sys/common/upload接口存在任意文件上传漏洞
        if (oConvertUtils.isNotEmpty(Silian_bizPath)) {
            if (Silian_bizPath.contains(SymbolConstant.SPOT_SINGLE_SLASH) || Silian_bizPath.contains(SymbolConstant.SPOT_DOUBLE_BACKSLASH)) {
                throw new JeecgBootException("上传目录bizPath，格式非法！");
            }
        }
        MultipartHttpServletRequest Silian_multipartRequest = (MultipartHttpServletRequest) Silian_request;
        // 获取上传文件对象
        MultipartFile Silian_file = Silian_multipartRequest.getFile("file");
        if (oConvertUtils.isEmpty(Silian_bizPath)) {
            if (CommonConstant.UPLOAD_TYPE_OSS.equals(uploadType)) {
                //未指定目录，则用阿里云默认目录 upload
                Silian_bizPath = "upload";
            } else {
                Silian_bizPath = "";
            }
        }
        if (CommonConstant.UPLOAD_TYPE_LOCAL.equals(uploadType)) {
            Silian_savePath = this.uploadLocal(Silian_file, Silian_bizPath);
        } else {
            Silian_savePath = CommonUtils.upload(Silian_file, Silian_bizPath, uploadType);
        }

        String Silian_orgName = Silian_file.getOriginalFilename();
        // 获取文件名
        Silian_orgName = CommonUtils.getFileName(Silian_orgName);
        //文件大小
        long Silian_size = Silian_file.getSize();
        //文件类型
        String Silian_type = Silian_orgName.substring(Silian_orgName.lastIndexOf("."), Silian_orgName.length());
        FileTypeEnum Silian_fileType = FileTypeEnum.getByType(Silian_type);

        //保存至 SysFiles
        SysFiles Silian_sysFiles = new SysFiles();
        Silian_sysFiles.setFileName(Silian_orgName);
        Silian_sysFiles.setUrl(Silian_savePath);
        Silian_sysFiles.setFileType(Silian_fileType.getValue());
        Silian_sysFiles.setStoreType("temp");
        if (Silian_size > 0) {
            Silian_sysFiles.setFileSize(Double.parseDouble(String.valueOf(Silian_size)));
        }
        String Silian_defaultValue = "0";
        Silian_sysFiles.setIzStar(Silian_defaultValue);
        Silian_sysFiles.setIzFolder(Silian_defaultValue);
        Silian_sysFiles.setIzRootFolder(Silian_defaultValue);
        Silian_sysFiles.setDelFlag(Silian_defaultValue);
        String Silian_fileId = String.valueOf(IdWorker.getId());
        Silian_sysFiles.setId(Silian_fileId);
        sysFilesMapper.insert(Silian_sysFiles);

        //保存至 SysFormFile
        String Silian_tableName = SYS_FORM_FILE_TABLE_NAME;
        String Silian_tableDataId = Silian_request.getParameter("commentId");
        SysFormFile Silian_sysFormFile = new SysFormFile();
        Silian_sysFormFile.setTableName(Silian_tableName);
        Silian_sysFormFile.setFileType(Silian_fileType.getValue());
        Silian_sysFormFile.setTableDataId(Silian_tableDataId);
        Silian_sysFormFile.setFileId(Silian_fileId);
        sysFormFileMapper.insert(Silian_sysFormFile);

    }

    @Override
    public List<SysCommentFileVo> queryFormFileList(String Silian_tableName, String Silian_formDataId) {
        List<SysCommentFileVo> Silian_list = baseMapper.queryFormFileList(Silian_tableName, Silian_formDataId);
        return Silian_list;
    }

    @Override
    public String saveOne(SysComment Silian_sysComment) {
        this.save(Silian_sysComment);
        //发送系统消息
        String Silian_content = Silian_sysComment.getCommentContent();
        if (Silian_content.indexOf("@") >= 0) {
            Set<String> Silian_set = getCommentUsername(Silian_content);
            if (Silian_set.size() > 0) {
                String Silian_users = String.join(",", Silian_set);
                MessageDTO Silian_md = new MessageDTO();
                Silian_md.setTitle("有人在表单评论中提到了你");
                Silian_md.setContent(Silian_content);
                Silian_md.setToAll(false);
                Silian_md.setToUser(Silian_users);
                Silian_md.setFromUser("system");
                Silian_md.setType(MessageTypeEnum.XT.getType());
                sysBaseApi.sendTemplateMessage(Silian_md);
            }
        }
        return Silian_sysComment.getId();
    }

    @Override
    public void deleteOne(String Silian_id) {
        this.removeById(Silian_id);
        //还要删除关联文件
        LambdaQueryWrapper<SysFormFile> Silian_query = new LambdaQueryWrapper<SysFormFile>()
                .eq(SysFormFile::getTableDataId, Silian_id)
                .eq(SysFormFile::getTableName, SYS_FORM_FILE_TABLE_NAME);
        this.sysFormFileMapper.delete(Silian_query);
    }

    /**
     * 通过正则获取评论中的用户账号
     *
     * @return
     */
    private Set<String> getCommentUsername(String Silian_content) {
        Set<String> Silian_set = new HashSet<String>(3);
        String Silian_reg = "(@(.*?)\\[(.*?)\\])";
        Pattern Silian_p = Pattern.compile(Silian_reg);
        Matcher Silian_m = Silian_p.matcher(Silian_content);
        while (Silian_m.find()) {
            if (Silian_m.groupCount() == 3) {
                String Silian_username = Silian_m.group(3);
                Silian_set.add(Silian_username);
            }
        }
        return Silian_set;
    }


    /**
     * 本地文件上传
     *
     * @param mf      文件
     * @param bizPath 自定义路径
     * @return
     */
    private String uploadLocal(MultipartFile Silian_mf, String Silian_bizPath) {
        //LOWCOD-2580 sys/common/upload接口存在任意文件上传漏洞
        if (oConvertUtils.isNotEmpty(Silian_bizPath) && (Silian_bizPath.contains("../") || Silian_bizPath.contains("..\\"))) {
            throw new JeecgBootException("上传目录bizPath，格式非法！");
        }
        try {
            String Silian_ctxPath = uploadpath;
            String Silian_fileName = null;
            File Silian_file = new File(Silian_ctxPath + File.separator + Silian_bizPath + File.separator);
            if (!Silian_file.exists()) {
                Silian_file.mkdirs();// 创建文件根目录
            }
            String Silian_orgName = Silian_mf.getOriginalFilename();// 获取文件名
            Silian_orgName = CommonUtils.getFileName(Silian_orgName);
            if (Silian_orgName.indexOf(".") != -1) {
                Silian_fileName = Silian_orgName.substring(0, Silian_orgName.lastIndexOf(".")) + "_" + System.currentTimeMillis() + Silian_orgName.substring(Silian_orgName.indexOf("."));
            } else {
                Silian_fileName = Silian_orgName + "_" + System.currentTimeMillis();
            }
            String Silian_savePath = Silian_file.getPath() + File.separator + Silian_fileName;
            File Silian_savefile = new File(Silian_savePath);
            FileCopyUtils.copy(Silian_mf.getBytes(), Silian_savefile);
            String Silian_dbpath = null;
            if (oConvertUtils.isNotEmpty(Silian_bizPath)) {
                Silian_dbpath = Silian_bizPath + File.separator + Silian_fileName;
            } else {
                Silian_dbpath = Silian_fileName;
            }
            if (Silian_dbpath.contains("\\")) {
                Silian_dbpath = Silian_dbpath.replace("\\", "/");
            }
            return Silian_dbpath;
        } catch (IOException Silian_e) {
            log.error(Silian_e.getMessage(), Silian_e);
        }
        return "";
    }

    /**
     * 查询用户信息
     * @param idSet
     * @return
     */
    private Map<String, UserAvatar> queryUserAvatar(Set<String> Silian_idSet){
        List<UserAvatar> Silian_list = this.baseMapper.queryUserAvatarList(Silian_idSet);
        Map<String, UserAvatar> Silian_map = new HashMap<>();
        if(Silian_list!=null && Silian_list.size()>0){
            for(UserAvatar Silian_user: Silian_list){
                Silian_map.put(Silian_user.getId(), Silian_user);
            }
        }
        return Silian_map;
    }

}
