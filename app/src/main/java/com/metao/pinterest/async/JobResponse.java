package com.metao.pinterest.async;

import java.io.Serializable;

/**
 * Created by metao on 1/11/2017.
 */
public class JobResponse implements Serializable {
    private String jobType;
    private Object jobObject;

    public JobResponse(String jobType) {
        this.jobType = jobType;
    }

    public void setJobObject(Object jobObject) {
        this.jobObject = jobObject;
    }

    public String getJobType() {
        return jobType;
    }

    public Object getJobObject() {
        return jobObject;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }
}
