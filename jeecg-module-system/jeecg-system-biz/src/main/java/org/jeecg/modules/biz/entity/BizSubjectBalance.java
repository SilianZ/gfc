package org.jeecg.modules.biz.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;
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
 * @Description: 主体资源余额
 * @Author: jeecg-boot
 * @Date:   2023-09-24
 * @Version: V1.0
 */
@Data
@TableName("biz_subject_balance")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="biz_subject_balance对象", description="主体资源余额")
public class BizSubjectBalance implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private java.lang.String id;
	/**所属主体*/
	@Excel(name = "所属主体", width = 15, dictTable = "sys_user", dicText = "realname", dicCode = "username")
	@Dict(dictTable = "sys_user", dicText = "realname", dicCode = "username")
    @ApiModelProperty(value = "所属主体")
    private java.lang.String userId;
	/**现金账户*/
	@Excel(name = "现金账户", width = 15)
    @ApiModelProperty(value = "现金账户")
    private java.lang.Double cashAcct;
	/**债权账户*/
	@Excel(name = "债权账户", width = 15)
    @ApiModelProperty(value = "债权账户")
    private java.lang.Double debtClaimAcct;
	/**债务账户*/
	@Excel(name = "债务账户", width = 15)
    @ApiModelProperty(value = "债务账户")
    private java.lang.Double debtLiabilityAcct;
	/**钢铁库存*/
	@Excel(name = "钢铁库存", width = 15)
    @ApiModelProperty(value = "钢铁库存")
    private java.lang.Double steelAcct;
	/**硅石库存*/
	@Excel(name = "硅石库存", width = 15)
    @ApiModelProperty(value = "硅石库存")
    private java.lang.Double silicaAcct;
	/**石油库存*/
	@Excel(name = "石油库存", width = 15)
    @ApiModelProperty(value = "石油库存")
    private java.lang.Double crudeAcct;
	/**塑料库存*/
	@Excel(name = "塑料库存", width = 15)
    @ApiModelProperty(value = "塑料库存")
    private java.lang.Double plasticsAcct;
	/**芯片库存*/
	@Excel(name = "芯片库存", width = 15)
    @ApiModelProperty(value = "芯片库存")
    private java.lang.Double chipAcct;
	/**汽车库存*/
	@Excel(name = "汽车库存", width = 15)
    @ApiModelProperty(value = "汽车库存")
    private java.lang.Double cardAcct;
	/**新能源库存*/
	@Excel(name = "新能源库存", width = 15)
    @ApiModelProperty(value = "新能源库存")
    private java.lang.Double energyAcct;
	/**玩具库存*/
	@Excel(name = "玩具库存", width = 15)
    @ApiModelProperty(value = "玩具库存")
    private java.lang.Double toyAcct;
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
	/**当前财年*/
	@TableField(exist = false)
    private java.lang.Integer yearCode;
}
