package com.coremedia.blueprint.connectors.previewconverters;

import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Converting .md files to HTML
 */
public class CommonMarkConverter implements ConnectorPreviewConverter {
  private static final Logger LOGGER = LoggerFactory.getLogger(CommonMarkConverter.class);

  @Override
  public boolean include(ConnectorItem connectorItem) {
    return connectorItem.getName() != null && connectorItem.getName().endsWith(".md");
  }

  @Nullable
  @Override
  public PreviewConversionResult convert(@Nonnull ConnectorContext context, @Nonnull ConnectorItem connectorItem, @Nonnull File itemTempFile) {
    try {
      Parser parser = Parser.builder().build();
      FileInputStream fileInputStream = new FileInputStream(itemTempFile);
      Reader targetReader = new InputStreamReader(fileInputStream);
      Node document = parser.parseReader(targetReader);
      HtmlRenderer renderer = HtmlRenderer.builder().build();
      fileInputStream.close();
      String renderResult = renderer.render(document);
      return new PreviewConversionResult(renderResult);
    } catch (IOException e) {
      LOGGER.error("Failed to generate markup for item " + connectorItem + ": " + e.getMessage(), e);
    }
    return null;
  }
}
