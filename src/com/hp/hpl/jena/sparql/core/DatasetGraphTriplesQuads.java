/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import com.hp.hpl.jena.graph.Node ;



/** A DatasetGraph base class for triples+quads storage.     
 */
public abstract class DatasetGraphTriplesQuads extends DatasetGraphBaseFind
{
    @Override
    public void add(Quad quad)
    {
        add(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject());
    }

    @Override
    public void delete(Quad quad)
    {
        delete(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject());
    }

    @Override
    public void add(Node g, Node s, Node p, Node o)
    {
        if ( Quad.isDefaultGraphGenerated(g) || Quad.isDefaultGraphExplicit(g) )
            addToDftGraph(s, p, o) ;
        else
            addToNamedGraph(g, s, p, o) ;
    }

    @Override
    public void delete(Node g, Node s, Node p, Node o)
    {
        if ( Quad.isDefaultGraphGenerated(g) || Quad.isDefaultGraphExplicit(g) )
            deleteFromDftGraph(s, p, o) ;
        else
            deleteFromNamedGraph(g, s, p, o) ;
    }
    
    protected abstract void addToDftGraph(Node s, Node p, Node o) ;
    protected abstract void addToNamedGraph(Node g, Node s, Node p, Node o) ;
    protected abstract void deleteFromDftGraph(Node s, Node p, Node o) ;
    protected abstract void deleteFromNamedGraph(Node g, Node s, Node p, Node o) ;
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