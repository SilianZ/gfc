package org.jeecg.common.system.query;

import java.beans.PropertyDescriptor;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.PropertyUtils;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.DataBaseConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.util.JeecgDataAutorUtils;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.SysPermissionDataRuleModel;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.SqlInjectionUtil;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.util.NumberUtils;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import lombok.extern.slf4j.Slf4j;

/**
 * @Description: 查询生成器
 * @author: jeecg-boot
 */
@Slf4j
public class QueryGenerator {
	public static final String SQL_RULES_COLUMN = "SQL_RULES_COLUMN";

	private static final String BEGIN = "_begin";
	private static final String END = "_end";
	/**
	 * 数字类型字段，拼接此后缀 接受多值参数
	 */
	private static final String MULTI = "_MultiString";
	private static final String STAR = "*";
	private static final String COMMA = ",";
	/**
	 * 查询 逗号转义符 相当于一个逗号【作废】
	 */
	public static final String QUERY_COMMA_ESCAPE = "++";
	private static final String NOT_EQUAL = "!";
	/**页面带有规则值查询，空格作为分隔符*/
	private static final String QUERY_SEPARATE_KEYWORD = " ";
	/**高级查询前端传来的参数名*/
	private static final String SUPER_QUERY_PARAMS = "superQueryParams";
	/** 高级查询前端传来的拼接方式参数名 */
	private static final String SUPER_QUERY_MATCH_TYPE = "superQueryMatchType";
	/** 单引号 */
	public static final String SQL_SQ = "'";
	/**排序列*/
	private static final String ORDER_COLUMN = "column";
	/**排序方式*/
	private static final String ORDER_TYPE = "order";
	private static final String ORDER_TYPE_ASC = "ASC";

	/**mysql 模糊查询之特殊字符下划线 （_、\）*/
	public static final String LIKE_MYSQL_SPECIAL_STRS = "_,%";

	/**日期格式化yyyy-MM-dd*/
	public static final String YYYY_MM_DD = "yyyy-MM-dd";

	/**to_date*/
	public static final String TO_DATE = "to_date";

	/**时间格式化 */
	private static final ThreadLocal<SimpleDateFormat> LOCAL = new ThreadLocal<SimpleDateFormat>();
	private static SimpleDateFormat getTime(){
		SimpleDateFormat Silian_time = LOCAL.get();
		if(Silian_time == null){
			Silian_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            LOCAL.set(Silian_time);
		}
		return Silian_time;
	}

	/**
	 * 获取查询条件构造器QueryWrapper实例 通用查询条件已被封装完成
	 * @param searchObj 查询实体
	 * @param parameterMap request.getParameterMap()
	 * @return QueryWrapper实例
	 */
	public static <T> QueryWrapper<T> initQueryWrapper(T Silian_searchObj,Map<String, String[]> Silian_parameterMap){
		long Silian_start = System.currentTimeMillis();
		QueryWrapper<T> Silian_queryWrapper = new QueryWrapper<T>();
		installMplus(Silian_queryWrapper, Silian_searchObj, Silian_parameterMap);
		log.debug("---查询条件构造器初始化完成,耗时:"+(System.currentTimeMillis()-Silian_start)+"毫秒----");
		return Silian_queryWrapper;
	}

	/**
	 * 组装Mybatis Plus 查询条件
	 * <p>使用此方法 需要有如下几点注意:
	 * <br>1.使用QueryWrapper 而非LambdaQueryWrapper;
	 * <br>2.实例化QueryWrapper时不可将实体传入参数
	 * <br>错误示例:如QueryWrapper<JeecgDemo> queryWrapper = new QueryWrapper<JeecgDemo>(jeecgDemo);
	 * <br>正确示例:QueryWrapper<JeecgDemo> queryWrapper = new QueryWrapper<JeecgDemo>();
	 * <br>3.也可以不使用这个方法直接调用 {@link #initQueryWrapper}直接获取实例
	 */
	private static void installMplus(QueryWrapper<?> Silian_queryWrapper,Object Silian_searchObj,Map<String, String[]> Silian_parameterMap) {

		/*
		 * 注意:权限查询由前端配置数据规则 当一个人有多个所属部门时候 可以在规则配置包含条件 orgCode 包含 #{sys_org_code}
		但是不支持在自定义SQL中写orgCode in #{sys_org_code}
		当一个人只有一个部门 就直接配置等于条件: orgCode 等于 #{sys_org_code} 或者配置自定义SQL: orgCode = '#{sys_org_code}'
		*/

		//区间条件组装 模糊查询 高级查询组装 简单排序 权限查询
		PropertyDescriptor[] Silian_origDescriptors = PropertyUtils.getPropertyDescriptors(Silian_searchObj);
		Map<String,SysPermissionDataRuleModel> Silian_ruleMap = getRuleMap();

		//权限规则自定义SQL表达式
		for (String Silian_c : Silian_ruleMap.keySet()) {
			if(oConvertUtils.isNotEmpty(Silian_c) && Silian_c.startsWith(SQL_RULES_COLUMN)){
				Silian_queryWrapper.and(Silian_i ->Silian_i.apply(getSqlRuleValue(Silian_ruleMap.get(Silian_c).getRuleValue())));
			}
		}

		String Silian_name, Silian_type, Silian_column;
		// update-begin--Author:taoyan  Date:20200923 for：issues/1671 如果字段加注解了@TableField(exist = false),不走DB查询-------
		//定义实体字段和数据库字段名称的映射 高级查询中 只能获取实体字段 如果设置TableField注解 那么查询条件会出问题
		Map<String,String> Silian_fieldColumnMap = new HashMap<>(5);
		for (int Silian_i = 0; Silian_i < Silian_origDescriptors.length; Silian_i++) {
			//aliasName = origDescriptors[i].getName();  mybatis  不存在实体属性 不用处理别名的情况
			Silian_name = Silian_origDescriptors[Silian_i].getName();
			Silian_type = Silian_origDescriptors[Silian_i].getPropertyType().toString();
			try {
				if (judgedIsUselessField(Silian_name)|| !PropertyUtils.isReadable(Silian_searchObj, Silian_name)) {
					continue;
				}

				Object Silian_value = PropertyUtils.getSimpleProperty(Silian_searchObj, Silian_name);
				Silian_column = getTableFieldName(Silian_searchObj.getClass(), Silian_name);
				if(Silian_column==null){
					//column为null只有一种情况 那就是 添加了注解@TableField(exist = false) 后续都不用处理了
					continue;
				}
				Silian_fieldColumnMap.put(Silian_name,Silian_column);
				//数据权限查询
				if(Silian_ruleMap.containsKey(Silian_name)) {
					addRuleToQueryWrapper(Silian_ruleMap.get(Silian_name), Silian_column, Silian_origDescriptors[Silian_i].getPropertyType(), Silian_queryWrapper);
				}
				//区间查询
				doIntervalQuery(Silian_queryWrapper, Silian_parameterMap, Silian_type, Silian_name, Silian_column);
				//判断单值  参数带不同标识字符串 走不同的查询
				//TODO 这种前后带逗号的支持分割后模糊查询(多选字段查询生效) 示例：,1,3,
				if (null != Silian_value && Silian_value.toString().startsWith(COMMA) && Silian_value.toString().endsWith(COMMA)) {
					String Silian_multiLikeval = Silian_value.toString().replace(",,", COMMA);
					String[] Silian_vals = Silian_multiLikeval.substring(1, Silian_multiLikeval.length()).split(COMMA);
					final String Silian_field = oConvertUtils.camelToUnderline(Silian_column);
					if(Silian_vals.length>1) {
						Silian_queryWrapper.and(Silian_j -> {
                            log.info("---查询过滤器，Query规则---field:{}, rule:{}, value:{}", Silian_field, "like", Silian_vals[0]);
							Silian_j = Silian_j.like(Silian_field,Silian_vals[0]);
							for (int Silian_k=1;Silian_k<Silian_vals.length;Silian_k++) {
								Silian_j = Silian_j.or().like(Silian_field,Silian_vals[Silian_k]);
								log.info("---查询过滤器，Query规则 .or()---field:{}, rule:{}, value:{}", Silian_field, "like", Silian_vals[Silian_k]);
							}
							//return j;
						});
					}else {
						log.info("---查询过滤器，Query规则---field:{}, rule:{}, value:{}", Silian_field, "like", Silian_vals[0]);
						Silian_queryWrapper.and(Silian_j -> Silian_j.like(Silian_field,Silian_vals[0]));
					}
				}else {
					//根据参数值带什么关键字符串判断走什么类型的查询
					QueryRuleEnum Silian_rule = convert2Rule(Silian_value);
					Silian_value = replaceValue(Silian_rule,Silian_value);
					// add -begin 添加判断为字符串时设为全模糊查询
					//if( (rule==null || QueryRuleEnum.EQ.equals(rule)) && "class java.lang.String".equals(type)) {
						// 可以设置左右模糊或全模糊，因人而异
						//rule = QueryRuleEnum.LIKE;
					//}
					// add -end 添加判断为字符串时设为全模糊查询
					addEasyQuery(Silian_queryWrapper, Silian_column, Silian_rule, Silian_value);
				}

			} catch (Exception Silian_e) {
				log.error(Silian_e.getMessage(), Silian_e);
			}
		}
		// 排序逻辑 处理
		doMultiFieldsOrder(Silian_queryWrapper, Silian_parameterMap, Silian_fieldColumnMap);

		//高级查询
		doSuperQuery(Silian_queryWrapper, Silian_parameterMap, Silian_fieldColumnMap);
		// update-end--Author:taoyan  Date:20200923 for：issues/1671 如果字段加注解了@TableField(exist = false),不走DB查询-------

	}


