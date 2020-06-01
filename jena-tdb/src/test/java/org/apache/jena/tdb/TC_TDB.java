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

package org.apache.jena.tdb;

import org.apache.jena.sparql.engine.optimizer.reorder.ReorderLib ;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderTransformation ;
import org.apache.jena.tdb.assembler.TS_TDBAssembler ;
import org.apache.jena.tdb.base.TC_Base ;
import org.apache.jena.tdb.base.block.FileMode ;
import org.apache.jena.tdb.base.objectfile.TS_ObjectFile ;
import org.apache.jena.tdb.graph.TS_Graph ;
import org.apache.jena.tdb.index.TS_Index ;
import org.apache.jena.tdb.lib.TS_LibTDB ;
import org.apache.jena.tdb.setup.TS_TDBSetup ;
import org.apache.jena.tdb.solver.TS_SolverTDB ;
import org.apache.jena.tdb.store.TS_Store ;
import org.apache.jena.tdb.store.nodetable.TS_NodeTable ;
import org.apache.jena.tdb.store.tupletable.TS_TupleTable ;
import org.apache.jena.tdb.sys.SystemTDB ;
import org.apache.jena.tdb.sys.TS_Sys ;
import org.apache.jena.tdb.transaction.TS_TransactionTDB ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.runner.RunWith ;
import org.junit.runners.Suite ;

// Naming conventions.
// TS_* - Test sets: collections of testing files (Often Test*)
// TC_*  - Test collection: sets of TS's and TC's.

@RunWith(Suite.class)
@Suite.SuiteClasses( {
    // TDB
    TC_Base.class       // ==>  TS_Block, TS_File.class, TS_Record, TS_RecordFile
    , TS_LibTDB.class
    , TS_NodeTable.class
    , TS_Index.class
    , TS_TupleTable.class
    , TS_TDBSetup.class
    , TS_Store.class        // The main storage implementation.  Some slow tests.
    , TS_SolverTDB.class
    , TS_Sys.class
    , TS_Graph.class
    , TS_TDBFactory.class
    , TS_TDBAssembler.class
    , TS_TransactionTDB.class
    , TS_ObjectFile.class
    , TS_ScriptsTDB1.class
} )

public class TC_TDB
{
    static {
        if ( false )
            SystemTDB.setFileMode(FileMode.direct) ;
    }
    static ReorderTransformation dftReorder = null ; 
        
    @BeforeClass
    static public void beforeClass() {
        // Turn off general reordering (turned on for specific reorder tests)
        dftReorder = SystemTDB.defaultReorderTransform;
        SystemTDB.defaultReorderTransform = ReorderLib.identity();
    }

    @AfterClass
    static public void afterClass() {
        SystemTDB.defaultReorderTransform = dftReorder;
    }
}
