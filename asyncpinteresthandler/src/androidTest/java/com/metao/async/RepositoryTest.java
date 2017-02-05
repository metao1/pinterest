package com.metao.async;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * Created by metao on 2/1/2017.
 */
@RunWith(AndroidJUnit4.class)
public class RepositoryTest {

    @Test
    public void testBasicOperations() throws Exception {

        Repository<List<WebCamTest>> repository = new Repository<List<WebCamTest>>("build") {
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
        for (int i = 0; i < 1; i++) {
            repository.addDownload("http://192.168.1.3/webcams/webcams?id=com.metao.webcams&action=true&user_id=12"
                    , new RepositoryCallback<List<WebCamTest>>() {
                        @Override
                        public void onDownloadFinished(String urlAddress, List<WebCamTest> o) {
                            assertNotNull(o);
                            Assert.assertEquals(o.size(), 66);
                            Log.d("StringResult", o.get(0).getUrl());
                        }
                    });
        }

    }
}