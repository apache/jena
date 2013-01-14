/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Package
///////////////
package com.hp.hpl.jena.ontology.impl;


// Imports
///////////////
import java.io.StringReader;
import java.util.Iterator;

import junit.framework.TestSuite;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;


/**
 * <p>
 * Unit tests for ontology individuals
 * </p>
 */
public class TestIndividual
    extends OntTestBase
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////



    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    static public TestSuite suite() {
        return new TestIndividual( "TestIndividual" );
    }

    public TestIndividual( String name ) {
        super( name );
    }


    // External signature methods
    //////////////////////////////////

    @Override
    public OntTestCase[] getTests() {
        return new OntTestCase[] {
            new OntTestCase( "Individual.sameAs", true, false, false ) {
                /** Note: 6/Nov/2003 - updated to use sameAs not sameIndividualAs, following changes to OWL spec */
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntClass A = m.createClass( NS + "A" );
                    Individual x = m.createIndividual( A );
                    Individual y = m.createIndividual( A );
                    Individual z = m.createIndividual( A );

                    x.addSameAs( y );
                    assertEquals( "Cardinality should be 1", 1, x.getCardinality( prof.SAME_AS() ) );
                    assertEquals( "x should be the same as y", y, x.getSameAs() );
                    assertTrue( "x should be the same as y", x.isSameAs( y ) );

                    x.addSameAs( z );
                    assertEquals( "Cardinality should be 2", 2, x.getCardinality( prof.SAME_AS() ) );
                    iteratorTest( x.listSameAs(), new Object[] {z,y} );

                    x.setSameAs( z );
                    assertEquals( "Cardinality should be 1", 1, x.getCardinality( prof.SAME_AS() ) );
                    assertEquals( "x should be same indiv. as z", z, x.getSameAs() );

                    x.removeSameAs( y );
                    assertEquals( "Cardinality should be 1", 1, x.getCardinality( prof.SAME_AS() ) );
                    x.removeSameAs( z );
                    assertEquals( "Cardinality should be 0", 0, x.getCardinality( prof.SAME_AS() ) );
                }
            },

            new OntTestCase( "Individual.hasOntClass", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    Individual x = m.createIndividual( A );

                    assertTrue( x.hasOntClass( A ) );
                    assertFalse( x.hasOntClass( B ) );
                }
            },

            new OntTestCase( "Individual.hasOntClass direct", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    A.addSubClass( B );

                    Individual x = m.createIndividual( A );
                    x.addRDFType( B );

                    assertTrue( x.hasOntClass( A, false ) );
                    assertTrue( x.hasOntClass( B, false ) );

                    assertTrue( x.hasOntClass( A, false ) );
                    assertTrue( x.hasOntClass( B, true ) );

                }
            },

            new OntTestCase( "Individual.hasOntClass string", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
                    OntClass A = m.createClass( NS + "A" );

                    Individual x = m.createIndividual( A );

                    assertTrue( x.hasOntClass( NS + "A" ) );
                }
            },

            new OntTestCase( "Individual.getOntClass", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
                    OntClass A = m.createClass( NS + "A" );
                    Individual x = m.createIndividual( A );

                    assertEquals( A, x.getOntClass() );
                }
            },

            new OntTestCase( "Individual.getOntClass direct", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    A.addSubClass( B );

                    Individual x = m.createIndividual( A );
                    x.addRDFType( B );

                    // should never get A since it's not a direct class
                    assertEquals( B, x.getOntClass( true ) );
                }
            },

            new OntTestCase( "Individual.listOntClasses", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    A.addSubClass( B );

                    Individual x = m.createIndividual( A );
                    x.addRDFType( B );

                    iteratorTest( x.listOntClasses( false ), new Object[] {A,B} );

                    // now check the return types
                    for (Iterator<OntClass> i = x.listOntClasses( false ) ; i.hasNext(); ) {
                        assertNotNull( i.next() );
                    }
                }
            },

            new OntTestCase( "Individual.listOntClasses direct", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    A.addSubClass( B );

                    Individual x = m.createIndividual( A );
                    x.addRDFType( B );

                    iteratorTest( x.listOntClasses( true ), new Object[] {B} );

                    // now check the return types
                    for (Iterator<OntClass> i = x.listOntClasses( true ) ; i.hasNext(); ) {
                        assertNotNull( i.next() );
                    }
                }
            },

            new OntTestCase( "Individual.addOntClass", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    A.addSubClass( B );

                    Individual x = m.createIndividual( A );

                    iteratorTest( x.listOntClasses( false ), new Object[] {A} );

                    // add a class
                    x.addOntClass( B );

                    // test again
                    iteratorTest( x.listOntClasses( false ), new Object[] {A,B} );
                    for (Iterator<OntClass> i = x.listOntClasses( false ) ; i.hasNext(); ) {
                        assertNotNull( i.next() );
                    }
                }
            },

            new OntTestCase( "Individual.setOntClass", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    A.addSubClass( B );

                    Individual x = m.createIndividual( A );

                    iteratorTest( x.listOntClasses( false ), new Object[] {A} );

                    // replace the class
                    x.setOntClass( B );

                    // test again
                    iteratorTest( x.listOntClasses( false ), new Object[] {B} );
                    for (Iterator<OntClass> i = x.listOntClasses( false ) ; i.hasNext(); ) {
                        assertNotNull( i.next() );
                    }
                }
            },

            new OntTestCase( "Individual.removeOntClass", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );

                    Individual x = m.createIndividual( A );
                    x.addOntClass( B );

                    iteratorTest( x.listOntClasses( false ), new Object[] {A,B} );

                    x.removeOntClass( A );
                    iteratorTest( x.listOntClasses( false ), new Object[] {B} );

                    x.removeOntClass( A );
                    iteratorTest( x.listOntClasses( false ), new Object[] {B} );

                    x.removeOntClass( B );
                    iteratorTest( x.listOntClasses( false ), new Object[] {} );
                }
            },

            new OntTestCase( "Individual.canAs", true, true, false ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
                    OntClass A = m.createClass( NS + "A" );
                    Resource r = m.createResource( NS + "r" );
                    Resource s = m.createResource( NS + "s" );

                    m.add( r, RDF.type, A );
                    assertTrue( r.canAs( Individual.class ) );
                    assertTrue( s.canAs( Individual.class ) ); // does not have to have an rdf:type to be an Individual

                    Property p = m.createDatatypeProperty(NS + "p");
                    m.add( r, p, m.createTypedLiteral( 42 ));
                    assertFalse( r.getProperty( p ).getObject().canAs(Individual.class));
                }
            },

            /** Test case for SF bug 945436 - a xml:lang='' in the dataset causes string index exception in getLabel() */
            new OntTestCase( "Individual.canAs", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
                    String SOURCE=
                        "<?xml version='1.0'?>" +
                        "<!DOCTYPE owl [" +
                        "      <!ENTITY rdf  'http://www.w3.org/1999/02/22-rdf-syntax-ns#' >" +
                        "      <!ENTITY rdfs 'http://www.w3.org/2000/01/rdf-schema#' >" +
                        "      <!ENTITY xsd  'http://www.w3.org/2001/XMLSchema#' >" +
                        "      <!ENTITY owl  'http://www.w3.org/2002/07/owl#' >" +
                        "      <!ENTITY dc   'http://purl.org/dc/elements/1.1/' >" +
                        "      <!ENTITY base  'http://jena.hpl.hp.com/test' >" +
                        "    ]>" +
                        "<rdf:RDF xmlns:owl ='&owl;' xmlns:rdf='&rdf;' xmlns:rdfs='&rdfs;' xmlns:dc='&dc;' xmlns='&base;#' xml:base='&base;'>" +
                        "  <C rdf:ID='x'>" +
                        "    <rdfs:label xml:lang=''>a_label</rdfs:label>" +
                        "  </C>" +
                        "  <owl:Class rdf:ID='C'>" +
                        "  </owl:Class>" +
                        "</rdf:RDF>";
                    m.read( new StringReader( SOURCE ), null );
                    Individual x = m.getIndividual( "http://jena.hpl.hp.com/test#x" );
                    assertEquals( "Label on resource x", "a_label", x.getLabel( null) );
                    assertEquals( "Label on resource x", "a_label", x.getLabel( "" ) );
                    assertSame( "fr label on resource x", null, x.getLabel( "fr" ) );
                }
            },

            new OntTestCase( "OntResource.isIndividual 1", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
                    OntModel defModel = ModelFactory.createOntologyModel();
                    OntClass c = defModel.createClass( "http://example.com/test#A" );
                    Individual i = c.createIndividual();
                    assertTrue( "i should be an individual", i.isIndividual() );
                }
            },
            /** User report of builtin classes showing up as individuals */
            new OntTestCase( "OntResource.isIndividual 1", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
                    String NS = "http://jena.hpl.hp.com/example#";
                    m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

                    OntClass c1 = m.createClass(NS + "C1");

                    for (Iterator<OntClass> it = m.listClasses(); it.hasNext(); ) {
                        OntClass ontClass = it.next();
                        assertFalse( ontClass.getLocalName() + "should not be an individual", ontClass.isIndividual() );
                    }
                }
            },

            new OntTestCase( "OntResource.isIndividual 1", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
                    String NS = "http://jena.hpl.hp.com/example#";
                    m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_MICRO_RULE_INF);

                    OntClass c1 = m.createClass(NS + "C1");

                    for (Iterator<OntClass> it=m.listClasses(); it.hasNext(); ) {
                        OntClass ontClass = it.next();
                        assertFalse( ontClass.getLocalName() + "should not be an individual", ontClass.isIndividual() );
                    }
                }
            },

            /** Edge case - suppose we imagine that user has materialised results of offline inference */
            new OntTestCase( "OntResource.isIndividual 1", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
                    String NS = "http://jena.hpl.hp.com/example#";
                    m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);

                    OntClass c1 = m.createClass(NS + "C1");
                    m.add( OWL.Class, RDF.type, OWL.Class );

                    for (Iterator<OntClass> it = m.listClasses(); it.hasNext(); ) {
                        OntClass ontClass = it.next();
                        assertFalse( ontClass.getLocalName() + " should not be an individual", ontClass.isIndividual() );
                    }
                }
            },

            new OntTestCase( "OntResource.isIndividual 1", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
                    String NS = "http://jena.hpl.hp.com/example#";
                    m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);

                    OntClass c1 = m.createClass(NS + "C1");
                    m.add( OWL.Class, RDF.type, RDFS.Class );

                    for (Iterator<OntClass> it = m.listClasses(); it.hasNext(); ) {
                        OntClass ontClass = it.next();
                        assertFalse( ontClass.getLocalName() + " should not be an individual", ontClass.isIndividual() );
                    }
                }
            },

            new OntTestCase( "OntResource.isIndividual 1", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
                    String NS = "http://jena.hpl.hp.com/example#";
                    m = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);

                    OntClass c1 = m.createClass(NS + "C1");
                    m.add( RDFS.Class, RDF.type, RDFS.Class );

                    for (Iterator<OntClass> it = m.listClasses(); it.hasNext(); ) {
                        OntClass ontClass = it.next();
                        assertFalse( ontClass.getLocalName() + " should not be an individual", ontClass.isIndividual() );
                    }
                }
            },

            /** But we do allow punning */
            new OntTestCase( "OntResource.isIndividual 1", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
                    String NS = "http://jena.hpl.hp.com/example#";
                    m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

                    OntClass punned = m.createClass(NS + "C1");
                    OntClass c2 = m.createClass(NS + "C2");
                    m.add( punned, RDF.type, c2 ); // punned is a class and and instance of c2

                    assertFalse( "should not be an individual", c2.isIndividual() );
                    assertTrue(  "should be an individual", punned.isIndividual() );
                }
            },

            new OntTestCase( "OntResource.isIndividual 1", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
                    String NS = "http://jena.hpl.hp.com/example#";
                    m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);

                    OntClass punned = m.createClass(NS + "C1");
                    OntClass c2 = m.createClass(NS + "C2");
                    m.add( punned, RDF.type, c2 ); // punned is a class and and instance of c2

                    assertFalse( "should not be an individual", c2.isIndividual() );
                    assertTrue(  "should be an individual", punned.isIndividual() );
                }
            }


        };
    }

    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
