package org.jeecg.modules.biz.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 成品加工
 * @Author: jeecg-boot
 * @Date:   2023-09-25
 * @Version: V1.0
 */
@Data
@TableName("biz_production_process")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="biz_production_process对象", description="成品加工")
public class BizProductionProcess implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private java.lang.String id;
	/**所属财年*/
	@Excel(name = "所属财年", width = 15, dictTable = "biz_fiscal_year", dicText = "year_name", dicCode = "year_code")
	@Dict(dictTable = "biz_fiscal_year", dicText = "year_name", dicCode = "year_code")
    @ApiModelProperty(value = "所属财年")
    private java.lang.Integer yearCode;
	/**所属团队*/
	@Excel(name = "所属团队", width = 15, dictTable = "sys_user", dicText = "realname", dicCode = "username")
	@Dict(dictTable = "sys_user", dicText = "realname", dicCode = "username")
    @ApiModelProperty(value = "所属团队")
    private java.lang.String userId;
	/**加工厂*/
	@Excel(name = "加工厂", width = 15, dictTable = "biz_team_resource", dicText = "resource_name", dicCode = "id")
	@Dict(dictTable = "biz_team_resource", dicText = "resource_name", dicCode = "id")
    @ApiModelProperty(value = "加工厂")
    private java.lang.String resourceId;
	/**生产速率*/
	@Excel(name = "生产速率", width = 15)
    @ApiModelProperty(value = "生产速率")
    private java.lang.Double processRate;
	/**芯片数量*/
	@Excel(name = "芯片数量", width = 15)
    @ApiModelProperty(value = "芯片数量")
    private java.lang.Integer chipNumber;
	/**汽车数量*/
	@Excel(name = "汽车数量", width = 15)
    @ApiModelProperty(value = "汽车数量")
    private java.lang.Integer cardNumber;
	/**新能源数量*/
	@Excel(name = "新能源数量", width = 15)
    @ApiModelProperty(value = "新能源数量")
    private java.lang.Integer energyNumber;
	/**玩具数量*/
	@Excel(name = "玩具数量", width = 15)
    @ApiModelProperty(value = "玩具数量")
    private java.lang.Integer toyNumber;
	/**钢铁数量*/
	@Excel(name = "钢铁数量", width = 15)
    @ApiModelProperty(value = "钢铁数量")
    private java.lang.Double gtNumber;
	/**硅石数量*/
	@Excel(name = "硅石数量", width = 15)
    @ApiModelProperty(value = "硅石数量")
    private java.lang.Double gsNumber;
	/**石油数量*/
	@Excel(name = "石油数量", width = 15)
    @ApiModelProperty(value = "石油数量")
    private java.lang.Double syNumber;
	/**塑料数量*/
	@Excel(name = "塑料数量", width = 15)
    @ApiModelProperty(value = "塑料数量")
    private java.lang.Double slNumber;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
    private java.lang.String remark;
	/**创建人*/
    @ApiModelProperty(value = "创建人")
    private java.lang.String createBy;
	/**创建日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建日期")
    private java.util.Date createTime;
	/**更新人*/
    @ApiModelProperty(value = "更新人")
    private java.lang.String updateBy;
	/**更新日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新日期")
    private java.util.Date updateTime;
	/**所属部门*/
    @ApiModelProperty(value = "所属部门")
    private java.lang.String sysOrgCode;
}
