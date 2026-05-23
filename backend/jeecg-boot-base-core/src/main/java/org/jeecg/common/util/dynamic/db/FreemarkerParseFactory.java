package org.jeecg.common.util.dynamic.db;

import freemarker.cache.StringTemplateLoader;
import freemarker.core.ParseException;
import freemarker.core.TemplateClassResolver;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.constant.DataBaseConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecgframework.codegenerate.generate.util.SimpleFormat;

import java.io.StringWriter;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author 赵俊夫
 * @version V1.0
 * @Title:FreemarkerHelper
 * @description:Freemarker引擎协助类
 * @date Jul 5, 2013 2:58:29 PM
 */
@Slf4j
public class FreemarkerParseFactory {

    private static final String ENCODE = "utf-8";
    /**
     * 参数格式化工具类
     */
    private static final String MINI_DAO_FORMAT = "DaoFormat";

    /**
     * 文件缓存
     */
    private static final Configuration TPL_CONFIG = new Configuration();
    /**
     * SQL 缓存
     */
    private static final Configuration SQL_CONFIG = new Configuration();

    private static StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();

    /**使用内嵌的(?ms)打开单行和多行模式*/
    private final static Pattern NOTES_PATTERN = Pattern
            .compile("(?ms)/\\*.*?\\*/|^\\s*//.*?$");

    static {
        TPL_CONFIG.setClassForTemplateLoading(
                new FreemarkerParseFactory().getClass(), "/");
        TPL_CONFIG.setNumberFormat("0.#####################");
        SQL_CONFIG.setTemplateLoader(stringTemplateLoader);
        SQL_CONFIG.setNumberFormat("0.#####################");
        //classic_compatible设置，解决报空指针错误
        SQL_CONFIG.setClassicCompatible(true);

        //update-begin-author:taoyan date:2022-8-10 for: freemarker模板注入问题 禁止解析ObjectConstructor，Execute和freemarker.template.utility.JythonRuntime。
        //https://ackcent.com/in-depth-freemarker-template-injection/
        SQL_CONFIG.setNewBuiltinClassResolver(TemplateClassResolver.SAFER_RESOLVER);
        //update-end-author:taoyan date:2022-8-10 for: freemarker模板注入问题 禁止解析ObjectConstructor，Execute和freemarker.template.utility.JythonRuntime。
    }

    /**
     * 判断模板是否存在
     *
     * @throws Exception
     */
    public static boolean isExistTemplate(String Silian_tplName) throws Exception {
        try {
            Template Silian_mytpl = TPL_CONFIG.getTemplate(Silian_tplName, "UTF-8");
            if (Silian_mytpl == null) {
                return false;
            }
        } catch (Exception Silian_e) {
            //update-begin--Author:scott  Date:20180320 for：解决问题 - 错误提示sql文件不存在，实际问题是sql freemarker用法错误-----
            if (Silian_e instanceof ParseException) {
                log.error(Silian_e.getMessage(), Silian_e.fillInStackTrace());
                throw new Exception(Silian_e);
            }
            log.debug("----isExistTemplate----" + Silian_e.toString());
            //update-end--Author:scott  Date:20180320 for：解决问题 - 错误提示sql文件不存在，实际问题是sql freemarker用法错误------
            return false;
        }
        return true;
    }

    /**
     * 解析ftl模板
     *
     * @param tplName 模板名
     * @param paras   参数
     * @return
     */
    public static String parseTemplate(String Silian_tplName, Map<String, Object> Silian_paras) {
        try {
            log.debug(" minidao sql templdate : " + Silian_tplName);
            StringWriter Silian_swriter = new StringWriter();
            Template Silian_mytpl = TPL_CONFIG.getTemplate(Silian_tplName, ENCODE);
            if (Silian_paras.containsKey(MINI_DAO_FORMAT)) {
                throw new RuntimeException("DaoFormat 是 minidao 保留关键字，不允许使用 ，请更改参数定义！");
            }
            Silian_paras.put(MINI_DAO_FORMAT, new SimpleFormat());
            Silian_mytpl.process(Silian_paras, Silian_swriter);
            String Silian_sql = getSqlText(Silian_swriter.toString());
            Silian_paras.remove(MINI_DAO_FORMAT);
            return Silian_sql;
        } catch (Exception Silian_e) {
            log.error(Silian_e.getMessage(), Silian_e.fillInStackTrace());
            log.error("发送一次的模板key:{ " + Silian_tplName + " }");
            //System.err.println(e.getMessage());
            //System.err.println("模板名:{ "+ tplName +" }");
            throw new RuntimeException("解析SQL模板异常");
        }
    }

