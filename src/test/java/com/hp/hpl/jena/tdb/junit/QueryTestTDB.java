/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.junit;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;

import com.hp.hpl.jena.util.FileManager;

import com.hp.hpl.jena.sparql.engine.QueryEngineFactory;
import com.hp.hpl.jena.sparql.engine.QueryExecutionBase;
import com.hp.hpl.jena.sparql.engine.ref.QueryEngineRef;
import com.hp.hpl.jena.sparql.junit.EarlReport;
import com.hp.hpl.jena.sparql.junit.EarlTestCase;
import com.hp.hpl.jena.sparql.junit.TestItem;
import com.hp.hpl.jena.sparql.resultset.ResultSetCompare ;
import com.hp.hpl.jena.sparql.resultset.ResultSetRewindable;
import com.hp.hpl.jena.sparql.resultset.SPARQLResult ;

import com.hp.hpl.jena.query.*;

import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.sys.DatasetGraphMakerTDB;

public class QueryTestTDB extends EarlTestCase
{
    // Changed to using in-memory graphs/datasets because this is testing the query
    // processing.  Physical graph/datsets is in package "store". 
    
    private static Logger log = LoggerFactory.getLogger(QueryTestTDB.class) ;
    private Dataset dataset = null ;

    boolean skipThisTest = false ;

    final List<String> defaultGraphURIs ;
    final List<String> namedGraphURIs ;
    final String queryFile ; 
    final SPARQLResult results ;
    
    final private DatasetGraphMakerTDB factory ;
 
    // Track what's currently loaded in the GraphLocation
    private static List<String> currentDefaultGraphs = null ;
    private static List<String> currentNamedGraphs = null ;

    // Old style (Junit3)
    public QueryTestTDB(String testName, EarlReport report, TestItem item, DatasetGraphMakerTDB factory)
    {
        this(testName, report, item.getURI(), 
             item.getDefaultGraphURIs(), item.getNamedGraphURIs(), 
             item.getResults(), item.getQueryFile(),
             factory) ;
    }
    
    public QueryTestTDB(String testName, EarlReport report, 
                        String uri,
                        List<String> dftGraphs,
                        List<String> namedGraphs,
                        SPARQLResult rs,
                        String queryFile,
                        DatasetGraphMakerTDB factory)
    {
        super(testName, uri, report) ;
        this.defaultGraphURIs = dftGraphs ;
        this.namedGraphURIs = namedGraphs ;
        this.queryFile = queryFile ;
        this.results = rs ;
        this.factory = factory ;
    }
    
    
    @Override public void setUp()
    {
        dataset = TDBFactory.createDataset() ;
        setupData() ;
    }
    
    @Override public void tearDown()
    { 
        if ( dataset != null ) dataset.close() ; 
    }
    
    public void setupData()
    {
        if ( compareLists(defaultGraphURIs, currentDefaultGraphs) &&
             compareLists(namedGraphURIs, currentNamedGraphs) )
            return ;
        
        if ( defaultGraphURIs == null )
            throw new TDBTestException("No default graphs given") ;

        //graphLocation.clear() ;
        
        for ( String fn : defaultGraphURIs )
            load(dataset.getDefaultModel(), fn) ;
        
        for ( String fn : namedGraphURIs )
            load(dataset.getNamedModel(fn), fn) ;
    }
    
    
    @Override
    protected void runTestForReal() throws Throwable
    {
        if ( skipThisTest )
        {
            log.info(this.getName()+" : Skipped") ;
            return ;
        }
        
        Query query = QueryFactory.read(queryFile) ;
        
        // Make sure a plain, no sameValueAs graph is used.
        Object oldValue = ARQ.getContext().get(ARQ.strictGraph) ;
        ARQ.setTrue(ARQ.strictGraph) ;
        Dataset ds = DatasetFactory.create(defaultGraphURIs, namedGraphURIs) ;
        ARQ.getContext().set(ARQ.strictGraph, oldValue) ;
        
        // ---- First, get the expected results by executing in-memory or from a results file.
        
        ResultSetRewindable rs1 = null ;
        String expectedLabel = "" ;
        if ( results != null )
        {
            rs1 = ResultSetFactory.makeRewindable(results.getResultSet()) ;
            expectedLabel = "Results file" ;
        }
        else
        {
            QueryEngineFactory f = QueryEngineRef.getFactory() ;
            QueryExecution qExec1 = new QueryExecutionBase(query, ds, null, f) ;
            rs1 = ResultSetFactory.makeRewindable(qExec1.execSelect()) ;
            qExec1.close() ;
            expectedLabel = "Standard engine" ;
        }
        
        // ---- Second, execute in persistent graph

        Dataset ds2 = dataset ; //DatasetFactory.create(model) ;
        QueryExecution qExec2 = QueryExecutionFactory.create(query, ds2) ;
        ResultSet rs = qExec2.execSelect() ;
        ResultSetRewindable rs2 = ResultSetFactory.makeRewindable(rs) ;
        
        // See if the same.
        boolean b = ResultSetCompare.equalsByValue(rs1, rs2) ;
        if ( !b )
        {
            rs1.reset() ;
            rs2.reset() ;
            System.out.println("------------------- "+this.getName());
            System.out.printf("**** Expected (%s)", expectedLabel) ;
            ResultSetFormatter.out(System.out, rs1) ; 
            System.out.println("**** Got (TDB)") ;
            ResultSetFormatter.out(System.out, rs2) ;
        }
        
        assertTrue("Results sets not the same", b) ; 
    }

    private static void load(Model model, String fn)
    {
        FileManager.get().readModel(model, fn) ;
    }
    
    private static boolean compareLists(List<String> list1, List<String> list2)
    {
        if ( list1 == null )
            return ( list2 == null ) ;
        return list1.equals(list2) ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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