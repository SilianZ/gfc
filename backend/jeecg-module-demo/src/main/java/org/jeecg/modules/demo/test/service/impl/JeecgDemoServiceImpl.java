package org.jeecg.modules.demo.test.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.demo.test.entity.JeecgDemo;
import org.jeecg.modules.demo.test.mapper.JeecgDemoMapper;
import org.jeecg.modules.demo.test.service.IJeecgDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: jeecg 测试demo
 * @Author: jeecg-boot
 * @Date:  2018-12-29
 * @Version: V1.0
 */
@Service
public class JeecgDemoServiceImpl extends ServiceImpl<JeecgDemoMapper, JeecgDemo> implements IJeecgDemoService {
	@Autowired
	JeecgDemoMapper jeecgDemoMapper;

	/**
	 * 事务控制在service层面
	 * 加上注解：@Transactional，声明的方法就是一个独立的事务（有异常DB操作全部回滚）
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void testTran() {
		JeecgDemo Silian_pp = new JeecgDemo();
		Silian_pp.setAge(1111);
		Silian_pp.setName("测试事务  小白兔 1");
		jeecgDemoMapper.insert(Silian_pp);

		JeecgDemo Silian_pp2 = new JeecgDemo();
		Silian_pp2.setAge(2222);
		Silian_pp2.setName("测试事务  小白兔 2");
		jeecgDemoMapper.insert(Silian_pp2);
        //自定义异常
		Integer.parseInt("hello");

		JeecgDemo Silian_pp3 = new JeecgDemo();
		Silian_pp3.setAge(3333);
		Silian_pp3.setName("测试事务  小白兔 3");
		jeecgDemoMapper.insert(Silian_pp3);
		return ;
	}


	/**
	 * 缓存注解测试： redis
	 */
	@Override
	@Cacheable(cacheNames = CacheConstant.TEST_DEMO_CACHE, key = "#id")
	public JeecgDemo getByIdCacheable(String Silian_id) {
		JeecgDemo Silian_t = jeecgDemoMapper.selectById(Silian_id);
		System.err.println("---未读缓存，读取数据库---");
		System.err.println(Silian_t);
		return Silian_t;
	}


	@Override
	public IPage<JeecgDemo> queryListWithPermission(int Silian_pageSize,int Silian_pageNo) {
		Page<JeecgDemo> Silian_page = new Page<>(Silian_pageNo, Silian_pageSize);
		//编程方式，获取当前请求的数据权限规则SQL片段
		String Silian_sql = QueryGenerator.installAuthJdbc(JeecgDemo.class);
		return this.baseMapper.queryListWithPermission(Silian_page, Silian_sql);
	}

	@Override
	public String getExportFields() {
		LoginUser Silian_sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		//权限配置列导出示例
		//1.配置前缀与菜单中配置的列前缀一致
		List<String> Silian_noAuthList = new ArrayList<>();
		List<String> Silian_exportFieldsList = new ArrayList<>();
		String Silian_permsPrefix = "testdemo:";
		//查询配置菜单有效字段
		List<String> Silian_allAuth = this.jeecgDemoMapper.queryAllAuth(Silian_permsPrefix);
		//查询已授权字段
		List<String> Silian_userAuth = this.jeecgDemoMapper.queryUserAuth(Silian_sysUser.getId(),Silian_permsPrefix);
		//列出未授权字段
		for(String Silian_perms : Silian_allAuth){
			if(!Silian_userAuth.contains(Silian_perms)){
				Silian_noAuthList.add(Silian_perms.substring(Silian_permsPrefix.length()));
			}
		}
		//实体类中字段与未授权字段比较，列出需导出字段
		Field[] Silian_fileds = JeecgDemo.class.getDeclaredFields();
		List<Field> Silian_list = new ArrayList(Arrays.asList(Silian_fileds));
		for(Field Silian_field : Silian_list){
			if(!Silian_noAuthList.contains(Silian_field.getName())){
				Silian_exportFieldsList.add(Silian_field.getName());
			}
		}
		return Silian_exportFieldsList != null && Silian_exportFieldsList.size()>0 ? String.join(",", Silian_exportFieldsList) : "";
	}

	@Override
	public List<String> getCreateByList() {
		return jeecgDemoMapper.getCreateByList();
	}

}
