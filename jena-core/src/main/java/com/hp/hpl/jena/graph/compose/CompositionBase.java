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

// Package
///////////////
package com.hp.hpl.jena.graph.compose;


// Imports
///////////////
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.util.IteratorCollection;
import com.hp.hpl.jena.util.iterator.*;

import java.util.*;


/**
 * <p>
 * Base class for graphs that are composed of multiple sub-graphs.  This is to provide
 * a home for shared functionality that was previously in {@link Dyadic} before
 * refactoring.
 * </p>
 */
public abstract class CompositionBase extends GraphBase
{
    /**
     * <p>
     * Answer a {@link Filter} that will reject any element that is a member of iterator i.
     * As a side-effect, i will be closed. 
     * </p>
     * 
     * @param i A closable iterator
     * @return A Filter that will accept any object not a member of i.
     */
    public static <T> Filter<T> reject( final ClosableIterator<? extends T> i )
        {
        final Set< ? extends T> suppress = IteratorCollection.iteratorToSet( i );
        return new Filter<T>()
            { @Override public boolean accept( T o ) { return !suppress.contains( o ); } };
        }
        
    /**
     * <p>
     * Answer an iterator over the elements of iterator a that are not members of iterator b.
     * As a side-effect, iterator b will be closed.
     * </p>
     * 
     * @param a An iterator that will be filtered by rejecting the elements of b
     * @param b A closable iterator 
     * @return The iteration of elements in a but not in b.
     */
    public static <T> ClosableIterator<T> butNot( final ClosableIterator<T> a, final ClosableIterator<? extends T> b )
        {
        return new FilterIterator<>( reject( b ), a );
        }
        
    /**
     * <p>
     * Answer an iterator that will record every element delived by <code>next()</code> in
     * the set <code>seen</code>. 
     * </p>
     * 
     * @param i A closable iterator
     * @param seen A set that will record each element of i in turn
     * @return An iterator that records the elements of i.
     */
    public static <T> ExtendedIterator<T> recording( final ClosableIterator<T> i, final Set<T> seen )
        {
        return new NiceIterator<T>()
            {
            @Override
            public void remove()
                { i.remove(); }
            
            @Override
            public boolean hasNext()
                { return i.hasNext(); }    
            
            @Override
            public T next()
                { T x = i.next(); 
                try { seen.add( x ); } catch (OutOfMemoryError e) { throw e; } return x; }  
                
            @Override
            public void close()
                { i.close(); }
            };
        }
        
    //static final Object absent = new Object();
    
    /**
     * <p>
     * Answer an iterator over the elements of iterator i that are not in the set <code>seen</code>. 
     * </p>
     * 
     * @param i An extended iterator
     * @param seen A set of objects
     * @return An iterator over the elements of i that are not in the set <code>seen</code>.
     */
    public static ExtendedIterator<Triple> rejecting( final ExtendedIterator<Triple> i, final Set<Triple> seen )
        {
        Filter<Triple> seenFilter = new Filter<Triple>()
            { @Override
            public boolean accept( Triple x ) { return seen.contains( x ); } };
        return i.filterDrop( seenFilter );
        }
        
    /**
         Answer an iterator over the elements of <code>i</code> that are not in
         the graph <code>seen</code>.
    */
    public static ExtendedIterator<Triple> rejecting( final ExtendedIterator<Triple> i, final Graph seen )
        {
        Filter<Triple> seenFilter = new Filter<Triple>()
            { @Override public boolean accept( Triple x ) { return seen.contains( x ); } };
        return i.filterDrop( seenFilter );
        }
  
    /**
     * <p>
     * Answer a {@link Filter} that will accept any object that is an element of 
     * iterator i.  As a side-effect, i will be evaluated and closed. 
     * </p>
     * 
     * @param i A closable iterator 
     * @return A Filter that will accept any object in iterator i.
     */
    public static <T> Filter<T> ifIn( final ClosableIterator<T> i )
        {
        final Set<T> allow = IteratorCollection.iteratorToSet( i );
        return new Filter<T>()
            { @Override public boolean accept( T x ) { return allow.contains( x ); } };
        }
        
    /**
     * <p>
     * Answer a {@link Filter} that will accept any triple that is an edge of 
     * graph g. 
     * </p>
     * 
     * @param g A graph 
     * @return A Filter that will accept any triple that is an edge in g.
     */
    public static Filter<Triple> ifIn( final Graph g )
        {
        return new Filter<Triple>()
            { @Override public boolean accept( Triple x ) { return g.contains( x ); } };
        }
        

    // Internal implementation methods
    //////////////////////////////////


    //==============================================================================
    // Inner class definitions
    //==============================================================================


}
