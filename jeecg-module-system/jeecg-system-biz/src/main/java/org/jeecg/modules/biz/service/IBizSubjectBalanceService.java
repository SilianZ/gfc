package org.jeecg.modules.biz.service;

import org.jeecg.modules.biz.entity.BizSubjectBalance;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 主体资源余额
 * @Author: jeecg-boot
 * @Date:   2023-09-24
 * @Version: V1.0
 */
public interface IBizSubjectBalanceService extends IService<BizSubjectBalance> {

    BizSubjectBalance getByUserId(String userId);

    /**
     * 通过主表id查询子表数据
     *
     * @param mainId 主表id
     * @return List<BizSubjectBalance>
     */
    public List<BizSubjectBalance> selectByMainId(String mainId);
}
