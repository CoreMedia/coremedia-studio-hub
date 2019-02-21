package com.coremedia.blueprint.connectors.instagram;

import com.coremedia.blueprint.connectors.api.*;
import com.coremedia.blueprint.connectors.impl.ConnectorPropertyNames;
import com.coremedia.blueprint.connectors.library.DefaultConnectorColumnValue;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 */
public class InstagramItem implements ConnectorItem {

    public static final String ITEM_TYPE = "Instagram";
    public static final String CREATED_TIME = "createdTime";

    private ConnectorId id;
    private ConnectorContext context;
    private String name;
    private ConnectorCategory category;

    private String description;
    private String thumbnailUrl;
    private String lowResolutionUrl;
    private String link;
    private Date createdTime;
    private String embbedCode;

    private final String dateFormat = "yyyy/MM/dd hh:mm a";


    public InstagramItem(ConnectorId id, ConnectorContext context, String name, ConnectorCategory category) {
        this.id = id;
        this.context = context;
        this.name = name;
        this.category = category;
    }

    @NonNull
    @Override
    public String getItemType() {
        return ITEM_TYPE;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    private String getCreatedTimeAsString() {
        DateFormat pstFormat = new SimpleDateFormat(dateFormat);
        return pstFormat.format(getCreatedTime());
    }

    @Nullable
    @Override
    public String getPreviewHtml() {
        //  return getEmbbedCode();
        if (StringUtils.isNotBlank(getLowResolutionUrl())){
           return MessageFormat.format("<img style=\"max-width:100%;\" src=\"{0}\" />", getLowResolutionUrl());
        }
        return getEmbbedCode();
    }

    public List<ConnectorColumnValue> getColumnValues() {
        return Arrays.asList(new DefaultConnectorColumnValue(getCreatedTimeAsString(), CREATED_TIME));
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Nullable
    @Override
    public String getDescription() {
        return description;
    }

    @Nullable
    @Override
    public InputStream stream() {
        //return null for ContentItemInterceptor (will be overwritten by the InstragramContentWriterInterceptor
        return null;
    }

    @Nullable
    @Override
    public String getOpenInTabUrl() {
        return getLink();
    }

    @Nullable
    @Override
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    @Nullable
    public String getLowResolutionUrl() {
        return lowResolutionUrl;
    }

    public String getLink() {
        return link;
    }

    @Nullable
    @Override
    public ConnectorMetaData getMetaData() {

        return () -> {
            Map<String, Object> metaData = new HashMap<>();
            metaData.put(ConnectorPropertyNames.URL, getLink());
            metaData.put("description", getDescription());
            return metaData;
        };
    }

    @Override
    public boolean isDownloadable() {
        return false;
    }

    @Override
    public ConnectorId getConnectorId() {
        return id;
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }

    @NonNull
    @Override
    public ConnectorContext getContext() {
        return context;
    }

    @Nullable
    @Override
    public ConnectorCategory getParent() {
        return category;
    }

    @NonNull
    @Override
    public String getDisplayName() {
        if (StringUtils.isNotEmpty(getDescription())) {
            return StringUtils.abbreviate(getDescription(), 100);
        }
        return getName();
    }

    @Nullable
    @Override
    public Date getLastModified() {
        return null;
    }

    @Nullable
    @Override
    public String getManagementUrl() {
        return null;
    }

    @Override
    public boolean isDeleteable() {
        return false;
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Nullable
    public String getEmbbedCode() {
        return embbedCode;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setLowResolutionUrl(String lowResolutionUrl) {
        this.lowResolutionUrl = lowResolutionUrl;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public void setEmbbedCode(String embbedCode) {
        this.embbedCode = embbedCode;
    }
}
