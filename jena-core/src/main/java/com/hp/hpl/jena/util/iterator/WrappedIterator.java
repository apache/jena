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

package com.hp.hpl.jena.util.iterator;

import java.util.*;

/**
    a WrappedIterator is an ExtendedIterator wrapping around a plain (or
    presented as plain) Iterator. The wrapping allows the usual extended
    operations (filtering, concatenating) to be done on an Iterator derived
    from some other source.
<br>
*/

public class WrappedIterator<T> extends NiceIterator<T>
    {
    /**
         set to <code>true</code> if this wrapping doesn't permit the use of 
         .remove(). Otherwise the .remove() is delegated to the base iterator.
    */
    protected boolean removeDenied;
    
    /**
        Answer an ExtendedIterator returning the elements of <code>it</code>.
        If <code>it</code> is itself an ExtendedIterator, return that; otherwise
        wrap <code>it</code>.
    */
    public static <T> ExtendedIterator<T> create( Iterator<T> it )
        { return it instanceof ExtendedIterator<?> ? (ExtendedIterator<T>) it : new WrappedIterator<>( it, false ); }
    
    /**
        Answer an ExtendedIterator wrapped round <code>it</code> which does not
        permit <code>.remove()</code> even if <code>it</code> does.
    */
    public static <T> WrappedIterator<T> createNoRemove( Iterator<T> it )
        { return new WrappedIterator<>( it, true ); }
   
    
    /** Given an Iterator that returns Iterator's, this creates an
     * Iterator over the next level values.
     * Similar to list splicing in lisp.
     */
    public static <T> ExtendedIterator<T> createIteratorIterator( Iterator<Iterator<T>> it )
    { 
    	ExtendedIterator<T> retval = NullIterator.instance();
    	while (it.hasNext())
    	{
    		retval = retval.andThen(it.next());
    	}
    	return retval;
    }
   
    /** the base iterator that we wrap */  
    protected final Iterator<? extends T> base;
    
    public Iterator<? extends T> forTestingOnly_getBase()
        { return base; }
    
    /** constructor: remember the base iterator */
    protected WrappedIterator( Iterator<? extends T> base )
        { this( base, false ); }
    
    /**
         Initialise this wrapping with the given base iterator and remove-control.
         @param base the base iterator that this tierator wraps
         @param removeDenied true if .remove() must throw an exception
    */
    protected WrappedIterator( Iterator<? extends T> base, boolean removeDenied )
        { this.base = base; 
        this.removeDenied = removeDenied; }
        
    /** hasNext: defer to the base iterator */
    @Override public boolean hasNext()
        { return base.hasNext(); }
        
    /** next: defer to the base iterator */
    @Override public T next()
        { return base.next(); }
        
    /** 
         if .remove() is allowed, delegate to the base iterator's .remove; 
         otherwise, throw an UnsupportedOperationException. 
    */
    @Override public void remove()
        {
        if (removeDenied) throw new UnsupportedOperationException();
        base.remove(); 
        }
        
    /** close: defer to the base, iff it is closable */
    @Override public void close()
        { close( base ); }

    /**
        if <code>it</code> is a Closableiterator, close it. Abstracts away from
        tests [that were] scattered through the code.
    */
    public static void close( Iterator<?> it )
        { NiceIterator.close( it ); }
    }
