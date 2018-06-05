package com.coremedia.blueprint.connectors.canto.rest.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorEntity extends AbstractCantoEntity {

  @JsonProperty("status")
  private int status;

  @JsonProperty("message")
  private String message;

  @JsonProperty("errorCode")
  private String errorCode;

  @JsonProperty("exception")
  private Object exception;

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

  public Object getException() {
    return exception;
  }

  public void setException(Object exception) {
    this.exception = exception;
  }
}
