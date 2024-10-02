package org.jeecg.modules.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.biz.entity.BizVirtualCurrency;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BizVirtualCurrencyMapper extends BaseMapper<BizVirtualCurrency> {
    // Custom SQL queries can be defined here if needed
}
