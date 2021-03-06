package com.coremedia.blueprint.connectors.youtube;

import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.content.ConnectorItemWriteInterceptor;
import com.coremedia.cap.content.Content;
import com.coremedia.rest.cap.intercept.ContentWriteRequest;
import com.google.api.services.youtube.model.ThumbnailDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Sets the youtube video URL
 */
public class YouTubeContentItemWriteInterceptor extends ConnectorItemWriteInterceptor {

  @Override
  public void intercept(ContentWriteRequest request) {
    Map<String, Object> properties = request.getProperties();
    if (properties.containsKey(CONNECTOR_ENTITY)) {
      ConnectorEntity entity = (ConnectorEntity) properties.get(CONNECTOR_ENTITY);

      //no actual blob, so we re-use the method to set the youtube video URL
      if (entity instanceof YouTubeConnectorVideo) {
        YouTubeConnectorVideo video = (YouTubeConnectorVideo) entity;
        properties.put("dataUrl", "https://www.youtube.com/watch?v=" + video.getVideo().getId());

        List<Content> pictures = new ArrayList<>();
        Content owner = (Content) properties.get(ConnectorItemWriteInterceptor.CONTENT_ITEM);
        ThumbnailDetails thumbnails = video.getVideo().getSnippet().getThumbnails();
        if (thumbnails != null) {
          String url = null;
          if(thumbnails.getMaxres() != null) {
            url = thumbnails.getMaxres().getUrl();
          }

          if(url == null && thumbnails.getHigh() != null) {
            url = thumbnails.getHigh().getUrl();
          }

          if(url == null && thumbnails.getDefault() != null) {
            url = thumbnails.getDefault().getUrl();
          }

          if (url != null) {
            String imageName = owner.getName() + " - Thumbnail";
            Content pictureFromUrl = getContentCreateService().createPictureFromUrl(owner, imageName, url);
            pictures.add(pictureFromUrl);
          }
        }

        properties.put("pictures", pictures);
      }
    }
  }
}
