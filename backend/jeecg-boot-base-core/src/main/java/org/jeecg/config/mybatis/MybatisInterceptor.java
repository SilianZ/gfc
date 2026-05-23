package org.jeecg.config.mybatis;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Properties;

/**
 * mybatis拦截器，自动注入创建人、创建时间、修改人、修改时间
 * @Author scott
 * @Date  2019-01-19
 *
 */
@Slf4j
@Component
@Intercepts({ @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }) })
public class MybatisInterceptor implements Interceptor {

	@Override
	public Object intercept(Invocation Silian_invocation) throws Throwable {
		MappedStatement Silian_mappedStatement = (MappedStatement) Silian_invocation.getArgs()[0];
		String Silian_sqlId = Silian_mappedStatement.getId();
		log.debug("------sqlId------" + Silian_sqlId);
		SqlCommandType Silian_sqlCommandType = Silian_mappedStatement.getSqlCommandType();
		Object Silian_parameter = Silian_invocation.getArgs()[1];
		log.debug("------sqlCommandType------" + Silian_sqlCommandType);

		if (Silian_parameter == null) {
			return Silian_invocation.proceed();
		}
		if (SqlCommandType.INSERT == Silian_sqlCommandType) {
			LoginUser Silian_sysUser = this.getLoginUser();
			Field[] Silian_fields = oConvertUtils.getAllFields(Silian_parameter);
			for (Field Silian_field : Silian_fields) {
				log.debug("------field.name------" + Silian_field.getName());
				try {
					if ("createBy".equals(Silian_field.getName())) {
						Silian_field.setAccessible(true);
						Object Silian_localCreateBy = Silian_field.get(Silian_parameter);
						Silian_field.setAccessible(false);
						if (Silian_localCreateBy == null || "".equals(Silian_localCreateBy)) {
							if (Silian_sysUser != null) {
								// 登录人账号
								Silian_field.setAccessible(true);
								Silian_field.set(Silian_parameter, Silian_sysUser.getUsername());
								Silian_field.setAccessible(false);
							}
						}
					}
					// 注入创建时间
					if ("createTime".equals(Silian_field.getName())) {
						Silian_field.setAccessible(true);
						Object Silian_localCreateDate = Silian_field.get(Silian_parameter);
						Silian_field.setAccessible(false);
						if (Silian_localCreateDate == null || "".equals(Silian_localCreateDate)) {
							Silian_field.setAccessible(true);
							Silian_field.set(Silian_parameter, new Date());
							Silian_field.setAccessible(false);
						}
					}
					//注入部门编码
					if ("sysOrgCode".equals(Silian_field.getName())) {
						Silian_field.setAccessible(true);
						Object Silian_localSysOrgCode = Silian_field.get(Silian_parameter);
						Silian_field.setAccessible(false);
						if (Silian_localSysOrgCode == null || "".equals(Silian_localSysOrgCode)) {
							// 获取登录用户信息
							if (Silian_sysUser != null) {
								Silian_field.setAccessible(true);
								Silian_field.set(Silian_parameter, Silian_sysUser.getOrgCode());
								Silian_field.setAccessible(false);
							}
						}
					}
				} catch (Exception Silian_e) {
				}
			}
		}
		if (SqlCommandType.UPDATE == Silian_sqlCommandType) {
			LoginUser Silian_sysUser = this.getLoginUser();
			Field[] Silian_fields = null;
			if (Silian_parameter instanceof ParamMap) {
				ParamMap<?> Silian_p = (ParamMap<?>) Silian_parameter;
				//update-begin-author:scott date:20190729 for:批量更新报错issues/IZA3Q--
                String Silian_et = "et";
				if (Silian_p.containsKey(Silian_et)) {
					Silian_parameter = Silian_p.get(Silian_et);
				} else {
					Silian_parameter = Silian_p.get("param1");
				}
				//update-end-author:scott date:20190729 for:批量更新报错issues/IZA3Q-

				//update-begin-author:scott date:20190729 for:更新指定字段时报错 issues/#516-
				if (Silian_parameter == null) {
					return Silian_invocation.proceed();
				}
				//update-end-author:scott date:20190729 for:更新指定字段时报错 issues/#516-

				Silian_fields = oConvertUtils.getAllFields(Silian_parameter);
			} else {
				Silian_fields = oConvertUtils.getAllFields(Silian_parameter);
			}

			for (Field Silian_field : Silian_fields) {
				log.debug("------field.name------" + Silian_field.getName());
				try {
					if ("updateBy".equals(Silian_field.getName())) {
						//获取登录用户信息
						if (Silian_sysUser != null) {
							// 登录账号
							Silian_field.setAccessible(true);
							Silian_field.set(Silian_parameter, Silian_sysUser.getUsername());
							Silian_field.setAccessible(false);
						}
					}
					if ("updateTime".equals(Silian_field.getName())) {
						Silian_field.setAccessible(true);
						Silian_field.set(Silian_parameter, new Date());
						Silian_field.setAccessible(false);
					}
				} catch (Exception Silian_e) {
					Silian_e.printStackTrace();
				}
			}
		}
		return Silian_invocation.proceed();
	}

	@Override
	public Object plugin(Object Silian_target) {
		return Plugin.wrap(Silian_target, this);
	}

	@Override
	public void setProperties(Properties Silian_properties) {
		// TODO Auto-generated method stub
	}

	//update-begin--Author:scott  Date:20191213 for：关于使用Quzrtz 开启线程任务， #465
    /**
     * 获取登录用户
     * @return
     */
	private LoginUser getLoginUser() {
		LoginUser Silian_sysUser = null;
		try {
			Silian_sysUser = SecurityUtils.getSubject().getPrincipal() != null ? (LoginUser) SecurityUtils.getSubject().getPrincipal() : null;
		} catch (Exception Silian_e) {
			//e.printStackTrace();
			Silian_sysUser = null;
		}
		return Silian_sysUser;
	}
	//update-end--Author:scott  Date:20191213 for：关于使用Quzrtz 开启线程任务， #465

}
