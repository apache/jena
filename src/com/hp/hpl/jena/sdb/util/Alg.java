/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.util;

import java.util.*;

import com.hp.hpl.jena.sdb.util.alg.AccString;
import com.hp.hpl.jena.sdb.util.alg.Accumulate;
import com.hp.hpl.jena.sdb.util.alg.Action;
import com.hp.hpl.jena.sdb.util.alg.Filter;
import com.hp.hpl.jena.sdb.util.alg.PrintAction;
import com.hp.hpl.jena.sdb.util.alg.Transform;
import com.hp.hpl.jena.sparql.util.IndentedLineBuffer;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.Printable;

public class Alg
{
    // ---- Common operations 
    
    public static <T extends Printable> String printString(Iterable<? extends T> struct)
    {
        return printString(struct, " ") ;
    }
    
    public static <T extends Printable> String printString(Iterable<? extends T> struct, String sep)
    {
        IndentedLineBuffer b = new IndentedLineBuffer() ;
        apply(struct, new PrintAction<T>(b.getIndentedWriter(), sep)) ;
        return b.asString() ; 
    }
    
    public static <T extends Printable> void print(IndentedWriter out, Iterable<? extends T> struct)
    {
        apply(struct, new PrintAction<T>(out)) ;
    }

    public static <T extends Printable> void print(IndentedWriter out, Iterable<? extends T> struct, String sep)
    {
        apply(struct, new PrintAction<T>(out, sep)) ;
    }

    public static <T> String asString(Iterable<T> stream)
    { return reduce(stream, new AccString<T>()) ; }

    public static <T> String asString(Iterator<T> stream)
    { return reduce(stream, new AccString<T>()) ; }

    public static <T> String asString(Iterable<T> stream, String sep)
    { return reduce(stream, new AccString<T>(sep)) ; }

    public static <T> String asString(Iterator<T> stream, String sep)
    { return reduce(stream, new AccString<T>(sep)) ; }

    // ---- Core operations
    // TODO stream merge (2 parallel streams , merge function , nulls for no entry).
    
    public static <T> Set<T> toSet(Iterable<T> stream) { return toSet(stream.iterator()); }

    public static <T> Set<T> toSet(Iterator<T> stream)
    {
        Accumulate<T,Set<T>> action = new Accumulate<T,Set<T>>()
        {
            private Set<T> acc = null ;
            public void accumulate(T item)   { acc.add(item) ; }
            public Set<T> get()             { return acc ; }
            public void start()             { acc = new HashSet<T>() ; }
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
            public void accumulate(T item)   { acc.add(item) ; }
            public List<T> get()             { return acc ; }
            public void start()             { acc = new ArrayList<T>() ; }
        } ;
        return reduce(stream, action) ;
    }

    // Note fold-left and fold-right
    // http://en.wikipedia.org/wiki/Fold_%28higher-order_function%29
    // This reduce is fold-right (take first element, apply to rest of list)
    // which copes with infinite lists.
    // Fold-left starts combinign from the list tail, is tail recursive (= iterative)
    
    public static <T, R> R reduce(Iterable<? extends T> stream,
                                      Accumulate<T, R> aggregator)
    { return reduce(stream.iterator(), aggregator) ; }

    public static <T, R> R reduce(Iterator<? extends T> stream,
                                        Accumulate<T, R> aggregator)
    {
        aggregator.start();
        for ( ; stream.hasNext() ; )
        {
            T item = stream.next(); 
            aggregator.accumulate(item) ;
        }
        return aggregator.get();
    }

    // map without the results - do immediately.
    
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

    public static <T> Iterator<T> filter(Iterable<? extends T> stream,
                                         Filter<T> filter)
    { return filter(stream.iterator(), filter) ; }

    public static <T> Iterator<T> filter(final Iterator<? extends T> stream, final Filter<T> filter)
    {
        final Iterator<T> iter = new Iterator<T>(){
            
            boolean finished = false ; 
            T slot ;
            
            public boolean hasNext()
            {
                if ( finished )
                    return false ; 
                while ( slot == null )
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
                        break ;
                    }
                }
                return slot != null ;
            }
    
            public T next()
            {
                if ( hasNext() )
                {
                    T returnValue = slot ;
                    slot = null ;
                    return returnValue ;
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
    
    public static <T> Iterator<T> append(Iterator<T> iter1, Iterator<T> iter2)
    { return new Iterator2<T>(iter1, iter2); }

    public static <T> Iterable<T> append(Iterable<T> iter1, Iterable<T> iter2)
    {
        Iterator<T> _iter1 = (iter1==null) ? null : iter1.iterator() ;
        Iterator<T> _iter2 = (iter2==null) ? null : iter2.iterator() ;
        
        return new Iterator2<T>(_iter1, _iter2); }
    

    // Helpers from the old world to the new.
    @SuppressWarnings("unchecked")
    public static <T> Iter<T> iter(Iterator iterator) { return new Iter<T>((Iterator<T>)iterator) ; }

    @SuppressWarnings("unchecked")
    public static <T> Iterator<T> convert(Iterator iterator) { return (Iterator<T>)iterator ; }

}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */