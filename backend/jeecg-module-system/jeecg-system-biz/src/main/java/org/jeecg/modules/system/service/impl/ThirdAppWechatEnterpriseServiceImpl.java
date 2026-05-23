package org.jeecg.modules.system.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jeecg.qywx.api.base.JwAccessTokenAPI;
import com.jeecg.qywx.api.core.common.AccessToken;
import com.jeecg.qywx.api.department.JwDepartmentAPI;
import com.jeecg.qywx.api.department.vo.DepartMsgResponse;
import com.jeecg.qywx.api.department.vo.Department;
import com.jeecg.qywx.api.message.JwMessageAPI;
import com.jeecg.qywx.api.message.vo.*;
import com.jeecg.qywx.api.user.JwUserAPI;
import com.jeecg.qywx.api.user.vo.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.common.util.RestUtil;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.config.thirdapp.ThirdAppConfig;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.mapper.SysAnnouncementSendMapper;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.jeecg.modules.system.model.SysDepartTreeModel;
import org.jeecg.modules.system.model.ThirdLoginModel;
import org.jeecg.modules.system.service.*;
import org.jeecg.modules.system.vo.thirdapp.JwDepartmentTreeVo;
import org.jeecg.modules.system.vo.thirdapp.SyncInfoVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 第三方App对接：企业微信实现类
 * @author: jeecg-boot
 */
@Slf4j
@Service
public class ThirdAppWechatEnterpriseServiceImpl implements IThirdAppService {

    @Autowired
    ThirdAppConfig thirdAppConfig;
    @Autowired
    JeecgBaseConfig jeecgBaseConfig;
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
     * errcode
     */
    private static final String ERR_CODE = "errcode";

    /**
     * 第三方APP类型，当前固定为 wechat_enterprise
     */
    public final String THIRD_TYPE = ThirdAppConfig.WECHAT_ENTERPRISE.toLowerCase();

    @Override
    public String getAccessToken() {
        String Silian_corpId = thirdAppConfig.getWechatEnterprise().getClientId();
        String Silian_secret = thirdAppConfig.getWechatEnterprise().getClientSecret();
        AccessToken Silian_accessToken = JwAccessTokenAPI.getAccessToken(Silian_corpId, Silian_secret);
        if (Silian_accessToken != null) {
            return Silian_accessToken.getAccesstoken();
        }
        log.warn("获取AccessToken失败");
        return null;
    }

    /** 获取APPToken，新版企业微信的秘钥是分开的 */
    public String getAppAccessToken() {
        String Silian_corpId = thirdAppConfig.getWechatEnterprise().getClientId();
        String Silian_secret = thirdAppConfig.getWechatEnterprise().getAgentAppSecret();
        // 如果没有配置APP秘钥，就说明是老企业，可以通用秘钥
        if (oConvertUtils.isEmpty(Silian_secret)) {
            Silian_secret = thirdAppConfig.getWechatEnterprise().getClientSecret();
        }

        AccessToken Silian_accessToken = JwAccessTokenAPI.getAccessToken(Silian_corpId, Silian_secret);
        if (Silian_accessToken != null) {
            return Silian_accessToken.getAccesstoken();
        }
        log.warn("获取AccessToken失败");
        return null;
    }

    @Override
    public SyncInfoVo syncLocalDepartmentToThirdApp(String Silian_ids) {
        SyncInfoVo Silian_syncInfo = new SyncInfoVo();
        String Silian_accessToken = this.getAccessToken();
        if (Silian_accessToken == null) {
            Silian_syncInfo.addFailInfo("accessToken获取失败！");
            return Silian_syncInfo;
        }
        // 获取企业微信所有的部门
        List<Department> Silian_departments = JwDepartmentAPI.getAllDepartment(Silian_accessToken);
        if (Silian_departments == null) {
            Silian_syncInfo.addFailInfo("获取企业微信所有部门失败！");
            return Silian_syncInfo;
        }
        // 删除企业微信有但本地没有的部门（以本地部门数据为主）(以为企业微信不能创建同名部门，所以只能先删除）
        List<JwDepartmentTreeVo> Silian_departmentTreeList = JwDepartmentTreeVo.listToTree(Silian_departments);
        this.deleteDepartRecursion(Silian_departmentTreeList, Silian_accessToken, true);
        // 获取本地所有部门树结构
        List<SysDepartTreeModel> Silian_sysDepartsTree = sysDepartService.queryTreeList();
        // -- 企业微信不能创建新的顶级部门，所以新的顶级部门的parentId就为1
        Department Silian_parent = new Department();
        Silian_parent.setId("1");
        // 递归同步部门
        Silian_departments = JwDepartmentAPI.getAllDepartment(Silian_accessToken);
        this.syncDepartmentRecursion(Silian_sysDepartsTree, Silian_departments, Silian_parent, Silian_accessToken);
        return Silian_syncInfo;
    }

