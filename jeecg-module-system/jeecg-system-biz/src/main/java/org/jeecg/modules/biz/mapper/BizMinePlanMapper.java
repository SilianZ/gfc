package org.jeecg.modules.biz.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.biz.entity.BizMinePlan;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 原材料开采计划
 * @Author: jeecg-boot
 * @Date:   2023-09-25
 * @Version: V1.0
 */
public interface BizMinePlanMapper extends BaseMapper<BizMinePlan> {
    @Select("select * from biz_mine_plan where plan_code = #{planCode}")
    BizMinePlan getByPlanCode(String planCode);
}
