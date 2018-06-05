package com.coremedia.blueprint.connectors.navigation;


import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorItemTypes;
import com.coremedia.blueprint.connectors.api.ConnectorMetaData;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentType;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NavigationConnectorItem extends NavigationConnectorEntity implements ConnectorItem {

  NavigationConnectorItem(NavigationConnectorServiceImpl service, ConnectorCategory parent, ConnectorContext context, Content content, ConnectorId connectorId) {
    super(service, parent, context, content, connectorId);
  }

  @Override
  public long getSize() {
    return 0;
  }

  @Override
  public Date getLastModified() {
    return content.getModificationDate().getTime();
  }

  @Nullable
  @Override
  public String getDescription() {
    return content.getPath();
  }

  @Nullable
  @Override
  public String getTargetContentType() {
    return null;
  }

  @Nullable
  @Override
  public String getOpenInTabUrl() {
    return null;
  }

  @Nonnull
  @Override
  public String getItemType() {
    ConnectorContext context = getContext();
    ConnectorItemTypes itemTypes = context.getItemTypes();
    if (itemTypes != null) {
      ContentType contentType = content.getType();
      while (contentType != null) {
        String typeForName = itemTypes.getTypeForName(contentType.getName());
        if (typeForName != null) {
          return typeForName;
        }

        contentType = contentType.getParent();
      }
    }
    return "download";
  }

  @Nullable
  @Override
  public ConnectorMetaData getMetaData() {
    Map<String, Object> data = new HashMap<>();
    int contentId = IdHelper.parseContentId(content.getId());
    data.put("id", contentId);
    data.put("path", content.getPath());

    if (content.getEditor() != null) {
      data.put("author", content.getEditor().getName());
    }

    if (content.getProperties().containsKey("title")) {
      String title = content.getString("title");
      if (!StringUtils.isEmpty(title)) {
        data.put("title", title);
      }
    }


    String type = content.getType().getName();
    type = type.replace("CM", "");
    data.put("type", type);

    if (content.getProperties().containsKey("keywords")) {
      if (!StringUtils.isEmpty(content.getString("keywords"))) {
        data.put("keywords", content.getString("keywords"));
      }
    }

    if (content.getProperties().containsKey("viewtype")) {
      Content viewType = content.getLink("viewtype");
      if (viewType != null) {
        data.put("viewType", content.getName());
      }
    }

    if (content.getProperties().containsKey("subjectTaxonomy")) {
      List<Content> subjectTaxonomy = content.getLinks("subjectTaxonomy");
      List<String> values = subjectTaxonomy.stream().map(t -> t.getString("value")).collect(Collectors.toList());
      data.put("tags", String.join(", ", values));
    }

    String link = "<a href=\"javascript:Ext.getCmp('collection-view').showInRepositoryMode(com.coremedia.ui.data.beanFactory.getRemoteBean('content/" + contentId + "'))\">" + content.getName() + "</a>";
    data.put("link", link);

    return () -> data;
  }

  @Override
  public boolean isDownloadable() {
    return false;
  }

  @Nullable
  @Override
  public InputStream stream() {
    return null;
  }
}
