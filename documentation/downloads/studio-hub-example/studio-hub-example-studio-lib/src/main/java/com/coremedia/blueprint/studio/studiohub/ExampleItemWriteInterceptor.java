package com.coremedia.blueprint.studio.studiohub;

import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.content.ConnectorItemWriteInterceptor;
import com.coremedia.rest.cap.intercept.ContentWriteRequest;

import java.util.Map;

public class ExampleItemWriteInterceptor extends ConnectorItemWriteInterceptor {

  @Override
  public void intercept(ContentWriteRequest request) {
    Map<String, Object> properties = request.getProperties();
    if (properties.containsKey(CONNECTOR_ITEM)) {
      ConnectorItem item = (ConnectorItem) properties.get(CONNECTOR_ITEM);

      //the intercepts is only applicable when the content was created for an Example Connector item
      if (item instanceof ExampleConnectorItem) {
        ExampleConnectorItem exampleItem = (ExampleConnectorItem) item;
        properties.put("segment", exampleItem.getName().toLowerCase().replaceAll(" ", "-"));
      }
    }
  }
}
