package com.coremedia.blueprint.connectors.navigation.util;

import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.struct.StructBuilder;
import com.coremedia.cap.struct.StructService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Utility class to fill placements with content.
 */
public class ConnectorPageGridService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorPageGridService.class);
  private static final String PLACEMENTS_2 = "placements_2";
  private static final String PLACEMENTS = "placements";
  private static final String PROPERTY_PAGEGRID = "placement";
  private static final String LOCKED = "locked";
  private static final String ITEMS = "items";
  private static final String PAGE_GRID_LINK_TYPE = "CMLinkable";

  private SitesService sitesService;
  private String placementPaths;

  /**
   * Adds the given content to the placement with the given name
   *
   * @param sectionName the name of the placement
   * @param page        the content to add
   */
  public void addToPlacement(String sectionName, Content page, Content item) {
    addToPlacement(sectionName, page, Arrays.asList(item));
  }

  public void addToPlacement(String sectionName, Content page, List<Content> items) {
    Content section = getSection(page, sectionName);
    if (section == null) {
      throw new UnsupportedOperationException("No section found '" + sectionName + "', used paths " + placementPaths);
    }

    if (page.getStruct(PROPERTY_PAGEGRID) == null) {
      StructService structService = page.getRepository().getConnection().getStructService();
      Struct struct = structService.emptyStruct();
      Struct placements2Struct = structService.emptyStruct();
      StructBuilder builder = struct.builder().declareStruct(PLACEMENTS_2, placements2Struct);
      builder.enter(PLACEMENTS_2);
      builder.declareStruct(PLACEMENTS, structService.emptyStruct());
      builder.enter(PLACEMENTS);
      Struct initialSectionStruct = structService.emptyStruct();
      builder.declareStruct(String.valueOf(IdHelper.parseContentId(section.getId())), initialSectionStruct);
      builder.enter(String.valueOf(IdHelper.parseContentId(section.getId())));
      builder.declareBoolean(LOCKED, false);
      builder.declareLink("section", section.getType(), section);
      builder.declareLinks(ITEMS, page.getRepository().getContentType(PAGE_GRID_LINK_TYPE), Collections.emptyList());
      page.set(PROPERTY_PAGEGRID, builder.build());
    }

    Struct pg = page.getStruct(PROPERTY_PAGEGRID);
    Map<String, Object> placements= pg.getStruct(PLACEMENTS).toNestedMaps();
    Struct llStruct = null;
    if(placements.containsKey(ITEMS) && !((List)placements.get(ITEMS)).isEmpty()) {
      llStruct = pg.getStruct(PLACEMENTS);
    }
    else {
      llStruct = pg.getStruct(PLACEMENTS_2).getStruct(PLACEMENTS).getStruct(String.valueOf(IdHelper.parseContentId(section.getId())));
    }

    List<Content> mainItems = new ArrayList<>(llStruct.getLinks(ITEMS));
    mainItems.addAll(items);

    Struct placementStruct = page.getStruct(PROPERTY_PAGEGRID);
    Struct struct = placementStruct.builder().enter(PLACEMENTS_2).enter(PLACEMENTS).enter(String.valueOf(IdHelper.parseContentId(section.getId()))).set("items", mainItems).build();
    page.set(PROPERTY_PAGEGRID, struct);

    page.getRepository().getConnection().flush();
  }

  public List<Content> getContents(Content page) {
    Collection<Content> sections = getSections(page);
    List<Content> result = new ArrayList<>();
    for (Content section : sections) {
      result.addAll(getPlacementContents(page, section.getName()));
    }

    return result;
  }

  public List<Content> getPlacementContents(Content page, String sectionName) {
    Content section = getSection(page, sectionName);
    List<Content> result = new ArrayList<>();

    Struct targetStruct = page.getStruct(PROPERTY_PAGEGRID);
    if (targetStruct != null) {
      if (targetStruct.toNestedMaps().containsKey(PLACEMENTS)) {
        List<Content> placementItems = getPlacementItems(targetStruct, page, section);
        result.addAll(placementItems);
      }

      if (targetStruct.toNestedMaps().containsKey(PLACEMENTS_2)) {
        Struct placementStruct = targetStruct.getStruct(PLACEMENTS_2);
        if (placementStruct != null) {
          Map<String, Object> placementsMap= placementStruct.toNestedMaps();
          if(placementsMap.containsKey(PLACEMENTS)) {
            List<Content> placementItems = getPlacementItems(placementStruct, page, section);
            result.addAll(placementItems);
          }
        }
      }
    }
    return result;
  }

  public Content getSection(Content channel, String name) {
    Collection<Content> sections = getSections(channel);
    for (Content section : sections) {
      if (section.getName().equalsIgnoreCase(name)) {
        return section;
      }
    }
    return null;
  }

  //------------------------ Helper ------------------------------------------------------------------------------------

  private List<Content> getPlacementItems(Struct parent, Content page, Content section) {
    List<Struct> structs = parent.getStructs(PLACEMENTS);
    for (Struct struct : structs) {
      Content placementSection = (Content) struct.toNestedMaps().get("section");
      if (placementSection != null && placementSection.getId().equals(section.getId())) {
        return (List<Content>) struct.toNestedMaps().get("items");
      }
    }
    return Collections.emptyList();
  }

  private Collection<Content> getSections(Content channel) {
    Site site = sitesService.getContentSiteAspect(channel).getSite();
    String[] paths = placementPaths.split(",");
    for (String path : paths) {
      Content placements = null;
      if (path.startsWith("/")) {
        placements = channel.getRepository().getRoot().getChild(path);
      }
      else {
        placements = site.getSiteRootFolder().getChild(path);
      }

      if (placements != null) {
        return placements.getChildDocuments();
      }
    }

    return Collections.emptyList();
  }

  //------------------------- Spring -----------------------------------------------------------------------------------

  @Required
  public void setSitesService(SitesService sitesService) {
    this.sitesService = sitesService;
  }

  @Required
  public void setPlacementPaths(String placementPaths) {
    this.placementPaths = placementPaths;
  }
}
