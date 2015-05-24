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

public class Iter<T> implements Iterator<T> {
    
    // Most Iterable<T> operations have been removed - use streams instad.
    
    public static <T> Stream<T> asStream(Iterator<T> iterator) {
        // Why isn't there a JDK operation for iterator -> (sequential) stream?
        return asStream(iterator, false);
    }

    public static <T> Stream<T> asStream(Iterator<T> iterator, boolean parallel) {
        // Why isn't there a JDK operation for iterator -> (sequential) stream?  
        Iterable<T> iterable = () -> iterator;
        return StreamSupport.stream(iterable.spliterator(), parallel);
    }

    // First part : the static function library.
    // Often with both Iterator<? extends T> and Iterable<? extends T>

    public static <T> Iterator<T> singleton(T item) {
        return new SingletonIterator<>(item) ;
    }

    @SuppressWarnings("rawtypes")
    private static final Iterator iter0 = new NullIterator() ;
    
    @SuppressWarnings({"unchecked"})
    public static <T> Iterator<T> nullIterator() { return iter0 ; }

//    public static <T> Iterator<T> nullIterator() {
//        return new NullIterator<T>() ;
//    }
    
    public static <T> Set<T> toSet(Iterator<? extends T> stream) {
        Accumulate<T, Set<T>> action = new Accumulate<T, Set<T>>() {
            private Set<T> acc = null ;

            @Override
            public void accumulate(T item) {
                acc.add(item) ;
            }

            @Override
            public Set<T> get() {
                return acc ;
            }

            @Override
            public void start() {
                acc = new HashSet<>() ;
            }

            @Override
            public void finish() {}
        } ;
        return reduce(stream, action) ;
    }

    public static <T> List<T> toList(Iterator<? extends T> stream) {
        Accumulate<T, List<T>> action = new Accumulate<T, List<T>>() {
            private List<T> acc = null ;

            @Override
            public void accumulate(T item) {
                acc.add(item) ;
            }

            @Override
            public List<T> get() {
                return acc ;
            }

            @Override
            public void start() {
                acc = new ArrayList<>() ;
            }

            @Override
            public void finish() {}
        } ;
        return reduce(stream, action) ;
    }

    /**
     * Create another iterator without risk of concurrent modification
     * exceptions. This materializes the input iterator.
     */
    public static <T> Iterator<T> iterator(Iterator<? extends T> iterator) {
        List<T> x = Iter.toList(iterator) ;
        return x.iterator() ;
    }

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

    public static <T, R> R reduce(Iterable<? extends T> stream, Accumulate<T, R> aggregator) {
        return reduce(stream.iterator(), aggregator) ;
    }

    public static <T, R> R reduce(Iterator<? extends T> stream, Accumulate<T, R> aggregator) {
        aggregator.start() ;
        for (; stream.hasNext();) {
            T item = stream.next() ;
            aggregator.accumulate(item) ;
        }
        aggregator.finish() ;
        return aggregator.get() ;
    }

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

