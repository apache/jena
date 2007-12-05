/*****************************************************************************
 * Source code metadata
 *
 * Original author    ijd
 * Package            Jena2
 * Created            4 Dec 2007
 * File               TestOntTools.java
 *
 * Copyright (c) 2007 Hewlett-Packard Development Company LP
 * All rights reserved.
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl.test;


// Imports
///////////////
import junit.framework.TestCase;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.ModelFactory;
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


    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


