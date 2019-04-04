package com.coremedia.blueprint.connectors.navigation;


import com.coremedia.connectors.api.ConnectorCategory;
import com.coremedia.connectors.api.ConnectorColumnValue;
import com.coremedia.connectors.api.ConnectorContext;
import com.coremedia.connectors.api.ConnectorEntity;
import com.coremedia.connectors.api.ConnectorId;
import com.coremedia.connectors.library.DefaultConnectorColumnValue;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Arrays;
import java.util.List;

abstract public class NavigationConnectorEntity implements ConnectorEntity {

  private ConnectorId connectorId;
  private String name;
  private ConnectorContext context;

  protected Content content;

  ConnectorCategory parent;
  NavigationConnectorServiceImpl service;


  NavigationConnectorEntity(NavigationConnectorServiceImpl service, ConnectorCategory parent, ConnectorContext context, Content content, ConnectorId connectorId) {
    this.service = service;
    this.context = context;
    this.connectorId = connectorId;
    this.parent = parent;
    this.content = content;
    if (content != null) {  //ROOT
      this.name = content.getName();
    }
  }

  @Nullable
  @Override
  public String getPreviewHtml() {
    String url = service.getPreviewUrl(getContext(), content);
    if (url != null) {
      String wrapperHtml = "<div class=\"x-container cm-preview-device x-container-default\" role=\"presentation\" " +
              "style=\"width:1280px; height: 390px; top: 0px; transform-origin: left top 0px; transform: scale(0.3);\">";
      String iFrame = "<iframe src=\"" + url + "\" class=\"cm-video\" frameborder=\"0\" style=\"width:100%;height:1280px;\" webkitAllowFullScreen=\"\" allowFullScreen=\"\"></iframe>";
      return wrapperHtml + iFrame + "</div>";
    }
    return null;
  }

  public NavigationConnectorServiceImpl getService() {
    return service;
  }

  public Content getContent() {
    return content;
  }

  @Override
  public boolean isDeleteable() {
    return false;
  }

  @Override
  public boolean delete() {
    return false;
  }

  @NonNull
  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @NonNull
  @Override
  public ConnectorContext getContext() {
    return context;
  }

  @Override
  public ConnectorCategory getParent() {
    return parent;
  }

  @NonNull
  @Override
  public String getDisplayName() {
    return getName();
  }

  @NonNull
  @Override
  public ConnectorId getConnectorId() {
    return connectorId;
  }

  @Nullable
  @Override
  public String getManagementUrl() {
    int contentId = IdHelper.parseContentId(content.getId());
    return "javascript:com.coremedia.cms.editor.sdk.editorContext.getWorkAreaTabManager().openTabForEntity(com.coremedia.ui.data.beanFactory.getRemoteBean('content/" + contentId + "'))";
  }

  @Override
  public List<ConnectorColumnValue> getColumnValues() {
    String lifecycle = service.getLifecycle(content);
    return Arrays.asList(new DefaultConnectorColumnValue(lifecycle, "status", null, lifecycle));
  }
}
