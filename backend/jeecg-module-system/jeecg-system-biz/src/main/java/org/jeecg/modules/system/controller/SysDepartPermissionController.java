package org.jeecg.modules.system.controller;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.entity.SysDepartPermission;
import org.jeecg.modules.system.entity.SysDepartRolePermission;
import org.jeecg.modules.system.entity.SysPermission;
import org.jeecg.modules.system.entity.SysPermissionDataRule;
import org.jeecg.modules.system.model.TreeModel;
import org.jeecg.modules.system.service.ISysDepartPermissionService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.system.service.ISysDepartRolePermissionService;
import org.jeecg.modules.system.service.ISysPermissionDataRuleService;
import org.jeecg.modules.system.service.ISysPermissionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

 /**
 * @Description: 部门权限表
 * @Author: jeecg-boot
 * @Date:   2020-02-11
 * @Version: V1.0
 */
@Slf4j
@Api(tags="部门权限表")
@RestController
@RequestMapping("/sys/sysDepartPermission")
public class SysDepartPermissionController extends JeecgController<SysDepartPermission, ISysDepartPermissionService> {
	@Autowired
	private ISysDepartPermissionService sysDepartPermissionService;

	 @Autowired
	 private ISysPermissionDataRuleService sysPermissionDataRuleService;

	 @Autowired
	 private ISysPermissionService sysPermissionService;

	 @Autowired
	 private ISysDepartRolePermissionService sysDepartRolePermissionService;

	 @Autowired
     private BaseCommonService baseCommonService;

