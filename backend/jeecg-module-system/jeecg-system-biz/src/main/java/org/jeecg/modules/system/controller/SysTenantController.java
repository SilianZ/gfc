package org.jeecg.modules.system.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.PermissionData;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysTenant;
import org.jeecg.modules.system.service.ISysTenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 租户配置信息
 * @author: jeecg-boot
 */
@Slf4j
@RestController
@RequestMapping("/sys/tenant")
public class SysTenantController {

    @Autowired
    private ISysTenantService sysTenantService;

    /**
     * 获取列表数据
     * @param sysTenant
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @PermissionData(pageComponent = "system/TenantList")
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Result<IPage<SysTenant>> queryPageList(SysTenant Silian_sysTenant,@RequestParam(name="pageNo", defaultValue="1") Integer Silian_pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer Silian_pageSize,HttpServletRequest Silian_req) {
		Result<IPage<SysTenant>> Silian_result = new Result<IPage<SysTenant>>();
        //---author:zhangyafei---date:20210916-----for: 租户管理添加日期范围查询---
        Date Silian_beginDate=null;
        Date Silian_endDate=null;
        if(oConvertUtils.isNotEmpty(Silian_sysTenant)) {
            Silian_beginDate=Silian_sysTenant.getBeginDate();
            Silian_endDate=Silian_sysTenant.getEndDate();
            Silian_sysTenant.setBeginDate(null);
            Silian_sysTenant.setEndDate(null);
        }
        //---author:zhangyafei---date:20210916-----for: 租户管理添加日期范围查询---
        QueryWrapper<SysTenant> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_sysTenant, Silian_req.getParameterMap());
        //---author:zhangyafei---date:20210916-----for: 租户管理添加日期范围查询---
        if(oConvertUtils.isNotEmpty(Silian_sysTenant)){
            Silian_queryWrapper.ge(oConvertUtils.isNotEmpty(Silian_beginDate),"begin_date",Silian_beginDate);
            Silian_queryWrapper.le(oConvertUtils.isNotEmpty(Silian_endDate),"end_date",Silian_endDate);
        }
        //---author:zhangyafei---date:20210916-----for: 租户管理添加日期范围查询---
		Page<SysTenant> Silian_page = new Page<SysTenant>(Silian_pageNo, Silian_pageSize);
		IPage<SysTenant> Silian_pageList = sysTenantService.page(Silian_page, Silian_queryWrapper);
		Silian_result.setSuccess(true);
		Silian_result.setResult(Silian_pageList);
		return Silian_result;
	}

    /**
     *   添加
     * @param
     * @return
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result<SysTenant> add(@RequestBody SysTenant Silian_sysTenant) {
        Result<SysTenant> Silian_result = new Result();
        if(sysTenantService.getById(Silian_sysTenant.getId())!=null){
            return Silian_result.error500("该编号已存在!");
        }
        try {
            sysTenantService.save(Silian_sysTenant);
            Silian_result.success("添加成功！");
        } catch (Exception Silian_e) {
            log.error(Silian_e.getMessage(), Silian_e);
            Silian_result.error500("操作失败");
        }
        return Silian_result;
    }

    /**
     *  编辑
     * @param
     * @return
     */
    @RequestMapping(value = "/edit", method ={RequestMethod.PUT, RequestMethod.POST})
    public Result<SysTenant> edit(@RequestBody SysTenant Silian_tenant) {
        Result<SysTenant> Silian_result = new Result();
        SysTenant Silian_sysTenant = sysTenantService.getById(Silian_tenant.getId());
        if(Silian_sysTenant==null) {
           return Silian_result.error500("未找到对应实体");
        }
        boolean Silian_ok = sysTenantService.updateById(Silian_tenant);
        if(Silian_ok) {
            Silian_result.success("修改成功!");
        }
        return Silian_result;
    }

    /**
     *   通过id删除
     * @param id
     * @return
     */
    @RequestMapping(value = "/delete", method ={RequestMethod.DELETE, RequestMethod.POST})
    public Result<?> delete(@RequestParam(name="id",required=true) String Silian_id) {
        sysTenantService.removeTenantById(Silian_id);
        return Result.ok("删除成功");
    }

    /**
     *  批量删除
     * @param ids
     * @return
     */
    @RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
    public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String Silian_ids) {
        Result<?> Silian_result = new Result<>();
        if(oConvertUtils.isEmpty(Silian_ids)) {
            Silian_result.error500("未选中租户！");
        }else {
            String[] Silian_ls = Silian_ids.split(",");
            // 过滤掉已被引用的租户
            List<Integer> Silian_idList = new ArrayList<>();
            for (String Silian_id : Silian_ls) {
                Long Silian_userCount = sysTenantService.countUserLinkTenant(Silian_id);
                if (Silian_userCount == 0) {
                    Silian_idList.add(Integer.parseInt(Silian_id));
                }
            }
            if (Silian_idList.size() > 0) {
                sysTenantService.removeByIds(Silian_idList);
                if (Silian_ls.length == Silian_idList.size()) {
                    Silian_result.success("删除成功！");
                } else {
                    Silian_result.success("部分删除成功！（被引用的租户无法删除）");
                }
            }else  {
                Silian_result.error500("选择的租户都已被引用，无法删除！");
            }
        }
        return Silian_result;
    }

    /**
     * 通过id查询
     * @param id
     * @return
     */
    @RequestMapping(value = "/queryById", method = RequestMethod.GET)
    public Result<SysTenant> queryById(@RequestParam(name="id",required=true) String Silian_id) {
        Result<SysTenant> Silian_result = new Result<SysTenant>();
        SysTenant Silian_sysTenant = sysTenantService.getById(Silian_id);
        if(Silian_sysTenant==null) {
            Silian_result.error500("未找到对应实体");
        }else {
            Silian_result.setResult(Silian_sysTenant);
            Silian_result.setSuccess(true);
        }
        return Silian_result;
    }


    /**
     * 查询有效的 租户数据
     * @return
     */
    @RequestMapping(value = "/queryList", method = RequestMethod.GET)
    public Result<List<SysTenant>> queryList(@RequestParam(name="ids",required=false) String Silian_ids) {
        Result<List<SysTenant>> Silian_result = new Result<List<SysTenant>>();
        LambdaQueryWrapper<SysTenant> Silian_query = new LambdaQueryWrapper<>();
        Silian_query.eq(SysTenant::getStatus, 1);
        if(oConvertUtils.isNotEmpty(Silian_ids)){
            Silian_query.in(SysTenant::getId, Silian_ids.split(","));
        }
        //此处查询忽略时间条件
        List<SysTenant> Silian_ls = sysTenantService.list(Silian_query);
        Silian_result.setSuccess(true);
        Silian_result.setResult(Silian_ls);
        return Silian_result;
    }
    /**
     *  查询当前用户的所有有效租户 【当前用于vue3版本】
     * @return
     */
    @RequestMapping(value = "/getCurrentUserTenant", method = RequestMethod.GET)
    public Result<Map<String,Object>> getCurrentUserTenant() {
        Result<Map<String,Object>> Silian_result = new Result<Map<String,Object>>();
        try {
            LoginUser Silian_sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            String Silian_tenantIds = Silian_sysUser.getRelTenantIds();
            Map<String,Object> Silian_map = new HashMap(5);
            if (oConvertUtils.isNotEmpty(Silian_tenantIds)) {
                List<Integer> Silian_tenantIdList = new ArrayList<>();
                for(String Silian_id: Silian_tenantIds.split(SymbolConstant.COMMA)){
                    Silian_tenantIdList.add(Integer.valueOf(Silian_id));
                }
                // 该方法仅查询有效的租户，如果返回0个就说明所有的租户均无效。
                List<SysTenant> Silian_tenantList = sysTenantService.queryEffectiveTenant(Silian_tenantIdList);
                Silian_map.put("list", Silian_tenantList);
            }
            Silian_result.setSuccess(true);
            Silian_result.setResult(Silian_map);
        }catch(Exception Silian_e) {
            log.error(Silian_e.getMessage(), Silian_e);
            Silian_result.error500("查询失败！");
        }
        return Silian_result;
    }
}
