/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: HashedTripleBunch.java,v 1.7 2005-10-31 15:13:02 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem;

import java.util.*;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.util.iterator.*;

public class HashedTripleBunch extends HashCommon implements TripleBunch
    {
    public HashedTripleBunch( TripleBunch b )
        {
        super( (int) (b.size() / loadFactor) + 11 );
        for (Iterator it = b.iterator(); it.hasNext();) add( (Triple) it.next() );        
        }
    
    public boolean contains( Triple t )
        { return findSlot( t ) < 0; }    
    
    protected int findSlotBySameValueAs( Triple key )
        {
        int index = initialIndexFor( key );
        // int k = 1;
        while (true)
            {
            Object current = keys[index];
            if (current == null) return index;
            if (key.matches( (Triple) current )) return ~index;
            // System.err.println( ">> collision " + ++k );
//            if (++k == 6) 
//                {
//                RuntimeException e = new RuntimeException( "that's too many collisions for " + key + " in set of size " + size + "/" + capacity );
//                  System.err.println( ";;; -- iterator -------------------------------------------" );
//                  for (int i = 0; i < capacity; i += 1)
//                      {
//                      Object x = keys[i];
//                      System.err.print( i + ": " );
//                      if (x == null) System.err.println( "FREE" );
//                      else System.err.println( "[" + initialIndexFor( x ) + "] " + x );
//                      }
//                  System.err.println( ";;; ++ done ++++++++++++++++++++++++++++++" );
//                e.printStackTrace( System.err );
//                throw e;
//                }
            if (--index < 0) index += capacity;
            }
        }
    
    public boolean containsBySameValueAs( Triple t )
        { return findSlotBySameValueAs( t ) < 0; }
    
    public int size()
        { return size; }
    
    public void add( Triple t )
        {
        keys[findSlot( t )] = t;
        if (++size > threshold) grow();
        }
    
    protected void grow()
        {
        Object [] oldContents = keys;
        final int oldCapacity = capacity;
        growCapacityAndThreshold();
        keys = new Triple[capacity];
        for (int i = 0; i < oldCapacity; i += 1)
            {
            Object t = oldContents[i];
            if (t != null) keys[findSlot( t )] = t;
            }
        }
    
    public void remove( Triple t )
        {
        int where = findSlot( t );
        removeFrom( ~where );
        size -= 1;
        }
    
    public ExtendedIterator iterator()
        {
        return new NiceIterator()
            {
            int index = 0;
            int lastIndex = -1;
            Object toRemove = null;
            Object current = null;
            
            public boolean hasNext()
                {
                if (current == null)
                    {
                    while (index < capacity && ((current = keys[index]) == null)) index += 1;
                    }
                return current != null;
                }
            
            public Object next()
                {
                if (current == null && hasNext() == false) noElements( "HashedTripleBunch iterator empty" );
                Object answer = toRemove = current;
                lastIndex = index;
                current = null;
                index += 1;
                return answer;
                }
            
            public void remove()
                {
                if (keys[lastIndex] != toRemove) throw new RuntimeException( "CME" );
                HashedTripleBunch.this.removeFrom( lastIndex );
                }
            };
        }
    
    public void app( Domain d, StageElement next, MatchOrBind s )
        {
        int i = capacity;
        while (i > 0)
            {
            Object t = keys[--i];
            if (t != null  && s.matches( (Triple) t )) next.run( d );
            }
        }
    }

/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
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