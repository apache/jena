/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import java.util.Iterator ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory ;

public class DatasetGraphFactory
{
    /** Create a DatasetGraph based on an existing one;
     *  this is a structre copy of the dataset struture 
     *  but graphs are shared 
     */ 
    public static DatasetGraph create(DatasetGraph dsg)
    { 
        // Fixed.
        //return new DatasetGraphMap(dsg) ;
        DatasetGraph dsg2 = createMem() ;
        copyOver(dsg2, dsg2) ;
        return dsg2 ;
    }
    
    private static void copyOver(DatasetGraph dsgDest, DatasetGraph dsgSrc)
    {
        dsgDest.setDefaultGraph(dsgSrc.getDefaultGraph()) ;
        for ( Iterator<Node> names = dsgSrc.listGraphNodes() ; names.hasNext() ; )
        {
            Node gn = names.next() ;
            dsgDest.addGraph(gn, dsgSrc.getGraph(gn)) ;
        }
    }

    /**
     * Create a DatasetGraph starting with a single graph.
     */
    public static DatasetGraph create(Graph graph)
    { 
        //return new DatasetGraphMap(graph) ; 
        DatasetGraph dsg2 = createMem() ;
        dsg2.setDefaultGraph(graph) ;
        return dsg2 ;
    }
    
    /**
     * Create a DatasetGraph which only ever has a single default graph.
     */
    public static DatasetGraph createOneGraph(Graph graph) { return new DatasetGraphOne(graph) ; } 
    
    private static DatasetGraphMaker.GraphMaker memGraphMaker = new DatasetGraphMaker.GraphMaker()
    {
        public Graph create()
        {
            return GraphFactory.createDefaultGraph() ;
        }
    } ;
    
    /**
     * Create a DatasetGraph which has all graphs in memory.
     */

    public static DatasetGraph createMem() { return new DatasetGraphMaker(memGraphMaker) ; }
    
    public static DatasetGraph createMemFixed() { return new DatasetGraphMap() ; }
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
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