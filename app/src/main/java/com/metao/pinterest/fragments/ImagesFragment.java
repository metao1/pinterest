
package com.metao.pinterest.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.metao.asyncpinteresthandler.appConstants.Helper;
import com.metao.asyncpinteresthandler.core.AsyncHandler;
import com.metao.asyncpinteresthandler.core.enums.QueueSort;
import com.metao.asyncpinteresthandler.database.elements.Task;
import com.metao.asyncpinteresthandler.report.listener.DownloadManagerListener;
import com.metao.asyncpinteresthandler.repository.Repository;
import com.metao.asyncpinteresthandler.repository.RepositoryBuilder;
import com.metao.pinterest.R;
import com.metao.pinterest.activities.DetailActivity;
import com.metao.pinterest.async.JobCallBack;
import com.metao.pinterest.async.JobHandler;
import com.metao.pinterest.async.JobResponse;
import com.metao.pinterest.async.MessageArg;
import com.metao.pinterest.listeners.EndlessRecycleViewListener;
import com.metao.pinterest.listeners.OnItemClickListener;
import com.metao.pinterest.models.Image;
import com.metao.pinterest.models.ImageList;
import com.metao.pinterest.views.adapters.AsyncHandlerSerializer;
import com.metao.pinterest.views.adapters.ImageAdapter;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit.RetrofitError;
import tr.xip.errorview.ErrorView;
import tr.xip.errorview.RetryListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class ImagesFragment extends Fragment {

    private static final String REPOSITORY_NAME = "AsyncHandler";
    public static SparseArray<Bitmap> photoCache = new SparseArray<>(1);

    private JobHandler jobHandler;
    private AsyncHandler asyncHandler;
    private String TAG = "ImagesFragment";
    private String URL = "http://avesty.com/img/programs/music.jpg";
    private ImageAdapter mImageAdapter;
    private ArrayList<Image> mImages;
    private ArrayList<Image> mCurrentImages;
    private RecyclerView mImageRecycler;
    private ProgressBar mImagesProgress;
    private ErrorView mImagesErrorView;
    private Repository<Task> repository;
    private ConcurrentHashMap<String, Image> imageConcurrentHashMap;
    private static ImagesFragment instance;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private StaggeredGridLayoutManager staggeredGridLayoutManager;

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }

    private OnItemClickListener recyclerRowClickListener = new OnItemClickListener() {

        @Override
        public void onClick(View v, int position) {
            Image selectedImage = mCurrentImages.get(position);
            Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
            detailIntent.putExtra("position", position);
            detailIntent.putExtra("selected_image", selectedImage);
            if (selectedImage.getSwatch() != null) {
                detailIntent.putExtra("swatch_title_text_color", selectedImage.getSwatch().getTitleTextColor());
                detailIntent.putExtra("swatch_rgb", selectedImage.getSwatch().getRgb());
            }

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
                    // Setup the transition to the detail activity
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), coverImage, "cover");
                    startActivity(detailIntent, options.toBundle());
                }
            }
        }
    };

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
        mImageAdapter = new ImageAdapter();
        mImageAdapter.setOnItemClickListener(recyclerRowClickListener);
        mImageRecycler.setAdapter(mImageAdapter);
        imageConcurrentHashMap = new ConcurrentHashMap<>();
        repository = new RepositoryBuilder<Task>(REPOSITORY_NAME, 1)
                .useReferenceInRam(1024 * 1000, new AsyncHandlerSerializer())
                .build();
        asyncHandler = new AsyncHandler(repository);//start async downloading
        showAll();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        jobHandler = new JobHandler();
        instance = this;
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
            fetchImages();
        }
    }

    private void fetchImages() {
        disposables.add(replaceImages()
                // Run on a background thread
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                // Be notified on the main thread
                .subscribeWith(new DisposableObserver<ImageList>() {
                    @Override
                    public void onNext(ImageList value) {
                        mImages = value.getData();
                        updateAdapter(mImages);
                    }

                    @Override
                    public void onError(Throwable error) {
                        if (error instanceof RetrofitError) {
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
                        });
                    }

                    @Override
                    public void onComplete() {
                        mImagesProgress.setVisibility(View.GONE);
                        mImageRecycler.setVisibility(View.VISIBLE);
                        mImagesErrorView.setVisibility(View.GONE);
                    }
                }));
    }

    private void updateAdapter(ArrayList<Image> images) {
        mCurrentImages = images;
        mImageAdapter.updateData(mCurrentImages);
    }

    private Observable<ImageList> replaceImages() {
        return Observable.defer(new Callable<ObservableSource<? extends ImageList>>() {
            @Override
            public ObservableSource<? extends ImageList> call() throws Exception {
                SystemClock.sleep(1000);
                final ImageList imageList = new ImageList();
                asyncHandler.init(4, new DownloadManagerListener() {
                    @Override
                    public void OnDownloadStarted(String taskId) {
                        Log.d(TAG, String.format("OnDownloadStarted of task %s", String.valueOf(taskId)));
                    }

                    @Override
                    public void OnDownloadPaused(String taskId) {
                        Log.d(TAG, String.format("OnDownloadPaused of task %s", String.valueOf(taskId)));
                    }

                    @Override
                    public void onDownloadProcess(final String taskId, final double percent, long downloadedLength) {
                        Log.d(TAG, String.format("onDownloadProcess of task %s1:%s2 ", String.valueOf(taskId), String.valueOf(percent)));
                        MessageArg messageArg = new MessageArg(UUID.randomUUID().toString());
                        messageArg.setJobType("onProgress");
                        messageArg.setActivity(getActivity());
                        jobHandler.execute(messageArg, new JobCallBack() {
                            @Override
                            public void onTaskDone(JobResponse addedResponse) {
                                Iterator<Task> taskIterator = repository.getRamCacheRepository().values().iterator();
                                while (taskIterator.hasNext()) {
                                    Task task = taskIterator.next();
                                    if (task.id.equalsIgnoreCase(taskId)) {
                                        mImageAdapter.setProgress(taskId, percent);
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void OnDownloadRebuildFinished(String taskId) {
                        Log.d(TAG, String.format("OnDownloadRebuildFinished of task %s", String.valueOf(taskId)));
                    }

                    @Override
                    public void OnDownloadFinished(final String taskId) {
                        Log.d(TAG, String.format("OnDownloadFinished of task %s", String.valueOf(taskId)));
                        MessageArg messageArg = new MessageArg(UUID.randomUUID().toString());
                        messageArg.setJobType("allImages");
                        messageArg.setActivity(getActivity());
                        jobHandler.execute(messageArg, new JobCallBack() {
                            @Override
                            public void onTaskDone(JobResponse addedResponse) {
                                Iterator<Task> taskIterator = repository.getRamCacheRepository().values().iterator();
                                ImageList imageList = new ImageList();
                                ArrayList<Image> images = new ArrayList<>();
                                while (taskIterator.hasNext()) {
                                    Task task = taskIterator.next();
                                    Image image = imageConcurrentHashMap.get(taskId);
                                    image.setBitmap(task.data);
                                    Log.d("ImageAdapter", String.valueOf(task.data));
                                    images.add(image);
                                }
                                imageList.setData(images);
                                updateAdapter(imageList.getData());
                            }
                        });
                    }

                    @Override
                    public void OnDownloadRebuildStart(String taskId) {
                        Log.d(TAG, String.format("OnDownloadRebuildStart of task %s", String.valueOf(taskId)));
                    }

                    @Override
                    public void OnDownloadCompleted(String taskId) {
                        Log.d(TAG, String.format("OnDownloadCompleted of task %s", String.valueOf(taskId)));
                    }

                    @Override
                    public void connectionLost(String taskId) {

                    }
                });
                ArrayList<Image> images = new ArrayList<>();
                for (int i = 0; i < 4; i++) {
                    Image image = new Image();
                    image.setAuthor("Loading...");
                    image.setImageSrc(URL);
                    String taskId = asyncHandler.addTask(REPOSITORY_NAME, Helper.createNewId()
                            , image.getUrl(), true, false);
                    image.setTaskId(taskId);
                    images.add(image);
                    imageConcurrentHashMap.put(taskId, image);
                }
                asyncHandler.startQueueDownload(10, QueueSort.earlierFirst);
                imageList.setData(images);
                return Observable.just(imageList);
            }
        });
    }

    @NonNull
    private StaggeredGridLayoutManager newStaggeredGridLayoutManager() {
        return new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
    }

    private void onLoadMoreRequest(int page, int totalItemsCount) {
        Log.d(TAG, "on load more");
        Image image = new Image();
        image.setAuthor("Loading...");
        image.setImageSrc(URL);
        String taskId = asyncHandler.addTask(REPOSITORY_NAME, Helper.createNewId()
                , image.getUrl(), true, false);
        image.setTaskId(taskId);
        imageConcurrentHashMap.put(taskId, image);
        mImageAdapter.addImage(image);
    }

    public static void signalRefresh() {
        instance.fetchImages();
    }
}
