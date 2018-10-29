package com.coremedia.blueprint.connectors.navigation;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorException;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorService;
import com.coremedia.blueprint.connectors.api.search.ConnectorSearchResult;
import com.coremedia.blueprint.connectors.navigation.util.ConnectorPageGridService;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.content.Version;
import com.coremedia.cap.content.search.SearchResult;
import com.coremedia.cap.content.search.SearchService;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.text.MessageFormat;
import java.util.ArrayList;
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
  private SitesService sitesService;

  @Override
  public boolean init(@NonNull ConnectorContext context) {
    return true;
  }

  @Nullable
  @Override
  public ConnectorItem getItem(@NonNull ConnectorContext context, @NonNull ConnectorId connectorId) throws ConnectorException {
    String capId = connectorId.getExternalId();
    Content content = repository.getContent(capId);

    ConnectorId parentId = connectorId.getParentId();
    String siteId = context.getPreferredSiteId();
    Site site = sitesService.getSite(siteId);
    if (parentId.getExternalId().equals(site.getSiteRootFolder().getId())) {
      parentId = ConnectorId.createRootId(context.getConnectionId());
    }

    ConnectorCategory parent = createParentCategory(context, parentId);
    return new NavigationConnectorItem(this, parent, context, content, connectorId);
  }

  @Nullable
  @Override
  public ConnectorCategory getCategory(@NonNull ConnectorContext context, @NonNull ConnectorId connectorId) throws ConnectorException {
    String siteId = context.getPreferredSiteId();
    Site site = sitesService.getSite(siteId);
    Content rootContent = site.getSiteRootDocument();
    String contentId = rootContent.getId();
    ConnectorCategory parent = null;
    Content content = null;

    if (connectorId.isRootId() || connectorId.getExternalId().equals(contentId)) {
      content = site.getSiteRootDocument();
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

  @NonNull
  @Override
  public ConnectorCategory getRootCategory(@NonNull ConnectorContext context) throws ConnectorException {
    String siteId = context.getPreferredSiteId();
    if (siteId == null || sitesService.getSite(siteId) == null) {
      throw new ConnectorException("No preferred site selected");
    }

    Site site = sitesService.getSite(siteId);
    ConnectorId rootId = ConnectorId.createRootId(context.getConnectionId());
    rootCategory = new NavigationConnectorCategory(this, null, context, site.getSiteRootDocument(), rootId);
    addSubCategories(rootCategory, context);
    addItems(rootCategory, context);

    String name = site.getSiteRootDocument().getName() + " (Navigation)";
    rootCategory.setName(name);
    return rootCategory;
  }

  @NonNull
  @Override
  public ConnectorSearchResult<ConnectorEntity> search(@NonNull ConnectorContext context, ConnectorCategory category, String query, String searchType, Map<String, String> params) {
    List<ConnectorEntity> results = new ArrayList<>();

    if (repository.isContentManagementServer()) {
      NavigationConnectorCategory navigationConnectorCategory = (NavigationConnectorCategory) category;
      Content folder = navigationConnectorCategory.getContent();
      ContentType searchContentType = repository.getContentType("CMChannel");
      SearchService searchService = repository.getSearchService();

      SearchResult search = searchService.search(query, "name", true, folder, true, searchContentType, true, 0, 200);
      List<Content> matches = search.getMatches();
      for (Content match : matches) {
        ConnectorId id = ConnectorId.createItemId(context.getConnectionId(), match.getId());
        NavigationConnectorItem item = new NavigationConnectorItem(this, null, context, match, id);
        results.add(item);
      }
    }

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
    String siteId = context.getPreferredSiteId();
    Site site = sitesService.getSite(siteId);
    if (connectorId.isRootId() || capId.equals(site.getSiteRootDocument().getId())) {
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

  @Required
  public void setSitesService(SitesService sitesService) {
    this.sitesService = sitesService;
  }
}
