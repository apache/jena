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

package org.apache.jena.rdf12;

import static org.apache.jena.rdf12.LibTestSPARQL12.testSPARQLSyntax;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import org.apache.jena.query.QueryParseException;
import org.apache.jena.sys.JenaSystem;


public class TestSPARQL12Syntax {

    static { JenaSystem.init(); }

    // Basics not broken.
    @Test public void basic_01() { testSyntaxFragment("{ :s :p :o  }") ; }
    @Test public void basic_02() { testSyntaxFragment("{ :s :p :o . }") ; }
    @Test public void basic_03() { testSyntaxFragment("{ :s :p :o ; :p2 :o2 }") ; }
    @Test public void basic_04() { testSyntaxFragment("{ :s :p :o ; :p2 :o2 . }") ; }

    // Triple terms.
    @Test public void tripleTerm_01() { testSyntaxFragment("{ :s :p <<(:s :p :o )>> }"); }
    @Test public void tripleTerm_02() { testSyntaxFragment("{ :s :p <<(:s :p :o )>> .}"); }
    @Test public void tripleTerm_03() { testSyntaxFragment("{ :s :p <<( ?x ?p ?z )>> .}"); }
    // SPARQL is symmetric, as it is with literals.
    @Test public void tripleTerm_04() { testSyntaxFragment("{ <<( ?x ?p ?z )>> :p <<( ?x ?p ?z )>> .}"); }

    // Blank node annotation
    @Test public void annotation_1() { testSyntaxFragment("{ :s :p :o {| :q :r |} . }");}
    @Test public void annotation_2() { testSyntaxFragment("{ :s :p :o {| :q1 :r1 ; :q2 :r2 ; |} }");}
    @Test public void annotation_3() { testSyntaxFragment("{ :s :p :o {| :x :y |} ; :q :r . }");}
    @Test public void annotation_4() { testSyntaxFragment("{ :s :p :o {| :x1 :y1 |} {| :x2 :y2 |} }"); }

    // Now bad syntax
    @Test public void annotation_bad_empty_1() { testSyntaxFragmentBad("{ :s :p :o {|  |}  }");}
    @Test public void annotation_bad_empty_2() { testSyntaxFragmentBad("{ :s :p :o {| |} ; :q :r }");}

    // Old syntax
    @Test public void annotation_bad_1() { testSyntaxFragmentBad("{ :s :p :o ~(:e) . }"); }
    @Test public void annotation_bad_2() { testSyntaxFragmentBad("{ :s :p/:q :o ~ . }"); }
    @Test public void annotation_bad_3() { testSyntaxFragmentBad("{ :s :p/:q :o {| :x :y |} }"); }
    @Test public void annotation_bad_4() { testSyntaxFragmentBad("{ :s :p :o | :e . }"); }

    // Reifier+Annotation
    @Test public void reifierAnnotation_1() { testSyntaxFragment("{ :s :p :o ~:e . }"); }
    @Test public void reifierAnnotation_2() { testSyntaxFragment("{ :s :p :o ~ . }"); }
    @Test public void reifierAnnotation_3() { testSyntaxFragment("{ :s :p :o ~[] . }"); }
    @Test public void reifierAnnotation_4() { testSyntaxFragment("{ :s :p :o ~:e {| :q :r |} }"); }
    @Test public void reifierAnnotation_5() { testSyntaxFragment("{ :s :p :o ~ {| :x :y |} ; :q :r . }"); }

    // Multiple reifiers, multiple annotation
    @Test public void reifierAnnotation_10() { testSyntaxFragment("{ :s :p :o ~:e ~ .}");}
    @Test public void reifierAnnotation_11() { testSyntaxFragment("{ :s :p :o ~:e ~:r }");}
    @Test public void reifierAnnotation_12() { testSyntaxFragment("{ :s :p :o ~ ~ {| :y :z|}  }");}
    @Test public void reifierAnnotation_13() { testSyntaxFragment("{ :s :p :o ~:e {| :q :r |} ~:r {| :q :r |} ~:r } ");}
    @Test public void reifierAnnotation_14() { testSyntaxFragment("{ :s :p :o {| :q :r |} {| :q :r |} .}"); }
    @Test public void reifierAnnotation_15() { testSyntaxFragment("{ :s :p :o ~  {| :x :y |} ; :q :r . }");}

