/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: HashedTripleBunch.java,v 1.6 2005-10-28 10:14:31 chris-dollin Exp $
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
        super( (int) (b.size() / loadFactor) + 10 );
        for (Iterator it = b.iterator(); it.hasNext();) add( (Triple) it.next() );        
        }
    
    public boolean contains( Triple t )
        { return findSlot( t ) < 0; }    
    
    protected int findSlotBySameValueAs( Triple t )
        {
        int index = initialIndexFor( t );
        while (true)
            {
            Object current = keys[index];
            if (current == null) return index;
            if (t.matches( (Triple) current )) return ~index;
            index = (index == 0 ? capacity - 1 : index - 1);
            }
        }
    
    public boolean containsBySameValueAs( Triple t )
        { return findSlotBySameValueAs( t ) < 0; }
    
    public int size()
        { return size; }
    
    public void add( Triple t )
        {
        int where = findSlot( t );
        keys[where] = t;
        size += 1;
        if (size > threshold) grow();
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
            
            public boolean hasNext()
                {
                while (index < capacity && (keys[index] == null))
                    index += 1;
                return index < capacity;
                }
            
            public Object next()
                {
                if (hasNext() == false) noElements( "" );
                Object answer = keys[index];
                lastIndex = index;
                toRemove = keys[index];
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