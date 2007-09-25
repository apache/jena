/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.test.junit;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.engine.QueryEngineSDB;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.DatasetStore;
import com.hp.hpl.jena.sdb.util.StoreUtils;
import com.hp.hpl.jena.shared.Command;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory;
import com.hp.hpl.jena.sparql.engine.QueryExecutionBase;
import com.hp.hpl.jena.sparql.engine.ref.QueryEngineRef;
import com.hp.hpl.jena.sparql.junit.EarlReport;
import com.hp.hpl.jena.sparql.junit.EarlTestCase;
import com.hp.hpl.jena.sparql.junit.TestItem;
import com.hp.hpl.jena.sparql.resultset.RSCompare;
import com.hp.hpl.jena.sparql.resultset.ResultSetRewindable;

public class QueryTestSDB extends EarlTestCase
{
    public static boolean VERBOSE = false ;
    Store store ;
    TestItem item ;
    private static Log log = LogFactory.getLog(QueryTestSDB.class) ; 
    
    public QueryTestSDB(Store store, String testName, EarlReport report, TestItem item)
    {
        super(testName, item.getURI(), report) ;
        this.store = store ;
        this.item = item ;
    }

    // NB static.
    // Assumes that tests run serially
    
    static String currentTestName = null ;
    static List<String> lastDftLoaded = new ArrayList<String>() ;
    static List<String> lastNamedLoaded = new ArrayList<String>() ;
    
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
        
        @SuppressWarnings("unchecked")
        final List<String> filenamesDft = (List<String>)item.getDefaultGraphURIs() ;
        @SuppressWarnings("unchecked")
        final List<String> filenamesNamed = (List<String>)item.getNamedGraphURIs() ;
        
        // Same as last time - skip.
        if ( lastDftLoaded.equals(filenamesDft) && lastNamedLoaded.equals(filenamesNamed) )
            return ;

        // Truncate outside a transaction.
        store.getTableFormatter().truncate() ;
        store.getConnection().executeInTransaction(new Command(){
            public Object execute()
            {
                // Default graph
                for ( String fn : filenamesDft )
                    StoreUtils.load(store, fn) ;    
                // Named graphs
                for ( String fn : filenamesNamed )
                    StoreUtils.load(store, fn, fn) ;    
                return null ;
            }}) ;
        lastDftLoaded = filenamesDft ;
        lastNamedLoaded = filenamesNamed ;
        
    }
    
    @Override
    public void tearDown()
    { 
        currentTestName = null ;
    }

    @Override
    public void runTestForReal()
    {
        if ( skipThisTest )
        {
            log.info(this.getName()+" : Skipped") ;
            return ;
        }
        
//        if ( item.getDefaultGraphURIs().size() != 1 || item.getNamedGraphURIs().size() != 0 )
//            fail("Only one data graph supported") ;
        
        Query query = QueryFactory.read(item.getQueryFile()) ;
        
        // If null, then compare to running ARQ in-memory 
        if ( VERBOSE )
        {
            System.out.println("Test: "+this.getName()) ;
            System.out.println(query) ;  
        }
        
        // Make sure a plain, no sameValueAs graph is used.
        Object oldValue = ARQ.getContext().get(ARQ.strictGraph) ;
        ARQ.setTrue(ARQ.strictGraph) ;
        Dataset ds = DatasetFactory.create(item.getDefaultGraphURIs(), item.getNamedGraphURIs()) ;
        ARQ.getContext().set(ARQ.strictGraph, oldValue) ;
        
        // ---- First, execute in-memory or from a results file.
        
        ResultSet rs = item.getResultSet() ;
        ResultSetRewindable rs1 = null ;
        if ( rs != null )
            rs1 = ResultSetFactory.makeRewindable(rs) ;
        else
        {
            System.err.println("Old way") ;
            QueryEngineFactory f = QueryEngineRef.getFactory() ;
            QueryExecution qExec1 = new QueryExecutionBase(query, ds, null, f) ;
            rs1 = ResultSetFactory.makeRewindable(qExec1.execSelect()) ;
            qExec1.close() ;
        }
        
        // ---- Second, execute in DB

        QueryEngineFactory f2 = QueryEngineSDB.getFactory() ;
        ds = DatasetStore.create(store) ;
        QueryExecution qExec2 = new QueryExecutionBase(query, ds, null, f2) ;
        
        try {
            SDBConnection.logSQLExceptions = true ;
            rs = qExec2.execSelect() ;
            
            ResultSetRewindable rs2 = ResultSetFactory.makeRewindable(rs) ;
            boolean b = RSCompare.same(rs1, rs2) ;
            if ( !b )
            {
                rs1.reset() ;
                rs2.reset() ;
                System.out.println("------------------- "+this.getName());
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
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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