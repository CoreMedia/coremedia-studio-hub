package com.coremedia.blueprint.studio.connectors.rest.representation;

import com.coremedia.cap.content.Content;

/**
 *
 */
public class ContentCreationResultRepresentation {
  private Content content;
  private String error;

  public ContentCreationResultRepresentation(Content content) {
    this.content = content;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public Content getContent() {
    return content;
  }

  public void setContent(Content content) {
    this.content = content;
  }
}
