/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: AbstractTestPrefixMapping.java,v 1.6 2003-05-30 13:50:15 chris-dollin Exp $
*/

package com.hp.hpl.jena.shared.test;

import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.graph.test.*;

import java.util.*;

/**
    Test prefix mappings - subclass this test and override getMapping() to
    deliver the prefixMapping to be tested.
    
    @author kers
*/

public abstract class AbstractTestPrefixMapping extends GraphTestBase
    {
    public AbstractTestPrefixMapping( String name )
         { super( name ); };


    abstract protected PrefixMapping getMapping();
        
    static final String crispURI = "http://crisp.nosuch.net/";
    static final String ropeURI = "scheme:rope/string#";
    static final String butterURI = "ftp://ftp.nowhere.at.all/cream#";
        
    static final String [] badNames =
        {
        "<hello>",
        "foo:bar",
        "with a space",
        "-argument"
        };
        
    public void testCheckNames()
        {
        PrefixMapping ns = getMapping();
        for (int i = 0; i < badNames.length; i += 1)
            {
            String bad = badNames[i];
            try 
                { 
                ns.setNsPrefix( bad, crispURI ); 
                fail( "'" + bad + "' is an illegal prefix and should be trapped" ); 
                }
            catch (PrefixMapping.IllegalPrefixException e) {}
            }
        }
                 
    /**
        test that a PrefixMapping maps names to URIs. The names and URIs are
        all fully distinct - overlapping names/uris are dealt with in other tests.
    */
    public void testPrefixMappingMapping()
        {
        String toast = "ftp://ftp.nowhere.not/";
        assertDiffer( "crisp and toast must differ", crispURI, toast );
    /* */
        PrefixMapping ns = getMapping();
        assertEquals( "crisp should be unset", null, ns.getNsPrefixURI( "crisp" ) );
        assertEquals( "toast should be unset", null, ns.getNsPrefixURI( "toast" ) );
        assertEquals( "butter should be unset", null, ns.getNsPrefixURI( "butter" ) );
    /* */
        ns.setNsPrefix( "crisp", crispURI );
        assertEquals( "crisp should be set", crispURI, ns.getNsPrefixURI( "crisp" ) );
        assertEquals( "toast should still be unset", null, ns.getNsPrefixURI( "toast" ) );
        assertEquals( "butter should still be unset", null, ns.getNsPrefixURI( "butter" ) );
    /* */
        ns.setNsPrefix( "toast", toast );
        assertEquals( "crisp should be set", crispURI, ns.getNsPrefixURI( "crisp" ) );
        assertEquals( "toast should be set", toast, ns.getNsPrefixURI( "toast" ) );
        assertEquals( "butter should still be unset", null, ns.getNsPrefixURI( "butter" ) );
        } 
    
    /**
       test that we can extract a proper Map from a PrefixMapping
    */
    public void testPrefixMappingMap()
        {
        PrefixMapping ns = getCrispyRope();
        Map map = ns.getNsPrefixMap();
        assertEquals( "map should have two elements", 2, map.size() );
        assertEquals( crispURI, map.get( "crisp" ) );
        assertEquals( "scheme:rope/string#", map.get( "rope" ) );
        }
    
    /**
       test that the Map returned by getNsPrefixMap does not alias (parts of)
       the secret internal map of the PrefixMapping
    */
    public void testPrefixMappingSecret()
        {
        PrefixMapping ns = getCrispyRope();
        Map map = ns.getNsPrefixMap();
    /* */
        map.put( "crisp", "with/onions" );
        map.put( "sandwich", "with/cheese" );
        assertEquals( crispURI, ns.getNsPrefixURI( "crisp" ) );
        assertEquals( ropeURI, ns.getNsPrefixURI( "rope" ) );
        assertEquals( null, ns.getNsPrefixURI( "sandwich" ) );
        }
        
    private PrefixMapping getCrispyRope()
        {
        PrefixMapping ns = getMapping();
        ns.setNsPrefix( "crisp", crispURI);
        ns.setNsPrefix( "rope", ropeURI );        
        return ns;
        }
    
    /**
       these are strings that should not change when they are prefix-expanded
       with crisp and rope as legal prefixes.
   */
   static final String [] dontChange = 
       { 
       "",
       "http://www.somedomain.something/whatever#",
       "crispy:cabbage",
       "cris:isOnInfiniteEarths",
       "rop:tangled/web",
       "roped:abseiling"
       };
    
    /**
       these are the required mappings which the test cases below should
       satisfy: an array of 2-arrays, where element 0 is the string to expand
       and element 1 is the string it should expand to. 
   */
   static final String [][] expansions =
       {
           { "crisp:pathPart", crispURI + "pathPart" },
           { "rope:partPath", ropeURI + "partPath" },
           { "crisp:path:part", crispURI + "path:part" },
       };
       
   public void testExpandPrefix()
       {
       PrefixMapping ns = getMapping();
       ns.setNsPrefix( "crisp", crispURI );
       ns.setNsPrefix( "rope", ropeURI );
   /* */
       for (int i = 0; i < dontChange.length; i += 1)
           assertEquals
               ( 
               "should be unchanged", 
               dontChange[i], 
               ns.expandPrefix( dontChange[i] ) 
               );    
   /* */
       for (int i = 0; i < expansions.length; i += 1)
           assertEquals
               ( 
               "should expand correctly", 
               expansions[i][1], 
               ns.expandPrefix( expansions[i][0] ) 
               );
       }
    
    public void testUseEasyPrefix()
       {
       testUseEasyPrefix( "prefix mapping impl", getMapping() );
       }
    
    public static void testUseEasyPrefix( String title, PrefixMapping ns )
        {
        ns.setNsPrefix( "crisp", crispURI );
        ns.setNsPrefix( "butter", butterURI );
        assertEquals( title, "", ns.usePrefix( "" ) );
        assertEquals( title, ropeURI, ns.usePrefix( ropeURI ) );
        assertEquals( title, "crisp:tail", ns.usePrefix( crispURI + "tail" ) );
        assertEquals( title, "butter:here:we:are", ns.usePrefix( butterURI + "here:we:are" ) );
        }
        
    /**
        test that we can add the maplets from another PrefixMapping without
        losing our own.
    */
    public void testAddOtherPrefixMapping()
        {
        PrefixMapping a = getMapping();
        PrefixMapping b = getMapping();
        assertFalse( "must have two diffferent maps", a == b );
        a.setNsPrefix( "crisp", crispURI );
        a.setNsPrefix( "rope", ropeURI );
        b.setNsPrefix( "butter", butterURI );
        assertEquals( null, b.getNsPrefixURI( "crisp") );
        assertEquals( null, b.getNsPrefixURI( "rope") );
        b.setNsPrefixes( a );
        checkContainsMapping( b );
        }
        
    private void checkContainsMapping( PrefixMapping b )
        {
        assertEquals( crispURI, b.getNsPrefixURI( "crisp") );
        assertEquals( ropeURI, b.getNsPrefixURI( "rope") );
        assertEquals( butterURI, b.getNsPrefixURI( "butter") );
        }
        
    /**
        as for testAddOtherPrefixMapping, except that it's a plain Map
        we're adding.
    */
    public void testAddMap()
        {
        PrefixMapping b = getMapping();
        Map map = new HashMap();
        map.put( "crisp", crispURI );
        map.put( "rope", ropeURI );
        b.setNsPrefix( "butter", butterURI );
        b.setNsPrefixes( map );
        checkContainsMapping( b );
        }
        
    /**
        Test that adding a new prefix mapping with the same URI as the old
        one throws that old one away.
    */
    public void testSameURI()
        {
        PrefixMapping pm = getMapping();
        pm.setNsPrefix( "crisp", crispURI );
        pm.setNsPrefix( "sharp", crispURI );
        assertEquals( null, pm.getNsPrefixURI( "crisp" ) );
        assertEquals( crispURI, pm.getNsPrefixURI( "sharp" ) );
        }
        
    /**
        Test that adding a new prefix mapping for U from another prefix
        map throws away any existing prefix for U.
    */
    public void testSameURIMaply()
        {
        PrefixMapping A = getMapping();
        PrefixMapping B = getMapping();
        A.setNsPrefix( "crisp", crispURI );
        B.setNsPrefix( "sharp", crispURI );
        A.setNsPrefixes( B );
        assertEquals( crispURI, A.getNsPrefixURI( "sharp" ) );
        assertEquals( null, A.getNsPrefixURI( "crisp" ) );
        }
    }

/*
    (c) Copyright Hewlett-Packard Company 2003
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/