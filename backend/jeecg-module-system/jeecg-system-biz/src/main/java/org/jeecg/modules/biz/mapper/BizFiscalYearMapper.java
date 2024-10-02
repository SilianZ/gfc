package org.jeecg.modules.biz.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.biz.entity.BizFiscalYear;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 财年信息
 * @Author: jeecg-boot
 * @Date:   2023-09-23
 * @Version: V1.0
 */
public interface BizFiscalYearMapper extends BaseMapper<BizFiscalYear> {

    @Select("select max(year_code) from biz_fiscal_year")
    Integer getMaxYearCode();

    @Select("select count(1) from biz_fiscal_year where status = 1")
    Integer getProcessCount();

    @Select("SELECT * FROM biz_fiscal_year WHERE year_code = #{yearCode}")
    BizFiscalYear getByYearCode(Integer yearCode);

    @Select("select year_code from biz_fiscal_year where status = 1")
    Integer getActiveYearCode();

    @Select("update biz_team_resource t1 inner join " +
            "(select t1.id from biz_team_resource t1 join biz_resource_rights t2 where t1.id = t2.resource_id and t1.user_id = t2.user_id and t2.year_code = #{yearCode}) t2 on t1.id = t2.id " +
            "set t1.status = 'ZC'")
    void updateResourceStatus(Integer yearCode);

    @Select("SELECT * FROM biz_fiscal_year")
    List<BizFiscalYear> getAllFiscalYears();
}
