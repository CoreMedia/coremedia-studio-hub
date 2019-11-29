package com.coremedia.blueprint.connectors.celum.rest;

import java.util.List;

/**
 *
 */
public class Node {
  private int id;
  private List<LocalizedLabel> name;
  private boolean valid;
  private ModificationInformation modificationInformation;
  private boolean hasChildren;
  private List<Asset> assets;
  private List<Node> children;
  private Node parent;
  private NodeType type;

  public Asset getAsset(int id) {
    for (Asset asset : assets) {
      if(asset.getId() == id) {
        return asset;
      }
    }
    return null;
  }

  public List<LocalizedLabel> getName() {
    return name;
  }

  public String getLabel(String language) {
    return Localization.getLabel(name, language);
  }

  public void setName(List<LocalizedLabel> name) {
    this.name = name;
  }

  public ModificationInformation getModificationInformation() {
    return modificationInformation;
  }

  public void setModificationInformation(ModificationInformation modificationInformation) {
    this.modificationInformation = modificationInformation;
  }

  public boolean isHasChildren() {
    return hasChildren;
  }

  public void setHasChildren(boolean hasChildren) {
    this.hasChildren = hasChildren;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public boolean isValid() {
    return valid;
  }

  public void setValid(boolean valid) {
    this.valid = valid;
  }

  public List<Asset> getAssets() {
    return assets;
  }

  public void setAssets(List<Asset> assets) {
    this.assets = assets;
  }

  public List<Node> getChildren() {
    return children;
  }

  public void setChildren(List<Node> children) {
    this.children = children;
  }

  public Node getParent() {
    return parent;
  }

  public void setParent(Node parent) {
    this.parent = parent;
  }

  public NodeType getType() {
    return type;
  }

  public void setType(NodeType type) {
    this.type = type;
  }
}
