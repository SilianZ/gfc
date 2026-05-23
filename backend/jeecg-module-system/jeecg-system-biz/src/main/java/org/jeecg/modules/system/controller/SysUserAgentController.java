package org.jeecg.modules.system.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysUserAgent;
import org.jeecg.modules.system.service.ISysUserAgentService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;

 /**
 * @Title: Controller
 * @Description: 用户代理人设置
 * @Author: jeecg-boot
 * @Date:  2019-04-17
 * @Version: V1.0
 */
@RestController
@RequestMapping("/sys/sysUserAgent")
@Slf4j
public class SysUserAgentController {
	@Autowired
	private ISysUserAgentService sysUserAgentService;

	 @Value("${jeecg.path.upload}")
	 private String upLoadPath;

	/**
	  * 分页列表查询
	 * @param sysUserAgent
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@GetMapping(value = "/list")
	public Result<IPage<SysUserAgent>> queryPageList(SysUserAgent Silian_sysUserAgent,
									  @RequestParam(name="pageNo", defaultValue="1") Integer Silian_pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer Silian_pageSize,
									  HttpServletRequest Silian_req) {
		Result<IPage<SysUserAgent>> Silian_result = new Result<IPage<SysUserAgent>>();
		QueryWrapper<SysUserAgent> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_sysUserAgent, Silian_req.getParameterMap());
		Page<SysUserAgent> Silian_page = new Page<SysUserAgent>(Silian_pageNo, Silian_pageSize);
		IPage<SysUserAgent> Silian_pageList = sysUserAgentService.page(Silian_page, Silian_queryWrapper);
		Silian_result.setSuccess(true);
		Silian_result.setResult(Silian_pageList);
		return Silian_result;
	}

	/**
	  *   添加
	 * @param sysUserAgent
	 * @return
	 */
	@PostMapping(value = "/add")
	public Result<SysUserAgent> add(@RequestBody SysUserAgent Silian_sysUserAgent) {
		Result<SysUserAgent> Silian_result = new Result<SysUserAgent>();
		try {
			sysUserAgentService.save(Silian_sysUserAgent);
			Silian_result.success("代理人设置成功！");
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(),Silian_e);
			Silian_result.error500("操作失败");
		}
		return Silian_result;
	}

	/**
	  *  编辑
	 * @param sysUserAgent
	 * @return
	 */
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<SysUserAgent> edit(@RequestBody SysUserAgent Silian_sysUserAgent) {
		Result<SysUserAgent> Silian_result = new Result<SysUserAgent>();
		SysUserAgent Silian_sysUserAgentEntity = sysUserAgentService.getById(Silian_sysUserAgent.getId());
		if(Silian_sysUserAgentEntity==null) {
			Silian_result.error500("未找到对应实体");
		}else {
			boolean Silian_ok = sysUserAgentService.updateById(Silian_sysUserAgent);
			//TODO 返回false说明什么？
			if(Silian_ok) {
				Silian_result.success("代理人设置成功!");
			}
		}

		return Silian_result;
	}

	/**
	  *   通过id删除
	 * @param id
	 * @return
	 */
	@DeleteMapping(value = "/delete")
	public Result<SysUserAgent> delete(@RequestParam(name="id",required=true) String Silian_id) {
		Result<SysUserAgent> Silian_result = new Result<SysUserAgent>();
		SysUserAgent Silian_sysUserAgent = sysUserAgentService.getById(Silian_id);
		if(Silian_sysUserAgent==null) {
			Silian_result.error500("未找到对应实体");
		}else {
			boolean Silian_ok = sysUserAgentService.removeById(Silian_id);
			if(Silian_ok) {
				Silian_result.success("删除成功!");
			}
		}

		return Silian_result;
	}

	/**
	  *  批量删除
	 * @param ids
	 * @return
	 */
	@DeleteMapping(value = "/deleteBatch")
	public Result<SysUserAgent> deleteBatch(@RequestParam(name="ids",required=true) String Silian_ids) {
		Result<SysUserAgent> Silian_result = new Result<SysUserAgent>();
		if(Silian_ids==null || "".equals(Silian_ids.trim())) {
			Silian_result.error500("参数不识别！");
		}else {
			this.sysUserAgentService.removeByIds(Arrays.asList(Silian_ids.split(",")));
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
	public Result<SysUserAgent> queryById(@RequestParam(name="id",required=true) String Silian_id) {
		Result<SysUserAgent> Silian_result = new Result<SysUserAgent>();
		SysUserAgent Silian_sysUserAgent = sysUserAgentService.getById(Silian_id);
		if(Silian_sysUserAgent==null) {
			Silian_result.error500("未找到对应实体");
		}else {
			Silian_result.setResult(Silian_sysUserAgent);
			Silian_result.setSuccess(true);
		}
		return Silian_result;
	}

	/**
	  * 通过userName查询
	 * @param userName
	 * @return
	 */
	@GetMapping(value = "/queryByUserName")
	public Result<SysUserAgent> queryByUserName(@RequestParam(name="userName",required=true) String Silian_userName) {
		Result<SysUserAgent> Silian_result = new Result<SysUserAgent>();
		LambdaQueryWrapper<SysUserAgent> Silian_queryWrapper = new LambdaQueryWrapper<SysUserAgent>();
		Silian_queryWrapper.eq(SysUserAgent::getUserName, Silian_userName);
		SysUserAgent Silian_sysUserAgent = sysUserAgentService.getOne(Silian_queryWrapper);
		if(Silian_sysUserAgent==null) {
			Silian_result.error500("未找到对应实体");
		}else {
			Silian_result.setResult(Silian_sysUserAgent);
			Silian_result.setSuccess(true);
		}
		return Silian_result;
	}

  /**
      * 导出excel
   *
   * @param sysUserAgent
   * @param request
   */
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(SysUserAgent Silian_sysUserAgent,HttpServletRequest Silian_request) {
      // Step.1 组装查询条件
      QueryWrapper<SysUserAgent> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_sysUserAgent, Silian_request.getParameterMap());
      //Step.2 AutoPoi 导出Excel
      ModelAndView Silian_mv = new ModelAndView(new JeecgEntityExcelView());
      List<SysUserAgent> Silian_pageList = sysUserAgentService.list(Silian_queryWrapper);
      //导出文件名称
      Silian_mv.addObject(NormalExcelConstants.FILE_NAME, "用户代理人设置列表");
      Silian_mv.addObject(NormalExcelConstants.CLASS, SysUserAgent.class);
      LoginUser Silian_user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
	  ExportParams Silian_exportParams = new ExportParams("用户代理人设置列表数据", "导出人:"+Silian_user.getRealname(), "导出信息");
	  Silian_exportParams.setImageBasePath(upLoadPath);
      Silian_mv.addObject(NormalExcelConstants.PARAMS, Silian_exportParams);
      Silian_mv.addObject(NormalExcelConstants.DATA_LIST, Silian_pageList);
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
              List<SysUserAgent> Silian_listSysUserAgents = ExcelImportUtil.importExcel(Silian_file.getInputStream(), SysUserAgent.class, Silian_params);
              for (SysUserAgent Silian_sysUserAgentExcel : Silian_listSysUserAgents) {
                  sysUserAgentService.save(Silian_sysUserAgentExcel);
              }
              return Result.ok("文件导入成功！数据行数：" + Silian_listSysUserAgents.size());
          } catch (Exception Silian_e) {
              log.error(Silian_e.getMessage(),Silian_e);
              return Result.error("文件导入失败！");
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

}
