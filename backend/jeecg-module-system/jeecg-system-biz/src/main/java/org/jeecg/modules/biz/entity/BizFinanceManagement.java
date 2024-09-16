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
 * @Description: 理财
 * @Author: jeecg-boot
 * @Date: 2024-9-16
 * @Version: V1.0
 */
@Data
@TableName("biz_finance_management")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "biz_finance_management对象", description = "理财")
public class BizFinanceManagement implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private java.lang.String id;
    /** 存入财年 */
    @Excel(name = "存入财年", width = 15, dictTable = "biz_fiscal_year", dicText = "year_name", dicCode = "year_code")
    @Dict(dictTable = "biz_fiscal_year", dicText = "year_name", dicCode = "year_code")
    @ApiModelProperty(value = "存入财年")
    private java.lang.Integer yearCode;
    /** 存入方 */
    @Excel(name = "存入方", width = 15, dictTable = "sys_user", dicText = "realname", dicCode = "username")
    @Dict(dictTable = "sys_user", dicText = "realname", dicCode = "username")
    @ApiModelProperty(value = "存入方")
    private java.lang.String buyerId;
    /** 交易价格 */
    @Excel(name = "交易价格", width = 15)
    @ApiModelProperty(value = "交易价格")
    private java.lang.Double transPrice;
    /** 预计收益 */
    @Excel(name = "预计收益", width = 15)
    @ApiModelProperty(value = "预计收益")
    private java.lang.Double Revenue;
    /** 备注 */
    @Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
    private java.lang.String remark;
    /** 创建人 */
    @ApiModelProperty(value = "创建人")
    private java.lang.String createBy;
    /** 创建日期 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建日期")
    private java.util.Date createTime;
    /** 更新人 */
    @ApiModelProperty(value = "更新人")
    private java.lang.String updateBy;
    /** 更新日期 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新日期")
    private java.util.Date updateTime;
    /** 所属部门 */
    @ApiModelProperty(value = "所属部门")
    private java.lang.String sysOrgCode;
}
