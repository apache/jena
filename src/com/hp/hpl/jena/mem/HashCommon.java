/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: HashCommon.java,v 1.1 2005-10-28 10:14:31 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem;

/**
    Shared stuff for our hashing implementations.
    @author kers
*/
public abstract class HashCommon
    {
    protected static final double loadFactor = 0.5;
    
    protected Object [] keys;
    protected int capacity;
    protected int threshold;
    
    protected int size = 0;
    
    protected HashCommon( int initialCapacity )
        {
        keys = new Object[capacity = initialCapacity];
        threshold = (int) (capacity * loadFactor);
        }

    protected final int initialIndexFor( Object key )
        { return (key.hashCode() & 0x7fffffff) % capacity; }    
    
    protected int findSlot( Object key )
        {
        int index = initialIndexFor( key );
        while (true)
            {
            Object current = keys[index];
            if (current == null) return index; 
            if (key.equals( current )) return ~index;
            index = (index == 0 ? capacity - 1 : index - 1);
            }
        }   
    
    protected void growCapacityAndThreshold()
        {
        capacity = capacity * 2;
        threshold = (int) (capacity * loadFactor);
        }
    
    /**
        Remove the triple at element <code>i</code> of <code>contents</code>.
        This is an implementation of Knuth's Algorithm R from tAoCP vol3, p 527,
        with exchanging of the roles of i and j so that they can be usefully renamed
        to <i>here</i> and <i>scan</i>.
    <p>
        It relies on linear probing but doesn't require a distinguished REMOVED
        value. Since we resize the table when it gets fullish, we don't worry [much]
        about the overhead of the linear probing.
    */
    protected void removeFrom( int here )
        {
        while (true)
            {
            keys[here] = null;
            removeAssociatedValues( here );
            int scan = here;
            while (true)
                {
                scan = (scan == 0 ? capacity - 1 : scan -1);
                Object key = keys[scan];
                if (key == null) return;
                int r = initialIndexFor( key );
                if (!((scan <= r && r < here) || (r < here && here < scan) || (here < scan && scan <= r) )) break;
                }
            keys[here] = keys[scan];
            moveAssociatedValues( here, scan );
            here = scan;
            }
        }

    protected void removeAssociatedValues( int here )
        {}

    protected void moveAssociatedValues( int here, int scan )
        {}
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