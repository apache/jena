/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            @package@
 * Web site           @website@
 * Created            18-Nov-2003
 * Filename           $RCSfile: TestRacer.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-12-01 22:40:07 $
 *               by   $Author: ian_dickinson $
 *
 * @copyright@
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
 * @version Release @release@ ($Id: TestRacer.java,v 1.2 2003-12-01 22:40:07 ian_dickinson Exp $)
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
@footer@
*/
