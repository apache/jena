/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestNamespace.java,v 1.2 2003-04-17 14:43:42 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;

import java.util.*;
import java.io.*;

import junit.framework.*;

/**
 	@author kers
*/
public class TestNamespace extends ModelTestBase
    {
    public TestNamespace( String name )
        { super( name ); }

    public static TestSuite suite()
         { return new TestSuite( TestNamespace.class ); }   

    /**
        a simple test of the prefix reader on a known file. test0014.rdf is known to
        have a namespace definition for eg and rdf, and not for spoo so we see if we
        can extract them (or not, for spoo).
    */
    public void testReadPrefixes()
        {
        Model m = ModelFactory.createDefaultModel();
        m.read( "file:testing/wg/rdf-ns-prefix-confusion/test0014.rdf" );
        Map ns = m.getNsPrefixMap(); 
        assertEquals( "namespace eg", "http://example.org/", ns.get( "eg" ) );
        assertEquals( "namespace rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#", ns.get( "rdf" ) );
        assertEquals( "not present", null, ns.get( "spoo" ) );
        }
        
    /**
        a horridly written test to write out a model with some known namespace
        prefixes and see if they can be read back in again.
        
        TODO tidy and abstract this - we want some more tests.
        
        TODO there's a problem: namespaces that aren't used on properties
        don't reliably get used. Maybe they shouldn't be - but it seems odd.
    */
    public void testWritePrefixes() throws IOException
        {
        Model m = ModelFactory.createDefaultModel();
        ModelCom.addNamespaces( m, makePrefixes( "fred=ftp://net.fred.org/;spoo=http://spoo.net/" ) );
        File f =  File.createTempFile( "hedgehog", ".rdf" );
        m.add( statement( m, "http://spoo.net/S http://spoo.net/P http://spoo.net/O" ) );
        m.add( statement( m, "http://spoo.net/S ftp://net.fred.org/P http://spoo.net/O" ) );
        m.write( new FileWriter( f ) );
    /* */
        Model m2 = ModelFactory.createDefaultModel();
        m2.read( "file:" + f.getAbsolutePath() );
        Map ns = m2.getNsPrefixMap();
        assertEquals( "namespace spoo", "http://spoo.net/", ns.get( "spoo" ) );
        assertEquals( "namespace fred", "ftp://net.fred.org/", ns.get( "fred" ) );
    /* */
        f.deleteOnExit();
        }
    
    /**
        turn a semi-separated set of P=U definitions into a namespace map.
    */
    private Map makePrefixes( String prefixes )
        {
        Map result = new HashMap();
        StringTokenizer st = new StringTokenizer( prefixes, ";" );
        while (st.hasMoreTokens())
            {
            String def = st.nextToken();
            // System.err.println( "| def is " + def );
            int eq = def.indexOf( '=' );
            result.put( def.substring( 0, eq ), set( def.substring( eq + 1 ) ) );
            }
        // result.put( "spoo", set( "http://spoo.net/" ) );
        return result;
        }
        
    /**
        make a single-element set.
        @param element the single element to contain
        @return a set whose only element == element
    */
    private Set set( String element )
        {
        Set s = new HashSet();
        s.add( element );
        return s;
        }
        
    /**
        test that a PrefixMapping maps names to URIs. The names and URIs are
        all fully distinct - overlapping names/uris are dealt with in other tests.
    */
    public void testPrefixMappingMapping()
        {
        String crisp = "http://crisp.nosuch.net/";
        String toast = "ftp://ftp.nowhere.not/";
        assertDiffer( "crisp and toast must differ", crisp, toast );
    /* */
        PrefixMapping ns = new PrefixMappingImpl();
        assertEquals( "crisp should be unset", null, ns.getNsPrefixURI( "crisp" ) );
        assertEquals( "toast should be unset", null, ns.getNsPrefixURI( "toast" ) );
        assertEquals( "butter should be unset", null, ns.getNsPrefixURI( "butter" ) );
    /* */
        ns.setNsPrefix( "crisp", crisp );
        assertEquals( "crisp should be set", crisp, ns.getNsPrefixURI( "crisp" ) );
        assertEquals( "toast should still be unset", null, ns.getNsPrefixURI( "toast" ) );
        assertEquals( "butter should still be unset", null, ns.getNsPrefixURI( "butter" ) );
    /* */
        ns.setNsPrefix( "toast", toast );
        assertEquals( "crisp should be set", crisp, ns.getNsPrefixURI( "crisp" ) );
        assertEquals( "toast should be set", toast, ns.getNsPrefixURI( "toast" ) );
        assertEquals( "butter should still be unset", null, ns.getNsPrefixURI( "butter" ) );
        }
        
    public void testPrefixMappingMap()
        {
        PrefixMapping ns = new PrefixMappingImpl();
        ns.setNsPrefix( "crisp", "crisp.nosuch.net" );
        ns.setNsPrefix( "rope", "scheme:rope/string#" );
        Map map = ns.getNsPrefixMap();
        assertEquals( "map should have two elements", 2, map.size() );
        assertEquals( "", "crisp.nosuch.net", map.get( "crisp" ) );
        assertEquals( "", "scheme:rope/string#", map.get( "rope" ) );
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