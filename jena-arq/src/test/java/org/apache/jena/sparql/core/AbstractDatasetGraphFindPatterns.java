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

import static org.apache.jena.atlas.iterator.Iter.toList ;
import static org.junit.Assert.assertNotNull ;

import java.util.Arrays ;
import java.util.Collection ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;
import org.junit.Test ;

/** Tests to cover cases of "find" patterns on a dataset graph
 * and graph from datasets.
 *
 * @see AbstractDatasetGraphFind
 * @see AbstractDatasetGraphTests
 * @see AbstractTestGraphOverDatasetGraph
 */
public abstract class AbstractDatasetGraphFindPatterns {
    
    // XXX Same for "graph", then Default graph, named graph.
    
    // Add/merge with AbstractDatasetGraphTests
    // Not for union graph here.
    
    static Quad q    = SSE.parseQuad("(:g :s :p :o)") ;
    
    static Quad q_g  = SSE.parseQuad("(:gx :s :p :o)") ;
    static Quad q_s  = SSE.parseQuad("(:g :sx :p :o)") ;
    static Quad q_p  = SSE.parseQuad("(:g :s :px :o)") ;
    static Quad q_o  = SSE.parseQuad("(:g :s :p :ox)") ;

    static Quad q_gs  = SSE.parseQuad("(:gx :sx :p :o)") ;
    static Quad q_gp  = SSE.parseQuad("(:gx :s :px :o)") ;
    static Quad q_go  = SSE.parseQuad("(:gx :s :p :ox)") ;
    static Quad q_sp  = SSE.parseQuad("(:g :sx :px :o)") ;
    static Quad q_so  = SSE.parseQuad("(:g :sx :p :ox)") ;
    static Quad q_po  = SSE.parseQuad("(:g :s :px :ox)") ;
    
    static Quad q_gsp = SSE.parseQuad("(:gx :sx :px :o)") ;
    static Quad q_gpo = SSE.parseQuad("(:gx :s :px :ox)") ;
    static Quad q_gso = SSE.parseQuad("(:gx :sx :p :ox)") ;
    static Quad q_spo = SSE.parseQuad("(:g :sx :px :ox)") ;
    
    static Quad q_gspo = SSE.parseQuad("(:gx :sx :px :ox)") ;
    
    static List<Quad> dataPattern = Arrays.asList(q, q_g, q_s, q_p, q_o, q_gs, q_gp, q_go, q_sp, q_so, q_po, q_gsp, q_gpo, q_gso, q_spo, q_gspo) ;
    
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
    
    private Node node(String str) { return SSE.parseNode(str) ; } 
    
    @Test public void find_quad_00() {
        DatasetGraph dsg = create(dataPattern) ;
        List<Quad> quads1 = Iter.toList(dsg.find()) ;
        assertNotNull("find()", quads1);
        List<Quad> quads2 = toList(dsg.find(null, null, null, null)) ;
        assertNotNull("find(null,null,null,null)", quads2);
        BaseTest.assertEqualsUnordered("find()", quads1, quads2);
    }
    
    @Test public void find_pattern_gspo() {
        DatasetGraph dsg = create(dataPattern) ;
        List<Quad> quads1 = toList(dsg.find(node(":g"), node(":s"), node(":p"), node(":o"))) ;
        assertNotNull("find(g,s,p,o)", quads1);
        List<Quad> quads2 = Arrays.asList(q) ;
        BaseTest.assertEqualsUnordered("find(gspo)", quads1, quads2);
    }
    
    @Test public void find_pattern_g() {
        DatasetGraph dsg = create(dataPattern) ;
        List<Quad> quads1 = Iter.toList(dsg.find(node(":gx"), null, null, null)) ;
        List<Quad> quads2 = Arrays.asList(q_g, q_gs, q_gp, q_go, q_gsp, q_gso, q_gpo, q_gspo) ;
        BaseTest.assertEqualsUnordered("find(g)", quads1, quads2);
    }
    
    @Test public void find_pattern_s() {
        DatasetGraph dsg = create(dataPattern) ;
        List<Quad> quads1 =  Iter.toList(dsg.find(null, node(":sx"), null, null)) ;
        List<Quad> quads2 = Arrays.asList(q_s, q_gs, q_sp, q_so, q_gsp, q_gso, q_spo, q_gspo) ;
        BaseTest.assertEqualsUnordered("find(s)", quads1, quads2);
    }

    @Test public void find_pattern_p() {
        DatasetGraph dsg = create(dataPattern) ;
        List<Quad> quads1 =  Iter.toList(dsg.find(null, null, node(":px"), null)) ;
        List<Quad> quads2 = Arrays.asList(q_p, q_gp, q_sp, q_po, q_gsp, q_gpo, q_spo, q_gspo) ;
        BaseTest.assertEqualsUnordered("find(p)", quads1, quads2);
    }

