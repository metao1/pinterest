package com.metao.async.download.database.elements;

public class Task {

    public String id;
    public long size;
    public int state;
    public String url;
    public Long percent;
    public int chunks;
    public boolean notify;
    public boolean resumable;
    public String repositoryId;
    public String extension;
    public byte[] data;
    public boolean priority;


    public Task(String id, long size, String url,
                int state, int chunks, String repositoryId, boolean priority) {
        this.id = id;
        this.size = size;
        this.state = state;
        this.url = url;
        this.percent = 0L;
        this.chunks = chunks;
        this.notify = true;
        this.resumable = true;
        this.repositoryId = repositoryId;
        this.extension = "";
        this.priority = priority;
    }
}
