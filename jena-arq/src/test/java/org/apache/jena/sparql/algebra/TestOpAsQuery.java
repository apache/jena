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

package org.apache.jena.sparql.algebra;

import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.assertFalse ;
import static org.junit.Assert.assertTrue ;

import java.util.Map ;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryFactory ;
import org.apache.jena.query.Syntax ;
import org.apache.jena.sparql.algebra.Algebra ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.sse.SSE ;
import org.junit.Assert ;
import org.junit.Test ;

/**
 * Tests for {@link OpAsQuery}
 */
public class TestOpAsQuery {

    // Basic stuff
    @Test public void testBasic01() { test_roundTripQuery("SELECT * { }") ; }
    @Test public void testBasic02() { test_roundTripQuery("SELECT * { ?s ?p ?o }") ; }
    @Test public void testBasic03() { test_roundTripQuery("SELECT * { ?s ?p ?o FILTER(?o > 5) }") ; }
    @Test public void testBasic04() { test_roundTripQuery("SELECT ?s { ?s ?p ?o FILTER(?o > 5) }") ; }
    
    // 01, 02: Same algebra.  
    @Test public void testBind01() { test_roundTripQuery("SELECT ?s (?o + 5 AS ?B) { ?s ?p ?o }") ; }
    @Test public void testBind02() { test_roundTripAlegbra("SELECT ?o ?B  { ?s ?p ?o BIND (?o + 5 AS ?B) }") ; }
    // No project
    @Test public void testBind03() { test_roundTripQuery("SELECT * { ?s ?p ?o BIND (?o + 5 AS ?B)  }") ; }
    
    // Over nested.
    @Test public void testBind04() { 
        test_roundTripQuery("SELECT * { ?s ?p ?o BIND(?o+1 AS ?a1) ?x ?q ?v BIND(?v+2 AS ?a2) }",
                            "SELECT * { { ?s ?p ?o BIND(( ?o + 1 ) AS ?a1) } ?x ?q ?v BIND(( ?v + 2 ) AS ?a2) } "); 
    }
    
    // Over nested.
    @Test public void testBind05() { 
        test_roundTripQuery("SELECT * { ?s ?p ?o BIND(?o+1 AS ?a1) ?x ?q ?v BIND(2 AS ?a2) } ORDER BY ?s",
                            "SELECT * { { { ?s ?p ?o BIND(( ?o + 1 ) AS ?a1) } ?x ?q ?v } BIND(2 AS ?a2) } ORDER BY ?s"); 
    }
    
    // https://issues.apache.org/jira/browse/JENA-1843
    @Test public void testBind06() { test_roundTripQuery("SELECT * { ?s ?p ?o BIND(?o + 1 AS ?a1) BIND(?v+2 as ?a2) }"); }
    @Test public void testBind07() { test_roundTripQuery("SELECT * { BIND(?o + 1 AS ?a1) BIND(?v+2 as ?a2) }"); }
    
    @Test public void testOptional01() 
    { test_roundTripQuery("SELECT * WHERE { ?s ?p ?o OPTIONAL { ?s ?q ?z FILTER (?foo) } }") ; }
    
    // Double {{...}} matter here in SPARQL.
    @Test public void testOptional02() 
    { test_roundTripQuery("SELECT * WHERE { ?s ?p ?o OPTIONAL { { ?s ?q ?z FILTER (?foo) } } }") ; }
    
    @Test public void testOptional03() 
    // Don't currently unnest the LHS of the second optional.  See testOptional03a
    { test_roundTripQuery("SELECT * WHERE { ?s ?p ?o OPTIONAL { ?s ?p1 ?o1 } OPTIONAL { ?s ?p2 ?o2 } } ") ; }

    @Test public void testOptional04() 
    { test_roundTripQuery("SELECT * WHERE { ?s ?p ?o OPTIONAL { ?s ?p1 ?o1 } OPTIONAL { ?s ?p2 ?o2 } OPTIONAL { ?s ?p3 ?o3 }} ") ; }

