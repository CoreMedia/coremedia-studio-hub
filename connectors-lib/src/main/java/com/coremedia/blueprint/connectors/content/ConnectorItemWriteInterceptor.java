package com.coremedia.blueprint.connectors.content;

import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorException;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.CapPropertyDescriptor;
import com.coremedia.cap.common.CapPropertyDescriptorType;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.struct.StructBuilder;
import com.coremedia.cap.struct.StructService;
import com.coremedia.mimetype.MimeTypeService;
import com.coremedia.rest.cap.intercept.ContentWriteInterceptorBase;
import com.coremedia.rest.cap.intercept.ContentWriteRequest;
import com.coremedia.xml.Markup;
import com.coremedia.xml.MarkupFactory;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
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
  private static final Logger LOG = LoggerFactory.getLogger(ConnectorItemWriteInterceptor.class);
  private static final String LOCAL_SETTINGS = ConnectorContentServiceImpl.LOCAL_SETTINGS;

  public final static String CONNECTOR_ENTITY = "connectorEntity";
  public final static String CONTENT_ITEM = "content";
  public final static String CONNECTOR_CONTEXT = "connectorContext";
  public static final String PICTURE_DOC_TYPE = "CMPicture";
  public static final String VIDEO_DOC_TYPE = "CMVideo";
  public static final String MEDIA_DOC_TYPE = "CMMedia";
  public static final String ARTICLE_DOCTYPE = "CMArticle";
  public static final String COLLECTION_DOCTYPE = "CMCollection";
  public static final String PAGE_DOCTYPE = "CMChannel";

  protected MimeTypeService mimeTypeService;
  protected ContentRepository contentRepository;

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
        properties.put("data", createBlob(item.stream(), item.getName(), item.getMimeType()));
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

  protected void setConnectorId(@Nonnull Content content, @Nonnull ConnectorId id) {
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

  protected Blob createBlob(InputStream in, String name, String mimeType) {
    try {
      if (in != null) {
        if (mimeType == null) {
          mimeType = mimeTypeService.getMimeTypeForResourceName(name);
        }
        MimeType mt = new MimeType(mimeType);
        Blob blob = contentRepository.getConnection().getBlobService().fromInputStream(in, mt);
        in.close();
        return blob;
      }
    } catch (MimeTypeParseException e) {
      LOG.error("Failed to resolve blob mime type for " + name + ": " + e.getMessage(), e);
    } catch (IOException e) {
      LOG.error("Error creating blob for item " + name + ": " + e.getMessage(), e);
    }
    return null;
  }


  /**
   * Creates a picture out of an URL.
   *
   * @param imageUrl the URL the image should be loaded from
   * @return the newly created image content
   */
  protected Content createPictureFromUrl(Content owner, String imageName, String imageUrl) {
    Content picture = createContent(owner.getParent().getPath(), imageName, PICTURE_DOC_TYPE);
    try {
      picture.set("title", imageName);

      URL url = new URL(imageUrl);
      URLConnection con = url.openConnection();
      InputStream in = con.getInputStream();
      Blob blob = createBlob(in, imageName, "image/jpeg");
      picture.set("data", blob);
      in.close();

      return picture;
    } catch (Exception e) {
      LOG.error("Failed to create image content: " + e.getMessage(), e);
    } finally {
      if (picture != null) {
        picture.checkIn();
      }
    }
    return null;
  }

  /**
   * Creates the content for the given attributes
   *
   * @param folder      the folder to create the new content for
   * @param name        the name of the new content
   * @param contentType the content type of the new content
   * @return the newly created content
   */
  protected Content createContent(@Nonnull String folder, @Nonnull String name, @Nonnull String contentType) {
    ContentType ct = contentRepository.getContentType(contentType);
    Content folderContent = contentRepository.getChild(folder);
    if (ct != null) {
      return ct.createByTemplate(folderContent, name, "{3} ({1})", new HashMap<>());
    }
    throw new ConnectorException("No content type '" + contentType + "' found for connector item content creation");
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
      nameSegment = nameSegment.substring(0, nameSegment.indexOf("."));
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
  public void setMimeTypeService(MimeTypeService mimeTypeService) {
    this.mimeTypeService = mimeTypeService;
  }

  @Required
  public void setContentRepository(ContentRepository contentRepository) {
    this.contentRepository = contentRepository;
  }
}
