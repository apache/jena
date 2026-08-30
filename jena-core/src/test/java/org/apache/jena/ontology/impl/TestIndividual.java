/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

// Package
///////////////
package org.apache.jena.ontology.impl;

// Imports
///////////////

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.Profile;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.io.StringReader;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.jena.test.JenaTestLib;

/**
 * <p>
 * Unit tests for ontology individuals
 * </p>
 */
@SuppressWarnings("removal")
public class TestIndividual extends OntTestBase
{

    static { JenaTestLib.setup(); }

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

    @Override
    public OntTestCase[] getTests() {
        return new OntTestCase[] {
            new OntTestCase( "Individual.sameAs", true, false, false ) {
                /** Note: 6/Nov/2003 - updated to use sameAs not sameIndividualAs, following changes to OWL spec */
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    OntClass A = m.createClass( NS + "A" );
                    Individual x = m.createIndividual( A );
                    Individual y = m.createIndividual( A );
                    Individual z = m.createIndividual( A );

                    x.addSameAs( y );
                    assertEquals( 1, x.getCardinality( prof.SAME_AS() ), "Cardinality should be 1" );
                    assertEquals( y, x.getSameAs(), "x should be the same as y" );
                    assertTrue( x.isSameAs( y ), "x should be the same as y" );

                    x.addSameAs( z );
                    assertEquals( 2, x.getCardinality( prof.SAME_AS() ), "Cardinality should be 2" );
                    iteratorTest( x.listSameAs(), new Object[] {z,y} );

                    x.setSameAs( z );
                    assertEquals( 1, x.getCardinality( prof.SAME_AS() ), "Cardinality should be 1" );
                    assertEquals( z, x.getSameAs(), "x should be same indiv. as z" );

                    x.removeSameAs( y );
                    assertEquals( 1, x.getCardinality( prof.SAME_AS() ), "Cardinality should be 1" );
                    x.removeSameAs( z );
                    assertEquals( 0, x.getCardinality( prof.SAME_AS() ), "Cardinality should be 0" );
                }
            },

            new OntTestCase( "Individual.hasOntClass", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) {
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    Individual x = m.createIndividual( A );

                    assertTrue( x.hasOntClass( A ) );
                    assertFalse( x.hasOntClass( B ) );
                }
            },

            new OntTestCase( "Individual.hasOntClass direct", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) {
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
                protected void ontTest( OntModel m ) {
                    OntClass A = m.createClass( NS + "A" );

                    Individual x = m.createIndividual( A );

                    assertTrue( x.hasOntClass( NS + "A" ) );
                }
            },

