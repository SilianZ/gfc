package org.jeecg.modules.biz.mapper;

import java.util.List;
import org.jeecg.modules.biz.entity.BizResourceRights;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Description: 使用权
 * @Author: jeecg-boot
 * @Date:   2023-09-28
 * @Version: V1.0
 */
public interface BizResourceRightsMapper extends BaseMapper<BizResourceRights> {

	/**
	 * 通过主表id删除子表数据
	 *
	 * @param mainId 主表id
	 * @return boolean
	 */
	public boolean deleteByMainId(@Param("mainId") String mainId);

  /**
   * 通过主表id查询子表数据
   *
   * @param mainId 主表id
   * @return List<BizResourceRights>
   */
	public List<BizResourceRights> selectByMainId(@Param("mainId") String mainId);
}
