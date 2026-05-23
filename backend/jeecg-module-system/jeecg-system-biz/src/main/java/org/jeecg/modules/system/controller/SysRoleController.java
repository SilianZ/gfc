package org.jeecg.modules.system.controller;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.PmsUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysPermission;
import org.jeecg.modules.system.entity.SysPermissionDataRule;
import org.jeecg.modules.system.entity.SysRole;
import org.jeecg.modules.system.entity.SysRolePermission;
import org.jeecg.modules.system.model.TreeModel;
import org.jeecg.modules.system.service.ISysPermissionDataRuleService;
import org.jeecg.modules.system.service.ISysPermissionService;
import org.jeecg.modules.system.service.ISysRolePermissionService;
import org.jeecg.modules.system.service.ISysRoleService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.jeecg.common.system.vo.LoginUser;
import org.apache.shiro.SecurityUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 角色表 前端控制器
 * </p>
 *
 * @Author scott
 * @since 2018-12-19
 */
@RestController
@RequestMapping("/sys/role")
@Slf4j
public class SysRoleController {
	@Autowired
	private ISysRoleService sysRoleService;

	@Autowired
	private ISysPermissionDataRuleService sysPermissionDataRuleService;

	@Autowired
	private ISysRolePermissionService sysRolePermissionService;

	@Autowired
	private ISysPermissionService sysPermissionService;

