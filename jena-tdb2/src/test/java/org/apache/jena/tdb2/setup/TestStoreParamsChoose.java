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
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.sys.Names;
import org.apache.jena.tdb2.ConfigTest;
import org.apache.jena.tdb2.params.StoreParams;
import org.apache.jena.tdb2.params.StoreParamsCodec;
import org.apache.jena.tdb2.params.StoreParamsFactory;
import org.junit.Test;

//TestParamsCreate
/** This test suite uses on-disk structures and can be slow */
public class TestStoreParamsChoose {
    private String DIR = ConfigTest.getCleanDir();

    // Junk values bu tmuist be different.
    static final StoreParams pApp = StoreParams.builder("App")
        .blockSize(12)              // Not dynamic, does not override.
        .nodeMissCacheSize(12)      // Dynamic
        .build();
    static final StoreParams pLocContainer = StoreParams.builder("Container")
        .blockSize(10)
        .nodeMissCacheSize(10).build();

    static final StoreParams pLocStorage = StoreParams.builder("Storage")
            .blockSize(20)
            .nodeMissCacheSize(20).build();


    static final StoreParams pDft = StoreParams.getDftStoreParams();

    @Test public void params_choose_new_1() {
        StoreParams p = StoreParamsFactory.decideStoreParams(Location.mem(), true, null, null, null, pDft);
        // New store, no pLoc, no pApp so pDft.
        assertTrue(StoreParams.sameValues(p, pDft));
    }

    @Test public void params_choose_new_2() {
        StoreParams p = StoreParamsFactory.decideStoreParams(Location.mem(), true, pApp, null, null, pDft);
        // New store, no pLoc, so pApp is the enire settings.
        assertEquals(12, p.getBlockSize().intValue());
        assertTrue(StoreParams.sameValues(p, pApp));
    }

    @Test public void params_choose_new_3() {
        StoreParams p = StoreParamsFactory.decideStoreParams(Location.mem(), true, null, null, pLocStorage, pDft);
        // New store, no container params, storage params, no pApp, so pLoc is the entire settings.
        assertEquals(20, p.getBlockSize().intValue());
        assertTrue(StoreParams.sameValues(p, pLocStorage));
    }

    @Test public void params_choose_new_3a() {
        StoreParams p = StoreParamsFactory.decideStoreParams(Location.mem(), true, null, pLocContainer, null, pDft);
        // New store, container params, no storage params, no pApp, so pLoc is the entire settings.
        assertEquals(10, p.getBlockSize().intValue());
        assertTrue(StoreParams.sameValues(p, pLocContainer));
    }

    @Test public void params_choose_new_4() {
        StoreParams p = StoreParamsFactory.decideStoreParams(Location.mem(), true, pApp, pLocContainer, pLocStorage, pDft);

        assertFalse(StoreParams.sameValues(p, pApp));
        assertFalse(StoreParams.sameValues(p, pLocStorage));
        assertFalse(StoreParams.sameValues(p, pDft));

        assertEquals(20, p.getBlockSize().intValue());
        assertEquals(12,  p.getNodeMissCacheSize().intValue());
    }

    @Test public void params_choose_existing_1() {
        StoreParams p = StoreParamsFactory.decideStoreParams(Location.mem(), false, null, null, null, pDft);
        // p is pDft.
        assertTrue(StoreParams.sameValues(p, pDft));
    }

    @Test public void params_choose_existing_2() {
        StoreParams p = StoreParamsFactory.decideStoreParams(Location.mem(), false, pApp, null, null, pDft);
        // p is pLoc modified by pApp
        assertFalse(StoreParams.sameValues(p, pApp));
        assertFalse(StoreParams.sameValues(p, pDft));
        // Existing store, no pLoc, so pDft is implicit pLoc and fixed the block size.
        assertEquals(pDft.getBlockSize(), p.getBlockSize());
        assertEquals(12, p.getNodeMissCacheSize().intValue());
    }

    @Test public void params_choose_existing_3() {
        StoreParams p = StoreParamsFactory.decideStoreParams(Location.mem(), false, null, null, pLocStorage, pDft);
        assertTrue(StoreParams.sameValues(p, pLocStorage));
    }

    @Test public void params_choose_existing_3a() {
        StoreParams p = StoreParamsFactory.decideStoreParams(Location.mem(), false, null, pLocContainer, null, pDft);
        assertTrue(StoreParams.sameValues(p, pLocContainer));
    }


    @Test public void params_choose_existing_4() {
        StoreParams p = StoreParamsFactory.decideStoreParams(Location.mem(), false, pApp, pLocContainer, pLocStorage, pDft);
        // p is pLocStorage modified by pApp.
        assertFalse(StoreParams.sameValues(p, pApp));
        assertFalse(StoreParams.sameValues(p, pLocStorage));
        assertFalse(StoreParams.sameValues(p, pDft));

        assertEquals(20, p.getBlockSize().intValue());
        assertEquals(12,  p.getNodeMissCacheSize().intValue());
    }

    @Test public void params_choose_existing_4a() {
        StoreParams p = StoreParamsFactory.decideStoreParams(Location.mem(), false, pApp, pLocContainer, null, pDft);
        // p is pLocContainer modified by pApp.
        assertFalse(StoreParams.sameValues(p, pApp));
        assertFalse(StoreParams.sameValues(p, pLocContainer));
        assertFalse(StoreParams.sameValues(p, pDft));

        assertEquals(10, p.getBlockSize().intValue());
        assertEquals(12,  p.getNodeMissCacheSize().intValue());
    }

    @Test public void params_choose_new_persist_1() {
        // new database, app defined.
        Location loc = Location.create(DIR);
        FileOps.clearAll(loc.getDirectoryPath());
        // Clear.
        StoreParams p = StoreParamsFactory.decideStoreParams(loc, true, pApp, null, null, pDft);
        // Check location now has a pLoc.
        String fn = loc.getPath(Names.TDB_CONFIG_FILE);
        assertTrue(FileOps.exists(fn));

        StoreParams pLoc2 = StoreParamsCodec.read(loc);
        assertTrue(StoreParams.sameValues(pLoc2, p));
    }

    @Test public void params_choose_new_persist_2a() {
        // new database, location defined.
        Location loc = Location.create(DIR);
        FileOps.clearAll(loc.getDirectoryPath());
        StoreParamsCodec.write(loc, pLocStorage);

        // Clear.
        StoreParams p = StoreParamsFactory.decideStoreParams(loc, true, null, null, pLocStorage, pDft);
        // Check location still has a pLoc.
        String fn = loc.getPath(Names.TDB_CONFIG_FILE);
        assertTrue(FileOps.exists(fn));

        StoreParams pLoc2 = StoreParamsCodec.read(loc);
        assertTrue(StoreParams.sameValues(pLocStorage, p));
    }

    @Test public void params_choose_new_persist_2b() {
        // new database, location defined.
        Location loc = Location.create(DIR);
        FileOps.clearAll(loc.getDirectoryPath());
        StoreParamsCodec.write(loc, pLocStorage);

        // Clear.
        StoreParams p = StoreParamsFactory.decideStoreParams(loc, true, null, pLocContainer, null, pDft);
        // Check location still has a pLoc.
        String fn = loc.getPath(Names.TDB_CONFIG_FILE);
        assertTrue(FileOps.exists(fn));

        StoreParams pLoc2 = StoreParamsCodec.read(loc);
        assertTrue(StoreParams.sameValues(pLocContainer, p));
    }

    @Test public void params_choose_new_persist_2() {
        // new database, location defined.
        Location loc = Location.create(DIR);
        FileOps.clearAll(loc.getDirectoryPath());
        StoreParamsCodec.write(loc, pLocStorage);

        // Clear.
        StoreParams p = StoreParamsFactory.decideStoreParams(loc, true, null, pLocContainer, pLocStorage, pDft);
        // Check location still has a pLoc.
        String fn = loc.getPath(Names.TDB_CONFIG_FILE);
        assertTrue(FileOps.exists(fn));

        StoreParams pLoc2 = StoreParamsCodec.read(loc);
        assertTrue(StoreParams.sameValues(pLocStorage, p));
    }

    @Test public void params_choose_new_persist_3() {
        // new database, location defined, application modified.
        Location loc = Location.create(DIR);
        FileOps.clearAll(loc.getDirectoryPath());
        StoreParamsCodec.write(loc, pLocStorage);

        // Clear.
        StoreParams p = StoreParamsFactory.decideStoreParams(loc, true, pApp, pLocContainer, pLocStorage, pDft);
        // Check location still has a pLoc.
        String fn = loc.getPath(Names.TDB_CONFIG_FILE);
        assertTrue(FileOps.exists(fn));

        StoreParams pLoc2 = StoreParamsCodec.read(loc);
        assertFalse(StoreParams.sameValues(pLocStorage, p));
        assertEquals(20, p.getBlockSize().intValue());  // Location
        assertEquals(12, p.getNodeMissCacheSize().intValue());  // Application
    }

}

