/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            26-Mar-2003
 * Filename           $RCSfile: TestClassExpression.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-03-31 20:37:33 $
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
 * @version CVS $Id: TestClassExpression.java,v 1.4 2003-03-31 20:37:33 ian_dickinson Exp $
 */
public class TestClassExpression
    extends OntTestCase 
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////



    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    public TestClassExpression( String s ) {
        super( s );
    }
    
    protected String getTestName() {
        return "TestClassExpression";
    }
    
    public static TestSuite suite() {
        return new TestClassExpression( "TestClassExpression" ).getSuite();
    }
    
    
    /** Fields are testID, pathset, property, profileURI, sourceData, expected, count, valueURI, rdfTypeURI, valueLit */
    protected Object[][] testData() {
        return new Object[][] {
            {   
                "OWL OntClass.subClassOf",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((OntClass) m.getResource( NS + "ClassA" )
                               .as( OntClass.class )).p_subClassOf(); } 
                },
                OWL.subClassOf,
                ProfileRegistry.OWL_LANG,
                "file:testing/ontology/owl/ClassExpression/test.rdf",
                T,
                new Integer( 1 ),
                NS + "ClassB",
                null,
                null
            },
            {   
                "OWL OntClass.equivalentClass",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((OntClass) m.getResource( NS + "ClassA" )
                               .as( OntClass.class )).p_equivalentClass(); } 
                },
                OWL.equivalentClass,
                ProfileRegistry.OWL_LANG,
                "file:testing/ontology/owl/ClassExpression/test.rdf",
                T,
                new Integer( 1 ),
                NS + "ClassC",
                null,
                null
            },
            {   
                "OWL OntClass.disjointWith",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((OntClass) m.getResource( NS + "ClassA" )
                               .as( OntClass.class )).p_disjointWith(); } 
                },
                OWL.disjointWith,
                ProfileRegistry.OWL_LANG,
                "file:testing/ontology/owl/ClassExpression/test.rdf",
                T,
                new Integer( 1 ),
                NS + "ClassD",
                null,
                null
            },
            {   
                "OWL Lite OntClass.subClassOf",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((OntClass) m.getResource( NS + "ClassA" )
                               .as( OntClass.class )).p_subClassOf(); } 
                },
                OWL.subClassOf,
                ProfileRegistry.OWL_LITE_LANG,
                "file:testing/ontology/owl/ClassExpression/test.rdf",
                T,
                new Integer( 1 ),
                NS + "ClassB",
                null,
                null
            },
            {   
                "OWL Lite OntClass.equivalentClass",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((OntClass) m.getResource( NS + "ClassA" )
                               .as( OntClass.class )).p_equivalentClass(); } 
                },
                OWL.equivalentClass,
                ProfileRegistry.OWL_LITE_LANG,
                "file:testing/ontology/owl/ClassExpression/test.rdf",
                T,
                new Integer( 1 ),
                NS + "ClassC",
                null,
                null
            },
            {   
                "OWL Lite OntClass.disjointWith",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((OntClass) m.getResource( NS + "ClassA" )
                               .as( OntClass.class )).p_disjointWith(); } 
                },
                OWL.disjointWith,
                ProfileRegistry.OWL_LITE_LANG,
                "file:testing/ontology/owl/ClassExpression/test.rdf",
                F,
                null,
                null,
                null,
                null
            },
            {   
                "DAML OntClass.subClassOf",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((OntClass) m.getResource( NS + "ClassA" )
                               .as( OntClass.class )).p_subClassOf(); } 
                },
                DAML_OIL.subClassOf,
                ProfileRegistry.DAML_LANG,
                "file:testing/ontology/daml/ClassExpression/test.rdf",
                T,
                new Integer( 1 ),
                NS + "ClassB",
                null,
                null
            },
            {   
                "DAML OntClass.equivalentClass",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((OntClass) m.getResource( NS + "ClassA" )
                               .as( OntClass.class )).p_equivalentClass(); } 
                },
                DAML_OIL.sameClassAs,
                ProfileRegistry.DAML_LANG,
                "file:testing/ontology/daml/ClassExpression/test.rdf",
                T,
                new Integer( 1 ),
                NS + "ClassC",
                null,
                null
            },
            {   
                "DAML OntClass.disjointWith",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((OntClass) m.getResource( NS + "ClassA" )
                               .as( OntClass.class )).p_disjointWith(); } 
                },
                DAML_OIL.disjointWith,
                ProfileRegistry.DAML_LANG,
                "file:testing/ontology/daml/ClassExpression/test.rdf",
                T,
                new Integer( 1 ),
                NS + "ClassD",
                null,
                null
            },
            
            // Enumerated class
            {   
                "OWL ClassDescription.oneOf",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((OntClass) m.getResource( NS + "ClassA" )
                               .as( OntClass.class )).p_oneOf(); 
                    } 
                },
                OWL.oneOf,
                ProfileRegistry.OWL_LANG,
                "file:testing/ontology/owl/ClassExpression/test-enum.rdf",
                T,
                new Integer( 1 ),
                null,
                RDF.List,
                null
            },
            {   
                "DAML ClassDescription.oneOf",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((OntClass) m.getResource( NS + "ClassA" )
                               .as( OntClass.class )).p_oneOf(); 
                    } 
                },
                DAML_OIL.oneOf,
                ProfileRegistry.DAML_LANG,
                "file:testing/ontology/daml/ClassExpression/test-enum.rdf",
                T,
                new Integer( 1 ),
                null,
                DAML_OIL.List,
                null
            },
            
            // Restrictions
            {   
                "OWL Restriction.onProperty",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((Restriction) m.getResource( NS + "ClassA" )
                               .as( Restriction.class )).p_onProperty(); 
                    } 
                },
                OWL.onProperty,
                ProfileRegistry.OWL_LANG,
                "file:testing/ontology/owl/ClassExpression/test-restriction.rdf",
                T,
                new Integer( 1 ),
                NS + "p",
                null,
                null
            },
            {   
                "OWL Restriction.allValuesFrom",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((Restriction) m.getResource( NS + "ClassA" )
                               .as( Restriction.class )).p_allValuesFrom(); 
                    } 
                },
                OWL.allValuesFrom,
                ProfileRegistry.OWL_LANG,
                "file:testing/ontology/owl/ClassExpression/test-restriction.rdf",
                T,
                new Integer( 1 ),
                NS + "ClassB",
                null,
                null
            },
            {   
                "OWL Restriction.someValuesFrom",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((Restriction) m.getResource( NS + "ClassB" )
                               .as( Restriction.class )).p_someValuesFrom(); 
                    } 
                },
                OWL.someValuesFrom,
                ProfileRegistry.OWL_LANG,
                "file:testing/ontology/owl/ClassExpression/test-restriction.rdf",
                T,
                new Integer( 1 ),
                NS + "ClassC",
                null,
                null
            },
            {   
                "OWL Restriction.hasValue",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((Restriction) m.getResource( NS + "ClassC" )
                               .as( Restriction.class )).p_hasValue(); 
                    } 
                },
                OWL.hasValue,
                ProfileRegistry.OWL_LANG,
                "file:testing/ontology/owl/ClassExpression/test-restriction.rdf",
                T,
                new Integer( 1 ),
                null,
                null,
                new Integer( 42 )
            },
            {   
                "OWL Restriction.minCardinality",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((Restriction) m.getResource( NS + "ClassD" )
                               .as( Restriction.class )).p_minCardinality(); 
                    } 
                },
                OWL.minCardinality,
                ProfileRegistry.OWL_LANG,
                "file:testing/ontology/owl/ClassExpression/test-restriction.rdf",
                T,
                new Integer( 1 ),
                null,
                null,
                new Long( 1 )
            },
            {   
                "OWL Restriction.maxCardinality",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((Restriction) m.getResource( NS + "ClassE" )
                               .as( Restriction.class )).p_maxCardinality(); 
                    } 
                },
                OWL.maxCardinality,
                ProfileRegistry.OWL_LANG,
                "file:testing/ontology/owl/ClassExpression/test-restriction.rdf",
                T,
                new Integer( 1 ),
                null,
                null,
                new Long( 2 )
            },
            {   
                "OWL Restriction.cardinality",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((Restriction) m.getResource( NS + "ClassF" )
                               .as( Restriction.class )).p_cardinality(); 
                    } 
                },
                OWL.cardinality,
                ProfileRegistry.OWL_LANG,
                "file:testing/ontology/owl/ClassExpression/test-restriction.rdf",
                T,
                new Integer( 1 ),
                null,
                null,
                new Long( 0 )
            },
            {   
                "DAML Restriction.onProperty",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((Restriction) m.getResource( NS + "ClassA" )
                               .as( Restriction.class )).p_onProperty(); 
                    } 
                },
                DAML_OIL.onProperty,
                ProfileRegistry.DAML_LANG,
                "file:testing/ontology/daml/ClassExpression/test-restriction.rdf",
                T,
                new Integer( 1 ),
                NS + "p",
                null,
                null
            },
            {   
                "DAML Restriction.allValuesFrom",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((Restriction) m.getResource( NS + "ClassA" )
                               .as( Restriction.class )).p_allValuesFrom(); 
                    } 
                },
                DAML_OIL.toClass,
                ProfileRegistry.DAML_LANG,
                "file:testing/ontology/daml/ClassExpression/test-restriction.rdf",
                T,
                new Integer( 1 ),
                NS + "ClassB",
                null,
                null
            },
            {   
                "DAML Restriction.someValuesFrom",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((Restriction) m.getResource( NS + "ClassB" )
                               .as( Restriction.class )).p_someValuesFrom(); 
                    } 
                },
                DAML_OIL.hasClass,
                ProfileRegistry.DAML_LANG,
                "file:testing/ontology/daml/ClassExpression/test-restriction.rdf",
                T,
                new Integer( 1 ),
                NS + "ClassC",
                null,
                null
            },
            {   
                "DAML Restriction.hasValue",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((Restriction) m.getResource( NS + "ClassC" )
                               .as( Restriction.class )).p_hasValue(); 
                    } 
                },
                DAML_OIL.hasValue,
                ProfileRegistry.DAML_LANG,
                "file:testing/ontology/daml/ClassExpression/test-restriction.rdf",
                T,
                new Integer( 1 ),
                null,
                null,
                new Integer( 42 )
            },
            {   
                "DAML Restriction.minCardinality",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((Restriction) m.getResource( NS + "ClassD" )
                               .as( Restriction.class )).p_minCardinality(); 
                    } 
                },
                DAML_OIL.minCardinality,
                ProfileRegistry.DAML_LANG,
                "file:testing/ontology/daml/ClassExpression/test-restriction.rdf",
                T,
                new Integer( 1 ),
                null,
                null,
                new Long( 1 )
            },
            {   
                "DAML Restriction.maxCardinality",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((Restriction) m.getResource( NS + "ClassE" )
                               .as( Restriction.class )).p_maxCardinality(); 
                    } 
                },
                DAML_OIL.maxCardinality,
                ProfileRegistry.DAML_LANG,
                "file:testing/ontology/daml/ClassExpression/test-restriction.rdf",
                T,
                new Integer( 1 ),
                null,
                null,
                new Long( 2 )
            },
            {   
                "DAML Restriction.cardinality",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((Restriction) m.getResource( NS + "ClassF" )
                               .as( Restriction.class )).p_cardinality(); 
                    } 
                },
                DAML_OIL.cardinality,
                ProfileRegistry.DAML_LANG,
                "file:testing/ontology/daml/ClassExpression/test-restriction.rdf",
                T,
                new Integer( 1 ),
                null,
                null,
                new Long( 0 )
            },
            
            // Boolean class expressions
            {   
                "OWL ClassDescription.intersectionOf",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((OntClass) m.getResource( NS + "ClassA" )
                               .as( OntClass.class )).p_intersectionOf(); 
                    } 
                },
                OWL.intersectionOf,
                ProfileRegistry.OWL_LANG,
                "file:testing/ontology/owl/ClassExpression/test-boolean.rdf",
                T,
                new Integer( 1 ),
                null,
                RDF.List,
                null
            },
            {   
                "OWL ClassDescription.unionOf",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((OntClass) m.getResource( NS + "ClassB" )
                               .as( OntClass.class )).p_unionOf(); 
                    } 
                },
                OWL.unionOf,
                ProfileRegistry.OWL_LANG,
                "file:testing/ontology/owl/ClassExpression/test-boolean.rdf",
                T,
                new Integer( 1 ),
                null,
                RDF.List,
                null
            },
            {   
                "OWL ClassDescription.complementOf",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((OntClass) m.getResource( NS + "ClassC" )
                               .as( OntClass.class )).p_complementOf(); 
                    } 
                },
                OWL.complementOf,
                ProfileRegistry.OWL_LANG,
                "file:testing/ontology/owl/ClassExpression/test-boolean.rdf",
                T,
                new Integer( 1 ),
                NS + "ClassZ",
                null,
                null
            },
            {   
                "OWL Lite ClassDescription.intersectionOf",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((OntClass) m.getResource( NS + "ClassA" )
                               .as( OntClass.class )).p_intersectionOf(); 
                    } 
                },
                OWL.intersectionOf,
                ProfileRegistry.OWL_LITE_LANG,
                "file:testing/ontology/owl/ClassExpression/test-boolean.rdf",
                T,
                new Integer( 1 ),
                null,
                RDF.List,
                null
            },
            {   
                "OWL Lite ClassDescription.unionOf",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((OntClass) m.getResource( NS + "ClassB" )
                               .as( OntClass.class )).p_unionOf(); 
                    } 
                },
                OWL.unionOf,
                ProfileRegistry.OWL_LITE_LANG,
                "file:testing/ontology/owl/ClassExpression/test-boolean.rdf",
                F,
                null,
                null,
                null,
                null
            },
            {   
                "OWL Lite ClassDescription.complementOf",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((OntClass) m.getResource( NS + "ClassC" )
                               .as( OntClass.class )).p_complementOf(); 
                    } 
                },
                OWL.complementOf,
                ProfileRegistry.OWL_LITE_LANG,
                "file:testing/ontology/owl/ClassExpression/test-boolean.rdf",
                F,
                null,
                null,
                null,
                null
            },
            {   
                "DAML ClassDescription.intersectionOf",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((OntClass) m.getResource( NS + "ClassA" )
                               .as( OntClass.class )).p_intersectionOf(); 
                    } 
                },
                DAML_OIL.intersectionOf,
                ProfileRegistry.DAML_LANG,
                "file:testing/ontology/daml/ClassExpression/test-boolean.rdf",
                T,
                new Integer( 1 ),
                null,
                DAML_OIL.List,
                null
            },
            {   
                "DAML ClassDescription.unionOf",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((OntClass) m.getResource( NS + "ClassB" )
                               .as( OntClass.class )).p_unionOf(); 
                    } 
                },
                DAML_OIL.unionOf,
                ProfileRegistry.DAML_LANG,
                "file:testing/ontology/daml/ClassExpression/test-boolean.rdf",
                T,
                new Integer( 1 ),
                null,
                DAML_OIL.List,
                null
            },
            {   
                "DAML ClassDescription.complementOf",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((OntClass) m.getResource( NS + "ClassC" )
                               .as( OntClass.class )).p_complementOf(); 
                    } 
                },
                DAML_OIL.complementOf,
                ProfileRegistry.DAML_LANG,
                "file:testing/ontology/daml/ClassExpression/test-boolean.rdf",
                T,
                new Integer( 1 ),
                NS + "ClassZ",
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


