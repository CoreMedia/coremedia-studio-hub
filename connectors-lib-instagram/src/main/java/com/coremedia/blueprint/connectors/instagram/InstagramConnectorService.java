package com.coremedia.blueprint.connectors.instagram;

import com.coremedia.connectors.api.ConnectorCategory;
import com.coremedia.connectors.api.ConnectorContext;
import com.coremedia.connectors.api.ConnectorEntity;
import com.coremedia.connectors.api.ConnectorException;
import com.coremedia.connectors.api.ConnectorId;
import com.coremedia.connectors.api.ConnectorItem;
import com.coremedia.connectors.api.ConnectorService;
import com.coremedia.connectors.api.search.ConnectorSearchResult;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
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
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 *
 */
public class InstagramConnectorService implements ConnectorService {
  private static final Logger LOG = LoggerFactory.getLogger(InstagramConnectorService.class);

  private ConnectorContext connectorContext;
  private InstagramCategory rootCategory;

  //get an accesstoken https://api.instagram.com/oauth/authorize/?client_id=c0b6b1b8ce88454eac629b42d3f1a970f&redirect_uri=http://127.0.0.1:8887&response_type=token
  private String accessToken = "";

  private final DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

  @Override
  public boolean init(@NonNull ConnectorContext context) throws ConnectorException {
    this.connectorContext = context;
    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("PST"));
    accessToken = context.getProperty("access_token");

    if (accessToken == null || accessToken.trim().length() == 0) {
      throw new ConnectorException("No credentials configured for Instagram connection " + context.getConnectionId());
    }

