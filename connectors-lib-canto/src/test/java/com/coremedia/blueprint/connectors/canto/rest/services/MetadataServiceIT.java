package com.coremedia.blueprint.connectors.canto.rest.services;

import com.coremedia.blueprint.connectors.canto.rest.CantoConnector;
import com.coremedia.blueprint.connectors.canto.rest.entities.AbstractCantoEntity;
import com.coremedia.blueprint.connectors.canto.rest.entities.AssetEntity;
import com.coremedia.blueprint.connectors.canto.rest.entities.CantoCatalogEntity;
import com.coremedia.blueprint.connectors.canto.rest.entities.CantoCategoryEntity;
import com.coremedia.blueprint.connectors.canto.rest.entities.SearchResultEntity;
import com.coremedia.translate.xliff.core.jaxb.Ex;
import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MetadataServiceIT {

  private CantoConnector connector;

  private MetadataService testling;

  @Before
  public void setUp() {
    connector = new CantoConnector("sandbox.canto.com","coremedia-poc", "m4YnSNpHOt2Q");

    testling = new MetadataService();
    testling.setConnector(connector);
  }

  @Test
  public void testGetCatalogs() throws Exception {
    List<CantoCatalogEntity> catalogs = testling.getCatalogs();
    assertNotNull(catalogs);
    assertEquals(1, catalogs.size());

    CantoCatalogEntity catalog = catalogs.get(0);
    assertEquals(59, catalog.getId());
    assertEquals("coremedia-poc", catalog.getName());
    assertEquals("coremedia-poc", catalog.getDisplayName());
  }

  @Test
  public void testGetRootCategory() throws Exception {
    CantoCategoryEntity category = testling.getRootCategory("coremedia-poc");
    assertNotNull(category);

    assertEquals(0, category.getId());
  }

  @Test
  public void testGetTopCategories() throws Exception {
    List<CantoCategoryEntity> topCategories = testling.getTopCategories("coremedia-poc");
    assertEquals(3, topCategories.size());

    CantoCategoryEntity cat1 = topCategories.get(0);
    assertEquals(1, cat1.getId());
    assertEquals("$Categories", cat1.getName());

    CantoCategoryEntity cat2 = topCategories.get(1);
    assertEquals(2, cat2.getId());
    assertEquals("$Keywords", cat2.getName());

    CantoCategoryEntity cat3 = topCategories.get(2);
    assertEquals(3, cat3.getId());
    assertEquals("$Sources", cat3.getName());
  }

  @Test
  public void testGetCategoryById() throws Exception {
    CantoCategoryEntity category = testling.getCategoryById("coremedia-poc", 32);
    assertNotNull(category);

    assertEquals(32, category.getId());
    assertEquals("Cumulus Demo", category.getName());
  }

  @Test
  public void testQuickSearch() throws Exception {
    SearchResultEntity result = testling.quickSearch("coremedia-poc", ":49:");
    assertNotNull(result);
    assertEquals(12, result.getTotalCount());
  }

  @Test
  public void testGetAssignedAssets() throws Exception {
    List<AssetEntity> assets = testling.getAssignedAssets("coremedia-poc", 49);
    assertNotNull(assets);
    assertEquals(12, assets.size());
  }

  @Test
  public void testGetUnassignedAssets() throws Exception {
    List<AssetEntity> assets = testling.getAssignedAssets("coremedia-poc", 1);
    assertNotNull(assets);
    assertEquals(3, assets.size());
  }

  @Test
  public void testGetAssetById() throws Exception {
    AssetEntity asset = testling.getAssetById("coremedia-poc", 88);
    assertNotNull(asset);

    assertEquals(88, asset.getId());
    assertEquals("facebook-futuro.jpg", asset.getName());
    assertEquals("Sample image for CoreMedia PoC.", asset.getNotes());
    assertEquals(648640, asset.getDataSize().getValue());
    assertEquals("634 KB", asset.getDataSize().getDisplayString());
    assertEquals("2018-03-23T09:55:22+0100", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(asset.getModificationDate()));
    assertEquals("JPEG Image", asset.getFileFormat());
    assertEquals(3, asset.getRating().getId());
    assertEquals("***", asset.getRating().getDisplayString());
  }
}
