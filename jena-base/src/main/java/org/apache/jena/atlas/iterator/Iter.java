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

package org.apache.jena.atlas.iterator ;

import java.io.PrintStream ;
import java.util.* ;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream ;
import java.util.stream.StreamSupport ;

import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.Sink ;

/**
 * Iter provides general utilities for working with {@link Iterator}s.
 * This class provides functionality similar to {@code Stream}
 * except for iterators (and hence single threaded).
 * <p>
 * Style 1: functional style using statics.
 *
 * <pre>
 *  import static org.apache.jena.atlas.iterator.Iter.* ;
 *
 *  filter(map(iterator, function), predicate)
 * </pre>
 *
 * Style 2: Stream-like: The class {@code Iter} provides methods to call on an iterator.
 *
 * <pre>
 * import static org.apache.jena.atlas.iterator.Iter.iter ;
 *
 * iter(iterator).map(...).filter(...)
 * </pre>
 *
 * @param <T> the type of element over which an instance of {@code Iter} iterates,
 */
public class Iter<T> implements Iterator<T> {

    /** Shorter form of "forEachRemaining" */
    public static <T> void forEach(Iterator<T> iter, Consumer<T> action) {
        iter.forEachRemaining(action);
    }

    public static <T> Stream<T> asStream(Iterator<T> iterator) {
        return asStream(iterator, false);
    }

