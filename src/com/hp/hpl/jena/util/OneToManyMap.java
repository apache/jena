/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena
 * Created            5 Jan 2001
 * Filename           $RCSfile: OneToManyMap.java,v $
 * Revision           $Revision: 1.12 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2005-02-21 12:18:57 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2001, 2002, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * See end of file for details
 *****************************************************************************/

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
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: OneToManyMap.java,v 1.12 2005-02-21 12:18:57 andy_seaborne Exp $
 */
public class OneToManyMap
    implements Map
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////

    /** Encapsulated hash table stores the values */
    private Map m_table = new HashMap();


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
    public OneToManyMap( OneToManyMap map ) {
        // copy the contents of the existing map
        // note we can't just use the copying constructor for hashmap
        // as we don't want to share the arraylists that are the key values
        for (Iterator i = map.keySet().iterator();  i.hasNext(); ) {
            Object key = i.next();

            for (Iterator j = map.getAll( key );  j.hasNext();  ) {
                put( key, j.next() );
            }
        }
    }




    // External signature methods
    //////////////////////////////////

    /**
     * Clear all entries from the map.
     */
    public void clear() {
        m_table.clear();
    }


    /**
     * Answer true if the map contains the given value as a key.
     *
     * @param key The key object to test for
     * @return True or false
     */
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
    public boolean containsValue( Object value ) {
        for (Iterator values = m_table.values().iterator();  values.hasNext(); ) {
            Object x = values.next();

            if (x.equals( value )) {
                return true;
            }
            else if (x instanceof List && ((List) x).contains( value )) {
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
        for (Iterator i = getAll( key ); i.hasNext(); ) {
            if (i.next().equals( value )) {
                return true;
            }
        }
        return false;
    }
    
    
    /**
     * Answer a set of the mappings in this map.  Each member of the set will
     * be a Map.Entry value.
     *
     * @return A Set of the mappings as Map.Entry values.
     */
    public Set entrySet() {
        Set s = CollectionFactory.createHashedSet();

        for (Iterator e0 = m_table.keySet().iterator();  e0.hasNext(); ) {
            Object key = e0.next();
            List values = (List) m_table.get( key );

            // add each key-value pair to the result set
            for (ListIterator e1 = values.listIterator();  e1.hasNext(); ) {
                s.add( new Entry( key, e1.next() ) );
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
    public boolean equals( Object o ) {
        if (o instanceof java.util.Map) {
            return entrySet().equals( ((Map) o).entrySet() );
        }
        else
            return false;
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
    public Object get( Object key ) {
        ArrayList entry = (ArrayList) m_table.get( key );

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
    public Iterator getAll( Object key ) {
        ArrayList entry = (ArrayList) m_table.get( key );
        return (entry != null) ? entry.iterator() : NullIterator.instance;
    }


    /**
     * Returns the hash code value for this map. The hash code of a map is
     * defined to be the sum of the hashCodes of each entry in the map's
     * entrySet view. This ensures that t1.equals(t2) implies
     * that t1.hashCode()==t2.hashCode() for any two maps t1 and t2,
     * as required by the general contract of Object.hashCode
     */
    public int hashCode() {
        int hc = 0;

        for (Iterator i = entrySet().iterator();  i.hasNext(); ) {
            hc ^= i.next().hashCode();
        }

        return hc;
    }


    /**
     * Answer true if the map is empty of key-value mappings.
     *
     * @return True if there are no entries.
     */
    public boolean isEmpty() {
        return m_table.isEmpty();
    }


    /**
     * Answer a set of the keys in this map
     *
     * @return The keys of the map as a Set
     */
    public Set keySet() {
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
    public Object put( Object key, Object value ) {
        ArrayList entries = (ArrayList) m_table.get( key );
        entries = entries == null ? new ArrayList() : entries;

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
    public void putAll( Map m ) {
        boolean many = (m instanceof OneToManyMap);
        
        for (Iterator i = m.keySet().iterator(); i.hasNext(); ) {
            Object key = i.next();
            if (many) {
                for (Iterator j = ((OneToManyMap) m).getAll( key ); j.hasNext(); ) {
                    put( key, j.next() );
                }
            }
            else {
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
    public Object remove( Object key ) {
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
     */
    public void remove( Object key, Object value ) {
        List entries = (List) m_table.get( key );

        if (entries != null) {
            entries.remove( value );
            
            if (entries.isEmpty()) {
                m_table.remove( key );
            }
        }
    }


    /**
     * <p>Answer the number of key-value mappings in the map</p>
     * @return The number of key-value pairs.
     */
    public int size() {
        int size = 0;

        for (Iterator i = m_table.keySet().iterator();  i.hasNext();  ) {
            size += ((List) m_table.get( i.next() )).size();
        }

        return size;
    }


    /**
     * <p>Returns a collection view of the values contained in this map.
     * Specifically, this will be a set, so duplicate values that appear
     * for multiple keys are suppressed.</p>
     * @return A set of the values contained in this map.
     */
    public Collection values() {
        Set s = CollectionFactory.createHashedSet();

        for (Iterator e = m_table.keySet().iterator();  e.hasNext();  ) {
            s.addAll( (List) m_table.get(e.next()) );
        }

        return s;
    }

    /**
     * <p>Answer a string representation of this map. This can be quite a long string for
     * large maps.<p>
     */
    public String toString() {
        StringBuffer buf = new StringBuffer( "OneToManyMap{" );
        String sep = "";
        
        for (Iterator i = keySet().iterator(); i.hasNext(); ) {
            Object key = i.next();
            buf.append( sep );
            buf.append( key );
            buf.append( "={" );
            
            String sep1 = "";
            for (Iterator j = getAll(key); j.hasNext(); ) {
                buf.append( sep1 );
                buf.append( j.next() );
                sep1=",";
            }
            buf.append("}");
            sep=",";
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
    public static class Entry
        implements Map.Entry
    {
        /** My key object */
        private Object m_key = null;

        /** My value object */
        private Object m_value = null;


        /**
         * Constructor - save the key and value
         */
        private Entry( Object key, Object value ) {
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
        public boolean equals( Object x ) {
            if (x instanceof java.util.Map.Entry) {
                Map.Entry e1 = (Map.Entry) x;

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
        public Object getKey() {
            return m_key;
        }


        /**
         * Answer the value for the entry
         *
         * @return The value object
         */
        public Object getValue() {
            return m_value;
        }


        /**
         * Set the value, which writes through to the map. Not implemented.
         */
        public Object setValue( Object value )
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
        public int hashCode() {
            return (getKey()==null   ? 0 : getKey().hashCode()) ^
                   (getValue()==null ? 0 : getValue().hashCode());
        }


    }

}

/*
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
