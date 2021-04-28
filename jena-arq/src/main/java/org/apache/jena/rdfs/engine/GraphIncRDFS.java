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

package org.apache.jena.rdfs.engine;

import static org.apache.jena.rdfs.engine.ConstRDFS.*;

import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.atlas.lib.StreamOps;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdfs.GraphRDFS;
import org.apache.jena.rdfs.setup.ConfigRDFS;
import org.apache.jena.rdfs.setup.MatchVocabRDFS;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

/**
 * RDFS graph over a base graph.
 * Also include the vocabulary and vocabulary-derived triples.
 *
 * @see GraphRDFS
 */
public class GraphIncRDFS extends GraphRDFS {
    private final MatchVocabRDFS vocab;
    private Set<Triple> extra;

    public GraphIncRDFS(Graph graph, ConfigRDFS<Node> setup) {
        super(graph, setup);
        this.vocab = new MatchVocabRDFS(setup);
        // Data-only "rdf:type T"
        this.extra = StreamOps.toSet(
            graph.stream(null, rdfType, null)
                .map(Triple::getObject)
                .filter(type->!setup.getSubClassHierarchy().keySet().contains(type))
                .map(type->Triple.create(type, rdfsSubClassOf, type))
                );

    }

    @Override
    public ExtendedIterator<Triple> find(Node s, Node p, Node o) {
        Stream<Triple> stream1 = super.stream(s, p, o);
        Stream<Triple> stream2 = vocab.match(s, p, o);
        Stream<Triple> stream = Stream.concat(stream1, stream2);

        // Include data-derived RDFS inferences.
        if ( wildcard(p) || rdfsSubClassOf.equals(p) ) {
            Stream<Triple> stream3 = extras(s, p, o);
            stream = Stream.concat(stream, stream3);
        }
        stream = stream.distinct();

        ExtendedIterator<Triple> iter = WrappedIterator.ofStream(stream);
        return iter;
    }

    private Stream<Triple> extras(Node s, Node p, Node o) {
        return extra.stream().filter(t->
            matchTerm(t.getSubject(),s) && matchTerm(t.getPredicate(),p) && matchTerm(t.getObject(),o) );
    }

    private boolean matchTerm(Node data, Node pattern) {
        if ( wildcard(pattern) )
            return true;
        return pattern.equals(data);
    }

    private boolean wildcard(Node n) { return n == null || Node.ANY.equals(n); }
}
