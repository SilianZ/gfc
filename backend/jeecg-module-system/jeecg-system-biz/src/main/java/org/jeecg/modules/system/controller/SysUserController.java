package org.jeecg.modules.system.controller;


import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.PermissionData;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.*;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.model.DepartIdModel;
import org.jeecg.modules.system.model.SysUserSysDepartModel;
import org.jeecg.modules.system.service.*;
import org.jeecg.modules.system.vo.SysDepartUsersVO;
import org.jeecg.modules.system.vo.SysUserRoleVO;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
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
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @Author scott
 * @since 2018-12-20
 */
@Slf4j
@RestController
@RequestMapping("/sys/user")
public class SysUserController {

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private ISysDepartService sysDepartService;

    @Autowired
    private ISysUserRoleService sysUserRoleService;

    @Autowired
    private ISysUserDepartService sysUserDepartService;

    @Autowired
    private ISysUserRoleService userRoleService;

    @Autowired
    private ISysDepartRoleUserService departRoleUserService;

    @Autowired
    private ISysDepartRoleService departRoleService;

    @Autowired
    private RedisUtil redisUtil;

    @Value("${jeecg.path.upload}")
    private String upLoadPath;

    @Autowired
    private BaseCommonService baseCommonService;

    /**
     * 删除用户
     */
    @RequestMapping(value = "/init", method = RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class)
    public Result<?> init(@RequestBody JSONObject Silian_jsonObject) {
        try {
            //先清空现有账号数据
            sysUserService.deleteUser();
            //增加账号
            Integer Silian_number = Silian_jsonObject.getInteger("number");
            Double Silian_initAmount = Silian_jsonObject.getDouble("initAmount");
            List<SysDepart> Silian_countrys = sysDepartService.queryDeptByPid("6d35e179cd814e3299bd588ea7daed3f");
            for (SysDepart Silian_country : Silian_countrys) {
                for (int Silian_i = 1; Silian_i <= Silian_number; Silian_i++) {
                    JSONObject Silian_user = new JSONObject();
                    Silian_user.put("username", Silian_country.getDepartName().substring(0, 1) + "-" + String.format("%03d", Silian_i));
                    Silian_user.put("realname", Silian_country.getDepartName() + "-" + String.format("%03d", Silian_i) + "组");
                    Silian_user.put("workno", Silian_initAmount);
                    Silian_user.put("post", "TEM");
                    Silian_user.put("password", "1qa@WS3ed");
                    Silian_user.put("confirmpassword", "1qa@WS3ed");
                    Silian_user.put("selecteddeparts", Silian_country.getId());
                    Silian_user.put("activitiSync", "1");
                    Silian_user.put("userIdentity", "1");
                    Silian_user.put("selectedroles", "1707659087407153153");
                    this.addUser(Silian_user);
                }
            }
            //初始化团队账户
            sysUserService.initBalance();
            //初始化银行配置
            sysUserService.initBankConf();
            //初始化团队固定资产
            sysUserService.initTeamResource();
            //初始化财年
            sysUserService.initFiscalYear();
            //删除其它数据
            sysUserService.deleteOtherData();
        } catch (Exception Silian_e) {
            Silian_e.printStackTrace();
            //回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.error("初始化失败:" + Silian_e.getMessage());
        }
        return Result.OK("初始化成功！");
    }

    public void addUser(JSONObject Silian_jsonObject) {
        String Silian_selectedRoles = Silian_jsonObject.getString("selectedroles");
        String Silian_selectedDeparts = Silian_jsonObject.getString("selecteddeparts");
        try {
            SysUser Silian_user = JSON.parseObject(Silian_jsonObject.toJSONString(), SysUser.class);
            Silian_user.setCreateTime(new Date());//设置创建时间
            String Silian_salt = oConvertUtils.randomGen(8);
            Silian_user.setSalt(Silian_salt);
            String Silian_passwordEncode = PasswordUtil.encrypt(Silian_user.getUsername(), Silian_user.getPassword(), Silian_salt);
            Silian_user.setPassword(Silian_passwordEncode);
            Silian_user.setStatus(1);
            Silian_user.setDelFlag(CommonConstant.DEL_FLAG_0);
            //用户表字段org_code不能在这里设置他的值
            Silian_user.setOrgCode(null);
            // 保存用户走一个service 保证事务
            sysUserService.saveUser(Silian_user, Silian_selectedRoles, Silian_selectedDeparts);
            baseCommonService.addLog("添加用户，username： " + Silian_user.getUsername(), CommonConstant.LOG_TYPE_2, 2);
        } catch (Exception Silian_e) {
            throw Silian_e;
        }
    }

    /**
     * 获取用户列表数据
     *
     * @param user
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @PermissionData(pageComponent = "system/UserList")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Result<IPage<SysUser>> queryPageList(SysUser Silian_user, @RequestParam(name = "pageNo", defaultValue = "1") Integer Silian_pageNo,
                                                @RequestParam(name = "pageSize", defaultValue = "10") Integer Silian_pageSize, HttpServletRequest Silian_req) {
        Result<IPage<SysUser>> Silian_result = new Result<IPage<SysUser>>();
        QueryWrapper<SysUser> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_user, Silian_req.getParameterMap());
        Silian_queryWrapper.in("post", "TEM", "ZXT");
        //update-begin-Author:wangshuai--Date:20211119--for:【vue3】通过部门id查询用户，通过code查询id
        //部门ID
        String Silian_departId = Silian_req.getParameter("departId");
        if (oConvertUtils.isNotEmpty(Silian_departId)) {
            LambdaQueryWrapper<SysUserDepart> Silian_query = new LambdaQueryWrapper<>();
            Silian_query.eq(SysUserDepart::getDepId, Silian_departId);
            List<SysUserDepart> Silian_list = sysUserDepartService.list(Silian_query);
            List<String> Silian_userIds = Silian_list.stream().map(SysUserDepart::getUserId).collect(Collectors.toList());
            //update-begin---author:wangshuai ---date:20220322  for：[issues/I4XTYB]查询用户时，当部门id 下没有分配用户时接口报错------------
            if (oConvertUtils.listIsNotEmpty(Silian_userIds)) {
                Silian_queryWrapper.in("id", Silian_userIds);
            } else {
                return Result.OK();
            }
            //update-end---author:wangshuai ---date:20220322  for：[issues/I4XTYB]查询用户时，当部门id 下没有分配用户时接口报错------------
        }
        //用户ID
        String Silian_code = Silian_req.getParameter("code");
        if (oConvertUtils.isNotEmpty(Silian_code)) {
            Silian_queryWrapper.in("id", Arrays.asList(Silian_code.split(",")));
            Silian_pageSize = Silian_code.split(",").length;
        }
        //update-end-Author:wangshuai--Date:20211119--for:【vue3】通过部门id查询用户，通过code查询id

        //update-begin-author:taoyan--date:20220104--for: JTC-372 【用户冻结问题】 online授权、用户组件，选择用户都能看到被冻结的用户
        String Silian_status = Silian_req.getParameter("status");
        if (oConvertUtils.isNotEmpty(Silian_status)) {
            Silian_queryWrapper.eq("status", Integer.parseInt(Silian_status));
        }
        //update-end-author:taoyan--date:20220104--for: JTC-372 【用户冻结问题】 online授权、用户组件，选择用户都能看到被冻结的用户

        //TODO 外部模拟登陆临时账号，列表不显示
        Silian_queryWrapper.ne("username", "_reserve_user_external");
        Page<SysUser> Silian_page = new Page<SysUser>(Silian_pageNo, Silian_pageSize);
        IPage<SysUser> Silian_pageList = sysUserService.page(Silian_page, Silian_queryWrapper);

        //批量查询用户的所属部门
        //step.1 先拿到全部的 useids
        //step.2 通过 useids，一次性查询用户的所属部门名字
        List<String> Silian_userIds = Silian_pageList.getRecords().stream().map(SysUser::getId).collect(Collectors.toList());
        if (Silian_userIds != null && Silian_userIds.size() > 0) {
            Map<String, String> Silian_useDepNames = sysUserService.getDepNamesByUserIds(Silian_userIds);
            Silian_pageList.getRecords().forEach(Silian_item -> {
                Silian_item.setOrgCodeTxt(Silian_useDepNames.get(Silian_item.getId()));
            });
        }
        Silian_result.setSuccess(true);
        Silian_result.setResult(Silian_pageList);
        log.info(Silian_pageList.toString());
        return Silian_result;
    }

    //@RequiresRoles({"admin"})
    //Permissions("system:user:add")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result<SysUser> add(@RequestBody JSONObject Silian_jsonObject) {
        Result<SysUser> Silian_result = new Result<SysUser>();
        String Silian_selectedRoles = Silian_jsonObject.getString("selectedroles");
        String Silian_selectedDeparts = Silian_jsonObject.getString("selecteddeparts");
        try {
            SysUser Silian_user = JSON.parseObject(Silian_jsonObject.toJSONString(), SysUser.class);
            Silian_user.setCreateTime(new Date());//设置创建时间
            String Silian_salt = oConvertUtils.randomGen(8);
            Silian_user.setSalt(Silian_salt);
            String Silian_passwordEncode = PasswordUtil.encrypt(Silian_user.getUsername(), Silian_user.getPassword(), Silian_salt);
            Silian_user.setPassword(Silian_passwordEncode);
            Silian_user.setStatus(1);
            Silian_user.setDelFlag(CommonConstant.DEL_FLAG_0);
            //用户表字段org_code不能在这里设置他的值
            Silian_user.setOrgCode(null);
            // 保存用户走一个service 保证事务
            sysUserService.saveUser(Silian_user, Silian_selectedRoles, Silian_selectedDeparts);
            baseCommonService.addLog("添加用户，username： " + Silian_user.getUsername(), CommonConstant.LOG_TYPE_2, 2);
            Silian_result.success("添加成功！");
        } catch (Exception Silian_e) {
            log.error(Silian_e.getMessage(), Silian_e);
            Silian_result.error500("操作失败");
        }
        return Silian_result;
    }

    //@RequiresRoles({"admin"})
    //Permissions("system:user:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<SysUser> edit(@RequestBody JSONObject Silian_jsonObject) {
        Result<SysUser> Silian_result = new Result<SysUser>();
        try {
            SysUser Silian_sysUser = sysUserService.getById(Silian_jsonObject.getString("id"));
            baseCommonService.addLog("编辑用户，username： " + Silian_sysUser.getUsername(), CommonConstant.LOG_TYPE_2, 2);
            if (Silian_sysUser == null) {
                Silian_result.error500("未找到对应实体");
            } else {
                SysUser Silian_user = JSON.parseObject(Silian_jsonObject.toJSONString(), SysUser.class);
                Silian_user.setUpdateTime(new Date());
                //String passwordEncode = PasswordUtil.encrypt(user.getUsername(), user.getPassword(), sysUser.getSalt());
                Silian_user.setPassword(Silian_sysUser.getPassword());
                String Silian_roles = Silian_jsonObject.getString("selectedroles");
                String Silian_departs = Silian_jsonObject.getString("selecteddeparts");
                if (oConvertUtils.isEmpty(Silian_departs)) {
                    //vue3.0前端只传递了departIds
                    Silian_departs = Silian_user.getDepartIds();
                }
                //用户表字段org_code不能在这里设置他的值
                Silian_user.setOrgCode(null);
                // 修改用户走一个service 保证事务
                sysUserService.editUser(Silian_user, Silian_roles, Silian_departs);
                Silian_result.success("修改成功!");
            }
        } catch (Exception Silian_e) {
            log.error(Silian_e.getMessage(), Silian_e);
            Silian_result.error500("操作失败");
        }
        return Silian_result;
    }

    /**
     * 删除用户
     */
    //@RequiresRoles({"admin"})
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result<?> delete(@RequestParam(name = "id", required = true) String Silian_id) {
        baseCommonService.addLog("删除用户，id： " + Silian_id, CommonConstant.LOG_TYPE_2, 3);
        this.sysUserService.deleteUser(Silian_id);
        return Result.ok("删除用户成功");
    }

