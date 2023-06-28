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
package org.apache.jena.mem2.store.legacy;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem2.collection.JenaMap;
import org.apache.jena.mem2.iterator.IteratorOfJenaSets;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NullIterator;

import java.util.Spliterator;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class NodeToTriplesMapMem implements NodeToTriplesMap {

    private final JenaMap<Node, TripleBunch> bunchMap = new HashedBunchMap();
    private final Triple.Field indexField;
    private final Triple.Field f2;
    private final Triple.Field f3;

    /**
     * The number of triples held in this NTM, maintained incrementally
     * (because it's a pain to compute from scratch).
     */
    private int size = 0;

    public NodeToTriplesMapMem(Triple.Field indexField, Triple.Field f2, Triple.Field f3) {
        this.indexField = indexField;
        this.f2 = f2;
        this.f3 = f3;
    }

    private Node getIndexNode(Triple t) {
        return indexField.getField(t);
    }

    @Override
    public void clear() {
        this.bunchMap.clear();
        this.size = 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    @SuppressWarnings("squid:S1121")
    public boolean tryAdd(Triple t) {
        final Node node = getIndexNode(t);

        TripleBunch s = bunchMap.get(node);
        if (s == null) {
            bunchMap.put(node, s = new ArrayBunch());
            s.addUnchecked(t);
            size++;
            return true;
        }

        if ((s.isArray()) && s.size() == 9) {
            bunchMap.put(node, s = new HashedTripleBunch(s));
        }
        if (s.tryAdd(t)) {
            size++;
            return true;
        }
        return false;
    }

    @Override
    public void addUnchecked(Triple t) {
        final Node node = getIndexNode(t);
        TripleBunch s = bunchMap.get(node);
        if (s == null) {
            s = new ArrayBunch();
            bunchMap.put(node, s);
        } else if ((s.isArray()) && s.size() == 9) {
            s = new HashedTripleBunch(s);
            bunchMap.put(node, s);
        }
        s.addUnchecked(t);
        size++;
    }

    @Override
    public boolean tryRemove(Triple t) {
        final Node node = getIndexNode(t);
        final TripleBunch s = bunchMap.get(node);

        if (s == null)
            return false;

        if (s.tryRemove(t)) {
            size--;
            if (s.isEmpty()) bunchMap.removeUnchecked(node);
            return true;
        }
        return false;
    }

    @Override
    public void removeUnchecked(Triple t) {
        final Node node = getIndexNode(t);
        final TripleBunch s = bunchMap.get(node);

        if (s == null)
            return;

        s.removeUnchecked(t);
        size--;
        if (s.isEmpty()) bunchMap.removeUnchecked(node);
    }

    @Override
    public ExtendedIterator<Triple> keyIterator() {
        return new IteratorOfJenaSets<>(bunchMap.valueIterator());
    }

    @Override
    public Spliterator<Triple> keySpliterator() {
        return keyStream().spliterator();
    }

    @Override
    public Stream<Triple> keyStream() {
        return StreamSupport.stream(bunchMap.valueSpliterator(), false)
                .flatMap(bunch -> StreamSupport.stream(bunch.keySpliterator(), false));
    }

    @Override
    public ExtendedIterator<Triple> iteratorForMatches(Node index, Node n2, Node n3) {
        final TripleBunch s = bunchMap.get(index);

        if (s == null) return NullIterator.instance();

        final var filter = FieldFilter.filterOn(f2, n2, f3, n3);
        return filter.hasFilter()
                ? s.keyIterator().filterKeep(filter.getFilter())
                : s.keyIterator();
    }

    @Override
    public Stream<Triple> streamForMatches(Node index, Node n2, Node n3) {
        final TripleBunch s = bunchMap.get(index);
        if (s == null)
            return Stream.empty();
        final var filter = FieldFilter.filterOn(f2, n2, f3, n3);
        return filter.hasFilter()
                ? StreamSupport.stream(s.keySpliterator(), false).filter(filter.getFilter())
                : StreamSupport.stream(s.keySpliterator(), false);
    }

    @Override
    public boolean containsMatch(Node index, Node n2, Node n3) {
        final TripleBunch s = bunchMap.get(index);
        if (s == null)
            return false;
        var filter = FieldFilter.filterOn(f2, n2, f3, n3);
        if (!filter.hasFilter())
            return true;
        return s.anyMatch(filter.getFilter());
    }

    @Override
    public boolean containsKey(Triple triple) {
        final TripleBunch s = bunchMap.get(getIndexNode(triple));
        if (s == null)
            return false;

        return s.containsKey(triple);
    }

    @Override
    public boolean anyMatch(Predicate<Triple> predicate) {
        return bunchMap.valueStream().anyMatch(bunch -> bunch.anyMatch(predicate));
    }
}
