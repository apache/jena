/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            11-Sep-2003
 * Filename           $RCSfile: TestPellet.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2006-03-22 13:53:31 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006 Hewlett-Packard Development Company, LP
 * [See end of file]
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.reasoner.dig.test;



// Imports
///////////////
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.dig.DIGAdapter;
import com.hp.hpl.jena.reasoner.test.TestUtil;

import junit.framework.*;


/**
 * <p>
 * Unit test suite for DIG reasoner interface to Pellet - note <b>not</b> part of standard Jena test
 * suite, since it requires a running Pellet reasoner.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version Release @release@ ($Id: TestPellet.java,v 1.2 2006-03-22 13:53:31 andy_seaborne Exp $)
 */
public class TestPellet
    extends TestCase
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    public void setUp() {
        // ensure the ont doc manager is in a consistent state
        OntDocumentManager.getInstance().reset( true );
    }


    public void testPelletName() {
        DIGAdapter r = new DIGAdapter( OntModelSpec.OWL_DL_MEM, ModelFactory.createOntologyModel().getGraph() );
        assertEquals( "Name should be Pellet", "Pellet", r.getDigIdentifier().getName() );
    }

    public void testPelletVersion() {
        DIGAdapter r = new DIGAdapter( OntModelSpec.OWL_DL_MEM, ModelFactory.createOntologyModel().getGraph() );
        assertNotNull( "Version should be non-null", r.getDigIdentifier().getVersion() );
    }

    public void testPelletMessage() {
        DIGAdapter r = new DIGAdapter( OntModelSpec.OWL_DL_MEM, ModelFactory.createOntologyModel().getGraph() );
        assertNotNull( "Message should be non-null", r.getDigIdentifier().getMessage() );
    }

    public void testPelletSupportsLanguage() {
        DIGAdapter r = new DIGAdapter( OntModelSpec.OWL_DL_MEM, ModelFactory.createOntologyModel().getGraph() );
        TestUtil.assertIteratorValues( this, r.getDigIdentifier().supportsLanguage(),
                      new Object[] {"top", "bottom", "catom", "ratom", "and", "or",
                                    "not", "some", "all", "atmost", "atleast", "inverse", "attribute",
                                    "intequals", "stringequals", "iset"} );
    }

    public void testPelletSupportsTell() {
        DIGAdapter r = new DIGAdapter( OntModelSpec.OWL_DL_MEM, ModelFactory.createOntologyModel().getGraph() );
        TestUtil.assertIteratorValues( this, r.getDigIdentifier().supportsTell(),
                      new Object[] {"defconcept", "defrole", "defattribute", "defindividual", "impliesc", "equalc",
                                    "disjoint", "impliesr", "domain", "range", "rangeint", "transitive", "functional",
                                    "instanceof", "related", "value", "equalr", "rangestring"} );
    }

    public void testPelletSupportsAsk() {
        DIGAdapter r = new DIGAdapter( OntModelSpec.OWL_DL_MEM, ModelFactory.createOntologyModel().getGraph() );
        TestUtil.assertIteratorValues( this, r.getDigIdentifier().supportsAsk(),
                      new Object[] {"allConceptNames", "allRoleNames", "allIndividuals", "satisfiable", "subsumes",
                                    "disjoint", "parents", "children", "descendants", "ancestors", "equivalents",
                                    "rparents", "rchildren", "rancestors", "rdescendants", "instances", "types",
                                    "instance", "roleFillers", "relatedIndividuals", "toldValues", } );
    }

    // Internal implementation methods
    //////////////////////////////////


    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
