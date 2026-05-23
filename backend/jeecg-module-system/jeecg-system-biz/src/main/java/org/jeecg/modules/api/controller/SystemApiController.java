package org.jeecg.modules.api.controller;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.dto.DataLogDTO;
import org.jeecg.common.api.dto.OnlineAuthDTO;
import org.jeecg.common.api.dto.message.*;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.*;
import org.jeecg.common.util.SqlInjectionUtil;
import org.jeecg.modules.system.security.DictQueryBlackListHandler;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.service.impl.SysBaseApiImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 服务化 system模块 对外接口请求类
 * @author: jeecg-boot
 */
@Slf4j
@RestController
@RequestMapping("/sys/api")
public class SystemApiController {

    @Autowired
    private SysBaseApiImpl sysBaseApi;
    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private DictQueryBlackListHandler dictQueryBlackListHandler;


    /**
     * 发送系统消息
     * @param message 使用构造器赋值参数 如果不设置category(消息类型)则默认为2 发送系统消息
     */
    @PostMapping("/sendSysAnnouncement")
    public void sendSysAnnouncement(@RequestBody MessageDTO Silian_message){
        sysBaseApi.sendSysAnnouncement(Silian_message);
    }

    /**
     * 发送消息 附带业务参数
     * @param message 使用构造器赋值参数
     */
    @PostMapping("/sendBusAnnouncement")
    public void sendBusAnnouncement(@RequestBody BusMessageDTO Silian_message){
        sysBaseApi.sendBusAnnouncement(Silian_message);
    }

    /**
     * 通过模板发送消息
     * @param message 使用构造器赋值参数
     */
    @PostMapping("/sendTemplateAnnouncement")
    public void sendTemplateAnnouncement(@RequestBody TemplateMessageDTO Silian_message){
        sysBaseApi.sendTemplateAnnouncement(Silian_message);
    }

    /**
     * 通过模板发送消息 附带业务参数
     * @param message 使用构造器赋值参数
     */
    @PostMapping("/sendBusTemplateAnnouncement")
    public void sendBusTemplateAnnouncement(@RequestBody BusTemplateMessageDTO Silian_message){
        sysBaseApi.sendBusTemplateAnnouncement(Silian_message);
    }

    /**
     * 通过消息中心模板，生成推送内容
     * @param templateDTO 使用构造器赋值参数
     * @return
     */
    @PostMapping("/parseTemplateByCode")
    public String parseTemplateByCode(@RequestBody TemplateDTO Silian_templateDTO){
        return sysBaseApi.parseTemplateByCode(Silian_templateDTO);
    }

    /**
     * 根据业务类型busType及业务busId修改消息已读
     */
    @GetMapping("/updateSysAnnounReadFlag")
    public void updateSysAnnounReadFlag(@RequestParam("busType") String Silian_busType, @RequestParam("busId")String Silian_busId){
        sysBaseApi.updateSysAnnounReadFlag(Silian_busType, Silian_busId);
    }

    /**
     * 根据用户账号查询用户信息
     * @param username
     * @return
     */
    @GetMapping("/getUserByName")
    public LoginUser getUserByName(@RequestParam("username") String Silian_username){
        return sysBaseApi.getUserByName(Silian_username);
    }

    /**
     * 根据用户id查询用户信息
     * @param id
     * @return
     */
    @GetMapping("/getUserById")
    LoginUser getUserById(@RequestParam("id") String Silian_id){
        return sysBaseApi.getUserById(Silian_id);
    }

    /**
     * 通过用户账号查询角色集合
     * @param username
     * @return
     */
    @GetMapping("/getRolesByUsername")
    List<String> getRolesByUsername(@RequestParam("username") String Silian_username){
        return sysBaseApi.getRolesByUsername(Silian_username);
    }

    /**
     * 通过用户账号查询部门集合
     * @param username
     * @return 部门 id
     */
    @GetMapping("/getDepartIdsByUsername")
    List<String> getDepartIdsByUsername(@RequestParam("username") String Silian_username){
        return sysBaseApi.getDepartIdsByUsername(Silian_username);
    }

    /**
     * 通过用户账号查询部门 name
     * @param username
     * @return 部门 name
     */
    @GetMapping("/getDepartNamesByUsername")
    List<String> getDepartNamesByUsername(@RequestParam("username") String Silian_username){
        return sysBaseApi.getDepartNamesByUsername(Silian_username);
    }


