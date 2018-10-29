package com.coremedia.blueprint.connectors.coremedia;


import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorColumnValue;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorMetaData;
import com.coremedia.blueprint.connectors.library.DefaultConnectorColumnValue;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.CapPropertyDescriptor;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.coremedia.blueprint.connectors.coremedia.CoreMediaConnectorServiceImpl.BLOB_PROPERTY_NAME;

public class CoreMediaConnectorItem extends CoreMediaConnectorEntity implements ConnectorItem {

  CoreMediaConnectorItem(CoreMediaConnectorServiceImpl service, ConnectorCategory parent, ConnectorContext context, Content content, ConnectorId connectorId) {
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
    return content.getType().getName();
  }

  @Nullable
  @Override
  public String getOpenInTabUrl() {
    if(service.isDownloadable(getContent())) {
      return ConnectorItem.super.getOpenInTabUrl();
    }
    return null;
  }


  @Override
  public List<ConnectorColumnValue> getColumnValues() {
    String lifecycle = service.getLifecycle(content);
    String docType = content.getType().getName();
    return Arrays.asList(new DefaultConnectorColumnValue(lifecycle, "status", null, lifecycle));
  }

  @Override
  public String getMimeType() {
    CapPropertyDescriptor descriptor = content.getType().getDescriptor(BLOB_PROPERTY_NAME);
    if(descriptor != null) {
      Blob data = content.getBlob(descriptor.getName());
      if (data != null && data.getContentType() != null) {
        return data.getContentType().toString();
      }
    }

    return null;
  }

  @Nullable
  @Override
  public String getPreviewHtml() {
    String html = PreviewFactory.generatePreviewHTML(service, this);
    if(html != null) {
      return html;
    }

    html = ConnectorItem.super.getPreviewHtml();
    return html;
  }

  @NonNull
  @Override
  public String getItemType() {
    return content.getType().getName();
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
    return service.isDownloadable(content);
  }

  @Nullable
  @Override
  public InputStream stream() {
    return service.stream(content);
  }
}
