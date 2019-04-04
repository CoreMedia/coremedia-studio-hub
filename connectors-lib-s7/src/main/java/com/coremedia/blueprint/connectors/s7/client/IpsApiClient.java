package com.coremedia.blueprint.connectors.s7.client;

import com.coremedia.blueprint.connectors.s7.client.interceptor.SoapHeaderInterceptor;
import com.scene7.ipsapi.AuthenticationFaultException;
import com.scene7.ipsapi.AuthorizationFaultException;
import com.scene7.ipsapi.IpsApiFaultException;
import com.scene7.ipsapi.IpsApiPortType;
import com.scene7.ipsapi.xsd.AuthHeader;
import com.scene7.ipsapi.xsd._2013_02_15.GetFolderTreeParam;
import com.scene7.ipsapi.xsd._2013_02_15.GetFolderTreeReturn;
import com.scene7.ipsapi.xsd._2013_02_15.ObjectFactory;
import com.scene7.ipsapi.xsd._2013_02_15.SearchAssetsParam;
import com.scene7.ipsapi.xsd._2013_02_15.SearchAssetsReturn;
import com.scene7.ipsapi.xsd._2013_02_15.StringArray;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class IpsApiClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(IpsApiClient.class);

  public static final String IMAGE = "Image";
  public static final String VIDEO = "Video";
  public static final String ASSET_ARRAY_ITEMS_NAME = "assetArray/items/name";
  public static final String ASSET_ARRAY_ITEMS_FILENAME = "assetArray/items/fileName";
  public static final String ASSET_ARRAY_ITEMS_TYPE = "assetArray/items/type";
  public static final String ASSET_ARRAY_ITEMS_ASSETHANDLE = "assetArray/items/assetHandle";
  public static final String ASSET_ARRAY_ITEMS_IPSIMAGEURL = "assetArray/items/ipsImageUrl";

  private List<String> responseArray = new ArrayList<>(Arrays.asList(ASSET_ARRAY_ITEMS_NAME, ASSET_ARRAY_ITEMS_FILENAME, ASSET_ARRAY_ITEMS_TYPE, ASSET_ARRAY_ITEMS_ASSETHANDLE, ASSET_ARRAY_ITEMS_IPSIMAGEURL));

  private IpsApiPortType ipsApiServiceProxy;
  private String companyHandle;
  private String userid;
  private String password;

  public IpsApiClient(String companyHandle, String userid, String password, String url) {
    this.companyHandle = companyHandle;
    this.userid = userid;
    this.password = password;
    String addressUrl = "https://s7sps1apissl.scene7.com/scene7/services/IpsApiService";
    if (StringUtils.isNotBlank(url)) {
      addressUrl = url;

    }
    LoggingOutInterceptor loggingOutInterceptor = new LoggingOutInterceptor();

    if (LOGGER.isDebugEnabled()) {
      loggingOutInterceptor.setPrettyLogging(true);
      loggingOutInterceptor.setPrintWriter(new PrintWriter(System.out, true));
    }
    JaxWsProxyFactoryBean jaxWsProxyFactoryBean =
            new JaxWsProxyFactoryBean();
    jaxWsProxyFactoryBean.setServiceClass(IpsApiPortType.class);
    jaxWsProxyFactoryBean.setAddress(addressUrl);
    jaxWsProxyFactoryBean.getOutInterceptors().add(new SoapHeaderInterceptor());
    jaxWsProxyFactoryBean.getOutInterceptors().add(loggingOutInterceptor);
    jaxWsProxyFactoryBean.getInInterceptors().add(new LoggingInInterceptor());
    jaxWsProxyFactoryBean.getInFaultInterceptors().add(new LoggingInInterceptor());
    ipsApiServiceProxy = (IpsApiPortType) jaxWsProxyFactoryBean.create();
  }

  /**
   * Creates the authentication header object for soap request
   *
   * @return AuthHeader object for SOAP request
   */
  private AuthHeader createAuthHeader() {
    AuthHeader header = new AuthHeader();
    header.setUser(this.userid);
    header.setPassword(this.password);
    return header;
  }

  public SearchAssetsReturn searchAssetsByMetadata(String folder) throws AuthenticationFaultException, AuthorizationFaultException, IpsApiFaultException {
    ObjectFactory factory = new ObjectFactory();
    SearchAssetsParam searchAssetsByMetadataParam = factory.createSearchAssetsParam();
    searchAssetsByMetadataParam.setIncludeSubfolders(false);
    searchAssetsByMetadataParam.setFolder(folder);
    StringArray array = factory.createStringArray();
    array.getItems().add(IMAGE);
    array.getItems().add(VIDEO);


    searchAssetsByMetadataParam.setAssetTypeArray(array);
    StringArray fields = factory.createStringArray();
    fields.getItems().addAll(responseArray);
    searchAssetsByMetadataParam.setResponseFieldArray(fields);
    searchAssetsByMetadataParam.setCompanyHandle(companyHandle);
    return ipsApiServiceProxy.searchAssets(searchAssetsByMetadataParam, createAuthHeader());
  }

  public GetFolderTreeReturn getFolderTree(String folderPath, int depth) throws AuthenticationFaultException, AuthorizationFaultException, IpsApiFaultException {
    ObjectFactory factory = new ObjectFactory();
    GetFolderTreeParam getFolderTree = factory.createGetFolderTreeParam();
    getFolderTree.setCompanyHandle(companyHandle);
    getFolderTree.setFolderPath(folderPath);
    getFolderTree.setDepth(depth);
    return ipsApiServiceProxy.getFolderTree(getFolderTree, createAuthHeader());
  }
}
