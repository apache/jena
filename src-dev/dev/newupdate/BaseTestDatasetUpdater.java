/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package dev.newupdate;

import org.junit.Assert ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public abstract class BaseTestDatasetUpdater extends Assert
{
    protected abstract DatasetGraph getEmptyDatasetGraph() ;
    protected abstract DatasetUpdater getDatasetUpdater(DatasetGraph dsg) ;
    
    protected static final Node n1 = SSE.parseNode("<example>") ;
    protected static final Graph graph1 = SSE.parseGraph("(graph (<x> <p> 1))") ;
    protected static final Graph graph2 = SSE.parseGraph("(graph (<x> <p> 2))") ;
    
    @Test public void get_01()
    {
        DatasetGraph dsg = getEmptyDatasetGraph() ;
        DatasetUpdater updater = getDatasetUpdater(dsg) ;
        Graph graph = updater.doGet() ;
        assertNotNull(graph) ;
    }
    
    @Test public void get_02()
    {
        DatasetGraph dsg = getEmptyDatasetGraph() ;
        DatasetUpdater updater = getDatasetUpdater(dsg) ;
        Graph graph = updater.doGet(n1) ;
        assertNotNull(graph) ;
    }
    
    @Test public void put_01()
    {
        DatasetGraph dsg = getEmptyDatasetGraph() ;
        DatasetUpdater updater = getDatasetUpdater(dsg) ;
        updater.doPut(graph1) ;
        
        Graph graph = updater.doGet() ;
        assertNotNull(graph) ;
        assertTrue(graph.isIsomorphicWith(graph1)) ;
    }

    
    @Test public void put_02()
    {
        DatasetGraph dsg = getEmptyDatasetGraph() ;
        DatasetUpdater updater = getDatasetUpdater(dsg) ;
        updater.doPut(n1, graph1) ;
        
        Graph graph = updater.doGet() ;
        assertNotNull(graph) ;
        assertTrue(graph.isEmpty()) ;
        
        graph = updater.doGet(n1) ;
        assertNotNull(graph) ;
        assertTrue(graph.isIsomorphicWith(graph1)) ;
        
    }

}

/*
 * (c) Copyright 2010 Talis Information Ltd.
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