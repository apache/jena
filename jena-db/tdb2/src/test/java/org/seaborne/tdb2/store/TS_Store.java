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

package org.seaborne.tdb2.store;

import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.runner.RunWith ;
import org.junit.runners.Suite ;
import org.seaborne.dboe.base.block.FileMode ;
import org.seaborne.tdb2.store.value.TestDoubleNode62;
import org.seaborne.tdb2.store.value.TestNodeIdInline;
import org.seaborne.tdb2.sys.SystemTDB ;
import org.seaborne.tdb2.sys.TestOps ;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
    TestNodeId.class
    , TestNodeIdInline.class
    , TestDoubleNode62.class
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
