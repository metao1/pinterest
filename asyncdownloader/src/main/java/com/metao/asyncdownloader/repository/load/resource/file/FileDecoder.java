package com.metao.asyncdownloader.repository.load.resource.file;

import com.metao.asyncdownloader.repository.load.Options;
import com.metao.asyncdownloader.repository.load.ResourceDecoder;
import com.metao.asyncdownloader.repository.load.engine.Resource;
import java.io.File;

/**
 * A simple {@link com.metao.asyncdownloader.repository.load.ResourceDecoder} that creates resource for a given {@link
 * File}.
 */
public class FileDecoder implements ResourceDecoder<File, File> {

  @Override
  public boolean handles(File source, Options options) {
    return true;
  }

  @Override
  public Resource<File> decode(File source, int width, int height, Options options) {
    return new FileResource(source);
  }
}
