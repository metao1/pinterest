package com.metao.async;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.metao.async.download.core.AsyncDownloadHandler;
import com.metao.async.download.core.enums.QueueSort;
import com.metao.async.download.database.elements.Chunk;
import com.metao.async.download.database.elements.Task;
import com.metao.async.download.report.listener.DownloadManagerListener;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by metao on 2/1/2017.
 */
public class DownloadHandler<T> {

    private static final String REPOSITORY_NAME = "TaskRepository";
    private static final boolean OVERWRITE = true;
    private static final boolean PRIORITY = true;
    private static final int DOWNLOAD_TASKS_PER_TIME = 14;
    private static final int SORT_TYPE = QueueSort.HighPriority;
    private static final int MAX_CHUNKS = 14;
    private static final int BUFFER_SIZE = 1024;
    private final Gson gson;
    private LinkedBlockingQueue<MessageArg> messageArgs = new LinkedBlockingQueue<>();
    private ArrayList<RepositoryCallback<T>> callbacks = new ArrayList<>();
    private static Handler mainUIHandler;
    private Repository<Task> taskRepository;
    private AsyncDownloadHandler asyncDownloadHandler;
    private Repository<Chunk> chunkRepository;
    private RepositoryCache<String, T> ramCacheRepository;

    DownloadHandler() {
        Log.d("tag", "starting thread...");
        mainUIHandler = new MainUIThread();
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        new ThreadHandler().start();
    }

    protected final void execute(MessageArg messageArg) {
        messageArgs.add(messageArg);
        asyncDownloadHandler = new AsyncDownloadHandler(this.chunkRepository, this.taskRepository);
    }

    protected synchronized final void setRepoCallback(RepositoryCallback<T> repoCallback) {
        callbacks.add(repoCallback);
    }

    private synchronized void dispatchDownloadSignal(String urlAddress, Object o) {
        for (int i = 0; i < callbacks.size(); i++) {
            RepositoryCallback repositoryCallback = callbacks.get(i);
            repositoryCallback.onDownloadFinished(urlAddress, o);
        }
    }

    private void dispatchProgressSignal(String url, Object object) {
        for (int i = 0; i < callbacks.size(); i++) {
            RepositoryCallback repositoryCallback = callbacks.get(i);
            if (object instanceof Double) {
                repositoryCallback.onDownloadProgress(url, (Double) object);
            }
        }
    }

    private void dispatchErrorSignal(String url, Object object) {
        for (int i = 0; i < callbacks.size(); i++) {
            RepositoryCallback repositoryCallback = callbacks.get(i);
            if (object instanceof Double) {
                repositoryCallback.onError(new Throwable(url));
            }
        }
    }

    public void setTaskRepository() {
        this.taskRepository = new Repository<Task>("TaskRepository") {

        };
    }

    public void setChunkRepository() {
        this.chunkRepository = new Repository<Chunk>("ChunkRepository") {

        };
    }

    public void setCacheRepository(RepositoryCache<String, T> ramCacheRepository) {
        this.ramCacheRepository = ramCacheRepository;
    }

    private class ThreadHandler extends Thread {

        @Override
        public void run() {
            try {
                MessageArg messageArg = null;
                while ((messageArg = messageArgs.take()) != null) {
                    Log.d("tag", "threading...");
                    this.setName(ThreadHandler.class.getName());
                    this.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                    final Repository.RepositoryType jobRepositoryType = messageArg.getJobRepositoryType();
                    final Type messageType = messageArg.getType();
                    final String urlAddress = messageArg.getUrl();
                    if (urlAddress == null) {
                        throw new Exception("no argument specified for url");
                    }
                    if (asyncDownloadHandler == null) {
                        asyncDownloadHandler = new AsyncDownloadHandler(chunkRepository, taskRepository);
                    }
                    asyncDownloadHandler.init(MAX_CHUNKS, new DownloadManagerListener() {
                        @Override
                        public void OnDownloadStarted(String taskId) {

                        }

                        @Override
                        public void OnDownloadPaused(String taskId) {

                        }

                        @Override
                        public void onDownloadProgress(String taskId, double percent, long downloadedLength) {
                            MessageArg finalMessageArg = new MessageArg(taskId);
                            finalMessageArg.setUrl(urlAddress);
                            Message message = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putString("type", "onDownloadProgress");
                            finalMessageArg.setObject(percent);
                            finalMessageArg.setType(messageType);
                            bundle.putSerializable("message", finalMessageArg);
                            message.setData(bundle);
                            mainUIHandler.sendMessage(message);
                        }

                        @Override
                        public void OnDownloadFinished(String taskId) {

                        }

                        @Override
                        public void OnDownloadRebuildStart(String taskId) {

                        }

                        @Override
                        public void OnDownloadRebuildFinished(String taskId) {

                        }

                        @Override
                        public void OnDownloadCompleted(String taskId) {
                            Collection<Task> values = taskRepository.getRamCacheRepository().snapshot().values();
                            for (Task task : values) {
                                if (task.id.equalsIgnoreCase(taskId)) {
                                    MessageArg finalMessageArg = new MessageArg(taskId);
                                    finalMessageArg.setUrl(urlAddress);
                                    byte[] bytes = task.data;
                                    if (jobRepositoryType == Repository.RepositoryType.JSON) {
                                        String response = new String(bytes);
                                        finalMessageArg.setType(messageType);
                                        T t = gson.fromJson(response, finalMessageArg.getType());
                                        ramCacheRepository.put(urlAddress, t);
                                        finalMessageArg.setObject(t);
                                    } else {
                                        Bitmap image = BitmapConverter.getImage(bytes);
                                        finalMessageArg.setObject(image);
                                        ramCacheRepository.put(urlAddress, (T) image);
                                    }
                                    Message message = new Message();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("type", "onDownloadCompleted");
                                    bundle.putSerializable("message", finalMessageArg);
                                    message.setData(bundle);
                                    mainUIHandler.sendMessage(message);
                                }
                            }
                        }

                        @Override
                        public void connectionLost(String taskId) {
                            MessageArg finalMessageArg = new MessageArg(taskId);
                            finalMessageArg.setUrl(urlAddress);
                            Message message = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putString("type", "onError");
                            bundle.putSerializable("message", finalMessageArg);
                            message.setData(bundle);
                            mainUIHandler.sendMessage(message);
                        }
                    });
                    asyncDownloadHandler.addTask(REPOSITORY_NAME, urlAddress, urlAddress, MAX_CHUNKS, OVERWRITE, PRIORITY);
                    asyncDownloadHandler.startQueueDownload(DOWNLOAD_TASKS_PER_TIME, SORT_TYPE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class MainUIThread extends Handler {
        MainUIThread() {
            super(Looper.getMainLooper());
            Log.d("JobHandler", "Initiate Job Handler");
        }

        @Override
        public void handleMessage(Message msg) {
            MessageArg messageArg = (MessageArg) msg.getData().getSerializable("message");
            String type = msg.getData().getString("type");
            String url = messageArg.getUrl();
            Object object = messageArg.getObject();
            switch (type) {
                case "onDownloadCompleted":
                    dispatchDownloadSignal(url, object);
                    break;
                case "onDownloadProgress":
                    dispatchProgressSignal(url, object);
                    break;
                case "onError":
                    dispatchErrorSignal(url, object);
                    break;
            }
        }
    }
}
