package Sinisa.Stevanovic.eventsApp;

import android.util.Base64;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
public final class PasswordHasher {
    private static final String ALGORITHM_SHA256 = "PBKDF2WithHmacSHA256";
    private static final String ALGORITHM_SHA1 = "PBKDF2WithHmacSHA1";
    private static final String ID_SHA256 = "pbkdf2_sha256";
    private static final String ID_SHA1 = "pbkdf2_sha1";
    private static final int ITERATIONS = 120_000;
    private static final int MIN_ITERATIONS = 10_000;
    private static final int MAX_ITERATIONS = 1_000_000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BYTES = 32;
    private PasswordHasher() {}
    public static String hashPassword(String password) {
        if (password == null) throw new IllegalArgumentException("Password cannot be null");
        char[] passwordChars = password.toCharArray();
        try {
            byte[] salt = new byte[SALT_BYTES];
            new SecureRandom().nextBytes(salt);
            String algorithmId = getBestAvailableAlgorithmId();
            byte[] hash = pbkdf2(algorithmId, passwordChars, salt, ITERATIONS, HASH_BYTES);
            return algorithmId + ":" + ITERATIONS + ":"
                    + Base64.encodeToString(salt, Base64.NO_WRAP) + ":"
                    + Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Password hashing failed", e);
        } finally {
            Arrays.fill(passwordChars, '\0');
        }
    }
    public static boolean verifyPassword(String enteredPassword, String storedPasswordData) {
        if (enteredPassword == null || storedPasswordData == null) return false;
        char[] enteredPasswordChars = enteredPassword.toCharArray();
        try {
            String[] parts = storedPasswordData.split(":", -1);
            if (parts.length != 4) return false;
            String algorithmId = parts[0];
            int iterations = Integer.parseInt(parts[1]);
            if (iterations < MIN_ITERATIONS || iterations > MAX_ITERATIONS) return false;
            byte[] salt = Base64.decode(parts[2], Base64.NO_WRAP);
            byte[] storedHash = Base64.decode(parts[3], Base64.NO_WRAP);
            if (salt.length != SALT_BYTES || storedHash.length == 0) return false;
            byte[] enteredHash = pbkdf2(algorithmId, enteredPasswordChars, salt, iterations,
                    storedHash.length);
            return MessageDigest.isEqual(storedHash, enteredHash);
        } catch (Exception e) {
            return false;
        } finally {
            Arrays.fill(enteredPasswordChars, '\0');
        }
    }
    private static byte[] pbkdf2(String algorithmId, char[] password, byte[] salt, int iterations, int
            hashBytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, hashBytes * 8);
        try {
            return
                    SecretKeyFactory.getInstance(getJavaAlgorithmName(algorithmId)).generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }
    private static String getBestAvailableAlgorithmId() throws NoSuchAlgorithmException {
        try {
            SecretKeyFactory.getInstance(ALGORITHM_SHA256);
            return ID_SHA256;
        } catch (NoSuchAlgorithmException e) {
            SecretKeyFactory.getInstance(ALGORITHM_SHA1);
            return ID_SHA1;
        }
    }
    private static String getJavaAlgorithmName(String algorithmId) throws NoSuchAlgorithmException {
        switch (algorithmId) {
            case ID_SHA256: return ALGORITHM_SHA256;
            case ID_SHA1: return ALGORITHM_SHA1;
            default: throw new NoSuchAlgorithmException("Unsupported algorithm: " + algorithmId);
        }
    }
}