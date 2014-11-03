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
