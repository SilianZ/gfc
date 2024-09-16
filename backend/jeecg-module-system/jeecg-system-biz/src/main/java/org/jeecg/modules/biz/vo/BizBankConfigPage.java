package org.jeecg.modules.biz.vo;

import java.util.List;
import org.jeecg.modules.biz.entity.BizBankConfig;
import org.jeecg.modules.biz.entity.BizSubjectBalance;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecgframework.poi.excel.annotation.ExcelEntity;
import org.jeecgframework.poi.excel.annotation.ExcelCollection;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Date;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Description: 银行管理
 * @Author: jeecg-boot
 * @Date:   2023-09-28
 * @Version: V1.0
 */
@Data
@ApiModel(value="biz_bank_configPage对象", description="银行管理")
public class BizBankConfigPage {

	/**主键*/
	@ApiModelProperty(value = "主键")
    private String id;
	/**国家银行*/
	@Excel(name = "国家银行", width = 15, dictTable = "sys_user", dicText = "realname", dicCode = "username")
    @Dict(dictTable = "sys_user", dicText = "realname", dicCode = "username")
	@ApiModelProperty(value = "国家银行")
    private String userId;
	/**所属国家*/
	@Excel(name = "所属国家", width = 15, dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
    @Dict(dictTable = "sys_depart", dicText = "depart_name", dicCode = "id")
	@ApiModelProperty(value = "所属国家")
    private String deptId;
	/**储蓄率*/
	@Excel(name = "储蓄率", width = 15)
	@ApiModelProperty(value = "储蓄率")
    private Double saveRate;
	/**关税*/
	@Excel(name = "关税", width = 15)
	@ApiModelProperty(value = "关税")
    private Double tariffRate;
	/**是否投资*/
	@Excel(name = "是否投资", width = 15, dicCode = "yes_or_no")
    @Dict(dicCode = "yes_or_no")
	@ApiModelProperty(value = "是否投资")
    private String isInvest;
	/**投资计划*/
	@Excel(name = "投资计划", width = 15, dicCode = "invest_plan")
    @Dict(dicCode = "invest_plan")
	@ApiModelProperty(value = "投资计划")
    private String investPlan;
	/**创建人*/
	@ApiModelProperty(value = "创建人")
    private String createBy;
	/**创建日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "创建日期")
    private Date createTime;
	/**更新人*/
	@ApiModelProperty(value = "更新人")
    private String updateBy;
	/**更新日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "更新日期")
    private Date updateTime;
	/**所属部门*/
	@ApiModelProperty(value = "所属部门")
    private String sysOrgCode;

	@ExcelCollection(name="账户余额")
	@ApiModelProperty(value = "账户余额")
	private List<BizSubjectBalance> bizSubjectBalanceList;

}