    @Test
    public void testCountStar() {
        test_roundTripQuery("select (count(*) as ?cs) { ?s ?p ?o }");
    }
    
    @Test
    public void testCountGroup() {
        test_roundTripQuery("select (count(?p) as ?cp) { ?s ?p ?o } group by ?s");
    }
    
    @Test
    public void testCountGroupAs() {
        test_roundTripQuery("select (count(?p) as ?cp) { ?s ?p ?o }");
    }
    
    @Test
    public void testDoubleCount() {
        Query[] result = test_roundTripQuery("select (count(?s) as ?sc) (count(?p) as ?pc) { ?s ?p ?o }") ;
        assertEquals(2, result[1].getResultVars().size());
        assertTrue(result[1].getResultVars().contains("sc"));
        assertTrue(result[1].getResultVars().contains("pc"));
    }
    
    /* JENA-166 */
    @Test
    public void testGroupWithExpression() {
        test_roundTripQuery("SELECT (sample(?a) + 1 AS ?c) {} GROUP BY ?x");
    }
    
    /* Coverage developed for JENA-963 : GROUP BY*/
    @Test public void testGroupBy_01()
    { test_roundTripQuery("SELECT ?s { ?s ?p ?o } GROUP BY ?s"); }
    
    @Test public void testGroupBy_02()
    { test_roundTripQuery("SELECT (count(?p) as ?cp) { ?s ?p ?o } GROUP BY ?s"); }
    
    @Test public void testGroupBy_03()
    { test_roundTripQuery("SELECT ?s { ?s ?p ?o } GROUP BY ?s HAVING (count(*) > 1 )"); }
    
    @Test public void testGroupBy_04()
    { test_roundTripQuery("SELECT ?s { ?s ?p ?o } GROUP BY ?s HAVING (?s > 1 )"); }
    
    @Test public void testGroupBy_05()
    { test_roundTripQuery("SELECT (count(?p) as ?cp) { ?s ?p ?o } GROUP BY ?s HAVING (?cp > 1 )"); }
    
    @Test public void testGroupBy_06()
    { test_roundTripQuery("SELECT (count(?p) as ?cp) { ?s ?p ?o } GROUP BY (abs(?o)) HAVING (?cp > 1 )"); }
    
    @Test public void testGroupBy_07()
    { test_roundTripQuery("SELECT (?X+2 AS ?Y) (count(?p) as ?cp) ?Z (1/?X AS ?X1) { ?s ?p ?o } GROUP BY ?Z (abs(?o) AS ?X) HAVING (?cp > 1 )"); }
    
    @Test public void testGroupBy_08()
    { test_roundTripQuery("SELECT (count(?p) as ?cp) { ?s ?p ?o } GROUP BY (abs(?o)) HAVING (?cp > 1 )"); }
    
    @Test public void testGroupBy_09()
    { test_roundTripQuery("SELECT (count(?p) as ?cp) { ?s ?p ?o } GROUP BY (abs(?o)) ORDER BY (COUNT(*))"); }
    
    @Test public void testGroupBy_10()
    { test_roundTripQuery("SELECT (7+count(?p) as ?cp) { ?s ?p ?o } GROUP BY (abs(?o)) HAVING (?cp > 1 && SUM(?o) > 99 ) ORDER BY (6+COUNT(*))"); }
    
    @Test public void testGroupBy_11()
    { test_roundTripQuery("SELECT ?X { ?s ?p ?o } GROUP BY (abs(?o) AS ?X) HAVING (?cp > 1 )"); }
    
    @Test public void testGroupBy_12()
    { test_roundTripQuery("SELECT * { ?s ?q ?z {SELECT DISTINCT * { ?s ?p ?o }} }"); } 
    
    // https://issues.apache.org/jira/browse/JENA-1844
    @Test public void testGroupBy_13()
    { test_roundTripQuery("SELECT * { ?s ?p ?o BIND(?o+1 AS ?a1) } ORDER BY ?s"); }
    
    @Test public void testSubQuery_01()
    { test_roundTripQuery("SELECT ?s { SELECT (count(*) as ?cp) { ?s ?p ?o } }") ; }
    
    @Test public void testSubQuery_02()
    { test_roundTripQuery("SELECT ?s { ?s ?p ?o { SELECT (count(*) as ?cp) { ?s ?p ?o } }}") ; }
    
    @Test public void testSubQuery_03()
    { test_roundTripQuery("SELECT ?s { { SELECT (count(*) as ?cp) { ?s ?p ?o } } ?s ?p ?o }") ; }
    
    @Test public void testSubQuery_04()
    { test_roundTripQuery("SELECT * WHERE { ?s ?p ?o . BIND(?o AS ?x) }") ; }
    
    @Test public void testSubQuery_05()
    { test_roundTripQuery("SELECT (?o AS ?x) WHERE { ?s ?p ?o .}") ; }
    
    @Test
    public void testProject1() {
        test_roundTripQuery("SELECT (?x + 1 AS ?c) {}");
    }
    
    @Test
    public void testProject2() {
        Query[] result = test_roundTripQuery("SELECT (?x + 1 AS ?c) ?d {}");
        assertEquals(2, result[1].getResultVars().size());
        assertTrue(result[1].getResultVars().contains("c"));
        assertTrue(result[1].getResultVars().contains("d"));
    }
    
    @Test
    public void testNestedBind() {
        test_roundTripQuery("SELECT ?c { { } UNION { BIND(?x + 1 AS ?c) } }");
    }
    
    @Test
    public void testNestedProject() {
        test_roundTripQuery("SELECT (?x + 1 AS ?c) { { } UNION { } }");
    }
    
    @Test
    public void testGroupExpression() {
        test_roundTripQuery("SELECT ?z { } GROUP BY (?x + ?y AS ?z)");
    }
    
    @Test
    public void testNestedProjectWithGroup() {
        test_roundTripQuery("SELECT (SAMPLE(?c) as ?s) { {} UNION {BIND(?x + 1 AS ?c)} } GROUP BY ?x");
    }
    
    @Test
    public void testQuadPatternInDefaultGraph() {
        test_roundTripQueryQuads("SELECT * WHERE { ?s a ?type }");
    }
    
    @Test
    public void testGraphClauseUri() {
        test_roundTripQuery("SELECT * WHERE { GRAPH <http://example> { ?s a ?type } }");
    }
    
    @Test
    public void testGraphClauseComplex() {
        test_roundTripQuery("SELECT * WHERE { GRAPH <http://example> { ?s a ?type . OPTIONAL { ?s <http://label> ?label } } }");
    }
    
    @Test
    public void testQuadPatternInGraph() {
        test_roundTripQueryQuads("SELECT * WHERE { GRAPH <http://example> { ?s a ?type } }");
    }
    
    @Test
    public void testQuadPatternInGraphComplex01() {
        //This fails because OpQuadPattern's are converted back to individual GRAPH clauses
        Object[] result = roundTripQueryQuad("SELECT * WHERE { GRAPH <http://example> { ?s a ?type . OPTIONAL { ?s <http://label> ?label } } }");
        assertFalse(result[0].equals(result[1]));
    }
    
    @Test
    public void testQuadPatternInGraphComplex02() {
        //This succeeds since each OpQuadPattern is from a single simple GRAPH clause
        test_roundTripQueryQuads("SELECT * WHERE { GRAPH <http://example> { ?s a ?type } OPTIONAL { GRAPH <http://example> { ?s <http://label> ?label } } }");
    }
    
    @Test
    public void testExtend1() {
        // Top Level BIND should now be round trippable
        test_roundTripQuery("SELECT * WHERE { ?s ?p ?o . BIND(?o AS ?x) }");
    }
    
    @Test
    public void testExtend2() {
        // Nested BIND should always have been round trippable
        test_roundTripQuery("SELECT * WHERE { GRAPH ?g { ?s ?p ?o . BIND(?o AS ?x) } }");
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
        test_roundTripQuery(query);
    }
    
    @Test
    public void testExtend4() {
        //Simplified repo of JENA-429
        test_roundTripQuery("SELECT ?key (COUNT(?member) AS ?total) WHERE { ?s ?p ?o . BIND(LCASE(?o) AS ?key) } GROUP BY ?key");
    }
    
    @Test
    public void testExtendInService() {
        //Original test case from JENA-422
        Query[] result = test_roundTripQuery("SELECT * WHERE { SERVICE <http://example/endpoint> { ?s ?p ?o . BIND(?o AS ?x) } }");
        assertTrue(result[1].toString().contains("BIND"));
    }
    
    @Test
    public void testSubQuery1() {
        test_roundTripQuery("SELECT ?s WHERE { SELECT ?s ?p WHERE { ?s ?p ?o } }");
    }
    
    @Test
    public void testSubQuery2() {
        String query = "SELECT ?s ?x WHERE { { SELECT ?s ?p WHERE { ?s ?p ?o } } { SELECT ?x WHERE { ?x ?p ?o } } }";
        // The second inner sub-query is specially fixed up  in OpJoin processing.
        // Not all cases of sub-query have unnecessary {} removed.
        test_roundTripQuery(query) ;
    }
    
    @Test
    public void testSubQuery3() {
        String query = "SELECT * WHERE { { SELECT ?s ?p WHERE { ?s ?p ?o } } { SELECT ?x WHERE { ?x ?p ?o } } }";
        test_roundTripQuery(query) ;
    }
    
    @Test
    public void testAggregatesInSubQuery1() {
        //Simplified form of a test case provided via the mailing list (JENA-445)
        String query = "SELECT ?key ?agg WHERE { SELECT ?key (COUNT(*) AS ?agg) { ?key ?p ?o } GROUP BY ?key }";
        test_roundTripQuery(query);
    }
    
    @Test
    public void testAggregatesInSubQuery2() {
        //Simplified form of a test case provided via the mailing list (JENA-445)
        test_roundTripAlegbra("SELECT * WHERE { { SELECT ?key (COUNT(*) AS ?agg) { ?key ?p ?o } GROUP BY ?key } }");
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
        test_roundTripQuery(queryString);
    }
    
    @Test
    public void testModifiersOnSubQuery1() {
        // From JENA-954
        String query = StrUtils.strjoinNL("SELECT (COUNT(*) as ?count) {",
                                          "  SELECT DISTINCT ?uri ?graph WHERE {",
                                          "    GRAPH ?graph {",
                                          "      ?uri ?p ?o .",
                                          "      }",
                                          "    } LIMIT 1",
                                          "}");
        
        test_roundTripQuery(query) ;
    }
    
    @Test
    public void testModifiersOnSubQuery2() {
        // From JENA-954
        String query = StrUtils.strjoinNL("SELECT (COUNT(*) as ?count) {",
                                          "  SELECT REDUCED ?uri ?graph WHERE {",
                                          "    GRAPH ?graph {",
                                          "      ?uri ?p ?o .",
                                          "      }",
                                          "    } LIMIT 1",
                                          "}");
        
        test_roundTripQuery(query);
    }
    
    @Test
    public void testModifiersOnSubQuery3() {
        // From JENA-954
        String query = StrUtils.strjoinNL("SELECT (COUNT(*) as ?count) {",
                                          "  SELECT ?uri ?graph WHERE {",
                                          "    GRAPH ?graph {",
                                          "      ?uri ?p ?o .",
                                          "      }",
                                          "    } LIMIT 1",
                                          "}");
        
        test_roundTripQuery(query);
    }
    
