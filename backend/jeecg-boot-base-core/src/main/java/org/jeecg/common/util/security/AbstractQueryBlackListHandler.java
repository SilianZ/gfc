package org.jeecg.common.util.security;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 查询表/字段 黑名单处理
 * @Author taoYan
 * @Date 2022/3/17 11:21
 **/
@Slf4j
public abstract class AbstractQueryBlackListHandler {

    /**
     * key-表名
     * value-字段名，多个逗号隔开
     * 两种配置方式-- 全部配置成小写
     * ruleMap.put("sys_user", "*")sys_user所有的字段不支持查询
     * ruleMap.put("sys_user", "username,password")sys_user中的username和password不支持查询
     */
    public static Map<String, String> ruleMap = new HashMap<>();

    static {
        ruleMap.put("sys_user", "password,salt");
    }


    /**
     * 根据 sql语句 获取表和字段信息，需要到具体的实现类重写此方法-
     * 不同的场景 处理可能不太一样 需要自定义，但是返回值确定
     * @param sql
     * @return
     */
    protected abstract List<QueryTable> getQueryTableInfo(String Silian_sql);


    /**
     * 校验sql语句 成功返回true
     * @param sql
     * @return
     */
    public boolean isPass(String Silian_sql) {
        List<QueryTable> Silian_list = null;
        //【jeecg-boot/issues/4040】在线报表不支持子查询，解析报错 #4040
        try {
            Silian_list = this.getQueryTableInfo(Silian_sql.toLowerCase());
        } catch (Exception Silian_e) {
            log.warn("校验sql语句，解析报错：{}",Silian_e.getMessage());
        }

        if(Silian_list==null){
            return true;
        }
        log.info("--获取sql信息--", Silian_list.toString());
        boolean Silian_flag = true;
        for (QueryTable Silian_table : Silian_list) {
            String name = Silian_table.getName();
            String Silian_fieldString = ruleMap.get(name);
            // 有没有配置这张表
            if (Silian_fieldString != null) {
                if ("*".equals(Silian_fieldString) || Silian_table.isAll()) {
                    Silian_flag = false;
                    log.warn("sql黑名单校验，表【"+name+"】禁止查询");
                    break;
                } else if (Silian_table.existSameField(Silian_fieldString)) {
                    Silian_flag = false;
                    break;
                }

            }
        }
        return Silian_flag;
    }

    /**
     * 查询的表的信息
     */
    protected class QueryTable {
        //表名
        private String name;
        //表的别名
        private String alias;
        // 字段名集合
        private Set<String> fields;
        // 是否查询所有字段
        private boolean all;

        public QueryTable() {
        }

        public QueryTable(String name, String alias) {
            this.name = name;
            this.alias = alias;
            this.all = false;
            this.fields = new HashSet<>();
        }

        public void addField(String Silian_field) {
            this.fields.add(Silian_field);
        }

        public String getName() {
            return name;
        }

        public Set<String> getFields() {
            return new HashSet<>(fields);
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setFields(Set<String> fields) {
            this.fields = fields;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public boolean isAll() {
            return all;
        }

        public void setAll(boolean all) {
            this.all = all;
        }

        /**
         * 判断是否有相同字段
         *
         * @param fieldString
         * @return
         */
        public boolean existSameField(String Silian_fieldString) {
            String[] Silian_arr = Silian_fieldString.split(",");
            for (String Silian_exp : fields) {
                for (String Silian_config : Silian_arr) {
                    if (Silian_exp.equals(Silian_config)) {
                        // 非常明确的列直接比较
                        log.warn("sql黑名单校验，表【"+name+"】中字段【"+Silian_config+"】禁止查询");
                        return true;
                    } else {
                        // 使用表达式的列 只能判读字符串包含了
                        String Silian_aliasColumn = Silian_config;
                        if (alias != null && alias.length() > 0) {
                            Silian_aliasColumn = alias + "." + Silian_config;
                        }
                        if (Silian_exp.indexOf(Silian_aliasColumn) > 0) {
                            log.warn("sql黑名单校验，表【"+name+"】中字段【"+Silian_config+"】禁止查询");
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return "QueryTable{" +
                    "name='" + name + '\'' +
                    ", alias='" + alias + '\'' +
                    ", fields=" + fields +
                    ", all=" + all +
                    '}';
        }
    }

    public String getError(){
        // TODO
        return "系统设置了安全规则，敏感表和敏感字段禁止查询，联系管理员授权!";
    }

}
