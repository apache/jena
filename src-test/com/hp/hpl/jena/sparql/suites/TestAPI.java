/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.suites;

import junit.framework.*;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.sparql.util.GraphUtils;

/** com.hp.hpl.jena.query.test.TestMisc
 * 
 * @author Andy Seaborne
 */

public class TestAPI extends TestCase
{
    public static TestSuite suite()
    {
        TestSuite ts = new TestSuite(TestAPI.class) ;
        ts.setName("TestMisc") ;
        return ts ;
    }
    
    private static final String ns = "http://example/ns#" ;
    
    Model m = null ;
    
    
    public void setUp()
    {
        m = GraphUtils.makeJenaDefaultModel() ;
        Resource r1 = m.createResource() ;
        
        Property p1 = m.createProperty(ns+"p1") ;
        Property p2 = m.createProperty(ns+"p1") ;
        Property p3 = m.createProperty(ns+"p1") ;
        
        m.add(r1, p1, "x1") ;
        m.add(r1, p2, "X2") ; // NB Capital
        m.add(r1, p3, "y1") ;
    }

    public void tearDown()
    {}
    
    public void testInitialBindingsConstruct()
    {
        QueryExecution qExec = makeQExec("CONSTRUCT {?s ?p ?z} {?s ?p 'x1'}") ;
        QuerySolutionMap init = new QuerySolutionMap() ;
        init.add("z", m.createLiteral("zzz"));
        
        qExec.setInitialBinding(init) ;
        Model r = qExec.execConstruct() ;
    
        assertTrue("Empty model", r.size() > 0 ) ;
    
        Property p1 = m.createProperty(ns+"p1") ;
    
        assertTrue("Empty model", r.contains(null,p1, init.get("z"))) ; 
        
        qExec.close() ;
    }

    public void test_API1()
    {
        QueryExecution qExec = makeQExec("SELECT * {?s ?p ?o}") ;
        try {
            ResultSet rs = qExec.execSelect() ;
            assertTrue("No results", rs.hasNext()) ;
            QuerySolution qs = rs.nextSolution() ;
            Resource qr = qs.getResource("s") ;
            assertSame("Not the same model as queried", qr.getModel(), m) ;
        } finally { qExec.close() ; }
        
    }
    
//    public void test_OptRegex1()
//    {
//        execRegexTest(1, "SELECT * {?s ?p ?o . FILTER regex(?o, '^x')}") ;
//    }
//
//    public void test_OptRegex2()
//    {
//        execRegexTest(2, "SELECT * {?s ?p ?o . FILTER regex(?o, '^x', 'i')}") ;
//    }

    public void testInitialBindings1()
    {
        QueryExecution qExec = makeQExec("SELECT * {?s ?p ?o}") ;
        QuerySolutionMap init = new QuerySolutionMap() ;
        init.add("o", m.createLiteral("y1"));
        qExec.setInitialBinding(init) ;
        int count = queryAndCount(qExec) ;
        assertEquals("Initial binding didn't restrict query properly", 1, count) ;
    }
    
    public void testInitialBindings2()
    {
        QueryExecution qExec = makeQExec("SELECT * {?s ?p ?o}") ;
        QuerySolutionMap init = new QuerySolutionMap() ;
        init.add("z", m.createLiteral("zzz"));
        qExec.setInitialBinding(init) ;
        int count = queryAndCount(qExec) ;
        assertEquals("Initial binding restricted query improperly", 3, count) ;
    }

    public void testInitialBindings3()
    {
        QueryExecution qExec = makeQExec("SELECT * {?s ?p 'x1'}") ;
        QuerySolutionMap init = new QuerySolutionMap() ;
        init.add("z", m.createLiteral("zzz"));
        qExec.setInitialBinding(init) ;
        ResultSet rs = qExec.execSelect() ;
        QuerySolution qs = rs.nextSolution() ;
        assertTrue("Initial setting not set correctly now", qs.getLiteral("z").getLexicalForm().equals("zzz")) ;
        qExec.close() ;
    }
    
//    // Execute a test both with and without regex optimization enabled
//    // Check the number of results
//    private void XexecRegexTest(int expected, String queryString)
//    {
//        Object b = ARQ.getContext().get(ARQ.enableRegexConstraintsOpt) ;
//        try {
//            ARQ.getContext().set(ARQ.enableRegexConstraintsOpt, "false") ;
//            int count1 = queryAndCount(queryString) ;
//            ARQ.getContext().set(ARQ.enableRegexConstraintsOpt, "true") ;
//            int count2 = queryAndCount(queryString) ;
//            assertEquals("Different number of results", count1, count2) ;
//            if ( expected >= 0 )
//                assertEquals("Unexpected number of results", expected, count1) ;
//        } finally {
//            ARQ.getContext().set(ARQ.enableRegexConstraintsOpt, b) ;
//        }
//    }
    
    private QueryExecution makeQExec(String queryString)
    {
        Query q = QueryFactory.create(queryString) ;
        QueryExecution qExec = QueryExecutionFactory.create(q, m) ;
        return qExec ;
    }

    private int queryAndCount(String queryString)
    {
        QueryExecution qExec = makeQExec(queryString) ;
        return queryAndCount(qExec) ;
    }

    
    private int queryAndCount(QueryExecution qExec)
    {
        try {
            ResultSet rs = qExec.execSelect() ;
            return ResultSetFormatter.consume(rs) ;
        } finally { qExec.close() ; }
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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