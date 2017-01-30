package com.metao.asyncpinteresthandler;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import com.metao.asyncpinteresthandler.repository.CacheSerializer;
import com.metao.asyncpinteresthandler.repository.JsonSerializer;
import com.metao.asyncpinteresthandler.repository.Repository;
import com.metao.asyncpinteresthandler.repository.SizeOf;
import com.metao.asyncpinteresthandler.testobjects.AbstractVehicle;
import com.metao.asyncpinteresthandler.testobjects.CoolBike;
import com.metao.asyncpinteresthandler.testobjects.CoolCar;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public abstract class RepositoryTest {

    protected static final int RAM_MAX_SIZE = 1000;
    protected static final String CACHE_NAME = "test";
    protected static final int TEST_APP_VERSION = 0;
    protected Repository<AbstractVehicle> cache;
    protected CacheSerializer<AbstractVehicle> defaultCacheSerializer;
    private Context context;

    protected Context getContext() {
        return context;
    }

    @Before
    public void setUp() throws Exception {
        defaultCacheSerializer = new JsonSerializer<>(AbstractVehicle.class);
        context = InstrumentationRegistry.getTargetContext();
    }

    @After
    public void tearDown() throws Exception {
        cache.invalidate();
    }

    @Test
    public void testBasicOperations() throws Exception {
        CoolCar car = new CoolCar();
        String keyCar = "car";
        cache.put(keyCar, car);
        assertEquals(car, cache.get(keyCar));
        assertEquals(true, cache.contains(keyCar));

        cache.invalidateRAM();
        assertEquals(car, cache.get(keyCar));
        assertEquals(true, cache.contains(keyCar));

        cache.put(keyCar, car);
        assertEquals(car, cache.get(keyCar));
        assertEquals(true, cache.contains(keyCar));
        cache.invalidate();

        CoolBike bike = new CoolBike();
        cache.put(keyCar, car);
        String keyBike = "bike";
        cache.put(keyBike, bike);

        assertEquals(cache.get(keyCar), car);
        assertEquals(true, cache.contains(keyCar));
        assertEquals(cache.get(keyBike), bike);
        assertEquals(true, cache.contains(keyBike));
    }

    @Test
    public void testBasicOperations2() throws Exception {
        CoolCar car = new CoolCar();
        String keyCar = "car";
        cache.put(keyCar, car);
        cache.invalidateRAM();

        assertEquals(car, cache.get(keyCar));
        assertEquals(true, cache.contains(keyCar));
        cache.invalidateRAM();

        cache.put(keyCar, car);
        cache.invalidateRAM();

        assertEquals(car, cache.get(keyCar));
        assertEquals(true, cache.contains(keyCar));

        assertEquals(true, cache.contains(keyCar));

        CoolBike bike = new CoolBike();
        String keyBike = "bike";
        cache.put(keyCar, car);
        cache.put(keyBike, bike);
        cache.delete(keyCar);
        cache.delete(keyBike);
        assertEquals(false, cache.contains(keyCar));
        assertEquals(false, cache.contains(keyBike));
    }

    @Test
    public void testLRUPolicy() {
        cache.invalidate();
        CoolCar carToEvict = new CoolCar();
        String keyCar = "car";
        cache.put(keyCar, carToEvict);
        long size = cache.getRamUsedInBytes();
        int numberOfItemsToAddForRAMEviction = (int) (RAM_MAX_SIZE / size);
        for (int i = 0; i < numberOfItemsToAddForRAMEviction; i++) {
            cache.put(keyCar + i, new CoolCar());
        }

        assertEquals(true, cache.contains(keyCar));

        cache.put(keyCar, carToEvict);
        for (int i = 0; i < numberOfItemsToAddForRAMEviction; i++) {
            cache.put(keyCar + i, new CoolCar());
        }
        assertEquals(true, cache.contains(keyCar));
    }

    @Test
    public void testConcurrentAccess() {
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            threads.add(createWorkerThread(cache));
        }
        Log.d("repositorydebuglogti", "start worker threads");
        for (Thread thread : threads) {
            thread.start();
        }

        Log.d("repositorydebuglogti", "join done");
    }

    private Thread createWorkerThread(final Repository<AbstractVehicle> cache) {
        return new Thread() {
            int sMaxNumberOfRun = 1000;

            @Override
            public void run() {
                String key = "key";
                try {
                    int numberOfRun = 0;
                    while (numberOfRun++ < sMaxNumberOfRun) {
                        Thread.sleep((long) (Math.random() * 2));
                        double choice = Math.random();
                        cache.put(key, new CoolCar());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public static class SerializerForTesting implements CacheSerializer<AbstractVehicle> {

        @Override
        public AbstractVehicle fromString(String data) {
            if (data.equals(CoolBike.class.getSimpleName())) {
                return new CoolBike();
            } else if (data.equals(CoolCar.class.getSimpleName())) {
                return new CoolCar();
            } else {
                return null;
            }
        }

        @Override
        public String toString(AbstractVehicle object) {
            return object.getClass().getSimpleName();
        }
    }

    public static class SizeOfVehiculeForTesting implements SizeOf<AbstractVehicle> {

        @Override
        public int sizeOf(AbstractVehicle object) {
            int size = 0;
            size += object.getName().length() * 2; // we suppose that char = 2 bytes
            size += 4; // we suppose that int = 4 bytes
            return size;
        }
    }
}
