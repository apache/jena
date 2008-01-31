/*
 	(c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: HashCommon.java,v 1.14 2008-01-31 12:30:53 chris-dollin Exp $
*/

package com.hp.hpl.jena.mem;

import java.util.*;

import com.hp.hpl.jena.shared.BrokenException;
import com.hp.hpl.jena.util.iterator.*;

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
    public int capacity;
    
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
        A count of the number of changes applied to this Hash object, used for
        detecting concurrent modifications.
    */
    protected int changes;
    
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
        A hashed structure may become empty as a side-effect of a .remove on one
        of its iterators: a container can request notification of this by passing
        a <code>NotifyEmpty</code> object in when the iterator is constructed,
        and its <code>emptied</code> method is called when the bunch
        becomes empty.
        @author kers
    */
    public static interface NotifyEmpty
        {
        /**
             A NotifyEmpty instance that ignores the notification.
        */
        public static NotifyEmpty ignore = new NotifyEmpty() 
            { public void emptied() { }};
        
        /**
             Method to call to notify that the collection has become empty.
        */
        public void emptied(); 
        }   

    /**
        When removeFrom [or remove] removes a key, it calls this method to 
        remove any associated values, passing in the index of the key's slot. 
        Subclasses override if they have any associated values.
    */
    protected void removeAssociatedValues( int here )
        {}

    /**
        When removeFrom [or remove] moves a key, it calls this method to move 
        any associated values, passing in the index of the slot <code>here</code>
        to move to and the index of the slot <code>scan</code> to move from.
        Subclasses override if they have any associated values.
    */
    protected void moveAssociatedValues( int here, int scan )
        {}
    
    /**
        Answer the item at index <code>i</code> of <code>keys</code>. This
        method is for testing purposes <i>only</i>.
    */
    public Object getItemForTestingAt( int i )
        { return keys[i]; }
    
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
        { return ((key.hashCode() * 1) & 0x7fffffff) % capacity; }    
    
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
        Remove the object <code>key</code> from this hash's keys if it
        is present (if it's absent, do nothing). If a key is removed, the
        <code>removeAssociatedValues</code> will be removed. If a key
        is moved, the <code>moveAssociatedValues</code> method will
        be called.
    */
    public void remove( Object key )
        {
        int slot = findSlot( key );
        if (slot < 0) removeFrom( ~slot );
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
    <p>
        Iterators running over the keys may miss elements that are moved from the
        top of the table to the bottom because of Iterator::remove. removeFrom
        returns such a moved key as its result, and null otherwise.
    */
    protected Object removeFrom( int here )
        {
        final int original = here;
        Object wrappedAround = null;
        size -= 1;
        while (true)
            {
            keys[here] = null;
            removeAssociatedValues( here );
            int scan = here;
            while (true)
                {
                if (--scan < 0) scan += capacity;
                Object key = keys[scan];
                if (key == null) return wrappedAround;
                int r = initialIndexFor( key );
                if (!((scan <= r && r < here) || (r < here && here < scan) || (here < scan && scan <= r) )) break;
                }
            // System.err.println( ">> move from " + scan + " to " + here + " [original = " + original + "]" );
            if (here <= original && scan > original) 
                {
                // System.err.println( "]] recording wrapped " );
                wrappedAround = keys[scan];
                }
            keys[here] = keys[scan];
            moveAssociatedValues( here, scan );
            here = scan;
            }
        }    
    
    void showkeys()
        {
        if (false)
            {
            System.err.print( ">> KEYS:" );
            for (int i = 0; i < capacity; i += 1)
                if (keys[i] != null) System.err.print( " " + initialIndexFor( keys[i] ) + "@" + i + "::" + keys[i] );
            System.err.println();
            }
        }

    public ExtendedIterator keyIterator()
        { return keyIterator( NotifyEmpty.ignore ); }
    
    public ExtendedIterator keyIterator( final NotifyEmpty container )
        {
        showkeys();
        final List movedKeys = new ArrayList();
        ExtendedIterator basic = new BasicKeyIterator( changes, container, movedKeys );
        ExtendedIterator leftovers = new MovedKeysIterator( changes, container, movedKeys );
        return basic.andThen( leftovers );
        }
    
    /**
        The MovedKeysIterator iterates over the elements of the <code>keys</code>
        list. It's not sufficient to just use List::iterator, because the .remove
        method must remove elements from the hash table itself.
    <p>
        Note that the list supplied on construction will be empty: it is filled before
        the first call to <code>hasNext()</code>.
    */
    protected final class MovedKeysIterator extends NiceIterator
        {
        private final List keys;

        protected int index = 0;
        final int initialChanges;
        final NotifyEmpty container;

        protected MovedKeysIterator( int initialChanges, NotifyEmpty container, List keys )
            { 
            this.keys = keys; 
            this.initialChanges = initialChanges; 
            this.container = container;
            }

        public boolean hasNext()
            { 
            if (changes > initialChanges) throw new ConcurrentModificationException();
            return index < keys.size(); 
            }

        public Object next()
            {
            if (changes > initialChanges) throw new ConcurrentModificationException();
            if (hasNext() == false) noElements( "" );
            return keys.get( index++ );
            }

        public void remove()
            { 
            if (changes > initialChanges) throw new ConcurrentModificationException();
            HashCommon.this.remove( keys.get( index - 1 ) ); 
            if (size == 0) container.emptied();
            }
        }

    /**
        The BasicKeyIterator iterates over the <code>keys</code> array.
        If a .remove call moves an unprocessed key underneath the iterator's
        index, that key value is added to the <code>movedKeys</code>
        list supplied to the constructor.
    */
    protected final class BasicKeyIterator extends NiceIterator
        {
        protected final List movedKeys;

        int index = 0;
        final int initialChanges;
        final NotifyEmpty container;

        protected BasicKeyIterator( int initialChanges, NotifyEmpty container, List movedKeys )
            { 
            this.movedKeys = movedKeys; 
            this.initialChanges = initialChanges;  
            this.container = container;
            }

        public boolean hasNext()
            {
            if (changes > initialChanges) throw new ConcurrentModificationException();
            while (index < capacity && keys[index] == null) index += 1;
            return index < capacity;
            }

        public Object next()
            {
            if (changes > initialChanges) throw new ConcurrentModificationException();
            if (hasNext() == false) noElements( "HashCommon keys" );
            return keys[index++];
            }

        public void remove()
            {
            if (changes > initialChanges) throw new ConcurrentModificationException();
            // System.err.println( ">> keyIterator::remove, size := " + size +
            // ", removing " + keys[index + 1] );
            Object moved = removeFrom( index - 1 );
            if (moved != null) movedKeys.add( moved );
            if (size == 0) container.emptied();
            if (size < 0) throw new BrokenException( "BROKEN" );
            showkeys();
            }
        }
    }

/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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