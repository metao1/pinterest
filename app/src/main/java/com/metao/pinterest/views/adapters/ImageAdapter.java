package com.metao.pinterest.views.adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.github.lzyzsd.circleprogress.CircleProgress;
import com.metao.pinterest.R;
import com.metao.pinterest.listeners.OnItemClickListener;
import com.metao.pinterest.models.Image;
import com.metao.pinterest.other.BitmapConverter;
import com.metao.pinterest.other.PaletteTransformation;
import com.metao.pinterest.other.Utils;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImagesViewHolder> {

    private Context mContext;
    private ArrayList<Image> mImages;
    private int mScreenWidth;
    private int mDefaultTextColor;
    private int mDefaultBackgroundColor;
    private OnItemClickListener onItemClickListener;

    public ImageAdapter() {
        // ready database data source to access tables
    }

    public ImageAdapter(ArrayList<Image> images) {
        this.mImages = images;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void updateData(ArrayList<Image> images) {
        this.mImages = images;
        notifyDataSetChanged();
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

    public void addImage(Image image) {
        mImages.add(image);
        updateData(mImages);
    }

    public void setProgress(String taskId, double progress) {
        for (int i = 0; i < mImages.size(); i++) {
            if (mImages.get(i).getTaskId().equalsIgnoreCase(taskId)) {
                mImages.get(i).setProgress(progress);
                notifyItemChanged(i);
            }
        }
    }

    @Override
    public void onBindViewHolder(final ImagesViewHolder imagesViewHolder, final int position) {

        final Image currentImage = mImages.get(position);
        imagesViewHolder.imageAuthor.setText(currentImage.getAuthor());
        imagesViewHolder.imageDate.setText(currentImage.getReadableModified_Date());
        imagesViewHolder.imageAuthor.setTextColor(mDefaultTextColor);
        imagesViewHolder.imageDate.setTextColor(mDefaultTextColor);
        imagesViewHolder.imageTextContainer.setBackgroundColor(mDefaultBackgroundColor);
        imagesViewHolder.onProgress(currentImage.getProgress(), 100);
        if (mImages.get(position).bitmap == null) {
            imagesViewHolder.imageView
                    .setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.header));
        } else {
            imagesViewHolder.imageTextContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onClick(v, position);
                }
            });
            imagesViewHolder.imageView.setOnClickListener(imagesViewHolder);
            imagesViewHolder.imageView.setImageBitmap(BitmapConverter.getImage(mImages.get(position).bitmap));
            if (mImages.get(position).bitmap != null) {
                Palette palette = PaletteTransformation.getPalette(imagesViewHolder.imageView.getDrawingCache());
                if (palette != null) {
                    Palette.Swatch s = palette.getVibrantSwatch();
                    if (s == null) {
                        s = palette.getDarkVibrantSwatch();
                    }
                    if (s == null) {
                        s = palette.getLightVibrantSwatch();
                    }
                    if (s == null) {
                        s = palette.getMutedSwatch();
                    }

                    if (s != null && position >= 0 && position < mImages.size()) {
                        if (mImages.get(position) != null) {
                            mImages.get(position).setSwatch(s);
                        }

                        imagesViewHolder.imageAuthor.setTextColor(s.getTitleTextColor());
                        imagesViewHolder.imageDate.setTextColor(s.getTitleTextColor());
                        Utils.animateViewColor(imagesViewHolder.imageTextContainer, mDefaultBackgroundColor, s.getRgb());
                    }
                }

            }
            if (Build.VERSION.SDK_INT >= 21) {
                imagesViewHolder.imageView.setTransitionName("cover" + position);
            }
        }
        DisplayMetrics displaymetrics = mContext.getResources().getDisplayMetrics();
        int finalHeight = (int) (displaymetrics.widthPixels / currentImage.getRatio());
        imagesViewHolder.imageView.setMinimumHeight(finalHeight);
    }

    @Override
    public int getItemCount() {
        return mImages.size();
    }
}

class ImagesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    protected final FrameLayout imageTextContainer;
    protected final ImageView imageView;
    protected final TextView imageAuthor;
    protected final TextView imageDate;
    private ImageView mFabButton;
    private CircleProgress mFabProgress;
    private Drawable mDrawableClose;
    private Drawable mDrawableSuccess;
    private Drawable mDrawablePhoto;
    private Drawable mDrawableError;

    private final OnItemClickListener onItemClickListener;

    public ImagesViewHolder(View itemView, OnItemClickListener onItemClickListener) {
        super(itemView);
        this.onItemClickListener = onItemClickListener;
        imageTextContainer = (FrameLayout) itemView.findViewById(R.id.item_image_text_container);
        imageView = (ImageView) itemView.findViewById(R.id.item_image_img);
        imageAuthor = (TextView) itemView.findViewById(R.id.item_image_author);
        imageDate = (TextView) itemView.findViewById(R.id.item_image_date);
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

    /**
     * A test to tell the adapter that we done with data
     */
    private void onDone() {
        mFabProgress.setVisibility(View.GONE);
        mFabButton.setVisibility(View.GONE);

    }

    public void onProgress(double downloaded, long total) {
        int progress = (int) (downloaded * 100.0 / total);
        if (progress < 1) {
            progress = progress + 1;
        }
        if (downloaded == total) {
            mFabProgress.setProgress(progress);
        }
    }

    private void initDownload(View view) {
        // Fab progress
        mFabProgress = (CircleProgress) view.findViewById(R.id.activity_detail_progress);
        mFabProgress.setMax(100);
        mFabProgress.setScaleX(1);
        mFabProgress.setScaleY(2);
        mFabProgress.setProgress(40);
        // Fab button
        mFabButton = (ImageView) view.findViewById(R.id.activity_detail_fab);
        mFabButton.setScaleX(1);
        mFabButton.setScaleY(2);
        mFabButton.setImageDrawable(mDrawableClose);
    }

    @Override
    public void onClick(View v) {
        onItemClickListener.onClick(v, getPosition());
    }
}

