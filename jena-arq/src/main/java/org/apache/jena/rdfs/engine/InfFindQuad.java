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

package org.apache.jena.rdfs.engine;

import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.rdfs.setup.ConfigRDFS;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

/**
 * Find in one graph of a dataset.
 * <p>
 * Unused.
 * <p>
 * The problem is that quads create by dstCreate have to reflect the graph name where
 * the triple that caused it to be inferred was found.
 */
public class InfFindQuad extends MatchRDFS<Node, Quad> {

    private final DatasetGraph dsg;
    private Node graph;

    private InfFindQuad(int dummy, ConfigRDFS<Node> setup, Node g, DatasetGraph dsg) {
        super(setup, Mappers.mapperQuad(g));
        if ( g == null || ! g.isConcrete() )
            throw new IllegalArgumentException("Quad/MatchRDFS must have a concrete graph name");
        this.graph = g;
        this.dsg = dsg;
    }

    @Override
    public Stream<Quad> sourceFind(Node s, Node p, Node o) {
        Iterator<Quad> iter = dsg.find(graph, s, p, o);
        Stream<Quad> stream = Iter.asStream(iter);
        return stream;
    }

    @Override
    protected boolean sourceContains(Node s, Node p, Node o) {
        return dsg.contains(graph, s, p, o);
    }

    @Override
    protected Quad dstCreate(Node s, Node p, Node o) {
        // Must be concrete for this quad creation.
        return Quad.create(graph, s, p, o);
    }
}