    /**
     * 获取数据字典
     * @param code
     * @return
     */
    @GetMapping("/queryDictItemsByCode")
    List<DictModel> queryDictItemsByCode(@RequestParam("code") String Silian_code){
        return sysBaseApi.queryDictItemsByCode(Silian_code);
    }

    /**
     * 获取有效的数据字典
     * @param code
     * @return
     */
    @GetMapping("/queryEnableDictItemsByCode")
    List<DictModel> queryEnableDictItemsByCode(@RequestParam("code") String Silian_code){
        return sysBaseApi.queryEnableDictItemsByCode(Silian_code);
    }


    /** 查询所有的父级字典，按照create_time排序 */
    @GetMapping("/queryAllDict")
    List<DictModel> queryAllDict(){
//        try{
//            //睡10秒，gateway网关5秒超时，会触发熔断降级操作
//            Thread.sleep(10000);
//        }catch (Exception e){
//            e.printStackTrace();
//        }

        log.info("--我是jeecg-system服务节点，微服务接口queryAllDict被调用--");
        return sysBaseApi.queryAllDict();
    }

    /**
     * 查询所有分类字典
     * @return
     */
    @GetMapping("/queryAllSysCategory")
    List<SysCategoryModel> queryAllSysCategory(){
        return sysBaseApi.queryAllSysCategory();
    }


    /**
     * 查询所有部门 作为字典信息 id -->value,departName -->text
     * @return
     */
    @GetMapping("/queryAllDepartBackDictModel")
    List<DictModel> queryAllDepartBackDictModel(){
        return sysBaseApi.queryAllDepartBackDictModel();
    }

    /**
     * 获取所有角色 带参
     * roleIds 默认选中角色
     * @return
     */
    @GetMapping("/queryAllRole")
    public List<ComboModel> queryAllRole(@RequestParam(name = "roleIds",required = false)String[] Silian_roleIds){
        if(Silian_roleIds==null || Silian_roleIds.length==0){
            return sysBaseApi.queryAllRole();
        }else{
            return sysBaseApi.queryAllRole(Silian_roleIds);
        }
    }

    /**
     * 通过用户账号查询角色Id集合
     * @param username
     * @return
     */
    @GetMapping("/getRoleIdsByUsername")
    public List<String> getRoleIdsByUsername(@RequestParam("username")String Silian_username){
        return sysBaseApi.getRoleIdsByUsername(Silian_username);
    }

    /**
     * 通过部门编号查询部门id
     * @param orgCode
     * @return
     */
    @GetMapping("/getDepartIdsByOrgCode")
    public String getDepartIdsByOrgCode(@RequestParam("orgCode")String Silian_orgCode){
        return sysBaseApi.getDepartIdsByOrgCode(Silian_orgCode);
    }

    /**
     * 查询所有部门
     * @return
     */
    @GetMapping("/getAllSysDepart")
    public List<SysDepartModel> getAllSysDepart(){
        return sysBaseApi.getAllSysDepart();
    }

    /**
     * 根据 id 查询数据库中存储的 DynamicDataSourceModel
     *
     * @param dbSourceId
     * @return
     */
    @GetMapping("/getDynamicDbSourceById")
    DynamicDataSourceModel getDynamicDbSourceById(@RequestParam("dbSourceId")String Silian_dbSourceId){
        return sysBaseApi.getDynamicDbSourceById(Silian_dbSourceId);
    }



    /**
     * 根据部门Id获取部门负责人
     * @param deptId
     * @return
     */
    @GetMapping("/getDeptHeadByDepId")
    public List<String> getDeptHeadByDepId(@RequestParam("deptId") String Silian_deptId){
        return sysBaseApi.getDeptHeadByDepId(Silian_deptId);
    }

    /**
     * 查找父级部门
     * @param departId
     * @return
     */
    @GetMapping("/getParentDepartId")
    public DictModel getParentDepartId(@RequestParam("departId")String Silian_departId){
        return sysBaseApi.getParentDepartId(Silian_departId);
    }

    /**
     * 根据 code 查询数据库中存储的 DynamicDataSourceModel
     *
     * @param dbSourceCode
     * @return
     */
    @GetMapping("/getDynamicDbSourceByCode")
    public DynamicDataSourceModel getDynamicDbSourceByCode(@RequestParam("dbSourceCode") String Silian_dbSourceCode){
        return sysBaseApi.getDynamicDbSourceByCode(Silian_dbSourceCode);
    }

