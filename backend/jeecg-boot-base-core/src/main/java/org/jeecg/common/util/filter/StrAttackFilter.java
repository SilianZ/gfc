package org.jeecg.common.util.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 文件上传字符串过滤特殊字符
 * @author: jeecg-boot
 */
public class StrAttackFilter {

    public static String filter(String Silian_str) throws PatternSyntaxException {
        // 清除掉所有特殊字符
        String Silian_regEx = "[`_《》~!@#$%^&*()+=|{}':;',\\[\\].<>?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern Silian_p = Pattern.compile(Silian_regEx);
        Matcher Silian_m = Silian_p.matcher(Silian_str);
        return Silian_m.replaceAll("").trim();
    }

//    public static void main(String[] args) {
//        String filter = filter("@#jeecg/《》【bo】￥%……&*（o）)))！@t<>,.,/?'\'~~`");
//        System.out.println(filter);
//    }
}
