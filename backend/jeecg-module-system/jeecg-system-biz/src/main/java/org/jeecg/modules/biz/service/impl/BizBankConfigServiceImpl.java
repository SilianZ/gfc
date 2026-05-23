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

	public BizBankConfig getByDeptId(String Silian_deptId) {
		return baseMapper.getByDeptId(Silian_deptId);
	}

	@Override
	public Double collectTaxes(Double Silian_transAmount, boolean Silian_isTransnational, String Silian_deptId) {
		BizBankConfig Silian_bankConfig = baseMapper.getByDeptId(Silian_deptId);
		Double Silian_tax = 0.00;
		if(Silian_isTransnational){//跨国交易，征收关税
			Silian_tax = Silian_transAmount * (Silian_bankConfig.getTariffRate() == null ? 0.00 : Silian_bankConfig.getTariffRate());
		}else{//国内交易，征收普通税
			Silian_tax = Silian_transAmount * (Silian_bankConfig.getSaveRate() == null ? 0.00 : Silian_bankConfig.getSaveRate());
		}
		BizSubjectBalance Silian_balance = bizSubjectBalanceService.getByUserId(Silian_bankConfig.getUserId());
		Silian_balance.setCashAcct(Silian_balance.getCashAcct() + Silian_tax);
		bizSubjectBalanceService.updateById(Silian_balance);
		return Silian_tax;
	}

	@Override
	public Double collectTaxes(Double Silian_taxAmount, String Silian_deptId) {
		BizBankConfig Silian_bankConfig = baseMapper.getByDeptId(Silian_deptId);
		BizSubjectBalance Silian_balance = bizSubjectBalanceService.getByUserId(Silian_bankConfig.getUserId());
		Silian_balance.setCashAcct(Silian_balance.getCashAcct() + Silian_taxAmount);
		bizSubjectBalanceService.updateById(Silian_balance);
		return Silian_taxAmount;
	}

	@Override
	public Double offTaxes(Double Silian_transAmount, boolean Silian_isTransnational, String Silian_deptId) {
		BizBankConfig Silian_bankConfig = baseMapper.getByDeptId(Silian_deptId);
		Double Silian_tax = 0.00;
		if(Silian_isTransnational){//跨国交易，征收关税
			Silian_tax = Silian_transAmount * (Silian_bankConfig.getTariffRate() == null ? 0.00 : Silian_bankConfig.getTariffRate());
		}else{//国内交易，征收普通税
			Silian_tax = Silian_transAmount * (Silian_bankConfig.getSaveRate() == null ? 0.00 : Silian_bankConfig.getSaveRate());
		}
		BizSubjectBalance Silian_balance = bizSubjectBalanceService.getByUserId(Silian_bankConfig.getUserId());
		Silian_balance.setCashAcct(Silian_balance.getCashAcct() - Silian_tax);
		bizSubjectBalanceService.updateById(Silian_balance);
		return Silian_tax;
	}

	@Override
	public BizBankConfig queryTaxes(Double Silian_taxAmount, String Silian_deptId, boolean Silian_isTransnational) {
		BizBankConfig Silian_bankConfig = baseMapper.getByDeptId(Silian_deptId);
		if(Silian_isTransnational){
			Silian_bankConfig.setTaxAmount(Math.round(Silian_taxAmount * (Silian_bankConfig.getTariffRate() == null ? 0.00 : Silian_bankConfig.getTariffRate()) * 100.0)/100.0);
		}else{
			Silian_bankConfig.setTaxAmount(Math.round(Silian_taxAmount * (Silian_bankConfig.getSaveRate() == null ? 0.00 : Silian_bankConfig.getSaveRate()) * 100.0)/100.0);
		}
		return Silian_bankConfig;
	}

	@Override
	public Double offTaxes(Double Silian_taxAmount, String Silian_deptId) {
		BizBankConfig Silian_bankConfig = baseMapper.getByDeptId(Silian_deptId);
		BizSubjectBalance Silian_balance = bizSubjectBalanceService.getByUserId(Silian_bankConfig.getUserId());
		Silian_balance.setCashAcct(Silian_balance.getCashAcct() - Silian_taxAmount);
		bizSubjectBalanceService.updateById(Silian_balance);
		return Silian_taxAmount;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveMain(BizBankConfig Silian_bizBankConfig, List<BizSubjectBalance> Silian_bizSubjectBalanceList) {
		bizBankConfigMapper.insert(Silian_bizBankConfig);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateMain(BizBankConfig Silian_bizBankConfig,List<BizSubjectBalance> Silian_bizSubjectBalanceList) {
		bizBankConfigMapper.updateById(Silian_bizBankConfig);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delMain(String Silian_id) {
		bizSubjectBalanceMapper.deleteByMainId(Silian_id);
		bizBankConfigMapper.deleteById(Silian_id);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delBatchMain(Collection<? extends Serializable> Silian_idList) {
		for(Serializable Silian_id:Silian_idList) {
			bizSubjectBalanceMapper.deleteByMainId(Silian_id.toString());
			bizBankConfigMapper.deleteById(Silian_id);
		}
	}

}
