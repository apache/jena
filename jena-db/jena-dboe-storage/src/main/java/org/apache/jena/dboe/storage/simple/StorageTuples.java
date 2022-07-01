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

package org.apache.jena.dboe.storage.simple;

import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.dboe.storage.StorageRDF;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;

/** Split into storage of tuples/3 for triples and tuples/4 for quads */
public abstract class StorageTuples implements StorageRDF {
    // Split into triples and quads?

    private final StorageTuplesN triples;
    private final StorageTuplesN quads;

    protected StorageTuples(StorageTuplesN triples, StorageTuplesN quads) {
        if ( triples.N != 3 )
            throw new IllegalArgumentException("Triples storage is not for tuples of length 3: provided length is "+triples.N);
        if ( quads.N != 4 )
            throw new IllegalArgumentException("Quads storage is not for tuples of length 4: provided length is "+quads.N);
        this.triples = triples;
        this.quads = quads;
    }

    @Override
    public void add(Node s, Node p, Node o) { triples.add(tuple(s, p, o)); }

    @Override
    public void add(Node g, Node s, Node p, Node o) { quads.add(tuple(g, s, p, o)); }

    @Override
    public void delete(Node s, Node p, Node o) { triples.delete(tuple(s, p, o)); }

    @Override
    public void delete(Node g, Node s, Node p, Node o) { quads.delete(tuple(g, s, p, o)); }

    private static Tuple<Node> tuple(Node...n) {
        return TupleFactory.create(n);
    }

    private static Triple triple(Tuple<Node> tuple) {
        check(3, tuple);
        return Triple.create(tuple.get(0), tuple.get(1), tuple.get(2));
    }

    private static Quad quad(Tuple<Node> tuple) {
        check(4, tuple);
        return Quad.create(tuple.get(0), tuple.get(1), tuple.get(2), tuple.get(3));
    }

    private static void check(int N, Tuple<Node> terms) {
        if ( terms.len() != N )
            throw new IllegalArgumentException("Length "+terms.len()+" : expected "+N);
    }

    @Override
    public void removeAll(Node s, Node p, Node o) {
        triples.removeAll(tuple(s, p, o));
    }

    @Override
    public void removeAll(Node g, Node s, Node p, Node o) {
        quads.removeAll(tuple(g, s, p, o));
    }

    @Override
    public Iterator<Triple> find(Node s, Node p, Node o) {
        return stream(s, p, o).iterator();
    }

    @Override
    public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
        return stream(g, s, p, o).iterator();
    }

    @Override
    public Stream<Triple> stream(Node s, Node p, Node o) {
        return triples.find(tuple(s, p, o)).map(StorageTuples::triple);
    }

    @Override
    public Stream<Quad> stream(Node g, Node s, Node p, Node o) {
        return quads.find(tuple(g, s, p, o)).map(StorageTuples::quad);
    }

    @Override
    public boolean contains(Node s, Node p, Node o) {
        return triples.contain(tuple(s, p, o));
    }

    @Override
    public boolean contains(Node g, Node s, Node p, Node o) {
        return quads.contain(tuple(g, s, p, o));
    }
}
