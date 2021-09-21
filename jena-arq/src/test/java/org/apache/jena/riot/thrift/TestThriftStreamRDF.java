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

package org.apache.jena.riot.thrift;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream ;
import java.io.ByteArrayOutputStream ;
import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.system.StreamRDFOps ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;
import org.apache.jena.riot.system.StreamRDFWriter ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.graph.GraphFactory ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.sparql.util.IsoMatcher ;
import org.junit.Test ;

public class TestThriftStreamRDF {

    private static final String DIR = TS_RDFThrift.TestingDir ;
    
    static String gs = StrUtils.strjoinNL(
        "(graph",
        "  (_:a :p 123) ",
        "  (_:a :p 'foo'@en) ",
        "  (_:b :p '456') ",        // Presrved values only.
        "  (_:b :p '456.5') ",
        "  (_:b :p '456.5e6') ",
         ")") ;
        
    static Graph graph = SSE.parseGraph(gs) ;
    
    static String dgs = StrUtils.strjoinNL(
        "(dataset",
        "  (graph (:s1 :p _:a) (:s2 :p _:a))" ,
        "  (graph :g  (:s1 :p _:a))" ,
        "  (graph _:a (:s2 :p _:a))" ,
        ")" ) ;
    
    static DatasetGraph datasetGraph = SSE.parseDatasetGraph(dgs) ;

    // graph_01 and graph_02 are the same test but use different ways to read/write the graph.
    // Ditto dataset_01 and dataset_02
    
    @Test public void graph_01() {
        ByteArrayOutputStream out = new ByteArrayOutputStream() ;
        StreamRDF stream = ThriftRDF.streamToOutputStream(out, true) ; // With values.
        StreamRDFOps.graphToStream(graph, stream) ;
        
        byte[] bytes = out.toByteArray() ;
        ByteArrayInputStream in = new ByteArrayInputStream(bytes) ;
        
        Graph g2 = GraphFactory.createGraphMem() ;
        StreamRDF stream2 = StreamRDFLib.graph(g2) ;
        ThriftRDF.inputStreamToStream(in, stream2) ;
        
        //assertTrue(graph.isIsomorphicWith(g2)) ;
        boolean b = IsoMatcher.isomorphic(graph, g2) ;
        if ( !b ) {
            RDFDataMgr.write(System.out, graph, Lang.TTL);
            System.out.println("---------");
            RDFDataMgr.write(System.out, g2, Lang.TTL);
            System.out.println("=========");
        }
        
        assertTrue(b) ;
        
        // Stronger - same bNodes.
        sameTerms(graph, g2) ;
    }

    @Test public void graph_02() {
        ByteArrayOutputStream out = new ByteArrayOutputStream() ;
        StreamRDFWriter.write(out, graph, Lang.RDFTHRIFT, null) ;

        byte[] bytes = out.toByteArray() ;
        ByteArrayInputStream in = new ByteArrayInputStream(bytes) ;
        
        
        Graph g2 = GraphFactory.createGraphMem() ;
        RDFDataMgr.read(g2, in, Lang.RDFTHRIFT) ;
        boolean b = IsoMatcher.isomorphic(graph, g2) ;
        assertTrue(b) ;
        
        // Stronger - same bNodes.
        // ** Java8
        //graph.find(null, null, null).forEachRemaining(t -> assertTrue(g2.contains(t))) ;
        
        // Stronger - same bNodes.
        sameTerms(graph, g2) ;
    }
    
    @Test public void dataset_01() {
        DatasetGraph dsg1 = datasetGraph ;
        ByteArrayOutputStream out = new ByteArrayOutputStream() ;
        StreamRDF stream = ThriftRDF.streamToOutputStream(out) ;
        StreamRDFOps.datasetToStream(dsg1, stream) ;
        
        byte[] bytes = out.toByteArray() ;
        ByteArrayInputStream in = new ByteArrayInputStream(bytes) ;
        DatasetGraph dsg2 = DatasetGraphFactory.create() ;
        StreamRDF stream2 = StreamRDFLib.dataset(dsg2) ;
        ThriftRDF.inputStreamToStream(in, stream2) ;
        
        boolean b = IsoMatcher.isomorphic(dsg1, dsg2) ;
        assertTrue(b) ;
        // Stronger - same bNode and same as in original data.
        Node obj = Iter.first(dsg1.listGraphNodes(), Node::isBlank) ;
        termAsObject(dsg1, obj) ;
    }

    @Test public void dataset_02() {
        DatasetGraph dsg1 = datasetGraph ;
        ByteArrayOutputStream out = new ByteArrayOutputStream() ;
        StreamRDFWriter.write(out, dsg1, Lang.RDFTHRIFT) ;
        
        byte[] bytes = out.toByteArray() ;
        ByteArrayInputStream in = new ByteArrayInputStream(bytes) ;
        DatasetGraph dsg2 = DatasetGraphFactory.create() ;
        
        StreamRDF stream2 = StreamRDFLib.dataset(dsg2) ;
        ThriftRDF.inputStreamToStream(in, stream2) ;
        
        boolean b = IsoMatcher.isomorphic(dsg1, dsg2) ;
        assertTrue(b) ;
        // Stronger - same bNode and same as in original data.
        Node obj = Iter.first(dsg1.listGraphNodes(), Node::isBlank) ;
        termAsObject(dsg1, obj) ;
    }
    
    static void sameTerms(Graph g1, Graph g2) {
        assertEquals(g1.size() , g2.size() ) ;
        // ** Java8
        //g1.find(null, null, null).forEachRemaining(t -> assertTrue(g2.contains(t))) ;
        Iterator<Triple> iter = g1.find(null, null, null) ;
        while(iter.hasNext()) {
            Triple t = iter.next() ;
            g2.contains(t) ;
        }
    }

    static void termAsObject(DatasetGraph dsg, Node term)  {
        Iterator<Quad> iter = dsg.find() ;
        for ( ; iter.hasNext() ; ) {
            Quad quad = iter.next() ;
            if ( quad.getObject().equals(term) )
                return ;
        }
        fail("Failed to find "+term) ;
    }
    
    // ** Java8
//    public static <T> Stream<T> stream(Iterator<? extends T> iterator) {
//        int characteristics = Spliterator.ORDERED | Spliterator.IMMUTABLE;
//        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, characteristics), false);
//    }
}

