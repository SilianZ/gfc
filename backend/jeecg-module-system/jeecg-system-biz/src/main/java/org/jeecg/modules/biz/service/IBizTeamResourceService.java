package org.jeecg.modules.biz.service;

import org.jeecg.modules.biz.entity.BizResourceRights;
import org.jeecg.modules.biz.entity.BizTeamResource;
import com.baomidou.mybatisplus.extension.service.IService;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @Description: 团队资源
 * @Author: jeecg-boot
 * @Date:   2023-09-28
 * @Version: V1.0
 */
public interface IBizTeamResourceService extends IService<BizTeamResource> {

	/**
	 * 添加一对多
	 *
	 * @param bizTeamResource
	 * @param bizResourceRightsList
	 */
	public void saveMain(BizTeamResource bizTeamResource,List<BizResourceRights> bizResourceRightsList) ;
	
	/**
	 * 修改一对多
	 *
   * @param bizTeamResource
   * @param bizResourceRightsList
	 */
	public void updateMain(BizTeamResource bizTeamResource,List<BizResourceRights> bizResourceRightsList);
	
	/**
	 * 删除一对多
	 *
	 * @param id
	 */
	public void delMain (String id);
	
	/**
	 * 批量删除一对多
	 *
	 * @param idList
	 */
	public void delBatchMain (Collection<? extends Serializable> idList);
	
}
