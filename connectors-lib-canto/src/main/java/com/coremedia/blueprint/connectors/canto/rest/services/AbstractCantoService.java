package com.coremedia.blueprint.connectors.canto.rest.services;

import com.coremedia.blueprint.connectors.canto.rest.CantoConnector;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.Resource;

public abstract class AbstractCantoService {

  public static final int EXTERNAL_ROOT_CATEGORY_ID = 1;

  public static final String CATALOG = "catalog";
  public static final String CATEGORY_ID = "categoryid";
  public static final String FIELD = "field";
  public static final String QUERY_STRING = "querystring";
  public static final String QUICK_SEARCH_STRING = "quicksearchstring";
  public static final String ASSET = "asset";

  CantoConnector connector;

  public void setConnector(CantoConnector connector) {
    this.connector = connector;
  }

}
