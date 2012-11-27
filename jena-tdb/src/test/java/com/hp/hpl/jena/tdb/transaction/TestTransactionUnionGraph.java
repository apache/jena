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

package com.hp.hpl.jena.tdb.transaction;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.junit.* ;

import com.hp.hpl.jena.query.*  ;

import static com.hp.hpl.jena.query.ReadWrite.* ;

import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.update.* ;

/** Tests of transactions and the TDG union graph */
public class TestTransactionUnionGraph extends BaseTest
{
    private Dataset ds ;
    
//    @BeforeClass public static void beforeClass()
//    { 
//        ds = TDBFactory.createDataset() ;
//        ds.asDatasetGraph().add(SSE.parseQuad("(<g> <s> <p> 1)")) ;
//    }
//    @AfterClass public static void afterClass() { ds = null ; }

    @Before
    public void before()
    {
        ds = TDBFactory.createDataset() ;
        ds.asDatasetGraph().add(SSE.parseQuad("(<g> <s> <p> 1)")) ;
    }
    
    @After public void after() {} 
    
    @Test public void uniontxn_global_r()
    {
        ARQ.getContext().setTrue(TDB.symUnionDefaultGraph) ;
        test(ReadWrite.READ) ;
        ARQ.getContext().unset(TDB.symUnionDefaultGraph) ;
    }
    
    @Test public void uniontxn_global_w()
    {
        ARQ.getContext().setTrue(TDB.symUnionDefaultGraph) ;
        test(ReadWrite.WRITE) ;
        ARQ.getContext().unset(TDB.symUnionDefaultGraph) ;
    }
    
    @Test public void uniontxn_ds_r_1()
    {
        ds.getContext().setTrue(TDB.symUnionDefaultGraph) ;
        test(ReadWrite.READ) ;
        ds.getContext().unset(TDB.symUnionDefaultGraph) ;
    }
    
    @Test public void uniontxn_ds_w_1()
    {
        ds.getContext().setTrue(TDB.symUnionDefaultGraph) ;
        test(ReadWrite.WRITE) ;
        ds.getContext().unset(TDB.symUnionDefaultGraph) ;
    }
    
    // Set after a transaction.
    @Test public void uniontxn_ds_rr()
    {
        ds.begin(READ) ;
        ds.commit(); 
        ds.end() ;
        
        ds.getContext().setTrue(TDB.symUnionDefaultGraph) ;
        test(ReadWrite.READ) ;
        //ds.getContext().unset(TDB.symUnionDefaultGraph) ;
    }
    
    @Test public void uniontxn_ds_wr()
    {
        ds.begin(WRITE) ;
        ds.commit(); 
        ds.end() ;
        
        ds.getContext().setTrue(TDB.symUnionDefaultGraph) ;
        test(ReadWrite.READ) ;
        //ds.getContext().unset(TDB.symUnionDefaultGraph) ;
    }
    
    @Test public void uniontxn_ds_ww()
    {
        ds.begin(WRITE) ;
        ds.commit(); 
        ds.end() ;
        
        ds.getContext().setTrue(TDB.symUnionDefaultGraph) ;
        test(ReadWrite.WRITE) ;
        //ds.getContext().unset(TDB.symUnionDefaultGraph) ;
    }
    
    @Test public void uniontxn_ds_rw()
    {
        ds.begin(READ) ;
        ds.commit(); 
        ds.end() ;
        
        ds.getContext().setTrue(TDB.symUnionDefaultGraph) ;
        test(ReadWrite.WRITE) ;
        //ds.getContext().unset(TDB.symUnionDefaultGraph) ;
    }
    
    @Test public void uniontxn_update()
    {
        String x = StrUtils.strjoinNL("BASE <http://example/>",
                                      "CLEAR ALL ; ", 
                                      "INSERT DATA { GRAPH <urn:g> { <s> <p> 1}} ; ",
                                      "INSERT { GRAPH <urn:g99> { ?s ?p 99} } WHERE  { ?s ?p 1 }"
                                      ) ;
        Dataset ds = TDBFactory.createDataset() ; 
        ds.getContext().setTrue(TDB.symUnionDefaultGraph) ;
        
        ds.begin(WRITE) ;
        UpdateRequest req = UpdateFactory.create(x) ;
        UpdateAction.execute(req, ds) ;
        ds.commit() ;
        ds.end() ;
        
        ds.begin(READ) ;
        assertEquals(1, ds.getNamedModel("urn:g99").size()) ;
        assertEquals(1, ds.getNamedModel("urn:g").size()) ;
        assertEquals(2, ds.getNamedModel(Quad.unionGraph.getURI()).size()) ;
        ds.end() ;
    }

    
    private void test(ReadWrite mode)
    {
        ds.begin(mode) ;
        Query q = QueryFactory.create("SELECT * { { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o }}}") ;
        QueryExecution qExec = QueryExecutionFactory.create(q, ds) ;
        long count = ResultSetFormatter.consume(qExec.execSelect()) ;
        ds.commit() ;
        ds.end() ;
        assertEquals(2, count) ;
    }
    
    @Test public void uniontxn05()
    {
        test2(READ) ;
    }
    
    @Test public void uniontxn06()
    {
        test2(WRITE) ;
    }
    
    // Sets the context of the execution  
    private void test2(ReadWrite mode)
    {
        ds.begin(mode) ;
        Query q = QueryFactory.create("SELECT * { { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o }}}") ;
        QueryExecution qExec = QueryExecutionFactory.create(q, ds) ;
        qExec.getContext().setTrue(TDB.symUnionDefaultGraph) ;
        long count = ResultSetFormatter.consume(qExec.execSelect()) ;
        ds.commit() ;
        ds.end() ;
        assertEquals(2, count) ;
    }

}

