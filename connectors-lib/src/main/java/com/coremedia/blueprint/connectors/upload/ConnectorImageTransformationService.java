package com.coremedia.blueprint.connectors.upload;

import com.coremedia.blueprint.connectors.api.ConnectorContext;
import com.coremedia.cap.common.Blob;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.struct.Struct;
import com.coremedia.cap.transform.Breakpoint;
import com.coremedia.cap.transform.TransformImageService;
import com.coremedia.cap.transform.Transformation;
import com.coremedia.image.ImageDimensionsExtractor;
import com.coremedia.transform.BlobTransformer;
import com.coremedia.transform.NamedTransformBeanBlobTransformer;
import com.coremedia.transform.TransformedBeanBlob;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimeType;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 */
public class ConnectorImageTransformationService {
  private static final Logger LOG = LoggerFactory.getLogger(ConnectorImageTransformationService.class);

  private TransformImageService transformImageService;
  private BlobTransformer blobTransformer;
  private ImageDimensionsExtractor imageDimensionsExtractor;
  private NamedTransformBeanBlobTransformer mediaTransformer;

  public ConnectorImageTransformationService(@NonNull TransformImageService transformImageService,
                                             @NonNull BlobTransformer blobTransformer,
                                             @NonNull ImageDimensionsExtractor imageDimensionsExtractor,
                                             @NonNull NamedTransformBeanBlobTransformer mediaTransformer) {
    this.transformImageService = transformImageService;
    this.blobTransformer = blobTransformer;
    this.imageDimensionsExtractor = imageDimensionsExtractor;
    this.mediaTransformer = mediaTransformer;
  }

  @NonNull
  public List<TransformedBlob> transform(@NonNull ConnectorContext context, @NonNull Content content, @NonNull String propertyName, @NonNull Blob originalBlob, @NonNull List<String> variants) {
    List<TransformedBlob> result = new ArrayList<>();

    //check if the blob transformer accepts the given mimeType of the data
    MimeType type = originalBlob.getContentType();
    if (!blobTransformer.accepts(type)) {
      LOG.info("Refused connector variant upload of {} as picture because MIME-type {} is not supported", originalBlob, type);
      return result;
    }

    //extract image dimensions to find matching breakpoint
    int[] pictureDimensions = getPictureDimensions(originalBlob);
    if (pictureDimensions.length == 0) {
      LOG.info("Failed to read image size of {} from {}", originalBlob, content.getPath());
      return result;
    }

    List<Transformation> transformations = transformImageService.getTransformations(content);
    for (String variant : variants) {
      //iterate through the transformation configurations to find matching variants
      Optional<Transformation> transformation = transformations.stream().filter(t->t.getName().equalsIgnoreCase(variant)).findFirst();
      if (!transformation.isPresent()) {
        continue;
      }

      //find the target resolution find using the largest matching breakpoint for the image dimension
      Breakpoint breakpoint = findBreakpoint(transformation.get(), pictureDimensions);
      if (breakpoint == null) {
        LOG.warn("No valid breakpoint found for connector image transformation of " + content.getPath());
        continue;
      }

      String transformationName = transformation.get().getName();
      TransformedBeanBlob transformedBlob = mediaTransformer.transform(new TransformationBean(content, propertyName), transformationName);
      if(transformedBlob != null) {
        Blob targetBlob = transformImageService.transformWithDimensions(content, originalBlob, transformedBlob, variant, null, breakpoint.getWidth(), breakpoint.getHeight());
        result.add(new TransformedBlob(targetBlob, transformationName));
      }
      else {
        LOG.warn("No image variant '" + transformationName + "' found for picture " + content.getPath());
      }
    }

    return result;
  }

  /**
   * Returns the matching breakpoint with the maximum size matching for the given image dimension.
   * @param transformation the transformation that contains the breakpoints
   * @param pictureDimensions the picture dimensions to find the matching breakpoint for
   * @return the maximum breakpoint or null if the image is too small for any breakpoint
   */
  @Nullable
  private Breakpoint findBreakpoint(@NonNull Transformation transformation, int[] pictureDimensions) {
    int imageWidth = pictureDimensions[0];
    int imageHeight = pictureDimensions[1];
    Breakpoint lastMatchingBreakpoint = null;
    for (Breakpoint breakpoint : transformation.getBreakpoints()) {
      int width = breakpoint.getWidth();
      int height = breakpoint.getHeight();

      if(width < imageWidth && height < imageHeight) {
        lastMatchingBreakpoint = breakpoint;
      }
    }
    return lastMatchingBreakpoint;
  }

  /**
   * Calculates the size of the image
   *
   * @param blob the original blob read from the blob property.
   */
  private int[] getPictureDimensions(Blob blob) {
    try (InputStream input = blob.getInputStream()) {
      String mimeType = blob.getContentType().getBaseType();
      return imageDimensionsExtractor.getImageDimensions(mimeType, input);
    } catch (Exception e) {
      LOG.warn("Could not read dimensions. Upload will ignore " + blob, e);
    }
    return new int[0];
  }

  /**
   * The NamedTransformBeanBlobTransformer requires a bean
   * to access the transformation data. We use this transformation ben
   * to extract the transformation data out of the original content.
   */
  class TransformationBean {
    private Content content;
    private String propertyName;

    TransformationBean(Content content, String propertyName) {
      this.content = content;
      this.propertyName = propertyName;
    }

    @SuppressWarnings("unused")
    public Blob getData() {
      return content.getBlob(propertyName);
    }

    @SuppressWarnings("unused")
    public Map<String, String> getTransformMap() {
      Map<String, String> transformations;
      Struct localSettings = content.getStruct("localSettings");
      if (localSettings != null) {
        Map<String, Object> structMap = localSettings.getStruct("transforms").getProperties();
        transformations = structMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> (String) e.getValue()));
      }
      else {
        transformations = new HashMap<>();
      }
      return transformImageService.getTransformationOperations(content, propertyName, transformations);
    }
  }
}
