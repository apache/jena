/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import iterator.TS_Iterator;
import lib.TS_Lib;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.hp.hpl.jena.tdb.TS_TDB;
import com.hp.hpl.jena.tdb.base.block.TS_Block;
import com.hp.hpl.jena.tdb.base.file.TS_File;
import com.hp.hpl.jena.tdb.base.loader.TS_Loader;
import com.hp.hpl.jena.tdb.base.record.TS_Record;
import com.hp.hpl.jena.tdb.base.recordfile.TS_RecordFile;
import com.hp.hpl.jena.tdb.index.TS_Index;
import com.hp.hpl.jena.tdb.pgraph.TS_GraphTDB;
import com.hp.hpl.jena.tdb.solver.TS_Solver;
import com.hp.hpl.jena.tdb.solver.reorder.ReorderLib;
import com.hp.hpl.jena.tdb.sys.SystemTDB;
import com.hp.hpl.jena.tdb.sys.TS_Sys;

// Ideal - find all TS_ classes on the classpath and run.  Like ant does

@RunWith(Suite.class)
@Suite.SuiteClasses( {
    TS_Lib.class,
//    TS_HTable.class,
    TS_Block.class,
    TS_File.class,
    TS_Loader.class,
    TS_Record.class,
    //TS_Base.class,
    TS_RecordFile.class,
    // Lib
//    TS_IO.class,
    TS_Iterator.class,
    
    TS_Index.class,
    
    TS_TDB.class,
    TS_GraphTDB.class,
    TS_Solver.class,
    TS_Sys.class
} )

public class TS_Main
{
    @BeforeClass static public void beforeClass()   
    {
        Logger.getLogger("com.hp.hpl.jena.tdb.info").setLevel(Level.WARN) ;
        Logger.getLogger("com.hp.hpl.jena.tdb.exec").setLevel(Level.WARN) ;
        SystemTDB.defaultOptimizer = ReorderLib.identity() ;
    }
    
    // For "ant" before 1.7 that only understands JUnit3. 
    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TS_Main.class) ;
    }
}


/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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