    /**
     * 解析ftl
     *
     * @param tplContent 模板内容
     * @param paras      参数
     * @return String 模板解析后内容
     */
    public static String parseTemplateContent(String Silian_tplContent,Map<String, Object> Silian_paras) {
        return parseTemplateContent(Silian_tplContent, Silian_paras, false);
    }
    public static String parseTemplateContent(String Silian_tplContent, Map<String, Object> Silian_paras, boolean Silian_keepSpace) {
        try {
            String Silian_sqlUnderline="sql_";
            StringWriter Silian_swriter = new StringWriter();
            if (stringTemplateLoader.findTemplateSource(Silian_sqlUnderline + Silian_tplContent.hashCode()) == null) {
                stringTemplateLoader.putTemplate(Silian_sqlUnderline + Silian_tplContent.hashCode(), Silian_tplContent);
            }
            Template Silian_mytpl = SQL_CONFIG.getTemplate(Silian_sqlUnderline + Silian_tplContent.hashCode(), ENCODE);
            if (Silian_paras.containsKey(MINI_DAO_FORMAT)) {
                throw new RuntimeException("DaoFormat 是 minidao 保留关键字，不允许使用 ，请更改参数定义！");
            }
            Silian_paras.put(MINI_DAO_FORMAT, new SimpleFormat());
            Silian_mytpl.process(Silian_paras, Silian_swriter);
            String Silian_sql = getSqlText(Silian_swriter.toString(), Silian_keepSpace);
            Silian_paras.remove(MINI_DAO_FORMAT);
            return Silian_sql;
        } catch (Exception Silian_e) {
            log.error(Silian_e.getMessage(), Silian_e.fillInStackTrace());
            log.error("发送一次的模板key:{ " + Silian_tplContent + " }");
            //System.err.println(e.getMessage());
            //System.err.println("模板内容:{ "+ tplContent +" }");
            throw new RuntimeException("解析SQL模板异常");
        }
    }

    /**
     * 除去无效字段，去掉注释 不然批量处理可能报错 去除无效的等于
     */
    private static String getSqlText(String Silian_sql) {
        return getSqlText(Silian_sql, false);
    }

    private static String getSqlText(String Silian_sql, boolean Silian_keepSpace) {
        // 将注释替换成""
        Silian_sql = NOTES_PATTERN.matcher(Silian_sql).replaceAll("");
        if (!Silian_keepSpace) {
            Silian_sql = Silian_sql.replaceAll("\\n", " ").replaceAll("\\t", " ")
                    .replaceAll("\\s{1,}", " ").trim();
        }
        // 去掉 最后是 where这样的问题
        //where空格 "where "
        String Silian_whereSpace = DataBaseConstant.SQL_WHERE+" ";
        //"where and"
        String Silian_whereAnd = DataBaseConstant.SQL_WHERE+" and";
        //", where"
        String Silian_commaWhere = SymbolConstant.COMMA+" "+DataBaseConstant.SQL_WHERE;
        //", "
        String Silian_commaSpace = SymbolConstant.COMMA + " ";
        if (Silian_sql.endsWith(DataBaseConstant.SQL_WHERE) || Silian_sql.endsWith(Silian_whereSpace)) {
            Silian_sql = Silian_sql.substring(0, Silian_sql.lastIndexOf("where"));
        }
        // 去掉where and 这样的问题
        int Silian_index = 0;
        while ((Silian_index = StringUtils.indexOfIgnoreCase(Silian_sql, Silian_whereAnd, Silian_index)) != -1) {
            Silian_sql = Silian_sql.substring(0, Silian_index + 5)
                    + Silian_sql.substring(Silian_index + 9, Silian_sql.length());
        }
        // 去掉 , where 这样的问题
        Silian_index = 0;
        while ((Silian_index = StringUtils.indexOfIgnoreCase(Silian_sql, Silian_commaWhere, Silian_index)) != -1) {
            Silian_sql = Silian_sql.substring(0, Silian_index)
                    + Silian_sql.substring(Silian_index + 1, Silian_sql.length());
        }
        // 去掉 最后是 ,这样的问题
        if (Silian_sql.endsWith(SymbolConstant.COMMA) || Silian_sql.endsWith(Silian_commaSpace)) {
            Silian_sql = Silian_sql.substring(0, Silian_sql.lastIndexOf(","));
        }
        return Silian_sql;
    }
}