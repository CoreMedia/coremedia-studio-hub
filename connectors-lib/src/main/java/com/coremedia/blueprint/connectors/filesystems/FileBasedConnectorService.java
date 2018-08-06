package com.coremedia.blueprint.connectors.filesystems;

import com.coremedia.blueprint.connectors.api.ConnectorCategory;
import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.blueprint.connectors.api.ConnectorException;
import com.coremedia.blueprint.connectors.api.ConnectorId;
import com.coremedia.blueprint.connectors.api.ConnectorService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Required;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract default implementation of a file based connector service.
 * This class should simplify the implementation of a connector service
 * when the external system consists of files and folders.
 *
 * Additional, it implements a cache so not all files and folders are re-requested
 * for every REST request.
 */
abstract public class FileBasedConnectorService<T> implements ConnectorService {
  //assume that all file based connectors have set the property 'folder' in the connection settings
  private static final String FOLDER_PROPERTY = "folder";

  protected boolean ensureSeparatorSuffix = false;
  protected ConnectorContext context;
  private FileSystemService fileSystemService;

  @Required
  public void setFileCache(FileSystemService connectorFileSystemService) {
    this.fileSystemService = connectorFileSystemService;
  }

  /**
   * Returns the list of remote system objects for the given category, files and folders.
   *
   * @param categoryId the category id that contains the remote folder
   * @return a list of remote object, depending on the implementing API
   */
  abstract public List<T> list(ConnectorId categoryId);

  /**
   * Returns the remote file or folder for the given id
   *
   * @param id the id that contains the remote path
   * @return the remote system object, depending on the implementing API
   */
  abstract public T getFile(ConnectorId id);

  /**
   * Returns true if the given remote system object is afile
   *
   * @param entry the remote system object
   */
  abstract public boolean isFile(T entry);

  /**
   * Returns the path of the given remote system object
   *
   * @param entry the entry to get the file path for
   */
  abstract public String getPath(T entry);

  /**
   * Return the file name without the path of the given remote object
   *
   * @param entry the entry to retrieve the name for
   */
  abstract public String getName(T entry);

  @Override
  public boolean init(@NonNull ConnectorContext context) {
    this.context = context;
    return true;
  }

  @Override
  public void shutdown(@NonNull ConnectorContext context) throws ConnectorException {
    fileSystemService.invalidate();
  }

  public boolean refresh(@NonNull ConnectorContext context, @NonNull ConnectorCategory category) {
    fileSystemService.invalidate();
    return true;
  }

  /**
   * Returns the subfolder of the given cateogry
   *
   * @param categoryId the category id that contains the remote folder
   * @return the list of remote system folder entities, depending on the implementing API
   */
  protected List<T> getSubfolderEntities(ConnectorContext context, ConnectorId categoryId) {
    List<T> entries = listCachedEntities(context, categoryId).getFolderItemsData();
    return entries.stream().filter(this::isFolder).collect(Collectors.toList());
  }

  /**
   * Returns a list of remote file objects, not folders.
   *
   * @param categoryId the category id that contains the remove folder name
   */
  protected List<T> getFileEntities(ConnectorContext context, ConnectorId categoryId) {
    List<T> entries = listCachedEntities(context, categoryId).getFolderItemsData();
    return entries.stream().filter(this::isFile).collect(Collectors.toList());
  }

  /**
   * Returns all files and folder for the given category.
   * Ensure that the result is cached or an already cached result is used.
   * @param categoryId the category/folder to retrieve the children for.
   */
  protected FileSystemItem<T> listCachedEntities(ConnectorContext context, ConnectorId categoryId) {
    return fileSystemService.listItems(this, context, categoryId);
  }

  /**
   * Returns the cached item for the given id.
   * The id may be a category or item id.
   */
  protected T getCachedFileOrFolderEntity(ConnectorContext context, ConnectorId id) {
    if (id.isItemId()) {
      ConnectorId categoryId = getFolderId(id);
      List<T> entries = listCachedEntities(context, categoryId).getFolderItemsData();
      for (T entry : entries) {
        ConnectorId entryId = ConnectorId.createItemId(context.getConnectionId(), getPath(entry));
        if (entryId.equals(id)) {
          return entry;
        }
      }
      return null;
    }

    return listCachedEntities(context, id).getFolderData();
  }

