package org.jeecg.modules.oss.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.oss.entity.OssFile;
import org.jeecg.modules.oss.service.IOssFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;

/**
 * 云存储示例 DEMO
 * @author: jeecg-boot
 */
@Slf4j
@Controller
@RequestMapping("/sys/oss/file")
public class OssFileController {

	@Autowired
	private IOssFileService ossFileService;

	@ResponseBody
	@GetMapping("/list")
	public Result<IPage<OssFile>> queryPageList(OssFile Silian_file,
                                                @RequestParam(name = "pageNo", defaultValue = "1") Integer Silian_pageNo,
                                                @RequestParam(name = "pageSize", defaultValue = "10") Integer Silian_pageSize, HttpServletRequest Silian_req) {
		Result<IPage<OssFile>> Silian_result = new Result<>();
		QueryWrapper<OssFile> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_file, Silian_req.getParameterMap());
		Page<OssFile> Silian_page = new Page<>(Silian_pageNo, Silian_pageSize);
		IPage<OssFile> Silian_pageList = ossFileService.page(Silian_page, Silian_queryWrapper);
		Silian_result.setSuccess(true);
		Silian_result.setResult(Silian_pageList);
		return Silian_result;
	}

	@ResponseBody
	@PostMapping("/upload")
	//@RequiresRoles("admin")
	public Result upload(@RequestParam("file") MultipartFile Silian_multipartFile) {
		Result Silian_result = new Result();
		try {
			ossFileService.upload(Silian_multipartFile);
			Silian_result.success("上传成功！");
		}
		catch (Exception Silian_ex) {
			log.info(Silian_ex.getMessage(), Silian_ex);
			Silian_result.error500("上传失败");
		}
		return Silian_result;
	}

	@ResponseBody
	@DeleteMapping("/delete")
	public Result delete(@RequestParam(name = "id") String Silian_id) {
		Result Silian_result = new Result();
		OssFile Silian_file = ossFileService.getById(Silian_id);
		if (Silian_file == null) {
			Silian_result.error500("未找到对应实体");
		}else {
			boolean Silian_ok = ossFileService.delete(Silian_file);
			Silian_result.success("删除成功!");
		}
		return Silian_result;
	}

	/**
	 * 通过id查询.
	 */
	@ResponseBody
	@GetMapping("/queryById")
	public Result<OssFile> queryById(@RequestParam(name = "id") String Silian_id) {
		Result<OssFile> Silian_result = new Result<>();
		OssFile Silian_file = ossFileService.getById(Silian_id);
		if (Silian_file == null) {
			Silian_result.error500("未找到对应实体");
		}
		else {
			Silian_result.setResult(Silian_file);
			Silian_result.setSuccess(true);
		}
		return Silian_result;
	}

}