    /**
     * 批量删除用户
     */
    //@RequiresRoles({"admin"})
    @RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String Silian_ids) {
        baseCommonService.addLog("批量删除用户， ids： " + Silian_ids, CommonConstant.LOG_TYPE_2, 3);
        this.sysUserService.deleteBatchUsers(Silian_ids);
        return Result.ok("批量删除用户成功");
    }

    /**
     * 冻结&解冻用户
     *
     * @param jsonObject
     * @return
     */
    //@RequiresRoles({"admin"})
    @RequestMapping(value = "/frozenBatch", method = RequestMethod.PUT)
    public Result<SysUser> frozenBatch(@RequestBody JSONObject Silian_jsonObject) {
        Result<SysUser> Silian_result = new Result<SysUser>();
        try {
            String Silian_ids = Silian_jsonObject.getString("ids");
            String Silian_status = Silian_jsonObject.getString("status");
            String[] Silian_arr = Silian_ids.split(",");
            for (String Silian_id : Silian_arr) {
                if (oConvertUtils.isNotEmpty(Silian_id)) {
                    this.sysUserService.update(new SysUser().setStatus(Integer.parseInt(Silian_status)),
                            new UpdateWrapper<SysUser>().lambda().eq(SysUser::getId, Silian_id));
                }
            }
        } catch (Exception Silian_e) {
            log.error(Silian_e.getMessage(), Silian_e);
            Silian_result.error500("操作失败" + Silian_e.getMessage());
        }
        Silian_result.success("操作成功!");
        return Silian_result;

    }

    @RequestMapping(value = "/queryById", method = RequestMethod.GET)
    public Result<SysUser> queryById(@RequestParam(name = "id", required = true) String Silian_id) {
        Result<SysUser> Silian_result = new Result<SysUser>();
        SysUser Silian_sysUser = sysUserService.getById(Silian_id);
        if (Silian_sysUser == null) {
            Silian_result.error500("未找到对应实体");
        } else {
            Silian_result.setResult(Silian_sysUser);
            Silian_result.setSuccess(true);
        }
        return Silian_result;
    }

    @RequestMapping(value = "/queryUserRole", method = RequestMethod.GET)
    public Result<List<String>> queryUserRole(@RequestParam(name = "userid", required = true) String Silian_userid) {
        Result<List<String>> Silian_result = new Result<>();
        List<String> Silian_list = new ArrayList<String>();
        List<SysUserRole> Silian_userRole = sysUserRoleService.list(new QueryWrapper<SysUserRole>().lambda().eq(SysUserRole::getUserId, Silian_userid));
        if (Silian_userRole == null || Silian_userRole.size() <= 0) {
            Silian_result.error500("未找到用户相关角色信息");
        } else {
            for (SysUserRole Silian_sysUserRole : Silian_userRole) {
                Silian_list.add(Silian_sysUserRole.getRoleId());
            }
            Silian_result.setSuccess(true);
            Silian_result.setResult(Silian_list);
        }
        return Silian_result;
    }


    /**
     * 校验用户账号是否唯一<br>
     * 可以校验其他 需要检验什么就传什么。。。
     *
     * @param sysUser
     * @return
     */
    @RequestMapping(value = "/checkOnlyUser", method = RequestMethod.GET)
    public Result<Boolean> checkOnlyUser(SysUser Silian_sysUser) {
        Result<Boolean> Silian_result = new Result<>();
        //如果此参数为false则程序发生异常
        Silian_result.setResult(true);
        try {
            //通过传入信息查询新的用户信息
            Silian_sysUser.setPassword(null);
            SysUser Silian_user = sysUserService.getOne(new QueryWrapper<SysUser>(Silian_sysUser));
            if (Silian_user != null) {
                Silian_result.setSuccess(false);
                Silian_result.setMessage("用户账号已存在");
                return Silian_result;
            }

        } catch (Exception Silian_e) {
            Silian_result.setSuccess(false);
            Silian_result.setMessage(Silian_e.getMessage());
            return Silian_result;
        }
        Silian_result.setSuccess(true);
        return Silian_result;
    }

    /**
     * 修改密码
     */
    //@RequiresRoles({"admin"})
    @RequestMapping(value = "/changePassword", method = RequestMethod.PUT)
    public Result<?> changePassword(@RequestBody SysUser Silian_sysUser) {
        SysUser Silian_u = this.sysUserService.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, Silian_sysUser.getUsername()));
        if (Silian_u == null) {
            return Result.error("用户不存在！");
        }
        Silian_sysUser.setId(Silian_u.getId());
        //update-begin---author:wangshuai ---date:20220316  for：[VUEN-234]修改密码添加敏感日志------------
        LoginUser Silian_loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        baseCommonService.addLog("修改用户 " + Silian_sysUser.getUsername() + " 的密码，操作人： " + Silian_loginUser.getUsername(), CommonConstant.LOG_TYPE_2, 2);
        //update-end---author:wangshuai ---date:20220316  for：[VUEN-234]修改密码添加敏感日志------------
        return sysUserService.changePassword(Silian_sysUser);
    }

    /**
     * 查询指定用户和部门关联的数据
     *
     * @param userId
     * @return
     */
    @RequestMapping(value = "/userDepartList", method = RequestMethod.GET)
    public Result<List<DepartIdModel>> getUserDepartsList(@RequestParam(name = "userId", required = true) String Silian_userId) {
        Result<List<DepartIdModel>> Silian_result = new Result<>();
        try {
            List<DepartIdModel> Silian_depIdModelList = this.sysUserDepartService.queryDepartIdsOfUser(Silian_userId);
            if (Silian_depIdModelList != null && Silian_depIdModelList.size() > 0) {
                Silian_result.setSuccess(true);
                Silian_result.setMessage("查找成功");
                Silian_result.setResult(Silian_depIdModelList);
            } else {
                Silian_result.setSuccess(false);
                Silian_result.setMessage("查找失败");
            }
            return Silian_result;
        } catch (Exception Silian_e) {
            log.error(Silian_e.getMessage(), Silian_e);
            Silian_result.setSuccess(false);
            Silian_result.setMessage("查找过程中出现了异常: " + Silian_e.getMessage());
            return Silian_result;
        }

    }

    /**
     * 生成在添加用户情况下没有主键的问题,返回给前端,根据该id绑定部门数据
     *
     * @return
     */
    @RequestMapping(value = "/generateUserId", method = RequestMethod.GET)
    public Result<String> generateUserId() {
        Result<String> Silian_result = new Result<>();
        System.out.println("我执行了,生成用户ID==============================");
        String Silian_userId = UUID.randomUUID().toString().replace("-", "");
        Silian_result.setSuccess(true);
        Silian_result.setResult(Silian_userId);
        return Silian_result;
    }

    /**
     * 根据部门id查询用户信息
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/queryUserByDepId", method = RequestMethod.GET)
    public Result<List<SysUser>> queryUserByDepId(@RequestParam(name = "id", required = true) String Silian_id, @RequestParam(name = "realname", required = false) String Silian_realname) {
        Result<List<SysUser>> Silian_result = new Result<>();
        //List<SysUser> userList = sysUserDepartService.queryUserByDepId(id);
        SysDepart Silian_sysDepart = sysDepartService.getById(Silian_id);
        List<SysUser> Silian_userList = sysUserDepartService.queryUserByDepCode(Silian_sysDepart.getOrgCode(), Silian_realname);

        //批量查询用户的所属部门
        //step.1 先拿到全部的 useids
        //step.2 通过 useids，一次性查询用户的所属部门名字
        List<String> Silian_userIds = Silian_userList.stream().map(SysUser::getId).collect(Collectors.toList());
        if (Silian_userIds != null && Silian_userIds.size() > 0) {
            Map<String, String> Silian_useDepNames = sysUserService.getDepNamesByUserIds(Silian_userIds);
            Silian_userList.forEach(Silian_item -> {
                //TODO 临时借用这个字段用于页面展示
                Silian_item.setOrgCodeTxt(Silian_useDepNames.get(Silian_item.getId()));
            });
        }

        try {
            Silian_result.setSuccess(true);
            Silian_result.setResult(Silian_userList);
            return Silian_result;
        } catch (Exception Silian_e) {
            log.error(Silian_e.getMessage(), Silian_e);
            Silian_result.setSuccess(false);
            return Silian_result;
        }
    }

    /**
     * 用户选择组件 专用  根据用户账号或部门分页查询
     *
     * @param departId
     * @param username
     * @return
     */
    @RequestMapping(value = "/queryUserComponentData", method = RequestMethod.GET)
    public Result<IPage<SysUser>> queryUserComponentData(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer Silian_pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer Silian_pageSize,
            @RequestParam(name = "departId", required = false) String Silian_departId,
            @RequestParam(name = "realname", required = false) String Silian_realname,
            @RequestParam(name = "username", required = false) String Silian_username,
            @RequestParam(name = "id", required = false) String Silian_id) {
        //update-begin-author:taoyan date:2022-7-14 for: VUEN-1702【禁止问题】sql注入漏洞
        String[] Silian_arr = new String[]{Silian_departId, Silian_realname, Silian_username, Silian_id};
        SqlInjectionUtil.filterContent(Silian_arr, SymbolConstant.SINGLE_QUOTATION_MARK);
        //update-end-author:taoyan date:2022-7-14 for: VUEN-1702【禁止问题】sql注入漏洞
        IPage<SysUser> Silian_pageList = sysUserDepartService.queryDepartUserPageList(Silian_departId, Silian_username, Silian_realname, Silian_pageSize, Silian_pageNo, Silian_id);
        return Result.OK(Silian_pageList);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param sysUser
     */
    //@RequiresRoles({"admin"})
    //@RequiresPermissions("system:user:export")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(SysUser Silian_sysUser, HttpServletRequest Silian_request) {
        // Step.1 组装查询条件
        QueryWrapper<SysUser> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_sysUser, Silian_request.getParameterMap());
        //Step.2 AutoPoi 导出Excel
        ModelAndView Silian_mv = new ModelAndView(new JeecgEntityExcelView());
        //update-begin--Author:kangxiaolin  Date:20180825 for：[03]用户导出，如果选择数据则只导出相关数据--------------------
        String Silian_selections = Silian_request.getParameter("selections");
        if (!oConvertUtils.isEmpty(Silian_selections)) {
            Silian_queryWrapper.in("id", Silian_selections.split(","));
        }
        //update-end--Author:kangxiaolin  Date:20180825 for：[03]用户导出，如果选择数据则只导出相关数据----------------------
        List<SysUser> Silian_pageList = sysUserService.list(Silian_queryWrapper);

        //导出文件名称
        Silian_mv.addObject(NormalExcelConstants.FILE_NAME, "用户列表");
        Silian_mv.addObject(NormalExcelConstants.CLASS, SysUser.class);
        LoginUser Silian_user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        ExportParams Silian_exportParams = new ExportParams("用户列表数据", "导出人:" + Silian_user.getRealname(), "导出信息");
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
    //@RequiresRoles({"admin"})
    //Permissions("system:user:import")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest Silian_request, HttpServletResponse Silian_response) throws IOException {
        MultipartHttpServletRequest Silian_multipartRequest = (MultipartHttpServletRequest) Silian_request;
        Map<String, MultipartFile> Silian_fileMap = Silian_multipartRequest.getFileMap();
        // 错误信息
        List<String> Silian_errorMessage = new ArrayList<>();
        int Silian_successLines = 0, Silian_errorLines = 0;
        for (Map.Entry<String, MultipartFile> Silian_entity : Silian_fileMap.entrySet()) {
            MultipartFile Silian_file = Silian_entity.getValue();// 获取上传文件对象
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try {
                List<SysUser> Silian_listSysUsers = ExcelImportUtil.importExcel(Silian_file.getInputStream(), SysUser.class, params);
                for (int Silian_i = 0; Silian_i < Silian_listSysUsers.size(); Silian_i++) {
                    SysUser Silian_sysUserExcel = Silian_listSysUsers.get(Silian_i);
                    if (StringUtils.isBlank(Silian_sysUserExcel.getPassword())) {
                        // 密码默认为 “123456”
                        Silian_sysUserExcel.setPassword("123456");
                    }
                    // 密码加密加盐
                    String Silian_salt = oConvertUtils.randomGen(8);
                    Silian_sysUserExcel.setSalt(Silian_salt);
                    String Silian_passwordEncode = PasswordUtil.encrypt(Silian_sysUserExcel.getUsername(), Silian_sysUserExcel.getPassword(), Silian_salt);
                    Silian_sysUserExcel.setPassword(Silian_passwordEncode);
                    try {
                        sysUserService.save(Silian_sysUserExcel);
                        Silian_successLines++;
                    } catch (Exception Silian_e) {
                        Silian_errorLines++;
                        String Silian_message = Silian_e.getMessage().toLowerCase();
                        int Silian_lineNumber = Silian_i + 1;
                        // 通过索引名判断出错信息
                        if (Silian_message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_USER_USERNAME)) {
                            Silian_errorMessage.add("第 " + Silian_lineNumber + " 行：用户名已经存在，忽略导入。");
                        } else if (Silian_message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_USER_WORK_NO)) {
                            Silian_errorMessage.add("第 " + Silian_lineNumber + " 行：工号已经存在，忽略导入。");
                        } else if (Silian_message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_USER_PHONE)) {
                            Silian_errorMessage.add("第 " + Silian_lineNumber + " 行：手机号已经存在，忽略导入。");
                        } else if (Silian_message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_USER_EMAIL)) {
                            Silian_errorMessage.add("第 " + Silian_lineNumber + " 行：电子邮件已经存在，忽略导入。");
                        } else if (Silian_message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_USER)) {
                            Silian_errorMessage.add("第 " + Silian_lineNumber + " 行：违反表唯一性约束。");
                        } else {
                            Silian_errorMessage.add("第 " + Silian_lineNumber + " 行：未知错误，忽略导入");
                            log.error(Silian_e.getMessage(), Silian_e);
                        }
                    }
                    // 批量将部门和用户信息建立关联关系
                    String Silian_departIds = Silian_sysUserExcel.getDepartIds();
                    if (StringUtils.isNotBlank(Silian_departIds)) {
                        String Silian_userId = Silian_sysUserExcel.getId();
                        String[] Silian_departIdArray = Silian_departIds.split(",");
                        List<SysUserDepart> Silian_userDepartList = new ArrayList<>(Silian_departIdArray.length);
                        for (String Silian_departId : Silian_departIdArray) {
                            Silian_userDepartList.add(new SysUserDepart(Silian_userId, Silian_departId));
                        }
                        sysUserDepartService.saveBatch(Silian_userDepartList);
                    }

                }
            } catch (Exception Silian_e) {
                Silian_errorMessage.add("发生异常：" + Silian_e.getMessage());
                log.error(Silian_e.getMessage(), Silian_e);
            } finally {
                try {
                    Silian_file.getInputStream().close();
                } catch (IOException Silian_e) {
                    log.error(Silian_e.getMessage(), Silian_e);
                }
            }
        }
        return ImportExcelUtil.imporReturnRes(Silian_errorLines, Silian_successLines, Silian_errorMessage);
    }

    /**
     * @param userIds
     * @return
     * @功能：根据id 批量查询
     */
    @RequestMapping(value = "/queryByIds", method = RequestMethod.GET)
    public Result<Collection<SysUser>> queryByIds(@RequestParam String Silian_userIds) {
        Result<Collection<SysUser>> Silian_result = new Result<>();
        String[] Silian_userId = Silian_userIds.split(",");
        Collection<String> Silian_idList = Arrays.asList(Silian_userId);
        Collection<SysUser> Silian_userRole = sysUserService.listByIds(Silian_idList);
        Silian_result.setSuccess(true);
        Silian_result.setResult(Silian_userRole);
        return Silian_result;
    }

    /**
     * 首页用户重置密码
     */
    //@RequiresRoles({"admin"})
    @RequestMapping(value = "/updatePassword", method = RequestMethod.PUT)
    public Result<?> updatePassword(@RequestBody JSONObject Silian_json) {
        String Silian_username = Silian_json.getString("username");
        String Silian_oldpassword = Silian_json.getString("oldpassword");
        String Silian_password = Silian_json.getString("password");
        String Silian_confirmpassword = Silian_json.getString("confirmpassword");
        LoginUser Silian_sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (!Silian_sysUser.getUsername().equals(Silian_username)) {
            return Result.error("只允许修改自己的密码！");
        }
        SysUser Silian_user = this.sysUserService.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, Silian_username));
        if (Silian_user == null) {
            return Result.error("用户不存在！");
        }
        //update-begin---author:wangshuai ---date:20220316  for：[VUEN-234]修改密码添加敏感日志------------
        LoginUser Silian_loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        baseCommonService.addLog("修改密码，username： " + Silian_loginUser.getUsername(), CommonConstant.LOG_TYPE_2, 2);
        //update-end---author:wangshuai ---date:20220316  for：[VUEN-234]修改密码添加敏感日志------------
        return sysUserService.resetPassword(Silian_username, Silian_oldpassword, Silian_password, Silian_confirmpassword);
    }

    @RequestMapping(value = "/userRoleList", method = RequestMethod.GET)
    public Result<IPage<SysUser>> userRoleList(@RequestParam(name = "pageNo", defaultValue = "1") Integer Silian_pageNo,
                                               @RequestParam(name = "pageSize", defaultValue = "10") Integer Silian_pageSize, HttpServletRequest Silian_req) {
        Result<IPage<SysUser>> Silian_result = new Result<IPage<SysUser>>();
        Page<SysUser> Silian_page = new Page<SysUser>(Silian_pageNo, Silian_pageSize);
        String Silian_roleId = Silian_req.getParameter("roleId");
        String Silian_username = Silian_req.getParameter("username");
        IPage<SysUser> Silian_pageList = sysUserService.getUserByRoleId(Silian_page, Silian_roleId, Silian_username);
        Silian_result.setSuccess(true);
        Silian_result.setResult(Silian_pageList);
        return Silian_result;
    }

    /**
     * 给指定角色添加用户
     *
     * @param
     * @return
     */
    //@RequiresRoles({"admin"})
    @RequestMapping(value = "/addSysUserRole", method = RequestMethod.POST)
    public Result<String> addSysUserRole(@RequestBody SysUserRoleVO Silian_sysUserRoleVO) {
        Result<String> Silian_result = new Result<String>();
        try {
            String Silian_sysRoleId = Silian_sysUserRoleVO.getRoleId();
            for (String Silian_sysUserId : Silian_sysUserRoleVO.getUserIdList()) {
                SysUserRole Silian_sysUserRole = new SysUserRole(Silian_sysUserId, Silian_sysRoleId);
                QueryWrapper<SysUserRole> Silian_queryWrapper = new QueryWrapper<SysUserRole>();
                Silian_queryWrapper.eq("role_id", Silian_sysRoleId).eq("user_id", Silian_sysUserId);
                SysUserRole Silian_one = sysUserRoleService.getOne(Silian_queryWrapper);
                if (Silian_one == null) {
                    sysUserRoleService.save(Silian_sysUserRole);
                }

            }
            Silian_result.setMessage("添加成功!");
            Silian_result.setSuccess(true);
            return Silian_result;
        } catch (Exception Silian_e) {
            log.error(Silian_e.getMessage(), Silian_e);
            Silian_result.setSuccess(false);
            Silian_result.setMessage("出错了: " + Silian_e.getMessage());
            return Silian_result;
        }
    }

    /**
     * 删除指定角色的用户关系
     *
     * @param
     * @return
     */
    //@RequiresRoles({"admin"})
    @RequestMapping(value = "/deleteUserRole", method = RequestMethod.DELETE)
    public Result<SysUserRole> deleteUserRole(@RequestParam(name = "roleId") String Silian_roleId,
                                              @RequestParam(name = "userId", required = true) String Silian_userId
    ) {
        Result<SysUserRole> Silian_result = new Result<SysUserRole>();
        try {
            QueryWrapper<SysUserRole> Silian_queryWrapper = new QueryWrapper<SysUserRole>();
            Silian_queryWrapper.eq("role_id", Silian_roleId).eq("user_id", Silian_userId);
            sysUserRoleService.remove(Silian_queryWrapper);
            Silian_result.success("删除成功!");
        } catch (Exception Silian_e) {
            log.error(Silian_e.getMessage(), Silian_e);
            Silian_result.error500("删除失败！");
        }
        return Silian_result;
    }

    /**
     * 批量删除指定角色的用户关系
     *
     * @param
     * @return
     */
    //@RequiresRoles({"admin"})
    @RequestMapping(value = "/deleteUserRoleBatch", method = RequestMethod.DELETE)
    public Result<SysUserRole> deleteUserRoleBatch(
            @RequestParam(name = "roleId") String Silian_roleId,
            @RequestParam(name = "userIds", required = true) String Silian_userIds) {
        Result<SysUserRole> Silian_result = new Result<SysUserRole>();
        try {
            QueryWrapper<SysUserRole> Silian_queryWrapper = new QueryWrapper<SysUserRole>();
            Silian_queryWrapper.eq("role_id", Silian_roleId).in("user_id", Arrays.asList(Silian_userIds.split(",")));
            sysUserRoleService.remove(Silian_queryWrapper);
            Silian_result.success("删除成功!");
        } catch (Exception Silian_e) {
            log.error(Silian_e.getMessage(), Silian_e);
            Silian_result.error500("删除失败！");
        }
        return Silian_result;
    }

    /**
     * 部门用户列表
     */
    @RequestMapping(value = "/departUserList", method = RequestMethod.GET)
    public Result<IPage<SysUser>> departUserList(@RequestParam(name = "pageNo", defaultValue = "1") Integer Silian_pageNo,
                                                 @RequestParam(name = "pageSize", defaultValue = "10") Integer Silian_pageSize, HttpServletRequest Silian_req) {
        Result<IPage<SysUser>> Silian_result = new Result<IPage<SysUser>>();
        Page<SysUser> Silian_page = new Page<SysUser>(Silian_pageNo, Silian_pageSize);
        String Silian_depId = Silian_req.getParameter("depId");
        String Silian_username = Silian_req.getParameter("username");
        //根据部门ID查询,当前和下级所有的部门IDS
        List<String> Silian_subDepids = new ArrayList<>();
        //部门id为空时，查询我的部门下所有用户
        if (oConvertUtils.isEmpty(Silian_depId)) {
            LoginUser Silian_user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            int Silian_userIdentity = Silian_user.getUserIdentity() != null ? Silian_user.getUserIdentity() : CommonConstant.USER_IDENTITY_1;
            if (oConvertUtils.isNotEmpty(Silian_userIdentity) && Silian_userIdentity == CommonConstant.USER_IDENTITY_2) {
                Silian_subDepids = sysDepartService.getMySubDepIdsByDepId(Silian_user.getDepartIds());
            }
        } else {
            Silian_subDepids = sysDepartService.getSubDepIdsByDepId(Silian_depId);
        }
        if (Silian_subDepids != null && Silian_subDepids.size() > 0) {
            IPage<SysUser> Silian_pageList = sysUserService.getUserByDepIds(Silian_page, Silian_subDepids, Silian_username);
            //批量查询用户的所属部门
            //step.1 先拿到全部的 useids
            //step.2 通过 useids，一次性查询用户的所属部门名字
            List<String> Silian_userIds = Silian_pageList.getRecords().stream().map(SysUser::getId).collect(Collectors.toList());
            if (Silian_userIds != null && Silian_userIds.size() > 0) {
                Map<String, String> Silian_useDepNames = sysUserService.getDepNamesByUserIds(Silian_userIds);
                Silian_pageList.getRecords().forEach(Silian_item -> {
                    //批量查询用户的所属部门
                    Silian_item.setOrgCode(Silian_useDepNames.get(Silian_item.getId()));
                });
            }
            Silian_result.setSuccess(true);
            Silian_result.setResult(Silian_pageList);
        } else {
            Silian_result.setSuccess(true);
            Silian_result.setResult(null);
        }
        return Silian_result;
    }


    /**
     * 根据 orgCode 查询用户，包括子部门下的用户
     * 若某个用户包含多个部门，则会显示多条记录，可自行处理成单条记录
     */
    @GetMapping("/queryByOrgCode")
    public Result<?> queryByDepartId(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer Silian_pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer Silian_pageSize,
            @RequestParam(name = "orgCode") String Silian_orgCode,
            SysUser Silian_userParams
    ) {
        IPage<SysUserSysDepartModel> Silian_pageList = sysUserService.queryUserByOrgCode(Silian_orgCode, Silian_userParams, new Page(Silian_pageNo, Silian_pageSize));
        return Result.ok(Silian_pageList);
    }

    /**
     * 根据 orgCode 查询用户，包括子部门下的用户
     * 针对通讯录模块做的接口，将多个部门的用户合并成一条记录，并转成对前端友好的格式
     */
    @GetMapping("/queryByOrgCodeForAddressList")
    public Result<?> queryByOrgCodeForAddressList(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer Silian_pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer Silian_pageSize,
            @RequestParam(name = "orgCode", required = false) String Silian_orgCode,
            SysUser Silian_userParams
    ) {
        IPage Silian_page = new Page(Silian_pageNo, Silian_pageSize);
        IPage<SysUserSysDepartModel> Silian_pageList = sysUserService.queryUserByOrgCode(Silian_orgCode, Silian_userParams, Silian_page);
        List<SysUserSysDepartModel> Silian_list = Silian_pageList.getRecords();

        // 记录所有出现过的 user, key = userId
        Map<String, JSONObject> Silian_hasUser = new HashMap<>(Silian_list.size());

        JSONArray Silian_resultJson = new JSONArray(Silian_list.size());

        for (SysUserSysDepartModel Silian_item : Silian_list) {
            String Silian_userId = Silian_item.getId();
            // userId
            JSONObject Silian_getModel = Silian_hasUser.get(Silian_userId);
            // 之前已存在过该用户，直接合并数据
            if (Silian_getModel != null) {
                String Silian_departName = Silian_getModel.get("departName").toString();
                Silian_getModel.put("departName", (Silian_departName + " | " + Silian_item.getDepartName()));
            } else {
                // 将用户对象转换为json格式，并将部门信息合并到 json 中
                JSONObject Silian_json = JSON.parseObject(JSON.toJSONString(Silian_item));
                Silian_json.remove("id");
                Silian_json.put("userId", Silian_userId);
                Silian_json.put("departId", Silian_item.getDepartId());
                Silian_json.put("departName", Silian_item.getDepartName());
//                json.put("avatar", item.getSysUser().getAvatar());
                Silian_resultJson.add(Silian_json);
                Silian_hasUser.put(Silian_userId, Silian_json);
            }
        }

        IPage<JSONObject> Silian_result = new Page<>(Silian_pageNo, Silian_pageSize, Silian_pageList.getTotal());
        Silian_result.setRecords(Silian_resultJson.toJavaList(JSONObject.class));
        return Result.ok(Silian_result);
    }

    /**
     * 给指定部门添加对应的用户
     */
    //@RequiresRoles({"admin"})
    @RequestMapping(value = "/editSysDepartWithUser", method = RequestMethod.POST)
    public Result<String> editSysDepartWithUser(@RequestBody SysDepartUsersVO Silian_sysDepartUsersVO) {
        Result<String> Silian_result = new Result<String>();
        try {
            String Silian_sysDepId = Silian_sysDepartUsersVO.getDepId();
            for (String Silian_sysUserId : Silian_sysDepartUsersVO.getUserIdList()) {
                SysUserDepart Silian_sysUserDepart = new SysUserDepart(null, Silian_sysUserId, Silian_sysDepId);
                QueryWrapper<SysUserDepart> Silian_queryWrapper = new QueryWrapper<SysUserDepart>();
                Silian_queryWrapper.eq("dep_id", Silian_sysDepId).eq("user_id", Silian_sysUserId);
                SysUserDepart Silian_one = sysUserDepartService.getOne(Silian_queryWrapper);
                if (Silian_one == null) {
                    sysUserDepartService.save(Silian_sysUserDepart);
                }
            }
            Silian_result.setMessage("添加成功!");
            Silian_result.setSuccess(true);
            return Silian_result;
        } catch (Exception Silian_e) {
            log.error(Silian_e.getMessage(), Silian_e);
            Silian_result.setSuccess(false);
            Silian_result.setMessage("出错了: " + Silian_e.getMessage());
            return Silian_result;
        }
    }

    /**
     * 删除指定机构的用户关系
     */
    //@RequiresRoles({"admin"})
    @RequestMapping(value = "/deleteUserInDepart", method = RequestMethod.DELETE)
    public Result<SysUserDepart> deleteUserInDepart(@RequestParam(name = "depId") String Silian_depId,
                                                    @RequestParam(name = "userId", required = true) String Silian_userId
    ) {
        Result<SysUserDepart> Silian_result = new Result<SysUserDepart>();
        try {
            QueryWrapper<SysUserDepart> Silian_queryWrapper = new QueryWrapper<SysUserDepart>();
            Silian_queryWrapper.eq("dep_id", Silian_depId).eq("user_id", Silian_userId);
            boolean Silian_b = sysUserDepartService.remove(Silian_queryWrapper);
            if (Silian_b) {
                List<SysDepartRole> Silian_sysDepartRoleList = departRoleService.list(new QueryWrapper<SysDepartRole>().eq("depart_id", Silian_depId));
                List<String> Silian_roleIds = Silian_sysDepartRoleList.stream().map(SysDepartRole::getId).collect(Collectors.toList());
                if (Silian_roleIds != null && Silian_roleIds.size() > 0) {
                    QueryWrapper<SysDepartRoleUser> Silian_query = new QueryWrapper<>();
                    Silian_query.eq("user_id", Silian_userId).in("drole_id", Silian_roleIds);
                    departRoleUserService.remove(Silian_query);
                }
                Silian_result.success("删除成功!");
            } else {
                Silian_result.error500("当前选中部门与用户无关联关系!");
            }
        } catch (Exception Silian_e) {
            log.error(Silian_e.getMessage(), Silian_e);
            Silian_result.error500("删除失败！");
        }
        return Silian_result;
    }

    /**
     * 批量删除指定机构的用户关系
     */
    //@RequiresRoles({"admin"})
    @RequestMapping(value = "/deleteUserInDepartBatch", method = RequestMethod.DELETE)
    public Result<SysUserDepart> deleteUserInDepartBatch(
            @RequestParam(name = "depId") String Silian_depId,
            @RequestParam(name = "userIds", required = true) String Silian_userIds) {
        Result<SysUserDepart> Silian_result = new Result<SysUserDepart>();
        try {
            QueryWrapper<SysUserDepart> Silian_queryWrapper = new QueryWrapper<SysUserDepart>();
            Silian_queryWrapper.eq("dep_id", Silian_depId).in("user_id", Arrays.asList(Silian_userIds.split(",")));
            boolean Silian_b = sysUserDepartService.remove(Silian_queryWrapper);
            if (Silian_b) {
                departRoleUserService.removeDeptRoleUser(Arrays.asList(Silian_userIds.split(",")), Silian_depId);
            }
            Silian_result.success("删除成功!");
        } catch (Exception Silian_e) {
            log.error(Silian_e.getMessage(), Silian_e);
            Silian_result.error500("删除失败！");
        }
        return Silian_result;
    }

    /**
     * 查询当前用户的所有部门/当前部门编码
     *
     * @return
     */
    @RequestMapping(value = "/getCurrentUserDeparts", method = RequestMethod.GET)
    public Result<Map<String, Object>> getCurrentUserDeparts() {
        Result<Map<String, Object>> Silian_result = new Result<Map<String, Object>>();
        try {
            LoginUser Silian_sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            List<SysDepart> Silian_list = this.sysDepartService.queryUserDeparts(Silian_sysUser.getId());
            Map<String, Object> Silian_map = new HashMap(5);
            Silian_map.put("list", Silian_list);
            Silian_map.put("orgCode", Silian_sysUser.getOrgCode());
            Silian_result.setSuccess(true);
            Silian_result.setResult(Silian_map);
        } catch (Exception Silian_e) {
            log.error(Silian_e.getMessage(), Silian_e);
            Silian_result.error500("查询失败！");
        }
        return Silian_result;
    }


    /**
     * 用户注册接口
     *
     * @param jsonObject
     * @param user
     * @return
     */
    @PostMapping("/register")
    public Result<JSONObject> userRegister(@RequestBody JSONObject Silian_jsonObject, SysUser Silian_user) {
        Result<JSONObject> Silian_result = new Result<JSONObject>();
        String Silian_phone = Silian_jsonObject.getString("phone");
        String Silian_smscode = Silian_jsonObject.getString("smscode");

        //update-begin-author:taoyan date:2022-9-13 for: VUEN-2245 【漏洞】发现新漏洞待处理20220906
        String Silian_redisKey = CommonConstant.PHONE_REDIS_KEY_PRE + Silian_phone;
        Object Silian_code = redisUtil.get(Silian_redisKey);
        //update-end-author:taoyan date:2022-9-13 for: VUEN-2245 【漏洞】发现新漏洞待处理20220906

        String Silian_username = Silian_jsonObject.getString("username");
        //未设置用户名，则用手机号作为用户名
        if (oConvertUtils.isEmpty(Silian_username)) {
            Silian_username = Silian_phone;
        }
        //未设置密码，则随机生成一个密码
        String Silian_password = Silian_jsonObject.getString("password");
        if (oConvertUtils.isEmpty(Silian_password)) {
            Silian_password = RandomUtil.randomString(8);
        }
        String Silian_email = Silian_jsonObject.getString("email");
        SysUser Silian_sysUser1 = sysUserService.getUserByName(Silian_username);
        if (Silian_sysUser1 != null) {
            Silian_result.setMessage("用户名已注册");
            Silian_result.setSuccess(false);
            return Silian_result;
        }
        SysUser Silian_sysUser2 = sysUserService.getUserByPhone(Silian_phone);
        if (Silian_sysUser2 != null) {
            Silian_result.setMessage("该手机号已注册");
            Silian_result.setSuccess(false);
            return Silian_result;
        }

        if (oConvertUtils.isNotEmpty(Silian_email)) {
            SysUser Silian_sysUser3 = sysUserService.getUserByEmail(Silian_email);
            if (Silian_sysUser3 != null) {
                Silian_result.setMessage("邮箱已被注册");
                Silian_result.setSuccess(false);
                return Silian_result;
            }
        }
        if (null == Silian_code) {
            Silian_result.setMessage("手机验证码失效，请重新获取");
            Silian_result.setSuccess(false);
            return Silian_result;
        }
        if (!Silian_smscode.equals(Silian_code.toString())) {
            Silian_result.setMessage("手机验证码错误");
            Silian_result.setSuccess(false);
            return Silian_result;
        }

        try {
            Silian_user.setCreateTime(new Date());// 设置创建时间
            String Silian_salt = oConvertUtils.randomGen(8);
            String Silian_passwordEncode = PasswordUtil.encrypt(Silian_username, Silian_password, Silian_salt);
            Silian_user.setSalt(Silian_salt);
            Silian_user.setUsername(Silian_username);
            Silian_user.setRealname(Silian_username);
            Silian_user.setPassword(Silian_passwordEncode);
            Silian_user.setEmail(Silian_email);
            Silian_user.setPhone(Silian_phone);
            Silian_user.setStatus(CommonConstant.USER_UNFREEZE);
            Silian_user.setDelFlag(CommonConstant.DEL_FLAG_0);
            Silian_user.setActivitiSync(CommonConstant.ACT_SYNC_0);
            sysUserService.addUserWithRole(Silian_user, "ee8626f80f7c2619917b6236f3a7f02b");//默认临时角色 test
            Silian_result.success("注册成功");
        } catch (Exception Silian_e) {
            Silian_result.error500("注册失败");
        }
        return Silian_result;
    }

