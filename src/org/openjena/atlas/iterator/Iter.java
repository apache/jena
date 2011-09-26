/**
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

package org.openjena.atlas.iterator;


import java.io.PrintStream ;
import java.util.* ;

import org.openjena.atlas.lib.ActionKeyValue ;
import org.openjena.atlas.lib.Closeable ;
import org.openjena.atlas.lib.Sink ;



public class Iter<T> implements Iterable<T>, Iterator<T>
{
    // First part : the static function library.
    // Often with both Iterator<? extends T> and Iterable<? extends T>
    
    public static <T> Iterator<T> singleton(T item)
    { return new SingletonIterator<T>(item) ; }

    public static <T> Iterator<T> nullIterator()
    { return new NullIterator<T>() ; }
    
    public static <T> Set<T> toSet(Iterable<? extends T> stream) { return toSet(stream.iterator()); }

    public static <T> Set<T> toSet(Iterator<? extends T> stream)
    {
        Accumulate<T,Set<T>> action = new Accumulate<T,Set<T>>()
        {
            private Set<T> acc = null ;
            public void accumulate(T item)  { acc.add(item) ; }
            public Set<T> get()             { return acc ; }
            public void start()             { acc = new HashSet<T>() ; }
            public void finish()            {}
        } ;
        return reduce(stream, action) ;
    }

    public static <T> List<T> toList(Iterable<? extends T> stream)
    { return toList(stream.iterator()) ; }

    public static <T> List<T> toList(Iterator<? extends T> stream)
    {
        Accumulate<T,List<T>> action = new Accumulate<T,List<T>>()
        {
            private List<T> acc = null ;
            public void accumulate(T item)  { acc.add(item) ; }
            public List<T> get()            { return acc ; }
            public void start()             { acc = new ArrayList<T>() ; }
            public void finish()            {}
        } ;
        return reduce(stream, action) ;
    }

    public interface Folder<X,Y> { Y eval(Y acc, X arg) ; }
    
    public static <T, R> R foldLeft(Iterable<? extends T> stream, Folder<T, R> function, R value)
    { return foldLeft(stream.iterator(), function, value) ; }
    
    public static <T, R> R foldLeft(Iterator<? extends T> stream, Folder<T, R> function, R value)
    {
        // Tail recursion, unwound
        for ( ; stream.hasNext() ; )
        {
            T item = stream.next(); 
            value = function.eval(value, item) ;
        }
        return value ;
    }
    
    public static <T, R> R foldRight(Iterable<? extends T> stream, Folder<T, R> function, R value)
    { return foldRight(stream.iterator(), function, value) ; }

    public static <T, R> R foldRight(Iterator<? extends T> stream, Folder<T, R> function, R value)
    {
        // Recursive.
        if ( ! stream.hasNext() ) return value ;
        T item = stream.next() ;
        return function.eval( foldRight(stream, function, value) , item ) ;
    }
    
    // Note fold-left and fold-right
    // http://en.wikipedia.org/wiki/Fold_%28higher-order_function%29
    
    // This reduce is fold-left (take first element, apply to rest of list)
    // which copes with infinite lists.
    // Fold-left starts by combining the first element, then moves on.
    
    public static <T, R> R reduce(Iterable<? extends T> stream, Accumulate<T, R> aggregator)
    { return reduce(stream.iterator(), aggregator) ; }

    public static <T, R> R reduce(Iterator<? extends T> stream, Accumulate<T, R> aggregator)
    {
        aggregator.start();
        for ( ; stream.hasNext() ; )
        {
            T item = stream.next(); 
            aggregator.accumulate(item) ;
        }
        aggregator.finish();
        return aggregator.get();
    }

    // map without the results - do immediately.
    // Also, apply with call in between?
    
    public static <T> void apply(Iterable<? extends T> stream, Action<T> action)
    { apply(stream.iterator(), action) ; }

    public static <T> void apply(Iterator<? extends T> stream, Action<T> action)
    {
        for ( ; stream.hasNext() ; )
        {
            T item = stream.next(); 
            action.apply(item) ;
        }
    }
    
    // -- Map specific apply.  No results - do immediately.
    
    public static <K, V> void apply(Map<K, V> map, ActionKeyValue<K, V> action)
    {
        for ( Map.Entry<K,V> entry : map.entrySet() )
            action.apply(entry.getKey(), entry.getValue()) ;
    }
    
    // ---- Filter
    
    public static <T> Iterator<T> filter(Iterable<? extends T> stream, Filter<T> filter)
    { return filter(stream.iterator(), filter) ; }

    public static <T> Iterator<T> filter(final Iterator<? extends T> stream, final Filter<T> filter)
    {
        final Iterator<T> iter = new Iterator<T>(){
            
            boolean finished = false ; 
            boolean slotOccupied = false ;
            T slot ;
            
            public boolean hasNext()
            {
                if ( finished )
                    return false ; 
                while ( ! slotOccupied )
                {
                    if ( ! stream.hasNext() )
                    { 
                        finished = true ;
                        break ;
                    }
                    T nextItem = stream.next() ;
                    if ( filter.accept(nextItem) )
                    { 
                        slot = nextItem ;
                        slotOccupied = true;
                        break ;
                    }
                }
                return slotOccupied ;
            }
    
            public T next()
            {
                if ( hasNext() )
                {
                    slotOccupied = false ;
                    return slot ;
                }
                throw new NoSuchElementException("filter.next") ;
            }
    
            public void remove() { throw new UnsupportedOperationException("filter.remove") ; }
        } ;
        
        return iter ;
    }
    
    
    private static class InvertedFilter<T> implements Filter<T>
    {
        public static <T> Filter<T> invert(Filter<T> filter) { return new InvertedFilter<T>(filter) ; }
        private Filter<T> baseFilter ;
        private InvertedFilter(Filter<T> baseFilter) { this.baseFilter = baseFilter ; }
        
        public boolean accept(T item)
        {
            return ! baseFilter.accept(item) ;
        }
    }
    
    public static <T> Iterator<T> notFilter(Iterable<? extends T> stream,
                                         Filter<T> filter)
    { return notFilter(stream.iterator(), filter) ; }
    
    public static <T> Iterator<T> notFilter(final Iterator<? extends T> stream, final Filter<T> filter)
    {
        Filter<T> flippedFilter = InvertedFilter.invert(filter) ;
        return filter(stream, flippedFilter) ;
    }
    
    // Filter-related
    
    /** Return true if every element of stream passes the filter (reads the stream) */
    public static <T> boolean every(Iterable<? extends T> stream, Filter<T> filter)
    { 
        for ( T item : stream )
            if ( ! filter.accept(item) ) 
                return false ;
        return true ;
    }

    /** Return true if every element of stream passes the filter (reads the stream until the first element not passing the filter) */
    public static <T> boolean every(Iterator<? extends T> stream, Filter<T> filter)
    { 
        for ( ; stream.hasNext() ; )
        {
            T item = stream.next();
            if ( ! filter.accept(item) ) 
                return false ;
        }
        return true ;
    }

    /** Return true if every element of stream passes the filter (reads the stream until the first element passing the filter) */
    public static <T> boolean some(Iterable<? extends T> stream, Filter<T> filter)
    { 
        for ( T item : stream )
            if ( filter.accept(item) ) 
                return true ;
        return false ;
    }

    /** Return true if one or more elements of stream passes the filter (reads the stream to first element passing the filter) */
    public static <T> boolean some(Iterator<? extends T> stream, Filter<T> filter)
    { 
        for ( ; stream.hasNext() ; )
        {
            T item = stream.next();
            if ( filter.accept(item) ) 
                return true ;
        }
        return false ;
    }

    
    // ---- Map

    public static <T, R> Iterator<R> map(Iterable<? extends T> stream, Transform<T, R> converter)
    { return map(stream.iterator(), converter) ; }

    public static <T, R> Iterator<R> map(final Iterator<? extends T> stream, final Transform<T, R> converter)
    {
        final Iterator<R> iter = new Iterator<R>(){
            public boolean hasNext()
            {
                return stream.hasNext() ;
            }
    
            public R next()
            {
                return converter.convert(stream.next()) ;
            }
    
            public void remove() { throw new UnsupportedOperationException("map.remove") ; }
        } ;
        return iter ;
    }
    
    public static <T, R> List<R> map(List<? extends T> list, Transform<T, R> converter)
    {
        return toList(map(list.iterator(), converter)) ;
    }
    
    
    /**
     * Projects each element of a sequence to an Iterator&lt;R&gt; and flattens the resulting sequences into one sequence.
     */
    public static <T, R> Iterator<R> mapMany(final Iterator<? extends T> stream, final Transform<? super T, Iterator<R>> converter)
    {
        final Iterator<R> iter = new Iterator<R>(){

            private Iterator<? extends R> it = null;    // Iterator for the current element of stream.
            
            public boolean hasNext()
            {
                if ( it != null && it.hasNext() )
                    // Element of the current iterator. 
                    return true ;
                // Start or current iterator has ended.
                it = null ;
                
                // Need to move to next non-empty iterator of the stream. 
                while ( stream.hasNext() )
                {
                    it = converter.convert(stream.next());
                    if ( it.hasNext() )
                        // There is something.
                        return true ;
                }
                it = null ;
                // Stream ran out.
                return false ;
            }
    
            public R next()
            {
                if ( ! hasNext() )
                    throw new NoSuchElementException() ;
                // "it" is never left at the end. 
                return it.next();
            }
    
            public void remove() { throw new UnsupportedOperationException("mapMany.remove") ; }
        } ;
        
        return iter;
    }
    
    public static <T, R> Iterator<R> mapMany(Iterable<? extends T> stream, Transform<T, Iterator<R>> converter)
    {
        return mapMany(stream.iterator(), converter) ;
    }
    
    public static <T, R> List<R> mapMany(List<? extends T> list, Transform<T, Iterator<R>> converter)
    {
        return toList(mapMany(list.iterator(), converter)) ;
    }
    
    
    /** Apply an action to everything in stream, yielding a stream of the same items */ 
    public static <T> Iterator<T> operate(Iterable<? extends T> stream, Action<T> converter)
    { return operate(stream.iterator(), converter) ; }

    /** Apply an action to everything in stream, yielding a stream of the same items */ 
    public static <T> Iterator<T> operate(final Iterator<? extends T> stream, 
                                          final Action<T> action)
    {
        final Iterator<T> iter = new Iterator<T>(){
            public boolean hasNext()
            {
                return stream.hasNext() ;
            }
    
            public T next()
            {
                T t = stream.next() ;
                action.apply(t) ;
                return t ;
            }
    
            public void remove() { throw new UnsupportedOperationException("operate.remove") ; }
        } ;
        return iter ;
    }

    /** Print an iterator as it gets used - this adds a printing wrapper */
    public static <T> Iterator<T> printWrapper(final Iterator<? extends T> stream)
    {
        return Iter.printWrapper(System.out, stream) ;
    }

    /** Print an iterator as it gets used - this adds a printing wrapper */
    public static <T> Iterator<T> printWrapper(final PrintStream out, final Iterator<? extends T> stream)
    {
        Action<T> action = new Action<T>(){
            public void apply(T item)
            { out.println(item) ; }
        } ;
        return Iter.operate(stream, action) ;
    }
    
    public static <T> Iterator<T> append(Iterable<T> iter1, Iterable<T> iter2)
    {
        return IteratorCons.create(iterator(iter1), iterator(iter2));
    }

    // Could try for <? extends T> on each arg.
    public static <T> Iterator<T> append(Iterator<? extends T> iter1, Iterator<? extends T> iter2)
    { return IteratorCons.create(iter1, iter2); }

    private static <T> Iterator<T> iterator(Iterable<T> iter) { return (iter==null) ? null : iter.iterator() ; }
    
    public static <T> Iterator<T> distinct(Iterable<T> iter)
    {
        return distinct(iter.iterator()) ;
    }

    public static <T> Iterator<T> distinct(Iterator<T> iter)
    {
        return filter(iter, new FilterUnique<T>()) ;
    }
    
    public static <T> Iterator<T> removeNulls(Iterable<T> iter)
    {
        return filter(iter, new FilterOutNulls<T>()) ;
    }
    
    public static <T> Iterator<T> removeNulls(Iterator<T> iter)
    {
        return filter(iter, new FilterOutNulls<T>()) ;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Iterator<T> convert(Iterator<?> iterator) { return (Iterator<T>)iterator ; }
    
    /** Count the iterable - many iterable objects have a .size() operation which should be used in preference to this explicit counting operation  */ 
    public static <T> long count(Iterable<T> iterator)
    {
        ActionCount<T> action = new ActionCount<T>() ;
        Iter.apply(iterator, action) ;
        return action.getCount() ;
    }

    /** Count the iterator (this is destructive on the iterator) */ 
    public static <T> long count(Iterator<T> iterator)
    {
        ActionCount<T> action = new ActionCount<T>() ;
        Iter.apply(iterator, action) ;
        return action.getCount() ;
    }

    // ---- String related helpers
    
    public static <T> String asString(Iterable<T> stream)
    { return asString(stream, new AccString<T>()) ; }

    public static <T> String asString(Iterator<T> stream)
    { return asString(stream, new AccString<T>()) ; }

    public static <T> String asString(Iter<T> stream)
    { return asString(stream, new AccString<T>()) ; }

    public static <T> String asString(Iterable<T> stream, String sep)
    { return asString(stream, new AccString<T>(sep)) ; }

    public static <T> String asString(Iterator<T> stream, String sep)
    { return asString(stream, new AccString<T>(sep)) ; }

    public static <T> String asString(Iter<T> stream, String sep)
    { return asString(stream.iterator(), new AccString<T>(sep)) ; }

    public static <T> String asString(Iterable<T> stream, AccString<T> formatter)
    { return reduce(stream, formatter) ; }

    public static <T> String asString(Iterator<T> stream, AccString<T> formatter)
    { return reduce(stream, formatter) ; }

    public static <T> String asString(Iter<T> stream, AccString<T> formatter)
    { return reduce(stream.iterator(), formatter) ; }

    // ----
    
    public static <T> void close(Iterator<T> iter)
    {
        if ( iter instanceof Closeable )
            ((Closeable)iter).close() ;
    }

    public static <T> Iterator<T> debug(Iterator<T> stream)
    { 
        Transform<T,T> x = new Transform<T, T>()
        {
            //@Override
            public T convert(T item)
            { 
                System.out.println(item) ;
                return item ;
            }
        } ;
        return map(stream, x) ;
    }
    
    /** Send the elements of the iterator to a sink - consumes the iterator */ 
    public static <T> void sendToSink(Iterator<T> iter, Sink<T> sink)
    {
        for ( ; iter.hasNext() ; )
        {
            T thing = iter.next() ;
            sink.send(thing) ;
        }
        sink.close();
    }
    
    /** Send the elements of the iterator to a sink - consumes the iterator */ 
    public static <T> void sendToSink(Iterable<T> stream, Sink<T> sink)
    { 
        sendToSink(stream.iterator(), sink) ;
    }

    //----
    // Iter class part
    // And ....
    // Could merge in concatenated iterators - if used a lot there is reducable cost.
    // Just putting in a slot is free (?) because objects of one or two slots have
    // the same memory allocation.
    // And .. be an iterator framework for extension
    
    // Or dynamically with a subclass and a static constructor
    // List<Iterator> concatenated = null ; 
    
    public static <T> Iter<T> iter(Iter<T> iter)
    { return iter ; }

    // May not do what you expect. iter(int[]) is iter of one object (an int[])
//    public static <T> Iter<T> iter(T...objects)
//    { return Iter.iter(Arrays.asList(objects)) ; }
    
    public static <T> Iter<T> iter(Collection<T> collection)
    {
        return Iter.iter(collection.iterator()) ;
    }
    
    public static <T> Iter<T> iter(Iterator<T> iterator)
    { 
        if ( iterator instanceof Iter<?> )
            return (Iter<T>)iterator ;
        return new Iter<T>(iterator) ;
    }
    
    public static <T> Iter<T> iter(Iterable<T> iterable)
    { 
        if ( iterable instanceof Iter<?> )
            return (Iter<T>)iterable ;
        return new Iter<T>(iterable.iterator()) ;
    }
    
    /** Materializae an iterator, that is, force it to run now - useful in debugging */ 
    public static <T> Iterator<T> materialize(Iterator<T> iter)
    {
        return Iter.toList(iter).iterator() ;
    }

    public static <T> Iter<T> concat(Iter<T> iter1, Iter<T>iter2)
    { 
        if ( iter1 == null )
            return iter2 ;
        if ( iter2 == null )
            return iter1 ;
        return iter1.append(iter2) ;
    }

    public static <T> Iterator<T> concat(Iterator<T> iter1, Iterator<T>iter2)
    { 
        if ( iter1 == null )
            return iter2 ;
        if ( iter2 == null )
            return iter1 ;
        return Iter.iter(iter1).append(Iter.iter(iter2)) ;
    }
    
    public static <T> T first(Iterator<T> iter, Filter<T> filter)
    {
        for ( int idx = 0 ; iter.hasNext() ; idx++ )
        {
            T t = iter.next();
            if ( filter.accept(t))
                return t ;
                //return idx ;
        }
        return null ;
    }

    public static <T> T first(Collection<T> collection, Filter<T> filter)
    { return first(collection.iterator(), filter) ; }

    public static <T> int firstIndex(Iterator<T> iter, Filter<T> filter)
    {
        for ( int idx = 0 ; iter.hasNext() ; idx++ )
        {
            T t = iter.next();
            if ( filter.accept(t))
                return idx ;
        }
        return -1 ;
    }

    public static <T> int firstIndex(Collection<T> collection, Filter<T> filter)
    { return firstIndex(collection.iterator(), filter) ; }

    public static <T> T last(Iterator<T> iter, Filter<T> filter)
    {
        T thing = null ;
        for ( int idx = 0 ; iter.hasNext() ; idx++ )
        {
            T t = iter.next();
            if ( filter.accept(t))
                thing = t ;
        }
        return thing ;
    }

    public static <T> T last(Collection<T> collection, Filter<T> filter)
    { return last(collection.iterator(), filter) ; }

    public static <T> int lastIndex(Iterator<T> iter, Filter<T> filter)
    {
        int location = -1 ;
        for ( int idx = 0 ; iter.hasNext() ; idx++ )
        {
            T t = iter.next();
            if ( filter.accept(t))
                location = idx  ;
        }
        return location ;
    }

    public static <T> int lastIndex(Collection<T> collection, Filter<T> filter)
    { return lastIndex(collection.iterator(), filter) ; }

    // ------------------------------------------------------
    // The class.
    
    private Iterator<T> iterator ;
    private Iter(Iterator<T> iterator) { this.iterator = iterator ; }
    
    public Set<T> toSet()
    {
        return toSet(iterator) ;
    }

    public List<T> toList()
    {
        return toList(iterator) ;
    }
    
    public void sendToSink(Sink<T> sink)
    {
        sendToSink(iterator, sink) ;
    }
    
    public T first(Filter<T> filter)
    {
        return first(iterator, filter) ;
    }

    public int firstIndex(Filter<T> filter)
    {
        return firstIndex(iterator, filter) ;
    }

    public T last(Filter<T> filter)
    {
        return last(iterator, filter) ;
    }

    public int lastIndex(Filter<T> filter)
    {
        return lastIndex(iterator, filter) ;
    }

    public Iter<T> filter(Filter<T> filter)
    {
        return iter(filter(iterator, filter)) ;
    }

    public boolean every(Filter<T> filter)
    {
        return every(iterator, filter) ;
    }
    
    public boolean some(Filter<T> filter)
    {
        return some(iterator, filter) ;
    }
    
    public Iter<T> removeNulls()
    {
        return filter(new FilterOutNulls<T>()) ;
    }

    public <R> Iter<R> map(Transform<T, R> converter)
    {
        return iter(map(iterator, converter)) ;
    }
    
    /** Apply an action to everything in the stream, yielding a stream of the same items */ 
    public Iter<T> operate(Action<T> action)
    {
        return iter(operate(iterator, action)) ;
    }

    public <R> R reduce(Accumulate<T, R> aggregator)
    {
        return reduce(iterator, aggregator) ;
    }

    public void apply(Action<T> action)
    {
        apply(iterator, action) ;
    }

    public Iter<T> append(Iterator<T> iter)
    {
        return new Iter<T>(IteratorCons.create(iterator, iter)) ;
    }

    /** Count the iterator (this is destructive on the iterator) */ 
    public long count()
    {
        ActionCount<T> action = new ActionCount<T>() ;
        apply(action) ;
        return action.getCount() ;
    }
    
    public String asString() { return asString(iterator) ; }
    public String asString(String sep) { return asString(iterator, sep) ; }
    
    public Iter<T> distinct()
    {
        return new Iter<T>(distinct(iterator())) ;
    }

    // ---- Iterable
    public Iterator<T>  iterator() { return iterator ; }
    
    // ---- Iterator
    
    //----
    // Could merge in concatenated iterators - if used a lot there is reducable cost.
    // Just putting in a slot is free (?) because objects of one or two slots have
    // the same memory allocation.
    // And .. be an iterator framework for extension
    
    public boolean hasNext()    { return iterator.hasNext() ; }

    public T next()             { return iterator.next() ; }

    public void remove()        { iterator.remove() ; }

    //----
    // Iter class part
    // And ....
    // Could merge in concatenated iterators - if used a lot there is reducable cost.
    // Just putting in a slot is free (?) because objects of one or two slots have
    // the same memory allocation.
    // And .. be an iterator framework for extension
    
    // Or dynamically with a subclass and a static constructor
    // List<Iterator> concatenated = null ; 
    
    public static <T> Iter<T> singletonIter(T item)
    { return new Iter<T>(new SingletonIterator<T>(item)) ; }

    public static <T> Iter<T> nullIter()
    { return new Iter<T>(new NullIterator<T>()) ; }
}
