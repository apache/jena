/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.tdb2.setup;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.sys.Names;
import org.apache.jena.tdb2.ConfigTest;
import org.apache.jena.tdb2.params.StoreParams;
import org.apache.jena.tdb2.params.StoreParamsCodec;
import org.apache.jena.tdb2.sys.DatabaseConnection;
import org.apache.jena.tdb2.sys.DatabaseOps;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This test suite uses on-disk structures, does a lot of clean/create/sync
 * calls and can be noticeably slow.
 */
public class TestStoreParamsCreate {
    // This may be different for each test
    private final String DB_DIR = ConfigTest.getCleanDir();
    private final Path dbContainer = Path.of(DB_DIR);
    private final Path dbStorage = dbContainer.resolve(DatabaseOps.dbPrefix+DatabaseOps.SEP+DatabaseOps.startCount);

    private final Path cfgContainer = dbContainer.resolve(Names.TDB_CONFIG_FILE);
    private final Path cfgStorage = dbStorage.resolve(Names.TDB_CONFIG_FILE);

    private final Location locContainer = Location.create(dbContainer);
    private final Location locStorage = Location.create(dbStorage);

    static final StoreParams pApp = StoreParams.getSmallStoreParams();
    static final StoreParams pDft = StoreParams.getDftStoreParams();
    static final StoreParams pSpecial = StoreParams.builder("pApp", pApp)
        .blockSize(1024)
        .blockReadCacheSize(4)
        .build();

    // May be created during a test.
    private DatabaseConnection dbConnection = null;

    private void expel() {
        expel(dbConnection);
        dbConnection = null;
    }

    private void expel(DatabaseConnection dbc) {
        if ( dbc != null ) {
            TDBInternal.expel(dbc.getDatasetGraph());
        }
    }

    @Before public void initTestEnv() {
        TDBInternal.reset();
        FileOps.clearAll(locContainer.getDirectoryPath());
    }

    @After public void clearTestEnv() {
        TDBInternal.reset();
        FileOps.clearAll(locContainer.getDirectoryPath());
    }

    @Test public void params_create_01() {
        dbConnection = DatabaseConnection.connectCreate(locContainer);
        // Check.  Default setup, no params.
        assertTrue("DB directory", Files.exists(dbContainer));
        assertFalse("Config file unexpectedly found (container)", Files.exists(cfgContainer));
        assertFalse("Config file unexpectedly found (storage)", Files.exists(cfgStorage));
    }

    @Test public void params_create_02() {
        dbConnection = DatabaseConnection.connectCreate(locContainer, pApp);
        // Check.  Custom setup.
        assertTrue("DB directory", Files.exists(dbContainer));
        assertTrue("Config file not found", Files.exists(cfgContainer));
        StoreParams pLoc = StoreParamsCodec.read(locContainer);
        assertTrue(StoreParams.sameValues(pLoc, pApp));
    }

    // Defaults
    @Test public void params_reconnect_01() {
        // Create.
        dbConnection = DatabaseConnection.connectCreate(locContainer);
        // Drop.
        expel();
        // Reconnect
        dbConnection = DatabaseConnection.connectCreate(locContainer);
        StoreParams pLoc = StoreParamsCodec.read(locContainer);
        assertNull(pLoc);

        StoreParams pDB = TDBInternal.getDatasetGraphTDB(dbConnection.getDatasetGraph()).getStoreParams();
        assertNotNull(pDB);
        // Should be the default setup.
        assertTrue(StoreParams.sameValues(pDft, pDB));
    }

    // Defaults, then reconnect with app modified.
    @Test public void params_reconnect_02() {
        // Create.
        dbConnection = DatabaseConnection.connectCreate(locContainer);
        // Drop.
        expel();
        // Reconnect
        dbConnection = DatabaseConnection.connectCreate(locContainer, pSpecial);

        StoreParams pDB = TDBInternal.getDatasetGraphTDB(dbConnection.getDatasetGraph()).getStoreParams();
        assertNotNull(pDB);
        // Should be the default setup, modified by pApp for cache sizes.
        assertFalse(StoreParams.sameValues(pDft, pDB));
        assertFalse(StoreParams.sameValues(pSpecial, pDB));

        // Check it's default-modified-by-special.
        assertEquals(pSpecial.getBlockReadCacheSize(), pDB.getBlockReadCacheSize());
        assertNotEquals(pDft.getBlockReadCacheSize(), pDB.getBlockReadCacheSize());

        assertNotEquals(pSpecial.getBlockSize(), pDB.getBlockSize());
        assertEquals(pDft.getBlockSize(), pDB.getBlockSize());
    }

    // Custom, then reconnect with some special settings.
    @Test public void params_reconnect_03() {
        // Create.
        dbConnection = DatabaseConnection.connectCreate(locContainer, pApp);
        // Drop.
        expel();
        // Reconnect
        dbConnection = DatabaseConnection.connectCreate(locContainer, pSpecial);
        StoreParams pDB = TDBInternal.getDatasetGraphTDB(dbConnection.getDatasetGraph()).getStoreParams();
        assertNotNull(pDB);

        // Should be the default setup, modified by pApp for cache sizes.
        assertFalse(StoreParams.sameValues(pApp, pDB));
        assertFalse(StoreParams.sameValues(pSpecial, pDB));

        // Check it's default-modified-by-special.
        assertEquals(pSpecial.getBlockReadCacheSize(), pDB.getBlockReadCacheSize());
        assertNotEquals(pApp.getBlockReadCacheSize(), pDB.getBlockReadCacheSize());

        assertNotEquals(pSpecial.getBlockSize(), pDB.getBlockSize());
        assertEquals(pApp.getBlockSize(), pDB.getBlockSize());

        assertFalse(StoreParams.sameValues(pApp, pDB));
        assertFalse(StoreParams.sameValues(pSpecial, pDB));
    }
}

