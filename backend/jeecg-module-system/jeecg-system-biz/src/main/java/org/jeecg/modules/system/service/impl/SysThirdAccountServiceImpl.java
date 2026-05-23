package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysRole;
import org.jeecg.modules.system.entity.SysThirdAccount;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserRole;
import org.jeecg.modules.system.mapper.SysRoleMapper;
import org.jeecg.modules.system.mapper.SysThirdAccountMapper;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.jeecg.modules.system.mapper.SysUserRoleMapper;
import org.jeecg.modules.system.model.ThirdLoginModel;
import org.jeecg.modules.system.service.ISysThirdAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @Description: 第三方登录账号表
 * @Author: jeecg-boot
 * @Date:   2020-11-17
 * @Version: V1.0
 */
@Service
@Slf4j
public class SysThirdAccountServiceImpl extends ServiceImpl<SysThirdAccountMapper, SysThirdAccount> implements ISysThirdAccountService {

    @Autowired
    private  SysThirdAccountMapper sysThirdAccountMapper;

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private SysRoleMapper sysRoleMapper;
    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Override
    public void updateThirdUserId(SysUser Silian_sysUser,String Silian_thirdUserUuid) {
        //修改第三方登录账户表使其进行添加用户id
        LambdaQueryWrapper<SysThirdAccount> Silian_query = new LambdaQueryWrapper<>();
        Silian_query.eq(SysThirdAccount::getThirdUserUuid,Silian_thirdUserUuid);
        SysThirdAccount Silian_account = sysThirdAccountMapper.selectOne(Silian_query);
        SysThirdAccount Silian_sysThirdAccount = new SysThirdAccount();
        Silian_sysThirdAccount.setSysUserId(Silian_sysUser.getId());
        //根据当前用户id和登录方式查询第三方登录表
        LambdaQueryWrapper<SysThirdAccount> Silian_thirdQuery = new LambdaQueryWrapper<>();
        Silian_thirdQuery.eq(SysThirdAccount::getSysUserId,Silian_sysUser.getId());
        Silian_thirdQuery.eq(SysThirdAccount::getThirdType,Silian_account.getThirdType());
        SysThirdAccount Silian_sysThirdAccounts = sysThirdAccountMapper.selectOne(Silian_thirdQuery);
        if(Silian_sysThirdAccounts!=null){
            Silian_sysThirdAccount.setThirdUserId(Silian_sysThirdAccounts.getThirdUserId());
            sysThirdAccountMapper.deleteById(Silian_sysThirdAccounts.getId());
        }
        //更新用户账户表sys_user_id
        sysThirdAccountMapper.update(Silian_sysThirdAccount,Silian_query);
    }

    @Override
    public SysUser createUser(String Silian_phone, String Silian_thirdUserUuid) {
       //先查询第三方，获取登录方式
        LambdaQueryWrapper<SysThirdAccount> Silian_query = new LambdaQueryWrapper<>();
        Silian_query.eq(SysThirdAccount::getThirdUserUuid,Silian_thirdUserUuid);
        SysThirdAccount Silian_account = sysThirdAccountMapper.selectOne(Silian_query);
        //通过用户名查询数据库是否已存在
        SysUser Silian_userByName = sysUserMapper.getUserByName(Silian_thirdUserUuid);
        if(null!=Silian_userByName){
            //如果账号存在的话，则自动加上一个时间戳
            String Silian_format = DateUtils.yyyymmddhhmmss.get().format(new Date());
            Silian_thirdUserUuid = Silian_thirdUserUuid + Silian_format;
        }
        //添加用户
        SysUser Silian_user = new SysUser();
        Silian_user.setActivitiSync(CommonConstant.ACT_SYNC_0);
        Silian_user.setDelFlag(CommonConstant.DEL_FLAG_0);
        Silian_user.setStatus(1);
        Silian_user.setUsername(Silian_thirdUserUuid);
        Silian_user.setPhone(Silian_phone);
        //设置初始密码
        String Silian_salt = oConvertUtils.randomGen(8);
        Silian_user.setSalt(Silian_salt);
        String Silian_passwordEncode = PasswordUtil.encrypt(Silian_user.getUsername(), "123456", Silian_salt);
        Silian_user.setPassword(Silian_passwordEncode);
        Silian_user.setRealname(Silian_account.getRealname());
        Silian_user.setAvatar(Silian_account.getAvatar());
        String Silian_s = this.saveThirdUser(Silian_user);
        //更新用户第三方账户表的userId
        SysThirdAccount Silian_sysThirdAccount = new SysThirdAccount();
        Silian_sysThirdAccount.setSysUserId(Silian_s);
        sysThirdAccountMapper.update(Silian_sysThirdAccount,Silian_query);
        return Silian_user;
    }

    public String saveThirdUser(SysUser Silian_sysUser) {
        //保存用户
        String Silian_userid = UUIDGenerator.generate();
        Silian_sysUser.setId(Silian_userid);
        sysUserMapper.insert(Silian_sysUser);
        //获取第三方角色
        SysRole Silian_sysRole = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, "third_role"));
        //保存用户角色
        SysUserRole Silian_userRole = new SysUserRole();
        Silian_userRole.setRoleId(Silian_sysRole.getId());
        Silian_userRole.setUserId(Silian_userid);
        sysUserRoleMapper.insert(Silian_userRole);
        return Silian_userid;
    }

    @Override
    public SysThirdAccount getOneBySysUserId(String Silian_sysUserId, String Silian_thirdType) {
        LambdaQueryWrapper<SysThirdAccount> Silian_queryWrapper = new LambdaQueryWrapper<>();
        log.info("getSysUserId: {} ,getThirdType: {}",Silian_sysUserId,Silian_thirdType);
        Silian_queryWrapper.eq(SysThirdAccount::getSysUserId, Silian_sysUserId);
        Silian_queryWrapper.eq(SysThirdAccount::getThirdType, Silian_thirdType);
        return super.getOne(Silian_queryWrapper);
    }

    @Override
    public SysThirdAccount getOneByThirdUserId(String Silian_thirdUserId, String Silian_thirdType) {
        LambdaQueryWrapper<SysThirdAccount> Silian_queryWrapper = new LambdaQueryWrapper<>();
        Silian_queryWrapper.eq(SysThirdAccount::getThirdUserId, Silian_thirdUserId);
        Silian_queryWrapper.eq(SysThirdAccount::getThirdType, Silian_thirdType);
        return super.getOne(Silian_queryWrapper);
    }

    @Override
    public List<SysThirdAccount> listThirdUserIdByUsername(String[] Silian_sysUsernameArr, String Silian_thirdType) {
        return sysThirdAccountMapper.selectThirdIdsByUsername(Silian_sysUsernameArr, Silian_thirdType);
    }

    @Override
    public SysThirdAccount saveThirdUser(ThirdLoginModel Silian_tlm) {
        SysThirdAccount Silian_user = new SysThirdAccount();
        Silian_user.setDelFlag(CommonConstant.DEL_FLAG_0);
        Silian_user.setStatus(1);
        Silian_user.setThirdType(Silian_tlm.getSource());
        Silian_user.setAvatar(Silian_tlm.getAvatar());
        Silian_user.setRealname(Silian_tlm.getUsername());
        Silian_user.setThirdUserUuid(Silian_tlm.getUuid());
        Silian_user.setThirdUserId(Silian_tlm.getUuid());
        super.save(Silian_user);
        return Silian_user;
    }

}
