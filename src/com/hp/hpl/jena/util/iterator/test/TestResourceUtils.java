/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            06-Jun-2003
 * Filename           $RCSfile: TestResourceUtils.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-08-27 13:07:55 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2002, 2003, Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.util.iterator.test;


// Imports
///////////////
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.util.*;

import junit.framework.*;

import java.util.*;


/**
 * <p>
 * Unit tests on resource utilities
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: TestResourceUtils.java,v 1.3 2003-08-27 13:07:55 andy_seaborne Exp $
 */
public class TestResourceUtils 
    extends TestCase
{
    // Constants
    //////////////////////////////////

    public static final String NS = "http://jena.hp.com/test#";
    
    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    public TestResourceUtils( String name ) {
        super( name );
    }
    
    // External signature methods
    //////////////////////////////////

    public void testMaximalLowerElements() {
        Model m = ModelFactory.createDefaultModel();
        
        Resource a = m.createResource( NS + "a" );
        Resource b = m.createResource( NS + "b" );
        Resource c = m.createResource( NS + "c" );
        Resource d = m.createResource( NS + "d" );
        
        b.addProperty( RDFS.subClassOf, a );
        c.addProperty( RDFS.subClassOf, a );
        d.addProperty( RDFS.subClassOf, c );
        d.addProperty( RDFS.subClassOf, a );
        
        List abcd = Arrays.asList( new Object[] {a,b,c,d} );
        List bcd = Arrays.asList( new Object[] {b,c,d} );
        List cd = Arrays.asList( new Object[] {c,d} );

        assertEquals( "Wrong number of remaining resources", 1, ResourceUtils.maximalLowerElements( abcd, RDFS.subClassOf, true ).size() );
        assertEquals( "Result should be a", a, ResourceUtils.maximalLowerElements( abcd, RDFS.subClassOf, true ).iterator().next() );
        assertEquals( "Wrong number of remaining resources", 2, ResourceUtils.maximalLowerElements( bcd, RDFS.subClassOf, true ).size() );
        assertEquals( "Wrong number of remaining resources", 1, ResourceUtils.maximalLowerElements( cd, RDFS.subClassOf, true ).size() );
        assertEquals( "Result should be a", c, ResourceUtils.maximalLowerElements( cd, RDFS.subClassOf, true ).iterator().next() );
    }
    
    public void testRenameResource() {
        Model m = ModelFactory.createDefaultModel();
        
        Resource a = m.createResource( NS + "a" );
        Resource b = m.createResource( NS + "b" );
        Resource c = m.createResource( NS + "c" );
        Resource d = m.createResource( NS + "d" );
        
        Property p = m.createProperty( NS, "p" );
        Property q = m.createProperty( NS, "q" );
        
        a.addProperty( p, b );
        a.addProperty( q, c );
        d.addProperty( p, a );
        d.addProperty( p, b );
        
        // now rename a to e
        Resource e = ResourceUtils.renameResource( a, NS + "e" );
        
        assertTrue( "should be no properties of a", !a.listProperties().hasNext() );
        assertEquals( "uri of a", NS + "a", a.getURI() );
        assertEquals( "uri of e", NS + "e", e.getURI() );

        assertTrue( "d should not have p a", !d.hasProperty( p, a ));
        assertTrue( "d should have p e", d.hasProperty( p, e ));
        
        assertTrue( "e should have p b", e.hasProperty( p, b ) );
        assertTrue( "e should have q c", e.hasProperty( q, c ) );
        
        assertTrue( "d p b should be unchanged", d.hasProperty( p, b ) );
        
        // now rename e to anon
        Resource anon = ResourceUtils.renameResource( e, null );
        
        assertTrue( "should be no properties of e", !e.listProperties().hasNext() );
        assertEquals( "uri of e", NS + "e", e.getURI() );
        assertTrue( "anon", anon.isAnon() );

        assertTrue( "d should not have p e", !d.hasProperty( p, e ));
        assertTrue( "d should have p anon", d.hasProperty( p, anon ));
        
        assertTrue( "anon should have p b", anon.hasProperty( p, b ) );
        assertTrue( "anon should have q c", anon.hasProperty( q, c ) );
        
        assertTrue( "d p b should be unchanged", d.hasProperty( p, b ) );
        
        // reflexive case
        Resource f = m.createResource( NS + "f" );
        f.addProperty( p, f );
        
        Resource f1 = ResourceUtils.renameResource( f, NS +"f1" );
        assertFalse( "Should be no f statements",  m.listStatements( f, null, (RDFNode) null).hasNext() );
        assertTrue( "f1 has p f1", f1.hasProperty( p, f1 ) );
    }
    
    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
    (c) Copyright 2002, 2003 Hewlett-Packard Development Company, LP
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
