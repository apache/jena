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

package com.hp.hpl.jena.sparql.path;

import java.util.ArrayList ;
import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.rdf.model.Bag ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.Seq ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;
import com.hp.hpl.jena.sparql.path.eval.PathEval ;
import com.hp.hpl.jena.sparql.sse.SSE ;

/** Tests of property functions in paths.
 * A property function can get invoked in the regular way
 * (propfunc) by simple property path flattening in the optimizer
 * or by use in complex path expressions.
 */  
public class TestPathPF extends BaseTest
{
    static Graph graph = GraphFactory.createDefaultGraph() ;
    static Node elt1 = SSE.parseNode("'elt1'") ;
    static Node elt2 = SSE.parseNode("'elt2'") ;
    static String base = "http://example/" ;
    static Node node0 = NodeFactory.createURI(base+"node0") ;
    static Node node1 = NodeFactory.createURI(base+"node1") ;
    static Node node2 = NodeFactory.createURI(base+"node2") ;
    
    @BeforeClass public static void beforeClass() {
        Model m = ModelFactory.createModelForGraph(graph) ;
        Bag bag0 = m.createBag(base+"node0") ;
        Bag bag1 = m.createBag(base+"node1") ;
        Seq seq2 = m.createSeq(base+"node2") ;
        bag1.add("elt1") ;
        seq2.add("elt1") ;
        seq2.add("elt2") ;
    }
    
    @AfterClass public static void afterClass() {
        graph = null ;
    }
    
    @Test public void path_pf_00() {
        Path path = SSE.parsePath("(link rdfs:member)") ;
        eval(graph, node0, path) ;
    }

    @Test public void path_pf_01() {
        Path path = SSE.parsePath("(path+ rdfs:member)") ;
        eval(graph, Node.ANY, path, elt1,elt2) ;
    }

    @Test public void path_pf_02() {
        Path path = SSE.parsePath("(link rdfs:member)") ;
        evalReverse(graph, elt1, path, node1, node2) ;
    }
    
    private static void evalReverse(Graph graph, Node start, Path path, Node... expected) {
        Iterator<Node> iter = PathEval.evalReverse(graph, start, path, null) ;
        check(iter, expected) ; 
    }
    
    private static void eval(Graph graph, Node start, Path path, Node... expected) {
        Iterator<Node> iter = PathEval.eval(graph, start, path, ARQ.getContext()) ;
        check(iter, expected) ; 
    }
    
    private static void check(Iterator<Node> iter, Node... expected) {
        List<Node> x = Iter.toList(iter) ;
//        assertEquals(expected.length, x.size()) ;
        List<Node> r = Arrays.asList(expected) ;
        if ( !sameUnorder(r,x) )
            fail("Expected: "+r+" : Actual: "+x) ;
    }
    
    private static <T> boolean sameUnorder(List<T> list1, List<T> list2) {
        list2 = new ArrayList<T>(list2) ;
        if ( list1.size() != list2.size() )
            return false;
        for ( T elt : list1 )
            list2.remove(elt) ;
        return list2.size() == 0 ;
    }
}
