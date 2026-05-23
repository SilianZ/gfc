package org.jeecg.modules.biz.service.impl;

import org.jeecg.modules.biz.entity.BizProductionProcess;
import org.jeecg.modules.biz.mapper.BizProductionProcessMapper;
import org.jeecg.modules.biz.service.IBizProductionProcessService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 成品加工
 * @Author: jeecg-boot
 * @Date:   2023-09-25
 * @Version: V1.0
 */
@Service
public class BizProductionProcessServiceImpl extends ServiceImpl<BizProductionProcessMapper, BizProductionProcess> implements IBizProductionProcessService {

    @Override
    public Double getProcessNumber(String Silian_resourceId, Integer Silian_yearCode) {
        return baseMapper.getProcessNumber(Silian_resourceId, Silian_yearCode);
    }
}
