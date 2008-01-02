/*****************************************************************************
 * Source code metadata
 *
 * Original author    ijd
 * Package            Jena2
 * Created            4 Dec 2007
 * File               TestOntTools.java
 *
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl.test;


// Imports
///////////////
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.vocabulary.OWL;


/**
 * <p>
 * Unit tests for experimental ontology tools class.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:ian.dickinson@hp.com">email</a>)
 */
public class TestOntTools
    extends TestCase
{
    // Constants
    //////////////////////////////////

    String NS = "http://example.com/test#";

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    OntModel m_model;

    OntClass m_a;
    OntClass m_b;
    OntClass m_c;
    OntClass m_d;
    OntClass m_e;
    OntClass m_f;
    OntClass m_g;
    OntClass m_top;

    // Constructors
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    /**
     * @throws java.lang.Exception
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        m_model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_MICRO_RULE_INF );
        m_a = m_model.createClass( NS + "A" );
        m_b = m_model.createClass( NS + "B" );
        m_c = m_model.createClass( NS + "C" );
        m_d = m_model.createClass( NS + "D" );
        m_e = m_model.createClass( NS + "E" );
        m_f = m_model.createClass( NS + "F" );
        m_g = m_model.createClass( NS + "G" );
        m_top = m_model.createClass( OWL.Thing.getURI() );
    }

    /**
     * Test method for {@link com.hp.hpl.jena.ontology.OntTools#indexLCA(com.hp.hpl.jena.rdf.model.Resource, com.hp.hpl.jena.ontology.OntTools.GetChildrenOp)}.
     */
    public void testIndexLCA0() {
        m_a.addSubClass( m_b );
        m_a.addSubClass( m_c );

        assertEquals( m_a, OntTools.getLCA( m_model, m_b, m_c ) );
    }

    public void testIndexLCA1() {
        m_a.addSubClass( m_b );
        m_a.addSubClass( m_c );

        assertEquals( m_a, OntTools.getLCA( m_model, m_c, m_b ) );
    }

    public void testIndexLCA2() {
        m_a.addSubClass( m_b );
        m_a.addSubClass( m_c );

        assertEquals( m_a, OntTools.getLCA( m_model, m_a, m_c ) );
    }

    public void testIndexLCA3() {
        m_a.addSubClass( m_b );
        m_a.addSubClass( m_c );

        assertEquals( m_a, OntTools.getLCA( m_model, m_b, m_a ) );
    }

    public void testIndexLCA4() {
        m_a.addSubClass( m_b );
        m_a.addSubClass( m_c );
        m_b.addSubClass( m_d );

        assertEquals( m_a, OntTools.getLCA( m_model, m_d, m_c ) );
    }

    public void testIndexLCA5() {
        m_a.addSubClass( m_b );
        m_a.addSubClass( m_c );
        m_b.addSubClass( m_d );

        assertEquals( m_a, OntTools.getLCA( m_model, m_c, m_d ) );
    }

    public void testIndexLCA6() {
        m_a.addSubClass( m_b );
        m_a.addSubClass( m_c );
        m_b.addSubClass( m_d );
        m_c.addSubClass( m_e );

        assertEquals( m_a, OntTools.getLCA( m_model, m_d, m_e ) );
    }

    public void testIndexLCA7() {
        m_a.addSubClass( m_b );
        m_a.addSubClass( m_c );
        m_b.addSubClass( m_d );
        m_c.addSubClass( m_e );

        assertEquals( m_a, OntTools.getLCA( m_model, m_e, m_d ) );
    }

    public void testIndexLCA8() {
        m_a.addSubClass( m_b );
        m_a.addSubClass( m_c );
        m_b.addSubClass( m_d );
        m_d.addSubClass( m_e );

        assertEquals( m_a, OntTools.getLCA( m_model, m_c, m_e ) );
    }

    public void testIndexLCA9() {
        m_a.addSubClass( m_b );
        m_a.addSubClass( m_c );
        m_b.addSubClass( m_d );
        m_d.addSubClass( m_e );

        assertEquals( m_a, OntTools.getLCA( m_model, m_b, m_c ) );
    }

    public void testIndexLCA10() {
        m_a.addSubClass( m_b );
        m_a.addSubClass( m_c );
        m_a.addSubClass( m_d );
        m_c.addSubClass( m_e );
        m_d.addSubClass( m_f );

        assertEquals( m_a, OntTools.getLCA( m_model, m_b, m_e ) );
    }

    public void testIndexLCA11() {
        m_a.addSubClass( m_b );
        m_a.addSubClass( m_c );
        m_a.addSubClass( m_d );
        m_c.addSubClass( m_e );
        m_d.addSubClass( m_f );

        assertEquals( m_a, OntTools.getLCA( m_model, m_b, m_f ) );
    }

    public void testIndexLCA12() {
        m_a.addSubClass( m_b );
        m_a.addSubClass( m_c );
        m_a.addSubClass( m_d );
        m_d.addSubClass( m_e );
        m_d.addSubClass( m_f );

        assertEquals( m_d, OntTools.getLCA( m_model, m_f, m_e ) );
    }

    public void testIndexLCA13() {
        m_a.addSubClass( m_b );
        m_a.addSubClass( m_c );
        m_a.addSubClass( m_d );
        m_c.addSubClass( m_e );
        m_d.addSubClass( m_e );
        m_d.addSubClass( m_f );

        assertEquals( m_d, OntTools.getLCA( m_model, m_f, m_e ) );
    }

    /** Disconnected trees */
    public void testIndexLCA14() {
        m_a.addSubClass( m_b );
        m_a.addSubClass( m_c );

        assertEquals( OWL.Thing, OntTools.getLCA( m_model, m_b, m_e ) );
        assertEquals( OWL.Thing, OntTools.getLCA( m_model, m_c, m_e ) );
        assertEquals( OWL.Thing, OntTools.getLCA( m_model, m_a, m_e ) );
    }

    /** Shortest path tests */

    public void testShortestPath0() {
        Property p = m_model.createProperty( NS + "p" );
        m_a.addProperty( p, m_b );

        testPath( OntTools.findShortestPath( m_model, m_a, m_b, Filter.any ),
                  new Property[] {p} );
    }

    public void testShortestPath1() {
        Property p = m_model.createProperty( NS + "p" );
        m_a.addProperty( p, m_b );
        m_b.addProperty( p, m_c );

        testPath( OntTools.findShortestPath( m_model, m_a, m_c, Filter.any ),
                  new Property[] {p,p} );
    }

    public void testShortestPath2() {
        Property p = m_model.createProperty( NS + "p" );
        // a - b - c
        m_a.addProperty( p, m_b );
        m_b.addProperty( p, m_c );

        // a - d - e - f
        m_a.addProperty( p, m_d );
        m_d.addProperty( p, m_e );
        m_e.addProperty( p, m_f );

        testPath( OntTools.findShortestPath( m_model, m_a, m_c, Filter.any ),
                new Property[] {p,p} );
        testPath( OntTools.findShortestPath( m_model, m_a, m_f, Filter.any ),
                new Property[] {p,p,p} );
    }

    public void testShortestPath3() {
        Property p = m_model.createProperty( NS + "p" );
        // a - b - c
        m_a.addProperty( p, m_b );
        m_b.addProperty( p, m_c );

        // a - d - e - f
        m_a.addProperty( p, m_d );
        m_d.addProperty( p, m_e );
        m_e.addProperty( p, m_f );

        testPath( OntTools.findShortestPath( m_model, m_a, m_c, new OntTools.PredicatesFilter( p ) ),
                new Property[] {p,p} );
        testPath( OntTools.findShortestPath( m_model, m_a, m_f, new OntTools.PredicatesFilter( p ) ),
                new Property[] {p,p,p} );
    }

    public void testShortestPath4() {
        Property p = m_model.createProperty( NS + "p" );
        Property q = m_model.createProperty( NS + "q" );

        // a - b - c by q
        m_a.addProperty( q, m_b );
        m_b.addProperty( q, m_c );

        // a - d - e - f by p
        m_a.addProperty( p, m_d );
        m_d.addProperty( p, m_e );
        m_e.addProperty( p, m_f );

        assertNull( OntTools.findShortestPath( m_model, m_a, m_c, new OntTools.PredicatesFilter( p ) ) );
        testPath( OntTools.findShortestPath( m_model, m_a, m_f, new OntTools.PredicatesFilter( p ) ),
                new Property[] {p,p,p} );
    }

    /** Reflexive loop is allowed */
    public void testShortestPath5() {
        Property p = m_model.createProperty( NS + "p" );
        m_a.addProperty( p, m_a );

        testPath( OntTools.findShortestPath( m_model, m_a, m_a, Filter.any ),
                  new Property[] {p} );
    }

    public void testShortestPath6() {
        Property p = m_model.createProperty( NS + "p" );
        Property q = m_model.createProperty( NS + "q" );

        // a - b - a by q
        // tests loop detection
        m_a.addProperty( q, m_b );
        m_b.addProperty( q, m_a );

        assertNull( OntTools.findShortestPath( m_model, m_a, m_c, new OntTools.PredicatesFilter( new Property[] {p,q} ) ) );
    }

    public void testShortestPath7() {
        Property p = m_model.createProperty( NS + "p" );
        Property q = m_model.createProperty( NS + "q" );

        // a - d - e - f by p and q
        m_a.addProperty( p, m_d );
        m_d.addProperty( q, m_e );
        m_d.addProperty( q, m_b );
        m_e.addProperty( p, m_f );

        testPath( OntTools.findShortestPath( m_model, m_a, m_f, new OntTools.PredicatesFilter( new Property[] {p,q} ) ),
                new Property[] {p,q,p} );
    }

    /** Find a literal target */
    public void testShortestPath8() {
        Property p = m_model.createProperty( NS + "p" );
        Property q = m_model.createProperty( NS + "q" );

        // a - d - e - f by p and q
        m_a.addProperty( p, m_d );
        m_d.addProperty( q, m_e );
        m_d.addProperty( q, "bluff" );
        m_d.addProperty( q, m_b );
        m_e.addProperty( p, m_f );
        m_f.addProperty( q, "arnie" );

        testPath( OntTools.findShortestPath( m_model, m_a, ResourceFactory.createPlainLiteral( "arnie" ),
                                             new OntTools.PredicatesFilter( new Property[] {p,q} ) ),
                new Property[] {p,q,p,q} );
    }

    /** Tests on {@link OntTools#namedHierarchyRoots(OntModel)} */

    public void testNamedHierarchyRoots0() {
        m_a.addSubClass( m_b );
        m_b.addSubClass( m_c );
        m_c.addSubClass( m_d );
        m_e.addSubClass( m_e );
        m_e.addSubClass( m_f );

        List nhr = OntTools.namedHierarchyRoots( m_model );
        assertEquals( 3, nhr.size() );
        assertTrue( nhr.contains( m_a ));
        assertTrue( nhr.contains( m_e ));
        assertTrue( nhr.contains( m_g ));
    }

    public void testNamedHierarchyRoots1() {
        m_a.addSubClass( m_b );
        m_b.addSubClass( m_c );
        m_c.addSubClass( m_d );
        m_e.addSubClass( m_e );
        m_e.addSubClass( m_f );

        OntClass anon0 = m_model.createClass();
        anon0.addSubClass( m_a );
        anon0.addSubClass( m_e );

        List nhr = OntTools.namedHierarchyRoots( m_model );
        assertEquals( 3, nhr.size() );
        assertTrue( nhr.contains( m_a ));
        assertTrue( nhr.contains( m_e ));
        assertTrue( nhr.contains( m_g ));
    }

    public void testNamedHierarchyRoots2() {
        OntClass anon0 = m_model.createClass();
        OntClass anon1 = m_model.createClass();
        anon0.addSubClass( m_a );
        anon0.addSubClass( m_e );
        anon0.addSubClass( anon1 );
        anon1.addSubClass( m_g );

        m_a.addSubClass( m_b );
        m_b.addSubClass( m_c );
        m_c.addSubClass( m_d );
        m_e.addSubClass( m_e );
        m_e.addSubClass( m_f );

        List nhr = OntTools.namedHierarchyRoots( m_model );
        assertEquals( 3, nhr.size() );
        assertTrue( nhr.contains( m_a ));
        assertTrue( nhr.contains( m_e ));
        assertTrue( nhr.contains( m_g ));
    }


    // Internal implementation methods
    //////////////////////////////////

    private void testPath( OntTools.Path path, Property[] expected ) {
        assertEquals( expected.length, path.size() );

        int i = 0;
        Iterator j = path.iterator();
        while (j.hasNext()) {
            assertEquals( "path position: " + i, expected[i], ((Statement) j.next()).getPredicate() );
            i++;
        }
    }


    //==============================================================================
    // Inner class definitions
    //==============================================================================

}




/*
    (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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

