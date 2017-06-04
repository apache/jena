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

import static org.apache.jena.atlas.iterator.Iter.asStream ;
import static org.apache.jena.atlas.iterator.Iter.toList;
import static org.apache.jena.atlas.iterator.Iter.toSet;
import static org.apache.jena.atlas.junit.BaseTest.assertEqualsUnordered;
import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.assertFalse ;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue ;
import static org.junit.Assert.fail;

import java.util.*;
import java.util.stream.Collectors ;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.sparql.graph.NodeConst ;
import org.apache.jena.sparql.sse.SSE ;
import org.junit.Assume ;
import org.junit.Before ;
import org.junit.Test ;

/** Tests to cover cases of "find" on a dataset graph.
 *  The coverage should be so that it tests all the frameworks
 *  (DatasetGraphBaseFind, DatasetGraphQuad) and the ways they send
 *  find operations to different methods. 
 */
public abstract class AbstractDatasetGraphFind {
    
    // More ordering tests.
    // More tests for AbstractDatasetGraphTests
    
    // == Union graph in tests.
    //   AbstractTestDynamicDataset
    //   AbstractTestGraphOverDataset
    //   AbstractTestUnionTransform -> TestUnionTransformQuads, TestUnionTransformTriples
    //   TestDatasetGraphViewGraphs
    
    //   GraphsTests -> TestGraphsMem (reenable)
    //   GraphsTests => AbstractTestGraphsTDB
    
    //   TestGraphUnionRead : GraphUnionRead
    //   TestUnionGraph
    static Node s = SSE.parseNode(":s") ;
    static Node p = SSE.parseNode(":p") ;
    static Node o = SSE.parseNode(":o") ;
    static Node g1 = SSE.parseNode(":g1") ;
    
    static Quad q1  = Quad.create(Quad.defaultGraphIRI,  s,  p, o ) ;
    static Quad q2  = Quad.create(Quad.defaultGraphIRI,  s,  p, NodeConst.nodeZero ) ;
    
    static Quad q3  = SSE.parseQuad("(:g1 :s :p :o)") ;
    static Quad q4  = SSE.parseQuad("(:g1 :s :p 1)") ;
    
    static Quad q5  = SSE.parseQuad("(:g2 :s :p :o)") ;
    static Quad q6  = SSE.parseQuad("(:g2 :s :p 1)") ;
    static Quad q7  = SSE.parseQuad("(:g2 :s :p 2)") ;

    static Quad q8  = SSE.parseQuad("(:g3 :s :p :o)") ;
    static Quad q9  = SSE.parseQuad("(:g3 :s :p 1)") ;
    static Quad q10 = SSE.parseQuad("(:g3 :s :p 2)") ;
    
    static List<Quad> data = Arrays.asList(q1, q2, q3, q4, q5, q6, q7, q8, q9, q10) ;
    
    // Helper.
    static void add(DatasetGraph data, Collection<Quad> quads) {
        for ( Quad q : quads ) data.add(q); 
    }

    /**
     * Create the dataset to test loaded with the supplied data - this dataset need not be mutable.
     * Either supply {@link #create()} or override this method.  
     */
    protected DatasetGraph create(Collection<Quad> data) {
        DatasetGraph dsg = create() ;
        add(dsg, data) ;
        return dsg ;
    }
    
    /** Easy route - create empty, mutable dataset.
     * If providing {@link #create(Collection)}, return null for this.
     */
    protected abstract DatasetGraph create() ;
        
    // Specifically having the DatasetGraphBaseFind.findInUnionGraph
    // This may be pulled into DatasetGraph sometime.
    protected DatasetGraphBaseFind createFind(DatasetGraph dsg) {
        try { 
            return (DatasetGraphBaseFind)dsg ;
        } catch (ClassCastException ex) {
            fail("Not a DatasetGraphBaseFind: "+dsg.getClass().getSimpleName()) ;
            return null ;
        }
    }
    
    private DatasetGraph dsg ;
    @Before public void setup() {
        dsg = create(data) ;
    }
    
