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

package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.impl.SimpleEventManager;
import com.hp.hpl.jena.graph.impl.WrappedGraph;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;

/**
 * A SafeGraph wraps a graph which might contain generalized RDF
 * triples and hides them from API queries so that consumers
 * of it are safe (but can use getRawGraph() to get back the unsafe graph.
 */
public class SafeGraph extends WrappedGraph implements Graph {

    /** Wrap a graph to hide generalized triples */
    public SafeGraph(Graph base) {
        super(base);
    }

    @Override
    public ExtendedIterator<Triple> find( TripleMatch m ) {
        return find(m.getMatchSubject(), m.getMatchPredicate(), m.getMatchObject());
    }
    
    @Override
    public ExtendedIterator<Triple> find( Node s, Node p, Node o ) {
        return SimpleEventManager.notifyingRemove( this, 
                base.find( s, p, o ).filterDrop( new Filter<Triple>() {
                    @Override
                    public boolean accept(Triple t) {
                        if (t.getSubject().isLiteral()) return true;
                        if (t.getPredicate().isBlank() || t.getPredicate().isLiteral()) return true;
                        return false;
                    }
                } ) );
    }

    /**
     * Return the unfiltered version of the graph
     */
    public Graph getRawGraph() {
        return base;
    }
    
}
