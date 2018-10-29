package com.coremedia.blueprint.connectors.coremedia;

import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorPreviewTemplates;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;
import com.coremedia.xml.Markup;
import com.coremedia.xml.MarkupUtil;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class PreviewFactory {

  static String generatePreviewHTML(CoreMediaConnectorServiceImpl service, CoreMediaConnectorItem item) {
    StringBuilder buffer = new StringBuilder();
    Content content = item.getContent();
    ConnectorContext context = item.getContext();
    ConnectorPreviewTemplates previewTemplates = context.getPreviewTemplates();

    if (content.getType().isSubtypeOf("CMCollection")) {
      String title = content.getString("title");
      List<Content> items = content.getLinks("items");

      if(StringUtils.isEmpty(title) && items.isEmpty()) {
        return null;
      }

      buffer.append("<b>" + title + "</b><br>");
      for (Content contentItem : items) {
        buffer.append("<hr/>");
        ConnectorId connectorId = ConnectorId.createItemId(context.getConnectionId(), contentItem.getId());
        CoreMediaConnectorItem childItem = (CoreMediaConnectorItem) service.getItem(context, connectorId);
        buffer.append(teaseable(service, childItem));
      }
    }
    else if (content.getType().isSubtypeOf("CMDownload")) {
      return null;
    }
    else if (content.getType().isSubtypeOf("CMSettings")) {
      Struct settings = content.getStruct("settings");

      if(settings != null) {
        Markup markup = settings.toMarkup();
        String xml = markup.asXml();

        String template = previewTemplates.getTemplate("text");
        MessageFormat form = new MessageFormat(template);
        String html = form.format(new Object[]{xml});
        buffer.append(html);
      }
    }
    else if (content.getType().isSubtypeOf("CMPicture")) {
      String template = previewTemplates.getTemplate("picture");
      MessageFormat form = new MessageFormat(template);
      String url = item.getStreamUrl();
      String html = form.format(new Object[]{url});
      buffer.append(html);
    }
    else if (content.getType().isSubtypeOf("CMGallery")) {
      List<Content> items = content.getLinks("items");
      if(!items.isEmpty()) {
        Content pic = items.get(0);
        ConnectorId connectorId = ConnectorId.createItemId(context.getConnectionId(), pic.getId());
        ConnectorItem pictureConnectorItem = service.getItem(context, connectorId);

        String template = previewTemplates.getTemplate("picture");
        MessageFormat form = new MessageFormat(template);
        String url = pictureConnectorItem.getStreamUrl();
        String html = form.format(new Object[]{url});
        buffer.append(html);
      }
    }
    else if (content.getType().isSubtypeOf("CMSpinner")  ) {
      List<Content> items = content.getLinks("sequence");
      if(!items.isEmpty()) {
        Content pic = items.get(0);
        ConnectorId connectorId = ConnectorId.createItemId(context.getConnectionId(), pic.getId());
        ConnectorItem pictureConnectorItem = service.getItem(context, connectorId);

        String template = previewTemplates.getTemplate("picture");
        MessageFormat form = new MessageFormat(template);
        String url = pictureConnectorItem.getStreamUrl();
        String html = form.format(new Object[]{url});
        buffer.append(html);
      }
    }
    else if (content.getType().isSubtypeOf("CMTeasable")) {
      buffer.append(teaseable(service, item));
    }
    else {
      return null;
    }

    return buffer.toString();
  }

  private static String teaseable(CoreMediaConnectorServiceImpl service, CoreMediaConnectorItem item) {
    Content content = item.getContent();
    StringBuilder buffer = new StringBuilder();

    String title = content.getString("title");
    if(StringUtils.isEmpty(title)) {
      title = content.getName();
    }
    buffer.append("<b>" + title + "</b><br>");

    String teaserText = MarkupUtil.asPlainText(content.getMarkup("teaserText"));
    if (!StringUtils.isEmpty(teaserText)) {
      buffer.append(teaserText);
      buffer.append("<br>");
      buffer.append("<br>");
    }

    ConnectorContext context = item.getContext();
    ConnectorPreviewTemplates previewTemplates = context.getPreviewTemplates();
    String template = previewTemplates.getTemplate("picture");
    MessageFormat form = new MessageFormat(template);

    List<Content> pictures = new ArrayList<>();
    pictures.addAll(content.getLinks("pictures"));
    if(content.getType().getName().equals("CMPicture")) {
      pictures.add(content);
    }

    for (Content picture : pictures) {
      ConnectorId connectorId = ConnectorId.createItemId(context.getConnectionId(), picture.getId());
      ConnectorItem pictureConnectorItem = service.getItem(context, connectorId);
      String url = pictureConnectorItem.getStreamUrl();
      String html = form.format(new Object[]{url});
      buffer.append(html);
      buffer.append("<br>");
    }

    String detailText = MarkupUtil.asPlainText(content.getMarkup("detailText"));
    if (!StringUtils.isEmpty(detailText)) {
      buffer.append(detailText);
    }

    return buffer.toString();
  }
}
