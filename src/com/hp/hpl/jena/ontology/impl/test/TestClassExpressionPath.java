/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            26-Mar-2003
 * Filename           $RCSfile: TestClassExpressionPath.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-05-27 22:26:11 $
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
 * @version CVS $Id: TestClassExpressionPath.java,v 1.2 2003-05-27 22:26:11 ian_dickinson Exp $
 */
public class TestClassExpressionPath
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

    public TestClassExpressionPath( String s ) {
        super( s );
    }
    
    protected String getTestName() {
        return "TestClassExpression";
    }
    
    public static TestSuite suite() {
        return new TestClassExpressionPath( "TestClassExpression" ).getSuite();
    }
    
    
    /** Fields are testID, pathset, property, profileURI, sourceData, expected, count, valueURI, rdfTypeURI, valueLit */
    protected Object[][] psTestData() {
        return new Object[][] {
            
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
                "OWL AllValuesFromRestriction.allValuesFrom",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((AllValuesFromRestriction) m.getResource( NS + "ClassA" )
                               .as( AllValuesFromRestriction.class )).p_allValuesFrom(); 
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
                "OWL SomeValuesFromRestriction.someValuesFrom",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((SomeValuesFromRestriction) m.getResource( NS + "ClassB" )
                               .as( SomeValuesFromRestriction.class )).p_someValuesFrom(); 
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
                "OWL HasValueRestriction.hasValue",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((HasValueRestriction) m.getResource( NS + "ClassC" )
                               .as( HasValueRestriction.class )).p_hasValue(); 
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
                "OWL MinCardinalityRestriction.minCardinality",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((MinCardinalityRestriction) m.getResource( NS + "ClassD" )
                               .as( MinCardinalityRestriction.class )).p_minCardinality(); 
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
                "OWL MaxCardinalityRestriction.maxCardinality",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((MaxCardinalityRestriction) m.getResource( NS + "ClassE" )
                               .as( MaxCardinalityRestriction.class )).p_maxCardinality(); 
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
                "OWL CardinalityRestriction.cardinality",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((CardinalityRestriction) m.getResource( NS + "ClassF" )
                               .as( CardinalityRestriction.class )).p_cardinality(); 
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
                "DAML AllValuesFromRestriction.allValuesFrom",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((AllValuesFromRestriction) m.getResource( NS + "ClassA" )
                               .as( AllValuesFromRestriction.class )).p_allValuesFrom(); 
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
                "DAML SomeValuesFromRestriction.someValuesFrom",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((SomeValuesFromRestriction) m.getResource( NS + "ClassB" )
                               .as( SomeValuesFromRestriction.class )).p_someValuesFrom(); 
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
                "DAML HasValueRestriction.hasValue",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((HasValueRestriction) m.getResource( NS + "ClassC" )
                               .as( HasValueRestriction.class )).p_hasValue(); 
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
                "DAML MinCardinalityRestriction.minCardinality",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((MinCardinalityRestriction) m.getResource( NS + "ClassD" )
                               .as( MinCardinalityRestriction.class )).p_minCardinality(); 
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
                "DAML MaxCardinalityRestriction.maxCardinality",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((MaxCardinalityRestriction) m.getResource( NS + "ClassE" )
                               .as( MaxCardinalityRestriction.class )).p_maxCardinality(); 
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
            {   
                "DAML CardinalityRestriction.cardinality",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((CardinalityRestriction) m.getResource( NS + "ClassF" )
                               .as( CardinalityRestriction.class )).p_cardinality(); 
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
                        return ((IntersectionClass) m.getResource( NS + "ClassA" )
                               .as( IntersectionClass.class )).p_intersectionOf(); 
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
                        return ((UnionClass) m.getResource( NS + "ClassB" )
                               .as( UnionClass.class )).p_unionOf(); 
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
                        return ((ComplementClass) m.getResource( NS + "ClassC" )
                               .as( ComplementClass.class )).p_complementOf(); 
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
                        return ((IntersectionClass) m.getResource( NS + "ClassA" )
                               .as( IntersectionClass.class )).p_intersectionOf(); 
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
                        return ((UnionClass) m.getResource( NS + "ClassB" )
                               .as( UnionClass.class )).p_unionOf(); 
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
                        return ((ComplementClass) m.getResource( NS + "ClassC" )
                               .as( ComplementClass.class )).p_complementOf(); 
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
                        return ((IntersectionClass) m.getResource( NS + "ClassA" )
                               .as( IntersectionClass.class )).p_intersectionOf(); 
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
                        return ((UnionClass) m.getResource( NS + "ClassB" )
                               .as( UnionClass.class )).p_unionOf(); 
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
                        return ((ComplementClass) m.getResource( NS + "ClassC" )
                               .as( ComplementClass.class )).p_complementOf(); 
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