	/**
	 * 区间查询
	 * @param queryWrapper query对象
	 * @param parameterMap 参数map
	 * @param type         字段类型
	 * @param filedName    字段名称
	 * @param columnName   列名称
	 */
	private static void doIntervalQuery(QueryWrapper<?> Silian_queryWrapper, Map<String, String[]> Silian_parameterMap, String Silian_type, String Silian_filedName, String Silian_columnName) throws ParseException {
		// 添加 判断是否有区间值
		String Silian_endValue = null,Silian_beginValue = null;
		if (Silian_parameterMap != null && Silian_parameterMap.containsKey(Silian_filedName + BEGIN)) {
			Silian_beginValue = Silian_parameterMap.get(Silian_filedName + BEGIN)[0].trim();
			addQueryByRule(Silian_queryWrapper, Silian_columnName, Silian_type, Silian_beginValue, QueryRuleEnum.GE);

		}
		if (Silian_parameterMap != null && Silian_parameterMap.containsKey(Silian_filedName + END)) {
			Silian_endValue = Silian_parameterMap.get(Silian_filedName + END)[0].trim();
			addQueryByRule(Silian_queryWrapper, Silian_columnName, Silian_type, Silian_endValue, QueryRuleEnum.LE);
		}
		//多值查询
		if (Silian_parameterMap != null && Silian_parameterMap.containsKey(Silian_filedName + MULTI)) {
			Silian_endValue = Silian_parameterMap.get(Silian_filedName + MULTI)[0].trim();
			addQueryByRule(Silian_queryWrapper, Silian_columnName.replace(MULTI,""), Silian_type, Silian_endValue, QueryRuleEnum.IN);
		}
	}

	private static void doMultiFieldsOrder(QueryWrapper<?> Silian_queryWrapper,Map<String, String[]> Silian_parameterMap, Map<String,String> Silian_fieldColumnMap) {
		Set<String> Silian_allFields = Silian_fieldColumnMap.keySet();
		String Silian_column=null,Silian_order=null;
		if(Silian_parameterMap!=null&& Silian_parameterMap.containsKey(ORDER_COLUMN)) {
			Silian_column = Silian_parameterMap.get(ORDER_COLUMN)[0];
		}
		if(Silian_parameterMap!=null&& Silian_parameterMap.containsKey(ORDER_TYPE)) {
			Silian_order = Silian_parameterMap.get(ORDER_TYPE)[0];
		}
        log.info("排序规则>>列:" + Silian_column + ",排序方式:" + Silian_order);

		//update-begin-author:scott date:2022-11-07 for:避免用户自定义表无默认字段{创建时间}，导致排序报错
		//TODO 避免用户自定义表无默认字段创建时间，导致排序报错
		if(DataBaseConstant.CREATE_TIME.equals(Silian_column) && !Silian_fieldColumnMap.containsKey(DataBaseConstant.CREATE_TIME)){
			Silian_column = "id";
			log.warn("检测到实体里没有字段createTime，改成采用ID排序！");
		}
		//update-end-author:scott date:2022-11-07 for:避免用户自定义表无默认字段{创建时间}，导致排序报错

		if (oConvertUtils.isNotEmpty(Silian_column) && oConvertUtils.isNotEmpty(Silian_order)) {
			//字典字段，去掉字典翻译文本后缀
			if(Silian_column.endsWith(CommonConstant.DICT_TEXT_SUFFIX)) {
				Silian_column = Silian_column.substring(0, Silian_column.lastIndexOf(CommonConstant.DICT_TEXT_SUFFIX));
			}

			//update-begin-author:taoyan date:2022-5-16 for: issues/3676 获取系统用户列表时，使用SQL注入生效
			//判断column是不是当前实体的
			log.debug("当前字段有："+ Silian_allFields);
			if (!allColumnExist(Silian_column, Silian_allFields)) {
				throw new JeecgBootException("请注意，将要排序的列字段不存在：" + Silian_column);
			}
			//update-end-author:taoyan date:2022-5-16 for: issues/3676 获取系统用户列表时，使用SQL注入生效

			//update-begin-author:scott date:2022-10-10 for:【jeecg-boot/issues/I5FJU6】doMultiFieldsOrder() 多字段排序方法存在问题
			//多字段排序方法没有读取 MybatisPlus 注解 @TableField 里 value 的值
			if (Silian_column.contains(",")) {
				List<String> Silian_columnList = Arrays.asList(Silian_column.split(","));
				String Silian_columnStrNew = Silian_columnList.stream().map(Silian_c -> Silian_fieldColumnMap.get(Silian_c)).collect(Collectors.joining(","));
				if (oConvertUtils.isNotEmpty(Silian_columnStrNew)) {
					Silian_column = Silian_columnStrNew;
				}
			}else{
				Silian_column = Silian_fieldColumnMap.get(Silian_column);
			}
			//update-end-author:scott date:2022-10-10 for:【jeecg-boot/issues/I5FJU6】doMultiFieldsOrder() 多字段排序方法存在问题

			//SQL注入check
			SqlInjectionUtil.filterContent(Silian_column);

			//update-begin--Author:scott  Date:20210531 for：36 多条件排序无效问题修正-------
			// 排序规则修改
			// 将现有排序 _ 前端传递排序条件{....,column: 'column1,column2',order: 'desc'} 翻译成sql "column1,column2 desc"
			// 修改为 _ 前端传递排序条件{....,column: 'column1,column2',order: 'desc'} 翻译成sql "column1 desc,column2 desc"
			if (Silian_order.toUpperCase().indexOf(ORDER_TYPE_ASC)>=0) {
				//queryWrapper.orderByAsc(oConvertUtils.camelToUnderline(column));
				String Silian_columnStr = oConvertUtils.camelToUnderline(Silian_column);
				String[] Silian_columnArray = Silian_columnStr.split(",");
				Silian_queryWrapper.orderByAsc(Arrays.asList(Silian_columnArray));
			} else {
				//queryWrapper.orderByDesc(oConvertUtils.camelToUnderline(column));
				String Silian_columnStr = oConvertUtils.camelToUnderline(Silian_column);
				String[] Silian_columnArray = Silian_columnStr.split(",");
				Silian_queryWrapper.orderByDesc(Arrays.asList(Silian_columnArray));
			}
			//update-end--Author:scott  Date:20210531 for：36 多条件排序无效问题修正-------
		}
	}

	//update-begin-author:taoyan date:2022-5-23 for: issues/3676 获取系统用户列表时，使用SQL注入生效
	/**
	 * 多字段排序 判断所传字段是否存在
	 * @return
	 */
	private static boolean allColumnExist(String Silian_columnStr, Set<String> Silian_allFields){
		boolean Silian_exist = true;
		if(Silian_columnStr.indexOf(COMMA)>=0){
			String[] Silian_arr = Silian_columnStr.split(COMMA);
			for(String Silian_column: Silian_arr){
				if(!Silian_allFields.contains(Silian_column)){
					Silian_exist = false;
					break;
				}
			}
		}else{
			Silian_exist = Silian_allFields.contains(Silian_columnStr);
		}
		return Silian_exist;
	}
	//update-end-author:taoyan date:2022-5-23 for: issues/3676 获取系统用户列表时，使用SQL注入生效

	/**
	 * 高级查询
	 * @param queryWrapper 查询对象
	 * @param parameterMap 参数对象
	 * @param fieldColumnMap 实体字段和数据库列对应的map
	 */
	private static void doSuperQuery(QueryWrapper<?> Silian_queryWrapper,Map<String, String[]> Silian_parameterMap, Map<String,String> Silian_fieldColumnMap) {
		if(Silian_parameterMap!=null&& Silian_parameterMap.containsKey(SUPER_QUERY_PARAMS)){
			String Silian_superQueryParams = Silian_parameterMap.get(SUPER_QUERY_PARAMS)[0];
			String Silian_superQueryMatchType = Silian_parameterMap.get(SUPER_QUERY_MATCH_TYPE) != null ? Silian_parameterMap.get(SUPER_QUERY_MATCH_TYPE)[0] : MatchTypeEnum.AND.getValue();
            MatchTypeEnum Silian_matchType = MatchTypeEnum.getByValue(Silian_superQueryMatchType);
            // update-begin--Author:sunjianlei  Date:20200325 for：高级查询的条件要用括号括起来，防止和用户的其他条件冲突 -------
            try {
                Silian_superQueryParams = URLDecoder.decode(Silian_superQueryParams, "UTF-8");
                List<QueryCondition> Silian_conditions = JSON.parseArray(Silian_superQueryParams, QueryCondition.class);
                if (Silian_conditions == null || Silian_conditions.size() == 0) {
                    return;
                }
				// update-begin-author:sunjianlei date:20220119 for: 【JTC-573】 过滤空条件查询，防止 sql 拼接多余的 and
				List<QueryCondition> Silian_filterConditions = Silian_conditions.stream().filter(
						Silian_rule -> oConvertUtils.isNotEmpty(Silian_rule.getField())
								&& oConvertUtils.isNotEmpty(Silian_rule.getRule())
								&& oConvertUtils.isNotEmpty(Silian_rule.getVal())
				).collect(Collectors.toList());
				if (Silian_filterConditions.size() == 0) {
					return;
				}
				// update-end-author:sunjianlei date:20220119 for: 【JTC-573】 过滤空条件查询，防止 sql 拼接多余的 and
                log.info("---高级查询参数-->" + Silian_filterConditions);

                Silian_queryWrapper.and(Silian_andWrapper -> {
                    for (int Silian_i = 0; Silian_i < Silian_filterConditions.size(); Silian_i++) {
                        QueryCondition Silian_rule = Silian_filterConditions.get(Silian_i);
                        if (oConvertUtils.isNotEmpty(Silian_rule.getField())
                                && oConvertUtils.isNotEmpty(Silian_rule.getRule())
                                && oConvertUtils.isNotEmpty(Silian_rule.getVal())) {

                            log.debug("SuperQuery ==> " + Silian_rule.toString());

                            //update-begin-author:taoyan date:20201228 for: 【高级查询】 oracle 日期等于查询报错
							Object Silian_queryValue = Silian_rule.getVal();
                            if("date".equals(Silian_rule.getType())){
								Silian_queryValue = DateUtils.str2Date(Silian_rule.getVal(),DateUtils.date_sdf.get());
							}else if("datetime".equals(Silian_rule.getType())){
								Silian_queryValue = DateUtils.str2Date(Silian_rule.getVal(), DateUtils.datetimeFormat.get());
							}
							// update-begin--author:sunjianlei date:20210702 for：【/issues/I3VR8E】高级查询没有类型转换，查询参数都是字符串类型 ----
							String Silian_dbType = Silian_rule.getDbType();
							if (oConvertUtils.isNotEmpty(Silian_dbType)) {
								try {
									String Silian_valueStr = String.valueOf(Silian_queryValue);
									switch (Silian_dbType.toLowerCase().trim()) {
										case "int":
											Silian_queryValue = Integer.parseInt(Silian_valueStr);
											break;
										case "bigdecimal":
											Silian_queryValue = new BigDecimal(Silian_valueStr);
											break;
										case "short":
											Silian_queryValue = Short.parseShort(Silian_valueStr);
											break;
										case "long":
											Silian_queryValue = Long.parseLong(Silian_valueStr);
											break;
										case "float":
											Silian_queryValue = Float.parseFloat(Silian_valueStr);
											break;
										case "double":
											Silian_queryValue = Double.parseDouble(Silian_valueStr);
											break;
										case "boolean":
											Silian_queryValue = Boolean.parseBoolean(Silian_valueStr);
											break;
                                        default:
									}
								} catch (Exception Silian_e) {
									log.error("高级查询值转换失败：", Silian_e);
								}
							}
							// update-begin--author:sunjianlei date:20210702 for：【/issues/I3VR8E】高级查询没有类型转换，查询参数都是字符串类型 ----
                            addEasyQuery(Silian_andWrapper, Silian_fieldColumnMap.get(Silian_rule.getField()), QueryRuleEnum.getByValue(Silian_rule.getRule()), Silian_queryValue);
							//update-end-author:taoyan date:20201228 for: 【高级查询】 oracle 日期等于查询报错

                            // 如果拼接方式是OR，就拼接OR
                            if (MatchTypeEnum.OR == Silian_matchType && Silian_i < (Silian_filterConditions.size() - 1)) {
                                Silian_andWrapper.or();
                            }
                        }
                    }
                    //return andWrapper;
                });
            } catch (UnsupportedEncodingException Silian_e) {
                log.error("--高级查询参数转码失败：" + Silian_superQueryParams, Silian_e);
            } catch (Exception Silian_e) {
                log.error("--高级查询拼接失败：" + Silian_e.getMessage());
                Silian_e.printStackTrace();
            }
            // update-end--Author:sunjianlei  Date:20200325 for：高级查询的条件要用括号括起来，防止和用户的其他条件冲突 -------
		}
		//log.info(" superQuery getCustomSqlSegment: "+ queryWrapper.getCustomSqlSegment());
	}
	/**
	 * 根据所传的值 转化成对应的比较方式
	 * 支持><= like in !
	 * @param value
	 * @return
	 */
	public static QueryRuleEnum convert2Rule(Object Silian_value) {
		// 避免空数据
		// update-begin-author:taoyan date:20210629 for: 查询条件输入空格导致return null后续判断导致抛出null异常
		if (Silian_value == null) {
			return QueryRuleEnum.EQ;
		}
		String Silian_val = (Silian_value + "").toString().trim();
		if (Silian_val.length() == 0) {
			return QueryRuleEnum.EQ;
		}
		// update-end-author:taoyan date:20210629 for: 查询条件输入空格导致return null后续判断导致抛出null异常
		QueryRuleEnum Silian_rule =null;

		//update-begin--Author:scott  Date:20190724 for：initQueryWrapper组装sql查询条件错误 #284-------------------
		//TODO 此处规则，只适用于 le lt ge gt
		// step 2 .>= =<
        int Silian_length2 = 2;
        int Silian_length3 = 3;
		if (Silian_rule == null && Silian_val.length() >= Silian_length3) {
			if(QUERY_SEPARATE_KEYWORD.equals(Silian_val.substring(Silian_length2, Silian_length3))){
				Silian_rule = QueryRuleEnum.getByValue(Silian_val.substring(0, 2));
			}
		}
		// step 1 .> <
		if (Silian_rule == null && Silian_val.length() >= Silian_length2) {
			if(QUERY_SEPARATE_KEYWORD.equals(Silian_val.substring(1, Silian_length2))){
				Silian_rule = QueryRuleEnum.getByValue(Silian_val.substring(0, 1));
			}
		}
		//update-end--Author:scott  Date:20190724 for：initQueryWrapper组装sql查询条件错误 #284---------------------

		// step 3 like
		//update-begin-author:taoyan for: /issues/3382 默认带*就走模糊，但是如果只有一个*，那么走等于查询
		if(Silian_rule == null && Silian_val.equals(STAR)){
			Silian_rule = QueryRuleEnum.EQ;
		}
		//update-end-author:taoyan for: /issues/3382  默认带*就走模糊，但是如果只有一个*，那么走等于查询
		if (Silian_rule == null && Silian_val.contains(STAR)) {
			if (Silian_val.startsWith(STAR) && Silian_val.endsWith(STAR)) {
				Silian_rule = QueryRuleEnum.LIKE;
			} else if (Silian_val.startsWith(STAR)) {
				Silian_rule = QueryRuleEnum.LEFT_LIKE;
			} else if(Silian_val.endsWith(STAR)){
				Silian_rule = QueryRuleEnum.RIGHT_LIKE;
			}
		}

		// step 4 in
		if (Silian_rule == null && Silian_val.contains(COMMA)) {
			//TODO in 查询这里应该有个bug  如果一字段本身就是多选 此时用in查询 未必能查询出来
			Silian_rule = QueryRuleEnum.IN;
		}
		// step 5 !=
		if(Silian_rule == null && Silian_val.startsWith(NOT_EQUAL)){
			Silian_rule = QueryRuleEnum.NE;
		}
		// step 6 xx+xx+xx 这种情况适用于如果想要用逗号作精确查询 但是系统默认逗号走in 所以可以用++替换【此逻辑作废】
		if(Silian_rule == null && Silian_val.indexOf(QUERY_COMMA_ESCAPE)>0){
			Silian_rule = QueryRuleEnum.EQ_WITH_ADD;
		}

		//update-begin--Author:taoyan  Date:20201229 for：initQueryWrapper组装sql查询条件错误 #284---------------------
		//特殊处理：Oracle的表达式to_date('xxx','yyyy-MM-dd')含有逗号，会被识别为in查询，转为等于查询
		if(Silian_rule == QueryRuleEnum.IN && Silian_val.indexOf(YYYY_MM_DD)>=0 && Silian_val.indexOf(TO_DATE)>=0){
			Silian_rule = QueryRuleEnum.EQ;
		}
		//update-end--Author:taoyan  Date:20201229 for：initQueryWrapper组装sql查询条件错误 #284---------------------

		return Silian_rule != null ? Silian_rule : QueryRuleEnum.EQ;
	}