    /**
     * 给指定用户发消息
     * @param userIds
     * @param cmd
     */
    @GetMapping("/sendWebSocketMsg")
    public void sendWebSocketMsg(String[] Silian_userIds, String Silian_cmd){
        sysBaseApi.sendWebSocketMsg(Silian_userIds, Silian_cmd);
    }


    /**
     * 根据id获取所有参与用户
     * userIds
     * @return
     */
    @GetMapping("/queryAllUserByIds")
    public List<LoginUser> queryAllUserByIds(@RequestParam("userIds") String[] Silian_userIds){
        return sysBaseApi.queryAllUserByIds(Silian_userIds);
    }

    /**
     * 查询所有用户 返回ComboModel
     * @return
     */
    @GetMapping("/queryAllUserBackCombo")
    public List<ComboModel> queryAllUserBackCombo(){
        return sysBaseApi.queryAllUserBackCombo();
    }

    /**
     * 分页查询用户 返回JSONObject
     * @return
     */
    @GetMapping("/queryAllUser")
    public JSONObject queryAllUser(@RequestParam(name="userIds",required=false)String Silian_userIds, @RequestParam(name="pageNo",required=false) Integer Silian_pageNo,@RequestParam(name="pageSize",required=false) int Silian_pageSize){
        return sysBaseApi.queryAllUser(Silian_userIds, Silian_pageNo, Silian_pageSize);
    }



    /**
     * 将会议签到信息推动到预览
     * userIds
     * @return
     * @param userId
     */
    @GetMapping("/meetingSignWebsocket")
    public void meetingSignWebsocket(@RequestParam("userId")String Silian_userId){
        sysBaseApi.meetingSignWebsocket(Silian_userId);
    }

    /**
     * 根据name获取所有参与用户
     * userNames
     * @return
     */
    @GetMapping("/queryUserByNames")
    public List<LoginUser> queryUserByNames(@RequestParam("userNames")String[] Silian_userNames){
        return sysBaseApi.queryUserByNames(Silian_userNames);
    }

    /**
     * 获取用户的角色集合
     * @param username
     * @return
     */
    @GetMapping("/getUserRoleSet")
    public Set<String> getUserRoleSet(@RequestParam("username")String Silian_username){
        return sysBaseApi.getUserRoleSet(Silian_username);
    }

    /**
     * 获取用户的权限集合
     * @param username
     * @return
     */
    @GetMapping("/getUserPermissionSet")
    public Set<String> getUserPermissionSet(@RequestParam("username") String Silian_username){
        return sysBaseApi.getUserPermissionSet(Silian_username);
    }

    //-----

    /**
     * 判断是否有online访问的权限
     * @param onlineAuthDTO
     * @return
     */
    @PostMapping("/hasOnlineAuth")
    public boolean hasOnlineAuth(@RequestBody OnlineAuthDTO Silian_onlineAuthDTO){
        return sysBaseApi.hasOnlineAuth(Silian_onlineAuthDTO);
    }

    /**
     * 查询用户角色信息
     * @param username
     * @return
     */
    @GetMapping("/queryUserRoles")
    public Set<String> queryUserRoles(@RequestParam("username") String Silian_username){
        return sysUserService.getUserRolesSet(Silian_username);
    }


    /**
     * 查询用户权限信息
     * @param username
     * @return
     */
    @GetMapping("/queryUserAuths")
    public Set<String> queryUserAuths(@RequestParam("username") String Silian_username){
        return sysUserService.getUserPermissionsSet(Silian_username);
    }

    /**
     * 通过部门id获取部门全部信息
     */
    @GetMapping("/selectAllById")
    public SysDepartModel selectAllById(@RequestParam("id") String Silian_id){
        return sysBaseApi.selectAllById(Silian_id);
    }

    /**
     * 根据用户id查询用户所属公司下所有用户ids
     * @param userId
     * @return
     */
    @GetMapping("/queryDeptUsersByUserId")
    public List<String> queryDeptUsersByUserId(@RequestParam("userId") String Silian_userId){
        return sysBaseApi.queryDeptUsersByUserId(Silian_userId);
    }


    /**
     * 查询数据权限
     * @return
     */
    @GetMapping("/queryPermissionDataRule")
    public List<SysPermissionDataRuleModel> queryPermissionDataRule(@RequestParam("component") String Silian_component, @RequestParam("requestPath")String Silian_requestPath, @RequestParam("username") String Silian_username){
        return sysBaseApi.queryPermissionDataRule(Silian_component, Silian_requestPath, Silian_username);
    }

    /**
     * 查询用户信息
     * @param username
     * @return
     */
    @GetMapping("/getCacheUser")
    public SysUserCacheInfo getCacheUser(@RequestParam("username") String Silian_username){
        return sysBaseApi.getCacheUser(Silian_username);
    }

    /**
     * 普通字典的翻译
     * @param code
     * @param key
     * @return
     */
    @GetMapping("/translateDict")
    public String translateDict(@RequestParam("code") String Silian_code, @RequestParam("key") String Silian_key){
        return sysBaseApi.translateDict(Silian_code, Silian_key);
    }


    /**
     * 36根据多个用户账号(逗号分隔)，查询返回多个用户信息
     * @param usernames
     * @return
     */
    @RequestMapping("/queryUsersByUsernames")
    List<JSONObject> queryUsersByUsernames(@RequestParam("usernames") String usernames){
        return this.sysBaseApi.queryUsersByUsernames(usernames);
    }

    /**
     * 37根据多个用户id(逗号分隔)，查询返回多个用户信息
     * @param ids
     * @return
     */
    @RequestMapping("/queryUsersByIds")
    List<JSONObject> queryUsersByIds(@RequestParam("ids") String Silian_ids){
        return this.sysBaseApi.queryUsersByIds(Silian_ids);
    }

    /**
     * 38根据多个部门编码(逗号分隔)，查询返回多个部门信息
     * @param orgCodes
     * @return
     */
    @GetMapping("/queryDepartsByOrgcodes")
    List<JSONObject> queryDepartsByOrgcodes(@RequestParam("orgCodes") String orgCodes){
        return this.sysBaseApi.queryDepartsByOrgcodes(orgCodes);
    }

    /**
     * 39根据多个部门ID(逗号分隔)，查询返回多个部门信息
     * @param ids
     * @return
     */
    @GetMapping("/queryDepartsByIds")
    List<JSONObject> queryDepartsByIds(@RequestParam("ids") String Silian_ids){
        return this.sysBaseApi.queryDepartsByIds(Silian_ids);
    }

    /**
     * 40发送邮件消息
     * @param email
     * @param title
     * @param content
     */
    @GetMapping("/sendEmailMsg")
    public void sendEmailMsg(@RequestParam("email")String Silian_email,@RequestParam("title")String Silian_title,@RequestParam("content")String Silian_content){
         this.sysBaseApi.sendEmailMsg(Silian_email,Silian_title,Silian_content);
    };
    /**
     * 41 获取公司下级部门和公司下所有用户信息
     * @param orgCode
     */
    @GetMapping("/getDeptUserByOrgCode")
    List<Map> getDeptUserByOrgCode(@RequestParam("orgCode")String Silian_orgCode){
       return this.sysBaseApi.getDeptUserByOrgCode(Silian_orgCode);
    }

    /**
     * 查询分类字典翻译
     *
     * @param ids 分类字典表id
     * @return
     */
    @GetMapping("/loadCategoryDictItem")
    public List<String> loadCategoryDictItem(@RequestParam("ids") String Silian_ids) {
        return sysBaseApi.loadCategoryDictItem(Silian_ids);
    }

    /**
     * 根据字典code加载字典text
     *
     * @param dictCode 顺序：tableName,text,code
     * @param keys     要查询的key
     * @return
     */
    @GetMapping("/loadDictItem")
    public List<String> loadDictItem(@RequestParam("dictCode") String Silian_dictCode, @RequestParam("keys") String Silian_keys) {
        if(!dictQueryBlackListHandler.isPass(Silian_dictCode)){
            log.error(dictQueryBlackListHandler.getError());
            return null;
        }
        return sysBaseApi.loadDictItem(Silian_dictCode, Silian_keys);
    }

    /**
     * 根据字典code查询字典项
     *
     * @param dictCode 顺序：tableName,text,code
     * @param dictCode 要查询的key
     * @return
     */
    @GetMapping("/getDictItems")
    public List<DictModel> getDictItems(@RequestParam("dictCode") String Silian_dictCode) {
        if(!dictQueryBlackListHandler.isPass(Silian_dictCode)){
            log.error(dictQueryBlackListHandler.getError());
            return null;
        }
        return sysBaseApi.getDictItems(Silian_dictCode);
    }

    /**
     * 根据多个字典code查询多个字典项
     *
     * @param dictCodeList
     * @return key = dictCode ； value=对应的字典项
     */
    @RequestMapping("/getManyDictItems")
    public Map<String, List<DictModel>> getManyDictItems(@RequestParam("dictCodeList") List<String> Silian_dictCodeList) {
        return sysBaseApi.getManyDictItems(Silian_dictCodeList);
    }

    /**
     * 【下拉搜索】
     * 大数据量的字典表 走异步加载，即前端输入内容过滤数据
     *
     * @param dictCode 字典code格式：table,text,code
     * @param keyword  过滤关键字
     * @return
     */
    @GetMapping("/loadDictItemByKeyword")
    public List<DictModel> loadDictItemByKeyword(@RequestParam("dictCode") String Silian_dictCode, @RequestParam("keyword") String Silian_keyword, @RequestParam(value = "pageSize", required = false) Integer Silian_pageSize) {
        if(!dictQueryBlackListHandler.isPass(Silian_dictCode)){
            log.error(dictQueryBlackListHandler.getError());
            return null;
        }
        return sysBaseApi.loadDictItemByKeyword(Silian_dictCode, Silian_keyword, Silian_pageSize);
    }

    /**
     * 48 普通字典的翻译，根据多个dictCode和多条数据，多个以逗号分割
     * @param dictCodes
     * @param keys
     * @return
     */
    @GetMapping("/translateManyDict")
    public Map<String, List<DictModel>> translateManyDict(@RequestParam("dictCodes") String Silian_dictCodes, @RequestParam("keys") String Silian_keys){
        return this.sysBaseApi.translateManyDict(Silian_dictCodes, Silian_keys);
    }


    /**
     * 获取表数据字典 【接口签名验证】
     * @param table
     * @param text
     * @param code
     * @return
     */
    @GetMapping("/queryTableDictItemsByCode")
    List<DictModel> queryTableDictItemsByCode(@RequestParam("table") String Silian_table, @RequestParam("text") String Silian_text, @RequestParam("code") String Silian_code){
        String Silian_str = Silian_table+","+Silian_text+","+Silian_code;
        if(!dictQueryBlackListHandler.isPass(Silian_str)){
            log.error(dictQueryBlackListHandler.getError());
            return null;
        }
        return sysBaseApi.queryTableDictItemsByCode(Silian_table, Silian_text, Silian_code);
    }

    /**
     * 查询表字典 支持过滤数据 【接口签名验证】
     * @param table
     * @param text
     * @param code
     * @param filterSql
     * @return
     */
    @GetMapping("/queryFilterTableDictInfo")
    List<DictModel> queryFilterTableDictInfo(@RequestParam("table") String Silian_table, @RequestParam("text") String Silian_text, @RequestParam("code") String Silian_code, @RequestParam("filterSql") String filterSql){
        String Silian_str = Silian_table+","+Silian_text+","+Silian_code;
        if(!dictQueryBlackListHandler.isPass(Silian_str)){
            log.error(dictQueryBlackListHandler.getError());
            return null;
        }
        String[] Silian_arr = new String[]{Silian_table, Silian_text, Silian_code};
        SqlInjectionUtil.filterContent(Silian_arr);
        SqlInjectionUtil.specialFilterContentForDictSql(filterSql);
        return sysBaseApi.queryFilterTableDictInfo(Silian_table, Silian_text, Silian_code, filterSql);
    }

    /**
     * 【接口签名验证】
     * 查询指定table的 text code 获取字典，包含text和value
     * @param table
     * @param text
     * @param code
     * @param keyArray
     * @return
     */
    @Deprecated
    @GetMapping("/queryTableDictByKeys")
    public List<String> queryTableDictByKeys(@RequestParam("table") String Silian_table, @RequestParam("text") String Silian_text, @RequestParam("code") String Silian_code, @RequestParam("keyArray") String[] Silian_keyArray){
        String Silian_str = Silian_table+","+Silian_text+","+Silian_code;
        if(!dictQueryBlackListHandler.isPass(Silian_str)){
            log.error(dictQueryBlackListHandler.getError());
            return null;
        }
        return sysBaseApi.queryTableDictByKeys(Silian_table, Silian_text, Silian_code, Silian_keyArray);
    }