  /**
   * Returns the parent category for the given category or null
   * if the given category is the root category
   *
   * @param categoryId the categoryId to retrieve the parent for
   */
  protected ConnectorCategory getParentCategory(@NonNull ConnectorContext context, ConnectorId categoryId) {
    if(isRootCategoryId(context, categoryId)) {
      return null;
    }

    String categoryPath = categoryId.getExternalId();
    String parentPath = categoryPath.substring(0, categoryPath.replaceAll("\\\\", "/").lastIndexOf("/"));

    //check for category path that end with a slash, e.g. S3
    if (categoryPath.endsWith("/")) {
      //then we have to re-cut the path again
      if (!parentPath.contains("/")) {
        parentPath = "";
      }
      else {
        parentPath = parentPath.substring(0, parentPath.replaceAll("\\\\", "/").lastIndexOf("/") + 1);
      }
    }

    ConnectorId parentId = ConnectorId.createCategoryId(context.getConnectionId(), parentPath);
    return getCategory(context, parentId);
  }

  /**
   * Ensures that during a file upload an existing file is not simply overwritten, but created with a file index
   *
   * @param categoryId the categoryId which contains the target folder for the upload
   * @param name       the default name of the file which might be renamed
   * @return a unique filename for the category folder
   */
  protected String createUniqueFilename(ConnectorContext context, ConnectorId categoryId, String name) {
    int index = 0;
    List<T> files = listCachedEntities(context, categoryId).getFolderItemsData();
    List<String> names = files.stream().map(this::getName).collect(Collectors.toList());

    while (names.contains(name)) {
      index++;
      String extension = FilenameUtils.getExtension(name);
      String baseName = FilenameUtils.getBaseName(name);
      name = baseName + " (" + index + ")";
      if (extension != null && extension.length() > 0) {
        name += "." + extension;
      }
    }

    String folder = getCategoryFolder(categoryId);
    if (!folder.endsWith("/")) {
      folder += "/";
    }
    return folder + name;
  }

  /**
   * Returns the connector id for the given item id
   *
   * @param itemId a ConnectorItem id
   */
  protected ConnectorId getFolderId(ConnectorId itemId) {
    String itemPath = itemId.getExternalId();
    if (itemPath.contains("/")) {
      String parentPath = itemPath.substring(0, itemPath.replaceAll("\\\\", "/").lastIndexOf("/"));
      String rootFolder = context.getProperty(FOLDER_PROPERTY);
      if (parentPath.equals("") || parentPath.equals(rootFolder)) {
        return ConnectorId.createRootId(context.getConnectionId());
      }
      //systems like S3 must have a slash suffix to indicate a folder
      if (ensureSeparatorSuffix) {
        parentPath = parentPath + "/";
      }
      return ConnectorId.createCategoryId(context.getConnectionId(), parentPath);
    }

    //if the path had no "/" we can only have an item of the root category where category folders have no leading slash
    return ConnectorId.createRootId(context.getConnectionId());
  }

  //--------------------- Helper ---------------------------------------------------------------------------------------

  private boolean isRootCategoryId(@NonNull ConnectorContext context, ConnectorId categoryId) {
    ConnectorId rootCategoryId = getRootCategory(context).getConnectorId();
    if (rootCategoryId.getId().equals(categoryId.getId())) {
      return true;
    }

    String categoryPath = categoryId.getExternalId();

    //check window specific path and remove drive letter
    if(categoryPath.indexOf(":") == 1) {
      categoryPath = categoryPath.substring(2, categoryPath.length());
    }

    //check for root
    String rootFolder = context.getProperty(FOLDER_PROPERTY);
    if(rootFolder == null) {
      rootFolder = "/";
    }

    rootFolder = rootFolder.replaceAll("\\\\", "/");
    if (categoryPath.equals("") || categoryPath.equals(rootFolder)) {
      return true;
    }

    return false;
  }

  private boolean isFolder(T t) {
    return !isFile(t);
  }

  private String getCategoryFolder(ConnectorId categoryId) {
    String path = categoryId.getExternalId();
    if (categoryId.isRootId()) {
      path = context.getProperty(FOLDER_PROPERTY);
      if (path == null) {
        path = "";
      }
    }
    return path.replaceAll("\\\\", "/");
  }

}
