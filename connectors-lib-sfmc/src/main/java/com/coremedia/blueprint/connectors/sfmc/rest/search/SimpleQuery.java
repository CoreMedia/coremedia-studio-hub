package com.coremedia.blueprint.connectors.sfmc.rest.search;

/**
 *
 */
public class SimpleQuery implements Query {
  private String property;
  private String simpleOperator;
  private String valueType;
  private Object value;

  SimpleQuery(String property, String operator, String valueType, Object value) {
    this.property = property;
    this.simpleOperator = operator;
    this.valueType = valueType;
    this.value = value;
  }


  public String getProperty() {
    return property;
  }

  public String getSimpleOperator() {
    return simpleOperator;
  }

  public String getValueType() {
    return valueType;
  }

  public Object getValue() {
    return value;
  }

}
