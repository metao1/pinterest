package com.metao.pinterest.async;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by metao on 1/2/2017.
 * Command Pattern Design
 * A Class for Handling commands
 */
public class JobHandler implements Job {

    private static Handler mainUIHandler;
    private static Handler bgThreadHandler;
    private static ConcurrentHashMap<String, JobCallBack> jobCallBacksHashMap = new ConcurrentHashMap<>();
    private  MessageArg messageArg;

    public JobHandler() {
        mainUIHandler = new MainUIThread();
        new ThreadHandler().start();
    }

    @Override
    public void execute(MessageArg messageArg,JobCallBack jobCallback) {
        this.messageArg = messageArg;
        this.jobCallBacksHashMap.put(messageArg.getId(), jobCallback);
        bgThreadHandler.sendEmptyMessage(1);
    }

    private class ThreadHandler extends Thread {

        @Override
        public void run() {
            this.setName(ThreadHandler.class.getName());
            this.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            Looper.prepare();
            bgThreadHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    String jobType = messageArg.getJobType();
                    bundle.putString("job", jobType);
                    bundle.putString("id", messageArg.getId());
                    if (jobType == null) {
                        return;
                    }
                    switch (jobType) {
                        case "fetchImages":
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            bundle.putSerializable("fetchImages", "");
                            break;
                        case "allImages":
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                        case "onProgress":
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                    message.setData(bundle);
                    mainUIHandler.sendMessage(message);
                }
            };
            Looper.loop();
        }
    }

    private static class MainUIThread extends Handler {
        MainUIThread() {
            super(Looper.getMainLooper());
            Log.d("JobHandler", "Initiate Job Handler");
        }

        @Override
        public void handleMessage(Message msg) {
            String job = msg.getData().getString("job");
            JobCallBack jobCallBack = null;
            try {
                String id = msg.getData().getString("id");
                jobCallBack = jobCallBacksHashMap.remove(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (jobCallBack == null) {
                return;
            }
            JobResponse jobResponse = new JobResponse(job);
            jobResponse.setJobObject(msg.getData().getSerializable(job));
            jobCallBack.onTaskDone(jobResponse);
        }
    }
}