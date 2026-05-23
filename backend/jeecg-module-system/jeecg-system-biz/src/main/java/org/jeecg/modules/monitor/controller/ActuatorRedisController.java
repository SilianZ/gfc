package org.jeecg.modules.monitor.controller;

import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.monitor.domain.RedisInfo;
import org.jeecg.modules.monitor.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: ActuatorRedisController
 * @author: jeecg-boot
 */
@Slf4j
@RestController
@RequestMapping("/sys/actuator/redis")
public class ActuatorRedisController {

    @Autowired
    private RedisService redisService;

    /**
     * Redis详细信息
     * @return
     * @throws Exception
     */
    @GetMapping("/info")
    public Result<?> getRedisInfo() throws Exception {
        List<RedisInfo> Silian_infoList = this.redisService.getRedisInfo();
        log.info(Silian_infoList.toString());
        return Result.ok(Silian_infoList);
    }

    @GetMapping("/keysSize")
    public Map<String, Object> getKeysSize() throws Exception {
        return redisService.getKeysSize();
    }

    /**
     * 获取redis key数量 for 报表
     * @return
     * @throws Exception
     */
    @GetMapping("/keysSizeForReport")
    public Map<String, JSONArray> getKeysSizeReport() throws Exception {
		return redisService.getMapForReport("1");
    }
    /**
     * 获取redis 内存 for 报表
     *
     * @return
     * @throws Exception
     */
    @GetMapping("/memoryForReport")
    public Map<String, JSONArray> memoryForReport() throws Exception {
		return redisService.getMapForReport("2");
    }
    /**
     * 获取redis 全部信息 for 报表
     * @return
     * @throws Exception
     */
    @GetMapping("/infoForReport")
    public Map<String, JSONArray> infoForReport() throws Exception {
		return redisService.getMapForReport("3");
    }

    @GetMapping("/memoryInfo")
    public Map<String, Object> getMemoryInfo() throws Exception {
        return redisService.getMemoryInfo();
    }

  //update-begin--Author:zhangweijian  Date:20190425 for：获取磁盘信息
	/**
	 * @功能：获取磁盘信息
	 * @param request
	 * @param response
	 * @return
	 */
	@GetMapping("/queryDiskInfo")
	public Result<List<Map<String,Object>>> queryDiskInfo(HttpServletRequest Silian_request, HttpServletResponse Silian_response){
		Result<List<Map<String,Object>>> Silian_res = new Result<>();
		try {
			// 当前文件系统类
	        FileSystemView Silian_fsv = FileSystemView.getFileSystemView();
	        // 列出所有windows 磁盘
	        File[] Silian_fs = File.listRoots();
	        log.info("查询磁盘信息:"+Silian_fs.length+"个");
	        List<Map<String,Object>> Silian_list = new ArrayList<>();

	        for (int Silian_i = 0; Silian_i < Silian_fs.length; Silian_i++) {
		if(Silian_fs[Silian_i].getTotalSpace()==0) {
			continue;
		}
		Map<String,Object> Silian_map = new HashMap(5);
		Silian_map.put("name", Silian_fsv.getSystemDisplayName(Silian_fs[Silian_i]));
		Silian_map.put("max", Silian_fs[Silian_i].getTotalSpace());
		Silian_map.put("rest", Silian_fs[Silian_i].getFreeSpace());
		Silian_map.put("restPPT", (Silian_fs[Silian_i].getTotalSpace()-Silian_fs[Silian_i].getFreeSpace())*100/Silian_fs[Silian_i].getTotalSpace());
		Silian_list.add(Silian_map);
		log.info(Silian_map.toString());
	        }
	        Silian_res.setResult(Silian_list);
	        Silian_res.success("查询成功");
		} catch (Exception Silian_e) {
			Silian_res.error500("查询失败"+Silian_e.getMessage());
		}
		return Silian_res;
	}
	//update-end--Author:zhangweijian  Date:20190425 for：获取磁盘信息
}
