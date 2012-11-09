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

import java.util.Iterator;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.QuerySolutionMap ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.Property ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;
import com.hp.hpl.jena.vocabulary.OWL ;

public class TestAPI extends BaseTest
{
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
    
    @Test public void testInitialBindingsConstruct()
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

    @Test public void test_API1()
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
    
//    @Test public void test_OptRegex1()
//    {
//        execRegexTest(1, "SELECT * {?s ?p ?o . FILTER regex(?o, '^x')}") ;
//    }
//
//    @Test public void test_OptRegex2()
//    {
//        execRegexTest(2, "SELECT * {?s ?p ?o . FILTER regex(?o, '^x', 'i')}") ;
//    }

    @Test public void testInitialBindings0()
    {
        QuerySolutionMap smap1 = new QuerySolutionMap() ;
        QuerySolutionMap smap2 = new QuerySolutionMap() ;
        smap1.add("o", m.createLiteral("y1"));
        smap2.addAll(smap1) ;
        assertTrue(smap2.contains("o")) ;
        smap2.clear() ;
        assertFalse(smap2.contains("o")) ;
        assertTrue(smap1.contains("o")) ;
        
        QuerySolutionMap smap3 = new QuerySolutionMap() ;
        smap2.addAll((QuerySolution)smap1) ;
        assertTrue(smap2.contains("o")) ;
    }
    
    @Test public void testInitialBindings1()
    {
        QueryExecution qExec = makeQExec("SELECT * {?s ?p ?o}") ;
        QuerySolutionMap init = new QuerySolutionMap() ;
        init.add("o", m.createLiteral("y1"));
        qExec.setInitialBinding(init) ;
        int count = queryAndCount(qExec) ;
        assertEquals("Initial binding didn't restrict query properly", 1, count) ;
    }
    
    @Test public void testInitialBindings2()
    {
        QueryExecution qExec = makeQExec("SELECT * {?s ?p ?o}") ;
        QuerySolutionMap init = new QuerySolutionMap() ;
        init.add("z", m.createLiteral("zzz"));
        qExec.setInitialBinding(init) ;
        int count = queryAndCount(qExec) ;
        assertEquals("Initial binding restricted query improperly", 3, count) ;
    }

    @Test public void testInitialBindings3()
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
    
    @Test public void testInitialBindings4()
    {
        // Test derived from report by Holger Knublauch
        String queryString =
            "PREFIX : <"+ns+">\n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
            "SELECT * \n" +
            "WHERE { \n" +
            "    ?x :p1 ?z ." +
            "    NOT EXISTS { \n" +
            "        ?x rdfs:label ?z . \n" +
            "    }\n" +
            "}";
        
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
        QueryExecution qexec = QueryExecutionFactory.create(query, m);

        QuerySolutionMap map = new QuerySolutionMap();
        map.add("this", OWL.Thing);
        qexec.setInitialBinding(map);
        
        ResultSet rs = qexec.execSelect();
        while(rs.hasNext()) {
            QuerySolution qs = rs.nextSolution();
            //System.out.println("Result: " + qs);
        }
        qexec.close() ;
    }
    
    @Test public void testReuseQueryObject1()
    {
        String queryString = "SELECT * {?s ?p ?o}";
        Query q = QueryFactory.create(queryString) ;
        
        QueryExecution qExec = QueryExecutionFactory.create(q, m) ;
        int count = queryAndCount(qExec) ;
        assertEquals(3, count) ;
        
        qExec = QueryExecutionFactory.create(q, m) ;
        count = queryAndCount(qExec) ;
        assertEquals(3, count) ;
    }
    
    
    @Test public void testReuseQueryObject2()
    {
        String queryString = "SELECT (count(?s) AS ?c) {?s ?p ?o} GROUP BY ?s";
        Query q = QueryFactory.create(queryString) ;
        
        QueryExecution qExec = QueryExecutionFactory.create(q, m) ;
        
        ResultSet rs = qExec.execSelect() ;
        QuerySolution qs = rs.nextSolution() ;
        assertEquals(3, qs.getLiteral("c").getInt()) ;
        qExec.close() ;
        
        qExec = QueryExecutionFactory.create(q, m) ;
        rs = qExec.execSelect() ;
        qs = rs.nextSolution() ;
        assertEquals(3, qs.getLiteral("c").getInt()) ;
        qExec.close() ;
    }
    
    @Test public void testConstructRejectsBadTriples1()
    {
        String queryString = "CONSTRUCT { ?s ?p ?o } WHERE { ?o ?p ?s }";
        Query q = QueryFactory.create(queryString);
        
        QueryExecution qExec = QueryExecutionFactory.create(q, m);
        
        Model resultModel = qExec.execConstruct();
        assertEquals(0, resultModel.size());
    }
    
    @Test public void testConstructRejectsBadTriples2()
    {
        String queryString = "CONSTRUCT { ?s ?p ?o } WHERE { ?o ?p ?s }";
        Query q = QueryFactory.create(queryString);
        
        QueryExecution qExec = QueryExecutionFactory.create(q, m);
        
        Iterator<Triple> ts = qExec.execConstructTriples();
        long count = 0;
        while (ts.hasNext()) {
            count++;
            ts.next();
        }
        assertEquals(0, count);
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
