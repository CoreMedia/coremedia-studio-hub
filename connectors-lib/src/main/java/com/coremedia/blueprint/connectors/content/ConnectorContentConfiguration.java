package com.coremedia.blueprint.connectors.content;

import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.mimetype.MimeTypeService;
import com.coremedia.springframework.xml.ResourceAwareXmlBeanDefinitionReader;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource(
        value = {
                "classpath:/com/coremedia/mimetype/mimetype-service.xml",
                "classpath:/com/coremedia/cap/common/uapi-services.xml"
        },
        reader = ResourceAwareXmlBeanDefinitionReader.class
)
public class ConnectorContentConfiguration {

  @Bean
  public ContentCreateService contentCreateService(@NonNull ContentRepository contentRepository,
                                                   @NonNull MimeTypeService mimeTypeService) {
    return new ContentCreateServiceImpl(contentRepository, mimeTypeService);
  }

  @Bean
  public ConnectorItemWriteInterceptor connectorItemWriteInterceptor(@NonNull ContentCreateService contentCreateService,
                                                                     @NonNull ContentRepository contentRepository) {
    ContentType contentType = contentRepository.getContentType("CMTeasable");

    ConnectorItemWriteInterceptor connectorItemWriteInterceptor = new ConnectorItemWriteInterceptor();
    connectorItemWriteInterceptor.setContentCreateService(contentCreateService);
    connectorItemWriteInterceptor.setPriority(0);
    connectorItemWriteInterceptor.setType(contentType);
    return connectorItemWriteInterceptor;
  }

  @Bean
  public ContentTagger connectorContentTagger(@NonNull ContentRepository contentRepository,
                                              @NonNull @Value("${connectors.taxonomies.path:/Settings/Taxonomies}") String taxonomyPath) {
    ContentTagger contentTagger = new ContentTagger();
    contentTagger.setContentRepository(contentRepository);
    contentTagger.setTaxonomyPath(taxonomyPath);
    return contentTagger;
  }
}
