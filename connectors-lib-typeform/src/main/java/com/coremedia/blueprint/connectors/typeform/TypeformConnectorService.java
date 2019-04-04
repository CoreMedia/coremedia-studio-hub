package com.coremedia.blueprint.connectors.typeform;

import com.coremedia.connectors.api.ConnectorCategory;
import com.coremedia.connectors.api.ConnectorContext;
import com.coremedia.connectors.api.ConnectorEntity;
import com.coremedia.connectors.api.ConnectorException;
import com.coremedia.connectors.api.ConnectorId;
import com.coremedia.connectors.api.ConnectorItem;
import com.coremedia.connectors.api.ConnectorService;
import com.coremedia.connectors.api.search.ConnectorSearchResult;

import java.util.Collections;
import java.util.Map;

public class TypeformConnectorService implements ConnectorService {

    private static final String ACCESS_TOKEN = "accessToken";

    private TypeformCategory rootCategory;
    TypeformConnectorClient client;

    @Override
    public boolean init(ConnectorContext connectorContext) throws ConnectorException {
        String accessToken = connectorContext.getProperty(ACCESS_TOKEN);

        if (accessToken != null) {
            client = new TypeformConnectorClient(accessToken);

            return true;
        }
        return false;
    }

    @Override
    public ConnectorItem getItem(ConnectorContext connectorContext, ConnectorId connectorId) throws ConnectorException {
        return new TypeformItem(this, connectorContext, connectorId);
    }

    @Override
    public ConnectorCategory getCategory(ConnectorContext connectorContext, ConnectorId connectorId) throws ConnectorException {
        if (connectorId.isRootId()) {
            return getRootCategory(connectorContext);
        }
        return null;
    }

    @Override
    public ConnectorCategory getRootCategory(ConnectorContext connectorContext) throws ConnectorException {
        if (rootCategory == null) {
            rootCategory = new TypeformCategory(this, connectorContext);
        }
        return rootCategory;
    }

    @Override
    public ConnectorSearchResult<ConnectorEntity> search(ConnectorContext connectorContext, ConnectorCategory connectorCategory, String s, String s1, Map<String, String> map) {
        return new ConnectorSearchResult<>(Collections.emptyList());
    }
}
