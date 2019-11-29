package com.coremedia.blueprint.connectors.celum.rest;

import com.google.common.base.Stopwatch;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.InputStream;
import java.util.Collections;

/**
 * REST-Connector for Celum REST API.
 */
public class CelumRestConnector {
  private static final Logger LOG = LoggerFactory.getLogger(CelumRestConnector.class);

  private static final String AUTHORIZATION_HEADER = "Authorization";

  private static final String PROTOCOL = "https";
  private static final String BASE_PATH = "cora/";

  private String authToken;
  private String host;

  private RestTemplate restTemplate;

  public CelumRestConnector(String host) {
    this.host = host;
    PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
    poolingHttpClientConnectionManager.setMaxTotal(20);

    RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(6000).setConnectTimeout(6000).setSocketTimeout(6000).build();
    CloseableHttpClient httpClient = HttpClientBuilder.create().setConnectionManager(poolingHttpClientConnectionManager).setDefaultRequestConfig(requestConfig).build();
    HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
    requestFactory.setHttpClient(httpClient);

    restTemplate = new RestTemplate(requestFactory);
  }

  public void setAuthToken(String token) {
    this.authToken = token;
  }


  public InputStream stream(String suffix) {
    String url = PROTOCOL + "://" + host + "/" + BASE_PATH + suffix;
    return streamUrl(url);
  }

  public InputStream streamUrl(String url) {
    try {
      HttpGet httpGet = new HttpGet(url);
      httpGet.setHeader(AUTHORIZATION_HEADER, "celumApiKey " + authToken);
      HttpClient client = HttpClientBuilder.create().build();
      HttpResponse response = client.execute(httpGet);
      org.apache.http.HttpEntity ent = response.getEntity();
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode > 200) {
        String result = IOUtils.toString(ent.getContent(), "utf8");
        LOG.error("Error getting Celum resource: " + result);
      }
      else {
        return ent.getContent();
      }
    } catch (Exception e) {
      LOG.error("Couldn't connect to resource " + url + ": " + e.getMessage(), e);
    }
    return null;
  }

  public <T> T performGet(String resourcePath, Class<T> responseType, MultiValueMap<String, String> queryParams) {
    String url = null;
    Stopwatch stopwatch = null;
    try {
      UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance()
              .scheme(PROTOCOL)
              .host(host)
              .path(BASE_PATH);

      if (queryParams != null) {
        uriComponentsBuilder.queryParams(queryParams);
      }

      // Add resource path
      uriComponentsBuilder.path(resourcePath);

      UriComponents uriComponents = uriComponentsBuilder.build();
      url = uriComponents.toString();

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
      if (StringUtils.isNotBlank(authToken)) {
        headers.set(AUTHORIZATION_HEADER, "celumApiKey " + authToken);
      }
      HttpEntity<String> requestEntity = new HttpEntity<>(headers);

      if (LOG.isDebugEnabled()) {
        stopwatch = Stopwatch.createStarted();
      }
      ResponseEntity<T> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, responseType);
      if (LOG.isDebugEnabled() && stopwatch != null && stopwatch.isRunning()) {
        stopwatch.stop();
        LOG.debug("GET Request '{}' returned with HTTP status code: {} (took {})", url, responseEntity.getStatusCode().value(), stopwatch);
      }

      return responseEntity.getBody();
    } catch (HttpClientErrorException hce) {
      HttpStatus statusCode = hce.getStatusCode();
      if (statusCode.is4xxClientError()) {
        LOG.warn("4xx executing celum request' " + url + "': " + hce.getMessage());
      }
      else {
        LOG.error("Failed to execute celum request' " + url + "': " + hce.getMessage());
      }
    } catch (Exception e) {
      LOG.error("Failed to execute celum request' " + url + "': " + e.getMessage());
    } finally {
      if (stopwatch != null && stopwatch.isRunning()) {
        try {
          stopwatch.stop();
        } catch (IllegalStateException ex) {
          LOG.warn(ex.getMessage(), ex);
        }
      }
    }
    return null;
  }
}
