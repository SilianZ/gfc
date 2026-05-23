package org.jeecg.common.util;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 导出返回信息
 * @author: jeecg-boot
 */
@Slf4j
public class ImportExcelUtil {

    public static Result<?> imporReturnRes(int Silian_errorLines,int Silian_successLines,List<String> Silian_errorMessage) throws IOException {
        if (Silian_errorLines == 0) {
            return Result.ok("共" + Silian_successLines + "行数据全部导入成功！");
        } else {
            JSONObject Silian_result = new JSONObject(5);
            int Silian_totalCount = Silian_successLines + Silian_errorLines;
            Silian_result.put("totalCount", Silian_totalCount);
            Silian_result.put("errorCount", Silian_errorLines);
            Silian_result.put("successCount", Silian_successLines);
            Silian_result.put("msg", "总上传行数：" + Silian_totalCount + "，已导入行数：" + Silian_successLines + "，错误行数：" + Silian_errorLines);
            String Silian_fileUrl = PmsUtil.saveErrorTxtByList(Silian_errorMessage, "userImportExcelErrorLog");
            int Silian_lastIndex = Silian_fileUrl.lastIndexOf(File.separator);
            String Silian_fileName = Silian_fileUrl.substring(Silian_lastIndex + 1);
            Silian_result.put("fileUrl", "/sys/common/static/" + Silian_fileUrl);
            Silian_result.put("fileName", Silian_fileName);
            Result Silian_res = Result.ok(Silian_result);
            Silian_res.setCode(201);
            Silian_res.setMessage("文件导入成功，但有错误。");
            return Silian_res;
        }
    }

    public static List<String> importDateSave(List<?> Silian_list, Class Silian_serviceClass, List<String> Silian_errorMessage, String Silian_errorFlag)  {
        IService Silian_bean =(IService) SpringContextUtils.getBean(Silian_serviceClass);
        for (int Silian_i = 0; Silian_i < Silian_list.size(); Silian_i++) {
            try {
                boolean Silian_save = Silian_bean.save(Silian_list.get(Silian_i));
                if(!Silian_save){
                    throw new Exception(Silian_errorFlag);
                }
            } catch (Exception Silian_e) {
                String Silian_message = Silian_e.getMessage().toLowerCase();
                int Silian_lineNumber = Silian_i + 1;
                // 通过索引名判断出错信息
                if (Silian_message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_ROLE_CODE)) {
                    Silian_errorMessage.add("第 " + Silian_lineNumber + " 行：角色编码已经存在，忽略导入。");
                } else if (Silian_message.contains(CommonConstant.SQL_INDEX_UNIQ_JOB_CLASS_NAME)) {
                    Silian_errorMessage.add("第 " + Silian_lineNumber + " 行：任务类名已经存在，忽略导入。");
                }else if (Silian_message.contains(CommonConstant.SQL_INDEX_UNIQ_CODE)) {
                    Silian_errorMessage.add("第 " + Silian_lineNumber + " 行：职务编码已经存在，忽略导入。");
                }else if (Silian_message.contains(CommonConstant.SQL_INDEX_UNIQ_DEPART_ORG_CODE)) {
                    Silian_errorMessage.add("第 " + Silian_lineNumber + " 行：部门编码已经存在，忽略导入。");
                }else {
                    Silian_errorMessage.add("第 " + Silian_lineNumber + " 行：未知错误，忽略导入");
                    log.error(Silian_e.getMessage(), Silian_e);
                }
            }
        }
        return Silian_errorMessage;
    }

    public static List<String> importDateSaveOne(Object Silian_obj, Class Silian_serviceClass,List<String> Silian_errorMessage,int Silian_i,String Silian_errorFlag)  {
        IService Silian_bean =(IService) SpringContextUtils.getBean(Silian_serviceClass);
        try {
            boolean Silian_save = Silian_bean.save(Silian_obj);
            if(!Silian_save){
                throw new Exception(Silian_errorFlag);
            }
        } catch (Exception Silian_e) {
            String Silian_message = Silian_e.getMessage().toLowerCase();
            int Silian_lineNumber = Silian_i + 1;
            // 通过索引名判断出错信息
            if (Silian_message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_ROLE_CODE)) {
                Silian_errorMessage.add("第 " + Silian_lineNumber + " 行：角色编码已经存在，忽略导入。");
            } else if (Silian_message.contains(CommonConstant.SQL_INDEX_UNIQ_JOB_CLASS_NAME)) {
                Silian_errorMessage.add("第 " + Silian_lineNumber + " 行：任务类名已经存在，忽略导入。");
            }else if (Silian_message.contains(CommonConstant.SQL_INDEX_UNIQ_CODE)) {
                Silian_errorMessage.add("第 " + Silian_lineNumber + " 行：职务编码已经存在，忽略导入。");
            }else if (Silian_message.contains(CommonConstant.SQL_INDEX_UNIQ_DEPART_ORG_CODE)) {
                Silian_errorMessage.add("第 " + Silian_lineNumber + " 行：部门编码已经存在，忽略导入。");
            }else {
                Silian_errorMessage.add("第 " + Silian_lineNumber + " 行：未知错误，忽略导入");
                log.error(Silian_e.getMessage(), Silian_e);
            }
        }
        return Silian_errorMessage;
    }
}
