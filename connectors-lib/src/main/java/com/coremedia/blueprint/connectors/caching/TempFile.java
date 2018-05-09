package com.coremedia.blueprint.connectors.caching;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class TempFile {
  private final File file;
  private String id;
  private InputStream inputStream;

  TempFile(String id, File file) {
    this.id = id;
    this.file = file;
  }

  public File getFile() {
    return file;
  }

  public String getId() {
    return id;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof TempFile && ((TempFile) obj).id.equals(id);
  }

  public InputStream stream() throws FileNotFoundException {
    inputStream = new FileInputStream(file);
    return inputStream;
  }

  public boolean delete() {
    try {
      if(inputStream != null) {
        inputStream.close();
      }
    } catch (IOException e) {
      //ignore
    }
    return file.delete();
  }
}
