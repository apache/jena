/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.engine;

import java.io.PrintStream ;

import org.junit.Assert ;
import org.apache.jena.riot.RDFDataMgr ;
import org.junit.Test ;

import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.sparql.engine.main.OpExecutorFactory ;
import com.hp.hpl.jena.sparql.engine.main.QC ;
import com.hp.hpl.jena.sparql.resultset.ResultSetCompare ;

/** Tests of OpExecutor */
public abstract class AbstractTestOpExecutor extends Assert
{
    private static final String DIR = "testing/Engines" ; 
    protected final OpExecutorFactory factory ;
    private String name ;
    
    public AbstractTestOpExecutor(String name, OpExecutorFactory factory) {
        this.name = name ;
        this.factory = factory ;
    }
    
    protected abstract Dataset createDataset() ;
    
    // XXX Need more tests
    // This first group of tests do not lead multiple matches per
    // same predicate in results nor match same-variable. 
    // 
    @Test public void engine_triples_01()       { test("query-triples-1.rq", "data-2.ttl", factory) ; }
    @Test public void engine_triples_02()       { test("query-triples-2.rq", "data-2.ttl", factory) ; }
    @Test public void engine_triples_03()       { test("query-triples-3.rq", "data-2.ttl", factory) ; }
    
    @Test public void engine_quads_01()         { test("query-quads-1.rq",  "data-1.trig", factory) ; }
    @Test public void engine_quads_02()         { test("query-quads-2.rq",  "data-1.trig", factory) ; }
    @Test public void engine_quads_03()         { test("query-quads-3.rq",  "data-1.trig", factory) ; }
    @Test public void engine_quads_04()         { test("query-quads-4.rq",  "data-1.trig", factory) ; }
    @Test public void engine_quads_filter_01()  { test("query-quads-filter-1.rq",  "data-1.trig", factory) ; }
    @Test public void engine_quads_filter_02()  { test("query-quads-filter-2.rq",  "data-1.trig", factory) ; }
    
    // Tests of more complicate BGP and variable patterns
    @Test public void engine_bgp_01()       { test("query-bgp-4.rq", "data-2.ttl", factory) ; }
    @Test public void engine_bgp_02()       { test("query-bgp-5.rq", "data-2.ttl", factory) ; }
    @Test public void engine_bgp_03()       { test("query-bgp-6.rq", "data-2.ttl", factory) ; }
    
    // Use of ?s in the object position!
    
    private void test(String queryFile, String datafile, OpExecutorFactory factory) {
        queryFile = DIR+"/"+queryFile ;
        datafile = DIR+"/"+datafile ;
        Query query = QueryFactory.read(queryFile) ;
        Dataset ds = RDFDataMgr.loadDataset(datafile) ;
        // Default. Generated expected results.
        QueryExecution qExec1 = QueryExecutionFactory.create(query, ds) ;
        ResultSetRewindable rs1 = ResultSetFactory.makeRewindable(qExec1.execSelect()) ;
        
        // Test 
        ds = createDataset() ;
        RDFDataMgr.read(ds, datafile) ;
        // Test
        if ( factory != null )
            QC.setFactory(ds.getContext(), factory) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
        ResultSetRewindable rs = ResultSetFactory.makeRewindable(qExec.execSelect()) ;
        
        boolean b = ResultSetCompare.equalsByTerm(rs1, rs) ;
        if ( ! b ) {
            PrintStream out = System.out ;
            out.println("---- Test : "+name) ;
            out.println("-- Expected") ;
            rs1.reset();
            ResultSetFormatter.out(out, rs1, query) ;
            out.println("-- Actual") ;
            rs.reset();
            ResultSetFormatter.out(out, rs, query) ;
            fail(name+" : Results not equal") ;
        }
    }
}
