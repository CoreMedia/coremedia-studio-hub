package com.coremedia.blueprint.connectors;

import com.coremedia.blueprint.connectors.caching.ConnectorCachingConfiguration;
import com.coremedia.blueprint.connectors.content.ConnectorContentConfiguration;
import com.coremedia.blueprint.connectors.filesystems.ConnectorFilesystemsConfiguration;
import com.coremedia.blueprint.connectors.impl.ConnectorImplConfiguration;
import com.coremedia.blueprint.connectors.metadataresolver.MetadataResolverConfiguration;
import com.coremedia.blueprint.connectors.previewconverters.ConnectorPreviewConvertersConfiguration;
import com.coremedia.blueprint.connectors.upload.ConnectorUploadConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
                MetadataResolverConfiguration.class,
                ConnectorPreviewConvertersConfiguration.class,
                ConnectorFilesystemsConfiguration.class,
                ConnectorContentConfiguration.class,
                ConnectorCachingConfiguration.class,
                ConnectorImplConfiguration.class,
                ConnectorUploadConfiguration.class
        })
public class ConnectorBundleConfiguration {
}
