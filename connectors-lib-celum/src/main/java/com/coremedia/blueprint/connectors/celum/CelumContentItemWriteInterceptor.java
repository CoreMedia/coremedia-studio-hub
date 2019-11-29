package com.coremedia.blueprint.connectors.celum;

import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.content.ConnectorItemWriteInterceptor;
import com.coremedia.cap.common.Blob;
import com.coremedia.rest.cap.intercept.ContentWriteRequest;

import java.util.Map;

/**
 *
 */
public class CelumContentItemWriteInterceptor extends ConnectorItemWriteInterceptor {

  @Override
  public void intercept(ContentWriteRequest request) {
    Map<String, Object> properties = request.getProperties();
    if (properties.containsKey(CONNECTOR_ENTITY)) {
      ConnectorEntity entity = (ConnectorEntity) properties.get(CONNECTOR_ENTITY);

      if (entity instanceof CelumConnectorItem) {
        CelumConnectorItem asset = (CelumConnectorItem) entity;
        //we can't use the CelumConnectorItem#getMimeType() value since we are using different download formats
        //the interceptor runs for CMPicture, so the format is always image/jpeg
        Blob blob = getContentCreateService().createBlob(asset.download(), asset.getName(), "image/jpeg");
        properties.put("data", blob);
      }
    }
  }
}
