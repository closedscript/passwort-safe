package ch.passwordsafe.service;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * AES-256-CBC Verschlüsselungsservice.
 * Pure functions: keine Seiteneffekte, gleiche Eingabe → gleiche Ausgabe (bis auf IV).
 */
@Service
public class AesEncryptionService {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";
    private static final String KEY_DERIVATION = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int IV_LENGTH = 16;

    /**
     * Leitet einen AES-256-Schlüssel aus Master-Passwort + Salt ab (PBKDF2).
     * Pure function: deterministisch, keine Seiteneffekte.
     */
    public SecretKey deriveKey(String masterPassword, String saltBase64) throws Exception {
        byte[] salt = Base64.getDecoder().decode(saltBase64);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_DERIVATION);
        KeySpec spec = new PBEKeySpec(masterPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    }

    /**
     * Generiert einen zufälligen Salt (Base64).
     */
    public String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Generiert einen zufälligen IV (Base64).
     */
    public String generateIv() {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return Base64.getEncoder().encodeToString(iv);
    }

    /**
     * Verschlüsselt einen Klartext-String mit AES-CBC.
     * @param plaintext  Der zu verschlüsselnde Text
     * @param key        Der abgeleitete AES-Schlüssel
     * @param ivBase64   Der IV als Base64-String
     * @return           Verschlüsselter Text als Base64
     */
    public String encrypt(String plaintext, SecretKey key, String ivBase64) throws Exception {
        if (plaintext == null || plaintext.isBlank()) return null;
        byte[] iv = Base64.getDecoder().decode(ivBase64);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] encrypted = cipher.doFinal(plaintext.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * Entschlüsselt einen Base64-kodierten verschlüsselten String.
     * @param ciphertextBase64  Der verschlüsselte Text als Base64
     * @param key               Der abgeleitete AES-Schlüssel
     * @param ivBase64          Der IV als Base64-String
     * @return                  Der entschlüsselte Klartext
     */
    public String decrypt(String ciphertextBase64, SecretKey key, String ivBase64) throws Exception {
        if (ciphertextBase64 == null || ciphertextBase64.isBlank()) return null;
        byte[] iv = Base64.getDecoder().decode(ivBase64);
        byte[] ciphertext = Base64.getDecoder().decode(ciphertextBase64);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] decrypted = cipher.doFinal(ciphertext);
        return new String(decrypted, "UTF-8");
    }
}
