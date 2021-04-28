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

import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdfs.engine.InfFindQuad;
import org.apache.jena.rdfs.engine.MatchRDFS;
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
        if ( g == null )
            g = Quad.defaultGraphIRI;

        Iterator<Quad> iter = findInf(g, s, p, o);
        if ( iter == null )
            return Iter.nullIterator();
        return iter;
    }

    private Iterator<Quad> findInf(Node g, Node s, Node p, Node o) {
        MatchRDFS<Node, Quad> infMatcher = new InfFindQuad(setup, g, getR());
        Stream<Quad> quads = infMatcher.match(s, p, o);
        Iterator<Quad> iter = quads.iterator();
        iter = Iter.onClose(iter, ()->quads.close());
        return iter;
    }

    @Override
    public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {
        if ( Quad.isDefaultGraph(g) )
            throw new IllegalArgumentException("Default graph is findNG call");
        if ( g == null )
            g = Node.ANY;
        Iterator<Quad> iter = findInf(g, s, p, o);
        if ( g == Node.ANY )
            // Exclude default graph by filter
            iter = Iter.filter(findInf(g, s, p, o), q-> ! q.isDefaultGraph());
        return iter;
    }

    @Override
    public boolean contains(Quad quad)
    { return contains(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()); }

    @Override
    public boolean contains(Node g, Node s, Node p, Node o)
    { return getR().contains(g, s, p, o); }
}
