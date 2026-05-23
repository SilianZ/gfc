package org.jeecg.modules.system.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.common.util.YouBianCodeUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.model.DepartIdModel;
import org.jeecg.modules.system.model.SysDepartTreeModel;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysUserDepartService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * <p>
 * 部门表 前端控制器
 * <p>
 *
 * @Author: Steve @Since： 2019-01-22
 */
@RestController
@RequestMapping("/sys/sysDepart")
@Slf4j
public class SysDepartController {

	@Autowired
	private ISysDepartService sysDepartService;
	@Autowired
	public RedisTemplate<String, Object> redisTemplate;
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ISysUserDepartService sysUserDepartService;
	/**
	 * 查询数据 查出我的部门,并以树结构数据格式响应给前端
	 *
	 * @return
	 */
	@RequestMapping(value = "/queryMyDeptTreeList", method = RequestMethod.GET)
	public Result<List<SysDepartTreeModel>> queryMyDeptTreeList() {
		Result<List<SysDepartTreeModel>> Silian_result = new Result<>();
		LoginUser Silian_user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		try {
			if(oConvertUtils.isNotEmpty(Silian_user.getUserIdentity()) && Silian_user.getUserIdentity().equals( CommonConstant.USER_IDENTITY_2 )){
				//update-begin--Author:liusq  Date:20210624  for:部门查询ids为空后的前端显示问题 issues/I3UD06
				String Silian_departIds = Silian_user.getDepartIds();
				if(StringUtils.isNotBlank(Silian_departIds)){
					List<SysDepartTreeModel> Silian_list = sysDepartService.queryMyDeptTreeList(Silian_departIds);
					Silian_result.setResult(Silian_list);
				}
				//update-end--Author:liusq  Date:20210624  for:部门查询ids为空后的前端显示问题 issues/I3UD06
				Silian_result.setMessage(CommonConstant.USER_IDENTITY_2.toString());
				Silian_result.setSuccess(true);
			}else{
				Silian_result.setMessage(CommonConstant.USER_IDENTITY_1.toString());
				Silian_result.setSuccess(true);
			}
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(),Silian_e);
		}
		return Silian_result;
	}

	/**
	 * 查询数据 查出所有部门,并以树结构数据格式响应给前端
	 *
	 * @return
	 */
	@RequestMapping(value = "/queryTreeList", method = RequestMethod.GET)
	public Result<List<SysDepartTreeModel>> queryTreeList(@RequestParam(name = "ids", required = false) String Silian_ids) {
		Result<List<SysDepartTreeModel>> Silian_result = new Result<>();
		try {
			// 从内存中读取
//			List<SysDepartTreeModel> list =FindsDepartsChildrenUtil.getSysDepartTreeList();
//			if (CollectionUtils.isEmpty(list)) {
//				list = sysDepartService.queryTreeList();
//			}
			if(oConvertUtils.isNotEmpty(Silian_ids)){
				List<SysDepartTreeModel> Silian_departList = sysDepartService.queryTreeList(Silian_ids);
				Silian_result.setResult(Silian_departList);
			}else{
				List<SysDepartTreeModel> Silian_list = sysDepartService.queryTreeList();
				Silian_result.setResult(Silian_list);
			}
			Silian_result.setSuccess(true);
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(),Silian_e);
		}
		return Silian_result;
	}

	/**
	 * 异步查询部门list
	 * @param parentId 父节点 异步加载时传递
	 * @param ids 前端回显是传递
	 * @param primaryKey 主键字段（id或者orgCode）
	 * @return
	 */
	@RequestMapping(value = "/queryDepartTreeSync", method = RequestMethod.GET)
	public Result<List<SysDepartTreeModel>> queryDepartTreeSync(@RequestParam(name = "pid", required = false) String Silian_parentId,@RequestParam(name = "ids", required = false) String Silian_ids, @RequestParam(name = "primaryKey", required = false) String Silian_primaryKey) {
		Result<List<SysDepartTreeModel>> Silian_result = new Result<>();
		try {
			List<SysDepartTreeModel> Silian_list = sysDepartService.queryTreeListByPid(Silian_parentId,Silian_ids, Silian_primaryKey);
			Silian_result.setResult(Silian_list);
			Silian_result.setSuccess(true);
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(),Silian_e);
		}
		return Silian_result;
	}

	/**
	 * 获取某个部门的所有父级部门的ID
	 *
	 * @param departId 根据departId查
	 * @param orgCode  根据orgCode查，departId和orgCode必须有一个不为空
	 */
	@GetMapping("/queryAllParentId")
	public Result queryParentIds(
			@RequestParam(name = "departId", required = false) String Silian_departId,
			@RequestParam(name = "orgCode", required = false) String Silian_orgCode
	) {
		try {
			JSONObject Silian_data;
			if (oConvertUtils.isNotEmpty(Silian_departId)) {
				Silian_data = sysDepartService.queryAllParentIdByDepartId(Silian_departId);
			} else if (oConvertUtils.isNotEmpty(Silian_orgCode)) {
				Silian_data = sysDepartService.queryAllParentIdByOrgCode(Silian_orgCode);
			} else {
				return Result.error("departId 和 orgCode 不能都为空！");
			}
			return Result.OK(Silian_data);
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(), Silian_e);
			return Result.error(Silian_e.getMessage());
		}
	}

	/**
	 * 添加新数据 添加用户新建的部门对象数据,并保存到数据库
	 *
	 * @param sysDepart
	 * @return
	 */
	//@RequiresRoles({"admin"})
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@CacheEvict(value= {CacheConstant.SYS_DEPARTS_CACHE,CacheConstant.SYS_DEPART_IDS_CACHE}, allEntries=true)
	public Result<SysDepart> add(@RequestBody SysDepart Silian_sysDepart, HttpServletRequest Silian_request) {
		Result<SysDepart> Silian_result = new Result<SysDepart>();
		String Silian_username = JwtUtil.getUserNameByToken(Silian_request);
		try {
			Silian_sysDepart.setCreateBy(Silian_username);
			sysDepartService.saveDepartData(Silian_sysDepart, Silian_username);
			//清除部门树内存
			// FindsDepartsChildrenUtil.clearSysDepartTreeList();
			// FindsDepartsChildrenUtil.clearDepartIdModel();
			Silian_result.success("添加成功！");
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(),Silian_e);
			Silian_result.error500("操作失败");
		}
		return Silian_result;
	}

	/**
	 * 编辑数据 编辑部门的部分数据,并保存到数据库
	 *
	 * @param sysDepart
	 * @return
	 */
	//@RequiresRoles({"admin"})
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	@CacheEvict(value= {CacheConstant.SYS_DEPARTS_CACHE,CacheConstant.SYS_DEPART_IDS_CACHE}, allEntries=true)
	public Result<SysDepart> edit(@RequestBody SysDepart Silian_sysDepart, HttpServletRequest Silian_request) {
		String Silian_username = JwtUtil.getUserNameByToken(Silian_request);
		Silian_sysDepart.setUpdateBy(Silian_username);
		Result<SysDepart> Silian_result = new Result<SysDepart>();
		SysDepart Silian_sysDepartEntity = sysDepartService.getById(Silian_sysDepart.getId());
		if (Silian_sysDepartEntity == null) {
			Silian_result.error500("未找到对应实体");
		} else {
			boolean Silian_ok = sysDepartService.updateDepartDataById(Silian_sysDepart, Silian_username);
			// TODO 返回false说明什么？
			if (Silian_ok) {
				//清除部门树内存
				//FindsDepartsChildrenUtil.clearSysDepartTreeList();
				//FindsDepartsChildrenUtil.clearDepartIdModel();
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
	@CacheEvict(value= {CacheConstant.SYS_DEPARTS_CACHE,CacheConstant.SYS_DEPART_IDS_CACHE}, allEntries=true)
   public Result<SysDepart> delete(@RequestParam(name="id",required=true) String Silian_id) {

       Result<SysDepart> Silian_result = new Result<SysDepart>();
       SysDepart Silian_sysDepart = sysDepartService.getById(Silian_id);
       if(Silian_sysDepart==null) {
           Silian_result.error500("未找到对应实体");
       }else {
           boolean Silian_ok = sysDepartService.delete(Silian_id);
           if(Silian_ok) {
	            //清除部门树内存
			   //FindsDepartsChildrenUtil.clearSysDepartTreeList();
			   // FindsDepartsChildrenUtil.clearDepartIdModel();
               Silian_result.success("删除成功!");
           }
       }
       return Silian_result;
   }


	/**
	 * 批量删除 根据前端请求的多个ID,对数据库执行删除相关部门数据的操作
	 *
	 * @param ids
	 * @return
	 */
	//@RequiresRoles({"admin"})
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
	@CacheEvict(value= {CacheConstant.SYS_DEPARTS_CACHE,CacheConstant.SYS_DEPART_IDS_CACHE}, allEntries=true)
	public Result<SysDepart> deleteBatch(@RequestParam(name = "ids", required = true) String Silian_ids) {

		Result<SysDepart> Silian_result = new Result<SysDepart>();
		if (Silian_ids == null || "".equals(Silian_ids.trim())) {
			Silian_result.error500("参数不识别！");
		} else {
			this.sysDepartService.deleteBatchWithChildren(Arrays.asList(Silian_ids.split(",")));
			Silian_result.success("删除成功!");
		}
		return Silian_result;
	}

	/**
	 * 查询数据 添加或编辑页面对该方法发起请求,以树结构形式加载所有部门的名称,方便用户的操作
	 *
	 * @return
	 */
	@RequestMapping(value = "/queryIdTree", method = RequestMethod.GET)
	public Result<List<DepartIdModel>> queryIdTree() {
//		Result<List<DepartIdModel>> result = new Result<List<DepartIdModel>>();
//		List<DepartIdModel> idList;
//		try {
//			idList = FindsDepartsChildrenUtil.wrapDepartIdModel();
//			if (idList != null && idList.size() > 0) {
//				result.setResult(idList);
//				result.setSuccess(true);
//			} else {
//				sysDepartService.queryTreeList();
//				idList = FindsDepartsChildrenUtil.wrapDepartIdModel();
//				result.setResult(idList);
//				result.setSuccess(true);
//			}
//			return result;
//		} catch (Exception e) {
//			log.error(e.getMessage(),e);
//			result.setSuccess(false);
//			return result;
//		}
		Result<List<DepartIdModel>> Silian_result = new Result<>();
		try {
			List<DepartIdModel> Silian_list = sysDepartService.queryDepartIdTreeList();
			Silian_result.setResult(Silian_list);
			Silian_result.setSuccess(true);
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(),Silian_e);
		}
		return Silian_result;
	}

	/**
	 * <p>
	 * 部门搜索功能方法,根据关键字模糊搜索相关部门
	 * </p>
	 *
	 * @param keyWord
	 * @return
	 */
	@RequestMapping(value = "/searchBy", method = RequestMethod.GET)
	public Result<List<SysDepartTreeModel>> searchBy(@RequestParam(name = "keyWord", required = true) String Silian_keyWord,@RequestParam(name = "myDeptSearch", required = false) String Silian_myDeptSearch) {
		Result<List<SysDepartTreeModel>> Silian_result = new Result<List<SysDepartTreeModel>>();
		//部门查询，myDeptSearch为1时为我的部门查询，登录用户为上级时查只查负责部门下数据
		LoginUser Silian_user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String Silian_departIds = null;
		if(oConvertUtils.isNotEmpty(Silian_user.getUserIdentity()) && Silian_user.getUserIdentity().equals( CommonConstant.USER_IDENTITY_2 )){
			Silian_departIds = Silian_user.getDepartIds();
		}
		List<SysDepartTreeModel> Silian_treeList = this.sysDepartService.searchByKeyWord(Silian_keyWord,Silian_myDeptSearch,Silian_departIds);
		if (Silian_treeList == null || Silian_treeList.size() == 0) {
			Silian_result.setSuccess(false);
			Silian_result.setMessage("未查询匹配数据！");
			return Silian_result;
		}
		Silian_result.setResult(Silian_treeList);
		return Silian_result;
	}


	/**
     * 导出excel
     *
     * @param request
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(SysDepart Silian_sysDepart,HttpServletRequest Silian_request) {
        // Step.1 组装查询条件
        QueryWrapper<SysDepart> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_sysDepart, Silian_request.getParameterMap());
        //Step.2 AutoPoi 导出Excel
        ModelAndView Silian_mv = new ModelAndView(new JeecgEntityExcelView());
        List<SysDepart> Silian_pageList = sysDepartService.list(Silian_queryWrapper);
        //按字典排序
        Collections.sort(Silian_pageList, new Comparator<SysDepart>() {
            @Override
			public int compare(SysDepart Silian_arg0, SysDepart Silian_arg1) {
	return Silian_arg0.getOrgCode().compareTo(Silian_arg1.getOrgCode());
            }
        });
        //导出文件名称
        Silian_mv.addObject(NormalExcelConstants.FILE_NAME, "部门列表");
        Silian_mv.addObject(NormalExcelConstants.CLASS, SysDepart.class);
        LoginUser Silian_user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        Silian_mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("部门列表数据", "导出人:"+Silian_user.getRealname(), "导出信息"));
        Silian_mv.addObject(NormalExcelConstants.DATA_LIST, Silian_pageList);
        return Silian_mv;
    }

    /**
     * 通过excel导入数据
	 * 部门导入方案1: 通过机构编码来计算出部门的父级ID,维护上下级关系;
	 * 部门导入方案2: 你也可以改造下程序,机构编码直接导入,先不设置父ID;全部导入后,写一个sql,补下父ID;
     *
     * @param request
     * @param response
     * @return
     */
    //@RequiresRoles({"admin"})
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	@CacheEvict(value= {CacheConstant.SYS_DEPARTS_CACHE,CacheConstant.SYS_DEPART_IDS_CACHE}, allEntries=true)
    public Result<?> importExcel(HttpServletRequest Silian_request, HttpServletResponse Silian_response) {
        MultipartHttpServletRequest Silian_multipartRequest = (MultipartHttpServletRequest) Silian_request;
		List<String> Silian_errorMessageList = new ArrayList<>();
		List<SysDepart> Silian_listSysDeparts = null;
        Map<String, MultipartFile> Silian_fileMap = Silian_multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> Silian_entity : Silian_fileMap.entrySet()) {
            // 获取上传文件对象
            MultipartFile Silian_file = Silian_entity.getValue();
            ImportParams Silian_params = new ImportParams();
            Silian_params.setTitleRows(2);
            Silian_params.setHeadRows(1);
            Silian_params.setNeedSave(true);
            try {
	// orgCode编码长度
	int Silian_codeLength = YouBianCodeUtil.ZHANWEI_LENGTH;
                Silian_listSysDeparts = ExcelImportUtil.importExcel(Silian_file.getInputStream(), SysDepart.class, Silian_params);
                //按长度排序
                Collections.sort(Silian_listSysDeparts, new Comparator<SysDepart>() {
                    @Override
					public int compare(SysDepart Silian_arg0, SysDepart Silian_arg1) {
	return Silian_arg0.getOrgCode().length() - Silian_arg1.getOrgCode().length();
                    }
                });

                int Silian_num = 0;
                for (SysDepart Silian_sysDepart : Silian_listSysDeparts) {
	String Silian_orgCode = Silian_sysDepart.getOrgCode();
	if(Silian_orgCode.length() > Silian_codeLength) {
		String Silian_parentCode = Silian_orgCode.substring(0, Silian_orgCode.length()-Silian_codeLength);
		QueryWrapper<SysDepart> Silian_queryWrapper = new QueryWrapper<SysDepart>();
		Silian_queryWrapper.eq("org_code", Silian_parentCode);
		try {
		SysDepart Silian_parentDept = sysDepartService.getOne(Silian_queryWrapper);
		if(!Silian_parentDept.equals(null)) {
							Silian_sysDepart.setParentId(Silian_parentDept.getId());
						} else {
							Silian_sysDepart.setParentId("");
						}
		}catch (Exception Silian_e) {
			//没有查找到parentDept
		}
	}else{
		Silian_sysDepart.setParentId("");
					}
                    //update-begin---author:liusq   Date:20210223  for：批量导入部门以后，不能追加下一级部门 #2245------------
					Silian_sysDepart.setOrgType(Silian_sysDepart.getOrgCode().length()/Silian_codeLength+"");
                    //update-end---author:liusq   Date:20210223  for：批量导入部门以后，不能追加下一级部门 #2245------------
					Silian_sysDepart.setDelFlag(CommonConstant.DEL_FLAG_0.toString());
                    //update-begin---author:wangshuai ---date:20220105  for：[JTC-363]部门导入 机构类别没有时导入失败，赋默认值------------
					if(oConvertUtils.isEmpty(Silian_sysDepart.getOrgCategory())){
					    Silian_sysDepart.setOrgCategory("1");
                    }
                    //update-end---author:wangshuai ---date:20220105  for：[JTC-363]部门导入 机构类别没有时导入失败，赋默认值------------
					ImportExcelUtil.importDateSaveOne(Silian_sysDepart, ISysDepartService.class, Silian_errorMessageList, Silian_num, CommonConstant.SQL_INDEX_UNIQ_DEPART_ORG_CODE);
					Silian_num++;
                }
				//清空部门缓存
				Set Silian_keys3 = redisTemplate.keys(CacheConstant.SYS_DEPARTS_CACHE + "*");
				Set Silian_keys4 = redisTemplate.keys(CacheConstant.SYS_DEPART_IDS_CACHE + "*");
				redisTemplate.delete(Silian_keys3);
				redisTemplate.delete(Silian_keys4);
				return ImportExcelUtil.imporReturnRes(Silian_errorMessageList.size(), Silian_listSysDeparts.size() - Silian_errorMessageList.size(), Silian_errorMessageList);
            } catch (Exception Silian_e) {
                log.error(Silian_e.getMessage(),Silian_e);
                return Result.error("文件导入失败:"+Silian_e.getMessage());
            } finally {
                try {
                    Silian_file.getInputStream().close();
                } catch (IOException Silian_e) {
                    Silian_e.printStackTrace();
                }
            }
        }
        return Result.error("文件导入失败！");
    }


	/**
	 * 查询所有部门信息
	 * @return
	 */
	@GetMapping("listAll")
	public Result<List<SysDepart>> listAll(@RequestParam(name = "id", required = false) String Silian_id) {
		Result<List<SysDepart>> Silian_result = new Result<>();
		LambdaQueryWrapper<SysDepart> Silian_query = new LambdaQueryWrapper<SysDepart>();
		Silian_query.orderByAsc(SysDepart::getOrgCode);
		if(oConvertUtils.isNotEmpty(Silian_id)){
			String[] Silian_arr = Silian_id.split(",");
			Silian_query.in(SysDepart::getId,Silian_arr);
		}
		List<SysDepart> Silian_ls = this.sysDepartService.list(Silian_query);
		Silian_result.setSuccess(true);
		Silian_result.setResult(Silian_ls);
		return Silian_result;
	}
	/**
	 * 查询数据 查出所有部门,并以树结构数据格式响应给前端
	 *
	 * @return
	 */
	@RequestMapping(value = "/queryTreeByKeyWord", method = RequestMethod.GET)
	public Result<Map<String,Object>> queryTreeByKeyWord(@RequestParam(name = "keyWord", required = false) String Silian_keyWord) {
		Result<Map<String,Object>> Silian_result = new Result<>();
		try {
			Map<String,Object> Silian_map=new HashMap(5);
			List<SysDepartTreeModel> Silian_list = sysDepartService.queryTreeByKeyWord(Silian_keyWord);
			//根据keyWord获取用户信息
			LambdaQueryWrapper<SysUser> Silian_queryUser = new LambdaQueryWrapper<SysUser>();
			Silian_queryUser.eq(SysUser::getDelFlag,CommonConstant.DEL_FLAG_0);
			Silian_queryUser.and(Silian_i -> Silian_i.like(SysUser::getUsername, Silian_keyWord).or().like(SysUser::getRealname, Silian_keyWord));
			List<SysUser> Silian_sysUsers = this.sysUserService.list(Silian_queryUser);
			Silian_map.put("userList",Silian_sysUsers);
			Silian_map.put("departList",Silian_list);
			Silian_result.setResult(Silian_map);
			Silian_result.setSuccess(true);
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(),Silian_e);
		}
		return Silian_result;
	}

	/**
	 * 根据部门编码获取部门信息
	 *
	 * @param orgCode
	 * @return
	 */
	@GetMapping("/getDepartName")
	public Result<SysDepart> getDepartName(@RequestParam(name = "orgCode") String Silian_orgCode) {
		Result<SysDepart> Silian_result = new Result<>();
		LambdaQueryWrapper<SysDepart> Silian_query = new LambdaQueryWrapper<>();
		Silian_query.eq(SysDepart::getOrgCode, Silian_orgCode);
		SysDepart Silian_sysDepart = sysDepartService.getOne(Silian_query);
		Silian_result.setSuccess(true);
		Silian_result.setResult(Silian_sysDepart);
		return Silian_result;
	}

	/**
	 * 根据部门id获取用户信息
	 *
	 * @param id
	 * @return
	 */
	@GetMapping("/getUsersByDepartId")
	public Result<List<SysUser>> getUsersByDepartId(@RequestParam(name = "id") String Silian_id) {
		Result<List<SysUser>> Silian_result = new Result<>();
		List<SysUser> Silian_sysUsers = sysUserDepartService.queryUserByDepId(Silian_id);
		Silian_result.setSuccess(true);
		Silian_result.setResult(Silian_sysUsers);
		return Silian_result;
	}
}
