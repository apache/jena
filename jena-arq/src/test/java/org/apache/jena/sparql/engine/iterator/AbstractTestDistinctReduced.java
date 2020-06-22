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

package org.apache.jena.sparql.engine.iterator;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays ;
import java.util.HashSet ;
import java.util.List ;
import java.util.Set ;
import java.util.stream.Collectors ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingFactory ;
import org.apache.jena.sparql.engine.binding.BindingMap ;
import org.junit.Test ;

public abstract class AbstractTestDistinctReduced {
    
    static List<String> data1 =     Arrays.asList("0","1","1","3","9","5","6","8","9","0") ;
    static List<String> results1 =  Arrays.asList("0","1",    "3","9","5","6","8"        ) ;
    static List<String> data2 =     Arrays.asList("0","0","0","0") ;
    static List<String> results2 =  Arrays.asList("0","0","0","0") ;
    
    static Var var_a = Var.alloc("a") ;
    static Var var_b = Var.alloc("b") ;
    
    private static List<Binding> build(List<String> items) {
        return items.stream().sequential().map((s)->{
            BindingMap b = BindingFactory.create() ;
            b.add(var_a, NodeFactory.createLiteral(s)) ;
            return b ;
        }).collect(Collectors.toList()) ;
    }
    
    protected abstract QueryIterator createQueryIter(List<Binding> data) ;
    
    @Test public void distinct0() {
        distinct(new ArrayList<>(), new ArrayList<>()) ;
    }

    @Test public void distinct1() {
        List<String> data =     Arrays.asList("0","1","1","3","9","5","6","8","9","0") ;
        List<String> results =  Arrays.asList("0","1",    "3","9","5","6","8"        ) ;
        distinct(data, results) ;
    }

    @Test public void distinct2() {
        List<String> data =     Arrays.asList("0","0","0","0") ;
        List<String> results =  Arrays.asList("0") ;
        distinct(data, results) ;
    }

    @Test public void distinct3() {
        List<String> data =     Arrays.asList("0","1","1","A","2","2","2","B","2","3","3","C","4","4","5") ; 
        List<String> results =  Arrays.asList("0","1","A","2","B","3","C","4","5") ;
        distinct(data, results) ;
    }

    private void distinct(List<String> data, List<String> results) {
        // Distinct Iterators are not required to preserve order.
        List<Binding> input = build(data) ;
        List<Binding> output = build(results) ;
        
        QueryIterator qIter = createQueryIter(input) ;
        
        List<Binding> iterList = Iter.toList(qIter) ;
        assertEquals(output+" :: "+iterList,
                     output.size() , iterList.size()) ;
        // Assume results has no duplicates so same size, same members => order dependent same.
        Set<Binding> testExpected = new HashSet<>(output) ;
        Set<Binding> testResult = new HashSet<>(iterList) ;
        assertEquals(testExpected , testResult) ;
        
    }

    private void testSame(QueryIterator qIter, List<Binding> data) {
        List<Binding> iterList = Iter.toList(qIter) ;
        assertEquals(data, iterList) ;
        
        
    }

    
    
}

