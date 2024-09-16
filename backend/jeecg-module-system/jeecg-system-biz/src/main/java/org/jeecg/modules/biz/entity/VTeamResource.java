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
 * @Date:   2023-09-27
 * @Version: V1.0
 */
@Data
@TableName("v_team_resource")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="v_team_resource对象", description="资源视图")
public class VTeamResource implements Serializable {
    private static final long serialVersionUID = 1L;

	/**dataType*/
	@Excel(name = "dataType", width = 15)
    @ApiModelProperty(value = "dataType")
    private String dataType;
	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "id")
    private String id;
	/**userId*/
	@Excel(name = "userId", width = 15, dictTable = "sys_user", dicText = "realname", dicCode = "username")
	@Dict(dictTable = "sys_user", dicText = "realname", dicCode = "username")
    @ApiModelProperty(value = "userId")
    private String userId;
	/**resourceName*/
	@Excel(name = "resourceName", width = 15)
    @ApiModelProperty(value = "resourceName")
    private String resourceName;
	/**resourceType*/
	@Excel(name = "resourceType", width = 15, dicCode = "resource_type")
	@Dict(dicCode = "resource_type")
    @ApiModelProperty(value = "resourceType")
    private String resourceType;
	/**limitYear*/
	@Excel(name = "limitYear", width = 15, dictTable = "biz_fiscal_year", dicText = "year_name", dicCode = "year_code")
	@Dict(dictTable = "biz_fiscal_year", dicText = "year_name", dicCode = "year_code")
    @ApiModelProperty(value = "limitYear")
    private String limitYear;
	/**status*/
	@Excel(name = "status", width = 15, dicCode = "resource_status")
	@Dict(dicCode = "resource_status")
    @ApiModelProperty(value = "status")
    private String status;
}
