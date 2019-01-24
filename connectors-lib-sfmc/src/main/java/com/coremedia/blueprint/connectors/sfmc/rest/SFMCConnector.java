package com.coremedia.blueprint.connectors.sfmc.rest;

import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.google.common.base.Stopwatch;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

import static com.google.common.base.Strings.nullToEmpty;

/**
 *
 */
public class SFMCConnector {
  private static final Logger LOG = LoggerFactory.getLogger(SFMCConnector.class);
  public static final String CLIENT_ID = "clientId";
  public static final String CLIENT_SECRED = "clientSecret";
  public static final String SUBDOMAIN = "subdomain";

  private static final String AUTHORIZATION_HEADER = "Authorization";

  private RestTemplate restTemplate;
  private AccessToken accessToken;

  public SFMCConnector() {
    restTemplate = new RestTemplate(new org.springframework.http.client.SimpleClientHttpRequestFactory());
  }

  @NonNull
  public <T> Optional<T> getResource(@NonNull String url,
                                     @NonNull Class<T> responseType,
                                     @NonNull ConnectorContext context) {
    String fullUrl = resolveUrl(context, url);
    HttpEntity<String> httpEntity = buildRequestEntity(context, null);
    return performRequest(HttpMethod.GET, fullUrl, httpEntity, responseType);
  }

  @NonNull
  public String resolveUrl(@NonNull ConnectorContext context, @NonNull String segment) {
    return "https://" + context.getProperty(SUBDOMAIN) + segment;
  }

  public InputStream streamUrl(String url) {
    try {
      HttpGet httpGet = new HttpGet(url);
      httpGet.setHeader(AUTHORIZATION_HEADER, "Bearer " + accessToken.getAccessToken());
      HttpClient client = HttpClientBuilder.create().build();
      HttpResponse response = client.execute(httpGet);
      org.apache.http.HttpEntity ent = response.getEntity();
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode > 200) {
        String result = IOUtils.toString(ent.getContent(), "utf8");
        LOG.error("Error getting SFMC resource '" + url + "': " + result);
      }
      else {
        return ent.getContent();
      }
    } catch (Exception e) {
      LOG.error("Couldn't connect to resource " + url + ": " + e.getMessage(), e);
    }
    return null;
  }

  @NonNull
  public <T> Optional<T> postResource(@NonNull String url,
                                      @Nullable String payload,
                                      @NonNull Class<T> responseType,
                                      @NonNull ConnectorContext context) {
    String domainUrl = "https://" + context.getProperty(SUBDOMAIN) + url;
    HttpEntity<String> httpEntity = buildRequestEntity(context, payload);
    return performRequest(HttpMethod.POST, domainUrl, httpEntity, responseType);
  }

  @NonNull
  private <T> Optional<T> performRequest(@NonNull HttpMethod httpMethod,
                                         @NonNull String url,
                                         @NonNull HttpEntity<String> requestEntity,
                                         @NonNull Class<T> responseType) {

    Optional<ResponseEntity<T>> responseEntityOptional = makeExchange(url, httpMethod, requestEntity, responseType);

    if (!responseEntityOptional.isPresent()) {
      return Optional.empty();
    }
    ResponseEntity<T> responseEntity = responseEntityOptional.get();
    T responseBody = responseEntity.getBody();
    return Optional.ofNullable(responseBody);
  }

  @NonNull
  private HttpEntity<String> buildRequestEntity(@NonNull ConnectorContext context, @Nullable String payload) {
    if (accessToken == null || !accessToken.isValid()) {
      accessToken = authenticate(context);
    }

    if (payload != null) {
      return new HttpEntity<>(payload, buildHttpHeaders(MediaType.APPLICATION_JSON));
    }
    return new HttpEntity<>(buildHttpHeaders(MediaType.APPLICATION_JSON));
  }

  @NonNull
  private HttpEntity<String> buildRequestEntity(MediaType mediaType, @Nullable String body, @NonNull ConnectorContext context) {
    return new HttpEntity<>(nullToEmpty(body), buildHttpHeaders(mediaType));
  }

  @NonNull
  private HttpHeaders buildHttpHeaders(@NonNull MediaType mediaType) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(mediaType);
    headers.set("Authorization", "Bearer " + accessToken.getAccessToken());
    return headers;
  }

  @NonNull
  private AccessToken authenticate(@NonNull ConnectorContext connectorContext) {
    String url = "https://" + connectorContext.getProperty(SUBDOMAIN) + ".auth.marketingcloudapis.com/v1/requestToken";
    String clientId = connectorContext.getProperty(CLIENT_ID);
    String clientSecret = connectorContext.getProperty(CLIENT_SECRED);

    String json = "{\n" +
            "    \"clientId\": \"" + clientId + "\",\n" +
            "    \"clientSecret\": \"" + clientSecret + "\"\n" +
            "}";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> httpEntity = new HttpEntity<>(json, headers);
    Optional<ResponseEntity<AccessToken>> accessToken = makeExchange(url, HttpMethod.POST, httpEntity, AccessToken.class);
    ResponseEntity<AccessToken> accessTokenResponseEntity = accessToken.get();
    return accessTokenResponseEntity.getBody();
  }


  @NonNull
  private <T> Optional<ResponseEntity<T>> makeExchange(@NonNull String url,
                                                       @NonNull HttpMethod httpMethod,
                                                       @NonNull HttpEntity<String> requestEntity,
                                                       @NonNull Class<T> responseType) {
    LOG.info(url);
    boolean logTime = LOG.isInfoEnabled();
    Stopwatch stopwatch = null;
    try {
      if (logTime) {
        stopwatch = Stopwatch.createStarted();
      }

      // For "Evaluate Expression" debugging: `restTemplate.exchange(url, httpMethod, requestEntity, String.class)`
      ResponseEntity<T> responseEntity = restTemplate.exchange(URI.create(url), httpMethod, requestEntity, responseType);

      if (logTime && stopwatch.isRunning()) {
        stopwatch.stop();
        LOG.trace("{} Request '{}' returned with HTTP status code: {} (took {})", httpMethod, url,
                responseEntity.getStatusCode().value(), stopwatch);
        LOG.info(stopwatch.toString());
      }
      return Optional.of(responseEntity);
    } catch (HttpClientErrorException ex) {
      HttpStatus statusCode = ex.getStatusCode();
      if (statusCode == HttpStatus.NOT_FOUND) {
        LOG.trace("Result from '{}' (response code: {}) will be interpreted as 'no result found'.", url, statusCode);
        return Optional.empty();
      }

      if (statusCode == HttpStatus.FORBIDDEN) {
        LOG.warn("Forbidden, not allowed to make this request to URL " + url + " with request entity " + requestEntity, ex);
        return Optional.empty();
      }

      LOG.warn("REST call to '{}' failed. Exception:\n{}", url, ex.getMessage());
      throw new UnsupportedOperationException(
              String.format("REST call to '%s' failed. Exception: %s", url, ex.getMessage()), ex);
    } finally {
      if (stopwatch != null && stopwatch.isRunning()) {
        try {
          stopwatch.stop();
        } catch (IllegalStateException ex) {
          LOG.warn(ex.getMessage(), ex);
        }
      }
    }
  }
}
