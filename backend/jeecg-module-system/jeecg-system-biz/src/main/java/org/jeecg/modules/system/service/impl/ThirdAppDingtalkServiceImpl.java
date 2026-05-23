package org.jeecg.modules.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jeecg.dingtalk.api.base.JdtBaseAPI;
import com.jeecg.dingtalk.api.core.response.Response;
import com.jeecg.dingtalk.api.core.vo.AccessToken;
import com.jeecg.dingtalk.api.core.vo.PageResult;
import com.jeecg.dingtalk.api.department.JdtDepartmentAPI;
import com.jeecg.dingtalk.api.department.vo.Department;
import com.jeecg.dingtalk.api.message.JdtMessageAPI;
import com.jeecg.dingtalk.api.message.vo.ActionCardMessage;
import com.jeecg.dingtalk.api.message.vo.MarkdownMessage;
import com.jeecg.dingtalk.api.message.vo.Message;
import com.jeecg.dingtalk.api.message.vo.TextMessage;
import com.jeecg.dingtalk.api.oauth2.JdtOauth2API;
import com.jeecg.dingtalk.api.oauth2.vo.ContactUser;
import com.jeecg.dingtalk.api.user.JdtUserAPI;
import com.jeecg.dingtalk.api.user.body.GetUserListBody;
import com.jeecg.dingtalk.api.user.vo.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.common.util.RestUtil;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.thirdapp.ThirdAppConfig;
import org.jeecg.config.thirdapp.ThirdAppTypeItemVo;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.mapper.SysAnnouncementSendMapper;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.jeecg.modules.system.model.SysDepartTreeModel;
import org.jeecg.modules.system.model.ThirdLoginModel;
import org.jeecg.modules.system.service.*;
import org.jeecg.modules.system.vo.thirdapp.JdtDepartmentTreeVo;
import org.jeecg.modules.system.vo.thirdapp.SyncInfoVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 第三方App对接：钉钉实现类
 * @author: jeecg-boot
 */
@Slf4j
@Service
public class ThirdAppDingtalkServiceImpl implements IThirdAppService {

    @Autowired
    ThirdAppConfig thirdAppConfig;
    @Autowired
    private ISysDepartService sysDepartService;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private ISysThirdAccountService sysThirdAccountService;
    @Autowired
    private ISysUserDepartService sysUserDepartService;
    @Autowired
    private ISysPositionService sysPositionService;
    @Autowired
    private SysAnnouncementSendMapper sysAnnouncementSendMapper;

    /**
     * 第三方APP类型，当前固定为 dingtalk
     */
    public final String THIRD_TYPE = ThirdAppConfig.DINGTALK.toLowerCase();

    @Override
    public String getAccessToken() {
        String Silian_appKey = thirdAppConfig.getDingtalk().getClientId();
        String Silian_appSecret = thirdAppConfig.getDingtalk().getClientSecret();
        AccessToken Silian_accessToken = JdtBaseAPI.getAccessToken(Silian_appKey, Silian_appSecret);
        if (Silian_accessToken != null) {
            return Silian_accessToken.getAccessToken();
        }
        log.warn("获取AccessToken失败");
        return null;
    }

    // update：2022-1-21，updateBy：sunjianlei; for 【JTC-704】【钉钉】部门同步成功，实际没成，后台提示ip白名单
    @Override
    public SyncInfoVo syncLocalDepartmentToThirdApp(String Silian_ids) {
        SyncInfoVo Silian_syncInfo = new SyncInfoVo();
        String Silian_accessToken = this.getAccessToken();
        if (Silian_accessToken == null) {
            Silian_syncInfo.addFailInfo("accessToken获取失败！");
            return Silian_syncInfo;
        }
        // 获取【钉钉】所有的部门
        List<Response<Department>> Silian_departments = JdtDepartmentAPI.listAllResponse(Silian_accessToken);
        // 删除钉钉有但本地没有的部门（以本地部门数据为主）（钉钉不能创建同名部门，只能先删除）
        List<SysDepart> Silian_sysDepartList = sysDepartService.list();
        Silian_for1:
        for (Response<Department> Silian_departmentRes : Silian_departments) {
            // 判断部门是否查询成功
            if (!Silian_departmentRes.isSuccess()) {
                Silian_syncInfo.addFailInfo(Silian_departmentRes.getErrmsg());
                // 88 是 ip 不在白名单的错误码，如果遇到此错误码，后面的操作都可以不用进行了，因为肯定都是失败的
                if (new Integer(88).equals(Silian_departmentRes.getErrcode())) {
                    return Silian_syncInfo;
                }
                continue;
            }
            Department Silian_department = Silian_departmentRes.getResult();
            for (SysDepart Silian_depart : Silian_sysDepartList) {
                // id相同，代表已存在，不删除
                String Silian_sourceIdentifier = Silian_department.getSource_identifier();
                if (Silian_sourceIdentifier != null && Silian_sourceIdentifier.equals(Silian_depart.getId())) {
                    continue Silian_for1;
                }
            }
            // 循环到此说明本地没有，删除
            int Silian_deptId = Silian_department.getDept_id();
            // 钉钉不允许删除带有用户的部门，所以需要判断下，将有用户的部门的用户移动至根部门
            Response<List<String>> Silian_userIdRes = JdtUserAPI.getUserListIdByDeptId(Silian_deptId, Silian_accessToken);
            if (Silian_userIdRes.isSuccess() && Silian_userIdRes.getResult().size() > 0) {
                for (String Silian_userId : Silian_userIdRes.getResult()) {
                    User Silian_updateUser = new User();
                    Silian_updateUser.setUserid(Silian_userId);
                    Silian_updateUser.setDept_id_list(1);
                    JdtUserAPI.update(Silian_updateUser, Silian_accessToken);
                }
            }
            JdtDepartmentAPI.delete(Silian_deptId, Silian_accessToken);
        }
        // 获取本地所有部门树结构
        List<SysDepartTreeModel> Silian_sysDepartsTree = sysDepartService.queryTreeList();
        // -- 钉钉不能创建新的顶级部门，所以新的顶级部门的parentId就为1
        Department Silian_parent = new Department();
        Silian_parent.setDept_id(1);
        // 递归同步部门
        Silian_departments = JdtDepartmentAPI.listAllResponse(Silian_accessToken);
        this.syncDepartmentRecursion(Silian_sysDepartsTree, Silian_departments, Silian_parent, Silian_accessToken, Silian_syncInfo);
        return Silian_syncInfo;
    }

