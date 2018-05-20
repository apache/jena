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

package org.apache.jena.tdb2;

import org.apache.jena.dboe.base.block.FileMode;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderLib ;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderTransformation ;
import org.apache.jena.tdb2.assembler.TS_TDBAssembler;
import org.apache.jena.tdb2.graph.TS_Graph;
import org.apache.jena.tdb2.lib.TS_LibTDB;
import org.apache.jena.tdb2.loader.TS_Loader;
import org.apache.jena.tdb2.setup.TS_TDBSetup;
import org.apache.jena.tdb2.solver.TS_SolverTDB;
import org.apache.jena.tdb2.store.TS_Store;
import org.apache.jena.tdb2.store.nodetable.TS_NodeTable;
import org.apache.jena.tdb2.store.tupletable.TS_TupleTable;
import org.apache.jena.tdb2.sys.SystemTDB;
import org.apache.jena.tdb2.sys.TS_Sys;
import org.apache.log4j.Level ;
import org.apache.log4j.Logger ;
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
    TS_LibTDB.class
    , TS_NodeTable.class
    , TS_TupleTable.class
    , TS_TDBSetup.class
    , TS_Store.class
    , TS_SolverTDB.class
    , TS_Graph.class
    , TS_Factory.class
    , TS_TDBAssembler.class
    , TS_Sys.class
    , TS_Loader.class
} )

public class TC_TDB2
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
        Logger.getLogger("org.apache.jena.tdb.info").setLevel(Level.WARN) ;
        //Logger.getLogger("org.apache.jena.tdb.exec").setLevel(Level.WARN) ;
        dftReorder = SystemTDB.defaultReorderTransform ;
        SystemTDB.defaultReorderTransform = ReorderLib.identity() ;
    }
    
    @AfterClass static public void afterClass() {
        SystemTDB.defaultReorderTransform = dftReorder ;
    }
}
