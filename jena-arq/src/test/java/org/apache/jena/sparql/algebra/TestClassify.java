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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.jena.query.Query ;
import org.apache.jena.query.QueryFactory ;
import org.apache.jena.query.Syntax ;
import org.apache.jena.sparql.algebra.op.OpJoin ;
import org.apache.jena.sparql.algebra.op.OpLeftJoin ;
import org.apache.jena.sparql.engine.main.JoinClassifier ;
import org.apache.jena.sparql.engine.main.LeftJoinClassifier ;
import org.junit.Test ;

public class TestClassify
{
    @Test public void testClassify_Join_01()
	{ classifyJ("{?s :p :o . { ?s :p :o FILTER(true) } }", true) ; }

    @Test public void testClassify_Join_02()
	{ classifyJ("{?s :p :o . { ?s :p :o FILTER(?s) } }", true) ; }

    @Test public void testClassify_Join_03()
	{ classifyJ("{?s :p :o . { ?s :p ?o FILTER(?o) } }", true) ; }

    // JENA-1167
    // This safe; ?o is not an input to the filter block.
    @Test public void testClassify_Join_04()
	{ classifyJ("{?s :p :o . { ?s :p :o FILTER(?o) } }", true) ; }

    @Test public void testClassify_Join_05()
	{ classifyJ("{?s :p :o . { ?x :p :o FILTER(?s) } }", false) ; }

    @Test public void testClassify_Join_06()
	{ classifyJ("{ { ?s :p :o FILTER(true) } ?s :p :o }", true) ; }

	@Test public void testClassify_Join_07()
	{ classifyJ("{ { ?s :p :o FILTER(?s) }   ?s :p :o }", true) ; }

	@Test public void testClassify_Join_08()
	{ classifyJ("{ { ?s :p ?o FILTER(?o) }   ?s :p :o }", true) ; }

	@Test public void testClassify_Join_09()
	{ classifyJ("{ { ?s :p :o FILTER(?o) }   ?s :p :o }", true) ; }

    // Actually, this is safe IF executed left, then streamed to right.
	@Test public void testClassify_Join_10()
	{ classifyJ("{ { ?x :p :o FILTER(?s) }   ?s :p :o }", true) ; }


    // OPTIONAL nested inside {} so it is a join of the LHS and the {}-RHS.

    // Not safe: ?s
    // Other parts of RHS may restrict ?s to things that can't match the LHS.
	@Test public void testClassify_Join_11()
	{ classifyJ("{?s :p :o . { OPTIONAL { ?s :p :o } } }", false) ; }

    // Not safe: ?s
	@Test public void testClassify_Join_12()
	{ classifyJ("{?s :p :o . { OPTIONAL { ?s :p :o FILTER(?s) } } }", false) ; }

	@Test public void testClassify_Join_13()
	{ classifyJ("{?s :p :o . { ?x :p :o OPTIONAL { :s :p :o FILTER(?x) } } }", true) ; }

	// See testClassify_Join_04()
	@Test public void testClassify_Join_14()
	{ classifyJ("{?s :p :o . { OPTIONAL { :s :p :o FILTER(?o) } } }", true) ; }

    @Test public void testClassify_Join_14a()
    { classifyJ("{?s :p :o . { OPTIONAL { :s :p ?o FILTER(?o) } } }", true) ; }

    @Test public void testClassify_Join_14b()
    { classifyJ("{?s :p ?o . { OPTIONAL { :s :p :o FILTER(?o) } } }", false) ; }

    @Test public void testClassify_Join_15()
	{ classifyJ("{?s :p :o . { OPTIONAL { ?x :p :o FILTER(?s) } } }", false) ; }

    @Test public void testClassify_Join_20()
    { classifyJ("{ {?s :p ?x } . { {} OPTIONAL { :s :p ?x } } }", false) ; }

    // Assuming left-right execution, this is safe.
    @Test public void testClassify_Join_21()
    { classifyJ("{ { {} OPTIONAL { :s :p ?x } } {?s :p ?x } }", true) ; }

    @Test public void testClassify_Join_31()
    { classifyJ("{ ?x ?y ?z {SELECT ?s { ?s ?p ?o} } }", true) ; }

    // JENA-1167 : Use of a filter variable not in the LHS
    @Test public void testClassify_Join_32()
    { classifyJ("{ GRAPH ?g { ?x ?y ?z } { FILTER (?a) } }", true) ; }

