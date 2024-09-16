package org.jeecg.modules.biz.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.biz.entity.BizProductionProcess;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 成品加工
 * @Author: jeecg-boot
 * @Date: 2023-09-25
 * @Version: V1.0
 */
public interface BizProductionProcessMapper extends BaseMapper<BizProductionProcess> {
    @Select("SELECT sum(gt_number+gs_number+sy_number+sl_number) processNumber FROM biz_production_process WHERE resource_id = #{resourceId} and year_code = #{yearCode}")
    Double getProcessNumber(@Param("resourceId") String resourceId, @Param("yearCode") Integer yearCode);
}
