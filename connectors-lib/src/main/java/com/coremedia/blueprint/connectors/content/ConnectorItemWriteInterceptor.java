package com.coremedia.blueprint.connectors.content;

import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.CapPropertyDescriptor;
import com.coremedia.cap.common.CapPropertyDescriptorType;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.struct.StructBuilder;
import com.coremedia.cap.struct.StructService;
import com.coremedia.rest.cap.intercept.ContentWriteInterceptorBase;
import com.coremedia.rest.cap.intercept.ContentWriteRequest;
import com.coremedia.xml.Markup;
import com.coremedia.xml.MarkupFactory;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Required;

import java.util.Map;

import static com.coremedia.blueprint.connectors.impl.ConnectorPropertyNames.CONNECTOR_ID;

/**
 * The default ConnectorItemWriteInterceptor used for the content creation of connector items.
 * Since the content creation is based on ContentWriteInterceptors, additional interceptors may
 * have already been executed before this interceptor which is why this one should come first and has a priority of 0.
 * <p>
 * The ContentWriteInterceptors for connector item based content creation can access the request parameters
 * 'connectorItem' and 'content' to access the connector item the content has been created for and the default content
 * that has already been created before the interceptors is executed.
 */
public class ConnectorItemWriteInterceptor extends ContentWriteInterceptorBase {
  private static final String LOCAL_SETTINGS = "localSettings";

  public final static String CONNECTOR_ENTITY = "connectorEntity";
  public final static String CONTENT_ITEM = "content";
  public final static String CONNECTOR_CONTEXT = "connectorContext";
  public static final String MEDIA_DOC_TYPE = "CMMedia";
  public static final String ARTICLE_DOCTYPE = "CMArticle";
  public static final String COLLECTION_DOCTYPE = "CMCollection";
  public static final String PAGE_DOCTYPE = "CMChannel";

  private ContentCreateService contentCreateService;

  @Override
  public void intercept(ContentWriteRequest request) {
    Map<String, Object> properties = request.getProperties();
    if (properties.containsKey(CONNECTOR_ENTITY)) {
      ConnectorEntity entity = (ConnectorEntity) properties.get(CONNECTOR_ENTITY);

      if (entity instanceof ConnectorItem) {
        ConnectorItem item = (ConnectorItem) entity;
        properties.put("title", entity.getDisplayName());
        properties.put("url", item.getOpenInTabUrl());
        properties.put("teaserText", createMarkup(item.getDescription()));
        Blob blob = getContentCreateService().createBlob(item.download(), item.getName(), item.getMimeType());
        properties.put("data", blob);
      }
    }
  }

  // ---------------------- Helper -------------------------------------------------------------------------------------

  protected void clearDefaultProperties(ContentWriteRequest request) {
    Map<String, Object> properties = request.getProperties();
    properties.remove("title");
    properties.remove("url");
    properties.remove("teaserText");
    properties.remove("data");
  }

  protected void setConnectorId(@NonNull Content content, @NonNull ConnectorId id) {
    CapPropertyDescriptor descriptor = content.getType().getDescriptor(LOCAL_SETTINGS);
    if (descriptor != null && descriptor.getType().equals(CapPropertyDescriptorType.STRUCT)) {
      Struct struct = content.getStruct(LOCAL_SETTINGS);
      if (struct == null) {
        StructService structService = content.getRepository().getConnection().getStructService();
        struct = structService.emptyStruct();
      }

      StructBuilder builder = struct.builder();
      builder.set(CONNECTOR_ID, id.toString());
      Struct updatedStruct = builder.build();
      if (!content.isCheckedOut()) {
        content.checkOut();
      }
      content.set(LOCAL_SETTINGS, updatedStruct);
      content.checkIn();
      content.getRepository().getConnection().flush();
    }
  }

  protected Markup createMarkup(@Nullable String description) {
    if (description != null) {
      description = description.replaceAll("\\<.*?\\s*.*?\\>", "").replaceAll("&nbsp;", "");
      description = StringEscapeUtils.unescapeHtml4(description);
      String markup = "<?xml version=\"1.0\" ?><div xmlns=\"http://www.coremedia.com/2003/richtext-1.0\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"><p>" + description + "</p></div>";
      return MarkupFactory.fromString(markup);
    }
    return null;
  }

  /**
   * Tries to resolve a meaningful image name out of an image URL.
   *
   * @param imageUrl the URL to extract the name from
   */
  protected String extractNameFromUrl(String imageUrl) {
    String[] split = imageUrl.split("/");
    String nameSegment = split[split.length - 1];
    if (nameSegment.contains(".")) {
      nameSegment = nameSegment.substring(0, nameSegment.lastIndexOf("."));
    }
    else if (nameSegment.contains("?")) {
      nameSegment = nameSegment.substring(0, nameSegment.indexOf("?"));
    }

    if (!nameSegment.endsWith(".jpg") || !nameSegment.endsWith(".png")) {
      nameSegment = nameSegment + ".jpg";
    }

    return nameSegment;
  }

  //---------------------------- Spring --------------------------------------------------------------------------------

  @Required
  public void setContentCreateService(ContentCreateService contentCreateService) {
    this.contentCreateService = contentCreateService;
  }

  public ContentCreateService getContentCreateService() {
    return this.contentCreateService;
  }
}
