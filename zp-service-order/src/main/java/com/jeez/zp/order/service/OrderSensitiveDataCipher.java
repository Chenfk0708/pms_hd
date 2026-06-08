package com.jeez.zp.order.service;

import com.jeez.zp.order.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class OrderSensitiveDataCipher {

    private static final String PREFIX = "enc:v1:";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final String secret;
    private final SecureRandom secureRandom = new SecureRandom();

    public OrderSensitiveDataCipher(@Value("${order.security.id-card-secret:}") String secret) {
        this.secret = secret == null ? "" : secret.trim();
    }

    public String encryptIdCard(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            return plainText;
        }
        if (plainText.startsWith(PREFIX)) {
            return plainText;
        }
        byte[] iv = new byte[IV_LENGTH];
        secureRandom.nextBytes(iv);
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec(), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            ByteBuffer payload = ByteBuffer.allocate(iv.length + encrypted.length);
            payload.put(iv);
            payload.put(encrypted);
            return PREFIX + Base64.getEncoder().encodeToString(payload.array());
        } catch (GeneralSecurityException exception) {
            throw new BusinessException(50001, "证件号加密失败");
        }
    }

    public String decryptIdCard(String value) {
        if (value == null || value.isBlank() || !value.startsWith(PREFIX)) {
            return value;
        }
        byte[] payload;
        try {
            payload = Base64.getDecoder().decode(value.substring(PREFIX.length()));
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(50001, "证件号密文格式错误");
        }
        if (payload.length <= IV_LENGTH) {
            throw new BusinessException(50001, "证件号密文格式错误");
        }

        try {
            ByteBuffer buffer = ByteBuffer.wrap(payload);
            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec(), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException exception) {
            throw new BusinessException(50001, "证件号解密失败");
        }
    }

    private SecretKeySpec keySpec() {
        if (secret.isBlank()) {
            throw new BusinessException(50001, "缺少证件号加密密钥");
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(digest, "AES");
        } catch (GeneralSecurityException exception) {
            throw new BusinessException(50001, "证件号加密密钥初始化失败");
        }
    }
}