    public static <T> Stream<T> asStream(Iterator<T> iterator, boolean parallel) {
        int characteristics = Spliterator.IMMUTABLE;
        Stream<T> stream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, characteristics), parallel);
        stream.onClose(()->close(iterator));
        return stream;
    }

    // ---- Special iterators.

    public static <T> Iterator<T> singleton(T item) {
        // There is a singleton iterator in Collections but it is not public.
        return new SingletonIterator<>(item) ;
    }

    public static <T> Iterator<T> nullIterator() {
        return Collections.emptyIterator();
    }

    // -- Stream inspired names

    public static <T> Iter<T> empty() {
        return Iter.iter(Collections.emptyIterator());
    }

    public static <T> Iter<T> of(T item) {
        return Iter.iter(new SingletonIterator<>(item)) ;
    }

    @SafeVarargs
    public static <T> Iter<T> of(T ...items) {
        return Iter.iter(Arrays.asList(items).iterator());
    }

    public static<T> Iter<T> ofNullable(T t) {
        return t == null ? Iter.empty() : Iter.of(t);
    }

    // --

    /**
     * Return an iterator that does not permit remove.
     * This makes an "UnmodifiableIterator".
     */
    public static <T> Iterator<T> noRemove(Iterator<T> iter) {
        return new IteratorNoRemove<T>(iter);
    }

    // ---- Collectors.

    /** Collect an iterator into a set. */
    public static <T> Set<T> toSet(Iterator<? extends T> stream) {
        return collect(stream, Collectors.toSet());
    }

    /** Collect an iterator into a list. */
    public static <T> List<T> toList(Iterator<? extends T> stream) {
        return collect(stream, Collectors.toList());
    }

    /**
     * Create another iterator without risk of concurrent modification
     * exceptions. This materializes the input iterator.
     */
    public static <T> Iterator<T> iterator(Iterator<? extends T> iterator) {
        List<T> x = Iter.toList(iterator) ;
        return x.iterator() ;
    }

    // Note fold-left and fold-right
    // http://en.wikipedia.org/wiki/Fold_%28higher-order_function%29

    // This reduce is a kind of fold-left (take first element, apply to rest of list)
    // and can deal with lists of zero elements.

    // -- Operations on iterators.

    @FunctionalInterface
    public interface Folder<X, Y> extends BiFunction<Y,X,Y> {}

    public static <T, R> R foldLeft(Iterator<? extends T> stream, R value, Folder<T, R> function) {
        // Tail recursion, unwound
        for (; stream.hasNext();) {
            T item = stream.next() ;
            value = function.apply(value, item) ;
        }
        return value ;
    }

    public static <T, R> R foldRight(Iterator<? extends T> stream, R value, Folder<T, R> function) {
        // Recursive.
        if ( !stream.hasNext() )
            return value ;
        T item = stream.next() ;
        return function.apply(foldRight(stream, value, function), item) ;
    }

    public static <T> Optional<T> reduce(Iterator<T> iter, BinaryOperator<T> accumulator) {
        T r = reduce(iter, null, accumulator);
        return Optional.ofNullable(r);
    }

    public static <T> T reduce(Iterator<T> iter, T identity, BinaryOperator<T> accumulator) {
        T result = identity;
        while(iter.hasNext()) {
            T elt = iter.next();
            result = (result == null) ? elt : accumulator.apply(result, elt);
        }
        return result;
    }

    // ---- min and max
    public static <T> Optional<T> min(Iterator<T> iter, Comparator<T> comparator) {
        T x = null;
        while(iter.hasNext()) {
            T elt = iter.next();
            if ( x == null )
                x = elt;
            else {
                int cmp = comparator.compare(x, elt);
                if ( cmp > 0 )
                    x = elt;
            }
        }
        return Optional.ofNullable(x);
    }

    public static <T> Optional<T> max(Iterator<T> iter, Comparator<T> comparator) {
        // Or min(iter, comparator.reversed())
        T x = null;
        while(iter.hasNext()) {
            T elt = iter.next();
            if ( x == null )
                x = elt;
            else {
                int cmp = comparator.compare(x, elt);
                if ( cmp < 0 )
                    x = elt;
            }
        }
        return Optional.ofNullable(x);
    }

    // ---- collect
    /** See {@link Stream#collect(Supplier, BiConsumer, BiConsumer)}, except without the {@code BiConsumer<R, R> combiner} */
    public static <T,R> R collect(Iterator<T> iter, Supplier<R> supplier, BiConsumer<R, ? super T> accumulator) {
        R result = supplier.get();
        while(iter.hasNext()) {
            T elt = iter.next();
            accumulator.accept(result, elt);
        }
        return result;
    }

    /** See {@link Stream#collect(Collector)} */
    public static <T, R, A> R collect(Iterator<T> iter, Collector<? super T, A, R> collector) {
        A a = collect(iter, collector.supplier(), collector.accumulator());
        return collector.finisher().apply(a);
    }

    /** Act on elements of an iterator.
     * @see #map(Iterator, Function)
     */
    public static <T> void apply(Iterator<? extends T> stream, Consumer<T> action) {
        for (; stream.hasNext();) {
            T item = stream.next() ;
            action.accept(item) ;
        }
    }

    // ---- Filter

    public static <T> Iterator<T> filter(final Iterator<? extends T> stream, final Predicate<T> filter) {
        final Iterator<T> iter = new Iterator<T>() {

            boolean finished     = false ;
            boolean slotOccupied = false ;
            T       slot ;

            @Override
            public boolean hasNext() {
                if ( finished )
                    return false ;
                while (!slotOccupied) {
                    if ( !stream.hasNext() ) {
                        finished = true ;
                        break ;
                    }
                    T nextItem = stream.next() ;
                    if ( filter.test(nextItem) ) {
                        slot = nextItem ;
                        slotOccupied = true ;
                        break ;
                    }
                }
                return slotOccupied ;
            }

            @Override
            public T next() {
                if ( hasNext() ) {
                    slotOccupied = false ;
                    return slot ;
                }
                throw new NoSuchElementException("filter.next") ;
            }
        } ;

        return iter ;
    }

    public static <T> Iterator<T> notFilter(final Iterator<? extends T> stream, final Predicate<T> filter) {
        return filter(stream, filter.negate()) ;
    }

    // Filter-related

    /**
     * Return true if every element of stream passes the filter (reads the
     * stream until the first element not passing the filter)
     */
    public static <T> boolean allMatch(Iterator<T> iter, Predicate<? super T> predicate) {
        while ( iter.hasNext() ) {
            T item = iter.next() ;
            if ( !predicate.test(item) )
                return false ;
        }
        return true ;
    }

    /**
     * Return true if one or more elements of stream passes the filter (reads
     * the stream to first element passing the filter)
     */
    public static <T> boolean anyMatch(Iterator<T> iter, Predicate<? super T> predicate) {
        while ( iter.hasNext() ) {
            T item = iter.next() ;
            if ( predicate.test(item) )
                return true ;
        }
        return false ;
    }

    /**
     * Return true if none of the elements of the iterator passes the predicate test  reads
     * the stream to first element passing the filter)
     */
    public static <T> boolean noneMatch(Iterator<T> iter, Predicate<? super T> predicate) {
        return ! anyMatch(iter, predicate);
    }

    /**
     * Return an Optional with the first element of an iterator that matches the predicate.
     * Return {@code Optional.empty} if none match.
     * Reads the iterator until the first match.
     */
    public static <T> Optional<T> findFirst(Iterator<T> iter, Predicate<? super T> predicate) {
        while ( iter.hasNext() ) {
            T item = iter.next() ;
            if ( predicate.test(item) )
                return Optional.of(item);
        }
        return Optional.empty() ;
    }

    /**
     * Return an Optional with the last element of an iterator that matches the predicate.
     * Return {@code Optional.empty} if no match.
     * Reads the iterator.
     */
    public static <T> Optional<T> findLast(Iterator<T> iter, Predicate<? super T> predicate) {
        T thing = null;
        while ( iter.hasNext() ) {
            T item = iter.next() ;
            if ( predicate.test(item) )
                thing = item;
        }
        return Optional.ofNullable(thing);
    }

    /**
     * Return an Optional with an element of an iterator that matches the predicate.
     * Return {@code Optional.empty} if none match.
     * The element returned is not specified by the API contract.
     */
    public static <T> Optional<T> findAny(Iterator<T> iter, Predicate<? super T> predicate) {
        return findFirst(iter, predicate);
    }

    // ---- Map

    /**
     * Apply a function to every element of an iterator, transforming it
     * from a {@code T} to an {@code R}.
     */
    public static <T, R> Iterator<R> map(Iterator<? extends T> stream, Function<T, R> converter) {
        Iterator<R> iter = new Iterator<R>() {
            @Override
            public boolean hasNext() {
                return stream.hasNext() ;
            }

            @Override
            public R next() {
                return converter.apply(stream.next()) ;
            }
        } ;
        return iter ;
    }

    /**
     * Apply a function to every element of an iterator, to produce possibly multiple mapping each time.
     * See {@link Stream#flatMap}
     */
    public static <T, R> Iterator<R> flatMap(Iterator<T> iter, Function<T, Iterator<R>> mapper) {
        return new IteratorFlatMap<>(iter, mapper);
    }

    /**
     * Apply an action to everything in stream, yielding a stream of the
     * same items.
     */
    public static <T> Iterator<T> operate(final Iterator<? extends T> stream, final Consumer<T> action) {
        final Iterator<T> iter = new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return stream.hasNext() ;
            }

            @Override
            public T next() {
                T t = stream.next() ;
                action.accept(t) ;
                return t ;
            }
        } ;
        return iter ;
    }

    /** Print an iterator as it gets used - this adds a printing wrapper */
    public static <T> Iterator<T> printWrapper(final Iterator<? extends T> stream) {
        return Iter.printWrapper(System.out, stream) ;
    }

    /** Print an iterator as it gets used - this adds a printing wrapper */
    public static <T> Iterator<T> printWrapper(final PrintStream out, final Iterator<? extends T> stream) {
        return Iter.operate(stream, out::println) ;
    }

    /** Join two iterators.
     * If there, potentially, going to be many iterators, it is better to
     * create an {@link IteratorConcat} explicitly and add each iterator.
     */
    public static <T> Iterator<T> append(Iterator<? extends T> iter1, Iterator<? extends T> iter2) {
        return IteratorCons.create(iter1, iter2) ;
    }

    /** Return an iterator that will see each element of the underlying iterator only once.
     * Note that this need working memory to remember the elements already seen.
     */
    public static <T> Iterator<T> distinct(Iterator<T> iter) {
        return filter(iter, new FilterUnique<T>()) ;
    }

    /** Remove adjacent duplicates. This operation does not need
     * working memory to remember the all elements already seen,
     * just a slot for the last element seen.
     */
    public static <T> Iterator<T> distinctAdjacent(Iterator<T> iter) {
        return filter(iter, new FilterDistinctAdjacent<T>()) ;
    }

    /** Remove nulls from an iterator */
    public static <T> Iterator<T> removeNulls(Iterator<T> iter) {
        return filter(iter, Objects::nonNull) ;
    }

    /** Step forward up to {@code steps} places.
     * <br/>Return number of steps taken.
     *
     * @apiNote
     * The iterator is moved at most {@code steps} places with no overshoot.
     * The iterator can be used afterwards.
     */
    public static int step(Iterator<?> iter, int steps) {
        for ( int i = 0 ; i < steps; i++) {
            if ( ! iter.hasNext() )
                return i;
            iter.next();
        }
        return steps;
    }

    /** Take the first N elements of an iterator - stop early if too few
     * @see #limit(Iterator, long)
     */
    public static <T> List<T> take(Iterator<T> iter, int N) {
        iter = new IteratorN<>(iter, N) ;
        List<T> x = new ArrayList<>(N) ;
        while ( iter.hasNext() )
            x.add(iter.next()) ;
        return x ;
    }

    /** Create an iterator such that it yields elements while a predicate test on
     *  the elements is true, end the iteration.
     *  @see Iter#filter(Iterator, Predicate)
     */
    public static <T> Iterator<T> takeWhile(Iterator<T> iter, Predicate<T> predicate) {
        return new IteratorTruncate<>(iter, predicate) ;
    }

    /**
     * Create an iterator such that it yields elements until a predicate test on
     * the elements becomes true, end the iteration.
     *
     * @see Iter#filter(Iterator, Predicate)
     */
    public static <T> Iterator<T> takeUntil(Iterator<T> iter, Predicate<T> predicate) {
        return new IteratorTruncate<>(iter, predicate.negate()) ;
    }

    /** Create an iterator such that elements from the front while
     *  a predicate test become true are dropped then return all remaining elements
     *  are iterated over.
     *  The first element where the predicted becomes true is the first element of the
     *  returned iterator.
     */
    public static <T> Iterator<T> dropWhile(Iterator<T> iter, Predicate<T> predicate) {
        PeekIterator<T> iter2 = new PeekIterator<>(iter) ;
        for(;;) {
            T elt = iter2.peek() ;
            if ( elt == null )
                return Iter.nullIterator() ;
            if ( ! predicate.test(elt) )
                break ;
        }
        return iter2 ;
    }

    /** Create an iterator such that elements from the front until
     *  a predicate test become true are dropped then return all remaining elements
     *  are iterated over.
     *  The first element where the predicate becomes true is the first element of the
     *  returned iterator.
     */
    public static <T> Iterator<T> dropUntil(Iterator<T> iter, Predicate<T> predicate) {
        return dropWhile(iter, predicate.negate()) ;
    }

    /** Return an iterator that is limited to the given number of elements.
     * If it is shorter than the limit, stop at the end.
     * @see #take(Iterator, int)
     */
    public static <X> Iterator<X> limit(Iterator<X> iterator, long limit) {
        final Iterator<X> iter = new Iterator<X>() {
            private long count = 0;
            @Override
            public boolean hasNext() {
                if ( count < limit )
                    return iterator.hasNext();
                return false;
            }

            @Override
            public X next() {
                 if ( ! hasNext() )
                     throw new NoSuchElementException();
                 X t = next();
                 count++;
                 return t;
            }
        };
        return iter;
    }

    /** Skip over a number of elements of an iterator */
    public static <X> Iterator<X> skip(Iterator<X> iterator, long limit) {
        for ( long i = 0; i < limit; i++ ) {
            if ( iterator.hasNext() )
                iterator.next();
            else
                // Now exhausted.
                break;
        }
        return iterator;
    }

    /** Iterator that only returns upto N items */
    static class IteratorN<T> implements Iterator<T> {
        private final Iterator<T> iter ;
        private final int         N ;
        private int               count ;

        IteratorN(Iterator<T> iter, int N) {
            this.iter = iter ;
            this.N = N ;
            this.count = 0 ;
        }

        @Override
        public boolean hasNext() {
            if ( count >= N )
                return false ;
            return iter.hasNext() ;
        }

        @Override
        public T next() {
            if ( count >= N )
                throw new NoSuchElementException() ;
            T x = iter.next() ;
            count++ ;
            return x ;
        }

        @Override
        public void remove() {
            // But leave the count as-is.
            iter.remove() ;
        }
    }

    /** Count the iterator (this is destructive on the iterator) */
    public static <T> long count(Iterator<T> iterator) {
        long x = 0 ;
        while (iterator.hasNext()) {
            iterator.next() ;
            x++ ;
        }
        return x ;
    }

    /** Consume the iterator */
    public static <T> void consume(Iterator<T> iterator) {
        count(iterator) ;
    }

    /** Create a string from an iterator, using the separator. Note: this consumes the iterator. */
    public static <T> String asString(Iterator<T> stream, String sep) {
        return Iter.iter(stream).map(x->x.toString()).collect(Collectors.joining(sep));
    }

    /** Create a string from an iterator, using the separator, prefix and suffix. Note: this consumes the iterator. */
    public static <T> String asString(Iterator<T> stream, CharSequence sep, CharSequence prefix, CharSequence suffix) {
        return Iter.iter(stream).map(x->x.toString()).collect(Collectors.joining(sep, prefix, suffix));
    }

    public static <T> void close(Iterator<T> iter) {
        if ( iter instanceof Closeable )
            ((Closeable)iter).close() ;
    }

    /**
     * Run an action when an iterator is closed.
     * This assumes the iterator closed with {@link Iter#close}.
     */
    public static <T> Iterator<T> onClose(Iterator<T> iter, Runnable closeHandler) {
        return new IteratorOnClose<>(iter, closeHandler);
    }

    /**
     * Print an iterator to stdout, return a copy of the iterator. Printing
     * occurs now. See {@link #debug} for an operation to print as the
     * iterator is used.
     */
    public static <T> Iterator<T> log(Iterator<T> stream) {
        return log(System.out, stream) ;
    }

    /**
     * Print an iterator to stdout, return a copy of the iterator. Printing
     * occurs now. See {@link #debug} for an operation to print as the
     * iterator is used.
     */
    public static <T> Iterator<T> log(final PrintStream out, Iterator<T> stream) {
        Iterator<T> iter = debug(out, stream) ;
        // And force it to run.
        if ( ! iter.hasNext() )
            out.println("<empty>");
        return Iter.toList(iter).iterator();
    }

    /**
     * Print an iterator to stdout, return a copy of the iterator. Printing
     * occurs when the iterator is used.  See {@link #log} for
     * an operation to print now.
     */
    public static <T> Iterator<T> debug(Iterator<T> stream) {
        return debug(System.out, stream) ;
    }

    /**
     * Print an iterator, return a copy of the iterator. Printing
     * occurs as the returned iterator is used.
     */
    public static <T> Iterator<T> debug(final PrintStream out, Iterator<T> stream) {
        try {
            return map(stream, item -> {out.println(item); return item;}) ;
        } finally { out.flush() ; }
    }

    /** Print an iterator (destructive) */
    public static <T> void print(Iterator<T> stream) {
        print(System.out, stream) ;
    }

    /** Print an iterator (destructive) */
    public static <T> void print(PrintStream out, Iterator<T> stream) {
        apply(stream, out::println) ;
    }

    /** Send the elements of the iterator to a sink - consumes the iterator */
    public static <T> void sendToSink(Iterator<T> iter, Sink<T> sink) {
        while ( iter.hasNext() ) {
            T thing = iter.next() ;
            sink.send(thing) ;
        }
        sink.close() ;
    }

    // ----
    // Iter class part : factories

    public static <T> Iter<T> iter(Iter<T> iter) {
        return iter ;
    }

    public static <T> Iter<T> iter(Collection<T> collection) {
        return iter(collection.iterator()) ;
    }

    public static <T> Iter<T> iter(Iterator<T> iterator) {
        Objects.requireNonNull(iterator);
        if ( iterator instanceof Iter<? > )
            return (Iter<T>)iterator ;
        return new Iter<>(iterator) ;
    }

    public static <T> Iter<T> singletonIter(T item) {
        return iter(new SingletonIterator<>(item)) ;
    }

    public static <T> Iter<T> nullIter() {
        return iter(new NullIterator<T>()) ;
    }

    /**
     * Materialize an iterator, that is, force it to run now - useful in
     * debugging or when iteration may modify underlying datastructures.
     */
    public static <T> Iterator<T> materialize(Iterator<T> iter) {
        return toList(iter).iterator() ;
    }

    /** An {@code Iter} of 2 {@code Iter}'s */
    public static <T> Iter<T> concat(Iter<T> iter1, Iter<T> iter2) {
        if ( iter1 == null )
            return iter2 ;
        if ( iter2 == null )
            return iter1 ;
        return iter1.append(iter2) ;
    }

    /** An {@code Iterator} of 2 {@code Iterator}'s.
     * See also {@link IteratorConcat}.
     */
    public static <T> Iter<T> concat(Iterator<T> iter1, Iterator<T> iter2) {
        if ( iter1 == null )
            return iter(iter2) ;
        if ( iter2 == null )
            return iter(iter1) ;
        return iter(iter1).append(iter(iter2)) ;
    }

    /** Return the first element of an iterator or null if no such element.
     * @param iter
     * @return An item or null.
     */
    public static <T> T first(Iterator<T> iter) {
        return first(iter, (x)-> true ) ;
    }

    /** Skip to the first element meeting a condition and return that element. */
    public static <T> T first(Iterator<T> iter, Predicate<T> filter) {
        while (iter.hasNext()) {
            T t = iter.next() ;
            if ( filter.test(t) )
                return t ;
        }
        return null ;
    }

    /** Skip to the first element meeting a condition and return that element's index (zero-based). */
    public static <T> int firstIndex(Iterator<T> iter, Predicate<T> filter) {
        for (int idx = 0; iter.hasNext(); idx++) {
            T t = iter.next() ;
            if ( filter.test(t) )
                return idx ;
        }
        return -1 ;
    }

    /** Return the last element or null, if no elements. This operation consumes the iterator. */
    public static <T> T last(Iterator<T> iter) {
        return last(iter, (x)->true) ;
    }

    /** Return the last element satisfying a predicate. This operation consumes the whole iterator. */
    public static <T> T last(Iterator<T> iter, Predicate<? super T> filter) {
        T thing = null ;
        while (iter.hasNext()) {
            T t = iter.next() ;
            if ( filter.test(t) )
                thing = t ;
        }
        return thing ;
    }

    /** Find the last occurrence, defined by a predicate, in a list. */
    public static <T> int lastIndex(List<T> list, Predicate<? super T> filter) {
        for (int idx = list.size()-1; idx >= 0 ; idx--) {
            T t = list.get(idx);
            if ( filter.test(t) )
                return idx;
        }
        return -1;
    }

    // ------------------------------------------------------
    // The class.

    private Iterator<T> iterator ;

    private Iter(Iterator<T> iterator) {
        this.iterator = iterator ;
    }

    /** Apply the Consumer to each element of the iterator */
    public void forEach(Consumer<T> action) {
        iterator.forEachRemaining(action);
    }

    /** Consume the {@code Iter} and produce a {@code Set} */
    public Set<T> toSet() {
        return toSet(iterator) ;
    }

    /** Consume the {@code Iter} and produce a {@code List} */
    public List<T> toList() {
        return toList(iterator) ;
    }

    public void sendToSink(Sink<T> sink) {
        sendToSink(iterator, sink) ;
    }

    public T first() {
        return first(iterator) ;
    }

    public T last() {
        return last(iterator) ;
    }

    /** Skip to the first element meeting a condition and return that element. */
    public T first(Predicate<T> filter) {
        return first(iterator, filter) ;
    }

    /** Skip to the first element meeting a condition and return that element's index (zero-based). */
    public int firstIndex(Predicate<T> filter) {
        return firstIndex(iterator, filter) ;
    }

    /** Filter by predicate */
    public Iter<T> filter(Predicate<T> filter) {
        return iter(filter(iterator, filter)) ;
    }

    public boolean allMatch(Predicate<? super T> predicate) {
        return allMatch(iterator, predicate);
    }

    public boolean anyMatch(Predicate<? super T> predicate) {
        return anyMatch(iterator, predicate);
    }

    public boolean noneMatch(Predicate<? super T> predicate) {
        return noneMatch(iterator, predicate);
    }

    public Optional<T> findFirst(Predicate<? super T> predicate) {
        return findFirst(iterator, predicate);
    }

    public Optional<T> findAny(Predicate<? super T> predicate) {
        return findAny(iterator, predicate);
    }

    public Optional<T> findLast(Predicate<? super T> predicate) {
        return findLast(iterator, predicate);
    }

    /** Remove nulls */
    public Iter<T> removeNulls() {
        return iter(removeNulls(this)) ;
    }

    /** Map each element using given function */
    public <R> Iter<R> map(Function<T, R> converter) {
        return iter(map(iterator, converter)) ;
    }

    /** FlatMap each element using given function of element to iterator of mapped elements.s */
    public <R> Iter<R> flatMap(Function<T, Iterator<R>> converter) {
        return iter(flatMap(iterator, converter)) ;
    }
    /**
     * Apply an action to everything in the stream, yielding a stream of the
     * original items.
     */
    public Iter<T> operate(Consumer<T> action) {
        return iter(operate(iterator, action)) ;
    }

    public <R> R foldLeft(R initial, Folder<T, R> accumulator) {
        return foldLeft(iterator, initial, accumulator) ;
    }

    public <R> R foldRight(R initial, Folder<T, R> accumulator) {
        return foldRight(iterator, initial, accumulator) ;
    }

    /** Reduce.
     * This reduce is fold-left (take first element, apply to rest of list)
     */
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        return reduce(iterator, accumulator) ;
    }

    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return reduce(iterator, identity, accumulator);
    }

    public Optional<T> min(Comparator<T> comparator) {
        return min(iterator, comparator);
    }

    public Optional<T> max(Comparator<T> comparator) {
        return max(iterator, comparator);
    }

    /** See {@link Stream#collect(Supplier, BiConsumer, BiConsumer)}, except without the {@code BiConsumer<R, R> combiner} */
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, T> accumulator/*, BiConsumer<R, R> combiner*/) {
        return collect(iterator, supplier, accumulator);
    }

    /** See {@link Stream#collect(Collector)} */
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        return collect(iterator, collector);
    }

    /** Apply an action to every element of an iterator */
    public void apply(Consumer<T> action) {
        apply(iterator, action) ;
    }

    /** Join on an {@code Iterator}..
     * If there are going to be many iterators, it is better to create an {@link IteratorConcat}
     * and <tt>.add</tt> each iterator.  The overheads are much lower.
     */
    public Iter<T> append(Iterator<T> iter) {
        return iter(IteratorCons.create(iterator, iter)) ;
    }

    /** Return an Iter that yields at most the first N items */
    public Iter<T> take(int N) {
        return iter(take(iterator, N)) ;
    }


    /** Create an {@code Iter} such that it yields elements while a predicate test on
     *  the elements is true, end the iteration.
     *  @see Iter#filter(Predicate)
     */
    public Iter<T> takeWhile(Predicate<T> predicate) {
        return iter(takeWhile(iterator, predicate)) ;
    }

    /**
     * Create an {@code Iter} such that it yields elements until a predicate test on
     * the elements becomes true, end the iteration.
     *
     * @see Iter#filter(Predicate)
     */
    public Iter<T> takeUntil(Predicate<T> predicate) {
        return iter(takeUntil(iterator, predicate)) ;
    }

    /** Create an {@code Iter} such that elements from the front while
     *  a predicate test become true are dropped then return all remaining elements
     *  are iterated over.
     *  The first element where the predicted becomes true is the first element of the
     *  returned iterator.
     */
    public Iter<T> dropWhile(Predicate<T> predicate) {
        return iter(dropWhile(iterator, predicate)) ;
    }

    /** Create an {@code Iter} such that elements from the front until
     *  a predicate test become true are dropped then return all remaining elements
     *  are iterated over.
     *  The first element where the predicate becomes true is the first element of the
     *  returned iterator.
     */
    public Iter<T> dropUntil(Predicate<T> predicate) {
        return iter(dropWhile(iterator, predicate.negate())) ;
    }

    /** Limit the number of elements. */
    public Iter<T> limit(long N) {
        return Iter.iter(limit(null, N));
    }

    /** Skip over a number of elements. */
    public Iter<T> skip(long N) {
        return Iter.iter(skip(null, N));
    }



    /** Count the iterator (this is destructive on the iterator) */
    public long count() {
        ActionCount<T> action = new ActionCount<>() ;
        apply(action) ;
        return action.getCount() ;
    }

    /**
     * Return an {@code Iter} that will see each element of the underlying iterator only once.
     * Note that this need working memory to remember the elements already seen.
     */
    public Iter<T> distinct() {
        return iter((distinct(iterator))) ;
    }

    /** Remove adjacent duplicates. This operation does not need
     * working memory to remember the all elements already seen,
     * just a slot for the last element seen.
     */
    public Iter<T> distinctAdjacent() {
        return iter(distinctAdjacent(iterator)) ;
    }

    // ---- Iterator

    @Override
    public boolean hasNext() {
        return iterator.hasNext() ;
    }

    @Override
    public T next() {
        return iterator.next() ;
    }

    @Override
    public void remove() {
        iterator.remove() ;
    }
}
