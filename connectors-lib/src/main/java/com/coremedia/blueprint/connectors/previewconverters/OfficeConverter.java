package com.coremedia.blueprint.connectors.previewconverters;

import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.ExpandedTitleContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converting items that are marked as text items to HTML
 */
public class OfficeConverter implements ConnectorPreviewConverter {
  private static final Logger LOGGER = LoggerFactory.getLogger(OfficeConverter.class);

  private static final List<String> IGNORE_LIST = Arrays.asList("Content-Type", "xmpTPg:NPages", "X-Parsed-By",
          "dc:creator", "cp:revision", "dcterms:modified", "meta:page-count", "meta:character-count",
          "extended-properties:AppVersion", "meta:creation-date", "custom:AppVersion", "extended-properties:PresentationFormat",
          "custom:MMClips", "custom:LinksUpToDate", "custom:ShareDoc", "custom:HyperlinksChanged");

  @Override
  public boolean include(ConnectorItem item) {
    return item.getName().endsWith(".pptx")
              || item.getName().endsWith(".docx")
              || item.getName().endsWith(".xlsx")
              || item.getName().endsWith(".odt")
              || item.getName().endsWith(".ods")
              || item.getName().endsWith(".odp");
  }

  @Nullable
  @Override
  public PreviewConversionResult convert(@NonNull ConnectorContext context, @NonNull ConnectorItem connectorItem, @NonNull File itemTempFile) {
    try {
      AutoDetectParser tikaParser = new AutoDetectParser();
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      SAXTransformerFactory factory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
      TransformerHandler handler = factory.newTransformerHandler();
      handler.getTransformer().setOutputProperty(OutputKeys.METHOD, "html");
      handler.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
      handler.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      handler.setResult(new StreamResult(out));
      ExpandedTitleContentHandler handler1 = new ExpandedTitleContentHandler(handler);

      FileInputStream fileInputStream = new FileInputStream(itemTempFile);
      Metadata metadata = new Metadata();
      tikaParser.parse(fileInputStream, handler1, metadata);
      byte[] bytes = out.toByteArray();
      fileInputStream.close();
      String converted = new String(bytes, "UTF-8");
      return new PreviewConversionResult(converted, toMap(metadata));
    } catch (Exception e) {
      LOGGER.error("Failed to generate markup for word item " + connectorItem + ": " + e.getMessage(), e);
    }
    return null;
  }

  private Map<String, Object> toMap(Metadata metadata) {
    Map<String, Object> result = new HashMap<>();
    String[] names = metadata.names();
    for (String name : names) {
      if (IGNORE_LIST.contains(name)) {
        continue;
      }

      String formattedName = name;
      if (name.contains(":")) {
        formattedName = formattedName.substring(formattedName.indexOf(":") + 1, formattedName.length());
      }
      result.put(formattedName, metadata.get(name));
    }
    return result;
  }
}
