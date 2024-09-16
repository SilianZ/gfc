package org.jeecg.modules.biz.service;

import org.jeecg.modules.biz.entity.BizSubjectBalance;
import org.jeecg.modules.biz.entity.BizBankConfig;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @Description: 银行管理
 * @Author: jeecg-boot
 * @Date: 2023-09-28
 * @Version: V1.0
 */
public interface IBizBankConfigService extends IService<BizBankConfig> {
    BizBankConfig getByDeptId(String deptId);

    Double collectTaxes(Double transAmount, boolean isTransnational, String deptId);

    Double collectTaxes(Double taxAmount, String deptId);

    Double offTaxes(Double transAmount, boolean isTransnational, String deptId);

    BizBankConfig queryTaxes(Double taxAmount, String deptId, boolean isTransnational);

    Double offTaxes(Double taxAmount, String deptId);

    /**
     * 添加一对多
     *
     * @param bizBankConfig
     * @param bizSubjectBalanceList
     */
    public void saveMain(BizBankConfig bizBankConfig, List<BizSubjectBalance> bizSubjectBalanceList);

    /**
     * 修改一对多
     *
     * @param bizBankConfig
     * @param bizSubjectBalanceList
     */
    public void updateMain(BizBankConfig bizBankConfig, List<BizSubjectBalance> bizSubjectBalanceList);

    /**
     * 删除一对多
     *
     * @param id
     */
    public void delMain(String id);

    /**
     * 批量删除一对多
     *
     * @param idList
     */
    public void delBatchMain(Collection<? extends Serializable> idList);

}
