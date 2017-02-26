
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.metao.async.repository.Repository;
import com.metao.async.repository.RepositoryCallback;
import com.metao.pinterest.R;
import com.metao.pinterest.activities.DetailActivity;
import com.metao.pinterest.listeners.EndlessRecycleViewListener;
import com.metao.pinterest.listeners.OnItemClickListener;
import com.metao.pinterest.models.WebCam;
import com.metao.pinterest.views.adapters.ImageAdapter;
import tr.xip.errorview.ErrorView;
import tr.xip.errorview.RetryListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImagesFragment extends Fragment {

    private static final String VPS = "http://webcam.xzn.ir/v5/webcams.php?id=com.metao.webcams&action=true&user_id=12";
    public static SparseArray<Bitmap> photoCache = new SparseArray<>(1);
    private String TAG = "ImagesFragment";
    private ImageAdapter mImageAdapter;
    private RecyclerView mImageRecycler;
    private ProgressBar mImagesProgress;
    private ErrorView mImagesErrorView;
    private static ImagesFragment instance;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private WebCam[] webCams;
    private int counter;

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private OnItemClickListener recyclerRowClickListener = new OnItemClickListener() {

        @Override
        public void onClick(View v, int position) {
            WebCam selectedImage = webCams[position];
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
                Bitmap bitmap = ((BitmapDrawable) coverImage.getDrawable()).getBitmap();
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
        mImageRecycler.setLayoutManager(staggeredGridLayoutManager);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mImageRecycler.getContext());
        mImageRecycler.setLayoutManager(linearLayoutManager);
        EndlessRecycleViewListener endlessRecycleViewListener = new EndlessRecycleViewListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                onLoadMoreRequest();
            }
        };
        mImageRecycler.addOnScrollListener(endlessRecycleViewListener);
        mImagesProgress = (ProgressBar) rootView.findViewById(R.id.fragment_images_progress);
        mImagesErrorView = (ErrorView) rootView.findViewById(R.id.fragment_images_error_view);
        mImageRecycler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        fetchImages();
        return rootView;
    }

    /**
     * a small helper class to update the adapter
     */
    private void showAll() {
        if (webCams != null) {
            updateAdapter(webCams);
        } else {
            mImagesProgress.setVisibility(View.VISIBLE);
            mImageRecycler.setVisibility(View.GONE);
            mImagesErrorView.setVisibility(View.GONE);
        }
    }

    private void fetchImages() {
        final Repository<ArrayList<WebCam>> repository = new Repository<ArrayList<WebCam>>("ImageRepo") {
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
        repository.addService(VPS, new RepositoryCallback<ArrayList<WebCam>>() {
            @Override
            public void onDownloadFinished(String urlAddress, ArrayList<WebCam> response) {
                mImagesProgress.setVisibility(View.GONE);
                mImageRecycler.setVisibility(View.VISIBLE);
                mImagesErrorView.setVisibility(View.GONE);
                webCams = new ArrayList<>(response).toArray(new WebCam[response.size()]);
                WebCam[] lowWebCams = Arrays.copyOfRange(webCams, 0, 5);
                List<WebCam> webCams = Arrays.asList(lowWebCams);
                mImageAdapter = new ImageAdapter(new ArrayList<>(webCams));
                mImageAdapter.setOnItemClickListener(recyclerRowClickListener);
                mImageRecycler.setAdapter(mImageAdapter);
            }

            @Override
            public void onError(Throwable error) {
                mImagesErrorView.setErrorTitle(R.string.error_network);
                mImagesErrorView.setErrorSubtitle(R.string.error_network_subtitle);
                mImagesProgress.setVisibility(View.GONE);
                mImageRecycler.setVisibility(View.GONE);
                mImagesErrorView.setVisibility(View.VISIBLE);
                mImagesErrorView.setOnRetryListener(new RetryListener() {
                    @Override
                    public void onRetry() {
                        showAll();
                    }
                });
            }
        });
    }

    private void updateAdapter(WebCam[] images) {
        mImageAdapter.updateData(Arrays.asList(images));
    }

    @NonNull
    private StaggeredGridLayoutManager newStaggeredGridLayoutManager() {
        return new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
    }

    private void onLoadMoreRequest() {
        WebCam[] lowWebCams;
        if (counter + 5 > webCams.length) {
            lowWebCams = Arrays.copyOfRange(webCams, counter, webCams.length);
        } else {
            counter += 5;
            lowWebCams = Arrays.copyOfRange(webCams, counter, counter + 5);
        }
        if (lowWebCams != null) {
            updateAdapter(lowWebCams);
        }
    }

    public static void signalRefresh() {
        instance.fetchImages();
    }
}
