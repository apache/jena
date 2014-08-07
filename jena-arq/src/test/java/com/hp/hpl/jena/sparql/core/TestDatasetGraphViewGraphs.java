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

package com.hp.hpl.jena.sparql.core;

import org.apache.jena.atlas.iterator.Iter ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.sse.SSE ;

/** Directly call the view mechanism */
public class TestDatasetGraphViewGraphs extends AbstractTestGraphOverDataset
{
    @Override
    protected DatasetGraph createBaseDSG() { return DatasetGraphFactory.createMem() ; }
    
    @Override
    protected Graph makeDefaultGraph(DatasetGraph dsg)
    {
        return GraphView.createDefaultGraph(dsg) ;
    }

    @Override
    protected Graph makeNamedGraph(DatasetGraph dsg, Node gn)
    {
        return GraphView.createNamedGraph(dsg, gn) ;
    }
    
    @Test public void graphDSG_basic_1()
    {
        Graph g = makeDefaultGraph(baseDSG) ;
        assertTrue(g instanceof GraphView) ;
        GraphView gv = (GraphView)g ;
        assertEquals(baseDSG, gv.getDataset()) ; 
        assertEquals(null, gv.getGraphName()) ;
    }
    
    @Test public void graphDSG_basic_2()
    {
        Node gn1 = SSE.parseNode("<g1>") ;
        Graph g = makeNamedGraph(baseDSG, gn1) ;
        assertTrue(g instanceof GraphView) ;
        GraphView gv = (GraphView)g ;
        assertEquals(baseDSG, gv.getDataset()) ; 
        assertEquals(gn1, gv.getGraphName()) ;
    }
    
    @Test public void graphDSG_view_union_2()
    {
        Graph g = GraphView.createUnionGraph(baseDSG) ;
        assertTrue(g instanceof GraphView) ;
        long x = Iter.count(g.find(null,null,null)) ;
        assertEquals(2, x) ;
    }
    
    @Test public void graphDSG_view_union_3()
    {
        Graph g = GraphView.createUnionGraph(baseDSG) ;
        assertEquals(2, g.size()) ;
    }



}
