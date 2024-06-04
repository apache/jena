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

package org.apache.jena.ontapi.utils;

import org.apache.jena.atlas.iterator.FilterUnique;
import org.apache.jena.util.iterator.ClosableIterator;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NiceIterator;
import org.apache.jena.util.iterator.NullIterator;
import org.apache.jena.util.iterator.SingletonIterator;
import org.apache.jena.util.iterator.WrappedIterator;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Misc utils to work with Iterators, Streams, Collections, etc.
 *
 * @see ExtendedIterator
 * @see ClosableIterator
 * @see org.apache.jena.atlas.iterator.Iter
 */
public class Iterators {

    /**
     * Creates a new sequential {@code Stream} from the given {@code ExtendedIterator}.
     * Takes care about degenerate cases empty and single-element iterator.
     *
     * @param iterator {@link ExtendedIterator} of {@code X}-elements
     * @param <X>      anything
     * @return a {@code Stream} of {@code X}
     * @see #asStream(Iterator)
     */
    public static <X> Stream<X> asStream(ExtendedIterator<? extends X> iterator) {
        if (iterator instanceof NullIterator) {
            return Stream.empty();
        }
        if (iterator instanceof SingletonIterator) {
            return Stream.of(iterator.next());
        }
        return asStream((Iterator<? extends X>) iterator);
    }

    /**
     * Creates a new sequential {@code Stream} from the given {@code Iterator},
     * which is expected to deliver nonnull items:
     * it is required that the operation {@link Iterator#next()} must not return {@code null}
     * if the method {@link Iterator#hasNext()} answers {@code true}.
     * The method {@link Iterators#asStream(Iterator, int)} with the second parameter equals {@code 0} can be used to
     * create a {@code Stream} for an iterator that may deliver {@code null}s.
     * <p>
     * If the given parameter is {@link ClosableIterator},
     * remember to call {@link Stream#close()} explicitly if the iterator is not exhausted
     * (i.e. in case {@link Iterator#hasNext()} is still {@code true}).
     * It should be done for all short-circuiting terminal operations such as {@link Stream#findFirst()},
     * {@link Stream#findAny()}, {@link Stream#anyMatch(Predicate)} etc.
     *
     * @param iterator {@link Iterator} that delivers nonnull elements, cannot be {@code null}
     * @param <X>      the type of iterator-items
     * @return {@code Stream}
     */
    public static <X> Stream<X> asStream(Iterator<? extends X> iterator) {
        return asStream(iterator, Spliterator.NONNULL);
    }

    /**
     * Constructs a new sequential {@code Stream} from the given {@code Iterator},
     * with the specified {@code characteristics}.
     * If the given parameter is {@link ClosableIterator}, an explicit call to the {@link Stream#close()} method
     * is required for all short-circuiting terminal operations.
     *
     * @param iterator        {@link Iterator}, the {@code Spliterator}'s source, not {@code null}
     * @param characteristics {@code int}, characteristics of the {@code Spliterator}'s source
     * @param <X>             the type of iterator-items
     * @return a non-parallel {@code Stream}, that wraps the {@code iterator} with the given characteristics
     */
    public static <X> Stream<X> asStream(Iterator<? extends X> iterator, int characteristics) {
        return asStream(iterator, -1, characteristics);
    }

    /**
     * Constructs a new sequential {@code Stream} from the given {@code Iterator},
     * with the specified {@code characteristics} and estimated {@code size}.
     * If the given parameter is {@link ClosableIterator}, an explicit call to the {@link Stream#close()} method
     * is required for all short-circuiting terminal operations.
     *
     * @param iterator        {@link Iterator}, the {@code Spliterator}'s source, not {@code null}
     * @param size            {@code long}, a {@code Spliterator}'s estimates size, positive number or {@code -1}
     * @param characteristics {@code int}, characteristics of the {@code Spliterator}'s source
     * @param <X>             the type of iterator-items
     * @return a non-parallel {@code Stream}, that wraps the {@code iterator} with the given parameters
     */
    public static <X> Stream<X> asStream(Iterator<? extends X> iterator, long size, int characteristics) {
        Stream<X> res = StreamSupport.stream(asSpliterator(iterator, size, characteristics), false);
        return iterator instanceof ClosableIterator ? res.onClose(((ClosableIterator<?>) iterator)::close) : res;
    }

