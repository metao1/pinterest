package com.metao.asyncdownloader.repository.load.resource.file;

import com.metao.asyncdownloader.repository.load.resource.SimpleResource;
import java.io.File;

/**
 * A simple {@link com.metao.asyncdownloader.repository.load.engine.Resource} that wraps a {@link File}.
 */
public class FileResource extends SimpleResource<File> {
  public FileResource(File file) {
    super(file);
  }
}
