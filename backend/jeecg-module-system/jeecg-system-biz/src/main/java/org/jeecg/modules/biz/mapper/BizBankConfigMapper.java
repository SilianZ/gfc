package org.jeecg.modules.biz.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.biz.entity.BizBankConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 银行管理
 * @Author: jeecg-boot
 * @Date:   2023-09-28
 * @Version: V1.0
 */
public interface BizBankConfigMapper extends BaseMapper<BizBankConfig> {
    @Select("select * from biz_bank_config where dept_id = #{deptId}")
    BizBankConfig getByDeptId(String deptId);
}
