package com.coremedia.blueprint.connectors.api;

/**
 * A runtime exception used for connector based errors.
 */
public class ConnectorException extends RuntimeException {
  public ConnectorException(String msg) {
    super(msg);
  }

  public ConnectorException(Throwable cause) {
    super(cause);
  }

  public ConnectorException(String message, Throwable cause) {
    super(message, cause);
  }
}
