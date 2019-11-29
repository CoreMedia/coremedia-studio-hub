package com.coremedia.blueprint.connectors.celum.rest;

import java.util.List;
import java.util.Map;

public class Asset {

  private int id;

  private String name;

  private List<Node> nodes;

  private VersionInformation versionInformation;

  private LockInformation lockInformation;

  private Map<String,Object> informationFieldValues;

  private ModificationInformation modificationInformation;

  private boolean valid;

  private AssetType type;

  private Availability availability;

  private String status;

  private String fileCategory;

  private FileInformation fileInformation;

  private List<FileProperty> fileProperties;

  private PreviewInformation previewInformation;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isValid() {
    return valid;
  }

  public void setValid(boolean valid) {
    this.valid = valid;
  }

  public Availability getAvailability() {
    return availability;
  }

  public void setAvailability(Availability availability) {
    this.availability = availability;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getFileCategory() {
    return fileCategory;
  }

  public void setFileCategory(String fileCategory) {
    this.fileCategory = fileCategory;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public VersionInformation getVersionInformation() {
    return versionInformation;
  }

  public void setVersionInformation(VersionInformation versionInformation) {
    this.versionInformation = versionInformation;
  }

  public LockInformation getLockInformation() {
    return lockInformation;
  }

  public void setLockInformation(LockInformation lockInformation) {
    this.lockInformation = lockInformation;
  }

  public ModificationInformation getModificationInformation() {
    return modificationInformation;
  }

  public void setModificationInformation(ModificationInformation modificationInformation) {
    this.modificationInformation = modificationInformation;
  }

  public FileInformation getFileInformation() {
    return fileInformation;
  }

  public void setFileInformation(FileInformation fileInformation) {
    this.fileInformation = fileInformation;
  }

  public List<FileProperty> getFileProperties() {
    return fileProperties;
  }

  public void setFileProperties(List<FileProperty> fileProperties) {
    this.fileProperties = fileProperties;
  }

  public PreviewInformation getPreviewInformation() {
    return previewInformation;
  }

  public void setPreviewInformation(PreviewInformation previewInformation) {
    this.previewInformation = previewInformation;
  }

  public AssetType getType() {
    return type;
  }

  public void setType(AssetType type) {
    this.type = type;
  }

  public Map<String, Object> getInformationFieldValues() {
    return informationFieldValues;
  }

  public void setInformationFieldValues(Map<String, Object> informationFieldValues) {
    this.informationFieldValues = informationFieldValues;
  }

  public List<Node> getNodes() {
    return nodes;
  }

  public void setNodes(List<Node> nodes) {
    this.nodes = nodes;
  }
}
