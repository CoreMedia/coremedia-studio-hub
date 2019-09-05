package com.coremedia.blueprint.studio.connectors.rest;

import com.coremedia.blueprint.connectors.api.ConnectorConnection;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorMetaData;
import com.coremedia.blueprint.connectors.caching.TempFile;
import com.coremedia.blueprint.connectors.caching.TempFileCacheService;
import com.coremedia.blueprint.connectors.metadataresolver.ConnectorMetaDataResolver;
import com.coremedia.blueprint.connectors.previewconverters.ConnectorPreviewConverter;
import com.coremedia.blueprint.connectors.previewconverters.PreviewConversionResult;
import com.coremedia.blueprint.studio.connectors.rest.representation.ConnectorItemRepresentation;
import com.coremedia.blueprint.studio.connectors.rest.representation.ConnectorPreviewRepresentation;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.mimetype.MimeTypeService;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

import static com.coremedia.blueprint.studio.connectors.rest.ConnectorEntityResource.ID;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * A resource to receive pictures.
 */
@RestController
@RequestMapping(value = "connector/item/{" + ID + ":.+}")
public class ConnectorItemResource extends ConnectorEntityResource<ConnectorItem> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorItemResource.class);

  private List<ConnectorPreviewConverter> connectorPreviewConverters;
  private List<ConnectorMetaDataResolver> connectorMetaDataResolvers;
  private MimeTypeService mimeTypeService;
  private ContentRepository contentRepository;
  private TempFileCacheService tempFileCacheService;

  private enum QueryMode {
    STREAM, DOWNLOAD, OPEN
  }

  public ConnectorItemResource(List<ConnectorPreviewConverter> connectorPreviewConverters, List<ConnectorMetaDataResolver> connectorMetaDataResolvers, MimeTypeService mimeTypeService, ContentRepository contentRepository, TempFileCacheService tempFileCacheService) {
    this.connectorPreviewConverters = connectorPreviewConverters;
    this.connectorMetaDataResolvers = connectorMetaDataResolvers;
    this.mimeTypeService = mimeTypeService;
    this.contentRepository = contentRepository;
    this.tempFileCacheService = tempFileCacheService;
  }

  @Override
  protected ConnectorItem doGetEntity() {
    ConnectorId id = ConnectorId.toId(getDecodedId());
    ConnectorConnection connection = getConnection(id);
    if (connection != null) {
      ConnectorContext context = getContext(id);
      return connection.getConnectorService().getItem(context, id);
    }
    return null;
  }

  @DeleteMapping
  public boolean delete(@PathVariable(ID) String id) {
    setId(id);
    ConnectorEntity entity = getEntity();
    return entity.delete();
  }

  @GetMapping(value = "data", produces = MediaType.ALL_VALUE)
  public ResponseEntity<InputStreamResource> data(@PathVariable(ID) String id, @RequestParam("mode") String mode, HttpServletResponse response) {
    setId(id);

    QueryMode queryMode = QueryMode.valueOf(mode.toUpperCase());
    //TODO differ here between preview and actual download, change caching accordingly
    switch (queryMode) {
      case OPEN: {
        return writeResponse(response, true, false);
      }
      case STREAM: {
        return writeResponse(response, true, true);
      }
      case DOWNLOAD: {
        return writeResponse(response, true, true);
      }
      default: {
        return writeResponse(response, false, true);
      }
    }
  }

  @GetMapping(value = "preview", produces = APPLICATION_JSON_VALUE)
  public ConnectorPreviewRepresentation preview(@PathVariable(ID) String id) {
    setId(id);

    ConnectorPreviewRepresentation representation = new ConnectorPreviewRepresentation();
    try {
      //check for custom templates
      ConnectorItem item = getEntity();
      if (item == null) {
        return representation;
      }

      //add metadata
      representation.addMetaData(item.getMetaData());

      if (item.getPreviewHtml() != null) {
        representation.setHtml(item.getPreviewHtml());
      }

      formatPreview(representation, item);
    } catch (Exception e) {
      LOGGER.error("Failed to retrieve text preview for " + getEntity() + ": " + e.getMessage(), e);
    }
    return representation;
  }


  /**
   * Creates a stream or download response object for the given asset
   *
   * @param response       the response to write to
   * @param streamResponse true if the request should be a stream
   * @param setFilename    true to add file name to response entity
   */
  private ResponseEntity<InputStreamResource> writeResponse(HttpServletResponse response, boolean streamResponse, boolean setFilename) {
    ResponseEntity result = null;

    try {
      ConnectorItem item = getEntity();
      String filename = getFilename(item);
      String mimeType = item.getMimeType();
      if (mimeType == null) {
        mimeType = mimeTypeService.detectMimeType(null, filename, MediaType.APPLICATION_OCTET_STREAM_VALUE);
      }

      //we can't return json as mime type since jersey would try to deserialize it.
      if (mimeType.equals(APPLICATION_JSON_VALUE)) {
        mimeType = MediaType.TEXT_PLAIN_VALUE;
      }

      //make sure to encode text as utf8
      //mimeType = mimeType + "; charset=utf-8";

//      response.setHeader("Content-Type", mimeType);
//      response.setHeader("X-Frame-Options", "SAMEORIGIN");

      //do not set for open in tab
//      if (setFilename) {
//        response.setHeader("content-disposition", "attachment; filename = " + filename);
//      }

      InputStream is = getStreamInputStream(item, streamResponse);

      if (streamResponse) {
        if (is != null) {
          InputStreamResource inputStreamResource = new InputStreamResource(is);

          if (setFilename) {
            result = ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename)
                    .header("X-Frame-Options", "SAMEORIGIN")
                    .body(inputStreamResource);
          }
          else {
            result = ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType))
                    .header("X-Frame-Options", "SAMEORIGIN")
                    .body(inputStreamResource);
          }
        }
      }
      else {
        result = ResponseEntity.ok(is);
      }

    } catch (IOException e) {
      result = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e);
    }

    return result;
  }

  @Override
  protected ConnectorItemRepresentation getRepresentation() throws URISyntaxException {
    ConnectorItem entity = getEntity();
    if (entity != null) {
      ConnectorItemRepresentation representation = new ConnectorItemRepresentation();
      representation.setDownloadable(entity.isDownloadable());
      representation.setOpenInTabUrl(entity.getOpenInTabUrl());
      representation.setDownloadUrl(entity.getDownloadUrl());
      representation.setStreamUrl(entity.getStreamUrl());
      representation.setColumnValues(entity.getColumnValues());
      representation.setTargetContentType(entity.getTargetContentType());
      representation.setPreviewUri(new URI("connector/item/" + entity.getConnectorId().toUri() + "/preview"));
      representation.setSize(entity.getSize());
      representation.setItemType(entity.getItemType());
      fillRepresentation(entity, representation);
      return representation;
    }
    return null;
  }

  @Override
  public void setEntity(ConnectorItem picture) {
    super.setEntity(picture);
  }

  //------------------------------------- Helper -----------------------------------------------------------------------
  private InputStream getStreamInputStream(ConnectorItem item, boolean usePreviewVariant) throws FileNotFoundException {
    InputStream is = null;
    TempFile tempFile = tempFileCacheService.findTempFile(item, usePreviewVariant);
    if (tempFile != null) {
      is = tempFile.stream();
    }
    else {
      if (usePreviewVariant) {
        is = item.stream();
      }
      else {
        is = item.download();
      }
    }
    return is;
  }

  /**
   * Additional preview processing such as preview formatting and additional metadata retrieval
   */
  private void formatPreview(ConnectorPreviewRepresentation representation, ConnectorItem item) throws IOException {
    List<ConnectorPreviewConverter> applicableConverters = connectorPreviewConverters.stream().filter(entry -> entry.include(item)).collect(Collectors.toList());
    List<ConnectorMetaDataResolver> applicableMetaDataResolvers = connectorMetaDataResolvers.stream().filter(entry -> entry.test(item)).collect(Collectors.toList());

    if (!applicableConverters.isEmpty() || !applicableMetaDataResolvers.isEmpty()) {
      //check threshold before creating a temp file
      ConnectorContext context = getContext(item.getConnectorId());
      int previewThresholdMB = context.getPreviewThresholdMB();
      int thresholdBytes = previewThresholdMB * 1024 * 1024;
      if (previewThresholdMB != -1 && item.getSize() > 0 && item.getSize() > thresholdBytes) {
        representation.setHtml(null);
        return;
      }

      TempFile tempFile = tempFileCacheService.createTempFile(contentRepository, item, true);
      if (tempFile != null && tempFile.getFile() != null) {
        File itemTempFile = tempFile.getFile();

        //convert the preview based on the temp file
        for (ConnectorPreviewConverter converter : applicableConverters) {
          PreviewConversionResult conversionResult = converter.convert(context, item, itemTempFile);
          if (conversionResult != null && conversionResult.getResult() != null) {
            representation.setHtml(conversionResult.getResult());
            representation.addMetaData(conversionResult::getMetaData);
            break;
          }
        }

        //read metadata based on the temp file
        for (ConnectorMetaDataResolver metaDataResolver : applicableMetaDataResolvers) {
          ConnectorMetaData metaData = metaDataResolver.resolveMetaData(item, itemTempFile);
          representation.addMetaData(metaData);
        }

        //finally return plain stream
        if (representation.getHtml() == null) {
          representation.setHtml(FileUtils.readFileToString(itemTempFile, Charset.defaultCharset()));
        }
      }
    }
  }

  /**
   * For streaming
   */
  private void readAndWrite(final InputStream is, OutputStream os) throws IOException {
    try {
      byte[] data = new byte[2048];
      int read;
      while ((read = is.read(data)) > 0) {
        os.write(data, 0, read);
      }
      os.flush();
    } catch (IOException e) {
      //may happen when the selection changes during streaming
      is.close();
    } finally {
      is.close();
    }
  }

  /**
   * Helper to determine a filename for the given asset
   */
  private String getFilename(ConnectorItem item) {
    String name = item.getName();
    if (!name.contains(".")) {
      String mimeType = item.getMimeType();
      String suffix = item.getItemType();
      if (mimeType != null) {
        suffix = mimeType.split("/")[1];
      }
      name = name + "." + suffix;
    }

    return name;
  }

}
