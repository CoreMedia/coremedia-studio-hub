package com.coremedia.blueprint.connectors.cae.link;

import com.coremedia.connectors.api.ConnectorItem;
import com.coremedia.objectserver.web.links.Link;

import java.util.Map;

@Link
public class ConnectorLinkScheme {

  @Link(type = {ConnectorItem.class})
  public String buildConnectorItemLink(ConnectorItem item, Map<String, Object> linkParameters) {
    return null;
  }
}
