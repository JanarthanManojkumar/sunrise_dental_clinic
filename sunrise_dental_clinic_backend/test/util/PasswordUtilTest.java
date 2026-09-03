package util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PasswordUtilTest {

    @Test
    @DisplayName("Hashed password matches the original plain password")
    public void hashedPasswordMatchesOriginalPlainPassword() {
        String hash = PasswordUtil.hash("admin123");
        assertTrue(PasswordUtil.matches("admin123", hash));
    }

    @Test
    @DisplayName("Password hash is never equal to the plain password text")
    public void hashIsNotEqualToPlainPassword() {
        String hash = PasswordUtil.hash("admin123");
        assertNotEquals("admin123", hash);
    }

    @Test
    @DisplayName("Wrong password does not match an existing hash")
    public void wrongPasswordDoesNotMatchHash() {
        String hash = PasswordUtil.hash("admin123");
        assertFalse(PasswordUtil.matches("wrongPassword", hash));
    }

    @Test
    @DisplayName("Null password never matches an existing hash")
    public void nullPasswordDoesNotMatch() {
        String hash = PasswordUtil.hash("admin123");
        assertFalse(PasswordUtil.matches(null, hash));
    }

    @Test
    @DisplayName("Hashing the same password twice produces two different hashes (random salt)")
    public void hashingSamePasswordTwiceProducesDifferentHashes() {
        String hash1 = PasswordUtil.hash("admin123");
        String hash2 = PasswordUtil.hash("admin123");

        assertNotEquals(hash1, hash2, "BCrypt.gensalt() should generate a fresh random salt every call");
        assertTrue(PasswordUtil.matches("admin123", hash1));
        assertTrue(PasswordUtil.matches("admin123", hash2));
    }

    @Test
    @DisplayName("Hashing an empty password succeeds and only matches an empty password")
    public void hashingEmptyPasswordOnlyMatchesEmptyString() {
        String hash = PasswordUtil.hash("");

        assertNotNull(hash);
        assertTrue(PasswordUtil.matches("", hash));
        assertFalse(PasswordUtil.matches("notEmpty", hash));
    }

    /**
     * jBCrypt's hashpw builds the payload with plain string concatenation:
     * {@code password + (minor >= 'a' ? "\000" : "")}. When password is null,
     * that compiles to StringBuilder.append((String) null), which per the
     * Javadoc appends the literal text "null" instead of throwing an NPE.
     * Confirmed by disassembling lib/jbcrypt-0.4.jar's BCrypt.hashpw. So
     * PasswordUtil.hash(null) does NOT throw - it silently hashes the
     * 4-character text "null" and returns a normal-looking hash.
     */
    @Test
    @DisplayName("Hashing a null password does not throw and quietly hashes the literal text \"null\"")
    public void hashingNullPasswordDoesNotThrowAndHashesLiteralNullText() {
        String hash = PasswordUtil.hash(null);

        assertNotNull(hash);
        assertTrue(PasswordUtil.matches("null", hash),
                "hash(null) treats the password as the 4-character text \"null\", not as absence of a password");
        assertFalse(PasswordUtil.matches("admin123", hash));
    }
}
