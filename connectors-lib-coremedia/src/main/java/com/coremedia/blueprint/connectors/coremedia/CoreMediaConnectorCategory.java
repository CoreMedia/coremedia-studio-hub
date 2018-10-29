package com.coremedia.blueprint.connectors.coremedia;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorColumn;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorMetaData;
import com.coremedia.blueprint.connectors.library.DefaultConnectorColumn;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.results.BulkOperationResult;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoreMediaConnectorCategory extends CoreMediaConnectorEntity implements ConnectorCategory {
  private List<ConnectorCategory> subCategories = new ArrayList<>();
  private List<ConnectorItem> items = new ArrayList<>();

  CoreMediaConnectorCategory(CoreMediaConnectorServiceImpl service, ConnectorCategory parent, ConnectorContext context, Content folder, ConnectorId id) {
    super(service, parent, context, folder, id);
  }

  @Override
  public String getType() {
    if (getConnectorId().isRootId()) {
      return "coremedia";
    }
    return ConnectorCategory.super.getType();
  }

  @Nullable
  @Override
  public ConnectorMetaData getMetaData() {
    Map<String, Object> data = new HashMap<>();
    int contentId = IdHelper.parseContentId(content.getId());
    data.put("id", contentId);
    data.put("path", content.getPath());
    return () -> data;
  }

  @NonNull
  @Override
  public List<ConnectorColumn> getColumns() {
    return Arrays.asList(new DefaultConnectorColumn("status", "status", 50, 2));
  }

  @Nullable
  @Override
  public String getManagementUrl() {
    return null;
  }

  @NonNull
  @Override
  public List<ConnectorCategory> getSubCategories() {
    return subCategories;
  }

  @NonNull
  @Override
  public List<ConnectorItem> getItems() {
    return items;
  }

  @Override
  public boolean isWriteable() {
    return service.isWriteable(content);
  }

  @Nullable
  @Override
  public Date getLastModified() {
    return content.getModificationDate().getTime();
  }

  @Override
  public boolean isDeleteable() {
    return service.isDeleteable(content);
  }

  @Override
  public boolean delete() {
    BulkOperationResult delete = content.delete();
    return delete.isSuccessful();
  }

  void setSubCategories(List<ConnectorCategory> subCategories) {
    this.subCategories = subCategories;
  }

  void setItems(List<ConnectorItem> items) {
    this.items = items;
  }
}
