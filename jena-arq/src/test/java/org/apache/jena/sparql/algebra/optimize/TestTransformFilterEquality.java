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

package org.apache.jena.sparql.algebra.optimize;

import static org.apache.jena.sparql.algebra.optimize.TransformTests.check;
import static org.apache.jena.sparql.algebra.optimize.TransformTests.testOp;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.Transform ;
import org.apache.jena.sparql.algebra.TransformCopy ;
import org.apache.jena.sparql.algebra.Transformer ;
import org.apache.jena.sparql.sse.SSE ;
import org.junit.Test ;

/** Tests of transforms related to filters */
public class TestTransformFilterEquality
{
    private Transform t_equality = new TransformFilterEquality() ;

    @Test public void equality01() {
        testOp("(filter (= ?x <x>) (bgp ( ?s ?p ?x)) )",
               t_equality,
              "(assign ((?x <x>)) (bgp ( ?s ?p <x>)) )") ;
    }

    @Test public void equality02() {
        // Safe on strings (RDF 1.1)
        testOp("(filter (= ?x 'x') (bgp ( ?s ?p ?x)) )",
               t_equality,
               "(assign ((?x 'x')) (bgp ( ?s ?p 'x')) )") ;
    }

    @Test public void equality02a() {
        // Safe on strings (RDF 1.1)
        testOp("(filter (= ?x 'x'^^xsd:string) (bgp ( ?s ?p ?x)) )",
               t_equality,
               "(assign ((?x 'x')) (bgp ( ?s ?p 'x')) )") ;
    }

    @Test public void equality03() {
        // Not safe on numbers
        testOp("(filter (= ?x 123) (bgp ( ?s ?p ?x)) )",
               t_equality,
               (String[])null) ;
    }

//    // JENA-1184 workaround - this optimization is current not active.
//    @Test public void equality04() {
//        // Eliminate unused
//        testOp("(filter (= ?UNUSED <x>) (bgp ( ?s ?p ?x)) )",
//               t_equality,
//               "(table empty)") ;
//    }

    @Test public void equality05() {
        // Can't optimize if filter does not cover vars in LHS
        testOp("(filter (= ?x2 <x>) (conditional (bgp ( ?s1 ?p1 ?x1))  (bgp ( ?s2 ?p2 ?x2))))",
               t_equality,
               "(filter (= ?x2 <x>) (conditional (bgp ( ?s1 ?p1 ?x1))  (bgp ( ?s2 ?p2 ?x2))))") ;
    }


    @Test public void equality06() {
        testOp("(filter (= ?x <x>) (conditional (bgp ( ?s ?p ?x))  (bgp ( ?s ?p ?x))))",
               t_equality,
               "(assign((?x <x>)) (conditional (bgp ( ?s ?p <x>))  (bgp ( ?s ?p <x>))))") ;
    }

    @Test public void equality07() {
        testOp("(filter (= ?x <x>) (conditional (bgp ( ?s ?p ?x))  (bgp ( ?s ?p ?x1))))",
               t_equality,
               "(assign((?x <x>)) (conditional (bgp ( ?s ?p <x>))  (bgp ( ?s ?p ?x1))))") ;
    }

    @Test public void equality08() {
        testOp("(filter (= ?x1 <x>) (conditional (bgp ( ?s ?p ?x))  (bgp ( ?s ?p ?x1))))",
               t_equality,
               "(filter (= ?x1 <x>) (conditional (bgp ( ?s ?p ?x))  (bgp ( ?s ?p ?x1))))") ;
    }

    @Test public void equality09() {
        // Can't optimize if filter does not cover vars in LHS
        testOp("(filter (= ?x2 <x>) (leftjoin (bgp ( ?s1 ?p1 ?x1))  (bgp ( ?s2 ?p2 ?x2))))",
               t_equality,
               "(filter (= ?x2 <x>) (leftjoin (bgp ( ?s1 ?p1 ?x1))  (bgp ( ?s2 ?p2 ?x2))))") ;
    }

    @Test public void equality10() {
        testOp("(filter (= ?x <x>) (leftjoin (bgp ( ?s ?p ?x))  (bgp ( ?s ?p ?x))))",
               t_equality,
               "(assign((?x <x>)) (leftjoin (bgp ( ?s ?p <x>))  (bgp ( ?s ?p <x>))))") ;
    }

    @Test public void equality11() {
        testOp("(filter (= ?x <x>) (leftjoin (bgp ( ?s ?p ?x))  (bgp ( ?s ?p ?x1))))",
               t_equality,
               "(assign((?x <x>)) (leftjoin (bgp ( ?s ?p <x>))  (bgp ( ?s ?p ?x1))))") ;
    }

