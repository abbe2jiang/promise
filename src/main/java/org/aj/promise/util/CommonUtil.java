package org.aj.promise.util;

import java.io.StringReader;

import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonUtil {
    /**
     * 从html中提取文本
     * @param html
     * @return
     */
    public static String getTextFromHtml(String html) {
        try {
            StringReader in = new StringReader(html);
            StringBuffer buffer = new StringBuffer();
            ParserDelegator delegator = new ParserDelegator();
            delegator.parse(in, new HTMLEditorKit.ParserCallback() {
                public void handleText(char[] text, int pos) {
                    buffer.append(text);
                }
            }, Boolean.TRUE);
            return buffer.toString().replace("\\n", "");
        } catch (Exception e) {
            log.error("getTextFromHtml error", e);
            return html;
        }
    }
}
