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
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.jena.test.JenaTestLib;

/**
 * <p>
 * Unit test cases for ontology resources
 * </p>
 */
@SuppressWarnings("removal")
public class TestOntResource extends OntTestBase
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

    // Internal implementation methods
    //////////////////////////////////

    @Override
    public OntTestCase[] getTests() {
        return new OntTestCase[] {
            new OntTestCase( "OntResource.sameAs", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    OntResource a = m.getResource( NS + "a" ).as( OntResource.class );
                    OntResource b = m.getResource( NS + "b" ).as( OntResource.class );
                    OntResource c = m.getResource( NS + "c" ).as( OntResource.class );

                    a.addSameAs( b );
                    assertEquals( 1, a.getCardinality( prof.SAME_AS() ), "Cardinality should be 1" );
                    assertEquals( b, a.getSameAs(), "a should be sameAs b" );

                    a.addSameAs( c );
                    assertEquals( 2, a.getCardinality( prof.SAME_AS() ), "Cardinality should be 2" );
                    iteratorTest( a.listSameAs(), new Object[] {b, c} );

                    assertTrue( a.isSameAs( b ), "a should be the same as b" );
                    assertTrue( a.isSameAs( c ), "a should be the same as c" );

                    a.setSameAs( b );
                    assertEquals( 1, a.getCardinality( prof.SAME_AS() ), "Cardinality should be 1" );
                    assertEquals( b, a.getSameAs(), "a should be sameAs b" );

                    a.removeSameAs( c );
                    assertEquals( 1, a.getCardinality( prof.SAME_AS() ), "Cardinality should be 1" );
                    a.removeSameAs( b );
                    assertEquals( 0, a.getCardinality( prof.SAME_AS() ), "Cardinality should be 0" );
                }
            },
            new OntTestCase( "OntResource.differentFrom", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    OntResource a = m.getResource( NS + "a" ).as( OntResource.class );
                    OntResource b = m.getResource( NS + "b" ).as( OntResource.class );
                    OntResource c = m.getResource( NS + "c" ).as( OntResource.class );

                    a.addDifferentFrom( b );
                    assertEquals( 1, a.getCardinality( prof.DIFFERENT_FROM() ), "Cardinality should be 1" );
                    assertEquals( b, a.getDifferentFrom(), "a should be differentFrom b" );

                    a.addDifferentFrom( c );
                    assertEquals( 2, a.getCardinality( prof.DIFFERENT_FROM() ), "Cardinality should be 2" );
                    iteratorTest( a.listDifferentFrom(), new Object[] {b, c} );

                    assertTrue( a.isDifferentFrom( b ), "a should be diff from b" );
                    assertTrue( a.isDifferentFrom( c ), "a should be diff from c" );

                    a.setDifferentFrom( b );
                    assertEquals( 1, a.getCardinality( prof.DIFFERENT_FROM() ), "Cardinality should be 1" );
                    assertEquals( b, a.getDifferentFrom(), "a should be differentFrom b" );

                    a.removeDifferentFrom( c );
                    assertEquals( 1, a.getCardinality( prof.DIFFERENT_FROM() ), "Cardinality should be 1" );
                    a.removeDifferentFrom( b );
                    assertEquals( 0, a.getCardinality( prof.DIFFERENT_FROM() ), "Cardinality should be 0" );
                }
            },
            new OntTestCase( "OntResource.seeAlso", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    OntResource a = m.getResource( NS + "a" ).as( OntResource.class );
                    OntResource b = m.getResource( NS + "b" ).as( OntResource.class );
                    OntResource c = m.getResource( NS + "c" ).as( OntResource.class );

                    a.addSeeAlso( b );
                    assertEquals( 1, a.getCardinality( prof.SEE_ALSO() ), "Cardinality should be 1" );
                    assertEquals( b, a.getSeeAlso(), "a should be seeAlso b" );

                    a.addSeeAlso( c );
                    assertEquals( 2, a.getCardinality( prof.SEE_ALSO() ), "Cardinality should be 2" );
                    iteratorTest( a.listSeeAlso(), new Object[] {b, c} );

                    assertTrue( a.hasSeeAlso( b ), "a should have seeAlso b" );
                    assertTrue( a.hasSeeAlso( c ), "a should have seeAlso c" );

                    a.setSeeAlso( b );
                    assertEquals( 1, a.getCardinality( prof.SEE_ALSO() ), "Cardinality should be 1" );
                    assertEquals( b, a.getSeeAlso(), "a should be seeAlso b" );

                    a.removeSeeAlso( c );
                    assertEquals( 1, a.getCardinality( prof.SEE_ALSO() ), "Cardinality should be 1" );
                    a.removeSeeAlso( b );
                    assertEquals( 0, a.getCardinality( prof.SEE_ALSO() ), "Cardinality should be 0" );
                }
            },
            new OntTestCase( "OntResource.isDefinedBy", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    OntResource a = m.getResource( NS + "a" ).as( OntResource.class );
                    OntResource b = m.getResource( NS + "b" ).as( OntResource.class );
                    OntResource c = m.getResource( NS + "c" ).as( OntResource.class );

                    a.addIsDefinedBy( b );
                    assertEquals( 1, a.getCardinality( prof.IS_DEFINED_BY() ), "Cardinality should be 1" );
                    assertEquals( b, a.getIsDefinedBy(), "a should be isDefinedBy b" );

                    a.addIsDefinedBy( c );
                    assertEquals( 2, a.getCardinality( prof.IS_DEFINED_BY() ), "Cardinality should be 2" );
                    iteratorTest( a.listIsDefinedBy(), new Object[] {b, c} );

                    assertTrue( a.isDefinedBy( b ), "a should be defined by b" );
                    assertTrue( a.isDefinedBy( c ), "a should be defined by c" );

                    a.setIsDefinedBy( b );
                    assertEquals( 1, a.getCardinality( prof.IS_DEFINED_BY() ), "Cardinality should be 1" );
                    assertEquals( b, a.getIsDefinedBy(), "a should be isDefinedBy b" );

                    a.removeDefinedBy( c );
                    assertEquals( 1, a.getCardinality( prof.IS_DEFINED_BY() ), "Cardinality should be 1" );
                    a.removeDefinedBy( b );
                    assertEquals( 0, a.getCardinality( prof.IS_DEFINED_BY() ), "Cardinality should be 0" );
            }
            },
            new OntTestCase( "OntResource.versionInfo", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    OntResource a = m.getResource( NS + "a" ).as( OntResource.class );

                    a.addVersionInfo( "some info" );
                    assertEquals( 1, a.getCardinality( prof.VERSION_INFO() ), "Cardinality should be 1" );
                    assertEquals( "some info", a.getVersionInfo(), "a has wrong version info" );

                    a.addVersionInfo( "more info" );
                    assertEquals( 2, a.getCardinality( prof.VERSION_INFO() ), "Cardinality should be 2" );
                    iteratorTest( a.listVersionInfo(), new Object[] {"some info", "more info"} );

                    assertTrue( a.hasVersionInfo( "some info" ), "a should have some info" );
                    assertTrue( a.hasVersionInfo( "more info" ), "a should have more info" );

                    a.setVersionInfo( "new info" );
                    assertEquals( 1, a.getCardinality( prof.VERSION_INFO() ), "Cardinality should be 1" );
                    assertEquals( "new info", a.getVersionInfo(), "a has wrong version info" );

                    a.removeVersionInfo( "old info" );
                    assertEquals( 1, a.getCardinality( prof.VERSION_INFO() ), "Cardinality should be 1" );
                    a.removeVersionInfo( "new info" );
                    assertEquals( 0, a.getCardinality( prof.VERSION_INFO() ), "Cardinality should be 0" );
                }
            },
            new OntTestCase( "OntResource.label.nolang", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    OntResource a = m.getResource( NS + "a" ).as( OntResource.class );

                    a.addLabel( "some info", null );
                    assertEquals( 1, a.getCardinality( prof.LABEL() ), "Cardinality should be 1" );
                    assertEquals( "some info", a.getLabel( null ), "a has wrong label" );

                    a.addLabel( "more info", null );
                    assertEquals( 2, a.getCardinality( prof.LABEL() ), "Cardinality should be 2" );
                    iteratorTest( a.listLabels( null ), new Object[] {m.createLiteral( "some info" ), m.createLiteral( "more info" )} );

                    assertTrue( a.hasLabel( "some info", null ), "a should have label some info" );
                    assertTrue( a.hasLabel( "more info", null ), "a should have label more info" );

                    a.setLabel( "new info", null );
                    assertEquals( 1, a.getCardinality( prof.LABEL() ), "Cardinality should be 1" );
                    assertEquals( "new info", a.getLabel( null ), "a has wrong label" );

                    a.removeLabel( "foo", null );
                    assertEquals( 1, a.getCardinality( prof.LABEL() ), "Cardinality should be 1" );
                    a.removeLabel( "new info", null );
                    assertEquals( 0, a.getCardinality( prof.LABEL() ), "Cardinality should be 0" );
                }
            },
            new OntTestCase( "OntResource.label.lang", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntResource a = m.getResource( NS + "a" ).as( OntResource.class );

                    a.addLabel( "good", "EN" );
                    assertEquals( "good", a.getLabel( null ), "wrong label" );

                    a.addLabel( "bon", "FR" );

                    assertEquals( "good", a.getLabel( "EN" ), "wrong label" );
                    assertEquals( null, a.getLabel( "EN-GB" ), "wrong label" );  // no literal with a specific enough language
                    assertEquals( "bon", a.getLabel( "FR" ), "wrong label" );

                    assertTrue( a.hasLabel( "good", "EN" ), "a should have label good" );
                    assertTrue( a.hasLabel( "bon", "FR" ), "a should have label bon" );
                    assertTrue( !a.hasLabel( "good", "DE" ), "a should note have label good (DE)" );

                    a.addLabel( "spiffing", "EN-GB" );
                    a.addLabel( "duude", "EN-US" );

                    assertEquals( "spiffing", a.getLabel( "EN-GB" ), "wrong label" );
                    assertEquals( "duude", a.getLabel( "EN-US" ), "wrong label" );
                    assertEquals( null, a.getLabel( "DE" ), "wrong label" );

                    a.addLabel( "abcdef", "AB-CD" );
                    assertEquals( "abcdef", a.getLabel( "AB" ), "wrong label" );
                    assertEquals( null, a.getLabel( "AB-XY" ), "wrong label" );

                    a.removeLabel( "abcde", "AB-CD" );
                    assertEquals( 5, a.getCardinality( a.getProfile().LABEL() ), "Cardinality should be 5" );
                    a.removeLabel( "abcdef", "AB-CD" );
                    assertEquals( 4, a.getCardinality( a.getProfile().LABEL() ), "Cardinality should be 4" );
                }
            },
            new OntTestCase( "OntResource.comment.nolang", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    OntResource a = m.getResource( NS + "a" ).as( OntResource.class );

                    a.addComment( "some info", null );
                    assertEquals( 1, a.getCardinality( prof.COMMENT() ), "Cardinality should be 1" );
                    assertEquals( "some info", a.getComment( null ), "a has wrong comment" );

                    a.addComment( "more info", null );
                    assertEquals( 2, a.getCardinality( prof.COMMENT() ), "Cardinality should be 2" );
                    iteratorTest( a.listComments( null ), new Object[] {m.createLiteral( "some info" ), m.createLiteral( "more info" )} );

                    assertTrue( a.hasComment( "some info", null ), "a should have comment some info" );
                    assertTrue( a.hasComment( "more info", null ), "a should have comment more info" );

                    a.setComment( "new info", null );
                    assertEquals( 1, a.getCardinality( prof.COMMENT() ), "Cardinality should be 1" );
                    assertEquals( "new info", a.getComment( null ), "a has wrong comment" );

                    a.removeComment( "foo", null );
                    assertEquals( 1, a.getCardinality( prof.COMMENT() ), "Cardinality should be 1" );
                    a.removeComment( "new info", null );
                    assertEquals( 0, a.getCardinality( prof.COMMENT() ), "Cardinality should be 0" );
                }
            },
            new OntTestCase( "OntResource.comment.lang", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntResource a = m.getResource( NS + "a" ).as( OntResource.class );

                    a.addComment( "good", "EN" );
                    assertEquals( "good", a.getComment( null ), "wrong comment" );

                    a.addComment( "bon", "FR" );

                    assertEquals( "good", a.getComment( "EN" ), "wrong comment" );
                    assertEquals( null, a.getComment( "EN-GB" ), "wrong comment" );  // no literal with a specific enough language
                    assertEquals( "bon", a.getComment( "FR" ), "wrong comment" );

                    assertTrue( a.hasComment( "good", "EN" ), "a should have label good" );
                    assertTrue( a.hasComment( "bon", "FR" ), "a should have label bon" );
                    assertTrue( !a.hasComment( "good", "DE" ), "a should note have label good (DE)" );

                    a.addComment( "spiffing", "EN-GB" );
                    a.addComment( "duude", "EN-US" );

                    assertEquals( "spiffing", a.getComment( "EN-GB" ), "wrong comment" );
                    assertEquals( "duude", a.getComment( "EN-US" ), "wrong comment" );
                    assertEquals( null, a.getComment( "DE" ), "wrong comment" );

                    a.addComment( "abcdef", "AB-CD" );
                    assertEquals( "abcdef", a.getComment( "AB" ), "wrong comment" );
                    assertEquals( null, a.getComment( "AB-XY" ), "wrong comment" );

                    a.removeComment( "abcde", "AB-CD" );
                    assertEquals( 5, a.getCardinality( a.getProfile().COMMENT() ), "Cardinality should be 5" );
                    a.removeComment( "abcdef", "AB-CD" );
                    assertEquals( 4, a.getCardinality( a.getProfile().COMMENT() ), "Cardinality should be 4" );
                }
            },
            new OntTestCase( "OntResource.type (no inference)", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    A.addSubClass( B );

                    OntResource a = m.getResource( NS + "a" ).as( OntResource.class );
                    assertEquals( 0, a.getCardinality( RDF.type ), "Cardinality of rdf:type is wrong" );

                    a.addRDFType( B );
                    assertEquals( B, a.getRDFType(), "rdf:type of a is wrong" );
                    assertEquals( B, a.getRDFType( false ), "rdf:type of a is wrong" );

                    iteratorTest( a.listRDFTypes( false ), new Object[] {B} );       // only B since we're not using an inference model
                    iteratorTest( a.listRDFTypes( true ), new Object[] {B} );

                    a.addRDFType( A );
                    iteratorTest( a.listRDFTypes( false ), new Object[] {A,B} );
                    iteratorTest( a.listRDFTypes( true ), new Object[] {B} );

                    assertTrue( !a.hasRDFType( A, true ), "a should not be of class A direct");
                    assertTrue( a.hasRDFType( B, true ), "a should not be of class B direct");

                    OntClass C = m.createClass( NS + "C" );
                    a.setRDFType( C );
                    assertTrue( a.hasRDFType( C, false ), "a should be of class C");
                    assertTrue( !a.hasRDFType( A, false ), "a should not be of class A");
                    assertTrue( !a.hasRDFType( B, false ), "a should not be of class B");

                    a.removeRDFType( B );
                    assertEquals( 1, a.getCardinality( RDF.type ), "Cardinality should be 1" );
                    a.removeRDFType( C );
                    assertEquals( 0, a.getCardinality( RDF.type ), "Cardinality should be 0" );
                }
            },
            new OntTestCase( "OntResource.remove", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    OntClass C = m.createClass( NS + "C" );
                    OntClass D = m.createClass( NS + "D" );
                    OntClass E = m.createClass( NS + "E" );
                    A.addSubClass( B );
                    A.addSubClass( C );
                    C.addSubClass( D );
                    C.addSubClass( E );

                    assertTrue( E.hasSuperClass( C, false ), "super-class of E" );
                    iteratorTest( A.listSubClasses(), new Object[] {B,C} );

                    C.remove();

                    assertTrue( !D.hasSuperClass( C, false ), "super-class of D" );
                    assertTrue( !E.hasSuperClass( C, false ), "super-class of E" );
                    iteratorTest( A.listSubClasses(), new Object[] {B} );
                }
            },
            new OntTestCase( "OntResource.asClass", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) {
                    Resource r = m.createResource();
                    r.addProperty( RDF.type, m.getProfile().CLASS() );
                    OntResource or = r.as( OntResource.class );
                    assertFalse( or.isAnnotationProperty(), "should not be annotation prop" );
                    assertFalse( or.isAllDifferent(), "should not be all different" );
                    assertTrue( or.isClass(), "should be class" );
                    assertFalse( or.isProperty(), "should not be property" );
                    assertFalse( or.isObjectProperty(), "should not be object property" );
                    assertFalse( or.isDatatypeProperty(), "should not be datatype property" );
                    assertTrue( owlFull() || !or.isIndividual(), "should not be individual" );
                    assertFalse( or.isDataRange(), "should not be data range" );
                    assertFalse( or.isOntology(), "should not be ontology" );

                    RDFNode n = or.asClass();
                    assertTrue( n instanceof OntClass, "Should be OntClass" );
                }
            },
            new OntTestCase( "OntResource.asAnnotationProperty", true, true, false) {
                @Override
                public void ontTest( OntModel m ) {
                    if (m.getProfile().ANNOTATION_PROPERTY() == null) {
                        throw new ProfileException(null,null);
                    }
                    Resource r = m.createResource();
                    r.addProperty( RDF.type, m.getProfile().ANNOTATION_PROPERTY() );
                    OntResource or = r.as( OntResource.class );

                    assertTrue( or.isAnnotationProperty(), "should be annotation prop" );
                    assertFalse( or.isAllDifferent(), "should not be all different" );
                    assertFalse( or.isClass(), "should not be class" );
                    assertTrue( or.isProperty(), "should be property" );
                    assertFalse( or.isObjectProperty(), "should not be object property" );
                    assertFalse( or.isDatatypeProperty(), "should not be datatype property" );
                    assertFalse( or.isIndividual(), "should not be individual" );
                    assertFalse( or.isDataRange(), "should not be data range" );
                    assertFalse( or.isOntology(), "should not be ontology" );

                    RDFNode n = or.asAnnotationProperty();
                    assertTrue( n instanceof AnnotationProperty, "Should be AnnotationProperty");
                }
            },
            new OntTestCase( "OntResource.asObjectProperty", true, true, false) {
                @Override
                public void ontTest( OntModel m ) {
                    if (m.getProfile().OBJECT_PROPERTY() == null) {
                        throw new ProfileException(null,null);
                    }
                    Resource r = m.createResource();
                    r.addProperty( RDF.type, m.getProfile().OBJECT_PROPERTY() );
                    OntResource or = r.as( OntResource.class );

                    assertFalse( or.isAnnotationProperty(), "should not be annotation prop" );
                    assertFalse( or.isAllDifferent(), "should not be all different" );
                    assertFalse( or.isClass(), "should not be class" );
                    assertTrue( or.isProperty(), "should be property" );
                    assertTrue( or.isObjectProperty(), "should be object property" );
                    assertFalse( or.isDatatypeProperty(), "should not be datatype property" );
                    assertFalse( or.isIndividual(), "should not be individual" );
                    assertFalse( or.isDataRange(), "should not be data range" );
                    assertFalse( or.isOntology(), "should not be ontology" );

                    RDFNode n = or.asObjectProperty();
                    assertTrue( n instanceof ObjectProperty, "Should be ObjectProperty");
                }
            },
            new OntTestCase( "OntResource.asDatatypeProperty", true, true, false) {
                @Override
                public void ontTest( OntModel m ) {
                    if (m.getProfile().DATATYPE_PROPERTY() == null) {
                        throw new ProfileException(null,null);
                    }
                    Resource r = m.createResource();
                    r.addProperty( RDF.type, m.getProfile().DATATYPE_PROPERTY() );
                    OntResource or = r.as( OntResource.class );

                    assertFalse( or.isAnnotationProperty(), "should not be annotation prop" );
                    assertFalse( or.isAllDifferent(), "should not be all different" );
                    assertFalse( or.isClass(), "should not be class" );
                    assertTrue( or.isProperty(), "should be property" );
                    assertFalse( or.isObjectProperty(), "should not be object property" );
                    assertTrue( or.isDatatypeProperty(), "should be datatype property" );
                    assertFalse( or.isIndividual(), "should not be individual" );
                    assertFalse( or.isDataRange(), "should not be data range" );
                    assertFalse( or.isOntology(), "should not be ontology" );

                    RDFNode n = or.asDatatypeProperty();
                    assertTrue( n instanceof DatatypeProperty, "Should be DatatypeProperty");
                }
            },
            new OntTestCase( "OntResource.asAllDifferent", true, true, false) {
                @Override
                public void ontTest( OntModel m ) {
                    if (m.getProfile().ALL_DIFFERENT() == null) {
                        throw new ProfileException(null,null);
                    }
                    Resource r = m.createResource();
                    r.addProperty( RDF.type, m.getProfile().ALL_DIFFERENT() );
                    OntResource or = r.as( OntResource.class );

                    assertFalse( or.isAnnotationProperty(), "should not be annotation prop" );
                    assertTrue( or.isAllDifferent(), "should be all different" );
                    assertFalse( or.isClass(), "should not be class" );
                    assertFalse( or.isProperty(), "should not be property" );
                    assertFalse( or.isObjectProperty(), "should not be object property" );
                    assertFalse( or.isDatatypeProperty(), "should not be datatype property" );
                    assertFalse( or.isIndividual(), "should not be individual" );
                    assertFalse( or.isDataRange(), "should not be data range" );
                    assertFalse( or.isOntology(), "should not be ontology" );

                    RDFNode n = or.asAllDifferent();
                    assertTrue( n instanceof AllDifferent, "Should be AnnotationProperty");
                }
            },
            new OntTestCase( "OntResource.asProperty", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) {
                    Resource r = m.createResource();
                    r.addProperty( RDF.type, m.getProfile().PROPERTY() );
                    OntResource or = r.as( OntResource.class );

                    assertFalse( or.isAnnotationProperty(), "should not be annotation prop" );
                    assertFalse( or.isAllDifferent(), "should not be all different" );
                    assertFalse( or.isClass(), "should not be class" );
                    assertTrue( or.isProperty(), "should be property" );
                    assertFalse( or.isObjectProperty(), "should not be object property" );
                    assertFalse( or.isDatatypeProperty(), "should not be datatype property" );
                    assertFalse( or.isIndividual(), "should not be individual" );
                    assertFalse( or.isDataRange(), "should not be data range" );
                    assertFalse( or.isOntology(), "should not be ontology" );

                    RDFNode n = or.asProperty();
                    assertTrue( n instanceof OntProperty, "Should be OntProperty");
                }
            },
            new OntTestCase( "OntResource.asIndividual", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) {
                    Resource r = m.createResource();
                    Resource s = m.createResource();
                    s.addProperty( RDF.type, m.getProfile().CLASS() );
                    r.addProperty( RDF.type, s );
                    OntResource or = r.as( OntResource.class );

                    assertFalse( or.isAnnotationProperty(), "should not be annotation prop" );
                    assertFalse( or.isAllDifferent(), "should not be all different" );
                    assertFalse( or.isClass(), "should not be class" );
                    assertFalse( or.isProperty(), "should not be property" );
                    assertFalse( or.isObjectProperty(), "should not be object property" );
                    assertFalse( or.isDatatypeProperty(), "should not be datatype property" );
                    assertTrue( or.isIndividual(), "should be individual" );
                    assertFalse( or.isDataRange(), "should not be data range" );
                    assertFalse( or.isOntology(), "should not be ontology" );

                    RDFNode n = or.asIndividual();
                    assertTrue( n instanceof Individual, "Should be individual");
                }
            },
            new OntTestCase( "OntResource.asDataRange", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    if (m.getProfile().DATARANGE() == null) {
                        throw new ProfileException(null,null);
                    }
                    Resource r = m.createResource();
                    r.addProperty( RDF.type, m.getProfile().DATARANGE() );
                    OntResource or = r.as( OntResource.class );

                    assertFalse( or.isAnnotationProperty(), "should not be annotation prop" );
                    assertFalse( or.isAllDifferent(), "should not be all different" );
                    assertFalse( or.isClass(), "should not be class" );
                    assertFalse( or.isProperty(), "should not be property" );
                    assertFalse( or.isObjectProperty(), "should not be object property" );
                    assertFalse( or.isDatatypeProperty(), "should not be datatype property" );
                    assertFalse( or.isIndividual(), "should not be individual" );
                    assertTrue( or.isDataRange(), "should be data range" );
                    assertFalse( or.isOntology(), "should not be ontology" );

                    RDFNode n = or.asDataRange();
                    assertTrue( n instanceof DataRange, "Should be DataRange" );
                }
            },
            new OntTestCase( "OntResource.asOntology", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    if (m.getProfile().ONTOLOGY() == null) {
                        throw new ProfileException(null,null);
                    }
                    Resource r = m.createResource();
                    r.addProperty( RDF.type, m.getProfile().ONTOLOGY() );
                    OntResource or = r.as( OntResource.class );

                    assertFalse( or.isAnnotationProperty(), "should not be annotation prop" );
                    assertFalse( or.isAllDifferent(), "should not be all different" );
                    assertFalse( or.isClass(), "should not be class" );
                    assertFalse( or.isProperty(), "should not be property" );
                    assertFalse( or.isObjectProperty(), "should not be object property" );
                    assertFalse( or.isDatatypeProperty(), "should not be datatype property" );
                    assertFalse( or.isIndividual(), "should not be individual" );
                    assertFalse( or.isDataRange(), "should not be data range" );
                    assertTrue( or.isOntology(), "should be ontology" );

                    RDFNode n = or.asOntology();
                    assertTrue( n instanceof Ontology, "Should be Ontology");
                }
            },
            new OntTestCase( "OntResource.isLanguageTerm", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) {
                    // class is defined (differently) in every profile
                    OntResource or = m.getProfile().CLASS().inModel(m).as( OntResource.class );
                    assertTrue( or.isOntLanguageTerm(), "should be a lang term" );

                    or = m.createOntResource( "http://foo/bar" );
                    assertFalse( or.isOntLanguageTerm(), "should not be a lang term" );
                }
            },
            new OntTestCase( "OntResource.getOntModel", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntResource or = m.createOntResource( "http://foo/bar" );
                    OntModel m0 = or.getOntModel();
                    assertEquals( m, m0 );
                }
            },
            new OntTestCase( "OntResource.getPropertyValue - object prop", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntResource a = m.createOntResource( "http://foo/bar#a" );
                    Resource b = m.createResource( "http://foo/bar#b" );
                    OntProperty p = m.createOntProperty( "http://foo/bar#p" );
                    m.add( a, p, b );
                    Object bb = a.getPropertyValue( p );
                    assertEquals( b, bb );
                    assertTrue( bb instanceof OntResource, "Return value should be an OntResource" );
                }
            },
            new OntTestCase( "OntResource.getPropertyValue - missing prop", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntResource a = m.createOntResource( "http://foo/bar#a" );
                    Resource b = m.createResource( "http://foo/bar#b" );
                    OntProperty p = m.createOntProperty( "http://foo/bar#p" );
                    OntProperty q = m.createOntProperty( "http://foo/bar#q" );
                    m.add( a, p, b );
                    Object bb = a.getPropertyValue( q );
                    assertNull( bb );
                }
            },
            new OntTestCase( "OntResource.listPropertyValues - object prop", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntResource a = m.createOntResource( "http://foo/bar#a" );
                    Resource b = m.createResource( "http://foo/bar#b" );
                    OntProperty p = m.createOntProperty( "http://foo/bar#p" );
                    Literal l = m.createTypedLiteral( false );
                    m.add( a, p, b );
                    m.add( a, p, l );
                    NodeIterator ni = a.listPropertyValues( p );

                    while (ni.hasNext()) {
                        RDFNode n = ni.nextNode();
                        if (n.isResource()) {
                            assertEquals( b, n );
                            assertTrue( n instanceof OntResource, "Return value should be an OntResource" );
                        }
                    }
                }
            },

        };
    }

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
