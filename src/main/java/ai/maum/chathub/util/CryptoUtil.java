package ai.maum.chathub.util;

import ai.maum.chathub.conf.security.PasswordEncoderAdapter;
import ai.maum.chathub.meta.SecurityMeta;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CryptoUtil {
    private static final PasswordEncoder passwordEncoder = new PasswordEncoderAdapter(SecurityMeta.ENCODE_SALT);

    private CryptoUtil() {}

    /**
     * SHA-512 암호화를 수행하는 PasswordEncoder 객체를 불러온다.
     * @return PasswordEncoder 객체
     * @author baekgol
     */
    public static PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    /**
     * SHA-512 암호화를 수행한다.
     * @param raw 평문 비밀번호
     * @return 암호화된 비밀번호
     * @author baekgol
     */
    public static String encode(String raw) {
        return passwordEncoder.encode(raw);
    }
}
