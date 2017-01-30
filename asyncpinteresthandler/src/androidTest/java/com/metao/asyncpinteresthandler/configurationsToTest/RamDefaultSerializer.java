package com.metao.asyncpinteresthandler.configurationsToTest;

import com.metao.asyncpinteresthandler.RepositoryTest;
import com.metao.asyncpinteresthandler.repository.RepositoryBuilder;
import com.metao.asyncpinteresthandler.testobjects.AbstractVehicle;

public class RamDefaultSerializer extends RepositoryTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        cache = new RepositoryBuilder<AbstractVehicle>(CACHE_NAME, TEST_APP_VERSION)
                .useSerializerInRam(RAM_MAX_SIZE, defaultCacheSerializer)
                .build();
    }
}
