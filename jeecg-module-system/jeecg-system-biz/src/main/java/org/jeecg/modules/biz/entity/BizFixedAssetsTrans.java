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
 * @Description: 固定资产交易
 * @Author: jeecg-boot
 * @Date:   2023-09-24
 * @Version: V1.0
 */
@Data
@TableName("biz_fixed_assets_trans")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="biz_fixed_assets_trans对象", description="固定资产交易")
public class BizFixedAssetsTrans implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private java.lang.String id;
	/**财年编号*/
    @Excel(name = "交易标的", width = 15, dictTable = "biz_fiscal_year", dicText = "year_name", dicCode = "year_code")
    @Dict(dictTable = "biz_fiscal_year", dicText = "year_name", dicCode = "year_code")
    @ApiModelProperty(value = "财年编号")
    private java.lang.Integer yearCode;
	/**交易类型*/
	@Excel(name = "交易类型", width = 15, dicCode = "trans_type")
	@Dict(dicCode = "trans_type")
    @ApiModelProperty(value = "交易类型")
    private java.lang.String transType;
	/**交易价格*/
	@Excel(name = "交易价格", width = 15)
    @ApiModelProperty(value = "交易价格")
    private java.lang.Double transPrice;
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
	/**交易标的*/
	@Excel(name = "交易标的", width = 15, dictTable = "biz_team_resource", dicText = "resource_name", dicCode = "id")
    @Dict(dictTable = "biz_team_resource", dicText = "resource_name", dicCode = "id")
    @ApiModelProperty(value = "交易标的")
    private java.lang.String transObject;
	/**所有权/使用权*/
	@Excel(name = "所有权/使用权", width = 15, dicCode = "right_type")
	@Dict(dicCode = "right_type")
    @ApiModelProperty(value = "所有权/使用权")
    private java.lang.String rightType;
	/**使用年限*/
	@Excel(name = "使用年限", width = 15)
    @ApiModelProperty(value = "使用年限")
    private java.lang.Integer yearLimit;
	/**交易附言*/
	@Excel(name = "交易附言", width = 15)
    @ApiModelProperty(value = "交易附言")
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