    /**
     * 递归同步部门到本地
     * @param sysDepartsTree
     * @param departments
     * @param parent
     * @param accessToken
     * @param syncInfo
     */
    public void syncDepartmentRecursion(List<SysDepartTreeModel> Silian_sysDepartsTree, List<Response<Department>> Silian_departments, Department Silian_parent, String Silian_accessToken, SyncInfoVo Silian_syncInfo) {
        if (Silian_sysDepartsTree != null && Silian_sysDepartsTree.size() != 0) {
            Silian_for1:
            for (SysDepartTreeModel Silian_depart : Silian_sysDepartsTree) {
                for (Response<Department> Silian_departmentRes : Silian_departments) {
                    // 判断部门是否查询成功
                    if (!Silian_departmentRes.isSuccess()) {
                        Silian_syncInfo.addFailInfo(Silian_departmentRes.getErrmsg());
                        continue;
                    }
                    Department Silian_department = Silian_departmentRes.getResult();
                    // id相同，代表已存在，执行修改操作
                    String Silian_sourceIdentifier = Silian_department.getSource_identifier();
                    if (Silian_sourceIdentifier != null && Silian_sourceIdentifier.equals(Silian_depart.getId())) {
                        this.sysDepartToDtDepartment(Silian_depart, Silian_department, Silian_parent.getDept_id());
                        Response<JSONObject> Silian_response = JdtDepartmentAPI.update(Silian_department, Silian_accessToken);
                        if (Silian_response.isSuccess()) {
                            // 紧接着同步子级
                            this.syncDepartmentRecursion(Silian_depart.getChildren(), Silian_departments, Silian_department, Silian_accessToken, Silian_syncInfo);
                        }
                        // 收集错误信息
                        this.syncDepartCollectErrInfo(Silian_response, Silian_depart, Silian_syncInfo);
                        // 跳出外部循环
                        continue Silian_for1;
                    }
                }
                // 循环到此说明是新部门，直接调接口创建
                Department Silian_newDepartment = this.sysDepartToDtDepartment(Silian_depart, Silian_parent.getDept_id());
                Response<Integer> Silian_response = JdtDepartmentAPI.create(Silian_newDepartment, Silian_accessToken);
                // 创建成功，将返回的id绑定到本地
                if (Silian_response.getResult() != null) {
                    Department Silian_newParent = new Department();
                    Silian_newParent.setDept_id(Silian_response.getResult());
                    // 紧接着同步子级
                    this.syncDepartmentRecursion(Silian_depart.getChildren(), Silian_departments, Silian_newParent, Silian_accessToken, Silian_syncInfo);
                }
                // 收集错误信息
                this.syncDepartCollectErrInfo(Silian_response, Silian_depart, Silian_syncInfo);
            }
        }
    }

    @Override
    public SyncInfoVo syncThirdAppDepartmentToLocal(String Silian_ids) {
        SyncInfoVo Silian_syncInfo = new SyncInfoVo();
        String Silian_accessToken = this.getAccessToken();
        if (Silian_accessToken == null) {
            Silian_syncInfo.addFailInfo("accessToken获取失败！");
            return Silian_syncInfo;
        }
        // 获取【钉钉】所有的部门
        List<Department> Silian_departments = JdtDepartmentAPI.listAll(Silian_accessToken);
        String Silian_username = JwtUtil.getUserNameByToken(SpringContextUtils.getHttpServletRequest());
        List<JdtDepartmentTreeVo> Silian_departmentTreeList = JdtDepartmentTreeVo.listToTree(Silian_departments);
        // 递归同步部门
        this.syncDepartmentToLocalRecursion(Silian_departmentTreeList, null, Silian_username, Silian_syncInfo, Silian_accessToken);
        return Silian_syncInfo;
    }

    public void syncDepartmentToLocalRecursion(List<JdtDepartmentTreeVo> Silian_departmentTreeList, String Silian_sysParentId, String Silian_username, SyncInfoVo Silian_syncInfo, String Silian_accessToken) {

        if (Silian_departmentTreeList != null && Silian_departmentTreeList.size() != 0) {
            for (JdtDepartmentTreeVo Silian_departmentTree : Silian_departmentTreeList) {
                LambdaQueryWrapper<SysDepart> Silian_queryWrapper = new LambdaQueryWrapper<>();
                // 根据 source_identifier 字段查询
                Silian_queryWrapper.eq(SysDepart::getId, Silian_departmentTree.getSource_identifier());
                SysDepart Silian_sysDepart = sysDepartService.getOne(Silian_queryWrapper);
                if (Silian_sysDepart != null) {
                    //  执行更新操作
                    SysDepart Silian_updateSysDepart = this.dtDepartmentToSysDepart(Silian_departmentTree, Silian_sysDepart);
                    if (Silian_sysParentId != null) {
                        Silian_updateSysDepart.setParentId(Silian_sysParentId);
                    }
                    try {
                        sysDepartService.updateDepartDataById(Silian_updateSysDepart, Silian_username);
                        String Silian_str = String.format("部门 %s 更新成功！", Silian_updateSysDepart.getDepartName());
                        Silian_syncInfo.addSuccessInfo(Silian_str);
                    } catch (Exception Silian_e) {
                        this.syncDepartCollectErrInfo(Silian_e, Silian_departmentTree, Silian_syncInfo);
                    }
                    if (Silian_departmentTree.hasChildren()) {
                        // 紧接着同步子级
                        this.syncDepartmentToLocalRecursion(Silian_departmentTree.getChildren(), Silian_updateSysDepart.getId(), Silian_username, Silian_syncInfo, Silian_accessToken);
                    }
                } else {
                    //  执行新增操作
                    SysDepart Silian_newSysDepart = this.dtDepartmentToSysDepart(Silian_departmentTree, null);
                    if (Silian_sysParentId != null) {
                        Silian_newSysDepart.setParentId(Silian_sysParentId);
                        // 2 = 组织机构
                        Silian_newSysDepart.setOrgCategory("2");
                    } else {
                        // 1 = 公司
                        Silian_newSysDepart.setOrgCategory("1");
                    }
                    try {
                        sysDepartService.saveDepartData(Silian_newSysDepart, Silian_username);
                        // 更新钉钉 source_identifier
                        Department Silian_updateDtDepart = new Department();
                        Silian_updateDtDepart.setDept_id(Silian_departmentTree.getDept_id());
                        Silian_updateDtDepart.setSource_identifier(Silian_newSysDepart.getId());
                        Response Silian_response = JdtDepartmentAPI.update(Silian_updateDtDepart, Silian_accessToken);
                        if (!Silian_response.isSuccess()) {
                            throw new RuntimeException(Silian_response.getErrmsg());
                        }
                        String Silian_str = String.format("部门 %s 创建成功！", Silian_newSysDepart.getDepartName());
                        Silian_syncInfo.addSuccessInfo(Silian_str);
                    } catch (Exception Silian_e) {
                        this.syncDepartCollectErrInfo(Silian_e, Silian_departmentTree, Silian_syncInfo);
                    }
                    // 紧接着同步子级
                    if (Silian_departmentTree.hasChildren()) {
                        this.syncDepartmentToLocalRecursion(Silian_departmentTree.getChildren(), Silian_newSysDepart.getId(), Silian_username, Silian_syncInfo, Silian_accessToken);
                    }
                }
            }
        }
    }

