package com.metao.async.repository;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by metao on 2/13/2017.
 */
public abstract class ImageViewHolder extends RecyclerView.ViewHolder {
    public ImageViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void onDone();

    public abstract void onError();

    public abstract void onProgress(double progress);

    public abstract void setImageResult(Bitmap bitmap);
}
