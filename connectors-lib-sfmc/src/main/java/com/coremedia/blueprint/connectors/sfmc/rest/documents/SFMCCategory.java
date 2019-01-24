package com.coremedia.blueprint.connectors.sfmc.rest.documents;

/**
 *  {
 *             "id": 14673,
 *             "description": "The root folder for assets",
 *             "enterpriseId": 100020115,
 *             "memberId": 100020115,
 *             "name": "Content Builder",
 *             "parentId": 0,
 *             "categoryType": "asset"
 *         },
 */
public class SFMCCategory extends SFMCEntity {
  private String memberId;
  private int parentId;
  private String categoryType;

  public String getMemberId() {
    return memberId;
  }

  public void setMemberId(String memberId) {
    this.memberId = memberId;
  }

  public int getParentId() {
    return parentId;
  }

  public void setParentId(int parentId) {
    this.parentId = parentId;
  }

  public String getCategoryType() {
    return categoryType;
  }

  public void setCategoryType(String categoryType) {
    this.categoryType = categoryType;
  }

  public boolean isRoot() {
    return getParentId() == 0;
  }
}
