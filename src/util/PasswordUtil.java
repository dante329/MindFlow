package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码工具类
 * 使用 SHA-256 + 随机盐值进行密码哈希
 */
public class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SALT_LENGTH = 16;

    private PasswordUtil() {
        // 工具类不允许实例化
    }

    /**
     * 对密码进行加盐哈希
     *
     * @param rawPassword 原始密码
     * @return "salt:hash" 格式的字符串
     */
    public static String hashPassword(String rawPassword) {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        String hash = sha256(saltBase64 + rawPassword);
        return saltBase64 + ":" + hash;
    }

    /**
     * 验证密码是否匹配
     *
     * @param rawPassword     用户输入的原始密码
     * @param storedPassword  数据库中存储的 "salt:hash" 格式密码
     * @return 是否匹配
     */
    public static boolean verifyPassword(String rawPassword, String storedPassword) {
        if (storedPassword == null || !storedPassword.contains(":")) {
            return false;
        }
        String[] parts = storedPassword.split(":", 2);
        String salt = parts[0];
        String expectedHash = parts[1];
        String actualHash = sha256(salt + rawPassword);
        return expectedHash.equals(actualHash);
    }

    /**
     * SHA-256 哈希
     */
    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 算法不可用", e);
        }
    }
}
