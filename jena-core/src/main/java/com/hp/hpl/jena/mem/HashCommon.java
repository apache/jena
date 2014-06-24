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

import java.util.*;

import com.hp.hpl.jena.shared.BrokenException;
import com.hp.hpl.jena.util.iterator.*;

/**
    Shared stuff for our hashing implementations: does the base work for
    hashing and growth sizes.
*/
public abstract class HashCommon<Key>
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
    protected Key [] keys;
    
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
        keys = newKeyArray( capacity = initialCapacity );
        threshold = (int) (capacity * loadFactor);
        }
    
    /**
        Subclasses must implement to answer a new Key[size] array.
    */
    protected abstract Key[] newKeyArray( int size );

    /**
        A hashed structure may become empty as a side-effect of a .remove on one
        of its iterators: a container can request notification of this by passing
        a <code>NotifyEmpty</code> object in when the iterator is constructed,
        and its <code>emptied</code> method is called when the bunch
        becomes empty.
       */
    public static interface NotifyEmpty
        {
        /**
             A NotifyEmpty instance that ignores the notification.
        */
        public static NotifyEmpty ignore = new NotifyEmpty() 
            { @Override
            public void emptied() { }};
        
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
        { return (improveHashCode( key.hashCode() ) & 0x7fffffff) % capacity; }

    /**
        Answer the transformed hash code, intended to be an improvement
        on the objects own hashcode. The magic number 127 is performance
        voodoo to (try to) eliminate problems experienced by Wolfgang.
    */
    protected int improveHashCode( int hashCode )
        { return hashCode * 127; }    
    
    /**
        Search for the slot in which <code>key</code> is found. If it is absent,
        return the index of the free slot in which it could be placed. If it is present,
        return the bitwise complement of the index of the slot it appears in. Hence
        negative values imply present, positive absent, and there's no confusion
        around 0.
    */
    protected final int findSlot( Key key )
        {
        int index = initialIndexFor( key );
        while (true)
            {
            Key current = keys[index];
            if (current == null) return index; 
            if (key.equals( current )) return ~index;
            if (--index < 0) index += capacity;
            }
        }   

    /**
        Remove the object <code>key</code> from this hash's keys if it
        is present (if it's absent, do nothing). If a key is removed, the
        <code>removeAssociatedValues</code> will be invoked. If a key
        is moved, the <code>moveAssociatedValues</code> method will
        be called.
    */
    public void remove( Key key )
        { primitiveRemove( key ); }

    private void primitiveRemove( Key key )
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
            for ( int prime : primes )
            {
                if ( prime > atLeast )
                {
                    return prime;
                }
            }
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
    protected Key removeFrom( int here )
        {
        final int original = here;
        Key wrappedAround = null;
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
                if (scan <= r && r < here || r < here && here < scan || here < scan && scan <= r)
                    { /* Nothing. We'd have preferred an `unless` statement. */}
                else
                    {
                    // System.err.println( ">> move from " + scan + " to " + here + " [original = " + original + ", r = " + r + "]" );
                    if (here <= original && scan > original) 
                        {
                        // System.err.println( "]] recording wrapped " );
                        wrappedAround = keys[scan];
                        }
                    keys[here] = keys[scan];
                    moveAssociatedValues( here, scan );
                    here = scan;
                    break;
                    }
                }
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

    public ExtendedIterator<Key> keyIterator()
        { return keyIterator( NotifyEmpty.ignore ); }
    
    public ExtendedIterator<Key> keyIterator( final NotifyEmpty container )
        {
        showkeys();
        final List<Key> movedKeys = new ArrayList<>();
        ExtendedIterator<Key> basic = new BasicKeyIterator( changes, container, movedKeys );
        ExtendedIterator<Key> leftovers = new MovedKeysIterator( changes, container, movedKeys );
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
    protected final class MovedKeysIterator extends NiceIterator<Key>
        {
        private final List<Key> movedKeys;

        protected int index = 0;
        final int initialChanges;
        final NotifyEmpty container;

        protected MovedKeysIterator( int initialChanges, NotifyEmpty container, List<Key> keys )
            { 
            this.movedKeys = keys; 
            this.initialChanges = initialChanges; 
            this.container = container;
            }

        @Override public boolean hasNext()
            { 
            if (changes > initialChanges) throw new ConcurrentModificationException( "changes " + changes + " > initialChanges " + initialChanges );
            return index < movedKeys.size(); 
            }

        @Override public Key next()
            {
            if (changes > initialChanges) throw new ConcurrentModificationException();
            if (hasNext() == false) noElements( "" );
            return movedKeys.get( index++ );
            }

        @Override public void remove()
            { 
            if (changes > initialChanges) throw new ConcurrentModificationException();
            primitiveRemove( movedKeys.get( index - 1 ) ); 
            if (size == 0) container.emptied();
            }
        }

    /**
        The BasicKeyIterator iterates over the <code>keys</code> array.
        If a .remove call moves an unprocessed key underneath the iterator's
        index, that key value is added to the <code>movedKeys</code>
        list supplied to the constructor.
    */
    protected final class BasicKeyIterator extends NiceIterator<Key>
        {
        protected final List<Key> movedKeys;

        int index = 0;
        final int initialChanges;
        final NotifyEmpty container;

        protected BasicKeyIterator( int initialChanges, NotifyEmpty container, List<Key> movedKeys )
            { 
            this.movedKeys = movedKeys; 
            this.initialChanges = initialChanges;  
            this.container = container;
            }

        @Override public boolean hasNext()
            {
            if (changes > initialChanges) throw new ConcurrentModificationException();
            while (index < capacity && keys[index] == null) index += 1;
            return index < capacity;
            }

        @Override public Key next()
            {
            if (changes > initialChanges) throw new ConcurrentModificationException();
            if (hasNext() == false) noElements( "HashCommon keys" );
            return keys[index++];
            }

        @Override public void remove()
            {
            if (changes > initialChanges) throw new ConcurrentModificationException();
            // System.err.println( ">> keyIterator::remove, size := " + size +
            // ", removing " + keys[index + 1] );
            Key moved = removeFrom( index - 1 );
            if (moved != null) movedKeys.add( moved );
            if (size == 0) container.emptied();
            if (size < 0) throw new BrokenException( "BROKEN" );
            showkeys();
            }
        }
    }
