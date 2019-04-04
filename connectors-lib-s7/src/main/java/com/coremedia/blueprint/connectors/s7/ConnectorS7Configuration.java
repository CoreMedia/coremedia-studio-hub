package com.coremedia.blueprint.connectors.s7;

import com.coremedia.cache.Cache;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.undoc.common.spring.CapRepositoriesConfiguration;
import com.coremedia.connectors.api.ConnectorConnection;
import com.coremedia.connectors.api.ConnectorService;
import com.coremedia.connectors.content.ConnectorContentConfiguration;
import com.coremedia.connectors.content.ContentCreateService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;

/*
Add an entry into the Settings-Document "Connectors" with the following entries (example)
<Struct>
<StringProperty Name="displayName">Assets</StringProperty>
<StringProperty Name="connectionId">s7Bucket</StringProperty>
<StringProperty Name="type">s7</StringProperty>
<IntegerProperty Name="previewThresholdMB">10</IntegerProperty>
<StringProperty Name="rootFolder">CoreMedia</StringProperty>
<StringProperty Name="folder"></StringProperty>
<StringProperty Name="url">https://s7sps1apissl.scene7.com/scene7/services/IpsApiService</StringProperty>
<StringProperty Name="userid">your user id</StringProperty>
<StringProperty Name="password">your password</StringProperty>
<StringProperty Name="companyHandle">your company handle</StringProperty>
<BooleanProperty Name="searchApiEnabled">false</BooleanProperty>
<StringProperty Name="contentScope">site</StringProperty>
<BooleanProperty Name="enabled">true</BooleanProperty>
</Struct>

  In Settings Document "Content Types" add the entry:

     <Struct>
      <StringProperty Name="name">Scene7</StringProperty>
      <StringProperty Name="connectorType">s7</StringProperty>
      <LinkProperty xlink:href="" LinkType="coremedia:///cap/contenttype/CMSettings" cmexport:path="/Settings/Options/Settings/Connectors/Connector Item Types" />
      <LinkProperty xlink:href="" LinkType="coremedia:///cap/contenttype/CMSettings" cmexport:path="/Settings/Options/Settings/Connectors/Preview Templates" />
      <LinkProperty xlink:href="" LinkType="coremedia:///cap/contenttype/CMSettings" cmexport:path="/Settings/Options/Settings/Connectors/Content Mapping" />
    </Struct>
*/


@Configuration
@Import({CapRepositoriesConfiguration.class, ConnectorContentConfiguration.class})
public class ConnectorS7Configuration {

  @Bean(name = "connector:s7")
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public ConnectorConnection s7ConnectorConnection(@NonNull @Qualifier("connectorS7Service") ConnectorService connectorService) {
    ConnectorConnection connectorConnection = new ConnectorConnection();
    connectorConnection.setConnectorService(connectorService);
    return connectorConnection;
  }

  @Bean
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public ConnectorService connectorS7Service(@NonNull Cache cache) {
    return new S7ConnectorServiceImpl(cache);
  }

  @Bean
  public S7ContentItemWriteInterceptor s7WriteInterceptor(@NonNull ContentRepository contentRepository,
                                                          @NonNull ContentCreateService contentCreateService) {
    S7ContentItemWriteInterceptor s7WriteInterceptor = new S7ContentItemWriteInterceptor();
    s7WriteInterceptor.setPriority(0);
    s7WriteInterceptor.setType(contentRepository.getContentType("CMPicture"));
    s7WriteInterceptor.setContentCreateService(contentCreateService);
    return s7WriteInterceptor;
  }
}
