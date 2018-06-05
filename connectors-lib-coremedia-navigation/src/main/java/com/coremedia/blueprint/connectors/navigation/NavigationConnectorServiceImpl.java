package com.coremedia.blueprint.connectors.navigation;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorException;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorService;
import com.coremedia.blueprint.connectors.api.search.ConnectorSearchResult;
import com.coremedia.blueprint.connectors.content.ConnectorPageGridService;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class NavigationConnectorServiceImpl implements ConnectorService {
  private static final Logger LOGGER = LoggerFactory.getLogger(NavigationConnectorServiceImpl.class);
  private static final String IS_IN_PRODUCTION = "isInProduction";
  private static final String NAVIGATION_TYPE = "CMChannel";
  private static final String NAVIGATION_PROPERTY = "children";

  private NavigationConnectorCategory rootCategory;
  private ContentRepository repository;
  private ConnectorPageGridService pageGridService;

  @Override
  public boolean init(@Nonnull ConnectorContext context) {
    return true;
  }

  @Nullable
  @Override
  public ConnectorItem getItem(@Nonnull ConnectorContext context, @Nonnull ConnectorId connectorId) throws ConnectorException {
    String capId = connectorId.getExternalId();
    Content content = repository.getContent(capId);

    ConnectorId parentId = connectorId.getParentId();
    if (parentId.getExternalId().equals(context.getPreferredSite().getSiteRootFolder().getId())) {
      parentId = ConnectorId.createRootId(context.getConnectionId());
    }

    ConnectorCategory parent = createParentCategory(context, parentId);
    return new NavigationConnectorItem(this, parent, context, content, connectorId);
  }

  @Nullable
  @Override
  public ConnectorCategory getCategory(@Nonnull ConnectorContext context, @Nonnull ConnectorId connectorId) throws ConnectorException {
    Content rootContent = context.getPreferredSite().getSiteRootDocument();
    String contentId = rootContent.getId();
    ConnectorCategory parent = null;
    Content content = null;

    if (connectorId.isRootId() || connectorId.getExternalId().equals(contentId)) {
      content = context.getPreferredSite().getSiteRootDocument();
    }
    else {
      String capId = connectorId.getExternalId();
      content = repository.getContent(capId);
      if (content == null) {
        LOGGER.error("No content found for content id " + capId);
        return null;
      }

      Content parentContent = content.getReferrerWithDescriptor(NAVIGATION_TYPE, NAVIGATION_PROPERTY);
      if (parentContent.equals(rootContent)) {
        parent = getRootCategory(context);
      }
      else {
        ConnectorId categoryId = ConnectorId.createCategoryId(context.getConnectionId(), parentContent.getId());
        parent = createParentCategory(context, categoryId);
      }
    }

    NavigationConnectorCategory category = new NavigationConnectorCategory(this, parent, context, content, connectorId);
    addSubCategories(category, context);
    addItems(category, context);
    return category;
  }

  @Nonnull
  @Override
  public ConnectorCategory getRootCategory(@Nonnull ConnectorContext context) throws ConnectorException {
    if (context.getPreferredSite() == null) {
      throw new ConnectorException("No preferred site selected");
    }

    ConnectorId rootId = ConnectorId.createRootId(context.getConnectionId());
    rootCategory = new NavigationConnectorCategory(this, null, context, context.getPreferredSite().getSiteRootDocument(), rootId);
    addSubCategories(rootCategory, context);
    addItems(rootCategory, context);

    String name = context.getPreferredSite().getSiteRootDocument().getName() + " (Navigation)";
    rootCategory.setName(name);
    return rootCategory;
  }

  @Nonnull
  @Override
  public ConnectorSearchResult<ConnectorEntity> search(@Nonnull ConnectorContext context, ConnectorCategory category, String query, String searchType, Map<String, String> params) {
    List<ConnectorEntity> results = new ArrayList<>();
    return new ConnectorSearchResult<>(results);
  }

  public String getPreviewUrl(ConnectorContext context, Content content) {
    String pattern = context.getProperty("previewUrlPattern");
    if (pattern != null) {
      return MessageFormat.format(pattern, content.getId());
    }
    return null;
  }

  public String getLifecycle(Content content) {
    //assume we have a MLS connection
    if (repository.isMasterLiveServer() || repository.isLiveServer()) {
      return "publish";
    }

    Version checkedInVersion = content.getCheckedInVersion();
    if (checkedInVersion != null) {
      if (repository.getPublicationService().isPublished(checkedInVersion)) {
        return "publish";
      }
    }

    if (content.isCheckedOut()) {
      return "edited_by_user";
    }
    return null;
  }

  //----------------------- Helper -------------------------------------------------------------------------------------


  private ConnectorCategory createParentCategory(ConnectorContext context, ConnectorId connectorId) {
    String capId = connectorId.getExternalId();
    if (connectorId.isRootId() || capId.equals(context.getPreferredSite().getSiteRootDocument().getId())) {
      return rootCategory;
    }

    Content folderContent = repository.getContent(capId);
    return new NavigationConnectorCategory(this, null, context, folderContent, connectorId);
  }

  private void addItems(NavigationConnectorCategory category, ConnectorContext context) {
    Content page = category.getContent();
    if (!page.getType().isSubtypeOf("CMChannel")) {
      return;
    }

    try {
      List<Content> contents = pageGridService.getContents(page);
      for (Content childDocument : contents) {
        //somehow the duplicate content mixes up the tree relation, so we skip items that is available as categories
        if (category.containsSubCategory(childDocument)) {
          continue;
        }

        String childCapId = childDocument.getId();
        ConnectorId itemId = ConnectorId.createItemId(category.getConnectorId(), childCapId);
        NavigationConnectorItem item = new NavigationConnectorItem(this, category, context, childDocument, itemId);
        category.getItems().add(item);
      }
    } catch (Exception e) {
      LOGGER.warn("Failed to resolve main content for " + page.getPath() + ": " + e.getMessage(), e);
    }
  }

  private void addSubCategories(NavigationConnectorCategory parent, ConnectorContext context) {
    List<Content> children = parent.getContent().getLinksFulfilling("children", "TYPE CMChannel AND " + IS_IN_PRODUCTION);
    for (Content childDocument : children) {
      if (childDocument.getType().isSubtypeOf("CMChannel")) {
        String childCapId = childDocument.getId();
        ConnectorId categoryId = ConnectorId.createCategoryId(context.getConnectionId(), childCapId);
        NavigationConnectorCategory subCategory = new NavigationConnectorCategory(this, parent, context, childDocument, categoryId);
        parent.getSubCategories().add(subCategory);
      }
    }
  }

  @Required
  public void setContentRepository(ContentRepository contentRepository) {
    this.repository = contentRepository;
  }

  @Required
  public void setPageGridService(ConnectorPageGridService pageGridService) {
    this.pageGridService = pageGridService;
  }
}
