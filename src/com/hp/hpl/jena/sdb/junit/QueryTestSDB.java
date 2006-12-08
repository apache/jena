/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.junit;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.query.junit.TestItem;
import com.hp.hpl.jena.query.resultset.RSCompare;
import com.hp.hpl.jena.query.resultset.ResultSetRewindable;
import com.hp.hpl.jena.sdb.engine.QueryEngineQuadSDB;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.DatasetStore;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.util.StoreUtils;
import com.hp.hpl.jena.shared.Command;
import com.hp.hpl.jena.util.FileManager;



public class QueryTestSDB extends TestCase
{
    public static boolean VERBOSE = false ;
    Store store ;
    TestItem item ;
    private static Log log = LogFactory.getLog(QueryTestSDB.class) ; 
    
    public QueryTestSDB(Store store, String testName, TestItem item)
    {
        super(testName) ;
        this.store = store ;
        this.item = item ;
    }

    public QueryTestSDB(Store store, String testName, FileManager fileManager, TestItem item)
    {
        this(store, testName, item) ;
    }

    // NB static.
    // Assumes that tests run serially
    
    static String currentTestName = null ;
    static String lastFileLoaded = null ;
    
    boolean skipThisTest = false ;
    
    @Override
    public void setUp()
    { 
        if ( currentTestName != null )
        {
            log.warn(this.getName()+" : Already in test '"+currentTestName+"'") ;
            skipThisTest = true ;
            return ;
        }
        
        currentTestName = getName() ;
        
        if ( item.getDefaultGraphURIs().size() != 1 || item.getNamedGraphURIs().size() != 0 )
            fail("Only one data graph supported") ;
        String dataFileName = (String)item.getDefaultGraphURIs().get(0) ;
        
        if ( lastFileLoaded == null || !lastFileLoaded.equals(dataFileName) )
        {
            log.debug("File loaded: "+dataFileName) ;
            final String filename = dataFileName ;
            // swap data in a transaction

            store.getConnection().executeInTransaction(new Command(){
                public Object execute()
                {
                    store.getTableFormatter().truncate() ;
                    StoreUtils.load(store, filename) ;
                    return null ;
                }}) ;
            
            lastFileLoaded = dataFileName ;
        }
    }
    
    @Override
    public void tearDown()
    { 
        currentTestName = null ;
    }

    @Override
    public void runTest()
    {
        if ( skipThisTest )
        {
            log.info(this.getName()+" : Skipped") ;
            return ;
        }
        
        if ( item.getDefaultGraphURIs().size() != 1 || item.getNamedGraphURIs().size() != 0 )
            fail("Only one data graph supported") ;
        
        Query query = QueryFactory.read(item.getQueryFile()) ;
            
        if ( VERBOSE )
        {
            System.out.println("Test: "+this.getName()) ;
            System.out.println(query) ;  
        }
        
        // Make sure a plain, no sameValueAs graph is used.
        Object oldValue = ARQ.getContext().get(ARQ.graphNoSameValueAs) ;
        ARQ.setTrue(ARQ.graphNoSameValueAs) ;
        Dataset ds = DatasetFactory.create(item.getDefaultGraphURIs(), item.getNamedGraphURIs()) ;
        ARQ.getContext().set(ARQ.graphNoSameValueAs, oldValue) ;
        
        // ---- First, execute in-memory.
        QueryExecution qExec1 = QueryExecutionFactory.create(query, ds) ;
        ResultSetRewindable rs1 = ResultSetFactory.makeRewindable(qExec1.execSelect()) ;
        qExec1.close() ;
        
        // ---- Second, execute in DB

        QueryEngineQuadSDB qExec2 = new QueryEngineQuadSDB(store, query, null) ;
        qExec2.setDataset(new DatasetStore(store)) ; // Not used
        
        ResultSet rs = null;
        try {
            SDBConnection.logSQLExceptions = true ;
            rs = qExec2.execSelect() ;
            
            ResultSetRewindable rs2 = ResultSetFactory.makeRewindable(rs) ;
            boolean b = RSCompare.same(rs1, rs2) ;
            if ( !b )
            {
                rs1.reset() ;
                rs2.reset() ;
                System.out.println("**** Expected (standard engine)") ;
                ResultSetFormatter.out(System.out, rs1) ; 
                System.out.println("**** Got (SDB engine)") ;
                ResultSetFormatter.out(System.out, rs2) ;
            }
            
            assertTrue("Results sets not the same", b) ; 
        } finally { SDBConnection.logSQLExceptions = false ; }
    }

}

/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
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