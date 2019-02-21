package com.coremedia.blueprint.connectors.s7;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

/**
 * @author uheidler
 */
public class EncryptUtils {


    public static final String TYPE = "PBEWithMD5AndDES";
    public static final int ITER_COUNT = 20;
    //private static final char[] SECRET = "(*E#$%^&!@%^&(*&HJHhbgh)".toCharArray();
    private static final char[] SECRET = "%fOP%BymPMg@]R:SnC'WyC/#".toCharArray();
    private static final byte[] SALT = {(byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12, (byte) 0xde, (byte) 0x33,
            (byte) 0x10, (byte) 0x12,};

    protected static String encrypt(String property) throws GeneralSecurityException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(TYPE);
        SecretKey key = keyFactory.generateSecret(new PBEKeySpec(SECRET));
        Cipher pbeCipher = Cipher.getInstance(TYPE);
        pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT, ITER_COUNT));
        return new BASE64Encoder().encode(pbeCipher.doFinal(property.getBytes(StandardCharsets.UTF_8)));
    }

    protected static String decrypt(String property) throws GeneralSecurityException, IOException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(TYPE);
        SecretKey key = keyFactory.generateSecret(new PBEKeySpec(SECRET));
        Cipher pbeCipher = Cipher.getInstance(TYPE);
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, ITER_COUNT));
        return new String(pbeCipher.doFinal(new BASE64Decoder().decodeBuffer(property)), StandardCharsets.UTF_8);
    }
}
