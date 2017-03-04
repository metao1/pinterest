package com.metao.asyncdownloader.download.report.listener;


public interface DownloadManagerListener {

    void OnDownloadStarted(String taskId);

    void OnDownloadPaused(String taskId);

    void onDownloadProgress(String taskId, double percent, long downloadedLength);

    void OnDownloadFinished(String taskId);

    void OnDownloadRebuildStart(String taskId);

    void OnDownloadRebuildFinished(String taskId);

    void OnDownloadCompleted(String taskId);

    void connectionLost(String taskId);

}
