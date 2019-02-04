package com.coremedia.blueprint.connectors.upload;

import com.coremedia.blueprint.connectors.impl.ConnectorImplConfiguration;
import com.coremedia.blueprint.connectors.impl.Connectors;
import com.coremedia.cap.transform.TransformImageService;
import com.coremedia.image.ImageDimensionsExtractor;
import com.coremedia.transform.BlobTransformer;
import com.coremedia.transform.NamedTransformBeanBlobTransformer;
import com.coremedia.transform.impl.ExpressionBasedBeanBlobTransformer;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

import java.util.List;


@Configuration
@ImportResource(value = {
        "classpath:/com/coremedia/image/image-service.xml",
        "classpath:/com/coremedia/cap/transform/transform-services.xml"
})
@Import({ConnectorImplConfiguration.class})
public class ConnectorUploadConfiguration {
  @Bean
  public ConnectorContentUploadService connectorContentUploadService(@NonNull Connectors connectors,
                                                                     @Autowired(required = false) List<ConnectorContentUploadInterceptor> connectorContentUploadInterceptors,
                                                                     @NonNull ConnectorImageTransformationService connectorImageTransformationService) {
    return new ConnectorContentUploadService(connectors, connectorImageTransformationService, connectorContentUploadInterceptors);
  }

  @Bean
  public ConnectorImageTransformationService connectorImageTransformationService(@NonNull TransformImageService transformImageService,
                                                                                 @NonNull BlobTransformer blobTransformer,
                                                                                 @NonNull ImageDimensionsExtractor imageDimensionsExtractor,
                                                                                 @NonNull @Qualifier("contentMediaTransformer") NamedTransformBeanBlobTransformer mediaTransformer) {
    return new ConnectorImageTransformationService(transformImageService, blobTransformer, imageDimensionsExtractor, mediaTransformer);
  }

  @Bean("contentMediaTransformer")
  public NamedTransformBeanBlobTransformer beanBlobTransformer(@NonNull BlobTransformer blobTransformer) {
    ExpressionBasedBeanBlobTransformer transformer = new ExpressionBasedBeanBlobTransformer();
    transformer.setBlobTransformer(blobTransformer);
    transformer.setDataExpression("data");
    transformer.setTransformMapExpression("transformMap");
    return transformer;
  }
}
