package org.jeecg.modules.biz.service.impl;

import org.jeecg.modules.biz.entity.BizBankConfig;
import org.jeecg.modules.biz.entity.BizSubjectBalance;
import org.jeecg.modules.biz.mapper.BizSubjectBalanceMapper;
import org.jeecg.modules.biz.mapper.BizBankConfigMapper;
import org.jeecg.modules.biz.service.IBizBankConfigService;
import org.jeecg.modules.biz.service.IBizSubjectBalanceService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.io.Serializable;
import java.util.List;
import java.util.Collection;

/**
 * @Description: 银行管理
 * @Author: jeecg-boot
 * @Date:   2023-09-28
 * @Version: V1.0
 */
@Service
public class BizBankConfigServiceImpl extends ServiceImpl<BizBankConfigMapper, BizBankConfig> implements IBizBankConfigService {

	@Autowired
	private BizBankConfigMapper bizBankConfigMapper;

	@Autowired
	private IBizSubjectBalanceService bizSubjectBalanceService;

	@Autowired
	private BizSubjectBalanceMapper bizSubjectBalanceMapper;

	public BizBankConfig getByDeptId(String deptId) {
		return baseMapper.getByDeptId(deptId);
	}

	@Override
	public Double collectTaxes(Double transAmount, boolean isTransnational, String deptId) {
		BizBankConfig bankConfig = baseMapper.getByDeptId(deptId);
		Double tax = 0.00;
		if(isTransnational){//跨国交易，征收关税
			tax = transAmount * (bankConfig.getTariffRate() == null ? 0.00 : bankConfig.getTariffRate());
		}else{//国内交易，征收普通税
			tax = transAmount * (bankConfig.getSaveRate() == null ? 0.00 : bankConfig.getSaveRate());
		}
		BizSubjectBalance balance = bizSubjectBalanceService.getByUserId(bankConfig.getUserId());
		balance.setCashAcct(balance.getCashAcct() + tax);
		bizSubjectBalanceService.updateById(balance);
		return tax;
	}

	@Override
	public Double collectTaxes(Double taxAmount, String deptId) {
		BizBankConfig bankConfig = baseMapper.getByDeptId(deptId);
		BizSubjectBalance balance = bizSubjectBalanceService.getByUserId(bankConfig.getUserId());
		balance.setCashAcct(balance.getCashAcct() + taxAmount);
		bizSubjectBalanceService.updateById(balance);
		return taxAmount;
	}

	@Override
	public Double offTaxes(Double transAmount, boolean isTransnational, String deptId) {
		BizBankConfig bankConfig = baseMapper.getByDeptId(deptId);
		Double tax = 0.00;
		if(isTransnational){//跨国交易，征收关税
			tax = transAmount * (bankConfig.getTariffRate() == null ? 0.00 : bankConfig.getTariffRate());
		}else{//国内交易，征收普通税
			tax = transAmount * (bankConfig.getSaveRate() == null ? 0.00 : bankConfig.getSaveRate());
		}
		BizSubjectBalance balance = bizSubjectBalanceService.getByUserId(bankConfig.getUserId());
		balance.setCashAcct(balance.getCashAcct() - tax);
		bizSubjectBalanceService.updateById(balance);
		return tax;
	}

	@Override
	public BizBankConfig queryTaxes(Double taxAmount, String deptId, boolean isTransnational) {
		BizBankConfig bankConfig = baseMapper.getByDeptId(deptId);
		if(isTransnational){
			bankConfig.setTaxAmount(Math.round(taxAmount * (bankConfig.getTariffRate() == null ? 0.00 : bankConfig.getTariffRate()) * 100.0)/100.0);
		}else{
			bankConfig.setTaxAmount(Math.round(taxAmount * (bankConfig.getSaveRate() == null ? 0.00 : bankConfig.getSaveRate()) * 100.0)/100.0);
		}
		return bankConfig;
	}

	@Override
	public Double offTaxes(Double taxAmount, String deptId) {
		BizBankConfig bankConfig = baseMapper.getByDeptId(deptId);
		BizSubjectBalance balance = bizSubjectBalanceService.getByUserId(bankConfig.getUserId());
		balance.setCashAcct(balance.getCashAcct() - taxAmount);
		bizSubjectBalanceService.updateById(balance);
		return taxAmount;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveMain(BizBankConfig bizBankConfig, List<BizSubjectBalance> bizSubjectBalanceList) {
		bizBankConfigMapper.insert(bizBankConfig);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateMain(BizBankConfig bizBankConfig,List<BizSubjectBalance> bizSubjectBalanceList) {
		bizBankConfigMapper.updateById(bizBankConfig);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delMain(String id) {
		bizSubjectBalanceMapper.deleteByMainId(id);
		bizBankConfigMapper.deleteById(id);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delBatchMain(Collection<? extends Serializable> idList) {
		for(Serializable id:idList) {
			bizSubjectBalanceMapper.deleteByMainId(id.toString());
			bizBankConfigMapper.deleteById(id);
		}
	}
	
}
