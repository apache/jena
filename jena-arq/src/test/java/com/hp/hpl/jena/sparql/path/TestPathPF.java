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

import java.io.StringReader ;
import java.util.Arrays ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Factory ;
import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.rdf.model.Bag ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.Seq ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;
import com.hp.hpl.jena.sparql.graph.NodeConst ;
import com.hp.hpl.jena.sparql.path.eval.PathEval ;
import com.hp.hpl.jena.sparql.sse.SSE ;

/** Tests of property functions in paths.
 * A property function can get invoked in the regular way
 * (propfunc) by simple property path flattening in the optimizer
 * or by use in complex path expressions.
 */  
public class TestPathPF extends BaseTest
{
    static Graph graph1 = GraphFactory.createDefaultGraph() ;
    static Node elt1 = SSE.parseNode("'elt1'") ;
    static Node elt2 = SSE.parseNode("'elt2'") ;
    static String base = "http://example/" ;
    static Node node0 = NodeFactory.createURI(base+"node0") ;
    static Node node1 = NodeFactory.createURI(base+"node1") ;
    static Node node2 = NodeFactory.createURI(base+"node2") ;

    static Node s1 = NodeFactory.createURI("http://example/s1") ;
    static Node s2 = NodeFactory.createURI("http://example/s2") ;
    static Node s3 = NodeFactory.createURI("http://example/s3") ;
    
    private static String data = 
        "prefix : <http://example/>\n" +
        ":s1 :p (1 2 3) .\n"+
        ":s2 :p () .\n" + 
        ":s1 :p (4 5) .\n" +
        ":s3 :p (8 9) .\n" ;
    
    private static Graph graph2 = Factory.createDefaultGraph() ;
    static { RDFDataMgr.read(graph2, new StringReader(data), null, Lang.TTL); }
    
    @BeforeClass public static void beforeClass() {
        Model m = ModelFactory.createModelForGraph(graph1) ;
        Bag bag0 = m.createBag(base+"node0") ;
        Bag bag1 = m.createBag(base+"node1") ;
        Seq seq2 = m.createSeq(base+"node2") ;
        bag1.add("elt1") ;
        seq2.add("elt1") ;
        seq2.add("elt2") ;
    }
    
    @AfterClass public static void afterClass() {
        graph1 = null ;
    }
    
    @Test public void path_pf_00() {
        Path path = SSE.parsePath("(link rdfs:member)") ;
        eval(graph1, node0, path, new Node[] {}) ;
    }

    @Test public void path_pf_01() {
        Path path = SSE.parsePath("(path+ rdfs:member)") ;
        eval(graph1, Node.ANY, path, elt1,elt2) ;
    }

    @Test public void path_pf_02() {
        Path path = SSE.parsePath("(link rdfs:member)") ;
        evalReverse(graph1, elt1, path, node1, node2) ;
    }
    
    @Test public void path_pf_03() {
        Path path = SSE.parsePath("(link rdfs:member)") ;
        evalReverse(graph1, Node.ANY, path, node2, node2, node1) ;
    }
    

    @Test public void path_pf_10() {
        Path path = SSE.parsePath("(prefix ((list: <http://jena.hpl.hp.com/ARQ/list#>)) (seq :p (link list:member)))") ;
        eval(graph2, s1, path, "1", "2", "3", "4", "5") ;
    }
    
    @Test public void path_pf_11() {
        Path path = SSE.parsePath("(prefix ((list: <http://jena.hpl.hp.com/ARQ/list#>)) (seq :p (link list:member)))") ;
        eval(graph2, s2, path, new Node[] {}) ;
    }

    @Test public void path_pf_12() {
        Path path = SSE.parsePath("(prefix ((list: <http://jena.hpl.hp.com/ARQ/list#>)) (seq :p (link list:member)))") ;
        eval(graph2, s3, path, "8", "9") ;
    }

    @Test public void path_pf_13() {
        Path path = SSE.parsePath("(prefix ((list: <http://jena.hpl.hp.com/ARQ/list#>)) (seq :p (link list:member)))") ;
        evalReverse(graph2, NodeConst.nodeOne, path, s1) ;
    }

    @Test public void path_pf_14() {
        Path path = SSE.parsePath("(prefix ((list: <http://jena.hpl.hp.com/ARQ/list#>)) (seq :p (link list:member)))") ;
        evalReverse(graph2, NodeConst.nodeNil, path) ;
    }

    private static void eval(Graph graph, Node start, Path path, String... expected) {
        Node[] r = new Node[expected.length] ;
        for ( int i = 0 ; i < expected.length ; i++ ) {
            r[i] = SSE.parseNode(expected[i]) ;
        }
        eval(graph, start, path, r) ;
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
        check(iter, Arrays.asList(expected)) ;
    }

    private static void check(Iterator<Node> iter, List<Node> expected) {
        List<Node> x = Iter.toList(iter) ;
        assertEqualsUnordered(expected,x) ;
    }
}
