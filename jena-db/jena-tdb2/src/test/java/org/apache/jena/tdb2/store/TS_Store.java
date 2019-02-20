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

package org.apache.jena.tdb2.store;

import org.apache.jena.dboe.base.block.FileMode;
import org.apache.jena.tdb2.store.value.TestDoubleNode62;
import org.apache.jena.tdb2.store.value.TestFloatNode;
import org.apache.jena.tdb2.store.value.TestNodeIdInline;
import org.apache.jena.tdb2.sys.SystemTDB;
import org.apache.jena.tdb2.sys.TestOps;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.runner.RunWith ;
import org.junit.runners.Suite ;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
    TestNodeId.class
    , TestNodeIdInline.class
    , TestDoubleNode62.class
    , TestFloatNode.class
    , TestTripleTable.class
    , TestGraphTDB.class
    , TestGraphNamedTDB.class
    , TestDatasetTDB.class
    , TestDatasetTDBPersist.class
    //, TestBulkLoader.class
    // The script suite
    , TestSuiteGraphTDB.class
    
    , Test_SPARQL_TDB.class
    , TestDynamicDatasetTDB.class
    , TestStoreConnectionMem.class
    , TestStoreConnectionDirect.class
    , TestStoreConnectionMapped.class
    , TestStoreConnectionLock.class
    , TestTransactions.class
    , TestTransactionLifecycleTDB.class
    , TestTransPromoteTDB.class
    , TestQuadFilter.class
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
