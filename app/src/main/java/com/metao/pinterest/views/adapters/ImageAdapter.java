package com.metao.pinterest.views.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.github.lzyzsd.circleprogress.CircleProgress;
import com.metao.async.repository.Repository;
import com.metao.async.repository.RepositoryCallbackInterface;
import com.metao.pinterest.R;
import com.metao.pinterest.listeners.OnItemClickListener;
import com.metao.pinterest.listeners.OnStatusClickListener;
import com.metao.pinterest.models.WebCam;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImagesViewHolder> {

    private Context mContext;
    private ArrayList<WebCam> mImages;
    private int mScreenWidth;
    private int mDefaultTextColor;
    private int mDefaultBackgroundColor;
    private int lastPosition = -1;

    private OnItemClickListener onItemClickListener;

    private Repository<Bitmap> repository = new Repository<Bitmap>("Image") {
        @Override
        public Repository.RepositoryType repositoryType() {
            return Repository.RepositoryType.BITMAP;
        }
    };

    public ImageAdapter() {
        // ready database data source to access tables
    }

    public ImageAdapter(ArrayList<WebCam> images) {
        this.mImages = images;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void updateData(final List<WebCam> images) {
        this.mImages.addAll(images);
        Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                notifyDataSetChanged();
            }
        };
        handler.post(r);
    }

    @Override
    public ImagesViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View rowView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_image, viewGroup, false);
        //set the mContext
        this.mContext = viewGroup.getContext();
        //get the colors
        mDefaultTextColor = mContext.getResources().getColor(R.color.text_without_palette);
        mDefaultBackgroundColor = mContext.getResources().getColor(R.color.image_without_palette);
        mScreenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        return new ImagesViewHolder(rowView, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(final ImagesViewHolder imagesViewHolder, int position) {
        final WebCam currentImage = mImages.get(position);
        if (currentImage == null) {
            return;
        }
        imagesViewHolder.imageAuthor.setText(currentImage.getName());
        imagesViewHolder.imageDate.setText(currentImage.getCreatedAt());
        imagesViewHolder.imageAuthor.setTextColor(mDefaultTextColor);
        imagesViewHolder.imageDate.setTextColor(mDefaultTextColor);
        imagesViewHolder.imageTextContainer.setBackgroundColor(mDefaultBackgroundColor);
        imagesViewHolder.imageView.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.header));
        imagesViewHolder.imageView.setOnClickListener(imagesViewHolder);
        WebCam webCam = mImages.get(position);
        setAnimation(imagesViewHolder.imageView, position);
        repository.downloadBitmapIntoViewHolder(webCam.getThumbUrl(), imagesViewHolder);
        if (mImages.get(position) != null) {
            if (Build.VERSION.SDK_INT >= 21) {
                imagesViewHolder.imageView.setTransitionName("cover" + position);
            }
        }
    }

    private void setAnimation(ImageView viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.bottom_to_top);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return mImages.size();
    }
}

class ImagesViewHolder extends RecyclerView.ViewHolder implements RepositoryCallbackInterface<Bitmap>, View.OnClickListener {

    final FrameLayout imageTextContainer;
    final ImageView imageView;
    final TextView imageAuthor;
    final TextView imageDate;
    final ImageButton statusButton;
    private CircleProgress mFabProgress;
    private Drawable mDrawableClose;
    private Drawable mDrawableSuccess;
    private Drawable mDrawablePhoto;
    private Drawable mDrawableError;

    private final OnItemClickListener onItemClickListener;
    private String errorUrl;
    private OnStatusClickListener onStatusClickListener;

    public ImagesViewHolder(View itemView, OnItemClickListener onItemClickListener) {
        super(itemView);
        this.onItemClickListener = onItemClickListener;
        imageTextContainer = (FrameLayout) itemView.findViewById(R.id.item_image_text_container);
        imageView = (ImageView) itemView.findViewById(R.id.item_image_img);
        imageAuthor = (TextView) itemView.findViewById(R.id.item_image_author);
        imageDate = (TextView) itemView.findViewById(R.id.item_image_date);
        statusButton = (ImageButton) itemView.findViewById(R.id.status_fab);
        statusButton.setOnClickListener(this);
        mDrawableClose = new IconicsDrawable(itemView.getContext()
                , FontAwesome.Icon.faw_close).color(Color.WHITE).sizeDp(14);
        mDrawableSuccess = new IconicsDrawable(itemView.getContext()
                , FontAwesome.Icon.faw_check).color(Color.WHITE).sizeDp(24);
        mDrawableError = new IconicsDrawable(itemView.getContext()
                , FontAwesome.Icon.faw_exclamation).color(Color.WHITE).sizeDp(24);
        mDrawablePhoto = new IconicsDrawable(itemView.getContext()
                , FontAwesome.Icon.faw_photo).color(Color.WHITE).sizeDp(24);
        initDownload(itemView);
    }

    private void initDownload(View view) {
        // Fab progress
        mFabProgress = (CircleProgress) view.findViewById(R.id.activity_detail_progress);
        mFabProgress.setMax(100);
        mFabProgress.setScaleX(1);
        mFabProgress.setScaleY(1);
        mFabProgress.setProgress(10);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.status_fab && errorUrl != null) {
            this.onStatusClickListener.onClick(v, errorUrl);
        } else {
            onItemClickListener.onClick(v, getPosition());
        }
    }

    void setOnStatusClickListener(OnStatusClickListener onStatusClickListener) {
        this.onStatusClickListener = onStatusClickListener;
    }

    @Override
    public void onDownloadFinished(String taskId, Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
        /**
         * To tell the adapter that we done with data
         */
        mFabProgress.setVisibility(View.GONE);
    }

    @Override
    public void onError(Throwable throwable) {
        this.errorUrl = errorUrl;
        statusButton.setVisibility(View.VISIBLE);
        statusButton.setImageDrawable(mDrawableError);
        mFabProgress.setVisibility(View.GONE);
    }

    @Override
    public void onDownloadProgress(String taskId, double progress) {
        int intProgress = (int) (progress);
        if (intProgress < 1) {
            intProgress = intProgress + 1;
        }
        if (mFabProgress.getProgress() < progress) {
            mFabProgress.setProgress(intProgress);
        }
    }
}
