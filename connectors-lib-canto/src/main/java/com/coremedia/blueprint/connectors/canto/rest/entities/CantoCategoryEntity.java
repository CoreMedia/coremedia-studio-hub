package com.coremedia.blueprint.connectors.canto.rest.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public class CantoCategoryEntity extends AbstractCantoEntity {

  @JsonProperty("id")
  private int id;

  @JsonProperty("hassubcategories")
  private boolean hasSubCategories;

  @JsonProperty("subcategories")
  private List<CantoCategoryEntity> subCategories;

  @JsonProperty("CategoryName")
  private String name;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public boolean hasSubCategories() {
    return hasSubCategories;
  }

  public void setHasSubCategories(boolean hasSubCategories) {
    this.hasSubCategories = hasSubCategories;
  }

  public List<CantoCategoryEntity> getSubCategories() {
    return subCategories;
  }

  public void setSubCategories(List<CantoCategoryEntity> subCategories) {
    this.subCategories = subCategories;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
