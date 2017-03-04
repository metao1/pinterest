package com.metao.asyncdownloader.download.report.exceptions;

public class QueueDownloadInProgressException extends IllegalAccessException {

    public QueueDownloadInProgressException() {
        super("queue download is already in progress");
    }
}