    /**
     * Creates a {@code Spliterator} using a given {@code Iterator} as the source of elements.
     * If the {@code size} is not {@code -1}, the returned {@code Spliterator} will report this number
     * as the initial {@link Spliterator#estimateSize() estimated size}.
     *
     * @param iterator        {@link Iterator}, not {@code null}
     * @param size            {@code long}, a positive number or {@code -1}
     * @param characteristics {@code int}, characteristics of the spliterator's source
     * @param <X>             the type of iterator-items
     * @return {@link Spliterator}
     * @throws NullPointerException if the given iterator is {@code null}
     */
    @SuppressWarnings("WeakerAccess")
    public static <X> Spliterator<X> asSpliterator(Iterator<? extends X> iterator, long size, int characteristics) {
        if (size < 0) {
            return Spliterators.spliteratorUnknownSize(iterator, characteristics);
        }
        return Spliterators.spliterator(iterator, size, characteristics);
    }

    /**
     * Creates a {@code Stream} for a future {@code Set}, which is produced by the factory-parameter {@code getAsSet}.
     * The produced {@code Set} must not change and must not contain {@code null}.
     *
     * @param getAsSet {@code Supplier} that produces a {@code Set} of {@code X}
     * @param <X>      the type of items
     * @return <b>distinct</b> sequential {@code Stream}
     * @see Iterators#create(Supplier)
     */
    public static <X> Stream<X> fromSet(Supplier<Set<X>> getAsSet) {
        int chs = Spliterator.NONNULL | Spliterator.DISTINCT | Spliterator.IMMUTABLE;
        return asStream(create(() -> getAsSet.get().iterator()), chs);
    }

    /**
     * Returns an {@link ExtendedIterator Extended Iterator} consisting of the results of replacing each element of
     * the given {@code base} iterator with the contents of a mapped iterator produced
     * by applying the provided mapping function ({@code map}) to each element.
     * A functional equivalent of {@link Stream#flatMap(Function)}, but for {@link ExtendedIterator}s.
     *
     * @param base   {@link ExtendedIterator} with elements of type {@code F}
     * @param mapper {@link Function} map-function with Object of type of {@code F} (or any super type) as an input,
     *               and an {@link Iterator} of type {@code T} (or any extended type) as an output
     * @param <F>    the element type of the base iterator (from)
     * @param <T>    the element type of the new iterator (to)
     * @return new {@link ExtendedIterator} of type {@code F}
     */
    @SuppressWarnings("unchecked")
    public static <T, F> ExtendedIterator<T> flatMap(ExtendedIterator<F> base,
                                                     Function<? super F, ? extends Iterator<? extends T>> mapper) {
        return WrappedIterator.createIteratorIterator(base.mapWith((Function<F, Iterator<T>>) mapper));
    }

    /**
     * Creates a lazily concatenated {@link ExtendedIterator Extended Iterator} whose elements are all the
     * elements of the first iterator followed by all the elements of the second iterator.
     * A functional equivalent of {@link Stream#concat(Stream, Stream)}, but for {@link ExtendedIterator}s.
     *
     * @param a   the first iterator
     * @param b   the second iterator
     * @param <X> the type of iterator elements
     * @return the concatenation of the two input iterators
     * @see Iterators#concat(ExtendedIterator[])
     */
    @SuppressWarnings("unchecked")
    public static <X> ExtendedIterator<X> concat(ExtendedIterator<? extends X> a, ExtendedIterator<? extends X> b) {
        return ((ExtendedIterator<X>) a).andThen(b);
    }

