package com.coremedia.blueprint.connectors.coremedia;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorException;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorService;
import com.coremedia.blueprint.connectors.api.invalidation.InvalidationResult;
import com.coremedia.blueprint.connectors.api.search.ConnectorSearchResult;
import com.coremedia.cap.Cap;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.common.CapPropertyDescriptor;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.content.Version;
import com.coremedia.cap.content.search.SearchResult;
import com.coremedia.cap.content.search.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CoreMediaConnectorServiceImpl implements ConnectorService {
  private static final Logger LOGGER = LoggerFactory.getLogger(CoreMediaConnectorServiceImpl.class);

  private final static String IOR = "ior";
  private final static String USERNAME = "username";
  private final static String PASSWORD = "password";
  private final static String PATH = "path";
  private final static String MEDIA_TYPE = "mediaType";
  private final static String IGNORED_TYPES = "ignoredTypes";

  final static String BLOB_PROPERTY_NAME = "data";


  private CoreMediaConnectorCategory rootCategory;
  private ContentRepository repository;
  private Content rootContent;
  private String mediaType;
  private List<String> ignoredTypes = new ArrayList<>();

  @Override
  public boolean init(@Nonnull ConnectorContext context) {
    try {
      String username = context.getProperty(USERNAME);
      String pwd = context.getProperty(PASSWORD);
      String ior = context.getProperty(IOR);
      String path = context.getProperty(PATH);
      this.mediaType = context.getProperty(MEDIA_TYPE);

      CapConnection con = Cap.connect(ior, username, pwd);
      repository = con.getContentRepository();
      rootContent = repository.getRoot();
      if (!StringUtils.isEmpty(path)) {
        rootContent = repository.getChild(path);
      }

      String ignoredTypesString = context.getProperty(IGNORED_TYPES);
      if (!StringUtils.isEmpty(ignoredTypesString)) {
        ignoredTypes = new ArrayList<>(Arrays.asList(ignoredTypesString.split(",")));
      }

      return true;
    } catch (Exception e) {
      LOGGER.error("Failed to initialized connector for CoreMedia repository: " + e.getMessage(), e);
    }
    return false;
  }

  @Override
  public Boolean refresh(@Nonnull ConnectorContext context, @Nonnull ConnectorCategory category) {
    return true;
  }

  @Override
  public ConnectorItem upload(@Nonnull ConnectorContext context, ConnectorCategory category, String itemName, InputStream inputStream) {
    return null;
  }

  @Nullable
  @Override
  public ConnectorItem getItem(@Nonnull ConnectorContext context, @Nonnull ConnectorId connectorId) throws ConnectorException {
    String capId = connectorId.getExternalId();
    Content content = repository.getContent(capId);

    String parentCapId = content.getParent().getId();
    ConnectorId parentId = ConnectorId.createCategoryId(context.getConnectionId(), parentCapId);
    if (parentCapId.equals(rootContent.getId())) {
      parentId = ConnectorId.createRootId(context.getConnectionId());
    }

    ConnectorCategory parent = createParentCategory(context, parentId);
    return new CoreMediaConnectorItem(this, parent, context, content, connectorId);
  }

  @Nullable
  @Override
  public ConnectorCategory getCategory(@Nonnull ConnectorContext context, @Nonnull ConnectorId connectorId) throws ConnectorException {
    ConnectorId parentId = null;
    Content content = null;
    String contentId = rootContent.getId();
    if (connectorId.isRootId() || connectorId.getExternalId().equals(contentId)) {
      parentId = rootCategory.getConnectorId();
      content = rootCategory.getContent();
    }
    else {
      String capId = connectorId.getExternalId();
      content = repository.getContent(capId);
      if(content == null) {
        LOGGER.error("No content found for content id " + capId);
        return null;
      }
      Content parent = content.getParent();
      String parentCapId = parent.getId();
      parentId = ConnectorId.createCategoryId(context.getConnectionId(), parentCapId);
    }

    ConnectorCategory parent = createParentCategory(context, parentId);
    CoreMediaConnectorCategory category = new CoreMediaConnectorCategory(this, parent, context, content, connectorId);
    addItems(category, context);
    addSubCategories(category, context);
    return category;
  }

  @Nonnull
  @Override
  public ConnectorCategory getRootCategory(@Nonnull ConnectorContext context) throws ConnectorException {
    if (rootCategory == null) {
      ConnectorId rootId = ConnectorId.createRootId(context.getConnectionId());
      rootCategory = new CoreMediaConnectorCategory(this, null, context, rootContent, rootId);
      addItems(rootCategory, context);
      addSubCategories(rootCategory, context);
    }

    rootCategory.setName(context.getProperty("displayName"));
    return rootCategory;
  }

  @Nonnull
  @Override
  public ConnectorSearchResult<ConnectorEntity> search(@Nonnull ConnectorContext context, ConnectorCategory category, String query, String searchType, Map<String, String> params) {
    List<ConnectorEntity> results = new ArrayList<>();

    if (repository.isContentManagementServer()) {
      CoreMediaConnectorCategory coreMediaConnectorCategory = (CoreMediaConnectorCategory) category;

      boolean includeSubFolders = !StringUtils.isEmpty(query) && query.equals("*");
      ContentType searchContentType = repository.getContentType(ContentType.CONTENT);
      List<String> types = context.getItemTypes().getTypes(searchType);
      if (!types.isEmpty()) {
        for (String type : types) {
          searchContentType = repository.getContentType(type);
          search(context, query, results, coreMediaConnectorCategory, includeSubFolders, searchContentType);
        }
      }
      else {
        search(context, query, results, coreMediaConnectorCategory, includeSubFolders, searchContentType);
      }
    }

    return new ConnectorSearchResult<>(results);
  }

  @Override
  public InvalidationResult invalidate(@Nonnull ConnectorContext context) {
    return null;
  }


  public boolean isDeleteable(Content content) {
    return repository.isContentManagementServer() && repository.getAccessControl().mayDelete(content);
  }

  public boolean isDownloadable(Content content) {
    return isReadable(content) && (content.getType().isSubtypeOf(mediaType) || content.getType().isSubtypeOf("CMDownload"));
  }

  public boolean isWriteable(Content content) {
    return false;
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

  public InputStream stream(Content content) {
    CapPropertyDescriptor descriptor = content.getType().getDescriptor(BLOB_PROPERTY_NAME);
    if (descriptor != null) {
      Blob data = content.getBlob(descriptor.getName());
      if (data != null) {
        return data.getInputStream();
      }
    }

    return null;
  }


  //----------------------- Helper -------------------------------------------------------------------------------------

  private boolean isReadable(Content content) {
    return repository.getAccessControl().mayRead(content);
  }

  private void search(@Nonnull ConnectorContext context, String query, List<ConnectorEntity> results, CoreMediaConnectorCategory category, boolean includeSubFolders, ContentType searchContentType) {
    boolean includeSubTypes = searchContentType.isAbstract();
    SearchService searchService = repository.getSearchService();

    SearchResult search = searchService.search(query, "name", true, category.getContent(), includeSubFolders, searchContentType, includeSubTypes, 0, 200);
    List<Content> matches = search.getMatches();
    for (Content match : matches) {
      if (match.isDocument() && !isIgnored(match)) {
        ConnectorId id = ConnectorId.createItemId(context.getConnectionId(), match.getId());
        CoreMediaConnectorItem item = new CoreMediaConnectorItem(this, null, context, match, id);
        results.add(item);
      }
      if (results.size() > 50) {
        break;
      }
    }
  }

  private ConnectorCategory createParentCategory(ConnectorContext context, ConnectorId connectorId) {
    String capId = connectorId.getExternalId();
    if (connectorId.isRootId() || capId.equals(rootContent.getId())) {
      return rootCategory;
    }

    Content folderContent = repository.getContent(capId);
    return new CoreMediaConnectorCategory(this, null, context, folderContent, connectorId);
  }

  private void addItems(CoreMediaConnectorCategory category, ConnectorContext context) {
    Set<Content> childDocuments = category.getContent().getChildDocuments();
    for (Content childDocument : childDocuments) {
      if (isIgnored(childDocument)) {
        continue;
      }

      String childCapId = childDocument.getId();
      ConnectorId itemId = ConnectorId.createItemId(context.getConnectionId(), childCapId);
      CoreMediaConnectorItem item = new CoreMediaConnectorItem(this, category, context, childDocument, itemId);
      category.getItems().add(item);
    }
  }

  private void addSubCategories(CoreMediaConnectorCategory parent, ConnectorContext context) {
    Set<Content> childDocuments = parent.getContent().getChildren();
    for (Content childDocument : childDocuments) {
      if (childDocument.isFolder() && isReadable(childDocument)) {
        String childCapId = childDocument.getId();
        ConnectorId categoryId = ConnectorId.createCategoryId(context.getConnectionId(), childCapId);
        CoreMediaConnectorCategory subCategory = new CoreMediaConnectorCategory(this, parent, context, childDocument, categoryId);
        parent.getSubCategories().add(subCategory);
      }
    }
  }

  private boolean isIgnored(Content childDocument) {
    for (String ignoredType : ignoredTypes) {
      if (childDocument.getType().isSubtypeOf(ignoredType)) {
        return true;
      }
    }
    return false;
  }
}
