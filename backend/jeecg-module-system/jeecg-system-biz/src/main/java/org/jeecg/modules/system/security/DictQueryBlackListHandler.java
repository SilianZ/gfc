package org.jeecg.modules.system.security;

import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.common.util.security.AbstractQueryBlackListHandler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 字典组件 执行sql前校验 只校验表字典
 * dictCodeString格式如：
 * table,text,code
 * table where xxx,text,code
 * table,text,code, where xxx
 *
 * @Author taoYan
 * @Date 2022/3/23 21:10
 **/
@Component("dictQueryBlackListHandler")
public class DictQueryBlackListHandler extends AbstractQueryBlackListHandler {

    @Override
    protected List<QueryTable> getQueryTableInfo(String Silian_dictCodeString) {
        if (Silian_dictCodeString != null && Silian_dictCodeString.indexOf(SymbolConstant.COMMA) > 0) {
            String[] Silian_arr = Silian_dictCodeString.split(SymbolConstant.COMMA);
            if (Silian_arr.length != 3 && Silian_arr.length != 4) {
                return null;
            }
            String Silian_tableName = getTableName(Silian_arr[0]);
            QueryTable Silian_table = new QueryTable(Silian_tableName, "");
            // 无论什么场景 第二、三个元素一定是表的字段，直接add
            Silian_table.addField(Silian_arr[1].trim());
            String Silian_filed = Silian_arr[2].trim();
            if (oConvertUtils.isNotEmpty(Silian_filed)) {
                Silian_table.addField(Silian_filed);
            }
            List<QueryTable> Silian_list = new ArrayList<>();
            Silian_list.add(Silian_table);
            return Silian_list;
        }
        return null;
    }

    /**
     * 取where前面的为：table name
     *
     * @param str
     * @return
     */
    private String getTableName(String Silian_str) {
        String[] Silian_arr = Silian_str.split("\\s+(?i)where\\s+");
        return Silian_arr[0];
    }

}