    /**
     * Creates a lazily concatenated {@link ExtendedIterator Extended Iterator} whose elements are all the
     * elements of the given iterators.
     * If the specified array has length equals {@code 2},
     * than this method is equivalent to the method {@link #concat(ExtendedIterator, ExtendedIterator)}).
     * An {@link ExtendedIterator}-based functional equivalent of
     * the expression {@code Stream#of(Stream, ..., Stream).flatMap(Function.identity())}.
     *
     * @param iterators Array of iterators
     * @param <X>       the type of iterator elements
     * @return all input elements as a single {@link ExtendedIterator} of type {@code X}
     * @see Iterators#concat(ExtendedIterator, ExtendedIterator)
     */
    @SafeVarargs
    public static <X> ExtendedIterator<X> concat(ExtendedIterator<? extends X>... iterators) {
        ExtendedIterator<X> res = NullIterator.instance();
        for (ExtendedIterator<? extends X> i : iterators) {
            res = res.andThen(i);
        }
        return res;
    }

    /**
     * Returns an extended iterator consisting of the elements of the specified extended iterator
     * that match the given predicate.
     * A functional equivalent of {@link Stream#filter(Predicate)}, but for {@link ExtendedIterator}s.
     *
     * @param iterator  {@link ExtendedIterator} with elements of type {@code X}
     * @param predicate {@link Predicate} to apply to elements of the iterator
     * @param <X>       the element type of the input and output iterators
     * @return a new iterator
     */
    @SuppressWarnings("unchecked")
    public static <X> ExtendedIterator<X> filter(ExtendedIterator<X> iterator, Predicate<? super X> predicate) {
        return iterator.filterKeep((Predicate<X>) predicate);
    }

    /**
     * Returns an {@link ExtendedIterator Extended Iterator} consisting of the elements
     * of the given {@code base} iterator, additionally performing the provided {@code action}
     * on each element as elements are consumed from the resulting iterator.
     * A functional equivalent of {@link Stream#peek(Consumer)}, but for {@link ExtendedIterator}s.
     *
     * @param base   {@link ExtendedIterator} with elements of type {@code X}
     * @param action {@link Consumer} action
     * @param <X>    the element type of the input and output iterators
     * @return new {@link ExtendedIterator} of type {@code X}
     */
    public static <X> ExtendedIterator<X> peek(ExtendedIterator<X> base, Consumer<? super X> action) {
        return base.mapWith(x -> {
            action.accept(x);
            return x;
        });
    }

    /**
     * Returns an {@link ExtendedIterator Extended Iterator} consisting of the distinct elements
     * (according to {@link Object#equals(Object)}) of the given iterator.
     * A functional equivalent of {@link Stream#distinct()}, but for {@link ExtendedIterator}s.
     * Warning: the result is temporary stored in memory!
     *
     * @param base {@link ExtendedIterator} with elements of type {@code X}
     * @param <X>  the element type of the input and output iterators
     * @return new {@link ExtendedIterator} of type {@code X} without duplicates
     */
    public static <X> ExtendedIterator<X> distinct(ExtendedIterator<X> base) {
        return base.filterKeep(new FilterUnique<>());
    }

    /**
     * Returns whether any elements of the given iterator match the provided predicate.
     * A functional equivalent of {@link Stream#anyMatch(Predicate)}, but for {@link Iterator}s.
     *
     * @param iterator  {@link Iterator} with elements of type {@code X}
     * @param predicate {@link Predicate} to apply to elements of the iterator
     * @param <X>       the element type of the iterator
     * @return {@code true} if any elements of the stream match the provided predicate, otherwise {@code false}
     * @see Iterators#allMatch(Iterator, Predicate)
     * @see Iterators#noneMatch(Iterator, Predicate)
     */
    public static <X> boolean anyMatch(Iterator<X> iterator, Predicate<? super X> predicate) {
        if (iterator instanceof NullIterator) return false;
        try {
            while (iterator.hasNext()) {
                if (predicate.test(iterator.next())) return true;
            }
        } finally {
            close(iterator);
        }
        return false;
    }

