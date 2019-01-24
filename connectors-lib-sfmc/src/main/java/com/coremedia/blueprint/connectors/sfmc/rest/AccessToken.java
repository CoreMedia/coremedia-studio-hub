package com.coremedia.blueprint.connectors.sfmc.rest;

import java.util.Date;

/**
 *
 */
public class AccessToken {
  private long creationTimeSeconds = new Date().getTime()/1000;
  private String accessToken;
  private long expiresIn;

  public boolean isValid() {
    long livingSinceSeconds = new Date().getTime()/1000-creationTimeSeconds;
    return livingSinceSeconds < expiresIn;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public long getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(long expiresIn) {
    this.expiresIn = expiresIn;
  }
}
