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

package com.hp.hpl.jena.sparql.algebra;

import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Query;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for {@link OpAsQuery}
 */
public class OpAsQueryTest {

    /**
     * Test of asQuery method, of class OpAsQuery.
     */
    @Test
    public void testCountStar() {
        Object[] result = checkQuery("select (count(*) as ?cs) { ?s ?p ?o }");
        assertEquals(result[0], result[1]);
    }
    
    @Test
    public void testCountGroup() {
        Object[] result = checkQuery("select (count(?p) as ?cp) { ?s ?p ?o } group by ?s");
        assertEquals(result[0], result[1]);
    }
    
    @Test
    public void testCountGroupAs() {
        Object[] result = checkQuery("select (count(?p) as ?cp) { ?s ?p ?o }");
        assertEquals(result[0], result[1]);
    }
    
    @Test
    public void testDoubleCount() {
        Object[] result = checkQuery("select (count(?s) as ?sc) (count(?p) as ?pc) { ?s ?p ?o }");
        assertEquals(result[0], result[1]);
    }
    
    /* JENA-166 */
    @Test
    public void testGroupWithExpression() {
        Object[] result = checkQuery("SELECT (sample(?a) + 1 AS ?c) {} GROUP BY ?x");
        assertEquals(result[0], result[1]);
    }
    
    // The next two tests represent an loss of information
    // I suspect the best solution is to let this pass and the other fail
    @Test
    public void testProject() {
        Object[] result = checkQuery("SELECT (?x + 1 AS ?c) {}");
        assertEquals(result[0], result[1]);
    }
    
    // Ambiguous, don't bother
    public void testBind() {
        Object[] result = checkQuery("SELECT ?c { BIND(?x + 1 AS ?c) }");
        assertEquals(result[0], result[1]);
    }
    
    // This BIND is distinguisable, however
    @Test
    public void testNestedBind() {
        Object[] result = checkQuery("SELECT ?c { { } UNION { BIND(?x + 1 AS ?c) } }");
        assertEquals(result[0], result[1]);
    }
    
    @Test
    public void testNestedProject() {
        Object[] result = checkQuery("SELECT (?x + 1 AS ?c) { { } UNION { } }");
        assertEquals(result[0], result[1]);
    }
    
    @Test
    public void testGroupExpression() {
        Object[] result = checkQuery("SELECT ?z { } GROUP BY (?x + ?y AS ?z)");
        assertEquals(result[0], result[1]);
    }
    
    @Test
    public void testNestedProjectWithGroup() {
        Object[] result = checkQuery("SELECT (SAMPLE(?c) as ?s) { {} UNION {BIND(?x + 1 AS ?c)} } GROUP BY ?x");
        assertEquals(result[0], result[1]);
    }
    
    @Test
    public void testQuadPatternInDefaultGraph() {
        Object[] result = checkQuadQuery("SELECT * WHERE { ?s a ?type }");
        assertEquals(result[0], result[1]);
    }
    
    @Test
    public void testGraphClauseUri() {
        Object[] result = checkQuery("SELECT * WHERE { GRAPH <http://example> { ?s a ?type } }");
        assertEquals(result[0], result[1]);
    }
    
    @Test
    public void testGraphClauseComplex() {
        Object[] result = checkQuery("SELECT * WHERE { GRAPH <http://example> { ?s a ?type . OPTIONAL { ?s <http://label> ?label } } }");
        assertEquals(result[0], result[1]);
    }
    
    @Test
    public void testQuadPatternInGraph() {
        Object[] result = checkQuadQuery("SELECT * WHERE { GRAPH <http://example> { ?s a ?type } }");
        assertEquals(result[0], result[1]);
    }
    
    @Test
    public void testQuadPatternInGraphComplex01() {
        //This fails because OpQuadPattern's are converted back to individual GRAPH clauses
        Object[] result = checkQuadQuery("SELECT * WHERE { GRAPH <http://example> { ?s a ?type . OPTIONAL { ?s <http://label> ?label } } }");
        assertFalse(result[0].equals(result[1]));
    }
    
    @Test
    public void testQuadPatternInGraphComplex02() {
        //This succeeds since each OpQuadPattern is from a single simple GRAPH clause
        Object[] result = checkQuadQuery("SELECT * WHERE { GRAPH <http://example> { ?s a ?type } OPTIONAL { GRAPH <http://example> { ?s <http://label> ?label } } }");
        assertEquals(result[0], result[1]);
    }
    
    public Object[] checkQuery(String query) {
        Query orig = QueryFactory.create(query, Syntax.syntaxSPARQL_11);
        Op toReconstruct = Algebra.compile(orig);
        Query got = OpAsQuery.asQuery(toReconstruct);
        Object[] r = { orig, got };
        return r;
    }
    
    public Object[] checkQuadQuery(String query) {
        Query orig = QueryFactory.create(query, Syntax.syntaxSPARQL_11);
        Op toReconstruct = Algebra.compile(orig);
        toReconstruct = Algebra.toQuadForm(toReconstruct);
        Query got = OpAsQuery.asQuery(toReconstruct);
        Object[] r = { orig, got };
        return r;
    }
}
