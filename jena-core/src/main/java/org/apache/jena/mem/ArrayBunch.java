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

package org.apache.jena.mem;

import java.util.ConcurrentModificationException ;
import java.util.Spliterator;
import java.util.function.Consumer;

import org.apache.jena.graph.Triple ;
import org.apache.jena.util.iterator.ExtendedIterator ;
import org.apache.jena.util.iterator.NiceIterator ;

/**
    An ArrayBunch implements TripleBunch with a linear search of a short-ish
    array of Triples. The array can grow, but it only grows by 4 elements each time
    (because, if it gets big enough for this linear growth to be bad, it should anyways
    have been replaced by a more efficient set-of-triples implementation).
*/
public class ArrayBunch implements TripleBunch
    {
    
    protected int size = 0;
    protected Triple [] elements;
    protected volatile int changes = 0; 

    public ArrayBunch()
        { elements = new Triple[5]; }
    
    @Override
    public boolean containsBySameValueAs( Triple t )
        {
        int i = size;
        while (i > 0) if (t.matches( elements[--i])) return true;
        return false;
        }
    
    @Override
    public boolean contains( Triple t )
        {
        int i = size;
        while (i > 0) if (t.equals( elements[--i] )) return true;
        return false;
        }
    
    @Override
    public int size()
        { return size; }
    
    @Override
    public void add( Triple t )
        { 
        if (size == elements.length) grow();
        elements[size++] = t; 
        changes++;
        }
    
    /**
        Note: linear growth is suboptimal (order n<sup>2</sup>) normally, but
        ArrayBunch's are meant for <i>small</i> sets and are replaced by some
        sort of hash- or tree- set when they get big; currently "big" means more
        than 9 elements, so that's only one growth spurt anyway.  
    */
    protected void grow()
        {
        Triple [] newElements = new Triple[size + 4];
        System.arraycopy( elements, 0, newElements, 0, size );
        elements = newElements;
        }

    @Override
    public void remove( Triple t )
        {
        changes++;
        for (int i = 0; i < size; i += 1)
            {
            if (t.equals( elements[i] ))
                { elements[i] = elements[--size];
                return; }
            }
        }

    @Override
    public ExtendedIterator<Triple> iterator()
        {
        return iterator( new HashCommon.NotifyEmpty() { @Override
        public void emptied() {} } );
        }
    
    @Override
    public ExtendedIterator<Triple> iterator( final HashCommon.NotifyEmpty container )
        {
        return new NiceIterator<Triple>()
            {
            protected final int initialChanges = changes;
            
            protected int i = size;

            @Override public boolean hasNext()
                { 
                return 0 < i;
                }
        
            @Override public Triple next()
                {
                if (changes != initialChanges) throw new ConcurrentModificationException();
                if (i == 0) noElements( "no elements left in ArrayBunch iteration" );
                return elements[--i];
                }

            @Override
                public void forEachRemaining(Consumer<? super Triple> action)
                {
                while(0 < i--) action.accept(elements[i]);
                if (changes != initialChanges) throw new ConcurrentModificationException();
                }

            @Override public void remove()
                {
                if (changes != initialChanges) throw new ConcurrentModificationException();
                int last = --size;
                elements[i] = elements[last];
                elements[last] = null;
                if (size == 0) container.emptied();
                }
            };
        }

        @Override
        public Spliterator<Triple> spliterator() {

            return new Spliterator<Triple>() {

                protected final int initialChanges = changes;

                int i = size;

                @Override
                public boolean tryAdvance(Consumer<? super Triple> action)
                    {
                    if(0 < i)
                        {
                        action.accept(elements[--i]);
                        if (changes != initialChanges) throw new ConcurrentModificationException();
                        return true;
                        }
                    return false;
                    }

                @Override
                public void forEachRemaining(Consumer<? super Triple> action) {
                    while(0 < i--) action.accept(elements[i]);
                    if (changes != initialChanges) throw new ConcurrentModificationException();
                }

                @Override
                public Spliterator<Triple> trySplit() {
                    /* the number of elements here should always be small, so splitting is not wise  */
                    return null;
                }

                @Override
                public long estimateSize() {
                    return i;
                }

                @Override
                public long getExactSizeIfKnown() {
                    return i;
                }

                @Override
                public int characteristics() {
                    return DISTINCT | SIZED | NONNULL | IMMUTABLE;
                }
            };
        }
    }
