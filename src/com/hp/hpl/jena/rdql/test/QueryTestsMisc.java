/*
 * (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdql.test;

import junit.framework.* ;

import java.util.* ;

import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.rdql.*;

/** Bunch of other tests.
 *   Workign with associated triples
 *  
 * @author Andy Seaborne
 * @version $Id: QueryTestsMisc.java,v 1.4 2004-08-31 09:49:52 andy_seaborne Exp $
 */

public class QueryTestsMisc extends TestSuite
{
	static final String testSetName = "RDQL - Query - Other" ;
    public static String baseURI = "http://rdql/" ;

    public static TestSuite suite()
    {
    	return new QueryTestsMisc(testSetName) ;
    }
    	
    	
    private QueryTestsMisc(String name)
    {
    	super(name) ;
    	
        try {
            //suite.addTest(new TestQuery("RDQL Query "));
            addTest(new TestQueryTriplesMerge()) ;
            addTest(new TestQueryGetTriples()) ;
            addTest(new TestQueryGetTriples2()) ;
        } catch (Exception ex)
        {
            System.err.println("Problems making RDQL test") ;
            ex.printStackTrace(System.err) ;
            return ;
        }
        // Tests of templates
    }

    static abstract class TestQueryTriples extends TestCase
    {
        Model model ;
        Resource r ;
        
        TestQueryTriples(String testName)
        {
            super(testName) ;
            model = ModelFactory.createDefaultModel() ;
            r = model.createResource(baseURI+"r") ;
            r.addProperty(model.createProperty(baseURI+"p1"), "v1") ;
            r.addProperty(model.createProperty(baseURI+"p2"), "v2") ;
        }
    }
    
    static class TestQueryTriplesMerge extends TestQueryTriples
    {
        
        TestQueryTriplesMerge() { super("TestQueryTriplesMerge" ) ; }
        
        protected void runTest() throws Throwable
        {
            Model model2 = ModelFactory.createDefaultModel() ;
            
            String queryString = "SELECT * WHERE (<"+r.getURI()+"> ?p ?v)" ;
            
            Query query = new Query(queryString) ;
            query.setSource(model);
            QueryExecution qe = new QueryEngine(query) ;
            QueryResults results = qe.exec() ;

            for ( ; results.hasNext() ; )
            {
                 ResultBindingImpl rb = (ResultBindingImpl)results.next() ;
                 rb.mergeTriples(model2) ;
            }
            results.close() ;
            
            assertTrue(getName()+": merged rsults not the same as target model",
                       model.isIsomorphicWith(model2)) ;
        }
    }
    
    static class TestQueryGetTriples extends TestQueryTriples
    {
        
        TestQueryGetTriples() { super("TestQueryGetTriples" ) ; }
        
        protected void runTest() throws Throwable
        {
            Model model2 = ModelFactory.createDefaultModel() ;
            
            String queryString = "SELECT * WHERE (<"+r.getURI()+"> ?p ?v)" ;
            
            Query query = new Query(queryString) ;
            query.setSource(model);
            QueryExecution qe = new QueryEngine(query) ;
            QueryResults results = qe.exec() ;

            int i = 0 ;
            for ( ; results.hasNext() ; )
            {
                i++ ;
                ResultBindingImpl rb = (ResultBindingImpl)results.next() ;
                assertTrue(getName()+": getTriples(loop "+i+")",                           rb.getTriples().size() == 1) ; 
            }
            results.close() ;
        }
    }

    static class TestQueryGetTriples2 extends TestQueryTriples
    {
        
        TestQueryGetTriples2() { super("TestQueryGetTriples2" ) ; }
        
        protected void runTest() throws Throwable
        {
            Model model2 = ModelFactory.createDefaultModel() ;
            
            String queryString = "SELECT * WHERE (<"+r.getURI()+"> ?p ?v)" ;
            
            Query query = new Query(queryString) ;
            query.setSource(model);
            QueryExecution qe = new QueryEngine(query) ;
            QueryResults results = qe.exec() ;

            int i = 0 ;
            for ( ; results.hasNext() ; )
            {
                i++ ;
                ResultBindingImpl rb = (ResultBindingImpl)results.next() ;
                Set s = rb.getTriples() ;
                // Try again - ensure caching works.
                assertTrue(getName()+": getTriples2(loop "+i+")",
                           rb.getTriples().size() == 1) ; 
            }
            results.close() ;
        }
    }

}
    
/*
 *  (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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

