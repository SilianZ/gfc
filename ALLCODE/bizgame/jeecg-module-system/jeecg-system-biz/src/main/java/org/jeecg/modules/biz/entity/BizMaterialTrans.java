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
 * @Description: 原材交易
 * @Author: jeecg-boot
 * @Date:   2023-09-27
 * @Version: V1.0
 */
@Data
@TableName("biz_material_trans")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="biz_material_trans对象", description="原材交易")
public class BizMaterialTrans implements Serializable {
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
	/**卖方*/
	@Excel(name = "卖方", width = 15, dictTable = "sys_user", dicText = "realname", dicCode = "username")
	@Dict(dictTable = "sys_user", dicText = "realname", dicCode = "username")
    @ApiModelProperty(value = "卖方")
    private java.lang.String sellerId;
	/**买方*/
	@Excel(name = "买方", width = 15, dictTable = "sys_user", dicText = "realname", dicCode = "username")
	@Dict(dictTable = "sys_user", dicText = "realname", dicCode = "username")
    @ApiModelProperty(value = "买方")
    private java.lang.String buyerId;
	/**是否跨国交易*/
	@Excel(name = "是否跨国交易", width = 15, dicCode = "yes_or_no")
	@Dict(dicCode = "yes_or_no")
    @ApiModelProperty(value = "是否跨国交易")
    private java.lang.String isTransnational;
	/**交易价格*/
	@Excel(name = "交易价格", width = 15)
    @ApiModelProperty(value = "交易价格")
    private java.lang.Double transPrice;
	/**税率*/
	@Excel(name = "税率", width = 15)
    @ApiModelProperty(value = "税率")
    private java.lang.Double taxRate;
	/**税额*/
	@Excel(name = "税额", width = 15)
    @ApiModelProperty(value = "税额")
    private java.lang.Double transTax;
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
