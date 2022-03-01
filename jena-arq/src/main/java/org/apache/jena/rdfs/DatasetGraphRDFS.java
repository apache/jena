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

package org.apache.jena.rdfs;

import static org.apache.jena.atlas.iterator.Iter.iter;

import java.util.Iterator;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphWrapper;
import org.apache.jena.sparql.core.DatasetGraphWrapperView;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context;

public class DatasetGraphRDFS extends DatasetGraphWrapper implements DatasetGraphWrapperView {
    // Do not unwrap for query execution.

    private final SetupRDFS setup;

    public DatasetGraphRDFS(DatasetGraph dsg, SetupRDFS setup) {
        super(dsg);
        this.setup = setup;
    }

    public DatasetGraphRDFS(DatasetGraph dsg, SetupRDFS setup, Context cxt) {
        super(dsg, cxt);
        this.setup = setup;
    }

    // Graph-centric access.
    @Override
    public Graph getDefaultGraph() {
        Graph base = getG().getDefaultGraph();
        return new GraphRDFS(base, setup);
    }

    @Override
    public Graph getUnionGraph() {
        Graph base = getG().getUnionGraph();
        return new GraphRDFS(base, setup);
    }

    @Override
    public Graph getGraph(Node graphNode) {
        Graph base = getG().getGraph(graphNode);
        if ( base == null )
            return null;
        return new GraphRDFS(base, setup);
    }

    // Quad-centric access
    @Override
    public Iterator<Quad> find(Quad quad) {
        return find(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject());
    }

    @Override
    public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
        Iterator<Quad> iter = findInf(g, s, p, o);
        if ( iter == null )
            return Iter.nullIterator();
        return iter;
    }

//    private Iterator<Quad> findInf(Node g, Node s, Node p, Node o) {
//        // Puts in the graph name for the quad base don g even if g is ANY or null.
//        MatchRDFS<Node, Quad> infMatcher = new InfFindQuad(setup, g, getR());
//        Stream<Quad> quads = infMatcher.match(s, p, o);
//        Iterator<Quad> iter = quads.iterator();
//        iter = Iter.onClose(iter, ()->quads.close());
//        return iter;
//    }

    /**
     * Find, graph by graph.
     */
    private Iterator<Quad> findInf(Node g, Node s, Node p, Node o) {
        if ( g != null && g.isConcrete() ) {
            // Includes the union graph case.
            return findOneGraphInf(g, s, p, o);
        }
        // Wildcard. Do each graph in-term.
        // This ensures the graph node of the quad corresponds to where the inference came from.
        Iter<Quad> iter1 = findOneGraphInf(Quad.defaultGraphIRI, s, p, o);
        Iterator<Quad> iter2 = findAllNamedGraphInf(s, p, o);
        return iter1.append(iter2);
    }

    // All named graphs, with inference. Quads refer to the name graph they were caused by.
    private Iterator<Quad> findAllNamedGraphInf(Node s, Node p, Node o) {
        return Iter.flatMap(listGraphNodes(), gn -> findOneGraphInf(gn, s, p, o));
    }

    // Single graph (inc. union graph). Quads refer to the name graph they were caused by.
    private Iter<Quad> findOneGraphInf(Node g, Node s, Node p, Node o) {
        if ( ! g.isConcrete()  )
            throw new IllegalStateException();
        // f ( Quad.isUnionGraph(g) ) {}
        // Specific named graph.
        return iter(getGraph(g).find(s,p,o)).map(t->Quad.create(g, t));
    }

    @Override
    public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {
        if ( Quad.isDefaultGraph(g) )
            throw new IllegalArgumentException("Default graph in findNG call");
        if ( g == null )
            g = Node.ANY;
        if ( g == Node.ANY )
            return findAllNamedGraphInf(s, p, o);
        // Same as specific named graph - we return quads in the union graph.
//        if ( Quad.isUnionGraph(g) ) {}
        return findOneGraphInf(g, s, p, o);
    }

    @Override
    public boolean contains(Quad quad)
    { return contains(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()); }

    @Override
    public boolean contains(Node g, Node s, Node p, Node o) {
        // Go through the inference machinery.
        Iterator<Quad> iter = find(g, s, p, o);
        try {
            return iter.hasNext();
        } finally { Iter.close(iter); }
    }
}
