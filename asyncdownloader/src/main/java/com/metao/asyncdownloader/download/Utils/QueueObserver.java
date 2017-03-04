package com.metao.asyncdownloader.download.Utils;

public interface QueueObserver {

    void wakeUp(String taskID);
}
