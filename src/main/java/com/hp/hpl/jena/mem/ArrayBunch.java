/*
    (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
    All rights reserved - see end of file.
    $Id: ArrayBunch.java,v 1.1 2009-06-29 08:55:55 castagna Exp $
*/
package com.hp.hpl.jena.mem;

import java.util.ConcurrentModificationException;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.util.iterator.*;

/**
    An ArrayBunch implements TripleBunch with a linear search of a short-ish
    array of Triples. The array can grow, but it only grows by 4 elements each time
    (because, if it gets big enough for this linear growth to be bad, it should anyways
    have been replaced by a more efficient set-of-triples implementation).
    
    @author kers
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
    public void app( Domain d, StageElement next, MatchOrBind s )
        {
        int i = size, initialChanges = changes;
        while (i > 0) 
            {
            if (changes > initialChanges) throw new ConcurrentModificationException();
            if (s.matches( elements[--i] )) next.run( d );
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

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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