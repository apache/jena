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

package com.hp.hpl.jena.sparql.api ;

import static org.apache.jena.atlas.lib.Lib.sleep ;

import java.util.concurrent.TimeUnit ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.query.QueryCancelledException ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.function.FunctionRegistry ;
import com.hp.hpl.jena.sparql.function.library.wait ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class TestQueryExecutionTimeout1 extends BaseTest
{
    static Graph                g   = SSE.parseGraph("(graph (<s> <p> <o1>) (<s> <p> <o2>) (<s> <p> <o3>))") ;
    static DatasetGraph         dsg = DatasetGraphFactory.createOneGraph(g) ;
    static Dataset              ds  = DatasetFactory.create(dsg) ;

    private static final String ns  = "http://example/ns#" ;

    @BeforeClass
    public static void beforeClass()
    {
        FunctionRegistry.get().put(ns + "wait", wait.class) ;
    }

    @AfterClass
    public static void afterClass()
    {
        FunctionRegistry.get().remove(ns + "wait") ;
    }

    static private String prefix = 
        "PREFIX f:       <http://example/ns#>\n"+
        "PREFIX afn:     <http://jena.hpl.hp.com/ARQ/function#>\n" ;

    // Numbers all a bit iffy and can result in test failures
    // on a heavily loaded CI system.
    // But we don't want testing to be to slow when used in general
    // development.  Could split into development and integration
    // level checking.
    
    @Test
    public void timeout_01()
    {
        String qs = prefix + "SELECT * { ?s ?p ?o }" ;
        QueryExecution qExec = QueryExecutionFactory.create(qs, ds) ;
        qExec.setTimeout(50, TimeUnit.MILLISECONDS) ;
        ResultSet rs = qExec.execSelect() ;
        sleep(120) ;
        exceptionExpected(rs) ; 
    }

    @Test
    public void timeout_02()
    {
        String qs = prefix + "SELECT * { ?s ?p ?o }" ;
        QueryExecution qExec = QueryExecutionFactory.create(qs, ds) ;
        qExec.setTimeout(50, TimeUnit.MILLISECONDS) ;
        ResultSet rs = qExec.execSelect() ;
        rs.next() ;
        sleep(75) ;
        exceptionExpected(rs) ; 
    }

    @Test
    public void timeout_03()
    {
        String qs = prefix + "SELECT * { ?s ?p ?o }" ;
        @SuppressWarnings("resource")
        QueryExecution qExec = QueryExecutionFactory.create(qs, ds) ;
        qExec.setTimeout(100, TimeUnit.MILLISECONDS) ;
        ResultSet rs = qExec.execSelect() ;
        ResultSetFormatter.consume(rs) ;
        qExec.close() ;
        qExec.abort() ;
    }

    @Test
    public void timeout_04()
    {
        String qs = prefix + "SELECT * { ?s ?p ?o }" ;
        try(QueryExecution qExec = QueryExecutionFactory.create(qs, ds)) {
            qExec.setTimeout(50, TimeUnit.MILLISECONDS) ;
            ResultSet rs = qExec.execSelect() ;
            ResultSetFormatter.consume(rs) ;
            sleep(100) ;
            rs.hasNext() ;         // Query ended - calling rs.hasNext() is safe.
        }
    }

//    @Test
//    public void timeout_05()
//    {
//        // This test is hard to get stable.
//        String qs = prefix + "SELECT * { ?s ?p ?o FILTER f:wait(200) }" ;
//        try(QueryExecution qExec = QueryExecutionFactory.create(qs, ds)) {
//            qExec.setTimeout(50, TimeUnit.MILLISECONDS) ;
//            ResultSet rs = qExec.execSelect() ;
//            exceptionExpected(rs) ; 
//        }
//    }
    
    @Test
    public void timeout_06()
    {
        String qs = prefix + "SELECT * { ?s ?p ?o FILTER f:wait(1) }" ; // Sleep in execution to kick timer thread.
        try(QueryExecution qExec = QueryExecutionFactory.create(qs, ds)) {
            qExec.setTimeout(100, TimeUnit.MILLISECONDS) ;
            ResultSet rs = qExec.execSelect() ;
            ResultSetFormatter.consume(rs) ;
        }
    }
    
    @Test
    public void timeout_07()
    {
        // No timeout.
        String qs = prefix + "SELECT * { ?s ?p ?o FILTER f:wait(1) }" ;
        try(QueryExecution qExec = QueryExecutionFactory.create(qs, ds)) {
            ResultSet rs = qExec.execSelect() ;
            ResultSetFormatter.consume(rs) ;
        }
    }

    @Test
    public void timeout_08()
    {
        // No timeout.
        String qs = prefix + "SELECT * { ?s ?p ?o FILTER f:wait(1) }" ;
        try(QueryExecution qExec = QueryExecutionFactory.create(qs, ds)) {
            qExec.setTimeout(-1, TimeUnit.MILLISECONDS) ;
            ResultSet rs = qExec.execSelect() ;
            ResultSetFormatter.consume(rs) ;
        }
    }

    @Test
    public void timeout_09()
    {
        String qs = prefix + "SELECT * { ?s ?p ?o }" ;
        try(QueryExecution qExec = QueryExecutionFactory.create(qs, ds)) {
            qExec.setTimeout(500, TimeUnit.MILLISECONDS, -1, TimeUnit.MILLISECONDS) ;
            ResultSet rs = qExec.execSelect() ;
            rs.next() ; // First timeout does not go off. Resets timers.
            rs.next() ; // Second timeout never goes off 
            assertTrue(rs.hasNext()) ;
            ResultSetFormatter.consume(rs) ; // Second timeout does not go off.
        }
    }

    @Test
    public void timeout_10()
    {
        String qs = prefix + "SELECT * { ?s ?p ?o }" ;
        try(QueryExecution qExec = QueryExecutionFactory.create(qs, ds)) {
            qExec.setTimeout(100, TimeUnit.MILLISECONDS, 100, TimeUnit.MILLISECONDS) ;
            ResultSet rs = qExec.execSelect() ;
            rs.next() ; // First timeout does not go off. Resets timers.
            rs.next() ; // Second timeout never goes off 
            assertTrue(rs.hasNext()) ;
            sleep(200) ;
            exceptionExpected(rs) ; 
        }
    }
    
    @Test
    public void timeout_11()
    {
        String qs = prefix + "SELECT * { ?s ?p ?o }" ;
        try(QueryExecution qExec = QueryExecutionFactory.create(qs, ds)) {
            qExec.setTimeout(-1, TimeUnit.MILLISECONDS, 100, TimeUnit.MILLISECONDS) ;
            ResultSet rs = qExec.execSelect() ;
            rs.next() ; // First timeout does not go off. Resets timer.
            rs.next() ; // Second timeout does not go off
            sleep(200) ;
            exceptionExpected(rs) ; 
        }
    }
    
    // Set timeouts via context.
    
    @Test
    public void timeout_20()
    {
        String qs = prefix + "SELECT * { ?s ?p ?o }" ;
        ARQ.getContext().set(ARQ.queryTimeout, "20") ;
        QueryExecution qExec = QueryExecutionFactory.create(qs, ds) ;
        ResultSet rs = qExec.execSelect() ;
        sleep(50) ;
        exceptionExpected(rs) ; 
    }
    
    @Test
    public void timeout_21()
    {
        String qs = prefix + "SELECT * { ?s ?p ?o }" ;
        ARQ.getContext().set(ARQ.queryTimeout, "20,10") ;
        QueryExecution qExec = QueryExecutionFactory.create(qs, ds) ;
        ResultSet rs = qExec.execSelect() ;
        sleep(50) ;
        exceptionExpected(rs) ; 
    }

    @Test
    public void timeout_22()
    {
        String qs = prefix + "SELECT * { ?s ?p ?o }" ;
        ARQ.getContext().set(ARQ.queryTimeout, "-1") ;
        try(QueryExecution qExec = QueryExecutionFactory.create(qs, ds)) {
            ResultSet rs = qExec.execSelect() ;
            ResultSetFormatter.consume(rs) ;
        }
    }

    private static void exceptionExpected(ResultSet rs)
    {
        try { ResultSetFormatter.consume(rs) ; fail("QueryCancelledException expected") ; } catch (QueryCancelledException ex) {}
    }
    
}
