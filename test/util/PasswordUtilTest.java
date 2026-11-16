package util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 密码工具类单元测试
 */
class PasswordUtilTest {

    @Test
    void hashPasswordShouldReturnSaltColonHashFormat() {
        String hashed = PasswordUtil.hashPassword("123456");
        assertNotNull(hashed);
        assertTrue(hashed.contains(":"));
        String[] parts = hashed.split(":", 2);
        assertEquals(2, parts.length);
        assertFalse(parts[0].isEmpty());
        assertFalse(parts[1].isEmpty());
    }

    @Test
    void verifyCorrectPasswordShouldReturnTrue() {
        String hashed = PasswordUtil.hashPassword("mypassword");
        assertTrue(PasswordUtil.verifyPassword("mypassword", hashed));
    }

    @Test
    void verifyWrongPasswordShouldReturnFalse() {
        String hashed = PasswordUtil.hashPassword("mypassword");
        assertFalse(PasswordUtil.verifyPassword("wrongpassword", hashed));
    }

    @Test
    void samePasswordHashedTwiceShouldBeDifferent() {
        // 因为盐值不同，两次哈希结果应该不一样
        String hash1 = PasswordUtil.hashPassword("same");
        String hash2 = PasswordUtil.hashPassword("same");
        assertNotEquals(hash1, hash2);
    }

    @Test
    void verifyWithNullStoredPasswordShouldReturnFalse() {
        assertFalse(PasswordUtil.verifyPassword("test", null));
    }

    @Test
    void verifyWithInvalidFormatShouldReturnFalse() {
        assertFalse(PasswordUtil.verifyPassword("test", "no-colon-here"));
    }
}
