package com.coremedia.blueprint.connectors.instagram;

import com.coremedia.connectors.api.ConnectorEntity;
import com.coremedia.connectors.content.ConnectorItemWriteInterceptor;
import com.coremedia.rest.cap.intercept.ContentWriteRequest;
import com.coremedia.xml.Markup;
import com.coremedia.xml.MarkupFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Map;

public class InstagramWriteInterceptor extends ConnectorItemWriteInterceptor {

    private static final String MARKUP_TEMPLATE = "<div xmlns=\"http://www.coremedia.com/2003/richtext-1.0\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"></div>";


    @Override
    public void intercept(ContentWriteRequest request) {
        Map<String, Object> properties = request.getProperties();
        if (properties.containsKey(CONNECTOR_ENTITY)) {
            ConnectorEntity entity = (ConnectorEntity) properties.get(CONNECTOR_ENTITY);
            //the intercepts is only applicable when the content was created for an Example Connector entity
            if (entity instanceof InstagramItem) {
                InstagramItem instagramItem = (InstagramItem) entity;
                properties.put("data", convertStringToMarkup(instagramItem.getEmbbedCode()));
                properties.put("teaserTitle", instagramItem.getDisplayName());
                properties.put("description", instagramItem.getDescription());
            }
        }
    }

    /**
     * Creates a simple CoreMedia Markup element and adds the given string as node.
     *
     * @param code The payload for the Markup
     * @return The payload wrapped in a CoreMedia markup.
     */
    private Markup convertStringToMarkup(String code) {
        Markup richtext = MarkupFactory.fromString(MARKUP_TEMPLATE);
        Document document = richtext.asDocument();
        Element p = document.createElementNS("http://www.coremedia.com/2003/richtext-1.0", "p");
        document.getDocumentElement().appendChild(p);
        Node node = document.createTextNode(code);
        p.appendChild(node);
        return MarkupFactory.fromDOM(document);
    }
}