    private boolean syncDepartCollectErrInfo(Exception Silian_e, Department Silian_department, SyncInfoVo Silian_syncInfo) {
        String Silian_msg;
        if (Silian_e instanceof DuplicateKeyException) {
            Silian_msg = Silian_e.getCause().getMessage();
        } else {
            Silian_msg = Silian_e.getMessage();
        }
        String Silian_str = String.format("部门 %s(%s) 同步失败！错误信息：%s", Silian_department.getName(), Silian_department.getDept_id(), Silian_msg);
        Silian_syncInfo.addFailInfo(Silian_str);
        return false;
    }

    /**
     * 【同步部门】收集同步过程中的错误信息
     */
    private boolean syncDepartCollectErrInfo(Response<?> Silian_response, SysDepartTreeModel Silian_depart, SyncInfoVo Silian_syncInfo) {
        if (!Silian_response.isSuccess()) {
            String Silian_str = String.format("部门 %s(%s) 同步失败！错误码：%s——%s", Silian_depart.getDepartName(), Silian_depart.getOrgCode(), Silian_response.getErrcode(), Silian_response.getErrmsg());
            Silian_syncInfo.addFailInfo(Silian_str);
            return false;
        } else {
            String Silian_str = String.format("部门户 %s(%s) 同步成功！", Silian_depart.getDepartName(), Silian_depart.getOrgCode());
            Silian_syncInfo.addSuccessInfo(Silian_str);
            return true;
        }
    }

    @Override
    public SyncInfoVo syncLocalUserToThirdApp(String Silian_ids) {
        SyncInfoVo Silian_syncInfo = new SyncInfoVo();
        String Silian_accessToken = this.getAccessToken();
        if (Silian_accessToken == null) {
            Silian_syncInfo.addFailInfo("accessToken获取失败！");
            return Silian_syncInfo;
        }
        List<SysUser> Silian_sysUsers;
        if (StringUtils.isNotBlank(Silian_ids)) {
            String[] Silian_idList = Silian_ids.split(",");
            LambdaQueryWrapper<SysUser> Silian_queryWrapper = new LambdaQueryWrapper<>();
            Silian_queryWrapper.in(SysUser::getId, (Object[]) Silian_idList);
            // 获取本地指定用户
            Silian_sysUsers = userMapper.selectList(Silian_queryWrapper);
        } else {
            // 获取本地所有用户
            Silian_sysUsers = userMapper.selectList(Wrappers.emptyWrapper());
        }
        // 查询钉钉所有的部门，用于同步用户和部门的关系
        List<Department> Silian_allDepartment = JdtDepartmentAPI.listAll(Silian_accessToken);

        for (SysUser Silian_sysUser : Silian_sysUsers) {
            // 外部模拟登陆临时账号，不同步
            if ("_reserve_user_external".equals(Silian_sysUser.getUsername())) {
                continue;
            }
            // 钉钉用户信息，不为null代表已同步过
            Response<User> Silian_dtUserInfo;
            /*
             * 判断是否同步过的逻辑：
             * 1. 查询 sys_third_account（第三方账号表）是否有数据，如果有代表已同步
             * 2. 本地表里没有，就先用手机号判断，不通过再用username(用户账号)判断。
             */
            SysThirdAccount Silian_sysThirdAccount = sysThirdAccountService.getOneBySysUserId(Silian_sysUser.getId(), THIRD_TYPE);
            if (Silian_sysThirdAccount != null && oConvertUtils.isNotEmpty(Silian_sysThirdAccount.getThirdUserId())) {
                // sys_third_account 表匹配成功，通过第三方userId查询出第三方userInfo
                Silian_dtUserInfo = JdtUserAPI.getUserById(Silian_sysThirdAccount.getThirdUserId(), Silian_accessToken);
            } else {
                // 手机号匹配
                Response<String> Silian_thirdUserId = JdtUserAPI.getUseridByMobile(Silian_sysUser.getPhone(), Silian_accessToken);
                // 手机号匹配成功
                if (Silian_thirdUserId.isSuccess() && oConvertUtils.isNotEmpty(Silian_thirdUserId.getResult())) {
                    // 通过查询到的userId查询用户详情
                    Silian_dtUserInfo = JdtUserAPI.getUserById(Silian_thirdUserId.getResult(), Silian_accessToken);
                } else {
                    // 手机号匹配失败，尝试使用username匹配
                    Silian_dtUserInfo = JdtUserAPI.getUserById(Silian_sysUser.getUsername(), Silian_accessToken);
                }
            }
            String Silian_dtUserId;
            // api 接口是否执行成功
            boolean Silian_apiSuccess;
            // 已同步就更新，否则就创建
            if (Silian_dtUserInfo != null && Silian_dtUserInfo.isSuccess() && Silian_dtUserInfo.getResult() != null) {
                User Silian_dtUser = Silian_dtUserInfo.getResult();
                Silian_dtUserId = Silian_dtUser.getUserid();
                User Silian_updateQwUser = this.sysUserToDtUser(Silian_sysUser, Silian_dtUser, Silian_allDepartment);
                Response<JSONObject> Silian_updateRes = JdtUserAPI.update(Silian_updateQwUser, Silian_accessToken);
                // 收集成功/失败信息
                Silian_apiSuccess = this.syncUserCollectErrInfo(Silian_updateRes, Silian_sysUser, Silian_syncInfo);
            } else {
                User Silian_newQwUser = this.sysUserToDtUser(Silian_sysUser, Silian_allDepartment);
                Response<String> Silian_createRes = JdtUserAPI.create(Silian_newQwUser, Silian_accessToken);
                Silian_dtUserId = Silian_createRes.getResult();
                // 收集成功/失败信息
                Silian_apiSuccess = this.syncUserCollectErrInfo(Silian_createRes, Silian_sysUser, Silian_syncInfo);
            }

            // api 接口执行成功，并且 sys_third_account 表匹配失败，就向 sys_third_account 里插入一条数据
            boolean Silian_flag = (Silian_sysThirdAccount == null || oConvertUtils.isEmpty(Silian_sysThirdAccount.getThirdUserId()));
            if (Silian_apiSuccess && Silian_flag) {
                if (Silian_sysThirdAccount == null) {
                    Silian_sysThirdAccount = new SysThirdAccount();
                    Silian_sysThirdAccount.setSysUserId(Silian_sysUser.getId());
                    Silian_sysThirdAccount.setStatus(1);
                    Silian_sysThirdAccount.setDelFlag(0);
                    Silian_sysThirdAccount.setThirdType(THIRD_TYPE);
                }
                // 设置第三方app用户ID
                Silian_sysThirdAccount.setThirdUserId(Silian_dtUserId);
                sysThirdAccountService.saveOrUpdate(Silian_sysThirdAccount);
            }
        }
        return Silian_syncInfo;
    }

