package com.coremedia.blueprint.connectors.s3;

import com.amazonaws.services.s3.model.S3ObjectSummary;

/**
 * The ids of S3 objects contain the path separator which can't be used
 * as external id for categories or assets. We replace them here.
 */
class S3Helper {

  static String formatName(S3ObjectSummary summary) {
    String s3Name = summary.getKey();
    String[] segments = s3Name.split("/");

    if (s3Name.contains("/")) {
      return segments[segments.length - 1];
    }

    return s3Name;
  }

  static boolean isItemOf(S3ObjectSummary summary, String path) {
    String key = summary.getKey();
    if (key.endsWith("/")) {
      return false;
    }

    if (!key.contains(path)) {
      return false;
    }

    String segment = key.substring(path.length(), key.length());
    return !segment.contains("/");
  }

  static boolean isSubCategoryOf(S3ObjectSummary summary, String path) {
    //is it a category
    if(!summary.getKey().endsWith("/")) {
      return false;
    }

    String key = summary.getKey();

    if (path.length() < key.length() && key.contains(path)) {
      //remove the leading path and the last folder separator the end
      String segment = key.substring(path.length(), key.length() - 1);
      return !segment.contains("/"); //no other subfolder inside this segment?
    }

    return false;
  }
}