	/**
	 * 分页列表查询
	 *
	 * @param sysDepartPermission
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@ApiOperation(value="部门权限表-分页列表查询", notes="部门权限表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(SysDepartPermission Silian_sysDepartPermission,
								   @RequestParam(name="pageNo", defaultValue="1") Integer Silian_pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer Silian_pageSize,
								   HttpServletRequest Silian_req) {
		QueryWrapper<SysDepartPermission> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_sysDepartPermission, Silian_req.getParameterMap());
		Page<SysDepartPermission> Silian_page = new Page<SysDepartPermission>(Silian_pageNo, Silian_pageSize);
		IPage<SysDepartPermission> Silian_pageList = sysDepartPermissionService.page(Silian_page, Silian_queryWrapper);
		return Result.ok(Silian_pageList);
	}

	/**
	 * 添加
	 *
	 * @param sysDepartPermission
	 * @return
	 */
	@ApiOperation(value="部门权限表-添加", notes="部门权限表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody SysDepartPermission Silian_sysDepartPermission) {
		sysDepartPermissionService.save(Silian_sysDepartPermission);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param sysDepartPermission
	 * @return
	 */
	@ApiOperation(value="部门权限表-编辑", notes="部门权限表-编辑")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<?> edit(@RequestBody SysDepartPermission Silian_sysDepartPermission) {
		sysDepartPermissionService.updateById(Silian_sysDepartPermission);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@ApiOperation(value="部门权限表-通过id删除", notes="部门权限表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String Silian_id) {
		sysDepartPermissionService.removeById(Silian_id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@ApiOperation(value="部门权限表-批量删除", notes="部门权限表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String Silian_ids) {
		this.sysDepartPermissionService.removeByIds(Arrays.asList(Silian_ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@ApiOperation(value="部门权限表-通过id查询", notes="部门权限表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String Silian_id) {
		SysDepartPermission Silian_sysDepartPermission = sysDepartPermissionService.getById(Silian_id);
		return Result.ok(Silian_sysDepartPermission);
	}

	/**
	* 导出excel
	*
	* @param request
	* @param sysDepartPermission
	*/
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest Silian_request, SysDepartPermission Silian_sysDepartPermission) {
	  return super.exportXls(Silian_request, Silian_sysDepartPermission, SysDepartPermission.class, "部门权限表");
	}

	/**
	* 通过excel导入数据
	*
	* @param request
	* @param response
	* @return
	*/
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public Result<?> importExcel(HttpServletRequest Silian_request, HttpServletResponse Silian_response) {
	  return super.importExcel(Silian_request, Silian_response, SysDepartPermission.class);
	}

	/**
	* 部门管理授权查询数据规则数据
	*/
	@GetMapping(value = "/datarule/{permissionId}/{departId}")
	public Result<?> loadDatarule(@PathVariable("permissionId") String Silian_permissionId,@PathVariable("departId") String Silian_departId) {
		List<SysPermissionDataRule> Silian_list = sysPermissionDataRuleService.getPermRuleListByPermId(Silian_permissionId);
		if(Silian_list==null || Silian_list.size()==0) {
			return Result.error("未找到权限配置信息");
		}else {
			Map<String,Object> Silian_map = new HashMap(5);
			Silian_map.put("datarule", Silian_list);
			LambdaQueryWrapper<SysDepartPermission> Silian_query = new LambdaQueryWrapper<SysDepartPermission>()
				 .eq(SysDepartPermission::getPermissionId, Silian_permissionId)
				 .eq(SysDepartPermission::getDepartId,Silian_departId);
			SysDepartPermission Silian_sysDepartPermission = sysDepartPermissionService.getOne(Silian_query);
			if(Silian_sysDepartPermission==null) {
			 //return Result.error("未找到角色菜单配置信息");
			}else {
				String Silian_drChecked = Silian_sysDepartPermission.getDataRuleIds();
				if(oConvertUtils.isNotEmpty(Silian_drChecked)) {
					Silian_map.put("drChecked", Silian_drChecked.endsWith(",")?Silian_drChecked.substring(0, Silian_drChecked.length()-1):Silian_drChecked);
				}
			}
			return Result.ok(Silian_map);
			//TODO 以后按钮权限的查询也走这个请求 无非在map中多加两个key
		}
	}

	/**
	* 保存数据规则至部门菜单关联表
	*/
	@PostMapping(value = "/datarule")
	public Result<?> saveDatarule(@RequestBody JSONObject Silian_jsonObject) {
		try {
			String Silian_permissionId = Silian_jsonObject.getString("permissionId");
			String Silian_departId = Silian_jsonObject.getString("departId");
			String Silian_dataRuleIds = Silian_jsonObject.getString("dataRuleIds");
			log.info("保存数据规则>>"+"菜单ID:"+Silian_permissionId+"部门ID:"+ Silian_departId+"数据权限ID:"+Silian_dataRuleIds);
			LambdaQueryWrapper<SysDepartPermission> Silian_query = new LambdaQueryWrapper<SysDepartPermission>()
				 .eq(SysDepartPermission::getPermissionId, Silian_permissionId)
				 .eq(SysDepartPermission::getDepartId,Silian_departId);
			SysDepartPermission Silian_sysDepartPermission = sysDepartPermissionService.getOne(Silian_query);
			if(Silian_sysDepartPermission==null) {
				return Result.error("请先保存部门菜单权限!");
			}else {
				Silian_sysDepartPermission.setDataRuleIds(Silian_dataRuleIds);
				this.sysDepartPermissionService.updateById(Silian_sysDepartPermission);
			}
		} catch (Exception Silian_e) {
			log.error("SysDepartPermissionController.saveDatarule()发生异常：" + Silian_e.getMessage(),Silian_e);
			return Result.error("保存失败");
		}
		return Result.ok("保存成功!");
	}

	 /**
	  * 查询角色授权
	  *
	  * @return
	  */
	 @RequestMapping(value = "/queryDeptRolePermission", method = RequestMethod.GET)
	 public Result<List<String>> queryDeptRolePermission(@RequestParam(name = "roleId", required = true) String Silian_roleId) {
		 Result<List<String>> Silian_result = new Result<>();
		 try {
			 List<SysDepartRolePermission> Silian_list = sysDepartRolePermissionService.list(new QueryWrapper<SysDepartRolePermission>().lambda().eq(SysDepartRolePermission::getRoleId, Silian_roleId));
			 Silian_result.setResult(Silian_list.stream().map(Silian_sysDepartRolePermission -> String.valueOf(Silian_sysDepartRolePermission.getPermissionId())).collect(Collectors.toList()));
			 Silian_result.setSuccess(true);
		 } catch (Exception Silian_e) {
			 log.error(Silian_e.getMessage(), Silian_e);
		 }
		 return Silian_result;
	 }

	 /**
	  * 保存角色授权
	  *
	  * @return
	  */
	 @RequestMapping(value = "/saveDeptRolePermission", method = RequestMethod.POST)
	 public Result<String> saveDeptRolePermission(@RequestBody JSONObject Silian_json) {
		 long Silian_start = System.currentTimeMillis();
		 Result<String> Silian_result = new Result<>();
		 try {
			 String Silian_roleId = Silian_json.getString("roleId");
			 String Silian_permissionIds = Silian_json.getString("permissionIds");
			 String Silian_lastPermissionIds = Silian_json.getString("lastpermissionIds");
			 this.sysDepartRolePermissionService.saveDeptRolePermission(Silian_roleId, Silian_permissionIds, Silian_lastPermissionIds);
			 Silian_result.success("保存成功！");
             //update-begin---author:wangshuai ---date:20220316  for：[VUEN-234]部门角色授权添加敏感日志------------
             LoginUser Silian_loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
             baseCommonService.addLog("修改部门角色ID:"+Silian_roleId+"的权限配置，操作人： " +Silian_loginUser.getUsername() ,CommonConstant.LOG_TYPE_2, 2);
             //update-end---author:wangshuai ---date:20220316  for：[VUEN-234]部门角色授权添加敏感日志------------
             log.info("======部门角色授权成功=====耗时:" + (System.currentTimeMillis() - Silian_start) + "毫秒");
		 } catch (Exception Silian_e) {
			 Silian_result.error500("授权失败！");
			 log.error(Silian_e.getMessage(), Silian_e);
		 }
		 return Silian_result;
	 }

	 /**
	  * 用户角色授权功能，查询菜单权限树
	  * @param request
	  * @return
	  */
	 @RequestMapping(value = "/queryTreeListForDeptRole", method = RequestMethod.GET)
	 public Result<Map<String,Object>> queryTreeListForDeptRole(@RequestParam(name="departId",required=true) String Silian_departId,HttpServletRequest Silian_request) {
		 Result<Map<String,Object>> Silian_result = new Result<>();
		 //全部权限ids
		 List<String> Silian_ids = new ArrayList<>();
		 try {
			 List<SysPermission> Silian_list = sysPermissionService.queryDepartPermissionList(Silian_departId);
			 for(SysPermission Silian_sysPer : Silian_list) {
				 Silian_ids.add(Silian_sysPer.getId());
			 }
			 List<TreeModel> Silian_treeList = new ArrayList<>();
			 getTreeModelList(Silian_treeList, Silian_list, null);
			 Map<String,Object> Silian_resMap = new HashMap(5);
             //全部树节点数据
			 Silian_resMap.put("treeList", Silian_treeList);
             //全部树ids
			 Silian_resMap.put("ids", Silian_ids);
			 Silian_result.setResult(Silian_resMap);
			 Silian_result.setSuccess(true);
		 } catch (Exception Silian_e) {
			 log.error(Silian_e.getMessage(), Silian_e);
		 }
		 return Silian_result;
	 }

	 private void getTreeModelList(List<TreeModel> Silian_treeList, List<SysPermission> Silian_metaList, TreeModel Silian_temp) {
		 for (SysPermission Silian_permission : Silian_metaList) {
			 String Silian_tempPid = Silian_permission.getParentId();
			 TreeModel Silian_tree = new TreeModel(Silian_permission.getId(), Silian_tempPid, Silian_permission.getName(),Silian_permission.getRuleFlag(), Silian_permission.isLeaf());
			 if(Silian_temp==null && oConvertUtils.isEmpty(Silian_tempPid)) {
				 Silian_treeList.add(Silian_tree);
				 if(!Silian_tree.getIsLeaf()) {
					 getTreeModelList(Silian_treeList, Silian_metaList, Silian_tree);
				 }
			 }else if(Silian_temp!=null && Silian_tempPid!=null && Silian_tempPid.equals(Silian_temp.getKey())){
				 Silian_temp.getChildren().add(Silian_tree);
				 if(!Silian_tree.getIsLeaf()) {
					 getTreeModelList(Silian_treeList, Silian_metaList, Silian_tree);
				 }
			 }

		 }
	 }

}
