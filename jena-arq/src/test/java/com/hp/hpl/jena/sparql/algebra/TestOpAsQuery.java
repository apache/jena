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

import java.util.Map;

import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Query;

import org.apache.jena.atlas.lib.StrUtils;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for {@link OpAsQuery}
 */
public class TestOpAsQuery {

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
        Query[] result = checkQuery("select (count(?s) as ?sc) (count(?p) as ?pc) { ?s ?p ?o }");
        assertEquals(2, result[1].getResultVars().size());
        assertTrue(result[1].getResultVars().contains("sc"));
        assertTrue(result[1].getResultVars().contains("pc"));
    }
    
    /* JENA-166 */
    @Test
    public void testGroupWithExpression() {
        Object[] result = checkQuery("SELECT (sample(?a) + 1 AS ?c) {} GROUP BY ?x");
        assertEquals(result[0], result[1]);
    }
    
    @Test
    public void testProject1() {
        Object[] result = checkQuery("SELECT (?x + 1 AS ?c) {}");
        assertEquals(result[0], result[1]);
    }
    
    @Test
    public void testProject2() {
        Query[] result = checkQuery("SELECT (?x + 1 AS ?c) ?d {}");
        assertEquals(2, result[1].getResultVars().size());
        assertTrue(result[1].getResultVars().contains("c"));
        assertTrue(result[1].getResultVars().contains("d"));
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
    
    @Test
    public void testExtend1() {
        //Top Level BIND should now be round trippable
        Query[] result = checkQuery("SELECT * WHERE { ?s ?p ?o . BIND(?o AS ?x) }");
        assertNotEquals(result[0], result[1]);
        assertTrue(result[1].getResultVars().contains("x"));
    }
    
    @Test
    public void testExtend2() {
        //Nested BIND should always have been round trippable
        Query[] result = checkQuery("SELECT * WHERE { GRAPH ?g { ?s ?p ?o . BIND(?o AS ?x) } }");
        assertEquals(result[0], result[1]);
    }
    
    @Test
    public void testExtend3() {
        //JENA-429
        String query = StrUtils.strjoinNL
                ("PREFIX : <http://www.cipe.accamargo.org.br/ontologias/h2tc.owl#>" ,
                 "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" ,
                 "PREFIX mylib: <java:dateadd.lib.pkgfor.arq.>",
                 "",
                 "SELECT ?yearmonth ( count(?document) as ?total )", 
                 "{" ,
                 "    ?document a :Document;",
                 "   :documentDateOfCreation ?date ;",
                 "   :documentType \"exam results\" ." ,
                 "    BIND( mylib:DateFormat( xsd:string(?date), \"yyyy-MM\" ) as ?yearmonth )",
                "} group by ?yearmonth") ;
        checkQueryParseable(query, true);
    }
    
    @Test
    public void testExtend4() {
        //Simplified repo of JENA-429
        String query  = "SELECT ?key (COUNT(?member) AS ?total) WHERE { ?s ?p ?o . BIND(LCASE(?o) AS ?key) } GROUP BY ?key";
        checkQueryParseable(query, true);
    }
    
    @Test
    public void testExtendInService() {
        //Original test case from JENA-422
        Query[] result = checkQuery("SELECT * WHERE { SERVICE <http://example/endpoint> { ?s ?p ?o . BIND(?o AS ?x) } }");
        assertEquals(result[0], result[1]);
        assertTrue(result[1].toString().contains("BIND"));
    }
    
    @Test
    public void testSubQuery1() {
        String query = "SELECT ?s WHERE { { SELECT ?s ?p WHERE { ?s ?p ?o } } }";
        checkQueryParseable(query, true);
    }
    
    @Test
    public void testSubQuery2() {
        String query = "SELECT ?s ?x WHERE { { SELECT ?s ?p WHERE { ?s ?p ?o } } { SELECT ?x WHERE { ?x ?p ?o } } }";
        //These end up being non-equal queries because the nesting in the final query is a little funky
        //but the results should still be semantically equivalent
        checkQueryParseable(query, false);
    }
    
    @Test
    public void testSubQuery3() {
        String query = "SELECT * WHERE { { SELECT ?s ?p WHERE { ?s ?p ?o } } { SELECT ?x WHERE { ?x ?p ?o } } }";
        //In this case there is insufficient information to correctly reverse translate the algebra so this query
        //will not round trip
        checkQueryNonRecoverable(query);
    }
    
    @Test
    public void testAggregatesInSubQuery1() {
        //Simplified form of a test case provided via the mailing list (JENA-445)
        String query = "SELECT ?key ?agg WHERE { { SELECT ?key (COUNT(*) AS ?agg) { ?key ?p ?o } GROUP BY ?key } }";
        checkQueryParseable(query, true);
    }
    
    @Test
    public void testAggregatesInSubQuery2() {
        //Simplified form of a test case provided via the mailing list (JENA-445)
        String query = "SELECT * WHERE { { SELECT ?key (COUNT(*) AS ?agg) { ?key ?p ?o } GROUP BY ?key } }";
        checkQueryParseable(query, false);
    }
    
    @Test
    public void testAggregatesInSubQuery3() {
        //Actual test case from JENA-445 bug report
        String queryString = 
                "PREFIX dcterms: <http://purl.org/dc/terms/> \n" + 
                "PREFIX dbpedia: <http://dbpedia.org/resource/> \n" + 

                "SELECT ?num_of_holidays ?celebrate_Chinese_New_Year WHERE { \n" + 
                "{" + 
                "SELECT ?country_cat (COUNT(?holiday) as ?num_of_holidays) \n" + 
                "WHERE {" + 
                "?country_cat <http://www.w3.org/2004/02/skos/core#broader> <http://dbpedia.org/resource/Category:Public_holidays_by_country>. \n" + 
                "?holiday dcterms:subject ?country_cat \n" + 
                "}GROUP by ?country_cat \n" + 
                "} \n" + 
                "{ \n" + 
                "SELECT ?country_cat (COUNT(?holiday) as ?celebrate_Chinese_New_Year) \n" + 
                "WHERE { \n" + 
                "?country_cat <http://www.w3.org/2004/02/skos/core#broader> <http://dbpedia.org/resource/Category:Public_holidays_by_country>. \n" + 
                "?holiday dcterms:subject ?country_cat \n" + 
                "FILTER(?holiday=\"http://dbpedia.org/resource/Lunar_New_Year\'s_Day\") \n" + 
                "}GROUP by ?country_cat \n" + 
                "} \n" + 
                "}\n"; 
        checkQuadQuery(queryString);
    }
    
    @Test
    public void testPathExpressions1() {
        // test that the query after serialization is legal (as much a test of the serializer as way OpAsQuery works)
        String query = "PREFIX : <http://example/> SELECT * { ?s :p* ?o . ?x :r 123 . }" ;
        Query r[] = checkQueryParseable(query, false);
    }
        
    @Test
    public void testPathExpressions2() {
        // test that the query  
        String query = "PREFIX : <http://example/> SELECT * { ?s :p*/:q ?o . ?x :r 123 . }" ;
        Query r[] = checkQueryParseable(query, false);
    }

    @Test
    public void testMinus1() {
        String query = "PREFIX : <http://example/> SELECT * { ?s :p ?o MINUS { ?s :q ?v .FILTER(?v<5) } }" ; 
        Query r[] = checkQueryParseable(query, true);
    }
    
    @Test
    public void testMinus2() {
        // query gains a level of {} but the meaning is the same. 
        String query = "PREFIX : <http://example/> SELECT * { ?s :p ?o OPTIONAL { ?s :x ?2 } MINUS { ?s :q ?v .FILTER(?v<5) } }" ; 
        Query r[] = checkQueryParseable(query, false);
    }
    
    public Query[] checkQuery(String query) {
        Query orig = QueryFactory.create(query, Syntax.syntaxSPARQL_11);
        Op toReconstruct = Algebra.compile(orig);
        Query got = OpAsQuery.asQuery(toReconstruct);
        Query[] r = { orig, got };
        return r;
    }
    
    public Query[] checkQuadQuery(String query) {
        Query orig = QueryFactory.create(query, Syntax.syntaxSPARQL_11);
        Op toReconstruct = Algebra.compile(orig);
        toReconstruct = Algebra.toQuadForm(toReconstruct);
        Query got = OpAsQuery.asQuery(toReconstruct);
        Query[] r = { orig, got };
        return r;
    }
    
    public Query[] checkQueryParseable(String query, boolean expectEquals) {
        Query[] r = checkQuery(query);
        
        // Strip namespaces and Base URI from each so comparison is not affected by those
        stripNamespacesAndBase(r[0]);
        stripNamespacesAndBase(r[1]);
        
        if (expectEquals) {
            // Expecting the string forms of the queries to be equal
            // If the strings forms are equal their algebras will be
            Assert.assertEquals(r[0], r[1]);
        } else {
            // Even if the strings come out as non-equal because of the translatation from algebra to query
            // the algebras should be equal
            // i.e. the queries should remain semantically equivalent
            Assert.assertNotEquals(r[0], r[1]);
            Op a1 = Algebra.compile(r[0]);
            Op a2 = Algebra.compile(r[1]);
            Assert.assertEquals(a1, a2);
        }
        String query2 = r[1].toString();
        Query q = QueryFactory.create(query2);
        return r;
    }
    
    public Query[] checkQueryNonRecoverable(String query) {
        Query[] r = checkQuery(query);
        
        // Strip namespaces and Base URI from each so comparison is not affected by those
        stripNamespacesAndBase(r[0]);
        stripNamespacesAndBase(r[1]);
        
        // If this method is being called then we expect the strings to be non-equal and also
        // the algebras to be non-equivalent because there will be insufficient information
        // in the algebra to allow it to be translated back into a semantically equivalent query
        Assert.assertNotEquals(r[0], r[1]);
        Op a1 = Algebra.compile(r[0]);
        Op a2 = Algebra.compile(r[1]);
        Assert.assertNotEquals(a1, a2);
        
        return r;
    }
    
    protected void stripNamespacesAndBase(Query q) {
        Map<String, String> prefixes = q.getPrefixMapping().getNsPrefixMap();
        for (String prefix : prefixes.keySet()) {
            q.getPrefixMapping().removeNsPrefix(prefix);
        }
        q.setBaseURI((String)null);
    }
}
