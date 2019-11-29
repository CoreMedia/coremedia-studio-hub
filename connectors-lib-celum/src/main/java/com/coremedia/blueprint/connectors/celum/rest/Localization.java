package com.coremedia.blueprint.connectors.celum.rest;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 */
public class Localization {
  private static Map<String,String> dateFormats = new HashMap<>();

  static {
    dateFormats.put("en", "yyyy/MM/dd");
    dateFormats.put("de", "dd.MM.yyyy");
  }

  public static String getLabel(List<LocalizedLabel> labels, String language) {
    for (LocalizedLabel label : labels) {
      if (label.getLocale().equalsIgnoreCase(language)) {
        String value = label.getValue();
        if (!StringUtils.isEmpty(value)) {
          return value;
        }
      }
    }
    return getFirstLabelValue(labels);
  }

  public static String getFirstLabelValue(List<LocalizedLabel> labels) {
    for (LocalizedLabel label : labels) {
      String value = label.getValue();
      if (!StringUtils.isEmpty(value)) {
        return value;
      }
    }

    return "";
  }

  public static String formatDate(Locale locale, Date creationDate) {
    if(dateFormats.containsKey(locale.getLanguage())) {
      return new SimpleDateFormat(dateFormats.get(locale.getLanguage())).format(creationDate);
    }

    return new SimpleDateFormat(dateFormats.get("en")).format(creationDate);
  }
}
