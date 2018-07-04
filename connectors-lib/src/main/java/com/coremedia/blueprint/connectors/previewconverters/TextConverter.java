package com.coremedia.blueprint.connectors.previewconverters;

import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;

/**
 * Converting items that are marked as text items to HTML
 */
public class TextConverter implements ConnectorPreviewConverter {
  private static final Logger LOGGER = LoggerFactory.getLogger(TextConverter.class);

  @Override
  public boolean include(ConnectorItem item) {
    String type = item.getItemType();
    return type.startsWith("text/");
  }

  @Nullable
  @Override
  public PreviewConversionResult convert(@Nonnull ConnectorContext context, @Nonnull ConnectorItem connectorItem, @Nonnull File itemTempFile) {
    try {
      String textTemplate = context.getPreviewTemplates().getTemplate("text");
      if(textTemplate == null) {
        throw new UnsupportedOperationException("No 'text' template defined in 'Preview Templates'");
      }

      String text = FileUtils.readFileToString(itemTempFile, Charset.defaultCharset());
      MessageFormat form = new MessageFormat(textTemplate);
      String formattedText = form.format(new Object[]{text});
      return new PreviewConversionResult(formattedText);
    } catch (IOException e) {
      LOGGER.error("Failed to generate markup for text item " + connectorItem + ": " + e.getMessage(), e);
    }
    return null;
  }
}
