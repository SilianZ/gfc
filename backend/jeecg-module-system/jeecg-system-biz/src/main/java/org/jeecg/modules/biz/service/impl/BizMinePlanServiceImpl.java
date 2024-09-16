package org.jeecg.modules.biz.service.impl;

import org.jeecg.modules.biz.entity.BizMinePlan;
import org.jeecg.modules.biz.mapper.BizMinePlanMapper;
import org.jeecg.modules.biz.service.IBizMinePlanService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 原材料开采计划
 * @Author: jeecg-boot
 * @Date:   2023-09-25
 * @Version: V1.0
 */
@Service
public class BizMinePlanServiceImpl extends ServiceImpl<BizMinePlanMapper, BizMinePlan> implements IBizMinePlanService {

    @Override
    public BizMinePlan getByPlanCode(String planCode) {
        return baseMapper.getByPlanCode(planCode);
    }
}
