/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.migrate;

import java.util.Collection ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.IteratorConcat ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.graph.TripleMatch ;
import com.hp.hpl.jena.graph.query.QueryHandler ;
import com.hp.hpl.jena.graph.query.SimpleQueryHandler ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.graph.GraphBase2 ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.WrappedIterator ;

/** Immutable graph that is the view of a a union of graphs in a TDB store. */ 
public class GraphDynamicUnion extends GraphBase2
{
    // This exists for the property path evaulator to have a graph to call.
    private final DatasetGraph dataset ;
    private final Collection<Node> graphs ;

    public GraphDynamicUnion(DatasetGraph dsg, Collection<Node> graphs)
    {
        this.dataset = dsg ;
        this.graphs = graphs ; 
    }
    
    @Override
    public QueryHandler queryHandler()
    {
        return new SimpleQueryHandler(this) ;
    }

    @Override
    protected PrefixMapping createPrefixMapping()
    {
        PrefixMapping pmap = new PrefixMappingImpl() ;
        for ( Node gn : graphs )
        {
            if ( ! gn.isURI() ) continue ;
            Graph g = dataset.getGraph(gn) ;
            PrefixMapping pmapNamedGraph = g.getPrefixMapping() ;
            pmap.setNsPrefixes(pmapNamedGraph) ;
        }
        return pmap ;
    }

    @Override
    protected ExtendedIterator<Triple> graphBaseFind(TripleMatch m)
    {
        IteratorConcat<Triple> iter = new IteratorConcat<Triple>() ;
        for ( Node gn : graphs )
        {
            ExtendedIterator<Triple> eIter = dataset.getGraph(gn).find(m) ;
            iter.add(eIter) ;
        }
        return WrappedIterator.create(Iter.distinct(iter)) ;
    }
}

/*
 * (c) Copyright 2011 Epimorphics Ltd.
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