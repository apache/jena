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
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.util.iterator.NullIterator;
import com.hp.hpl.jena.vocabulary.*;

import junit.framework.*;


/**
 * <p>
 * Unit tests for OntClass and other class expressions.
 * </p>
 */
public class TestClassExpression
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
        return new TestClassExpression( "TestClassExpression" );
    }

    public TestClassExpression( String name ) {
        super( name );
    }


    // External signature methods
    //////////////////////////////////

    @Override
    public OntTestCase[] getTests() {
        return new OntTestCase[] {
            new OntTestCase( "OntClass.super-class", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    OntClass C = m.createClass( NS + "C" );

                    A.addSuperClass( B );
                    assertEquals( "Cardinality should be 1", 1, A.getCardinality( prof.SUB_CLASS_OF() ) );
                    assertEquals( "A should have super-class B", B, A.getSuperClass() );

                    A.addSuperClass( C );
                    assertEquals( "Cardinality should be 2", 2, A.getCardinality( prof.SUB_CLASS_OF() ) );
                    iteratorTest( A.listSuperClasses(), new Object[] {C, B} );

                    A.setSuperClass( C );
                    assertEquals( "Cardinality should be 1", 1, A.getCardinality( prof.SUB_CLASS_OF() ) );
                    assertEquals( "A shuold have super-class C", C, A.getSuperClass() );
                    assertTrue( "A shuold not have super-class B", !A.hasSuperClass( B, false ) );

                    A.removeSuperClass( B );
                    assertEquals( "Cardinality should be 1", 1, A.getCardinality( prof.SUB_CLASS_OF() ) );
                    A.removeSuperClass( C );
                    assertEquals( "Cardinality should be 0", 0, A.getCardinality( prof.SUB_CLASS_OF() ) );
                }
            },
            new OntTestCase( "OntClass.sub-class", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    OntClass C = m.createClass( NS + "C" );

                    A.addSubClass( B );
                    assertEquals( "Cardinality should be 1", 1, B.getCardinality( prof.SUB_CLASS_OF() ) );
                    assertEquals( "A should have sub-class B", B, A.getSubClass() );

                    A.addSubClass( C );
                    assertEquals( "Cardinality should be 2", 2, B.getCardinality( prof.SUB_CLASS_OF() ) + C.getCardinality( prof.SUB_CLASS_OF() ) );
                    iteratorTest( A.listSubClasses(), new Object[] {C, B} );

                    A.setSubClass( C );
                    assertEquals( "Cardinality should be 1", 1, B.getCardinality( prof.SUB_CLASS_OF() ) + C.getCardinality( prof.SUB_CLASS_OF() ) );
                    assertEquals( "A shuold have sub-class C", C, A.getSubClass() );
                    assertTrue( "A shuold not have sub-class B", !A.hasSubClass( B, false ) );

                    A.removeSubClass( B );
                    assertTrue( "A should have sub-class C", A.hasSubClass( C, false ) );
                    A.removeSubClass( C );
                    assertTrue( "A should not have sub-class C", !A.hasSubClass( C, false ) );
                }
            },
            new OntTestCase( "OntClass.equivalentClass", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    OntClass C = m.createClass( NS + "C" );

                    A.addEquivalentClass( B );
                    assertEquals( "Cardinality should be 1", 1, A.getCardinality( prof.EQUIVALENT_CLASS() ) );
                    assertEquals( "A have equivalentClass B", B, A.getEquivalentClass() );

                    A.addEquivalentClass( C );
                    assertEquals( "Cardinality should be 2", 2, A.getCardinality( prof.EQUIVALENT_CLASS() ) );
                    iteratorTest( A.listEquivalentClasses(), new Object[] {C, B} );

                    A.setEquivalentClass( C );
                    assertEquals( "Cardinality should be 1", 1, A.getCardinality( prof.EQUIVALENT_CLASS() ) );
                    assertEquals( "A should have equivalentClass C", C, A.getEquivalentClass() );
                    assertTrue( "A should not have equivalentClass B", !A.hasEquivalentClass( B ) );

                    A.removeEquivalentClass( B );
                    assertEquals( "Cardinality should be 1", 1, A.getCardinality( prof.EQUIVALENT_CLASS() ) );
                    A.removeEquivalentClass( C );
                    assertEquals( "Cardinality should be 0", 0, A.getCardinality( prof.EQUIVALENT_CLASS() ) );
                }
            },
            new OntTestCase( "OntClass.disjointWith", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    OntClass C = m.createClass( NS + "C" );

                    A.addDisjointWith( B );
                    assertEquals( "Cardinality should be 1", 1, A.getCardinality( prof.DISJOINT_WITH() ) );
                    assertEquals( "A have be disjoint with B", B, A.getDisjointWith() );

                    A.addDisjointWith( C );
                    assertEquals( "Cardinality should be 2", 2, A.getCardinality( prof.DISJOINT_WITH() ) );
                    iteratorTest( A.listDisjointWith(), new Object[] {C,B} );

                    A.setDisjointWith( C );
                    assertEquals( "Cardinality should be 1", 1, A.getCardinality( prof.DISJOINT_WITH() ) );
                    assertEquals( "A should be disjoint with C", C, A.getDisjointWith() );
                    assertTrue( "A should not be disjoint with B", !A.isDisjointWith( B ) );

                    A.removeDisjointWith( B );
                    assertEquals( "Cardinality should be 1", 1, A.getCardinality( prof.DISJOINT_WITH() ) );
                    A.removeDisjointWith( C );
                    assertEquals( "Cardinality should be 0", 0, A.getCardinality( prof.DISJOINT_WITH() ) );
                }
            },
            new OntTestCase( "EnumeratedClass.oneOf", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    EnumeratedClass A = m.createEnumeratedClass( NS + "A", null );
                    OntResource a = m.getResource( NS + "a" ).as( OntResource.class );
                    OntResource b = m.getResource( NS + "b" ).as( OntResource.class );

                    A.addOneOf( a );
                    assertEquals( "Cardinality should be 1", 1, A.getCardinality( prof.ONE_OF() ) );
                    assertEquals( "Size should be 1", 1, A.getOneOf().size() );
                    assertTrue( "A should have a as enumerated member", A.getOneOf().contains( a ) );

                    A.addOneOf( b );
                    assertEquals( "Cardinality should be 1", 1, A.getCardinality( prof.ONE_OF() ) );
                    assertEquals( "Size should be 2", 2, A.getOneOf().size() );
                    iteratorTest( A.listOneOf(), new Object[] {a,b} );

                    A.setOneOf( m.createList( new RDFNode[] {b} ) );
                    assertEquals( "Cardinality should be 1", 1, A.getCardinality( prof.ONE_OF() ) );
                    assertEquals( "Size should be 1", 1, A.getOneOf().size() );
                    assertTrue( "A should have b in the enum", A.hasOneOf( b ) );
                    assertTrue( "A should not have a in the enum", !A.hasOneOf( a ) );

                    A.removeOneOf( a );
                    assertTrue( "Should have b as an enum value", A.hasOneOf( b ) );
                    A.removeOneOf( b );
                    assertTrue( "Should not have b as an enum value", !A.hasOneOf( b ) );
                }
            },
            new OntTestCase( "IntersectionClass.intersectionOf", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    IntersectionClass A = m.createIntersectionClass( NS + "A", null );
                    OntClass B = m.createClass( NS + "B" );
                    OntClass C = m.createClass( NS + "C" );

                    A.addOperand( B );
                    assertEquals( "Cardinality should be 1", 1, A.getCardinality( prof.INTERSECTION_OF() ) );
                    assertEquals( "Size should be 1", 1, A.getOperands().size() );
                    assertTrue( "A should have a as intersection member", A.getOperands().contains( B ) );

                    A.addOperand( C );
                    assertEquals( "Cardinality should be 1", 1, A.getCardinality( prof.INTERSECTION_OF() ) );
                    assertEquals( "Size should be 2", 2, A.getOperands().size() );
                    iteratorTest( A.listOperands(), new Object[] {B,C} );

                    ClosableIterator<? extends Resource> i = A.listOperands();
                    assertTrue( "Argument should be an OntClass", i.next() instanceof OntClass );
                    i.close();

                    A.setOperands( m.createList( new RDFNode[] {C} ) );
                    assertEquals( "Cardinality should be 1", 1, A.getCardinality( prof.INTERSECTION_OF() ) );
                    assertEquals( "Size should be 1", 1, A.getOperands().size() );
                    assertTrue( "A should have C in the intersection", A.hasOperand( C ) );
                    assertTrue( "A should not have B in the intersection", !A.hasOperand( B ) );

                    A.removeOperand( B );
                    assertTrue( "Should have C as an operand", A.hasOperand( C ) );
                    A.removeOperand( C );
                    assertTrue( "Should not have C as an operand", !A.hasOperand( C ) );
                }
            },
            new OntTestCase( "UnionClass.unionOf", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    UnionClass A = m.createUnionClass( NS + "A", null );
                    OntClass B = m.createClass( NS + "B" );
                    OntClass C = m.createClass( NS + "C" );

                    A.addOperand( B );
                    assertEquals( "Cardinality should be 1", 1, A.getCardinality( prof.UNION_OF() ) );
                    assertEquals( "Size should be 1", 1, A.getOperands().size() );
                    assertTrue( "A should have a as union member", A.getOperands().contains( B ) );

                    A.addOperand( C );
                    assertEquals( "Cardinality should be 1", 1, A.getCardinality( prof.UNION_OF() ) );
                    assertEquals( "Size should be 2", 2, A.getOperands().size() );
                    iteratorTest( A.listOperands(), new Object[] {B,C} );

                    ClosableIterator<? extends Resource> i = A.listOperands();
                    assertTrue( "Argument should be an OntClass", i.next() instanceof OntClass );
                    i.close();

                    A.setOperands( m.createList( new RDFNode[] {C} ) );
                    assertEquals( "Cardinality should be 1", 1, A.getCardinality( prof.UNION_OF() ) );
                    assertEquals( "Size should be 1", 1, A.getOperands().size() );
                    assertTrue( "A should have C in the union", A.hasOperand( C ) );
                    assertTrue( "A should not have B in the union", !A.hasOperand( B ) );

                    A.removeOperand( B );
                    assertTrue( "Should have C as an operand", A.hasOperand( C ) );
                    A.removeOperand( C );
                    assertTrue( "Should not have C as an operand", !A.hasOperand( C ) );
                }
            },
            new OntTestCase( "ComplementClass.complementOf", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    ComplementClass A = m.createComplementClass( NS + "A", null );
                    OntClass B = m.createClass( NS + "B" );
                    OntClass C = m.createClass( NS + "C" );
                    boolean ex = false;

                    try { A.addOperand( B ); } catch (UnsupportedOperationException e) {ex = true;}
                    assertTrue( "Should fail to add to a complement", ex );

                    ex = false;
                    try { A.addOperands( new NullIterator<Resource>() ); } catch (UnsupportedOperationException e) {ex = true;}
                    assertTrue( "Should fail to add to a complement", ex );

                    ex = false;
                    try { A.setOperands( m.createList( new RDFNode[] {C} ) ); } catch (UnsupportedOperationException e) {ex = true;}
                    assertTrue( "Should fail to set a list to a complement", ex );

                    A.setOperand( B );
                    assertEquals( "Cardinality should be 1", 1, A.getCardinality( prof.COMPLEMENT_OF() ) );
                    assertEquals( "Complement should be B", B, A.getOperand() );
                    iteratorTest( A.listOperands(), new Object[] {B} );

                    A.setOperand( C );
                    assertEquals( "Cardinality should be 1", 1, A.getCardinality( prof.COMPLEMENT_OF() ) );
                    assertTrue( "A should have C in the complement", A.hasOperand( C ) );
                    assertTrue( "A should not have B in the complement", !A.hasOperand( B ) );

                    A.removeOperand( B );
                    assertTrue( "Should have C as an operand", A.hasOperand( C ) );
                    A.removeOperand( C );
                    assertTrue( "Should not have C as an operand", !A.hasOperand( C ) );
                }
            },
            new OntTestCase( "Restriction.onProperty", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createObjectProperty( NS + "p" );
                    OntProperty q = m.createObjectProperty( NS + "q" );
                    OntClass B = m.createClass( NS + "B" );

                    Restriction A = m.createAllValuesFromRestriction( NS + "A", p, B  );

                    assertEquals( "Restriction should be on property p", p, A.getOnProperty() );
                    assertTrue( "Restriction should be on property p", A.onProperty( p ) );
                    assertTrue( "Restriction should not be on property q", !A.onProperty( q ) );
                    assertEquals( "cardinality should be 1 ", 1, A.getCardinality( prof.ON_PROPERTY() ));

                    A.setOnProperty( q );

                    assertEquals( "Restriction should be on property q", q, A.getOnProperty() );
                    assertTrue( "Restriction should not be on property p", !A.onProperty( p ) );
                    assertTrue( "Restriction should not on property q", A.onProperty( q ) );
                    assertEquals( "cardinality should be 1 ", 1, A.getCardinality( prof.ON_PROPERTY() ));

                    A.removeOnProperty( p );
                    assertTrue( "Should have q as on property", A.onProperty( q ) );
                    A.removeOnProperty( q );
                    assertTrue( "Should not have q as on property", !A.onProperty( q ) );
                }
            },
            new OntTestCase( "AllValuesFromRestriction.allValuesFrom", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createObjectProperty( NS + "p" );
                    OntClass B = m.createClass( NS + "B" );
                    OntClass C = m.createClass( NS + "C" );

                    AllValuesFromRestriction A = m.createAllValuesFromRestriction( NS + "A", p, B  );

                    assertEquals( "Restriction should be all values from B", B, A.getAllValuesFrom() );
                    assertTrue( "Restriction should be all values from B", A.hasAllValuesFrom( B ) );
                    assertTrue( "Restriction should not be all values from C", !A.hasAllValuesFrom( C ) );
                    assertEquals( "cardinality should be 1 ", 1, A.getCardinality( prof.ALL_VALUES_FROM() ));

                    A.setAllValuesFrom( C );

                    assertEquals( "Restriction should be all values from C", C, A.getAllValuesFrom() );
                    assertTrue( "Restriction should not be all values from B", !A.hasAllValuesFrom( B ) );
                    assertTrue( "Restriction should be all values from C", A.hasAllValuesFrom( C ) );
                    assertEquals( "cardinality should be 1 ", 1, A.getCardinality( prof.ALL_VALUES_FROM() ));

                    A.removeAllValuesFrom( C );

                    assertTrue( "Restriction should not be some values from C", !A.hasAllValuesFrom( C ) );
                    assertEquals( "cardinality should be 0 ", 0, A.getCardinality( prof.ALL_VALUES_FROM() ));
                }
            },
            new OntTestCase( "AllValuesFromRestriction.allValuesFrom.datatype", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createObjectProperty( NS + "p" );

                    AllValuesFromRestriction A = m.createAllValuesFromRestriction( NS + "A", p, XSD.gDay  );

                    assertEquals( "Restriction should be all values from gDay", XSD.gDay, A.getAllValuesFrom() );
                    assertTrue( "Restriction should be all values from gDay", A.hasAllValuesFrom( XSD.gDay ) );
                    assertTrue( "Restriction should not be all values from decimal", !A.hasAllValuesFrom( XSD.decimal ) );
                    assertEquals( "cardinality should be 1 ", 1, A.getCardinality( prof.ALL_VALUES_FROM() ));

                    A.setAllValuesFrom( XSD.gMonth );

                    assertEquals( "Restriction should be all values from gMonth", XSD.gMonth, A.getAllValuesFrom() );
                    assertTrue( "Restriction should not be all values from gDay", !A.hasAllValuesFrom( XSD.gDay ) );
                    assertTrue( "Restriction should be all values from gMonth", A.hasAllValuesFrom( XSD.gMonth ) );
                    assertEquals( "cardinality should be 1 ", 1, A.getCardinality( prof.ALL_VALUES_FROM() ));

                    A.removeAllValuesFrom( XSD.gMonth );

                    assertTrue( "Restriction should not be some values from gMonth", !A.hasAllValuesFrom( XSD.gMonth ) );
                    assertEquals( "cardinality should be 0 ", 0, A.getCardinality( prof.ALL_VALUES_FROM() ));
                }
            },
            new OntTestCase( "AllValuesFromRestriction.allValuesFrom.literal", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createObjectProperty( NS + "p" );

                    AllValuesFromRestriction A = m.createAllValuesFromRestriction( NS + "A", p, RDFS.Literal  );

                    assertEquals( "Restriction should be all values from literal", RDFS.Literal, A.getAllValuesFrom() );
                    assertTrue( "Restriction should be all values from literal", A.hasAllValuesFrom( RDFS.Literal ) );
                    assertTrue( "Restriction should not be all values from decimal", !A.hasAllValuesFrom( XSD.decimal ) );
                    assertEquals( "cardinality should be 1 ", 1, A.getCardinality( prof.ALL_VALUES_FROM() ));
                }
            },
            new OntTestCase( "AllValuesFromRestriction.allValuesFrom.datarange", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    Literal x = m.createTypedLiteral( 1 );
                    Literal y = m.createTypedLiteral( 2 );
                    DataRange dr = m.createDataRange( m.createList( new RDFNode[] {x, y} ) );
                    OntProperty p = m.createObjectProperty( NS + "p" );

                    AllValuesFromRestriction A = m.createAllValuesFromRestriction( NS + "A", p, dr  );

                    assertEquals( "Restriction should be all values from dr", dr, A.getAllValuesFrom() );
                    assertTrue( "value should be a datarange", A.getAllValuesFrom() instanceof DataRange );
                    assertTrue( "Restriction should be all values from dr", A.hasAllValuesFrom( dr ) );
                    assertTrue( "Restriction should not be all values from decimal", !A.hasAllValuesFrom( XSD.decimal ) );
                    assertEquals( "cardinality should be 1 ", 1, A.getCardinality( prof.ALL_VALUES_FROM() ));

                    A.removeAllValuesFrom( dr );

                    assertTrue( "Restriction should not be some values from gMonth", !A.hasAllValuesFrom( dr ) );
                    assertEquals( "cardinality should be 0 ", 0, A.getCardinality( prof.ALL_VALUES_FROM() ));
                }
            },
            new OntTestCase( "HasValueRestriction.hasValue", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createObjectProperty( NS + "p" );
                    OntClass B = m.createClass( NS + "B" );
                    Individual b = m.createIndividual( B );
                    OntClass C = m.createClass( NS + "C" );
                    Individual c = m.createIndividual( C );

                    HasValueRestriction A = m.createHasValueRestriction( NS + "A", p, b  );

                    assertEquals( "Restriction should be has value b", b, A.getHasValue() );
                    assertTrue( A.getHasValue() instanceof Individual );
                    assertTrue( "Restriction should be to have value b", A.hasValue( b ) );
                    assertTrue( "Restriction should not be have value c", !A.hasValue( c ) );
                    assertEquals( "cardinality should be 1 ", 1, A.getCardinality( prof.HAS_VALUE() ));

                    A.setHasValue( c );

                    assertEquals( "Restriction should be has value c", c, A.getHasValue() );
                    assertTrue( "Restriction should not be to have value b", !A.hasValue( b ) );
                    assertTrue( "Restriction should not be have value c", A.hasValue( c ) );
                    assertEquals( "cardinality should be 1 ", 1, A.getCardinality( prof.HAS_VALUE() ));

                    A.removeHasValue( c );

                    assertTrue( "Restriction should not be to have value b", !A.hasValue( b ) );
                    assertTrue( "Restriction should not be have value c", !A.hasValue( c ) );
                    assertEquals( "cardinality should be 0 ", 0, A.getCardinality( prof.HAS_VALUE() ));
                }
            },
            new OntTestCase( "SomeValuesFromRestriction.someValuesFrom", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createObjectProperty( NS + "p" );
                    OntClass B = m.createClass( NS + "B" );
                    OntClass C = m.createClass( NS + "C" );

                    SomeValuesFromRestriction A = m.createSomeValuesFromRestriction( NS + "A", p, B  );

                    assertEquals( "Restriction should be some values from B", B, A.getSomeValuesFrom() );
                    assertTrue( "Restriction should be some values from B", A.hasSomeValuesFrom( B ) );
                    assertTrue( "Restriction should not be some values from C", !A.hasSomeValuesFrom( C ) );
                    assertEquals( "cardinality should be 1 ", 1, A.getCardinality( prof.SOME_VALUES_FROM() ));

                    A.setSomeValuesFrom( C );

                    assertEquals( "Restriction should be some values from C", C, A.getSomeValuesFrom() );
                    assertTrue( "Restriction should not be some values from B", !A.hasSomeValuesFrom( B ) );
                    assertTrue( "Restriction should be some values from C", A.hasSomeValuesFrom( C ) );
                    assertEquals( "cardinality should be 1 ", 1, A.getCardinality( prof.SOME_VALUES_FROM() ));

                    A.removeSomeValuesFrom( C );

                    assertTrue( "Restriction should not be some values from C", !A.hasSomeValuesFrom( C ) );
                    assertEquals( "cardinality should be 0 ", 0, A.getCardinality( prof.SOME_VALUES_FROM() ));
                }
            },
            new OntTestCase( "SomeValuesFromRestriction.SomeValuesFrom.datatype", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createObjectProperty( NS + "p" );

                    SomeValuesFromRestriction A = m.createSomeValuesFromRestriction( NS + "A", p, XSD.gDay  );

                    assertEquals( "Restriction should be some values from gDay", XSD.gDay, A.getSomeValuesFrom() );
                    assertTrue( "Restriction should be some values from gDay", A.hasSomeValuesFrom( XSD.gDay ) );
                    assertTrue( "Restriction should not be some values from decimal", !A.hasSomeValuesFrom( XSD.decimal ) );
                    assertEquals( "cardinality should be 1 ", 1, A.getCardinality( prof.SOME_VALUES_FROM() ));

                    A.setSomeValuesFrom( XSD.gMonth );

                    assertEquals( "Restriction should be some values from gMonth", XSD.gMonth, A.getSomeValuesFrom() );
                    assertTrue( "Restriction should not be some values from gDay", !A.hasSomeValuesFrom( XSD.gDay ) );
                    assertTrue( "Restriction should be some values from gMonth", A.hasSomeValuesFrom( XSD.gMonth ) );
                    assertEquals( "cardinality should be 1 ", 1, A.getCardinality( prof.SOME_VALUES_FROM() ));

                    A.removeSomeValuesFrom( XSD.gMonth );

                    assertTrue( "Restriction should not be some values from gMonth", !A.hasSomeValuesFrom( XSD.gMonth ) );
                    assertEquals( "cardinality should be 0 ", 0, A.getCardinality( prof.SOME_VALUES_FROM() ));
                }
            },
            new OntTestCase( "SomeValuesFromRestriction.SomeValuesFrom.literal", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createObjectProperty( NS + "p" );

                    SomeValuesFromRestriction A = m.createSomeValuesFromRestriction( NS + "A", p, RDFS.Literal  );

                    assertEquals( "Restriction should be some values from literal", RDFS.Literal, A.getSomeValuesFrom() );
                    assertTrue( "Restriction should be some values from literal", A.hasSomeValuesFrom( RDFS.Literal ) );
                    assertTrue( "Restriction should not be some values from decimal", !A.hasSomeValuesFrom( XSD.decimal ) );
                    assertEquals( "cardinality should be 1 ", 1, A.getCardinality( prof.SOME_VALUES_FROM() ));
                }
            },
            new OntTestCase( "SomeValuesFromRestriction.SomeValuesFrom.datarange", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    Literal x = m.createTypedLiteral( 1 );
                    Literal y = m.createTypedLiteral( 2 );
                    DataRange dr = m.createDataRange( m.createList( new RDFNode[] {x, y} ) );
                    OntProperty p = m.createObjectProperty( NS + "p" );

                    SomeValuesFromRestriction A = m.createSomeValuesFromRestriction( NS + "A", p, dr  );

                    assertEquals( "Restriction should be some values from dr", dr, A.getSomeValuesFrom() );
                    assertTrue( "value should be a datarange", A.getSomeValuesFrom() instanceof DataRange );
                    assertTrue( "Restriction should be some values from dr", A.hasSomeValuesFrom( dr ) );
                    assertTrue( "Restriction should not be some values from decimal", !A.hasSomeValuesFrom( XSD.decimal ) );
                    assertEquals( "cardinality should be 1 ", 1, A.getCardinality( prof.SOME_VALUES_FROM() ));

                    A.removeSomeValuesFrom( dr );

                    assertTrue( "Restriction should not be some values from gMonth", !A.hasSomeValuesFrom( dr ) );
                    assertEquals( "cardinality should be 0 ", 0, A.getCardinality( prof.SOME_VALUES_FROM() ));
                }
            },
            new OntTestCase( "CardinalityRestriction.cardinality", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createObjectProperty( NS + "p" );

                    CardinalityRestriction A = m.createCardinalityRestriction( NS + "A", p, 3  );

                    assertEquals( "Restriction should be cardinality 3", 3, A.getCardinality() );
                    assertTrue( "Restriction should be cardinality 3", A.hasCardinality( 3 ) );
                    assertTrue( "Restriction should not be cardinality 2", !A.hasCardinality( 2 ) );
                    assertEquals( "cardinality should be 1 ", 1, A.getCardinality( prof.CARDINALITY() ));

                    A.setCardinality( 2 );

                    assertEquals( "Restriction should be cardinality 2", 2, A.getCardinality() );
                    assertTrue( "Restriction should not be cardinality 3", !A.hasCardinality( 3 ) );
                    assertTrue( "Restriction should be cardinality 2", A.hasCardinality( 2 ) );
                    assertEquals( "cardinality should be 1 ", 1, A.getCardinality( prof.CARDINALITY() ));

                    A.removeCardinality( 2 );

                    assertTrue( "Restriction should not be cardinality 3", !A.hasCardinality( 3 ) );
                    assertTrue( "Restriction should not be cardinality 2", !A.hasCardinality( 2 ) );
                    assertEquals( "cardinality should be 0 ", 0, A.getCardinality( prof.CARDINALITY() ));
                }
            },
            new OntTestCase( "MinCardinalityRestriction.minCardinality", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createObjectProperty( NS + "p" );

                    MinCardinalityRestriction A = m.createMinCardinalityRestriction( NS + "A", p, 3  );

                    assertEquals( "Restriction should be min cardinality 3", 3, A.getMinCardinality() );
                    assertTrue( "Restriction should be min cardinality 3", A.hasMinCardinality( 3 ) );
                    assertTrue( "Restriction should not be min cardinality 2", !A.hasMinCardinality( 2 ) );
                    assertEquals( "cardinality should be 1 ", 1, A.getCardinality( prof.MIN_CARDINALITY() ));

                    A.setMinCardinality( 2 );

                    assertEquals( "Restriction should be min cardinality 2", 2, A.getMinCardinality() );
                    assertTrue( "Restriction should not be min cardinality 3", !A.hasMinCardinality( 3 ) );
                    assertTrue( "Restriction should be min cardinality 2", A.hasMinCardinality( 2 ) );
                    assertEquals( "cardinality should be 1 ", 1, A.getCardinality( prof.MIN_CARDINALITY() ));

                    A.removeMinCardinality( 2 );

                    assertTrue( "Restriction should not be cardinality 3", !A.hasMinCardinality( 3 ) );
                    assertTrue( "Restriction should not be cardinality 2", !A.hasMinCardinality( 2 ) );
                    assertEquals( "cardinality should be 0 ", 0, A.getCardinality( prof.MIN_CARDINALITY() ));
                }
            },
            new OntTestCase( "MaxCardinalityRestriction.maxCardinality", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createObjectProperty( NS + "p" );

                    MaxCardinalityRestriction A = m.createMaxCardinalityRestriction( NS + "A", p, 3  );

                    assertEquals( "Restriction should be max cardinality 3", 3, A.getMaxCardinality() );
                    assertTrue( "Restriction should be max cardinality 3", A.hasMaxCardinality( 3 ) );
                    assertTrue( "Restriction should not be max cardinality 2", !A.hasMaxCardinality( 2 ) );
                    assertEquals( "cardinality should be 1 ", 1, A.getCardinality( prof.MAX_CARDINALITY() ));

                    A.setMaxCardinality( 2 );

                    assertEquals( "Restriction should be max cardinality 2", 2, A.getMaxCardinality() );
                    assertTrue( "Restriction should not be max cardinality 3", !A.hasMaxCardinality( 3 ) );
                    assertTrue( "Restriction should be max cardinality 2", A.hasMaxCardinality( 2 ) );
                    assertEquals( "cardinality should be 1 ", 1, A.getCardinality( prof.MAX_CARDINALITY() ));

                    A.removeMaxCardinality( 2 );

                    assertTrue( "Restriction should not be cardinality 3", !A.hasMaxCardinality( 3 ) );
                    assertTrue( "Restriction should not be cardinality 2", !A.hasMaxCardinality( 2 ) );
                    assertEquals( "cardinality should be 0 ", 0, A.getCardinality( prof.MAX_CARDINALITY() ));
                }
            },
            new OntTestCase( "QualifiedRestriction.hasClassQ", false, false, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntProperty p = m.createObjectProperty( NS + "p" );
                    OntClass c = m.createClass( NS + "C" );
                    OntClass d = m.createClass( NS + "D" );

                    String nameA = "ABCBA";
                    QualifiedRestriction A = m.createMaxCardinalityQRestriction( NS + nameA, p, 3, c  );

                    assertEquals( "Restriction should hasClassQ c", c, A.getHasClassQ() );
                    assertTrue( "Restriction should be hasClassQ c", A.hasHasClassQ( c ) );
                    assertFalse( "Restriction should not be hasClassQ d", A.hasHasClassQ( d ) );

                    A.setHasClassQ( d );

                    assertEquals( "Restriction should hasClassQ d", d, A.getHasClassQ() );
                    assertTrue( "Restriction should be hasClassQ d", A.hasHasClassQ( d ) );
                    assertFalse( "Restriction should not be hasClassQ c", A.hasHasClassQ( c ) );

                    assertTrue( "Should be a qualified restriction", m.getResource( NS + nameA ).canAs( QualifiedRestriction.class ) );
                    A.removeHasClassQ( d );
                    assertFalse( "Should not be a qualified restriction", m.getResource( NS + nameA ).canAs( QualifiedRestriction.class ) );
                }
            },
            new OntTestCase( "CardinalityQRestriction.cardinality", false, false, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntProperty p = m.createObjectProperty( NS + "p" );
                    OntClass c = m.createClass( NS + "C" );

                    CardinalityQRestriction A = m.createCardinalityQRestriction( NS + "A", p, 3, c  );

                    assertEquals( "Restriction should cardinality 3", 3, A.getCardinalityQ() );
                    assertTrue( "Restriction should be cardinality 3", A.hasCardinalityQ( 3 ) );
                    assertFalse( "Restriction should not be cardinality 1", A.hasCardinalityQ( 1 ) );

                    A.setCardinalityQ( 1 );

                    assertEquals( "Restriction should cardinality 1", 1, A.getCardinalityQ() );
                    assertFalse( "Restriction should not be cardinality 3", A.hasCardinalityQ( 3 ) );
                    assertTrue( "Restriction should be cardinality 1", A.hasCardinalityQ( 1 ) );

                    assertTrue( "Should be a qualified cardinality restriction", m.getResource( NS + "A" ).canAs( CardinalityQRestriction.class ) );
                    A.removeCardinalityQ( 1 );
                    assertFalse( "Should not be a qualified cardinality restriction", m.getResource( NS + "A" ).canAs( CardinalityQRestriction.class ) );
                }
            },
            new OntTestCase( "MinCardinalityQRestriction.minCardinality", false, false, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntProperty p = m.createObjectProperty( NS + "p" );
                    OntClass c = m.createClass( NS + "C" );

                    MinCardinalityQRestriction A = m.createMinCardinalityQRestriction( NS + "A", p, 3, c  );

                    assertEquals( "Restriction should min cardinality 3", 3, A.getMinCardinalityQ() );
                    assertTrue( "Restriction should be min cardinality 3", A.hasMinCardinalityQ( 3 ) );
                    assertFalse( "Restriction should not be min cardinality 1", A.hasMinCardinalityQ( 1 ) );

                    A.setMinCardinalityQ( 1 );

                    assertEquals( "Restriction should min cardinality 1", 1, A.getMinCardinalityQ() );
                    assertFalse( "Restriction should not be min cardinality 3", A.hasMinCardinalityQ( 3 ) );
                    assertTrue( "Restriction should be min cardinality 1", A.hasMinCardinalityQ( 1 ) );

                    assertTrue( "Should be a qualified min cardinality restriction", m.getResource( NS + "A" ).canAs( MinCardinalityQRestriction.class ) );
                    A.removeMinCardinalityQ( 1 );
                    assertFalse( "Should not be a qualified min cardinality restriction", m.getResource( NS + "A" ).canAs( MinCardinalityQRestriction.class ) );
                }
            },
            new OntTestCase( "MaxCardinalityQRestriction.maxCardinality", false, false, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntProperty p = m.createObjectProperty( NS + "p" );
                    OntClass c = m.createClass( NS + "C" );

                    MaxCardinalityQRestriction A = m.createMaxCardinalityQRestriction( NS + "A", p, 3, c  );

                    assertEquals( "Restriction should max cardinality 3", 3, A.getMaxCardinalityQ() );
                    assertTrue( "Restriction should be max cardinality 3", A.hasMaxCardinalityQ( 3 ) );
                    assertFalse( "Restriction should not be max cardinality 1", A.hasMaxCardinalityQ( 1 ) );

                    A.setMaxCardinalityQ( 1 );

                    assertEquals( "Restriction should max cardinality 1", 1, A.getMaxCardinalityQ() );
                    assertFalse( "Restriction should not be max cardinality 3", A.hasMaxCardinalityQ( 3 ) );
                    assertTrue( "Restriction should be max cardinality 1", A.hasMaxCardinalityQ( 1 ) );

                    assertTrue( "Should be a qualified max cardinality restriction", m.getResource( NS + "A" ).canAs( MaxCardinalityQRestriction.class ) );
                    A.removeMaxCardinalityQ( 1 );
                    assertFalse( "Should not be a qualified max cardinality restriction", m.getResource( NS + "A" ).canAs( MaxCardinalityQRestriction.class ) );
                }
            },

            // from file
            new OntTestCase( "OntClass.subclass.fromFile", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    String lang = m_owlLang ? "owl" : "rdfs" ;
                    String fileName = "file:testing/ontology/" + lang + "/ClassExpression/test.rdf";
                    m.read( fileName );

                    OntClass A = m.createClass( NS + "ClassA" );
                    OntClass B = m.createClass( NS + "ClassB" );

                    iteratorTest( A.listSuperClasses(), new Object[] {B} );
                    iteratorTest( B.listSubClasses(), new Object[] {A} );
                }
            },
            new OntTestCase( "OntClass.equivalentClass.fromFile", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    String lang = m_owlLang ? "owl" : "rdfs" ;
                    String fileName = "file:testing/ontology/" + lang + "/ClassExpression/test.rdf";
                    m.read( fileName );

                    OntClass A = m.createClass( NS + "ClassA" );
                    OntClass C = m.createClass( NS + "ClassC" );

                    assertTrue( "A should be equiv to C", A.hasEquivalentClass( C ) );
                }
            },
            new OntTestCase( "OntClass.disjoint.fromFile", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    String lang = m_owlLang ? "owl" : "rdfs" ;
                    String fileName = "file:testing/ontology/" + lang + "/ClassExpression/test.rdf";
                    m.read( fileName );

                    OntClass A = m.createClass( NS + "ClassA" );
                    OntClass D = m.createClass( NS + "ClassD" );

                    assertTrue( "A should be disjoint with D", A.isDisjointWith( D ) );
                }
            },

            // type testing
            new OntTestCase( "OntClass.isEnumeratedClass", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntClass b = m.createClass( NS + "B" );
                    Individual x = m.createIndividual( NS + "x", b );
                    Individual y = m.createIndividual( NS + "y", b );
                    OntClass a = m.createEnumeratedClass( NS + "A", m.createList( new RDFNode[] {x, y} ) );

                    assertTrue( "enumerated class test not correct",    a.isEnumeratedClass() );
                    assertTrue( "intersection class test not correct",  !a.isIntersectionClass() );
                    assertTrue( "union class test not correct",         !a.isUnionClass() );
                    assertTrue( "complement class test not correct",    !a.isComplementClass() );
                    assertTrue( "restriction test not correct",         !a.isRestriction() );
                }
            },
            new OntTestCase( "OntClass.isIntersectionClass", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntClass b = m.createClass( NS + "B" );
                    OntClass c = m.createClass( NS + "C" );
                    OntClass a = m.createIntersectionClass( NS + "A", m.createList( new RDFNode[] {b,c} ) );

                    assertTrue( "enumerated class test not correct",    m_owlLiteLang || !a.isEnumeratedClass() );
                    assertTrue( "intersection class test not correct",  a.isIntersectionClass() );
                    assertTrue( "union class test not correct",         m_owlLiteLang || !a.isUnionClass() );
                    assertTrue( "complement class test not correct",    m_owlLiteLang || !a.isComplementClass() );
                    assertTrue( "restriction test not correct",         !a.isRestriction() );
                }
            },
            new OntTestCase( "OntClass.isUnionClass", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntClass b = m.createClass( NS + "B" );
                    OntClass c = m.createClass( NS + "C" );
                    OntClass a = m.createUnionClass( NS + "A", m.createList( new RDFNode[] {b,c} ) );

                    assertTrue( "enumerated class test not correct",    !a.isEnumeratedClass() );
                    assertTrue( "intersection class test not correct",  !a.isIntersectionClass() );
                    assertTrue( "union class test not correct",         a.isUnionClass() );
                    assertTrue( "complement class test not correct",    !a.isComplementClass() );
                    assertTrue( "restriction test not correct",         !a.isRestriction() );
                }
            },
            new OntTestCase( "OntClass.isComplementClass", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntClass b = m.createClass( NS + "B" );
                    OntClass a = m.createComplementClass( NS + "A", b );

                    assertTrue( "enumerated class test not correct",    !a.isEnumeratedClass() );
                    assertTrue( "intersection class test not correct",  !a.isIntersectionClass() );
                    assertTrue( "union class test not correct",         !a.isUnionClass() );
                    assertTrue( "complement class test not correct",    a.isComplementClass() );
                    assertTrue( "restriction test not correct",         !a.isRestriction() );
                }
            },
            new OntTestCase( "OntClass.isRestriction", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntClass a = m.createRestriction( null );

                    assertTrue( "enumerated class test not correct",    m_owlLiteLang || !a.isEnumeratedClass() );
                    assertTrue( "intersection class test not correct",  !a.isIntersectionClass() );
                    assertTrue( "union class test not correct",         m_owlLiteLang || !a.isUnionClass() );
                    assertTrue( "complement class test not correct",    m_owlLiteLang || !a.isComplementClass() );
                    assertTrue( "restriction test not correct",         a.isRestriction() );
                }
            },

            // conversion
            new OntTestCase( "OntClass.toEnumeratedClass", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntClass a = m.createClass( NS + "A" );

                    assertTrue( "enumerated class test not correct",    !a.isEnumeratedClass() );
                    assertTrue( "intersection class test not correct",  !a.isIntersectionClass() );
                    assertTrue( "union class test not correct",         !a.isUnionClass() );
                    assertTrue( "complement class test not correct",    !a.isComplementClass() );
                    assertTrue( "restriction test not correct",         !a.isRestriction() );

                    OntClass b = m.createClass( NS + "B" );
                    Individual x = m.createIndividual( NS + "x", b );
                    Individual y = m.createIndividual( NS + "y", b );
                    a = a.convertToEnumeratedClass( m.createList( new RDFNode[] {x, y} ) );

                    assertTrue( "enumerated class test not correct",    a.isEnumeratedClass() );
                    assertTrue( "intersection class test not correct",  !a.isIntersectionClass() );
                    assertTrue( "union class test not correct",         !a.isUnionClass() );
                    assertTrue( "complement class test not correct",    !a.isComplementClass() );
                    assertTrue( "restriction test not correct",         !a.isRestriction() );
                }
            },
            new OntTestCase( "OntClass.toIntersectionClass", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntClass a = m.createClass( NS + "A" );

                    assertTrue( "enumerated class test not correct",    m_owlLiteLang || !a.isEnumeratedClass() );
                    assertTrue( "intersection class test not correct",  !a.isIntersectionClass() );
                    assertTrue( "union class test not correct",         m_owlLiteLang || !a.isUnionClass() );
                    assertTrue( "complement class test not correct",    m_owlLiteLang || !a.isComplementClass() );
                    assertTrue( "restriction test not correct",         !a.isRestriction() );

                    OntClass b = m.createClass( NS + "B" );
                    OntClass c = m.createClass( NS + "C" );
                    a = a.convertToIntersectionClass( m.createList( new RDFNode[] {b,c} ) );

                    assertTrue( "enumerated class test not correct",    m_owlLiteLang || !a.isEnumeratedClass() );
                    assertTrue( "intersection class test not correct",  a.isIntersectionClass() );
                    assertTrue( "union class test not correct",         m_owlLiteLang || !a.isUnionClass() );
                    assertTrue( "complement class test not correct",    m_owlLiteLang || !a.isComplementClass() );
                    assertTrue( "restriction test not correct",         !a.isRestriction() );
                }
            },
            new OntTestCase( "OntClass.toUnionClass", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntClass a = m.createClass( NS + "A" );

                    assertTrue( "enumerated class test not correct",    !a.isEnumeratedClass() );
                    assertTrue( "intersection class test not correct",  !a.isIntersectionClass() );
                    assertTrue( "union class test not correct",         !a.isUnionClass() );
                    assertTrue( "complement class test not correct",    !a.isComplementClass() );
                    assertTrue( "restriction test not correct",         !a.isRestriction() );

                    OntClass b = m.createClass( NS + "B" );
                    OntClass c = m.createClass( NS + "C" );
                    a = a.convertToUnionClass( m.createList( new RDFNode[] {b,c} ) );

                    assertTrue( "enumerated class test not correct",    m_owlLiteLang || !a.isEnumeratedClass() );
                    assertTrue( "intersection class test not correct",  !a.isIntersectionClass() );
                    assertTrue( "union class test not correct",         m_owlLiteLang || a.isUnionClass() );
                    assertTrue( "complement class test not correct",    m_owlLiteLang || !a.isComplementClass() );
                    assertTrue( "restriction test not correct",         !a.isRestriction() );
                }
            },
            new OntTestCase( "OntClass.toComplementClass", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntClass a = m.createClass( NS + "A" );

                    assertTrue( "enumerated class test not correct",    !a.isEnumeratedClass() );
                    assertTrue( "intersection class test not correct",  !a.isIntersectionClass() );
                    assertTrue( "union class test not correct",         !a.isUnionClass() );
                    assertTrue( "complement class test not correct",    !a.isComplementClass() );
                    assertTrue( "restriction test not correct",         !a.isRestriction() );

                    OntClass b = m.createClass( NS + "B" );
                    a = a.convertToComplementClass( b );

                    assertTrue( "enumerated class test not correct",    m_owlLiteLang || !a.isEnumeratedClass() );
                    assertTrue( "intersection class test not correct",  !a.isIntersectionClass() );
                    assertTrue( "union class test not correct",         m_owlLiteLang || !a.isUnionClass() );
                    assertTrue( "complement class test not correct",    m_owlLiteLang || a.isComplementClass() );
                    assertTrue( "restriction test not correct",         !a.isRestriction() );
                }
            },
            new OntTestCase( "OntClass.toRestriction", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntClass a = m.createClass( NS + "A" );

                    assertTrue( "enumerated class test not correct",    m_owlLiteLang || !a.isEnumeratedClass() );
                    assertTrue( "intersection class test not correct",  !a.isIntersectionClass() );
                    assertTrue( "union class test not correct",         m_owlLiteLang || !a.isUnionClass() );
                    assertTrue( "complement class test not correct",    m_owlLiteLang || !a.isComplementClass() );
                    assertTrue( "restriction test not correct",         !a.isRestriction() );

                    ObjectProperty p = m.createObjectProperty( NS + "p" );
                    a = a.convertToRestriction( p );

                    assertTrue( "enumerated class test not correct",    m_owlLiteLang || !a.isEnumeratedClass() );
                    assertTrue( "intersection class test not correct",  !a.isIntersectionClass() );
                    assertTrue( "union class test not correct",         m_owlLiteLang || !a.isUnionClass() );
                    assertTrue( "complement class test not correct",    m_owlLiteLang || !a.isComplementClass() );
                    assertTrue( "restriction test not correct",         a.isRestriction() );
                }
            },


            // restriction type testing
            new OntTestCase( "Restriction.isAllValuesFrom", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntClass b = m.createClass( NS + "B" );
                    ObjectProperty p = m.createObjectProperty( NS + "p" );
                    Restriction a = m.createAllValuesFromRestriction( null, p, b );

                    assertTrue( "all values from test not correct",   a.isAllValuesFromRestriction() );
                    assertTrue( "some values from test not correct",  !a.isSomeValuesFromRestriction() );
                    assertTrue( "has value test not correct",         m_owlLiteLang || !a.isHasValueRestriction() );
                    assertTrue( "cardinality test not correct",       !a.isCardinalityRestriction() );
                    assertTrue( "min cardinality test not correct",   !a.isMinCardinalityRestriction() );
                    assertTrue( "max cardinality test not correct",   !a.isMaxCardinalityRestriction() );
                }
            },
            new OntTestCase( "Restriction.isSomeValuesFrom", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntClass b = m.createClass( NS + "B" );
                    ObjectProperty p = m.createObjectProperty( NS + "p" );
                    Restriction a = m.createSomeValuesFromRestriction( null, p, b );

                    assertTrue( "all values from test not correct",   !a.isAllValuesFromRestriction() );
                    assertTrue( "some values from test not correct",  a.isSomeValuesFromRestriction() );
                    assertTrue( "has value test not correct",         m_owlLiteLang || !a.isHasValueRestriction() );
                    assertTrue( "cardinality test not correct",       !a.isCardinalityRestriction() );
                    assertTrue( "min cardinality test not correct",   !a.isMinCardinalityRestriction() );
                    assertTrue( "max cardinality test not correct",   !a.isMaxCardinalityRestriction() );
                }
            },
            new OntTestCase( "Restriction.isHasValue", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntClass b = m.createClass( NS + "B" );
                    Individual x = m.createIndividual( b );
                    ObjectProperty p = m.createObjectProperty( NS + "p" );
                    Restriction a = m.createHasValueRestriction( null, p, x );

                    assertTrue( "all values from test not correct",   !a.isAllValuesFromRestriction() );
                    assertTrue( "some values from test not correct",  !a.isSomeValuesFromRestriction() );
                    assertTrue( "has value test not correct",         m_owlLiteLang || a.isHasValueRestriction() );
                    assertTrue( "cardinality test not correct",       !a.isCardinalityRestriction() );
                    assertTrue( "min cardinality test not correct",   !a.isMinCardinalityRestriction() );
                    assertTrue( "max cardinality test not correct",   !a.isMaxCardinalityRestriction() );
                }
            },
            new OntTestCase( "Restriction.isCardinality", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    ObjectProperty p = m.createObjectProperty( NS + "p" );
                    Restriction a = m.createCardinalityRestriction( null, p, 3 );

                    assertTrue( "all values from test not correct",   !a.isAllValuesFromRestriction() );
                    assertTrue( "some values from test not correct",  !a.isSomeValuesFromRestriction() );
                    assertTrue( "has value test not correct",         m_owlLiteLang || !a.isHasValueRestriction() );
                    assertTrue( "cardinality test not correct",       a.isCardinalityRestriction() );
                    assertTrue( "min cardinality test not correct",   !a.isMinCardinalityRestriction() );
                    assertTrue( "max cardinality test not correct",   !a.isMaxCardinalityRestriction() );
                }
            },
            new OntTestCase( "Restriction.isMinCardinality", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    ObjectProperty p = m.createObjectProperty( NS + "p" );
                    Restriction a = m.createMinCardinalityRestriction( null, p, 1 );

                    assertTrue( "all values from test not correct",   !a.isAllValuesFromRestriction() );
                    assertTrue( "some values from test not correct",  !a.isSomeValuesFromRestriction() );
                    assertTrue( "has value test not correct",         m_owlLiteLang || !a.isHasValueRestriction() );
                    assertTrue( "cardinality test not correct",       !a.isCardinalityRestriction() );
                    assertTrue( "min cardinality test not correct",   a.isMinCardinalityRestriction() );
                    assertTrue( "max cardinality test not correct",   !a.isMaxCardinalityRestriction() );
                }
            },
            new OntTestCase( "Restriction.isMaxCardinality", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    ObjectProperty p = m.createObjectProperty( NS + "p" );
                    Restriction a = m.createMaxCardinalityRestriction( null, p, 5 );

                    assertTrue( "all values from test not correct",   !a.isAllValuesFromRestriction() );
                    assertTrue( "some values from test not correct",  !a.isSomeValuesFromRestriction() );
                    assertTrue( "has value test not correct",         m_owlLiteLang || !a.isHasValueRestriction() );
                    assertTrue( "cardinality test not correct",       !a.isCardinalityRestriction() );
                    assertTrue( "min cardinality test not correct",   !a.isMinCardinalityRestriction() );
                    assertTrue( "max cardinality test not correct",   a.isMaxCardinalityRestriction() );
                }
            },

            // restriction conversions
            new OntTestCase( "Restriction.convertToAllValuesFrom", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    ObjectProperty p = m.createObjectProperty( NS + "p" );
                    Restriction a = m.createRestriction( p );

                    assertTrue( "all values from test not correct",   !a.isAllValuesFromRestriction() );
                    assertTrue( "some values from test not correct",  !a.isSomeValuesFromRestriction() );
                    assertTrue( "has value test not correct",         m_owlLiteLang || !a.isHasValueRestriction() );
                    assertTrue( "cardinality test not correct",       !a.isCardinalityRestriction() );
                    assertTrue( "min cardinality test not correct",   !a.isMinCardinalityRestriction() );
                    assertTrue( "max cardinality test not correct",   !a.isMaxCardinalityRestriction() );

                    OntClass b = m.createClass( NS + "B" );
                    a = a.convertToAllValuesFromRestriction( b );

                    assertTrue( "all values from test not correct",   a.isAllValuesFromRestriction() );
                    assertTrue( "some values from test not correct",  !a.isSomeValuesFromRestriction() );
                    assertTrue( "has value test not correct",         m_owlLiteLang || !a.isHasValueRestriction() );
                    assertTrue( "cardinality test not correct",       !a.isCardinalityRestriction() );
                    assertTrue( "min cardinality test not correct",   !a.isMinCardinalityRestriction() );
                    assertTrue( "max cardinality test not correct",   !a.isMaxCardinalityRestriction() );
                }
            },
            new OntTestCase( "Restriction.convertToSomeValuesFrom", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    ObjectProperty p = m.createObjectProperty( NS + "p" );
                    Restriction a = m.createRestriction( p );

                    assertTrue( "all values from test not correct",   !a.isAllValuesFromRestriction() );
                    assertTrue( "some values from test not correct",  !a.isSomeValuesFromRestriction() );
                    assertTrue( "has value test not correct",         m_owlLiteLang || !a.isHasValueRestriction() );
                    assertTrue( "cardinality test not correct",       !a.isCardinalityRestriction() );
                    assertTrue( "min cardinality test not correct",   !a.isMinCardinalityRestriction() );
                    assertTrue( "max cardinality test not correct",   !a.isMaxCardinalityRestriction() );

                    OntClass b = m.createClass( NS + "B" );
                    a = a.convertToSomeValuesFromRestriction( b );

                    assertTrue( "all values from test not correct",   !a.isAllValuesFromRestriction() );
                    assertTrue( "some values from test not correct",  a.isSomeValuesFromRestriction() );
                    assertTrue( "has value test not correct",         m_owlLiteLang || !a.isHasValueRestriction() );
                    assertTrue( "cardinality test not correct",       !a.isCardinalityRestriction() );
                    assertTrue( "min cardinality test not correct",   !a.isMinCardinalityRestriction() );
                    assertTrue( "max cardinality test not correct",   !a.isMaxCardinalityRestriction() );
                }
            },
            new OntTestCase( "Restriction.convertToHasValue", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    ObjectProperty p = m.createObjectProperty( NS + "p" );
                    Restriction a = m.createRestriction( p );

                    assertTrue( "all values from test not correct",   !a.isAllValuesFromRestriction() );
                    assertTrue( "some values from test not correct",  !a.isSomeValuesFromRestriction() );
                    assertTrue( "has value test not correct",         m_owlLiteLang || !a.isHasValueRestriction() );
                    assertTrue( "cardinality test not correct",       !a.isCardinalityRestriction() );
                    assertTrue( "min cardinality test not correct",   !a.isMinCardinalityRestriction() );
                    assertTrue( "max cardinality test not correct",   !a.isMaxCardinalityRestriction() );

                    OntClass b = m.createClass( NS + "B" );
                    Individual x = m.createIndividual( b );
                    a = a.convertToHasValueRestriction( x );

                    assertTrue( "all values from test not correct",   !a.isAllValuesFromRestriction() );
                    assertTrue( "some values from test not correct",  !a.isSomeValuesFromRestriction() );
                    assertTrue( "has value test not correct",         m_owlLiteLang || a.isHasValueRestriction() );
                    assertTrue( "cardinality test not correct",       !a.isCardinalityRestriction() );
                    assertTrue( "min cardinality test not correct",   !a.isMinCardinalityRestriction() );
                    assertTrue( "max cardinality test not correct",   !a.isMaxCardinalityRestriction() );
                }
            },
            new OntTestCase( "Restriction.convertCardinality", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    ObjectProperty p = m.createObjectProperty( NS + "p" );
                    Restriction a = m.createRestriction( p );

                    assertTrue( "all values from test not correct",   !a.isAllValuesFromRestriction() );
                    assertTrue( "some values from test not correct",  !a.isSomeValuesFromRestriction() );
                    assertTrue( "has value test not correct",         m_owlLiteLang || !a.isHasValueRestriction() );
                    assertTrue( "cardinality test not correct",       !a.isCardinalityRestriction() );
                    assertTrue( "min cardinality test not correct",   !a.isMinCardinalityRestriction() );
                    assertTrue( "max cardinality test not correct",   !a.isMaxCardinalityRestriction() );

                    a = a.convertToCardinalityRestriction( 3 );

                    assertTrue( "all values from test not correct",   !a.isAllValuesFromRestriction() );
                    assertTrue( "some values from test not correct",  !a.isSomeValuesFromRestriction() );
                    assertTrue( "has value test not correct",         m_owlLiteLang || !a.isHasValueRestriction() );
                    assertTrue( "cardinality test not correct",       a.isCardinalityRestriction() );
                    assertTrue( "min cardinality test not correct",   !a.isMinCardinalityRestriction() );
                    assertTrue( "max cardinality test not correct",   !a.isMaxCardinalityRestriction() );
                }
            },
            new OntTestCase( "Restriction.convertMinCardinality", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    ObjectProperty p = m.createObjectProperty( NS + "p" );
                    Restriction a = m.createRestriction( p );

                    assertTrue( "all values from test not correct",   !a.isAllValuesFromRestriction() );
                    assertTrue( "some values from test not correct",  !a.isSomeValuesFromRestriction() );
                    assertTrue( "has value test not correct",         m_owlLiteLang || !a.isHasValueRestriction() );
                    assertTrue( "cardinality test not correct",       !a.isCardinalityRestriction() );
                    assertTrue( "min cardinality test not correct",   !a.isMinCardinalityRestriction() );
                    assertTrue( "max cardinality test not correct",   !a.isMaxCardinalityRestriction() );

                    a = a.convertToMinCardinalityRestriction( 3 );

                    assertTrue( "all values from test not correct",   !a.isAllValuesFromRestriction() );
                    assertTrue( "some values from test not correct",  !a.isSomeValuesFromRestriction() );
                    assertTrue( "has value test not correct",         m_owlLiteLang || !a.isHasValueRestriction() );
                    assertTrue( "cardinality test not correct",       !a.isCardinalityRestriction() );
                    assertTrue( "min cardinality test not correct",   a.isMinCardinalityRestriction() );
                    assertTrue( "max cardinality test not correct",   !a.isMaxCardinalityRestriction() );
                }
            },
            new OntTestCase( "Restriction.convertMaxCardinality", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    ObjectProperty p = m.createObjectProperty( NS + "p" );
                    Restriction a = m.createRestriction( p );

                    assertTrue( "all values from test not correct",   !a.isAllValuesFromRestriction() );
                    assertTrue( "some values from test not correct",  !a.isSomeValuesFromRestriction() );
                    assertTrue( "has value test not correct",         m_owlLiteLang || !a.isHasValueRestriction() );
                    assertTrue( "cardinality test not correct",       !a.isCardinalityRestriction() );
                    assertTrue( "min cardinality test not correct",   !a.isMinCardinalityRestriction() );
                    assertTrue( "max cardinality test not correct",   !a.isMaxCardinalityRestriction() );

                    a = a.convertToMaxCardinalityRestriction( 3 );

                    assertTrue( "all values from test not correct",   !a.isAllValuesFromRestriction() );
                    assertTrue( "some values from test not correct",  !a.isSomeValuesFromRestriction() );
                    assertTrue( "has value test not correct",         m_owlLiteLang || !a.isHasValueRestriction() );
                    assertTrue( "cardinality test not correct",       !a.isCardinalityRestriction() );
                    assertTrue( "min cardinality test not correct",   !a.isMinCardinalityRestriction() );
                    assertTrue( "max cardinality test not correct",   a.isMaxCardinalityRestriction() );
                }
            },
            new OntTestCase( "OntClass.listInstances", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );

                    Individual a0 = m.createIndividual( A );
                    Individual a1 = m.createIndividual( NS + "a1", A );
                    Individual b0 = m.createIndividual( B );
                    /*Individual b1 =*/ m.createIndividual( NS + "b1", B );
                    b0.addRDFType( A );

                    iteratorTest( A.listInstances(), new Object[] {a0, a1, b0} );
                }
            },
            new OntTestCase( "OntClass.listDefinedProperties", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntClass A = m.createClass( NS + "A" );
                    //OntClass B = m.createClass( NS + "B" );
                    OntClass C = m.createClass( NS + "C" );

                    OntProperty p = m.createOntProperty( NS + "p" );
                    OntProperty q = m.createOntProperty( NS + "q" );
                    OntProperty r = m.createOntProperty( NS + "r" );
                    OntProperty s = m.createOntProperty( NS + "s" );

                    p.setDomain( A );
                    q.setDomain( A );
                    s.setDomain( C );

                    if (m_rdfsLang) {
                        iteratorTest( A.listDeclaredProperties(), new Object[] {p, q, r} );
                    }
                    else {
                        Restriction r0 = m.createRestriction( r );
                        C.addSuperClass( r0 );

                        iteratorTest( A.listDeclaredProperties(), new Object[] {p, q, r} );

                        iteratorTest( C.listDeclaredProperties(), new Object[] {s, r} );

                        iteratorTest( r0.listDeclaredProperties(), new Object[] {r} );
                    }
                }
            },
            new OntTestCase( "OntClass.listDefinedProperties.notAll", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    OntClass A = m.createClass( NS + "A" );
                    OntClass C = m.createClass( NS + "C" );
                    C.addSuperClass(A);

                    OntProperty p = m.createOntProperty( NS + "p" );
                    OntProperty q = m.createOntProperty( NS + "q" );
                    OntProperty s = m.createOntProperty( NS + "s" );

                    p.setDomain( A );
                    q.setDomain( A );
                    s.setDomain( C );

                    iteratorTest( C.listDeclaredProperties( false ), new Object[] { p, q, s} );
                    iteratorTest( C.listDeclaredProperties( true ), new Object[] {s} );

                    assertNotNull( "declared property should be an ont prop", C.listDeclaredProperties( true ).next() );
                    assertNotNull( "declared property should be an ont prop", C.listDeclaredProperties( false ).next()  );
                }
            },
            new OntTestCase( "DataRange.oneOf", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) throws Exception {
                    Literal x = m.createTypedLiteral( 42 );
                    Literal y = m.createTypedLiteral( true );
                    Literal z = m.createTypedLiteral( "life" );
                    RDFList lits = m.createList( new RDFNode[] {x,y} );

                    DataRange d0 = m.createDataRange( lits );

                    assertTrue( "datarange should contain x", d0.hasOneOf( x ) );
                    assertTrue( "datarange should contain y", d0.hasOneOf( y ) );
                    assertFalse( "datarange should not contain z", d0.hasOneOf( z ) );

                    d0.removeOneOf( z );
                    assertTrue( "datarange should contain x", d0.hasOneOf( x ) );
                    assertTrue( "datarange should contain y", d0.hasOneOf( y ) );
                    assertFalse( "datarange should not contain z", d0.hasOneOf( z ) );

                    d0.removeOneOf( x );
                    assertFalse( "datarange should not contain x", d0.hasOneOf( x ) );
                    assertTrue( "datarange should contain y", d0.hasOneOf( y ) );
                    assertFalse( "datarange should not contain z", d0.hasOneOf( z ) );

                    d0.addOneOf( z );
                    assertEquals( "datarange should be size 2", 2, d0.getOneOf().size() );
                    iteratorTest( d0.listOneOf(), new Object[] {y,z} );

                    d0.setOneOf( m.createList( new RDFNode[] {x} ) );
                    iteratorTest( d0.listOneOf(), new Object[] {x} );
                }
            },

            // Removal

            new OntTestCase( "Remove intersection", true, true, false )  {
                @Override
                protected void ontTest(OntModel m) throws Exception {
                    String ns = "http://example.com/foo#";
                    OntClass a = m.createClass(ns + "A");
                    OntClass b = m.createClass(ns + "B");

                    long old = m.size();

                    RDFList members = m.createList(new RDFNode[] { a, b });
                    IntersectionClass intersectionClass = m.createIntersectionClass(null, members);
                    intersectionClass.remove();

                    assertEquals( old, m.size() );
                }
            },
            new OntTestCase( "Remove union", true, false, false )  {
                @Override
                protected void ontTest(OntModel m) throws Exception {
                    String ns = "http://example.com/foo#";
                    OntClass a = m.createClass(ns + "A");
                    OntClass b = m.createClass(ns + "B");

                    long old = m.size();

                    RDFList members = m.createList(new RDFNode[] { a, b });
                    UnionClass unionClass = m.createUnionClass(null, members);
                    unionClass.remove();

                    assertEquals( old, m.size() );
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