    // Coverage of calls to DatasetGraphBaseFind:
    //    All: dft graph and named graphs
    //    findNG(Wildcard for g) ->  findInAnyNamedGraphs
    //    Fixed g - findInSpecificNamedGraph
    //    Default graph via quad -> findInDftGraph
    //    Union graph for g -> findInUnionGraph
    // Not al these tests are specific to DatasetGraphBaseFind
    
    @Test public void find_quad_01() { 
        List<Quad> x = toList(dsg.find()) ;
        assertEquals(10, x.size()) ;
        assertTrue(x.contains(q1)) ;
        assertTrue(x.contains(q5)) ;
    }
    
    @Test public void find_quad_02() { 
        List<Quad> x = toList(dsg.find(null, s,p,o)) ;
        assertEquals(4, x.size()) ;
        assertFalse(x.contains(q2)) ;
        assertFalse(x.contains(q4)) ;
        assertTrue(x.contains(q5)) ;
    }

    @Test public void find_ng_01() { 
        List<Quad> x = toList(dsg.findNG(null, null, null, null)) ;
        assertEquals(8, x.size()) ;
        assertTrue(x.contains(q4)) ;
        assertTrue(x.contains(q10)) ;
        assertFalse(x.contains(q1)) ;
    }
    
    @Test public void find_ng_02() { 
        List<Quad> x = toList(dsg.findNG(null, s, p, o)) ;
        assertEquals(3, x.size()) ;
        assertFalse(x.contains(q4)) ;
        assertFalse(x.contains(q1)) ;
        assertTrue(x.contains(q3)) ;
        assertTrue(x.contains(q5)) ;
        assertTrue(x.contains(q8)) ;
    }
    
    @Test public void find_specific_01() { 
        List<Quad> x = toList(dsg.find(g1, null, null, null)) ;
        assertEquals(2, x.size()) ;
        assertTrue(x.contains(q4)) ;
        assertTrue(x.contains(q3)) ;
    }
    
    @Test public void find_specific_02() { 
        List<Quad> x = toList(dsg.find(g1, null, null, NodeConst.nodeOne)) ;
        assertEquals(1, x.size()) ;
        assertTrue(x.contains(q4)) ;
    }
    
    @Test public void find_dft_01() { 
        List<Quad> x = toList(dsg.find(Quad.defaultGraphIRI, null, null, null)) ;
        assertEquals(2, x.size()) ;
        assertTrue(x.contains(q1)) ;
        assertTrue(x.contains(q2)) ;
    }
    
    @Test public void find_dft_02() { 
        List<Quad> x = toList(dsg.find(Quad.defaultGraphIRI, null, null, NodeConst.nodeOne)) ;
        assertEquals(0, x.size()) ;
    }
    
    @Test public void find_dft_03() { 
        List<Quad> x = toList(dsg.find(Quad.defaultGraphIRI, null, null, NodeConst.nodeZero)) ;
        assertEquals(1, x.size()) ;
        assertTrue(x.contains(q2)) ;
    }
    
    // Union graph by name.

    @Test public void find_union_01() {
        List<Quad> x = toList(dsg.find(Quad.unionGraph, null, null, null)) ;
        assertEquals(3, x.size()) ;
        x.stream().allMatch(q->q.getGraph().equals(Quad.unionGraph)) ;
        
        List<Triple> z = x.stream().map(Quad::asTriple).collect(Collectors.toList()) ;
        assertTrue(z.contains(q4.asTriple())) ;
        assertTrue(z.contains(q5.asTriple())) ;
        Quad qx = Quad.create(Quad.unionGraph, q4.asTriple()) ;
        assertTrue(x.contains(qx)) ;
        Quad qz = Quad.create(Quad.unionGraph, q2.asTriple()) ;
        assertFalse(x.contains(qz)) ;
    }

    // Union graph as graph
    
