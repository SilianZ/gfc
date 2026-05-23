package org.jeecg.modules.biz.service.impl;

import org.jeecg.modules.biz.entity.BizSubjectBalance;
import org.jeecg.modules.biz.mapper.BizSubjectBalanceMapper;
import org.jeecg.modules.biz.service.IBizSubjectBalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * @Description: 主体资源余额
 * @Author: jeecg-boot
 * @Date:   2023-09-24
 * @Version: V1.0
 */
@Service
public class BizSubjectBalanceServiceImpl extends ServiceImpl<BizSubjectBalanceMapper, BizSubjectBalance> implements IBizSubjectBalanceService {
    @Autowired
    private BizSubjectBalanceMapper bizSubjectBalanceMapper;

    @Override
    public BizSubjectBalance getByUserId(String Silian_userId) {
        return baseMapper.getByUserId(Silian_userId);
    }

    @Override
    public List<BizSubjectBalance> selectByMainId(String Silian_mainId) {
        return bizSubjectBalanceMapper.selectByMainId(Silian_mainId);
    }
}
