package com.metao.asyncdownloader.repository.core;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * A factory class that produces a functional
 * {@link ConnectivityMonitor}.
 */
public interface ConnectivityMonitorFactory {

  @NonNull
  ConnectivityMonitor build(
          @NonNull Context context,
          @NonNull ConnectivityMonitor.ConnectivityListener listener);
}
