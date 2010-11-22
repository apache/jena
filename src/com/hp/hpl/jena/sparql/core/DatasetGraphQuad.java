/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import java.util.Iterator ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.Transform ;

import com.hp.hpl.jena.graph.Node ;


/** A DatasetGraph base class for pure quad-centric storage.     
 */
public abstract class DatasetGraphQuad extends DatasetGraphBase
{
    static Transform<Quad, Node> projectGraphName = new Transform<Quad, Node>() {
        public Node convert(Quad quad)
        {
            return quad.getGraph() ; 
        }} ;
    
    public Iterator<Node> listGraphNodes()
    {
        Iter<Quad> iter = Iter.iter(find(Node.ANY, Node.ANY, Node.ANY, Node.ANY)) ;
        return iter.map(projectGraphName).distinct() ;
    }

    @Override
    public void removeGraph(Node graphName)
    { 
        deleteAny(graphName, Node.ANY, Node.ANY, Node.ANY) ;
    }
    
    //@Override
    public abstract Iterator<Quad> find(Node g, Node s, Node p, Node o) ;

    //@Override
    public abstract Iterator<Quad> findNG(Node g, Node s, Node p, Node o) ;

    @Override
    public abstract void add(Quad quad) ;

    @Override
    public abstract void delete(Quad quad) ;
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