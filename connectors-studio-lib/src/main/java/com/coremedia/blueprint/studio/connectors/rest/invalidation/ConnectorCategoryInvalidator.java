package com.coremedia.blueprint.studio.connectors.rest.invalidation;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.impl.ConnectorCategoryChangeListener;
import com.coremedia.blueprint.connectors.impl.Connectors;
import com.coremedia.rest.invalidations.SimpleInvalidationSource;
import com.coremedia.rest.linking.Linker;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class ConnectorCategoryInvalidator extends SimpleInvalidationSource implements ConnectorCategoryChangeListener, InitializingBean {

  private Linker linker;
  private Connectors connectors;

  public ConnectorCategoryInvalidator(String id, Connectors connectors, Linker linker) {
    super();
    setId(id);
    this.connectors = connectors;
    this.linker = linker;
  }

  @Override
  public void categoryChanged(@NonNull ConnectorContext context, @NonNull ConnectorCategory category) {
    Set<String> links = new HashSet<>();
    URI link = linker.link(category);
    links.add(link.toString());
    addInvalidations(links);
  }

  @Required
  public void setLinker(Linker linker) {
    this.linker = linker;
  }

  @Required
  public void setConnectors(Connectors connectors) {
    this.connectors = connectors;
  }

  @Override
  public void afterPropertiesSet() {
    super.afterPropertiesSet();
    connectors.addConnectorCategoryChangeListener(this);
  }

}
