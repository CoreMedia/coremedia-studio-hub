package com.coremedia.blueprint.connectors.canto.rest;

import com.google.common.base.Stopwatch;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * REST-Connector for Canto Cumulus REST API.
 * This is the "should-be" connector with clientId and clientSecret.
 */
public class CantoConnector {

  private static final Logger LOG = LoggerFactory.getLogger(CantoConnector.class);

  private RestTemplate restTemplate;

  private String basePath = "/CIP";
  private String host;
  private String clientId;
  private String clientSecret;

  public CantoConnector(String host, String clientId, String clientSecret) {
    this.host = host;
    this.clientId = clientId;
    this.clientSecret = clientSecret;

    PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
    poolingHttpClientConnectionManager.setMaxTotal(20);

    RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(6000).setConnectTimeout(6000).setSocketTimeout(6000).build();
    CloseableHttpClient httpClient = HttpClientBuilder.create().setConnectionManager(poolingHttpClientConnectionManager).setDefaultRequestConfig(requestConfig).build();
    HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
    requestFactory.setHttpClient(httpClient);
    restTemplate = new RestTemplate(requestFactory);
  }


  // --- GET ---

  /**
   * Perform 'GET' request.
   *
   * @param resourcePath
   * @param pathParams
   * @param queryParams
   * @param responseType
   * @param <T>
   * @return
   */
  public <T> T performGet(String resourcePath, Map<String, String> pathParams, MultiValueMap<String, String> queryParams, Class<T> responseType) {
    String url = buildRequestUrl(resourcePath, pathParams, queryParams);
    HttpHeaders headers = buildHeaders();

    HttpEntity<String> requestEntity = new HttpEntity<>(headers);
    ResponseEntity<T> responseEntity = null;

    Stopwatch stopwatch = null;
    if (LOG.isInfoEnabled()) {
      stopwatch = Stopwatch.createStarted();
    }

    //ResponseEntity<String> responseEntityDebug = null;
    try {
      //responseEntityDebug = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
      responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, responseType);

      if (LOG.isInfoEnabled() && stopwatch != null && stopwatch.isRunning()) {
        stopwatch.stop();
        LOG.info("GET Request '{}' returned with HTTP status code: {} (took {})", url, responseEntity.getStatusCode().value(), stopwatch);
      }

    } catch (HttpServerErrorException e) {
      LOG.warn("REST call to '{}' failed. Exception:\n{}", url, e.getResponseBodyAsString());

    } finally {
      if (stopwatch != null && stopwatch.isRunning()) {
        try {
          stopwatch.stop();
        } catch (IllegalStateException ex) {
          LOG.warn(ex.getMessage(), ex);
        }
      }
    }

    if (responseEntity != null) {
      return responseEntity.getBody();
    }

    return null;
  }

  public <T> T performGet(String resourcePath, Class<T> responseType) {
    return performGet(resourcePath, null, null, responseType);
  }

  public <T> T performGet(String resourcePath, ParameterizedTypeReference<T> responseType) {
    return performGet(resourcePath, null, null, responseType);
  }

  public <T> T performGet(String resourcePath, Map<String, String> pathParams, MultiValueMap<String, String> queryParams, ParameterizedTypeReference<T> responseType) {
    String url = buildRequestUrl(resourcePath, pathParams, queryParams);
    HttpHeaders headers = buildHeaders();

    HttpEntity<String> requestEntity = new HttpEntity<>(headers);
    ResponseEntity<T> responseEntity = null;

    Stopwatch stopwatch = null;
    if (LOG.isInfoEnabled()) {
      stopwatch = Stopwatch.createStarted();
    }

    try {
      responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, responseType);

      if (LOG.isInfoEnabled() && stopwatch != null && stopwatch.isRunning()) {
        stopwatch.stop();
        LOG.info("GET Request '{}' returned with HTTP status code: {} (took {})", url, responseEntity.getStatusCode().value(), stopwatch);
      }

    } catch (HttpServerErrorException e) {
      LOG.warn("REST call to '{}' failed. Exception:\n{}", url, e.getResponseBodyAsString());

    } finally {
      if (stopwatch != null && stopwatch.isRunning()) {
        try {
          stopwatch.stop();
        } catch (IllegalStateException ex) {
          LOG.warn(ex.getMessage(), ex);
        }
      }
    }

    if (responseEntity != null) {
      return responseEntity.getBody();
    }

    return null;
  }

  // --- POST ---

  /**
   * Perform 'GET' request.
   *
   * @param resourcePath
   * @param pathParams
   * @param queryParams
   * @param responseType
   * @param <T>
   * @return
   */
  public <T> T performPost(String resourcePath, Map<String, String> pathParams, MultiValueMap<String, String> queryParams, Class<T> responseType) {
    String url = buildRequestUrl(resourcePath, pathParams, queryParams);
    HttpHeaders headers = buildHeaders();

    HttpEntity<String> requestEntity = new HttpEntity<>(headers);
    ResponseEntity<T> responseEntity = null;

    Stopwatch stopwatch = null;
    if (LOG.isInfoEnabled()) {
      stopwatch = Stopwatch.createStarted();
    }

    try {
      responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, responseType);

      if (LOG.isInfoEnabled() && stopwatch != null && stopwatch.isRunning()) {
        stopwatch.stop();
        LOG.info("POST Request '{}' returned with HTTP status code: {} (took {})", url, responseEntity.getStatusCode().value(), stopwatch);
      }

    } catch (HttpServerErrorException e) {
      LOG.warn("REST call to '{}' failed. Exception:\n{}", url, e.getResponseBodyAsString());
      throw e;

    } finally {
      if (stopwatch != null && stopwatch.isRunning()) {
        try {
          stopwatch.stop();
        } catch (IllegalStateException ex) {
          LOG.warn(ex.getMessage(), ex);
        }
      }
    }

    return responseEntity.getBody();
  }

  public InputStream streamResource(String resourcePath, Map<String, String> pathParams, MultiValueMap<String, String> queryParams) {
    String url = buildRequestUrl(resourcePath, pathParams, queryParams);
    HttpHeaders headers = buildHeaders();

    HttpEntity<String> requestEntity = new HttpEntity<>(headers);
    ResponseEntity<Resource> responseEntity = null;
    InputStream responseInputStream = null;

    Stopwatch stopwatch = null;
    if (LOG.isInfoEnabled()) {
      stopwatch = Stopwatch.createStarted();
    }

    try {
      responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Resource.class);
      responseInputStream = responseEntity.getBody().getInputStream();

      if (LOG.isInfoEnabled() && stopwatch != null && stopwatch.isRunning()) {
        stopwatch.stop();
        LOG.info("GET Request '{}' returned with HTTP status code: {} (took {})", url, responseEntity.getStatusCode().value(), stopwatch);
      }

    } catch (HttpServerErrorException e) {
      LOG.warn("REST call to '{}' failed. Exception:\n{}", url, e.getResponseBodyAsString());

    } catch (Exception ioe) {
      LOG.error("Error streaming resource '{}'", url, ioe);

    } finally {
      if (stopwatch != null && stopwatch.isRunning()) {
        try {
          stopwatch.stop();
        } catch (IllegalStateException ex) {
          LOG.warn(ex.getMessage(), ex);
        }
      }
    }

    return responseInputStream;
  }

  public int uploadResource(@Nonnull String resourcePath, Map<String, String> pathParams, MultiValueMap<String, String> queryParams, @Nonnull String fileName, @Nullable Map<String, Object> fieldData, @Nonnull InputStream inputStream) {
    String url = buildRequestUrl(resourcePath, pathParams, queryParams);
    HttpHeaders headers = buildHeaders();

    MultiValueMap<String, Object> payload = new LinkedMultiValueMap<>();
    payload.add("file", new MultipartFileResource(inputStream, fileName));
    if (fieldData != null) {
      payload.add("fields", fieldData);
    }

    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(payload, headers);

    ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<Map<String, Object>>() {
    });

    int createdId = -1;
    Map<String, Object> returnData = response.getBody();
    if (response.getStatusCode() == HttpStatus.OK && returnData != null && returnData.containsKey("id")) {
      createdId = (Integer) returnData.get("id");
    }

    return createdId;
  }


  // --- private ---

  private HttpHeaders buildHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.ACCEPT, APPLICATION_JSON);
    headers.set(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes()));
    return headers;
  }

  private String buildRequestUrl(String resourcePath, Map<String, String> pathParams, MultiValueMap<String, String> queryParams) {
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
            .scheme("https")
            .host(host)
            .path(basePath);

    // add resource path
    uriBuilder.path(resourcePath);

    // Add query params
    if (queryParams != null) {
      uriBuilder.queryParams(queryParams);
    }

    UriComponents uriComponents = pathParams != null ? uriBuilder.buildAndExpand(pathParams) : uriBuilder.build();
    return uriComponents.toString();
  }

  private class MultipartFileResource extends InputStreamResource {

    private String filename;

    public MultipartFileResource(InputStream inputStream, String filename) {
      super(inputStream);
      this.filename = filename;
    }

    @Override
    public String getFilename() {
      return this.filename;
    }

    @Override
    public long contentLength() throws IOException {
      return -1; // we do not want to generally read the whole stream into memory ...
    }
  }

}
