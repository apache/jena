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

package com.hp.hpl.jena.sparql.api;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryCancelledException ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.Property ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sparql.function.FunctionRegistry ;
import com.hp.hpl.jena.sparql.function.library.wait ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;

public class TestQueryExecutionCancel extends BaseTest {

    private static final String ns = "http://example/ns#" ;
    
    static Model m = GraphFactory.makeJenaDefaultModel() ;
    static Resource r1 = m.createResource() ;
    static Property p1 = m.createProperty(ns+"p1") ;
    static Property p2 = m.createProperty(ns+"p2") ;
    static Property p3 = m.createProperty(ns+"p3") ;
    static  {
        m.add(r1, p1, "x1") ;
        m.add(r1, p2, "X2") ; // NB Capital
        m.add(r1, p3, "y1") ;
    }
    
    @BeforeClass public static void beforeClass() { FunctionRegistry.get().put(ns + "wait", wait.class) ; }
    @AfterClass  public static void afterClass() { FunctionRegistry.get().remove(ns + "wait") ; }
    
    @Test(expected=QueryCancelledException.class)
    public void test_Cancel_API_1()
    {
        try(QueryExecution qExec = makeQExec("SELECT * {?s ?p ?o}")) {
            ResultSet rs = qExec.execSelect() ;
            assertTrue(rs.hasNext()) ;
            qExec.abort();
            assertTrue(rs.hasNext()) ;
            rs.nextSolution();
            assertFalse("Results not expected after cancel.", rs.hasNext()) ;
        }
    }
    
    @Test(expected=QueryCancelledException.class)
    public void test_Cancel_API_2()
    {
        try(QueryExecution qExec = makeQExec("PREFIX ex: <" + ns + "> SELECT * {?s ?p ?o . FILTER ex:wait(100) }")) {
            ResultSet rs = qExec.execSelect() ;
            assertTrue(rs.hasNext()) ;
            qExec.abort();
            assertTrue(rs.hasNext()) ;
            rs.nextSolution();
            assertFalse("Results not expected after cancel.", rs.hasNext()) ;
        }
    }    
    
    @Test public void test_Cancel_API_3() throws InterruptedException
    {
        // Don't qExec.close on this thread.
        QueryExecution qExec = makeQExec("PREFIX ex: <" + ns + "> SELECT * { ?s ?p ?o . FILTER ex:wait(100) }") ;
        CancelThreadRunner thread = new CancelThreadRunner(qExec);
        thread.start();
        synchronized (qExec) { qExec.wait() ; }
        synchronized (qExec) { qExec.abort() ;}
        synchronized (qExec) { qExec.notify() ; }
        assertEquals (1, thread.getCount()) ;
    }
    
    @Test public void test_Cancel_API_4() throws InterruptedException
    { 
        // Don't qExec.close on this thread.
        QueryExecution qExec = makeQExec("PREFIX ex: <" + ns + "> SELECT * { ?s ?p ?o } ORDER BY ex:wait(100)") ;
        CancelThreadRunner thread = new CancelThreadRunner(qExec);
        thread.start();
        synchronized (qExec) { qExec.wait() ; }
        synchronized (qExec) { qExec.abort(); }
        synchronized (qExec) { qExec.notify() ; }
        assertEquals (1, thread.getCount()) ;
    }

    private QueryExecution makeQExec(String queryString)
    {
        Query q = QueryFactory.create(queryString) ;
        QueryExecution qExec = QueryExecutionFactory.create(q, m) ;
        return qExec ;
    }

    class CancelThreadRunner extends Thread 
    {
    	private QueryExecution qExec = null ;
    	private int count = 0 ;

    	public CancelThreadRunner(QueryExecution qExec) 
    	{
    		this.qExec = qExec ;
    	}
    	
    	@Override
    	public void run() 
    	{
            try 
            {
                ResultSet rs = qExec.execSelect() ;
                while ( rs.hasNext() ) 
                {
                    rs.nextSolution() ;
                    count++ ;
                    synchronized (qExec) { qExec.notify() ; }
                    synchronized (qExec) { qExec.wait() ; }
                }
    		} 
            catch (QueryCancelledException e) {}
            catch (InterruptedException e) { 
                e.printStackTrace();
    		} finally { qExec.close() ; }
    	}
    	
    	public int getCount() 
    	{
    		return count ;
    	}
    }
}
