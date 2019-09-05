package com.coremedia.blueprint.connectors.shutterstock;

import com.google.common.base.Stopwatch;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.InputStream;
import java.util.Base64;
import java.util.Map;

/**
 *
 */
public class ShutterstockConnector {

  private static final Logger LOG = LoggerFactory.getLogger(ShutterstockConnector.class);

  private RestTemplate restTemplate;

  private final static String HOST = "api.shutterstock.com";
  private final static String BASE_URL = "v2";
  private String clientId;
  private String clientSecret;

  public ShutterstockConnector(String clientId, String clientSecret) {
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
    if (LOG.isDebugEnabled()) {
      stopwatch = Stopwatch.createStarted();
    }

    //ResponseEntity<String> responseEntityDebug = null;
    try {
      //responseEntityDebug = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
      responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, responseType);

      if (LOG.isDebugEnabled() && stopwatch != null && stopwatch.isRunning()) {
        stopwatch.stop();
        LOG.debug("GET Request '{}' returned with HTTP status code: {} (took {})", url, responseEntity.getStatusCode().value(), stopwatch);
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
    if (LOG.isDebugEnabled()) {
      stopwatch = Stopwatch.createStarted();
    }

    try {
      System.out.println(url);
      responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, responseType);

      if (LOG.isDebugEnabled() && stopwatch != null && stopwatch.isRunning()) {
        stopwatch.stop();
        LOG.debug("GET Request '{}' returned with HTTP status code: {} (took {})", url, responseEntity.getStatusCode().value(), stopwatch);
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
    if (LOG.isDebugEnabled()) {
      stopwatch = Stopwatch.createStarted();
    }

    try {
      responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, responseType);

      if (LOG.isDebugEnabled() && stopwatch != null && stopwatch.isRunning()) {
        stopwatch.stop();
        LOG.debug("POST Request '{}' returned with HTTP status code: {} (took {})", url, responseEntity.getStatusCode().value(), stopwatch);
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

    if (responseEntity != null) {
      return responseEntity.getBody();
    }

    return null;
  }

  public InputStream streamResource(String resourcePath, Map<String, String> pathParams, MultiValueMap<String, String> queryParams) {
    String url = buildRequestUrl(resourcePath, pathParams, queryParams);
    HttpHeaders headers = buildHeaders();

    Stopwatch stopwatch = null;
    if (LOG.isDebugEnabled()) {
      stopwatch = Stopwatch.createStarted();
    }

    try {
      ResponseEntity<Resource> responseEntity = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(headers), Resource.class);
      Resource body = responseEntity.getBody();
      if (body == null) {
        LOG.error("Empty resource body, streaming of " + this + " failed.");
        return null;
      }

      InputStream responseInputStream = responseEntity.getBody().getInputStream();
      if (LOG.isDebugEnabled() && stopwatch != null && stopwatch.isRunning()) {
        stopwatch.stop();
        LOG.debug("GET Request '{}' returned with HTTP status code: {} (took {})", url, responseEntity.getStatusCode().value(), stopwatch);
      }
      return responseInputStream;
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

    return null;
  }


  // --- private ---

  private HttpHeaders buildHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.ACCEPT, "application/json");
    headers.set(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes()));
    return headers;
  }

  private String buildRequestUrl(String resourcePath, Map<String, String> pathParams, MultiValueMap<String, String> queryParams) {
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.newInstance()
            .scheme("https")
            .host(HOST)
            .path(BASE_URL);

    // add resource path
    uriBuilder.path(resourcePath);

    // Add query params
    if (queryParams != null) {
      uriBuilder.queryParams(queryParams);
    }

    UriComponents uriComponents = pathParams != null ? uriBuilder.buildAndExpand(pathParams) : uriBuilder.build();
    return uriComponents.toString();
  }
}
