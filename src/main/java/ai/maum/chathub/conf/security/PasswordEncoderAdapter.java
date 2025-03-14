package ai.maum.chathub.conf.security;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-512 암호화를 수행하는 PasswordEncoder
 * @author baekgol
 */
public class PasswordEncoderAdapter implements PasswordEncoder {
    private String salt;
    private MessageDigest messageDigest;

    public PasswordEncoderAdapter(String salt) {
        try {
            this.salt = salt;
            messageDigest = MessageDigest.getInstance("SHA-512");
        } catch(NoSuchAlgorithmException ignored) {}
    }

    @Override
    public String encode(CharSequence rawPassword) {
        messageDigest.update((rawPassword + salt).getBytes());
        StringBuilder result = new StringBuilder();
        for(byte b: messageDigest.digest()) result.append(String.format("%02x", b));
        return result.toString();
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return rawPassword.equals(encodedPassword);
    }

    boolean isAlive() {
        return messageDigest != null;
    }
}
