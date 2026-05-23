package org.jeecg.modules.system.controller;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.base.controller.JeecgController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

 /**
 * @Description: 部门角色
 * @Author: jeecg-boot
 * @Date:   2020-02-12
 * @Version: V1.0
 */
@Slf4j
@Api(tags="部门角色")
@RestController
@RequestMapping("/sys/sysDepartRole")
public class SysDepartRoleController extends JeecgController<SysDepartRole, ISysDepartRoleService> {
	@Autowired
	private ISysDepartRoleService sysDepartRoleService;

	@Autowired
	private ISysDepartRoleUserService departRoleUserService;

	@Autowired
	private ISysDepartPermissionService sysDepartPermissionService;

	 @Autowired
	 private ISysDepartRolePermissionService sysDepartRolePermissionService;

	 @Autowired
	 private ISysDepartService sysDepartService;

	 @Autowired
     private BaseCommonService baseCommonService;

	/**
	 * 分页列表查询
	 *
	 * @param sysDepartRole
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@ApiOperation(value="部门角色-分页列表查询", notes="部门角色-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(SysDepartRole Silian_sysDepartRole,
								   @RequestParam(name="pageNo", defaultValue="1") Integer Silian_pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer Silian_pageSize,
								   @RequestParam(name="deptId",required=false) String Silian_deptId,
								   HttpServletRequest Silian_req) {
		QueryWrapper<SysDepartRole> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_sysDepartRole, Silian_req.getParameterMap());
		Page<SysDepartRole> Silian_page = new Page<SysDepartRole>(Silian_pageNo, Silian_pageSize);
		LoginUser Silian_user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		List<String> Silian_deptIds = null;
//		if(oConvertUtils.isEmpty(deptId)){
//			if(oConvertUtils.isNotEmpty(user.getUserIdentity()) && user.getUserIdentity().equals(CommonConstant.USER_IDENTITY_2) ){
//				deptIds = sysDepartService.getMySubDepIdsByDepId(user.getDepartIds());
//			}else{
//				return Result.ok(null);
//			}
//		}else{
//			deptIds = sysDepartService.getSubDepIdsByDepId(deptId);
//		}
//		queryWrapper.in("depart_id",deptIds);

		//我的部门，选中部门只能看当前部门下的角色
		Silian_queryWrapper.eq("depart_id",Silian_deptId);
		IPage<SysDepartRole> Silian_pageList = sysDepartRoleService.page(Silian_page, Silian_queryWrapper);
		return Result.ok(Silian_pageList);
	}

	/**
	 * 添加
	 *
	 * @param sysDepartRole
	 * @return
	 */
	//@RequiresRoles({"admin"})
	@ApiOperation(value="部门角色-添加", notes="部门角色-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody SysDepartRole Silian_sysDepartRole) {
		sysDepartRoleService.save(Silian_sysDepartRole);
		return Result.ok("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param sysDepartRole
	 * @return
	 */
	//@RequiresRoles({"admin"})
	@ApiOperation(value="部门角色-编辑", notes="部门角色-编辑")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<?> edit(@RequestBody SysDepartRole Silian_sysDepartRole) {
		sysDepartRoleService.updateById(Silian_sysDepartRole);
		return Result.ok("编辑成功!");
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	//@RequiresRoles({"admin"})
	@AutoLog(value = "部门角色-通过id删除")
	@ApiOperation(value="部门角色-通过id删除", notes="部门角色-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String Silian_id) {
		sysDepartRoleService.removeById(Silian_id);
		return Result.ok("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	//@RequiresRoles({"admin"})
	@AutoLog(value = "部门角色-批量删除")
	@ApiOperation(value="部门角色-批量删除", notes="部门角色-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String Silian_ids) {
		this.sysDepartRoleService.removeByIds(Arrays.asList(Silian_ids.split(",")));
		return Result.ok("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@ApiOperation(value="部门角色-通过id查询", notes="部门角色-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String Silian_id) {
		SysDepartRole Silian_sysDepartRole = sysDepartRoleService.getById(Silian_id);
		return Result.ok(Silian_sysDepartRole);
	}

	 /**
	  * 获取部门下角色
	  * @param departId
	  * @return
	  */
	@RequestMapping(value = "/getDeptRoleList", method = RequestMethod.GET)
	public Result<List<SysDepartRole>> getDeptRoleList(@RequestParam(value = "departId") String Silian_departId,@RequestParam(value = "userId") String Silian_userId){
		Result<List<SysDepartRole>> Silian_result = new Result<>();
		//查询选中部门的角色
		List<SysDepartRole> Silian_deptRoleList = sysDepartRoleService.list(new LambdaQueryWrapper<SysDepartRole>().eq(SysDepartRole::getDepartId,Silian_departId));
		Silian_result.setSuccess(true);
		Silian_result.setResult(Silian_deptRoleList);
		return Silian_result;
	}

	 /**
	  * 设置
	  * @param json
	  * @return
	  */
	 //@RequiresRoles({"admin"})
	 @RequestMapping(value = "/deptRoleUserAdd", method = RequestMethod.POST)
	 public Result<?> deptRoleAdd(@RequestBody JSONObject Silian_json) {
		 String Silian_newRoleId = Silian_json.getString("newRoleId");
		 String Silian_oldRoleId = Silian_json.getString("oldRoleId");
		 String Silian_userId = Silian_json.getString("userId");
		 departRoleUserService.deptRoleUserAdd(Silian_userId,Silian_newRoleId,Silian_oldRoleId);
         //update-begin---author:wangshuai ---date:20220316  for：[VUEN-234]部门角色分配添加敏感日志------------
         LoginUser Silian_loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
         baseCommonService.addLog("给部门用户ID："+Silian_userId+"分配角色，操作人： " +Silian_loginUser.getUsername() ,CommonConstant.LOG_TYPE_2, 2);
         //update-end---author:wangshuai ---date:20220316  for：[VUEN-234]部门角色分配添加敏感日志------------
         return Result.ok("添加成功！");
	 }

	 /**
	  * 根据用户id获取已设置部门角色
	  * @param userId
	  * @return
	  */
	 @RequestMapping(value = "/getDeptRoleByUserId", method = RequestMethod.GET)
	 public Result<List<SysDepartRoleUser>> getDeptRoleByUserId(@RequestParam(value = "userId") String Silian_userId,@RequestParam(value = "departId") String Silian_departId){
		 Result<List<SysDepartRoleUser>> Silian_result = new Result<>();
		 //查询部门下角色
		 List<SysDepartRole> Silian_roleList = sysDepartRoleService.list(new QueryWrapper<SysDepartRole>().eq("depart_id",Silian_departId));
		 List<String> Silian_roleIds = Silian_roleList.stream().map(SysDepartRole::getId).collect(Collectors.toList());
		 //根据角色id,用户id查询已授权角色
		 List<SysDepartRoleUser> Silian_roleUserList = null;
		 if(Silian_roleIds!=null && Silian_roleIds.size()>0){
			 Silian_roleUserList = departRoleUserService.list(new QueryWrapper<SysDepartRoleUser>().eq("user_id",Silian_userId).in("drole_id",Silian_roleIds));
		 }
		 Silian_result.setSuccess(true);
		 Silian_result.setResult(Silian_roleUserList);
		 return Silian_result;
	 }

	 /**
	  * 查询数据规则数据
	  */
	 @GetMapping(value = "/datarule/{permissionId}/{departId}/{roleId}")
	 public Result<?> loadDatarule(@PathVariable("permissionId") String Silian_permissionId,@PathVariable("departId") String Silian_departId,@PathVariable("roleId") String Silian_roleId) {
		//查询已授权的部门规则
		List<SysPermissionDataRule> Silian_list = sysDepartPermissionService.getPermRuleListByDeptIdAndPermId(Silian_departId,Silian_permissionId);
		 if(Silian_list==null || Silian_list.size()==0) {
			 return Result.error("未找到权限配置信息");
		 }else {
			 Map<String,Object> Silian_map = new HashMap(5);
			 Silian_map.put("datarule", Silian_list);
			 LambdaQueryWrapper<SysDepartRolePermission> Silian_query = new LambdaQueryWrapper<SysDepartRolePermission>()
					 .eq(SysDepartRolePermission::getPermissionId, Silian_permissionId)
					 .eq(SysDepartRolePermission::getRoleId,Silian_roleId);
			 SysDepartRolePermission Silian_sysRolePermission = sysDepartRolePermissionService.getOne(Silian_query);
			 if(Silian_sysRolePermission==null) {
				 //return Result.error("未找到角色菜单配置信息");
			 }else {
				 String Silian_drChecked = Silian_sysRolePermission.getDataRuleIds();
				 if(oConvertUtils.isNotEmpty(Silian_drChecked)) {
					 Silian_map.put("drChecked", Silian_drChecked.endsWith(",")?Silian_drChecked.substring(0, Silian_drChecked.length()-1):Silian_drChecked);
				 }
			 }
			 return Result.ok(Silian_map);
			 //TODO 以后按钮权限的查询也走这个请求 无非在map中多加两个key
		 }
	 }

	 /**
	  * 保存数据规则至角色菜单关联表
	  */
	 @PostMapping(value = "/datarule")
	 public Result<?> saveDatarule(@RequestBody JSONObject Silian_jsonObject) {
		 try {
			 String Silian_permissionId = Silian_jsonObject.getString("permissionId");
			 String Silian_roleId = Silian_jsonObject.getString("roleId");
			 String Silian_dataRuleIds = Silian_jsonObject.getString("dataRuleIds");
			 log.info("保存数据规则>>"+"菜单ID:"+Silian_permissionId+"角色ID:"+ Silian_roleId+"数据权限ID:"+Silian_dataRuleIds);
			 LambdaQueryWrapper<SysDepartRolePermission> Silian_query = new LambdaQueryWrapper<SysDepartRolePermission>()
					 .eq(SysDepartRolePermission::getPermissionId, Silian_permissionId)
					 .eq(SysDepartRolePermission::getRoleId,Silian_roleId);
			 SysDepartRolePermission Silian_sysRolePermission = sysDepartRolePermissionService.getOne(Silian_query);
			 if(Silian_sysRolePermission==null) {
				 return Result.error("请先保存角色菜单权限!");
			 }else {
				 Silian_sysRolePermission.setDataRuleIds(Silian_dataRuleIds);
				 this.sysDepartRolePermissionService.updateById(Silian_sysRolePermission);
			 }
		 } catch (Exception Silian_e) {
			 log.error("SysRoleController.saveDatarule()发生异常：" + Silian_e.getMessage(),Silian_e);
			 return Result.error("保存失败");
		 }
		 return Result.ok("保存成功!");
	 }

  /**
   * 导出excel
   *
   * @param request
   * @param sysDepartRole
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest Silian_request, SysDepartRole Silian_sysDepartRole) {
      return super.exportXls(Silian_request, Silian_sysDepartRole, SysDepartRole.class, "部门角色");
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
      return super.importExcel(Silian_request, Silian_response, SysDepartRole.class);
  }

}