    /**
     * Returns whether all elements of the given iterator match the provided predicate.
     * A functional equivalent of {@link Stream#allMatch(Predicate)}, but for {@link Iterator}s.
     *
     * @param iterator  {@link Iterator} with elements of type {@code X}
     * @param predicate {@link Predicate} to apply to elements of the iterator
     * @param <X>       the element type of the iterator
     * @return {@code true} if either all elements of the iterator match the provided predicate
     * or the iterator is empty, otherwise {@code false}
     * @see Iterators#anyMatch(Iterator, Predicate)
     * @see Iterators#noneMatch(Iterator, Predicate)
     */
    public static <X> boolean allMatch(Iterator<X> iterator, Predicate<? super X> predicate) {
        if (iterator instanceof NullIterator) return true;
        try {
            while (iterator.hasNext()) {
                if (!predicate.test(iterator.next())) return false;
            }
        } finally {
            close(iterator);
        }
        return true;
    }

    /**
     * Returns whether no elements of the given iterator match the provided predicate.
     * A functional equivalent of {@link Stream#noneMatch(Predicate)}, but for {@link Iterator}s.
     *
     * @param iterator  {@link Iterator} with elements of type {@code X}
     * @param predicate {@link Predicate} to apply to elements of the iterator
     * @param <X>       the element type of the iterator
     * @return {@code true} if either no elements of the iterator match the provided predicate
     * or the iterator is empty, otherwise {@code false}
     * @see Iterators#anyMatch(Iterator, Predicate)
     * @see Iterators#allMatch(Iterator, Predicate)
     */
    public static <X> boolean noneMatch(Iterator<X> iterator, Predicate<? super X> predicate) {
        return allMatch(iterator, predicate.negate());
    }

    /**
     * Returns an {@link Optional} describing the first element of the iterator,
     * or an empty {@code Optional} if the iterator is empty.
     * A functional equivalent of {@link Stream#findFirst()}, but for {@link Iterator}s.
     * Warning: the method closes the specified iterator, so it is not possible to reuse it after calling this method.
     *
     * @param iterator {@link Iterator}, not {@code null}
     * @param <X>      the element type of the iterator
     * @return {@link Optional} of {@code X}
     * @throws NullPointerException if the element selected is {@code null}
     */
    public static <X> Optional<X> findFirst(Iterator<X> iterator) {
        if (iterator instanceof NullIterator) return Optional.empty();
        try {
            return iterator.hasNext() ? Optional.of(iterator.next()) : Optional.empty();
        } finally {
            close(iterator);
        }
    }

    /**
     * Returns the count of elements in the given iterator.
     * A functional equivalent of {@link Stream#count()}, but for {@link Iterator}s.
     * Warning: the method closes the specified iterator, so it is not possible to reuse it after.
     *
     * @param iterator {@link Iterator}, not {@code null}
     * @return long, the count of elements in the given {@code iterator}
     */
    public static long count(Iterator<?> iterator) {
        try {
            long res = 0;
            while (iterator.hasNext()) {
                iterator.next();
                res++;
            }
            return res;
        } finally {
            close(iterator);
        }
    }

    /**
     * Answers {@code true} iff the given iterator has more than {@code n} or equal to {@code n} elements.
     *
     * @param n        positive number
     * @param iterator {@link Iterator}, not {@code null}
     * @return {@code true} if the specified iterator has at least {@code n} elements
     */
    public static boolean hasAtLeast(Iterator<?> iterator, int n) {
        if (n <= 0) {
            throw new IllegalArgumentException();
        }
        try {
            long res = 0;
            while (iterator.hasNext()) {
                iterator.next();
                res++;
                if (res == n) {
                    return true;
                }
            }
            return false;
        } finally {
            close(iterator);
        }
    }

    /**
     * Answers {@code true} iff the given iterator has exactly {@code n} elements.
     *
     * @param n        positive number
     * @param iterator {@link Iterator}, not {@code null}
     * @return {@code true} if the specified iterator has exactly {@code n} elements
     */
    public static boolean hasExactly(Iterator<?> iterator, int n) {
        if (n <= 0) {
            throw new IllegalArgumentException();
        }
        try {
            long res = 0;
            while (iterator.hasNext()) {
                iterator.next();
                res++;
                if (res > n) {
                    return false;
                }
            }
            return res == n;
        } finally {
            close(iterator);
        }
    }

