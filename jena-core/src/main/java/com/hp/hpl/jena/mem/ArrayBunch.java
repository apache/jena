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

package com.hp.hpl.jena.mem;

import java.util.ConcurrentModificationException ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.NiceIterator ;

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
        changes += 1;
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
        changes += 1;
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
//        System.err.println( ">> ArrayBunch::iterator: intial state" );
//        for (int j = 0; j < size; j += 1) System.err.println( "==    " + elements[j] );
//        System.err.println( ">> (done)" );
        return new NiceIterator<Triple>()
            {
            protected final int initialChanges = changes;
            
            protected int i = size;
            protected final Triple [] e = elements;
            
            @Override public boolean hasNext()
                { 
                if (changes > initialChanges) throw new ConcurrentModificationException();
                return i > 0; 
                }
        
            @Override public Triple next()
                {
                if (changes > initialChanges) throw new ConcurrentModificationException();
                if (i == 0) noElements( "no elements left in ArrayBunch iteration" );
                return e[--i]; 
                }
            
            @Override public void remove()
                {
                if (changes > initialChanges) throw new ConcurrentModificationException();
//                System.err.println( ">> ArrayBunch.iterator::remove" );
//                System.err.println( "++  size currently " + size );
//                System.err.println( "++  container is " + container );
//                System.err.println( "++  selector currently " + i + " (triple " + e[i] + ")" );
                int last = --size;
                e[i] = e[last];
                e[last] = null;
                if (size == 0) container.emptied();
//                System.err.println( "++  post remove, triples are:" );
//                for (int j = 0; j < size; j += 1) System.err.println( "==    " + e[j] );
                }
            };
        }
    }
