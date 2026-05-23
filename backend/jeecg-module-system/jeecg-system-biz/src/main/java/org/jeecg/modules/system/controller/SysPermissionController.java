package org.jeecg.modules.system.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.Md5Util;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.model.SysPermissionTree;
import org.jeecg.modules.system.model.TreeModel;
import org.jeecg.modules.system.service.*;
import org.jeecg.modules.system.util.PermissionDataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 菜单权限表 前端控制器
 * </p>
 *
 * @Author scott
 * @since 2018-12-21
 */
@Slf4j
@RestController
@RequestMapping("/sys/permission")
public class SysPermissionController {

	@Autowired
	private ISysPermissionService sysPermissionService;

	@Autowired
	private ISysRolePermissionService sysRolePermissionService;

	@Autowired
	private ISysPermissionDataRuleService sysPermissionDataRuleService;

	@Autowired
	private ISysDepartPermissionService sysDepartPermissionService;

	@Autowired
	private ISysUserService sysUserService;

	@Autowired
	private JeecgBaseConfig jeecgBaseConfig;

	@Autowired
    private BaseCommonService baseCommonService;

	@Autowired
	private ISysRoleIndexService sysRoleIndexService;

    /**
     * 子菜单
     */
	private static final String CHILDREN = "children";

	/**
	 * 加载数据节点
	 *
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Result<List<SysPermissionTree>> list(SysPermission Silian_sysPermission, HttpServletRequest Silian_req) {
        long Silian_start = System.currentTimeMillis();
		Result<List<SysPermissionTree>> Silian_result = new Result<>();
		try {
			LambdaQueryWrapper<SysPermission> Silian_query = new LambdaQueryWrapper<SysPermission>();
			Silian_query.eq(SysPermission::getDelFlag, CommonConstant.DEL_FLAG_0);
			Silian_query.orderByAsc(SysPermission::getSortNo);

			//支持通过菜单名字，模糊查询
			if(oConvertUtils.isNotEmpty(Silian_sysPermission.getName())){
				Silian_query.like(SysPermission::getName, Silian_sysPermission.getName());
			}
			List<SysPermission> Silian_list = sysPermissionService.list(Silian_query);
			List<SysPermissionTree> Silian_treeList = new ArrayList<>();

			//如果有菜单名查询条件，则平铺数据 不做上下级
			if(oConvertUtils.isNotEmpty(Silian_sysPermission.getName())){
				if(Silian_list!=null && Silian_list.size()>0){
					Silian_treeList = Silian_list.stream().map(Silian_e -> {
						Silian_e.setLeaf(true);
						return new SysPermissionTree(Silian_e);
					}).collect(Collectors.toList());
				}
			}else{
				getTreeList(Silian_treeList, Silian_list, null);
			}
			Silian_result.setResult(Silian_treeList);
			Silian_result.setSuccess(true);
            log.info("======获取全部菜单数据=====耗时:" + (System.currentTimeMillis() - Silian_start) + "毫秒");
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(), Silian_e);
		}
		return Silian_result;
	}

	/*update_begin author:wuxianquan date:20190908 for:先查询一级菜单，当用户点击展开菜单时加载子菜单 */
	/**
	 * 系统菜单列表(一级菜单)
	 *
	 * @return
	 */
	@RequestMapping(value = "/getSystemMenuList", method = RequestMethod.GET)
	public Result<List<SysPermissionTree>> getSystemMenuList() {
        long Silian_start = System.currentTimeMillis();
		Result<List<SysPermissionTree>> Silian_result = new Result<>();
		try {
			LambdaQueryWrapper<SysPermission> Silian_query = new LambdaQueryWrapper<SysPermission>();
			Silian_query.eq(SysPermission::getMenuType,CommonConstant.MENU_TYPE_0);
			Silian_query.eq(SysPermission::getDelFlag, CommonConstant.DEL_FLAG_0);
			Silian_query.orderByAsc(SysPermission::getSortNo);
			List<SysPermission> Silian_list = sysPermissionService.list(Silian_query);
			List<SysPermissionTree> Silian_sysPermissionTreeList = new ArrayList<SysPermissionTree>();
			for(SysPermission Silian_sysPermission : Silian_list){
				SysPermissionTree Silian_sysPermissionTree = new SysPermissionTree(Silian_sysPermission);
				Silian_sysPermissionTreeList.add(Silian_sysPermissionTree);
			}
			Silian_result.setResult(Silian_sysPermissionTreeList);
			Silian_result.setSuccess(true);
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(), Silian_e);
		}
        log.info("======获取一级菜单数据=====耗时:" + (System.currentTimeMillis() - Silian_start) + "毫秒");
		return Silian_result;
	}

	/**
	 * 查询子菜单
	 * @param parentId
	 * @return
	 */
	@RequestMapping(value = "/getSystemSubmenu", method = RequestMethod.GET)
	public Result<List<SysPermissionTree>> getSystemSubmenu(@RequestParam("parentId") String Silian_parentId){
		Result<List<SysPermissionTree>> Silian_result = new Result<>();
		try{
			LambdaQueryWrapper<SysPermission> Silian_query = new LambdaQueryWrapper<SysPermission>();
			Silian_query.eq(SysPermission::getParentId,Silian_parentId);
			Silian_query.eq(SysPermission::getDelFlag, CommonConstant.DEL_FLAG_0);
			Silian_query.orderByAsc(SysPermission::getSortNo);
			List<SysPermission> Silian_list = sysPermissionService.list(Silian_query);
			List<SysPermissionTree> Silian_sysPermissionTreeList = new ArrayList<SysPermissionTree>();
			for(SysPermission Silian_sysPermission : Silian_list){
				SysPermissionTree Silian_sysPermissionTree = new SysPermissionTree(Silian_sysPermission);
				Silian_sysPermissionTreeList.add(Silian_sysPermissionTree);
			}
			Silian_result.setResult(Silian_sysPermissionTreeList);
			Silian_result.setSuccess(true);
		}catch (Exception Silian_e){
			log.error(Silian_e.getMessage(), Silian_e);
		}
		return Silian_result;
	}
	/*update_end author:wuxianquan date:20190908 for:先查询一级菜单，当用户点击展开菜单时加载子菜单 */

	// update_begin author:sunjianlei date:20200108 for: 新增批量根据父ID查询子级菜单的接口 -------------
	/**
	 * 查询子菜单
	 *
	 * @param parentIds 父ID（多个采用半角逗号分割）
	 * @return 返回 key-value 的 Map
	 */
	@GetMapping("/getSystemSubmenuBatch")
	public Result getSystemSubmenuBatch(@RequestParam("parentIds") String Silian_parentIds) {
		try {
			LambdaQueryWrapper<SysPermission> Silian_query = new LambdaQueryWrapper<>();
			List<String> Silian_parentIdList = Arrays.asList(Silian_parentIds.split(","));
			Silian_query.in(SysPermission::getParentId, Silian_parentIdList);
			Silian_query.eq(SysPermission::getDelFlag, CommonConstant.DEL_FLAG_0);
			Silian_query.orderByAsc(SysPermission::getSortNo);
			List<SysPermission> Silian_list = sysPermissionService.list(Silian_query);
			Map<String, List<SysPermissionTree>> Silian_listMap = new HashMap(5);
			for (SysPermission Silian_item : Silian_list) {
				String Silian_pid = Silian_item.getParentId();
				if (Silian_parentIdList.contains(Silian_pid)) {
					List<SysPermissionTree> Silian_mapList = Silian_listMap.get(Silian_pid);
					if (Silian_mapList == null) {
						Silian_mapList = new ArrayList<>();
					}
					Silian_mapList.add(new SysPermissionTree(Silian_item));
					Silian_listMap.put(Silian_pid, Silian_mapList);
				}
			}
			return Result.ok(Silian_listMap);
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(), Silian_e);
			return Result.error("批量查询子菜单失败：" + Silian_e.getMessage());
		}
	}
	// update_end author:sunjianlei date:20200108 for: 新增批量根据父ID查询子级菜单的接口 -------------

