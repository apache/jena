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

package org.apache.jena.sparql.core;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.sse.SSE ;
import static org.junit.Assert.*  ;

import java.util.List;

import org.junit.Test ;

public class TestDatasetGraphLink extends AbstractDatasetGraphTests 
{
    @Override
    protected DatasetGraph emptyDataset() { return DatasetGraphFactory.createGeneral() ; }

    // Change the graph after adding should affect the linked graph.
    // c.f. TestDatasetGraphCopyAdd.copyAdd_01
    @Test public void linkAdd_01() {
        Graph graph = SSE.parseGraph("(graph (:s :p :o))") ;
        Node g = SSE.parseNode(":g") ;
        DatasetGraph dsg = emptyDataset() ;
        dsg.addGraph(g, graph);
        graph.clear(); 
        assertTrue(graph.isEmpty()) ;
        assertTrue(dsg.getGraph(g).isEmpty()) ;
    }
    
    // Empty graphs are visiable.
    @Override
    @Test public void emptyGraph_1() { }
    @Test public void emptyGraph_1_link() { 
        DatasetGraph dsg = emptyDataset() ;
        Node gn = NodeFactory.createURI("http://example/g") ;
        Quad q = SSE.parseQuad("(:g :s :p :o)");
        dsg.add(q);
        List<Node> nodes1 = Iter.toList(dsg.listGraphNodes());
        assertEquals(1, nodes1.size());

        // Variation on emptyGraph_1
        dsg.delete(q);
        List<Node> nodes2 = Iter.toList(dsg.listGraphNodes());
        assertEquals(1, nodes2.size());
    }

    @Override
    @Test public void emptyGraph_2() { }
    @Test public void emptyGraph_2_link() {
        DatasetGraph dsg = emptyDataset() ;
        Node gn = NodeFactory.createURI("http://example/g") ;
        Quad q = SSE.parseQuad("(:g :s :p :o)");
        
        dsg.add(q);
        assertTrue(dsg.containsGraph(gn));
        
        // Variation on emptyGraph_2
        dsg.delete(q);
        assertTrue(dsg.containsGraph(gn));
    }

}
