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
import junit.framework.TestSuite;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;



/**
 * <p>
 * Unit test cases for ontology resources
 * </p>
 */
public class TestResource extends OntTestBase
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
        return new TestResource( "TestResource" );
    }

    public TestResource( String name ) {
        super( name );
    }


    // External signature methods
    //////////////////////////////////

    // Internal implementation methods
    //////////////////////////////////

    @Override
    public OntTestCase[] getTests() {
        return new OntTestCase[] {
            new OntTestCase( "OntResource.sameAs", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntResource a = m.getResource( NS + "a" ).as( OntResource.class );
                    OntResource b = m.getResource( NS + "b" ).as( OntResource.class );
                    OntResource c = m.getResource( NS + "c" ).as( OntResource.class );

                    a.addSameAs( b );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.SAME_AS() ) );
                    assertEquals( "a should be sameAs b", b, a.getSameAs() );

                    a.addSameAs( c );
                    assertEquals( "Cardinality should be 2", 2, a.getCardinality( prof.SAME_AS() ) );
                    iteratorTest( a.listSameAs(), new Object[] {b, c} );

                    assertTrue( "a should be the same as b", a.isSameAs( b ) );
                    assertTrue( "a should be the same as c", a.isSameAs( c ) );

                    a.setSameAs( b );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.SAME_AS() ) );
                    assertEquals( "a should be sameAs b", b, a.getSameAs() );

                    a.removeSameAs( c );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.SAME_AS() ) );
                    a.removeSameAs( b );
                    assertEquals( "Cardinality should be 0", 0, a.getCardinality( prof.SAME_AS() ) );
                }
            },
            new OntTestCase( "OntResource.differentFrom", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntResource a = m.getResource( NS + "a" ).as( OntResource.class );
                    OntResource b = m.getResource( NS + "b" ).as( OntResource.class );
                    OntResource c = m.getResource( NS + "c" ).as( OntResource.class );

                    a.addDifferentFrom( b );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.DIFFERENT_FROM() ) );
                    assertEquals( "a should be differentFrom b", b, a.getDifferentFrom() );

                    a.addDifferentFrom( c );
                    assertEquals( "Cardinality should be 2", 2, a.getCardinality( prof.DIFFERENT_FROM() ) );
                    iteratorTest( a.listDifferentFrom(), new Object[] {b, c} );

                    assertTrue( "a should be diff from b", a.isDifferentFrom( b ) );
                    assertTrue( "a should be diff from c", a.isDifferentFrom( c ) );

                    a.setDifferentFrom( b );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.DIFFERENT_FROM() ) );
                    assertEquals( "a should be differentFrom b", b, a.getDifferentFrom() );

                    a.removeDifferentFrom( c );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.DIFFERENT_FROM() ) );
                    a.removeDifferentFrom( b );
                    assertEquals( "Cardinality should be 0", 0, a.getCardinality( prof.DIFFERENT_FROM() ) );
                }
            },
            new OntTestCase( "OntResource.seeAlso", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntResource a = m.getResource( NS + "a" ).as( OntResource.class );
                    OntResource b = m.getResource( NS + "b" ).as( OntResource.class );
                    OntResource c = m.getResource( NS + "c" ).as( OntResource.class );

                    a.addSeeAlso( b );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.SEE_ALSO() ) );
                    assertEquals( "a should be seeAlso b", b, a.getSeeAlso() );

                    a.addSeeAlso( c );
                    assertEquals( "Cardinality should be 2", 2, a.getCardinality( prof.SEE_ALSO() ) );
                    iteratorTest( a.listSeeAlso(), new Object[] {b, c} );

                    assertTrue( "a should have seeAlso b", a.hasSeeAlso( b ) );
                    assertTrue( "a should have seeAlso c", a.hasSeeAlso( c ) );

                    a.setSeeAlso( b );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.SEE_ALSO() ) );
                    assertEquals( "a should be seeAlso b", b, a.getSeeAlso() );

                    a.removeSeeAlso( c );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.SEE_ALSO() ) );
                    a.removeSeeAlso( b );
                    assertEquals( "Cardinality should be 0", 0, a.getCardinality( prof.SEE_ALSO() ) );
                }
            },
            new OntTestCase( "OntResource.isDefinedBy", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntResource a = m.getResource( NS + "a" ).as( OntResource.class );
                    OntResource b = m.getResource( NS + "b" ).as( OntResource.class );
                    OntResource c = m.getResource( NS + "c" ).as( OntResource.class );

                    a.addIsDefinedBy( b );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.IS_DEFINED_BY() ) );
                    assertEquals( "a should be isDefinedBy b", b, a.getIsDefinedBy() );

                    a.addIsDefinedBy( c );
                    assertEquals( "Cardinality should be 2", 2, a.getCardinality( prof.IS_DEFINED_BY() ) );
                    iteratorTest( a.listIsDefinedBy(), new Object[] {b, c} );

                    assertTrue( "a should be defined by b", a.isDefinedBy( b ) );
                    assertTrue( "a should be defined by c", a.isDefinedBy( c ) );

                    a.setIsDefinedBy( b );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.IS_DEFINED_BY() ) );
                    assertEquals( "a should be isDefinedBy b", b, a.getIsDefinedBy() );

                    a.removeDefinedBy( c );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.IS_DEFINED_BY() ) );
                    a.removeDefinedBy( b );
                    assertEquals( "Cardinality should be 0", 0, a.getCardinality( prof.IS_DEFINED_BY() ) );
            }
            },
            new OntTestCase( "OntResource.versionInfo", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntResource a = m.getResource( NS + "a" ).as( OntResource.class );

                    a.addVersionInfo( "some info" );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.VERSION_INFO() ) );
                    assertEquals( "a has wrong version info", "some info", a.getVersionInfo() );

                    a.addVersionInfo( "more info" );
                    assertEquals( "Cardinality should be 2", 2, a.getCardinality( prof.VERSION_INFO() ) );
                    iteratorTest( a.listVersionInfo(), new Object[] {"some info", "more info"} );

                    assertTrue( "a should have some info", a.hasVersionInfo( "some info" ) );
                    assertTrue( "a should have more info", a.hasVersionInfo( "more info" ) );

                    a.setVersionInfo( "new info" );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.VERSION_INFO() ) );
                    assertEquals( "a has wrong version info", "new info", a.getVersionInfo() );

                    a.removeVersionInfo( "old info" );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.VERSION_INFO() ) );
                    a.removeVersionInfo( "new info" );
                    assertEquals( "Cardinality should be 0", 0, a.getCardinality( prof.VERSION_INFO() ) );
                }
            },
            new OntTestCase( "OntResource.label.nolang", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntResource a = m.getResource( NS + "a" ).as( OntResource.class );

                    a.addLabel( "some info", null );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.LABEL() ) );
                    assertEquals( "a has wrong label", "some info", a.getLabel( null ) );

                    a.addLabel( "more info", null );
                    assertEquals( "Cardinality should be 2", 2, a.getCardinality( prof.LABEL() ) );
                    iteratorTest( a.listLabels( null ), new Object[] {m.createLiteral( "some info" ), m.createLiteral( "more info" )} );

                    assertTrue( "a should have label some info", a.hasLabel( "some info", null ) );
                    assertTrue( "a should have label more info", a.hasLabel( "more info", null ) );

                    a.setLabel( "new info", null );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.LABEL() ) );
                    assertEquals( "a has wrong label", "new info", a.getLabel( null ) );

                    a.removeLabel( "foo", null );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.LABEL() ) );
                    a.removeLabel( "new info", null );
                    assertEquals( "Cardinality should be 0", 0, a.getCardinality( prof.LABEL() ) );
                }
            },
            new OntTestCase( "OntResource.label.lang", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntResource a = m.getResource( NS + "a" ).as( OntResource.class );

                    a.addLabel( "good", "EN" );
                    assertEquals( "wrong label", "good", a.getLabel( null ) );

                    a.addLabel( "bon", "FR" );

                    assertEquals( "wrong label", "good", a.getLabel( "EN" ) );
                    assertEquals( "wrong label", null, a.getLabel( "EN-GB" ) );  // no literal with a specific enough language
                    assertEquals( "wrong label", "bon", a.getLabel( "FR" ) );

                    assertTrue( "a should have label good", a.hasLabel( "good", "EN" ) );
                    assertTrue( "a should have label bon", a.hasLabel( "bon", "FR" ) );
                    assertTrue( "a should note have label good (DE)", !a.hasLabel( "good", "DE" ) );

                    a.addLabel( "spiffing", "EN-GB" );
                    a.addLabel( "duude", "EN-US" );

                    assertEquals( "wrong label", "spiffing", a.getLabel( "EN-GB" ) );
                    assertEquals( "wrong label", "duude", a.getLabel( "EN-US" ) );
                    assertEquals( "wrong label", null, a.getLabel( "DE" ) );

                    a.addLabel( "abcdef", "AB-CD" );
                    assertEquals( "wrong label", "abcdef", a.getLabel( "AB" ) );
                    assertEquals( "wrong label", null, a.getLabel( "AB-XY" ) );

                    a.removeLabel( "abcde", "AB-CD" );
                    assertEquals( "Cardinality should be 5", 5, a.getCardinality( a.getProfile().LABEL() ) );
                    a.removeLabel( "abcdef", "AB-CD" );
                    assertEquals( "Cardinality should be 4", 4, a.getCardinality( a.getProfile().LABEL() ) );
                }
            },
            new OntTestCase( "OntResource.comment.nolang", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntResource a = m.getResource( NS + "a" ).as( OntResource.class );

                    a.addComment( "some info", null );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.COMMENT() ) );
                    assertEquals( "a has wrong comment", "some info", a.getComment( null ) );

                    a.addComment( "more info", null );
                    assertEquals( "Cardinality should be 2", 2, a.getCardinality( prof.COMMENT() ) );
                    iteratorTest( a.listComments( null ), new Object[] {m.createLiteral( "some info" ), m.createLiteral( "more info" )} );

                    assertTrue( "a should have comment some info", a.hasComment( "some info", null ) );
                    assertTrue( "a should have comment more info", a.hasComment( "more info", null ) );

                    a.setComment( "new info", null );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.COMMENT() ) );
                    assertEquals( "a has wrong comment", "new info", a.getComment( null ) );

                    a.removeComment( "foo", null );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( prof.COMMENT() ) );
                    a.removeComment( "new info", null );
                    assertEquals( "Cardinality should be 0", 0, a.getCardinality( prof.COMMENT() ) );
                }
            },
            new OntTestCase( "OntResource.comment.lang", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntResource a = m.getResource( NS + "a" ).as( OntResource.class );

                    a.addComment( "good", "EN" );
                    assertEquals( "wrong comment", "good", a.getComment( null ) );

                    a.addComment( "bon", "FR" );

                    assertEquals( "wrong comment", "good", a.getComment( "EN" ) );
                    assertEquals( "wrong comment", null, a.getComment( "EN-GB" ) );  // no literal with a specific enough language
                    assertEquals( "wrong comment", "bon", a.getComment( "FR" ) );

                    assertTrue( "a should have label good", a.hasComment( "good", "EN" ) );
                    assertTrue( "a should have label bon", a.hasComment( "bon", "FR" ) );
                    assertTrue( "a should note have label good (DE)", !a.hasComment( "good", "DE" ) );

                    a.addComment( "spiffing", "EN-GB" );
                    a.addComment( "duude", "EN-US" );

                    assertEquals( "wrong comment", "spiffing", a.getComment( "EN-GB" ) );
                    assertEquals( "wrong comment", "duude", a.getComment( "EN-US" ) );
                    assertEquals( "wrong comment", null, a.getComment( "DE" ) );

                    a.addComment( "abcdef", "AB-CD" );
                    assertEquals( "wrong comment", "abcdef", a.getComment( "AB" ) );
                    assertEquals( "wrong comment", null, a.getComment( "AB-XY" ) );

                    a.removeComment( "abcde", "AB-CD" );
                    assertEquals( "Cardinality should be 5", 5, a.getCardinality( a.getProfile().COMMENT() ) );
                    a.removeComment( "abcdef", "AB-CD" );
                    assertEquals( "Cardinality should be 4", 4, a.getCardinality( a.getProfile().COMMENT() ) );
                }
            },
            new OntTestCase( "OntResource.type (no inference)", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    A.addSubClass( B );

                    OntResource a = m.getResource( NS + "a" ).as( OntResource.class );
                    assertEquals( "Cardinality of rdf:type is wrong", 0, a.getCardinality( RDF.type ) );

                    a.addRDFType( B );
                    assertEquals( "rdf:type of a is wrong", B, a.getRDFType() );
                    assertEquals( "rdf:type of a is wrong", B, a.getRDFType( false ) );

                    iteratorTest( a.listRDFTypes( false ), new Object[] {B} );       // only B since we're not using an inference model
                    iteratorTest( a.listRDFTypes( true ), new Object[] {B} );

                    a.addRDFType( A );
                    iteratorTest( a.listRDFTypes( false ), new Object[] {A,B} );
                    iteratorTest( a.listRDFTypes( true ), new Object[] {B} );

                    assertTrue( "a should not be of class A direct", !a.hasRDFType( A, true ));
                    assertTrue( "a should not be of class B direct", a.hasRDFType( B, true ));

                    OntClass C = m.createClass( NS + "C" );
                    a.setRDFType( C );
                    assertTrue( "a should be of class C", a.hasRDFType( C, false ));
                    assertTrue( "a should not be of class A", !a.hasRDFType( A, false ));
                    assertTrue( "a should not be of class B", !a.hasRDFType( B, false ));

                    a.removeRDFType( B );
                    assertEquals( "Cardinality should be 1", 1, a.getCardinality( RDF.type ) );
                    a.removeRDFType( C );
                    assertEquals( "Cardinality should be 0", 0, a.getCardinality( RDF.type ) );
                }
            },
            new OntTestCase( "OntResource.remove", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    OntClass C = m.createClass( NS + "C" );
                    OntClass D = m.createClass( NS + "D" );
                    OntClass E = m.createClass( NS + "E" );
                    A.addSubClass( B );
                    A.addSubClass( C );
                    C.addSubClass( D );
                    C.addSubClass( E );

                    assertTrue( "super-class of E", E.hasSuperClass( C, false ) );
                    iteratorTest( A.listSubClasses(), new Object[] {B,C} );

                    C.remove();

                    assertTrue( "super-class of D", !D.hasSuperClass( C, false ) );
                    assertTrue( "super-class of E", !E.hasSuperClass( C, false ) );
                    iteratorTest( A.listSubClasses(), new Object[] {B} );
                }
            },
            new OntTestCase( "OntResource.asClass", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Resource r = m.createResource();
                    r.addProperty( RDF.type, m.getProfile().CLASS() );
                    OntResource or = r.as( OntResource.class );
                    assertFalse( "should not be annotation prop", or.isAnnotationProperty() );
                    assertFalse( "should not be all different", or.isAllDifferent() );
                    assertTrue( "should be class", or.isClass() );
                    assertFalse( "should not be property", or.isProperty() );
                    assertFalse( "should not be object property", or.isObjectProperty() );
                    assertFalse( "should not be datatype property", or.isDatatypeProperty() );
                    assertTrue( "should not be individual", owlFull() || !or.isIndividual() );
                    assertFalse( "should not be data range", or.isDataRange() );
                    assertFalse( "should not be ontology", or.isOntology() );

                    RDFNode n = or.asClass();
                    assertTrue( "Should be OntClass", n instanceof OntClass );
                }
            },
            new OntTestCase( "OntResource.asAnnotationProperty", true, true, false) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    if (m.getProfile().ANNOTATION_PROPERTY() == null) {
                        throw new ProfileException(null,null);
                    }
                    Resource r = m.createResource();
                    r.addProperty( RDF.type, m.getProfile().ANNOTATION_PROPERTY() );
                    OntResource or = r.as( OntResource.class );

                    assertTrue( "should be annotation prop", or.isAnnotationProperty() );
                    assertFalse( "should not be all different", or.isAllDifferent() );
                    assertFalse( "should not be class", or.isClass() );
                    assertTrue( "should be property", or.isProperty() );
                    assertFalse( "should not be object property", or.isObjectProperty() );
                    assertFalse( "should not be datatype property", or.isDatatypeProperty() );
                    assertFalse( "should not be individual", or.isIndividual() );
                    assertFalse( "should not be data range", or.isDataRange() );
                    assertFalse( "should not be ontology", or.isOntology() );

                    RDFNode n = or.asAnnotationProperty();
                    assertTrue( "Should be AnnotationProperty", n instanceof AnnotationProperty);
                }
            },
            new OntTestCase( "OntResource.asObjectProperty", true, true, false) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    if (m.getProfile().OBJECT_PROPERTY() == null) {
                        throw new ProfileException(null,null);
                    }
                    Resource r = m.createResource();
                    r.addProperty( RDF.type, m.getProfile().OBJECT_PROPERTY() );
                    OntResource or = r.as( OntResource.class );

                    assertFalse( "should not be annotation prop", or.isAnnotationProperty() );
                    assertFalse( "should not be all different", or.isAllDifferent() );
                    assertFalse( "should not be class", or.isClass() );
                    assertTrue( "should be property", or.isProperty() );
                    assertTrue( "should be object property", or.isObjectProperty() );
                    assertFalse( "should not be datatype property", or.isDatatypeProperty() );
                    assertFalse( "should not be individual", or.isIndividual() );
                    assertFalse( "should not be data range", or.isDataRange() );
                    assertFalse( "should not be ontology", or.isOntology() );

                    RDFNode n = or.asObjectProperty();
                    assertTrue( "Should be ObjectProperty", n instanceof ObjectProperty);
                }
            },
            new OntTestCase( "OntResource.asDatatypeProperty", true, true, false) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    if (m.getProfile().DATATYPE_PROPERTY() == null) {
                        throw new ProfileException(null,null);
                    }
                    Resource r = m.createResource();
                    r.addProperty( RDF.type, m.getProfile().DATATYPE_PROPERTY() );
                    OntResource or = r.as( OntResource.class );

                    assertFalse( "should not be annotation prop", or.isAnnotationProperty() );
                    assertFalse( "should not be all different", or.isAllDifferent() );
                    assertFalse( "should not be class", or.isClass() );
                    assertTrue( "should be property", or.isProperty() );
                    assertFalse( "should not be object property", or.isObjectProperty() );
                    assertTrue( "should be datatype property", or.isDatatypeProperty() );
                    assertFalse( "should not be individual", or.isIndividual() );
                    assertFalse( "should not be data range", or.isDataRange() );
                    assertFalse( "should not be ontology", or.isOntology() );

                    RDFNode n = or.asDatatypeProperty();
                    assertTrue( "Should be DatatypeProperty", n instanceof DatatypeProperty);
                }
            },
            new OntTestCase( "OntResource.asAllDifferent", true, true, false) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    if (m.getProfile().ALL_DIFFERENT() == null) {
                        throw new ProfileException(null,null);
                    }
                    Resource r = m.createResource();
                    r.addProperty( RDF.type, m.getProfile().ALL_DIFFERENT() );
                    OntResource or = r.as( OntResource.class );

                    assertFalse( "should not be annotation prop", or.isAnnotationProperty() );
                    assertTrue( "should be all different", or.isAllDifferent() );
                    assertFalse( "should not be class", or.isClass() );
                    assertFalse( "should not be property", or.isProperty() );
                    assertFalse( "should not be object property", or.isObjectProperty() );
                    assertFalse( "should not be datatype property", or.isDatatypeProperty() );
                    assertFalse( "should not be individual", or.isIndividual() );
                    assertFalse( "should not be data range", or.isDataRange() );
                    assertFalse( "should not be ontology", or.isOntology() );

                    RDFNode n = or.asAllDifferent();
                    assertTrue( "Should be AnnotationProperty", n instanceof AllDifferent);
                }
            },
            new OntTestCase( "OntResource.asProperty", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Resource r = m.createResource();
                    r.addProperty( RDF.type, m.getProfile().PROPERTY() );
                    OntResource or = r.as( OntResource.class );

                    assertFalse( "should not be annotation prop", or.isAnnotationProperty() );
                    assertFalse( "should not be all different", or.isAllDifferent() );
                    assertFalse( "should not be class", or.isClass() );
                    assertTrue( "should be property", or.isProperty() );
                    assertFalse( "should not be object property", or.isObjectProperty() );
                    assertFalse( "should not be datatype property", or.isDatatypeProperty() );
                    assertFalse( "should not be individual", or.isIndividual() );
                    assertFalse( "should not be data range", or.isDataRange() );
                    assertFalse( "should not be ontology", or.isOntology() );

                    RDFNode n = or.asProperty();
                    assertTrue( "Should be OntProperty", n instanceof OntProperty);
                }
            },
            new OntTestCase( "OntResource.asIndividual", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Resource r = m.createResource();
                    Resource s = m.createResource();
                    s.addProperty( RDF.type, m.getProfile().CLASS() );
                    r.addProperty( RDF.type, s );
                    OntResource or = r.as( OntResource.class );

                    assertFalse( "should not be annotation prop", or.isAnnotationProperty() );
                    assertFalse( "should not be all different", or.isAllDifferent() );
                    assertFalse( "should not be class", or.isClass() );
                    assertFalse( "should not be property", or.isProperty() );
                    assertFalse( "should not be object property", or.isObjectProperty() );
                    assertFalse( "should not be datatype property", or.isDatatypeProperty() );
                    assertTrue( "should be individual", or.isIndividual() );
                    assertFalse( "should not be data range", or.isDataRange() );
                    assertFalse( "should not be ontology", or.isOntology() );

                    RDFNode n = or.asIndividual();
                    assertTrue( "Should be individual", n instanceof Individual);
                }
            },
            new OntTestCase( "OntResource.asDataRange", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    if (m.getProfile().DATARANGE() == null) {
                        throw new ProfileException(null,null);
                    }
                    Resource r = m.createResource();
                    r.addProperty( RDF.type, m.getProfile().DATARANGE() );
                    OntResource or = r.as( OntResource.class );

                    assertFalse( "should not be annotation prop", or.isAnnotationProperty() );
                    assertFalse( "should not be all different", or.isAllDifferent() );
                    assertFalse( "should not be class", or.isClass() );
                    assertFalse( "should not be property", or.isProperty() );
                    assertFalse( "should not be object property", or.isObjectProperty() );
                    assertFalse( "should not be datatype property", or.isDatatypeProperty() );
                    assertFalse( "should not be individual", or.isIndividual() );
                    assertTrue( "should be data range", or.isDataRange() );
                    assertFalse( "should not be ontology", or.isOntology() );

                    RDFNode n = or.asDataRange();
                    assertTrue( "Should be DataRange", n instanceof DataRange );
                }
            },
            new OntTestCase( "OntResource.asOntology", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    if (m.getProfile().ONTOLOGY() == null) {
                        throw new ProfileException(null,null);
                    }
                    Resource r = m.createResource();
                    r.addProperty( RDF.type, m.getProfile().ONTOLOGY() );
                    OntResource or = r.as( OntResource.class );

                    assertFalse( "should not be annotation prop", or.isAnnotationProperty() );
                    assertFalse( "should not be all different", or.isAllDifferent() );
                    assertFalse( "should not be class", or.isClass() );
                    assertFalse( "should not be property", or.isProperty() );
                    assertFalse( "should not be object property", or.isObjectProperty() );
                    assertFalse( "should not be datatype property", or.isDatatypeProperty() );
                    assertFalse( "should not be individual", or.isIndividual() );
                    assertFalse( "should not be data range", or.isDataRange() );
                    assertTrue( "should be ontology", or.isOntology() );

                    RDFNode n = or.asOntology();
                    assertTrue( "Should be Ontology", n instanceof Ontology);
                }
            },
            new OntTestCase( "OntResource.isLanguageTerm", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    // class is defined (differently) in every profile
                    OntResource or = m.getProfile().CLASS().inModel(m).as( OntResource.class );
                    assertTrue( "should be a lang term", or.isOntLanguageTerm() );

                    or = m.createOntResource( "http://foo/bar" );
                    assertFalse( "should not be a lang term", or.isOntLanguageTerm() );
                }
            },
            new OntTestCase( "OntResource.getOntModel", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntResource or = m.createOntResource( "http://foo/bar" );
                    OntModel m0 = or.getOntModel();
                    assertEquals( m, m0 );
                }
            },
            new OntTestCase( "OntResource.getPropertyValue - object prop", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntResource a = m.createOntResource( "http://foo/bar#a" );
                    Resource b = m.createResource( "http://foo/bar#b" );
                    OntProperty p = m.createOntProperty( "http://foo/bar#p" );
                    m.add( a, p, b );
                    Object bb = a.getPropertyValue( p );
                    assertEquals( b, bb );
                    assertTrue( "Return value should be an OntResource", bb instanceof OntResource );
                }
            },
            new OntTestCase( "OntResource.getPropertyValue - missing prop", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
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
                public void ontTest( OntModel m ) throws Exception {
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
                            assertTrue( "Return value should be an OntResource", n instanceof OntResource );
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