    @Test public void reifierVarAnnotation_1() { testSyntaxFragment("{ :s :p :o ~?v .}");}
    @Test public void reifierVarAnnotation_2() { testSyntaxFragment("{ :s :p :o ~?v ; :q :r }");}
    @Test public void reifierVarAnnotation_3() { testSyntaxFragment("{ :s :p :o ~?v {| :x :y |} ; :q :r }");}

    // Reified triple
    @Test public void reifiedTriple_1() { testSyntaxFragment("{ << :s :p :o >> . }");}
    @Test public void reifiedTriple_2() { testSyntaxFragment("{ << :s :p :o >> :p :r . }");}
    @Test public void reifiedTriple_3() { testSyntaxFragment("{ :x :y << :s :p :o >> . }");}
    @Test public void reifiedTriple_4() { testSyntaxFragment("{ :x :y << << :s :p :o1 >> :p :o2 >> . }");}
    @Test public void reifiedTriple_5() { testSyntaxFragment("{ :x :y << :s1 :p << :s2 :p :o >>  >> . }");}

    @Test public void reifiedTriple_6() { testSyntaxFragment("{ << :s :p :o ~:e >> . }");}
    @Test public void reifiedTriple_7() { testSyntaxFragment("{ << :s :p :o ~ >> . }");}
    @Test public void reifiedTriple_8() { testSyntaxFragment("{ << :s :p :o ~ [] >> . }");}
    @Test public void reifiedTriple_9() { testSyntaxFragment("{ :a :b << :s :p :o ~:e >> . }");}

    @Test public void reifierTriple_bad_1() { testSyntaxFragmentBad("{ << :s :p :o ~:e1 ~:e2 >> . }"); }
    @Test public void reifierTriple_bad_2() { testSyntaxFragmentBad("{ << :s :p :o ~(:e) >> . }"); }
    @Test public void reifierTriple_bad_3() { testSyntaxFragmentBad("{ << :s :p :o ~() >> . }"); }
    @Test public void reifierTriple_bad_4() { testSyntaxFragmentBad("{ << :s :p/:q :o ~() >> . }"); }
    @Test public void reifierTriple_bad_5() { testSyntaxFragmentBad("{ << :s :p :o | :r >> . }"); }

    // Nesting
    @Test public void reifierReifiedTriple_nested_1() { testSyntaxFragment("{ << :s :p << :s :p :o >> >> :y :z }");}

    @Test public void reifierVarReifiedTriple_1() { testSyntaxFragment("{ << :s :p :o ~?v >> . }");}
    @Test public void reifierVarReifiedTriple_2() { testSyntaxFragment("{ :a :b << :s :p :o ~?v >> . }");}

    @Test public void reifierMultipleReifiedTriple_bad_1() { testSyntaxFragmentBad("{ << :s :p :o ~:e1 ~:e2 >> . }");}

    // In larger units.
    @Test public void reifierData_1() { testSyntaxFragment("{ :x :q << :s :p :o >> . }");}
    @Test public void reifierData_2() { testSyntaxFragment("{ :x :q << :s :p :o ~:e >> . }");}
    @Test public void reifierData_3() { testSyntaxFragment("{ :x :q << :s :p :o ~:e >> ; :x :y }");}
    @Test public void reifierData_4() { testSyntaxFragment("{ :s :p :o . :x :q << :s :p :o ~:e >> ; :x :y }");}
    @Test public void reifierData_5() { testSyntaxFragment("{ :s :p :o . << :s :p :o ~:e >> . :e :x :y }");}

    // Variables.
    @Test public void reifierAnnotationVars_1()     { testSyntaxFragment("{ :s :p :o {| ?x ?y |} {| :x2 :y2 |} }"); }
    @Test public void reifierAnnotationVars_2()     { testSyntaxFragment("{ :s :p :o ~?v1 ~?v2 }"); }

    // Variables.
    @Test public void reifierReifiedTriplesVars_1()  { testSyntaxFragment("{  << ?s ?p ?o ~?e >> }"); }
    @Test public void reifierReifiedTriplesVars_2()  { testSyntaxFragment("{  << ?s ?p ?o ~?e >> ?p ?q }"); }

    // Expressions.
    @Test public void exprTripleTerm_1() { testSyntaxFragment("{ FILTER(?x = <<( :s :p :o )>>) }"); }
    @Test public void exprTripleterm_2() { testSyntaxFragment("{ FILTER(sameTerm(?x, <<( :s :p :o )>>)) }"); }
    // Expressions.
    @Test public void exprTripleTerm_bad_1() { testSyntaxFragmentBad("{ FILTER(?x = << :s :p :o >>) }"); }

