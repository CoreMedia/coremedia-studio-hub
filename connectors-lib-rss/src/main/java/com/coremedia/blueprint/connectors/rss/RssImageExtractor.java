package com.coremedia.blueprint.connectors.rss;

import com.sun.syndication.feed.module.mediarss.MediaEntryModule;
import com.sun.syndication.feed.module.mediarss.types.MediaContent;
import com.sun.syndication.feed.module.mediarss.types.MediaGroup;
import com.sun.syndication.feed.module.mediarss.types.Thumbnail;
import com.sun.syndication.feed.synd.SyndEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to extract all image URLs from a feed entry.
 */
class RssImageExtractor {

  /**
   * Evaluates the HTML and the metadata for the RSS entry to find images
   * @param entry the RSS entry to evaluate
   * @return the list of image URLs
   */
  static List<String> extractImageUrls(SyndEntry entry) {
    List<String> result = new ArrayList<>();

    for (Object module : entry.getModules()) {
      if (module instanceof MediaEntryModule) {
        MediaEntryModule media = (MediaEntryModule)module;
        for (Thumbnail thumb : media.getMetadata().getThumbnail()) {
          String url = thumb.getUrl().toString();
          duplicateCheck(result, url);
        }
        for (MediaGroup group: media.getMediaGroups()) {
          MediaContent[] contents = group.getContents();
          for (MediaContent content : contents) {
            if(content.getType().startsWith("image") && content.getReference() != null) {
              String url = content.getReference().toString();
              duplicateCheck(result, url);
            }
          }
        }
      }
    }
    return result;
  }

  private static void duplicateCheck(List<String> result, String url) {
    if(url.contains("?")) {
      url = url.substring(0, url.indexOf("?"));
    }

    if(!result.contains(url)) {
      result.add(url);
    }
  }
}
