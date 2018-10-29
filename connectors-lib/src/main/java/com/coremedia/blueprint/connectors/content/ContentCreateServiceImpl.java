package com.coremedia.blueprint.connectors.content;

import com.coremedia.blueprint.connectors.api.ConnectorException;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.content.ContentType;
import com.coremedia.mimetype.MimeTypeService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

public class ContentCreateServiceImpl implements ContentCreateService {
  private static final Logger LOG = LoggerFactory.getLogger(ContentCreateServiceImpl.class);
  private static final String PICTURE_DOC_TYPE = "CMPicture";

  private ContentRepository contentRepository;
  private MimeTypeService mimeTypeService;

  public ContentCreateServiceImpl(@NonNull ContentRepository contentRepository,
                                  @NonNull MimeTypeService mimeTypeService) {
    this.contentRepository = contentRepository;
    this.mimeTypeService = mimeTypeService;
  }

  @Nullable
  @Override
  public Content createPictureFromUrl(Content owner, String imageName, String imageUrl) {
    Content picture = createContent(owner.getParent().getPath(), imageName, PICTURE_DOC_TYPE);
    try {
      picture.set("title", imageName);

      URL url = new URL(imageUrl);
      URLConnection con = url.openConnection();
      InputStream in = con.getInputStream();
      Blob blob = createBlob(in, imageName, "image/jpeg");
      picture.set("data", blob);
      in.close();

      return picture;
    } catch (Exception e) {
      LOG.error("Failed to create image content: " + e.getMessage(), e);
    } finally {
      if (picture != null) {
        picture.checkIn();
      }
    }
    return null;
  }

  @Nullable
  @Override
  public Blob createBlob(InputStream in, String name, String mimeType) {
    try {
      if (in != null) {
        if (mimeType == null) {
          mimeType = mimeTypeService.getMimeTypeForResourceName(name);
        }
        MimeType mt = new MimeType(mimeType);
        Blob blob = contentRepository.getConnection().getBlobService().fromInputStream(in, mt);
        in.close();
        return blob;
      }
    } catch (MimeTypeParseException e) {
      LOG.error("Failed to resolve blob mime type for " + name + ": " + e.getMessage(), e);
    } catch (IOException e) {
      LOG.error("Error creating blob for item " + name + ": " + e.getMessage(), e);
    }
    return null;
  }

  @NonNull
  @Override
  public Content createContent(@NonNull String folder, @NonNull String name, @NonNull String contentType) {
    ContentType ct = contentRepository.getContentType(contentType);
    Content folderContent = contentRepository.getChild(folder);
    if (ct != null) {
      return ct.createByTemplate(folderContent, name, "{3} ({1})", new HashMap<>());
    }
    throw new ConnectorException("No content type '" + contentType + "' found for connector item content creation");
  }
}
