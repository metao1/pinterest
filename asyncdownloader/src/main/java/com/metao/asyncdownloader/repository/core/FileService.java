package com.metao.asyncdownloader.repository.core;

import java.io.File;

class FileService {
  public boolean exists(File file) {
    return file.exists();
  }

  public long length(File file) {
    return file.length();
  }

  public File get(String path) {
    return new File(path);
  }
}