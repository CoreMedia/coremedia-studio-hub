package com.coremedia.blueprint.connectors.instagram;

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

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;


/*
Add an entry into the Settings-Document "Connectors" with the following entries (example)

     <Struct>
      <StringProperty Name="displayName">Your Instagram Profle name</StringProperty>
      <StringProperty Name="connectionId">instagramccbucket</StringProperty>
      <StringProperty Name="contentScope">site</StringProperty>
      <StringProperty Name="type">instagram</StringProperty>
      <StringProperty Name="access_token">your access token</StringProperty>
      <IntegerProperty Name="previewThresholdMB">10</IntegerProperty>
      <BooleanProperty Name="enabled">true</BooleanProperty>
    </Struct>


    In Settings Document "Content Mapping" add the entry:

  <StringProperty Name="Instagram">CMHTML</StringProperty>

    In Settings Document "Content Types" add the entry:

     <Struct>
      <StringProperty Name="name">Instagram</StringProperty>
      <StringProperty Name="connectorType">instagram</StringProperty>
      <StringProperty Name="defaultColumns">type,name,createdTime</StringProperty>
      <LinkProperty xlink:href="" LinkType="coremedia:///cap/contenttype/CMSettings" cmexport:path="/Settings/Options/Settings/Connectors/Connector Item Types" />
      <LinkProperty xlink:href="" LinkType="coremedia:///cap/contenttype/CMSettings" cmexport:path="/Settings/Options/Settings/Connectors/Preview Templates" />
      <LinkProperty xlink:href="" LinkType="coremedia:///cap/contenttype/CMSettings" cmexport:path="/Settings/Options/Settings/Connectors/Content Mapping" />
    </Struct>

*/

@Configuration
@Import({CapRepositoriesConfiguration.class, ConnectorContentConfiguration.class})
public class ConnectorInstagramConfiguration {


  @Bean
  @Scope(SCOPE_PROTOTYPE)
  public ConnectorService instagramConnectorCache() {
    return new InstagramConnectorService();
  }


  @Bean(name = "connector:instagram")
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public ConnectorConnection instagramConnectorConnection(@NonNull @Qualifier("connectorInstagramService") ConnectorService connectorService) {
    ConnectorConnection connectorConnection = new ConnectorConnection();
    connectorConnection.setConnectorService(connectorService);
    return connectorConnection;
  }

  @Bean
  @Scope(BeanDefinition.SCOPE_PROTOTYPE)
  public ConnectorService connectorInstagramService(@NonNull @Qualifier("instagramConnectorCache") ConnectorService connectorService) {
    InstagramConnectorService instagramConnectorService = new InstagramConnectorService();
    /* instagramConnectorService.setFileCache(fileSystemService);*/
    return instagramConnectorService;
  }

  @Bean
  public InstagramWriteInterceptor instagramWriteInterceptor(@NonNull ContentRepository contentRepository,
                                                             @NonNull ContentCreateService contentCreateService) {
    InstagramWriteInterceptor instagramWriteInterceptor = new InstagramWriteInterceptor();
    instagramWriteInterceptor.setPriority(0);
    instagramWriteInterceptor.setType(contentRepository.getContentType("CMHTML"));
    instagramWriteInterceptor.setContentCreateService(contentCreateService);
    return instagramWriteInterceptor;
  }
}
