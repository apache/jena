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

package com.hp.hpl.jena.tdb;

import org.apache.log4j.Level ;
import org.apache.log4j.Logger ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.runner.RunWith ;
import org.junit.runners.Suite ;

import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderLib ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderTransformation ;
import com.hp.hpl.jena.tdb.assembler.TS_TDBAssembler ;
import com.hp.hpl.jena.tdb.base.TC_Base ;
import com.hp.hpl.jena.tdb.base.block.FileMode ;
import com.hp.hpl.jena.tdb.base.objectfile.TS_ObjectFile ;
import com.hp.hpl.jena.tdb.graph.TS_Graph ;
import com.hp.hpl.jena.tdb.index.TS_Index ;
import com.hp.hpl.jena.tdb.lib.TS_LibTDB ;
import com.hp.hpl.jena.tdb.solver.TS_SolverTDB ;
import com.hp.hpl.jena.tdb.store.TS_Store ;
import com.hp.hpl.jena.tdb.store.nodetable.TS_NodeTable ;
import com.hp.hpl.jena.tdb.store.tupletable.TS_TupleTable ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;
import com.hp.hpl.jena.tdb.sys.TS_Sys ;
import com.hp.hpl.jena.tdb.transaction.TS_TransactionTDB ;

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
    , TS_Store.class        // The main storage implementation.  Slow tests.
    , TS_SolverTDB.class
    , TS_Sys.class
    , TS_Graph.class
    , TS_Factory.class
    , TS_TDBAssembler.class
    , TS_TransactionTDB.class
    , TS_ObjectFile.class
} )

public class TC_TDB
{
    static {
        if ( false )
            SystemTDB.setFileMode(FileMode.direct) ;
    }
    static ReorderTransformation dftReorder = null ; 
        
    @BeforeClass static public void beforeClass()   
    {
        //org.apache.log4j.LogManager.resetConfiguration() ;
        //org.apache.log4j.PropertyConfigurator.configure("log4j.properties") ;
        Logger.getLogger("com.hp.hpl.jena.tdb.info").setLevel(Level.WARN) ;
        //Logger.getLogger("com.hp.hpl.jena.tdb.exec").setLevel(Level.WARN) ;
        dftReorder = SystemTDB.defaultReorderTransform ;
        SystemTDB.defaultReorderTransform = ReorderLib.identity() ;
    }
    
    @AfterClass static public void afterClass() {
        SystemTDB.defaultReorderTransform = dftReorder ;
    }
}
