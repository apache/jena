/**
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

package com.hp.hpl.jena.sparql.algebra.optimize;

import org.junit.Test ;

import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.algebra.optimize.TransformReorder ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderFixed ;

/** Test BGP reordering using the fixed reordering algorithm */ 
public class TestReorderBGP extends AbstractTestTransform {
    private static Transform t_reorder = new TransformReorder(new ReorderFixed()) ; 
    
    public TestReorderBGP() {}

    @Test public void reorderbgp_1_1() {
        testOp("(bgp (?s :p ?o))", t_reorder, "(bgp (?s :p ?o))") ;
    }
    
    @Test public void reorderbgp_1_2() {
        testOp("(bgp (?s ?p ?o))", t_reorder, "(bgp (?s ?p ?o))") ;
    }

    @Test public void reorderbgp_2_1() {
        testOp("(bgp (?s :p ?o) (?s :p 123) )", t_reorder, "(bgp  (?s :p 123) (?s :p ?o))") ;
    }

    @Test public void reorderbgp_2_2() {
        testOp("(bgp (?s :p 123) (?s :p ?o) )", t_reorder, "(bgp  (?s :p 123) (?s :p ?o))") ;
    }
    
    @Test public void reorderbgp_2_3() {
        testOp("(bgp (?s :p 123) (?s rdf:type :T) )", t_reorder, "(bgp  (?s :p 123) (?s rdf:type :T))") ;
    }

    @Test public void reorderbgp_2_4() {
        testOp("(bgp (?s rdf:type :T) (?s :p 123) )", t_reorder, "(bgp  (?s :p 123) (?s rdf:type :T))") ;
    }

    private static String expected3 = "(bgp  (?s :p 123)  (?s rdf:type :T) (?s :p ?o) )" ;

    @Test public void reorderbgp_3_1() {
        testOp("(bgp (?s rdf:type :T) (?s :p ?o) (?s :p 123) )",
             t_reorder, expected3) ;
    }
    
    @Test public void reorderbgp_3_2() {
        testOp("(bgp (?s :p ?o) (?s :p 123) (?s rdf:type :T) )",
               t_reorder, expected3) ;
      }

    @Test public void reorderbgp_3_3() {
        testOp("(bgp (?s :p 123) (?s rdf:type :T) (?s :p ?o) )",
               t_reorder, expected3) ;
      }

    @Test public void reorderbgp_3_4() {
        testOp("(bgp (?s rdf:type :T) (?s :p 123)  (?s :p ?o) )",
               t_reorder, expected3) ;
      }
    
    @Test public void reorderbgp_3_5() {
        testOp("(bgp (?s :p 123)  (?s :p ?o) (?s rdf:type :T))",
               t_reorder, expected3) ;
      }
    @Test public void reorderbgp_3_6() {
        testOp("(bgp (?s :p ?o) (?s rdf:type :T) (?s :p 123)  )",
               t_reorder, expected3) ;
      }

}