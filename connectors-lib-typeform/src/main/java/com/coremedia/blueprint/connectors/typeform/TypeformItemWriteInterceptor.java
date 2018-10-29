package com.coremedia.blueprint.connectors.typeform;

import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.content.ConnectorItemWriteInterceptor;
import com.coremedia.blueprint.connectors.typeform.data.Form;
import com.coremedia.rest.cap.intercept.ContentWriteRequest;
import com.coremedia.xml.Markup;
import com.coremedia.xml.MarkupFactory;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.Map;

public class TypeformItemWriteInterceptor extends ConnectorItemWriteInterceptor {

    public static final String EMBED_CODE = "embedCode";

    @Override
    public void intercept(ContentWriteRequest request) {
        Map<String, Object> properties = request.getProperties();
        if (properties.containsKey(CONNECTOR_ENTITY)) {
            ConnectorEntity entity = (ConnectorEntity) properties.get(CONNECTOR_ENTITY);

            //no actual blob, so we re-use the method to set the youtube video URL
            if (entity instanceof TypeformItem) {
                TypeformItem form = (TypeformItem) entity;
                String embedCodeTemplate = form.getContext().getProperty(EMBED_CODE);

                properties.put("title", form.getName());
                properties.put("teaserTitle", form.getName());
                Form typeform = form.getForm();
                String embedCode = String.format(embedCodeTemplate, typeform.getDisplay());
                if (typeform != null) {
                    properties.put("data", createHTMLMarkup(embedCode));
                }
            }
        }
    }

    protected Markup createHTMLMarkup(String description) {
        if (description != null) {
            description = StringEscapeUtils.escapeHtml4(description);
            String markup = "<?xml version=\"1.0\" ?><div xmlns=\"http://www.coremedia.com/2003/richtext-1.0\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"><p>" + description + "</p></div>";
            return MarkupFactory.fromString(markup);
        } else {
            return null;
        }
    }
}
