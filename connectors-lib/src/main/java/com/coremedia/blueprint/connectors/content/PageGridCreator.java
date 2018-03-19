package com.coremedia.blueprint.connectors.content;

import com.coremedia.cap.common.IdHelper;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.multisite.Site;
import com.coremedia.cap.multisite.SitesService;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.struct.StructBuilder;
import com.coremedia.cap.struct.StructService;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Utility class to fill placements with content.
 */
public class PageGridCreator {
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
   * @param sectionName the name of the placement
   * @param page the content to add
   */
  public void addToPlacement(String sectionName, Content page, Content item) {
    Content section = getSection(page, sectionName);
    if(section == null) {
      throw new UnsupportedOperationException("No section found '" + sectionName + "', used paths " + placementPaths);
    }

    if(page.getStruct(PROPERTY_PAGEGRID) == null) {
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


    Struct llStruct = page.getStruct(PROPERTY_PAGEGRID).getStruct(PLACEMENTS_2).getStruct(PLACEMENTS).getStruct(String.valueOf(IdHelper.parseContentId(section.getId())));
    List<Content> mainItems = new ArrayList<>(llStruct.getLinks(ITEMS));
    mainItems.add(item);

    Struct placementStruct = page.getStruct(PROPERTY_PAGEGRID);
    Struct struct = placementStruct.builder().enter(PLACEMENTS_2).enter(PLACEMENTS).enter(String.valueOf(IdHelper.parseContentId(section.getId()))).set("items", mainItems).build();
    page.set(PROPERTY_PAGEGRID, struct);

    page.getRepository().getConnection().flush();
  }


  //------------------------ Helper ------------------------------------------------------------------------------------

  private Content getSection(Content channel, String name) {
    Site site = sitesService.getContentSiteAspect(channel).getSite();
    String[] paths = placementPaths.split(",");
    for (String path : paths) {
      Content placements = null;
      if(path.startsWith("/")) {
        placements = channel.getRepository().getRoot().getChild(path);
      }
      else {
        placements = site.getSiteRootFolder().getChild(path);
      }

      if(placements != null) {
        Set<Content> sections = placements.getChildDocuments();
        for (Content section : sections) {
          if(section.getName().equalsIgnoreCase(name)) {
            return section;
          }
        }
      }
    }
    return null;
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
