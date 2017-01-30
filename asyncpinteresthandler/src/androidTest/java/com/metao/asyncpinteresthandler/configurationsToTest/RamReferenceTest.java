package com.metao.asyncpinteresthandler.configurationsToTest;

import com.metao.asyncpinteresthandler.RepositoryTest;
import com.metao.asyncpinteresthandler.repository.RepositoryBuilder;
import com.metao.asyncpinteresthandler.testobjects.AbstractVehicle;

public class RamReferenceTest extends RepositoryTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        cache = new RepositoryBuilder<AbstractVehicle>(CACHE_NAME, TEST_APP_VERSION)
                .useReferenceInRam(RAM_MAX_SIZE, new SizeOfVehiculeForTesting())
                .build();
    }
}
