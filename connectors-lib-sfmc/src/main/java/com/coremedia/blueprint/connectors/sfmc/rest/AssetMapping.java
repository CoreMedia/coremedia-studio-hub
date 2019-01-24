package com.coremedia.blueprint.connectors.sfmc.rest;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * https://developer.salesforce.com/docs/atlas.en-us.mc-apis.meta/mc-apis/base-asset-types.htm
 */
public class AssetMapping {
  private static Map<Integer, String> baseMapping = new HashMap<>();
  private static Map<String, AssetType> nameMapping = new HashMap<>();
  private static Map<Integer, AssetType> typeMapping = new HashMap<>();

  private static final int DEFAULT_TYPE = 13; //archive

  public static String getBaseType(int type) {
    AssetType value = typeMapping.get(type);
    if (value.inherits == 0) {
      return value.name;
    }

    return baseMapping.get(value.inherits);
  }

  @NonNull
  public static String getAssetName(@NonNull String filename) {
    String suffix = filename;
    if (filename.contains(".")) {
      suffix = suffix.substring(suffix.lastIndexOf(".") + 1);
    }
    return suffix;
  }

  public static int getAssetId(@NonNull String filename) {
    String suffix = filename;
    if (filename.contains(".")) {
      suffix = suffix.substring(suffix.lastIndexOf(".") + 1);
    }

    AssetType assetType = nameMapping.get(suffix);
    if (assetType == null) {
      return DEFAULT_TYPE;
    }

    return assetType.id;
  }

  static class AssetType {
    private int id;
    private String name;
    private int inherits;

    AssetType(int id, String name, int inherits) {
      this.id = id;
      this.name = name;
      this.inherits = inherits;

      nameMapping.put(name, this);
      typeMapping.put(id, this);
    }
  }

