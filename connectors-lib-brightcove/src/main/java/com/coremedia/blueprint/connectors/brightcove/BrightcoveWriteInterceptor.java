package com.coremedia.blueprint.connectors.brightcove;

import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.content.ConnectorItemWriteInterceptor;
import com.coremedia.rest.cap.intercept.ContentWriteRequest;
import com.coremedia.xml.MarkupFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class BrightcoveWriteInterceptor extends ConnectorItemWriteInterceptor {

  @Override
  public void intercept(ContentWriteRequest request) {
    Map<String, Object> properties = request.getProperties();
    if (properties.containsKey(CONNECTOR_ENTITY)) {
      ConnectorEntity entity = (ConnectorEntity) properties.get(CONNECTOR_ENTITY);

      //the intercepts is only applicable when the content was created for an Example Connector entity
      if (entity instanceof BrightcoveItem) {
        BrightcoveItem exampleItem = (BrightcoveItem) entity;
        properties.put("dataUrl", "http://players.brightcove.net/20318290001/f1d70e07-8480-4bbd-b4ef-747e9333a034_default/index.html?videoId=" + exampleItem.getConnectorId().getExternalId());

        String description = exampleItem.getDescription();
        if (StringUtils.isNotBlank(description)) {
          //build Markup
          String detailText = "<?xml version=\"1.0\" ?><div xmlns=\"http://www.coremedia.com/2003/richtext-1.0\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"><p>" + description + "</p></div>";
          properties.put("detailText", MarkupFactory.fromString(detailText));
        }
      }
    }
  }
}
