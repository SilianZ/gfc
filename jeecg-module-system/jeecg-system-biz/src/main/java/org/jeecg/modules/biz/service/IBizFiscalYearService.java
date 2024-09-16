package org.jeecg.modules.biz.service;

import org.jeecg.modules.biz.entity.BizFiscalYear;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 财年信息
 * @Author: jeecg-boot
 * @Date: 2023-09-23
 * @Version: V1.0
 */
public interface IBizFiscalYearService extends IService<BizFiscalYear> {
    Integer getMaxYearCode();

    Integer getProcessCount();

    Integer getActiveYearCode();

    void updateResourceStatus(Integer yearCode);
}
