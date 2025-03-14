package ai.maum.chathub.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class StringUtil {
    private StringUtil() {}

    /**
     * 해당 문자열이 패턴과 일치하는지 확인한다.
     * @param target 대상 문자열
     * @param pattern 패턴, 정규식
     * @return 패턴 일치 유무
     * @author baekgol
     */
    public static boolean matches(String target, String pattern) {
        return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE) // 대소문자 구분 없이 check
                .matcher(target)
                .matches();
    }

    /**
     * 해당 문자열(target)이 패턴 목록(patterns) 중 하나 이상 일치하는 지 확인한다.
     * @param target 대상 문자열
     * @param patterns 패턴, 정규식 리스트
     * @return 패턴 일치 유무
     * @author bhr
     */
    public static boolean matches(String target, List<String> patterns) {
        for(String pattern: patterns) {
            if(Pattern.compile(pattern)
                    .matcher(target)
                    .matches()
            ) {
                return true;
            }
        }

        return false;
    }

    /**
     * Camel 형식의 문자열 또는 Snake 형식의 문자열을 반대 형식으로 변환한다.
     * 대상 문자열의 형식이 isCamel과 일치하지 않을 경우 대상 문자열을 그대로 반환한다.
     * @param target 대상 문자열
     * @param isCamel Camel 형식의 대상 문자열인지 유무
     * @return 변환된 형식의 문자열
     * @author baekgol
     */
    public static String convertNaming(String target, boolean isCamel) {
        if((isCamel && Character.isUpperCase(target.charAt(0)))
        || (!isCamel && !target.contains("_"))) return target;

        int len = target.length();
        StringBuilder sb = new StringBuilder();

        for(int i=0; i<len; i++) {
            char c = target.charAt(i);

            if(isCamel) {
                if(Character.isUpperCase(c)) {
                    sb.append('_');
                    sb.append(Character.toLowerCase(c));
                }
                else sb.append(c);
            }
            else {
                if(i>0 && target.charAt(i-1) == '_') sb.append(Character.toUpperCase(c));
                else if(c != '_') sb.append(c);
            }
        }

        return sb.toString();
    }

    /**
     * 예외 추적 정보를 문자열로 변환한다.
     * @param e 예외
     * @return 예외 추적 정보가 담긴 문자열
     * @author baekgol
     */
    public static String getStackTrace(Throwable e) {
        String message = e.getMessage();

        try(StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw)) {
            e.printStackTrace(pw);
            message = sw.toString();
        } catch(IOException ignored) {}

        return message;
    }

    public static String valueOf(Object str) {
        return str == null? "" : String.valueOf(str);
    }

    public static List<Long> convertStringToLongList(String stringList) {
        // 입력된 문자열이 null이거나 빈 경우 빈 리스트를 반환
        if (stringList == null || stringList.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // 대괄호 제거 후, 콤마(,)로 구분하여 문자열 배열로 변환
        stringList = stringList.replaceAll("[\\[\\]]", "");
        String[] stringArray = stringList.split(",");

        // 문자열 배열을 Long 타입의 리스트로 변환
        List<Long> longList = new ArrayList<>();
        try {
            for (String s : stringArray) {
                longList.add(Long.parseLong(s.trim()));
            }
        } catch (NumberFormatException e) {
            // 숫자 형식 변환에 실패할 경우 빈 리스트를 반환
            return new ArrayList<>();
        }

        return longList;
    }

    public static boolean parseBoolean(String input) {
        if (input == null) {
            return false;
        }

        // 대소문자 구분 없이 특정 문자열들에 대해 true를 반환
        switch (input.trim().toLowerCase()) {
            case "true":
            case "yes":
            case "1":
                return true;
            case "false":
            case "no":
            case "0":
                return false;
            default:
                return false; // 그 외의 모든 경우 false
        }
    }
}
