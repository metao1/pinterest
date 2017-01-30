package com.metao.asyncpinteresthandler.report.exceptions;

public class QueueDownloadInProgressException extends IllegalAccessException {

    public QueueDownloadInProgressException() {
        super("queue download is already in progress");
    }
}