    /**
     * Performs {@code forEach}.
     * On finish iteration, the iterator will be closed.
     *
     * @param iterator {@link Iterator}, not {@code null}
     * @param action   {@link Consumer} accepting {@code X}
     * @param <X>      any
     */
    public static <X> void forEach(Iterator<X> iterator, Consumer<X> action) {
        try {
            while (iterator.hasNext()) {
                action.accept(iterator.next());
            }
        } finally {
            close(iterator);
        }
    }

    /**
     * Puts all the remaining items of the given iterator into the {@code collection},
     * and returns this collection itself.
     * This is a terminal operation.
     *
     * @param <X>    the element type of the iterator, not {@code null}
     * @param <C>    the {@code Collection} type, not {@code null}
     * @param source the {@code Iterator} with elements of type {@code X}
     * @param target the collection of type {@code C}
     * @return {@code C}, the same instance as specified
     */
    public static <X, C extends Collection<X>> C addAll(Iterator<? extends X> source, C target) {
        if (source instanceof NullIterator) {
            return target;
        }
        try {
            source.forEachRemaining(target::add);
            return target;
        } finally {
            close(source);
        }
    }

    /**
     * Takes the specified number of items from the source iterator.
     *
     * @param source {@link Iterator}
     * @param number int, positive
     * @param <X>    any
     * @return {@link Set}
     */
    public static <X> Set<X> takeAsSet(Iterator<? extends X> source, int number) {
        if (number < 0) {
            throw new IllegalArgumentException();
        }
        if (number == 0) {
            return Set.of();
        }
        Set<X> res = new HashSet<>();
        try {
            int i = 0;
            while (source.hasNext() && res.size() < number) {
                res.add(source.next());
            }
            return res;
        } finally {
            close(source);
        }
    }

    /**
     * Returns a {@code Map} (of the type of {@code M})
     * whose keys and values are the result of applying the provided mapping functions to the input elements.
     * A functional equivalent of {@code stream.collect(Collectors.toMap(...))}, but for plain {@link Iterator}s.
     * This method makes no guarantees about synchronization or atomicity properties of it.
     *
     * @param iterator      input elements in the form of {@link Iterator}
     * @param keyMapper     a mapping function to produce keys
     * @param valueMapper   a mapping function to produce values
     * @param mergeFunction a merge function, used to resolve collisions between values associated with the same key,
     *                      as supplied to {@link Map#merge(Object, Object, BiFunction)}
     * @param mapSupplier   a function which returns new, empty {@code Map} into which the results will be inserted
     * @param <X>           the type of the input elements
     * @param <K>           the output type of the key mapping function
     * @param <V>           the output type of the value mapping function
     * @param <M>           the type of the resulting {@code Map}
     * @return a {@code Map} whose keys are the result of applying a key mapping function to the input elements,
     * and whose values are the result of applying a value mapping function to all input elements
     * equal to the key and combining them using the merge function
     * @see Collectors#toMap(Function, Function, BinaryOperator, Supplier)
     */
    public static <X, K, V, M extends Map<K, V>> M toMap(Iterator<X> iterator,
                                                         Function<? super X, ? extends K> keyMapper,
                                                         Function<? super X, ? extends V> valueMapper,
                                                         BinaryOperator<V> mergeFunction,
                                                         Supplier<M> mapSupplier) {
        M res = mapSupplier.get();
        while (iterator.hasNext()) {
            X x = iterator.next();
            K k = keyMapper.apply(x);
            V v = valueMapper.apply(x);
            res.merge(k, v, mergeFunction);
        }
        return res;
    }

    /**
     * Closes iterator if it is {@link ClosableIterator CloseableIterator}.
     *
     * @param iterator {@link Iterator}
     */
    public static void close(Iterator<?> iterator) {
        if (iterator instanceof ClosableIterator) {
            ((ClosableIterator<?>) iterator).close();
        }
    }