    @Test public void find_union_02() {
        DatasetGraphBaseFind dsgx = (DatasetGraphBaseFind)dsg ;
        assertNotNull(dsgx.getUnionGraph());
        List<Triple> x = toList(dsgx.getUnionGraph().find(null, null, null)) ;
        assertEquals(3, x.size()) ;
        assertTrue(x.contains(q4.asTriple())) ;
        assertTrue(x.contains(q5.asTriple())) ;
        assertTrue(x.contains(q10.asTriple())) ;
    }
    
    @Test public void find_union_03() {
        DatasetGraphBaseFind dsgx = (DatasetGraphBaseFind)dsg ;
        assertNotNull(dsgx.getUnionGraph());
        Set<Triple> x1 = toSet(dsgx.getUnionGraph().find(null, null, null)) ;
        Set<Triple> x2 = Iter.iter(dsg.find(Quad.unionGraph, null, null, null)).map(Quad::asTriple).toSet();
        assertEquals(x1, x2);
    }

    @Test(expected=AddDeniedException.class)
    public void find_union_04() {
        DatasetGraphBaseFind dsgx = (DatasetGraphBaseFind)dsg ;
        dsgx.getUnionGraph().add(q4.asTriple());
    }
    
    @Test(expected=DeleteDeniedException.class)
    public void find_union_05() {
        DatasetGraphBaseFind dsgx = (DatasetGraphBaseFind)dsg ;
        dsgx.getUnionGraph().delete(q4.asTriple());
    }
    
    // DatasetGraphBaseFind specific.
    
    @Test public void find_dsgFind_union_02() {
        Assume.assumeTrue("Not a DatasetGraphBaseFind", dsg instanceof DatasetGraphBaseFind) ;
        DatasetGraphBaseFind dsgx = (DatasetGraphBaseFind)dsg ;
        List<Triple> x = toList(dsgx.findInUnionGraph(null, null, null)) ;
        assertEquals(3, x.size()) ;
        assertTrue(x.contains(q4.asTriple())) ;
        assertTrue(x.contains(q5.asTriple())) ;
        assertTrue(x.contains(q10.asTriple())) ;
    }
    
    @Test public void find_dsgFind_union_03() {
        Assume.assumeTrue("Not a DatasetGraphBaseFind", dsg instanceof DatasetGraphBaseFind) ;
        DatasetGraphBaseFind dsgx = (DatasetGraphBaseFind)dsg ;
        List<Triple> x1 = toList(dsgx.findInUnionGraph(null, null, null)) ;
        List<Triple> x2 = quadsToDistinctTriples(dsg.find(Quad.unionGraph,null, null, null)) ;
        assertEqualsUnordered(x1, x2) ;
        assertEquals(3, x2.size()) ;
    }
    
    @Test public void find_dsgFind_union_04() {
        Assume.assumeTrue("Not a DatasetGraphBaseFind", dsg instanceof DatasetGraphBaseFind) ;
        DatasetGraphBaseFind dsgx = (DatasetGraphBaseFind)dsg ;
        List<Triple> x = toList(dsgx.findInUnionGraph(null, null, o)) ;
        //print(dsgx) ;
        assertEquals(1, x.size()) ;
        assertTrue(x.contains(q3.asTriple())) ;
    }
    
    @Test public void find_dsgFind_union_05() {
        Assume.assumeTrue("Not a DatasetGraphBaseFind", dsg instanceof DatasetGraphBaseFind) ;
        DatasetGraphBaseFind dsgx = (DatasetGraphBaseFind)dsg ;
        List<Triple> x1 = toList(dsgx.findInUnionGraph(null, null, o)) ;
        List<Triple> x2 = quadsToDistinctTriples(dsg.find(Quad.unionGraph, null, null, o)) ;
        assertEqualsUnordered(x1, x2) ;
        assertEquals(1, x2.size()) ;
    }
    
    static List<Triple> quadsToDistinctTriples(Iterator<Quad> iter) {
        return asStream(iter).map(Quad::asTriple).distinct().collect(Collectors.toList()) ;
    }
    
    static void print(List<Quad> x) {
        x.stream().sequential().forEach(System.out::println);
    }
}
