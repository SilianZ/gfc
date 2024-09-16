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
 * @Description: 资源视图
 * @Author: jeecg-boot
 * @Date:   2023-09-30
 * @Version: V1.0
 */
@Data
@TableName("v_country_resource")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="v_country_resource对象", description="资源视图")
public class VCountryResource implements Serializable {
    private static final long serialVersionUID = 1L;

	/**国家*/
	@Excel(name = "国家", width = 15)
    @ApiModelProperty(value = "国家")
    private java.lang.String depId;
	/**团队*/
	@Excel(name = "团队", width = 15)
    @ApiModelProperty(value = "团队")
    @Dict(dictTable = "sys_user", dicText = "realname", dicCode = "username")
    private java.lang.String username;
	/**角色*/
	@Excel(name = "角色", width = 15)
    @ApiModelProperty(value = "角色")
    private java.lang.String post;
	/**资源类型*/
	@Excel(name = "资源类型", width = 15)
    @ApiModelProperty(value = "资源类型")
    @Dict(dicCode = "resource_type")
    private java.lang.String resourceType;
	/**资源名称*/
	@Excel(name = "资源名称", width = 15)
    @ApiModelProperty(value = "资源名称")
    private java.lang.String resourceName;
	/**状态*/
	@Excel(name = "状态", width = 15)
    @ApiModelProperty(value = "状态")
    @Dict(dicCode = "resource_status")
    private java.lang.String status;
}