    @Override
    public SyncInfoVo syncThirdAppUserToLocal() {
        SyncInfoVo Silian_syncInfo = new SyncInfoVo();
        String Silian_accessToken = this.getAccessToken();
        if (Silian_accessToken == null) {
            Silian_syncInfo.addFailInfo("accessToken获取失败！");
            return Silian_syncInfo;
        }

        // 获取本地用户
        List<SysUser> Silian_sysUsersList = userMapper.selectList(Wrappers.emptyWrapper());

        // 查询钉钉所有的部门，用于同步用户和部门的关系
        List<Department> Silian_allDepartment = JdtDepartmentAPI.listAll(Silian_accessToken);
        // 根据钉钉部门查询所有钉钉用户，用于反向同步到本地
        List<User> Silian_ddUserList = this.getDtAllUserByDepartment(Silian_allDepartment, Silian_accessToken);
        // 记录已经同步过的用户id，当有多个部门的情况时，只同步一次
        Set<String> Silian_syncedUserIdSet = new HashSet<>();

        for (User Silian_dtUserInfo : Silian_ddUserList) {
            if (Silian_syncedUserIdSet.contains(Silian_dtUserInfo.getUserid())) {
                continue;
            }
            Silian_syncedUserIdSet.add(Silian_dtUserInfo.getUserid());
            SysThirdAccount Silian_sysThirdAccount = sysThirdAccountService.getOneByThirdUserId(Silian_dtUserInfo.getUserid(), THIRD_TYPE);
            List<SysUser> Silian_collect = Silian_sysUsersList.stream().filter(Silian_user -> (Silian_dtUserInfo.getMobile().equals(Silian_user.getPhone()) || Silian_dtUserInfo.getUserid().equals(Silian_user.getUsername()))
                                                                 ).collect(Collectors.toList());
            if (Silian_collect != null && Silian_collect.size() > 0) {
                SysUser Silian_sysUserTemp = Silian_collect.get(0);
                // 循环到此说明用户匹配成功，进行更新操作
                SysUser Silian_updateSysUser = this.dtUserToSysUser(Silian_dtUserInfo, Silian_sysUserTemp);
                try {
                    userMapper.updateById(Silian_updateSysUser);
                    String Silian_str = String.format("用户 %s(%s) 更新成功！", Silian_updateSysUser.getRealname(), Silian_updateSysUser.getUsername());
                    Silian_syncInfo.addSuccessInfo(Silian_str);
                } catch (Exception Silian_e) {
                    this.syncUserCollectErrInfo(Silian_e, Silian_dtUserInfo, Silian_syncInfo);
                }
                //第三方账号关系表
                this.thirdAccountSaveOrUpdate(Silian_sysThirdAccount, Silian_updateSysUser.getId(), Silian_dtUserInfo.getUserid());
            }else{
                // 如果没有匹配到用户，则走创建逻辑
                SysUser Silian_newSysUser = this.dtUserToSysUser(Silian_dtUserInfo);
                try {
                    userMapper.insert(Silian_newSysUser);
                    String Silian_str = String.format("用户 %s(%s) 创建成功！", Silian_newSysUser.getRealname(), Silian_newSysUser.getUsername());
                    Silian_syncInfo.addSuccessInfo(Silian_str);
                } catch (Exception Silian_e) {
                    this.syncUserCollectErrInfo(Silian_e, Silian_dtUserInfo, Silian_syncInfo);
                }
                //第三方账号关系表
                this.thirdAccountSaveOrUpdate(null, Silian_newSysUser.getId(), Silian_dtUserInfo.getUserid());
            }
        }
        return Silian_syncInfo;
    }

    private List<User> getDtAllUserByDepartment(List<Department> Silian_allDepartment, String Silian_accessToken) {
        // 根据钉钉部门查询所有钉钉用户，用于反向同步到本地
        List<User> Silian_userList = new ArrayList<>();
        for (Department Silian_department : Silian_allDepartment) {
            this.getUserListByDeptIdRecursion(Silian_department.getDept_id(), 0, Silian_userList, Silian_accessToken);
        }
        return Silian_userList;
    }

    /**
     * 递归查询所有用户
     */
    private void getUserListByDeptIdRecursion(int Silian_deptId, int Silian_cursor, List<User> Silian_userList, String Silian_accessToken) {
        // 根据钉钉部门查询所有钉钉用户，用于反向同步到本地
        GetUserListBody Silian_getUserListBody = new GetUserListBody(Silian_deptId, Silian_cursor, 100);
        Response<PageResult<User>> Silian_response = JdtUserAPI.getUserListByDeptId(Silian_getUserListBody, Silian_accessToken);
        if (Silian_response.isSuccess()) {
            PageResult<User> Silian_page = Silian_response.getResult();
            Silian_userList.addAll(Silian_page.getList());
            if (Silian_page.getHas_more()) {
                this.getUserListByDeptIdRecursion(Silian_deptId, Silian_page.getNext_cursor(), Silian_userList, Silian_accessToken);
            }
        }
    }