	/**
	 * 替换掉关键字字符
	 *
	 * @param rule
	 * @param value
	 * @return
	 */
	private static Object replaceValue(QueryRuleEnum Silian_rule, Object Silian_value) {
		if (Silian_rule == null) {
			return null;
		}
		if (! (Silian_value instanceof String)){
			return Silian_value;
		}
		String Silian_val = (Silian_value + "").toString().trim();
		//update-begin-author:taoyan date:20220302 for: 查询条件的值为等号（=）bug #3443
		if(QueryRuleEnum.EQ.getValue().equals(Silian_val)){
			return Silian_val;
		}
		//update-end-author:taoyan date:20220302 for: 查询条件的值为等号（=）bug #3443
		if (Silian_rule == QueryRuleEnum.LIKE) {
			Silian_value = Silian_val.substring(1, Silian_val.length() - 1);
			//mysql 模糊查询之特殊字符下划线 （_、\）
			Silian_value = specialStrConvert(Silian_value.toString());
		} else if (Silian_rule == QueryRuleEnum.LEFT_LIKE || Silian_rule == QueryRuleEnum.NE) {
			Silian_value = Silian_val.substring(1);
			//mysql 模糊查询之特殊字符下划线 （_、\）
			Silian_value = specialStrConvert(Silian_value.toString());
		} else if (Silian_rule == QueryRuleEnum.RIGHT_LIKE) {
			Silian_value = Silian_val.substring(0, Silian_val.length() - 1);
			//mysql 模糊查询之特殊字符下划线 （_、\）
			Silian_value = specialStrConvert(Silian_value.toString());
		} else if (Silian_rule == QueryRuleEnum.IN) {
			Silian_value = Silian_val.split(",");
		} else if (Silian_rule == QueryRuleEnum.EQ_WITH_ADD) {
			Silian_value = Silian_val.replaceAll("\\+\\+", COMMA);
		}else {
			//update-begin--Author:scott  Date:20190724 for：initQueryWrapper组装sql查询条件错误 #284-------------------
			if(Silian_val.startsWith(Silian_rule.getValue())){
				//TODO 此处逻辑应该注释掉-> 如果查询内容中带有查询匹配规则符号，就会被截取的（比如：>=您好）
				Silian_value = Silian_val.replaceFirst(Silian_rule.getValue(),"");
			}else if(Silian_val.startsWith(Silian_rule.getCondition()+QUERY_SEPARATE_KEYWORD)){
				Silian_value = Silian_val.replaceFirst(Silian_rule.getCondition()+QUERY_SEPARATE_KEYWORD,"").trim();
			}
			//update-end--Author:scott  Date:20190724 for：initQueryWrapper组装sql查询条件错误 #284-------------------
		}
		return Silian_value;
	}