    // Use of a filter variable from the LHS
    @Test public void testClassify_Join_33()
    { classifyJ("{ GRAPH ?g { ?x ?y ?z } { FILTER (?z) } }", false) ; }

    // Use of a filter variable from the LHS but grounded in RHS
    @Test public void testClassify_Join_34()
    { classifyJ("{ GRAPH ?g { ?x ?y ?z } { ?a ?b ?z FILTER (?z) } }", true) ; }

    // Use of a filter variable from the LHS but optional in RHS
    @Test public void testClassify_Join_35()
    { classifyJ("{ GRAPH ?g { ?x ?y ?z } { OPTIONAL{?a ?b ?z} FILTER (?z) } }", false) ; }

    // The fix for JENA-1187 invalidates this test.
//    @Test public void testClassify_Join_40()
//    { classifyJ("{ ?x ?y ?z { ?x ?y ?z } UNION { ?x1 ?y1 ?z1 }}", true) ; }

    @Test public void testClassify_Join_41()
    { classifyJ("{ ?x ?y ?z { ?x1 ?y1 ?z1 BIND(?z+2 AS ?A) } UNION { ?x1 ?y1 ?z1 }}", false) ; }

    @Test public void testClassify_Join_42()
    { classifyJ("{ ?x ?y ?z { BIND(?z+2 AS ?A) } UNION { BIND(?z+2 AS ?B) }}", false) ; }

    @Test public void testClassify_Join_43()
    { classifyJ("{ ?x ?y ?z { LET(?A := ?z+2) } UNION { }}", false) ; }

    @Test public void testClassify_Join_44()
    { classifyJ("{ BIND(<x> AS ?typeX) { BIND(?typeX AS ?type) } }", false) ; }

    @Test public void testClassify_Join_45()
    { classifyJ("{ BIND(<x> AS ?typeX) { BIND(?typeX AS ?type) ?s ?p ?o FILTER(?o=?type) } }", false) ; }

    // Unsafe - deep MINUS
    // JENA-1021
    @Test public void testClassify_Join_50()
    { classifyJ("{ ?x ?y ?z { ?x1 ?y1 ?z1 MINUS { ?a ?b ?c } } UNION {} }", false) ; }

    // JENA-1280
    @Test public void testClassify_Join_51() {
        String x1 = "{ ?s ?p ?o   GRAPH ?g { ?s1 ?p ?o1 FILTER EXISTS {  [] ?p ?unique } }  }";
        TestClassify.classifyJ(x1, true);
    }

    // JENA-1167, JENA-1280, JENA-1534
    @Test public void testClassify_Join_52() {
        String x1 = "{ ?s  ?p  ?V0 GRAPH ?g { ?s1  ?p  ?o1 FILTER EXISTS { _:b0  ?p  ?V1 } } }";
        TestClassify.classifyJ(x1, true);
    }

    // JENA-1167, JENA-1280, JENA-1534
    @Test public void testClassify_Join_53() {
        String x1 = "{ ?s  ?p  ?V GRAPH ?g { ?s1  ?p  ?o1 FILTER EXISTS { _:b0  ?p  ?V } } }";
        TestClassify.classifyJ(x1, false);
    }

    // JENA-1813
    @Test public void testClassify_Join_54() {
         String x1 = "{ ?s  ?p  ?V GRAPH ?g { SELECT (?w AS ?V) { ?t  ?q  ?w } GROUP BY ?w } }";
         TestClassify.classifyJ(x1, false);
    }

    // JENA-2332
    @Test public void testClassify_Join_60() {
        String x1 = "{ ?s  ?p  ?V SERVICE <test:g> { SELECT (?w AS ?V) { ?t  ?q  ?w } GROUP BY ?w } }";
        TestClassify.classifyJ(x1, false);
    }

    public static void classifyJ(String pattern, boolean expected)
    {
        String qs1 = "PREFIX : <http://example/>\n" ;
        String qs = qs1+"SELECT * "+pattern;
        Query query = QueryFactory.create(qs, Syntax.syntaxARQ) ;
        Op op = Algebra.compile(query.getQueryPattern()) ;
        if ( ! ( op instanceof OpJoin ) )
            fail("Not a join: "+pattern) ;

        boolean nonLinear = JoinClassifier.isLinear((OpJoin)op) ;
        if ( nonLinear != expected ) {
            System.out.println(query);
            System.out.println(op);
        }

        assertEquals("Join: "+pattern, expected, nonLinear) ;
    }