    // VALUES
    @Test public void valuesTripleTerm_1() { testSyntaxFragment("{ VALUES ?x { :x <<( :s :p :o )>> } }"); }
    @Test public void valuesTripleTerm_2() { testSyntaxFragment("{ VALUES (?x ?y) { (:x <<( :s :p :o )>>) } }"); }

    @Test public void valuesTripleTerm_bad_1() { testSyntaxFragmentBad("{ VALUES ?x { :x << :s :p :o >> } }"); }
    @Test public void valuesTripleTerm_bad_2() { testSyntaxFragmentBad("{ VALUES ?x { :x <<( ?s ?p ?o )>> } }"); }

    // Old syntax
    @Test public void oldSyntax_1() { testSyntaxFragmentBad("{ << :r | :s :p :o >> :x : y  }"); }
    @Test public void oldSyntax_2() { testSyntaxFragmentBad("{ :s :p :o {| :r | : q :x |} }"); }

    @Test public void reifierData_10() { testSyntaxFragment("""
                    {
                      :s :p1 1 ;
                         :p2 22 {| :a 99 |} {| :a 98 |} ~:e ;
                         :p3 2 ,
                             3 {| :a 99 |} {| :a 98 |} ~?v ~:e1 ~:e2 ;
                         :x :y
                    }
                    """
            );}

    // Old alternative.
    @Test public void annotationReifierList_1() { testSyntaxFragmentBad("{ :s :p :o ~(:e1 :e2) }");}
    @Test public void annotationReifierList_2() { testSyntaxFragmentBad("{ :s :p :o ~() }");}

    // CONSTRUCT patterns.
    @Test public void construct_1() { testSyntaxQuery("CONSTRUCT { :s :p :o ~:e } WHERE {}"); }
    @Test public void construct_2() { testSyntaxQuery("CONSTRUCT { :s :p :o {| :q 'ABC' |} } WHERE {}"); }
    @Test public void construct_3() { testSyntaxQuery("CONSTRUCT { :s :p <<( :a :b :c )>> } WHERE {}"); }
    @Test public void construct_4() { testSyntaxQuery("CONSTRUCT { :s :p <<( :a :b  <<( :d :e :f )>> )>> } WHERE {}"); }

    @Test public void construct_10() { testSyntaxQuery("CONSTRUCT { :s :p << :x :y :z >> } WHERE {}"); }
    @Test public void construct_11() { testSyntaxQuery("CONSTRUCT { << :x :y :z >> :q :z } WHERE {}"); }

    // "a"

    static String PREFIXES = "PREFIX : <http://example/>\n";

    /**
     * Test a query fragment, expecting a syntax error.
     * The fragment is wrapped in prefixes and {@code "SELECT * "}.
     */
    private void testSyntaxFragmentBad(String queryFragment) {
        assertThrows(QueryParseException.class, ()->execFragment(Outcome.BAD, null, queryFragment, false));
    }

    /** Test a query fragment. The fragment is wrapped in prefixes and {@code "SELECT * "}. */
    private void testSyntaxFragment(String queryFragment) {
        execFragment(Outcome.GOOD, null, queryFragment, false);
    }

    /** Test a complete query string. Prefixes are prepended.*/
    private void testSyntaxQuery(String queryString) {
        String qs = PREFIXES+"\n"+queryString;
        testSPARQLSyntax(Outcome.GOOD, null, qs, false);
    }

    private void execFragment(Outcome testType, String label, String queryFragment, boolean verbose) {
        String qs = PREFIXES+"\nSELECT * "+queryFragment+"\n";
        testSPARQLSyntax(testType, label, qs, verbose);
    }

    // The method names are arranged so that the test* (looking up the stack) is immediately before the test itself.
    private static int unknownCount;

    private static String getTestMethodName() {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        int N = stacktrace.length;
        boolean seenTestTypeName = false;
        boolean isBad = false;
        for ( int i = 0 ; i < N ; i++ ) {
            StackTraceElement e = stacktrace[i];
            String methodName = e.getMethodName();
            if ( seenTestTypeName ) {
                if ( ! methodName.startsWith("test") ) {
                    // First name after test*
                    //return !isBad ? methodName : methodName+"[bad]";
                    return methodName;
                }
                if ( methodName.startsWith("lambda") )
                    seenTestTypeName = false;
            } else {
                if ( methodName.startsWith("test") )
                    seenTestTypeName = true;
                isBad = methodName.endsWith("Bad");
            }
        }
        return "Unknown_"+(++unknownCount);
    }
}
