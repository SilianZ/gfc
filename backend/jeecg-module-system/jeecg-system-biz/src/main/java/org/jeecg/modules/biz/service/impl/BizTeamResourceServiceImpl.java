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
	public void saveMain(BizTeamResource Silian_bizTeamResource, List<BizResourceRights> Silian_bizResourceRightsList) {
		bizTeamResourceMapper.insert(Silian_bizTeamResource);
		List<BizFiscalYear> Silian_bizFiscalYearList = bizFiscalYearService.list();
		for(BizFiscalYear Silian_bizFiscalYear:Silian_bizFiscalYearList){
			BizResourceRights Silian_bizResourceRights = new BizResourceRights();
			if(Silian_bizResourceRightsList.size() > 0 && Silian_bizResourceRightsList.stream().filter(Silian_entity -> Silian_bizFiscalYear.getYearCode().equals(Silian_entity.getYearCode())).count() != 0){
				Silian_bizResourceRights = Silian_bizResourceRightsList.stream().filter(Silian_entity -> Silian_entity.getYearCode().equals(Silian_bizFiscalYear.getYearCode())).findFirst().get();
			}else{
				Silian_bizResourceRights.setYearCode(Silian_bizFiscalYear.getYearCode());
				Silian_bizResourceRights.setUserId(Silian_bizTeamResource.getUserId());
			}
			Silian_bizResourceRights.setResourceName(Silian_bizTeamResource.getResourceName());
			Silian_bizResourceRights.setResourceId(Silian_bizTeamResource.getId());
			if(!Silian_bizResourceRights.getUserId().equals(Silian_bizTeamResource.getUserId()) //如果当前财年已出租，修正状态
					&& bizFiscalYearService.getActiveYearCode().equals(Silian_bizResourceRights.getYearCode())){
				Silian_bizTeamResource.setStatus("CZ");
				bizTeamResourceMapper.updateById(Silian_bizTeamResource);
			}else{
				Silian_bizTeamResource.setStatus("ZC");
				bizTeamResourceMapper.updateById(Silian_bizTeamResource);
			}
			bizResourceRightsMapper.insert(Silian_bizResourceRights);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateMain(BizTeamResource Silian_bizTeamResource,List<BizResourceRights> Silian_bizResourceRightsList) {
		bizTeamResourceMapper.updateById(Silian_bizTeamResource);

		//1.先删除子表数据
		bizResourceRightsMapper.deleteByMainId(Silian_bizTeamResource.getId());

		//2.子表数据重新插入
		List<BizFiscalYear> Silian_bizFiscalYearList = bizFiscalYearService.list();
		for(BizFiscalYear Silian_bizFiscalYear:Silian_bizFiscalYearList){
			BizResourceRights Silian_bizResourceRights = new BizResourceRights();
			if(Silian_bizResourceRightsList.size() > 0 && Silian_bizResourceRightsList.stream().filter(Silian_entity -> Silian_entity.getYearCode().equals(Silian_bizFiscalYear.getYearCode())).count() != 0){
				Silian_bizResourceRights = Silian_bizResourceRightsList.stream().filter(Silian_entity -> Silian_entity.getYearCode().equals(Silian_bizFiscalYear.getYearCode())).findFirst().get();
			}else{
				Silian_bizResourceRights.setYearCode(Silian_bizFiscalYear.getYearCode());
				Silian_bizResourceRights.setUserId(Silian_bizTeamResource.getUserId());
			}
			Silian_bizResourceRights.setResourceName(Silian_bizTeamResource.getResourceName());
			Silian_bizResourceRights.setResourceId(Silian_bizTeamResource.getId());
			if(!Silian_bizResourceRights.getUserId().equals(Silian_bizTeamResource.getUserId()) //如果当前财年已出租，修正状态
					&& bizFiscalYearService.getActiveYearCode().equals(Silian_bizResourceRights.getYearCode())){
				Silian_bizTeamResource.setStatus("CZ");
				bizTeamResourceMapper.updateById(Silian_bizTeamResource);
			}else{
				Silian_bizTeamResource.setStatus("ZC");
				bizTeamResourceMapper.updateById(Silian_bizTeamResource);
			}
			bizResourceRightsMapper.insert(Silian_bizResourceRights);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delMain(String Silian_id) {
		bizResourceRightsMapper.deleteByMainId(Silian_id);
		bizTeamResourceMapper.deleteById(Silian_id);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delBatchMain(Collection<? extends Serializable> Silian_idList) {
		for(Serializable Silian_id:Silian_idList) {
			bizResourceRightsMapper.deleteByMainId(Silian_id.toString());
			bizTeamResourceMapper.deleteById(Silian_id);
		}
	}

}
