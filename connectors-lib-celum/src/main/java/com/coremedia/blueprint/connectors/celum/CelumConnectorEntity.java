package com.coremedia.blueprint.connectors.celum;

import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.Serializable;

/**
 *
 */
abstract public class CelumConnectorEntity implements Serializable, ConnectorEntity {
  static final String DATA_INDEX_CELUM_ID = "celumId";
  static final String DATA_INDEX_CELUM_FOLDER = "celumFolder";
  protected ConnectorId id;
  protected ConnectorContext context;

  CelumConnectorEntity(ConnectorContext context, ConnectorId id) {
    this.id = id;
    this.context = context;
  }

  @NonNull
  @Override
  public ConnectorId getConnectorId() {
    return id;
  }

  @NonNull
  @Override
  public ConnectorContext getContext() {
    return context;
  }
}
