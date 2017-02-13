package com.metao.async.download.core.chunkWorker;

import android.util.Log;
import com.metao.async.repository.Repository;
import com.metao.async.download.core.enums.TaskStates;
import com.metao.async.download.core.mainWorker.QueueModerator;
import com.metao.async.download.database.ChunksDataSource;
import com.metao.async.download.database.TasksDataSource;
import com.metao.async.download.database.elements.Chunk;
import com.metao.async.download.database.elements.Task;
import com.metao.async.download.report.ReportStructure;
import com.metao.async.download.report.listener.DownloadManagerListenerModerator;

import java.util.HashMap;
import java.util.List;

public class Moderator {

    private final Repository<Task> repository;
    private final int THRESHOLD = 1024 * 20;
    public DownloadManagerListenerModerator downloadManagerListener;
    private ChunksDataSource chunksDataSource;  // query on chunk table
    private TasksDataSource tasksDataSource;    // query on task table
    private HashMap<Integer, Thread> workerList;          // chunk downloader list
    private HashMap<String, ReportStructure> processReports;  // to save download percent
    private QueueModerator finishedDownloadQueueObserver;
    /*
    to calculate download percentage
    if download task is un resumable it return -1 as percent
     */
    private int downloadByteThreshold = 0;

    public Moderator(Repository<Task> repository, TasksDataSource tasksDS, ChunksDataSource chunksDS) {
        tasksDataSource = tasksDS;
        chunksDataSource = chunksDS;
        this.repository = repository;
        workerList = new HashMap<>(); // chunk downloader with they id key
        processReports = new HashMap<>();
    }

    public void setQueueObserver(QueueModerator queueObserver) {
        finishedDownloadQueueObserver = queueObserver;
    }

    public void start(Task task, DownloadManagerListenerModerator listener) {
        downloadManagerListener = listener;
        // fetch task chunk info
        // set task state to Downloading
        // get any chunk file size calculate where it has to begin
        // start any of them as AsyncTask
        // fetch task chunk info
        List<Chunk> taskChunks = chunksDataSource.chunksRelatedTask(task.id);
        ReportStructure rps = new ReportStructure();
        rps.setObjectValues(task, taskChunks);
        processReports.put(task.id, rps);
        Long downloaded;
        Long totalSize;
        if (taskChunks != null) {
            // set task state to Downloading
            // to lock start download again!
            task.state = TaskStates.DOWNLOADING;
            tasksDataSource.update(task);
            // get any chunk file size calculate
            for (Chunk chunk : taskChunks) {
                downloaded = task.percent;
                totalSize = new Long(chunk.end - chunk.begin + 1);
                if (!task.resumable) {
                    chunk.begin = 0;
                    chunk.end = 0;
                    // start one chunk as AsyncTask (duplicate code!! :( )
                    Thread chunkDownloaderThread = new AsyncWorker(task, chunk, this);
                    workerList.put(chunk.id, chunkDownloaderThread);
                    chunkDownloaderThread.start();
                } else if (!downloaded.equals(totalSize)) {
                    // where it has to begin
                    // modify start point but i have not save it in Database
                    chunk.begin = chunk.begin + downloaded;
                    // start any of them as AsyncTask
                    Thread chunkDownloaderThread = new AsyncWorker(task, chunk, this);
                    workerList.put(chunk.id, chunkDownloaderThread);
                    chunkDownloaderThread.start();
                }
            }
            // notify to developer------------------------------------------------------------
            downloadManagerListener.OnDownloadStarted(task.id);
        }
    }

    /*
     * pause all chunk thread related to one Task
     */
    public void pause(String taskID) {

        Task task = tasksDataSource.getTaskInfo(taskID);

        if (task != null && task.state != TaskStates.PAUSED) {
            // pause task asyncWorker
            // change task state
            // save in DB
            // notify developer

            // pause task asyncWorker
            List<Chunk> taskChunks =
                    chunksDataSource.chunksRelatedTask(task.id);
            for (Chunk chunk : taskChunks) {
                Thread worker = workerList.get(chunk.id);
                if (worker != null) {
                    worker.interrupt();
                    workerList.remove(chunk.id);
                }
            }

            // change task state
            // save in DB
            task.state = TaskStates.PAUSED;
            tasksDataSource.update(task);

            // notify to developer------------------------------------------------------------
            downloadManagerListener.OnDownloadPaused(task.id);

        }
    }

    public void connectionLost(String taskId) {
        downloadManagerListener.ConnectionLost(taskId);
    }

    public void process(String taskId, long byteRead) {
        ReportStructure report = processReports.get(taskId);
        double percent = -1;
        long downloadLength = report
                .setDownloadLength(byteRead);

        downloadByteThreshold += byteRead;
        if (downloadByteThreshold > THRESHOLD) {
            downloadByteThreshold = 0;
            if (report.isResumable()) {
                percent = ((float) downloadLength / report.getTotalSize() * 100);
            }
            // notify to developer------------------------------------------------------------
            downloadManagerListener.onDownloadProcess(taskId, percent, downloadLength);
        }
    }

    public void rebuild(Chunk chunk) {
        workerList.remove(chunk.id);
        List<Chunk> taskChunks =
                chunksDataSource.chunksRelatedTask(chunk.taskId); // delete itself from worker list
        for (Chunk ch : taskChunks) {
            if (workerList.get(ch.id) != null)
                return;
        }
        Task task = tasksDataSource.getTaskInfo(chunk.taskId);
        // set state task state to finished
        task.state = TaskStates.DOWNLOAD_FINISHED;
        tasksDataSource.update(task);

        // notify to developer------------------------------------------------------------
        downloadManagerListener.OnDownloadFinished(task.id);

        // assign chunk files together
        Thread t = new Builder(task, taskChunks, this);
        t.start();
    }

    public void reBuildIsDone(Task task, List<Chunk> taskChunks) {
        // delete chunk row from chunk table
        for (Chunk chunk : taskChunks) {
            chunksDataSource.delete(chunk.id);
            repository.delete(task.id);
        }

        // notify
        downloadManagerListener.OnDownloadRebuildFinished(task.id);

        // change task row state
        task.state = TaskStates.END;
        task.notify = false;
        tasksDataSource.update(task);
        // notify
        downloadManagerListener.OnDownloadCompleted(task.id);

        wakeUpObserver(task.id);
    }

    private void wakeUpObserver(String taskID) {
        if (finishedDownloadQueueObserver != null) {
            finishedDownloadQueueObserver.wakeUp(taskID);
        }
    }
}
