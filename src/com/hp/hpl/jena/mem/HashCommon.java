/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: HashCommon.java,v 1.4 2005-11-01 15:30:19 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem;

/**
    Shared stuff for our hashing implementations: does the base work for
    hashing and growth sizes.
    @author kers
*/
public abstract class HashCommon
    {
    /**
        Jeremy suggests, from his experiments, that load factors more than
        0.6 leave the table too dense, and little advantage is gained below 0.4.
        Although that was with a quadratic probe, I'm borrowing the same 
        plausible range, and use 0.5 by default. 
    */
    protected static final double loadFactor = 0.5;
    
    /**
        The keys of whatever table it is we're implementing. Since we share code
        for triple sets and for node->bunch maps, it has to be an Object array; we
        take the casting hit.
     */
    protected Object [] keys;
    
    /**
        The capacity (length) of the key array.
    */
    protected int capacity;
    
    /**
        The threshold number of elements above which we resize the table;
        equal to the capacity times the load factor.
    */
    protected int threshold;
    
    /**
        The number of active elements in the table, maintained incrementally.
    */
    protected int size = 0;
    
    /**
        Initialise this hashed thingy to have <code>initialCapacity</code> as its
        capacity and the corresponding threshold. All the key elements start out
        null.
    */
    protected HashCommon( int initialCapacity )
        {
        keys = new Object[capacity = initialCapacity];
        threshold = (int) (capacity * loadFactor);
        }

    /**
        Answer the initial index for the object <code>key</code> in the table.
        With luck, this will be the final position for that object. The initial index
        will always be non-negative and less than <code>capacity</code>.
    <p>
        Implementation note: do <i>not</i> use <code>Math.abs</code> to turn a
        hashcode into a positive value; there is a single specific integer on which
        it does not work. (Hence, here, the use of bitmasks.)
    */
    protected final int initialIndexFor( Object key )
        { return (key.hashCode() & 0x7fffffff) % capacity; }    
    
    /**
        Search for the slot in which <code>key</code> is found. If it is absent,
        return the index of the free slot in which it could be placed. If it is present,
        return the bitwise complement of the index of the slot it appears in. Hence
        negative values imply present, positive absent, and there's no confusion
        around 0.
    */
    protected final int findSlot( Object key )
        {
        int index = initialIndexFor( key );
        while (true)
            {
            Object current = keys[index];
            if (current == null) return index; 
            if (key.equals( current )) return ~index;
            if (--index < 0) index += capacity;
            }
        }   
    
    /**
        Work out the capacity and threshold sizes for a new improved bigger
        table (bigger by a factor of two, at present).
    */
    protected void growCapacityAndThreshold()
        {
        capacity = nextSize( capacity * 2 );
        threshold = (int) (capacity * loadFactor);
        }
     
    static final int [] primes =
        {
        7, 19, 37, 79, 149, 307, 617, 1237, 2477, 4957, 9923,
        19853, 39709, 79423, 158849, 317701, 635413,
        1270849, 2541701, 5083423
        };
    
    protected static int nextSize( int atLeast )
        {
        for (int i = 0; i < primes.length; i += 1)
            if (primes[i] > atLeast) return primes[i];
        return atLeast;
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
                if (--scan < 0) scan += capacity;
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

    /**
        When removeFrom removes a key, it calls this method to remove any
        associated values, passing in the index of the key's slot. Subclasses 
        override if they have any associated values.
    */
    protected void removeAssociatedValues( int here )
        {}

    /**
        When removeFrom moves a key, it calls this method to move any
        associated values, passing in the index of the slot <code>here</code>
        to move to and the index of the slot <code>scan</code> to move from.
        Subclasses override if they have any associated values.
    */
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