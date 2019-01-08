package com.coremedia.blueprint.connectors.s7;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 */

public class EncryptionUtilsTest {

  private static final String PASSWORD = "C0remedia#";

  @Test
  public void encryptDecryptPassword() throws GeneralSecurityException, IOException {
    String encrypt = EncryptUtils.encrypt(PASSWORD);
    System.out.println(encrypt);
    Assert.assertNotEquals(PASSWORD, encrypt);
    String decrypt = EncryptUtils.decrypt(encrypt);
    System.out.println(decrypt);
    Assert.assertEquals(PASSWORD, decrypt);


  }
}
