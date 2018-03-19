package com.coremedia.blueprint.connectors.rss;

import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.content.ConnectorItemWriteInterceptor;
import com.coremedia.cap.content.Content;
import com.coremedia.rest.cap.intercept.ContentWriteRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Extracts the images out of an RSS feed entry and adds the to the pictures list of
 * the already created content.
 */
public class RssContentItemWriteInterceptor extends ConnectorItemWriteInterceptor {

  @Override
  public void intercept(ContentWriteRequest request) {
    Map<String, Object> properties = request.getProperties();
    if (properties.containsKey(CONNECTOR_ITEM)) {
      ConnectorItem item = (ConnectorItem) properties.get(CONNECTOR_ITEM);

      //the intercepts is only applicable when the content was created for an RSS item
      if (item instanceof RssConnectorItem) {
        RssConnectorItem rssAsset = (RssConnectorItem) item;
        List<String> imageUrls = rssAsset.getImageUrls();

        List<Content> images = new ArrayList<>();

        //read the content from the faked write request
        Content owner = (Content) properties.get(ConnectorItemWriteInterceptor.CONTENT_ITEM);
        for (String imageUrl : imageUrls) {
          if (imageUrl.contains(".gif")) {
            continue;
          }

          String imageName = super.extractNameFromUrl(imageUrl);
          if (imageName.indexOf(".") > 0) {
            imageName = imageName.substring(0, imageName.indexOf("."));
          }
          imageName = imageName + " - Thumbnail";
          Content pictureFromUrl = super.createPictureFromUrl(owner, imageName, imageUrl);
          images.add(pictureFromUrl);
        }

        properties.put("pictures", images);
      }
    }
  }
}
