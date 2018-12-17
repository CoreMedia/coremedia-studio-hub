package com.coremedia.blueprint.connectors.metadataresolver;

import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorMetaData;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static com.coremedia.blueprint.connectors.metadataresolver.PreviewHelper.formatKey;

/**
 * Tries to extract the MP3 metadata out of the item's tmp file.
 */
public class AudioMetaDataResolver implements ConnectorMetaDataResolver {
  private static final Logger LOG = LoggerFactory.getLogger(AudioMetaDataResolver.class);

  @Override
  public boolean test(ConnectorItem item) {
    return item.getItemType().equals("audio");
  }

  @Override
  public ConnectorMetaData resolveMetaData(ConnectorItem item, File itemTempFile) {
    final Map<String, Object> result = new HashMap<>();
    InputStream input = null;
    try {
      org.apache.tika.metadata.Metadata metadata = new org.apache.tika.metadata.Metadata();
      input = new BufferedInputStream(new FileInputStream(itemTempFile));

      ContentHandler handler = new DefaultHandler();
      Parser parser = new Mp3Parser();
      ParseContext parseCtx = new ParseContext();
      parser.parse(input, handler, metadata, parseCtx);
      input.close();

      // List all metadata
      String[] metadataNames = metadata.names();

      for (String name : metadataNames) {
        result.put(formatKey(name), metadata.get(name));
      }
    }
    catch (Exception e) {
      LOG.warn("Error processing audio meta data of {}", item, e);
    }
    finally {
      if(input != null) {
        try {
          input.close();
        } catch (IOException e) {
          //ignore
        }
      }
    }
    return () -> result;
  }
}
