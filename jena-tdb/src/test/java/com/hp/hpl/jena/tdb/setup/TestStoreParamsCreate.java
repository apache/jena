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

package com.hp.hpl.jena.tdb.setup;

import static com.hp.hpl.jena.tdb.setup.StoreParamsConst.TDB_CONFIG_FILE ;

import java.io.File ;
import java.io.FileOutputStream ;
import java.io.IOException ;
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

import com.hp.hpl.jena.tdb.StoreConnection ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.setup.StoreParams ;
import com.hp.hpl.jena.tdb.setup.StoreParamsCodec ;

//TestParamsCreate
/** This test suite uses on-diskstructures and can be slow */ 
public class TestStoreParamsCreate extends BaseTest {
    static String DB_DIR = "target/test/DB" ; 
    
    @Before public void clearAnyDatabase() {
        FileOps.clearAll(new File(DB_DIR)); 
    }

    @After public void clearupTest() {}

    
    @Test public void params_create_01() {
        Location loc = Location.create(DB_DIR) ;
        StoreConnection.make(loc, null) ;
        Path db = Paths.get(DB_DIR) ;
        assertTrue("DB directory", Files.exists(db)) ;
        Path dbCfg = db.resolve(TDB_CONFIG_FILE) ;
        // Fake it.
        try {
            new FileOutputStream(dbCfg.toFile()).close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        
        
        assertTrue("DB config file", Files.exists(dbCfg)) ;
        
    }
    
    // Create store.
    // Test params.
    
    static StoreParams read(Location location) {
        String fn = location.getPath(TDB_CONFIG_FILE) ;
        JsonObject obj = JSON.read(fn) ;
        return StoreParamsCodec.decode(obj) ;
    }
}

