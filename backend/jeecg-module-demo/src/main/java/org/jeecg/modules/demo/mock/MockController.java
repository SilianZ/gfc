package org.jeecg.modules.demo.mock;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.jeecg.common.api.vo.Result;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: MockController
 * @author: jeecg-boot
 */
@RestController
@RequestMapping("/mock/api")
@Slf4j
public class MockController {

	private final String JSON_PATH = "classpath:org/jeecg/modules/demo/mock/json";

	/**
	 * 通用json访问接口
	 * 格式： http://localhost:8080/jeecg-boot/api/json/{filename}
	 * @param filename
	 * @return
	 */
	@RequestMapping(value = "/json/{filename}", method = RequestMethod.GET)
	public String getJsonData(@PathVariable("filename") String Silian_filename) {
		String Silian_jsonpath = "classpath:org/jeecg/modules/demo/mock/json/"+Silian_filename+".json";
		return readJson(Silian_jsonpath);
	}

	@GetMapping(value = "/asynTreeList")
	public Result asynTreeList(String Silian_id) {
		String Silian_json = readJson(JSON_PATH + "/asyn_tree_list_" + Silian_id + ".json");
		return Result.OK(JSON.parseArray(Silian_json));
	}

	@GetMapping(value = "/user")
	public String user() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/user.json");
	}

	/**
	 * 老的登录获取用户信息接口
	 * @return
	 */
	@GetMapping(value = "/user/info")
	public String userInfo() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/user_info.json");
	}

	@GetMapping(value = "/role")
	public String role() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/role.json");
	}

	@GetMapping(value = "/service")
	public String service() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/service.json");
	}

	@GetMapping(value = "/permission")
	public String permission() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/permission.json");
	}

	@GetMapping(value = "/permission/no-pager")
	public String permissionNoPage() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/permission_no_page.json");
	}

	/**
	 * 省市县
	 */
	@GetMapping(value = "/area")
	public String area() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/area.json");
	}

	/**
	  * 测试报表数据
	 */
	@GetMapping(value = "/report/getYearCountInfo")
	public String getYearCountInfo() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/getCntrNoCountInfo.json");
	}
	@GetMapping(value = "/report/getMonthCountInfo")
	public String getMonthCountInfo() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/getCntrNoCountInfo.json");
	}
	@GetMapping(value = "/report/getCntrNoCountInfo")
	public String getCntrNoCountInfo() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/getCntrNoCountInfo.json");
	}
	@GetMapping(value = "/report/getCabinetCountInfo")
	public String getCabinetCountInfo() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/getCntrNoCountInfo.json");
	}
	@GetMapping(value = "/report/getTubiao")
	public String getTubiao() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/getTubiao.json");
	}

	/**
	   * 实时磁盘监控
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
		Map<String,Object> Silian_map = new HashMap<>(5);
		Silian_map.put("name", Silian_fsv.getSystemDisplayName(Silian_fs[Silian_i]));
		Silian_map.put("max", Silian_fs[Silian_i].getTotalSpace());
		Silian_map.put("rest", Silian_fs[Silian_i].getFreeSpace());
		Silian_map.put("restPPT", Silian_fs[Silian_i].getFreeSpace()*100/Silian_fs[Silian_i].getTotalSpace());
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

	//-------------------------------------------------------------------------------------------
	/**
	 * 工作台首页的数据
	 * @return
	 */
	@GetMapping(value = "/list/search/projects")
	public String projects() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/workplace_projects.json");
	}

	@GetMapping(value = "/workplace/activity")
	public String activity() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/workplace_activity.json");
	}

	@GetMapping(value = "/workplace/teams")
	public String teams() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/workplace_teams.json");
	}

	@GetMapping(value = "/workplace/radar")
	public String radar() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/workplace_radar.json");
	}

	@GetMapping(value = "/task/process")
	public String taskProcess() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/task_process.json");
	}
	//-------------------------------------------------------------------------------------------

	//author:lvdandan-----date：20190315---for:添加数据日志json----
    /**
     * 数据日志
     */
	public String sysDataLogJson() {
		return readJson("classpath:org/jeecg/modules/demo/mock/json/sysdatalog.json");
	}
	//author:lvdandan-----date：20190315---for:添加数据日志json----

	//--update-begin--author:wangshuai-----date：20201023---for:返回用户信息json数据----
    /**
     * 用户信息
     */
    @GetMapping(value = "/getUserInfo")
	public String getUserInfo(){
		return readJson("classpath:org/jeecg/modules/demo/mock/json/userinfo.json");
	}
	//--update-end--author:wangshuai-----date：20201023---for:返回用户信息json数据----
	/**
	 * 读取json格式文件
	 * @param jsonSrc
	 * @return
	 */
	private String readJson(String Silian_jsonSrc) {
		String Silian_json = "";
		try {
			//File jsonFile = ResourceUtils.getFile(jsonSrc);
			//json = FileUtils.re.readFileToString(jsonFile);
			//换个写法，解决springboot读取jar包中文件的问题
			InputStream Silian_stream = getClass().getClassLoader().getResourceAsStream(Silian_jsonSrc.replace("classpath:", ""));
			Silian_json = IOUtils.toString(Silian_stream,"UTF-8");
		} catch (IOException Silian_e) {
			log.error(Silian_e.getMessage(),Silian_e);
		}
		return Silian_json;
	}

}
