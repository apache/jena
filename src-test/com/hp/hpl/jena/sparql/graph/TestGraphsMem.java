/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.graph;

import org.junit.Test ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory ;

/** Test the test for datasets and graphs */
public class TestGraphsMem extends GraphsTests
{
    //TODO reenable when memory DSGs support quads and union.
    @Override
    protected Dataset createDataset()
    {
        DatasetGraph dsg = DatasetGraphFactory.createMem() ;
        dsg.addGraph(Node.createURI(graph1), GraphFactory.createDefaultGraph()) ;
        dsg.addGraph(Node.createURI(graph2), GraphFactory.createDefaultGraph()) ;
        dsg.addGraph(Node.createURI(graph3), GraphFactory.createDefaultGraph()) ;
        return DatasetFactory.create(dsg) ;
    }
    
    @Override
    @Test public void graph4() {}           // Quad.unionGraph

    @Override
    @Test public void graph5() {}           // Quad.defaultGraphIRI

    @Override
    @Test public void graph6() {}           // defaultGraphNodeGenerated
    
    @Override
    @Test public void graph_api4() {}       // Quad.unionGraph
    
    @Override
    @Test public void graph_api5() {}       // defaultGraphIRI
    
    @Override
    @Test public void graph_api6() {}       // defaultGraphNodeGenerated
    
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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