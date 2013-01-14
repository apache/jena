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
import java.util.List;

import junit.framework.TestSuite;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.reasoner.test.TestUtil;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;



/**
 * <p>
 * Unit test cases for the OntProperty class
 * </p>
 */
public class TestProperty
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
        return new TestProperty( "TestProperty" );
    }

    public TestProperty( String name ) {
        super( name );
    }


    // External signature methods
    //////////////////////////////////

    @Override
    public OntTestCase[] getTests() {
        return new OntTestCase[] {
            new OntTestCase( "OntProperty.super-property", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createOntProperty( NS + "p" );
                    OntProperty q = m.createOntProperty( NS + "q" );
                    OntProperty r = m.createOntProperty( NS + "r" );

                    p.addSuperProperty( q );
                    assertEquals( "Cardinality should be 1", 1, p.getCardinality( prof.SUB_PROPERTY_OF() ) );
                    assertEquals( "p have super-prop q", q, p.getSuperProperty() );

                    p.addSuperProperty( r );
                    assertEquals( "Cardinality should be 2", 2, p.getCardinality( prof.SUB_PROPERTY_OF() ) );
                    iteratorTest( p.listSuperProperties(), new Object[] {q, r} );

                    p.setSuperProperty( r );
                    assertEquals( "Cardinality should be 1", 1, p.getCardinality( prof.SUB_PROPERTY_OF() ) );
                    assertEquals( "p shuold have super-prop r", r, p.getSuperProperty() );

                    p.removeSuperProperty( q );
                    assertEquals( "Cardinality should be 1", 1, p.getCardinality( prof.SUB_PROPERTY_OF() ) );
                    p.removeSuperProperty( r );
                    assertEquals( "Cardinality should be 0", 0, p.getCardinality( prof.SUB_PROPERTY_OF() ) );

                    // for symmetry with listSuperClasses(), exclude the reflexive case
                    List<? extends OntProperty> sp = p.listSuperProperties().toList();
                    assertFalse( "super-properties should not include reflexive case", sp.contains( p ) );
                }
            },
            new OntTestCase( "OntProperty.sub-property", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createOntProperty( NS + "p" );
                    OntProperty q = m.createOntProperty( NS + "q" );
                    OntProperty r = m.createOntProperty( NS + "r" );

                    p.addSubProperty( q );
                    assertEquals( "Cardinality should be 1", 1, q.getCardinality( prof.SUB_PROPERTY_OF() ) );
                    assertEquals( "p have sub-prop q", q, p.getSubProperty() );

                    p.addSubProperty( r );
                    assertEquals( "Cardinality should be 2", 2, q.getCardinality( prof.SUB_PROPERTY_OF() ) + r.getCardinality( prof.SUB_PROPERTY_OF() ) );
                    iteratorTest( p.listSubProperties(), new Object[] {q, r} );
                    iteratorTest( q.listSuperProperties(), new Object[] {p} );
                    iteratorTest( r.listSuperProperties(), new Object[] {p} );

                    p.setSubProperty( r );
                    assertEquals( "Cardinality should be 1", 1, q.getCardinality( prof.SUB_PROPERTY_OF() ) + r.getCardinality( prof.SUB_PROPERTY_OF() ) );
                    assertEquals( "p should have sub-prop r", r, p.getSubProperty() );

                    p.removeSubProperty( q );
                    assertTrue( "Should have sub-prop r", p.hasSubProperty( r, false ) );
                    p.removeSubProperty( r );
                    assertTrue( "Should not have sub-prop r", !p.hasSubProperty( r, false ) );
                }
            },
            new OntTestCase( "OntProperty.domain", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createOntProperty( NS + "p" );
                    OntResource a = m.getResource( NS + "a" ).as( OntResource.class );
                    OntResource b = m.getResource( NS + "b" ).as( OntResource.class );

                    p.addDomain( a );
                    assertEquals( "Cardinality should be 1", 1, p.getCardinality( prof.DOMAIN() ) );
                    assertEquals( "p have domain a", a, p.getDomain() );

                    p.addDomain( b );
                    assertEquals( "Cardinality should be 2", 2, p.getCardinality( prof.DOMAIN() ) );
                    iteratorTest( p.listDomain(), new Object[] {a, b} );

                    p.setDomain( b );
                    assertEquals( "Cardinality should be 1", 1, p.getCardinality( prof.DOMAIN() ) );
                    assertEquals( "p should have domain b", b, p.getDomain() );

                    p.removeDomain( a );
                    assertEquals( "Cardinality should be 1", 1, p.getCardinality( prof.DOMAIN() ) );
                    p.removeDomain( b );
                    assertEquals( "Cardinality should be 0", 0, p.getCardinality( prof.DOMAIN() ) );
                }
            },
            new OntTestCase( "OntProperty.range", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createOntProperty( NS + "p" );
                    OntResource a = m.getResource( NS + "a" ).as( OntResource.class );
                    OntResource b = m.getResource( NS + "b" ).as( OntResource.class );

                    p.addRange( a );
                    assertEquals( "Cardinality should be 1", 1, p.getCardinality( prof.RANGE() ) );
                    assertEquals( "p have range a", a, p.getRange() );

                    p.addRange( b );
                    assertEquals( "Cardinality should be 2", 2, p.getCardinality( prof.RANGE() ) );
                    iteratorTest( p.listRange(), new Object[] {a, b} );

                    p.setRange( b );
                    assertEquals( "Cardinality should be 1", 1, p.getCardinality( prof.RANGE() ) );
                    assertEquals( "p should have range b", b, p.getRange() );

                    p.removeRange( a );
                    assertEquals( "Cardinality should be 1", 1, p.getCardinality( prof.RANGE() ) );
                    p.removeRange( b );
                    assertEquals( "Cardinality should be 0", 0, p.getCardinality( prof.RANGE() ) );
                }
            },
            new OntTestCase( "OntProperty.equivalentProperty", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createObjectProperty( NS + "p" );
                    OntProperty q = m.createObjectProperty( NS + "q" );
                    OntProperty r = m.createObjectProperty( NS + "r" );

                    p.addEquivalentProperty( q );
                    assertEquals( "Cardinality should be 1", 1, p.getCardinality( prof.EQUIVALENT_PROPERTY() ) );
                    assertEquals( "p have equivalentProperty q", q, p.getEquivalentProperty() );

                    p.addEquivalentProperty( r );
                    assertEquals( "Cardinality should be 2", 2, p.getCardinality( prof.EQUIVALENT_PROPERTY() ) );
                    iteratorTest( p.listEquivalentProperties(), new Object[] {q,r} );

                    p.setEquivalentProperty( r );
                    assertEquals( "Cardinality should be 1", 1, p.getCardinality( prof.EQUIVALENT_PROPERTY() ) );
                    assertEquals( "p should have equivalentProperty r", r, p.getEquivalentProperty() );

                    p.removeEquivalentProperty( q );
                    assertEquals( "Cardinality should be 1", 1, p.getCardinality( prof.EQUIVALENT_PROPERTY() ) );
                    p.removeEquivalentProperty( r );
                    assertEquals( "Cardinality should be 0", 0, p.getCardinality( prof.EQUIVALENT_PROPERTY() ) );
                }
            },
            new OntTestCase( "OntProperty.inverseOf", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createObjectProperty( NS + "p" );
                    OntProperty q = m.createObjectProperty( NS + "q" );
                    OntProperty r = m.createObjectProperty( NS + "r" );

                    assertFalse( p.isInverseOf( q ) );
                    assertEquals( null, p.getInverseOf() );

                    p.addInverseOf( q );
                    assertEquals( "Cardinality should be 1", 1, p.getCardinality( prof.INVERSE_OF() ) );
                    assertEquals( "p should have inverse q", q, p.getInverseOf() );
                    assertTrue( "inverse value should be an object property", p.getInverseOf() instanceof ObjectProperty );
                    assertTrue( "inverse value should be an object property", q.getInverse() instanceof ObjectProperty );

                    p.addInverseOf( r );
                    assertEquals( "Cardinality should be 2", 2, p.getCardinality( prof.INVERSE_OF() ) );
                    iteratorTest( p.listInverseOf(), new Object[] {q,r} );

                    p.setInverseOf( r );
                    assertEquals( "Cardinality should be 1", 1, p.getCardinality( prof.INVERSE_OF() ) );
                    assertEquals( "p should have inverse r", r, p.getInverseOf() );

                    p.removeInverseProperty( q );
                    assertEquals( "Cardinality should be 1", 1, p.getCardinality( prof.INVERSE_OF() ) );
                    p.removeInverseProperty( r );
                    assertEquals( "Cardinality should be 0", 0, p.getCardinality( prof.INVERSE_OF() ) );
                }
            },
            new OntTestCase( "OntProperty.subproperty.fromFile", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    String lang = m_owlLang ? "owl" : "rdfs" ;
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
                public void ontTest( OntModel m ) throws Exception {
                    String lang = m_owlLang ? "owl" : "rdfs" ;
                    String fileName = "file:testing/ontology/" + lang + "/Property/test.rdf";
                    m.read( fileName );

                    OntProperty p = m.getProperty( NS, "p" ).as( OntProperty.class );
                    OntClass A = m.getResource( NS + "ClassA").as( OntClass.class);

                    assertTrue( "p should have domain A", p.hasDomain( A ) );
                }
            },
            new OntTestCase( "OntProperty.range.fromFile", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    String lang = m_owlLang ? "owl" : "rdfs" ;
                    String fileName = "file:testing/ontology/" + lang + "/Property/test.rdf";
                    m.read( fileName );

                    OntProperty p = m.getProperty( NS, "p" ).as( OntProperty.class );
                    OntClass B = m.getResource( NS + "ClassB").as( OntClass.class);

                    assertTrue( "p should have domain B", p.hasRange( B ) );
                }
            },
            new OntTestCase( "OntProperty.equivalentProeprty.fromFile", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    String lang = m_owlLang ? "owl" : "rdfs" ;
                    String fileName = "file:testing/ontology/" + lang + "/Property/test.rdf";
                    m.read( fileName );

                    OntProperty p = m.getProperty( NS, "p" ).as( OntProperty.class );
                    OntProperty r = m.getProperty( NS, "r" ).as( OntProperty.class );

                    assertTrue( "p should have equiv prop r", p.hasEquivalentProperty( r ) );
                }
            },
            new OntTestCase( "OntProperty.inversePropertyOf.fromFile", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    String lang = m_owlLang ? "owl" : "rdfs" ;
                    String fileName = "file:testing/ontology/" + lang + "/Property/test.rdf";
                    m.read( fileName );

                    OntProperty p = m.getProperty( NS, "p" ).as( OntProperty.class );
                    OntProperty s = m.getProperty( NS, "s" ).as( OntProperty.class );

                    assertTrue( "p should have inv prop s", p.isInverseOf( s ) );
                }
            },

            // type tests
            new OntTestCase( "OntProperty.isFunctionalProperty dt", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntProperty p = m.createDatatypeProperty( NS + "p", true );

                    assertTrue( "isFunctionalProperty not correct",         p.isFunctionalProperty() );
                    assertTrue( "isDatatypeProperty not correct",           p.isDatatypeProperty() );
                    assertTrue( "isObjectProperty not correct",             !p.isObjectProperty() );
                    assertTrue( "isTransitiveProperty not correct",         !p.isTransitiveProperty() );
                    assertTrue( "isInverseFunctionalProperty not correct",  !p.isInverseFunctionalProperty() );
                    if (m_owlLang) {
                        assertTrue( "isSymmetricProperty not correct",      !p.isSymmetricProperty() );
                    }
                }
            },
            new OntTestCase( "OntProperty.isFunctionalProperty object", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntProperty p = m.createObjectProperty( NS + "p", true );

                    assertTrue( "isFunctionalProperty not correct",         p.isFunctionalProperty() );
                    assertTrue( "isDatatypeProperty not correct",           !p.isDatatypeProperty() );
                    assertTrue( "isObjectProperty not correct",             p.isObjectProperty() );
                    assertTrue( "isTransitiveProperty not correct",         !p.isTransitiveProperty() );
                    assertTrue( "isInverseFunctionalProperty not correct",  !p.isInverseFunctionalProperty() );
                    if (m_owlLang) {
                        assertTrue( "isSymmetricProperty not correct",      !p.isSymmetricProperty() );
                    }
                }
            },
            new OntTestCase( "OntProperty.isDatatypeProperty", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntProperty p = m.createDatatypeProperty( NS + "p", false );

                    assertTrue( "isFunctionalProperty not correct",         !p.isFunctionalProperty() );
                    assertTrue( "isDatatypeProperty not correct",           p.isDatatypeProperty() );
                    assertTrue( "isObjectProperty not correct",             !p.isObjectProperty() );
                    assertTrue( "isTransitiveProperty not correct",         !p.isTransitiveProperty() );
                    assertTrue( "isInverseFunctionalProperty not correct",  !p.isInverseFunctionalProperty() );
                    if (m_owlLang) {
                        assertTrue( "isSymmetricProperty not correct",      !p.isSymmetricProperty() );
                    }
                }
            },
            new OntTestCase( "OntProperty.isObjectProperty", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntProperty p = m.createObjectProperty( NS + "p", false );

                    assertTrue( "isFunctionalProperty not correct",         !p.isFunctionalProperty() );
                    assertTrue( "isDatatypeProperty not correct",           !p.isDatatypeProperty() );
                    assertTrue( "isObjectProperty not correct",             p.isObjectProperty() );
                    assertTrue( "isTransitiveProperty not correct",         !p.isTransitiveProperty() );
                    assertTrue( "isInverseFunctionalProperty not correct",  !p.isInverseFunctionalProperty() );
                    if (m_owlLang) {
                        assertTrue( "isSymmetricProperty not correct",      !p.isSymmetricProperty() );
                    }
                }
            },
            new OntTestCase( "OntProperty.isTransitiveProperty", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntProperty p = m.createTransitiveProperty( NS + "p" );

                    assertTrue( "isFunctionalProperty not correct",         !p.isFunctionalProperty() );
                    assertTrue( "isDatatypeProperty not correct",           !p.isDatatypeProperty() );
                    assertTrue( "isObjectProperty not correct",             !p.isObjectProperty() );    // this should be true by entailment, but we have reasoning switched off
                    assertTrue( "isTransitiveProperty not correct",         p.isTransitiveProperty() );
                    assertTrue( "isInverseFunctionalProperty not correct",  !p.isInverseFunctionalProperty() );
                    if (m_owlLang) {
                        assertTrue( "isSymmetricProperty not correct",      !p.isSymmetricProperty() );
                    }
                }
            },
            new OntTestCase( "OntProperty.isInverseFunctionalProperty", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntProperty p = m.createInverseFunctionalProperty( NS + "p" );

                    assertTrue( "isFunctionalProperty not correct",         !p.isFunctionalProperty() );
                    assertTrue( "isDatatypeProperty not correct",           !p.isDatatypeProperty() );
                    assertTrue( "isObjectProperty not correct",             !p.isObjectProperty() );    // this should be true by entailment, but we have reasoning switched off
                    assertTrue( "isTransitiveProperty not correct",         !p.isTransitiveProperty() );
                    assertTrue( "isInverseFunctionalProperty not correct",  p.isInverseFunctionalProperty() );
                    if (m_owlLang) {
                        assertTrue( "isSymmetricProperty not correct",      !p.isSymmetricProperty() );
                    }
                }
            },
            new OntTestCase( "OntProperty.isSymmetricProperty", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntProperty p = m.createSymmetricProperty( NS + "p" );

                    assertTrue( "isFunctionalProperty not correct",         !p.isFunctionalProperty() );
                    assertTrue( "isDatatypeProperty not correct",           !p.isDatatypeProperty() );
                    assertTrue( "isObjectProperty not correct",             !p.isObjectProperty() );    // this should be true by entailment, but we have reasoning switched off
                    assertTrue( "isTransitiveProperty not correct",         !p.isTransitiveProperty() );
                    assertTrue( "isInverseFunctionalProperty not correct",  !p.isInverseFunctionalProperty() );
                    if (m_owlLang) {
                        assertTrue( "isSymmetricProperty not correct",      p.isSymmetricProperty() );
                    }
                }
            },
            new OntTestCase( "OntProperty.convertToFunctionalProperty", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Property pSimple = m.createProperty( NS, "p" );
                    pSimple.addProperty( RDF.type, RDF.Property );
                    OntProperty p = pSimple.as( OntProperty.class );

                    assertTrue( "isFunctionalProperty not correct",         !p.isFunctionalProperty() );
                    assertTrue( "isDatatypeProperty not correct",           !p.isDatatypeProperty() );
                    assertTrue( "isObjectProperty not correct",             !p.isObjectProperty() );
                    assertTrue( "isTransitiveProperty not correct",         !p.isTransitiveProperty() );
                    assertTrue( "isInverseFunctionalProperty not correct",  !p.isInverseFunctionalProperty() );
                    if (m_owlLang) {assertTrue( "isSymmetricProperty not correct", !p.isSymmetricProperty() ); }

                    p = p.convertToFunctionalProperty();

                    assertTrue( "isFunctionalProperty not correct",         p.isFunctionalProperty() );
                    assertTrue( "isDatatypeProperty not correct",           !p.isDatatypeProperty() );
                    assertTrue( "isObjectProperty not correct",             !p.isObjectProperty() );
                    assertTrue( "isTransitiveProperty not correct",         !p.isTransitiveProperty() );
                    assertTrue( "isInverseFunctionalProperty not correct",  !p.isInverseFunctionalProperty() );
                    if (m_owlLang) {assertTrue( "isSymmetricProperty not correct", !p.isSymmetricProperty() ); }
                }
            },
            new OntTestCase( "OntProperty.convertToDatatypeProperty", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Property pSimple = m.createProperty( NS, "p" );
                    pSimple.addProperty( RDF.type, RDF.Property );
                    OntProperty p = pSimple.as( OntProperty.class );

                    assertTrue( "isFunctionalProperty not correct",         !p.isFunctionalProperty() );
                    assertTrue( "isDatatypeProperty not correct",           !p.isDatatypeProperty() );
                    assertTrue( "isObjectProperty not correct",             !p.isObjectProperty() );
                    assertTrue( "isTransitiveProperty not correct",         !p.isTransitiveProperty() );
                    assertTrue( "isInverseFunctionalProperty not correct",  !p.isInverseFunctionalProperty() );
                    if (m_owlLang) {assertTrue( "isSymmetricProperty not correct", !p.isSymmetricProperty() ); }

                    p = p.convertToDatatypeProperty();

                    assertTrue( "isFunctionalProperty not correct",         !p.isFunctionalProperty() );
                    assertTrue( "isDatatypeProperty not correct",           p.isDatatypeProperty() );
                    assertTrue( "isObjectProperty not correct",             !p.isObjectProperty() );
                    assertTrue( "isTransitiveProperty not correct",         !p.isTransitiveProperty() );
                    assertTrue( "isInverseFunctionalProperty not correct",  !p.isInverseFunctionalProperty() );
                    if (m_owlLang) {assertTrue( "isSymmetricProperty not correct", !p.isSymmetricProperty() ); }
                }
            },
            new OntTestCase( "OntProperty.convertToObjectProperty", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Property pSimple = m.createProperty( NS, "p" );
                    pSimple.addProperty( RDF.type, RDF.Property );
                    OntProperty p = pSimple.as( OntProperty.class );

                    assertTrue( "isFunctionalProperty not correct",         !p.isFunctionalProperty() );
                    assertTrue( "isDatatypeProperty not correct",           !p.isDatatypeProperty() );
                    assertTrue( "isObjectProperty not correct",             !p.isObjectProperty() );
                    assertTrue( "isTransitiveProperty not correct",         !p.isTransitiveProperty() );
                    assertTrue( "isInverseFunctionalProperty not correct",  !p.isInverseFunctionalProperty() );
                    if (m_owlLang) {assertTrue( "isSymmetricProperty not correct", !p.isSymmetricProperty() ); }

                    p = p.convertToObjectProperty();

                    assertTrue( "isFunctionalProperty not correct",         !p.isFunctionalProperty() );
                    assertTrue( "isDatatypeProperty not correct",           !p.isDatatypeProperty() );
                    assertTrue( "isObjectProperty not correct",             p.isObjectProperty() );
                    assertTrue( "isTransitiveProperty not correct",         !p.isTransitiveProperty() );
                    assertTrue( "isInverseFunctionalProperty not correct",  !p.isInverseFunctionalProperty() );
                    if (m_owlLang) {assertTrue( "isSymmetricProperty not correct", !p.isSymmetricProperty() ); }
                }
            },
            new OntTestCase( "OntProperty.convertToTransitiveProperty", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Property pSimple = m.createProperty( NS, "p" );
                    pSimple.addProperty( RDF.type, RDF.Property );
                    OntProperty p = pSimple.as( OntProperty.class );

                    assertTrue( "isFunctionalProperty not correct",         !p.isFunctionalProperty() );
                    assertTrue( "isDatatypeProperty not correct",           !p.isDatatypeProperty() );
                    assertTrue( "isObjectProperty not correct",             !p.isObjectProperty() );
                    assertTrue( "isTransitiveProperty not correct",         !p.isTransitiveProperty() );
                    assertTrue( "isInverseFunctionalProperty not correct",  !p.isInverseFunctionalProperty() );
                    if (m_owlLang) {assertTrue( "isSymmetricProperty not correct", !p.isSymmetricProperty() ); }

                    p = p.convertToTransitiveProperty();

                    assertTrue( "isFunctionalProperty not correct",         !p.isFunctionalProperty() );
                    assertTrue( "isDatatypeProperty not correct",           !p.isDatatypeProperty() );
                    assertTrue( "isObjectProperty not correct",             !p.isObjectProperty() );
                    assertTrue( "isTransitiveProperty not correct",         p.isTransitiveProperty() );
                    assertTrue( "isInverseFunctionalProperty not correct",  !p.isInverseFunctionalProperty() );
                    if (m_owlLang) {assertTrue( "isSymmetricProperty not correct", !p.isSymmetricProperty() ); }
                }
            },
            new OntTestCase( "OntProperty.convertToInverseFunctionalProperty", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Property pSimple = m.createProperty( NS, "p" );
                    pSimple.addProperty( RDF.type, RDF.Property );
                    OntProperty p = pSimple.as( OntProperty.class );

                    assertTrue( "isFunctionalProperty not correct",         !p.isFunctionalProperty() );
                    assertTrue( "isDatatypeProperty not correct",           !p.isDatatypeProperty() );
                    assertTrue( "isObjectProperty not correct",             !p.isObjectProperty() );
                    assertTrue( "isTransitiveProperty not correct",         !p.isTransitiveProperty() );
                    assertTrue( "isInverseFunctionalProperty not correct",  !p.isInverseFunctionalProperty() );
                    if (m_owlLang) {assertTrue( "isSymmetricProperty not correct", !p.isSymmetricProperty() ); }

                    p = p.convertToInverseFunctionalProperty();

                    assertTrue( "isFunctionalProperty not correct",         !p.isFunctionalProperty() );
                    assertTrue( "isDatatypeProperty not correct",           !p.isDatatypeProperty() );
                    assertTrue( "isObjectProperty not correct",             !p.isObjectProperty() );
                    assertTrue( "isTransitiveProperty not correct",         !p.isTransitiveProperty() );
                    assertTrue( "isInverseFunctionalProperty not correct",  p.isInverseFunctionalProperty() );
                    if (m_owlLang) {assertTrue( "isSymmetricProperty not correct", !p.isSymmetricProperty() ); }
                }
            },
            new OntTestCase( "OntProperty.convertToSymmetricProperty", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Property pSimple = m.createProperty( NS, "p" );
                    pSimple.addProperty( RDF.type, RDF.Property );
                    OntProperty p = pSimple.as( OntProperty.class );

                    assertTrue( "isFunctionalProperty not correct",         !p.isFunctionalProperty() );
                    assertTrue( "isDatatypeProperty not correct",           !p.isDatatypeProperty() );
                    assertTrue( "isObjectProperty not correct",             !p.isObjectProperty() );
                    assertTrue( "isTransitiveProperty not correct",         !p.isTransitiveProperty() );
                    assertTrue( "isInverseFunctionalProperty not correct",  !p.isInverseFunctionalProperty() );
                    if (m_owlLang) {assertTrue( "isSymmetricProperty not correct", !p.isSymmetricProperty() ); }

                    p = p.convertToSymmetricProperty();

                    assertTrue( "isFunctionalProperty not correct",         !p.isFunctionalProperty() );
                    assertTrue( "isDatatypeProperty not correct",           !p.isDatatypeProperty() );
                    assertTrue( "isObjectProperty not correct",             !p.isObjectProperty() );
                    assertTrue( "isTransitiveProperty not correct",         !p.isTransitiveProperty() );
                    assertTrue( "isInverseFunctionalProperty not correct",  !p.isInverseFunctionalProperty() );
                    if (m_owlLang) {assertTrue( "isSymmetricProperty not correct", p.isSymmetricProperty() ); }
                }
            },
            new OntTestCase( "ObjectProperty.inverse", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    ObjectProperty p = m.createObjectProperty( NS + "p" );
                    ObjectProperty q = m.createObjectProperty( NS + "q" );
                    ObjectProperty r = m.createObjectProperty( NS + "r" );

                    assertFalse( "No inverse of p", p.hasInverse() );
                    assertEquals( null, p.getInverse() );

                    q.addInverseOf( p );
                    assertTrue( "Inverse of p", p.hasInverse() );
                    assertEquals( "inverse of p ", q, p.getInverse() );

                    r.addInverseOf( p );
                    iteratorTest( p.listInverse(), new Object[] {q,r} );
                }
            },
            new OntTestCase( "OntProperty.listReferringRestrictions", true, true, false ) {
                @Override
                protected void ontTest( OntModel m ) throws Exception {
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
                protected void ontTest( OntModel m ) throws Exception {
                    OntModel m0 = ModelFactory.createOntologyModel( OntModelSpec.OWL_DL_MEM_RULE_INF, null );
                    FileManager.get().readModel( m0, "file:testing/ontology/testImport9/a.ttl" );

                    OntProperty p0 = m0.getOntProperty( "http://incubator.apache.org/jena/2011/10/testont/b#propB" );
                    TestUtil.assertIteratorLength( p0.listDomain(), 3 );

                    // repeat test - thus using previously cached model for import

                    OntModel m1 = ModelFactory.createOntologyModel( OntModelSpec.OWL_DL_MEM_RULE_INF, null );
                    FileManager.get().readModel( m1, "file:testing/ontology/testImport9/a.ttl" );

                    OntProperty p1 = m1.getOntProperty( "http://incubator.apache.org/jena/2011/10/testont/b#propB" );
                    TestUtil.assertIteratorLength( p1.listDomain(), 3 );
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
