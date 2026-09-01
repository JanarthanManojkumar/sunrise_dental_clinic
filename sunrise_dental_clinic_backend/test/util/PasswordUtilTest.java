package util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class PasswordUtilTest {

    @Test
    public void hashedPasswordMatchesOriginalPlainPassword() {
        String hash = PasswordUtil.hash("admin123");
        assertTrue(PasswordUtil.matches("admin123", hash));
    }

    @Test
    public void hashIsNotEqualToPlainPassword() {
        String hash = PasswordUtil.hash("admin123");
        assertNotEquals("admin123", hash);
    }

    @Test
    public void wrongPasswordDoesNotMatchHash() {
        String hash = PasswordUtil.hash("admin123");
        assertFalse(PasswordUtil.matches("wrongPassword", hash));
    }

    @Test
    public void nullPasswordDoesNotMatch() {
        String hash = PasswordUtil.hash("admin123");
        assertFalse(PasswordUtil.matches(null, hash));
    }
}
