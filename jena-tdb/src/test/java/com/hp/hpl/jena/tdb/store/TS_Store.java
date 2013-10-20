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

package com.hp.hpl.jena.tdb.store;

import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.runner.RunWith ;
import org.junit.runners.Suite ;

import com.hp.hpl.jena.tdb.base.block.FileMode ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;
import com.hp.hpl.jena.tdb.sys.TestOps ;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
    TestNodeId.class
    , TestTripleTable.class
    , TestGraphTDB.class
    , TestGraphNamedTDB.class
    , TestDatasetTDBPersist.class
    , TestDatasetTDB.class
    , TestLoader.class
    // The script suite
    , TestSuiteGraphTDB.class
    , Test_SPARQL_TDB.class
    , TestConcurrentAccess.class
    , TestDynamicDatasetTDB.class
    , TestStoreConnectionsDirect.class
    , TestStoreConnectionsMapped.class
} )
public class TS_Store
{ 
    static FileMode mode ; 
    
    @BeforeClass
    public static void beforeClass()
    {
        mode = SystemTDB.fileMode() ;
    }
    
    @AfterClass
    public static void afterClass()
    {
        if ( ! SystemTDB.fileMode().equals(mode) )
            TestOps.setFileMode(mode) ;    
    }
}
