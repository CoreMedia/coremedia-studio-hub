package com.coremedia.blueprint.connectors.cloudinary.rest;

import com.coremedia.connectors.api.ConnectorContext;
import com.coremedia.connectors.api.ConnectorItemTypes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static com.coremedia.connectors.api.ConnectorItem.DEFAULT_TYPE;

/**
 * 0 = {java.util.HashMap$Node@22265} "bytes" -> "2298695"
 * 1 = {java.util.HashMap$Node@22266} "format" -> "jpg"
 * 2 = {java.util.HashMap$Node@22267} "resource_type" -> "image"
 * 3 = {java.util.HashMap$Node@22268} "width" -> "2815"
 * 4 = {java.util.HashMap$Node@22269} "secure_url" -> "https://res.cloudinary.com/coremedia/image/upload/v1526389142/CoreMedia/14965400A1A93C87.jpg"
 * 5 = {java.util.HashMap$Node@22270} "created_at" -> "2018-05-15T12:59:02Z"
 * 6 = {java.util.HashMap$Node@22271} "type" -> "upload"
 * 7 = {java.util.HashMap$Node@22272} "version" -> "1526389142"
 * 8 = {java.util.HashMap$Node@22273} "url" -> "http://res.cloudinary.com/coremedia/image/upload/v1526389142/CoreMedia/14965400A1A93C87.jpg"
 * 9 = {java.util.HashMap$Node@22274} "public_id" -> "CoreMedia/14965400A1A93C87"
 * 10 = {java.util.HashMap$Node@22275} "height" -> "1823"
 */
public class CloudinaryAsset extends CloudinaryEntity {
  private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  private String id;
  private long size;
  private String name;
  private String type;
  private String format;
  private String resourceType;
  private String url;
  private String secureUrl;
  private Date lastModificationDate;
  private int width;
  private int height;
  private String folder;


  public CloudinaryAsset(Map data) {
    this.id = (String) data.get("public_id");
    this.type = (String) data.get("type");
    this.url = (String) data.get("url");
    this.secureUrl = (String) data.get("secure_url");
    this.resourceType = (String) data.get("resource_type");
    this.format = (String) data.get("format");
    this.size = (Integer) data.get("bytes");

    String[] split = id.split("/");
    this.name = split[split.length-1];
    this.folder = "";
    if(id.contains("/")) {
      this.folder = id.substring(0, id.lastIndexOf('/'));
    }

    String dateString = (String) data.get("created_at");
    try {
      this.lastModificationDate = new SimpleDateFormat(DATE_FORMAT).parse(dateString);
    } catch (ParseException e) {
      //ignore
    }

    if(data.containsKey("width")) {
      width = (int) data.get("width");
    }
    if(data.containsKey("height")) {
      height = (int) data.get("height");
    }
  }

  public String getFolder() {
    return folder;
  }

  public long getSize() {
    return size;
  }

  public String getName() {
    return name;
  }

  public Date getLastModificationDate() {
    return lastModificationDate;
  }

  public String getId() {
    return id;
  }

  public String getType() {
    return type;
  }

  public String getFormat() {
    return format;
  }

  public String getResourceType() {
    return resourceType;
  }

  public String getUrl() {
    return url;
  }

  public String getSecureUrl() {
    return secureUrl;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public boolean isInFolder(String folder) {
    if(id.startsWith(folder)) {
      if(!folder.endsWith("/")) {
        folder = folder + "/";
      }

      String idSegment = id.substring(folder.length(), id.length());
      return !idSegment.contains("/");
    }

    return false;
  }

  public String getConnectorItemType(ConnectorContext context) {
    ConnectorItemTypes itemTypes = context.getItemTypes();
    if (itemTypes != null) {
      String typeForName = itemTypes.getTypeForName(getName());
      if (typeForName != null) {
        return typeForName;
      }

      if(getFormat() != null) {
        typeForName = itemTypes.getTypeForName(getFormat());
      }

      if(typeForName != null) {
        return typeForName;
      }
    }

    String format = getResourceType();
    if(format.equals("image")) {
      return "picture";
    }
    if(format.equals("video")) {
      return "video";
    }
    return DEFAULT_TYPE;
  }

  public String getMimeType(ConnectorContext context) {
    String resourceType = getResourceType();
    //use mime type guessing instead
    if(resourceType.equals("raw") && getName().contains(".")) {
      return null;
    }

    String itemType = getConnectorItemType(context);
    if("audio".equals(itemType)) {
      resourceType = "audio";
    }

    //fix mime type
    String format = getFormat();
    if(format.equals("jpg")) {
      format = "jpeg";
    }
    if(format.equals("pdf")) {
      resourceType = "application";
    }

    return resourceType + "/" + format;
  }
}
