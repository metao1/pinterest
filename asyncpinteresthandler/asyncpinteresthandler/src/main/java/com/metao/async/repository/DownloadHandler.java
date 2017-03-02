package com.metao.async.repository;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.metao.async.download.core.AsyncDownloadHandler;
import com.metao.async.download.core.enums.QueueSort;
import com.metao.async.download.database.elements.Chunk;
import com.metao.async.download.database.elements.Task;
import com.metao.async.download.report.listener.DownloadManagerListener;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

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
    private static Handler mainUIHandler;
    private final Gson gson;
    private final ThreadHandler threadHandler;
    private ConcurrentHashMap<String, RepositoryCallback<T>> callbacks;
    private Repository<Task> taskRepository;
    private AsyncDownloadHandler asyncDownloadHandler;
    private Repository<Chunk> chunkRepository;
    private RepositoryCache<String, T> ramCacheRepository;
    private MessageArg messageArg;

    DownloadHandler() {
        Log.d("tag", "starting thread...");
        mainUIHandler = new MainUIThread();
        callbacks = new ConcurrentHashMap<>();
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        threadHandler = new ThreadHandler();
    }

    protected final void execute(MessageArg messageArg) {
        this.messageArg = messageArg;
        threadHandler.start();
    }

    protected final void setRepoCallback(String url, RepositoryCallback<T> repoCallback) {
        callbacks.put(url, repoCallback);
    }

    private void dispatchDownloadSignal(String taskId, T o) {
        Enumeration<String> keys = callbacks.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            try {
                callbacks.get(key).onDownloadFinished(taskId, o);
            } catch (Exception e) {

            }
        }
    }

    private void dispatchProgressSignal(String taskId, Object object) {
        Enumeration<String> keys = callbacks.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            RepositoryCallback<T> tRepositoryCallback = callbacks.get(key);
            if (object instanceof Double) {
                tRepositoryCallback.onDownloadProgress(taskId, (Double) object);
            }
        }
    }

    private void dispatchErrorSignal(String taskId, T object) {
        Enumeration<String> keys = callbacks.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            RepositoryCallback<T> tRepositoryCallback = callbacks.get(key);
            tRepositoryCallback.onError(new Throwable(taskId));
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
                        bundle.putString("resultType", "onDownloadProgress");
                        finalMessageArg.setObject(percent);
                        finalMessageArg.setType(messageType);
                        bundle.putString("taskId", taskId);
                        bundle.putString("dataType", String.valueOf(finalMessageArg.getType()));
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
                                Message message = new Message();
                                Bundle bundle = new Bundle();
                                MessageArg finalMessageArg = new MessageArg(taskId);
                                byte[] bytes = task.data;
                                finalMessageArg.setUrl(urlAddress);
                                if (jobRepositoryType == Repository.RepositoryType.JSON) {
                                    String response = new String(bytes).trim();
                                    finalMessageArg.setType(messageType);
                                    JsonReader reader = new JsonReader(new StringReader(response));
                                    reader.setLenient(true);
                                    T t = gson.fromJson(reader, finalMessageArg.getType());
                                    ramCacheRepository.put(taskId, t);
                                    finalMessageArg.setObject(t);
                                    bundle.putString("taskId", taskId);
                                    bundle.putString("resultType", "onDownloadCompleted");
                                    bundle.putSerializable("message", finalMessageArg);
                                    message.setData(bundle);
                                    mainUIHandler.sendMessage(message);
                                } else if (jobRepositoryType == Repository.RepositoryType.BITMAP) {
                                    if (bytes != null && bytes.length > 0) {
                                        /*Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);*/
                                        BitmapFactory.Options options = new BitmapFactory.Options();
                                        options.inMutable = true;
                                        Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                                        try {
                                            Log.d("image repo", bytes.length + ":" + String.valueOf(image) + ":" + urlAddress);
                                            if (image != null) {
                                                ramCacheRepository.put(taskId, (T) image);
                                                finalMessageArg.setObject(image);
                                                bundle.putString("taskId", taskId);
                                                bundle.putString("resultType", "onDownloadCompleted");
                                                bundle.putSerializable("message", finalMessageArg);
                                                message.setData(bundle);
                                                mainUIHandler.sendMessage(message);
                                            }
                                        } catch (Exception e) {

                                        }
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void connectionLost(String taskId) {
                        MessageArg finalMessageArg = new MessageArg(taskId);
                        finalMessageArg.setUrl(urlAddress);
                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putString("taskId", taskId);
                        bundle.putString("resultType", "onError");
                        bundle.putSerializable("message", finalMessageArg);
                        message.setData(bundle);
                        mainUIHandler.sendMessage(message);
                    }
                });
                asyncDownloadHandler.addTask(messageArg.getId(), REPOSITORY_NAME, urlAddress, MAX_CHUNKS, OVERWRITE, PRIORITY);
                asyncDownloadHandler.startQueueDownload(DOWNLOAD_TASKS_PER_TIME, SORT_TYPE);
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
            String resultType = msg.getData().getString("resultType");
            String taskId = msg.getData().getString("taskId");
            String url = messageArg.getUrl();
            T object = (T) messageArg.getObject();
            switch (resultType) {
                case "onDownloadCompleted":
                    dispatchDownloadSignal(taskId, object);
                    break;
                case "onDownloadProgress":
                    dispatchProgressSignal(taskId, object);
                    break;
                case "onError":
                    dispatchErrorSignal(taskId, object);
                    break;
            }
        }
    }
}
