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

package org.apache.jena.sparql.graph ;

import java.util.Collection ;
import java.util.Iterator;
import java.util.function.Consumer ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.IteratorConcat ;
import org.apache.jena.atlas.lib.CollectionUtils;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.TransactionHandler;
import org.apache.jena.graph.Triple ;
import org.apache.jena.graph.impl.GraphBase ;
import org.apache.jena.shared.AddDeniedException ;
import org.apache.jena.shared.DeleteDeniedException ;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.shared.impl.PrefixMappingImpl ;
import org.apache.jena.sparql.core.*;
import org.apache.jena.util.iterator.ExtendedIterator ;
import org.apache.jena.util.iterator.NullIterator;
import org.apache.jena.util.iterator.WrappedIterator ;

/** Immutable graph that is the view of a union of graphs in a dataset.
 *  This union can be a fixed set of graph or all named graph.
 *  This union iterates over graphs.
 * <p>
 *  {@link GraphView} provides a view over a dataset and does support union graph but
 *  assumes quad access is efficient and does not end up looping.
 *  
 * @see GraphView
 * @see DatasetGraphMap
 */
public class GraphUnionRead extends GraphBase {
    private final DatasetGraph     dataset ;
    private final Collection<Node> graphs ;
    // Special case.
    private final Node graphName;

    /** Read-only graph view of all named graphs in the dataset.
     * If graphs are added after this view if created, then this is reflected in
     * the {@code find} call.
     */
    public GraphUnionRead(DatasetGraph dsg) {
        this(dsg, null) ;
    }
    
    /** Read-only graph view of a set of graphs from the dataset */ 
    public GraphUnionRead(DatasetGraph dsg, Collection<Node> graphs) {
        this.dataset = dsg ;
        this.graphs = graphs ;
        // Special case.
        if ( graphs != null && graphs.size() == 1 ) {
            // No need to suppress duplicates because there aren't any.
            // Assumes the dataset handles Quad.unionGraph.
            graphName = CollectionUtils.oneElt(graphs);
        }
        else
            graphName = null;
    }

    @Override
    protected PrefixMapping createPrefixMapping() {
        PrefixMapping pmap = new PrefixMappingImpl() ;
        forEachGraph((g) -> {
            PrefixMapping pmapNamedGraph = g.getPrefixMapping() ;
            pmap.setNsPrefixes(pmapNamedGraph) ;
        }) ;
        return pmap ;
    }

    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Triple m) {
        if ( graphs == null ) {
            // This produces unique quads with the same graph node,
            // hence the triples are distinct. 
            return quadsToTriples(dataset, Quad.unionGraph, m);
        }
        if ( graphs.isEmpty() )
            return NullIterator.instance();
        if ( graphName != null ) {
            if ( ! dataset.containsGraph(graphName) )
                // Avoid auto-creation.
                return NullIterator.instance();
            // Avoid needing distinct.
            return dataset.getGraph(graphName).find(m);
        }
        // Only certain graphs.
        IteratorConcat<Triple> iter = new IteratorConcat<>() ;
        forEachGraph((g) -> iter.add(g.find(m))) ;
        return WrappedIterator.createNoRemove(Iter.distinct(iter)) ;
    }
    
    private static ExtendedIterator<Triple> quadsToTriples(DatasetGraph dsg, Node graphName, Triple m) {
        Iterator<Quad> qIter = dsg.findNG(graphName, m.getSubject(), m.getPredicate(), m.getObject());
        Iterator<Triple> tIter = Iter.map(qIter, quad->quad.asTriple());
        return WrappedIterator.createNoRemove(tIter) ;
    }
    
    /** Execute action for each graph that exists */
    private void forEachGraph(Consumer<Graph> action) {
        if ( graphs == null ) {
            // Fast-path the dynamic union of all named graphs.
            dataset.listGraphNodes().forEachRemaining((gn) -> action.accept(dataset.getGraph(gn)));
            return ; 
        }
        
        graphs.stream()
          // Need to check to avoid auto-creation.
          .filter(gn -> dataset.containsGraph(gn))
          // For the explicit name of the default graph.
          .map(gn -> Quad.isDefaultGraph(gn) ? dataset.getDefaultGraph() : dataset.getGraph(gn))
          .forEach(action);
    }
    
    @Override public TransactionHandler getTransactionHandler() {
        return new TransactionHandlerView(dataset);
    }
    
    // Override to give more specific message.
    
    @Override
    public void performAdd(Triple t) {
        throw new AddDeniedException("GraphUnionRead::performAdd - Read-only graph") ;
    }

    @Override
    public void performDelete(Triple t) {
        throw new DeleteDeniedException("GraphUnionRead::performDelete - Read-only graph") ;
    }
}