    @Test public void find_pattern_o() {
        DatasetGraph dsg = create(dataPattern) ;
        List<Quad> quads1 =  Iter.toList(dsg.find(null, null, null, node(":ox"))) ;
        List<Quad> quads2 = Arrays.asList(q_o, q_go, q_so, q_po, q_gpo, q_gso, q_spo, q_gspo) ;
        BaseTest.assertEqualsUnordered("find(o)", quads1, quads2);
    }
    
    @Test public void find_pattern_gs() {
        DatasetGraph dsg = create(dataPattern) ;
        List<Quad> quads1 =  Iter.toList(dsg.find(node(":gx"), node(":sx"), null, null)) ;
        List<Quad> quads2 = Arrays.asList(q_gs, q_gsp, q_gso, q_gspo) ;
        BaseTest.assertEqualsUnordered("find(gs)", quads1, quads2);
    }
    
    @Test public void find_pattern_gp() {
        DatasetGraph dsg = create(dataPattern) ;
        List<Quad> quads1 =  Iter.toList(dsg.find(node(":gx"), null, node(":px"), null)) ;
        List<Quad> quads2 = Arrays.asList(q_gp, q_gsp, q_gpo, q_gspo) ;
        BaseTest.assertEqualsUnordered("find(gp)", quads1, quads2);
    }


    @Test public void find_pattern_go() {
        DatasetGraph dsg = create(dataPattern) ;
        List<Quad> quads1 =  Iter.toList(dsg.find(node(":gx"), null, null, node(":ox"))) ;
        List<Quad> quads2 = Arrays.asList(q_go, q_gpo, q_gso, q_gspo) ;
        BaseTest.assertEqualsUnordered("find(go)", quads1, quads2);
    }
    
    @Test public void find_pattern_sp() {
        DatasetGraph dsg = create(dataPattern) ;
        List<Quad> quads1 =  Iter.toList(dsg.find(null, node(":sx"), node(":px"), null)) ;
        List<Quad> quads2 = Arrays.asList(q_sp, q_gsp, q_spo, q_gspo) ;
        BaseTest.assertEqualsUnordered("find(sp)", quads1, quads2);
    }
    
    @Test public void find_pattern_so() {
        DatasetGraph dsg = create(dataPattern) ;
        List<Quad> quads1 =  Iter.toList(dsg.find(null, node(":sx"), null, node(":ox"))) ;
        List<Quad> quads2 = Arrays.asList(q_so, q_gso, q_spo, q_gspo) ;
        BaseTest.assertEqualsUnordered("find(so)", quads1, quads2);
    }
    
    @Test public void find_pattern_po() {
        DatasetGraph dsg = create(dataPattern) ;
        List<Quad> quads1 =  Iter.toList(dsg.find(null, null, node(":px"), node(":ox"))) ;
        List<Quad> quads2 = Arrays.asList(q_po, q_gpo, q_spo, q_gspo) ;
        BaseTest.assertEqualsUnordered("find(po)", quads1, quads2);
    }

    @Test public void find_pattern_gsp() {
        DatasetGraph dsg = create(dataPattern) ;
        List<Quad> quads1 =  Iter.toList(dsg.find(node(":gx"), node(":sx"), node(":px"), null)) ;
        List<Quad> quads2 = Arrays.asList(q_gsp, q_gspo) ;
        BaseTest.assertEqualsUnordered("find(gsp)", quads1, quads2);

    }
    @Test public void find_pattern_gpo() {
        DatasetGraph dsg = create(dataPattern) ;
        List<Quad> quads1 =  Iter.toList(dsg.find(node(":gx"), null, node(":px"), node(":ox"))) ;
        List<Quad> quads2 = Arrays.asList(q_gpo, q_gspo) ;
        BaseTest.assertEqualsUnordered("find(gpo)", quads1, quads2);

    }
    @Test public void find_pattern_gso() {
        DatasetGraph dsg = create(dataPattern) ;
        List<Quad> quads1 =  Iter.toList(dsg.find(node(":gx"), null, node(":px"), node(":ox"))) ;
        List<Quad> quads2 = Arrays.asList(q_gpo, q_gspo) ;
        BaseTest.assertEqualsUnordered("find(gso)", quads1, quads2);
    }
    @Test public void find_pattern_spo() {
        DatasetGraph dsg = create(dataPattern) ;
        List<Quad> quads1 =  Iter.toList(dsg.find(null, node(":sx"), node(":px"), node(":ox"))) ;
        List<Quad> quads2 = Arrays.asList(q_spo, q_gspo) ;
        BaseTest.assertEqualsUnordered("find(spo)", quads1, quads2);
    }
}
