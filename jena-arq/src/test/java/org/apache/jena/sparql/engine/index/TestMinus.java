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

package org.apache.jena.sparql.engine.index;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.ListUtils;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphZero;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingRoot;
import org.apache.jena.sparql.engine.main.QueryEngineMain;
import org.apache.jena.sparql.engine.ref.QueryEngineRef;
import org.junit.Test;

// More tests for MINUS (JENA-1633)
public class TestMinus {
    // These tests evaluate the query twice, once with the ref engine and once with the
    // main engine. These two engines have completely different evaluation code.    
    
    // Need multi row left test case.
    @Test
    public void minus_x1() {
        // Uses HashIndexTable
        // Unbound but var is mentioned.
        String x1 = StrUtils.strjoinNL
                ("SELECT ?x ?undef {"
                ,"  VALUES ( ?x ?undef ) { ( 3 UNDEF ) ( 2 UNDEF ) }"
                ,"  MINUS {"
                ,"     BIND(2 AS ?x)"
                ,"     BIND(1 AS ?undef)"
                ,"  }"
                ,"}"
                );
        test(x1);
    }

    @Test
    public void minus_x2() {
        //SetIndexTable
        // Check "in common" includes ?undef
        String x2 = StrUtils.strjoinNL
            ("SELECT ?x ?undef {"
            ,"  VALUES ( ?x ?undef ) { ( 99 UNDEF ) }"
            ,"  MINUS {"
            ,"     BIND(1 AS ?undef)"
            ,"  }"
            ,"}"
            );
        test(x2);
    }
    
    @Test
    public void minus_x3() {
        String x3 = StrUtils.strjoinNL
            ("SELECT ?x ?undef {"
            ,"  VALUES ( ?x ) { ( 3 ) }"            
            //Not mentioned:  ?undef
            ,"  MINUS {"
            ,"     BIND(2 AS ?x)"
            ,"     BIND(1 AS ?undef)"
            ,"  }"
            ,"}"
            );
        test(x3);
     }
    
    private static DatasetGraph dsgzero = new DatasetGraphZero();
    
    private void test(String queryStr) {
        Query ast = QueryFactory.create(queryStr);
        List<Binding> x1 = exec(queryStr, QueryEngineRef.getFactory());
        List<Binding> x2 = exec(queryStr, QueryEngineMain.getFactory());
        boolean b = ListUtils.equalsUnordered(x1, x2);
//        if ( !b ) {
//            System.out.println("Ref:  "+x1);
//            System.out.println("Main: "+x2);
//        }
        assertTrue("Ref != main", b);
    }
    
    private static List<Binding> exec(String queryStr, QueryEngineFactory factory) {
        Query ast = QueryFactory.create(queryStr);
        return Iter.toList(factory.create(ast, dsgzero, BindingRoot.create(), ARQ.getContext()).iterator());
    }

}