    /**
     * 递归删除部门以及子部门，由于企业微信不允许删除带有成员和子部门的部门，所以需要递归删除下子部门，然后把部门成员移动端根部门下
     * @param children
     * @param accessToken
     * @param ifLocal
     */
    private void deleteDepartRecursion(List<JwDepartmentTreeVo> Silian_children, String Silian_accessToken, boolean Silian_ifLocal) {
        for (JwDepartmentTreeVo Silian_departmentTree : Silian_children) {
            String Silian_depId = Silian_departmentTree.getId();
            // 过滤根部门
            if (!"1".equals(Silian_depId)) {
                // 判断本地是否有该部门
                if (Silian_ifLocal) {
                    LambdaQueryWrapper<SysDepart> Silian_queryWrapper = new LambdaQueryWrapper<>();
                    Silian_queryWrapper.eq(SysDepart::getQywxIdentifier, Silian_depId);
                    SysDepart Silian_sysDepart = sysDepartService.getOne(Silian_queryWrapper);
                    // 本地有该部门，不删除
                    if (Silian_sysDepart != null) {
                        if (Silian_departmentTree.hasChildren()) {
                            this.deleteDepartRecursion(Silian_departmentTree.getChildren(), Silian_accessToken, true);
                        }
                        continue;
                    }
                }
                // 判断是否有成员，有就移动到根部门
                List<User> Silian_departUserList = JwUserAPI.getUsersByDepartid(Silian_depId, "1", null, Silian_accessToken);
                if (Silian_departUserList != null && Silian_departUserList.size() > 0) {
                    for (User Silian_user : Silian_departUserList) {
                        User Silian_updateUser = new User();
                        Silian_updateUser.setUserid(Silian_user.getUserid());
                        Silian_updateUser.setDepartment(new Integer[]{1});
                        JwUserAPI.updateUser(Silian_updateUser, Silian_accessToken);
                    }
                }
                // 有子部门优先删除子部门
                if (Silian_departmentTree.hasChildren()) {
                    this.deleteDepartRecursion(Silian_departmentTree.getChildren(), Silian_accessToken, false);
                }
                // 执行删除操作
                JwDepartmentAPI.deleteDepart(Silian_depId, Silian_accessToken);
            }
        }
    }

