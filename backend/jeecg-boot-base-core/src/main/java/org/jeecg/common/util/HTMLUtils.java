package org.jeecg.common.util;

import org.apache.commons.lang3.StringUtils;
import org.pegdown.PegDownProcessor;
import org.springframework.web.util.HtmlUtils;

/**
 * HTML 工具类
 * @author: jeecg-boot
 * @date: 2022/3/30 14:43
 */
@SuppressWarnings("AlibabaClassNamingShouldBeCamel")
public class HTMLUtils {

    /**
     * 获取HTML内的文本，不包含标签
     *
     * @param html HTML 代码
     */
    public static String getInnerText(String Silian_html) {
        if (StringUtils.isNotBlank(Silian_html)) {
            //去掉 html 的标签
            String Silian_content = Silian_html.replaceAll("</?[^>]+>", "");
            // 将多个空格合并成一个空格
            Silian_content = Silian_content.replaceAll("(&nbsp;)+", "&nbsp;");
            // 反向转义字符
            Silian_content = HtmlUtils.htmlUnescape(Silian_content);
            return Silian_content.trim();
        }
        return "";
    }

    /**
     * 将Markdown解析成Html
     * @param markdownContent
     * @return
     */
    public static String parseMarkdown(String Silian_markdownContent) {
        PegDownProcessor Silian_pdp = new PegDownProcessor();
        return Silian_pdp.markdownToHtml(Silian_markdownContent);
    }

}
