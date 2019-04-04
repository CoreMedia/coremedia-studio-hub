package com.coremedia.blueprint.connectors.s7;

import com.coremedia.blueprint.connectors.s7.client.IpsApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 */
public class S7Callable implements Callable<List<S7Container>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(S7ConnectorServiceImpl.class);

    private String folder;
    private IpsApiClient client;

    public S7Callable(String folder, IpsApiClient client) {
        this.folder = folder;
        this.client = client;
    }

    @Override
    public List<S7Container> call() throws Exception {
        LOGGER.info("Searching files in folder: {}", folder);

        return Collections.emptyList();
    }
}