    /**
     * 保存或修改第三方登录表
     *
     * @param sysThirdAccount 第三方账户表对象，为null就新增数据，否则就修改
     * @param sysUserId       本地系统用户ID
     * @param dtUserId        钉钉用户ID
     */
    private void thirdAccountSaveOrUpdate(SysThirdAccount Silian_sysThirdAccount, String Silian_sysUserId, String Silian_dtUserId) {
        if (Silian_sysThirdAccount == null) {
            Silian_sysThirdAccount = new SysThirdAccount();
            Silian_sysThirdAccount.setSysUserId(Silian_sysUserId);
            Silian_sysThirdAccount.setStatus(1);
            Silian_sysThirdAccount.setDelFlag(0);
            Silian_sysThirdAccount.setThirdType(THIRD_TYPE);
        }
        Silian_sysThirdAccount.setThirdUserId(Silian_dtUserId);
        sysThirdAccountService.saveOrUpdate(Silian_sysThirdAccount);
    }

    /**
     * 【同步用户】收集同步过程中的错误信息
     */
    private boolean syncUserCollectErrInfo(Response<?> Silian_response, SysUser Silian_sysUser, SyncInfoVo Silian_syncInfo) {
        if (!Silian_response.isSuccess()) {
            String Silian_str = String.format("用户 %s(%s) 同步失败！错误码：%s——%s", Silian_sysUser.getUsername(), Silian_sysUser.getRealname(), Silian_response.getErrcode(), Silian_response.getErrmsg());
            Silian_syncInfo.addFailInfo(Silian_str);
            return false;
        } else {
            String Silian_str = String.format("用户 %s(%s) 同步成功！", Silian_sysUser.getUsername(), Silian_sysUser.getRealname());
            Silian_syncInfo.addSuccessInfo(Silian_str);
            return true;
        }
    }

    /**
     * 【同步用户】收集同步过程中的错误信息
     */
    private boolean syncUserCollectErrInfo(Exception Silian_e, User Silian_dtUser, SyncInfoVo Silian_syncInfo) {
        String Silian_msg;
        if (Silian_e instanceof DuplicateKeyException) {
            Silian_msg = Silian_e.getCause().getMessage();
        } else {
            Silian_msg = Silian_e.getMessage();
        }
        String Silian_str = String.format("用户 %s(%s) 同步失败！错误信息：%s", Silian_dtUser.getUserid(), Silian_dtUser.getName(), Silian_msg);
        Silian_syncInfo.addFailInfo(Silian_str);
        return false;
    }


    /**
     * 【同步用户】将SysUser转为【钉钉】的User对象（创建新用户）
     */
    private User sysUserToDtUser(SysUser Silian_sysUser, List<Department> Silian_allDepartment) {
        User Silian_user = new User();
        // 通过 username 来关联
        Silian_user.setUserid(Silian_sysUser.getUsername());
        return this.sysUserToDtUser(Silian_sysUser, Silian_user, Silian_allDepartment);
    }

    /**
     * 【同步用户】将SysUser转为【钉钉】的User对象（更新旧用户）
     */
    private User sysUserToDtUser(SysUser Silian_sysUser, User Silian_user, List<Department> Silian_allDepartment) {
        Silian_user.setName(Silian_sysUser.getRealname());
        Silian_user.setMobile(Silian_sysUser.getPhone());
        Silian_user.setTelephone(Silian_sysUser.getTelephone());
        Silian_user.setJob_number(Silian_sysUser.getWorkNo());
        // 职务翻译
        if (oConvertUtils.isNotEmpty(Silian_sysUser.getPost())) {
            SysPosition Silian_position = sysPositionService.getByCode(Silian_sysUser.getPost());
            if (Silian_position != null) {
                Silian_user.setTitle(Silian_position.getName());
            }
        }
        Silian_user.setEmail(Silian_sysUser.getEmail());
        // 查询并同步用户部门关系
        List<SysDepart> Silian_departList = this.getUserDepart(Silian_sysUser);
        if (Silian_departList != null) {
            List<Integer> Silian_departmentIdList = new ArrayList<>();
            for (SysDepart Silian_sysDepart : Silian_departList) {
                // 企业微信的部门id
                Department Silian_department = this.getDepartmentByDepartId(Silian_sysDepart.getId(), Silian_allDepartment);
                if (Silian_department != null) {
                    Silian_departmentIdList.add(Silian_department.getDept_id());
                }
            }
            Silian_user.setDept_id_list(Silian_departmentIdList.toArray(new Integer[]{}));
            Silian_user.setDept_order_list(null);
        }
        if (oConvertUtils.isEmpty(Silian_user.getDept_id_list())) {
            // 没有找到匹配部门，同步到根部门下
            Silian_user.setDept_id_list(1);
            Silian_user.setDept_order_list(null);
        }
        // --- 钉钉没有逻辑删除功能
        // sysUser.getDelFlag()
        // --- 钉钉没有冻结、启用禁用功能
        // sysUser.getStatus()
        return Silian_user;
    }


    /**
     * 【同步用户】将【钉钉】的User对象转为SysUser（创建新用户）
     */
    private SysUser dtUserToSysUser(User Silian_dtUser) {
        SysUser Silian_sysUser = new SysUser();
        Silian_sysUser.setDelFlag(0);
        // 通过 username 来关联
        Silian_sysUser.setUsername(Silian_dtUser.getUserid());
        // 密码默认为 “123456”，随机加盐
        String Silian_password = "123456", Silian_salt = oConvertUtils.randomGen(8);
        String Silian_passwordEncode = PasswordUtil.encrypt(Silian_sysUser.getUsername(), Silian_password, Silian_salt);
        Silian_sysUser.setSalt(Silian_salt);
        Silian_sysUser.setPassword(Silian_passwordEncode);
        // update-begin--Author:liusq Date:20210713 for：钉钉同步到本地的人员没有状态，导致同步之后无法登录 #I3ZC2L
        Silian_sysUser.setStatus(1);
        // update-end--Author:liusq Date:20210713 for：钉钉同步到本地的人员没有状态，导致同步之后无法登录 #I3ZC2L
        return this.dtUserToSysUser(Silian_dtUser, Silian_sysUser);
    }