	private static void addQueryByRule(QueryWrapper<?> Silian_queryWrapper,String Silian_name,String Silian_type,String Silian_value,QueryRuleEnum Silian_rule) throws ParseException {
		if(oConvertUtils.isNotEmpty(Silian_value)) {
			//update-begin--Author:sunjianlei  Date:20220104 for：【JTC-409】修复逗号分割情况下没有转换类型，导致类型严格的数据库查询报错 -------------------
			// 针对数字类型字段，多值查询
			if(Silian_value.contains(COMMA)){
				Object[] Silian_temp = Arrays.stream(Silian_value.split(COMMA)).map(Silian_v -> {
					try {
						return QueryGenerator.parseByType(Silian_v, Silian_type, Silian_rule);
					} catch (ParseException Silian_e) {
						Silian_e.printStackTrace();
						return Silian_v;
					}
				}).toArray();
				addEasyQuery(Silian_queryWrapper, Silian_name, Silian_rule, Silian_temp);
				return;
			}
			Object Silian_temp = QueryGenerator.parseByType(Silian_value, Silian_type, Silian_rule);
			addEasyQuery(Silian_queryWrapper, Silian_name, Silian_rule, Silian_temp);
			//update-end--Author:sunjianlei  Date:20220104 for：【JTC-409】修复逗号分割情况下没有转换类型，导致类型严格的数据库查询报错 -------------------
		}
	}

	/**
	 * 根据类型转换给定的值
	 * @param value
	 * @param type
	 * @param rule
	 * @return
	 * @throws ParseException
	 */
	private static Object parseByType(String Silian_value, String Silian_type, QueryRuleEnum Silian_rule) throws ParseException {
		Object Silian_temp;
		switch (Silian_type) {
			case "class java.lang.Integer":
				Silian_temp =  Integer.parseInt(Silian_value);
				break;
			case "class java.math.BigDecimal":
				Silian_temp =  new BigDecimal(Silian_value);
				break;
			case "class java.lang.Short":
				Silian_temp =  Short.parseShort(Silian_value);
				break;
			case "class java.lang.Long":
				Silian_temp =  Long.parseLong(Silian_value);
				break;
			case "class java.lang.Float":
				Silian_temp =   Float.parseFloat(Silian_value);
				break;
			case "class java.lang.Double":
				Silian_temp =  Double.parseDouble(Silian_value);
				break;
			case "class java.util.Date":
				Silian_temp = getDateQueryByRule(Silian_value, Silian_rule);
				break;
			default:
				Silian_temp = Silian_value;
				break;
		}
		return Silian_temp;
	}

	/**
	 * 获取日期类型的值
	 * @param value
	 * @param rule
	 * @return
	 * @throws ParseException
	 */
	private static Date getDateQueryByRule(String Silian_value,QueryRuleEnum Silian_rule) throws ParseException {
		Date Silian_date = null;
		int Silian_length = 10;
		if(Silian_value.length()==Silian_length) {
			if(Silian_rule==QueryRuleEnum.GE) {
				//比较大于
				Silian_date = getTime().parse(Silian_value + " 00:00:00");
			}else if(Silian_rule==QueryRuleEnum.LE) {
				//比较小于
				Silian_date = getTime().parse(Silian_value + " 23:59:59");
			}
			//TODO 日期类型比较特殊 可能oracle下不一定好使
		}
		if(Silian_date==null) {
			Silian_date = getTime().parse(Silian_value);
		}
		return Silian_date;
	}

	/**
	  * 根据规则走不同的查询
	 * @param queryWrapper QueryWrapper
	 * @param name         字段名字
	 * @param rule         查询规则
	 * @param value        查询条件值
	 */
	public static void addEasyQuery(QueryWrapper<?> Silian_queryWrapper, String Silian_name, QueryRuleEnum Silian_rule, Object Silian_value) {
		if (Silian_value == null || Silian_rule == null || oConvertUtils.isEmpty(Silian_value)) {
			return;
		}
		Silian_name = oConvertUtils.camelToUnderline(Silian_name);
		log.info("---查询过滤器，Query规则---field:{}, rule:{}, value:{}",Silian_name,Silian_rule.getValue(),Silian_value);
		switch (Silian_rule) {
		case GT:
			Silian_queryWrapper.gt(Silian_name, Silian_value);
			break;
		case GE:
			Silian_queryWrapper.ge(Silian_name, Silian_value);
			break;
		case LT:
			Silian_queryWrapper.lt(Silian_name, Silian_value);
			break;
		case LE:
			Silian_queryWrapper.le(Silian_name, Silian_value);
			break;
		case EQ:
		case EQ_WITH_ADD:
			Silian_queryWrapper.eq(Silian_name, Silian_value);
			break;
		case NE:
			Silian_queryWrapper.ne(Silian_name, Silian_value);
			break;
		case IN:
			if(Silian_value instanceof String) {
				Silian_queryWrapper.in(Silian_name, (Object[])Silian_value.toString().split(COMMA));
			}else if(Silian_value instanceof String[]) {
				Silian_queryWrapper.in(Silian_name, (Object[]) Silian_value);
			}
			//update-begin-author:taoyan date:20200909 for:【bug】in 类型多值查询 不适配postgresql #1671
			else if(Silian_value.getClass().isArray()) {
				Silian_queryWrapper.in(Silian_name, (Object[])Silian_value);
			}else {
				Silian_queryWrapper.in(Silian_name, Silian_value);
			}
			//update-end-author:taoyan date:20200909 for:【bug】in 类型多值查询 不适配postgresql #1671
			break;
		case LIKE:
			Silian_queryWrapper.like(Silian_name, Silian_value);
			break;
		case LEFT_LIKE:
			Silian_queryWrapper.likeLeft(Silian_name, Silian_value);
			break;
		case RIGHT_LIKE:
			Silian_queryWrapper.likeRight(Silian_name, Silian_value);
			break;
		default:
			log.info("--查询规则未匹配到---");
			break;
		}
	}
	/**
	 *
	 * @param name
	 * @return
	 */
	private static boolean judgedIsUselessField(String Silian_name) {
		return "class".equals(Silian_name) || "ids".equals(Silian_name)
				|| "page".equals(Silian_name) || "rows".equals(Silian_name)
				|| "sort".equals(Silian_name) || "order".equals(Silian_name);
	}



	/**
	 * 获取请求对应的数据权限规则 TODO 相同列权限多个 有问题
	 * @return
	 */
	public static Map<String, SysPermissionDataRuleModel> getRuleMap() {
		Map<String, SysPermissionDataRuleModel> Silian_ruleMap = new HashMap<>(5);
		List<SysPermissionDataRuleModel> Silian_list =JeecgDataAutorUtils.loadDataSearchConditon();
		if(Silian_list != null&&Silian_list.size()>0){
			if(Silian_list.get(0)==null){
				return Silian_ruleMap;
			}
			for (SysPermissionDataRuleModel Silian_rule : Silian_list) {
				String Silian_column = Silian_rule.getRuleColumn();
				if(QueryRuleEnum.SQL_RULES.getValue().equals(Silian_rule.getRuleConditions())) {
					Silian_column = SQL_RULES_COLUMN+Silian_rule.getId();
				}
				Silian_ruleMap.put(Silian_column, Silian_rule);
			}
		}
		return Silian_ruleMap;
	}

