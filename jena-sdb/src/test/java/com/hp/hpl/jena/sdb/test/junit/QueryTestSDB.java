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

package com.hp.hpl.jena.sdb.test.junit;

import java.util.ArrayList ;
import java.util.List ;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.sdb.Store ;
import com.hp.hpl.jena.sdb.StoreDesc ;
import com.hp.hpl.jena.sdb.engine.QueryEngineSDB ;
import com.hp.hpl.jena.sdb.sql.SDBConnection ;
import com.hp.hpl.jena.sdb.store.DatasetStore ;
import com.hp.hpl.jena.sdb.util.StoreUtils ;
import com.hp.hpl.jena.shared.Command ;
import com.hp.hpl.jena.sparql.SystemARQ ;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory ;
import com.hp.hpl.jena.sparql.engine.QueryExecutionBase ;
import com.hp.hpl.jena.sparql.engine.ref.QueryEngineRef ;
import com.hp.hpl.jena.sparql.junit.EarlReport ;
import com.hp.hpl.jena.sparql.junit.EarlTestCase ;
import com.hp.hpl.jena.sparql.junit.TestItem ;
import com.hp.hpl.jena.sparql.resultset.ResultSetCompare ;

public class QueryTestSDB extends EarlTestCase
{
    public static boolean VERBOSE = false ;
    StoreDesc storeDesc ;
    Store store = null ;
    TestItem item ;
    private static Logger log = LoggerFactory.getLogger(QueryTestSDB.class) ; 
    
    public QueryTestSDB(StoreDesc desc, String testName, EarlReport report, TestItem item)
    {
        super(testName, item.getURI(), report) ;
        this.storeDesc = desc ;
        this.item = item ;
    }

    // NB static.
    // Assumes that tests run serially
    
    static String currentTestName = null ;
    static List<String> lastDftLoaded = new ArrayList<String>() ;
    static List<String> lastNamedLoaded = new ArrayList<String>() ;
    
    boolean skipThisTest = false ;
    boolean origValueUsePlainGraph = false ;

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
        
        final List<String> filenamesDft = item.getDefaultGraphURIs() ;
        final List<String> filenamesNamed = item.getNamedGraphURIs() ;
        
        try {
            store = StoreList.testStore(storeDesc) ;
        } catch (Exception ex)
        {
            ex.printStackTrace(System.err) ;
            return ;
        }

        // More trouble than it's worth.
//      // Same as last time - skip.
      if ( ! StoreList.inMem(store) && lastDftLoaded.equals(filenamesDft) && lastNamedLoaded.equals(filenamesNamed) )
          return ;

        // Truncate outside a transaction.
        store.getTableFormatter().truncate() ;
        store.getConnection().executeInTransaction(new Command(){
            @Override
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
        
        origValueUsePlainGraph = SystemARQ.UsePlainGraph ;
    }
    
    @Override
    public void tearDown()
    { 
        SystemARQ.UsePlainGraph = origValueUsePlainGraph ;
        if ( store != null )
        {
            // Other databases can have problems if all are running on the same machine at the same time.
            // e.g. running out of shared memory segments.
            
            store.close() ;
            store.getConnection().close() ;
            // Oracle seems to async release connections (in the XE server only?)
            if ( StoreUtils.isOracle(store) )
            {
                try { synchronized (this) { this.wait(200) ; } }
                catch (InterruptedException ex) { ex.printStackTrace(); }
            }
        }
        store = null ;
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
        
        if ( store == null )
            fail("No store") ;
        
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
        SystemARQ.UsePlainGraph = true ;
        Dataset ds = DatasetFactory.create(item.getDefaultGraphURIs(), item.getNamedGraphURIs()) ;
        
        // ---- First, get the expected results by executing in-memory or from a results file.
        
        ResultSet rs = null ;
        if ( item.getResults() != null )
            rs = item.getResults().getResultSet() ;
        ResultSetRewindable rs1 = null ;
        String expectedLabel = "" ;
        if ( rs != null )
        {
            rs1 = ResultSetFactory.makeRewindable(rs) ;
            expectedLabel = "Results file" ;
        }
        else
        {
            QueryEngineFactory f = QueryEngineRef.getFactory() ;
            try ( QueryExecution qExec1 = new QueryExecutionBase(query, ds, null, f) ) {
                rs1 = ResultSetFactory.makeRewindable(qExec1.execSelect()) ;
            }
            expectedLabel = "Standard engine" ;
        }
        
        // ---- Second, execute in DB

        QueryEngineFactory f2 = QueryEngineSDB.getFactory() ;
        ds = DatasetStore.create(store) ;
        try (QueryExecution qExec2 = new QueryExecutionBase(query, ds, null, f2) ) {
            SDBConnection.logSQLExceptions = true ;
            rs = qExec2.execSelect() ;
            
            ResultSetRewindable rs2 = ResultSetFactory.makeRewindable(rs) ;
            boolean b = ResultSetCompare.equalsByTerm(rs1, rs2) ;
            if ( !b )
            {
                rs1.reset() ;
                rs2.reset() ;
                System.out.println("------------------- "+this.getName());
                System.out.printf("**** Expected (%s)", expectedLabel) ;
                ResultSetFormatter.out(System.out, rs1) ; 
                System.out.println("**** Got (SDB engine)") ;
                ResultSetFormatter.out(System.out, rs2) ;
            }
            
            assertTrue("Results sets not the same", b) ; 
        } finally { SDBConnection.logSQLExceptions = false ; }
    }

}
