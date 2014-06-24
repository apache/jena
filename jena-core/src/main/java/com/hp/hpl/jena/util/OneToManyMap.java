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

// Package
///////////////
package com.hp.hpl.jena.util;


// Imports
///////////////
import java.util.*;

import com.hp.hpl.jena.util.iterator.NullIterator;

/**
 * An extension to a standard map that supports one-to-many mappings: that is, there
 * may be zero, one or many values corresponding to a given key.
 */
public class OneToManyMap<From, To> implements Map<From, To>
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////

    /** Encapsulated hash table stores the values */
    private Map<From, List<To>> m_table = new HashMap<>();


    // Constructors
    //////////////////////////////////

    /**
     * <p>Construct a new empty one-to-many map</p>
     */
    public OneToManyMap() {
    }
    
    
    /**
     * <p>Construct a new one-to-many map whose contents are
     * initialised from the existing map.</p>
     *
     * @param map An existing one-to-many map
     */
    public OneToManyMap( OneToManyMap<From, To> map ) {
        // copy the contents of the existing map
        // note we can't just use the copying constructor for hashmap
        // as we don't want to share the arraylists that are the key values
        for ( From key : map.keySet() )
        {
            for ( Iterator<To> j = map.getAll( key ); j.hasNext(); )
            {
                put( key, j.next() );
            }
        }
    }

    // External signature methods
    //////////////////////////////////

    /**
     * Clear all entries from the map.
     */
    @Override
    public void clear() {
        m_table.clear();
    }


    /**
     * Answer true if the map contains the given value as a key.
     *
     * @param key The key object to test for
     * @return True or false
     */
    @Override
    public boolean containsKey( Object key ) {
        return m_table.containsKey( key );
    }


    /**
     * Answer true if the map contains the given object as a value
     * stored against any key. Note that this is quite an expensive
     * operation in the current implementation.
     *
     * @param value The value to test for
     * @return True if the value is in the map
     */
    @Override
    public boolean containsValue( Object value ) {
        for ( List<To> x : m_table.values() )
        {
            if ( x.contains( value ) )
            {
                return true;
            }
        }
        return false;
    }


    /**
     * <p>Answer true if this mapping contains the pair
     * <code>(key,&nbsp;value)</code>.</p>
     * @param key A key object
     * @param value A value object
     * @return True if <code>key</code> has <code>value</code>
     * as one of its values in this mapping
     */
    public boolean contains( Object key, Object value ) {
        for (Iterator<To> i = getAll( key ); i.hasNext(); ) {
            if (i.next().equals( value )) return true;
        }
        return false;
    }
    
    
    /**
     * Answer a set of the mappings in this map.  Each member of the set will
     * be a Map.Entry value.
     *
     * @return A Set of the mappings as Map.Entry values.
     */
    @Override
    public Set<Map.Entry<From, To>> entrySet() {
        Set<Map.Entry<From, To>> s = CollectionFactory.createHashedSet();

        for ( From key : m_table.keySet() )
        {
            List<To> values = m_table.get( key );

            // add each key-value pair to the result set
            for ( ListIterator<To> e1 = values.listIterator(); e1.hasNext(); )
            {
                s.add( new Entry<>( key, e1.next() ) );
            }
        }

        return s;
    }


    /**
     * Compares the specified object with this map for equality.
     * Returns true if the given object is also a map and the two Maps
     * represent the same mappings. More formally, two maps t1 and t2 represent
     * the same mappings if t1.entrySet().equals(t2.entrySet()).
     *
     * This ensures that the equals method works properly across different
     * implementations of the Map interface.
     *
     * @param o The object to be compared for equality with this map.
     * @return True if the specified object is equal to this map.
     */
    @Override public boolean equals( Object o ) {
        return o instanceof java.util.Map<?,?> && entrySet().equals( ((Map<?,?>) o).entrySet() );
    }


    /**
     * Get a value for this key.  Since this map is explicitly designed to
     * allow there to be more than one mapping per key, this method will return
     * an undetermined instance of the mapping. If no mapping exists, or the
     * selected value is null, null is returned.
     *
     * @param key The key to access the map.
     * @return One of the values this key corresponds to, or null.
     * @see #getAll
     */
    @Override
    public To get( Object key ) {
        List<To> entry = m_table.get( key );

        if (entry != null) {
            if (!entry.isEmpty()) {
                return entry.get( 0 );
            }
        }

        // not present
        return null;
    }


    /**
     * Answer an iterator over all of the values for the given key.  An iterator
     * is always supplied, even if the key is not present.
     *
     * @param key The key object
     * @return An iterator over all of the values for this key in the map
     */
    public Iterator<To> getAll( Object key ) {
        List<To> entry = m_table.get( key );
        return (entry != null) ? entry.iterator() : new NullIterator<To>();
    }


    /**
     * Returns the hash code value for this map. The hash code of a map is
     * defined to be the sum of the hashCodes of each entry in the map's
     * entrySet view. This ensures that t1.equals(t2) implies
     * that t1.hashCode()==t2.hashCode() for any two maps t1 and t2,
     * as required by the general contract of Object.hashCode
     */
    @Override public int hashCode() {
        int hc = 0;

        for ( Map.Entry<From, To> fromToEntry : entrySet() )
        {
            hc ^= fromToEntry.hashCode();
        }

        return hc;
    }


    /**
     * Answer true if the map is empty of key-value mappings.
     *
     * @return True if there are no entries.
     */
    @Override
    public boolean isEmpty() {
        return m_table.isEmpty();
    }


    /**
     * Answer a set of the keys in this map
     *
     * @return The keys of the map as a Set
     */
    @Override
    public Set<From> keySet() {
        return m_table.keySet();
    }


    /**
     * Associates the given value with the given key.  Since this map formulation
     * allows many values for one key, previous associations with the key are not
     * lost.  Consequently, the method always returns null (since the replaced value
     * is not defined).
     *
     * @param key The key object
     * @param value The value object
     * @return Null.
     */
    @Override
    public To put( From key, To value ) {
        List<To> entries = m_table.get( key );
        if (entries == null) entries = new ArrayList<>();
        // add the new value to the list of values held against this key
        entries.add( value );
        m_table.put( key, entries );
        return null;
    }


    /**
     * <p>Put all entries from one map into this map. Tests for m being a 
     * OneToManyMap, and, if so, copies all of the entries for each key.</p>
     * @param m The map whose contents are to be copied into this map
     */
    @Override
    public void putAll( Map<? extends From, ? extends To> m ) {
        boolean many = (m instanceof OneToManyMap<?,?>);

        for ( From key : m.keySet() )
        {
            if ( many )
            {
                // Bizare way to write it but this way makes all compilers happy.
                OneToManyMap<?, ?> Z = (OneToManyMap<?, ?>) m;
                @SuppressWarnings( "unchecked" ) OneToManyMap<? extends From, ? extends To> X =
                    (OneToManyMap<? extends From, ? extends To>) Z;
                Iterator<? extends To> j = X.getAll( key );

                for (; j.hasNext(); )
                {
                    put( key, j.next() );
                }
            }
            else
            {
                put( key, m.get( key ) );
            }
        }
    }


    /**
     * Remove all of the associations for the given key.  If only a specific
     * association is to be removed, use {@link #remove( java.lang.Object, java.lang.Object )}
     * instead.  Has no effect if the key is not present in the map.  Since no
     * single specific association with the key is defined, this method always
     * returns null.
     *
     * @param key All associations with this key will be removed
     * @return null
     */
    @Override
    public To remove( Object key ) {
        m_table.remove( key );
        return null;
    }


    /**
     * <p>Remove the specific association between the given key and value. Has
     * no effect if the association is not present in the map. If all values
     * for a particular key have been removed post removing this particular
     * association, the key will no longer appear as a key in the map.</p>
     *
     * @param key The key object
     * @param value The value object
     * @return {@code true} if an entry was removed. 
     */
    //@Override
    public boolean remove( Object key, Object value ) {
        // Java 8 added a default method with the above signature.
        List<To> entries = m_table.get( key );

        if (entries != null) {
            entries.remove( value );
            
            if (entries.isEmpty()) {
                m_table.remove( key );
                return true;
            }
        }
        return false ;
    }


    /**
     * <p>Answer the number of key-value mappings in the map</p>
     * @return The number of key-value pairs.
     */
    @Override
    public int size() {
        int size = 0;
        for ( From from : m_table.keySet() )
        {
            size += m_table.get( from ).size();
        }
        return size;
    }


    /**
     * <p>Returns a collection view of the values contained in this map.
     * Specifically, this will be a set, so duplicate values that appear
     * for multiple keys are suppressed.</p>
     * @return A set of the values contained in this map.
     */
    @Override
    public Collection<To> values() {
        Set<To> s = CollectionFactory.createHashedSet();
        for ( From from : m_table.keySet() )
        {
            s.addAll( m_table.get( from ) );
        }
        return s;
    }

    /**
     * <p>Answer a string representation of this map. This can be quite a long string for
     * large maps.<p>
     */
    @Override public String toString() {
        StringBuffer buf = new StringBuffer( "OneToManyMap{" );
        String sep = "";

        for ( From key : keySet() )
        {
            buf.append( sep );
            buf.append( key );
            buf.append( "={" );

            String sep1 = "";
            for ( Iterator<To> j = getAll( key ); j.hasNext(); )
            {
                buf.append( sep1 );
                buf.append( j.next() );
                sep1 = ",";
            }
            buf.append( "}" );
            sep = ",";
        }
        buf.append("}");
        return buf.toString();
    }

    // Internal implementation methods
    //////////////////////////////////////
    
    
    // Inner classes 
    //////////////////////////////////////
    
    
    //////////////////////////////////


    //==============================================================================
    // Inner class definitions
    //==============================================================================

    /**
     * Helper class to implement the Map.Entry interface to enumerate entries in the map
     */
    public static class Entry<From, To> implements Map.Entry<From, To>
    {
        /** My key object */
        private From m_key = null;

        /** My value object */
        private To m_value = null;


        /**
         * Constructor - save the key and value
         */
        private Entry( From key, To value ) {
            m_key = key;
            m_value = value;
        }


        /**
         * Compares the specified object with this entry for equality. Returns true if the given
         * object is also a map entry and the two entries represent the same mapping.
         * More formally, two entries e1 and e2 represent the same mapping if
         * <code><pre>
         *      (e1.getKey()==null ?
         *                         e2.getKey()==null : e1.getKey().equals(e2.getKey()))  &&
         *      (e1.getValue()==null ?
         *                         e2.getValue()==null : e1.getValue().equals(e2.getValue()))
         * </pre></code>
         *
         * This ensures that the equals method works properly across different implementations of the Map.Entry interface.
         *
         * @param x The object to compare against
         * @return True if the given object is equal to this Map.Entry object.
         */
        @Override public boolean equals( Object x ) {
            if (x instanceof java.util.Map.Entry<?,?>) {
                Map.Entry<?,?> e1 = (Map.Entry<?,?>) x;
                return (e1.getKey()==null ?
                                          m_key==null : e1.getKey().equals(m_key))  &&
                       (e1.getValue()==null ?
                                            m_value == null : e1.getValue().equals(m_value));
            }
            else
                return false;
        }


        /**
         * Answer the key for the entry
         *
         * @return The key object
         */
        @Override
        public From getKey() {
            return m_key;
        }


        /**
         * Answer the value for the entry
         *
         * @return The value object
         */
        @Override
        public To getValue() {
            return m_value;
        }


        /**
         * Set the value, which writes through to the map. Not implemented.
         */
        @Override
        public To setValue( To value )
            throws  UnsupportedOperationException
        {
            throw new UnsupportedOperationException( "not implemented" );
        }


        /**
         * Returns the hash code value for this map entry.
         * The hash code of a map entry e is defined to be:
         *     (e.getKey()==null   ? 0 : e.getKey().hashCode()) ^
         *     (e.getValue()==null ? 0 : e.getValue().hashCode())
         *
         * This ensures that e1.equals(e2) implies that e1.hashCode()==e2.hashCode() for any two
         * Entries e1 and e2, as required by the general contract of Object.hashCode.
         */
        @Override public int hashCode() {
            return (getKey()==null   ? 0 : getKey().hashCode()) ^
                   (getValue()==null ? 0 : getValue().hashCode());
        }


    }

}
