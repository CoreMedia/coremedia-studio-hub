package com.coremedia.blueprint.connectors.celum.rest;

import java.util.List;

/**
 *
 */
public class NodeType {
  private int id;
  private String name;
  private List<LocalizedLabel> labels;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public List<LocalizedLabel> getLabels() {
    return labels;
  }

  public void setLabels(List<LocalizedLabel> labels) {
    this.labels = labels;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLabel(String language) {
    return Localization.getLabel(labels, language);
  }
}
