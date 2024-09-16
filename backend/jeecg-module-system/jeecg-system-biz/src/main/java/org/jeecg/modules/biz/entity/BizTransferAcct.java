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
 * @Description: 账户转账
 * @Author: jeecg-boot
 * @Date:   2023-09-29
 * @Version: V1.0
 */
@Data
@TableName("biz_transfer_acct")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="biz_transfer_acct对象", description="账户转账")
public class BizTransferAcct implements Serializable {
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
	/**转账类型*/
	@Excel(name = "转账类型", width = 15, dicCode = "transfer_type")
	@Dict(dicCode = "transfer_type")
    @ApiModelProperty(value = "转账类型")
    private java.lang.String payType;
	/**付款人*/
	@Excel(name = "付款人", width = 15, dictTable = "sys_user", dicText = "realname", dicCode = "username")
	@Dict(dictTable = "sys_user", dicText = "realname", dicCode = "username")
    @ApiModelProperty(value = "付款人")
    private java.lang.String payerId;
	/**收款人*/
	@Excel(name = "收款人", width = 15, dictTable = "sys_user", dicText = "realname", dicCode = "username")
	@Dict(dictTable = "sys_user", dicText = "realname", dicCode = "username")
    @ApiModelProperty(value = "收款人")
    private java.lang.String payeeId;
	/**总金额*/
	@Excel(name = "总金额", width = 15)
    @ApiModelProperty(value = "总金额")
    private java.lang.Double totalAmount;
	/**本金*/
	@Excel(name = "本金", width = 15)
    @ApiModelProperty(value = "本金")
    private java.lang.Double principal;
	/**收入*/
	@Excel(name = "收入", width = 15)
    @ApiModelProperty(value = "收入")
    private java.lang.Double income;
	/**征税类型*/
	@Excel(name = "征税类型", width = 15, dicCode = "tax_type")
	@Dict(dicCode = "tax_type")
    @ApiModelProperty(value = "征税类型")
    private java.lang.String taxType;
	/**适用税率*/
	@Excel(name = "适用税率", width = 15)
    @ApiModelProperty(value = "适用税率")
    private java.lang.Double taxRate;
	/**税额*/
	@Excel(name = "税额", width = 15)
    @ApiModelProperty(value = "税额")
    private java.lang.Double taxAmount;
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
