package com.coremedia.blueprint.connectors.canto.rest;

public enum FieldKeys {

  ASSET_DATA_SIZE(          "{b2a84783-86a0-4360-af0a-dcbc2a5a3d8e}", "Asset Data Size (Long)",   "size"),
  ASSET_MODIFICATION_DATE(  "{af4b2e06-5f6a-11d2-8f20-0000c0e166dc}", "Asset Modification Date",  "modification_date"),
  ASSET_NAME(               "{af4b2e69-5f6a-11d2-8f20-0000c0e166dc}", "Asset Name",               "name"),
  ASSET_REFERENCE(          "{af4b2e16-5f6a-11d2-8f20-0000c0e166dc}", "Asset Reference",          "reference"),
  CATEGORIES(               "{af4b2e0c-5f6a-11d2-8f20-0000c0e166dc}", "Categories",               "categories"),
  DESCRIPTION(              "{dedb3f5e-e1d0-3f4d-9e2e-e1dec5b09ba3}", "Description",              "description"),
  FILE_FORMAT(              "{af4b2e0d-5f6a-11d2-8f20-0000c0e166dc}", "File Format",              "file_format"),
  NOTES(                    "{af4b2e0b-5f6a-11d2-8f20-0000c0e166dc}", "Notes",                    "notes"),
  RATING(                   "{1e29ca59-4022-43f3-9448-539a3da4097c}", "Rating",                   "rating"),
  RECORD_NAME(              "{af4b2e00-5f6a-11d2-8f20-0000c0e166dc}", "Record Name",              "name"),
  STATUS(                   "{af4b2e07-5f6a-11d2-8f20-0000c0e166dc}", "Status",                   "status");


  private String key;
  private String name;
  private String alias;

  FieldKeys(String key, String name, String alias) {
    this.key = key;
    this.name = name;
    this.alias = alias;
  }

  public String key() {
    return key;
  }

  public String displayName() {
    return name;
  }

  public String alias() {
    return alias;
  }

  public String keyWithAlias() {
    return alias + ":" + key;
  }

  public String keyWithAliasAndSubField(String subField) {
    return alias + ":" + key + "/" + subField;
  }

  public String nameWithAlias() {
    return alias + ":" + name;
  }

  public String nameWithAliasAndSubField(String subField) {
    return alias + ":" + name + "/" + subField;
  }

}
