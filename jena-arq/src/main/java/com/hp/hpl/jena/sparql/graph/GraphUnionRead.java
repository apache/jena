/*
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

package com.hp.hpl.jena.sparql.graph;

import java.util.Collection ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.IteratorConcat ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.graph.TripleMatch ;
import com.hp.hpl.jena.graph.impl.GraphBase ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.WrappedIterator ;

/** Immutable graph that is the view of a union of graphs in a dataset. */ 
public class GraphUnionRead extends GraphBase
{
    // This exists for the property path evaulator to have a graph to call.
    private final DatasetGraph dataset ;
    private final Collection<Node> graphs ;

    public GraphUnionRead(DatasetGraph dsg, Collection<Node> graphs)
    {
        this.dataset = dsg ;
        this.graphs = graphs ; 
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
        IteratorConcat<Triple> iter = new IteratorConcat<>() ;
        for ( Node gn : graphs )
        {
            if ( ! GraphOps.containsGraph(dataset, gn) )
                continue ;
            
            ExtendedIterator<Triple> eIter = GraphOps.getGraph(dataset, gn).find(m) ;
            iter.add(eIter) ;
        }
        return WrappedIterator.create(Iter.distinct(iter)) ;
    }
}
