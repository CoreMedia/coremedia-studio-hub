package com.coremedia.blueprint.connectors.content;

import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.cap.struct.Struct;
import org.springframework.beans.factory.annotation.Required;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Utility class that can be used to tag content with a list of
 * tags that have been read from another source - other than the CMS.
 * The tagger will automatically create the missing content tags and link
 * the newly created tag to the given content.
 */
public class ContentTagger {
  private static final String ROOT_SETTINGS_DOCUMENT = "_root";
  private static final String SETTINGS_STRUCT = "settings";
  private static final String ROOTS_LIST = "roots";
  private static final String TAG_NAME_PROPERTY = "value";
  private static final String TAG_CHILDREN_PROPERTY = "children";

  private String taxonomyPath;
  private ContentRepository contentRepository;

  /**
   * Applies the list of tags to the given content
   *
   * @param content          the content that the tags should be applied to
   * @param parentTag        the parent tag used as overall root tag for all new tags that are not created yet,
   *                         if null, the taxonomy folder is the parent and all nodes will be added as new root nodes
   * @param taxonomyName     the name of the taxonomy the tags should be created in
   * @param taxonomyProperty the content link list property to append the tag documents to
   * @param taxonomyDocType  the document type of the tag, e.g. CMTaxonomy or CMLocTaxonomy.
   * @param tags             the list of tags to apply to the content
   */
  public void tag(@NonNull Content content,
                  @Nullable String parentTag,
                  @NonNull String taxonomyName,
                  @NonNull String taxonomyProperty,
                  @NonNull String taxonomyDocType,
                  @NonNull List<String> tags) {
    Content taxonomiesFolder = contentRepository.getChild(taxonomyPath);
    Content taxonomyFolder = taxonomiesFolder.getChild(taxonomyName);

    ContentType ct = contentRepository.getContentType(taxonomyDocType);
    if (ct == null) {
      throw new RuntimeException("Requested taxonomy content type " + taxonomyDocType + " does not exist.");
    }

    List<Content> result = new ArrayList<>();
    for (String tag : tags) {
      Content tagContent = getOrCreateTag(taxonomyFolder, ct, parentTag, tag);
      result.add(tagContent);
    }

    if (!content.isCheckedOut()) {
      content.checkOut();
    }
    if (!content.isCheckedOutByCurrentSession()) {
      throw new UnsupportedOperationException("The content '" + content.getPath() + "' is currently checked out by another user, no tags can be applied.");
    }

    content.set(taxonomyProperty, result);
  }

  /**
   * Checks if a tag document for the given tag name already exists.
   * If not, the document is created for it
   *
   * @param taxonomyFolder the content folder the taxonomy tree is located in
   * @param contenType     the content type used to create a new tag
   * @param parentTag      the parent tag that should be used as root tag
   * @param tag            that name of the tag to create or search for
   * @return the tag docment that will be linked
   */
  private synchronized Content getOrCreateTag(@NonNull Content taxonomyFolder,
                                              @NonNull ContentType contenType,
                                              @Nullable String parentTag,
                                              @NonNull String tag) {
    //exists check
    Content existingTag = taxonomyFolder.getChild(tag);
    if (existingTag != null) {
      return existingTag;
    }

    Content tagContent = contenType.createByTemplate(taxonomyFolder, tag, "{3} ({1})", new HashMap<>());
    tagContent.set(TAG_NAME_PROPERTY, tag);
    tagContent.checkIn();

    //parent may be null, so we only create root nodes
    if(parentTag != null) {
      Content parent = getOrCreateParent(taxonomyFolder, contenType, parentTag);
      List<Content> children = new ArrayList<>(parent.getLinks(TAG_CHILDREN_PROPERTY));
      children.add(tagContent);

      if (!parent.isCheckedOut()) {
        parent.checkOut();
      }

      parent.set(TAG_CHILDREN_PROPERTY, children);
      if (parent.isCheckedOut()) {
        parent.checkIn();
      }
    }

    return tagContent;
  }

  /**
   * Newly created tags are always added to a specific parent
   *
   * @param taxonomyFolder the content folder the taxonomy tree is located in
   * @param contentType    the content type used to create a new tag
   * @param parentTag      the name of the parent
   * @return the parent tag document
   */
  private Content getOrCreateParent(@NonNull Content taxonomyFolder,
                                    @NonNull ContentType contentType,
                                    @NonNull String parentTag) {
    Content parent = taxonomyFolder.getChild(parentTag);
    if (parent != null) {
      return parent;
    }

    parent = contentType.createByTemplate(taxonomyFolder, parentTag, "{3} ({1})", new HashMap<>());
    parent.set(TAG_NAME_PROPERTY, parentTag);
    parent.checkIn();

    rootLinkCheck(taxonomyFolder, parent);
    return parent;
  }

  /**
   * Check if taxonomy is root settings linked
   */
  private void rootLinkCheck(@NonNull Content taxonomyFolder, Content tagContent) {
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