//	/**
//	 * 根据用户名或手机号查询用户信息
//	 * @param
//	 * @return
//	 */
//	@GetMapping("/querySysUser")
//	public Result<Map<String, Object>> querySysUser(SysUser sysUser) {
//		String phone = sysUser.getPhone();
//		String username = sysUser.getUsername();
//		Result<Map<String, Object>> result = new Result<Map<String, Object>>();
//		Map<String, Object> map = new HashMap<String, Object>();
//		if (oConvertUtils.isNotEmpty(phone)) {
//			SysUser user = sysUserService.getUserByPhone(phone);
//			if(user!=null) {
//				map.put("username",user.getUsername());
//				map.put("phone",user.getPhone());
//				result.setSuccess(true);
//				result.setResult(map);
//				return result;
//			}
//		}
//		if (oConvertUtils.isNotEmpty(username)) {
//			SysUser user = sysUserService.getUserByName(username);
//			if(user!=null) {
//				map.put("username",user.getUsername());
//				map.put("phone",user.getPhone());
//				result.setSuccess(true);
//				result.setResult(map);
//				return result;
//			}
//		}
//		result.setSuccess(false);
//		result.setMessage("验证失败");
//		return result;
//	}

    /**
     * 用户手机号验证
     */
    @PostMapping("/phoneVerification")
    public Result<Map<String, String>> phoneVerification(@RequestBody JSONObject Silian_jsonObject) {
        Result<Map<String, String>> Silian_result = new Result<Map<String, String>>();
        String Silian_phone = Silian_jsonObject.getString("phone");
        String Silian_smscode = Silian_jsonObject.getString("smscode");
        //update-begin-author:taoyan date:2022-9-13 for: VUEN-2245 【漏洞】发现新漏洞待处理20220906
        String Silian_redisKey = CommonConstant.PHONE_REDIS_KEY_PRE + Silian_phone;
        Object Silian_code = redisUtil.get(Silian_redisKey);
        if (!Silian_smscode.equals(Silian_code)) {
            Silian_result.setMessage("手机验证码错误");
            Silian_result.setSuccess(false);
            return Silian_result;
        }
        //设置有效时间
        redisUtil.set(Silian_redisKey, Silian_smscode, 600);
        //update-end-author:taoyan date:2022-9-13 for: VUEN-2245 【漏洞】发现新漏洞待处理20220906

        //新增查询用户名
        LambdaQueryWrapper<SysUser> Silian_query = new LambdaQueryWrapper<>();
        Silian_query.eq(SysUser::getPhone, Silian_phone);
        SysUser Silian_user = sysUserService.getOne(Silian_query);
        Map<String, String> Silian_map = new HashMap(5);
        Silian_map.put("smscode", Silian_smscode);
        Silian_map.put("username", Silian_user.getUsername());
        Silian_result.setResult(Silian_map);
        Silian_result.setSuccess(true);
        return Silian_result;
    }

    /**
     * 用户更改密码
     */
    @GetMapping("/passwordChange")
    public Result<SysUser> passwordChange(@RequestParam(name = "username") String Silian_username,
                                          @RequestParam(name = "password") String Silian_password,
                                          @RequestParam(name = "smscode") String Silian_smscode,
                                          @RequestParam(name = "phone") String Silian_phone) {
        Result<SysUser> Silian_result = new Result<SysUser>();
        if (oConvertUtils.isEmpty(Silian_username) || oConvertUtils.isEmpty(Silian_password) || oConvertUtils.isEmpty(Silian_smscode) || oConvertUtils.isEmpty(Silian_phone)) {
            Silian_result.setMessage("重置密码失败！");
            Silian_result.setSuccess(false);
            return Silian_result;
        }

        SysUser Silian_sysUser = new SysUser();
        //update-begin-author:taoyan date:2022-9-13 for: VUEN-2245 【漏洞】发现新漏洞待处理20220906
        String Silian_redisKey = CommonConstant.PHONE_REDIS_KEY_PRE + Silian_phone;
        Object Silian_object = redisUtil.get(Silian_redisKey);
        //update-end-author:taoyan date:2022-9-13 for: VUEN-2245 【漏洞】发现新漏洞待处理20220906
        if (null == Silian_object) {
            Silian_result.setMessage("短信验证码失效！");
            Silian_result.setSuccess(false);
            return Silian_result;
        }
        if (!Silian_smscode.equals(Silian_object.toString())) {
            Silian_result.setMessage("短信验证码不匹配！");
            Silian_result.setSuccess(false);
            return Silian_result;
        }
        Silian_sysUser = this.sysUserService.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, Silian_username).eq(SysUser::getPhone, Silian_phone));
        if (Silian_sysUser == null) {
            Silian_result.setMessage("未找到用户！");
            Silian_result.setSuccess(false);
            return Silian_result;
        } else {
            String Silian_salt = oConvertUtils.randomGen(8);
            Silian_sysUser.setSalt(Silian_salt);
            String Silian_passwordEncode = PasswordUtil.encrypt(Silian_sysUser.getUsername(), Silian_password, Silian_salt);
            Silian_sysUser.setPassword(Silian_passwordEncode);
            this.sysUserService.updateById(Silian_sysUser);
            //update-begin---author:wangshuai ---date:20220316  for：[VUEN-234]密码重置添加敏感日志------------
            baseCommonService.addLog("重置 " + Silian_username + " 的密码，操作人： " + Silian_sysUser.getUsername(), CommonConstant.LOG_TYPE_2, 2);
            //update-end---author:wangshuai ---date:20220316  for：[VUEN-234]密码重置添加敏感日志------------
            Silian_result.setSuccess(true);
            Silian_result.setMessage("密码重置完成！");
            return Silian_result;
        }
    }


    /**
     * 根据TOKEN获取用户的部分信息（返回的数据是可供表单设计器使用的数据）
     *
     * @return
     */
    @GetMapping("/getUserSectionInfoByToken")
    public Result<?> getUserSectionInfoByToken(HttpServletRequest Silian_request, @RequestParam(name = "token", required = false) String Silian_token) {
        try {
            String Silian_username = null;
            // 如果没有传递token，就从header中获取token并获取用户信息
            if (oConvertUtils.isEmpty(Silian_token)) {
                Silian_username = JwtUtil.getUserNameByToken(Silian_request);
            } else {
                Silian_username = JwtUtil.getUsername(Silian_token);
            }

            log.debug(" ------ 通过令牌获取部分用户信息，当前用户： " + Silian_username);

            // 根据用户名查询用户信息
            SysUser Silian_sysUser = sysUserService.getUserByName(Silian_username);
            Map<String, Object> Silian_map = new HashMap<String, Object>();
            Silian_map.put("sysUserId", Silian_sysUser.getId());
            Silian_map.put("sysUserCode", Silian_sysUser.getUsername()); // 当前登录用户登录账号
            Silian_map.put("sysUserName", Silian_sysUser.getRealname()); // 当前登录用户真实名称
            Silian_map.put("sysOrgCode", Silian_sysUser.getOrgCode()); // 当前登录用户部门编号

            log.debug(" ------ 通过令牌获取部分用户信息，已获取的用户信息： " + Silian_map);

            return Result.ok(Silian_map);
        } catch (Exception Silian_e) {
            log.error(Silian_e.getMessage(), Silian_e);
            return Result.error(500, "查询失败:" + Silian_e.getMessage());
        }
    }

    /**
     * 【APP端接口】获取用户列表  根据用户名和真实名 模糊匹配
     *
     * @param keyword
     * @param pageNo
     * @param pageSize
     * @return
     */
    @GetMapping("/appUserList")
    public Result<?> appUserList(@RequestParam(name = "keyword", required = false) String Silian_keyword,
                                 @RequestParam(name = "username", required = false) String Silian_username,
                                 @RequestParam(name = "pageNo", defaultValue = "1") Integer Silian_pageNo,
                                 @RequestParam(name = "pageSize", defaultValue = "10") Integer Silian_pageSize,
                                 @RequestParam(name = "syncFlow", required = false) String Silian_syncFlow) {
        try {
            //TODO 从查询效率上将不要用mp的封装的page分页查询 建议自己写分页语句
            LambdaQueryWrapper<SysUser> Silian_query = new LambdaQueryWrapper<SysUser>();
            if (oConvertUtils.isNotEmpty(Silian_syncFlow)) {
                Silian_query.eq(SysUser::getActivitiSync, CommonConstant.ACT_SYNC_1);
            }
            Silian_query.eq(SysUser::getDelFlag, CommonConstant.DEL_FLAG_0);
            if (oConvertUtils.isNotEmpty(Silian_username)) {
                if (Silian_username.contains(",")) {
                    Silian_query.in(SysUser::getUsername, Silian_username.split(","));
                } else {
                    Silian_query.eq(SysUser::getUsername, Silian_username);
                }
            } else {
                Silian_query.and(Silian_i -> Silian_i.like(SysUser::getUsername, Silian_keyword).or().like(SysUser::getRealname, Silian_keyword));
            }
            Page<SysUser> Silian_page = new Page<>(Silian_pageNo, Silian_pageSize);
            IPage<SysUser> Silian_res = this.sysUserService.page(Silian_page, Silian_query);
            return Result.ok(Silian_res);
        } catch (Exception Silian_e) {
            log.error(Silian_e.getMessage(), Silian_e);
            return Result.error(500, "查询失败:" + Silian_e.getMessage());
        }

    }

    /**
     * 获取被逻辑删除的用户列表，无分页
     *
     * @return logicDeletedUserList
     */
    @GetMapping("/recycleBin")
    public Result getRecycleBin() {
        List<SysUser> Silian_logicDeletedUserList = sysUserService.queryLogicDeleted();
        if (Silian_logicDeletedUserList.size() > 0) {
            // 批量查询用户的所属部门
            // step.1 先拿到全部的 userIds
            List<String> Silian_userIds = Silian_logicDeletedUserList.stream().map(SysUser::getId).collect(Collectors.toList());
            // step.2 通过 userIds，一次性查询用户的所属部门名字
            Map<String, String> Silian_useDepNames = sysUserService.getDepNamesByUserIds(Silian_userIds);
            Silian_logicDeletedUserList.forEach(Silian_item -> Silian_item.setOrgCode(Silian_useDepNames.get(Silian_item.getId())));
        }
        return Result.ok(Silian_logicDeletedUserList);
    }

    /**
     * 还原被逻辑删除的用户
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/putRecycleBin", method = RequestMethod.PUT)
    public Result putRecycleBin(@RequestBody JSONObject Silian_jsonObject, HttpServletRequest Silian_request) {
        String Silian_userIds = Silian_jsonObject.getString("userIds");
        if (StringUtils.isNotBlank(Silian_userIds)) {
            SysUser Silian_updateUser = new SysUser();
            Silian_updateUser.setUpdateBy(JwtUtil.getUserNameByToken(Silian_request));
            Silian_updateUser.setUpdateTime(new Date());
            sysUserService.revertLogicDeleted(Arrays.asList(Silian_userIds.split(",")), Silian_updateUser);
        }
        return Result.ok("还原成功");
    }

    /**
     * 彻底删除用户
     *
     * @param userIds 被删除的用户ID，多个id用半角逗号分割
     * @return
     */
    //@RequiresRoles({"admin"})
    @RequestMapping(value = "/deleteRecycleBin", method = RequestMethod.DELETE)
    public Result deleteRecycleBin(@RequestParam("userIds") String Silian_userIds) {
        if (StringUtils.isNotBlank(Silian_userIds)) {
            sysUserService.removeLogicDeleted(Arrays.asList(Silian_userIds.split(",")));
        }
        return Result.ok("删除成功");
    }


    /**
     * 移动端修改用户信息
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/appEdit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<SysUser> appEdit(HttpServletRequest Silian_request, @RequestBody JSONObject Silian_jsonObject) {
        Result<SysUser> Silian_result = new Result<SysUser>();
        try {
            String Silian_username = JwtUtil.getUserNameByToken(Silian_request);
            SysUser Silian_sysUser = sysUserService.getUserByName(Silian_username);
            baseCommonService.addLog("移动端编辑用户，id： " + Silian_jsonObject.getString("id"), CommonConstant.LOG_TYPE_2, 2);
            String Silian_realname = Silian_jsonObject.getString("realname");
            String Silian_avatar = Silian_jsonObject.getString("avatar");
            String Silian_sex = Silian_jsonObject.getString("sex");
            String Silian_phone = Silian_jsonObject.getString("phone");
            String Silian_email = Silian_jsonObject.getString("email");
            Date Silian_birthday = Silian_jsonObject.getDate("birthday");
            SysUser Silian_userPhone = sysUserService.getUserByPhone(Silian_phone);
            if (Silian_sysUser == null) {
                Silian_result.error500("未找到对应用户!");
            } else {
                if (Silian_userPhone != null) {
                    String Silian_userPhonename = Silian_userPhone.getUsername();
                    if (!Silian_userPhonename.equals(Silian_username)) {
                        Silian_result.error500("手机号已存在!");
                        return Silian_result;
                    }
                }
                if (StringUtils.isNotBlank(Silian_realname)) {
                    Silian_sysUser.setRealname(Silian_realname);
                }
                if (StringUtils.isNotBlank(Silian_avatar)) {
                    Silian_sysUser.setAvatar(Silian_avatar);
                }
                if (StringUtils.isNotBlank(Silian_sex)) {
                    Silian_sysUser.setSex(Integer.parseInt(Silian_sex));
                }
                if (StringUtils.isNotBlank(Silian_phone)) {
                    Silian_sysUser.setPhone(Silian_phone);
                }
                if (StringUtils.isNotBlank(Silian_email)) {
                    //update-begin---author:wangshuai ---date:20220708  for：[VUEN-1528]积木官网邮箱重复，应该提示准确------------
                    LambdaQueryWrapper<SysUser> Silian_emailQuery = new LambdaQueryWrapper<>();
                    Silian_emailQuery.eq(SysUser::getEmail, Silian_email);
                    long Silian_count = sysUserService.count(Silian_emailQuery);
                    if (!Silian_email.equals(Silian_sysUser.getEmail()) && Silian_count != 0) {
                        Silian_result.error500("保存失败，邮箱已存在!");
                        return Silian_result;
                    }
                    //update-end---author:wangshuai ---date:20220708  for：[VUEN-1528]积木官网邮箱重复，应该提示准确--------------
                    Silian_sysUser.setEmail(Silian_email);
                }
                if (null != Silian_birthday) {
                    Silian_sysUser.setBirthday(Silian_birthday);
                }
                Silian_sysUser.setUpdateTime(new Date());
                sysUserService.updateById(Silian_sysUser);
            }
        } catch (Exception Silian_e) {
            log.error(Silian_e.getMessage(), Silian_e);
            Silian_result.error500("保存失败!");
        }
        return Silian_result;
    }

    /**
     * 移动端保存设备信息
     *
     * @param clientId
     * @return
     */
    @RequestMapping(value = "/saveClientId", method = RequestMethod.GET)
    public Result<SysUser> saveClientId(HttpServletRequest Silian_request, @RequestParam("clientId") String Silian_clientId) {
        Result<SysUser> Silian_result = new Result<SysUser>();
        try {
            String Silian_username = JwtUtil.getUserNameByToken(Silian_request);
            SysUser Silian_sysUser = sysUserService.getUserByName(Silian_username);
            if (Silian_sysUser == null) {
                Silian_result.error500("未找到对应用户!");
            } else {
                Silian_sysUser.setClientId(Silian_clientId);
                sysUserService.updateById(Silian_sysUser);
            }
        } catch (Exception Silian_e) {
            log.error(Silian_e.getMessage(), Silian_e);
            Silian_result.error500("操作失败!");
        }
        return Silian_result;
    }

    /**
     * 根据userid获取用户信息和部门员工信息
     *
     * @return Result
     */
    @GetMapping("/queryChildrenByUsername")
    public Result queryChildrenByUsername(@RequestParam("userId") String Silian_userId) {
        //获取用户信息
        Map<String, Object> Silian_map = new HashMap(5);
        SysUser Silian_sysUser = sysUserService.getById(Silian_userId);
        String Silian_username = Silian_sysUser.getUsername();
        Integer Silian_identity = Silian_sysUser.getUserIdentity();
        Silian_map.put("sysUser", Silian_sysUser);
        if (Silian_identity != null && Silian_identity == 2) {
            //获取部门用户信息
            String Silian_departIds = Silian_sysUser.getDepartIds();
            if (StringUtils.isNotBlank(Silian_departIds)) {
                List<String> Silian_departIdList = Arrays.asList(Silian_departIds.split(","));
                List<SysUser> Silian_childrenUser = sysUserService.queryByDepIds(Silian_departIdList, Silian_username);
                Silian_map.put("children", Silian_childrenUser);
            }
        }
        return Result.ok(Silian_map);
    }

    /**
     * 移动端查询部门用户信息
     *
     * @param departId
     * @return
     */
    @GetMapping("/appQueryByDepartId")
    public Result<List<SysUser>> appQueryByDepartId(@RequestParam(name = "departId", required = false) String Silian_departId) {
        Result<List<SysUser>> Silian_result = new Result<List<SysUser>>();
        List<String> Silian_list = new ArrayList<String>();
        Silian_list.add(Silian_departId);
        List<SysUser> Silian_childrenUser = sysUserService.queryByDepIds(Silian_list, null);
        Silian_result.setResult(Silian_childrenUser);
        return Silian_result;
    }

    /**
     * 移动端查询用户信息(通过用户名模糊查询)
     *
     * @param keyword
     * @return
     */
    @GetMapping("/appQueryUser")
    public Result<List<SysUser>> appQueryUser(@RequestParam(name = "keyword", required = false) String Silian_keyword,
                                              @RequestParam(name = "pageNo", defaultValue = "1") Integer Silian_pageNo,
                                              @RequestParam(name = "pageSize", defaultValue = "10") Integer Silian_pageSize) {
        Result<List<SysUser>> Silian_result = new Result<List<SysUser>>();
        LambdaQueryWrapper<SysUser> Silian_queryWrapper = new LambdaQueryWrapper<SysUser>();
        //TODO 外部模拟登陆临时账号，列表不显示
        Silian_queryWrapper.ne(SysUser::getUsername, "_reserve_user_external");
        if (StringUtils.isNotBlank(Silian_keyword)) {
            Silian_queryWrapper.and(Silian_i -> Silian_i.like(SysUser::getUsername, Silian_keyword).or().like(SysUser::getRealname, Silian_keyword));
        }
        Page<SysUser> Silian_page = new Page<>(Silian_pageNo, Silian_pageSize);
        IPage<SysUser> Silian_pageList = this.sysUserService.page(Silian_page, Silian_queryWrapper);
        //批量查询用户的所属部门
        //step.1 先拿到全部的 useids
        //step.2 通过 useids，一次性查询用户的所属部门名字
        List<String> Silian_userIds = Silian_pageList.getRecords().stream().map(SysUser::getId).collect(Collectors.toList());
        if (Silian_userIds != null && Silian_userIds.size() > 0) {
            Map<String, String> Silian_useDepNames = sysUserService.getDepNamesByUserIds(Silian_userIds);
            Silian_pageList.getRecords().forEach(Silian_item -> {
                Silian_item.setOrgCodeTxt(Silian_useDepNames.get(Silian_item.getId()));
            });
        }
        Silian_result.setResult(Silian_pageList.getRecords());
        return Silian_result;
    }

    /**
     * 根据用户名修改手机号[该方法未使用]
     *
     * @param json
     * @return
     */
    @RequestMapping(value = "/updateMobile", method = RequestMethod.PUT)
    public Result<?> changMobile(@RequestBody JSONObject Silian_json, HttpServletRequest Silian_request) {
        String Silian_smscode = Silian_json.getString("smscode");
        String Silian_phone = Silian_json.getString("phone");
        Result<SysUser> Silian_result = new Result<SysUser>();
        //获取登录用户名
        String Silian_username = JwtUtil.getUserNameByToken(Silian_request);
        if (oConvertUtils.isEmpty(Silian_username) || oConvertUtils.isEmpty(Silian_smscode) || oConvertUtils.isEmpty(Silian_phone)) {
            Silian_result.setMessage("修改手机号失败！");
            Silian_result.setSuccess(false);
            return Silian_result;
        }
        //update-begin-author:taoyan date:2022-9-13 for: VUEN-2245 【漏洞】发现新漏洞待处理20220906
        String Silian_redisKey = CommonConstant.PHONE_REDIS_KEY_PRE + Silian_phone;
        Object Silian_object = redisUtil.get(Silian_redisKey);
        //update-end-author:taoyan date:2022-9-13 for: VUEN-2245 【漏洞】发现新漏洞待处理20220906
        if (null == Silian_object) {
            Silian_result.setMessage("短信验证码失效！");
            Silian_result.setSuccess(false);
            return Silian_result;
        }
        if (!Silian_smscode.equals(Silian_object.toString())) {
            Silian_result.setMessage("短信验证码不匹配！");
            Silian_result.setSuccess(false);
            return Silian_result;
        }
        SysUser Silian_user = sysUserService.getUserByName(Silian_username);
        if (Silian_user == null) {
            return Result.error("用户不存在！");
        }
        Silian_user.setPhone(Silian_phone);
        sysUserService.updateById(Silian_user);
        return Result.ok("手机号设置成功!");
    }


    /**
     * 根据对象里面的属性值作in查询 属性可能会变 用户组件用到
     *
     * @param sysUser
     * @return
     */
    @GetMapping("/getMultiUser")
    public List<SysUser> getMultiUser(SysUser Silian_sysUser) {
        QueryWrapper<SysUser> Silian_queryWrapper = QueryGenerator.initQueryWrapper(Silian_sysUser, null);
        //update-begin---author:wangshuai ---date:20220104  for：[JTC-297]已冻结用户仍可设置为代理人------------
        Silian_queryWrapper.eq("status", Integer.parseInt(CommonConstant.STATUS_1));
        //update-end---author:wangshuai ---date:20220104  for：[JTC-297]已冻结用户仍可设置为代理人------------
        List<SysUser> Silian_ls = this.sysUserService.list(Silian_queryWrapper);
        for (SysUser Silian_user : Silian_ls) {
            Silian_user.setPassword(null);
            Silian_user.setSalt(null);
        }
        return Silian_ls;
    }


    /**
     * 获取参赛队伍数量
     *
     * @return
     */
    @GetMapping("/getTeamNumber")
    public Result<?> getTeamNumber() {
        List<Map<String, Object>> Silian_list = sysUserService.getTeamNumber();
        if (Silian_list == null || Silian_list.isEmpty()){
            return Result.error("获取数据失败");
        }
        Map<String, Long> Silian_resultMap = new HashMap<>();
        for (Map<String, Object> Silian_data : Silian_list) {
            String Silian_departName = (String) Silian_data.get("depart_name");
            Long Silian_count = (Long) Silian_data.get("count");
            Silian_resultMap.put(Silian_departName, Silian_count);
        }
        return Result.ok(Silian_resultMap);
    }


    /**
     * 获取银行公共储蓄
     *
     * @return
     */
    @GetMapping("/getBankBalance")
    public Result<?> getBankBalance() {
        List<Map<String, Object>> Silian_list = sysUserService.getBankBalance();
        if (Silian_list == null || Silian_list.isEmpty()){
            return Result.error("获取数据失败");
        }
        Map<String, Double> Silian_resultMap = new HashMap<>();
        for (Map<String, Object> Silian_data : Silian_list) {
            String Silian_departName = (String) Silian_data.get("depart_name");
            Double Silian_balance = (Double) Silian_data.get("balance");
            Silian_resultMap.put(Silian_departName, Silian_balance);
        }
        return Result.ok(Silian_resultMap);
    }

}
