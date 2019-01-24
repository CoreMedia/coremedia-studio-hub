package com.coremedia.blueprint.connectors.sfmc.rest.documents;

/**
 *
 */
public class SFMCUser extends SFMCEntity {
  private String userId;
  private String email;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}
