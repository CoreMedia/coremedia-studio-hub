package com.coremedia.blueprint.connectors.coremedia;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.content.ConnectorItemWriteInterceptor;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.CapPropertyDescriptor;
import com.coremedia.cap.common.CapPropertyDescriptorType;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.struct.Struct;
import com.coremedia.rest.cap.intercept.ContentWriteRequest;
import com.coremedia.xml.Markup;
import com.coremedia.xml.MarkupFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Extracts the images out of an RSS feed entry and adds the to the pictures list of
 * the already created content.
 */
public class CoreMediaConnectorWriteInterceptor extends ConnectorItemWriteInterceptor {
  private static final Logger LOG = LoggerFactory.getLogger(CoreMediaConnectorWriteInterceptor.class);

  @Override
  public ContentType getType() {
    return contentRepository.getContentType(ContentType.CONTENT);
  }

  @Override
  public int getPriority() {
    //ensure execution after the default ConnectorItemWriteInterceptor
    return super.getPriority() + 1;
  }

  @Override
  public void intercept(ContentWriteRequest request) {
    Map<String, Object> properties = request.getProperties();
    if (properties.containsKey(CONNECTOR_ENTITY)) {
      ConnectorEntity entity = (ConnectorEntity) properties.get(CONNECTOR_ENTITY);
      if (entity instanceof CoreMediaConnectorEntity) {
        super.clearDefaultProperties(request);
        Content content = (Content) properties.get(ConnectorItemWriteInterceptor.CONTENT_ITEM);

        if (entity instanceof CoreMediaConnectorItem) {
          Content parent = content.getParent();
          CoreMediaConnectorItem item = (CoreMediaConnectorItem) entity;
          copyItem(parent, item, content);

          //re-set the connectorId since we have overwritten the localSettings
          setConnectorId(content, item.getConnectorId());
        }
        else if (entity instanceof CoreMediaConnectorCategory) {
          CoreMediaConnectorCategory category = (CoreMediaConnectorCategory) entity;
          copyCategory(content.getParent(), category);
        }
      }
    }
  }

  /**
   * Recursive copy of a content folder, represented by a category
   *
   * @param category the category to copy
   */
  private void copyCategory(Content parent, CoreMediaConnectorCategory category) {
    ConnectorContext context = category.getContext();
    ConnectorId id = category.getConnectorId();
    CoreMediaConnectorServiceImpl service = category.getService();

    Content folder = parent.getChild(category.getName());
    if (folder == null) {
      folder = contentRepository.createSubfolders(parent, category.getName());
    }

    //fetch the complete entity for a recursive call
    category = (CoreMediaConnectorCategory) service.getCategory(context, id);
    List<ConnectorCategory> subCategories = category.getSubCategories();
    for (ConnectorCategory subCategory : subCategories) {
      copyCategory(folder, (CoreMediaConnectorCategory) subCategory);
    }

    List<ConnectorItem> items = category.getItems();
    for (ConnectorItem item : items) {
      copyItem(folder, (CoreMediaConnectorItem) item, null);
    }
  }

  private Content copyItem(Content folder, CoreMediaConnectorItem item, Content content) {
    Content original = item.getContent();
    ContentType ct = contentRepository.getContentType(original.getType().getName());
    if (content == null) {
      content = ct.createByTemplate(folder, original.getName(), "{3} ({1})", new HashMap<>());
    }

    try {
      Map<String, Object> properties = original.getProperties();
      Set<Map.Entry<String, Object>> entries = properties.entrySet();
      for (Map.Entry<String, Object> entry : entries) {
        String key = entry.getKey();
        Object value = entry.getValue();

        CapPropertyDescriptor descriptor = original.getType().getDescriptor(key);
        String type = descriptor.getType().name();
        if (type.equals(CapPropertyDescriptorType.STRING.name())) {
          content.set(key, value);
        }
        else if (type.equals(CapPropertyDescriptorType.MARKUP.name())) {
          Markup originalMarkup = original.getMarkup(key);
          if (originalMarkup != null) {
            String xml = originalMarkup.asXml();
            Markup newMarkup = MarkupFactory.fromString(xml);
            content.set(key, newMarkup);
          }
        }
        else if (type.equals(CapPropertyDescriptorType.BLOB.name())) {
          Blob blob = original.getBlob(key);
          if (blob != null && blob.getSize() > 0) {
            Blob newBlob = super.createBlob(blob.getInputStream(), null, blob.getContentType().toString());
            content.set(key, newBlob);
          }
        }
        else if (type.equals(CapPropertyDescriptorType.LINK.name())) {
          List<Content> newLinks = new ArrayList<>();
          List<Content> links = original.getLinks(key);
          for (Content link : links) {
            Content existingLink = contentRepository.getChild(link.getPath());
            if (existingLink != null) {
              newLinks.add(existingLink);
            }
          }
          content.set(key, newLinks);
        }
        else if (type.equals(CapPropertyDescriptorType.STRUCT.name())) {
          Struct struct = original.getStruct(key);
          if(struct != null) {
            Markup originalMarkup = struct.toMarkup();
            String xml = originalMarkup.asXml();
            Markup newMarkup = MarkupFactory.fromString(xml);
            Struct newStruct = contentRepository.getConnection().getStructService().fromMarkup(newMarkup);
            content.set(key, newStruct);
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to copy " + original.getPath() + ": " + e.getMessage(), e);
    } finally {
      content.checkIn();
    }

    return content;
  }
}
