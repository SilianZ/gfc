package org.jeecg.modules.biz.service;

import org.jeecg.modules.biz.entity.BizProductionProcess;
import com.baomidou.mybatisplus.extension.service.IService;
import org.omg.PortableInterceptor.INACTIVE;

/**
 * @Description: 成品加工
 * @Author: jeecg-boot
 * @Date: 2023-09-25
 * @Version: V1.0
 */
public interface IBizProductionProcessService extends IService<BizProductionProcess> {
    Double getProcessNumber(String resourceId, Integer yearCode);
}
