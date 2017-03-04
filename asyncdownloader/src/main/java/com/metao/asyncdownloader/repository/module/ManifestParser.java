package com.metao.asyncdownloader.repository.module;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses {@link com.metao.asyncdownloader.repository.module.ImageDownloaderModule} references out of the AndroidManifest file.
 */
public final class ManifestParser {
  private static final String TAG = "ManifestParser";
  private static final String ImageDownloader_MODULE_VALUE = "ImageDownloaderModule";

  private final Context context;

  public ManifestParser(Context context) {
    this.context = context;
  }

  public List<ImageDownloaderModule> parse() {
    if (Log.isLoggable(TAG, Log.DEBUG)) {
      Log.d(TAG, "Loading ImageDownloader modules");
    }
    List<ImageDownloaderModule> modules = new ArrayList<>();
    try {
      ApplicationInfo appInfo = context.getPackageManager()
          .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
      if (appInfo.metaData == null) {
        return modules;
      }
      for (String key : appInfo.metaData.keySet()) {
        if (ImageDownloader_MODULE_VALUE.equals(appInfo.metaData.get(key))) {
          modules.add(parseModule(key));
          if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Loaded ImageDownloader module: " + key);
          }
        }
      }
    } catch (PackageManager.NameNotFoundException e) {
      throw new RuntimeException("Unable to find metadata to parse ImageDownloaderModules", e);
    }
    if (Log.isLoggable(TAG, Log.DEBUG)) {
      Log.d(TAG, "Finished loading ImageDownloader modules");
    }

    return modules;
  }

  private static ImageDownloaderModule parseModule(String className) {
    Class<?> clazz;
    try {
      clazz = Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("Unable to find ImageDownloaderModule implementation", e);
    }

    Object module;
    try {
      module = clazz.newInstance();
    } catch (InstantiationException e) {
      throw new RuntimeException("Unable to instantiate ImageDownloaderModule implementation for " + clazz,
              e);
      // These can't be combined until API minimum is 19.
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Unable to instantiate ImageDownloaderModule implementation for " + clazz,
              e);
    }

    if (!(module instanceof ImageDownloaderModule)) {
      throw new RuntimeException("Expected instanceof ImageDownloaderModule, but found: " + module);
    }
    return (ImageDownloaderModule) module;
  }
}