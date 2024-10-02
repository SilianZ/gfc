package org.jeecg.modules.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.biz.entity.BizStock;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BizStockMapper extends BaseMapper<BizStock> {
    // Custom SQL queries can be defined here if needed
}
