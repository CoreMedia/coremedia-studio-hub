package com.coremedia.blueprint.connectors.sfmc.rest.search;

import com.google.gson.Gson;

/**
 *
 */
public class QueryBuilder {

  private Query query;

  public static QueryBuilder createSimpleQuery(String property, String operator, String valueType, Object value) {
    QueryBuilder builder = new QueryBuilder();
    builder.query = new SimpleQuery(property, operator, valueType, value);
    return builder;
  }

  public String build() {
    Gson mapper = new Gson();
    return mapper.toJson(this);
  }
}
