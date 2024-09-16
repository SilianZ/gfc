package org.jeecg.modules.biz.service;

import org.jeecg.modules.biz.entity.BizResourceRights;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * @Description: 使用权
 * @Author: jeecg-boot
 * @Date:   2023-09-28
 * @Version: V1.0
 */
public interface IBizResourceRightsService extends IService<BizResourceRights> {

	/**
	 * 通过主表id查询子表数据
	 *
	 * @param mainId 主表id
	 * @return List<BizResourceRights>
	 */
	public List<BizResourceRights> selectByMainId(String mainId);
}