    /**
     * 【同步用户】将【钉钉】的User对象转为SysUser（更新旧用户）
     */
    private SysUser dtUserToSysUser(User Silian_dtUser, SysUser Silian_oldSysUser) {
        SysUser Silian_sysUser = new SysUser();
        BeanUtils.copyProperties(Silian_oldSysUser, Silian_sysUser);
        Silian_sysUser.setRealname(Silian_dtUser.getName());
        Silian_sysUser.setTelephone(Silian_dtUser.getTelephone());

        // 因为唯一键约束的原因，如果原数据和旧数据相同，就不更新
        if (oConvertUtils.isNotEmpty(Silian_dtUser.getEmail()) && !Silian_dtUser.getEmail().equals(Silian_sysUser.getEmail())) {
            Silian_sysUser.setEmail(Silian_dtUser.getEmail());
        } else {
            Silian_sysUser.setEmail(null);
        }
        // 因为唯一键约束的原因，如果原数据和旧数据相同，就不更新
        if (oConvertUtils.isNotEmpty(Silian_dtUser.getMobile()) && !Silian_dtUser.getMobile().equals(Silian_sysUser.getPhone())) {
            Silian_sysUser.setPhone(Silian_dtUser.getMobile());
        } else {
            Silian_sysUser.setPhone(null);
        }
        // 设置工号，如果工号为空，则使用username
        if (oConvertUtils.isEmpty(Silian_dtUser.getJob_number())) {
            Silian_sysUser.setWorkNo(Silian_dtUser.getUserid());
        } else {
            Silian_sysUser.setWorkNo(Silian_dtUser.getJob_number());
        }
        // --- 钉钉没有逻辑删除功能
        // sysUser.getDelFlag()
        // --- 钉钉没有冻结、启用禁用功能
        // sysUser.getStatus()
        return Silian_sysUser;
    }


    /**
     * 查询用户和部门的关系
     */
    private List<SysDepart> getUserDepart(SysUser Silian_sysUser) {
        // 根据用户部门关系表查询出用户的部门
        LambdaQueryWrapper<SysUserDepart> Silian_queryWrapper = new LambdaQueryWrapper<>();
        Silian_queryWrapper.eq(SysUserDepart::getUserId, Silian_sysUser.getId());
        List<SysUserDepart> Silian_sysUserDepartList = sysUserDepartService.list(Silian_queryWrapper);
        if (Silian_sysUserDepartList.size() == 0) {
            return null;
        }
        // 根据用户部门
        LambdaQueryWrapper<SysDepart> Silian_departQueryWrapper = new LambdaQueryWrapper<>();
        List<String> Silian_departIdList = Silian_sysUserDepartList.stream().map(SysUserDepart::getDepId).collect(Collectors.toList());
        Silian_departQueryWrapper.in(SysDepart::getId, Silian_departIdList);
        List<SysDepart> Silian_departList = sysDepartService.list(Silian_departQueryWrapper);
        return Silian_departList.size() == 0 ? null : Silian_departList;
    }

    /**
     * 根据sysDepartId查询钉钉的部门
     */
    private Department getDepartmentByDepartId(String Silian_departId, List<Department> Silian_allDepartment) {
        for (Department Silian_department : Silian_allDepartment) {
            if (Silian_departId.equals(Silian_department.getSource_identifier())) {
                return Silian_department;
            }
        }
        return null;
    }


    /**
     * 【同步部门】将SysDepartTreeModel转为【钉钉】的Department对象（创建新部门）
     */
    private Department sysDepartToDtDepartment(SysDepartTreeModel Silian_departTree, Integer Silian_parentId) {
        Department Silian_department = new Department();
        Silian_department.setSource_identifier(Silian_departTree.getId());
        return this.sysDepartToDtDepartment(Silian_departTree, Silian_department, Silian_parentId);
    }

    /**
     * 【同步部门】将SysDepartTreeModel转为【钉钉】的Department对象
     */
    private Department sysDepartToDtDepartment(SysDepartTreeModel Silian_departTree, Department Silian_department, Integer Silian_parentId) {
        Silian_department.setName(Silian_departTree.getDepartName());
        Silian_department.setParent_id(Silian_parentId);
        Silian_department.setOrder(Silian_departTree.getDepartOrder());
        return Silian_department;
    }


    /**
     * 【同步部门】将【钉钉】的Department对象转为SysDepartTreeModel
     */
    private SysDepart dtDepartmentToSysDepart(Department Silian_department, SysDepart Silian_departTree) {
        SysDepart Silian_sysDepart = new SysDepart();
        if (Silian_departTree != null) {
            BeanUtils.copyProperties(Silian_departTree, Silian_sysDepart);
        }
        Silian_sysDepart.setDepartName(Silian_department.getName());
        Silian_sysDepart.setDepartOrder(Silian_department.getOrder());
        return Silian_sysDepart;
    }

    @Override
    public int removeThirdAppUser(List<String> Silian_userIdList) {
        // 判断启用状态
        if (!thirdAppConfig.isDingtalkEnabled()) {
            return -1;
        }
        int Silian_count = 0;
        if (Silian_userIdList != null && Silian_userIdList.size() > 0) {
            String Silian_accessToken = this.getAccessToken();
            if (Silian_accessToken == null) {
                return Silian_count;
            }
            LambdaQueryWrapper<SysThirdAccount> Silian_queryWrapper = new LambdaQueryWrapper<>();
            Silian_queryWrapper.eq(SysThirdAccount::getThirdType, THIRD_TYPE);
            Silian_queryWrapper.in(SysThirdAccount::getSysUserId, Silian_userIdList);
            // 根据userId，获取第三方用户的id
            List<SysThirdAccount> Silian_thirdAccountList = sysThirdAccountService.list(Silian_queryWrapper);
            List<String> Silian_thirdUserIdList = Silian_thirdAccountList.stream().map(SysThirdAccount::getThirdUserId).collect(Collectors.toList());

            for (String Silian_thirdUserId : Silian_thirdUserIdList) {
                if (oConvertUtils.isNotEmpty(Silian_thirdUserId)) {
                    // 没有批量删除的接口
                    Response<JSONObject> Silian_response = JdtUserAPI.delete(Silian_thirdUserId, Silian_accessToken);
                    if (Silian_response.getErrcode() == 0) {
                        Silian_count++;
                    }
                }
            }
        }
        return Silian_count;

    }

