package org.jeecg.modules.biz.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Description: 团队资源
 * @Author: jeecg-boot
 * @Date:   2023-09-28
 * @Version: V1.0
 */
@ApiModel(value="biz_team_resource对象", description="团队资源")
@Data
@TableName("biz_team_resource")
public class BizTeamResource implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private java.lang.String id;
	/**所属团队*/
	@Excel(name = "所属团队", width = 15, dictTable = "sys_user", dicText = "realname", dicCode = "username")
    @Dict(dictTable = "sys_user", dicText = "realname", dicCode = "username")
    @ApiModelProperty(value = "所属团队")
    private java.lang.String userId;
	/**资源类型*/
	@Excel(name = "资源类型", width = 15, dicCode = "resource_type")
    @Dict(dicCode = "resource_type")
    @ApiModelProperty(value = "资源类型")
    private java.lang.String resourceType;
	/**资源名称*/
	@Excel(name = "资源名称", width = 15)
    @ApiModelProperty(value = "资源名称")
    private java.lang.String resourceName;
	/**所有权*/
	@Excel(name = "所有权", width = 15, dicCode = "yes_or_no")
    @Dict(dicCode = "yes_or_no")
    @ApiModelProperty(value = "所有权")
    private java.lang.String isOwnership;
	/**购买价格*/
	@Excel(name = "购买价格", width = 15)
    @ApiModelProperty(value = "购买价格")
    private java.lang.Double price;
	/**状态*/
	@Excel(name = "状态", width = 15, dicCode = "resource_status")
    @Dict(dicCode = "resource_status")
    @ApiModelProperty(value = "状态")
    private java.lang.String status;
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
    /**生产效率*/
    @TableField(exist = false)
    private java.lang.Double productRate;
    /**是否有银行投资效率提升*/
    @TableField(exist = false)
    private java.lang.Boolean isBankRate;
    /**银行效率*/
    @TableField(exist = false)
    private java.lang.Double bankRate;
    /**当前财年*/
    @TableField(exist = false)
    private java.lang.Integer yearCode;
}
