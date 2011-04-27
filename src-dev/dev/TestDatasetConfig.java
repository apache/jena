/*
 * (c) Copyright 2010, 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package dev;

import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;
import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBFactory ;

public class TestDatasetConfig extends BaseTest
{
    @BeforeClass
    public static void beforeClass()
    {
        //TDB.getContext().set(TDB.symUnionDefaultGraph, true) ;
        ds = TDBFactory.createDataset() ;
        DatasetGraph dsg = ds.asDatasetGraph() ;
        // Three triple in the default graph,
        // Two triples in the others
        // In all graphs: <s> <p> <o>
        
        dsg.add(SSE.parseQuad("(_ <s> <p> <o> )")) ;
        dsg.add(SSE.parseQuad("(_ <s> <p> 'A' )")) ;
        dsg.add(SSE.parseQuad("(_ <s> <p> 'B' )")) ;
        
        dsg.add(SSE.parseQuad("(<g1> <s> <p> <o> )")) ;
        dsg.add(SSE.parseQuad("(<g1> <s> <p> <o1> )")) ;
        
        dsg.add(SSE.parseQuad("(<g2> <s> <p> <o> )")) ;
        dsg.add(SSE.parseQuad("(<g2> <s> <p> <o2> )")) ;
        
        dsg.add(SSE.parseQuad("(<g3> <s> <p> <o> )")) ;
        dsg.add(SSE.parseQuad("(<g3> <s> <p> <o3> )")) ;
        
        dsg.getContext().set(TDB.symUnionDefaultGraph, true) ;
    }

    @AfterClass
    public static void afterClass()
    { }
    
    static Dataset ds = null ;
    
    
    @Test public void dsg_union_01()
    {
        count(ds.getDefaultModel(), 3) ;
        //count(ds.asDatasetGraph().getDefaultGraph(), 4) ;
    }

    @Test public void dsg_union_02()
    {
        count(ds.getNamedModel(Quad.defaultGraphIRI.getURI()), 3) ;
    }

    @Test public void dsg_union_03()
    {
        count(ds.getNamedModel(Quad.unionGraph.getURI()), 4) ;
    }

    @Test public void dsg_union_04()
    {
        count(ds.getNamedModel(Quad.defaultGraphNodeGenerated.getURI()), 3) ;
    }

    private void count(Model m, long expected)
    {
        long actual = Iter.count(m.listStatements()) ;
        assertEquals(expected, actual) ;
    }

    private void count(Graph graph, long expected)
    {
        long actual = Iter.count(graph.find(null,null,null)) ;
        assertEquals(expected, actual) ;
    }

}

/*
 * (c) Copyright 2010, 2011 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */