/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            @package@
 * Web site           @website@
 * Created            21-Jan-2005
 * Filename           $RCSfile: TestOneToManyMap.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2005-01-21 17:00:45 $
 *               by   $Author: ian_dickinson $
 *
 * @copyright@
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.util.test;


// Imports
///////////////
import java.util.*;
import java.util.HashSet;
import java.util.Map;

import com.hp.hpl.jena.util.OneToManyMap;

import junit.framework.TestCase;



/**
 * <p>
 * Unit tests for one-to-many map
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version Release @release@ ($Id: TestOneToManyMap.java,v 1.1 2005-01-21 17:00:45 ian_dickinson Exp $)
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

    public void setUp() {
    }
    
    public void tearDown() {
    }
    
    public void testConstruct0() {
        OneToManyMap map0 = new OneToManyMap();
        assertNotNull( map0 );
        
        assertTrue( map0.isEmpty() );
        
        OneToManyMap map1 = new OneToManyMap( map0 );
        assertNotNull( map1 );
        assertTrue( map1.isEmpty() );
    }
    
    public void testConstruct1() {
        OneToManyMap map0 = new OneToManyMap();
        
        map0.put( s0, s1 );
        assertTrue( map0.contains( s0, s1 ) );
        
        OneToManyMap map1 = new OneToManyMap( map0 );
        assertTrue( map0.contains( s0, s1 ) );
        
        map0.put( s0, s2 );
        assertTrue( map0.contains( s0, s2 ) );
        assertFalse( map0.contains( s1, s2 ) );
        
    }
    
    public void testClear() {
        OneToManyMap map0 = new OneToManyMap();
        
        map0.put( s0, s1 );
        assertTrue( map0.contains( s0, s1 ) );
        assertFalse( map0.isEmpty() );
        
        map0.clear();
        assertFalse( map0.contains( s0, s1 ) );
        assertTrue( map0.isEmpty() );
    }

    public void testContainsKey() {
        OneToManyMap map0 = new OneToManyMap();
        assertFalse( map0.containsKey( s0 ) );
        assertFalse( map0.containsKey( s1 ) );
        map0.put( s0, s1 );
        assertTrue( map0.containsKey( s0 ) );
        assertFalse( map0.containsKey( s1 ) );
    }
    
    public void testContainsValue() {
        OneToManyMap map0 = new OneToManyMap();
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
        OneToManyMap map0 = new OneToManyMap();
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
        OneToManyMap map0 = new OneToManyMap();
        map0.put( s0, s1 );
        map0.put( s0, s2 );
        map0.put( s3, s4 );
        
        boolean s0s1 = false;
        boolean s0s2 = false;
        boolean s3s4 = false;
        
        for (Iterator i = map0.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry) i.next();
            if (e.getKey().equals( s0 ) && e.getValue().equals( s1 )) {
                s0s1 = true;
            }
            else if (e.getKey().equals( s0 ) && e.getValue().equals( s2 )) {
                s0s2 = true;
            }
            else if (e.getKey().equals( s3 ) && e.getValue().equals( s4 )) {
                s3s4 = true;
            }
            else {
                throw new IllegalArgumentException( "unexpected: " + e );
            }
        }
        
        assertTrue( s0s1 );
        assertTrue( s0s2 );
        assertTrue( s3s4 );
    }
    
    public void testEquals() {
        OneToManyMap map0 = new OneToManyMap();
        map0.put( s0, s1 );
        map0.put( s0, s2 );
        map0.put( s3, s4 );
        OneToManyMap map1 = new OneToManyMap();
        map1.put( s3, s4 );
        map1.put( s0, s1 );
        map1.put( s0, s2 );
        OneToManyMap map2 = new OneToManyMap();
        map2.put( s0, s2 );
        map2.put( s3, s4 );
        
        assertTrue( map0.equals( map1 ) );
        assertTrue( map1.equals( map0 ) );
        assertTrue( map0.hashCode() == map1.hashCode() );
        
        assertFalse( map0.equals( map2 ));
        assertFalse( map2.equals( map0 ));
    }
    
    public void testGet() {
        OneToManyMap map0 = new OneToManyMap();
        assertNull( map0.get( s0 ));
        map0.put( s0, s1 );
        assertEquals( s1, map0.get( s0 ));
        map0.put( s0, s2 );
        assertTrue( map0.get( s0 ).equals( s1 ) ||
                    map0.get( s0 ).equals( s2 ));
    }
    
    public void testGetAll() {
        OneToManyMap map0 = new OneToManyMap();
        Iterator i = map0.getAll(s0);
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
        OneToManyMap map0 = new OneToManyMap();
        Set keys = new HashSet();
        assertEquals( keys, map0.keySet() );
        
        map0.put( s0, s1 );
        keys.add( s0 );
        assertEquals( keys, map0.keySet() );
        
        map0.put( s2, s1 );
        keys.add( s2 );
        assertEquals( keys, map0.keySet() );   
    }
    
    public void testPutAll0() {
        OneToManyMap map0 = new OneToManyMap();
        map0.put( s0, s1 );
        map0.put( s0, s2 );
        map0.put( s3, s4 );
        
        OneToManyMap map1 = new OneToManyMap();
        map1.put( s0, s2 );
        map1.put( s3, s4 );
        map1.put( s0, s1 );

        OneToManyMap map2 = new OneToManyMap();
        map2.putAll( map1 );
        assertEquals( map0, map2 );
    }
    
    public void testPutAll1() {
        OneToManyMap map0 = new OneToManyMap();
        map0.put( s0, s1 );
        map0.put( s3, s4 );
        
        Map map1 = new HashMap();
        map1.put( s3, s4 );
        map1.put( s0, s1 );

        OneToManyMap map2 = new OneToManyMap();
        map2.putAll( map1 );
        assertEquals( map0, map2 );
    }
    
    public void testRemove0() {
        OneToManyMap map0 = new OneToManyMap();
        map0.put( s0, s1 );
        map0.put( s3, s4 );
        
        map0.remove( s0 );
        map0.remove( s3 );
        
        assertTrue( map0.isEmpty() );
    }
    
    public void testRemove1() {
        OneToManyMap map0 = new OneToManyMap();
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
        OneToManyMap map0 = new OneToManyMap();
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
        OneToManyMap map0 = new OneToManyMap();
        Set vals = new HashSet();
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


/*
@footer@
*/
