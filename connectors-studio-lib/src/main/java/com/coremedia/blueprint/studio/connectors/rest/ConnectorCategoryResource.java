package com.coremedia.blueprint.studio.connectors.rest;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorConnection;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.caching.TempFileCacheService;
import com.coremedia.blueprint.connectors.upload.ConnectorContentUploadService;
import com.coremedia.blueprint.studio.connectors.rest.representation.ConnectorCategoryRepresentation;
import com.coremedia.blueprint.studio.connectors.rest.representation.ConnectorChildRepresentation;
import com.coremedia.blueprint.studio.connectors.rest.representation.ConnectorPreviewRepresentation;
import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.rest.linking.ResponseLocationHeaderLinker;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.MimeType;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.coremedia.blueprint.studio.connectors.rest.ConnectorEntityResource.ID;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

/**
 * A resource to receive categories.
 */
@RestController
@RequestMapping(value = "connector/category/{" + ID + "}", produces = APPLICATION_JSON_VALUE)
public class ConnectorCategoryResource extends ConnectorEntityResource<ConnectorCategory> {

  private TempFileCacheService tempFileCacheService;
  private ContentRepository contentRepository;
  private ConnectorContentUploadService connectorUploadService;

  public ConnectorCategoryResource(ConnectorContentUploadService connectorUploadService, ContentRepository contentRepository, TempFileCacheService tempFileCacheService) {
    this.tempFileCacheService = tempFileCacheService;
    this.contentRepository = contentRepository;
    this.connectorUploadService = connectorUploadService;
  }

  @DeleteMapping
  public boolean delete(@PathVariable(ID) String id) {
    setId(id);
    ConnectorEntity entity = getEntity();
    return entity.delete();
  }

  @Override
  protected ConnectorCategory doGetEntity() {
    ConnectorId id = ConnectorId.toId(getDecodedId());
    ConnectorConnection connection = getConnection(id);
    if (connection != null) {
      ConnectorContext context = getContext(id);
      if (id.isRootId()) {
        return connection.getConnectorService().getRootCategory(context);
      }
      return connection.getConnectorService().getCategory(context, id);
    }
    return null;
  }

  @GetMapping("preview")
  public ConnectorPreviewRepresentation preview(@PathVariable(ID) String id) {
    setId(id);

    ConnectorPreviewRepresentation representation = new ConnectorPreviewRepresentation();
    ConnectorCategory category = getEntity();
    if (category == null) {
      return representation;
    }

    //add metadata
    representation.addMetaData(category.getMetaData());
    representation.setHtml(category.getPreviewHtml());
    return representation;
  }


  @GetMapping("refresh")
  public boolean refresh(@PathVariable(ID) String id) {
    setId(id);

    ConnectorCategory category = getEntity();
    ConnectorContext context = getContext(category.getConnectorId());
    tempFileCacheService.clear(context);
    return category.refresh(context);
  }

  @PostMapping("contents")
  public ConnectorCategory dropContents(@PathVariable(ID) String id,
                                        @FormParam("contentIds") @DefaultValue("") String contentIds,
                                        @FormParam("propertyNames") @DefaultValue("") String propertyNames,
                                        @FormParam("defaultAction") @DefaultValue("true") Boolean defaultAction) {
    setId(id);

    List<String> userSelectedPropertyNames = Arrays.stream(propertyNames.split(",")).filter(item -> item.length() > 0).collect(toList());
    List<String> idList = Arrays.stream(contentIds.split(",")).filter(item -> item.length() > 0).collect(toList());

    List<Content> contents = idList.stream().map(contentId -> contentRepository.getContent(IdHelper.formatContentId(contentId))).collect(toList());
    ConnectorCategory category = getEntity();
    ConnectorContext context = getContext(category.getConnectorId());
    connectorUploadService.upload(context, category, contents, userSelectedPropertyNames, defaultAction);
    return category;
  }

  @PostMapping(path = "upload", consumes = MULTIPART_FORM_DATA_VALUE)
  @ResponseLocationHeaderLinker
  public ConnectorItem handleBlobUpload(@PathVariable(ID) String id,
                                        @HeaderParam("site") String siteId,
                                        @RequestParam("file") MultipartFile file) throws Exception {
    setId(id);

    ConnectorCategory category = getEntity();
    String contentName = file.getOriginalFilename();
    String fileName = file.getOriginalFilename();
    String extension = FilenameUtils.getExtension(fileName);
    if (!StringUtils.isEmpty(contentName) && !contentName.endsWith(extension)) {
      contentName = contentName + "." + extension;
    }

    MimeType mimeType = new MimeType(file.getContentType());
    ConnectorContext context = getContext(category.getConnectorId());
    return category.upload(context, contentName, mimeType, file.getInputStream());
  }


  @Override
  protected ConnectorCategoryRepresentation getRepresentation() throws URISyntaxException {
    ConnectorCategory entity = getEntity();
    if (entity == null) {
      return null;
    }

    ConnectorCategoryRepresentation representation = new ConnectorCategoryRepresentation();
    fillRepresentation(entity, representation);
    fillCategoryRepresentation(entity, representation);
    return representation;
  }

  private void fillCategoryRepresentation(ConnectorCategory entity, ConnectorCategoryRepresentation representation) throws URISyntaxException {
    representation.setRefreshUri(new URI("connector/category/" + entity.getConnectorId().toUri() + "/refresh"));
    representation.setUploadUri(new URI("connector/category/" + entity.getConnectorId().toUri() + "/upload"));
    representation.setPreviewUri(new URI("connector/category/" + entity.getConnectorId().toUri() + "/preview"));
    representation.setContentDropUri(new URI("connector/category/" + entity.getConnectorId().toUri() + "/contents"));
    representation.setWriteable(entity.isWriteable());
    representation.setContentUploadEnabled(entity.isContentUploadEnabled());
    representation.setType(entity.getType());
    representation.setColumns(entity.getColumns());
    representation.setColumnValues(entity.getColumnValues());

    List<ConnectorCategory> subCategories = entity.getSubCategories();
    representation.setSubCategories(subCategories);
    List<ConnectorItem> items = entity.getItems();
    representation.setItems(items);
    List<ConnectorEntity> children = new ArrayList<>();
    children.addAll(subCategories);
    children.addAll(items);
    representation.setChildren(children);

    Map<String, ConnectorChildRepresentation> childrenByName = new LinkedHashMap<>();
    for (ConnectorEntity child : children) {
      if (child == null) {
        continue;
      }
      ConnectorChildRepresentation childRepresentation = new ConnectorChildRepresentation();
      childRepresentation.setChild(child);
      if (child instanceof ConnectorCategory) {
        childRepresentation.setDisplayName(child.getDisplayName());
      } else {
        childRepresentation.setDisplayName(child.getName());
      }

      childrenByName.put(child.getName(), childRepresentation);
    }

    representation.setChildrenByName(childrenByName);
  }

  @Override
  public void setEntity(ConnectorCategory category) {
    super.setEntity(category);
  }

  public void setTempFileCacheService(TempFileCacheService tempFileCacheService) {
    this.tempFileCacheService = tempFileCacheService;
  }

  public void setContentRepository(ContentRepository contentRepository) {
    this.contentRepository = contentRepository;
  }

  public void setConnectorContentUploadService(ConnectorContentUploadService connectorUploadService) {
    this.connectorUploadService = connectorUploadService;
  }

}
