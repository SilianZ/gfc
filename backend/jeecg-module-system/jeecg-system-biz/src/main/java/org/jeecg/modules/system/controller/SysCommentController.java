package org.jeecg.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.dto.DataLogDTO;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.system.entity.SysComment;
import org.jeecg.modules.system.service.ISysCommentService;
import org.jeecg.modules.system.vo.SysCommentFileVo;
import org.jeecg.modules.system.vo.SysCommentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 系统评论回复表
 * @Author: jeecg-boot
 * @Date: 2022-07-19
 * @Version: V1.0
 */
@Api(tags = "系统评论回复表")
@RestController
@RequestMapping("/sys/comment")
@Slf4j
public class SysCommentController extends JeecgController<SysComment, ISysCommentService> {

    @Autowired
    private ISysCommentService sysCommentService;

    @Autowired
    private ISysBaseAPI sysBaseAPI;


    /**
     * 在线预览文件地址
     */
    @Value("${jeecg.file-view-domain}/onlinePreview")
    private String onlinePreviewDomain;

    /**
     * 查询评论+文件
     *
     * @param sysComment
     * @return
     */
    @ApiOperation(value = "系统评论回复表-列表查询", notes = "系统评论回复表-列表查询")
    @GetMapping(value = "/listByForm")
    public Result<IPage<SysCommentVO>> queryListByForm(SysComment Silian_sysComment) {
        List<SysCommentVO> Silian_list = sysCommentService.queryFormCommentInfo(Silian_sysComment);
        IPage<SysCommentVO> Silian_pageList = new Page();
        Silian_pageList.setRecords(Silian_list);
        return Result.OK(Silian_pageList);
    }

    /**
     * 查询文件
     *
     * @param sysComment
     * @return
     */
    @ApiOperation(value = "系统评论回复表-列表查询", notes = "系统评论回复表-列表查询")
    @GetMapping(value = "/fileList")
    public Result<IPage<SysCommentFileVo>> queryFileList(SysComment Silian_sysComment) {
        List<SysCommentFileVo> Silian_list = sysCommentService.queryFormFileList(Silian_sysComment.getTableName(), Silian_sysComment.getTableDataId());
        IPage<SysCommentFileVo> Silian_pageList = new Page();
        Silian_pageList.setRecords(Silian_list);
        return Result.OK(Silian_pageList);
    }

    @ApiOperation(value = "系统评论表-添加文本", notes = "系统评论表-添加文本")
    @PostMapping(value = "/addText")
    public Result<String> addText(@RequestBody SysComment Silian_sysComment) {
        String Silian_commentId = sysCommentService.saveOne(Silian_sysComment);
        return Result.OK(Silian_commentId);
    }

    @ApiOperation(value = "系统评论表-添加文件", notes = "系统评论表-添加文件")
    @PostMapping(value = "/addFile")
    public Result<String> addFile(HttpServletRequest Silian_request) {
        try {
            sysCommentService.saveOneFileComment(Silian_request);
            return Result.OK("success");
        } catch (Exception Silian_e) {
            log.error("评论文件上传失败", Silian_e.getMessage());
            return Result.error("操作失败," + Silian_e.getMessage());
        }
    }

    @ApiOperation(value = "系统评论回复表-通过id删除", notes = "系统评论回复表-通过id删除")
    @DeleteMapping(value = "/deleteOne")
    public Result<String> deleteOne(@RequestParam(name = "id", required = true) String Silian_id) {
        SysComment Silian_comment = sysCommentService.getById(Silian_id);
        if(Silian_comment==null){
            return Result.error("该评论已被删除！");
        }
        LoginUser Silian_sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String Silian_username = Silian_sysUser.getUsername();
        String Silian_admin = "admin";
        //除了admin外 其他人只能删除自己的评论
        if((!Silian_admin.equals(Silian_username)) && !Silian_username.equals(Silian_comment.getCreateBy())){
            return Result.error("只能删除自己的评论！");
        }
        sysCommentService.deleteOne(Silian_id);
        //删除评论添加日志
        String Silian_logContent = "删除了评论， "+ Silian_comment.getCommentContent();
        DataLogDTO Silian_dataLog = new DataLogDTO(Silian_comment.getTableName(), Silian_comment.getTableDataId(), Silian_logContent, CommonConstant.DATA_LOG_TYPE_COMMENT);
        sysBaseAPI.saveDataLog(Silian_dataLog);
        return Result.OK("删除成功!");
    }


