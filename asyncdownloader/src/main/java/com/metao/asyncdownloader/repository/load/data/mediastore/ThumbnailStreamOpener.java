package com.metao.asyncdownloader.repository.load.data.mediastore;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.metao.asyncdownloader.repository.load.ImageHeaderParser;
import com.metao.asyncdownloader.repository.load.ImageHeaderParserUtils;
import com.metao.asyncdownloader.repository.load.engine.bitmap_recycle.ArrayPool;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

class ThumbnailStreamOpener {
  private static final String TAG = "ThumbStreamOpener";
  private static final FileService DEFAULT_SERVICE = new FileService();

  private final FileService service;
  private final ThumbnailQuery query;
  private final ArrayPool byteArrayPool;
  private final ContentResolver contentResolver;
  private final List<ImageHeaderParser> parsers;

  public ThumbnailStreamOpener(
      List<ImageHeaderParser> parsers, ThumbnailQuery query, ArrayPool byteArrayPool,
      ContentResolver contentResolver) {
    this(parsers, DEFAULT_SERVICE, query, byteArrayPool, contentResolver);
  }

  public ThumbnailStreamOpener(
      List<ImageHeaderParser> parsers,
      FileService service,
      ThumbnailQuery query,
      ArrayPool byteArrayPool,
      ContentResolver contentResolver) {
    this.service = service;
    this.query = query;
    this.byteArrayPool = byteArrayPool;
    this.contentResolver = contentResolver;
    this.parsers = parsers;
  }

  public int getOrientation(Uri uri) {
    InputStream is = null;
    try {
      is = contentResolver.openInputStream(uri);
      return ImageHeaderParserUtils.getOrientation(parsers, is, byteArrayPool);
    } catch (IOException e) {
      if (Log.isLoggable(TAG, Log.DEBUG)) {
        Log.d(TAG, "Failed to open uri: " + uri, e);
      }
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          // Ignored.
        }
      }
    }
    return ImageHeaderParser.UNKNOWN_ORIENTATION;
  }

  public InputStream open(Uri uri) throws FileNotFoundException {
    Uri thumbnailUri = null;
    InputStream inputStream = null;

    final Cursor cursor = query.query(uri);
    try {
      if (cursor == null || !cursor.moveToFirst()) {
        return null;
      }
      String path = cursor.getString(0);
      if (TextUtils.isEmpty(path)) {
        return null;
      }

      File file = service.get(path);
      if (service.exists(file) && service.length(file) > 0) {
        thumbnailUri = Uri.fromFile(file);
      }
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    if (thumbnailUri != null) {
      inputStream = contentResolver.openInputStream(thumbnailUri);
    }
    return inputStream;
  }
}
