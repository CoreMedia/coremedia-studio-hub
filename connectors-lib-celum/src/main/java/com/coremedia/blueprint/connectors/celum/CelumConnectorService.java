package com.coremedia.blueprint.connectors.celum;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorException;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorService;
import com.coremedia.blueprint.connectors.api.invalidation.InvalidationResult;
import com.coremedia.blueprint.connectors.api.search.ConnectorSearchResult;
import com.coremedia.blueprint.connectors.celum.rest.Asset;
import com.coremedia.blueprint.connectors.celum.rest.Binary;
import com.coremedia.blueprint.connectors.celum.rest.CelumRestConnector;
import com.coremedia.blueprint.connectors.celum.rest.Node;
import com.coremedia.blueprint.connectors.impl.ConnectorContextImpl;
import com.coremedia.cap.struct.Struct;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import net.sf.ehcache.CacheManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class CelumConnectorService implements ConnectorService {
  private static final Logger LOG = LoggerFactory.getLogger(CelumConnectorService.class);
  private static final String CELUM_CACHE = "cacheManagerCelum";

  private static final String CELUM_API_KEY = "apiKey";
  private static final String CELUM_HOST = "host";
  private static final String CELUM_ROOT_NODES = "rootNodes";
  private static final String CELUM_DEFAULT_DOWNLOAD_FORMAT = "defaultDownloadFormat";
  private static final String CELUM_DOWNLOAD_FORMATS = "downloadFormats";

  private CelumConnectorCategory rootCategory;
  private CelumCoraService cora;

  private Map<String, Integer> downloadFormats = new HashMap<>();
  private int defaultDownloadFormat = 1;

  public CelumConnectorService(@NonNull CelumCoraService celumCoraService) {
    this.cora = celumCoraService;
  }


  @Override
  public boolean init(@NonNull ConnectorContext context) throws ConnectorException {
    String apiKey = context.getProperty(CELUM_API_KEY);
    String host = context.getProperty(CELUM_HOST);
    if (StringUtils.isEmpty(host)) {
      throw new ConnectorException("No '" + CELUM_HOST + "' configured for connection " + context.getConnectionId());
    }

    CelumRestConnector restConnector = new CelumRestConnector(host);
    if (StringUtils.isEmpty(apiKey)) {
      throw new ConnectorException("No '" + CELUM_API_KEY + "' configured for connection " + context.getConnectionId());
    }
    restConnector.setAuthToken(apiKey);
    cora.setConnector(restConnector);


    Map<String, Object> properties = ((ConnectorContextImpl) context).getProperties();
    if (properties.containsKey(CELUM_DEFAULT_DOWNLOAD_FORMAT)) {
      defaultDownloadFormat = (Integer)properties.get(CELUM_DEFAULT_DOWNLOAD_FORMAT);
    }

    //read all mapped download formats
    if (properties.containsKey(CELUM_DOWNLOAD_FORMATS)) {
      Struct struct = (Struct) properties.get(CELUM_DOWNLOAD_FORMATS);
      Map<String, Object> formats = struct.toNestedMaps();
      for (Map.Entry<String, Object> formatEntry : formats.entrySet()) {
        String[] extensions = formatEntry.getKey().split(",");
        for (String extension : extensions) {
          downloadFormats.put(extension, (Integer) formatEntry.getValue());
        }
      }
    }

    LOG.info("Studio Hub Extension for Celum initialized!");
    return true;
  }

  @Nullable
  @Override
  public ConnectorItem getItem(@NonNull ConnectorContext context, @NonNull ConnectorId id) throws ConnectorException {
    int assetId = Integer.parseInt(id.getExternalId());
    Asset asset = cora.getAsset(context.getConnectionId(), assetId);
    CelumConnectorCategory category = (CelumConnectorCategory) getCategory(context, id.getParentId());
    return new CelumConnectorItem(id, context, this, asset, category);
  }

  @Nullable
  @Override
  public ConnectorCategory getCategory(@NonNull ConnectorContext context, @NonNull ConnectorId id) throws ConnectorException {
    if (id.isRootId()) {
      return getRootCategory(context);
    }

    ConnectorCategory category = createCategory(context, id);
    if (category == null) {
      throw new ConnectorException("Celum data not available for connection '" + context.getConnectionId() + "'.");
    }
    return category;
  }

  @NonNull
  @Override
  public ConnectorCategory getRootCategory(@NonNull ConnectorContext context) throws ConnectorException {
    if (rootCategory == null) {
      String name = context.getProperty("displayName");
      ConnectorId id = ConnectorId.createRootId(context.getConnectionId());
      List<ConnectorCategory> children = new ArrayList<>();
      rootCategory = new CelumConnectorCategory(this, context, id, name, children);

      List<String> rootNodes = new ArrayList<>();
      String rootNodesString = context.getProperty(CELUM_ROOT_NODES);
      if (!StringUtils.isEmpty(rootNodesString)) {
        rootNodes = new ArrayList<>(Arrays.asList(rootNodesString.split(",")));
      }

      List<Node> topLevelCategories = cora.getTopLevelCategories(context.getConnectionId());
      for (Node node : topLevelCategories) {
        int nodeId = node.getId();
        if (rootNodes.isEmpty() || rootNodes.contains(String.valueOf(nodeId))) {
          ConnectorId categoryId = ConnectorId.createCategoryId(context.getConnectionId(), node.getId());
          ConnectorCategory category = createCategory(context, categoryId);
          children.add(category);
        }
      }
    }
    return rootCategory;
  }

  @Override
  public InvalidationResult invalidate(@NonNull ConnectorContext context) {
    return null;
  }

  @NonNull
  @Override
  public ConnectorSearchResult<ConnectorEntity> search(@NonNull ConnectorContext context, ConnectorCategory category, String query, String searchType, Map<String, String> params) {
    List<ConnectorEntity> result = new ArrayList<>();
    List<Asset> matches = cora.search(context.getConnectionId(), query);

    for (Asset match : matches) {
      List<Node> parents = match.getNodes();
      for (Node parent : parents) {
        String parentId = String.valueOf(parent.getId());
        //append the search result only if we are searching on the root level or the item's category is matching.
        if (category.getConnectorId().isRootId() || category.getConnectorId().getExternalId().equals(parentId)) {
          ConnectorId parentCategoryId = ConnectorId.createCategoryId(context.getConnectionId(), parentId);
          ConnectorId itemId = ConnectorId.createItemId(parentCategoryId, match.getId());

          //CelumConnectorItem item = (CelumConnectorItem) getItem(context, itemId);
          CelumConnectorCategory itemCategory = new CelumConnectorCategory(this, context, parentCategoryId, parent, null, null);
          CelumConnectorItem item = new CelumConnectorItem(itemId, context, this, match, itemCategory);

          if (matchesType(item, searchType)) {
            result.add(item);
          }

          break;
        }
      }
    }

    return new ConnectorSearchResult<>(result);
  }

  public boolean refresh(@NonNull ConnectorContext context) {
    CacheManager cacheManager = CacheManager.getCacheManager(CELUM_CACHE);
    cacheManager.getCache("celumCategoryCache").removeAll();
    cacheManager.getCache("celumAssetCache").removeAll();
    cacheManager.getCache("celumSearchResultCache").removeAll();
    init(context);
    return true;
  }

  //------------------------- Helper -----------------------------------------------------------------------------------

  private boolean matchesType(ConnectorItem item, String searchType) {
    if ((searchType == null || searchType.equals(ConnectorCategory.DEFAULT_TYPE) || searchType.equals("Content_"))) {
      return true;
    }

    String type = item.getItemType();
    if (searchType.equals(type)) {
      return true;
    }

    return false;
  }

  InputStream stream(Asset asset, ConnectorContext context) {
    return cora.stream(asset, getDownloadFormat(asset));
  }

  InputStream download(Asset asset, ConnectorContext context) {
    Binary binary = cora.getBinary(asset, getDownloadFormat(asset));
    return cora.download(binary);
  }

  private int getDownloadFormat(Asset asset) {
    String fileExtension = asset.getFileInformation().getFileExtension();
    int downloadFormat = defaultDownloadFormat;
    if (downloadFormats.containsKey(fileExtension)) {
      downloadFormat = downloadFormats.get(fileExtension);
    }
    return downloadFormat;
  }

  private ConnectorCategory createCategory(ConnectorContext context, ConnectorId categoryId) {
    int id = Integer.parseInt(categoryId.getExternalId());
    Node node = cora.getCategory(context.getConnectionId(), id);
    if (node == null) {
      return null;
    }

    return createCategory(context, categoryId, node);
  }

  private ConnectorCategory createCategory(ConnectorContext context, ConnectorId categoryId, Node node) {
    List<ConnectorCategory> subCategories = new ArrayList<>();
    Node parent = node.getParent();
    ConnectorCategory parentCategory = rootCategory;
    if (parent != null) {
      ConnectorId connectorId = ConnectorId.createCategoryId(context.getConnectionId(), parent.getId());
      parentCategory = new CelumConnectorCategory(this, context, connectorId, node, null, Collections.emptyList());
    }

    CelumConnectorCategory category = new CelumConnectorCategory(this, context, categoryId, node, parentCategory, subCategories);
    category.setChildItems(getAssets(context, category));

    for (Node childCategory : node.getChildren()) {
      ConnectorId childCategoryId = ConnectorId.createCategoryId(context.getConnectionId(), childCategory.getId());
      CelumConnectorCategory childCat = new CelumConnectorCategory(this, context, childCategoryId, childCategory, category, Collections.emptyList());
      subCategories.add(childCat);
    }

    return category;
  }

  private List<ConnectorItem> getAssets(ConnectorContext context, CelumConnectorCategory category) {
    List<Asset> assets = category.getAssets();
    List<ConnectorItem> items = new ArrayList<>();

    for (Asset asset : assets) {
      ConnectorId itemId = ConnectorId.createItemId(category.getConnectorId(), asset.getId());
      CelumConnectorItem item = new CelumConnectorItem(itemId, context, this, asset, category);
      items.add(item);
    }

    return items;
  }
}
