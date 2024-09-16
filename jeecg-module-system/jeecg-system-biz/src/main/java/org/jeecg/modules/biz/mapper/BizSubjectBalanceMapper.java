package org.jeecg.modules.biz.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.biz.entity.BizSubjectBalance;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 主体资源余额
 * @Author: jeecg-boot
 * @Date:   2023-09-24
 * @Version: V1.0
 */
public interface BizSubjectBalanceMapper extends BaseMapper<BizSubjectBalance> {
    @Select("select * from biz_subject_balance where user_id = #{userId}")
    BizSubjectBalance getByUserId(String userId);
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
     * @return List<BizSubjectBalance>
     */
    public List<BizSubjectBalance> selectByMainId(@Param("mainId") String mainId);
}
