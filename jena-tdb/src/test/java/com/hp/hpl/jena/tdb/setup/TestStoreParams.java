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

package com.hp.hpl.jena.tdb.setup;

import java.util.Objects ;

import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.base.block.FileMode ;

import org.apache.jena.atlas.json.JSON ;
import org.apache.jena.atlas.json.JsonObject ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.InternalErrorException ;
import org.junit.Test ;

public class TestStoreParams extends BaseTest {

    @Test public void store_params_01() {
        assertEqualsStoreParams(StoreParams.getDftStoreParams(), StoreParams.getDftStoreParams()) ; 
    }
    
    @Test public void store_params_02() {
        StoreParams sp = StoreParamsBuilder.create().build() ;
        assertEqualsStoreParams(StoreParams.getDftStoreParams(), sp) ; 
    }

    @Test public void store_params_03() {
        StoreParams params = StoreParamsBuilder.create().build() ;
        StoreParams params2 = roundTrip(params) ;
        assertEqualsStoreParams(params,params2) ;
    }
    
    // ----
    
    @Test public void store_params_04() {
        StoreParams params = StoreParamsBuilder.create().fileMode(FileMode.direct).blockSize(1024).build() ;
        StoreParams params2 = roundTrip(params) ;
        assertEqualsStoreParams(params,params2) ;
        assertEquals(params.getFileMode(), params2.getFileMode()) ;
        assertEquals(params.getBlockSize(), params2.getBlockSize()) ;
    }

    @Test public void store_params_05() {
        String xs = "{ \"tdb.block_size\": 2048 }" ;
        JsonObject x = JSON.parse(xs) ;
        StoreParams paramsExpected = StoreParamsBuilder.create().blockSize(2048).build() ;
        StoreParams paramsActual = StoreParamsCodec.decode(x) ;
        assertEqualsStoreParams(paramsExpected,paramsActual) ;
    }

    @Test public void store_params_06() {
        String xs = "{ \"tdb.file_mode\": \"direct\" , \"tdb.block_size\": 2048 }" ;
        JsonObject x = JSON.parse(xs) ;
        StoreParams paramsExpected = StoreParamsBuilder.create().blockSize(2048).fileMode(FileMode.direct).build() ;
        StoreParams paramsActual = StoreParamsCodec.decode(x) ;
        assertEqualsStoreParams(paramsExpected,paramsActual) ;
    }

    @Test public void store_params_07() {
        String xs = "{ \"tdb.triple_indexes\" : [ \"POS\" , \"PSO\"] } " ; 
        JsonObject x = JSON.parse(xs) ;
        StoreParams params = StoreParamsCodec.decode(x) ;
        String[] expected =  { "POS" , "PSO" } ;
        assertArrayEquals(expected, params.getTripleIndexes()) ;
    }

    @Test(expected=TDBException.class)
    public void store_params_08() {
        String xs = "{ \"tdb.triples_indexes\" : [ \"POS\" , \"PSO\"] } " ; // Misspelt. 
        JsonObject x = JSON.parse(xs) ;
        StoreParams params = StoreParamsCodec.decode(x) ;
        String[] expected =  { "POS" , "PSO" } ;
        assertArrayEquals(expected, params.getTripleIndexes()) ;
    }

    // --------
    
    private static StoreParams roundTrip(StoreParams params) {
        JsonObject obj = StoreParamsCodec.encodeToJson(params) ;
        StoreParams params2 = StoreParamsCodec.decode(obj) ;
        return params2 ;
    }
    
    private static void assertEqualsStoreParams(StoreParams params1, StoreParams params2) {
        assertTrue(same(params1, params2)) ;
    }
    
    private static boolean same(StoreParams params1, StoreParams params2) {
        boolean b0 = same0(params1, params2) ;
        boolean b1 = same1(params1, params2) ;
        if ( b0 != b1 )
            throw new InternalErrorException() ; 
        return b0 ;
    }
    
    private static boolean same0(StoreParams params1, StoreParams params2) {
        return params1.toString().equals(params2.toString()) ;
    }
    
    private static boolean same1(StoreParams params1, StoreParams params2) {
        return Objects.equals(params1, params2) ;
    }
}
