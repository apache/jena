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
import org.apache.jena.mem2.collection.HashCommonMap;
import org.apache.jena.mem2.collection.JenaSet;
import org.apache.jena.mem2.store.TripleStore;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NiceIterator;
import org.apache.jena.util.iterator.SingletonIterator;

import java.util.stream.Stream;

/**
 * Successor of {@link org.apache.jena.mem.GraphTripleStoreMem} that uses term-equality
 * instead of literal value equality.
 * This implementation also does not support {@link java.util.Iterator#remove()}.
 * <p>
 * Inner structure:
 * - three {@link NodeToTriplesMapMem} instances for each of the three triple fields (subject, predicate, object)
 * - each of these maps is a {@link HashCommonMap} with {@link Node} keys and {@link JenaSet} values.
 * - for up to 9 triples with the same subject, predicate or object, the {@link JenaSet} is
 * a {@link ArrayBunch}, otherwise it is a {@link HashedTripleBunch}.
 * <p>
 * Additional optimizations:
 * - because we know that if a triple exists in one of the maps, it also exists in the other two, we can use the
 * {@link org.apache.jena.mem2.collection.JenaSet#addUnchecked(java.lang.Object)} and
 * {@link org.apache.jena.mem2.collection.JenaMapSetCommon#removeUnchecked(java.lang.Object)} methods to avoid
 * unnecessary checks.
 */
public class LegacyTripleStore implements TripleStore {

    private final NodeToTriplesMap subjects
            = new NodeToTriplesMapMem(Triple.Field.fieldSubject, Triple.Field.fieldPredicate, Triple.Field.fieldObject);
    private final NodeToTriplesMap predicates
            = new NodeToTriplesMapMem(Triple.Field.fieldPredicate, Triple.Field.fieldObject, Triple.Field.fieldSubject);
    private final NodeToTriplesMap objects
            = new NodeToTriplesMapMem(Triple.Field.fieldObject, Triple.Field.fieldSubject, Triple.Field.fieldPredicate);

    @Override
    public void add(Triple triple) {
        if (subjects.tryAdd(triple)) {
            predicates.addUnchecked(triple);
            objects.addUnchecked(triple);
        }
    }

    @Override
    public void remove(Triple triple) {
        if (subjects.tryRemove(triple)) {
            predicates.removeUnchecked(triple);
            objects.removeUnchecked(triple);
        }
    }

    @Override
    public void clear() {
        subjects.clear();
        predicates.clear();
        objects.clear();
    }

    @Override
    public int countTriples() {
        return subjects.size();
    }

    @Override
    public boolean isEmpty() {
        return subjects.isEmpty();
    }

    @Override
    public boolean contains(Triple tripleMatch) {
        if (tripleMatch.isConcrete()) {
            return subjects.containsKey(tripleMatch);
        }

        final Node pm = tripleMatch.getPredicate();
        final Node om = tripleMatch.getObject();
        final Node sm = tripleMatch.getSubject();
        if (sm.isConcrete())
            return subjects.containsMatch(sm, pm, om);
        else if (om.isConcrete())
            return objects.containsMatch(om, sm, pm);
        else if (pm.isConcrete())
            return predicates.containsMatch(pm, om, sm);
        else
            return !this.isEmpty();
    }

    @Override
    public Stream<Triple> stream() {
        return subjects.keyStream();
    }

    @Override
    public Stream<Triple> stream(Triple tripleMatch) {
        if (tripleMatch.isConcrete()) {
            return subjects.containsKey(tripleMatch) ? Stream.of(tripleMatch) : Stream.empty();
        }

        final Node pm = tripleMatch.getPredicate();
        final Node om = tripleMatch.getObject();
        final Node sm = tripleMatch.getSubject();

        if (sm.isConcrete())
            return subjects.streamForMatches(sm, pm, om);
        else if (om.isConcrete())
            return objects.streamForMatches(om, sm, pm);
        else if (pm.isConcrete())
            return predicates.streamForMatches(pm, om, sm);
        else
            return subjects.keyStream();
    }

    @Override
    public ExtendedIterator<Triple> find(Triple tripleMatch) {
        if (tripleMatch.isConcrete()) {
            return subjects.containsKey(tripleMatch) ? new SingletonIterator<>(tripleMatch) : NiceIterator.emptyIterator();
        }
        final Node pm = tripleMatch.getPredicate();
        final Node om = tripleMatch.getObject();
        final Node sm = tripleMatch.getSubject();

        if (sm.isConcrete())
            return subjects.iteratorForMatches(sm, pm, om);
        else if (om.isConcrete())
            return objects.iteratorForMatches(om, sm, pm);
        else if (pm.isConcrete())
            return predicates.iteratorForMatches(pm, om, sm);
        else
            return subjects.keyIterator();
    }
}
