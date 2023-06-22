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
package org.apache.jena.mem2.store.fast;

import org.apache.jena.graph.Triple;
import org.apache.jena.mem2.collection.FastHashMap;
import org.apache.jena.mem2.iterator.IteratorOfJenaSets;
import org.apache.jena.mem2.pattern.PatternClassifier;
import org.apache.jena.mem2.store.TripleStore;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NiceIterator;
import org.apache.jena.util.iterator.SingletonIterator;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A triple store that uses hash tables to map from nodes to triple bunches.
 * <p>
 * Inner structure:
 * - three {@link FastHashMap}, one for each node (subject, predicate, object) in the triple
 * - each map maps from a node to a {@link FastTripleBunch}
 * - for up to 16 triples with the same subject, the bunch is an {@link FastArrayBunch}, otherwise it is
 * a {@link FastHashedTripleBunch}
 * - for up to 32 triples with the same predicate and object, the bunch is an {@link FastArrayBunch}, otherwise it is
 * a {@link FastHashedTripleBunch}
 * <p>
 * Other optimizations:
 * - each triple is added to three {@link FastTripleBunch}es. To avoid the overhead of calculating the hash code three
 * times, the hash code is calculated once and passed to the {@link FastTripleBunch}es.
 * - the different sizes for the {@link FastArrayBunch}es are chosen:
 * - to avoid the memory-overhead of {@link FastHashedTripleBunch} for a small number of triples
 * - the subject bunch is smaller than the predicate and object bunches, because the subject is typically used for
 * #contains operations, which are faster for {@link FastHashedTripleBunch}es.
 * - the predicate and object bunches are the same size, because they are typically used for #find operations, which
 * typically do not answer #contains operations. Making them much larger might slow down #remove operations on the
 * graph.
 * - "ANY_PRE_OBJ" matches primarily use the object bunch unless the size of the bunch is larger than 400 triples. In
 * that case, there is a secondary lookup in the predicate bunch. Then the smaller bunch is used for the lookup.
 * Especially for RDF graphs there are some very common object nodes like "true"/"false" or "0"/"1". In that case,
 * a secondary lookup in the predicate bunch might be faster than a primary lookup in the object bunch.
 * - FastTripleStore#contains uses {@link org.apache.jena.mem2.store.fast.FastTripleBunch#anyMatchRandomOrder} for
 * "ANY_PRE_OBJ" lookups. This is only faster than {@link org.apache.jena.mem2.collection.JenaMapSetCommon#anyMatch}
 * if there are many matches and the set is ordered in an unfavorable way. "ANY_PRE_OBJ" matches usually fall into
 * this category. This optimization was only needed because the {@link FastTripleBunch}es does not use the random
 * order of the triples in #anyMatch but the ordered dense array of triples, which is faster if there are only a few
 * matches.
 * - for the FastArrayBunches, the equals method of the triple is not called. Instead, only the two nodes that are
 *   not part of the key of the containing map are compared.
 */
public class FastTripleStore implements TripleStore {

    protected static final int THRESHOLD_FOR_SECONDARY_LOOKUP = 400;
    protected static final int MAX_ARRAY_BUNCH_SIZE_SUBJECT = 16;
    protected static final int MAX_ARRAY_BUNCH_SIZE_PREDICATE_OBJECT = 32;
    final FastHashedBunchMap subjects = new FastHashedBunchMap();
    final FastHashedBunchMap predicates = new FastHashedBunchMap();
    final FastHashedBunchMap objects = new FastHashedBunchMap();
    private int size = 0;

    @Override
    public void add(Triple triple) {
        final int hashCodeOfTriple = triple.hashCode();
        final boolean added;
        var sBunch = subjects.get(triple.getSubject());
        if (sBunch == null) {
            sBunch = new ArrayBunchWithSameSubject();
            sBunch.addUnchecked(triple, hashCodeOfTriple);
            subjects.put(triple.getSubject(), sBunch);
            added = true;
        } else {
            if (sBunch.isArray() && sBunch.size() == MAX_ARRAY_BUNCH_SIZE_SUBJECT) {
                sBunch = new FastHashedTripleBunch(sBunch);
                subjects.put(triple.getSubject(), sBunch);
            }
            added = sBunch.tryAdd(triple, hashCodeOfTriple);
        }
        if (added) {
            size++;
            var pBunch = predicates.computeIfAbsent(triple.getPredicate(), ArrayBunchWithSamePredicate::new);
            if (pBunch.isArray() && pBunch.size() == MAX_ARRAY_BUNCH_SIZE_PREDICATE_OBJECT) {
                pBunch = new FastHashedTripleBunch(pBunch);
                predicates.put(triple.getPredicate(), pBunch);
            }
            pBunch.addUnchecked(triple, hashCodeOfTriple);
            var oBunch = objects.computeIfAbsent(triple.getObject(), ArrayBunchWithSameObject::new);
            if (oBunch.isArray() && oBunch.size() == MAX_ARRAY_BUNCH_SIZE_PREDICATE_OBJECT) {
                oBunch = new FastHashedTripleBunch(oBunch);
                objects.put(triple.getObject(), oBunch);
            }
            oBunch.addUnchecked(triple, hashCodeOfTriple);
        }
    }

    @Override
    public void remove(Triple triple) {
        final int hashCodeOfTriple = triple.hashCode();
        final var sBunch = subjects.get(triple.getSubject());
        if (sBunch == null)
            return;

        if (sBunch.tryRemove(triple, hashCodeOfTriple)) {
            if (sBunch.isEmpty()) {
                subjects.removeUnchecked(triple.getSubject());
            }
            final var pBunch = predicates.get(triple.getPredicate());
            pBunch.removeUnchecked(triple, hashCodeOfTriple);
            if (pBunch.isEmpty()) {
                predicates.removeUnchecked(triple.getPredicate());
            }
            final var oBunch = objects.get(triple.getObject());
            oBunch.removeUnchecked(triple, hashCodeOfTriple);
            if (oBunch.isEmpty()) {
                objects.removeUnchecked(triple.getObject());
            }
            size--;
        }
    }

    @Override
    public void clear() {
        subjects.clear();
        predicates.clear();
        objects.clear();
        size = 0;
    }

    @Override
    public int countTriples() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Triple tripleMatch) {
        switch (PatternClassifier.classify(tripleMatch)) {

            case SUB_PRE_OBJ: {
                final var triples = subjects.get(tripleMatch.getSubject());
                if (triples == null) {
                    return false;
                }
                return triples.containsKey(tripleMatch);
            }

            case SUB_PRE_ANY: {
                final var triplesBySubject = subjects.get(tripleMatch.getSubject());
                if (triplesBySubject == null) {
                    return false;
                }
                return triplesBySubject.anyMatch(t -> tripleMatch.getPredicate().equals(t.getPredicate()));
            }

            case SUB_ANY_OBJ: {
                final var triplesBySubject = subjects.get(tripleMatch.getSubject());
                if (triplesBySubject == null) {
                    return false;
                }
                return triplesBySubject.anyMatch(t -> tripleMatch.getObject().equals(t.getObject()));
            }

            case SUB_ANY_ANY:
                return subjects.containsKey(tripleMatch.getSubject());

            case ANY_PRE_OBJ: {
                final var triplesByObject = objects.get(tripleMatch.getObject());
                if (triplesByObject == null) {
                    return false;
                }
                // Optimization for typical RDF data, where there may be common values like "0" or "false"/"true".
                // In this case, there may be many matches but due to the ordered nature of FastHashBase,
                // the same predicates are often grouped together. If they are at the beginning of the bunch,
                // we can avoid the linear scan of the bunch. This is a common case for RDF data.
                // #anyMatchRandomOrder is a bit slower if the predicate is not found than #anyMatch, but not by much.
                if (triplesByObject.size() > THRESHOLD_FOR_SECONDARY_LOOKUP) {
                    final var triplesByPredicate = predicates.get(tripleMatch.getPredicate());
                    if (triplesByPredicate == null) {
                        return false;
                    }
                    if (triplesByPredicate.size() < triplesByObject.size()) {
                        return triplesByPredicate.anyMatchRandomOrder(t -> tripleMatch.getObject().equals(t.getObject()));
                    }
                }
                return triplesByObject.anyMatchRandomOrder(t -> tripleMatch.getPredicate().equals(t.getPredicate()));
            }

            case ANY_PRE_ANY:
                return predicates.containsKey(tripleMatch.getPredicate());

            case ANY_ANY_OBJ:
                return objects.containsKey(tripleMatch.getObject());

            case ANY_ANY_ANY:
                return !isEmpty();

            default:
                throw new IllegalStateException(String.format("Unexpected value: %s", PatternClassifier.classify(tripleMatch)));
        }
    }

    @Override
    public Stream<Triple> stream() {
        return StreamSupport.stream(subjects.valueSpliterator(), false)
                .flatMap(bunch -> StreamSupport.stream(bunch.keySpliterator(), false));
    }

    @Override
    public Stream<Triple> stream(Triple tripleMatch) {
        switch (PatternClassifier.classify(tripleMatch)) {

            case SUB_PRE_OBJ: {
                final var triples = subjects.get(tripleMatch.getSubject());
                if (triples == null) {
                    return Stream.empty();
                }
                return triples.containsKey(tripleMatch) ? Stream.of(tripleMatch) : Stream.empty();
            }

            case SUB_PRE_ANY: {
                final var triplesBySubject = subjects.get(tripleMatch.getSubject());
                if (triplesBySubject == null) {
                    return Stream.empty();
                }
                return triplesBySubject.keyStream().filter(t -> tripleMatch.getPredicate().equals(t.getPredicate()));
            }

            case SUB_ANY_OBJ: {
                final var triplesBySubject = subjects.get(tripleMatch.getSubject());
                if (triplesBySubject == null) {
                    return Stream.empty();
                }
                return triplesBySubject.keyStream().filter(t -> tripleMatch.getObject().equals(t.getObject()));
            }

            case SUB_ANY_ANY: {
                final var triples = subjects.get(tripleMatch.getSubject());
                return triples == null ? Stream.empty() : triples.keyStream();
            }

            case ANY_PRE_OBJ: {
                final var triplesByObject = objects.get(tripleMatch.getObject());
                if (triplesByObject == null) {
                    return Stream.empty();
                }
                if (triplesByObject.size() > THRESHOLD_FOR_SECONDARY_LOOKUP) {
                    final var triplesByPredicate = predicates.get(tripleMatch.getPredicate());
                    if (triplesByPredicate == null) {
                        return Stream.empty();
                    }
                    if (triplesByPredicate.size() < triplesByObject.size()) {
                        return triplesByPredicate.keyStream().filter(t -> tripleMatch.getObject().equals(t.getObject()));
                    }
                }
                return triplesByObject.keyStream().filter(t -> tripleMatch.getPredicate().equals(t.getPredicate()));
            }

            case ANY_PRE_ANY: {
                final var triples = predicates.get(tripleMatch.getPredicate());
                return triples == null ? Stream.empty() : triples.keyStream();
            }

            case ANY_ANY_OBJ: {
                final var triples = objects.get(tripleMatch.getObject());
                return triples == null ? Stream.empty() : triples.keyStream();
            }

            case ANY_ANY_ANY:
                return stream();

            default:
                throw new IllegalStateException("Unexpected value: " + PatternClassifier.classify(tripleMatch));
        }
    }

    @Override
    public ExtendedIterator<Triple> find(Triple tripleMatch) {
        switch (PatternClassifier.classify(tripleMatch)) {

            case SUB_PRE_OBJ: {
                final var triples = subjects.get(tripleMatch.getSubject());
                if (triples == null) {
                    return NiceIterator.emptyIterator();
                }
                return triples.containsKey(tripleMatch) ? new SingletonIterator<>(tripleMatch) : NiceIterator.emptyIterator();
            }

            case SUB_PRE_ANY: {
                final var triplesBySubject = subjects.get(tripleMatch.getSubject());
                if (triplesBySubject == null) {
                    return NiceIterator.emptyIterator();
                }
                return triplesBySubject.keyIterator().filterKeep(t -> tripleMatch.getPredicate().equals(t.getPredicate()));
            }

            case SUB_ANY_OBJ: {
                final var triplesBySubject = subjects.get(tripleMatch.getSubject());
                if (triplesBySubject == null) {
                    return NiceIterator.emptyIterator();
                }
                return triplesBySubject.keyIterator().filterKeep(t -> tripleMatch.getObject().equals(t.getObject()));
            }

            case SUB_ANY_ANY: {
                final var triples = subjects.get(tripleMatch.getSubject());
                return triples == null ? NiceIterator.emptyIterator() : triples.keyIterator();
            }

            case ANY_PRE_OBJ: {
                final var triplesByObject = objects.get(tripleMatch.getObject());
                if (triplesByObject == null) {
                    return NiceIterator.emptyIterator();
                }
                if (triplesByObject.size() > THRESHOLD_FOR_SECONDARY_LOOKUP) {
                    final var triplesByPredicate = predicates.get(tripleMatch.getPredicate());
                    if (triplesByPredicate == null) {
                        return NiceIterator.emptyIterator();
                    }
                    if (triplesByPredicate.size() < triplesByObject.size()) {
                        return triplesByPredicate.keyIterator().filterKeep(t -> tripleMatch.getObject().equals(t.getObject()));
                    }
                }
                return triplesByObject.keyIterator().filterKeep(t -> tripleMatch.getPredicate().equals(t.getPredicate()));
            }

            case ANY_PRE_ANY: {
                final var triples = predicates.get(tripleMatch.getPredicate());
                return triples == null ? NiceIterator.emptyIterator() : triples.keyIterator();
            }

            case ANY_ANY_OBJ: {
                final var triples = objects.get(tripleMatch.getObject());
                return triples == null ? NiceIterator.emptyIterator() : triples.keyIterator();
            }

            case ANY_ANY_ANY:
                return new IteratorOfJenaSets<>(subjects.valueIterator());

            default:
                throw new IllegalStateException("Unexpected value: " + PatternClassifier.classify(tripleMatch));
        }
    }

    protected static class ArrayBunchWithSameSubject extends FastArrayBunch {

        @Override
        public boolean areEqual(final Triple a, final Triple b) {
            return a.getPredicate().equals(b.getPredicate())
                    && a.getObject().equals(b.getObject());
        }
    }

    protected static class ArrayBunchWithSamePredicate extends FastArrayBunch {
        @Override
        public boolean areEqual(final Triple a, final Triple b) {
            return a.getSubject().equals(b.getSubject())
                    && a.getObject().equals(b.getObject());
        }
    }

    protected static class ArrayBunchWithSameObject extends FastArrayBunch {
        @Override
        public boolean areEqual(final Triple a, final Triple b) {
            return a.getSubject().equals(b.getSubject())
                    && a.getPredicate().equals(b.getPredicate());
        }
    }
}
