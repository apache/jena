/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            26-Mar-2003
 * Filename           $RCSfile: TestProperty.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-05-23 11:13:05 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl.test;


// Imports
///////////////
import junit.framework.TestSuite;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.path.*;
import com.hp.hpl.jena.vocabulary.*;



/**
 * <p>
 * Unit test cases for the Ontology class
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: TestProperty.java,v 1.3 2003-05-23 11:13:05 ian_dickinson Exp $
 */
public class TestProperty
    extends PathTestCase 
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////



    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    public TestProperty( String s ) {
        super( s );
    }
    
    protected String getTestName() {
        return "TestProperty";
    }
    
    public static TestSuite suite() {
        return new TestProperty( "TestProperty" ).getSuite();
    }
    
    
    /** Fields are testID, pathset, property, profileURI, sourceData, expected, count, valueURI, rdfTypeURI, valueLit */
    protected Object[][] psTestData() {
        return new Object[][] {
            {   
                "OWL OntProperty.subPropertyOf",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((OntProperty) m.getResource( NS + "p" )
                               .as( OntProperty.class )).p_subPropertyOf(); } 
                },
                OWL.subPropertyOf,
                ProfileRegistry.OWL_LANG,
                "file:testing/ontology/owl/Property/test.rdf",
                T,
                new Integer( 2 ),
                null,//NS + "q",
                RDF.Property,
                null
            },
            {   
                "DAML OntProperty.subPropertyOf",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((OntProperty) m.getResource( NS + "p" )
                               .as( OntProperty.class )).p_subPropertyOf(); } 
                },
                DAML_OIL.subPropertyOf,
                ProfileRegistry.DAML_LANG,
                "file:testing/ontology/daml/Property/test.rdf",
                T,
                new Integer( 1 ),
                NS + "q",
                null,
                null
            },
            {   
                "OWL OntProperty.domain",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((OntProperty) m.getResource( NS + "p" )
                               .as( OntProperty.class )).p_domain(); } 
                },
                OWL.domain,
                ProfileRegistry.OWL_LANG,
                "file:testing/ontology/owl/Property/test.rdf",
                T,
                new Integer( 1 ),
                NS + "ClassA",
                null,
                null
            },
            {   
                "OWL OntProperty.range",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((OntProperty) m.getResource( NS + "p" )
                               .as( OntProperty.class )).p_range(); } 
                },
                OWL.range,
                ProfileRegistry.OWL_LANG,
                "file:testing/ontology/owl/Property/test.rdf",
                T,
                new Integer( 1 ),
                NS + "ClassB",
                null,
                null
            },
            {   
                "DAML OntProperty.domain",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((OntProperty) m.getResource( NS + "p" )
                               .as( OntProperty.class )).p_domain(); } 
                },
                DAML_OIL.domain,
                ProfileRegistry.DAML_LANG,
                "file:testing/ontology/daml/Property/test.rdf",
                T,
                new Integer( 1 ),
                NS + "ClassA",
                null,
                null
            },
            {   
                "DAML OntProperty.range",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((OntProperty) m.getResource( NS + "p" )
                               .as( OntProperty.class )).p_range(); } 
                },
                DAML_OIL.range,
                ProfileRegistry.DAML_LANG,
                "file:testing/ontology/daml/Property/test.rdf",
                T,
                new Integer( 1 ),
                NS + "ClassB",
                null,
                null
            },
            {   
                "OWL OntProperty.equivalentProperty",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((OntProperty) m.getResource( NS + "p" )
                               .as( OntProperty.class )).p_equivalentProperty(); } 
                },
                OWL.equivalentProperty,
                ProfileRegistry.OWL_LANG,
                "file:testing/ontology/owl/Property/test.rdf",
                T,
                new Integer( 1 ),
                NS + "r",
                null,
                null
            },
            {   
                "OWL OntProperty.inverseOf",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((OntProperty) m.getResource( NS + "p" )
                               .as( OntProperty.class )).p_inverseOf(); } 
                },
                OWL.inverseOf,
                ProfileRegistry.OWL_LANG,
                "file:testing/ontology/owl/Property/test.rdf",
                T,
                new Integer( 1 ),
                NS + "s",
                null,
                null
            },
            {   
                "DAML OntProperty.equivalentProperty",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((OntProperty) m.getResource( NS + "p" )
                               .as( OntProperty.class )).p_equivalentProperty(); } 
                },
                DAML_OIL.samePropertyAs,
                ProfileRegistry.DAML_LANG,
                "file:testing/ontology/daml/Property/test.rdf",
                T,
                new Integer( 1 ),
                NS + "r",
                null,
                null
            },
            {   
                "DAML OntProperty.inverseOf",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((OntProperty) m.getResource( NS + "p" )
                               .as( OntProperty.class )).p_inverseOf(); } 
                },
                DAML_OIL.inverseOf,
                ProfileRegistry.DAML_LANG,
                "file:testing/ontology/daml/Property/test.rdf",
                T,
                new Integer( 1 ),
                NS + "s",
                null,
                null
            },
            
      };
    }
    
    
    // External signature methods
    //////////////////////////////////

    
    
    // Internal implementation methods
    //////////////////////////////////

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


