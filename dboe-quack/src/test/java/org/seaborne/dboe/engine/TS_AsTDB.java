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

package org.seaborne.dboe.engine;

import org.junit.runner.RunWith ;
import org.junit.runners.Suite ;

import org.apache.jena.tdb.TS_Factory ;
import org.apache.jena.tdb.base.TC_Base ;
import org.apache.jena.tdb.base.objectfile.TS_ObjectFile ;
import org.apache.jena.tdb.index.TS_Index ;
import org.apache.jena.tdb.lib.TS_LibTDB ;
import org.apache.jena.tdb.solver.TS_SolverTDB ;
import org.apache.jena.tdb.store.* ;
import org.apache.jena.tdb.store.nodetable.TS_NodeTable ;
import org.apache.jena.tdb.sys.TS_Sys ;
import org.apache.jena.tdb.transaction.* ;

/** TDB tests run directly here as appropriate */

@RunWith(Suite.class)
@Suite.SuiteClasses( {
    // else selection from TC_TDB - remove the slow (disk related) ones
    // because we are testing a query engine.
    TC_Base.class
    , TS_LibTDB.class
    , TS_NodeTable.class
    , TS_Index.class
    //, TS_Store.class  SLOW // Includes dynamic datasets and unionDefaultGraph  
    , TestNodeId.class
    //, TestTripleTable.class
    //, TestGraphTDB.class
    //, TestGraphNamedTDB.class
    //, TestDatasetTDBPersist.class
    , TestDatasetTDB.class
    //, TestLoader.class
    
    
    
    // The script suite
//    , TestSuiteGraphTDB.class
    , Test_SPARQL_TDB.class
    , TestConcurrentAccess.class
    , TestDynamicDatasetTDB.class
    //, TestStoreConnectionsDirect.class
    //, TestStoreConnectionsMapped.class
    // End TS_Store
    
    , TS_SolverTDB.class
    , TS_Sys.class
    //, TS_Graph.class
    , TS_Factory.class
    //, TS_TDBAssembler.class
    //, TS_TransactionTDB.class  SLOW // Includes unionDefaultGraph  
    ,  TestJournal.class
    , TestTransIterator.class
    , TestObjectFileTransMem.class
    , TestObjectFileTransStorage.class
    , TestNodeTableTransMem.class
    , TestNodeTableTransDisk.class
    , TestTransMem.class
    //, TestTransDiskDirect.class
    //, TestTransDiskMapped.class
    //, TestTransRestart.class
    //, TestTransactionTDB.class
    , TestTransactionUnionGraph.class
    // End TS_TransactionTDB
    
    // 
    , TS_ObjectFile.class
} )
public class TS_AsTDB
{
}
