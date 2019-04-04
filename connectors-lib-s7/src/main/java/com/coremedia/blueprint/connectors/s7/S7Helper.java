package com.coremedia.blueprint.connectors.s7;

import com.scene7.ipsapi.xsd._2013_02_15.Asset;

/**
 * The ids of S7 objects contain the path separator which can't be used
 * as external id for categories or assets. We replace them here.
 */
class S7Helper {

    static String getName(S7Container summary) {
        if (summary == null) {
            return null;
        }
        Asset asset = summary.getAsset();
        if (asset != null) {
            String filename = asset.getName();
            return filename;
        } else if (summary.getFolder() != null) {
            return summary.getFolder().getPath();
        }
        return null;
    }

}
