package com.metao.async.repository;

/**
 * Created by metao on 2/1/2017.
 */
public class RepositoryCallback<T> implements RepositoryCallbackInterface<T> {

    @Override
    public void onDownloadFinished(String urlAddress, T t) {
        throw new RuntimeException("You should override method onDownloadFinished()");
    }

    @Override
    public void onError(Throwable throwable) {
        throw new RuntimeException("You should override method onError(Throwable throwable)");
    }

    @Override
    public void onDownloadProgress(String urlAddress, double progress) {

    }
}
