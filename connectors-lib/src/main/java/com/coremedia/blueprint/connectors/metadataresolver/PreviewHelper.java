package com.coremedia.blueprint.connectors.metadataresolver;

import com.coremedia.blueprint.connectors.api.ConnectorItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import javax.annotation.Nonnull;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 */
public class PreviewHelper {

  static String formatDate(Calendar calendar) {
    if (calendar != null) {
      DateFormat simpleSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      return simpleSDF.format(new Date(calendar.getTimeInMillis()));
    }
    return null;
  }

  /**
   * Pretty format for meta data keys.
   *
   * @param key The key to format.
   * @return the formatted key
   */
  public static String formatKey(String key) {
    String result = key;
    if (result.contains(":")) {
      result = result.substring(result.lastIndexOf(':') + 1, result.length());
    }
    result = WordUtils.capitalize(result);
    result = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(result), ' ');
    result = result.replaceAll(" - ", "-");
    result = result.replaceAll(" / ", "/");
    return result;
  }

  @Nonnull
  public static String createStudioLink(@Nonnull ConnectorItem item, @Nonnull String label) {
    return "<a style=\"color:black;\" href=\"javascript:Ext.getCmp('connectorRepositoryList').showItem('" + item.getConnectorId() + "');\">" + label + "</a>";
  }
}
