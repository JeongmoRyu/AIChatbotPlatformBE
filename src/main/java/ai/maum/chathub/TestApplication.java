package ai.maum.chathub;

import ai.maum.chathub.ChathubBeApplication;
import org.jasypt.util.text.AES256TextEncryptor;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestApplication {

    public static void main(String[] args) {

//        String input = "어쩌구 저쩌구 블라블라(아이디) 왁짜지껄~";
//        String extracted = extractTextWithinParentheses(input);
//        System.out.println("Extracted text: " + extracted);

//        dbinfo();

        decrypt(args);

    }

    private static void decrypt(String[] args) {
        String encryptedText = "";
        ApplicationContext context = SpringApplication.run(ChathubBeApplication.class, args);

    }

    private static void dbinfo() {
        String myEncryptionKey = System.getenv("JASYPT_ENCRYPTOR_PASSWORD");
        System.out.println("myEncryptionKey: " + myEncryptionKey);
        String param1 = "jdbc:postgresql://genai-dev-common-pg-rds.ck8v2txozggz.ap-northeast-2.rds.amazonaws.com/genaidev?currentSchema=skins";
        AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
        textEncryptor.setPassword(myEncryptionKey);
        textEncryptor.encrypt(param1);
        System.out.println("param1:" + textEncryptor.encrypt(param1));
    }

    public static String extractTextWithinParentheses(String input) {
        // 정규식 패턴: 괄호 안의 내용을 캡처
        Pattern pattern = Pattern.compile("\\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(input);

        // 첫 번째 매칭된 그룹을 반환
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null; // 괄호 안에 텍스트가 없는 경우
        }
    }

//    public static void passwordSet

//    public static void main(String[] args) {
//        System.out.println("test");
//        String myEncryptionKey = System.getenv("JASYPT_ENCRYPTOR_PASSWORD");
//        AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
//        textEncryptor.setPassword(myEncryptionKey);
//
////        String param1 = "R000000071_004";
////        String param2 = "R000000071_ka3XG2UCRamQ0vQfpL9NUg";
//
////        String param1 = "jdbc:postgresql://genai-dev-common-pg-rds.ck8v2txozggz.ap-northeast-2.rds.amazonaws.com/genaidev?currentSchema=skins";
////        String param2 = "skinsown";
////        String param3 = "skinsown123!";
//
////        System.out.println("param1:" + textEncryptor.encrypt(param1));
////        System.out.println("param2:" + textEncryptor.encrypt(param2));
////        System.out.println("param3:" + textEncryptor.encrypt(param3));
//
//        System.out.println("|" + textEncryptor.encrypt("4639bf90a9ee65f9a383247bdb34c1fc2ff12896") + "|");
//        System.out.println("|" + textEncryptor.encrypt("f1b3a18202e10a3c7258fe626d09cfa10f57f83c") + "|");
//        System.out.println("|" + textEncryptor.decrypt("hrkHP2eVaAo5nHAEmZ2GQegrlpuGEyWOGV4vYyRE2pNx1mbzWzTmm1txetggdZ0P") + "|");
//        System.out.println("|" + textEncryptor.decrypt("mfPTJSeuOndDg9181ucUg9/Auhn/kNTDYZ8tz5TEZZjeHsDtuUlB+nYrxuS+6elSYcLKLJIUFglwqDzzkRp+K03G88BmmH9FUEvb485+n7w=") + "|");
//        System.out.println("|" + textEncryptor.decrypt("30w8TnAMTddKERMghy5EnYyqIKEC9V986duuhow9GoTFP3HUCpsdK18vM4hd0tCZTt8RF2MjFG53DjMnSrMcGlrF+PvQSxxZjhrUPs8lcIA=") + "|");
//
//    }
}
