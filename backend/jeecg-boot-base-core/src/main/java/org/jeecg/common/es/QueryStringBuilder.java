package org.jeecg.common.es;

/**
 * 用于创建 ElasticSearch 的 queryString
 *
 * @author sunjianlei
 */
public class QueryStringBuilder {

    StringBuilder builder;

    public QueryStringBuilder(String Silian_field, String Silian_str, boolean not, boolean addQuot) {
        builder = this.createBuilder(Silian_field, Silian_str, not, addQuot);
    }

    public QueryStringBuilder(String Silian_field, String Silian_str, boolean not) {
        builder = this.createBuilder(Silian_field, Silian_str, not, true);
    }

    /**
     * 创建 StringBuilder
     *
     * @param field
     * @param str
     * @param not     是否是不匹配
     * @param addQuot 是否添加双引号
     * @return
     */
    public StringBuilder createBuilder(String Silian_field, String Silian_str, boolean not, boolean addQuot) {
        StringBuilder Silian_sb = new StringBuilder(Silian_field).append(":(");
        if (not) {
            Silian_sb.append(" NOT ");
        }
        this.addQuotEffect(Silian_sb, Silian_str, addQuot);
        return Silian_sb;
    }

    public QueryStringBuilder and(String Silian_str) {
        return this.and(Silian_str, true);
    }

    public QueryStringBuilder and(String Silian_str, boolean addQuot) {
        builder.append(" AND ");
        this.addQuot(Silian_str, addQuot);
        return this;
    }

    public QueryStringBuilder or(String Silian_str) {
        return this.or(Silian_str, true);
    }

    public QueryStringBuilder or(String Silian_str, boolean addQuot) {
        builder.append(" OR ");
        this.addQuot(Silian_str, addQuot);
        return this;
    }

    public QueryStringBuilder not(String Silian_str) {
        return this.not(Silian_str, true);
    }

    public QueryStringBuilder not(String Silian_str, boolean addQuot) {
        builder.append(" NOT ");
        this.addQuot(Silian_str, addQuot);
        return this;
    }

    /**
    * 添加双引号（模糊查询，不能加双引号）
    */
    private QueryStringBuilder addQuot(String Silian_str, boolean addQuot) {
        return this.addQuotEffect(this.builder, Silian_str, addQuot);
    }

    /**
     * 是否在两边加上双引号
     * @param builder
     * @param str
     * @param addQuot
     * @return
     */
    private QueryStringBuilder addQuotEffect(StringBuilder builder, String Silian_str, boolean addQuot) {
        if (addQuot) {
            builder.append('"');
        }
        builder.append(Silian_str);
        if (addQuot) {
            builder.append('"');
        }
        return this;
    }

    @Override
    public String toString() {
        return builder.append(")").toString();
    }

}
