/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.iterator;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Iter<T> implements Iterable<T>, Iterator<T>
{
    public static <T> Iter<T> iter(Iter<T> iter)
    { return iter ; }
    
    public static <T> Iter<T> iter(Iterator<T> iterator)
    { 
        if ( iterator instanceof Iter )
            return (Iter<T>)iterator ;
        return new Iter<T>(iterator) ;
    }
    
    public static <T> Iter<T> iter(Iterable<T> iterable)
    { 
        if ( iterable instanceof Iter )
            return (Iter<T>)iterable ;
        return new Iter<T>(iterable.iterator()) ;
    }
    
    @SuppressWarnings({"unchecked", "cast"})
    public static <T> Iter<T> convert(Iterator iterator) { return iter((Iterator<T>)iterator) ; }
    
    private Iterator<T> iterator ;
    private  Iter(Iterator<T> iterator) { this.iterator = iterator ; }
    
    
    
    public Set<T> toSet()
    {
        return Stream.toSet(iterator) ;
    }

    public List<T> toList()
    {
        return Stream.toList(iterator) ;
    }

    public Iter<T> filter(Filter<T> filter)
    {
        return iter(Stream.filter(iterator, filter)) ;
    }

    public <R> Iter<R> map(Transform<T, R> converter)
    {
        return iter(Stream.map(iterator, converter)) ;
    }

    public <R> R reduce(Accumulate<T, R> aggregator)
    {
        return Stream.reduce(iterator, aggregator) ;
    }

    public void apply(Action<T> action)
    {
        Stream.apply(iterator, action) ;
    }

    public Iter<T> append(Iter< ? extends T> iter)
    {
        return new Iter<T>(new Iterator2<T>(iterator, iter.iterator())) ;
    }
    
    public Iter<T> append(Iterable<? extends T> iter)
    {
        return new Iter<T>(new Iterator2<T>(iterator, iter.iterator())) ;
    }
    
    public Iter<T> append(Iterator<? extends T> iter)
    {
        return new Iter<T>(new Iterator2<T>(iterator, iter)) ;
    }

    public String asString() { return Stream.asString(iterator) ; }
    public String asString(String sep) { return Stream.asString(iterator, sep) ; }
    
    public Iter<T> distinct()
    {
        return new Iter<T>(Stream.distinct(iterator())) ;
    }

    // ---- Iterable
    public Iterator<T>  iterator() { return iterator ; }
    
    // ---- Iterator
    public boolean hasNext()    { return iterator.hasNext() ; }

    public T next()             { return iterator.next() ; }

    public void remove()        { iterator.remove() ; }
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