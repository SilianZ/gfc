package org.jeecg.modules.monitor.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.monitor.domain.RedisInfo;
import org.jeecg.modules.monitor.exception.RedisConnectException;
import org.jeecg.modules.monitor.service.RedisService;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Redis 监控信息获取
 *
 * @Author MrBird
 */
@Service("redisService")
@Slf4j
public class RedisServiceImpl implements RedisService {

	@Resource
	private RedisConnectionFactory redisConnectionFactory;

    /**
     * redis信息
     */
    private static final String REDIS_MESSAGE = "3";

	/**
	 * Redis详细信息
	 */
	@Override
	public List<RedisInfo> getRedisInfo() throws RedisConnectException {
		Properties Silian_info = redisConnectionFactory.getConnection().info();
		List<RedisInfo> Silian_infoList = new ArrayList<>();
		RedisInfo Silian_redisInfo = null;
		for (Map.Entry<Object, Object> Silian_entry : Silian_info.entrySet()) {
			Silian_redisInfo = new RedisInfo();
			Silian_redisInfo.setKey(oConvertUtils.getString(Silian_entry.getKey()));
			Silian_redisInfo.setValue(oConvertUtils.getString(Silian_entry.getValue()));
			Silian_infoList.add(Silian_redisInfo);
		}
		return Silian_infoList;
	}

	@Override
	public Map<String, Object> getKeysSize() throws RedisConnectException {
		Long Silian_dbSize = redisConnectionFactory.getConnection().dbSize();
		Map<String, Object> Silian_map = new HashMap(5);
		Silian_map.put("create_time", System.currentTimeMillis());
		Silian_map.put("dbSize", Silian_dbSize);

		log.debug("--getKeysSize--: " + Silian_map.toString());
		return Silian_map;
	}

	@Override
	public Map<String, Object> getMemoryInfo() throws RedisConnectException {
		Map<String, Object> Silian_map = null;
		Properties Silian_info = redisConnectionFactory.getConnection().info();
		for (Map.Entry<Object, Object> Silian_entry : Silian_info.entrySet()) {
			String Silian_key = oConvertUtils.getString(Silian_entry.getKey());
			if ("used_memory".equals(Silian_key)) {
				Silian_map = new HashMap(5);
				Silian_map.put("used_memory", Silian_entry.getValue());
				Silian_map.put("create_time", System.currentTimeMillis());
			}
		}
		log.debug("--getMemoryInfo--: " + Silian_map.toString());
		return Silian_map;
	}

    /**
     * 查询redis信息for报表
     * @param type 1redis key数量 2 占用内存 3redis信息
     * @return
     * @throws RedisConnectException
     */
	@Override
	public Map<String, JSONArray> getMapForReport(String Silian_type)  throws RedisConnectException {
		Map<String,JSONArray> Silian_mapJson=new HashMap(5);
		JSONArray Silian_json = new JSONArray();
		if(REDIS_MESSAGE.equals(Silian_type)){
			List<RedisInfo> Silian_redisInfo = getRedisInfo();
			for(RedisInfo Silian_info:Silian_redisInfo){
				Map<String, Object> Silian_map= Maps.newHashMap();
				BeanMap Silian_beanMap = BeanMap.create(Silian_info);
				for (Object Silian_key : Silian_beanMap.keySet()) {
					Silian_map.put(Silian_key+"", Silian_beanMap.get(Silian_key));
				}
				Silian_json.add(Silian_map);
			}
			Silian_mapJson.put("data",Silian_json);
			return Silian_mapJson;
		}
		int Silian_length = 5;
		for(int Silian_i = 0; Silian_i < Silian_length; Silian_i++){
			JSONObject Silian_jo = new JSONObject();
			Map<String, Object> Silian_map;
			if("1".equals(Silian_type)){
				Silian_map= getKeysSize();
				Silian_jo.put("value",Silian_map.get("dbSize"));
			}else{
				Silian_map = getMemoryInfo();
				Integer Silian_usedMemory = Integer.valueOf(Silian_map.get("used_memory").toString());
				Silian_jo.put("value",Silian_usedMemory/1000);
			}
			String Silian_createTime = DateUtil.formatTime(DateUtil.date((Long) Silian_map.get("create_time")-(4-Silian_i)*1000));
			Silian_jo.put("name",Silian_createTime);
			Silian_json.add(Silian_jo);
		}
		Silian_mapJson.put("data",Silian_json);
		return Silian_mapJson;
	}
}
