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

package org.apache.jena.sparql.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.sparql.sse.SSE ;
import org.junit.Before ;
import org.junit.Test ;

/**
 * Tests of graphs as views of a DatasetGraph.
 */
public abstract class AbstractTestGraphOverDatasetGraph
{
    // See also: ARQ/GraphsTests, TestGraphsMem(enable tests?), UnionTransformTests

    protected DatasetGraph baseDSG ;
    protected abstract DatasetGraph createBaseDSG() ;

    private static Quad q0 = SSE.parseQuad("(_ <s> <p> 0)") ;
    private static Quad q1 = SSE.parseQuad("(<g1> <s> <p> 1)") ;
    private static Quad q2 = SSE.parseQuad("(<g2> <s> <p> 2)") ;    // same triple - different graph
    private static Quad q3 = SSE.parseQuad("(<g3> <s> <p> 2)") ;    // same triple - different graph

    private static Node gn1 = SSE.parseNode("<g1>") ;
    private static Node gn2 = SSE.parseNode("<g2>") ;
    private static Node gn3 = SSE.parseNode("<g3>") ;
    private static Node gnNotSuchGraph = SSE.parseNode("<NoSuchGraph>") ;

    protected abstract Graph makeNamedGraph(DatasetGraph dsg, Node gn) ;
    protected abstract Graph makeDefaultGraph(DatasetGraph dsg) ;

    @Before public void before()
    {
        baseDSG = createBaseDSG() ;
        baseDSG.add(q0) ;
        baseDSG.add(q1) ;
        baseDSG.add(q2) ;
        baseDSG.add(q3) ;
    }

    @Test public void graphDSG_view_1()
    {
        Triple t = makeDefaultGraph(baseDSG).find(null, null, null).next() ;
        assertEquals(SSE.parseTriple("(<s> <p> 0)"), t) ;
        // Check exact iterator.
    }

    @Test public void graphDSG_view_2()
    {
        Triple t = makeNamedGraph(baseDSG, gn1).find(null, null, null).next() ;
        assertEquals(SSE.parseTriple("(<s> <p> 1)"), t) ;
        // Check exact iterator.
    }

    @Test public void graphDSG_view_3()
    {
        Graph g = makeDefaultGraph(baseDSG) ;
        g.add(SSE.parseTriple("(<s> <p> 99)")) ;
        long x = Iter.count(baseDSG.find(Quad.defaultGraphNodeGenerated, null, null, null)) ;
        assertEquals(2, x) ;
        assertEquals(2, g.size()) ;
        // Check exact iterator.
    }

    // non-existant graph
    @Test public void graphDSG_view_4()
    {
        Graph g = makeNamedGraph(baseDSG, gnNotSuchGraph) ;
        long x = Iter.count(baseDSG.find(gnNotSuchGraph, null, null, null)) ;
        assertEquals(0, x) ;
        assertEquals(0, g.size()) ;
    }

    // This test only works if the underlying dataset implements Quad.unionGraph
    @Test public void graphDSG_view_union_1()
    {
        Graph g = makeNamedGraph(baseDSG, Quad.unionGraph) ;
        Iterator<Triple> iter = g.find(null,null,null) ;
        while(iter.hasNext())
            iter.next() ;
        assertEquals(2, g.size()) ;
    }

    // ---- prefixes

    @Test public void graphDSG_prefixes_1()
    {
        Graph g = makeNamedGraph(baseDSG, gn1) ;
        PrefixMapping pmap = g.getPrefixMapping() ;
        assertNotNull(pmap) ;
    }

    @Test public void graphDSG_prefixes_2()
    {
        Graph g = makeNamedGraph(baseDSG, Quad.unionGraph) ;
        PrefixMapping pmap = g.getPrefixMapping() ;
        assertNotNull(pmap) ;
    }

    @Test public void graphDSG_prefixes_3()
    {
        Graph g = makeDefaultGraph(baseDSG) ;
        PrefixMapping pmap = g.getPrefixMapping() ;
        assertNotNull(pmap) ;
    }

    @Test public void graphDSG_prefixes_4()
    {
        // All graphs exist.
        Graph g = makeNamedGraph(baseDSG, gnNotSuchGraph)  ;
        PrefixMapping pmap = g.getPrefixMapping() ;
        assertNotNull(pmap) ;
    }

    // ---- update

    @Test public void graphDGS_update_1()
    {
        Quad q9 = SSE.parseQuad("(<g3> <s> <p> 9)") ;
        Graph g = makeNamedGraph(baseDSG, gn3)  ;
        baseDSG.add(q9) ;
        Triple t = SSE.parseTriple("(<s> <p> 9)") ;
        assertTrue(g.contains(t)) ;
    }

    @Test public void graphDGS_update_2()
    {
        Triple t = SSE.parseTriple("(<s> <p> 9)") ;
        Graph g = makeNamedGraph(baseDSG, gn3)  ;
        g.add(t) ;
        Quad q9 = SSE.parseQuad("(<g3> <s> <p> 9)") ;
        assertTrue(baseDSG.contains(q9)) ;
    }
}