    @Test public void testClassify_LeftJoin_01()
    { classifyLJ("{ ?s ?p ?o OPTIONAL { ?s1 ?p2 ?x} }", true)  ; }

    @Test public void testClassify_LeftJoin_02()
    { classifyLJ("{ ?s ?p ?o OPTIONAL { ?s1 ?p2 ?o3 OPTIONAL { ?s1 ?p2 ?x} } }", true)  ; }

    @Test public void testClassify_LeftJoin_03()
    { classifyLJ("{ ?s ?p ?x OPTIONAL { ?s1 ?p2 ?o3 OPTIONAL { ?s1 :p ?o3} } }", true)  ; }

    @Test public void testClassify_LeftJoin_04()
    { classifyLJ("{ ?s ?p ?x OPTIONAL { ?s1 ?p2 ?o3 OPTIONAL { ?s1 :p ?x} } }", false)  ; }

    @Test public void testClassify_LeftJoin_05()
    { classifyLJ("{ ?s ?p ?x OPTIONAL { ?s ?p ?x OPTIONAL { ?s ?p ?x } } }", true)  ; }

    @Test public void testClassify_LeftJoin_06()  // Note use of {{ }}
    { classifyLJ("{ ?s ?p ?x OPTIONAL { { ?s ?p ?o FILTER(?x) } } }", false)  ; }

    @Test public void testClassify_LeftJoin_07()
    { classifyLJ("{ ?s ?p ?x OPTIONAL { ?s ?p ?x1 OPTIONAL { ?s ?p ?x2 FILTER(?x) } } }", false)  ; }

    // Can't linearize into a projection.
    @Test public void testClassify_LeftJoin_10()
    { classifyLJ("{ ?s ?p ?x OPTIONAL { SELECT ?s { ?s ?p ?o } } }", false)  ; }

    /**
     * Can linearize with BIND present provided mentioned vars are also on RHS
     */
    @Test public void testClassify_LeftJoin_11()
    { classifyLJ("{ ?s ?p ?x OPTIONAL { ?s1 ?p2 ?x . BIND(?x AS ?test) } }", true)  ; }

    /**
     * Can't linearize with BIND present if any mentioned vars are not on RHS
     */
    @Test public void testClassify_LeftJoin_12()
    { classifyLJ("{ ?s ?p ?x OPTIONAL { ?s1 ?p2 ?x . BIND(?s AS ?test) } }", false)  ; }

    /**
     * Can't linearize with BIND present if any mentioned vars are not on RHS
     */
    @Test public void testClassify_LeftJoin_13()
    { classifyLJ("{ ?s ?p ?x OPTIONAL { ?s1 ?p2 ?x . BIND(CONCAT(?s, ?x) AS ?test) } }", false)  ; }

    /**
     * Can't linearize with BIND present if any mentioned vars are not on RHS
     */
    @Test public void testClassify_LeftJoin_14()
    { classifyLJ("{ ?s ?p ?x OPTIONAL { ?s1 ?p2 ?x . BIND(CONCAT(?s1, ?p1, ?p2, ?x) AS ?test) } }", false)  ; }

    /**
     * Can't linearize with BIND present if any mentioned vars are not fixed on RHS
     */
    @Test public void testClassify_LeftJoin_15()
    { classifyLJ("{ ?s ?p ?x OPTIONAL { BIND(?x AS ?test) OPTIONAL { ?x ?p1 ?o1 } } }", false)  ; }

    /**
     * Test left join classification
     * @param pattern WHERE clause for the query as a string
     * @param expected Whether the join should be classified as linear
     */
    private void classifyLJ(String pattern, boolean expected)
    {
        String qs1 = "PREFIX : <http://example/>\n" ;
        String qs = qs1+"SELECT * "+pattern;
        Query query = QueryFactory.create(qs, Syntax.syntaxARQ) ;
        Op op = Algebra.compile(query.getQueryPattern()) ;

        if ( ! ( op instanceof OpLeftJoin ) )
            fail("Not a leftjoin: "+pattern) ;

        boolean nonLinear = LeftJoinClassifier.isLinear((OpLeftJoin)op) ;
        assertEquals("LeftJoin: "+pattern, expected, nonLinear) ;
    }
}
