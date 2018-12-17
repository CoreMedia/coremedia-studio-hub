package com.coremedia.blueprint.connectors.metadataresolver;

import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorMetaData;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.coremedia.blueprint.connectors.metadataresolver.PreviewHelper.formatDate;

/**
 * PDF metadata extraction.
 */
public class PdfMetaDataResolver implements ConnectorMetaDataResolver {
  private static final Logger LOG = LoggerFactory.getLogger(PdfMetaDataResolver.class);

  @Override
  public boolean test(ConnectorItem item) {
    return item.getItemType().equals("pdf");
  }

  @Override
  public ConnectorMetaData resolveMetaData(ConnectorItem item, File itemTempFile) {
    final Map<String, Object> result = new HashMap<>();
    try {
      PDDocument doc = PDDocument.load(itemTempFile);
      PDDocumentInformation info = doc.getDocumentInformation();
      result.put("author", info.getAuthor());
      result.put("creator", info.getCreator());
      result.put("subject", info.getSubject());
      result.put("creationDate", formatDate(info.getCreationDate()));
      result.put("modificationDate", formatDate(info.getModificationDate()));
      result.put("pageCount", doc.getNumberOfPages());
      result.put("printable", doc.getCurrentAccessPermission().canPrint());
      result.put("readonly", doc.getCurrentAccessPermission().isReadOnly());
      doc.close();
    } catch (IOException e) {
      LOG.warn("Error processing pdf meta data of {}", item, e);
    }
    return () -> result;
  }
}
