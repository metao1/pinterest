package com.metao.asyncdownloader.download.report;

import com.metao.asyncdownloader.download.core.enums.TaskStates;
import com.metao.asyncdownloader.download.database.elements.Chunk;
import com.metao.asyncdownloader.download.database.elements.Task;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ReportStructure {

    public String id;
    public String name;
    public int state;
    public String url;
    public long fileSize;
    public boolean resumable;
    public String type;
    public int chunks;
    public double percent;
    public long downloadLength;
    public String saveAddress;
    public boolean priority;

    public long setDownloadLength(long downloadedLength) {
        return downloadLength += downloadedLength;
    }

    public long getTotalSize() {
        return fileSize;
    }

    public boolean isResumable() {
        return resumable;
    }

    public ReportStructure setObjectValues(Task task, List<Chunk> taskChunks) {
        this.id = task.id;
        this.state = task.state;
        this.resumable = task.resumable;
        this.url = task.url;
        this.fileSize = task.size;
        this.type = task.extension;
        this.chunks = task.chunks;
        this.priority = task.priority;
        this.percent = calculatePercent(task, taskChunks);

        return this;
    }

    /**
     * calculate download percent from compare chunks size with real file size
     **/
    private double calculatePercent(Task task, List<Chunk> chunks) {
        // initialize report
        double report = 0;

        // if download not completed we have chunks
        if (task.state != TaskStates.DOWNLOAD_FINISHED) {
            int sum = 0;
            for (Chunk chunk : chunks) {
                if (chunk.data != null) {
                    this.downloadLength += chunk.data.length;
                }
            }

            if (task.size > 0) {
                report = ((float) downloadLength / task.size * 100);
            }
        } else {
            this.downloadLength = task.size;
            report = 100;
        }

        return report;
    }


    public JSONObject toJsonObject() {
        JSONObject json = new JSONObject();
        try {
            return json.put("token", String.valueOf(id))
                    .put("name", name)
                    .put("state", state)
                    .put("resumable", resumable)
                    .put("fileSize", fileSize)
                    .put("url", url)
                    .put("type", type)
                    .put("chunks", chunks)
                    .put("percent", percent)
                    .put("downloadLength", downloadLength)
                    .put("saveAddress", saveAddress)
                    .put("priority", priority);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }
}
