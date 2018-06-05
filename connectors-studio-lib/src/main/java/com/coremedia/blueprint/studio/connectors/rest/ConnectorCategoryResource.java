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
import com.coremedia.rest.linking.LocationHeaderResourceFilter;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.spi.container.ResourceFilters;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * A resource to receive categories.
 */
@Produces(MediaType.APPLICATION_JSON)
@Path("connector/category/{id:[^/]+}")
public class ConnectorCategoryResource extends ConnectorEntityResource<ConnectorCategory> {

  private TempFileCacheService tempFileCacheService;
  private ContentRepository contentRepository;
  private ConnectorContentUploadService connectorUploadService;

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

  @GET
  @Path("preview")
  @Produces(MediaType.APPLICATION_JSON)
  public ConnectorPreviewRepresentation preview() {
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


  @GET
  @Path("refresh")
  @Produces(MediaType.APPLICATION_JSON)
  public Boolean refresh() {
    ConnectorCategory category = getEntity();
    ConnectorContext context = getContext(category.getConnectorId());
    tempFileCacheService.clear(context);
    return category.refresh(context);
  }

  @POST
  @Path("contents")
  public ConnectorCategory dropContents(@FormParam("contentIds") @DefaultValue("") String contentIds,
                                        @FormParam("defaultAction") Boolean defaultAction) {
    String[] ids = contentIds.split(",");
    List<Content> contents = Arrays.asList(ids).stream().map(id -> contentRepository.getContent(IdHelper.formatContentId(id))).collect(toList());
    ConnectorCategory category = getEntity();
    ConnectorContext context = getContext(category.getConnectorId());
    connectorUploadService.upload(context, category, contents, defaultAction);
    return category;
  }

  @POST
  @Path("upload")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @ResourceFilters(value = {LocationHeaderResourceFilter.class})
  public ConnectorItem handleBlobUpload(@HeaderParam("site") String siteId,
                                        @FormDataParam("contentName") String contentName,
                                        @FormDataParam("file") InputStream inputStream,
                                        @FormDataParam("file") FormDataContentDisposition fileDetail,
                                        @FormDataParam("file") FormDataBodyPart fileBodyPart) {
    ConnectorCategory category = getEntity();
    String fileName = fileDetail.getFileName();
    String extension = FilenameUtils.getExtension(fileName);
    if (!StringUtils.isEmpty(extension) && !contentName.endsWith(extension)) {
      contentName = contentName + "." + extension;
    }
    ConnectorContext context = getContext(category.getConnectorId());
    return category.upload(context, contentName, inputStream);
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
      }
      else {
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

  @Required
  public void setTempFileCacheService(TempFileCacheService tempFileCacheService) {
    this.tempFileCacheService = tempFileCacheService;
  }

  @Required
  public void setContentRepository(ContentRepository contentRepository) {
    this.contentRepository = contentRepository;
  }

  @Required
  public void setConnectorContentUploadService(ConnectorContentUploadService connectorUploadService) {
    this.connectorUploadService = connectorUploadService;
  }
}
