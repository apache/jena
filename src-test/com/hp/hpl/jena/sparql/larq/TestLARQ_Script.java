/**
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

package com.hp.hpl.jena.sparql.larq;

import junit.framework.JUnit4TestAdapter ;
import junit.framework.TestCase ;
import org.junit.Test ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.query.larq.IndexBuilderModel ;
import com.hp.hpl.jena.query.larq.IndexBuilderString ;
import com.hp.hpl.jena.query.larq.IndexBuilderSubject ;
import com.hp.hpl.jena.query.larq.IndexLARQ ;
import com.hp.hpl.jena.query.larq.LARQ ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.sparql.junit.QueryTest ;
import com.hp.hpl.jena.sparql.resultset.ResultSetRewindable ;
import com.hp.hpl.jena.util.FileManager ;
import com.hp.hpl.jena.vocabulary.DC ;

public class TestLARQ_Script extends TestCase
{
    public static junit.framework.Test suite()
    {
        return new JUnit4TestAdapter(TestLARQ_Script.class) ;
    }
    
//    public static TestSuite suite()
//    {
//        TestSuite ts = new TestSuite(TestLARQ2.class) ;
//        ts.setName("LARQ-Scripts") ;
//        return ts ;
//    }
    
    static final String root = "testing/LARQ/" ;
//    static final String datafile = "testing/LARQ/data-1.ttl" ;
//    static final String results1 = "testing/LARQ/results-1.srj" ;
//    static final String results2 = "testing/LARQ/results-2.srj" ;
//    static final String results3 = "testing/LARQ/results-3.srj" ;
    
    public TestLARQ_Script(String name)
    { 
        super(name) ;
    }
    
    // See TestLARQ.
    
    static void runTestScript(String queryFile, String dataFile, String resultsFile, IndexBuilderModel builder)
    {
        Query query = QueryFactory.read(root+queryFile) ;
        IndexBuilderString larqBuilder = new IndexBuilderString() ;
        Model model = ModelFactory.createDefaultModel() ; 
        model.register(builder) ;    
        FileManager.get().readModel(model, root+dataFile) ;
        model.unregister(builder) ;
        
        IndexLARQ index = builder.getIndex() ;
        LARQ.setDefaultIndex(index) ;
        
        QueryExecution qe = QueryExecutionFactory.create(query, model) ;
        ResultSetRewindable rsExpected = 
            ResultSetFactory.makeRewindable(ResultSetFactory.load(root+resultsFile)) ;
        
        ResultSetRewindable rsActual = 
            ResultSetFactory.makeRewindable(qe.execSelect()) ;
        boolean b = QueryTest.resultSetEquivalent(query, rsActual, rsExpected) ;
        if ( ! b ) 
        {
            rsActual.reset() ;
            rsExpected.reset() ;
            System.out.println("==== Different (LARQ)") ;
            System.out.println("== Actual") ;
            ResultSetFormatter.out(rsActual) ;
            System.out.println("== Expected") ;
            ResultSetFormatter.out(rsExpected) ;
        }
        
        assertTrue(b) ;
        qe.close() ; 
        LARQ.removeDefaultIndex() ;
    }
    
    @Test public void test_larq_1()
    { runTestScript("larq-q-1.rq", "data-1.ttl", "results-1.srj", new IndexBuilderString()) ; }

    @Test public void test_larq_2()
    { runTestScript("larq-q-2.rq", "data-1.ttl", "results-2.srj", new IndexBuilderString(DC.title)) ; }

    @Test public void test_larq_3()
    { runTestScript("larq-q-3.rq", "data-1.ttl", "results-3.srj", new IndexBuilderSubject(DC.title)) ; }
    
    @Test public void test_larq_4()
    { runTestScript("larq-q-4.rq", "data-1.ttl", "results-4.srj", new IndexBuilderString()) ; }
    
    @Test public void test_larq_5()
    { runTestScript("larq-q-5.rq", "data-1.ttl", "results-5.srj", new IndexBuilderString()) ; }

    @Test public void test_larq_6()
    { runTestScript("larq-q-6.rq", "data-1.ttl", "results-6.srj", new IndexBuilderString()) ; }

    @Test public void test_larq_7()
    { runTestScript("larq-q-7.rq", "data-1.ttl", "results-7.srj", new IndexBuilderString()) ; }
}