    @Test public void equality12() {
        testOp("(filter (= ?x1 <x>) (leftjoin (bgp ( ?s ?p ?x))  (bgp ( ?s ?p ?x1))))",
               t_equality,
               "(filter (= ?x1 <x>) (leftjoin (bgp ( ?s ?p ?x))  (bgp ( ?s ?p ?x1))))") ;
    }

    @Test public void equality13() {
        testOp("(filter (= ?x1 <x>) (join (bgp ( ?s ?p ?x1))  (bgp ( ?s ?p ?x1))))",
               t_equality,
               "(assign((?x1 <x>))  (join (bgp ( ?s ?p <x>))  (bgp ( ?s ?p <x>))))") ;
    }

    @Test public void equality14() {
        testOp("(filter (= ?x1 <x>) (union (bgp ( ?s ?p ?x1))  (bgp ( ?s ?p ?x1))))",
               t_equality,
               "(assign((?x1 <x>))  (union (bgp ( ?s ?p <x>))  (bgp ( ?s ?p <x>))))") ;
    }

    @Test public void equality15() {
        // assign-push-in optimization.
        testOp("(filter (= ?x1 <x>) (leftjoin (leftjoin (table unit) (bgp ( ?s ?p ?x1)) ) (bgp ( ?s ?p ?x1)) ))",
               t_equality,
               "(filter (= ?x1 <x>)",
               "   (leftjoin",
               "     (leftjoin",
               "       (table unit)",
               "       (assign ((?x1 <x>)) (bgp (triple ?s ?p <x>)))",
               "     )",
               "     (assign ((?x1 <x>)) (bgp (triple ?s ?p <x>)))",
            "   ))" ) ;
    }

    // JENA-432 (simplified)
    @Test public void equality16() {
        /*
        SELECT *
        WHERE {
          ?test ?p1 ?o1.
          FILTER ( ?test = <http://localhost/t2> )
          OPTIONAL {
            SELECT ?s1
            { ?s1 ?p2 ?o2 }
          }
        } */
        String qs = StrUtils.strjoinNL
            ( "(filter (= ?test <http://localhost/t2>)"
              , "  (leftjoin"
              , "    (bgp (triple ?test ?p1 ?o1))"
              , "      (project (?s1)"
              , "       (bgp (triple ?s1 ?p2 ?o2)))))"
                ) ;
        testOp(qs,
               t_equality,
               "(assign ((?test <http://localhost/t2>))" ,
               "  (leftjoin" ,
               "    (bgp (triple <http://localhost/t2> ?p1 ?o1))" ,
               "    (project (?s1)" ,
               "      (bgp (triple ?s1 ?p2 ?o2)))))"
            ) ;
    }

    @Test public void equality17() {
        // Conflicting constraints should result in no optimization
        testOp("(filter ((= ?x <http://constant1>) (= ?x <http://constant2>)) (join (bgp (?x <http://p1> ?o1)) (bgp (?x <http://p2> ?o2))))",
               t_equality,
               (String[])null);
    }

    // JENA -1202
    @Test public void equality_expression_1() {

        // Need to fold to a string or URI to trigger equality.
        Op op = SSE.parseOp("(filter (= ?o (+ 'a' 'b')) (bgp (?x <http://p2> ?o)))") ;
        // Fold constants.
        Op op1 = Transformer.transform(new TransformCopy(), new ExprTransformConstantFold(), op);
        // Then apply filter-equality.
        check(op1, t_equality, "(assign ((?o 'ab')) (bgp (?x <http://p2> 'ab')) )") ;
    }


    @Test public void equality_path_1() {
        testOp("(filter (= ?x <http://constant1/>) (path ?x (path+ :p) ?y))",
               t_equality,
               "(assign ((?x <http://constant1/>)) (path <http://constant1/> (path+ :p) ?y) )") ;
    }

    @Test public void equality_path_2() {
        testOp("(filter (= ?y <http://constant1/>) (path ?x (path+ :p) ?y))",
               t_equality,
               "(assign ((?y <http://constant1/>)) (path ?x (path+ :p) <http://constant1/>) )") ;
    }

    @Test public void equality_path_3() {
        testOp("(filter ((= ?x <http://constant1/>) (= ?y <http://constant2/>)) (path ?x (path+ :p) ?y))",
               t_equality,
               "(assign ((?x <http://constant1/>) (?y <http://constant2/>)) (path <http://constant1/> (path+ :p) <http://constant2/>) )") ;
    }

    @Test public void equality_path_4() {
        testOp("(filter (= ?x <http://constant1/>) (join (bgp (?x :q ?z)) (path ?x (path+ :p) ?y) ))",
               t_equality,
               "(assign ((?x <http://constant1/>)) (join (bgp (<http://constant1/> :q ?z)) (path <http://constant1/> (path+ :p) ?y) ))") ;
    }
}
