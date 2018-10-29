package com.coremedia.blueprint.connectors.typeform;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.typeform.data.Form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class TypeformCategory implements ConnectorCategory {

    public static final String CONNECTION_NAME = "name";

    private String name;
    private ConnectorId connectorId;
    private TypeformConnectorService service;
    private ConnectorContext context;

    public TypeformCategory(TypeformConnectorService service, ConnectorContext context) {

        this.name = context.getProperty(CONNECTION_NAME);
        this.service = service;
        this.context = context;

        connectorId = ConnectorId.createRootId(context.getConnectionId());
    }

    @Override
    public List<ConnectorCategory> getSubCategories() {
        return Collections.emptyList();
    }

    @Override
    public List<ConnectorItem> getItems() {
        ArrayList<ConnectorItem> items = new ArrayList<>();

        List<Form> forms = service.client.getForms();
        for (Form form : forms) {
            items.add(new TypeformItem(service, context, form.getId()));
        }
        return items;
    }

    @Override
    public boolean isWriteable() {
        return false;
    }

    @Override
    public ConnectorId getConnectorId() {
        return connectorId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ConnectorContext getContext() {
        return context;
    }

    @Override
    public ConnectorCategory getParent() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public Date getLastModified() {
        return null;
    }

    @Override
    public String getManagementUrl() {
        //@Todo add management url
        return null;
    }

    @Override
    public boolean isDeleteable() {
        return false;
    }

    @Override
    public boolean delete() {
        return false;
    }
}
