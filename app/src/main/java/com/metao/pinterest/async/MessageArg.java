package com.metao.pinterest.async;

import android.app.Activity;
import android.content.Context;

/**
 * Created by metao on 1/2/2017.
 */
public class MessageArg {

    private final String id;
    private String jobType;
    private Object object;
    private Context context;
    private Activity activity;

    public MessageArg(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
