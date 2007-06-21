/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.util;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.sdb.util.alg.Accumulate;
import com.hp.hpl.jena.sdb.util.alg.Action;
import com.hp.hpl.jena.sdb.util.alg.Filter;
import com.hp.hpl.jena.sdb.util.alg.Transform;

public class Iter<T> implements Iterable<T>
{
    public static <T> Iter<T> iter(Iterator<T> iterator) { return new Iter<T>(iterator) ; }
    
    @SuppressWarnings("unchecked")
    public static <T> Iter<T> convert(Iterator iterator) { return new Iter<T>((Iterator<T>)iterator) ; }
    
    private Iterator<T> iterator ;
    public Iter(Iterator<T> iterator) { this.iterator = iterator ; }
    public Iterator<T>  iterator() { return iterator ; }
    
    // .map/.reduce/.appy/.filter/.append
    // .asList/.asSet
    // Calls to Alg?
 
    public Set<T> toSet()
    {
        return Alg.toSet(iterator) ;
    }

    public List<T> toList()
    {
        return Alg.toList(iterator) ;
    }

    public Iter<T> filter(Filter<T> filter)
    {
        return iter(Alg.filter(iterator, filter)) ;
    }

    public <R> Iter<R> map(Transform<T, R> converter)
    {
        return iter(Alg.map(iterator, converter)) ;
    }

    public <R> R reduce(Accumulate<T, R> aggregator)
    {
        return Alg.reduce(iterator, aggregator) ;
    }

    public void apply(Action<T> action)
    {
        Alg.apply(iterator, action) ;
    }

    public Iter<T> append(Iter< ? extends T> iter)
    {
        return iter(new Iterator2<T>(iterator, iter.iterator())) ;
    }
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