    return (accessToken != null);
  }

  @Override
  public void shutdown(@NonNull ConnectorContext context) throws ConnectorException {

  }

  @Nullable
  @Override
  public ConnectorItem getItem(@NonNull ConnectorContext context, @NonNull ConnectorId id) throws ConnectorException {
    InstagramItem instagramItem = null;

    //since there is no "Get Item By ID" functionality in Instagram, we have to get find it in our own repository"
    for (ConnectorItem item : rootCategory.getItems()) {
      InstagramItem instagramPost = (InstagramItem) item;
      if (id.equals(instagramPost.getConnectorId())) {
        instagramItem = instagramPost;
      }
    }
    // get comments
    HttpResponse response = doCall("https://api.instagram.com/v1/media/" + id.getExternalId() + "/comments?access_token=" + accessToken);
    return instagramItem;
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
      rootCategory = new InstagramCategory(id, context, name, null, childCategories, null);
      // calculate children
      List<ConnectorItem> childItems = new ArrayList<>();

      // get posts of account
      HttpResponse response = doCall("https://api.instagram.com/v1/users/self/media/recent/?access_token=" + accessToken);
      try {
        String stringResponse = EntityUtils.toString(response.getEntity());
        JSONObject responseObject = new JSONObject(stringResponse);
        JSONArray dataArray = responseObject.getJSONArray("data");
        for (int i = 0; i < dataArray.length(); i++) {
          childItems.add(transformJsonObject(dataArray.getJSONObject(i), rootCategory));
        }
      } catch (IOException e) {
        // handle exception
        LOG.error("Error while parsing Instagram data", e);
        //set root Category to null, so it can be requested the next studio call
        rootCategory = null;
        return rootCategory;
      }
      rootCategory.setItems(childItems);
    }
    return rootCategory;
  }


  private HttpResponse doCall(String url) {
    if (accessToken != null) {
      try {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(url);   // the http GET request
        // httpGet.addHeader("Authorization", "Bearer " + accessToken);
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
        LOG.error("Error while requesting Instragram post", ex);
      }
    }
    return null;
  }

  /*
     transform JSONObject to InstagramItem
   */
  private InstagramItem transformJsonObject(JSONObject jsonObject, ConnectorCategory connectorCategory) {
    ConnectorId id = ConnectorId.createItemId(connectorContext.getConnectionId(), jsonObject.getString("id"));
    JSONObject imagesObject = jsonObject.getJSONObject("images");
    JSONObject thumbnailObject = imagesObject.getJSONObject("thumbnail");
    JSONObject lowResObject = imagesObject.getJSONObject("low_resolution");
    JSONObject captionObject = jsonObject.getJSONObject("caption");
    JSONObject fromObject = captionObject.getJSONObject("from");
    String fullName = fromObject.getString("full_name");
    String username = fromObject.getString("username");
    String utc = jsonObject.getString("created_time") + "000";
    Timestamp ts = new Timestamp(Long.parseLong(utc));
    Date createdAt = new Date(ts.getTime());

    String link = jsonObject.getString("link");
    String text = captionObject.getString("text");

    String name = StringUtils.abbreviate(text, 40);
    InstagramItem item = new InstagramItem(id, connectorContext, name, connectorCategory);
    item.setDescription(text);
    item.setThumbnailUrl(thumbnailObject.getString("url"));
    item.setLink(link);
    item.setEmbbedCode(generateEmbbedCode(jsonObject, fullName, username, link, text, convertTimeStamp(createdAt)));
    item.setLowResolutionUrl(lowResObject.getString("url"));
    item.setCreatedTime(createdAt);
    return item;
  }

  private String convertTimeStamp(Date createdAt) {
    return simpleDateFormat.format(createdAt);
  }


  private String generateEmbbedCode(JSONObject jsonObject, String fullName, String userName, String link, String description, String utc) {
    String code = "<div><blockquote class=\"instagram-media\" data-instgrm-captioned=\"true\" data-instgrm-permalink=\"" + link +
            "?utm_source=ig_embed&amp;utm_medium=loading\" data-instgrm-version=\"12\" style=\" background:#FFF; border:0; border-radius:3px; box-shadow:0 0 1px 0 rgba(0,0,0,0.5),0 1px 10px 0 rgba(0,0,0,0.15); margin: 1px; max-width:540px; min-width:326px; padding:0; width:99.375%; width:-webkit-calc(100% - 2px); width:calc(100% - 2px);\"><div style=\"padding:16px;\"> <a href=\"" + link +
            "?utm_source=ig_embed&amp;utm_medium=loading\" style=\" background:#FFFFFF; line-height:0; padding:0 0; text-align:center; text-decoration:none; width:100%;\" target=\"_blank\"> <div style=\" display: flex; flex-direction: row; align-items: center;\"> <div style=\"background-color: #F4F4F4; border-radius: 50%; flex-grow: 0; height: 40px; margin-right: 14px; width: 40px;\"></div>" +
            " <div style=\"display: flex; flex-direction: column; flex-grow: 1; justify-content: center;\"> <div style=\" background-color: #F4F4F4; border-radius: 4px; flex-grow: 0; height: 14px; margin-bottom: 6px; width: 100px;\"></div> <div style=\" background-color: #F4F4F4; border-radius: 4px; flex-grow: 0; height: 14px; width: 60px;\"></div></div></div><div style=\"padding: 19% 0;\"></div>" +
            "<div style=\"display:block; height:50px; margin:0 auto 12px; width:50px;\"><svg width=\"50px\" height=\"50px\" viewBox=\"0 0 60 60\" version=\"1.1\" xmlns=\"https://www.w3.org/2000/svg\" xmlns:xlink=\"https://www.w3.org/1999/xlink\"><g stroke=\"none\" stroke-width=\"1\" fill=\"none\" fill-rule=\"evenodd\"><g transform=\"translate(-511.000000, -20.000000)\" fill=\"#000000\"><g>" +
            "<path d=\"M556.869,30.41 C554.814,30.41 553.148,32.076 553.148,34.131 C553.148,36.186 554.814,37.852 556.869,37.852 C558.924,37.852 560.59,36.186 560.59,34.131 C560.59,32.076 558.924,30.41 556.869,30.41 M541,60.657 C535.114,60.657 530.342,55.887 530.342,50 C530.342,44.114 535.114,39.342 541,39.342 C546.887,39.342 551.658,44.114 551.658,50 C551.658,55.887 546.887,60.657 541,60.657 M541,33.886 C532.1,33.886 524.886,41.1 524.886,50 C524.886,58.899 532.1,66.113 541,66.113 C549.9,66.113 557.115,58.899 557.115,50 C557.115,41.1 549.9,33.886 541,33.886 M565.378,62.101 C565.244,65.022 564.756,66.606 564.346,67.663 C563.803,69.06 563.154,70.057 562.106,71.106 C561.058,72.155 560.06,72.803 558.662,73.347 C557.607,73.757 556.021,74.244 553.102,74.378 C549.944,74.521 548.997,74.552 541,74.552 C533.003,74.552 532.056,74.521 528.898,74.378 C525.979,74.244 524.393,73.757 523.338,73.347 C521.94,72.803 520.942,72.155 519.894,71.106 C518.846,70.057 518.197,69.06 517.654,67.663 C517.244,66.606 516.755,65.022 516.623,62.101 C516.479,58.943 516.448,57.996 516.448,50 C516.448,42.003 516.479,41.056 516.623,37.899 C516.755,34.978 517.244,33.391 517.654,32.338 C518.197,30.938 518.846,29.942 519.894,28.894 C520.942,27.846 521.94,27.196 523.338,26.654 C524.393,26.244 525.979,25.756 528.898,25.623 C532.057,25.479 533.004,25.448 541,25.448 C548.997,25.448 549.943,25.479 553.102,25.623 C556.021,25.756 557.607,26.244 558.662,26.654 C560.06,27.196 561.058,27.846 562.106,28.894 C563.154,29.942 563.803,30.938 564.346,32.338 C564.756,33.391 565.244,34.978 565.378,37.899 C565.522,41.056 565.552,42.003 565.552,50 C565.552,57.996 565.522,58.943 565.378,62.101 M570.82,37.631 C570.674,34.438 570.167,32.258 569.425,30.349 C568.659,28.377 567.633,26.702 565.965,25.035 C564.297,23.368 562.623,22.342 560.652,21.575 C558.743,20.834 556.562,20.326 553.369,20.18 C550.169,20.033 549.148,20 541,20 C532.853,20 531.831,20.033 528.631,20.18 C525.438,20.326 523.257,20.834 521.349,21.575 C519.376,22.342 517.703,23.368 516.035,25.035 C514.368,26.702 513.342,28.377 512.574,30.349 C511.834,32.258 511.326,34.438 511.181,37.631 C511.035,40.831 511,41.851 511,50 C511,58.147 511.035,59.17 511.181,62.369 C511.326,65.562 511.834,67.743 512.574,69.651 C513.342,71.625 514.368,73.296 516.035,74.965 C517.703,76.634 519.376,77.658 521.349,78.425 C523.257,79.167 525.438,79.673 528.631,79.82 C531.831,79.965 532.853,80.001 541,80.001 C549.148,80.001 550.169,79.965 553.369,79.82 C556.562,79.673 558.743,79.167 560.652,78.425 C562.623,77.658 564.297,76.634 565.965,74.965 C567.633,73.296 568.659,71.625 569.425,69.651 C570.167,67.743 570.674,65.562 570.82,62.369 C570.966,59.17 571,58.147 571,50 C571,41.851 570.966,40.831 570.82,37.631\"></path></g></g></g></svg></div><div style=\"padding-top: 8px;\">" +
            " <div style=\" color:#3897f0; font-family:Arial,sans-serif; font-size:14px; font-style:normal; font-weight:550; line-height:18px;\"> View this post on Instagram</div></div><div style=\"padding: 12.5% 0;\"></div> <div style=\"display: flex; flex-direction: row; margin-bottom: 14px; align-items: center;\"><div> <div style=\"background-color: #F4F4F4; border-radius: 50%; height: 12.5px; width: 12.5px; transform: translateX(0px) translateY(7px);\"></div>" +
            " <div style=\"background-color: #F4F4F4; height: 12.5px; transform: rotate(-45deg) translateX(3px) translateY(1px); width: 12.5px; flex-grow: 0; margin-right: 14px; margin-left: 2px;\"></div> <div style=\"background-color: #F4F4F4; border-radius: 50%; height: 12.5px; width: 12.5px; transform: translateX(9px) translateY(-18px);\"></div></div><div style=\"margin-left: 8px;\">" +
            " <div style=\" background-color: #F4F4F4; border-radius: 50%; flex-grow: 0; height: 20px; width: 20px;\"></div> <div style=\" width: 0; height: 0; border-top: 2px solid transparent; border-left: 6px solid #f4f4f4; border-bottom: 2px solid transparent; transform: translateX(16px) translateY(-4px) rotate(30deg)\"></div></div><div style=\"margin-left: auto;\">" +
            " <div style=\" width: 0px; border-top: 8px solid #F4F4F4; border-right: 8px solid transparent; transform: translateY(16px);\"></div> <div style=\" background-color: #F4F4F4; flex-grow: 0; height: 12px; width: 16px; transform: translateY(-4px);\"></div> <div style=\" width: 0; height: 0; border-top: 8px solid #F4F4F4; border-left: 8px solid transparent; transform: translateY(-4px) translateX(8px);\"></div>" +
            "</div></div></a> <p style=\" margin:8px 0 0 0; padding:0 4px;\"> <a href=\"" + link + "?utm_source=ig_embed&amp;utm_medium=loading\" style=\" color:#000; font-family:Arial,sans-serif; font-size:14px; font-style:normal; font-weight:normal; line-height:17px; text-decoration:none; word-wrap:break-word;\" target=\"_blank\">" + description + "</a></p>" +
            " <p style=\" color:#c9c8cd; font-family:Arial,sans-serif; font-size:14px; line-height:17px; margin-bottom:0; margin-top:8px; overflow:hidden; padding:8px 0 7px; text-align:center; text-overflow:ellipsis; white-space:nowrap;\">A post shared by <a href=\"https://www.instagram.com/" + userName + "/?utm_source=ig_embed&amp;utm_medium=loading\" style=\" color:#c9c8cd; font-family:Arial,sans-serif; font-size:14px; font-style:normal; font-weight:normal; line-height:17px;\" target=\"_blank\"> " + fullName +
            "</a> (@" + userName + ") on <time style=\" font-family:Arial,sans-serif; font-size:14px; line-height:17px;\" datetime=\"2019-01-23T20:09:19+00:00\">" + utc + "</time></p></div></blockquote> <script async=\"true\" src=\"//www.instagram.com/embed.js\"></script></div>";
    return code;
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
