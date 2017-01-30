package com.metao.pinterest.async;

/**
 * Created by metao on 1/2/2017.
 */
public interface Job {
    public void execute(MessageArg messageArg,JobCallBack jobCallback);
}