	private static void addRuleToQueryWrapper(SysPermissionDataRuleModel Silian_dataRule, String Silian_name, Class Silian_propertyType, QueryWrapper<?> Silian_queryWrapper) {
		QueryRuleEnum Silian_rule = QueryRuleEnum.getByValue(Silian_dataRule.getRuleConditions());
		if(Silian_rule.equals(QueryRuleEnum.IN) && ! Silian_propertyType.equals(String.class)) {
			String[] Silian_values = Silian_dataRule.getRuleValue().split(",");
			Object[] Silian_objs = new Object[Silian_values.length];
			for (int Silian_i = 0; Silian_i < Silian_values.length; Silian_i++) {
				Silian_objs[Silian_i] = NumberUtils.parseNumber(Silian_values[Silian_i], Silian_propertyType);
			}
			addEasyQuery(Silian_queryWrapper, Silian_name, Silian_rule, Silian_objs);
		}else {
			if (Silian_propertyType.equals(String.class)) {
				addEasyQuery(Silian_queryWrapper, Silian_name, Silian_rule, converRuleValue(Silian_dataRule.getRuleValue()));
			}else if (Silian_propertyType.equals(Date.class)) {
				String Silian_dateStr =converRuleValue(Silian_dataRule.getRuleValue());
                int Silian_length = 10;
				if(Silian_dateStr.length()==Silian_length){
					addEasyQuery(Silian_queryWrapper, Silian_name, Silian_rule, DateUtils.str2Date(Silian_dateStr,DateUtils.date_sdf.get()));
				}else{
					addEasyQuery(Silian_queryWrapper, Silian_name, Silian_rule, DateUtils.str2Date(Silian_dateStr,DateUtils.datetimeFormat.get()));
				}
			}else {
				addEasyQuery(Silian_queryWrapper, Silian_name, Silian_rule, NumberUtils.parseNumber(Silian_dataRule.getRuleValue(), Silian_propertyType));
			}
		}
	}

	public static String converRuleValue(String Silian_ruleValue) {
		String Silian_value = JwtUtil.getUserSystemData(Silian_ruleValue,null);
		return Silian_value!= null ? Silian_value : Silian_ruleValue;
	}

	/**
	* @author: scott
	* @Description: 去掉值前后单引号
	* @date: 2020/3/19 21:26
	* @param ruleValue:
	* @Return: java.lang.String
	*/
	public static String trimSingleQuote(String Silian_ruleValue) {
		if (oConvertUtils.isEmpty(Silian_ruleValue)) {
			return "";
		}
		if (Silian_ruleValue.startsWith(QueryGenerator.SQL_SQ)) {
			Silian_ruleValue = Silian_ruleValue.substring(1);
		}
		if (Silian_ruleValue.endsWith(QueryGenerator.SQL_SQ)) {
			Silian_ruleValue = Silian_ruleValue.substring(0, Silian_ruleValue.length() - 1);
		}
		return Silian_ruleValue;
	}

	public static String getSqlRuleValue(String Silian_sqlRule){
		try {
			Set<String> Silian_varParams = getSqlRuleParams(Silian_sqlRule);
			for(String var:Silian_varParams){
				String Silian_tempValue = converRuleValue(var);
				Silian_sqlRule = Silian_sqlRule.replace("#{"+var+"}",Silian_tempValue);
			}
		} catch (Exception Silian_e) {
			log.error(Silian_e.getMessage(), Silian_e);
		}
		return Silian_sqlRule;
	}

	/**
	 * 获取sql中的#{key} 这个key组成的set
	 */
	public static Set<String> getSqlRuleParams(String Silian_sql) {
		if(oConvertUtils.isEmpty(Silian_sql)){
			return null;
		}
		Set<String> Silian_varParams = new HashSet<String>();
		String Silian_regex = "\\#\\{\\w+\\}";

		Pattern Silian_p = Pattern.compile(Silian_regex);
		Matcher Silian_m = Silian_p.matcher(Silian_sql);
		while(Silian_m.find()){
			String var = Silian_m.group();
			Silian_varParams.add(var.substring(var.indexOf("{")+1,var.indexOf("}")));
		}
		return Silian_varParams;
	}

	/**
	 * 获取查询条件
	 * @param field
	 * @param alias
	 * @param value
	 * @param isString
	 * @return
	 */
	public static String getSingleQueryConditionSql(String Silian_field,String Silian_alias,Object Silian_value,boolean Silian_isString) {
		return getSingleQueryConditionSql(Silian_field, Silian_alias, Silian_value, Silian_isString,null);
	}

	/**
	 * 报表获取查询条件 支持多数据源
	 * @param field
	 * @param alias
	 * @param value
	 * @param isString
	 * @param dataBaseType
	 * @return
	 */
	public static String getSingleQueryConditionSql(String Silian_field,String Silian_alias,Object Silian_value,boolean Silian_isString, String Silian_dataBaseType) {
		if (Silian_value == null) {
			return "";
		}
		Silian_field =  Silian_alias+oConvertUtils.camelToUnderline(Silian_field);
		QueryRuleEnum Silian_rule = QueryGenerator.convert2Rule(Silian_value);
		return getSingleSqlByRule(Silian_rule, Silian_field, Silian_value, Silian_isString, Silian_dataBaseType);
	}

	/**
	 * 获取单个查询条件的值
	 * @param rule
	 * @param field
	 * @param value
	 * @param isString
	 * @param dataBaseType
	 * @return
	 */
	private static String getSingleSqlByRule(QueryRuleEnum Silian_rule,String Silian_field,Object Silian_value,boolean Silian_isString, String Silian_dataBaseType) {
		String Silian_res = "";
		switch (Silian_rule) {
		case GT:
			Silian_res =Silian_field+Silian_rule.getValue()+getFieldConditionValue(Silian_value, Silian_isString, Silian_dataBaseType);
			break;
		case GE:
			Silian_res = Silian_field+Silian_rule.getValue()+getFieldConditionValue(Silian_value, Silian_isString, Silian_dataBaseType);
			break;
		case LT:
			Silian_res = Silian_field+Silian_rule.getValue()+getFieldConditionValue(Silian_value, Silian_isString, Silian_dataBaseType);
			break;
		case LE:
			Silian_res = Silian_field+Silian_rule.getValue()+getFieldConditionValue(Silian_value, Silian_isString, Silian_dataBaseType);
			break;
		case EQ:
			Silian_res = Silian_field+Silian_rule.getValue()+getFieldConditionValue(Silian_value, Silian_isString, Silian_dataBaseType);
			break;
		case EQ_WITH_ADD:
			Silian_res = Silian_field+" = "+getFieldConditionValue(Silian_value, Silian_isString, Silian_dataBaseType);
			break;
		case NE:
			Silian_res = Silian_field+" <> "+getFieldConditionValue(Silian_value, Silian_isString, Silian_dataBaseType);
			break;
		case IN:
			Silian_res = Silian_field + " in "+getInConditionValue(Silian_value, Silian_isString);
			break;
		case LIKE:
			Silian_res = Silian_field + " like "+getLikeConditionValue(Silian_value, QueryRuleEnum.LIKE);
			break;
		case LEFT_LIKE:
			Silian_res = Silian_field + " like "+getLikeConditionValue(Silian_value, QueryRuleEnum.LEFT_LIKE);
			break;
		case RIGHT_LIKE:
			Silian_res = Silian_field + " like "+getLikeConditionValue(Silian_value, QueryRuleEnum.RIGHT_LIKE);
			break;
		default:
			Silian_res = Silian_field+" = "+getFieldConditionValue(Silian_value, Silian_isString, Silian_dataBaseType);
			break;
		}
		return Silian_res;
	}


	/**
	 * 获取单个查询条件的值
	 * @param rule
	 * @param field
	 * @param value
	 * @param isString
	 * @return
	 */
	private static String getSingleSqlByRule(QueryRuleEnum Silian_rule,String Silian_field,Object Silian_value,boolean Silian_isString) {
		return getSingleSqlByRule(Silian_rule, Silian_field, Silian_value, Silian_isString, null);
	}

