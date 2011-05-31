/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb;

import org.apache.log4j.Level ;
import org.apache.log4j.Logger ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.runner.RunWith ;
import org.junit.runners.Suite ;

import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderLib ;
import com.hp.hpl.jena.tdb.assembler.TS_TDBAssembler ;
import com.hp.hpl.jena.tdb.base.TC_Base ;
import com.hp.hpl.jena.tdb.graph.TS_Graph ;
import com.hp.hpl.jena.tdb.index.TS_Index ;
import com.hp.hpl.jena.tdb.lib.TS_LibTDB ;
import com.hp.hpl.jena.tdb.migrate.TS_Migrate ;
import com.hp.hpl.jena.tdb.nodetable.TS_NodeTable ;
import com.hp.hpl.jena.tdb.solver.TS_SolverTDB ;
import com.hp.hpl.jena.tdb.store.TS_Store ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;
import com.hp.hpl.jena.tdb.sys.TS_Sys ;
import com.hp.hpl.jena.tdb.transaction.TS_Transaction ;

// Naming conventions.
// TS_* - Test sets: collections of testing files (Often Test*)
// TC_*  - Test collection: sets of TS's and TC's.

@RunWith(Suite.class)
@Suite.SuiteClasses( {
    // TDB
    TC_Base.class    
    , TS_LibTDB.class
    , TS_NodeTable.class
    , TS_Index.class
    , TS_Store.class        // The main storage implementation.  Slow tests.
    , TS_SolverTDB.class
    , TS_Sys.class
    , TS_Graph.class
    , TS_Factory.class
    , TS_TDBAssembler.class
    , TS_Jena.class
    , TS_Migrate.class
    , TS_Transaction.class
} )

public class TC_TDB
{
    @BeforeClass static public void beforeClass()   
    {
        //org.apache.log4j.LogManager.resetConfiguration() ;
        //org.apache.log4j.PropertyConfigurator.configure("log4j.properties") ;
        Logger.getLogger("com.hp.hpl.jena.tdb.info").setLevel(Level.WARN) ;
        //Logger.getLogger("com.hp.hpl.jena.tdb.exec").setLevel(Level.WARN) ;
        SystemTDB.defaultOptimizer = ReorderLib.identity() ;
    }
    
    @AfterClass static public void afterClass() {}   
    
    // For "ant" before 1.7 that only understands JUnit3. 
    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TC_TDB.class) ;
    }
}


/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */