package org.jeecg.modules.biz.service;

import org.jeecg.modules.biz.entity.BizMinePlan;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 原材料开采计划
 * @Author: jeecg-boot
 * @Date:   2023-09-25
 * @Version: V1.0
 */
public interface IBizMinePlanService extends IService<BizMinePlan> {
    BizMinePlan getByPlanCode(String planCode);
}
