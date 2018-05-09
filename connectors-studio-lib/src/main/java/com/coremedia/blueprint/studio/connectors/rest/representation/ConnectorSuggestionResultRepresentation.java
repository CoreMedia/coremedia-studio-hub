package com.coremedia.blueprint.studio.connectors.rest.representation;

import java.util.Collections;
import java.util.List;

public class ConnectorSuggestionResultRepresentation {
  private final List<ConnectorSuggestionRepresentation> suggestions;

  public ConnectorSuggestionResultRepresentation(List<ConnectorSuggestionRepresentation> suggestions) {
    if (suggestions == null) {
      throw new IllegalArgumentException("parameter is null: suggestions");
    }
    else {
      this.suggestions = suggestions;
    }
  }

  public List<ConnectorSuggestionRepresentation> getSuggestions() {
    return Collections.unmodifiableList(this.suggestions);
  }
}
