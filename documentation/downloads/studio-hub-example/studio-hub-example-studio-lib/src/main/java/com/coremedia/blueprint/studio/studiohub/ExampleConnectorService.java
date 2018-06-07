package com.coremedia.blueprint.studio.studiohub;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorException;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorService;
import com.coremedia.blueprint.connectors.api.invalidation.InvalidationResult;
import com.coremedia.blueprint.connectors.api.search.ConnectorSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class ExampleConnectorService implements ConnectorService {
  private static final Logger LOG = LoggerFactory.getLogger(ExampleConnectorService.class);

  private ConnectorContext context;
  private ExampleConnectorCategory rootCategory;
  private ExampleConnectorCategory subCategory1;
  private ExampleConnectorCategory subCategory2;
  private ExampleConnectorItem exampleItem1;
  private ExampleConnectorItem exampleItem2;

  @Override
  public boolean init(@Nonnull ConnectorContext context) throws ConnectorException {
    this.context = context;
    LOG.info("Studio Hub example initialized.");
    return true;
  }

  @Nullable
  @Override
  public ConnectorItem getItem(@Nonnull ConnectorContext context, @Nonnull ConnectorId id) throws ConnectorException {
    if(id.equals(exampleItem1.getConnectorId())) {
      return exampleItem1;
    }

    if(id.equals(exampleItem2.getConnectorId())) {
      return exampleItem2;
    }
    return null;
  }

  @Nullable
  @Override
  public ConnectorCategory getCategory(@Nonnull ConnectorContext context, @Nonnull ConnectorId id) throws ConnectorException {
    if(id.isRootId()) {
      return rootCategory;
    }

    if(id.equals(subCategory1.getConnectorId())) {
      return subCategory1;
    }

    if(id.equals(subCategory2.getConnectorId())) {
      return subCategory2;
    }
    return null;
  }

  @Nonnull
  @Override
  public ConnectorCategory getRootCategory(@Nonnull ConnectorContext context) throws ConnectorException {
    if(rootCategory == null) {
      String name = this.context.getProperty("displayName");
      List<ConnectorCategory> childCategories = new ArrayList<>();
      List<ConnectorItem> childItems = Collections.emptyList();
      ConnectorId id = ConnectorId.createRootId(context.getConnectionId());
      rootCategory = new ExampleConnectorCategory(id, context, name, null, childCategories, childItems);

      //create fix child categories with fix children
      ConnectorId item1d = ConnectorId.createItemId(context.getConnectionId(), "item1");
      exampleItem1 = new ExampleConnectorItem(item1d, context, "test.png", subCategory1);
      ConnectorId item2d = ConnectorId.createItemId(context.getConnectionId(), "article:someExternalArticle");
      exampleItem2 = new ExampleConnectorItem(item2d, context, "External Article", subCategory1);

      ConnectorId category1Id = ConnectorId.createCategoryId(context.getConnectionId(), "A");
      subCategory1 = new ExampleConnectorCategory(category1Id, context, "Sub A", rootCategory, Collections.emptyList(), Arrays.asList(exampleItem1, exampleItem2));
      childCategories.add(subCategory1);

      ConnectorId category2Id = ConnectorId.createCategoryId(context.getConnectionId(), "B");
      subCategory2 = new ExampleConnectorCategory(category2Id, context, "Sub B", rootCategory, Collections.emptyList(), Collections.emptyList());

      childCategories.add(subCategory2);
    }
    return rootCategory;
  }

  @Override
  public InvalidationResult invalidate(@Nonnull ConnectorContext context) {
    return null;
  }

  @Nonnull
  @Override
  public ConnectorSearchResult<ConnectorEntity> search(@Nonnull ConnectorContext context, ConnectorCategory category, String query, String searchType, Map<String, String> params) {
    return null;
  }
}