//	/**
//	 * 查询用户拥有的菜单权限和按钮权限（根据用户账号）
//	 *
//	 * @return
//	 */
//	@RequestMapping(value = "/queryByUser", method = RequestMethod.GET)
//	public Result<JSONArray> queryByUser(HttpServletRequest req) {
//		Result<JSONArray> result = new Result<>();
//		try {
//			String username = req.getParameter("username");
//			List<SysPermission> metaList = sysPermissionService.queryByUser(username);
//			JSONArray jsonArray = new JSONArray();
//			this.getPermissionJsonArray(jsonArray, metaList, null);
//			result.setResult(jsonArray);
//			result.success("查询成功");
//		} catch (Exception e) {
//			result.error500("查询失败:" + e.getMessage());
//			log.error(e.getMessage(), e);
//		}
//		return result;
//	}

	/**
	 * 查询用户拥有的菜单权限和按钮权限
	 *
	 * @return
	 */
	@RequestMapping(value = "/getUserPermissionByToken", method = RequestMethod.GET)
	//@DynamicTable(value = DynamicTableConstant.SYS_ROLE_INDEX)
	public Result<?> getUserPermissionByToken(HttpServletRequest Silian_request) {
		Result<JSONObject> Silian_result = new Result<JSONObject>();
		try {
			//直接获取当前用户不适用前端token
			LoginUser Silian_loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			if (oConvertUtils.isEmpty(Silian_loginUser)) {
				return Result.error("请登录系统！");
			}
			List<SysPermission> Silian_metaList = sysPermissionService.queryByUser(Silian_loginUser.getUsername());
			//添加首页路由
			//update-begin-author:taoyan date:20200211 for: TASK #3368 【路由缓存】首页的缓存设置有问题，需要根据后台的路由配置来实现是否缓存
			if(!PermissionDataUtil.hasIndexPage(Silian_metaList)){
				SysPermission Silian_indexMenu = sysPermissionService.list(new LambdaQueryWrapper<SysPermission>().eq(SysPermission::getName,"首页")).get(0);
				Silian_metaList.add(0,Silian_indexMenu);
			}
			//update-end-author:taoyan date:20200211 for: TASK #3368 【路由缓存】首页的缓存设置有问题，需要根据后台的路由配置来实现是否缓存

			//update-begin--Author:zyf Date:20220425  for:自定义首页地址 LOWCOD-1578
			String Silian_version = Silian_request.getHeader(CommonConstant.VERSION);
			//update-begin---author:liusq ---date:2022-06-29  for：接口返回值修改，同步修改这里的判断逻辑-----------
			SysRoleIndex Silian_roleIndex= sysUserService.getDynamicIndexByUserRole(Silian_loginUser.getUsername(),Silian_version);
			//update-end---author:liusq ---date:2022-06-29  for：接口返回值修改，同步修改这里的判断逻辑-----------
			//update-end--Author:zyf  Date:20220425  for：自定义首页地址 LOWCOD-1578

			if(Silian_roleIndex!=null){
				List<SysPermission> Silian_menus = Silian_metaList.stream().filter(Silian_sysPermission -> "首页".equals(Silian_sysPermission.getName())).collect(Collectors.toList());
				//update-begin---author:liusq ---date:2022-06-29  for：设置自定义首页地址和组件----------
				String Silian_component = Silian_roleIndex.getComponent();
				String Silian_routeUrl = Silian_roleIndex.getUrl();
				boolean Silian_route = Silian_roleIndex.isRoute();
				if(oConvertUtils.isNotEmpty(Silian_routeUrl)){
					Silian_menus.get(0).setComponent(Silian_component);
					Silian_menus.get(0).setRoute(Silian_route);
					Silian_menus.get(0).setUrl(Silian_routeUrl);
				}else{
					Silian_menus.get(0).setComponent(Silian_component);
				}
				//update-end---author:liusq ---date:2022-06-29  for：设置自定义首页地址和组件-----------
			}

			JSONObject Silian_json = new JSONObject();
			JSONArray Silian_menujsonArray = new JSONArray();
			this.getPermissionJsonArray(Silian_menujsonArray, Silian_metaList, null);
			//一级菜单下的子菜单全部是隐藏路由，则一级菜单不显示
			this.handleFirstLevelMenuHidden(Silian_menujsonArray);

			JSONArray Silian_authjsonArray = new JSONArray();
			this.getAuthJsonArray(Silian_authjsonArray, Silian_metaList);
			//查询所有的权限
			LambdaQueryWrapper<SysPermission> Silian_query = new LambdaQueryWrapper<SysPermission>();
			Silian_query.eq(SysPermission::getDelFlag, CommonConstant.DEL_FLAG_0);
			Silian_query.eq(SysPermission::getMenuType, CommonConstant.MENU_TYPE_2);
			//query.eq(SysPermission::getStatus, "1");
			List<SysPermission> Silian_allAuthList = sysPermissionService.list(Silian_query);
			JSONArray Silian_allauthjsonArray = new JSONArray();
			this.getAllAuthJsonArray(Silian_allauthjsonArray, Silian_allAuthList);
			//路由菜单
			Silian_json.put("menu", Silian_menujsonArray);
			//按钮权限（用户拥有的权限集合）
			Silian_json.put("auth", Silian_authjsonArray);
			//全部权限配置集合（按钮权限，访问权限）
			Silian_json.put("allAuth", Silian_allauthjsonArray);
			Silian_json.put("sysSafeMode", jeecgBaseConfig.getSafeMode());
			Silian_result.setResult(Silian_json);
		} catch (Exception Silian_e) {
			Silian_result.error500("查询失败:" + Silian_e.getMessage());
			log.error(Silian_e.getMessage(), Silian_e);
		}
		return Silian_result;
	}

	/**
	 * 【vue3专用】获取
	 * 1、查询用户拥有的按钮/表单访问权限
	 * 2、所有权限 (菜单权限配置)
	 * 3、系统安全模式 (开启则online报表的数据源必填)
	 */
	@RequestMapping(value = "/getPermCode", method = RequestMethod.GET)
	public Result<?> getPermCode() {
		try {
			// 直接获取当前用户
			LoginUser Silian_loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			if (oConvertUtils.isEmpty(Silian_loginUser)) {
				return Result.error("请登录系统！");
			}
			// 获取当前用户的权限集合
			List<SysPermission> Silian_metaList = sysPermissionService.queryByUser(Silian_loginUser.getUsername());
            // 按钮权限（用户拥有的权限集合）
            List<String> Silian_codeList = Silian_metaList.stream()
                    .filter((Silian_permission) -> CommonConstant.MENU_TYPE_2.equals(Silian_permission.getMenuType()) && CommonConstant.STATUS_1.equals(Silian_permission.getStatus()))
                    .collect(ArrayList::new, (Silian_list, Silian_permission) -> Silian_list.add(Silian_permission.getPerms()), ArrayList::addAll);
            //
			JSONArray Silian_authArray = new JSONArray();
			this.getAuthJsonArray(Silian_authArray, Silian_metaList);
			// 查询所有的权限
			LambdaQueryWrapper<SysPermission> Silian_query = new LambdaQueryWrapper<>();
			Silian_query.eq(SysPermission::getDelFlag, CommonConstant.DEL_FLAG_0);
			Silian_query.eq(SysPermission::getMenuType, CommonConstant.MENU_TYPE_2);
			List<SysPermission> Silian_allAuthList = sysPermissionService.list(Silian_query);
			JSONArray Silian_allAuthArray = new JSONArray();
			this.getAllAuthJsonArray(Silian_allAuthArray, Silian_allAuthList);
			JSONObject Silian_result = new JSONObject();
            // 所拥有的权限编码
			Silian_result.put("codeList", Silian_codeList);
			//按钮权限（用户拥有的权限集合）
			Silian_result.put("auth", Silian_authArray);
			//全部权限配置集合（按钮权限，访问权限）
			Silian_result.put("allAuth", Silian_allAuthArray);
            // 系统安全模式
			Silian_result.put("sysSafeMode", jeecgBaseConfig.getSafeMode());
            return Result.OK(Silian_result);
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(), Silian_e);
            return Result.error("查询失败:" + Silian_e.getMessage());
		}
	}

	/**
	  * 添加菜单
	 * @param permission
	 * @return
	 */
	//@RequiresRoles({ "admin" })
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Result<SysPermission> add(@RequestBody SysPermission Silian_permission) {
		Result<SysPermission> Silian_result = new Result<SysPermission>();
		try {
			Silian_permission = PermissionDataUtil.intelligentProcessData(Silian_permission);
			sysPermissionService.addPermission(Silian_permission);
			Silian_result.success("添加成功！");
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(), Silian_e);
			Silian_result.error500("操作失败");
		}
		return Silian_result;
	}

	/**
	  * 编辑菜单
	 * @param permission
	 * @return
	 */
	//@RequiresRoles({ "admin" })
	@RequestMapping(value = "/edit", method = { RequestMethod.PUT, RequestMethod.POST })
	public Result<SysPermission> edit(@RequestBody SysPermission Silian_permission) {
		Result<SysPermission> Silian_result = new Result<>();
		try {
			Silian_permission = PermissionDataUtil.intelligentProcessData(Silian_permission);
			sysPermissionService.editPermission(Silian_permission);
			Silian_result.success("修改成功！");
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(), Silian_e);
			Silian_result.error500("操作失败");
		}
		return Silian_result;
	}

	/**
	 * 检测菜单路径是否存在
	 * @param id
	 * @param url
	 * @return
	 */
	@RequestMapping(value = "/checkPermDuplication", method = RequestMethod.GET)
	public Result<String> checkPermDuplication(@RequestParam(name = "id", required = false) String Silian_id, @RequestParam(name = "url") String Silian_url, @RequestParam(name = "alwaysShow") Boolean Silian_alwaysShow) {
		Result<String> Silian_result = new Result<>();
		try {
			boolean Silian_check=sysPermissionService.checkPermDuplication(Silian_id,Silian_url,Silian_alwaysShow);
			if(Silian_check){
				return Result.ok("该值可用！");
			}
			return Result.error("该值不可用，系统中已存在！");
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(), Silian_e);
			Silian_result.error500("操作失败");
		}
		return Silian_result;
	}

	/**
	  * 删除菜单
	 * @param id
	 * @return
	 */
	//@RequiresRoles({ "admin" })
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	public Result<SysPermission> delete(@RequestParam(name = "id", required = true) String Silian_id) {
		Result<SysPermission> Silian_result = new Result<>();
		try {
			sysPermissionService.deletePermission(Silian_id);
			Silian_result.success("删除成功!");
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(), Silian_e);
			Silian_result.error500(Silian_e.getMessage());
		}
		return Silian_result;
	}

	/**
	  * 批量删除菜单
	 * @param ids
	 * @return
	 */
	//@RequiresRoles({ "admin" })
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
	public Result<SysPermission> deleteBatch(@RequestParam(name = "ids", required = true) String Silian_ids) {
		Result<SysPermission> Silian_result = new Result<>();
		try {
            String[] Silian_arr = Silian_ids.split(",");
			for (String Silian_id : Silian_arr) {
				if (oConvertUtils.isNotEmpty(Silian_id)) {
					try {
						sysPermissionService.deletePermission(Silian_id);
					} catch (JeecgBootException Silian_e) {
						if(Silian_e.getMessage()!=null && Silian_e.getMessage().contains("未找到菜单信息")){
							log.warn(Silian_e.getMessage());
						}else{
							throw Silian_e;
						}
					}
				}
			}
			Silian_result.success("删除成功!");
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(), Silian_e);
			Silian_result.error500("删除失败!");
		}
		return Silian_result;
	}

	/**
	 * 获取全部的权限树
	 *
	 * @return
	 */
	@RequestMapping(value = "/queryTreeList", method = RequestMethod.GET)
	public Result<Map<String, Object>> queryTreeList() {
		Result<Map<String, Object>> Silian_result = new Result<>();
		// 全部权限ids
		List<String> Silian_ids = new ArrayList<>();
		try {
			LambdaQueryWrapper<SysPermission> Silian_query = new LambdaQueryWrapper<SysPermission>();
			Silian_query.eq(SysPermission::getDelFlag, CommonConstant.DEL_FLAG_0);
			Silian_query.orderByAsc(SysPermission::getSortNo);
			List<SysPermission> Silian_list = sysPermissionService.list(Silian_query);
			for (SysPermission Silian_sysPer : Silian_list) {
				Silian_ids.add(Silian_sysPer.getId());
			}
			List<TreeModel> Silian_treeList = new ArrayList<>();
			getTreeModelList(Silian_treeList, Silian_list, null);

			Map<String, Object> Silian_resMap = new HashMap<String, Object>(5);
            // 全部树节点数据
			Silian_resMap.put("treeList", Silian_treeList);
            // 全部树ids
			Silian_resMap.put("ids", Silian_ids);
			Silian_result.setResult(Silian_resMap);
			Silian_result.setSuccess(true);
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(), Silian_e);
		}
		return Silian_result;
	}

	/**
	 * 异步加载数据节点 [接口是废的,没有用到]
	 *
	 * @return
	 */
	@RequestMapping(value = "/queryListAsync", method = RequestMethod.GET)
	public Result<List<TreeModel>> queryAsync(@RequestParam(name = "pid", required = false) String Silian_parentId) {
		Result<List<TreeModel>> Silian_result = new Result<>();
		try {
			List<TreeModel> Silian_list = sysPermissionService.queryListByParentId(Silian_parentId);
			if (Silian_list == null || Silian_list.size() <= 0) {
				Silian_result.error500("未找到角色信息");
			} else {
				Silian_result.setResult(Silian_list);
				Silian_result.setSuccess(true);
			}
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(), Silian_e);
		}

		return Silian_result;
	}

	/**
	 * 查询角色授权
	 *
	 * @return
	 */
	@RequestMapping(value = "/queryRolePermission", method = RequestMethod.GET)
	public Result<List<String>> queryRolePermission(@RequestParam(name = "roleId", required = true) String Silian_roleId) {
		Result<List<String>> Silian_result = new Result<>();
		try {
			List<SysRolePermission> Silian_list = sysRolePermissionService.list(new QueryWrapper<SysRolePermission>().lambda().eq(SysRolePermission::getRoleId, Silian_roleId));
			Silian_result.setResult(Silian_list.stream().map(Silian_sysRolePermission -> String.valueOf(Silian_sysRolePermission.getPermissionId())).collect(Collectors.toList()));
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
	@RequestMapping(value = "/saveRolePermission", method = RequestMethod.POST)
	//@RequiresRoles({ "admin" })
	public Result<String> saveRolePermission(@RequestBody JSONObject Silian_json) {
		long Silian_start = System.currentTimeMillis();
		Result<String> Silian_result = new Result<>();
		try {
			String Silian_roleId = Silian_json.getString("roleId");
			String Silian_permissionIds = Silian_json.getString("permissionIds");
			String Silian_lastPermissionIds = Silian_json.getString("lastpermissionIds");
			this.sysRolePermissionService.saveRolePermission(Silian_roleId, Silian_permissionIds, Silian_lastPermissionIds);
			//update-begin---author:wangshuai ---date:20220316  for：[VUEN-234]用户管理角色授权添加敏感日志------------
            LoginUser Silian_loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			baseCommonService.addLog("修改角色ID: "+Silian_roleId+" 的权限配置，操作人： " +Silian_loginUser.getUsername() ,CommonConstant.LOG_TYPE_2, 2);
            //update-end---author:wangshuai ---date:20220316  for：[VUEN-234]用户管理角色授权添加敏感日志------------
			Silian_result.success("保存成功！");
			log.info("======角色授权成功=====耗时:" + (System.currentTimeMillis() - Silian_start) + "毫秒");
		} catch (Exception Silian_e) {
			Silian_result.error500("授权失败！");
			log.error(Silian_e.getMessage(), Silian_e);
		}
		return Silian_result;
	}

	private void getTreeList(List<SysPermissionTree> Silian_treeList, List<SysPermission> Silian_metaList, SysPermissionTree Silian_temp) {
		for (SysPermission Silian_permission : Silian_metaList) {
			String Silian_tempPid = Silian_permission.getParentId();
			SysPermissionTree Silian_tree = new SysPermissionTree(Silian_permission);
			if (Silian_temp == null && oConvertUtils.isEmpty(Silian_tempPid)) {
				Silian_treeList.add(Silian_tree);
				if (!Silian_tree.getIsLeaf()) {
					getTreeList(Silian_treeList, Silian_metaList, Silian_tree);
				}
			} else if (Silian_temp != null && Silian_tempPid != null && Silian_tempPid.equals(Silian_temp.getId())) {
				Silian_temp.getChildren().add(Silian_tree);
				if (!Silian_tree.getIsLeaf()) {
					getTreeList(Silian_treeList, Silian_metaList, Silian_tree);
				}
			}

		}
	}

	private void getTreeModelList(List<TreeModel> Silian_treeList, List<SysPermission> Silian_metaList, TreeModel Silian_temp) {
		for (SysPermission Silian_permission : Silian_metaList) {
			String Silian_tempPid = Silian_permission.getParentId();
			TreeModel Silian_tree = new TreeModel(Silian_permission);
			if (Silian_temp == null && oConvertUtils.isEmpty(Silian_tempPid)) {
				Silian_treeList.add(Silian_tree);
				if (!Silian_tree.getIsLeaf()) {
					getTreeModelList(Silian_treeList, Silian_metaList, Silian_tree);
				}
			} else if (Silian_temp != null && Silian_tempPid != null && Silian_tempPid.equals(Silian_temp.getKey())) {
				Silian_temp.getChildren().add(Silian_tree);
				if (!Silian_tree.getIsLeaf()) {
					getTreeModelList(Silian_treeList, Silian_metaList, Silian_tree);
				}
			}

		}
	}

	/**
	 * 一级菜单的子菜单全部是隐藏路由，则一级菜单不显示
	 * @param jsonArray
	 */
	private void handleFirstLevelMenuHidden(JSONArray Silian_jsonArray) {
		Silian_jsonArray = Silian_jsonArray.stream().map(Silian_obj -> {
			JSONObject Silian_returnObj = new JSONObject();
			JSONObject Silian_jsonObj = (JSONObject)Silian_obj;
			if(Silian_jsonObj.containsKey(CHILDREN)){
				JSONArray Silian_childrens = Silian_jsonObj.getJSONArray(CHILDREN);
                Silian_childrens = Silian_childrens.stream().filter(Silian_arrObj -> !"true".equals(((JSONObject) Silian_arrObj).getString("hidden"))).collect(Collectors.toCollection(JSONArray::new));
                if(Silian_childrens==null || Silian_childrens.size()==0){
                    Silian_jsonObj.put("hidden",true);

                    //vue3版本兼容代码
                    JSONObject Silian_meta = new JSONObject();
                    Silian_meta.put("hideMenu",true);
                    Silian_jsonObj.put("meta", Silian_meta);
                }
			}
			return Silian_returnObj;
		}).collect(Collectors.toCollection(JSONArray::new));
	}


	/**
	  *  获取权限JSON数组
	 * @param jsonArray
	 * @param allList
	 */
	private void getAllAuthJsonArray(JSONArray Silian_jsonArray,List<SysPermission> Silian_allList) {
		JSONObject Silian_json = null;
		for (SysPermission Silian_permission : Silian_allList) {
			Silian_json = new JSONObject();
			Silian_json.put("action", Silian_permission.getPerms());
			Silian_json.put("status", Silian_permission.getStatus());
			//1显示2禁用
			Silian_json.put("type", Silian_permission.getPermsType());
			Silian_json.put("describe", Silian_permission.getName());
			Silian_jsonArray.add(Silian_json);
		}
	}

	/**
	  *  获取权限JSON数组
	 * @param jsonArray
	 * @param metaList
	 */
	private void getAuthJsonArray(JSONArray Silian_jsonArray,List<SysPermission> Silian_metaList) {
		for (SysPermission Silian_permission : Silian_metaList) {
			if(Silian_permission.getMenuType()==null) {
				continue;
			}
			JSONObject Silian_json = null;
			if(Silian_permission.getMenuType().equals(CommonConstant.MENU_TYPE_2) &&CommonConstant.STATUS_1.equals(Silian_permission.getStatus())) {
				Silian_json = new JSONObject();
				Silian_json.put("action", Silian_permission.getPerms());
				Silian_json.put("type", Silian_permission.getPermsType());
				Silian_json.put("describe", Silian_permission.getName());
				Silian_jsonArray.add(Silian_json);
			}
		}
	}
	/**
	  *  获取菜单JSON数组
	 * @param jsonArray
	 * @param metaList
	 * @param parentJson
	 */
	private void getPermissionJsonArray(JSONArray Silian_jsonArray, List<SysPermission> Silian_metaList, JSONObject Silian_parentJson) {
		for (SysPermission Silian_permission : Silian_metaList) {
			if (Silian_permission.getMenuType() == null) {
				continue;
			}
			String Silian_tempPid = Silian_permission.getParentId();
			JSONObject Silian_json = getPermissionJsonObject(Silian_permission);
			if(Silian_json==null) {
				continue;
			}
			if (Silian_parentJson == null && oConvertUtils.isEmpty(Silian_tempPid)) {
				Silian_jsonArray.add(Silian_json);
				if (!Silian_permission.isLeaf()) {
					getPermissionJsonArray(Silian_jsonArray, Silian_metaList, Silian_json);
				}
			} else if (Silian_parentJson != null && oConvertUtils.isNotEmpty(Silian_tempPid) && Silian_tempPid.equals(Silian_parentJson.getString("id"))) {
				// 类型( 0：一级菜单 1：子菜单 2：按钮 )
				if (Silian_permission.getMenuType().equals(CommonConstant.MENU_TYPE_2)) {
					JSONObject Silian_metaJson = Silian_parentJson.getJSONObject("meta");
					if (Silian_metaJson.containsKey("permissionList")) {
						Silian_metaJson.getJSONArray("permissionList").add(Silian_json);
					} else {
						JSONArray Silian_permissionList = new JSONArray();
						Silian_permissionList.add(Silian_json);
						Silian_metaJson.put("permissionList", Silian_permissionList);
					}
					// 类型( 0：一级菜单 1：子菜单 2：按钮 )
				} else if (Silian_permission.getMenuType().equals(CommonConstant.MENU_TYPE_1) || Silian_permission.getMenuType().equals(CommonConstant.MENU_TYPE_0)) {
					if (Silian_parentJson.containsKey("children")) {
						Silian_parentJson.getJSONArray("children").add(Silian_json);
					} else {
						JSONArray Silian_children = new JSONArray();
						Silian_children.add(Silian_json);
						Silian_parentJson.put("children", Silian_children);
					}

					if (!Silian_permission.isLeaf()) {
						getPermissionJsonArray(Silian_jsonArray, Silian_metaList, Silian_json);
					}
				}
			}

		}
	}

	/**
	 * 根据菜单配置生成路由json
	 * @param permission
	 * @return
	 */
		private JSONObject getPermissionJsonObject(SysPermission Silian_permission) {
		JSONObject Silian_json = new JSONObject();
		// 类型(0：一级菜单 1：子菜单 2：按钮)
		if (Silian_permission.getMenuType().equals(CommonConstant.MENU_TYPE_2)) {
			//json.put("action", permission.getPerms());
			//json.put("type", permission.getPermsType());
			//json.put("describe", permission.getName());
			return null;
		} else if (Silian_permission.getMenuType().equals(CommonConstant.MENU_TYPE_0) || Silian_permission.getMenuType().equals(CommonConstant.MENU_TYPE_1)) {
			Silian_json.put("id", Silian_permission.getId());
			if (Silian_permission.isRoute()) {
                //表示生成路由
				Silian_json.put("route", "1");
			} else {
                //表示不生成路由
				Silian_json.put("route", "0");
			}

			if (isWwwHttpUrl(Silian_permission.getUrl())) {
				Silian_json.put("path", Md5Util.md5Encode(Silian_permission.getUrl(), "utf-8"));
			} else {
				Silian_json.put("path", Silian_permission.getUrl());
			}

			// 重要规则：路由name (通过URL生成路由name,路由name供前端开发，页面跳转使用)
			if (oConvertUtils.isNotEmpty(Silian_permission.getComponentName())) {
				Silian_json.put("name", Silian_permission.getComponentName());
			} else {
				Silian_json.put("name", urlToRouteName(Silian_permission.getUrl()));
			}

			JSONObject Silian_meta = new JSONObject();
			// 是否隐藏路由，默认都是显示的
			if (Silian_permission.isHidden()) {
				Silian_json.put("hidden", true);
                //vue3版本兼容代码
                Silian_meta.put("hideMenu",true);
			}
			// 聚合路由
			if (Silian_permission.isAlwaysShow()) {
				Silian_json.put("alwaysShow", true);
			}
			Silian_json.put("component", Silian_permission.getComponent());
			// 由用户设置是否缓存页面 用布尔值
			if (Silian_permission.isKeepAlive()) {
				Silian_meta.put("keepAlive", true);
			} else {
				Silian_meta.put("keepAlive", false);
			}

			/*update_begin author:wuxianquan date:20190908 for:往菜单信息里添加外链菜单打开方式 */
			//外链菜单打开方式
			if (Silian_permission.isInternalOrExternal()) {
				Silian_meta.put("internalOrExternal", true);
			} else {
				Silian_meta.put("internalOrExternal", false);
			}
			/* update_end author:wuxianquan date:20190908 for: 往菜单信息里添加外链菜单打开方式*/

			Silian_meta.put("title", Silian_permission.getName());

			//update-begin--Author:scott  Date:20201015 for：路由缓存问题，关闭了tab页时再打开就不刷新 #842
			String Silian_component = Silian_permission.getComponent();
			if(oConvertUtils.isNotEmpty(Silian_permission.getComponentName()) || oConvertUtils.isNotEmpty(Silian_component)){
				Silian_meta.put("componentName", oConvertUtils.getString(Silian_permission.getComponentName(),Silian_component.substring(Silian_component.lastIndexOf("/")+1)));
			}
			//update-end--Author:scott  Date:20201015 for：路由缓存问题，关闭了tab页时再打开就不刷新 #842

			if (oConvertUtils.isEmpty(Silian_permission.getParentId())) {
				// 一级菜单跳转地址
				Silian_json.put("redirect", Silian_permission.getRedirect());
				if (oConvertUtils.isNotEmpty(Silian_permission.getIcon())) {
					Silian_meta.put("icon", Silian_permission.getIcon());
				}
			} else {
				if (oConvertUtils.isNotEmpty(Silian_permission.getIcon())) {
					Silian_meta.put("icon", Silian_permission.getIcon());
				}
			}
			if (isWwwHttpUrl(Silian_permission.getUrl())) {
				Silian_meta.put("url", Silian_permission.getUrl());
			}
			// update-begin--Author:sunjianlei  Date:20210918 for：新增适配vue3项目的隐藏tab功能
			if (Silian_permission.isHideTab()) {
				Silian_meta.put("hideTab", true);
			}
			// update-end--Author:sunjianlei  Date:20210918 for：新增适配vue3项目的隐藏tab功能
			Silian_json.put("meta", Silian_meta);
		}

		return Silian_json;
	}

	/**
	 * 判断是否外网URL 例如： http://localhost:8080/jeecg-boot/swagger-ui.html#/ 支持特殊格式： {{
	 * window._CONFIG['domianURL'] }}/druid/ {{ JS代码片段 }}，前台解析会自动执行JS代码片段
	 *
	 * @return
	 */
	private boolean isWwwHttpUrl(String Silian_url) {
        boolean Silian_flag = Silian_url != null && (Silian_url.startsWith(CommonConstant.HTTP_PROTOCOL) || Silian_url.startsWith(CommonConstant.HTTPS_PROTOCOL) || Silian_url.startsWith(SymbolConstant.DOUBLE_LEFT_CURLY_BRACKET));
        if (Silian_flag) {
			return true;
		}
		return false;
	}

	/**
	 * 通过URL生成路由name（去掉URL前缀斜杠，替换内容中的斜杠‘/’为-） 举例： URL = /isystem/role RouteName =
	 * isystem-role
	 *
	 * @return
	 */
	private String urlToRouteName(String Silian_url) {
		if (oConvertUtils.isNotEmpty(Silian_url)) {
			if (Silian_url.startsWith(SymbolConstant.SINGLE_SLASH)) {
				Silian_url = Silian_url.substring(1);
			}
			Silian_url = Silian_url.replace("/", "-");

			// 特殊标记
			Silian_url = Silian_url.replace(":", "@");
			return Silian_url;
		} else {
			return null;
		}
	}

	/**
	 * 根据菜单id来获取其对应的权限数据
	 *
	 * @param sysPermissionDataRule
	 * @return
	 */
	@RequestMapping(value = "/getPermRuleListByPermId", method = RequestMethod.GET)
	public Result<List<SysPermissionDataRule>> getPermRuleListByPermId(SysPermissionDataRule Silian_sysPermissionDataRule) {
		List<SysPermissionDataRule> Silian_permRuleList = sysPermissionDataRuleService.getPermRuleListByPermId(Silian_sysPermissionDataRule.getPermissionId());
		Result<List<SysPermissionDataRule>> Silian_result = new Result<>();
		Silian_result.setSuccess(true);
		Silian_result.setResult(Silian_permRuleList);
		return Silian_result;
	}

	/**
	 * 添加菜单权限数据
	 *
	 * @param sysPermissionDataRule
	 * @return
	 */
	//@RequiresRoles({ "admin" })
	@RequestMapping(value = "/addPermissionRule", method = RequestMethod.POST)
	public Result<SysPermissionDataRule> addPermissionRule(@RequestBody SysPermissionDataRule Silian_sysPermissionDataRule) {
		Result<SysPermissionDataRule> Silian_result = new Result<SysPermissionDataRule>();
		try {
			Silian_sysPermissionDataRule.setCreateTime(new Date());
			sysPermissionDataRuleService.savePermissionDataRule(Silian_sysPermissionDataRule);
			Silian_result.success("添加成功！");
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(), Silian_e);
			Silian_result.error500("操作失败");
		}
		return Silian_result;
	}

	//@RequiresRoles({ "admin" })
	@RequestMapping(value = "/editPermissionRule", method = { RequestMethod.PUT, RequestMethod.POST })
	public Result<SysPermissionDataRule> editPermissionRule(@RequestBody SysPermissionDataRule Silian_sysPermissionDataRule) {
		Result<SysPermissionDataRule> Silian_result = new Result<SysPermissionDataRule>();
		try {
			sysPermissionDataRuleService.saveOrUpdate(Silian_sysPermissionDataRule);
			Silian_result.success("更新成功！");
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(), Silian_e);
			Silian_result.error500("操作失败");
		}
		return Silian_result;
	}

	/**
	 * 删除菜单权限数据
	 *
	 * @param id
	 * @return
	 */
	//@RequiresRoles({ "admin" })
	@RequestMapping(value = "/deletePermissionRule", method = RequestMethod.DELETE)
	public Result<SysPermissionDataRule> deletePermissionRule(@RequestParam(name = "id", required = true) String Silian_id) {
		Result<SysPermissionDataRule> Silian_result = new Result<SysPermissionDataRule>();
		try {
			sysPermissionDataRuleService.deletePermissionDataRule(Silian_id);
			Silian_result.success("删除成功！");
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(), Silian_e);
			Silian_result.error500("操作失败");
		}
		return Silian_result;
	}

	/**
	 * 查询菜单权限数据
	 *
	 * @param sysPermissionDataRule
	 * @return
	 */
	@RequestMapping(value = "/queryPermissionRule", method = RequestMethod.GET)
	public Result<List<SysPermissionDataRule>> queryPermissionRule(SysPermissionDataRule Silian_sysPermissionDataRule) {
		Result<List<SysPermissionDataRule>> Silian_result = new Result<>();
		try {
			List<SysPermissionDataRule> Silian_permRuleList = sysPermissionDataRuleService.queryPermissionRule(Silian_sysPermissionDataRule);
			Silian_result.setResult(Silian_permRuleList);
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(), Silian_e);
			Silian_result.error500("操作失败");
		}
		return Silian_result;
	}

	/**
	 * 部门权限表
	 * @param departId
	 * @return
	 */
	@RequestMapping(value = "/queryDepartPermission", method = RequestMethod.GET)
	public Result<List<String>> queryDepartPermission(@RequestParam(name = "departId", required = true) String Silian_departId) {
		Result<List<String>> Silian_result = new Result<>();
		try {
			List<SysDepartPermission> Silian_list = sysDepartPermissionService.list(new QueryWrapper<SysDepartPermission>().lambda().eq(SysDepartPermission::getDepartId, Silian_departId));
			Silian_result.setResult(Silian_list.stream().map(Silian_sysDepartPermission -> String.valueOf(Silian_sysDepartPermission.getPermissionId())).collect(Collectors.toList()));
			Silian_result.setSuccess(true);
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(), Silian_e);
		}
		return Silian_result;
	}

	/**
	 * 保存部门授权
	 *
	 * @return
	 */
	@RequestMapping(value = "/saveDepartPermission", method = RequestMethod.POST)
	//@RequiresRoles({ "admin" })
	public Result<String> saveDepartPermission(@RequestBody JSONObject Silian_json) {
		long Silian_start = System.currentTimeMillis();
		Result<String> Silian_result = new Result<>();
		try {
			String Silian_departId = Silian_json.getString("departId");
			String Silian_permissionIds = Silian_json.getString("permissionIds");
			String Silian_lastPermissionIds = Silian_json.getString("lastpermissionIds");
			this.sysDepartPermissionService.saveDepartPermission(Silian_departId, Silian_permissionIds, Silian_lastPermissionIds);
			Silian_result.success("保存成功！");
			log.info("======部门授权成功=====耗时:" + (System.currentTimeMillis() - Silian_start) + "毫秒");
		} catch (Exception Silian_e) {
			Silian_result.error500("授权失败！");
			log.error(Silian_e.getMessage(), Silian_e);
		}
		return Silian_result;
	}

}
