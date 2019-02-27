package com.coremedia.blueprint.connectors.s7;


import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class S7ConnectorEntity implements ConnectorEntity {

    private static final Logger LOGGER = LoggerFactory.getLogger(S7ConnectorEntity.class);

    public static final String PNG = ".jpg";
    private static final Pattern FOLDER_NAME = Pattern.compile(".*\\/(.*)\\/$");
    public static final String ASSET_URL_PATTERN = "assetUrl";
    protected S7Container file;
    private ConnectorId connectorId;
    private String name;
    private ConnectorContext context;
    private ConnectorCategory parent;
    private String assetUrlPattern = "http://s7d2.scene7.com/is/image/%s?scl=1";
    private String videoUrlPattern = "http://s7d2.scene7.com/is/content/%s";


    S7ConnectorEntity(ConnectorCategory parent, ConnectorContext context, ConnectorId connectorId, S7Container file) {
        this.context = context;
        this.connectorId = connectorId;
        this.parent = parent;
        this.name = S7Helper.getName(file);
        this.file = file;

        String assetUrl = context.getProperty(ASSET_URL_PATTERN);
        if (!StringUtils.isBlank(assetUrl)) {
            assetUrlPattern = assetUrl;
        }
    }

    public S7Container getFile() {
        return file;
    }

    @Override
    public boolean isDeleteable() {
        return true;
    }

    @Override
    public boolean delete() {
        return false;
    }

    public Date getLastModified() {
        return new Date();
    }

    @NonNull
    @Override
    public String getName() {
        if (file.getAsset() != null) {
            switch (file.getAsset().getType().toLowerCase()) {
                case "image":return file.getAsset().getFileName();
                case "video":return file.getAsset().getFileName();
                default: return name;
            }
            //return name + PNG;
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @NonNull
    public String getUrl() {
        if (file.getAsset() != null) {
            switch (file.getAsset().getType().toLowerCase()) {
                case "image":return String.format(assetUrlPattern, file.getAsset().getIpsImageUrl());
                case "video":return String.format(videoUrlPattern, file.getAsset().getIpsImageUrl());
            }
        }

        return String.format(assetUrlPattern, getName());
    }

    public String getFolder() {
        if (file.getAsset() != null) {
            return file.getAsset().getFolder();
        }
        return null;
    }

    @NonNull
    @Override
    public ConnectorContext getContext() {
        return context;
    }

    public void setContext(ConnectorContext context) {
        this.context = context;
    }

    @Override
    public ConnectorCategory getParent() {
        return parent;
    }

    public void setParent(ConnectorCategory parent) {
        this.parent = parent;
    }

    @NonNull
    @Override
    public String getDisplayName() {
        if (file.getFolder() != null && StringUtils.isNoneBlank(name)) {
            String folderName = name;
            if (!folderName.endsWith("/")) {
                folderName = folderName + "/";
            }
            Matcher matcher = FOLDER_NAME.matcher(folderName);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return getName();
    }

    public ConnectorId getConnectorId() {
        return connectorId;
    }

    @Nullable
    @Override
    public String getManagementUrl() {
        return null;
    }
}
