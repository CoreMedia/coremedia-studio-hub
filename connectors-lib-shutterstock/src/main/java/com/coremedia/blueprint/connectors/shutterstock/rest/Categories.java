package com.coremedia.blueprint.connectors.shutterstock.rest;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

/**
 *
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Categories {
  private List<Category> data;

  public List<Category> getData() {
    return data;
  }

  public void setData(List<Category> data) {
    this.data = data;
  }
}