            new OntTestCase( "Individual.getOntClass", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) {
                    OntClass A = m.createClass( NS + "A" );
                    Individual x = m.createIndividual( A );

                    assertEquals( A, x.getOntClass() );
                }
            },

            new OntTestCase( "Individual.getOntClass direct", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) {
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
                protected void ontTest( OntModel m ) {
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    A.addSubClass( B );

                    Individual x = m.createIndividual( A );
                    x.addRDFType( B );

                    iteratorTest( x.listOntClasses( false ), new Object[] {A,B} );

                    // now check the return types
                    for (Iterator<OntClass> i = x.listOntClasses( false ); i.hasNext(); ) {
                        assertNotNull( i.next() );
                    }
                }
            },

            new OntTestCase( "Individual.listOntClasses direct", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) {
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    A.addSubClass( B );

                    Individual x = m.createIndividual( A );
                    x.addRDFType( B );

                    iteratorTest( x.listOntClasses( true ), new Object[] {B} );

                    // now check the return types
                    for (Iterator<OntClass> i = x.listOntClasses( true ); i.hasNext(); ) {
                        assertNotNull( i.next() );
                    }
                }
            },

            new OntTestCase( "Individual.addOntClass", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) {
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    A.addSubClass( B );

                    Individual x = m.createIndividual( A );

                    iteratorTest( x.listOntClasses( false ), new Object[] {A} );

                    // add a class
                    x.addOntClass( B );

                    // test again
                    iteratorTest( x.listOntClasses( false ), new Object[] {A,B} );
                    for (Iterator<OntClass> i = x.listOntClasses( false ); i.hasNext(); ) {
                        assertNotNull( i.next() );
                    }
                }
            },

            new OntTestCase( "Individual.setOntClass", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) {
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    A.addSubClass( B );

                    Individual x = m.createIndividual( A );

                    iteratorTest( x.listOntClasses( false ), new Object[] {A} );

                    // replace the class
                    x.setOntClass( B );

                    // test again
                    iteratorTest( x.listOntClasses( false ), new Object[] {B} );
                    for (Iterator<OntClass> i = x.listOntClasses( false ); i.hasNext(); ) {
                        assertNotNull( i.next() );
                    }
                }
            },

            new OntTestCase( "Individual.removeOntClass", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) {
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
                protected void ontTest( OntModel m ) {
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
                protected void ontTest( OntModel m ) {
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
                    assertEquals( "a_label", x.getLabel( null), "Label on resource x" );
                    assertEquals( "a_label", x.getLabel( "" ), "Label on resource x" );
                    assertSame( null, x.getLabel( "fr" ), "fr label on resource x" );
                }
            },

            new OntTestCase( "OntResource.isIndividual 1", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) {
                    OntModel defModel = ModelFactory.createOntologyModel();
                    OntClass c = defModel.createClass( "http://example.com/test#A" );
                    Individual i = c.createIndividual();
                    assertTrue( i.isIndividual(), "i should be an individual" );
                }
            },
            /** User report of builtin classes showing up as individuals */
            new OntTestCase( "OntResource.isIndividual 1", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) {
                    String NS = "http://jena.hpl.hp.com/example#";
                    m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

                    OntClass c1 = m.createClass(NS + "C1");

                    for (Iterator<OntClass> it = m.listClasses(); it.hasNext(); ) {
                        OntClass ontClass = it.next();
                        assertFalse( ontClass.isIndividual(), ontClass.getLocalName() + "should not be an individual" );
                    }
                }
            },

            new OntTestCase( "OntResource.isIndividual 1", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) {
                    String NS = "http://jena.hpl.hp.com/example#";
                    m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_MICRO_RULE_INF);

                    OntClass c1 = m.createClass(NS + "C1");

                    for (Iterator<OntClass> it=m.listClasses(); it.hasNext(); ) {
                        OntClass ontClass = it.next();
                        assertFalse( ontClass.isIndividual(), ontClass.getLocalName() + "should not be an individual" );
                    }
                }
            },

            /** Edge case - suppose we imagine that user has materialised results of offline inference */
            new OntTestCase( "OntResource.isIndividual 1", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) {
                    String NS = "http://jena.hpl.hp.com/example#";
                    m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);

                    OntClass c1 = m.createClass(NS + "C1");
                    m.add( OWL.Class, RDF.type, OWL.Class );

                    for (Iterator<OntClass> it = m.listClasses(); it.hasNext(); ) {
                        OntClass ontClass = it.next();
                        assertFalse( ontClass.isIndividual(), ontClass.getLocalName() + " should not be an individual" );
                    }
                }
            },

            new OntTestCase( "OntResource.isIndividual 1", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) {
                    String NS = "http://jena.hpl.hp.com/example#";
                    m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);

                    OntClass c1 = m.createClass(NS + "C1");
                    m.add( OWL.Class, RDF.type, RDFS.Class );

                    for (Iterator<OntClass> it = m.listClasses(); it.hasNext(); ) {
                        OntClass ontClass = it.next();
                        assertFalse( ontClass.isIndividual(), ontClass.getLocalName() + " should not be an individual" );
                    }
                }
            },

            new OntTestCase( "OntResource.isIndividual 1", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) {
                    String NS = "http://jena.hpl.hp.com/example#";
                    m = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);

                    OntClass c1 = m.createClass(NS + "C1");
                    m.add( RDFS.Class, RDF.type, RDFS.Class );

                    for (Iterator<OntClass> it = m.listClasses(); it.hasNext(); ) {
                        OntClass ontClass = it.next();
                        assertFalse( ontClass.isIndividual(), ontClass.getLocalName() + " should not be an individual" );
                    }
                }
            },

            /** But we do allow punning */
            new OntTestCase( "OntResource.isIndividual 1", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) {
                    String NS = "http://jena.hpl.hp.com/example#";
                    m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

                    OntClass punned = m.createClass(NS + "C1");
                    OntClass c2 = m.createClass(NS + "C2");
                    m.add( punned, RDF.type, c2 ); // punned is a class and and instance of c2

                    assertFalse( c2.isIndividual(), "should not be an individual" );
                    assertTrue( punned.isIndividual(), "should be an individual" );
                }
            },

            new OntTestCase( "OntResource.isIndividual 1", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) {
                    String NS = "http://jena.hpl.hp.com/example#";
                    m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);

                    OntClass punned = m.createClass(NS + "C1");
                    OntClass c2 = m.createClass(NS + "C2");
                    m.add( punned, RDF.type, c2 ); // punned is a class and and instance of c2

                    assertFalse( c2.isIndividual(), "should not be an individual" );
                    assertTrue( punned.isIndividual(), "should be an individual" );
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
