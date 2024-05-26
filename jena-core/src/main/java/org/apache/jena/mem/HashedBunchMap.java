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

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function ;

import org.apache.jena.shared.BrokenException ;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NiceIterator;

/**
    An implementation of BunchMap that does open-addressed hashing.
*/
public class HashedBunchMap extends HashCommon<Object> implements BunchMap
    {
    protected TripleBunch [] values;

    public HashedBunchMap()
        {
        super( 10 );
        values = new TripleBunch[capacity];
        }

    @Override protected Object[] newKeyArray( int size )
        { return new Object[size]; }

    /**
        Clear this map: all entries are removed. The keys <i>and value</i> array
        elements are set to null (so the values may be garbage-collected).
    */
    @Override
    public void clear()
        {
        size = 0;
        for (int i = 0; i < capacity; i += 1) keys[i] = values[i] = null;
        }

    @Override
    public long size()
        { return size; }

    @Override
    public TripleBunch get( Object key )
        {
        int slot = findSlot( key );
        return slot < 0 ? values[~slot] : null;
        }

    @Override
    public void put( Object key, TripleBunch value )
        {
        int slot = findSlot( key );
        if (slot < 0)
            values[~slot] = value;
        else
            put$(slot, key, value) ;
        }

    @Override
    public TripleBunch getOrSet( Object key, Function<Object, TripleBunch> setter) {
        int slot = findSlot( key );
        if (slot < 0)
            // Get.
            return values[~slot] ;
        // Or set value.
        TripleBunch value = setter.apply(key) ;
        put$(slot, key, value) ;
        return value ;
        }

    private void put$(int slot, Object key, TripleBunch value) {
        keys[slot] = key;
        values[slot] = value;
        size += 1;
        if ( size == threshold )
            grow();
    }

    protected void grow()
        {
        Object [] oldContents = keys;
        TripleBunch [] oldValues = values;
        final int oldCapacity = capacity;
        growCapacityAndThreshold();
        keys = newKeyArray( capacity );
        values = new TripleBunch[capacity];
        for (int i = 0; i < oldCapacity; i += 1)
            {
            Object key = oldContents[i];
            if (key != null)
                {
                int j = findSlot( key );
                if (j < 0)
                    {
                    throw new BrokenException( "oh dear, already have a slot for " + key  + ", viz " + ~j );
                    }
                keys[j] = key;
                values[j] = oldValues[i];
                }
            }
        }

    /**
        Called by HashCommon when a key is removed: remove
        associated element of the <code>values</code> array.
    */
    @Override protected void removeAssociatedValues( int here )
        { values[here] = null; }

    /**
        Called by HashCommon when a key is moved: move the
        associated element of the <code>values</code> array.
    */
    @Override protected void moveAssociatedValues( int here, int scan )
        { values[here] = values[scan]; }

    @Override public Iterator<TripleBunch> iterator()
        {
        final List<Object> movedKeys = new ArrayList<>();
        ExtendedIterator<TripleBunch> basic = new BasicValueIterator( changes, movedKeys );
        ExtendedIterator<TripleBunch> leftovers = new MovedValuesIterator( changes, movedKeys );
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
        protected final class MovedValuesIterator extends NiceIterator<TripleBunch>
        {
            private final List<Object> movedKeys;

            protected int index = 0;
            final int initialChanges;

            protected MovedValuesIterator(int initialChanges, List<Object> movedKeys)
            {
                this.movedKeys = movedKeys;
                this.initialChanges = initialChanges;
            }

            @Override public boolean hasNext()
            {
                return index < movedKeys.size();
            }

            @Override public TripleBunch next()
            {
                if (changes > initialChanges) throw new ConcurrentModificationException( "changes " + changes + " > initialChanges " + initialChanges );
                if (index < movedKeys.size()) return get(movedKeys.get( index++ ));
                return noElements( "" );
            }

            @Override public void forEachRemaining(Consumer<? super TripleBunch> action)
            {
                while(index < movedKeys.size()) action.accept( get(movedKeys.get( index++ )) );
                if (changes > initialChanges) throw new ConcurrentModificationException();
            }

            @Override public void remove()
            {
                if (changes > initialChanges) throw new ConcurrentModificationException();
                primitiveRemove( movedKeys.get( index - 1 ) );
            }
        }

        /**
         The BasicKeyIterator iterates over the <code>keys</code> array.
         If a .remove call moves an unprocessed key underneath the iterator's
         index, that key value is added to the <code>movedKeys</code>
         list supplied to the constructor.
         */
        protected final class BasicValueIterator extends NiceIterator<TripleBunch>
        {
            protected final List<Object> movedKeys;

            int pos = capacity-1;
            final int initialChanges;

            protected BasicValueIterator(int initialChanges, List<Object> movedKeys)
            {
                this.movedKeys = movedKeys;
                this.initialChanges = initialChanges;
            }

            @Override public boolean hasNext()
            {
                while(-1 < pos)
                {
                    if(null != values[pos])
                        return true;
                    pos--;
                }
                return false;
            }

            @Override public TripleBunch next()
            {
                if (changes > initialChanges) throw new ConcurrentModificationException();
                if (-1 < pos && null != values[pos]) return values[pos--];
                throw new NoSuchElementException("HashCommon keys");
            }

            @Override public void forEachRemaining(Consumer<? super TripleBunch> action)
            {
                while(-1 < pos)
                {
                    if(null != values[pos]) action.accept(values[pos]);
                    pos--;
                }
                if (changes > initialChanges) throw new ConcurrentModificationException();
            }

            @Override public void remove()
            {
                if (changes > initialChanges) throw new ConcurrentModificationException();
                // System.err.println( ">> keyIterator::remove, size := " + size +
                // ", removing " + keys[index + 1] );
                Object moved = removeFrom( pos + 1 );
                if (moved != null) movedKeys.add( moved );
                if (size < 0) throw new BrokenException( "BROKEN" );
            }
        }

    @Override public Spliterator<TripleBunch> spliterator() {
        final var initialChanges = changes;
        final Runnable checkForConcurrentModification = () ->
        {
            if (changes != initialChanges) throw new ConcurrentModificationException();
        };

        return new SparseArraySpliterator<>(values, size, checkForConcurrentModification);
        }
    }
