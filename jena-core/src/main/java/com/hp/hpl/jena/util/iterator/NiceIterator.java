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
    NiceIterator is the standard base class implementing ExtendedIterator. It provides
    the static methods for <code>andThen</code>, <code>filterKeep</code> and
    <code>filterDrop</code>; these can be reused from any other class. It defines
    equivalent instance methods for descendants and to satisfy ExtendedIterator.
*/

public class NiceIterator<T> implements ExtendedIterator<T>
    {
    public NiceIterator()
        { super(); }

    /**
        default close: don't need to do anything.
    */
    @Override
    public void close()
        { }

    /**
        default hasNext: no elements, return false.
    */
    @Override
    public boolean hasNext()
        {  return false; }

    protected void ensureHasNext()
        { if (hasNext() == false) throw new NoSuchElementException(); }
    
    /**
        default next: throw an exception.
    */
    @Override
    public T next()
        { throw new NoSuchElementException( "empty NiceIterator" ); }
        
    /**
        Utility method for this and other (sub)classes: raise the appropriate
        "no more elements" exception. I note that we raised the wrong exception
        in at least one case ...
    
        @param message the string to include in the exception
        @return never - but we have a return type to please the compiler
    */
    protected T noElements( String message )
        { throw new NoSuchElementException( message ); }
        
    /**
        default remove: we have no elements, so we can't remove any.
    */
    @Override
    public void remove()
        { 
        throw new UnsupportedOperationException( "remove not supported for this iterator" ); 
        }
    
    /**
         Answer the next object, and remove it.
    */
    @Override
    public T removeNext()
        { T result = next(); remove(); return result; }
        
    /**
        concatenate two closable iterators.
    */
    
    public static <T> ExtendedIterator<T> andThen( final Iterator<T> a, final Iterator<? extends T> b )
        {
        final List<Iterator<? extends T>> pending = new ArrayList<>( 2 );
        pending.add( b );
        return new NiceIterator<T>()
            {
            private int index = 0;
            
            private Iterator<? extends T> current = a;
            private Iterator<? extends T> removeFrom = null;
            
            @Override public boolean hasNext()
                { 
                while (current.hasNext() == false && index < pending.size()) current = advance();
                return current.hasNext();
                }

            private Iterator< ? extends T> advance()
                {
                Iterator< ? extends T> result = pending.get( index );
                pending.set( index, null );
                index += 1;
                return result;
                }
                
            @Override public T next()
                {
                if (!hasNext()) noElements( "concatenation" );
                removeFrom = current;
                return current.next();
                }
                
            @Override public void close()
                {
                close( current );
                for (int i = index; i < pending.size(); i += 1) close( pending.get(i) );
                pending.clear();
                removeFrom = null;
                }
                
            @Override public void remove()
                {
                if (null == removeFrom) throw new IllegalStateException("no calls to next() since last call to remove()");
                removeFrom.remove();
                removeFrom = null;
                }
            
            @Override public <X extends T> ExtendedIterator<T> andThen( Iterator<X> other )
                { pending.add( other ); 
                return this; }
            };
        }
    
    /**
        make a new iterator, which is us then the other chap.
    */   
    @Override
    public <X extends T> ExtendedIterator<T> andThen( Iterator<X> other )
        { return andThen( this, other ); }
        
    /**
        make a new iterator, which is our elements that pass the filter
    */
    @Override
    public ExtendedIterator<T> filterKeep( Filter<T> f )
        { return new FilterKeepIterator<>( f, this ); }

    /**
        make a new iterator, which is our elements that do not pass the filter
    */        
    @Override
    public ExtendedIterator<T> filterDrop( final Filter<T> f )
        { return new FilterDropIterator<>( f, this ); }
   
    /**
        make a new iterator which is the elementwise _map1_ of the base iterator.
    */     
    @Override
    public <U> ExtendedIterator<U> mapWith( Map1<T, U> map1 )
        { return new Map1Iterator<>( map1, this ); }

    /**
        If <code>it</code> is a Closableiterator, close it. Abstracts away from
        tests [that were] scattered through the code.
    */
    public static void close( Iterator<?> it )
        { if (it instanceof ClosableIterator<?>) ((ClosableIterator<?>) it).close(); }
   
    /**
     * An iterator over no elements.
     * @return A class singleton which doesn't iterate.
     */
    static public <T> ExtendedIterator<T> emptyIterator() 
        { return NullIterator.instance() ; }

    /**
        Answer a list of the elements in order, consuming this iterator.
    */
    @Override
    public List<T> toList()
        { return asList( this ); }

    /**
        Answer a list of the elements in order, consuming this iterator.
    */
    @Override
    public Set<T> toSet()
        { return asSet( this ); }

    /**
        Answer a list of the elements of <code>it</code> in order, consuming this iterator.
        Canonical implementation of toSet().
    */
    public static <T> Set<T> asSet( ExtendedIterator<T> it )
        {
        Set<T> result = new HashSet<>();
        while (it.hasNext()) result.add( it.next() );
        return result;
        }

    /**
        Answer a list of the elements from <code>it</code>, in order, consuming
        that iterator. Canonical implementation of toList().
    */
    public static <T> List<T> asList( ExtendedIterator<T> it )
        {
        List<T> result = new ArrayList<>();
        while (it.hasNext()) result.add( it.next() );
        return result;
        }
    }