    /**
     * 获取文件预览的地址
     * @return
     */
    @GetMapping(value = "/getFileViewDomain")
    public Result<String> getFileViewDomain() {
        return Result.OK(onlinePreviewDomain);
    }


    /**
     * 分页列表查询
     *
     * @param sysComment
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    ////@AutoLog(value = "系统评论回复表-分页列表查询")
    @ApiOperation(value = "系统评论回复表-分页列表查询", notes = "系统评论回复表-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<SysComment>> queryPageList(SysComment Silian_sysComment,
                                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer Silian_pageNo,
                                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer Silian_pageSize,
                                                   HttpServletRequest Silian_req) {
        QueryWrapper<SysComment> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_sysComment, Silian_req.getParameterMap());
        Page<SysComment> Silian_page = new Page<SysComment>(Silian_pageNo, Silian_pageSize);
        IPage<SysComment> Silian_pageList = sysCommentService.page(Silian_page, Silian_queryWrapper);
        return Result.OK(Silian_pageList);
    }


    /**
     * 添加
     *
     * @param sysComment
     * @return
     */
    @ApiOperation(value = "系统评论回复表-添加", notes = "系统评论回复表-添加")
    //@RequiresPermissions("org.jeecg.modules.demo:sys_comment:add")
    @PostMapping(value = "/add")
    public Result<String> add(@RequestBody SysComment Silian_sysComment) {
        sysCommentService.save(Silian_sysComment);
        return Result.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param sysComment
     * @return
     */
    //@AutoLog(value = "系统评论回复表-编辑")
    @ApiOperation(value = "系统评论回复表-编辑", notes = "系统评论回复表-编辑")
    //@RequiresPermissions("org.jeecg.modules.demo:sys_comment:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody SysComment Silian_sysComment) {
        sysCommentService.updateById(Silian_sysComment);
        return Result.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    //@AutoLog(value = "系统评论回复表-通过id删除")
    @ApiOperation(value = "系统评论回复表-通过id删除", notes = "系统评论回复表-通过id删除")
    //@RequiresPermissions("org.jeecg.modules.demo:sys_comment:delete")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@RequestParam(name = "id", required = true) String Silian_id) {
        sysCommentService.removeById(Silian_id);
        return Result.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    //@AutoLog(value = "系统评论回复表-批量删除")
    @ApiOperation(value = "系统评论回复表-批量删除", notes = "系统评论回复表-批量删除")
    //@RequiresPermissions("org.jeecg.modules.demo:sys_comment:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String Silian_ids) {
        this.sysCommentService.removeByIds(Arrays.asList(Silian_ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    ////@AutoLog(value = "系统评论回复表-通过id查询")
    @ApiOperation(value = "系统评论回复表-通过id查询", notes = "系统评论回复表-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<SysComment> queryById(@RequestParam(name = "id", required = true) String Silian_id) {
        SysComment Silian_sysComment = sysCommentService.getById(Silian_id);
        if (Silian_sysComment == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(Silian_sysComment);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param sysComment
     */
    //@RequiresPermissions("org.jeecg.modules.demo:sys_comment:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest Silian_request, SysComment Silian_sysComment) {
        return super.exportXls(Silian_request, Silian_sysComment, SysComment.class, "系统评论回复表");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    //@RequiresPermissions("sys_comment:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest Silian_request, HttpServletResponse Silian_response) {
        return super.importExcel(Silian_request, Silian_response, SysComment.class);
    }


}
