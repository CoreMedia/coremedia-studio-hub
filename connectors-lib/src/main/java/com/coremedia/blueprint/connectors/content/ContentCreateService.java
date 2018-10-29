package com.coremedia.blueprint.connectors.content;

import com.coremedia.cap.common.Blob;
import com.coremedia.cap.content.Content;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.InputStream;

public interface ContentCreateService {
  /**
   * Creates a picture out of an URL.
   *
   * @param imageUrl the URL the image should be loaded from
   * @return the newly created image content
   */
  @Nullable
  Content createPictureFromUrl(Content owner, String imageName, String imageUrl);

  @Nullable
  Blob createBlob(InputStream in, String name, String mimeType);

  /**
   * Creates the content for the given attributes
   *
   * @param folder      the folder to create the new content for
   * @param name        the name of the new content
   * @param contentType the content type of the new content
   * @return the newly created content
   */
  @NonNull
  Content createContent(@NonNull String folder, @NonNull String name, @NonNull String contentType);
}
