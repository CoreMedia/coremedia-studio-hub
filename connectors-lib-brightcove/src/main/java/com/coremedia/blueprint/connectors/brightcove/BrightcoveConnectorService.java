package com.coremedia.blueprint.connectors.brightcove;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorEntity;
import com.coremedia.blueprint.connectors.api.ConnectorException;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorItem;
import com.coremedia.blueprint.connectors.api.ConnectorService;
import com.coremedia.blueprint.connectors.api.invalidation.InvalidationResult;
import com.coremedia.blueprint.connectors.api.search.ConnectorSearchResult;
import com.google.api.client.auth.oauth2.ClientCredentialsTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class BrightcoveConnectorService implements ConnectorService {
  private static final Logger LOG = LoggerFactory.getLogger(BrightcoveConnectorService.class);

  private ConnectorContext connectorContext;
  private BrightcoveCategory rootCategory;
  private TokenResponse tokenResponse;
  private LocalTime expiryDate;

  private String CLIENT_ACCOUNT_ID = "";
  private String CLIENT_ID = "";
  private String CLIENT_SECRET = "";

  @Override
  public boolean init(@NonNull ConnectorContext context) throws ConnectorException {
    this.connectorContext = context;
    CLIENT_ID = context.getProperty("client_id");
    CLIENT_SECRET = context.getProperty("client_secret");
    CLIENT_ACCOUNT_ID = context.getProperty("client_account_id");

    if (CLIENT_ID == null || CLIENT_ID.trim().length() == 0 || CLIENT_SECRET == null || CLIENT_SECRET.trim().length() == 0 || CLIENT_ACCOUNT_ID == null || CLIENT_ACCOUNT_ID.trim().length() == 0) {
      throw new ConnectorException("No credentials configured for Brightcove connection " + context.getConnectionId());
    }

    // Get the tokenResponse (this needs to be made more robust subsequently
    tokenResponse = getTokenResponse();

    return (tokenResponse != null);
  }

  @Override
  public void shutdown(@NonNull ConnectorContext context) throws ConnectorException {

  }

  @Nullable
  @Override
  public ConnectorItem getItem(@NonNull ConnectorContext context, @NonNull ConnectorId id) throws ConnectorException {
    BrightcoveItem brightcoveItem = null;

    HttpResponse response = doCall("https://cms.api.brightcove.com/v1/accounts/" + CLIENT_ACCOUNT_ID + "/videos/" + id.getExternalId());

    try {
      String stringResponse = EntityUtils.toString(response.getEntity());  // now you have the response as String, which you can convert to a JSONObject or do other stuff
      JSONObject jsonObject = new JSONObject(stringResponse);
      brightcoveItem = transformJsonObject(jsonObject, rootCategory);
      // Add description
      if (jsonObject.has("description") && !jsonObject.get("description").equals(null)) {
        brightcoveItem.setDescription(jsonObject.getString("description"));
      }
      // Add thumbnail
      JSONObject images = jsonObject.getJSONObject("images");
      if (images != null && images.has("thumbnail") && images.get("thumbnail") != null) {
        JSONObject thumbnail = images.getJSONObject("thumbnail");
        if (thumbnail != null && thumbnail.has("src") && thumbnail.get("src") != null) {
          brightcoveItem.setThumbnailUrl(thumbnail.getString("src"));
        }
      }
    } catch (IOException e) {
      // handle exception
    }
    return brightcoveItem;
  }

  @Nullable
  @Override
  public ConnectorCategory getCategory(@NonNull ConnectorContext context, @NonNull ConnectorId id) throws ConnectorException {
    return null;
  }

  @NonNull
  @Override
  public ConnectorCategory getRootCategory(@NonNull ConnectorContext context) throws ConnectorException {
    if (rootCategory == null) {
      String name = this.connectorContext.getProperty("displayName");

      ConnectorId id = ConnectorId.createRootId(context.getConnectionId());
      // calculate categories
      List<ConnectorCategory> childCategories = Collections.emptyList();
      rootCategory = new BrightcoveCategory(id, context, name, null, childCategories, null);
      // calculate children
      List<ConnectorItem> childItems = new ArrayList<>();

      // get child items
      HttpResponse response = doCall("https://cms.api.brightcove.com/v1/accounts/" + CLIENT_ACCOUNT_ID + "/videos");
      try {
        String stringResponse = EntityUtils.toString(response.getEntity());
        JSONArray jsonArray = new JSONArray(stringResponse);
        for (int i = 0; i < jsonArray.length(); i++) {
          childItems.add(transformJsonObject(jsonArray.getJSONObject(i), rootCategory));
        }
      } catch (IOException e) {
        // handle exception
      }
      rootCategory.setItems(childItems);
    }
    return rootCategory;
  }


  private HttpResponse doCall(String url) {
    if (expiryDate == null || tokenResponse == null || LocalTime.now().isAfter(expiryDate)) {
//      create new token
      tokenResponse = getTokenResponse();
    } else {
      if (tokenResponse != null) {
        try {
          HttpClient httpClient = HttpClientBuilder.create().build();
          HttpGet httpGet = new HttpGet(url);   // the http GET request
          httpGet.addHeader("Authorization", "Bearer " + tokenResponse.getAccessToken());
          HttpResponse response = httpClient.execute(httpGet); // the client executes the request and gets a response
          int responseCode = response.getStatusLine().getStatusCode();  // check the response code
          switch (responseCode) {
            case 200: {
              return response;
            }
            case 500: {
              // server problems ?
              break;
            }
            case 403: {
              // you have no authorization to access that resource
              break;
            }
          }
        } catch (IOException | ParseException ex) {
          // handle exception
        }
      }
    }
    return null;
  }

  /*
     transform JSONObject to BrightcoveItem
   */
  private BrightcoveItem transformJsonObject(JSONObject jsonObject, ConnectorCategory connectorCategory) {
    ConnectorId id = ConnectorId.createItemId(connectorContext.getConnectionId(), jsonObject.getString("id"));
    String name = jsonObject.getString("name");
    return new BrightcoveItem(id, connectorContext, name, connectorCategory);
  }

  /*
  Helper method to get the token response and/or return the existing one.
   */
  private TokenResponse getTokenResponse() {
    // Get the tokenResponse (this needs to be made more robust subsequently
    try {
      TokenResponse tokenResponse = new ClientCredentialsTokenRequest(
              new NetHttpTransport(),
              new JacksonFactory(),
              new GenericUrl("https://oauth.brightcove.com/v4/access_token"))
              .setClientAuthentication(new BasicAuthentication(CLIENT_ID, CLIENT_SECRET))
              .execute();
      // Connection established calculate new expiry date
      expiryDate = LocalTime.now().plusSeconds(tokenResponse.getExpiresInSeconds());
      return tokenResponse;
    } catch (TokenResponseException e) {
      if (e.getDetails() != null) {
        System.err.println("Error: " + e.getDetails().getError());
        if (e.getDetails().getErrorDescription() != null) {
          System.err.println(e.getDetails().getErrorDescription());
        }
        if (e.getDetails().getErrorUri() != null) {
          System.err.println(e.getDetails().getErrorUri());
        }
      } else {
        System.err.println(e.getMessage());
      }
    } catch (IOException e) {
      System.err.println(e);
    }
    return null;
  }


  @Override
  public InvalidationResult invalidate(@NonNull ConnectorContext context) {
    return null;
  }

  @NonNull
  @Override
  public ConnectorSearchResult<ConnectorEntity> search(@NonNull ConnectorContext context, ConnectorCategory
          category, String query, String searchType, Map<String, String> params) {
    return null;
  }

  public boolean refresh(@NonNull ConnectorContext context, @NonNull ConnectorCategory category) {
    return false;
  }

  @Nullable
  public ConnectorItem upload(@NonNull ConnectorContext context, ConnectorCategory category, String
          itemName, InputStream inputStream) {
    return null;
  }
}
