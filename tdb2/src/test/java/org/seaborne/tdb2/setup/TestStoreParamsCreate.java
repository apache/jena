/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.tdb2.setup;

import java.nio.file.Files ;
import java.nio.file.Path ;
import java.nio.file.Paths ;

import org.apache.jena.atlas.json.JSON ;
import org.apache.jena.atlas.json.JsonObject ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.FileOps ;
import org.junit.After ;
import org.junit.Before ;
import org.junit.Test ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.tdb2.ConfigTest ;
import org.seaborne.tdb2.setup.StoreParams ;
import org.seaborne.tdb2.setup.StoreParamsCodec ;
import org.seaborne.tdb2.setup.StoreParamsConst ;
import org.seaborne.tdb2.sys.StoreConnection ;

/**
 * This test suite uses on-disk structures, does a lot of clean/create/sync
 * calls and can be noticably slow.
 */
public class TestStoreParamsCreate extends BaseTest {
    private final String DB_DIR = ConfigTest.getCleanDir() ;
    private final Path db = Paths.get(DB_DIR) ;
    private final Path cfg = Paths.get(DB_DIR, StoreParamsConst.TDB_CONFIG_FILE) ;
    private final Location loc = Location.create(DB_DIR) ;
    
    static final StoreParams pApp = StoreParams.getSmallStoreParams() ; 
    static final StoreParams pDft = StoreParams.getDftStoreParams() ;
    static final StoreParams pSpecial = StoreParams.builder(pApp)
        .blockSize(1024)
        .blockReadCacheSize(4)
        .build();
    
    @Before public void clearupTest() { 
        // Flush and clean.
        StoreConnection.expel(loc, true) ;
        FileOps.clearAll(DB_DIR);
    }

    @After public void expelDatabase() { 
        StoreConnection.expel(loc, true) ;
    }
    
    @Test public void params_create_01() {
        StoreConnection.make(loc, null) ;
        // Check.  Default setup, no params.
        assertTrue("DB directory", Files.exists(db)) ;
        assertFalse("Config file unexpectedly found", Files.exists(cfg)) ;
    }
    
    @Test public void params_create_02() {
        StoreConnection.make(loc, pApp) ;
        // Check.  Custom setup.
        assertTrue("DB directory", Files.exists(db)) ;
        assertTrue("Config file not found", Files.exists(cfg)) ;
        StoreParams pLoc = StoreParamsCodec.read(loc) ;
        assertTrue(StoreParams.sameValues(pLoc, pApp)) ;
    }
    
    // Defaults
    @Test public void params_reconnect_01() { 
        // Create.
        StoreConnection.make(loc, null) ;
        // Drop.
        StoreConnection.expel(loc, true) ;
        // Reconnect
        StoreConnection.make(loc, null) ;
        StoreParams pLoc = StoreParamsCodec.read(loc) ;
        assertNull(pLoc) ;
        
        StoreParams pDB = StoreConnection.connectExisting(loc).getDatasetGraphTDB().getStoreParams() ;
        assertNotNull(pDB) ;
        // Should be the default setup.
        assertTrue(StoreParams.sameValues(pDft, pDB)) ;
    }
    
    // Defaults, then reconnect with app modified.
    @Test public void params_reconnect_02() { 
        // Create.
        StoreConnection.make(loc, null) ;
        // Drop.
        StoreConnection.expel(loc, true) ;
        // Reconnect
        StoreConnection.make(loc, pSpecial) ;
        //StoreParams pLoc = StoreParamsCodec.read(loc) ;
        //assertNotNull(pLoc) ;
        
        StoreParams pDB = StoreConnection.connectExisting(loc).getDatasetGraphTDB().getStoreParams() ;
        assertNotNull(pDB) ;
        // Should be the default setup, modified by pApp for cache sizes.
        assertFalse(StoreParams.sameValues(pDft, pDB)) ;
        assertFalse(StoreParams.sameValues(pSpecial, pDB)) ;

        // Check it's default-modified-by-special.
        assertEquals(pSpecial.getBlockReadCacheSize(), pDB.getBlockReadCacheSize()) ;
        assertNotEquals(pDft.getBlockReadCacheSize(), pDB.getBlockReadCacheSize()) ;
        
        assertNotEquals(pSpecial.getBlockSize(), pDB.getBlockSize()) ;
        assertEquals(pDft.getBlockSize(), pDB.getBlockSize()) ;
    }
    
    // Custom, then reconnect with some special settings.
    @Test public void params_reconnect_03() { 
        // Create.
        StoreConnection.make(loc, pApp) ;
        // Drop.
        StoreConnection.expel(loc, true) ;
        // Reconnect
        StoreConnection.make(loc, pSpecial) ;
        //StoreParams pLoc = StoreParamsCodec.read(loc) ;
        //assertNotNull(pLoc) ;
        
        StoreParams pDB = StoreConnection.connectExisting(loc).getDatasetGraphTDB().getStoreParams() ;
        assertNotNull(pDB) ;
        // Should be the default setup, modified by pApp for cache sizes.
        assertFalse(StoreParams.sameValues(pApp, pDB)) ;
        assertFalse(StoreParams.sameValues(pSpecial, pDB)) ;

        // Check it's default-modified-by-special.
        assertEquals(pSpecial.getBlockReadCacheSize(), pDB.getBlockReadCacheSize()) ;
        assertNotEquals(pApp.getBlockReadCacheSize(), pDB.getBlockReadCacheSize()) ;
        
        assertNotEquals(pSpecial.getBlockSize(), pDB.getBlockSize()) ;
        assertEquals(pApp.getBlockSize(), pDB.getBlockSize()) ;
    }

    
//    // Custom then modified.
//    @Test public void params_reconnect_03() { 
//        // Create.
//        StoreConnection.make(loc, pLoc) ;
//        // Drop.
//        StoreConnection.expel(loc, true) ;
//        // Reconnect
//        StoreConnection.make(loc, pApp) ;
//        StoreParams pLoc = StoreParamsCodec.read(loc) ;
//        assertFalse(StoreParams.sameValues(pApp, pLoc)) ;
//        assertFalse(StoreParams.sameValues(pApp, pLoc)) ;
//    }

    // Dataset tests

    static StoreParams read(Location location) {
        String fn = location.getPath(StoreParamsConst.TDB_CONFIG_FILE) ;
        JsonObject obj = JSON.read(fn) ;
        return StoreParamsCodec.decode(obj) ;
    }
}

