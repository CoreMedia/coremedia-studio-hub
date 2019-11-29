package com.coremedia.blueprint.connectors.celum.rest;

import java.util.List;

/**
 *
 */
public class Tag {
  private int id;
  private List<LocalizedLabel> name;
  private String validationLevel;
  private boolean validationLevelInherited;
  private boolean valid;
  private ModificationInformation modificationInformation;
  private boolean hasChildren;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public List<LocalizedLabel> getName() {
    return name;
  }

  public void setName(List<LocalizedLabel> name) {
    this.name = name;
  }

  public String getValidationLevel() {
    return validationLevel;
  }

  public void setValidationLevel(String validationLevel) {
    this.validationLevel = validationLevel;
  }

  public boolean isValidationLevelInherited() {
    return validationLevelInherited;
  }

  public void setValidationLevelInherited(boolean validationLevelInherited) {
    this.validationLevelInherited = validationLevelInherited;
  }

  public boolean isValid() {
    return valid;
  }

  public void setValid(boolean valid) {
    this.valid = valid;
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
}