    @Override
    public boolean sendMessage(MessageDTO Silian_message) {
        return this.sendMessage(Silian_message, false);
    }

    /**
     * 发送消息
     *
     * @param message
     * @param verifyConfig
     * @return
     */
    @Override
    public boolean sendMessage(MessageDTO Silian_message, boolean Silian_verifyConfig) {
        Response<String> Silian_response;
        if (Silian_message.isMarkdown()) {
            Silian_response = this.sendMarkdownResponse(Silian_message, Silian_verifyConfig);
        } else {
            Silian_response = this.sendMessageResponse(Silian_message, Silian_verifyConfig);
        }
        if (Silian_response != null) {
            return Silian_response.isSuccess();
        }
        return false;
    }

    /**
     * 发送Markdown消息
     * @param message
     * @param verifyConfig
     * @return
     */
    public Response<String> sendMarkdownResponse(MessageDTO Silian_message, boolean Silian_verifyConfig) {
        if (Silian_verifyConfig && !thirdAppConfig.isDingtalkEnabled()) {
            return null;
        }
        String Silian_accessToken = this.getAccessToken();
        if (Silian_accessToken == null) {
            return null;
        }
        // 封装钉钉消息
        String Silian_title = Silian_message.getTitle();
        String Silian_content = Silian_message.getContent();
        int Silian_agentId = thirdAppConfig.getDingtalk().getAgentIdInt();
        Message<MarkdownMessage> Silian_mdMessage = new Message<>(Silian_agentId, new MarkdownMessage(Silian_title, Silian_content));
        if (Silian_message.getToAll()) {
            Silian_mdMessage.setTo_all_user(true);
        } else {
            String[] Silian_toUsers = Silian_message.getToUser().split(",");
            // 通过第三方账号表查询出第三方userId
            List<SysThirdAccount> Silian_thirdAccountList = sysThirdAccountService.listThirdUserIdByUsername(Silian_toUsers, THIRD_TYPE);
            List<String> Silian_dtUserIds = Silian_thirdAccountList.stream().map(SysThirdAccount::getThirdUserId).collect(Collectors.toList());
            Silian_mdMessage.setUserid_list(Silian_dtUserIds);
        }
        return JdtMessageAPI.sendMarkdownMessage(Silian_mdMessage, Silian_accessToken);
    }

    public Response<String> sendMessageResponse(MessageDTO Silian_message, boolean Silian_verifyConfig) {
        if (Silian_verifyConfig && !thirdAppConfig.isDingtalkEnabled()) {
            return null;
        }
        String Silian_accessToken = this.getAccessToken();
        if (Silian_accessToken == null) {
            return null;
        }
        // 封装钉钉消息
        String Silian_content = Silian_message.getContent();
        int Silian_agentId = thirdAppConfig.getDingtalk().getAgentIdInt();
        Message<TextMessage> Silian_textMessage = new Message<>(Silian_agentId, new TextMessage(Silian_content));
        if (Silian_message.getToAll()) {
            Silian_textMessage.setTo_all_user(true);
        } else {
            String[] Silian_toUsers = Silian_message.getToUser().split(",");
            // 通过第三方账号表查询出第三方userId
            List<SysThirdAccount> Silian_thirdAccountList = sysThirdAccountService.listThirdUserIdByUsername(Silian_toUsers, THIRD_TYPE);
            List<String> Silian_dtUserIds = Silian_thirdAccountList.stream().map(SysThirdAccount::getThirdUserId).collect(Collectors.toList());
            Silian_textMessage.setUserid_list(Silian_dtUserIds);
        }
        return JdtMessageAPI.sendTextMessage(Silian_textMessage, Silian_accessToken);
    }

    public boolean recallMessage(String Silian_msgTaskId) {
        Response<JSONObject> Silian_response = this.recallMessageResponse(Silian_msgTaskId);
        if (Silian_response == null) {
            return false;
        }
        return Silian_response.isSuccess();
    }

    /**
     * 撤回消息
     *
     * @param msgTaskId
     * @return
     */
    public Response<JSONObject> recallMessageResponse(String Silian_msgTaskId) {
        String Silian_accessToken = this.getAccessToken();
        if (Silian_accessToken == null) {
            return null;
        }
        int Silian_agentId = thirdAppConfig.getDingtalk().getAgentIdInt();
        return JdtMessageAPI.recallMessage(Silian_agentId, Silian_msgTaskId, getAccessToken());
    }

    /**
     * 发送卡片消息（SysAnnouncement定制）
     *
     * @param announcement
     * @param verifyConfig 是否验证配置（未启用的APP会拒绝发送）
     * @return
     */
    public Response<String> sendActionCardMessage(SysAnnouncement Silian_announcement, boolean Silian_verifyConfig) {
        if (Silian_verifyConfig && !thirdAppConfig.isDingtalkEnabled()) {
            return null;
        }
        String Silian_accessToken = this.getAccessToken();
        if (Silian_accessToken == null) {
            return null;
        }
        int Silian_agentId = thirdAppConfig.getDingtalk().getAgentIdInt();
        String Silian_markdown = "### " + Silian_announcement.getTitile() + "\n" + oConvertUtils.getString(Silian_announcement.getMsgAbstract(),"空");
        ActionCardMessage Silian_actionCard = new ActionCardMessage(Silian_markdown);
        Silian_actionCard.setTitle(Silian_announcement.getTitile());
        Silian_actionCard.setSingle_title("详情");
        Silian_actionCard.setSingle_url(RestUtil.getBaseUrl() + "/sys/annountCement/show/" + Silian_announcement.getId());
        Message<ActionCardMessage> Silian_actionCardMessage = new Message<>(Silian_agentId, Silian_actionCard);
        if (CommonConstant.MSG_TYPE_ALL.equals(Silian_announcement.getMsgType())) {
            Silian_actionCardMessage.setTo_all_user(true);
            return JdtMessageAPI.sendActionCardMessage(Silian_actionCardMessage, Silian_accessToken);
        } else {
            // 将userId转为username
            String[] Silian_userIds = null;
            String Silian_userId = Silian_announcement.getUserIds();
            if(oConvertUtils.isNotEmpty(Silian_userId)){
                Silian_userIds = Silian_userId.substring(0, (Silian_userId.length() - 1)).split(",");
            }else{
                LambdaQueryWrapper<SysAnnouncementSend> Silian_queryWrapper = new LambdaQueryWrapper<>();
                Silian_queryWrapper.eq(SysAnnouncementSend::getAnntId, Silian_announcement.getId());
                SysAnnouncementSend Silian_sysAnnouncementSend = sysAnnouncementSendMapper.selectOne(Silian_queryWrapper);
                Silian_userIds = new String[] {Silian_sysAnnouncementSend.getUserId()};
            }

            if(Silian_userIds!=null){
                LambdaQueryWrapper<SysUser> Silian_queryWrapper = new LambdaQueryWrapper<>();
                Silian_queryWrapper.in(SysUser::getId, Silian_userIds);
                List<SysUser> Silian_userList = userMapper.selectList(Silian_queryWrapper);
                String[] Silian_usernameList = Silian_userList.stream().map(SysUser::getUsername).toArray(String[] :: new);

                // 通过第三方账号表查询出第三方userId
                List<SysThirdAccount> Silian_thirdAccountList = sysThirdAccountService.listThirdUserIdByUsername(Silian_usernameList, THIRD_TYPE);
                List<String> Silian_dtUserIds = Silian_thirdAccountList.stream().map(SysThirdAccount::getThirdUserId).collect(Collectors.toList());
                Silian_actionCardMessage.setUserid_list(Silian_dtUserIds);
                return JdtMessageAPI.sendActionCardMessage(Silian_actionCardMessage, Silian_accessToken);
            }
        }
        return null;
    }