    @Test
    public void testModifiersOnSubQuery4() {
        // From JENA-954
        String query = StrUtils.strjoinNL("SELECT (COUNT(*) as ?count) {",
                                          "  SELECT ?uri ?graph WHERE {",
                                          "    GRAPH ?graph {",
                                          "      ?uri ?p ?o .",
                                          "      }",
                                          "    } OFFSET 1",
                                          "}");
        
        test_roundTripQuery(query);
    }
    
    @Test
    public void testPathExpressions1() {
        String query = "PREFIX : <http://example/> SELECT * { ?s :p* ?o . ?x :r 123 . }" ;
        test_roundTripQuery(query);
    }
        
    @Test
    public void testPathExpressions2() {
        String query = "PREFIX : <http://example/> SELECT * { ?s :p*/:q ?o . ?x :r 123 . }" ;
        test_roundTripQuery(query);
    }

    @Test
    public void testMinus1() {
        test_roundTripQuery("PREFIX : <http://example/> SELECT * { ?s :p ?o MINUS { ?s :q ?v .FILTER(?v<5) } }") ; 
    }
    
    @Test
    public void testMinus2() {
        // query gains a level of {} but the meaning is the same.
        String query = "PREFIX : <http://example/> SELECT * { ?s :p ?o OPTIONAL { ?s :x ?2 } MINUS { ?s :q ?v .FILTER(?v<5) } }" ;
        test_roundTripAlegbra(query) ;
    }
    
    @Test
    public void testTable1() {
        String query = "SELECT * WHERE { ?x ?p ?z . VALUES ?y { } }" ;
        roundTripQuery(query);
    }
    
    @Test
    public void testTable2() {
        // JENA-1468 : op to string and back.
        String qs = "SELECT * WHERE { ?x ?p ?z . VALUES ?y { } }" ;
        Query query = QueryFactory.create(qs); 
        Op op = Algebra.compile(query);
        String x = op.toString();
        Op op1 = SSE.parseOp(x);
        Query query2 = OpAsQuery.asQuery(op1);
        assertEquals(query, query2);
    }
    

    @Test
    public void testValues1() {
        String query = "SELECT  * { VALUES ?x {1 2} ?s ?p ?x }" ;
        test_roundTripQuery(query) ;
    }
    
    @Test
    public void testValues2() {
        String query = "SELECT  * { ?s ?p ?x  VALUES ?x {1 2} }" ;
        test_roundTripQuery(query) ;
    }

    // Algebra to query : optimization cases OpAsQuery can handle.
    
    @Test
    public void testAlgebra01() {
        String opStr = "(sequence (bgp (?s1 ?p1 ?o1)) (bgp (?s2 ?p2 ?o2)) )" ;
        String query = "SELECT * { ?s1 ?p1 ?o1. ?s2 ?p2 ?o2}" ;
        test_AlgebraToQuery(opStr, query);
    }
    
    @Test
    public void testAlgebra02() {
        String opStr = "(sequence (bgp (?s1 ?p1 ?o1)) (path ?x (path* :p) ?z) )" ;
        String query = "PREFIX : <http://example/> SELECT * { ?s1 ?p1 ?o1. ?x :p* ?z}" ;
        test_AlgebraToQuery(opStr, query);
    }

    @Test
    public void testAlgebra03() {
        String opStr = "(sequence  (path ?x (path* :p) ?z) (bgp (?s1 ?p1 ?o1)) )" ;
        String query = "PREFIX : <http://example/> SELECT * { ?x :p* ?z . ?s1 ?p1 ?o1. }" ;
        test_AlgebraToQuery(opStr, query);
    }
    
    // There 3 classes of transformations: there are 3 main test operations.
    //   test_roundTripQuery: The same query is recovered from OpAsQuery
    //   test_roundTripAlegbra: Different queries with the same alegra forms
    //   test_equivalentQuery: Different equivalent queries - same answers, different algebra.
    //   test_algebraToQuery: algebra to query (e.g. optimization shapes)
    // 
    // test_roundTripQuery is test_equivalentQuery with same input and expected.
    // + quad variants.
    
