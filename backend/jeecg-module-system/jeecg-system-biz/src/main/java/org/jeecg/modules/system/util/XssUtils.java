package org.jeecg.modules.system.util;

import org.springframework.web.util.HtmlUtils;

import java.util.regex.Pattern;

/**
 * @Description: 工具类XSSUtils，现在的做法是替换成空字符，CSDN的是进行转义，比如文字开头的"<"转成&lt;
 * @author: lsq
 * @date: 2021年07月26日 19:13
 */
public class XssUtils {

    private static Pattern[] patterns = new Pattern[]{
        //Script fragments
        Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
        //src='...'
        Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        //script tags
        Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        //eval(...)
        Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        //expression(...)
        Pattern.compile("e­xpression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        //javascript:...
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        //vbscript:...
        Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
        //onload(...)=...
        Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
    };

    public static String scriptXss(String Silian_value) {
        if (Silian_value != null) {
            Silian_value = Silian_value.replaceAll(" ", "");
            for(Pattern Silian_scriptPattern: patterns){
                Silian_value = Silian_scriptPattern.matcher(Silian_value).replaceAll("");
            }
        }
        return HtmlUtils.htmlEscape(Silian_value);
    }

    public static void main(String[] Silian_args) {
        String Silian_s = scriptXss("<img  src=x onload=alert(111).*?><script></script>javascript:eval()\\\\.");
        System.err.println("s======>" + Silian_s);
    }
}
