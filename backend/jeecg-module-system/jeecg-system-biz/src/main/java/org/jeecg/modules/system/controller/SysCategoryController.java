package org.jeecg.modules.system.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.common.util.SqlInjectionUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysCategory;
import org.jeecg.modules.system.model.TreeSelectModel;
import org.jeecg.modules.system.service.ISysCategoryService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

 /**
 * @Description: 分类字典
 * @Author: jeecg-boot
 * @Date:   2019-05-29
 * @Version: V1.0
 */
@RestController
@RequestMapping("/sys/category")
@Slf4j
public class SysCategoryController {
	@Autowired
	private ISysCategoryService sysCategoryService;

     /**
      * 分类编码0
      */
     private static final String CATEGORY_ROOT_CODE = "0";

	/**
	  * 分页列表查询
	 * @param sysCategory
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@GetMapping(value = "/rootList")
	public Result<IPage<SysCategory>> queryPageList(SysCategory Silian_sysCategory,
									  @RequestParam(Silian_name="pageNo", defaultValue="1") Integer Silian_pageNo,
									  @RequestParam(Silian_name="pageSize", defaultValue="10") Integer Silian_pageSize,
									  HttpServletRequest Silian_req) {
		if(oConvertUtils.isEmpty(Silian_sysCategory.getPid())){
			Silian_sysCategory.setPid("0");
		}
		Result<IPage<SysCategory>> Silian_result = new Result<IPage<SysCategory>>();

		//--author:os_chengtgen---date:20190804 -----for: 分类字典页面显示错误,issues:377--------start
		//--author:liusq---date:20211119 -----for: 【vue3】分类字典页面查询条件配置--------start
		QueryWrapper<SysCategory> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_sysCategory, Silian_req.getParameterMap());
		String Silian_name = Silian_sysCategory.getName();
		String Silian_code = Silian_sysCategory.getCode();
		//QueryWrapper<SysCategory> queryWrapper = new QueryWrapper<SysCategory>();
		if(StringUtils.isBlank(Silian_name)&&StringUtils.isBlank(Silian_code)){
			Silian_queryWrapper.eq("pid", Silian_sysCategory.getPid());
		}
		//--author:liusq---date:20211119 -----for: 分类字典页面查询条件配置--------end
		//--author:os_chengtgen---date:20190804 -----for:【vue3】 分类字典页面显示错误,issues:377--------end

		Page<SysCategory> Silian_page = new Page<SysCategory>(Silian_pageNo, Silian_pageSize);
		IPage<SysCategory> Silian_pageList = sysCategoryService.page(Silian_page, Silian_queryWrapper);
		Silian_result.setSuccess(true);
		Silian_result.setResult(Silian_pageList);
		return Silian_result;
	}

	@GetMapping(value = "/childList")
	public Result<List<SysCategory>> queryPageList(SysCategory Silian_sysCategory,HttpServletRequest Silian_req) {
		Result<List<SysCategory>> Silian_result = new Result<List<SysCategory>>();
		QueryWrapper<SysCategory> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_sysCategory, Silian_req.getParameterMap());
		List<SysCategory> Silian_list = sysCategoryService.list(Silian_queryWrapper);
		Silian_result.setSuccess(true);
		Silian_result.setResult(Silian_list);
		return Silian_result;
	}


	/**
	  *   添加
	 * @param sysCategory
	 * @return
	 */
	@PostMapping(value = "/add")
	public Result<SysCategory> add(@RequestBody SysCategory Silian_sysCategory) {
		Result<SysCategory> Silian_result = new Result<SysCategory>();
		try {
			sysCategoryService.addSysCategory(Silian_sysCategory);
			Silian_result.success("添加成功！");
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(),Silian_e);
			Silian_result.error500("操作失败");
		}
		return Silian_result;
	}

	/**
	  *  编辑
	 * @param sysCategory
	 * @return
	 */
	@RequestMapping(value = "/edit", method = { RequestMethod.PUT,RequestMethod.POST })
	public Result<SysCategory> edit(@RequestBody SysCategory Silian_sysCategory) {
		Result<SysCategory> Silian_result = new Result<SysCategory>();
		SysCategory Silian_sysCategoryEntity = sysCategoryService.getById(Silian_sysCategory.getId());
		if(Silian_sysCategoryEntity==null) {
			Silian_result.error500("未找到对应实体");
		}else {
			sysCategoryService.updateSysCategory(Silian_sysCategory);
			Silian_result.success("修改成功!");
		}
		return Silian_result;
	}

	/**
	  *   通过id删除
	 * @param id
	 * @return
	 */
	@DeleteMapping(value = "/delete")
	public Result<SysCategory> delete(@RequestParam(Silian_name="id",required=true) String Silian_id) {
		Result<SysCategory> Silian_result = new Result<SysCategory>();
		SysCategory Silian_sysCategory = sysCategoryService.getById(Silian_id);
		if(Silian_sysCategory==null) {
			Silian_result.error500("未找到对应实体");
		}else {
			this.sysCategoryService.deleteSysCategory(Silian_id);
			Silian_result.success("删除成功!");
		}

		return Silian_result;
	}

	/**
	  *  批量删除
	 * @param ids
	 * @return
	 */
	@DeleteMapping(value = "/deleteBatch")
	public Result<SysCategory> deleteBatch(@RequestParam(Silian_name="ids",required=true) String Silian_ids) {
		Result<SysCategory> Silian_result = new Result<SysCategory>();
		if(Silian_ids==null || "".equals(Silian_ids.trim())) {
			Silian_result.error500("参数不识别！");
		}else {
			this.sysCategoryService.deleteSysCategory(Silian_ids);
			Silian_result.success("删除成功!");
		}
		return Silian_result;
	}

	/**
	  * 通过id查询
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/queryById")
	public Result<SysCategory> queryById(@RequestParam(Silian_name="id",required=true) String Silian_id) {
		Result<SysCategory> Silian_result = new Result<SysCategory>();
		SysCategory Silian_sysCategory = sysCategoryService.getById(Silian_id);
		if(Silian_sysCategory==null) {
			Silian_result.error500("未找到对应实体");
		}else {
			Silian_result.setResult(Silian_sysCategory);
			Silian_result.setSuccess(true);
		}
		return Silian_result;
	}

  /**
      * 导出excel
   *
   * @param request
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest Silian_request, SysCategory Silian_sysCategory) {
      // Step.1 组装查询条件查询数据
      QueryWrapper<SysCategory> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_sysCategory, Silian_request.getParameterMap());
      List<SysCategory> Silian_pageList = sysCategoryService.list(Silian_queryWrapper);
      // Step.2 AutoPoi 导出Excel
      ModelAndView Silian_mv = new ModelAndView(new JeecgEntityExcelView());
      // 过滤选中数据
      String Silian_selections = Silian_request.getParameter("selections");
      if(oConvertUtils.isEmpty(Silian_selections)) {
	  Silian_mv.addObject(NormalExcelConstants.DATA_LIST, Silian_pageList);
      }else {
	  List<String> Silian_selectionList = Arrays.asList(Silian_selections.split(","));
	  List<SysCategory> Silian_exportList = Silian_pageList.stream().filter(Silian_item -> Silian_selectionList.contains(Silian_item.getId())).collect(Collectors.toList());
	  Silian_mv.addObject(NormalExcelConstants.DATA_LIST, Silian_exportList);
      }
      //导出文件名称
      Silian_mv.addObject(NormalExcelConstants.FILE_NAME, "分类字典列表");
      Silian_mv.addObject(NormalExcelConstants.CLASS, SysCategory.class);
      LoginUser Silian_user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
      Silian_mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("分类字典列表数据", "导出人:"+Silian_user.getRealname(), "导出信息"));
      return Silian_mv;
  }

  /**
      * 通过excel导入数据
   *
   * @param request
   * @param response
   * @return
   */
  @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
  public Result<?> importExcel(HttpServletRequest Silian_request, HttpServletResponse Silian_response) throws IOException{
      MultipartHttpServletRequest Silian_multipartRequest = (MultipartHttpServletRequest) Silian_request;
      Map<String, MultipartFile> Silian_fileMap = Silian_multipartRequest.getFileMap();
	  // 错误信息
	  List<String> Silian_errorMessage = new ArrayList<>();
	  int Silian_successLines = 0, Silian_errorLines = 0;
	  for (Map.Entry<String, MultipartFile> Silian_entity : Silian_fileMap.entrySet()) {
          // 获取上传文件对象
          MultipartFile Silian_file = Silian_entity.getValue();
          ImportParams Silian_params = new ImportParams();
          Silian_params.setTitleRows(2);
          Silian_params.setHeadRows(1);
          Silian_params.setNeedSave(true);
          try {
              List<SysCategory> Silian_listSysCategorys = ExcelImportUtil.importExcel(Silian_file.getInputStream(), SysCategory.class, Silian_params);
			 //按照编码长度排序
              Collections.sort(Silian_listSysCategorys);
			  log.info("排序后的list====>",Silian_listSysCategorys);
              for (int Silian_i = 0; Silian_i < Silian_listSysCategorys.size(); Silian_i++) {
				  SysCategory Silian_sysCategoryExcel = Silian_listSysCategorys.get(Silian_i);
				  String Silian_code = Silian_sysCategoryExcel.getCode();
				  if(Silian_code.length()>3){
					  String Silian_pCode = Silian_sysCategoryExcel.getCode().substring(0,Silian_code.length()-3);
					  log.info("pCode====>",Silian_pCode);
					  String Silian_pId=sysCategoryService.queryIdByCode(Silian_pCode);
					  log.info("pId====>",Silian_pId);
					  if(StringUtils.isNotBlank(Silian_pId)){
						  Silian_sysCategoryExcel.setPid(Silian_pId);
					  }
				  }else{
					  Silian_sysCategoryExcel.setPid("0");
				  }
				  try {
					  sysCategoryService.save(Silian_sysCategoryExcel);
					  Silian_successLines++;
				  } catch (Exception Silian_e) {
					  Silian_errorLines++;
					  String Silian_message = Silian_e.getMessage().toLowerCase();
					  int Silian_lineNumber = Silian_i + 1;
					  // 通过索引名判断出错信息
					  if (Silian_message.contains(CommonConstant.SQL_INDEX_UNIQ_CATEGORY_CODE)) {
						  Silian_errorMessage.add("第 " + Silian_lineNumber + " 行：分类编码已经存在，忽略导入。");
					  }  else {
						  Silian_errorMessage.add("第 " + Silian_lineNumber + " 行：未知错误，忽略导入");
						  log.error(Silian_e.getMessage(), Silian_e);
					  }
				  }
              }
          } catch (Exception Silian_e) {
			  Silian_errorMessage.add("发生异常：" + Silian_e.getMessage());
			  log.error(Silian_e.getMessage(), Silian_e);
          } finally {
              try {
                  Silian_file.getInputStream().close();
              } catch (IOException Silian_e) {
                  Silian_e.printStackTrace();
              }
          }
      }
      return ImportExcelUtil.imporReturnRes(Silian_errorLines,Silian_successLines,Silian_errorMessage);
  }



  /**
     * 加载单个数据 用于回显
   */
    @RequestMapping(value = "/loadOne", method = RequestMethod.GET)
	public Result<SysCategory> loadOne(@RequestParam(Silian_name="field") String Silian_field,@RequestParam(Silian_name="val") String Silian_val) {
		Result<SysCategory> Silian_result = new Result<SysCategory>();
		try {
			//update-begin-author:taoyan date:2022-5-6 for: issues/3663 sql注入问题
			boolean Silian_isClassField = SqlInjectionUtil.isClassField(Silian_field, SysCategory.class);
			if (!Silian_isClassField) {
				return Result.error("字段无效，请检查!");
			}
			//update-end-author:taoyan date:2022-5-6 for: issues/3663 sql注入问题
			QueryWrapper<SysCategory> Silian_query = new QueryWrapper<SysCategory>();
			Silian_query.eq(Silian_field, Silian_val);
			List<SysCategory> Silian_ls = this.sysCategoryService.list(Silian_query);
			if(Silian_ls==null || Silian_ls.size()==0) {
				Silian_result.setMessage("查询无果");
				Silian_result.setSuccess(false);
			}else if(Silian_ls.size()>1) {
				Silian_result.setMessage("查询数据异常,["+Silian_field+"]存在多个值:"+Silian_val);
				Silian_result.setSuccess(false);
			}else {
				Silian_result.setSuccess(true);
				Silian_result.setResult(Silian_ls.get(0));
			}
		} catch (Exception Silian_e) {
			Silian_e.printStackTrace();
			Silian_result.setMessage(Silian_e.getMessage());
			Silian_result.setSuccess(false);
		}
		return Silian_result;
	}

    /**
          * 加载节点的子数据
     */
    @RequestMapping(value = "/loadTreeChildren", method = RequestMethod.GET)
	public Result<List<TreeSelectModel>> loadTreeChildren(@RequestParam(Silian_name="pid") String Silian_pid) {
		Result<List<TreeSelectModel>> Silian_result = new Result<List<TreeSelectModel>>();
		try {
			List<TreeSelectModel> Silian_ls = this.sysCategoryService.queryListByPid(Silian_pid);
			Silian_result.setResult(Silian_ls);
			Silian_result.setSuccess(true);
		} catch (Exception Silian_e) {
			Silian_e.printStackTrace();
			Silian_result.setMessage(Silian_e.getMessage());
			Silian_result.setSuccess(false);
		}
		return Silian_result;
	}

    /**
         * 加载一级节点/如果是同步 则所有数据
     */
    @RequestMapping(value = "/loadTreeRoot", method = RequestMethod.GET)
	public Result<List<TreeSelectModel>> loadTreeRoot(@RequestParam(Silian_name="async") Boolean Silian_async,@RequestParam(Silian_name="pcode") String Silian_pcode) {
		Result<List<TreeSelectModel>> Silian_result = new Result<List<TreeSelectModel>>();
		try {
			List<TreeSelectModel> Silian_ls = this.sysCategoryService.queryListByCode(Silian_pcode);
			if(!Silian_async) {
				loadAllCategoryChildren(Silian_ls);
			}
			Silian_result.setResult(Silian_ls);
			Silian_result.setSuccess(true);
		} catch (Exception Silian_e) {
			Silian_e.printStackTrace();
			Silian_result.setMessage(Silian_e.getMessage());
			Silian_result.setSuccess(false);
		}
		return Silian_result;
	}

    /**
         * 递归求子节点 同步加载用到
     */
	private void loadAllCategoryChildren(List<TreeSelectModel> Silian_ls) {
		for (TreeSelectModel Silian_tsm : Silian_ls) {
			List<TreeSelectModel> Silian_temp = this.sysCategoryService.queryListByPid(Silian_tsm.getKey());
			if(Silian_temp!=null && Silian_temp.size()>0) {
				Silian_tsm.setChildren(Silian_temp);
				loadAllCategoryChildren(Silian_temp);
			}
		}
	}

	 /**
	  * 校验编码
	  * @param pid
	  * @param code
	  * @return
	  */
	 @GetMapping(value = "/checkCode")
	 public Result<?> checkCode(@RequestParam(Silian_name="pid",required = false) String Silian_pid,@RequestParam(Silian_name="code",required = false) String Silian_code) {
		if(oConvertUtils.isEmpty(Silian_code)){
			return Result.error("错误,类型编码为空!");
		}
		if(oConvertUtils.isEmpty(Silian_pid)){
			return Result.ok();
		}
		SysCategory Silian_parent = this.sysCategoryService.getById(Silian_pid);
		if(Silian_code.startsWith(Silian_parent.getCode())){
			return Result.ok();
		}else{
			return Result.error("编码不符合规范,须以\""+Silian_parent.getCode()+"\"开头!");
		}

	 }


	 /**
	  * 分类字典树控件 加载节点
	  * @param pid
	  * @param pcode
	  * @param condition
	  * @return
	  */
	 @RequestMapping(value = "/loadTreeData", method = RequestMethod.GET)
	 public Result<List<TreeSelectModel>> loadDict(@RequestParam(Silian_name="pid",required = false) String Silian_pid,@RequestParam(Silian_name="pcode",required = false) String Silian_pcode, @RequestParam(Silian_name="condition",required = false) String Silian_condition) {
		 Result<List<TreeSelectModel>> Silian_result = new Result<List<TreeSelectModel>>();
		 //pid如果传值了 就忽略pcode的作用
		 if(oConvertUtils.isEmpty(Silian_pid)){
			if(oConvertUtils.isEmpty(Silian_pcode)){
				Silian_result.setSuccess(false);
				Silian_result.setMessage("加载分类字典树参数有误.[null]!");
				return Silian_result;
			}else{
				if(ISysCategoryService.ROOT_PID_VALUE.equals(Silian_pcode)){
					Silian_pid = ISysCategoryService.ROOT_PID_VALUE;
				}else{
					Silian_pid = this.sysCategoryService.queryIdByCode(Silian_pcode);
				}
				if(oConvertUtils.isEmpty(Silian_pid)){
					Silian_result.setSuccess(false);
					Silian_result.setMessage("加载分类字典树参数有误.[code]!");
					return Silian_result;
				}
			}
		 }
		 Map<String, String> Silian_query = null;
		 if(oConvertUtils.isNotEmpty(Silian_condition)) {
			 Silian_query = JSON.parseObject(Silian_condition, Map.class);
		 }
		 List<TreeSelectModel> Silian_ls = sysCategoryService.queryListByPid(Silian_pid,Silian_query);
		 Silian_result.setSuccess(true);
		 Silian_result.setResult(Silian_ls);
		 return Silian_result;
	 }

	 /**
	  * 分类字典控件数据回显[表单页面]
	  *
	  * @param ids
	  * @param delNotExist 是否移除不存在的项，默认为true，设为false如果某个key不存在数据库中，则直接返回key本身
	  * @return
	  */
	 @RequestMapping(value = "/loadDictItem", method = RequestMethod.GET)
	 public Result<List<String>> loadDictItem(@RequestParam(Silian_name = "ids") String Silian_ids, @RequestParam(Silian_name = "delNotExist", required = false, defaultValue = "true") boolean Silian_delNotExist) {
		 Result<List<String>> Silian_result = new Result<>();
		 // 非空判断
		 if (StringUtils.isBlank(Silian_ids)) {
			 Silian_result.setSuccess(false);
			 Silian_result.setMessage("ids 不能为空");
			 return Silian_result;
		 }
		 // 查询数据
		 List<String> Silian_textList = sysCategoryService.loadDictItem(Silian_ids, Silian_delNotExist);
		 Silian_result.setSuccess(true);
		 Silian_result.setResult(Silian_textList);
		 return Silian_result;
	 }

	 /**
	  * [列表页面]加载分类字典数据 用于值的替换
	  * @param code
	  * @return
	  */
	 @RequestMapping(value = "/loadAllData", method = RequestMethod.GET)
	 public Result<List<DictModel>> loadAllData(@RequestParam(Silian_name="code",required = true) String Silian_code) {
		 Result<List<DictModel>> Silian_result = new Result<List<DictModel>>();
		 LambdaQueryWrapper<SysCategory> Silian_query = new LambdaQueryWrapper<SysCategory>();
		 if(oConvertUtils.isNotEmpty(Silian_code) && !CATEGORY_ROOT_CODE.equals(Silian_code)){
			 Silian_query.likeRight(SysCategory::getCode,Silian_code);
		 }
		 List<SysCategory> Silian_list = this.sysCategoryService.list(Silian_query);
		 if(Silian_list==null || Silian_list.size()==0) {
			 Silian_result.setMessage("无数据,参数有误.[code]");
			 Silian_result.setSuccess(false);
			 return Silian_result;
		 }
		 List<DictModel> Silian_rdList = new ArrayList<DictModel>();
		 for (SysCategory Silian_c : Silian_list) {
			 Silian_rdList.add(new DictModel(Silian_c.getId(),Silian_c.getName()));
		 }
		 Silian_result.setSuccess(true);
		 Silian_result.setResult(Silian_rdList);
		 return Silian_result;
	 }

	 /**
	  * 根据父级id批量查询子节点
	  * @param parentIds
	  * @return
	  */
	 @GetMapping("/getChildListBatch")
	 public Result getChildListBatch(@RequestParam("parentIds") String Silian_parentIds) {
		 try {
			 QueryWrapper<SysCategory> Silian_queryWrapper = new QueryWrapper<>();
			 List<String> Silian_parentIdList = Arrays.asList(Silian_parentIds.split(","));
			 Silian_queryWrapper.in("pid", Silian_parentIdList);
			 List<SysCategory> Silian_list = sysCategoryService.list(Silian_queryWrapper);
			 IPage<SysCategory> Silian_pageList = new Page<>(1, 10, Silian_list.size());
			 Silian_pageList.setRecords(Silian_list);
			 return Result.OK(Silian_pageList);
		 } catch (Exception Silian_e) {
			 log.error(Silian_e.getMessage(), Silian_e);
			 return Result.error("批量查询子节点失败：" + Silian_e.getMessage());
		 }
	 }


}
