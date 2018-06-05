package com.coremedia.blueprint.connectors.canto.rest.services;

import com.coremedia.blueprint.connectors.canto.rest.CantoConnector;
import com.coremedia.blueprint.connectors.canto.rest.entities.AssetEntity;
import com.coremedia.blueprint.connectors.canto.rest.entities.CantoCatalogEntity;
import com.coremedia.blueprint.connectors.canto.rest.entities.CantoCategoryEntity;
import com.coremedia.blueprint.connectors.canto.rest.entities.SearchResultEntity;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AssetServiceIT {

  private CantoConnector connector;

  private AssetService testling;

  @Before
  public void setUp() {
    connector = new CantoConnector("sandbox.canto.com","coremedia-poc", "m4YnSNpHOt2Q");

    testling = new AssetService();
    testling.setConnector(connector);
  }

  @Test
  public void testUploadAsset() throws Exception {
    ClassLoader classLoader = getClass().getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream("assets/upload-test-asset.jpg");
    String itemName = "cm-test-asset,jpg";
    int assetId = testling.uploadAsset("coremedia-poc", 32, itemName, inputStream);
    assertTrue(assetId > 0);
  }

  @Test
  public void testDeleteAsset() throws Exception {
    boolean deleted = testling.deleteAsset("coremedia-poc", 90);
    assertTrue(deleted);
  }
}
