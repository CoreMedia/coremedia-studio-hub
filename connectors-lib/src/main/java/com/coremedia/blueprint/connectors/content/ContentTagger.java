package com.coremedia.blueprint.connectors.content;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.struct.Struct;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 */
public class ContentTagger {
  private static final String ROOT_SETTINGS_DOCUMENT = "_root";
  private static final String SETTINGS_STRUCT = "settings";
  private static final String ROOTS_LIST = "roots";

  private String taxonomyPath;
  private ContentRepository contentRepository;

  public void tag(Content content, String parentTag, String taxonomyName, String taxonomyProperty, String taxonomyDocType, List<String> tags) {
    Content taxonomiesFolder = contentRepository.getChild(taxonomyPath);
    Content taxonomyFolder = taxonomiesFolder.getChild(taxonomyName);
    ContentType ct = contentRepository.getContentType(taxonomyDocType);

    List<Content> result = new ArrayList<>();
    for (String tag : tags) {
      Content tagContent = getOrCreateTag(taxonomyFolder, ct, parentTag, tag);
      result.add(tagContent);
    }

    content.set(taxonomyProperty, result);
  }

  private synchronized Content getOrCreateTag(Content taxonomyFolder, ContentType ct, String parentTag, String tag) {
    //exists check
    Content existingTag = taxonomyFolder.getChild(tag);
    if (existingTag != null) {
      return existingTag;
    }

    Content tagContent = ct.createByTemplate(taxonomyFolder, tag, "{3} ({1})", new HashMap<>());
    tagContent.set("value", tag);
    tagContent.checkIn();


    Content parent = getOrCreateParent(taxonomyFolder, ct, parentTag);
    List<Content> children = new ArrayList<>(parent.getLinks("children"));
    children.add(tagContent);

    if (!parent.isCheckedOut()) {
      parent.checkOut();
    }

    parent.set("children", children);
    if (parent.isCheckedOut()) {
      parent.checkIn();
    }

    return tagContent;
  }

  /**
   * Newly created tags are always added to a specific parent
   *
   * @param taxonomyFolder
   * @param ct
   * @param parentTag
   * @return
   */
  private Content getOrCreateParent(Content taxonomyFolder, ContentType ct, String parentTag) {
    Content parent = taxonomyFolder.getChild(parentTag);
    if (parent != null) {
      return parent;
    }

    parent = ct.createByTemplate(taxonomyFolder, parentTag, "{3} ({1})", new HashMap<>());
    parent.set("value", parentTag);
    parent.checkIn();

    rootLinkCheck(taxonomyFolder, parent);
    return parent;
  }

  /**
   * Check if taxonomy is root settings linked
   */
  private void rootLinkCheck(Content taxonomyFolder, Content tagContent) {
    Content rootSettings = taxonomyFolder.getChild(ROOT_SETTINGS_DOCUMENT);
    if (rootSettings != null && rootSettings.getType().getName().equals("CMSettings")) {
      if (!rootSettings.isCheckedOut()) {
        rootSettings.checkOut();
      }

      Struct settings = rootSettings.getStruct(SETTINGS_STRUCT);
      List<Content> roots = new ArrayList<>(settings.getLinks(ROOTS_LIST));
      roots.add(tagContent);

      settings = settings.builder().set(ROOTS_LIST, roots).build();
      rootSettings.set(SETTINGS_STRUCT, settings);
      rootSettings.checkIn();
    }
  }

  //--------------------------- Spring ---------------------------------------------------------------------------------

  @Required
  public void setTaxonomyPath(String taxonomyPath) {
    this.taxonomyPath = taxonomyPath;
  }

  @Required
  public void setContentRepository(ContentRepository contentRepository) {
    this.contentRepository = contentRepository;
  }
}
