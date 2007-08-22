/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.util.alg;

import java.util.*;

import com.hp.hpl.jena.sdb.util.IterFunc;



public class Stream<T> implements Iterable<T>
{
    // Add merge(Stream, Stream, MergeOp)
    // statics to Alg?
    // .andThen = .concat
    
    Iterator<T> iterator ;
    
    public Stream(Iterator<T> other)
    {
        iterator = other ;
    }
    
    public Stream(Iterable<T> set)
    { this(set.iterator()) ; }
    
    // A proper "clone/split" would be better (delayed caching)
//    public Stream(Stream<? extends T> other)
//    { iterator = other.iterator ; }
    
    public Iterator<T> iterator()
    { return iterator ; }

    public Set<T> toSet()
    { return IterFunc.toSet(iterator) ; }

    public List<T> toList()
    { return IterFunc.toList(iterator) ; } 
    
    public <R> R reduce(Accumulate<T, R> aggregator)
    { return IterFunc.reduce(iterator, aggregator) ; }
    
    public void apply(Action<T> action)
    { IterFunc.apply(iterator, action) ; }
    
    public Stream<T> filter(Filter<T> filter)
    { return new Stream<T>(IterFunc.filter(iterator, filter)) ; }

    public <R> Stream<R> map(Transform<T, R> converter)
    { return new Stream<R>(IterFunc.map(iterator, converter)) ; }

    @Override
    public String toString() { return IterFunc.asString(iterator()) ; }
    
    // --------
    
    private static class ToString<T> implements Accumulate<T, String>
    {
        StringBuilder buffer = null ;
        private String sep ;
        private boolean first = true ;
        
        public ToString(String sep) { this.sep = sep ; }
        public ToString() { this(" ") ; }
        
        public void accumulate(T item)
        { 
            if ( ! first )
                buffer.append(sep) ;
            buffer.append(item.toString()) ;
            first = false ;
        }

        public String get()
        {
            return buffer.toString() ;
        }

        public void start()
        { buffer = new StringBuilder() ; first = true ; }
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