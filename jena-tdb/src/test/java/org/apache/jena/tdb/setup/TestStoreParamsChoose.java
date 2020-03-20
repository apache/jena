/**
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

package org.apache.jena.tdb.setup;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.tdb.ConfigTest ;
import org.apache.jena.tdb.base.file.Location ;
import org.junit.Test ;

//TestParamsCreate
/** This test suite uses on-disk structures and can be slow */ 
public class TestStoreParamsChoose extends BaseTest {
    private String DIR = ConfigTest.getCleanDir() ;
    
    static final StoreParams pApp = StoreParams.builder()
        .blockSize(12)              // Not dynamic
        .nodeMissCacheSize(12)      // Dynamic
        .build();
    static final StoreParams pLoc = StoreParams.builder()
        .blockSize(0)
        .nodeMissCacheSize(0).build();
    
    static final StoreParams pDft = StoreParams.getDftStoreParams() ;

    @Test public void params_choose_new_1() {
        StoreParams p = Build.decideStoreParams(Location.mem(), true, null, null, pDft) ;
        // New store, no pLoc, no pApp so pDft.
        assertTrue(StoreParams.sameValues(p, pDft)) ;
    }
    
    @Test public void params_choose_new_2() {
        StoreParams p = Build.decideStoreParams(Location.mem(), true, pApp, null, pDft) ;
        // New store, no pLoc, so pApp is the enire settings.
        assertEquals(12, p.getBlockSize().intValue()) ;
        assertTrue(StoreParams.sameValues(p, pApp)) ;
    }

    @Test public void params_choose_new_3() {
        StoreParams p = Build.decideStoreParams(Location.mem(), true, null, pLoc, pDft) ;
        // New store, pLoc, no pApp, so pLoc is the entire settings.
        assertEquals(0, p.getBlockSize().intValue()) ;
        assertTrue(StoreParams.sameValues(p, pLoc)) ;
    }

    @Test public void params_choose_new_4() {
        StoreParams p = Build.decideStoreParams(Location.mem(), true, pApp, pLoc, pDft) ;
        // New store, pLoc, no pApp, so pLoc is the entire settings.
        
        assertFalse(StoreParams.sameValues(p, pApp)) ;
        assertFalse(StoreParams.sameValues(p, pLoc)) ;
        assertFalse(StoreParams.sameValues(p, pDft)) ;
        
        assertEquals(0, p.getBlockSize().intValue()) ;
        assertEquals(12,  p.getNodeMissCacheSize().intValue()) ;
    }

    @Test public void params_choose_existing_1() {
        StoreParams p = Build.decideStoreParams(Location.mem(), false, null, null, pDft) ;
        // p is pDft.
        assertTrue(StoreParams.sameValues(p, pDft)) ;
    }

    @Test public void params_choose_existing_2() {
        StoreParams p = Build.decideStoreParams(Location.mem(), false, pApp, null, pDft) ;
        // p is pLoc modified by pApp
        assertFalse(StoreParams.sameValues(p, pApp)) ;
        assertFalse(StoreParams.sameValues(p, pDft)) ;
        // Existing store, no pLoc, so pDft is implicit pLoc and fixed the block size.  
        assertEquals(pDft.getBlockSize(), p.getBlockSize()) ;
        assertEquals(12, p.getNodeMissCacheSize().intValue()) ;
    }
    
    @Test public void params_choose_existing_3() {
        StoreParams p = Build.decideStoreParams(Location.mem(), false, null, pLoc, pDft) ;
        // p is pLoc
        assertTrue(StoreParams.sameValues(p, pLoc)) ;
        
    }

    @Test public void params_choose_existing_4() {
        StoreParams p = Build.decideStoreParams(Location.mem(), false, pApp, pLoc, pDft) ;
        // p is pLoc modifed by pApp.
        assertFalse(StoreParams.sameValues(p, pApp)) ;
        assertFalse(StoreParams.sameValues(p, pLoc)) ;
        assertFalse(StoreParams.sameValues(p, pDft)) ;
        
        assertEquals(0, p.getBlockSize().intValue()) ;
        assertEquals(12,  p.getNodeMissCacheSize().intValue()) ;
    }
    
    @Test public void params_choose_new_persist_1() {
        // new database, app defined.
        Location loc = Location.create(DIR) ;
        FileOps.clearAll(loc.getDirectoryPath());
        // Clear.
        StoreParams p = Build.decideStoreParams(loc, true, pApp, null, pDft) ;
        // Check location now has a pLoc.
        String fn = loc.getPath(StoreParamsConst.TDB_CONFIG_FILE) ;
        assertTrue(FileOps.exists(fn)) ;

        StoreParams pLoc2 = StoreParamsCodec.read(loc) ;
        assertTrue(StoreParams.sameValues(pLoc2, p)) ;
    }
    
    @Test public void params_choose_new_persist_2() {
        // new database, location defined.
        Location loc = Location.create(DIR) ;
        FileOps.clearAll(loc.getDirectoryPath());
        StoreParamsCodec.write(loc, pLoc); 
        
        // Clear.
        StoreParams p = Build.decideStoreParams(loc, true, null, pLoc, pDft) ;
        // Check location still has a pLoc.
        String fn = loc.getPath(StoreParamsConst.TDB_CONFIG_FILE) ;
        assertTrue(FileOps.exists(fn)) ;

        StoreParams pLoc2 = StoreParamsCodec.read(loc) ;
        assertTrue(StoreParams.sameValues(pLoc, p)) ;
    }

    @Test public void params_choose_new_persist_3() {
        // new database, location defined, application modified.
        Location loc = Location.create(DIR) ;
        FileOps.clearAll(loc.getDirectoryPath());
        StoreParamsCodec.write(loc, pLoc); 
        
        // Clear.
        StoreParams p = Build.decideStoreParams(loc, true, pApp, pLoc, pDft) ;
        // Check location still has a pLoc.
        String fn = loc.getPath(StoreParamsConst.TDB_CONFIG_FILE) ;
        assertTrue(FileOps.exists(fn)) ;

        StoreParams pLoc2 = StoreParamsCodec.read(loc) ;
        assertFalse(StoreParams.sameValues(pLoc, p)) ;
        assertEquals(0, p.getBlockSize().intValue()) ;  // Location
        assertEquals(12, p.getNodeMissCacheSize().intValue()) ;  // Application
    }

}

