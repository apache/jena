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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.sys.Names;
import org.apache.jena.tdb2.ConfigTest;
import org.apache.jena.tdb2.params.StoreParams;
import org.apache.jena.tdb2.params.StoreParamsCodec;
import org.apache.jena.tdb2.sys.StoreConnection;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This test suite uses on-disk structures, does a lot of clean/create/sync
 * calls and can be noticeably slow.
 */
public class TestStoreParamsCreate {
    // These tests work on storage locations.
    private final String DB_DIR = ConfigTest.getCleanDir();
    private final Path db = Path.of(DB_DIR);
    private final Path cfg = Path.of(DB_DIR, Names.TDB_CONFIG_FILE);
    private final Location loc = Location.create(DB_DIR);

    static final StoreParams pApp = StoreParams.getSmallStoreParams();
    static final StoreParams pDft = StoreParams.getDftStoreParams();
    static final StoreParams pSpecial = StoreParams.builder("pApp", pApp)
        .blockSize(1024)
        .blockReadCacheSize(4)
        .build();

    private void expel() {
        StoreConnection.internalExpel(loc, true);
    }

    @Before public void clearupTest() {
        // Flush and clean.
        TDBInternal.reset();
        FileOps.clearAll(loc.getDirectoryPath());
    }

    @After public void expelDatabase() {
        expel();
    }

    @Test public void params_create_01() {
        StoreConnection.connectCreate(loc);
        // Check.  Default setup, no params.
        assertTrue("DB directory", Files.exists(db));
        assertFalse("Config file unexpectedly found", Files.exists(cfg));
    }

    @Test public void params_create_02() {
        StoreConnection.connectCreate(loc, pApp);
        // Check.  Custom setup.
        assertTrue("DB directory", Files.exists(db));
        assertTrue("Config file not found", Files.exists(cfg));
        StoreParams pLoc = StoreParamsCodec.read(loc);
        assertTrue(StoreParams.sameValues(pLoc, pApp));
    }

    // Defaults
    @Test public void params_reconnect_01() {
        // Create.
        StoreConnection.connectCreate(loc);
        // Drop.
        expel();
        // Reconnect
        StoreConnection.connectCreate(loc, null);
        StoreParams pLoc = StoreParamsCodec.read(loc);
        assertNull(pLoc);

        StoreParams pDB = StoreConnection.connectExisting(loc).getDatasetGraphTDB().getStoreParams();
        assertNotNull(pDB);
        // Should be the default setup.
        assertTrue(StoreParams.sameValues(pDft, pDB));
    }

    // Defaults, then reconnect with app modified.
    @Test public void params_reconnect_02() {
        // Create.
        StoreConnection.connectCreate(loc, null);
        // Drop.
        expel();
        // Reconnect
        StoreConnection.connectCreate(loc, pSpecial);
        //StoreParams pLoc = StoreParamsCodec.read(loc);
        //assertNotNull(pLoc);

        StoreParams pDB = StoreConnection.connectExisting(loc).getDatasetGraphTDB().getStoreParams();
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
        StoreConnection.connectCreate(loc, pApp);
        // Drop.
        expel();
        // Reconnect
        StoreConnection.connectCreate(loc, pSpecial);
        //StoreParams pLoc = StoreParamsCodec.read(loc);
        //assertNotNull(pLoc);

        StoreParams pDB = StoreConnection.connectExisting(loc).getDatasetGraphTDB().getStoreParams();
        assertNotNull(pDB);
        // Should be the default setup, modified by pApp for cache sizes.
        assertFalse(StoreParams.sameValues(pApp, pDB));
        assertFalse(StoreParams.sameValues(pSpecial, pDB));

        // Check it's default-modified-by-special.
        assertEquals(pSpecial.getBlockReadCacheSize(), pDB.getBlockReadCacheSize());
        assertNotEquals(pApp.getBlockReadCacheSize(), pDB.getBlockReadCacheSize());

        assertNotEquals(pSpecial.getBlockSize(), pDB.getBlockSize());
        assertEquals(pApp.getBlockSize(), pDB.getBlockSize());
    }


//    // Custom then modified.
//    @Test public void params_reconnect_03() {
//        // Create.
//        StoreConnection.connectCreate(loc, pLoc);
//        // Drop.
//        StoreConnection.expel(loc, true);
//        // Reconnect
//        StoreConnection.connectCreate(loc, pApp);
//        StoreParams pLoc = StoreParamsCodec.read(loc);
//        assertFalse(StoreParams.sameValues(pApp, pLoc));
//        assertFalse(StoreParams.sameValues(pApp, pLoc));
//    }

    // Dataset tests

    static StoreParams read(Location location) {
        String fn = location.getPath(Names.TDB_CONFIG_FILE);
        JsonObject obj = JSON.read(fn);
        return StoreParamsCodec.decode(obj);
    }
}