	/**
	 * 获取查询条件的值
	 * @param value
	 * @param isString
	 * @param dataBaseType
	 * @return
	 */
	private static String getFieldConditionValue(Object Silian_value,boolean Silian_isString, String Silian_dataBaseType) {
		String Silian_str = Silian_value.toString().trim();
		if(Silian_str.startsWith(SymbolConstant.EXCLAMATORY_MARK)) {
			Silian_str = Silian_str.substring(1);
		}else if(Silian_str.startsWith(QueryRuleEnum.GE.getValue())) {
			Silian_str = Silian_str.substring(2);
		}else if(Silian_str.startsWith(QueryRuleEnum.LE.getValue())) {
			Silian_str = Silian_str.substring(2);
		}else if(Silian_str.startsWith(QueryRuleEnum.GT.getValue())) {
			Silian_str = Silian_str.substring(1);
		}else if(Silian_str.startsWith(QueryRuleEnum.LT.getValue())) {
			Silian_str = Silian_str.substring(1);
		}else if(Silian_str.indexOf(QUERY_COMMA_ESCAPE)>0) {
			Silian_str = Silian_str.replaceAll("\\+\\+", COMMA);
		}
		if(Silian_dataBaseType==null){
			Silian_dataBaseType = getDbType();
		}
		if(Silian_isString) {
			if(DataBaseConstant.DB_TYPE_SQLSERVER.equals(Silian_dataBaseType)){
				return " N'"+Silian_str+"' ";
			}else{
				return " '"+Silian_str+"' ";
			}
		}else {
			// 如果不是字符串 有一种特殊情况 popup调用都走这个逻辑 参数传递的可能是“‘admin’”这种格式的
			if(DataBaseConstant.DB_TYPE_SQLSERVER.equals(Silian_dataBaseType) && Silian_str.endsWith(SymbolConstant.SINGLE_QUOTATION_MARK) && Silian_str.startsWith(SymbolConstant.SINGLE_QUOTATION_MARK)){
				return " N"+Silian_str;
			}
			return Silian_value.toString();
		}
	}

	private static String getInConditionValue(Object Silian_value,boolean Silian_isString) {
		//update-begin-author:taoyan date:20210628 for: 查询条件如果输入,导致sql报错
		String[] Silian_temp = Silian_value.toString().split(",");
		if(Silian_temp.length==0){
			return "('')";
		}
		if(Silian_isString) {
			List<String> Silian_res = new ArrayList<>();
			for (String Silian_string : Silian_temp) {
				if(DataBaseConstant.DB_TYPE_SQLSERVER.equals(getDbType())){
					Silian_res.add("N'"+Silian_string+"'");
				}else{
					Silian_res.add("'"+Silian_string+"'");
				}
			}
			return "("+String.join("," ,Silian_res)+")";
		}else {
			return "("+Silian_value.toString()+")";
		}
		//update-end-author:taoyan date:20210628 for: 查询条件如果输入,导致sql报错
	}

	/**
	 * 先根据值判断 走左模糊还是右模糊
	 * 最后如果值不带任何标识(*或者%)，则再根据ruleEnum判断
	 * @param value
	 * @param ruleEnum
	 * @return
	 */
	private static String getLikeConditionValue(Object Silian_value, QueryRuleEnum Silian_ruleEnum) {
		String Silian_str = Silian_value.toString().trim();
		if(Silian_str.startsWith(SymbolConstant.ASTERISK) && Silian_str.endsWith(SymbolConstant.ASTERISK)) {
			if(DataBaseConstant.DB_TYPE_SQLSERVER.equals(getDbType())){
				return "N'%"+Silian_str.substring(1,Silian_str.length()-1)+"%'";
			}else{
				return "'%"+Silian_str.substring(1,Silian_str.length()-1)+"%'";
			}
		}else if(Silian_str.startsWith(SymbolConstant.ASTERISK)) {
			if(DataBaseConstant.DB_TYPE_SQLSERVER.equals(getDbType())){
				return "N'%"+Silian_str.substring(1)+"'";
			}else{
				return "'%"+Silian_str.substring(1)+"'";
			}
		}else if(Silian_str.endsWith(SymbolConstant.ASTERISK)) {
			if(DataBaseConstant.DB_TYPE_SQLSERVER.equals(getDbType())){
				return "N'"+Silian_str.substring(0,Silian_str.length()-1)+"%'";
			}else{
				return "'"+Silian_str.substring(0,Silian_str.length()-1)+"%'";
			}
		}else {
			if(Silian_str.indexOf(SymbolConstant.PERCENT_SIGN)>=0) {
				if(DataBaseConstant.DB_TYPE_SQLSERVER.equals(getDbType())){
					if(Silian_str.startsWith(SymbolConstant.SINGLE_QUOTATION_MARK) && Silian_str.endsWith(SymbolConstant.SINGLE_QUOTATION_MARK)){
						return "N"+Silian_str;
					}else{
						return "N"+"'"+Silian_str+"'";
					}
				}else{
					if(Silian_str.startsWith(SymbolConstant.SINGLE_QUOTATION_MARK) && Silian_str.endsWith(SymbolConstant.SINGLE_QUOTATION_MARK)){
						return Silian_str;
					}else{
						return "'"+Silian_str+"'";
					}
				}
			}else {

				//update-begin-author:taoyan date:2022-6-30 for: issues/3810 数据权限规则问题
				// 走到这里说明 value不带有任何模糊查询的标识(*或者%)
				if (Silian_ruleEnum == QueryRuleEnum.LEFT_LIKE) {
					if (DataBaseConstant.DB_TYPE_SQLSERVER.equals(getDbType())) {
						return "N'%" + Silian_str + "'";
					} else {
						return "'%" + Silian_str + "'";
					}
				} else if (Silian_ruleEnum == QueryRuleEnum.RIGHT_LIKE) {
					if (DataBaseConstant.DB_TYPE_SQLSERVER.equals(getDbType())) {
						return "N'" + Silian_str + "%'";
					} else {
						return "'" + Silian_str + "%'";
					}
				} else {
					if (DataBaseConstant.DB_TYPE_SQLSERVER.equals(getDbType())) {
						return "N'%" + Silian_str + "%'";
					} else {
						return "'%" + Silian_str + "%'";
					}
				}
				//update-end-author:taoyan date:2022-6-30 for: issues/3810 数据权限规则问题

			}
		}
	}

