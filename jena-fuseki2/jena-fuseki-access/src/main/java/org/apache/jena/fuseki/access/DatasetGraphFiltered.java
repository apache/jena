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

package org.apache.jena.fuseki.access;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphReadOnly;
import org.apache.jena.sparql.core.GraphView;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.GraphReadOnly;
import org.apache.jena.sparql.graph.GraphUnionRead;

/**
 * A read-only {@link DatasetGraph} that applies a filter testing all triples and quads
 * returned by accessing the data. Only quads where the filter tests for "true" are exposed. 
 * 
 */
public class DatasetGraphFiltered extends DatasetGraphReadOnly {
    
    // Or DatasetGraphTriplesQuads
    
    // Core operations at the triple/quad level.
    // This would work for TDB where the root access is triples/quads
    // but not for graph-style root access. 
    /*
  Write operations
    add(Quad)
    delete(Quad)
    add(Node, Node, Node, Node)
    delete(Node, Node, Node, Node)
    deleteAny(Node, Node, Node, Node)
    clear()
    
  Read operations  
    listGraphNodes()
    isEmpty()
    find()
    find(Quad)
    find(Node, Node, Node, Node)
    findNG(Node, Node, Node, Node)
    contains(Quad)
    contains(Node, Node, Node, Node)
    size()
    toString()
     */
    /*
    getGraph(Node)
    getDefaultGraph()
    containsGraph(Node)
     */

    private final Predicate<Quad> quadFilter;
    private final Collection<Node> visibleGraphs;

    public DatasetGraphFiltered(DatasetGraph dsg, Predicate<Quad> filter, Collection<Node> visibleGraphs) {
        super(dsg);
        this.quadFilter = filter;
        this.visibleGraphs = visibleGraphs;
    }
    
    private boolean filter(Quad quad) {
        return quadFilter.test(quad);
    }

    private Iterator<Quad> filter(Iterator<Quad> iter) {
        return Iter.filter(iter, this::filter);
    }
    
    // Need to intercept these because otherwise that are a GraphView of the wrapped "dsg", not this one.  

    @Override
    public Graph getDefaultGraph()
    {
        Graph g = GraphView.createDefaultGraph(this);
        return new GraphReadOnly(g);
    }
    
    @Override
    public Graph getGraph(Node graphNode) {
        if ( Quad.isUnionGraph(graphNode)) 
            return getUnionGraph(); 
        Graph g = GraphView.createNamedGraph(this, graphNode);
        return new GraphReadOnly(g);
   }

    @Override
    public Graph getUnionGraph() {
        return new GraphUnionRead(get(), visibleGraphs);
   }

    @Override
    public Iterator<Quad> find() {
        return filter(super.find());
    }

    @Override public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
        // Need union handling if for general API use.
        return filter(super.find(g, s, p, o));
    }
    
    @Override public Iterator<Quad> find(Quad quad) {
        // union
        return filter(super.find(quad));
    }
    
    @Override public Iterator<Quad> findNG(Node g, Node s, Node p , Node o) {
        // union
        return filter(super.findNG(g, s, p, o));
    }

    @Override public boolean contains(Node g, Node s, Node p , Node o) {
        return filter(super.find(g, s, p, o)).hasNext();
    }

    @Override public boolean contains(Quad quad) {
        return filter(super.find(quad)).hasNext();
    }
    
    @Override public boolean isEmpty() {
        return ! this.find().hasNext(); 
    }
}
