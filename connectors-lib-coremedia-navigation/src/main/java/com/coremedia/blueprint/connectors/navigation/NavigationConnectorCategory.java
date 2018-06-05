package com.coremedia.blueprint.connectors.navigation;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorColumn;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorMetaData;
import com.coremedia.blueprint.connectors.library.DefaultConnectorColumn;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NavigationConnectorCategory extends NavigationConnectorEntity implements ConnectorCategory {
  private List<ConnectorCategory> subCategories = new ArrayList<>();
  private List<ConnectorItem> items = new ArrayList<>();

  NavigationConnectorCategory(NavigationConnectorServiceImpl service, ConnectorCategory parent, ConnectorContext context, Content navigation, ConnectorId id) {
    super(service, parent, context, navigation, id);
  }

  @Override
  public String getType() {
    if (getConnectorId().isRootId()) {
      return "navigation";
    }

    Integer hidden = content.getInteger("hidden");
    if(hidden != null && hidden == 1) {
      return "navigation_hidden";
    }

    return "navigation";
  }

  @Nullable
  @Override
  public ConnectorMetaData getMetaData() {
    Map<String, Object> data = new HashMap<>();
    data.put("id", content.getId());
    data.put("path", content.getPath());

    int contentId = IdHelper.parseContentId(content.getId());
    String link = "<a href=\"javascript:Ext.getCmp('collection-view').showInRepositoryMode(com.coremedia.ui.data.beanFactory.getRemoteBean('content/" + contentId + "'))\">" + content.getName() + "</a>";
    data.put("link", link);
    return () -> data;
  }

  @Nonnull
  @Override
  public List<ConnectorColumn> getColumns() {
    return Arrays.asList(new DefaultConnectorColumn("status", "status", 50, 2),
            new DefaultConnectorColumn("docType", "docType", 100, 3));
  }

  @Nonnull
  @Override
  public List<ConnectorCategory> getSubCategories() {
    return subCategories;
  }

  @Nonnull
  @Override
  public List<ConnectorItem> getItems() {
    return items;
  }

  @Override
  public boolean isWriteable() {
    return false;
  }

  @Nullable
  @Override
  public Date getLastModified() {
    return content.getModificationDate().getTime();
  }

  @Override
  public Boolean isDeleteable() {
    return false;
  }

  @Override
  public Boolean delete() {
    return false;
  }

  void setSubCategories(List<ConnectorCategory> subCategories) {
    this.subCategories = subCategories;
  }

  void setItems(List<ConnectorItem> items) {
    this.items = items;
  }

  public boolean containsSubCategory(Content childDocument) {
    for (ConnectorCategory connectorCategory : getSubCategories()) {
      NavigationConnectorCategory category = (NavigationConnectorCategory) connectorCategory;
      if(category.getContent() != null && category.getContent().equals(childDocument)) {
        return true;
      }
    }
    return false;
  }
}
