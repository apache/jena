/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            11-Sep-2003
 * Filename           $RCSfile: TestRacer.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-12-02 10:04:58 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.reasoner.dig.test;



// Imports
///////////////
import java.util.*;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.dig.DIGAdapter;

import junit.framework.*;


/**
 * <p>
 * Unit test suite for DIG reasoner interface to Racer - note <b>not</b> part of standard Jena test
 * suite, since it requires a running Racer reasoner.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version Release @release@ ($Id: TestRacer.java,v 1.3 2003-12-02 10:04:58 ian_dickinson Exp $)
 */
public class TestRacer 
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

    public void testRacerName() {
        DIGAdapter r = new DIGAdapter( OntModelSpec.OWL_DL_MEM, ModelFactory.createOntologyModel().getGraph() );
        assertEquals( "Name should be racer", "Racer", r.getDigIdentifier().getName() );
    }
    
    public void testRacerVersion() {
        DIGAdapter r = new DIGAdapter( OntModelSpec.OWL_DL_MEM, ModelFactory.createOntologyModel().getGraph() );
        assertNotNull( "Version should be non-null", r.getDigIdentifier().getVersion() );
    }
    
    public void testRacerMessage() {
        DIGAdapter r = new DIGAdapter( OntModelSpec.OWL_DL_MEM, ModelFactory.createOntologyModel().getGraph() );
        assertNotNull( "Message should be non-null", r.getDigIdentifier().getMessage() );
    }
    
    public void testRacerSupportsLanguage() {
        DIGAdapter r = new DIGAdapter( OntModelSpec.OWL_DL_MEM, ModelFactory.createOntologyModel().getGraph() );
        iteratorTest( r.getDigIdentifier().supportsLanguage(), 
                      new Object[] {"top", "bottom", "catom", "ratom", "and", "or", 
                                    "not", "some", "all", "atmost", "atleast", "inverse", "feature", "attribute", 
                                    "intmin", "intmax", "intrange", "intequals", "defined", "stringequals"} );
    }
    
    public void testRacerSupportsTell() {
        DIGAdapter r = new DIGAdapter( OntModelSpec.OWL_DL_MEM, ModelFactory.createOntologyModel().getGraph() );
        iteratorTest( r.getDigIdentifier().supportsTell(), 
                      new Object[] {"defconcept", "defrole", "deffeature", "defattribute", "defindividual", "impliesc", "equalc", 
                                    "disjoint", "impliesr", "domain", "range", "rangeint", "transitive", "functional", 
                                    "instanceof", "related", "value", "equalr", "rangestring"} );
    }
    
    public void testRacerSupportsAsk() {
        DIGAdapter r = new DIGAdapter( OntModelSpec.OWL_DL_MEM, ModelFactory.createOntologyModel().getGraph() );
        iteratorTest( r.getDigIdentifier().supportsAsk(), 
                      new Object[] {"allConceptNames", "allRoleNames", "allIndividuals", "satisfiable", "subsumes", 
                                    "disjoint", "parents", "children", "descendants", "ancestors", "equivalents", 
                                    "rparents", "rchildren", "rancestors", "rdescendants", "instances", "types", 
                                    "instance", "roleFillers", "relatedIndividuals", "toldValues", } );
    }
    
    // Internal implementation methods
    //////////////////////////////////

    /** Test that an iterator delivers the expected values */
    protected void iteratorTest( Iterator i, Object[] expected ) {
        assertNotNull( "Iterator should not be null", i );
        
        Logger logger = Logger.getLogger( getClass() );
        List expList = new ArrayList();
        for (int j = 0; j < expected.length; j++) {
            expList.add( expected[j] );
        }
        
        while (i.hasNext()) {
            Object next = i.next();
                
            // debugging
            if (!expList.contains( next )) {
                logger.debug( getName() + " - Unexpected iterator result: " + next );
            }
                
            assertTrue( "Value " + next + " was not expected as a result from this iterator ", expList.contains( next ) );
            assertTrue( "Value " + next + " was not removed from the list ", expList.remove( next ) );
        }
        
        if (!(expList.size() == 0)) {
            logger.debug( getName() + "Expected iterator results not found" );
            for (Iterator j = expList.iterator(); j.hasNext(); ) {
                logger.debug( getName() + " - missing: " + j.next() );
            }
        }
        assertEquals( "There were expected elements from the iterator that were not found", 0, expList.size() );
    }


    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
    (c) Copyright Hewlett-Packard Company 2002-2003
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
