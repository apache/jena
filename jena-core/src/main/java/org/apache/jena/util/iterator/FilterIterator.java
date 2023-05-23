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

package org.apache.jena.util.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** 
     Creates a sub-Iterator by filtering. This class should not be used
     directly any more; the subclasses FilterKeepIterator and FilterDropIterator
     should be used instead.
 */
public class FilterIterator<T> extends WrappedIterator<T>
    {
	protected final Predicate<T> f;
	protected T current;
    protected boolean canRemove;
    protected boolean hasCurrent;

    /** 
        Initialises a FilterIterator with its filter and base.
        @param fl An object is included if it is accepted by this Filter.
        @param e The base Iterator.
    */        
	public FilterIterator( Predicate<T> fl, Iterator<T> e ) 
        {
		super( e );
		f = fl;
        }

    /** 
        Answer true iff there is at least one more acceptable object.
        [Stores reference into <code>current</code>, sets <code>canRemove</code>
        false; answer preserved in `hasCurrent`]
    */        
	@Override public boolean hasNext() 
        {
	    while (!hasCurrent && super.hasNext())
            hasCurrent = f.test( current = super.next() );
        canRemove = false;
        return hasCurrent;
        }

    /** 
         Remove the current member from the underlying iterator. Legal only
         after a .next() but before any subsequent .hasNext(), because that
         may advance the underlying iterator.
    */        
    @Override public void remove() 
        {
        if (!canRemove ) throw new IllegalStateException
            ( "FilterIterators do not permit calls to hasNext between calls to next and remove." );
        super.remove();
        }
        
    /** 
        Answer the next acceptable object from the base iterator. The redundant
        test of `hasCurrent` appears to make a detectable speed difference.
        Crazy.
    */        
	@Override public T next() 
        {
		if (hasCurrent || hasNext()) 
            {
            canRemove = true;
            hasCurrent = false;
            return current;
            }
		throw new NoSuchElementException();
        }

    @Override
        public void forEachRemaining(Consumer<? super T> action)
        {
        if (hasCurrent) {
            action.accept(current);
            hasCurrent = false;
        }
        super.forEachRemaining(e -> {
            if (f.test(e)) {
                action.accept(e);
            }
        });
        }
    }
