package org.jeecg.modules.biz.service.impl;

import org.jeecg.modules.biz.entity.BizResourceRights;
import org.jeecg.modules.biz.mapper.BizResourceRightsMapper;
import org.jeecg.modules.biz.service.IBizResourceRightsService;
import org.springframework.stereotype.Service;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Description: 使用权
 * @Author: jeecg-boot
 * @Date:   2023-09-28
 * @Version: V1.0
 */
@Service
public class BizResourceRightsServiceImpl extends ServiceImpl<BizResourceRightsMapper, BizResourceRights> implements IBizResourceRightsService {
	
	@Autowired
	private BizResourceRightsMapper bizResourceRightsMapper;
	
	@Override
	public List<BizResourceRights> selectByMainId(String mainId) {
		return bizResourceRightsMapper.selectByMainId(mainId);
	}
}
