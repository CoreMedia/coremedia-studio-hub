package com.coremedia.blueprint.connectors.s7;

import com.coremedia.connectors.api.ConnectorItem;
import com.coremedia.connectors.content.ConnectorItemWriteInterceptor ;
import com.coremedia.rest.cap.intercept.ContentWriteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Map;

/**
 * Sets the S7 image width and height
 */
public class S7ContentItemWriteInterceptor extends ConnectorItemWriteInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(S7ConnectorServiceImpl.class);

    @Override
    public void intercept(ContentWriteRequest request) {
        Map<String, Object> properties = request.getProperties();
        if (properties.containsKey(CONNECTOR_ENTITY)) {
            ConnectorItem item = (ConnectorItem) properties.get(CONNECTOR_ENTITY);
            if (item instanceof S7ConnectorItem) {
                try {
                    URL url = new URL(item.getOpenInTabUrl());
                    BufferedImage image = ImageIO.read(url);
                    properties.put("width", image.getWidth());
                    properties.put("height", image.getHeight());
                } catch (Exception e) {
                    LOGGER.error("Can't set width and height", e);
                }
            }
        }
    }
}
