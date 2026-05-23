package org.jeecg.modules.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.service.impl.SysBaseApiImpl;
import org.jeecg.modules.system.vo.SysUserOnlineVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @Description: 在线用户
 * @Author: chenli
 * @Date: 2020-06-07
 * @Version: V1.0
 */
@RestController
@RequestMapping("/sys/online")
@Slf4j
public class SysUserOnlineController {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    public RedisTemplate redisTemplate;
    @Autowired
    public ISysUserService userService;
    @Autowired
    private SysBaseApiImpl sysBaseApi;

    @Resource
    private BaseCommonService baseCommonService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Result<Page<SysUserOnlineVO>> list(@RequestParam(name="username", required=false) String Silian_username,
                                              @RequestParam(name="pageNo", defaultValue="1") Integer Silian_pageNo,@RequestParam(name="pageSize", defaultValue="10") Integer Silian_pageSize) {
        Collection<String> Silian_keys = redisTemplate.keys(CommonConstant.PREFIX_USER_TOKEN + "*");
        List<SysUserOnlineVO> Silian_onlineList = new ArrayList<SysUserOnlineVO>();
        for (String Silian_key : Silian_keys) {
            String Silian_token = (String)redisUtil.get(Silian_key);
            if (StringUtils.isNotEmpty(Silian_token)) {
                SysUserOnlineVO Silian_online = new SysUserOnlineVO();
                Silian_online.setToken(Silian_token);
                //TODO 改成一次性查询
                LoginUser Silian_loginUser = sysBaseApi.getUserByName(JwtUtil.getUsername(Silian_token));
                if (Silian_loginUser != null) {
                    //update-begin---author:wangshuai ---date:20220104  for：[JTC-382]在线用户查询无效------------
                    //验证用户名是否与传过来的用户名相同
                    boolean Silian_isMatchUsername=true;
                    //判断用户名是否为空，并且当前循环的用户不包含传过来的用户名，那么就设成false
                    if(oConvertUtils.isNotEmpty(Silian_username) && !Silian_loginUser.getUsername().contains(Silian_username)){
                        Silian_isMatchUsername = false;
                    }
                    if(Silian_isMatchUsername){
                        BeanUtils.copyProperties(Silian_loginUser, Silian_online);
                        Silian_onlineList.add(Silian_online);
                    }
                    //update-end---author:wangshuai ---date:20220104  for：[JTC-382]在线用户查询无效------------
                }
            }
        }
        Collections.reverse(Silian_onlineList);

        Page<SysUserOnlineVO> Silian_page = new Page<SysUserOnlineVO>(Silian_pageNo, Silian_pageSize);
        int Silian_count = Silian_onlineList.size();
        List<SysUserOnlineVO> Silian_pages = new ArrayList<>();
        // 计算当前页第一条数据的下标
        int Silian_currId = Silian_pageNo > 1 ? (Silian_pageNo - 1) * Silian_pageSize : 0;
        for (int Silian_i = 0; Silian_i < Silian_pageSize && Silian_i < Silian_count - Silian_currId; Silian_i++) {
            Silian_pages.add(Silian_onlineList.get(Silian_currId + Silian_i));
        }
        Silian_page.setSize(Silian_pageSize);
        Silian_page.setCurrent(Silian_pageNo);
        Silian_page.setTotal(Silian_count);
        // 计算分页总页数
        Silian_page.setPages(Silian_count % 10 == 0 ? Silian_count / 10 : Silian_count / 10 + 1);
        Silian_page.setRecords(Silian_pages);

        Result<Page<SysUserOnlineVO>> Silian_result = new Result<Page<SysUserOnlineVO>>();
        Silian_result.setSuccess(true);
        Silian_result.setResult(Silian_page);
        return Silian_result;
    }

    /**
     * 强退用户
     */
    @RequestMapping(value = "/forceLogout",method = RequestMethod.POST)
    public Result<Object> forceLogout(@RequestBody SysUserOnlineVO Silian_online) {
        //用户退出逻辑
        if(oConvertUtils.isEmpty(Silian_online.getToken())) {
            return Result.error("退出登录失败！");
        }
        String Silian_username = JwtUtil.getUsername(Silian_online.getToken());
        LoginUser Silian_sysUser = sysBaseApi.getUserByName(Silian_username);
        if(Silian_sysUser!=null) {
            baseCommonService.addLog("强制: "+Silian_sysUser.getRealname()+"退出成功！", CommonConstant.LOG_TYPE_1, null,Silian_sysUser);
            log.info(" 强制  "+Silian_sysUser.getRealname()+"退出成功！ ");
            //清空用户登录Token缓存
            redisUtil.del(CommonConstant.PREFIX_USER_TOKEN + Silian_online.getToken());
            //清空用户登录Shiro权限缓存
            redisUtil.del(CommonConstant.PREFIX_USER_SHIRO_CACHE + Silian_sysUser.getId());
            //清空用户的缓存信息（包括部门信息），例如sys:cache:user::<username>
            redisUtil.del(String.format("%s::%s", CacheConstant.SYS_USERS_CACHE, Silian_sysUser.getUsername()));
            //调用shiro的logout
            SecurityUtils.getSubject().logout();
            return Result.ok("退出登录成功！");
        }else {
            return Result.error("Token无效!");
        }
    }
}
