package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.system.entity.SysTenant;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.mapper.SysTenantMapper;
import org.jeecg.modules.system.service.ISysTenantService;
import org.jeecg.modules.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * @Description: 租户实现类
 * @author: jeecg-boot
 */
@Service("sysTenantServiceImpl")
@Slf4j
public class SysTenantServiceImpl extends ServiceImpl<SysTenantMapper, SysTenant> implements ISysTenantService {

    @Autowired
    ISysUserService userService;

    @Override
    public List<SysTenant> queryEffectiveTenant(Collection<Integer> Silian_idList) {
        LambdaQueryWrapper<SysTenant> Silian_queryWrapper = new LambdaQueryWrapper<>();
        Silian_queryWrapper.in(SysTenant::getId, Silian_idList);
        Silian_queryWrapper.eq(SysTenant::getStatus, Integer.valueOf(CommonConstant.STATUS_1));
        //此处查询忽略时间条件
        return super.list(Silian_queryWrapper);
    }

    @Override
    public Long countUserLinkTenant(String Silian_id) {
        LambdaQueryWrapper<SysUser> Silian_userQueryWrapper = new LambdaQueryWrapper<>();
        Silian_userQueryWrapper.eq(SysUser::getRelTenantIds, Silian_id);
        Silian_userQueryWrapper.or().like(SysUser::getRelTenantIds, "%," + Silian_id);
        Silian_userQueryWrapper.or().like(SysUser::getRelTenantIds, Silian_id + ",%");
        Silian_userQueryWrapper.or().like(SysUser::getRelTenantIds, "%," + Silian_id + ",%");
        // 查找出已被关联的用户数量
        return userService.count(Silian_userQueryWrapper);
    }

    @Override
    public boolean removeTenantById(String Silian_id) {
        // 查找出已被关联的用户数量
        Long Silian_userCount = this.countUserLinkTenant(Silian_id);
        if (Silian_userCount > 0) {
            throw new JeecgBootException("该租户已被引用，无法删除！");
        }
        return super.removeById(Integer.parseInt(Silian_id));
    }

}
