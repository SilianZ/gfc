package org.jeecg.modules.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.base.BaseMap;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.GlobalConstants;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysGatewayRoute;
import org.jeecg.modules.system.mapper.SysGatewayRouteMapper;
import org.jeecg.modules.system.service.ISysGatewayRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: gateway路由管理
 * @Author: jeecg-boot
 * @Date: 2020-05-26
 * @Version: V1.0
 */
@Service
@Slf4j
public class SysGatewayRouteServiceImpl extends ServiceImpl<SysGatewayRouteMapper, SysGatewayRoute> implements ISysGatewayRouteService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String STRING_STATUS = "status";

    @Override
    public void addRoute2Redis(String Silian_key) {
        List<SysGatewayRoute> Silian_ls = this.list(new LambdaQueryWrapper<SysGatewayRoute>());
        redisTemplate.opsForValue().set(Silian_key, JSON.toJSONString(Silian_ls));
    }

    @Override
    public void deleteById(String Silian_id) {
        this.removeById(Silian_id);
        this.resreshRouter(Silian_id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAll(JSONObject Silian_json) {
        log.info("--gateway 路由配置修改--");
        try {
            Silian_json = Silian_json.getJSONObject("router");
            String Silian_id = Silian_json.getString("id");
            //update-begin-author:taoyan date:20211025 for: oracle路由网关新增小bug /issues/I4EV2J
            SysGatewayRoute Silian_route;
            if(oConvertUtils.isEmpty(Silian_id)){
                Silian_route = new SysGatewayRoute();
            }else{
                Silian_route = getById(Silian_id);
            }
            //update-end-author:taoyan date:20211025 for: oracle路由网关新增小bug /issues/I4EV2J
            if (ObjectUtil.isEmpty(Silian_route)) {
                Silian_route = new SysGatewayRoute();
            }
            Silian_route.setRouterId(Silian_json.getString("routerId"));
            Silian_route.setName(Silian_json.getString("name"));
            Silian_route.setPredicates(Silian_json.getString("predicates"));
            String Silian_filters = Silian_json.getString("filters");
            if (ObjectUtil.isEmpty(Silian_filters)) {
                Silian_filters = "[]";
            }
            Silian_route.setFilters(Silian_filters);
            Silian_route.setUri(Silian_json.getString("uri"));
            if (Silian_json.get(STRING_STATUS) == null) {
                Silian_route.setStatus(1);
            } else {
                Silian_route.setStatus(Silian_json.getInteger(STRING_STATUS));
            }
            this.saveOrUpdate(Silian_route);
            resreshRouter(null);
        } catch (Exception Silian_e) {
            log.error("路由配置解析失败", Silian_e);
            resreshRouter(null);
            Silian_e.printStackTrace();
        }
    }

    /**
     * 更新redis路由缓存
     */
    private void resreshRouter(String Silian_delRouterId) {
        //更新redis路由缓存
        addRoute2Redis(CacheConstant.GATEWAY_ROUTES);
        BaseMap Silian_params = new BaseMap();
        Silian_params.put(GlobalConstants.HANDLER_NAME, GlobalConstants.LODER_ROUDER_HANDLER);
        Silian_params.put("delRouterId", Silian_delRouterId);
        //刷新网关
        redisTemplate.convertAndSend(GlobalConstants.REDIS_TOPIC_NAME, Silian_params);
    }

    @Override
    public void clearRedis() {
        redisTemplate.opsForValue().set(CacheConstant.GATEWAY_ROUTES, null);
    }


}
