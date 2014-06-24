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

import com.hp.hpl.jena.util.OneToManyMap;

import junit.framework.TestCase;



/**
 * <p>
 * Unit tests for one-to-many map
 * </p>
 */
public class TestOneToManyMap 
    extends TestCase
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    private String s0 = "s0";
    private String s1 = "s1";
    private String s2 = "s2";
    private String s3 = "s3";
    private String s4 = "s4";

    
    // Constructors
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    @Override public void setUp() {}
    
    @Override public void tearDown() {}
    
    public void testConstruct0() {
        // the types of these maps
        OneToManyMap<String, Integer> map0 = new OneToManyMap<>();
        assertNotNull( map0 );
        
        assertTrue( map0.isEmpty() );
        
        OneToManyMap<String, Integer> map1 = new OneToManyMap<>( map0 );
        assertNotNull( map1 );
        assertTrue( map1.isEmpty() );
    }
    
    public void testConstruct1() {
        OneToManyMap<String, String> map0 = new OneToManyMap<>();
        
        map0.put( s0, s1 );
        assertTrue( map0.contains( s0, s1 ) );
        
        OneToManyMap<String, String> map1 = new OneToManyMap<>( map0 );
        assertTrue( map0.contains( s0, s1 ) );
        
        map0.put( s0, s2 );
        assertTrue( map0.contains( s0, s2 ) );
        assertFalse( map0.contains( s1, s2 ) );
        
    }
    
    public void testClear() {
        OneToManyMap<String, String> map0 = new OneToManyMap<>();
        
        map0.put( s0, s1 );
        assertTrue( map0.contains( s0, s1 ) );
        assertFalse( map0.isEmpty() );
        
        map0.clear();
        assertFalse( map0.contains( s0, s1 ) );
        assertTrue( map0.isEmpty() );
    }

    public void testContainsKey() {
        OneToManyMap<String, String> map0 = new OneToManyMap<>();
        assertFalse( map0.containsKey( s0 ) );
        assertFalse( map0.containsKey( s1 ) );
        map0.put( s0, s1 );
        assertTrue( map0.containsKey( s0 ) );
        assertFalse( map0.containsKey( s1 ) );
    }
    
    public void testContainsValue() {
        OneToManyMap<String, String> map0 = new OneToManyMap<>();
        assertFalse( map0.containsValue( s0 ) );
        assertFalse( map0.containsValue( s1 ) );
        assertFalse( map0.containsValue( s2 ) );
        map0.put( s0, s1 );
        assertFalse( map0.containsValue( s0 ) );
        assertTrue( map0.containsValue( s1 ) );
        assertFalse( map0.containsValue( s2 ) );
        map0.put( s0, s2 );
        assertFalse( map0.containsValue( s0 ) );
        assertTrue( map0.containsValue( s1 ) );
        assertTrue( map0.containsValue( s2 ) );
    }
    
    public void testContains() {
        OneToManyMap<String, String> map0 = new OneToManyMap<>();
        assertFalse( map0.contains( s0, s1 ) );
        assertFalse( map0.contains( s0, s2 ) );
        assertFalse( map0.contains( s1, s2 ) );
        map0.put( s0, s1 );
        assertTrue( map0.contains( s0, s1 ) );
        assertFalse( map0.contains( s0, s2 ) );
        assertFalse( map0.contains( s1, s2 ) );
        map0.put( s0, s2 );
        assertTrue( map0.contains( s0, s1 ) );
        assertTrue( map0.contains( s0, s2 ) );
        assertFalse( map0.contains( s1, s2 ) );
    }
    
    public void testEntrySet() {
        OneToManyMap<String, String> map0 = new OneToManyMap<>();
        map0.put( s0, s1 );
        map0.put( s0, s2 );
        map0.put( s3, s4 );
        
        boolean s0s1 = false;
        boolean s0s2 = false;
        boolean s3s4 = false;

        for ( Map.Entry<String, String> e : map0.entrySet() )
        {
            if ( e.getKey().equals( s0 ) && e.getValue().equals( s1 ) )
            {
                s0s1 = true;
            }
            else if ( e.getKey().equals( s0 ) && e.getValue().equals( s2 ) )
            {
                s0s2 = true;
            }
            else if ( e.getKey().equals( s3 ) && e.getValue().equals( s4 ) )
            {
                s3s4 = true;
            }
            else
            {
                throw new IllegalArgumentException( "unexpected: " + e );
            }
        }
        
        assertTrue( s0s1 );
        assertTrue( s0s2 );
        assertTrue( s3s4 );
    }
    
    public void testEquals() {
        OneToManyMap<String, String> map0 = new OneToManyMap<>();
        map0.put( s0, s1 );
        map0.put( s0, s2 );
        map0.put( s3, s4 );
        OneToManyMap<String, String> map1 = new OneToManyMap<>();
        map1.put( s3, s4 );
        map1.put( s0, s1 );
        map1.put( s0, s2 );
        OneToManyMap<String, String> map2 = new OneToManyMap<>();
        map2.put( s0, s2 );
        map2.put( s3, s4 );
        
        assertTrue( map0.equals( map1 ) );
        assertTrue( map1.equals( map0 ) );
        assertTrue( map0.hashCode() == map1.hashCode() );
        
        assertFalse( map0.equals( map2 ));
        assertFalse( map2.equals( map0 ));
    }
    
    public void testGet() {
        OneToManyMap<String, String> map0 = new OneToManyMap<>();
        assertNull( map0.get( s0 ));
        map0.put( s0, s1 );
        assertEquals( s1, map0.get( s0 ));
        map0.put( s0, s2 );
        assertTrue( map0.get( s0 ).equals( s1 ) ||
                    map0.get( s0 ).equals( s2 ));
    }
    
    public void testGetAll() {
        OneToManyMap<String, String> map0 = new OneToManyMap<>();
        Iterator<String> i = map0.getAll(s0);
        assertNotNull( i );
        assertFalse( i.hasNext() );

        map0.put( s0, s1 );
        i = map0.getAll(s0);
        assertNotNull( i );
        assertTrue( i.hasNext() );
        assertEquals( s1, i.next() );
        assertFalse( i.hasNext() );
        
        map0.put( s0, s2 );
        i = map0.getAll(s0);
        assertNotNull( i );
        boolean founds1 = false, founds2 = false;
        while (i.hasNext()) {
            Object x = i.next();
            if (x.equals(s1)) {
                founds1 = true;
            }
            else if (x.equals(s2)) {
                founds2 = true;
            }
            else {
                throw new IllegalArgumentException( x.toString() );
            }
        }
        assertTrue( founds1 );
        assertTrue( founds2 );
    }
    
    public void testKeySet() {
        OneToManyMap<String, String> map0 = new OneToManyMap<>();
        Set<String> keys = new HashSet<>();
        assertEquals( keys, map0.keySet() );
        
        map0.put( s0, s1 );
        keys.add( s0 );
        assertEquals( keys, map0.keySet() );
        
        map0.put( s2, s1 );
        keys.add( s2 );
        assertEquals( keys, map0.keySet() );   
    }
    
    public void testPutAll0() {
        OneToManyMap<String, String> map0 = new OneToManyMap<>();
        map0.put( s0, s1 );
        map0.put( s0, s2 );
        map0.put( s3, s4 );
        
        OneToManyMap<String, String> map1 = new OneToManyMap<>();
        map1.put( s0, s2 );
        map1.put( s3, s4 );
        map1.put( s0, s1 );

        OneToManyMap<String, String> map2 = new OneToManyMap<>();
        map2.putAll( map1 );
        assertEquals( map0, map2 );
    }
    
    public void testPutAll1() {
        OneToManyMap<String, String> map0 = new OneToManyMap<>();
        map0.put( s0, s1 );
        map0.put( s3, s4 );
        
        Map<String, String> map1 = new HashMap<>();
        map1.put( s3, s4 );
        map1.put( s0, s1 );

        OneToManyMap<String, String> map2 = new OneToManyMap<>();
        map2.putAll( map1 );
        assertEquals( map0, map2 );
    }
    
    public void testRemove0() {
        OneToManyMap<String, String> map0 = new OneToManyMap<>();
        map0.put( s0, s1 );
        map0.put( s3, s4 );
        
        map0.remove( s0 );
        map0.remove( s3 );
        
        assertTrue( map0.isEmpty() );
    }
    
    public void testRemove1() {
        OneToManyMap<String, String> map0 = new OneToManyMap<>();
        map0.put( s0, s1 );
        map0.put( s0, s2 );
        map0.put( s3, s4 );
        
        map0.remove( s0, s2 );
        map0.remove( s3, s4 );
        
        assertFalse( map0.isEmpty() );
        
        map0.remove( s0, s1 );
        assertTrue( map0.isEmpty() );
    }
    
    public void testSize() {
        OneToManyMap<String, String> map0 = new OneToManyMap<>();
        assertEquals( 0, map0.size() );
        map0.put( s0, s1 );
        assertEquals( 1, map0.size() );
        map0.put( s0, s2 );
        assertEquals( 2, map0.size() );
        map0.put( s3, s4 );
        assertEquals( 3, map0.size() );
        map0.remove( s0, s2 );
        assertEquals( 2, map0.size() );
        map0.remove( s3, s4 );
        assertEquals( 1, map0.size() );
        map0.remove( s0, s1 );
        assertEquals( 0, map0.size() );
    }

    public void testValues() {
        OneToManyMap<String, String> map0 = new OneToManyMap<>();
        Set<String> vals = new HashSet<>();
        assertEquals( vals, map0.values() );
        
        map0.put( s0, s1 );
        vals.add( s1 );
        assertEquals( vals, map0.values() );
        
        map0.put( s2, s1 );
        assertEquals( vals, map0.values() );   
        
        map0.put( s2, s3 );
        vals.add( s3 );
        assertEquals( vals, map0.values() );   
    }
    
    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