    public static void test_equivalentQuery(String input, String expected) {
        Query orig = QueryFactory.create(input, Syntax.syntaxSPARQL_11);
        Op toReconstruct = Algebra.compile(orig);
        Query got = OpAsQuery.asQuery(toReconstruct);
        Query result = QueryFactory.create(expected, Syntax.syntaxSPARQL_11);
        assertEquals(result, got); 
    }
    
    // Test for queries that do query->algebra->OpAsQuery->query
    // to produce an output that is .equals the input.
    /** query->algebra->OpAsQuery->query */
    public static Query[] test_roundTripQuery(String query) {
        // [original, got]
        Query[] r = roundTripQuery(query) ;
        stripNamespacesAndBase(r[0]) ; 
        stripNamespacesAndBase(r[1]) ;
        assertEquals(r[0], r[1]) ;
        return r ;
    }
    
    public static void test_roundTripQuery(String query, String outcome) {
        Query[] r = roundTripQuery(query) ;
        Query orig = r[0];
        Query output = r[1];
        Query q2 = QueryFactory.create(outcome);
        stripNamespacesAndBase(orig) ; 
        stripNamespacesAndBase(output) ;
        stripNamespacesAndBase(q2) ;
        assertEquals(q2, output) ;
    }
    
    // Test via quads  
    public static Query[] test_roundTripQueryQuads(String query) {
        Query[] r = roundTripQueryQuad(query) ;
        assertEquals(r[0], r[1]) ;
        return r ;
    }

    // Compare A1 and A2 where 
    //  query[Q1]->algebra[A1]->OpAsQuery->query[Q2]->algebra[A2]
    // Sometimes Q1 and Q2 are equivalent but not .equals.  
    public void test_roundTripAlegbra(String query) {
        Query[] r = roundTripQuery(query);
        // Even if the strings come out as non-equal because of the translation from algebra to query
        // the algebras should be equal
        // i.e. the queries should remain semantically equivalent
        Op a1 = Algebra.compile(r[0]);
        Op a2 = Algebra.compile(r[1]);
        Assert.assertEquals(a1, a2);
    }
    
    /** algebra->OpAsQuery->query */
    public static void test_AlgebraToQuery(String input, String expected) {
        Op op = SSE.parseOp(input) ;
        Query orig = QueryFactory.create(expected, Syntax.syntaxSPARQL_11);
        stripNamespacesAndBase(orig) ;
        Query got = OpAsQuery.asQuery(op);
        Assert.assertEquals(orig, got) ;
    }

    /** query->algebra->OpAsQuery->query **/
    private static Query[] roundTripQuery(String query) {
        Query orig = QueryFactory.create(query, Syntax.syntaxSPARQL_11);
        Op toReconstruct = Algebra.compile(orig);
        Query got = OpAsQuery.asQuery(toReconstruct);
        Query[] r = { orig, got };
        return r;
    }
    
    /** query->algebra/quads->OpAsQuery->query */
    private static Query[] roundTripQueryQuad(String query) {
        Query orig = QueryFactory.create(query, Syntax.syntaxSPARQL_11);
        Op toReconstruct = Algebra.compile(orig);
        toReconstruct = Algebra.toQuadForm(toReconstruct);
        Query got = OpAsQuery.asQuery(toReconstruct);
        Query[] r = { orig, got };
        return r;
    }
    
    protected static void stripNamespacesAndBase(Query q) {
        Map<String, String> prefixes = q.getPrefixMapping().getNsPrefixMap();
        for (String prefix : prefixes.keySet()) {
            q.getPrefixMapping().removeNsPrefix(prefix);
        }
        q.setBaseURI((String)null);
    }
}