  static {
    baseMapping.put(1, "default");
    baseMapping.put(2, "default");
    baseMapping.put(3, "text");
    baseMapping.put(4, "text");
    baseMapping.put(5, "text");
    baseMapping.put(6, "default");
    baseMapping.put(7, "default");
    baseMapping.put(8, "picture");
    baseMapping.put(9, "picture");
    baseMapping.put(10, "video");
    baseMapping.put(11, "default");
    baseMapping.put(12, "audio");
    baseMapping.put(13, "default");
    baseMapping.put(14, "text");
    baseMapping.put(15, "text");

    new AssetType(16, "ai", 8);
    new AssetType(17, "psd", 8);
    new AssetType(18, "pdd", 8);
    new AssetType(19, "eps", 8);
    new AssetType(20, "gif", 8);
    new AssetType(21, "jpe", 8);
    new AssetType(22, "jpeg", 8);
    new AssetType(23, "jpg", 8);
    new AssetType(24, "jp2", 8);
    new AssetType(25, "jpx", 8);
    new AssetType(26, "pict", 8);
    new AssetType(27, "pct", 8);
    new AssetType(28, "png", 8);
    new AssetType(29, "tif", 8);
    new AssetType(30, "tiff", 8);
    new AssetType(31, "tga", 8);
    new AssetType(32, "bmp", 8);
    new AssetType(33, "wmf", 8);
    new AssetType(34, "vsd", 8);
    new AssetType(35, "pnm", 8);
    new AssetType(36, "pgm", 8);
    new AssetType(37, "pbm", 8);
    new AssetType(38, "ppm", 8);
    new AssetType(39, "svg", 8);
    new AssetType(40, "3fr", 9);
    new AssetType(41, "ari", 9);
    new AssetType(42, "arw", 9);
    new AssetType(43, "bay", 9);
    new AssetType(44, "cap", 9);
    new AssetType(45, "crw", 9);
    new AssetType(46, "cr2", 9);
    new AssetType(47, "dcr", 9);
    new AssetType(48, "dcs", 9);
    new AssetType(49, "dng", 9);
    new AssetType(50, "drf", 9);
    new AssetType(51, "eip", 9);
    new AssetType(52, "erf", 9);
    new AssetType(53, "fff", 9);
    new AssetType(54, "iiq", 9);
    new AssetType(55, "k25", 9);
    new AssetType(56, "kdc", 9);
    new AssetType(57, "mef", 9);
    new AssetType(58, "mos", 9);
    new AssetType(59, "mrw", 9);
    new AssetType(60, "nef", 9);
    new AssetType(61, "nrw", 9);
    new AssetType(62, "orf", 9);
    new AssetType(63, "pef", 9);
    new AssetType(64, "ptx", 9);
    new AssetType(65, "pxn", 9);
    new AssetType(66, "raf", 9);
    new AssetType(67, "raw", 9);
    new AssetType(68, "rw2", 9);
    new AssetType(69, "rwl", 9);
    new AssetType(70, "rwz", 9);
    new AssetType(71, "srf", 9);
    new AssetType(72, "sr2", 9);
    new AssetType(73, "srw", 9);
    new AssetType(74, "x3f", 9);
    new AssetType(75, "3gp", 10);
    new AssetType(76, "3gpp", 10);
    new AssetType(77, "3g2", 10);
    new AssetType(78, "3gp2", 10);
    new AssetType(79, "asf", 10);
    new AssetType(80, "avi", 10);
    new AssetType(81, "m2ts", 10);
    new AssetType(82, "mts", 10);
    new AssetType(83, "dif", 10);
    new AssetType(84, "dv", 10);
    new AssetType(85, "mkv", 10);
    new AssetType(86, "mpg", 10);
    new AssetType(87, "f4v", 10);
    new AssetType(88, "flv", 10);
    new AssetType(89, "mjpg", 10);
    new AssetType(90, "mjpeg", 10);
    new AssetType(91, "mxf", 10);
    new AssetType(92, "mpeg", 10);
    new AssetType(93, "mp4", 10);
    new AssetType(94, "m4v", 10);
    new AssetType(95, "mp4v", 10);
    new AssetType(96, "mov", 10);
    new AssetType(97, "swf", 10);
    new AssetType(98, "wmv", 10);
    new AssetType(99, "rm", 10);
    new AssetType(100, "ogv", 10);
    new AssetType(101, "indd", 11);
    new AssetType(102, "indt", 11);
    new AssetType(103, "incx", 11);
    new AssetType(104, "wwcx", 11);
    new AssetType(105, "doc", 11);
    new AssetType(106, "docx", 11);
    new AssetType(107, "dot", 11);
    new AssetType(108, "dotx", 11);
    new AssetType(109, "mdb", 11);
    new AssetType(110, "mpp", 11);
    new AssetType(111, "ics", 11);
    new AssetType(112, "xls", 11);
    new AssetType(113, "xlsx", 11);
    new AssetType(114, "xlk", 11);
    new AssetType(115, "xlsm", 11);
    new AssetType(116, "xlt", 11);
    new AssetType(117, "xltm", 11);
    new AssetType(118, "csv", 11);
    new AssetType(119, "tsv", 11);
    new AssetType(120, "tab", 11);
    new AssetType(121, "pps", 11);
    new AssetType(122, "ppsx", 11);
    new AssetType(123, "ppt", 11);
    new AssetType(124, "pptx", 11);
    new AssetType(125, "pot", 11);
    new AssetType(126, "thmx", 11);
    new AssetType(127, "pdf", 11);
    new AssetType(128, "ps", 11);
    new AssetType(129, "qxd", 11);
    new AssetType(130, "rtf", 11);
    new AssetType(131, "sxc", 11);
    new AssetType(132, "sxi", 11);
    new AssetType(133, "sxw", 11);
    new AssetType(134, "odt", 11);
    new AssetType(135, "ods", 11);
    new AssetType(136, "ots", 11);
    new AssetType(137, "odp", 11);
    new AssetType(138, "otp", 11);
    new AssetType(139, "epub", 11);
    new AssetType(140, "dvi", 11);
    new AssetType(141, "key", 11);
    new AssetType(142, "keynote", 11);
    new AssetType(143, "pez", 11);
    new AssetType(144, "aac", 12);
    new AssetType(145, "m4a", 12);
    new AssetType(146, "au", 12);
    new AssetType(147, "aif", 12);
    new AssetType(148, "aiff", 12);
    new AssetType(149, "aifc", 12);
    new AssetType(150, "mp3", 12);
    new AssetType(151, "wav", 12);
    new AssetType(152, "wma", 12);
    new AssetType(153, "midi", 12);
    new AssetType(154, "oga", 12);
    new AssetType(155, "ogg", 12);
    new AssetType(156, "ra", 12);
    new AssetType(157, "vox", 12);
    new AssetType(158, "voc", 12);
    new AssetType(159, "7z", 13);
    new AssetType(160, "arj", 13);
    new AssetType(161, "bz2", 13);
    new AssetType(162, "cab", 13);
    new AssetType(163, "gz", 13);
    new AssetType(164, "gzip", 13);
    new AssetType(165, "iso", 13);
    new AssetType(166, "lha", 13);
    new AssetType(167, "sit", 13);
    new AssetType(168, "tgz", 13);
    new AssetType(169, "jar", 13);
    new AssetType(170, "rar", 13);
    new AssetType(171, "tar", 13);
    new AssetType(172, "zip", 13);
    new AssetType(173, "gpg", 13);
    new AssetType(174, "htm", 14);
    new AssetType(175, "html", 14);
    new AssetType(176, "xhtml", 14);
    new AssetType(177, "xht", 14);
    new AssetType(178, "css", 14);
    new AssetType(179, "less", 14);
    new AssetType(180, "sass", 14);
    new AssetType(181, "js", 14);
    new AssetType(182, "json", 14);
    new AssetType(183, "atom", 14);
    new AssetType(184, "rss", 14);
    new AssetType(185, "xml", 14);
    new AssetType(186, "xsl", 14);
    new AssetType(187, "xslt", 14);
    new AssetType(188, "md", 14);
    new AssetType(189, "markdown", 14);
    new AssetType(190, "as", 14);
    new AssetType(191, "fla", 14);
    new AssetType(192, "eml", 14);
    new AssetType(193, "text", 15);
    new AssetType(194, "txt", 15);
    new AssetType(195, "freeformblock", 3);
    new AssetType(196, "textblock", 3);
    new AssetType(197, "htmlblock", 3);
    new AssetType(198, "textplusimageblock", 3);
    new AssetType(199, "imageblock", 3);
    new AssetType(200, "abtestblock", 3);
    new AssetType(201, "dynamicblock", 3);
    new AssetType(202, "stylingblock", 3);
    new AssetType(203, "einsteincontentblock", 3);
    new AssetType(205, "webpage", 1);
    new AssetType(206, "webtemplate", 1);
    new AssetType(207, "templatebasedemail", 5);
    new AssetType(208, "htmlemail", 5);
    new AssetType(209, "textonlyemail", 5);
    new AssetType(210, "socialshareblock", 3);
    new AssetType(211, "socialfollowblock", 3);
    new AssetType(212, "buttonblock", 3);
    new AssetType(213, "layoutblock", 3);
    new AssetType(214, "defaulttemplate", 4);
    new AssetType(215, "smartcaptureblock", 0);
    new AssetType(216, "smartcaptureformfieldblock", 0);
    new AssetType(217, "smartcapturesubmitoptionsblock", 0);
    new AssetType(218, "slotpropertiesblock", 0);
    new AssetType(219, "externalcontentblock", 0);
    new AssetType(220, "codesnippetblock", 0);
    new AssetType(221, "rssfeedblock", 0);
    new AssetType(222, "formstylingblock", 0);
    new AssetType(223, "referenceblock", 0);
    new AssetType(224, "imagecarouselblock", 0);
    new AssetType(225, "customblock", 0);
    new AssetType(226, "liveimageblock", 0);
    new AssetType(227, "livesettingblock", 0);
    new AssetType(228, "contentmap", 0);
    new AssetType(230, "jsonmessage", 1);
  }
}