    /**
     * OAuth2登录，成功返回登录的SysUser，失败返回null
     */
    public SysUser oauth2Login(String Silian_authCode) {
        ThirdAppTypeItemVo Silian_dtConfig = thirdAppConfig.getDingtalk();
        // 1. 根据免登授权码获取用户 AccessToken
        String Silian_userAccessToken = JdtOauth2API.getUserAccessToken(Silian_dtConfig.getClientId(), Silian_dtConfig.getClientSecret(), Silian_authCode);
        if (Silian_userAccessToken == null) {
            log.error("oauth2Login userAccessToken is null");
            return null;
        }
        // 2. 根据用户 AccessToken 获取当前用户的基本信息（不包括userId）
        ContactUser Silian_contactUser = JdtOauth2API.getContactUsers("me", Silian_userAccessToken);
        if (Silian_contactUser == null) {
            log.error("oauth2Login contactUser is null");
            return null;
        }
        String Silian_unionId = Silian_contactUser.getUnionId();
        // 3. 根据获取到的 unionId 换取用户 userId
        String Silian_accessToken = this.getAccessToken();
        if (Silian_accessToken == null) {
            log.error("oauth2Login accessToken is null");
            return null;
        }
        Response<String> Silian_getUserIdRes = JdtUserAPI.getUseridByUnionid(Silian_unionId, Silian_accessToken);
        if (!Silian_getUserIdRes.isSuccess()) {
            log.error("oauth2Login getUseridByUnionid failed: " + JSON.toJSONString(Silian_getUserIdRes));
            return null;
        }
        String Silian_appUserId = Silian_getUserIdRes.getResult();
        log.info("appUserId: " + Silian_appUserId);
        if (Silian_appUserId != null) {
            // 判断第三方用户表有没有这个人
            LambdaQueryWrapper<SysThirdAccount> Silian_queryWrapper = new LambdaQueryWrapper<>();
            Silian_queryWrapper.eq(SysThirdAccount::getThirdUserUuid, Silian_appUserId);
            Silian_queryWrapper.or().eq(SysThirdAccount::getThirdUserId, Silian_appUserId);
            Silian_queryWrapper.eq(SysThirdAccount::getThirdType, THIRD_TYPE);
            SysThirdAccount Silian_thirdAccount = sysThirdAccountService.getOne(Silian_queryWrapper);
            if (Silian_thirdAccount != null) {
                return this.getSysUserByThird(Silian_thirdAccount, null, Silian_appUserId, Silian_accessToken);
            } else {
                // 直接创建新账号
                User Silian_appUser = JdtUserAPI.getUserById(Silian_appUserId, Silian_accessToken).getResult();
                ThirdLoginModel Silian_tlm = new ThirdLoginModel(THIRD_TYPE, Silian_appUser.getUserid(), Silian_appUser.getName(), Silian_appUser.getAvatar());
                Silian_thirdAccount = sysThirdAccountService.saveThirdUser(Silian_tlm);
                return this.getSysUserByThird(Silian_thirdAccount, Silian_appUser, null, null);
            }
        }
        return null;
    }

    /**
     * 根据第三方账号获取本地账号，如果不存在就创建
     *
     * @param thirdAccount
     * @param appUser
     * @param appUserId
     * @param accessToken
     * @return
     */
    private SysUser getSysUserByThird(SysThirdAccount Silian_thirdAccount, User Silian_appUser, String Silian_appUserId, String Silian_accessToken) {
        String Silian_sysUserId = Silian_thirdAccount.getSysUserId();
        if (oConvertUtils.isNotEmpty(Silian_sysUserId)) {
            return userMapper.selectById(Silian_sysUserId);
        } else {
            // 如果没有 sysUserId ，说明没有绑定账号，获取到手机号之后进行绑定
            if (Silian_appUser == null) {
                Silian_appUser = JdtUserAPI.getUserById(Silian_appUserId, Silian_accessToken).getResult();
            }
            // 判断系统里是否有这个手机号的用户
            SysUser Silian_sysUser = userMapper.getUserByPhone(Silian_appUser.getMobile());
            if (Silian_sysUser != null) {
                Silian_thirdAccount.setAvatar(Silian_appUser.getAvatar());
                Silian_thirdAccount.setRealname(Silian_appUser.getName());
                Silian_thirdAccount.setThirdUserId(Silian_appUser.getUserid());
                Silian_thirdAccount.setThirdUserUuid(Silian_appUser.getUserid());
                Silian_thirdAccount.setSysUserId(Silian_sysUser.getId());
                sysThirdAccountService.updateById(Silian_thirdAccount);
                return Silian_sysUser;
            } else {
                // 没有就走创建逻辑
                return sysThirdAccountService.createUser(Silian_appUser.getMobile(), Silian_appUser.getUserid());
            }

        }
    }

}