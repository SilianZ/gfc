package org.jeecg.modules.biz.service.impl;

import org.jeecg.modules.biz.entity.BizFiscalYear;
import org.jeecg.modules.biz.entity.BizTeamResource;
import org.jeecg.modules.biz.entity.BizResourceRights;
import org.jeecg.modules.biz.mapper.BizFiscalYearMapper;
import org.jeecg.modules.biz.mapper.BizResourceRightsMapper;
import org.jeecg.modules.biz.mapper.BizTeamResourceMapper;
import org.jeecg.modules.biz.service.IBizFiscalYearService;
import org.jeecg.modules.biz.service.IBizTeamResourceService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.io.Serializable;
import java.util.List;
import java.util.Collection;

/**
 * @Description: 团队资源
 * @Author: jeecg-boot
 * @Date:   2023-09-28
 * @Version: V1.0
 */
@Service
public class BizTeamResourceServiceImpl extends ServiceImpl<BizTeamResourceMapper, BizTeamResource> implements IBizTeamResourceService {

	@Autowired
	private BizTeamResourceMapper bizTeamResourceMapper;
	@Autowired
	private BizResourceRightsMapper bizResourceRightsMapper;
	@Autowired
	private IBizFiscalYearService bizFiscalYearService;
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveMain(BizTeamResource bizTeamResource, List<BizResourceRights> bizResourceRightsList) {
		bizTeamResourceMapper.insert(bizTeamResource);
		List<BizFiscalYear> bizFiscalYearList = bizFiscalYearService.list();
		for(BizFiscalYear bizFiscalYear:bizFiscalYearList){
			BizResourceRights bizResourceRights = new BizResourceRights();
			if(bizResourceRightsList.size() > 0 && bizResourceRightsList.stream().filter(entity -> bizFiscalYear.getYearCode().equals(entity.getYearCode())).count() != 0){
				bizResourceRights = bizResourceRightsList.stream().filter(entity -> entity.getYearCode().equals(bizFiscalYear.getYearCode())).findFirst().get();
			}else{
				bizResourceRights.setYearCode(bizFiscalYear.getYearCode());
				bizResourceRights.setUserId(bizTeamResource.getUserId());
			}
			bizResourceRights.setResourceName(bizTeamResource.getResourceName());
			bizResourceRights.setResourceId(bizTeamResource.getId());
			if(!bizResourceRights.getUserId().equals(bizTeamResource.getUserId()) //如果当前财年已出租，修正状态
					&& bizFiscalYearService.getActiveYearCode().equals(bizResourceRights.getYearCode())){
				bizTeamResource.setStatus("CZ");
				bizTeamResourceMapper.updateById(bizTeamResource);
			}else{
				bizTeamResource.setStatus("ZC");
				bizTeamResourceMapper.updateById(bizTeamResource);
			}
			bizResourceRightsMapper.insert(bizResourceRights);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateMain(BizTeamResource bizTeamResource,List<BizResourceRights> bizResourceRightsList) {
		bizTeamResourceMapper.updateById(bizTeamResource);
		
		//1.先删除子表数据
		bizResourceRightsMapper.deleteByMainId(bizTeamResource.getId());
		
		//2.子表数据重新插入
		List<BizFiscalYear> bizFiscalYearList = bizFiscalYearService.list();
		for(BizFiscalYear bizFiscalYear:bizFiscalYearList){
			BizResourceRights bizResourceRights = new BizResourceRights();
			if(bizResourceRightsList.size() > 0 && bizResourceRightsList.stream().filter(entity -> entity.getYearCode().equals(bizFiscalYear.getYearCode())).count() != 0){
				bizResourceRights = bizResourceRightsList.stream().filter(entity -> entity.getYearCode().equals(bizFiscalYear.getYearCode())).findFirst().get();
			}else{
				bizResourceRights.setYearCode(bizFiscalYear.getYearCode());
				bizResourceRights.setUserId(bizTeamResource.getUserId());
			}
			bizResourceRights.setResourceName(bizTeamResource.getResourceName());
			bizResourceRights.setResourceId(bizTeamResource.getId());
			if(!bizResourceRights.getUserId().equals(bizTeamResource.getUserId()) //如果当前财年已出租，修正状态
					&& bizFiscalYearService.getActiveYearCode().equals(bizResourceRights.getYearCode())){
				bizTeamResource.setStatus("CZ");
				bizTeamResourceMapper.updateById(bizTeamResource);
			}else{
				bizTeamResource.setStatus("ZC");
				bizTeamResourceMapper.updateById(bizTeamResource);
			}
			bizResourceRightsMapper.insert(bizResourceRights);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delMain(String id) {
		bizResourceRightsMapper.deleteByMainId(id);
		bizTeamResourceMapper.deleteById(id);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delBatchMain(Collection<? extends Serializable> idList) {
		for(Serializable id:idList) {
			bizResourceRightsMapper.deleteByMainId(id.toString());
			bizTeamResourceMapper.deleteById(id);
		}
	}
	
}
