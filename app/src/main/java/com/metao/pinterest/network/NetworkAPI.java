package com.metao.pinterest.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.metao.pinterest.activities.CustomApplication;
import com.metao.pinterest.models.ImageList;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import io.reactivex.Observable;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;

import java.io.File;

public class NetworkAPI {
    public static final String ENDPOINT = "http://lanora.eu/projects/wallsplash/";
    private final WebService mWebService;

    public static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create(); //2015-01-18 15:48:56

    public NetworkAPI() {
        Cache cache = null;
        OkHttpClient okHttpClient = null;
        try {
            File cacheDir = new File(CustomApplication.getContext().getCacheDir().getPath(), "pictures.json");
            cache = new Cache(cacheDir, 10 * 1024 * 1024);
            okHttpClient = new OkHttpClient();
            okHttpClient.setCache(cache);
        } catch (Exception e) {
        }

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ENDPOINT)
                .setClient(new OkClient(okHttpClient))
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addHeader("Cache-Control", "public, max-age=" + 60 * 60 * 4);
                    }
                })
                .build();
        mWebService = restAdapter.create(WebService.class);
    }


    public interface WebService {
        @GET("/pictures")
        Observable<ImageList> listImages();
    }

    public Observable<ImageList> fetchImages() {
        return mWebService.listImages();
    }

}
