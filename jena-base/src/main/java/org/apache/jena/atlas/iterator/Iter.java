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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
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
    // IteratorSlotted needed? IteratorPeek
    //   IteratorSlotted.inspect
    
    public static <T> Stream<T> asStream(Iterator<T> iterator) {
        return asStream(iterator, false);
    }

    public static <T> Stream<T> asStream(Iterator<T> iterator, boolean parallel) {
        // Why isn't there a JDK operation for Iterator -> (sequential) stream?  
        int characteristics = 0 ; //Spliterator.IMMUTABLE;
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, characteristics), parallel);
    }

    // ---- Special iterators. 
    
    public static <T> Iterator<T> singleton(T item) {
        // There is a singleton iterator in Collections but it is not public.
        return new SingletonIterator<>(item) ;
    }
    
    public static <T> Iterator<T> nullIterator() {
        // Java7 caught up.
        return Collections.emptyIterator();
    }
    
    // ---- Collectors.
    
    /** Collect an iterator into a set. */
    public static <T> Set<T> toSet(Iterator<? extends T> stream) {
        Set<T> acc = new HashSet<>() ;
        collect(acc, stream) ;
        return acc ;
    }
    
    /** Collect an iterator into a list. */
    public static <T> List<T> toList(Iterator<? extends T> stream) {
        List<T> acc = new ArrayList<>() ;
        collect(acc, stream) ;
        return acc ;
    }

    /** Collect an iterator. */
    private static <T> void collect(Collection<T> acc, Iterator<? extends T> stream) {
        stream.forEachRemaining((x)->acc.add(x)) ;
    }

    /**
     * Create another iterator without risk of concurrent modification
     * exceptions. This materializes the input iterator.
     */
    public static <T> Iterator<T> iterator(Iterator<? extends T> iterator) {
        List<T> x = Iter.toList(iterator) ;
        return x.iterator() ;
    }
    
    // -- Operations on iterators.

    public interface Folder<X, Y> {
        Y eval(Y acc, X arg) ;
    }

    public static <T, R> R foldLeft(Iterable<? extends T> stream, Folder<T, R> function, R value) {
        return foldLeft(stream.iterator(), function, value) ;
    }

    public static <T, R> R foldLeft(Iterator<? extends T> stream, Folder<T, R> function, R value) {
        // Tail recursion, unwound
        for (; stream.hasNext();) {
            T item = stream.next() ;
            value = function.eval(value, item) ;
        }
        return value ;
    }

    public static <T, R> R foldRight(Iterable<? extends T> stream, Folder<T, R> function, R value) {
        return foldRight(stream.iterator(), function, value) ;
    }

    public static <T, R> R foldRight(Iterator<? extends T> stream, Folder<T, R> function, R value) {
        // Recursive.
        if ( !stream.hasNext() )
            return value ;
        T item = stream.next() ;
        return function.eval(foldRight(stream, function, value), item) ;
    }

    // Note fold-left and fold-right
    // http://en.wikipedia.org/wiki/Fold_%28higher-order_function%29

    // This reduce is fold-left (take first element, apply to rest of list)
    // which copes with infinite lists.
    // Fold-left starts by combining the first element, then moves on.

    /** Reduce by aggregator.
     * This reduce is fold-left (take first element, apply to rest of list)
     */
    public static <T, R> R reduce(Iterable<? extends T> stream, Accumulate<T, R> aggregator) {
        return reduce(stream.iterator(), aggregator) ;
    }
    /** Reduce by aggregator.
     * This reduce is fold-left (take first element, apply to rest of list)
     */
    public static <T, R> R reduce(Iterator<? extends T> stream, Accumulate<T, R> aggregator) {
        aggregator.start() ;
        for (; stream.hasNext();) {
            T item = stream.next() ;
            aggregator.accumulate(item) ;
        }
        aggregator.finish() ;
        return aggregator.get() ;
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

            @Override
            public void remove() {
                throw new UnsupportedOperationException("filter.remove") ;
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
    public static <T> boolean every(Iterator<? extends T> stream, Predicate<T> filter) {
        while ( stream.hasNext() ) {
            T item = stream.next() ;
            if ( !filter.test(item) )
                return false ;
        }
        return true ;
    }

    /**
     * Return true if one or more elements of stream passes the filter (reads
     * the stream to first element passing the filter)
     */
    public static <T> boolean some(Iterator<? extends T> stream, Predicate<T> filter) {
        while ( stream.hasNext() ) {
            T item = stream.next() ;
            if ( filter.test(item) )
                return true ;
        }
        return false ;
    }

    // ---- Map

    /** Apply a function to every element of an iterator, transforming it
     * from a {@code T} to an {@code R}. 
     */
    public static <T, R> Iterator<R> map(Iterator<? extends T> stream, Function<T, R> converter) {
        final Iterator<R> iter = new Iterator<R>() {
            @Override
            public boolean hasNext() {
                return stream.hasNext() ;
            }

            @Override
            public R next() {
                return converter.apply(stream.next()) ;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("map.remove") ;
            }
        } ;
        return iter ;
    }

    /** Transform a list of elements to a new list of the function applied to each element.
     * Using a stream is often better.  This operation preserves the order of the list.
     * @deprecated Use Java8 Streams
     */
    @Deprecated
    public static <T, R> List<R> map(List<? extends T> list, Function<T, R> converter) {
        return toList(map(list.iterator(), converter)) ;
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

            @Override
            public void remove() {
                throw new UnsupportedOperationException("operate.remove") ;
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

    /** Take the first N elements of an iterator - stop early if too few */
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

    // ---- String related helpers
    // Java8 has StringJoin

    public static <T> String asString(Iterable<T> stream) {
        return asString(stream, new AccString<T>()) ;
    }

    public static <T> String asString(Iterator<T> stream) {
        return asString(stream, new AccString<T>()) ;
    }

    public static <T> String asString(Iterable<T> stream, String sep) {
        return asString(stream, new AccString<T>(sep)) ;
    }

    public static <T> String asString(Iterator<T> stream, String sep) {
        return asString(stream, new AccString<T>(sep)) ;
    }

    public static <T> String asString(Iterable<T> stream, AccString<T> formatter) {
        return asString(stream.iterator(), formatter) ;
    }

    public static <T> String asString(Iterator<T> stream, AccString<T> formatter) {
        return reduce(stream, formatter) ;
    }

    // ----

    public static <T> void close(Iterator<T> iter) {
        if ( iter instanceof Closeable )
            ((Closeable)iter).close() ;
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
     * Print an iterator,  return a copy of the iterator. Printing
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
    public static <T> void print(final PrintStream out, Iterator<T> stream) {
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

    /** Send the elements of the iterable to a sink */
    public static <T> void sendToSink(Iterable<T> stream, Sink<T> sink) {
        sendToSink(stream.iterator(), sink) ;
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

    /** @deprecated Use Java8 Streams */
    @Deprecated
    public static <T> T first(Collection<T> collection, Predicate<T> filter) {
        return collection.stream().filter(filter).findFirst().orElse(null);
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

    /** @deprecated Use Java8 Streams */
    @Deprecated
    public static <T> int firstIndex(Collection<T> collection, Predicate<T> filter) {
        return firstIndex(collection.iterator(), filter) ;
    }

    /** Return the last element or null, if no elements. This operation consumes the iterator. */
    public static <T> T last(Iterator<T> iter) {
        return last(iter, (x)->true) ;
    }

    /** Return the last element satisfying a predicate. This operation consumes the whole iterator. */
    public static <T> T last(Iterator<T> iter, Predicate<T> filter) {
        T thing = null ;
        while (iter.hasNext()) {
            T t = iter.next() ;
            if ( filter.test(t) )
                thing = t ;
        }
        return thing ;
    }

    /** @deprecated Use Java8 Streams */
    @Deprecated
    public static <T> T last(Collection<T> collection, Predicate<T> filter) {
        return last(collection.iterator(), filter) ;
    }

    /** Return the index of the last element satisfying a predicate (zero-based). */
    public static <T> int lastIndex(Iterator<T> iter, Predicate<T> filter) {
        int location = -1 ;
        for (int idx = 0; iter.hasNext(); idx++) {
            T t = iter.next() ;
            if ( filter.test(t) )
                location = idx ;
        }
        return location ;
    }

    /** @deprecated Use Java8 Streams */
    @Deprecated
    public static <T> int lastIndex(Collection<T> collection, Predicate<T> filter) {
        return lastIndex(collection.iterator(), filter) ;
    }

    // ------------------------------------------------------
    // The class.

    private Iterator<T> iterator ;

    private Iter(Iterator<T> iterator) {
        this.iterator = iterator ;
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

    /** Return the last element satisfying a predicate. This operation destroys the whole iterator. */
    public T last(Predicate<T> filter) {
        return last(iterator, filter) ;
    }

    /** Return the index of the last element satisfying a predicate (zero-based). */
    public int lastIndex(Predicate<T> filter) {
        return lastIndex(iterator, filter) ;
    }

    /** Filter by predicate */  
    public Iter<T> filter(Predicate<T> filter) {
        return iter(filter(iterator, filter)) ;
    }

    /** Return true if every element satisfies a predicate */ 
    public boolean every(Predicate<T> predciate) {
        return every(iterator, predciate) ;
    }

    /** Return true if some element satisfies a predicate */ 
    public boolean some(Predicate<T> filter) {
        return some(iterator, filter) ;
    }

    /** Remove nulls */
    public Iter<T> removeNulls() {
        return iter(removeNulls(this)) ;
    }

    /** Map each element using given function */
    public <R> Iter<R> map(Function<T, R> converter) {
        return iter(map(iterator, converter)) ;
    }

    /**
     * Apply an action to everything in the stream, yielding a stream of the
     * original items.
     */
    public Iter<T> operate(Consumer<T> action) {
        return iter(operate(iterator, action)) ;
    }

    /** Reduce by aggregator.
     * This reduce is fold-left (take first element, apply to rest of list)
     */
    public <R> R reduce(Accumulate<T, R> aggregator) {
        return reduce(iterator, aggregator) ;
    }

    /** Apply an action to every element of an iterator */ 
    public void apply(Consumer<T> action) {
        apply(iterator, action) ;
    }

    /** Join on an {@code Iterator}..
     * If there are going to be many iterators, uit is better to create an {@link IteratorConcat}
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
    
    /** Count the iterator (this is destructive on the iterator) */
    public long count() {
        ActionCount<T> action = new ActionCount<>() ;
        apply(action) ;
        return action.getCount() ;
    }

    public String asString() {
        return asString(iterator) ;
    }

    public String asString(String sep) {
        return asString(iterator, sep) ;
    }

    /** Return an {:@code Iter} that will see each element of the underlying iterator only once.
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
