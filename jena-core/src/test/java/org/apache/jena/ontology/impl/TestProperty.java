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
import java.util.List;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.jena.test.JenaTestLib;

/**
 * <p>
 * Unit test cases for the OntProperty class
 * </p>
 */
@SuppressWarnings("removal")
public class TestProperty extends OntTestBase
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
            new OntTestCase( "OntProperty.super-property", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createOntProperty( NS + "p" );
                    OntProperty q = m.createOntProperty( NS + "q" );
                    OntProperty r = m.createOntProperty( NS + "r" );

                    p.addSuperProperty( q );
                    assertEquals( 1, p.getCardinality( prof.SUB_PROPERTY_OF() ), "Cardinality should be 1" );
                    assertEquals( q, p.getSuperProperty(), "p have super-prop q" );

                    p.addSuperProperty( r );
                    assertEquals( 2, p.getCardinality( prof.SUB_PROPERTY_OF() ), "Cardinality should be 2" );
                    iteratorTest( p.listSuperProperties(), new Object[] {q, r} );

                    p.setSuperProperty( r );
                    assertEquals( 1, p.getCardinality( prof.SUB_PROPERTY_OF() ), "Cardinality should be 1" );
                    assertEquals( r, p.getSuperProperty(), "p shuold have super-prop r" );

                    p.removeSuperProperty( q );
                    assertEquals( 1, p.getCardinality( prof.SUB_PROPERTY_OF() ), "Cardinality should be 1" );
                    p.removeSuperProperty( r );
                    assertEquals( 0, p.getCardinality( prof.SUB_PROPERTY_OF() ), "Cardinality should be 0" );

                    // for symmetry with listSuperClasses(), exclude the reflexive case
                    List<? extends OntProperty> sp = p.listSuperProperties().toList();
                    assertFalse( sp.contains( p ), "super-properties should not include reflexive case" );
                }
            },
            new OntTestCase( "OntProperty.sub-property", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createOntProperty( NS + "p" );
                    OntProperty q = m.createOntProperty( NS + "q" );
                    OntProperty r = m.createOntProperty( NS + "r" );

                    p.addSubProperty( q );
                    assertEquals( 1, q.getCardinality( prof.SUB_PROPERTY_OF() ), "Cardinality should be 1" );
                    assertEquals( q, p.getSubProperty(), "p have sub-prop q" );

                    p.addSubProperty( r );
                    assertEquals( 2, q.getCardinality( prof.SUB_PROPERTY_OF() ) + r.getCardinality( prof.SUB_PROPERTY_OF() ), "Cardinality should be 2" );
                    iteratorTest( p.listSubProperties(), new Object[] {q, r} );
                    iteratorTest( q.listSuperProperties(), new Object[] {p} );
                    iteratorTest( r.listSuperProperties(), new Object[] {p} );

                    p.setSubProperty( r );
                    assertEquals( 1, q.getCardinality( prof.SUB_PROPERTY_OF() ) + r.getCardinality( prof.SUB_PROPERTY_OF() ), "Cardinality should be 1" );
                    assertEquals( r, p.getSubProperty(), "p should have sub-prop r" );

                    p.removeSubProperty( q );
                    assertTrue( p.hasSubProperty( r, false ), "Should have sub-prop r" );
                    p.removeSubProperty( r );
                    assertTrue( !p.hasSubProperty( r, false ), "Should not have sub-prop r" );
                }
            },
            new OntTestCase( "OntProperty.domain", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createOntProperty( NS + "p" );
                    OntResource a = m.getResource( NS + "a" ).as( OntResource.class );
                    OntResource b = m.getResource( NS + "b" ).as( OntResource.class );

                    p.addDomain( a );
                    assertEquals( 1, p.getCardinality( prof.DOMAIN() ), "Cardinality should be 1" );
                    assertEquals( a, p.getDomain(), "p have domain a" );

                    p.addDomain( b );
                    assertEquals( 2, p.getCardinality( prof.DOMAIN() ), "Cardinality should be 2" );
                    iteratorTest( p.listDomain(), new Object[] {a, b} );

                    p.setDomain( b );
                    assertEquals( 1, p.getCardinality( prof.DOMAIN() ), "Cardinality should be 1" );
                    assertEquals( b, p.getDomain(), "p should have domain b" );

                    p.removeDomain( a );
                    assertEquals( 1, p.getCardinality( prof.DOMAIN() ), "Cardinality should be 1" );
                    p.removeDomain( b );
                    assertEquals( 0, p.getCardinality( prof.DOMAIN() ), "Cardinality should be 0" );
                }
            },
            new OntTestCase( "OntProperty.range", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createOntProperty( NS + "p" );
                    OntResource a = m.getResource( NS + "a" ).as( OntResource.class );
                    OntResource b = m.getResource( NS + "b" ).as( OntResource.class );

                    p.addRange( a );
                    assertEquals( 1, p.getCardinality( prof.RANGE() ), "Cardinality should be 1" );
                    assertEquals( a, p.getRange(), "p have range a" );

                    p.addRange( b );
                    assertEquals( 2, p.getCardinality( prof.RANGE() ), "Cardinality should be 2" );
                    iteratorTest( p.listRange(), new Object[] {a, b} );

                    p.setRange( b );
                    assertEquals( 1, p.getCardinality( prof.RANGE() ), "Cardinality should be 1" );
                    assertEquals( b, p.getRange(), "p should have range b" );

                    p.removeRange( a );
                    assertEquals( 1, p.getCardinality( prof.RANGE() ), "Cardinality should be 1" );
                    p.removeRange( b );
                    assertEquals( 0, p.getCardinality( prof.RANGE() ), "Cardinality should be 0" );
                }
            },
            new OntTestCase( "OntProperty.equivalentProperty", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createObjectProperty( NS + "p" );
                    OntProperty q = m.createObjectProperty( NS + "q" );
                    OntProperty r = m.createObjectProperty( NS + "r" );

                    p.addEquivalentProperty( q );
                    assertEquals( 1, p.getCardinality( prof.EQUIVALENT_PROPERTY() ), "Cardinality should be 1" );
                    assertEquals( q, p.getEquivalentProperty(), "p have equivalentProperty q" );

                    p.addEquivalentProperty( r );
                    assertEquals( 2, p.getCardinality( prof.EQUIVALENT_PROPERTY() ), "Cardinality should be 2" );
                    iteratorTest( p.listEquivalentProperties(), new Object[] {q,r} );

                    p.setEquivalentProperty( r );
                    assertEquals( 1, p.getCardinality( prof.EQUIVALENT_PROPERTY() ), "Cardinality should be 1" );
                    assertEquals( r, p.getEquivalentProperty(), "p should have equivalentProperty r" );

                    p.removeEquivalentProperty( q );
                    assertEquals( 1, p.getCardinality( prof.EQUIVALENT_PROPERTY() ), "Cardinality should be 1" );
                    p.removeEquivalentProperty( r );
                    assertEquals( 0, p.getCardinality( prof.EQUIVALENT_PROPERTY() ), "Cardinality should be 0" );
                }
            },
            new OntTestCase( "OntProperty.inverseOf", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createObjectProperty( NS + "p" );
                    OntProperty q = m.createObjectProperty( NS + "q" );
                    OntProperty r = m.createObjectProperty( NS + "r" );

                    assertFalse( p.isInverseOf( q ) );
                    assertEquals( null, p.getInverseOf() );

                    p.addInverseOf( q );
                    assertEquals( 1, p.getCardinality( prof.INVERSE_OF() ), "Cardinality should be 1" );
                    assertEquals( q, p.getInverseOf(), "p should have inverse q" );
                    assertTrue( p.getInverseOf() instanceof ObjectProperty, "inverse value should be an object property" );
                    assertTrue( q.getInverse() instanceof ObjectProperty, "inverse value should be an object property" );

                    p.addInverseOf( r );
                    assertEquals( 2, p.getCardinality( prof.INVERSE_OF() ), "Cardinality should be 2" );
                    iteratorTest( p.listInverseOf(), new Object[] {q,r} );

                    p.setInverseOf( r );
                    assertEquals( 1, p.getCardinality( prof.INVERSE_OF() ), "Cardinality should be 1" );
                    assertEquals( r, p.getInverseOf(), "p should have inverse r" );

                    p.removeInverseProperty( q );
                    assertEquals( 1, p.getCardinality( prof.INVERSE_OF() ), "Cardinality should be 1" );
                    p.removeInverseProperty( r );
                    assertEquals( 0, p.getCardinality( prof.INVERSE_OF() ), "Cardinality should be 0" );
                }
            },
            new OntTestCase( "OntProperty.subproperty.fromFile", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) {
                    String lang = m_owlLang ? "owl" : "rdfs";
                    String fileName = "file:testing/ontology/" + lang + "/Property/test.rdf";
                    m.read( fileName );

                    OntProperty p = m.getProperty( NS, "p" ).as( OntProperty.class );
                    OntProperty q = m.getProperty( NS, "q" ).as( OntProperty.class );

                    iteratorTest( p.listSuperProperties(), new Object[] {q} );
                    iteratorTest( q.listSubProperties(), new Object[] {p} );
                }
            },
            new OntTestCase( "OntProperty.domain.fromFile", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) {
                    String lang = m_owlLang ? "owl" : "rdfs";
                    String fileName = "file:testing/ontology/" + lang + "/Property/test.rdf";
                    m.read( fileName );

                    OntProperty p = m.getProperty( NS, "p" ).as( OntProperty.class );
                    OntClass A = m.getResource( NS + "ClassA").as( OntClass.class);

                    assertTrue( p.hasDomain( A ), "p should have domain A" );
                }
            },
            new OntTestCase( "OntProperty.range.fromFile", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) {
                    String lang = m_owlLang ? "owl" : "rdfs";
                    String fileName = "file:testing/ontology/" + lang + "/Property/test.rdf";
                    m.read( fileName );

                    OntProperty p = m.getProperty( NS, "p" ).as( OntProperty.class );
                    OntClass B = m.getResource( NS + "ClassB").as( OntClass.class);

                    assertTrue( p.hasRange( B ), "p should have domain B" );
                }
            },
            new OntTestCase( "OntProperty.equivalentProeprty.fromFile", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    String lang = m_owlLang ? "owl" : "rdfs";
                    String fileName = "file:testing/ontology/" + lang + "/Property/test.rdf";
                    m.read( fileName );

                    OntProperty p = m.getProperty( NS, "p" ).as( OntProperty.class );
                    OntProperty r = m.getProperty( NS, "r" ).as( OntProperty.class );

                    assertTrue( p.hasEquivalentProperty( r ), "p should have equiv prop r" );
                }
            },
            new OntTestCase( "OntProperty.inversePropertyOf.fromFile", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    String lang = m_owlLang ? "owl" : "rdfs";
                    String fileName = "file:testing/ontology/" + lang + "/Property/test.rdf";
                    m.read( fileName );

                    OntProperty p = m.getProperty( NS, "p" ).as( OntProperty.class );
                    OntProperty s = m.getProperty( NS, "s" ).as( OntProperty.class );

                    assertTrue( p.isInverseOf( s ), "p should have inv prop s" );
                }
            },

            // type tests
            new OntTestCase( "OntProperty.isFunctionalProperty dt", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntProperty p = m.createDatatypeProperty( NS + "p", true );

                    assertTrue( p.isFunctionalProperty(), "isFunctionalProperty not correct" );
                    assertTrue( p.isDatatypeProperty(), "isDatatypeProperty not correct" );
                    assertTrue( !p.isObjectProperty(), "isObjectProperty not correct" );
                    assertTrue( !p.isTransitiveProperty(), "isTransitiveProperty not correct" );
                    assertTrue( !p.isInverseFunctionalProperty(), "isInverseFunctionalProperty not correct" );
                    if (m_owlLang) {
                        assertTrue( !p.isSymmetricProperty(), "isSymmetricProperty not correct" );
                    }
                }
            },
            new OntTestCase( "OntProperty.isFunctionalProperty object", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntProperty p = m.createObjectProperty( NS + "p", true );

                    assertTrue( p.isFunctionalProperty(), "isFunctionalProperty not correct" );
                    assertTrue( !p.isDatatypeProperty(), "isDatatypeProperty not correct" );
                    assertTrue( p.isObjectProperty(), "isObjectProperty not correct" );
                    assertTrue( !p.isTransitiveProperty(), "isTransitiveProperty not correct" );
                    assertTrue( !p.isInverseFunctionalProperty(), "isInverseFunctionalProperty not correct" );
                    if (m_owlLang) {
                        assertTrue( !p.isSymmetricProperty(), "isSymmetricProperty not correct" );
                    }
                }
            },
            new OntTestCase( "OntProperty.isDatatypeProperty", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntProperty p = m.createDatatypeProperty( NS + "p", false );

                    assertTrue( !p.isFunctionalProperty(), "isFunctionalProperty not correct" );
                    assertTrue( p.isDatatypeProperty(), "isDatatypeProperty not correct" );
                    assertTrue( !p.isObjectProperty(), "isObjectProperty not correct" );
                    assertTrue( !p.isTransitiveProperty(), "isTransitiveProperty not correct" );
                    assertTrue( !p.isInverseFunctionalProperty(), "isInverseFunctionalProperty not correct" );
                    if (m_owlLang) {
                        assertTrue( !p.isSymmetricProperty(), "isSymmetricProperty not correct" );
                    }
                }
            },
            new OntTestCase( "OntProperty.isObjectProperty", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntProperty p = m.createObjectProperty( NS + "p", false );

                    assertTrue( !p.isFunctionalProperty(), "isFunctionalProperty not correct" );
                    assertTrue( !p.isDatatypeProperty(), "isDatatypeProperty not correct" );
                    assertTrue( p.isObjectProperty(), "isObjectProperty not correct" );
                    assertTrue( !p.isTransitiveProperty(), "isTransitiveProperty not correct" );
                    assertTrue( !p.isInverseFunctionalProperty(), "isInverseFunctionalProperty not correct" );
                    if (m_owlLang) {
                        assertTrue( !p.isSymmetricProperty(), "isSymmetricProperty not correct" );
                    }
                }
            },
            new OntTestCase( "OntProperty.isTransitiveProperty", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntProperty p = m.createTransitiveProperty( NS + "p" );

                    assertTrue( !p.isFunctionalProperty(), "isFunctionalProperty not correct" );
                    assertTrue( !p.isDatatypeProperty(), "isDatatypeProperty not correct" );
                    assertTrue( !p.isObjectProperty(), "isObjectProperty not correct" );    // this should be true by entailment, but we have reasoning switched off
                    assertTrue( p.isTransitiveProperty(), "isTransitiveProperty not correct" );
                    assertTrue( !p.isInverseFunctionalProperty(), "isInverseFunctionalProperty not correct" );
                    if (m_owlLang) {
                        assertTrue( !p.isSymmetricProperty(), "isSymmetricProperty not correct" );
                    }
                }
            },
            new OntTestCase( "OntProperty.isInverseFunctionalProperty", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntProperty p = m.createInverseFunctionalProperty( NS + "p" );

                    assertTrue( !p.isFunctionalProperty(), "isFunctionalProperty not correct" );
                    assertTrue( !p.isDatatypeProperty(), "isDatatypeProperty not correct" );
                    assertTrue( !p.isObjectProperty(), "isObjectProperty not correct" );    // this should be true by entailment, but we have reasoning switched off
                    assertTrue( !p.isTransitiveProperty(), "isTransitiveProperty not correct" );
                    assertTrue( p.isInverseFunctionalProperty(), "isInverseFunctionalProperty not correct" );
                    if (m_owlLang) {
                        assertTrue( !p.isSymmetricProperty(), "isSymmetricProperty not correct" );
                    }
                }
            },
            new OntTestCase( "OntProperty.isSymmetricProperty", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntProperty p = m.createSymmetricProperty( NS + "p" );

                    assertTrue( !p.isFunctionalProperty(), "isFunctionalProperty not correct" );
                    assertTrue( !p.isDatatypeProperty(), "isDatatypeProperty not correct" );
                    assertTrue( !p.isObjectProperty(), "isObjectProperty not correct" );    // this should be true by entailment, but we have reasoning switched off
                    assertTrue( !p.isTransitiveProperty(), "isTransitiveProperty not correct" );
                    assertTrue( !p.isInverseFunctionalProperty(), "isInverseFunctionalProperty not correct" );
                    if (m_owlLang) {
                        assertTrue( p.isSymmetricProperty(), "isSymmetricProperty not correct" );
                    }
                }
            },
            new OntTestCase( "OntProperty.convertToFunctionalProperty", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Property pSimple = m.createProperty( NS, "p" );
                    pSimple.addProperty( RDF.type, RDF.Property );
                    OntProperty p = pSimple.as( OntProperty.class );

                    assertTrue( !p.isFunctionalProperty(), "isFunctionalProperty not correct" );
                    assertTrue( !p.isDatatypeProperty(), "isDatatypeProperty not correct" );
                    assertTrue( !p.isObjectProperty(), "isObjectProperty not correct" );
                    assertTrue( !p.isTransitiveProperty(), "isTransitiveProperty not correct" );
                    assertTrue( !p.isInverseFunctionalProperty(), "isInverseFunctionalProperty not correct" );
                    if (m_owlLang) {assertTrue( !p.isSymmetricProperty(), "isSymmetricProperty not correct" ); }

                    p = p.convertToFunctionalProperty();

                    assertTrue( p.isFunctionalProperty(), "isFunctionalProperty not correct" );
                    assertTrue( !p.isDatatypeProperty(), "isDatatypeProperty not correct" );
                    assertTrue( !p.isObjectProperty(), "isObjectProperty not correct" );
                    assertTrue( !p.isTransitiveProperty(), "isTransitiveProperty not correct" );
                    assertTrue( !p.isInverseFunctionalProperty(), "isInverseFunctionalProperty not correct" );
                    if (m_owlLang) {assertTrue( !p.isSymmetricProperty(), "isSymmetricProperty not correct" ); }
                }
            },
            new OntTestCase( "OntProperty.convertToDatatypeProperty", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Property pSimple = m.createProperty( NS, "p" );
                    pSimple.addProperty( RDF.type, RDF.Property );
                    OntProperty p = pSimple.as( OntProperty.class );

                    assertTrue( !p.isFunctionalProperty(), "isFunctionalProperty not correct" );
                    assertTrue( !p.isDatatypeProperty(), "isDatatypeProperty not correct" );
                    assertTrue( !p.isObjectProperty(), "isObjectProperty not correct" );
                    assertTrue( !p.isTransitiveProperty(), "isTransitiveProperty not correct" );
                    assertTrue( !p.isInverseFunctionalProperty(), "isInverseFunctionalProperty not correct" );
                    if (m_owlLang) {assertTrue( !p.isSymmetricProperty(), "isSymmetricProperty not correct" ); }

                    p = p.convertToDatatypeProperty();

                    assertTrue( !p.isFunctionalProperty(), "isFunctionalProperty not correct" );
                    assertTrue( p.isDatatypeProperty(), "isDatatypeProperty not correct" );
                    assertTrue( !p.isObjectProperty(), "isObjectProperty not correct" );
                    assertTrue( !p.isTransitiveProperty(), "isTransitiveProperty not correct" );
                    assertTrue( !p.isInverseFunctionalProperty(), "isInverseFunctionalProperty not correct" );
                    if (m_owlLang) {assertTrue( !p.isSymmetricProperty(), "isSymmetricProperty not correct" ); }
                }
            },
            new OntTestCase( "OntProperty.convertToObjectProperty", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Property pSimple = m.createProperty( NS, "p" );
                    pSimple.addProperty( RDF.type, RDF.Property );
                    OntProperty p = pSimple.as( OntProperty.class );

                    assertTrue( !p.isFunctionalProperty(), "isFunctionalProperty not correct" );
                    assertTrue( !p.isDatatypeProperty(), "isDatatypeProperty not correct" );
                    assertTrue( !p.isObjectProperty(), "isObjectProperty not correct" );
                    assertTrue( !p.isTransitiveProperty(), "isTransitiveProperty not correct" );
                    assertTrue( !p.isInverseFunctionalProperty(), "isInverseFunctionalProperty not correct" );
                    if (m_owlLang) {assertTrue( !p.isSymmetricProperty(), "isSymmetricProperty not correct" ); }

                    p = p.convertToObjectProperty();

                    assertTrue( !p.isFunctionalProperty(), "isFunctionalProperty not correct" );
                    assertTrue( !p.isDatatypeProperty(), "isDatatypeProperty not correct" );
                    assertTrue( p.isObjectProperty(), "isObjectProperty not correct" );
                    assertTrue( !p.isTransitiveProperty(), "isTransitiveProperty not correct" );
                    assertTrue( !p.isInverseFunctionalProperty(), "isInverseFunctionalProperty not correct" );
                    if (m_owlLang) {assertTrue( !p.isSymmetricProperty(), "isSymmetricProperty not correct" ); }
                }
            },
            new OntTestCase( "OntProperty.convertToTransitiveProperty", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Property pSimple = m.createProperty( NS, "p" );
                    pSimple.addProperty( RDF.type, RDF.Property );
                    OntProperty p = pSimple.as( OntProperty.class );

                    assertTrue( !p.isFunctionalProperty(), "isFunctionalProperty not correct" );
                    assertTrue( !p.isDatatypeProperty(), "isDatatypeProperty not correct" );
                    assertTrue( !p.isObjectProperty(), "isObjectProperty not correct" );
                    assertTrue( !p.isTransitiveProperty(), "isTransitiveProperty not correct" );
                    assertTrue( !p.isInverseFunctionalProperty(), "isInverseFunctionalProperty not correct" );
                    if (m_owlLang) {assertTrue( !p.isSymmetricProperty(), "isSymmetricProperty not correct" ); }

                    p = p.convertToTransitiveProperty();

                    assertTrue( !p.isFunctionalProperty(), "isFunctionalProperty not correct" );
                    assertTrue( !p.isDatatypeProperty(), "isDatatypeProperty not correct" );
                    assertTrue( !p.isObjectProperty(), "isObjectProperty not correct" );
                    assertTrue( p.isTransitiveProperty(), "isTransitiveProperty not correct" );
                    assertTrue( !p.isInverseFunctionalProperty(), "isInverseFunctionalProperty not correct" );
                    if (m_owlLang) {assertTrue( !p.isSymmetricProperty(), "isSymmetricProperty not correct" ); }
                }
            },
            new OntTestCase( "OntProperty.convertToInverseFunctionalProperty", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Property pSimple = m.createProperty( NS, "p" );
                    pSimple.addProperty( RDF.type, RDF.Property );
                    OntProperty p = pSimple.as( OntProperty.class );

                    assertTrue( !p.isFunctionalProperty(), "isFunctionalProperty not correct" );
                    assertTrue( !p.isDatatypeProperty(), "isDatatypeProperty not correct" );
                    assertTrue( !p.isObjectProperty(), "isObjectProperty not correct" );
                    assertTrue( !p.isTransitiveProperty(), "isTransitiveProperty not correct" );
                    assertTrue( !p.isInverseFunctionalProperty(), "isInverseFunctionalProperty not correct" );
                    if (m_owlLang) {assertTrue( !p.isSymmetricProperty(), "isSymmetricProperty not correct" ); }

                    p = p.convertToInverseFunctionalProperty();

                    assertTrue( !p.isFunctionalProperty(), "isFunctionalProperty not correct" );
                    assertTrue( !p.isDatatypeProperty(), "isDatatypeProperty not correct" );
                    assertTrue( !p.isObjectProperty(), "isObjectProperty not correct" );
                    assertTrue( !p.isTransitiveProperty(), "isTransitiveProperty not correct" );
                    assertTrue( p.isInverseFunctionalProperty(), "isInverseFunctionalProperty not correct" );
                    if (m_owlLang) {assertTrue( !p.isSymmetricProperty(), "isSymmetricProperty not correct" ); }
                }
            },
            new OntTestCase( "OntProperty.convertToSymmetricProperty", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Property pSimple = m.createProperty( NS, "p" );
                    pSimple.addProperty( RDF.type, RDF.Property );
                    OntProperty p = pSimple.as( OntProperty.class );

                    assertTrue( !p.isFunctionalProperty(), "isFunctionalProperty not correct" );
                    assertTrue( !p.isDatatypeProperty(), "isDatatypeProperty not correct" );
                    assertTrue( !p.isObjectProperty(), "isObjectProperty not correct" );
                    assertTrue( !p.isTransitiveProperty(), "isTransitiveProperty not correct" );
                    assertTrue( !p.isInverseFunctionalProperty(), "isInverseFunctionalProperty not correct" );
                    if (m_owlLang) {assertTrue( !p.isSymmetricProperty(), "isSymmetricProperty not correct" ); }

                    p = p.convertToSymmetricProperty();

                    assertTrue( !p.isFunctionalProperty(), "isFunctionalProperty not correct" );
                    assertTrue( !p.isDatatypeProperty(), "isDatatypeProperty not correct" );
                    assertTrue( !p.isObjectProperty(), "isObjectProperty not correct" );
                    assertTrue( !p.isTransitiveProperty(), "isTransitiveProperty not correct" );
                    assertTrue( !p.isInverseFunctionalProperty(), "isInverseFunctionalProperty not correct" );
                    if (m_owlLang) {assertTrue( p.isSymmetricProperty(), "isSymmetricProperty not correct" ); }
                }
            },
            new OntTestCase( "ObjectProperty.inverse", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    ObjectProperty p = m.createObjectProperty( NS + "p" );
                    ObjectProperty q = m.createObjectProperty( NS + "q" );
                    ObjectProperty r = m.createObjectProperty( NS + "r" );

                    assertFalse( p.hasInverse(), "No inverse of p" );
                    assertEquals( null, p.getInverse() );

                    q.addInverseOf( p );
                    assertTrue( p.hasInverse(), "Inverse of p" );
                    assertEquals( q, p.getInverse(), "inverse of p " );

                    r.addInverseOf( p );
                    iteratorTest( p.listInverse(), new Object[] {q,r} );
                }
            },
            new OntTestCase( "OntProperty.listReferringRestrictions", true, true, false ) {
                @Override
                protected void ontTest( OntModel m ) {
                    ObjectProperty p = m.createObjectProperty( NS+"p" );
                    ObjectProperty q = m.createObjectProperty( NS+"q" );
                    Restriction r0 = m.createCardinalityRestriction( null, p, 2 );
                    Restriction r1 = m.createCardinalityRestriction( null, p, 3 );
                    Restriction r2 = m.createCardinalityRestriction( null, q, 2 );
                    Restriction r3 = m.createCardinalityRestriction( null, q, 3 );

                    assertTrue( iteratorContains( p.listReferringRestrictions(), r0 ) );
                    assertTrue( iteratorContains( p.listReferringRestrictions(), r1 ) );
                    assertFalse( iteratorContains( p.listReferringRestrictions(), r2 ) );
                    assertFalse( iteratorContains( p.listReferringRestrictions(), r3 ) );

                    assertNotNull( p.listReferringRestrictions().next() );
                }
            },
            new OntTestCase( "no duplication from imported models", true, true, true ) {
                @Override
                protected void ontTest( OntModel m ) {
                    OntModel m0 = ModelFactory.createOntologyModel( OntModelSpec.OWL_DL_MEM_RULE_INF, null );
                    FileManager.getInternal().readModelInternal( m0, "file:testing/ontology/testImport9/a.ttl" );

                    OntProperty p0 = m0.getOntProperty( "http://incubator.apache.org/jena/2011/10/testont/b#propB" );
                    OntTestUtil.assertIteratorLength( p0.listDomain(), 3 );

                    // repeat test - thus using previously cached model for import

                    OntModel m1 = ModelFactory.createOntologyModel( OntModelSpec.OWL_DL_MEM_RULE_INF, null );
                    FileManager.getInternal().readModelInternal( m1, "file:testing/ontology/testImport9/a.ttl" );

                    OntProperty p1 = m1.getOntProperty( "http://incubator.apache.org/jena/2011/10/testont/b#propB" );
                    OntTestUtil.assertIteratorLength( p1.listDomain(), 3 );
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
