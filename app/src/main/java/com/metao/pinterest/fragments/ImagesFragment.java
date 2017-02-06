
package com.metao.pinterest.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.metao.async.Repository;
import com.metao.async.RepositoryCallback;
import com.metao.pinterest.R;
import com.metao.pinterest.activities.DetailActivity;
import com.metao.pinterest.async.JobHandler;
import com.metao.pinterest.listeners.EndlessRecycleViewListener;
import com.metao.pinterest.listeners.OnItemClickListener;
import com.metao.pinterest.models.WebCam;
import com.metao.pinterest.views.adapters.ImageAdapter;
import tr.xip.errorview.ErrorView;

import java.util.ArrayList;
import java.util.List;

public class ImagesFragment extends Fragment {

    private static final String JSON_API = "http://webcam.xzn.ir/v5/webcams.php?id=com.metao.webcams&action=true&user_id=12";
    public static SparseArray<Bitmap> photoCache = new SparseArray<>(1);

    private JobHandler jobHandler;
    private String TAG = "ImagesFragment";
    private ImageAdapter mImageAdapter;
    private ArrayList<WebCam> mImages;
    private ArrayList<WebCam> mCurrentImages;
    private RecyclerView mImageRecycler;
    private ProgressBar mImagesProgress;
    private ErrorView mImagesErrorView;
    private static ImagesFragment instance;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private ImageAdapter imageAdapter;

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private OnItemClickListener recyclerRowClickListener = new OnItemClickListener() {

        @Override
        public void onClick(View v, int position) {
            WebCam selectedImage = mCurrentImages.get(position);
            Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
            detailIntent.putExtra("position", position);
            detailIntent.putExtra("selected_image", selectedImage);
            ImageView coverImage = (ImageView) v.findViewById(R.id.item_image_img);
            if (coverImage == null) {
                coverImage = (ImageView) ((View) v.getParent()).findViewById(R.id.item_image_img);
            }
            if (Build.VERSION.SDK_INT >= 21) {
                if (coverImage.getParent() != null) {
                    ((ViewGroup) coverImage.getParent()).setTransitionGroup(false);
                }
            }
            if (coverImage != null && coverImage.getDrawable() != null) {
                Bitmap bitmap = ((BitmapDrawable) coverImage.getDrawable()).getBitmap(); //ew
                if (bitmap != null && !bitmap.isRecycled()) {
                    photoCache.put(position, bitmap);
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), coverImage, "cover");
                    startActivity(detailIntent, options.toBundle());
                }
            }
        }
    };

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        jobHandler = new JobHandler();
        instance = this;
        showAll();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_images, container, false);
        mImageRecycler = (RecyclerView) rootView.findViewById(R.id.fragment_last_images_recycler);
        mImageRecycler.setHasFixedSize(true);
        mImageRecycler.setItemAnimator(new DefaultItemAnimator());
        staggeredGridLayoutManager = newStaggeredGridLayoutManager();
        EndlessRecycleViewListener endlessRecycleViewListener = new EndlessRecycleViewListener(staggeredGridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                onLoadMoreRequest(page, totalItemsCount);
            }
        };
        endlessRecycleViewListener.setVisibleThreshold(10);
        mImageRecycler.addOnScrollListener(endlessRecycleViewListener);
        mImageRecycler.setLayoutManager(staggeredGridLayoutManager);
        mImagesProgress = (ProgressBar) rootView.findViewById(R.id.fragment_images_progress);
        mImagesErrorView = (ErrorView) rootView.findViewById(R.id.fragment_images_error_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        mImageRecycler.setLayoutManager(gridLayoutManager);
        mImageRecycler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        final Repository<List<WebCam>> repository = new Repository<List<WebCam>>("ImageRepo") {
            static final int RAM_SIZE = 4 * 1024 * 1024;//4MiB

            @Override
            public RepositoryType repositoryType() {
                return RepositoryType.JSON;
            }

            @Override
            public int ramSize() {
                return RAM_SIZE;
            }
        };
        repository.addDownload(JSON_API
                , new RepositoryCallback<List<WebCam>>() {
                    @Override
                    public void onDownloadFinished(String urlAddress, List<WebCam> response) {
                        mImagesProgress.setVisibility(View.GONE);
                        mImageRecycler.setVisibility(View.VISIBLE);
                        mImagesErrorView.setVisibility(View.GONE);
                        mImageAdapter = new ImageAdapter((ArrayList<WebCam>) response);
                        mCurrentImages = (ArrayList<WebCam>) response;
                        mImageAdapter.setOnItemClickListener(recyclerRowClickListener);
                        mImageRecycler.setAdapter(mImageAdapter);
                    }

                    @Override
                    public void onError(Throwable error) {
                       /*/ if (error instanceof RetrofitError) {
                            RetrofitError e = (RetrofitError) error;
                            if (e.getKind() == RetrofitError.Kind.NETWORK) {
                                mImagesErrorView.setErrorTitle(R.string.error_network);
                                mImagesErrorView.setErrorSubtitle(R.string.error_network_subtitle);
                            } else if (e.getKind() == RetrofitError.Kind.HTTP) {
                                mImagesErrorView.setErrorTitle(R.string.error_server);
                                mImagesErrorView.setErrorSubtitle(R.string.error_server_subtitle);
                            } else {
                                mImagesErrorView.setErrorTitle(R.string.error_uncommon);
                                mImagesErrorView.setErrorSubtitle(R.string.error_uncommon_subtitle);
                            }
                        }
                        mImagesProgress.setVisibility(View.GONE);
                        mImageRecycler.setVisibility(View.GONE);
                        mImagesErrorView.setVisibility(View.VISIBLE);
                        mImagesErrorView.setOnRetryListener(new RetryListener() {
                            @Override
                            public void onRetry() {
                                showAll();
                            }
                        });*/
                    }
                });
        return rootView;
    }

    /**
     * a small helper class to update the adapter
     */
    private void showAll() {
        if (mImages != null) {
            updateAdapter(mImages);
        } else {
            mImagesProgress.setVisibility(View.VISIBLE);
            mImageRecycler.setVisibility(View.GONE);
            mImagesErrorView.setVisibility(View.GONE);
        }
    }

    private void fetchImages() {
        final Repository<List<WebCam>> repository = new Repository<List<WebCam>>("ImageRepo") {
            static final int RAM_SIZE = 4 * 1024 * 1024;//4MiB

            @Override
            public RepositoryType repositoryType() {
                return RepositoryType.JSON;
            }

            @Override
            public int ramSize() {
                return RAM_SIZE;
            }
        };
        repository.addDownload("http://192.168.1.3/webcams/webcams?id=com.metao.webcams&action=true&user_id=12"
                , new RepositoryCallback<List<WebCam>>() {
                    @Override
                    public void onDownloadFinished(String urlAddress, List<WebCam> response) {
                        mImagesProgress.setVisibility(View.GONE);
                        mImageRecycler.setVisibility(View.VISIBLE);
                        mImagesErrorView.setVisibility(View.GONE);
                        imageAdapter = new ImageAdapter((ArrayList<WebCam>) response);
                    }

                    @Override
                    public void onError(Throwable error) {

                    }
                });
    }

    private void updateAdapter(ArrayList<WebCam> images) {
        mCurrentImages = images;
        mImageAdapter.updateData(mCurrentImages);
    }

    @NonNull
    private StaggeredGridLayoutManager newStaggeredGridLayoutManager() {
        return new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
    }

    private void onLoadMoreRequest(int page, int totalItemsCount) {

    }

    public static void signalRefresh() {
        instance.fetchImages();
    }
}