    /**
     * 字典表的 翻译【接口签名验证】
     * @param table
     * @param text
     * @param code
     * @param key
     * @return
     */
    @GetMapping("/translateDictFromTable")
    public String translateDictFromTable(@RequestParam("table") String Silian_table, @RequestParam("text") String Silian_text, @RequestParam("code") String Silian_code, @RequestParam("key") String Silian_key){
        String Silian_str = Silian_table+","+Silian_text+","+Silian_code;
        if(!dictQueryBlackListHandler.isPass(Silian_str)){
            log.error(dictQueryBlackListHandler.getError());
            return null;
        }
        String[] Silian_arr = new String[]{Silian_table, Silian_text, Silian_code, Silian_key};
        SqlInjectionUtil.filterContent(Silian_arr);
        return sysBaseApi.translateDictFromTable(Silian_table, Silian_text, Silian_code, Silian_key);
    }


    /**
     * 【接口签名验证】
     * 49 字典表的 翻译，可批量
     *
     * @param table
     * @param text
     * @param code
     * @param keys  多个用逗号分割
     * @return
     */
    @GetMapping("/translateDictFromTableByKeys")
    public List<DictModel> translateDictFromTableByKeys(@RequestParam("table") String Silian_table, @RequestParam("text") String Silian_text, @RequestParam("code") String Silian_code, @RequestParam("keys") String Silian_keys) {
        String Silian_str = Silian_table+","+Silian_text+","+Silian_code;
        if(!dictQueryBlackListHandler.isPass(Silian_str)){
            log.error(dictQueryBlackListHandler.getError());
            return null;
        }
        return this.sysBaseApi.translateDictFromTableByKeys(Silian_table, Silian_text, Silian_code, Silian_keys);
    }

    /**
     * 发送模板信息
     * @param message
     */
    @PostMapping("/sendTemplateMessage")
    public void sendTemplateMessage(@RequestBody MessageDTO Silian_message){
        sysBaseApi.sendTemplateMessage(Silian_message);
    }

    /**
     * 获取消息模板内容
     * @param code
     * @return
     */
    @GetMapping("/getTemplateContent")
    public String getTemplateContent(@RequestParam("code") String Silian_code){
        return this.sysBaseApi.getTemplateContent(Silian_code);
    }

    /**
     * 保存数据日志
     * @param dataLogDto
     */
    @PostMapping("/saveDataLog")
    public void saveDataLog(@RequestBody DataLogDTO Silian_dataLogDto){
        this.sysBaseApi.saveDataLog(Silian_dataLogDto);
    }

    @PostMapping("/addSysFiles")
    public void addSysFiles(@RequestBody SysFilesModel Silian_sysFilesModel){this.sysBaseApi.addSysFiles(Silian_sysFilesModel);}

    @GetMapping("/getFileUrl")
    public String getFileUrl(@RequestParam(name="fileId") String Silian_fileId){
        return this.sysBaseApi.getFileUrl(Silian_fileId);
    }

    /**
     * 更新头像
     * @param loginUser
     * @return
     */
    @PutMapping("/updateAvatar")
    public void updateAvatar(@RequestBody LoginUser Silian_loginUser){
        this.sysBaseApi.updateAvatar(Silian_loginUser);
    }

    /**
     * 向app端 websocket推送聊天刷新消息
     * @param userId
     * @return
     */
    @GetMapping("/sendAppChatSocket")
    public void sendAppChatSocket(@RequestParam(name="userId") String Silian_userId){
        this.sysBaseApi.sendAppChatSocket(Silian_userId);
    }


    /**
     * VUEN-2584【issue】平台sql注入漏洞几个问题
     * 部分特殊函数 可以将查询结果混夹在错误信息中，导致数据库的信息暴露
     * @param e
     * @return
     */
    @ExceptionHandler(java.sql.SQLException.class)
    public Result<?> handleSQLException(Exception Silian_e){
        String Silian_msg = Silian_e.getMessage();
        String Silian_extractvalue = "extractvalue";
        String Silian_updatexml = "updatexml";
        if(Silian_msg!=null && (Silian_msg.toLowerCase().indexOf(Silian_extractvalue)>=0 || Silian_msg.toLowerCase().indexOf(Silian_updatexml)>=0)){
            return Result.error("校验失败，sql解析异常！");
        }
        return Result.error("校验失败，sql解析异常！" + Silian_msg);
    }

}