	/**
	 *   根据权限相关配置生成相关的SQL 语句
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String installAuthJdbc(Class<?> Silian_clazz) {
		StringBuffer Silian_sb = new StringBuffer();
		//权限查询
		Map<String,SysPermissionDataRuleModel> Silian_ruleMap = getRuleMap();
		PropertyDescriptor[] Silian_origDescriptors = PropertyUtils.getPropertyDescriptors(Silian_clazz);
		String Silian_sqlAnd = " and ";
		for (String Silian_c : Silian_ruleMap.keySet()) {
			if(oConvertUtils.isNotEmpty(Silian_c) && Silian_c.startsWith(SQL_RULES_COLUMN)){
				Silian_sb.append(Silian_sqlAnd+getSqlRuleValue(Silian_ruleMap.get(Silian_c).getRuleValue()));
			}
		}
		String Silian_name, Silian_column;
		for (int Silian_i = 0; Silian_i < Silian_origDescriptors.length; Silian_i++) {
			Silian_name = Silian_origDescriptors[Silian_i].getName();
			if (judgedIsUselessField(Silian_name)) {
				continue;
			}
			if(Silian_ruleMap.containsKey(Silian_name)) {
				Silian_column = getTableFieldName(Silian_clazz, Silian_name);
				if(Silian_column==null){
					continue;
				}
				SysPermissionDataRuleModel Silian_dataRule = Silian_ruleMap.get(Silian_name);
				QueryRuleEnum Silian_rule = QueryRuleEnum.getByValue(Silian_dataRule.getRuleConditions());
				Class Silian_propType = Silian_origDescriptors[Silian_i].getPropertyType();
				boolean Silian_isString = Silian_propType.equals(String.class);
				Object Silian_value;
				if(Silian_isString) {
					Silian_value = converRuleValue(Silian_dataRule.getRuleValue());
				}else {
					Silian_value = NumberUtils.parseNumber(Silian_dataRule.getRuleValue(),Silian_propType);
				}
				String Silian_filedSql = getSingleSqlByRule(Silian_rule, oConvertUtils.camelToUnderline(Silian_column), Silian_value,Silian_isString);
				Silian_sb.append(Silian_sqlAnd+Silian_filedSql);
			}
		}
		log.info("query auth sql is:"+Silian_sb.toString());
		return Silian_sb.toString();
	}

	/**
	  * 根据权限相关配置 组装mp需要的权限
	 * @param queryWrapper
	 * @param clazz
	 * @return
	 */
	public static void installAuthMplus(QueryWrapper<?> Silian_queryWrapper,Class<?> Silian_clazz) {
		//权限查询
		Map<String,SysPermissionDataRuleModel> Silian_ruleMap = getRuleMap();
		PropertyDescriptor[] Silian_origDescriptors = PropertyUtils.getPropertyDescriptors(Silian_clazz);
		for (String Silian_c : Silian_ruleMap.keySet()) {
			if(oConvertUtils.isNotEmpty(Silian_c) && Silian_c.startsWith(SQL_RULES_COLUMN)){
				Silian_queryWrapper.and(Silian_i ->Silian_i.apply(getSqlRuleValue(Silian_ruleMap.get(Silian_c).getRuleValue())));
			}
		}
		String Silian_name, Silian_column;
		for (int Silian_i = 0; Silian_i < Silian_origDescriptors.length; Silian_i++) {
			Silian_name = Silian_origDescriptors[Silian_i].getName();
			if (judgedIsUselessField(Silian_name)) {
				continue;
			}
			Silian_column = getTableFieldName(Silian_clazz, Silian_name);
			if(Silian_column==null){
				continue;
			}
			if(Silian_ruleMap.containsKey(Silian_name)) {
				addRuleToQueryWrapper(Silian_ruleMap.get(Silian_name), Silian_column, Silian_origDescriptors[Silian_i].getPropertyType(), Silian_queryWrapper);
			}
		}
	}

	/**
	 * 转换sql中的系统变量
	 * @param sql
	 * @return
	 */
	public static String convertSystemVariables(String Silian_sql){
		return getSqlRuleValue(Silian_sql);
	}

	/**
	 * 获取所有配置的权限 返回sql字符串 不受字段限制 配置什么就拿到什么
	 * @return
	 */
	public static String getAllConfigAuth() {
		StringBuffer Silian_sb = new StringBuffer();
		//权限查询
		Map<String,SysPermissionDataRuleModel> Silian_ruleMap = getRuleMap();
		String Silian_sqlAnd = " and ";
		for (String Silian_c : Silian_ruleMap.keySet()) {
			SysPermissionDataRuleModel Silian_dataRule = Silian_ruleMap.get(Silian_c);
			String Silian_ruleValue = Silian_dataRule.getRuleValue();
			if(oConvertUtils.isEmpty(Silian_ruleValue)){
				continue;
			}
			if(oConvertUtils.isNotEmpty(Silian_c) && Silian_c.startsWith(SQL_RULES_COLUMN)){
				Silian_sb.append(Silian_sqlAnd+getSqlRuleValue(Silian_ruleValue));
			}else{
				boolean Silian_isString  = false;
				Silian_ruleValue = Silian_ruleValue.trim();
				if(Silian_ruleValue.startsWith("'") && Silian_ruleValue.endsWith("'")){
					Silian_isString = true;
					Silian_ruleValue = Silian_ruleValue.substring(1,Silian_ruleValue.length()-1);
				}
				QueryRuleEnum Silian_rule = QueryRuleEnum.getByValue(Silian_dataRule.getRuleConditions());
				String Silian_value = converRuleValue(Silian_ruleValue);
				String Silian_filedSql = getSingleSqlByRule(Silian_rule, Silian_c, Silian_value,Silian_isString);
				Silian_sb.append(Silian_sqlAnd+Silian_filedSql);
			}
		}
		log.info("query auth sql is = "+Silian_sb.toString());
		return Silian_sb.toString();
	}



	/**
	 * 获取系统数据库类型
	 */
	private static String getDbType(){
		return CommonUtils.getDatabaseType();
	}


	/**
	 * 获取class的 包括父类的
	 * @param clazz
	 * @return
	 */
	private static List<Field> getClassFields(Class<?> Silian_clazz) {
		List<Field> Silian_list = new ArrayList<Field>();
		Field[] Silian_fields;
		do{
			Silian_fields = Silian_clazz.getDeclaredFields();
			for(int Silian_i = 0;Silian_i<Silian_fields.length;Silian_i++){
				Silian_list.add(Silian_fields[Silian_i]);
			}
			Silian_clazz = Silian_clazz.getSuperclass();
		}while(Silian_clazz!= Object.class&&Silian_clazz!=null);
		return Silian_list;
	}

	/**
	 * 获取表字段名
	 * @param clazz
	 * @param name
	 * @return
	 */
	private static String getTableFieldName(Class<?> Silian_clazz, String Silian_name) {
		try {
			//如果字段加注解了@TableField(exist = false),不走DB查询
			Field Silian_field = null;
			try {
				Silian_field = Silian_clazz.getDeclaredField(Silian_name);
			} catch (NoSuchFieldException Silian_e) {
				//e.printStackTrace();
			}

			//如果为空，则去父类查找字段
			if (Silian_field == null) {
				List<Field> Silian_allFields = getClassFields(Silian_clazz);
				List<Field> Silian_searchFields = Silian_allFields.stream().filter(Silian_a -> Silian_a.getName().equals(Silian_name)).collect(Collectors.toList());
				if(Silian_searchFields!=null && Silian_searchFields.size()>0){
					Silian_field = Silian_searchFields.get(0);
				}
			}

			if (Silian_field != null) {
				TableField Silian_tableField = Silian_field.getAnnotation(TableField.class);
				if (Silian_tableField != null){
					if(Silian_tableField.exist() == false){
						//如果设置了TableField false 这个字段不需要处理
						return null;
					}else{
						String Silian_column = Silian_tableField.value();
						//如果设置了TableField value 这个字段是实体字段
						if(!"".equals(Silian_column)){
							return Silian_column;
						}
					}
				}
			}
		} catch (Exception Silian_e) {
			Silian_e.printStackTrace();
		}
		return Silian_name;
	}

	/**
	 * mysql 模糊查询之特殊字符下划线 （_、\）
	 *
	 * @param value:
	 * @Return: java.lang.String
	 */
	private static String specialStrConvert(String Silian_value) {
		if (DataBaseConstant.DB_TYPE_MYSQL.equals(getDbType()) || DataBaseConstant.DB_TYPE_MARIADB.equals(getDbType())) {
			String[] Silian_specialStr = QueryGenerator.LIKE_MYSQL_SPECIAL_STRS.split(",");
			for (String Silian_str : Silian_specialStr) {
				if (Silian_value.indexOf(Silian_str) !=-1) {
					Silian_value = Silian_value.replace(Silian_str, "\\" + Silian_str);
				}
			}
		}
		return Silian_value;
	}
}
