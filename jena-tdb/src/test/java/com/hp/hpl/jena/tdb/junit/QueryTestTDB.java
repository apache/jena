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

package com.hp.hpl.jena.tdb.junit;

import java.util.List ;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.SystemARQ ;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory ;
import com.hp.hpl.jena.sparql.engine.QueryExecutionBase ;
import com.hp.hpl.jena.sparql.engine.ref.QueryEngineRef ;
import com.hp.hpl.jena.sparql.junit.EarlReport ;
import com.hp.hpl.jena.sparql.junit.EarlTestCase ;
import com.hp.hpl.jena.sparql.junit.TestItem ;
import com.hp.hpl.jena.sparql.resultset.ResultSetCompare ;
import com.hp.hpl.jena.sparql.resultset.SPARQLResult ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.util.FileManager ;

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
    
    // Track what's currently loaded in the GraphLocation
    private static List<String> currentDefaultGraphs = null ;
    private static List<String> currentNamedGraphs = null ;

    // Old style (Junit3)
    public QueryTestTDB(String testName, EarlReport report, TestItem item)
    {
        this(testName, report, item.getURI(), 
             item.getDefaultGraphURIs(), item.getNamedGraphURIs(), 
             item.getResults(), item.getQueryFile()
             ) ;
    }
    
    public QueryTestTDB(String testName, EarlReport report, 
                        String uri,
                        List<String> dftGraphs,
                        List<String> namedGraphs,
                        SPARQLResult rs,
                        String queryFile
                        )
    {
        super(testName, uri, report) ;
        this.defaultGraphURIs = dftGraphs ;
        this.namedGraphURIs = namedGraphs ;
        this.queryFile = queryFile ;
        this.results = rs ;
    }
    
    boolean oldValueUsePlainGraph = SystemARQ.UsePlainGraph ;
    
    @Override public void setUpTest()
    {
        dataset = TDBFactory.createDataset() ;
        // Make sure a plain, no sameValueAs graph is used.
        oldValueUsePlainGraph = SystemARQ.UsePlainGraph ;
        SystemARQ.UsePlainGraph = true ;
        setupData() ;
    }
    
    @Override public void tearDownTest()
    { 
        if ( dataset != null )
        {
            dataset.close() ;
            dataset = null ;
        }
        SystemARQ.UsePlainGraph = oldValueUsePlainGraph ;
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
        Dataset ds = DatasetFactory.create(defaultGraphURIs, namedGraphURIs) ;
        
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
            try(QueryExecution qExec1 = new QueryExecutionBase(query, ds, null, f)) {
                rs1 = ResultSetFactory.makeRewindable(qExec1.execSelect()) ;
            }
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
