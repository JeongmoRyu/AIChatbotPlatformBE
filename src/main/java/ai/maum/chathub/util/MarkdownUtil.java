package ai.maum.chathub.util;

import java.util.regex.*;

public class MarkdownUtil {
    public static String convertMarkdownToText(String markdown) {
        // 패턴 정의
        String[] patterns = {
                "\\*\\*(.*?)\\*\\*",  // **bold**
                "\\*(.*?)\\*",        // *italic*
                "\\`(.*?)\\`",        // `code`
//                "\\[(.*?)\\]\\((.*?)\\)", // [link text](url)
//                "\\!\\[(.*?)\\]\\((.*?)\\)", // ![alt text](image url)
                "\\# (.*?)\n",         // # Header 1
                "\\#\\# (.*?)\n",      // ## Header 2
                "\\#\\#\\# (.*?)\n",   // ### Header 3
                "\\#\\#\\#\\# (.*?)\n",// #### Header 4
                "\\#\\#\\#\\#\\# (.*?)\n", // ##### Header 5
                "\\#\\#\\#\\#\\#\\# (.*?)\n" // ###### Header 6
        };

        // 정규 표현식에 맞는 텍스트를 일반 텍스트로 변환
        for (String pattern : patterns) {
            markdown = markdown.replaceAll(pattern, "$1");
        }

        // 특정 마크다운 구문을 텍스트로 변환
//        markdown = markdown.replaceAll("\\*\\*(.*?)\\*\\*", "$1"); // bold
//        markdown = markdown.replaceAll("\\*(.*?)\\*", "$1"); // italic
//        markdown = markdown.replaceAll("\\`(.*?)\\`", "$1"); // code
//        markdown = markdown.replaceAll("\\[(.*?)\\]\\((.*?)\\)", "$1"); // link
//        markdown = markdown.replaceAll("\\!\\[(.*?)\\]\\((.*?)\\)", "$1"); // image alt text

        return markdown;
    }

    public static void main(String[] args) {
        String markdown = "This is **bold** text, this is *italic* text, this is `code`, and this is a [link](http://example.com).";
        String text = convertMarkdownToText(markdown);
        System.out.println(text);
    }
}