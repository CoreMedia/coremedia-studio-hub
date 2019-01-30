package com.coremedia.blueprint.connectors.upload;

import com.coremedia.cap.common.Blob;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Utility class just for post processing the uploaded image variants.
 */
public class TransformedBlob {
  @NonNull
  private final Blob blob;
  @NonNull
  private final String variant;

  TransformedBlob(@NonNull Blob blob, @NonNull String variant) {
    this.blob = blob;
    this.variant = variant;
  }

  @NonNull
  public Blob getBlob() {
    return blob;
  }

  @NonNull
  public String getVariant() {
    return variant;
  }
}
