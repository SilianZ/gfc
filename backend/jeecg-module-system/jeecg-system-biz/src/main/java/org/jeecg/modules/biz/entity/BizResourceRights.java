package org.jeecg.modules.biz.entity;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import java.util.Date;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.UnsupportedEncodingException;

/**
 * @Description: 使用权
 * @Author: jeecg-boot
 * @Date:   2023-09-28
 * @Version: V1.0
 */
@ApiModel(value="biz_resource_rights对象", description="使用权")
@Data
@TableName("biz_resource_rights")
public class BizResourceRights implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private java.lang.String id;
	/**资源ID*/
    @ApiModelProperty(value = "资源ID")
    private java.lang.String resourceId;
	/**资源名称*/
	@Excel(name = "资源名称", width = 15)
    @ApiModelProperty(value = "资源名称")
    private java.lang.String resourceName;
	/**财年编号*/
	@Excel(name = "财年编号", width = 15, dictTable = "biz_fiscal_year", dicText = "year_name", dicCode = "year_code")
    @ApiModelProperty(value = "财年编号")
    private java.lang.Integer yearCode;
	/**所有权者*/
	@Excel(name = "所有权者", width = 15, dictTable = "sys_user", dicText = "realname", dicCode = "username")
    @ApiModelProperty(value = "所有权者")
    private java.lang.String userId;
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