    public static <T, R> Iterator<R> map(final Iterator<? extends T> stream, final Function<T, R> converter) {
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

    public static <T, R> List<R> map(List<? extends T> list, Function<T, R> converter) {
        return toList(map(list.iterator(), converter)) ;
    }

    /**
     * Apply an action to everything in stream, yielding a stream of the same
     * items
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

    /** Join two iterator
     * If there, potentially, going to be many iterators, it is better to 
     * create an {@link IteratorConcat} explicitly and add each iterator.
     */
    public static <T> Iterator<T> append(Iterator<? extends T> iter1, Iterator<? extends T> iter2) {
        return IteratorCons.create(iter1, iter2) ;
    }

    public static <T> Iterator<T> distinct(Iterator<T> iter) {
        return filter(iter, new FilterUnique<T>()) ;
    }

    /** Remove adjacent duplicates */
    public static <T> Iterator<T> distinctAdjacent(Iterator<T> iter) {
        return filter(iter, new FilterDistinctAdjacent<T>()) ;
    }

    public static <T> Iterator<T> removeNulls(Iterator<T> iter) {
        return filter(iter, Objects::nonNull) ;
    }

    /** Take the first N elements of an iterator - stop early if too few */
    public static <T> List<T> take(Iterator<T> iter, int N) {
        iter = new IteratorN<>(iter, N) ;
        List<T> x = new ArrayList<>(N) ;
        while ( iter.hasNext() )
            x.add(iter.next()) ;
        return x ;
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

    @SuppressWarnings("unchecked")
    public static <T> Iterator<T> convert(Iterator<? > iterator) {
        return (Iterator<T>)iterator ;
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
     * occurs when the returned iterator is used
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
     * Print an iterator to stdout, return a copy of the iterator. Printing
     * occurs when the returned iterator is used
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

    public static <T> Iter<T> iterSingleton(T x) {
        return iter(SingletonIterator.create(x)) ;
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
     * debugging
     */
    public static <T> Iterator<T> materialize(Iterator<T> iter) {
        return toList(iter).iterator() ;
    }

    public static <T> Iter<T> concat(Iter<T> iter1, Iter<T> iter2) {
        if ( iter1 == null )
            return iter2 ;
        if ( iter2 == null )
            return iter1 ;
        return iter1.append(iter2) ;
    }

    public static <T> Iterator<T> concat(Iterator<T> iter1, Iterator<T> iter2) {
        if ( iter1 == null )
            return iter2 ;
        if ( iter2 == null )
            return iter1 ;
        return iter(iter1).append(iter(iter2)) ;
    }

    public static <T> T first(Iterator<T> iter, Predicate<T> filter) {
        while (iter.hasNext()) {
            T t = iter.next() ;
            if ( filter.test(t) )
                return t ;
        }
        return null ;
    }

    public static <T> T first(Collection<T> collection, Predicate<T> filter) {
        return collection.stream().filter(filter).findFirst().orElse(null);
    }

    public static <T> int firstIndex(Iterator<T> iter, Predicate<T> filter) {
        for (int idx = 0; iter.hasNext(); idx++) {
            T t = iter.next() ;
            if ( filter.test(t) )
                return idx ;
        }
        return -1 ;
    }

    public static <T> int firstIndex(Collection<T> collection, Predicate<T> filter) {
        return firstIndex(collection.iterator(), filter) ;
    }

    public static <T> T last(Iterator<T> iter, Predicate<T> filter) {
        T thing = null ;
        while (iter.hasNext()) {
            T t = iter.next() ;
            if ( filter.test(t) )
                thing = t ;
        }
        return thing ;
    }

    public static <T> T last(Collection<T> collection, Predicate<T> filter) {
        return last(collection.iterator(), filter) ;
    }

    public static <T> int lastIndex(Iterator<T> iter, Predicate<T> filter) {
        int location = -1 ;
        for (int idx = 0; iter.hasNext(); idx++) {
            T t = iter.next() ;
            if ( filter.test(t) )
                location = idx ;
        }
        return location ;
    }

    public static <T> int lastIndex(Collection<T> collection, Predicate<T> filter) {
        return lastIndex(collection.iterator(), filter) ;
    }

    // ------------------------------------------------------
    // The class.

    private Iterator<T> iterator ;

    private Iter(Iterator<T> iterator) {
        this.iterator = iterator ;
    }

    public Set<T> toSet() {
        return toSet(iterator) ;
    }

    public List<T> toList() {
        return toList(iterator) ;
    }

    public void sendToSink(Sink<T> sink) {
        sendToSink(iterator, sink) ;
    }

    public T first(Predicate<T> filter) {
        return first(iterator, filter) ;
    }

    public int firstIndex(Predicate<T> filter) {
        return firstIndex(iterator, filter) ;
    }

    public T last(Predicate<T> filter) {
        return last(iterator, filter) ;
    }

    public int lastIndex(Predicate<T> filter) {
        return lastIndex(iterator, filter) ;
    }

    public Iter<T> filter(Predicate<T> filter) {
        return iter(filter(iterator, filter)) ;
    }

    public boolean every(Predicate<T> filter) {
        return every(iterator, filter) ;
    }

    public boolean some(Predicate<T> filter) {
        return some(iterator, filter) ;
    }

    public Iter<T> removeNulls() {
        return iter(removeNulls(this)) ;
    }

    public <R> Iter<R> map(Function<T, R> converter) {
        return iter(map(iterator, converter)) ;
    }

    /**
     * Apply an action to everything in the stream, yielding a stream of the
     * same items
     */
    public Iter<T> operate(Consumer<T> action) {
        return iter(operate(iterator, action)) ;
    }

    public <R> R reduce(Accumulate<T, R> aggregator) {
        return reduce(iterator, aggregator) ;
    }

    public void apply(Consumer<T> action) {
        apply(iterator, action) ;
    }

    /** Join on an iterator.
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

    public Iter<T> distinct() {
        return iter((distinct(iterator))) ;
    }

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