    /**
     * Creates a new {@link ExtendedIterator Extended Iterator}} containing the specified elements.
     *
     * @param members Array of elements of the type {@code X}
     * @param <X>     the element type of the new iterator
     * @return a fresh {@link ExtendedIterator} instance
     */
    @SafeVarargs // Creating an iterator from an array is safe
    public static <X> ExtendedIterator<X> of(X... members) {
        return create(Arrays.asList(members));
    }

    /**
     * Creates a new {@link ExtendedIterator Extended Iterator}} containing nothing.
     *
     * @param <X> the element type of the new iterator
     * @return a fresh {@link ExtendedIterator} instance
     */
    public static <X> ExtendedIterator<X> of() {
        return NullIterator.instance();
    }

    /**
     * Creates a new {@link ExtendedIterator Extended Iterator}} containing a single specified element.
     *
     * @param item - an object of type {@code X}
     * @param <X>  the element type of the new iterator
     * @return a fresh {@link ExtendedIterator} instance
     */
    public static <X> ExtendedIterator<X> of(X item) {
        return new SingletonIterator<>(item);
    }

    /**
     * Creates a new {@link ExtendedIterator Extended Iterator}} over all elements of the specified collection.
     *
     * @param members {@code Collection} of elements of the type {@code X}
     * @param <X>     the element type of the new iterator
     * @return a fresh {@link ExtendedIterator} instance
     */
    public static <X> ExtendedIterator<X> create(Collection<? extends X> members) {
        return members.isEmpty() ? NullIterator.instance() : create(members.iterator());
    }

    /**
     * Answers an {@code ExtendedIterator} returning the elements of the specified {@code iterator}.
     * If the given {@code iterator} is itself an {@code ExtendedIterator}, return that;
     * otherwise wrap {@code iterator}.
     *
     * @param iterator {@link Iterator}, not {@code null}
     * @param <X>      the element type of the iterator
     * @return {@link ExtendedIterator} instance
     */
    @SuppressWarnings("unchecked")
    public static <X> ExtendedIterator<X> create(Iterator<? extends X> iterator) {
        return (ExtendedIterator<X>) WrappedIterator.create(iterator);
    }

    /**
     * Creates a new {@link ExtendedIterator Extended Iterator} over all elements of an iterator
     * which will be created by the {@code provider} on first iteration.
     * The returned iterator does not contain any elements,
     * but they will be derived at once when calling any of the {@code ExtendedIterator} methods.
     * <p>
     * The idea is to provide a truly lazy iterator
     * and, subsequently, a stream (through the {@link #asStream(Iterator)} method).
     * When any distinct operation (i.e. {@link #distinct(ExtendedIterator)} or {@link Stream#distinct()}) is used,
     * it, in fact, collects on demand an in-memory {@code Set} containing all elements,
     * but it will be appeared in process and an iterator or a stream initially weighs nothing.
     * This method allows achieving a similar behavior:
     * when creating an {@code ExtendedIterator} does not weight anything,
     * but it materializes itself when processing.
     * Therefore, operations such as {@code (stream-1 + stream-2).findFirst()} will demand less memory.
     * <p>
     * The returned iterator is not thread-safe, just as like any other RDF extended iterator we work with.
     *
     * @param provider {@link Supplier} deriving nonnull {@link Iterator}, cannot be {@code null}
     * @param <X>      the element type of the new iterator
     * @return a fresh {@link ExtendedIterator} instance wrapping a feature iterator
     */
    public static <X> ExtendedIterator<X> create(Supplier<Iterator<? extends X>> provider) {
        Objects.requireNonNull(provider);
        return new NiceIterator<X>() {
            private Iterator<? extends X> base;

            Iterator<? extends X> base() {
                return base == null ? base = Objects.requireNonNull(provider.get()) : base;
            }

            @Override
            public boolean hasNext() {
                return base().hasNext();
            }

            @Override
            public X next() {
                return base().next();
            }

            @Override
            public void remove() {
                base().remove();
            }

            @Override
            public void close() {
                if (base != null) {
                    close(base);
                }
            }
        };
    }

}