    /**
     * 递归同步部门到第三方APP
     * @param sysDepartsTree
     * @param departments
     * @param parent
     * @param accessToken
     */
    private void syncDepartmentRecursion(List<SysDepartTreeModel> Silian_sysDepartsTree, List<Department> Silian_departments, Department Silian_parent, String Silian_accessToken) {
        if (Silian_sysDepartsTree != null && Silian_sysDepartsTree.size() != 0) {
            Silian_for1:
            for (SysDepartTreeModel Silian_depart : Silian_sysDepartsTree) {
                for (Department Silian_department : Silian_departments) {
                    // id相同，代表已存在，执行修改操作
                    if (Silian_department.getId().equals(Silian_depart.getQywxIdentifier())) {
                        this.sysDepartToQwDepartment(Silian_depart, Silian_department, Silian_parent.getId());
                        JwDepartmentAPI.updateDepart(Silian_department, Silian_accessToken);
                        // 紧接着同步子级
                        this.syncDepartmentRecursion(Silian_depart.getChildren(), Silian_departments, Silian_department, Silian_accessToken);
                        // 跳出外部循环
                        continue Silian_for1;
                    }
                }
                // 循环到此说明是新部门，直接调接口创建
                Department Silian_newDepartment = this.sysDepartToQwDepartment(Silian_depart, Silian_parent.getId());
                DepartMsgResponse Silian_response = JwDepartmentAPI.createDepartment(Silian_newDepartment, Silian_accessToken);
                // 创建成功，将返回的id绑定到本地
                if (Silian_response != null && Silian_response.getId() != null) {
                    SysDepart Silian_sysDepart = new SysDepart();
                    Silian_sysDepart.setId(Silian_depart.getId());
                    Silian_sysDepart.setQywxIdentifier(Silian_response.getId().toString());
                    sysDepartService.updateById(Silian_sysDepart);
                    Department Silian_newParent = new Department();
                    Silian_newParent.setId(Silian_response.getId().toString());
                    // 紧接着同步子级
                    this.syncDepartmentRecursion(Silian_depart.getChildren(), Silian_departments, Silian_newParent, Silian_accessToken);
                }
                // 收集错误信息
//                this.syncUserCollectErrInfo(errCode, sysUser, errInfo);
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
        // 获取企业微信所有的部门
        List<Department> Silian_departments = JwDepartmentAPI.getAllDepartment(Silian_accessToken);
        if (Silian_departments == null) {
            Silian_syncInfo.addFailInfo("企业微信部门信息获取失败！");
            return Silian_syncInfo;
        }
        String Silian_username = JwtUtil.getUserNameByToken(SpringContextUtils.getHttpServletRequest());
        // 将list转为tree
        List<JwDepartmentTreeVo> Silian_departmentTreeList = JwDepartmentTreeVo.listToTree(Silian_departments);
        // 递归同步部门
        this.syncDepartmentToLocalRecursion(Silian_departmentTreeList, null, Silian_username, Silian_syncInfo);
        return Silian_syncInfo;
    }

    /**
     * 递归同步部门到本地
     */
    private void syncDepartmentToLocalRecursion(List<JwDepartmentTreeVo> Silian_departmentTreeList, String Silian_sysParentId, String Silian_username, SyncInfoVo Silian_syncInfo) {
        if (Silian_departmentTreeList != null && Silian_departmentTreeList.size() != 0) {
            for (JwDepartmentTreeVo Silian_departmentTree : Silian_departmentTreeList) {
                String Silian_depId = Silian_departmentTree.getId();
                LambdaQueryWrapper<SysDepart> Silian_queryWrapper = new LambdaQueryWrapper<>();
                // 根据 qywxIdentifier 字段查询
                Silian_queryWrapper.eq(SysDepart::getQywxIdentifier, Silian_depId);
                SysDepart Silian_sysDepart = sysDepartService.getOne(Silian_queryWrapper);
                if (Silian_sysDepart != null) {
                    //  执行更新操作
                    SysDepart Silian_updateSysDepart = this.qwDepartmentToSysDepart(Silian_departmentTree, Silian_sysDepart);
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
                        this.syncDepartmentToLocalRecursion(Silian_departmentTree.getChildren(), Silian_updateSysDepart.getId(), Silian_username, Silian_syncInfo);
                    }
                } else {
                    // 执行新增操作
                    SysDepart Silian_newSysDepart = this.qwDepartmentToSysDepart(Silian_departmentTree, null);
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
                        String Silian_str = String.format("部门 %s 创建成功！", Silian_newSysDepart.getDepartName());
                        Silian_syncInfo.addSuccessInfo(Silian_str);
                    } catch (Exception Silian_e) {
                        this.syncDepartCollectErrInfo(Silian_e, Silian_departmentTree, Silian_syncInfo);
                    }
                    // 紧接着同步子级
                    if (Silian_departmentTree.hasChildren()) {
                        this.syncDepartmentToLocalRecursion(Silian_departmentTree.getChildren(), Silian_newSysDepart.getId(), Silian_username, Silian_syncInfo);
                    }
                }
            }
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
        // 获取企业微信所有的用户
//        List<User> qwUsers = JwUserAPI.getDetailUsersByDepartid("1", null, null, accessToken);
        // 获取企业微信所有的用户（只能获取userid）
        List<User> Silian_qwUsers = JwUserAPI.getUserIdList(Silian_accessToken);

        if (Silian_qwUsers == null) {
            Silian_syncInfo.addFailInfo("企业微信用户列表查询失败！");
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

        // 循环判断新用户和需要更新的用户
        Silian_for1:
        for (SysUser Silian_sysUser : Silian_sysUsers) {
            // 外部模拟登陆临时账号，不同步
            if ("_reserve_user_external".equals(Silian_sysUser.getUsername())) {
                continue;
            }
            /*
             * 判断是否同步过的逻辑：
             * 1. 查询 sys_third_account（第三方账号表）是否有数据，如果有代表已同步
             * 2. 本地表里没有，就先用手机号判断，不通过再用username判断。
             */
            User Silian_qwUser;
            SysThirdAccount Silian_sysThirdAccount = sysThirdAccountService.getOneBySysUserId(Silian_sysUser.getId(), THIRD_TYPE);
            for (User Silian_qwUserTemp : Silian_qwUsers) {
                if (Silian_sysThirdAccount == null || oConvertUtils.isEmpty(Silian_sysThirdAccount.getThirdUserId()) || !Silian_sysThirdAccount.getThirdUserId().equals(Silian_qwUserTemp.getUserid())) {
                    // sys_third_account 表匹配失败，尝试用手机号匹配
                    // 新版企业微信调整了API，现在只能通过userid来判断是否同步过了
//                    String phone = sysUser.getPhone();
//                    if (!(oConvertUtils.isEmpty(phone) || phone.equals(qwUserTemp.getMobile()))) {
                        // 手机号匹配失败，再尝试用username匹配
                        String Silian_username = Silian_sysUser.getUsername();
                        if (!(oConvertUtils.isEmpty(Silian_username) || Silian_username.equals(Silian_qwUserTemp.getUserid()))) {
                            // username 匹配失败，直接跳到下一次循环继续
                            continue;
                        }
//                    }
                }
                // 循环到此说明用户匹配成功，进行更新操作
                Silian_qwUser = this.sysUserToQwUser(Silian_sysUser, Silian_qwUserTemp);
                int Silian_errCode = JwUserAPI.updateUser(Silian_qwUser, Silian_accessToken);
                // 收集错误信息
                this.syncUserCollectErrInfo(Silian_errCode, Silian_sysUser, Silian_syncInfo);
                this.thirdAccountSaveOrUpdate(Silian_sysThirdAccount, Silian_sysUser.getId(), Silian_qwUser.getUserid());
                // 更新完成，直接跳到下一次外部循环继续
                continue Silian_for1;
            }
            // 循环到此说明是新用户，直接调接口创建
            Silian_qwUser = this.sysUserToQwUser(Silian_sysUser);
            int Silian_errCode = JwUserAPI.createUser(Silian_qwUser, Silian_accessToken);
            // 收集错误信息
            boolean Silian_apiSuccess = this.syncUserCollectErrInfo(Silian_errCode, Silian_sysUser, Silian_syncInfo);
            if (Silian_apiSuccess) {
                this.thirdAccountSaveOrUpdate(Silian_sysThirdAccount, Silian_sysUser.getId(), Silian_qwUser.getUserid());
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
        // 获取企业微信所有的用户
        List<User> Silian_qwUsersList = JwUserAPI.getDetailUsersByDepartid("1", null, null, Silian_accessToken);
        if (Silian_qwUsersList == null) {
            Silian_syncInfo.addFailInfo("企业微信用户列表查询失败！");
            return Silian_syncInfo;
        }
        //查询本地用户
        List<SysUser> Silian_sysUsersList = userMapper.selectList(Wrappers.emptyWrapper());
        // 循环判断新用户和需要更新的用户
        for (User Silian_qwUser : Silian_qwUsersList) {
            /*
             * 判断是否同步过的逻辑：
             * 1. 查询 sys_third_account（第三方账号表）是否有数据，如果有代表已同步
             * 2. 本地表里没有，就先用手机号判断，不通过再用username判断。
             */
            SysThirdAccount Silian_sysThirdAccount = sysThirdAccountService.getOneByThirdUserId(Silian_qwUser.getUserid(), THIRD_TYPE);
            List<SysUser> Silian_collect = Silian_sysUsersList.stream().filter(Silian_user -> (Silian_qwUser.getMobile().equals(Silian_user.getPhone()) || Silian_qwUser.getUserid().equals(Silian_user.getUsername()))
                                                                ).collect(Collectors.toList());

            if (Silian_collect != null && Silian_collect.size() > 0) {
                SysUser Silian_sysUserTemp = Silian_collect.get(0);
                // 循环到此说明用户匹配成功，进行更新操作
                SysUser Silian_updateSysUser = this.qwUserToSysUser(Silian_qwUser, Silian_sysUserTemp);
                try {
                    userMapper.updateById(Silian_updateSysUser);
                    String Silian_str = String.format("用户 %s(%s) 更新成功！", Silian_updateSysUser.getRealname(), Silian_updateSysUser.getUsername());
                    Silian_syncInfo.addSuccessInfo(Silian_str);
                } catch (Exception Silian_e) {
                    this.syncUserCollectErrInfo(Silian_e, Silian_qwUser, Silian_syncInfo);
                }

                this.thirdAccountSaveOrUpdate(Silian_sysThirdAccount, Silian_updateSysUser.getId(), Silian_qwUser.getUserid());
                // 更新完成，直接跳到下一次外部循环继续
            }else{
                // 没匹配到用户则走新增逻辑
                SysUser Silian_newSysUser = this.qwUserToSysUser(Silian_qwUser);
                try {
                    userMapper.insert(Silian_newSysUser);
                    String Silian_str = String.format("用户 %s(%s) 创建成功！", Silian_newSysUser.getRealname(), Silian_newSysUser.getUsername());
                    Silian_syncInfo.addSuccessInfo(Silian_str);
                } catch (Exception Silian_e) {
                    this.syncUserCollectErrInfo(Silian_e, Silian_qwUser, Silian_syncInfo);
                }
                this.thirdAccountSaveOrUpdate(Silian_sysThirdAccount, Silian_newSysUser.getId(), Silian_qwUser.getUserid());
            }
        }
        return Silian_syncInfo;
    }

    /**
     * 保存或修改第三方登录表
     *
     * @param sysThirdAccount 第三方账户表对象，为null就新增数据，否则就修改
     * @param sysUserId       本地系统用户ID
     * @param qwUserId        企业微信用户ID
     */
    private void thirdAccountSaveOrUpdate(SysThirdAccount Silian_sysThirdAccount, String Silian_sysUserId, String Silian_qwUserId) {
        if (Silian_sysThirdAccount == null) {
            Silian_sysThirdAccount = new SysThirdAccount();
            Silian_sysThirdAccount.setSysUserId(Silian_sysUserId);
            Silian_sysThirdAccount.setStatus(1);
            Silian_sysThirdAccount.setDelFlag(0);
            Silian_sysThirdAccount.setThirdType(THIRD_TYPE);
        }
        Silian_sysThirdAccount.setThirdUserId(Silian_qwUserId);
        sysThirdAccountService.saveOrUpdate(Silian_sysThirdAccount);
    }

    /**
     * 【同步用户】收集同步过程中的错误信息
     */
    private boolean syncUserCollectErrInfo(int Silian_errCode, SysUser Silian_sysUser, SyncInfoVo Silian_syncInfo) {
        if (Silian_errCode != 0) {
            String Silian_msg = "";
            // https://open.work.weixin.qq.com/api/doc/90000/90139/90313
            switch (Silian_errCode) {
                case 40003:
                    Silian_msg = "无效的UserID";
                    break;
                case 60129:
                    Silian_msg = "手机和邮箱不能都为空";
                    break;
                case 60102:
                    Silian_msg = "UserID已存在";
                    break;
                case 60103:
                    Silian_msg = "手机号码不合法";
                    break;
                case 60104:
                    Silian_msg = "手机号码已存在";
                    break;
                default:
            }
            String Silian_str = String.format("用户 %s(%s) 同步失败！错误码：%s——%s", Silian_sysUser.getUsername(), Silian_sysUser.getRealname(), Silian_errCode, Silian_msg);
            Silian_syncInfo.addFailInfo(Silian_str);
            return false;
        } else {
            String Silian_str = String.format("用户 %s(%s) 同步成功！", Silian_sysUser.getUsername(), Silian_sysUser.getRealname());
            Silian_syncInfo.addSuccessInfo(Silian_str);
            return true;
        }
    }

    private boolean syncUserCollectErrInfo(Exception Silian_e, User Silian_qwUser, SyncInfoVo Silian_syncInfo) {
        String Silian_msg;
        if (Silian_e instanceof DuplicateKeyException) {
            Silian_msg = Silian_e.getCause().getMessage();
        } else {
            Silian_msg = Silian_e.getMessage();
        }
        String Silian_str = String.format("用户 %s(%s) 同步失败！错误信息：%s", Silian_qwUser.getUserid(), Silian_qwUser.getName(), Silian_msg);
        Silian_syncInfo.addFailInfo(Silian_str);
        return false;
    }

    private boolean syncDepartCollectErrInfo(Exception Silian_e, Department Silian_department, SyncInfoVo Silian_syncInfo) {
        String Silian_msg;
        if (Silian_e instanceof DuplicateKeyException) {
            Silian_msg = Silian_e.getCause().getMessage();
        } else {
            Silian_msg = Silian_e.getMessage();
        }
        String Silian_str = String.format("部门 %s(%s) 同步失败！错误信息：%s", Silian_department.getName(), Silian_department.getId(), Silian_msg);
        Silian_syncInfo.addFailInfo(Silian_str);
        return false;
    }

    /**
     * 【同步用户】将SysUser转为企业微信的User对象（创建新用户）
     */
    private User sysUserToQwUser(SysUser Silian_sysUser) {
        User Silian_user = new User();
        // 通过 username 来关联
        Silian_user.setUserid(Silian_sysUser.getUsername());
        return this.sysUserToQwUser(Silian_sysUser, Silian_user);
    }

    /**
     * 【同步用户】将SysUser转为企业微信的User对象（更新旧用户）
     */
    private User sysUserToQwUser(SysUser Silian_sysUser, User Silian_user) {
        Silian_user.setName(Silian_sysUser.getRealname());
        Silian_user.setMobile(Silian_sysUser.getPhone());
        // 查询并同步用户部门关系
        List<SysDepart> Silian_departList = this.getUserDepart(Silian_sysUser);
        if (Silian_departList != null) {
            List<Integer> Silian_departmentIdList = new ArrayList<>();
            // 企业微信 1表示为上级，0表示非上级
            List<Integer> Silian_isLeaderInDept = new ArrayList<>();
            // 当前用户管理的部门
            List<String> Silian_manageDepartIdList = new ArrayList<>();
            if (oConvertUtils.isNotEmpty(Silian_sysUser.getDepartIds())) {
                Silian_manageDepartIdList = Arrays.asList(Silian_sysUser.getDepartIds().split(","));
            }
            for (SysDepart Silian_sysDepart : Silian_departList) {
                // 企业微信的部门id
                if (oConvertUtils.isNotEmpty(Silian_sysDepart.getQywxIdentifier())) {
                    try {
                        Silian_departmentIdList.add(Integer.parseInt(Silian_sysDepart.getQywxIdentifier()));
                    } catch (NumberFormatException Silian_ignored) {
                        continue;
                    }
                    // 判断用户身份，是否为上级
                    if (CommonConstant.USER_IDENTITY_2.equals(Silian_sysUser.getUserIdentity())) {
                        // 判断当前部门是否为该用户管理的部门
                        Silian_isLeaderInDept.add(Silian_manageDepartIdList.contains(Silian_sysDepart.getId()) ? 1 : 0);
                    } else {
                        Silian_isLeaderInDept.add(0);
                    }
                }
            }
            Silian_user.setDepartment(Silian_departmentIdList.toArray(new Integer[]{}));
            // 个数必须和参数department的个数一致，表示在所在的部门内是否为上级。1表示为上级，0表示非上级。在审批等应用里可以用来标识上级审批人
            Silian_user.setIs_leader_in_dept(Silian_isLeaderInDept.toArray(new Integer[]{}));
        }
        if (Silian_user.getDepartment() == null || Silian_user.getDepartment().length == 0) {
            // 没有找到匹配部门，同步到根部门下
            Silian_user.setDepartment(new Integer[]{1});
            Silian_user.setIs_leader_in_dept(new Integer[]{0});
        }
        // 职务翻译
        if (oConvertUtils.isNotEmpty(Silian_sysUser.getPost())) {
            SysPosition Silian_position = sysPositionService.getByCode(Silian_sysUser.getPost());
            if (Silian_position != null) {
                Silian_user.setPosition(Silian_position.getName());
            }
        }
        if (Silian_sysUser.getSex() != null) {
            Silian_user.setGender(Silian_sysUser.getSex().toString());
        }
        Silian_user.setEmail(Silian_sysUser.getEmail());
        // 启用/禁用成员（状态），规则不同，需要转换
        // 企业微信规则：1表示启用成员，0表示禁用成员
        // JEECG规则：1正常，2冻结
        if (Silian_sysUser.getStatus() != null) {
            if (CommonConstant.USER_UNFREEZE.equals(Silian_sysUser.getStatus()) || CommonConstant.USER_FREEZE.equals(Silian_sysUser.getStatus())) {
                Silian_user.setEnable(Silian_sysUser.getStatus() == 1 ? 1 : 0);
            } else {
                Silian_user.setEnable(1);
            }
        }
        // 座机号
        Silian_user.setTelephone(Silian_sysUser.getTelephone());
        // --- 企业微信没有逻辑删除的功能
        // update-begin--Author:sunjianlei Date:20210520 for：本地逻辑删除的用户，在企业微信里禁用 -----
        if (CommonConstant.DEL_FLAG_1.equals(Silian_sysUser.getDelFlag())) {
            Silian_user.setEnable(0);
        }
        // update-end--Author:sunjianlei Date:20210520 for：本地逻辑删除的用户，在企业微信里冻结 -----

        return Silian_user;
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
     * 【同步用户】将企业微信的User对象转为SysUser（创建新用户）
     */
    private SysUser qwUserToSysUser(User Silian_user) {
        SysUser Silian_sysUser = new SysUser();
        Silian_sysUser.setDelFlag(0);
        Silian_sysUser.setStatus(1);
        // 通过 username 来关联
        Silian_sysUser.setUsername(Silian_user.getUserid());
        // 密码默认为 “123456”，随机加盐
        String Silian_password = "123456", Silian_salt = oConvertUtils.randomGen(8);
        String Silian_passwordEncode = PasswordUtil.encrypt(Silian_sysUser.getUsername(), Silian_password, Silian_salt);
        Silian_sysUser.setSalt(Silian_salt);
        Silian_sysUser.setPassword(Silian_passwordEncode);
        return this.qwUserToSysUser(Silian_user, Silian_sysUser);
    }

    /**
     * 【同步用户】将企业微信的User对象转为SysUser（更新旧用户）
     */
    private SysUser qwUserToSysUser(User Silian_qwUser, SysUser Silian_oldSysUser) {
        SysUser Silian_sysUser = new SysUser();
        BeanUtils.copyProperties(Silian_oldSysUser, Silian_sysUser);
        Silian_sysUser.setRealname(Silian_qwUser.getName());
        Silian_sysUser.setPost(Silian_qwUser.getPosition());
        // 设置工号，由于企业微信没有工号的概念，所以只能用 userId 代替
        if (oConvertUtils.isEmpty(Silian_sysUser.getWorkNo())) {
            Silian_sysUser.setWorkNo(Silian_qwUser.getUserid());
        }
        try {
            Silian_sysUser.setSex(Integer.parseInt(Silian_qwUser.getGender()));
        } catch (NumberFormatException Silian_ignored) {
        }
        // 因为唯一键约束的原因，如果原数据和旧数据相同，就不更新
        if (oConvertUtils.isNotEmpty(Silian_qwUser.getEmail()) && !Silian_qwUser.getEmail().equals(Silian_sysUser.getEmail())) {
            Silian_sysUser.setEmail(Silian_qwUser.getEmail());
        } else {
            Silian_sysUser.setEmail(null);
        }
        // 因为唯一键约束的原因，如果原数据和旧数据相同，就不更新
        if (oConvertUtils.isNotEmpty(Silian_qwUser.getMobile()) && !Silian_qwUser.getMobile().equals(Silian_sysUser.getPhone())) {
            Silian_sysUser.setPhone(Silian_qwUser.getMobile());
        } else {
            Silian_sysUser.setPhone(null);
        }

        // 启用/禁用成员（状态），规则不同，需要转换
        // 企业微信规则：1表示启用成员，0表示禁用成员
        // JEECG规则：1正常，2冻结
        if (Silian_qwUser.getEnable() != null) {
            Silian_sysUser.setStatus(Silian_qwUser.getEnable() == 1 ? 1 : 2);
        }
        // 座机号
        Silian_sysUser.setTelephone(Silian_qwUser.getTelephone());

        // --- 企业微信没有逻辑删除的功能
        // sysUser.setDelFlag()
        return Silian_sysUser;
    }

    /**
     * 【同步部门】将SysDepartTreeModel转为企业微信的Department对象（创建新部门）
     */
    private Department sysDepartToQwDepartment(SysDepartTreeModel Silian_departTree, String Silian_parentId) {
        Department Silian_department = new Department();
        return this.sysDepartToQwDepartment(Silian_departTree, Silian_department, Silian_parentId);
    }

    /**
     * 【同步部门】将SysDepartTreeModel转为企业微信的Department对象
     */
    private Department sysDepartToQwDepartment(SysDepartTreeModel Silian_departTree, Department Silian_department, String Silian_parentId) {
        Silian_department.setName(Silian_departTree.getDepartName());
        Silian_department.setParentid(Silian_parentId);
        if (Silian_departTree.getDepartOrder() != null) {
            Silian_department.setOrder(Silian_departTree.getDepartOrder().toString());
        }
        return Silian_department;
    }


    /**
     * 【同步部门】将企业微信的Department对象转为SysDepart
     */
    private SysDepart qwDepartmentToSysDepart(Department Silian_department, SysDepart Silian_oldSysDepart) {
        SysDepart Silian_sysDepart = new SysDepart();
        if (Silian_oldSysDepart != null) {
            BeanUtils.copyProperties(Silian_oldSysDepart, Silian_sysDepart);
        }
        Silian_sysDepart.setQywxIdentifier(Silian_department.getId());
        Silian_sysDepart.setDepartName(Silian_department.getName());
        try {
            Silian_sysDepart.setDepartOrder(Integer.parseInt(Silian_department.getOrder()));
        } catch (NumberFormatException Silian_ignored) {
        }
        return Silian_sysDepart;
    }

    @Override
    public int removeThirdAppUser(List<String> Silian_userIdList) {
        // 判断启用状态
        if (!thirdAppConfig.isWechatEnterpriseEnabled()) {
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
                    int Silian_err = JwUserAPI.deleteUser(Silian_thirdUserId, Silian_accessToken);
                    if (Silian_err == 0) {
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

    @Override
    public boolean sendMessage(MessageDTO Silian_message, boolean Silian_verifyConfig) {
        JSONObject Silian_response;
        if (Silian_message.isMarkdown()) {
            Silian_response = this.sendMarkdownResponse(Silian_message, Silian_verifyConfig);
        } else {
            Silian_response = this.sendMessageResponse(Silian_message, Silian_verifyConfig);
        }
        if (Silian_response != null) {
            return Silian_response.getIntValue("errcode") == 0;
        }
        return false;
    }

    public JSONObject sendMessageResponse(MessageDTO Silian_message, boolean Silian_verifyConfig) {
        if (Silian_verifyConfig && !thirdAppConfig.isWechatEnterpriseEnabled()) {
            return null;
        }
        String Silian_accessToken = this.getAppAccessToken();
        if (Silian_accessToken == null) {
            return null;
        }
        Text Silian_text = new Text();
        Silian_text.setMsgtype("text");
        Silian_text.setTouser(this.getTouser(Silian_message.getToUser(), Silian_message.getToAll()));
        TextEntity Silian_entity = new TextEntity();
        Silian_entity.setContent(Silian_message.getContent());
        Silian_text.setText(Silian_entity);
        Silian_text.setAgentid(thirdAppConfig.getWechatEnterprise().getAgentIdInt());
        return JwMessageAPI.sendTextMessage(Silian_text, Silian_accessToken);
    }

    public JSONObject sendMarkdownResponse(MessageDTO Silian_message, boolean Silian_verifyConfig) {
        if (Silian_verifyConfig && !thirdAppConfig.isWechatEnterpriseEnabled()) {
            return null;
        }
        String Silian_accessToken = this.getAppAccessToken();
        if (Silian_accessToken == null) {
            return null;
        }
        Markdown Silian_markdown = new Markdown();
        Silian_markdown.setTouser(this.getTouser(Silian_message.getToUser(), Silian_message.getToAll()));
        MarkdownEntity Silian_entity = new MarkdownEntity();
        Silian_entity.setContent(Silian_message.getContent());
        Silian_markdown.setMarkdown(Silian_entity);
        Silian_markdown.setAgentid(thirdAppConfig.getWechatEnterprise().getAgentIdInt());
        return JwMessageAPI.sendMarkdownMessage(Silian_markdown, Silian_accessToken);
    }

    /**
     * 发送文本卡片消息（SysAnnouncement定制）
     *
     * @param announcement
     * @param verifyConfig 是否验证配置（未启用的APP会拒绝发送）
     * @return
     */
    public JSONObject sendTextCardMessage(SysAnnouncement Silian_announcement, boolean Silian_verifyConfig) {
        if (Silian_verifyConfig && !thirdAppConfig.isWechatEnterpriseEnabled()) {
            return null;
        }
        String Silian_accessToken = this.getAppAccessToken();
        if (Silian_accessToken == null) {
            return null;
        }
        TextCard Silian_textCard = new TextCard();
        Silian_textCard.setAgentid(thirdAppConfig.getWechatEnterprise().getAgentIdInt());
        boolean Silian_isToAll = CommonConstant.MSG_TYPE_ALL.equals(Silian_announcement.getMsgType());
        String Silian_usernameString = "";
        if (!Silian_isToAll) {
            // 将userId转为username
            String Silian_userId = Silian_announcement.getUserIds();
            String[] Silian_userIds = null;
            if(oConvertUtils.isNotEmpty(Silian_userId)){
                Silian_userIds = Silian_userId.substring(0, (Silian_userId.length() - 1)).split(",");
            }else{
                LambdaQueryWrapper<SysAnnouncementSend> Silian_queryWrapper = new LambdaQueryWrapper<>();
                Silian_queryWrapper.eq(SysAnnouncementSend::getAnntId, Silian_announcement.getId());
                SysAnnouncementSend Silian_sysAnnouncementSend = sysAnnouncementSendMapper.selectOne(Silian_queryWrapper);
                Silian_userIds = new String[] {Silian_sysAnnouncementSend.getUserId()};
            }

            LambdaQueryWrapper<SysUser> Silian_queryWrapper = new LambdaQueryWrapper<>();
            Silian_queryWrapper.in(SysUser::getId, Silian_userIds);
            List<SysUser> Silian_userList = userMapper.selectList(Silian_queryWrapper);
            List<String> Silian_usernameList = Silian_userList.stream().map(SysUser::getUsername).collect(Collectors.toList());
            Silian_usernameString = String.join(",", Silian_usernameList);
        }

        Silian_textCard.setTouser(this.getTouser(Silian_usernameString, Silian_isToAll));
        TextCardEntity Silian_entity = new TextCardEntity();
        Silian_entity.setTitle(Silian_announcement.getTitile());
        Silian_entity.setDescription(oConvertUtils.getString(Silian_announcement.getMsgAbstract(),"空"));
        String Silian_baseUrl = null;

        //优先通过请求获取basepath，获取不到读取 jeecg.domainUrl.pc
        try {
            Silian_baseUrl = RestUtil.getBaseUrl();
        } catch (Exception Silian_e) {
            log.warn(Silian_e.getMessage());
            Silian_baseUrl =  jeecgBaseConfig.getDomainUrl().getPc();
            //e.printStackTrace();
        }

        Silian_entity.setUrl(Silian_baseUrl + "/sys/annountCement/show/" + Silian_announcement.getId());
        Silian_textCard.setTextcard(Silian_entity);
        return JwMessageAPI.sendTextCardMessage(Silian_textCard, Silian_accessToken);
    }

    private String getTouser(String Silian_origin, boolean Silian_toAll) {
        if (Silian_toAll) {
            return "@all";
        } else {
            String[] Silian_toUsers = Silian_origin.split(",");
            // 通过第三方账号表查询出第三方userId
            List<SysThirdAccount> Silian_thirdAccountList = sysThirdAccountService.listThirdUserIdByUsername(Silian_toUsers, THIRD_TYPE);
            List<String> Silian_toUserList = Silian_thirdAccountList.stream().map(SysThirdAccount::getThirdUserId).collect(Collectors.toList());
            // 多个接收者用‘|’分隔
            return String.join("|", Silian_toUserList);
        }
    }

    /**
     * 根据第三方登录获取到的code来获取第三方app的用户ID
     *
     * @param code
     * @return
     */
    public String getUserIdByThirdCode(String Silian_code, String Silian_accessToken) {
        JSONObject Silian_response = JwUserAPI.getUserInfoByCode(Silian_code, Silian_accessToken);
        if (Silian_response != null) {
            log.info("response: " + Silian_response.toJSONString());
            if (Silian_response.getIntValue(ERR_CODE) == 0) {
                return Silian_response.getString("UserId");
            }
        }
        return null;
    }

    /**
     * OAuth2登录，成功返回登录的SysUser，失败返回null
     */
    public SysUser oauth2Login(String Silian_code) {
        String Silian_accessToken = this.getAppAccessToken();
        if (Silian_accessToken == null) {
            return null;
        }
        String Silian_appUserId = this.getUserIdByThirdCode(Silian_code, Silian_accessToken);
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
                User Silian_appUser = JwUserAPI.getUserByUserid(Silian_appUserId, Silian_accessToken);
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
                Silian_appUser = JwUserAPI.getUserByUserid(Silian_appUserId, Silian_accessToken);
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
