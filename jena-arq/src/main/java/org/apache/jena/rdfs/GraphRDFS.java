/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.apache.jena.rdfs;

import java.util.stream.Stream;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdfs.engine.InfFindTriple;
import org.apache.jena.rdfs.engine.MatchRDFS;
import org.apache.jena.rdfs.setup.ConfigRDFS;
import org.apache.jena.sparql.graph.GraphWrapper;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

/**
 * RDFS graph over a base graph.
 */
public class GraphRDFS extends GraphWrapper {
    private final MatchRDFS<Node, Triple> source;
    private final ConfigRDFS<Node> setup;

    public GraphRDFS(Graph graph, ConfigRDFS<Node> setup) {
        super(graph);
        this.setup = setup;
        this.source = new InfFindTriple(setup, graph);
    }

    @Override
    public ExtendedIterator<Triple> find(Triple m) {
        return find(m.getSubject(), m.getPredicate(), m.getObject());
    }

    @Override
    public ExtendedIterator<Triple> find(Node s, Node p, Node o) {
        Stream<Triple> stream = source.match(s, p, o);
        ExtendedIterator<Triple> iter = WrappedIterator.ofStream(stream);
        return iter;
    }

    @Override
    public Stream<Triple> stream(Node s, Node p, Node o) {
        return source.match(s, p, o);
    }

    @Override
    public int size() {
        // Report the size of the underlying graph.
        // Even better, don't ask.
        return super.size();
    }

    @Override
    public boolean dependsOn(Graph other) {
        if ( other == super.get() )
            return true;
        return super.dependsOn(other);
    }
}