	/**
	  * 分页列表查询
	 * @param role
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Result<IPage<SysRole>> queryPageList(SysRole Silian_role,
									  @RequestParam(name="pageNo", defaultValue="1") Integer Silian_pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer Silian_pageSize,
									  HttpServletRequest Silian_req) {
		Result<IPage<SysRole>> Silian_result = new Result<IPage<SysRole>>();
		QueryWrapper<SysRole> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_role, Silian_req.getParameterMap());
		Page<SysRole> Silian_page = new Page<SysRole>(Silian_pageNo, Silian_pageSize);
		IPage<SysRole> Silian_pageList = sysRoleService.page(Silian_page, Silian_queryWrapper);
		Silian_result.setSuccess(true);
		Silian_result.setResult(Silian_pageList);
		return Silian_result;
	}

	/**
	  *   添加
	 * @param role
	 * @return
	 */
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	//@RequiresRoles({"admin"})
	public Result<SysRole> add(@RequestBody SysRole Silian_role) {
		Result<SysRole> Silian_result = new Result<SysRole>();
		try {
			Silian_role.setCreateTime(new Date());
			sysRoleService.save(Silian_role);
			Silian_result.success("添加成功！");
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(), Silian_e);
			Silian_result.error500("操作失败");
		}
		return Silian_result;
	}

	/**
	  *  编辑
	 * @param role
	 * @return
	 */
	//@RequiresRoles({"admin"})
	@RequestMapping(value = "/edit",method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<SysRole> edit(@RequestBody SysRole Silian_role) {
		Result<SysRole> Silian_result = new Result<SysRole>();
		SysRole Silian_sysrole = sysRoleService.getById(Silian_role.getId());
		if(Silian_sysrole==null) {
			Silian_result.error500("未找到对应实体");
		}else {
			Silian_role.setUpdateTime(new Date());
			boolean Silian_ok = sysRoleService.updateById(Silian_role);
			//TODO 返回false说明什么？
			if(Silian_ok) {
				Silian_result.success("修改成功!");
			}
		}

		return Silian_result;
	}

	/**
	  *   通过id删除
	 * @param id
	 * @return
	 */
	//@RequiresRoles({"admin"})
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	public Result<?> delete(@RequestParam(name="id",required=true) String Silian_id) {
		sysRoleService.deleteRole(Silian_id);
		return Result.ok("删除角色成功");
	}

	/**
	  *  批量删除
	 * @param ids
	 * @return
	 */
	//@RequiresRoles({"admin"})
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
	public Result<SysRole> deleteBatch(@RequestParam(name="ids",required=true) String Silian_ids) {
		Result<SysRole> Silian_result = new Result<SysRole>();
		if(oConvertUtils.isEmpty(Silian_ids)) {
			Silian_result.error500("未选中角色！");
		}else {
			sysRoleService.deleteBatchRole(Silian_ids.split(","));
			Silian_result.success("删除角色成功!");
		}
		return Silian_result;
	}

	/**
	  * 通过id查询
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/queryById", method = RequestMethod.GET)
	public Result<SysRole> queryById(@RequestParam(name="id",required=true) String Silian_id) {
		Result<SysRole> Silian_result = new Result<SysRole>();
		SysRole Silian_sysrole = sysRoleService.getById(Silian_id);
		if(Silian_sysrole==null) {
			Silian_result.error500("未找到对应实体");
		}else {
			Silian_result.setResult(Silian_sysrole);
			Silian_result.setSuccess(true);
		}
		return Silian_result;
	}

	@RequestMapping(value = "/queryall", method = RequestMethod.GET)
	public Result<List<SysRole>> queryall() {
		Result<List<SysRole>> Silian_result = new Result<>();
		List<SysRole> Silian_list = sysRoleService.list();
		if(Silian_list==null||Silian_list.size()<=0) {
			Silian_result.error500("未找到角色信息");
		}else {
			Silian_result.setResult(Silian_list);
			Silian_result.setSuccess(true);
		}
		return Silian_result;
	}

	/**
	  * 校验角色编码唯一
	 */
	@RequestMapping(value = "/checkRoleCode", method = RequestMethod.GET)
	public Result<Boolean> checkUsername(String Silian_id,String Silian_roleCode) {
		Result<Boolean> Silian_result = new Result<>();
        //如果此参数为false则程序发生异常
		Silian_result.setResult(true);
		log.info("--验证角色编码是否唯一---id:"+Silian_id+"--roleCode:"+Silian_roleCode);
		try {
			SysRole Silian_role = null;
			if(oConvertUtils.isNotEmpty(Silian_id)) {
				Silian_role = sysRoleService.getById(Silian_id);
			}
			SysRole Silian_newRole = sysRoleService.getOne(new QueryWrapper<SysRole>().lambda().eq(SysRole::getRoleCode, Silian_roleCode));
			if(Silian_newRole!=null) {
				//如果根据传入的roleCode查询到信息了，那么就需要做校验了。
				if(Silian_role==null) {
					//role为空=>新增模式=>只要roleCode存在则返回false
					Silian_result.setSuccess(false);
					Silian_result.setMessage("角色编码已存在");
					return Silian_result;
				}else if(!Silian_id.equals(Silian_newRole.getId())) {
					//否则=>编辑模式=>判断两者ID是否一致-
					Silian_result.setSuccess(false);
					Silian_result.setMessage("角色编码已存在");
					return Silian_result;
				}
			}
		} catch (Exception Silian_e) {
			Silian_result.setSuccess(false);
			Silian_result.setResult(false);
			Silian_result.setMessage(Silian_e.getMessage());
			return Silian_result;
		}
		Silian_result.setSuccess(true);
		return Silian_result;
	}

	/**
	 * 导出excel
	 * @param request
	 */
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(SysRole Silian_sysRole,HttpServletRequest Silian_request) {
		// Step.1 组装查询条件
		QueryWrapper<SysRole> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_sysRole, Silian_request.getParameterMap());
		//Step.2 AutoPoi 导出Excel
		ModelAndView Silian_mv = new ModelAndView(new JeecgEntityExcelView());
		List<SysRole> Silian_pageList = sysRoleService.list(Silian_queryWrapper);
		//导出文件名称
		Silian_mv.addObject(NormalExcelConstants.FILE_NAME,"角色列表");
		Silian_mv.addObject(NormalExcelConstants.CLASS,SysRole.class);
		LoginUser Silian_user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		Silian_mv.addObject(NormalExcelConstants.PARAMS,new ExportParams("角色列表数据","导出人:"+Silian_user.getRealname(),"导出信息"));
		Silian_mv.addObject(NormalExcelConstants.DATA_LIST,Silian_pageList);
		return Silian_mv;
	}

	/**
	 * 通过excel导入数据
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public Result<?> importExcel(HttpServletRequest Silian_request, HttpServletResponse Silian_response) {
		MultipartHttpServletRequest Silian_multipartRequest = (MultipartHttpServletRequest) Silian_request;
		Map<String, MultipartFile> Silian_fileMap = Silian_multipartRequest.getFileMap();
		for (Map.Entry<String, MultipartFile> Silian_entity : Silian_fileMap.entrySet()) {
            // 获取上传文件对象
			MultipartFile Silian_file = Silian_entity.getValue();
			ImportParams Silian_params = new ImportParams();
			Silian_params.setTitleRows(2);
			Silian_params.setHeadRows(1);
			Silian_params.setNeedSave(true);
			try {
				return sysRoleService.importExcelCheckRoleCode(Silian_file, Silian_params);
			} catch (Exception Silian_e) {
				log.error(Silian_e.getMessage(), Silian_e);
				return Result.error("文件导入失败:" + Silian_e.getMessage());
			} finally {
				try {
					Silian_file.getInputStream().close();
				} catch (IOException Silian_e) {
					log.error(Silian_e.getMessage(), Silian_e);
				}
			}
		}
		return Result.error("文件导入失败！");
	}

	/**
	 * 查询数据规则数据
	 */
	@GetMapping(value = "/datarule/{permissionId}/{roleId}")
	public Result<?> loadDatarule(@PathVariable("permissionId") String Silian_permissionId,@PathVariable("roleId") String Silian_roleId) {
		List<SysPermissionDataRule> Silian_list = sysPermissionDataRuleService.getPermRuleListByPermId(Silian_permissionId);
		if(Silian_list==null || Silian_list.size()==0) {
			return Result.error("未找到权限配置信息");
		}else {
			Map<String,Object> Silian_map = new HashMap(5);
			Silian_map.put("datarule", Silian_list);
			LambdaQueryWrapper<SysRolePermission> Silian_query = new LambdaQueryWrapper<SysRolePermission>()
					.eq(SysRolePermission::getPermissionId, Silian_permissionId)
					.isNotNull(SysRolePermission::getDataRuleIds)
					.eq(SysRolePermission::getRoleId,Silian_roleId);
			SysRolePermission Silian_sysRolePermission = sysRolePermissionService.getOne(Silian_query);
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
			LambdaQueryWrapper<SysRolePermission> Silian_query = new LambdaQueryWrapper<SysRolePermission>()
					.eq(SysRolePermission::getPermissionId, Silian_permissionId)
					.eq(SysRolePermission::getRoleId,Silian_roleId);
			SysRolePermission Silian_sysRolePermission = sysRolePermissionService.getOne(Silian_query);
			if(Silian_sysRolePermission==null) {
				return Result.error("请先保存角色菜单权限!");
			}else {
				Silian_sysRolePermission.setDataRuleIds(Silian_dataRuleIds);
				this.sysRolePermissionService.updateById(Silian_sysRolePermission);
			}
		} catch (Exception Silian_e) {
			log.error("SysRoleController.saveDatarule()发生异常：" + Silian_e.getMessage(),Silian_e);
			return Result.error("保存失败");
		}
		return Result.ok("保存成功!");
	}


	/**
	 * 用户角色授权功能，查询菜单权限树
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryTreeList", method = RequestMethod.GET)
	public Result<Map<String,Object>> queryTreeList(HttpServletRequest Silian_request) {
		Result<Map<String,Object>> Silian_result = new Result<>();
		//全部权限ids
		List<String> Silian_ids = new ArrayList<>();
		try {
			LambdaQueryWrapper<SysPermission> Silian_query = new LambdaQueryWrapper<SysPermission>();
			Silian_query.eq(SysPermission::getDelFlag, CommonConstant.DEL_FLAG_0);
			Silian_query.orderByAsc(SysPermission::getSortNo);
			List<SysPermission> Silian_list = sysPermissionService.list(Silian_query);
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

	private void getTreeModelList(List<TreeModel> Silian_treeList,List<SysPermission> Silian_metaList,TreeModel Silian_temp) {
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
