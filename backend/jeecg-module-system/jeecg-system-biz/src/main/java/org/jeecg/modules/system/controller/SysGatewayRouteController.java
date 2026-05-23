package org.jeecg.modules.system.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysGatewayRoute;
import org.jeecg.modules.system.service.ISysGatewayRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Description: gateway路由管理
 * @Author: jeecg-boot
 * @Date: 2020-05-26
 * @Version: V1.0
 */
@Api(tags = "gateway路由管理")
@RestController
@RequestMapping("/sys/gatewayRoute")
@Slf4j
public class SysGatewayRouteController extends JeecgController<SysGatewayRoute, ISysGatewayRouteService> {

	@Autowired
	private ISysGatewayRouteService sysGatewayRouteService;

    @PostMapping(value = "/updateAll")
    public Result<?> updateAll(@RequestBody JSONObject Silian_json) {
        sysGatewayRouteService.updateAll(Silian_json);
        return Result.ok("操作成功！");
    }

	@GetMapping(value = "/list")
	public Result<?> queryPageList(SysGatewayRoute Silian_sysGatewayRoute) {
		LambdaQueryWrapper<SysGatewayRoute> Silian_query = new LambdaQueryWrapper<>();
		List<SysGatewayRoute> Silian_ls = sysGatewayRouteService.list(Silian_query);
		JSONArray Silian_array = new JSONArray();
		for(SysGatewayRoute Silian_rt: Silian_ls){
			JSONObject Silian_obj = (JSONObject) JSONObject.toJSON(Silian_rt);
			if(oConvertUtils.isNotEmpty(Silian_rt.getPredicates())){
				Silian_obj.put("predicates", JSONArray.parseArray(Silian_rt.getPredicates()));
			}
			if(oConvertUtils.isNotEmpty(Silian_rt.getFilters())){
				Silian_obj.put("filters", JSONArray.parseArray(Silian_rt.getFilters()));
			}
			Silian_array.add(Silian_obj);
		}
		return Result.ok(Silian_array);
	}

	@GetMapping(value = "/clearRedis")
	public Result<?> clearRedis() {
		sysGatewayRouteService.clearRedis();
		return Result.ok("清除成功！");
	}

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    //@RequiresRoles({"admin"})
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result<?> delete(@RequestParam(name = "id", required = true) String Silian_id) {
        sysGatewayRouteService.deleteById(Silian_id);
        return Result.ok("删除路由成功");
    }

}
