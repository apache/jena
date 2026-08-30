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
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.iterator.ClosableIterator;
import org.apache.jena.util.iterator.NullIterator;
import org.apache.jena.vocabulary.*;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.jena.test.JenaTestLib;

/**
 * <p>
 * Unit tests for OntClass and other class expressions.
 * </p>
 */
@SuppressWarnings("removal")
public class TestClassExpression extends OntTestBase
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
            new OntTestCase( "OntClass.super-class", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    OntClass C = m.createClass( NS + "C" );

                    A.addSuperClass( B );
                    assertEquals( 1, A.getCardinality( prof.SUB_CLASS_OF() ), "Cardinality should be 1" );
                    assertEquals( B, A.getSuperClass(), "A should have super-class B" );

                    A.addSuperClass( C );
                    assertEquals( 2, A.getCardinality( prof.SUB_CLASS_OF() ), "Cardinality should be 2" );
                    iteratorTest( A.listSuperClasses(), new Object[] {C, B} );

                    A.setSuperClass( C );
                    assertEquals( 1, A.getCardinality( prof.SUB_CLASS_OF() ), "Cardinality should be 1" );
                    assertEquals( C, A.getSuperClass(), "A shuold have super-class C" );
                    assertTrue( !A.hasSuperClass( B, false ), "A shuold not have super-class B" );

                    A.removeSuperClass( B );
                    assertEquals( 1, A.getCardinality( prof.SUB_CLASS_OF() ), "Cardinality should be 1" );
                    A.removeSuperClass( C );
                    assertEquals( 0, A.getCardinality( prof.SUB_CLASS_OF() ), "Cardinality should be 0" );
                }
            },
            new OntTestCase( "OntClass.sub-class", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    OntClass C = m.createClass( NS + "C" );

                    A.addSubClass( B );
                    assertEquals( 1, B.getCardinality( prof.SUB_CLASS_OF() ), "Cardinality should be 1" );
                    assertEquals( B, A.getSubClass(), "A should have sub-class B" );

                    A.addSubClass( C );
                    assertEquals( 2, B.getCardinality( prof.SUB_CLASS_OF() ) + C.getCardinality( prof.SUB_CLASS_OF() ), "Cardinality should be 2" );
                    iteratorTest( A.listSubClasses(), new Object[] {C, B} );

                    A.setSubClass( C );
                    assertEquals( 1, B.getCardinality( prof.SUB_CLASS_OF() ) + C.getCardinality( prof.SUB_CLASS_OF() ), "Cardinality should be 1" );
                    assertEquals( C, A.getSubClass(), "A shuold have sub-class C" );
                    assertTrue( !A.hasSubClass( B, false ), "A shuold not have sub-class B" );

                    A.removeSubClass( B );
                    assertTrue( A.hasSubClass( C, false ), "A should have sub-class C" );
                    A.removeSubClass( C );
                    assertTrue( !A.hasSubClass( C, false ), "A should not have sub-class C" );
                }
            },
            new OntTestCase( "OntClass.equivalentClass", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    OntClass C = m.createClass( NS + "C" );

                    A.addEquivalentClass( B );
                    assertEquals( 1, A.getCardinality( prof.EQUIVALENT_CLASS() ), "Cardinality should be 1" );
                    assertEquals( B, A.getEquivalentClass(), "A have equivalentClass B" );

                    A.addEquivalentClass( C );
                    assertEquals( 2, A.getCardinality( prof.EQUIVALENT_CLASS() ), "Cardinality should be 2" );
                    iteratorTest( A.listEquivalentClasses(), new Object[] {C, B} );

                    A.setEquivalentClass( C );
                    assertEquals( 1, A.getCardinality( prof.EQUIVALENT_CLASS() ), "Cardinality should be 1" );
                    assertEquals( C, A.getEquivalentClass(), "A should have equivalentClass C" );
                    assertTrue( !A.hasEquivalentClass( B ), "A should not have equivalentClass B" );

                    A.removeEquivalentClass( B );
                    assertEquals( 1, A.getCardinality( prof.EQUIVALENT_CLASS() ), "Cardinality should be 1" );
                    A.removeEquivalentClass( C );
                    assertEquals( 0, A.getCardinality( prof.EQUIVALENT_CLASS() ), "Cardinality should be 0" );
                }
            },
            new OntTestCase( "OntClass.disjointWith", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    OntClass A = m.createClass( NS + "A" );
                    OntClass B = m.createClass( NS + "B" );
                    OntClass C = m.createClass( NS + "C" );

                    A.addDisjointWith( B );
                    assertEquals( 1, A.getCardinality( prof.DISJOINT_WITH() ), "Cardinality should be 1" );
                    assertEquals( B, A.getDisjointWith(), "A have be disjoint with B" );

                    A.addDisjointWith( C );
                    assertEquals( 2, A.getCardinality( prof.DISJOINT_WITH() ), "Cardinality should be 2" );
                    iteratorTest( A.listDisjointWith(), new Object[] {C,B} );

                    A.setDisjointWith( C );
                    assertEquals( 1, A.getCardinality( prof.DISJOINT_WITH() ), "Cardinality should be 1" );
                    assertEquals( C, A.getDisjointWith(), "A should be disjoint with C" );
                    assertTrue( !A.isDisjointWith( B ), "A should not be disjoint with B" );

                    A.removeDisjointWith( B );
                    assertEquals( 1, A.getCardinality( prof.DISJOINT_WITH() ), "Cardinality should be 1" );
                    A.removeDisjointWith( C );
                    assertEquals( 0, A.getCardinality( prof.DISJOINT_WITH() ), "Cardinality should be 0" );
                }
            },
            new OntTestCase( "EnumeratedClass.oneOf", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    EnumeratedClass A = m.createEnumeratedClass( NS + "A", null );
                    OntResource a = m.getResource( NS + "a" ).as( OntResource.class );
                    OntResource b = m.getResource( NS + "b" ).as( OntResource.class );

                    A.addOneOf( a );
                    assertEquals( 1, A.getCardinality( prof.ONE_OF() ), "Cardinality should be 1" );
                    assertEquals( 1, A.getOneOf().size(), "Size should be 1" );
                    assertTrue( A.getOneOf().contains( a ), "A should have a as enumerated member" );

                    A.addOneOf( b );
                    assertEquals( 1, A.getCardinality( prof.ONE_OF() ), "Cardinality should be 1" );
                    assertEquals( 2, A.getOneOf().size(), "Size should be 2" );
                    iteratorTest( A.listOneOf(), new Object[] {a,b} );

                    A.setOneOf( m.createList( new RDFNode[] {b} ) );
                    assertEquals( 1, A.getCardinality( prof.ONE_OF() ), "Cardinality should be 1" );
                    assertEquals( 1, A.getOneOf().size(), "Size should be 1" );
                    assertTrue( A.hasOneOf( b ), "A should have b in the enum" );
                    assertTrue( !A.hasOneOf( a ), "A should not have a in the enum" );

                    A.removeOneOf( a );
                    assertTrue( A.hasOneOf( b ), "Should have b as an enum value" );
                    A.removeOneOf( b );
                    assertTrue( !A.hasOneOf( b ), "Should not have b as an enum value" );
                }
            },
            new OntTestCase( "IntersectionClass.intersectionOf", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    IntersectionClass A = m.createIntersectionClass( NS + "A", null );
                    OntClass B = m.createClass( NS + "B" );
                    OntClass C = m.createClass( NS + "C" );

                    A.addOperand( B );
                    assertEquals( 1, A.getCardinality( prof.INTERSECTION_OF() ), "Cardinality should be 1" );
                    assertEquals( 1, A.getOperands().size(), "Size should be 1" );
                    assertTrue( A.getOperands().contains( B ), "A should have a as intersection member" );

                    A.addOperand( C );
                    assertEquals( 1, A.getCardinality( prof.INTERSECTION_OF() ), "Cardinality should be 1" );
                    assertEquals( 2, A.getOperands().size(), "Size should be 2" );
                    iteratorTest( A.listOperands(), new Object[] {B,C} );

                    ClosableIterator<? extends Resource> i = A.listOperands();
                    assertTrue( i.next() instanceof OntClass, "Argument should be an OntClass" );
                    i.close();

                    A.setOperands( m.createList( new RDFNode[] {C} ) );
                    assertEquals( 1, A.getCardinality( prof.INTERSECTION_OF() ), "Cardinality should be 1" );
                    assertEquals( 1, A.getOperands().size(), "Size should be 1" );
                    assertTrue( A.hasOperand( C ), "A should have C in the intersection" );
                    assertTrue( !A.hasOperand( B ), "A should not have B in the intersection" );

                    A.removeOperand( B );
                    assertTrue( A.hasOperand( C ), "Should have C as an operand" );
                    A.removeOperand( C );
                    assertTrue( !A.hasOperand( C ), "Should not have C as an operand" );
                }
            },
            new OntTestCase( "UnionClass.unionOf", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    UnionClass A = m.createUnionClass( NS + "A", null );
                    OntClass B = m.createClass( NS + "B" );
                    OntClass C = m.createClass( NS + "C" );

                    A.addOperand( B );
                    assertEquals( 1, A.getCardinality( prof.UNION_OF() ), "Cardinality should be 1" );
                    assertEquals( 1, A.getOperands().size(), "Size should be 1" );
                    assertTrue( A.getOperands().contains( B ), "A should have a as union member" );

                    A.addOperand( C );
                    assertEquals( 1, A.getCardinality( prof.UNION_OF() ), "Cardinality should be 1" );
                    assertEquals( 2, A.getOperands().size(), "Size should be 2" );
                    iteratorTest( A.listOperands(), new Object[] {B,C} );

                    ClosableIterator<? extends Resource> i = A.listOperands();
                    assertTrue( i.next() instanceof OntClass, "Argument should be an OntClass" );
                    i.close();

                    A.setOperands( m.createList( new RDFNode[] {C} ) );
                    assertEquals( 1, A.getCardinality( prof.UNION_OF() ), "Cardinality should be 1" );
                    assertEquals( 1, A.getOperands().size(), "Size should be 1" );
                    assertTrue( A.hasOperand( C ), "A should have C in the union" );
                    assertTrue( !A.hasOperand( B ), "A should not have B in the union" );

                    A.removeOperand( B );
                    assertTrue( A.hasOperand( C ), "Should have C as an operand" );
                    A.removeOperand( C );
                    assertTrue( !A.hasOperand( C ), "Should not have C as an operand" );
                }
            },
            new OntTestCase( "ComplementClass.complementOf", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    ComplementClass A = m.createComplementClass( NS + "A", null );
                    OntClass B = m.createClass( NS + "B" );
                    OntClass C = m.createClass( NS + "C" );
                    boolean ex = false;

                    try { A.addOperand( B ); } catch (UnsupportedOperationException e) {ex = true;}
                    assertTrue( ex, "Should fail to add to a complement" );

                    ex = false;
                    try { A.addOperands( new NullIterator<Resource>() ); } catch (UnsupportedOperationException e) {ex = true;}
                    assertTrue( ex, "Should fail to add to a complement" );

                    ex = false;
                    try { A.setOperands( m.createList( new RDFNode[] {C} ) ); } catch (UnsupportedOperationException e) {ex = true;}
                    assertTrue( ex, "Should fail to set a list to a complement" );

                    A.setOperand( B );
                    assertEquals( 1, A.getCardinality( prof.COMPLEMENT_OF() ), "Cardinality should be 1" );
                    assertEquals( B, A.getOperand(), "Complement should be B" );
                    iteratorTest( A.listOperands(), new Object[] {B} );

                    A.setOperand( C );
                    assertEquals( 1, A.getCardinality( prof.COMPLEMENT_OF() ), "Cardinality should be 1" );
                    assertTrue( A.hasOperand( C ), "A should have C in the complement" );
                    assertTrue( !A.hasOperand( B ), "A should not have B in the complement" );

                    A.removeOperand( B );
                    assertTrue( A.hasOperand( C ), "Should have C as an operand" );
                    A.removeOperand( C );
                    assertTrue( !A.hasOperand( C ), "Should not have C as an operand" );
                }
            },
            new OntTestCase( "Restriction.onProperty", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createObjectProperty( NS + "p" );
                    OntProperty q = m.createObjectProperty( NS + "q" );
                    OntClass B = m.createClass( NS + "B" );

                    Restriction A = m.createAllValuesFromRestriction( NS + "A", p, B  );

                    assertEquals( p, A.getOnProperty(), "Restriction should be on property p" );
                    assertTrue( A.onProperty( p ), "Restriction should be on property p" );
                    assertTrue( !A.onProperty( q ), "Restriction should not be on property q" );
                    assertEquals( 1, A.getCardinality( prof.ON_PROPERTY() ), "cardinality should be 1 ");

                    A.setOnProperty( q );

                    assertEquals( q, A.getOnProperty(), "Restriction should be on property q" );
                    assertTrue( !A.onProperty( p ), "Restriction should not be on property p" );
                    assertTrue( A.onProperty( q ), "Restriction should not on property q" );
                    assertEquals( 1, A.getCardinality( prof.ON_PROPERTY() ), "cardinality should be 1 ");

                    A.removeOnProperty( p );
                    assertTrue( A.onProperty( q ), "Should have q as on property" );
                    A.removeOnProperty( q );
                    assertTrue( !A.onProperty( q ), "Should not have q as on property" );
                }
            },
            new OntTestCase( "AllValuesFromRestriction.allValuesFrom", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createObjectProperty( NS + "p" );
                    OntClass B = m.createClass( NS + "B" );
                    OntClass C = m.createClass( NS + "C" );

                    AllValuesFromRestriction A = m.createAllValuesFromRestriction( NS + "A", p, B  );

                    assertEquals( B, A.getAllValuesFrom(), "Restriction should be all values from B" );
                    assertTrue( A.hasAllValuesFrom( B ), "Restriction should be all values from B" );
                    assertTrue( !A.hasAllValuesFrom( C ), "Restriction should not be all values from C" );
                    assertEquals( 1, A.getCardinality( prof.ALL_VALUES_FROM() ), "cardinality should be 1 ");

                    A.setAllValuesFrom( C );

                    assertEquals( C, A.getAllValuesFrom(), "Restriction should be all values from C" );
                    assertTrue( !A.hasAllValuesFrom( B ), "Restriction should not be all values from B" );
                    assertTrue( A.hasAllValuesFrom( C ), "Restriction should be all values from C" );
                    assertEquals( 1, A.getCardinality( prof.ALL_VALUES_FROM() ), "cardinality should be 1 ");

                    A.removeAllValuesFrom( C );

                    assertTrue( !A.hasAllValuesFrom( C ), "Restriction should not be some values from C" );
                    assertEquals( 0, A.getCardinality( prof.ALL_VALUES_FROM() ), "cardinality should be 0 ");
                }
            },
            new OntTestCase( "AllValuesFromRestriction.allValuesFrom.datatype", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createObjectProperty( NS + "p" );

                    AllValuesFromRestriction A = m.createAllValuesFromRestriction( NS + "A", p, XSD.gDay  );

                    assertEquals( XSD.gDay, A.getAllValuesFrom(), "Restriction should be all values from gDay" );
                    assertTrue( A.hasAllValuesFrom( XSD.gDay ), "Restriction should be all values from gDay" );
                    assertTrue( !A.hasAllValuesFrom( XSD.decimal ), "Restriction should not be all values from decimal" );
                    assertEquals( 1, A.getCardinality( prof.ALL_VALUES_FROM() ), "cardinality should be 1 ");

                    A.setAllValuesFrom( XSD.gMonth );

                    assertEquals( XSD.gMonth, A.getAllValuesFrom(), "Restriction should be all values from gMonth" );
                    assertTrue( !A.hasAllValuesFrom( XSD.gDay ), "Restriction should not be all values from gDay" );
                    assertTrue( A.hasAllValuesFrom( XSD.gMonth ), "Restriction should be all values from gMonth" );
                    assertEquals( 1, A.getCardinality( prof.ALL_VALUES_FROM() ), "cardinality should be 1 ");

                    A.removeAllValuesFrom( XSD.gMonth );

                    assertTrue( !A.hasAllValuesFrom( XSD.gMonth ), "Restriction should not be some values from gMonth" );
                    assertEquals( 0, A.getCardinality( prof.ALL_VALUES_FROM() ), "cardinality should be 0 ");
                }
            },
            new OntTestCase( "AllValuesFromRestriction.allValuesFrom.literal", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createObjectProperty( NS + "p" );

                    AllValuesFromRestriction A = m.createAllValuesFromRestriction( NS + "A", p, RDFS.Literal  );

                    assertEquals( RDFS.Literal, A.getAllValuesFrom(), "Restriction should be all values from literal" );
                    assertTrue( A.hasAllValuesFrom( RDFS.Literal ), "Restriction should be all values from literal" );
                    assertTrue( !A.hasAllValuesFrom( XSD.decimal ), "Restriction should not be all values from decimal" );
                    assertEquals( 1, A.getCardinality( prof.ALL_VALUES_FROM() ), "cardinality should be 1 ");
                }
            },
            new OntTestCase( "AllValuesFromRestriction.allValuesFrom.datarange", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    Literal x = m.createTypedLiteral( 1 );
                    Literal y = m.createTypedLiteral( 2 );
                    DataRange dr = m.createDataRange( m.createList( new RDFNode[] {x, y} ) );
                    OntProperty p = m.createObjectProperty( NS + "p" );

                    AllValuesFromRestriction A = m.createAllValuesFromRestriction( NS + "A", p, dr  );

                    assertEquals( dr, A.getAllValuesFrom(), "Restriction should be all values from dr" );
                    assertTrue( A.getAllValuesFrom() instanceof DataRange, "value should be a datarange" );
                    assertTrue( A.hasAllValuesFrom( dr ), "Restriction should be all values from dr" );
                    assertTrue( !A.hasAllValuesFrom( XSD.decimal ), "Restriction should not be all values from decimal" );
                    assertEquals( 1, A.getCardinality( prof.ALL_VALUES_FROM() ), "cardinality should be 1 ");

                    A.removeAllValuesFrom( dr );

                    assertTrue( !A.hasAllValuesFrom( dr ), "Restriction should not be some values from gMonth" );
                    assertEquals( 0, A.getCardinality( prof.ALL_VALUES_FROM() ), "cardinality should be 0 ");
                }
            },
            new OntTestCase( "HasValueRestriction.hasValue", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createObjectProperty( NS + "p" );
                    OntClass B = m.createClass( NS + "B" );
                    Individual b = m.createIndividual( B );
                    OntClass C = m.createClass( NS + "C" );
                    Individual c = m.createIndividual( C );

                    HasValueRestriction A = m.createHasValueRestriction( NS + "A", p, b  );

                    assertEquals( b, A.getHasValue(), "Restriction should be has value b" );
                    assertTrue( A.getHasValue() instanceof Individual );
                    assertTrue( A.hasValue( b ), "Restriction should be to have value b" );
                    assertTrue( !A.hasValue( c ), "Restriction should not be have value c" );
                    assertEquals( 1, A.getCardinality( prof.HAS_VALUE() ), "cardinality should be 1 ");

                    A.setHasValue( c );

                    assertEquals( c, A.getHasValue(), "Restriction should be has value c" );
                    assertTrue( !A.hasValue( b ), "Restriction should not be to have value b" );
                    assertTrue( A.hasValue( c ), "Restriction should not be have value c" );
                    assertEquals( 1, A.getCardinality( prof.HAS_VALUE() ), "cardinality should be 1 ");

                    A.removeHasValue( c );

                    assertTrue( !A.hasValue( b ), "Restriction should not be to have value b" );
                    assertTrue( !A.hasValue( c ), "Restriction should not be have value c" );
                    assertEquals( 0, A.getCardinality( prof.HAS_VALUE() ), "cardinality should be 0 ");
                }
            },
            new OntTestCase( "SomeValuesFromRestriction.someValuesFrom", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createObjectProperty( NS + "p" );
                    OntClass B = m.createClass( NS + "B" );
                    OntClass C = m.createClass( NS + "C" );

                    SomeValuesFromRestriction A = m.createSomeValuesFromRestriction( NS + "A", p, B  );

                    assertEquals( B, A.getSomeValuesFrom(), "Restriction should be some values from B" );
                    assertTrue( A.hasSomeValuesFrom( B ), "Restriction should be some values from B" );
                    assertTrue( !A.hasSomeValuesFrom( C ), "Restriction should not be some values from C" );
                    assertEquals( 1, A.getCardinality( prof.SOME_VALUES_FROM() ), "cardinality should be 1 ");

                    A.setSomeValuesFrom( C );

                    assertEquals( C, A.getSomeValuesFrom(), "Restriction should be some values from C" );
                    assertTrue( !A.hasSomeValuesFrom( B ), "Restriction should not be some values from B" );
                    assertTrue( A.hasSomeValuesFrom( C ), "Restriction should be some values from C" );
                    assertEquals( 1, A.getCardinality( prof.SOME_VALUES_FROM() ), "cardinality should be 1 ");

                    A.removeSomeValuesFrom( C );

                    assertTrue( !A.hasSomeValuesFrom( C ), "Restriction should not be some values from C" );
                    assertEquals( 0, A.getCardinality( prof.SOME_VALUES_FROM() ), "cardinality should be 0 ");
                }
            },
            new OntTestCase( "SomeValuesFromRestriction.SomeValuesFrom.datatype", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createObjectProperty( NS + "p" );

                    SomeValuesFromRestriction A = m.createSomeValuesFromRestriction( NS + "A", p, XSD.gDay  );

                    assertEquals( XSD.gDay, A.getSomeValuesFrom(), "Restriction should be some values from gDay" );
                    assertTrue( A.hasSomeValuesFrom( XSD.gDay ), "Restriction should be some values from gDay" );
                    assertTrue( !A.hasSomeValuesFrom( XSD.decimal ), "Restriction should not be some values from decimal" );
                    assertEquals( 1, A.getCardinality( prof.SOME_VALUES_FROM() ), "cardinality should be 1 ");

                    A.setSomeValuesFrom( XSD.gMonth );

                    assertEquals( XSD.gMonth, A.getSomeValuesFrom(), "Restriction should be some values from gMonth" );
                    assertTrue( !A.hasSomeValuesFrom( XSD.gDay ), "Restriction should not be some values from gDay" );
                    assertTrue( A.hasSomeValuesFrom( XSD.gMonth ), "Restriction should be some values from gMonth" );
                    assertEquals( 1, A.getCardinality( prof.SOME_VALUES_FROM() ), "cardinality should be 1 ");

                    A.removeSomeValuesFrom( XSD.gMonth );

                    assertTrue( !A.hasSomeValuesFrom( XSD.gMonth ), "Restriction should not be some values from gMonth" );
                    assertEquals( 0, A.getCardinality( prof.SOME_VALUES_FROM() ), "cardinality should be 0 ");
                }
            },
            new OntTestCase( "SomeValuesFromRestriction.SomeValuesFrom.literal", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createObjectProperty( NS + "p" );

                    SomeValuesFromRestriction A = m.createSomeValuesFromRestriction( NS + "A", p, RDFS.Literal  );

                    assertEquals( RDFS.Literal, A.getSomeValuesFrom(), "Restriction should be some values from literal" );
                    assertTrue( A.hasSomeValuesFrom( RDFS.Literal ), "Restriction should be some values from literal" );
                    assertTrue( !A.hasSomeValuesFrom( XSD.decimal ), "Restriction should not be some values from decimal" );
                    assertEquals( 1, A.getCardinality( prof.SOME_VALUES_FROM() ), "cardinality should be 1 ");
                }
            },
            new OntTestCase( "SomeValuesFromRestriction.SomeValuesFrom.datarange", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    Literal x = m.createTypedLiteral( 1 );
                    Literal y = m.createTypedLiteral( 2 );
                    DataRange dr = m.createDataRange( m.createList( new RDFNode[] {x, y} ) );
                    OntProperty p = m.createObjectProperty( NS + "p" );

                    SomeValuesFromRestriction A = m.createSomeValuesFromRestriction( NS + "A", p, dr  );

                    assertEquals( dr, A.getSomeValuesFrom(), "Restriction should be some values from dr" );
                    assertTrue( A.getSomeValuesFrom() instanceof DataRange, "value should be a datarange" );
                    assertTrue( A.hasSomeValuesFrom( dr ), "Restriction should be some values from dr" );
                    assertTrue( !A.hasSomeValuesFrom( XSD.decimal ), "Restriction should not be some values from decimal" );
                    assertEquals( 1, A.getCardinality( prof.SOME_VALUES_FROM() ), "cardinality should be 1 ");

                    A.removeSomeValuesFrom( dr );

                    assertTrue( !A.hasSomeValuesFrom( dr ), "Restriction should not be some values from gMonth" );
                    assertEquals( 0, A.getCardinality( prof.SOME_VALUES_FROM() ), "cardinality should be 0 ");
                }
            },
            new OntTestCase( "CardinalityRestriction.cardinality", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createObjectProperty( NS + "p" );

                    CardinalityRestriction A = m.createCardinalityRestriction( NS + "A", p, 3  );

                    assertEquals( 3, A.getCardinality(), "Restriction should be cardinality 3" );
                    assertTrue( A.hasCardinality( 3 ), "Restriction should be cardinality 3" );
                    assertTrue( !A.hasCardinality( 2 ), "Restriction should not be cardinality 2" );
                    assertEquals( 1, A.getCardinality( prof.CARDINALITY() ), "cardinality should be 1 ");

                    A.setCardinality( 2 );

                    assertEquals( 2, A.getCardinality(), "Restriction should be cardinality 2" );
                    assertTrue( !A.hasCardinality( 3 ), "Restriction should not be cardinality 3" );
                    assertTrue( A.hasCardinality( 2 ), "Restriction should be cardinality 2" );
                    assertEquals( 1, A.getCardinality( prof.CARDINALITY() ), "cardinality should be 1 ");

                    A.removeCardinality( 2 );

                    assertTrue( !A.hasCardinality( 3 ), "Restriction should not be cardinality 3" );
                    assertTrue( !A.hasCardinality( 2 ), "Restriction should not be cardinality 2" );
                    assertEquals( 0, A.getCardinality( prof.CARDINALITY() ), "cardinality should be 0 ");
                }
            },
            new OntTestCase( "MinCardinalityRestriction.minCardinality", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createObjectProperty( NS + "p" );

                    MinCardinalityRestriction A = m.createMinCardinalityRestriction( NS + "A", p, 3  );

                    assertEquals( 3, A.getMinCardinality(), "Restriction should be min cardinality 3" );
                    assertTrue( A.hasMinCardinality( 3 ), "Restriction should be min cardinality 3" );
                    assertTrue( !A.hasMinCardinality( 2 ), "Restriction should not be min cardinality 2" );
                    assertEquals( 1, A.getCardinality( prof.MIN_CARDINALITY() ), "cardinality should be 1 ");

                    A.setMinCardinality( 2 );

                    assertEquals( 2, A.getMinCardinality(), "Restriction should be min cardinality 2" );
                    assertTrue( !A.hasMinCardinality( 3 ), "Restriction should not be min cardinality 3" );
                    assertTrue( A.hasMinCardinality( 2 ), "Restriction should be min cardinality 2" );
                    assertEquals( 1, A.getCardinality( prof.MIN_CARDINALITY() ), "cardinality should be 1 ");

                    A.removeMinCardinality( 2 );

                    assertTrue( !A.hasMinCardinality( 3 ), "Restriction should not be cardinality 3" );
                    assertTrue( !A.hasMinCardinality( 2 ), "Restriction should not be cardinality 2" );
                    assertEquals( 0, A.getCardinality( prof.MIN_CARDINALITY() ), "cardinality should be 0 ");
                }
            },
            new OntTestCase( "MaxCardinalityRestriction.maxCardinality", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createObjectProperty( NS + "p" );

                    MaxCardinalityRestriction A = m.createMaxCardinalityRestriction( NS + "A", p, 3  );

                    assertEquals( 3, A.getMaxCardinality(), "Restriction should be max cardinality 3" );
                    assertTrue( A.hasMaxCardinality( 3 ), "Restriction should be max cardinality 3" );
                    assertTrue( !A.hasMaxCardinality( 2 ), "Restriction should not be max cardinality 2" );
                    assertEquals( 1, A.getCardinality( prof.MAX_CARDINALITY() ), "cardinality should be 1 ");

                    A.setMaxCardinality( 2 );

                    assertEquals( 2, A.getMaxCardinality(), "Restriction should be max cardinality 2" );
                    assertTrue( !A.hasMaxCardinality( 3 ), "Restriction should not be max cardinality 3" );
                    assertTrue( A.hasMaxCardinality( 2 ), "Restriction should be max cardinality 2" );
                    assertEquals( 1, A.getCardinality( prof.MAX_CARDINALITY() ), "cardinality should be 1 ");

                    A.removeMaxCardinality( 2 );

                    assertTrue( !A.hasMaxCardinality( 3 ), "Restriction should not be cardinality 3" );
                    assertTrue( !A.hasMaxCardinality( 2 ), "Restriction should not be cardinality 2" );
                    assertEquals( 0, A.getCardinality( prof.MAX_CARDINALITY() ), "cardinality should be 0 ");
                }
            },
            new OntTestCase( "QualifiedRestriction.hasClassQ", false, false, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntProperty p = m.createObjectProperty( NS + "p" );
                    OntClass c = m.createClass( NS + "C" );
                    OntClass d = m.createClass( NS + "D" );

                    String nameA = "ABCBA";
                    QualifiedRestriction A = m.createMaxCardinalityQRestriction( NS + nameA, p, 3, c  );

                    assertEquals( c, A.getHasClassQ(), "Restriction should hasClassQ c" );
                    assertTrue( A.hasHasClassQ( c ), "Restriction should be hasClassQ c" );
                    assertFalse( A.hasHasClassQ( d ), "Restriction should not be hasClassQ d" );

                    A.setHasClassQ( d );

                    assertEquals( d, A.getHasClassQ(), "Restriction should hasClassQ d" );
                    assertTrue( A.hasHasClassQ( d ), "Restriction should be hasClassQ d" );
                    assertFalse( A.hasHasClassQ( c ), "Restriction should not be hasClassQ c" );

                    assertTrue( m.getResource( NS + nameA ).canAs( QualifiedRestriction.class ), "Should be a qualified restriction" );
                    A.removeHasClassQ( d );
                    assertFalse( m.getResource( NS + nameA ).canAs( QualifiedRestriction.class ), "Should not be a qualified restriction" );
                }
            },
            new OntTestCase( "CardinalityQRestriction.cardinality", false, false, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntProperty p = m.createObjectProperty( NS + "p" );
                    OntClass c = m.createClass( NS + "C" );

                    CardinalityQRestriction A = m.createCardinalityQRestriction( NS + "A", p, 3, c  );

                    assertEquals( 3, A.getCardinalityQ(), "Restriction should cardinality 3" );
                    assertTrue( A.hasCardinalityQ( 3 ), "Restriction should be cardinality 3" );
                    assertFalse( A.hasCardinalityQ( 1 ), "Restriction should not be cardinality 1" );

                    A.setCardinalityQ( 1 );

                    assertEquals( 1, A.getCardinalityQ(), "Restriction should cardinality 1" );
                    assertFalse( A.hasCardinalityQ( 3 ), "Restriction should not be cardinality 3" );
                    assertTrue( A.hasCardinalityQ( 1 ), "Restriction should be cardinality 1" );

                    assertTrue( m.getResource( NS + "A" ).canAs( CardinalityQRestriction.class ), "Should be a qualified cardinality restriction" );
                    A.removeCardinalityQ( 1 );
                    assertFalse( m.getResource( NS + "A" ).canAs( CardinalityQRestriction.class ), "Should not be a qualified cardinality restriction" );
                }
            },
            new OntTestCase( "MinCardinalityQRestriction.minCardinality", false, false, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntProperty p = m.createObjectProperty( NS + "p" );
                    OntClass c = m.createClass( NS + "C" );

                    MinCardinalityQRestriction A = m.createMinCardinalityQRestriction( NS + "A", p, 3, c  );

                    assertEquals( 3, A.getMinCardinalityQ(), "Restriction should min cardinality 3" );
                    assertTrue( A.hasMinCardinalityQ( 3 ), "Restriction should be min cardinality 3" );
                    assertFalse( A.hasMinCardinalityQ( 1 ), "Restriction should not be min cardinality 1" );

                    A.setMinCardinalityQ( 1 );

                    assertEquals( 1, A.getMinCardinalityQ(), "Restriction should min cardinality 1" );
                    assertFalse( A.hasMinCardinalityQ( 3 ), "Restriction should not be min cardinality 3" );
                    assertTrue( A.hasMinCardinalityQ( 1 ), "Restriction should be min cardinality 1" );

                    assertTrue( m.getResource( NS + "A" ).canAs( MinCardinalityQRestriction.class ), "Should be a qualified min cardinality restriction" );
                    A.removeMinCardinalityQ( 1 );
                    assertFalse( m.getResource( NS + "A" ).canAs( MinCardinalityQRestriction.class ), "Should not be a qualified min cardinality restriction" );
                }
            },
            new OntTestCase( "MaxCardinalityQRestriction.maxCardinality", false, false, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntProperty p = m.createObjectProperty( NS + "p" );
                    OntClass c = m.createClass( NS + "C" );

                    MaxCardinalityQRestriction A = m.createMaxCardinalityQRestriction( NS + "A", p, 3, c  );

                    assertEquals( 3, A.getMaxCardinalityQ(), "Restriction should max cardinality 3" );
                    assertTrue( A.hasMaxCardinalityQ( 3 ), "Restriction should be max cardinality 3" );
                    assertFalse( A.hasMaxCardinalityQ( 1 ), "Restriction should not be max cardinality 1" );

                    A.setMaxCardinalityQ( 1 );

                    assertEquals( 1, A.getMaxCardinalityQ(), "Restriction should max cardinality 1" );
                    assertFalse( A.hasMaxCardinalityQ( 3 ), "Restriction should not be max cardinality 3" );
                    assertTrue( A.hasMaxCardinalityQ( 1 ), "Restriction should be max cardinality 1" );

                    assertTrue( m.getResource( NS + "A" ).canAs( MaxCardinalityQRestriction.class ), "Should be a qualified max cardinality restriction" );
                    A.removeMaxCardinalityQ( 1 );
                    assertFalse( m.getResource( NS + "A" ).canAs( MaxCardinalityQRestriction.class ), "Should not be a qualified max cardinality restriction" );
                }
            },

            // from file
            new OntTestCase( "OntClass.subclass.fromFile", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) {
                    String lang = m_owlLang ? "owl" : "rdfs";
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
                public void ontTest( OntModel m ) {
                    String lang = m_owlLang ? "owl" : "rdfs";
                    String fileName = "file:testing/ontology/" + lang + "/ClassExpression/test.rdf";
                    m.read( fileName );

                    OntClass A = m.createClass( NS + "ClassA" );
                    OntClass C = m.createClass( NS + "ClassC" );

                    assertTrue( A.hasEquivalentClass( C ), "A should be equiv to C" );
                }
            },
            new OntTestCase( "OntClass.disjoint.fromFile", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    String lang = m_owlLang ? "owl" : "rdfs";
                    String fileName = "file:testing/ontology/" + lang + "/ClassExpression/test.rdf";
                    m.read( fileName );

                    OntClass A = m.createClass( NS + "ClassA" );
                    OntClass D = m.createClass( NS + "ClassD" );

                    assertTrue( A.isDisjointWith( D ), "A should be disjoint with D" );
                }
            },

            // type testing
            new OntTestCase( "OntClass.isEnumeratedClass", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntClass b = m.createClass( NS + "B" );
                    Individual x = m.createIndividual( NS + "x", b );
                    Individual y = m.createIndividual( NS + "y", b );
                    OntClass a = m.createEnumeratedClass( NS + "A", m.createList( new RDFNode[] {x, y} ) );

                    assertTrue( a.isEnumeratedClass(), "enumerated class test not correct" );
                    assertTrue( !a.isIntersectionClass(), "intersection class test not correct" );
                    assertTrue( !a.isUnionClass(), "union class test not correct" );
                    assertTrue( !a.isComplementClass(), "complement class test not correct" );
                    assertTrue( !a.isRestriction(), "restriction test not correct" );
                }
            },
            new OntTestCase( "OntClass.isIntersectionClass", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntClass b = m.createClass( NS + "B" );
                    OntClass c = m.createClass( NS + "C" );
                    OntClass a = m.createIntersectionClass( NS + "A", m.createList( new RDFNode[] {b,c} ) );

                    assertTrue( m_owlLiteLang || !a.isEnumeratedClass(), "enumerated class test not correct" );
                    assertTrue( a.isIntersectionClass(), "intersection class test not correct" );
                    assertTrue( m_owlLiteLang || !a.isUnionClass(), "union class test not correct" );
                    assertTrue( m_owlLiteLang || !a.isComplementClass(), "complement class test not correct" );
                    assertTrue( !a.isRestriction(), "restriction test not correct" );
                }
            },
            new OntTestCase( "OntClass.isUnionClass", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntClass b = m.createClass( NS + "B" );
                    OntClass c = m.createClass( NS + "C" );
                    OntClass a = m.createUnionClass( NS + "A", m.createList( new RDFNode[] {b,c} ) );

                    assertTrue( !a.isEnumeratedClass(), "enumerated class test not correct" );
                    assertTrue( !a.isIntersectionClass(), "intersection class test not correct" );
                    assertTrue( a.isUnionClass(), "union class test not correct" );
                    assertTrue( !a.isComplementClass(), "complement class test not correct" );
                    assertTrue( !a.isRestriction(), "restriction test not correct" );
                }
            },
            new OntTestCase( "OntClass.isComplementClass", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntClass b = m.createClass( NS + "B" );
                    OntClass a = m.createComplementClass( NS + "A", b );

                    assertTrue( !a.isEnumeratedClass(), "enumerated class test not correct" );
                    assertTrue( !a.isIntersectionClass(), "intersection class test not correct" );
                    assertTrue( !a.isUnionClass(), "union class test not correct" );
                    assertTrue( a.isComplementClass(), "complement class test not correct" );
                    assertTrue( !a.isRestriction(), "restriction test not correct" );
                }
            },
            new OntTestCase( "OntClass.isRestriction", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntClass a = m.createRestriction( null );

                    assertTrue( m_owlLiteLang || !a.isEnumeratedClass(), "enumerated class test not correct" );
                    assertTrue( !a.isIntersectionClass(), "intersection class test not correct" );
                    assertTrue( m_owlLiteLang || !a.isUnionClass(), "union class test not correct" );
                    assertTrue( m_owlLiteLang || !a.isComplementClass(), "complement class test not correct" );
                    assertTrue( a.isRestriction(), "restriction test not correct" );
                }
            },

            // conversion
            new OntTestCase( "OntClass.toEnumeratedClass", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntClass a = m.createClass( NS + "A" );

                    assertTrue( !a.isEnumeratedClass(), "enumerated class test not correct" );
                    assertTrue( !a.isIntersectionClass(), "intersection class test not correct" );
                    assertTrue( !a.isUnionClass(), "union class test not correct" );
                    assertTrue( !a.isComplementClass(), "complement class test not correct" );
                    assertTrue( !a.isRestriction(), "restriction test not correct" );

                    OntClass b = m.createClass( NS + "B" );
                    Individual x = m.createIndividual( NS + "x", b );
                    Individual y = m.createIndividual( NS + "y", b );
                    a = a.convertToEnumeratedClass( m.createList( new RDFNode[] {x, y} ) );

                    assertTrue( a.isEnumeratedClass(), "enumerated class test not correct" );
                    assertTrue( !a.isIntersectionClass(), "intersection class test not correct" );
                    assertTrue( !a.isUnionClass(), "union class test not correct" );
                    assertTrue( !a.isComplementClass(), "complement class test not correct" );
                    assertTrue( !a.isRestriction(), "restriction test not correct" );
                }
            },
            new OntTestCase( "OntClass.toIntersectionClass", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntClass a = m.createClass( NS + "A" );

                    assertTrue( m_owlLiteLang || !a.isEnumeratedClass(), "enumerated class test not correct" );
                    assertTrue( !a.isIntersectionClass(), "intersection class test not correct" );
                    assertTrue( m_owlLiteLang || !a.isUnionClass(), "union class test not correct" );
                    assertTrue( m_owlLiteLang || !a.isComplementClass(), "complement class test not correct" );
                    assertTrue( !a.isRestriction(), "restriction test not correct" );

                    OntClass b = m.createClass( NS + "B" );
                    OntClass c = m.createClass( NS + "C" );
                    a = a.convertToIntersectionClass( m.createList( new RDFNode[] {b,c} ) );

                    assertTrue( m_owlLiteLang || !a.isEnumeratedClass(), "enumerated class test not correct" );
                    assertTrue( a.isIntersectionClass(), "intersection class test not correct" );
                    assertTrue( m_owlLiteLang || !a.isUnionClass(), "union class test not correct" );
                    assertTrue( m_owlLiteLang || !a.isComplementClass(), "complement class test not correct" );
                    assertTrue( !a.isRestriction(), "restriction test not correct" );
                }
            },
            new OntTestCase( "OntClass.toUnionClass", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntClass a = m.createClass( NS + "A" );

                    assertTrue( !a.isEnumeratedClass(), "enumerated class test not correct" );
                    assertTrue( !a.isIntersectionClass(), "intersection class test not correct" );
                    assertTrue( !a.isUnionClass(), "union class test not correct" );
                    assertTrue( !a.isComplementClass(), "complement class test not correct" );
                    assertTrue( !a.isRestriction(), "restriction test not correct" );

                    OntClass b = m.createClass( NS + "B" );
                    OntClass c = m.createClass( NS + "C" );
                    a = a.convertToUnionClass( m.createList( new RDFNode[] {b,c} ) );

                    assertTrue( m_owlLiteLang || !a.isEnumeratedClass(), "enumerated class test not correct" );
                    assertTrue( !a.isIntersectionClass(), "intersection class test not correct" );
                    assertTrue( m_owlLiteLang || a.isUnionClass(), "union class test not correct" );
                    assertTrue( m_owlLiteLang || !a.isComplementClass(), "complement class test not correct" );
                    assertTrue( !a.isRestriction(), "restriction test not correct" );
                }
            },
            new OntTestCase( "OntClass.toComplementClass", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntClass a = m.createClass( NS + "A" );

                    assertTrue( !a.isEnumeratedClass(), "enumerated class test not correct" );
                    assertTrue( !a.isIntersectionClass(), "intersection class test not correct" );
                    assertTrue( !a.isUnionClass(), "union class test not correct" );
                    assertTrue( !a.isComplementClass(), "complement class test not correct" );
                    assertTrue( !a.isRestriction(), "restriction test not correct" );

                    OntClass b = m.createClass( NS + "B" );
                    a = a.convertToComplementClass( b );

                    assertTrue( m_owlLiteLang || !a.isEnumeratedClass(), "enumerated class test not correct" );
                    assertTrue( !a.isIntersectionClass(), "intersection class test not correct" );
                    assertTrue( m_owlLiteLang || !a.isUnionClass(), "union class test not correct" );
                    assertTrue( m_owlLiteLang || a.isComplementClass(), "complement class test not correct" );
                    assertTrue( !a.isRestriction(), "restriction test not correct" );
                }
            },
            new OntTestCase( "OntClass.toRestriction", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntClass a = m.createClass( NS + "A" );

                    assertTrue( m_owlLiteLang || !a.isEnumeratedClass(), "enumerated class test not correct" );
                    assertTrue( !a.isIntersectionClass(), "intersection class test not correct" );
                    assertTrue( m_owlLiteLang || !a.isUnionClass(), "union class test not correct" );
                    assertTrue( m_owlLiteLang || !a.isComplementClass(), "complement class test not correct" );
                    assertTrue( !a.isRestriction(), "restriction test not correct" );

                    ObjectProperty p = m.createObjectProperty( NS + "p" );
                    a = a.convertToRestriction( p );

                    assertTrue( m_owlLiteLang || !a.isEnumeratedClass(), "enumerated class test not correct" );
                    assertTrue( !a.isIntersectionClass(), "intersection class test not correct" );
                    assertTrue( m_owlLiteLang || !a.isUnionClass(), "union class test not correct" );
                    assertTrue( m_owlLiteLang || !a.isComplementClass(), "complement class test not correct" );
                    assertTrue( a.isRestriction(), "restriction test not correct" );
                }
            },

            // restriction type testing
            new OntTestCase( "Restriction.isAllValuesFrom", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntClass b = m.createClass( NS + "B" );
                    ObjectProperty p = m.createObjectProperty( NS + "p" );
                    Restriction a = m.createAllValuesFromRestriction( null, p, b );

                    assertTrue( a.isAllValuesFromRestriction(), "all values from test not correct" );
                    assertTrue( !a.isSomeValuesFromRestriction(), "some values from test not correct" );
                    assertTrue( m_owlLiteLang || !a.isHasValueRestriction(), "has value test not correct" );
                    assertTrue( !a.isCardinalityRestriction(), "cardinality test not correct" );
                    assertTrue( !a.isMinCardinalityRestriction(), "min cardinality test not correct" );
                    assertTrue( !a.isMaxCardinalityRestriction(), "max cardinality test not correct" );
                }
            },
            new OntTestCase( "Restriction.isSomeValuesFrom", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntClass b = m.createClass( NS + "B" );
                    ObjectProperty p = m.createObjectProperty( NS + "p" );
                    Restriction a = m.createSomeValuesFromRestriction( null, p, b );

                    assertTrue( !a.isAllValuesFromRestriction(), "all values from test not correct" );
                    assertTrue( a.isSomeValuesFromRestriction(), "some values from test not correct" );
                    assertTrue( m_owlLiteLang || !a.isHasValueRestriction(), "has value test not correct" );
                    assertTrue( !a.isCardinalityRestriction(), "cardinality test not correct" );
                    assertTrue( !a.isMinCardinalityRestriction(), "min cardinality test not correct" );
                    assertTrue( !a.isMaxCardinalityRestriction(), "max cardinality test not correct" );
                }
            },
            new OntTestCase( "Restriction.isHasValue", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    OntClass b = m.createClass( NS + "B" );
                    Individual x = m.createIndividual( b );
                    ObjectProperty p = m.createObjectProperty( NS + "p" );
                    Restriction a = m.createHasValueRestriction( null, p, x );

                    assertTrue( !a.isAllValuesFromRestriction(), "all values from test not correct" );
                    assertTrue( !a.isSomeValuesFromRestriction(), "some values from test not correct" );
                    assertTrue( m_owlLiteLang || a.isHasValueRestriction(), "has value test not correct" );
                    assertTrue( !a.isCardinalityRestriction(), "cardinality test not correct" );
                    assertTrue( !a.isMinCardinalityRestriction(), "min cardinality test not correct" );
                    assertTrue( !a.isMaxCardinalityRestriction(), "max cardinality test not correct" );
                }
            },
            new OntTestCase( "Restriction.isCardinality", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    ObjectProperty p = m.createObjectProperty( NS + "p" );
                    Restriction a = m.createCardinalityRestriction( null, p, 3 );

                    assertTrue( !a.isAllValuesFromRestriction(), "all values from test not correct" );
                    assertTrue( !a.isSomeValuesFromRestriction(), "some values from test not correct" );
                    assertTrue( m_owlLiteLang || !a.isHasValueRestriction(), "has value test not correct" );
                    assertTrue( a.isCardinalityRestriction(), "cardinality test not correct" );
                    assertTrue( !a.isMinCardinalityRestriction(), "min cardinality test not correct" );
                    assertTrue( !a.isMaxCardinalityRestriction(), "max cardinality test not correct" );
                }
            },
            new OntTestCase( "Restriction.isMinCardinality", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    ObjectProperty p = m.createObjectProperty( NS + "p" );
                    Restriction a = m.createMinCardinalityRestriction( null, p, 1 );

                    assertTrue( !a.isAllValuesFromRestriction(), "all values from test not correct" );
                    assertTrue( !a.isSomeValuesFromRestriction(), "some values from test not correct" );
                    assertTrue( m_owlLiteLang || !a.isHasValueRestriction(), "has value test not correct" );
                    assertTrue( !a.isCardinalityRestriction(), "cardinality test not correct" );
                    assertTrue( a.isMinCardinalityRestriction(), "min cardinality test not correct" );
                    assertTrue( !a.isMaxCardinalityRestriction(), "max cardinality test not correct" );
                }
            },
            new OntTestCase( "Restriction.isMaxCardinality", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    ObjectProperty p = m.createObjectProperty( NS + "p" );
                    Restriction a = m.createMaxCardinalityRestriction( null, p, 5 );

                    assertTrue( !a.isAllValuesFromRestriction(), "all values from test not correct" );
                    assertTrue( !a.isSomeValuesFromRestriction(), "some values from test not correct" );
                    assertTrue( m_owlLiteLang || !a.isHasValueRestriction(), "has value test not correct" );
                    assertTrue( !a.isCardinalityRestriction(), "cardinality test not correct" );
                    assertTrue( !a.isMinCardinalityRestriction(), "min cardinality test not correct" );
                    assertTrue( a.isMaxCardinalityRestriction(), "max cardinality test not correct" );
                }
            },

            // restriction conversions
            new OntTestCase( "Restriction.convertToAllValuesFrom", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    ObjectProperty p = m.createObjectProperty( NS + "p" );
                    Restriction a = m.createRestriction( p );

                    assertTrue( !a.isAllValuesFromRestriction(), "all values from test not correct" );
                    assertTrue( !a.isSomeValuesFromRestriction(), "some values from test not correct" );
                    assertTrue( m_owlLiteLang || !a.isHasValueRestriction(), "has value test not correct" );
                    assertTrue( !a.isCardinalityRestriction(), "cardinality test not correct" );
                    assertTrue( !a.isMinCardinalityRestriction(), "min cardinality test not correct" );
                    assertTrue( !a.isMaxCardinalityRestriction(), "max cardinality test not correct" );

                    OntClass b = m.createClass( NS + "B" );
                    a = a.convertToAllValuesFromRestriction( b );

                    assertTrue( a.isAllValuesFromRestriction(), "all values from test not correct" );
                    assertTrue( !a.isSomeValuesFromRestriction(), "some values from test not correct" );
                    assertTrue( m_owlLiteLang || !a.isHasValueRestriction(), "has value test not correct" );
                    assertTrue( !a.isCardinalityRestriction(), "cardinality test not correct" );
                    assertTrue( !a.isMinCardinalityRestriction(), "min cardinality test not correct" );
                    assertTrue( !a.isMaxCardinalityRestriction(), "max cardinality test not correct" );
                }
            },
            new OntTestCase( "Restriction.convertToSomeValuesFrom", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    ObjectProperty p = m.createObjectProperty( NS + "p" );
                    Restriction a = m.createRestriction( p );

                    assertTrue( !a.isAllValuesFromRestriction(), "all values from test not correct" );
                    assertTrue( !a.isSomeValuesFromRestriction(), "some values from test not correct" );
                    assertTrue( m_owlLiteLang || !a.isHasValueRestriction(), "has value test not correct" );
                    assertTrue( !a.isCardinalityRestriction(), "cardinality test not correct" );
                    assertTrue( !a.isMinCardinalityRestriction(), "min cardinality test not correct" );
                    assertTrue( !a.isMaxCardinalityRestriction(), "max cardinality test not correct" );

                    OntClass b = m.createClass( NS + "B" );
                    a = a.convertToSomeValuesFromRestriction( b );

                    assertTrue( !a.isAllValuesFromRestriction(), "all values from test not correct" );
                    assertTrue( a.isSomeValuesFromRestriction(), "some values from test not correct" );
                    assertTrue( m_owlLiteLang || !a.isHasValueRestriction(), "has value test not correct" );
                    assertTrue( !a.isCardinalityRestriction(), "cardinality test not correct" );
                    assertTrue( !a.isMinCardinalityRestriction(), "min cardinality test not correct" );
                    assertTrue( !a.isMaxCardinalityRestriction(), "max cardinality test not correct" );
                }
            },
            new OntTestCase( "Restriction.convertToHasValue", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    ObjectProperty p = m.createObjectProperty( NS + "p" );
                    Restriction a = m.createRestriction( p );

                    assertTrue( !a.isAllValuesFromRestriction(), "all values from test not correct" );
                    assertTrue( !a.isSomeValuesFromRestriction(), "some values from test not correct" );
                    assertTrue( m_owlLiteLang || !a.isHasValueRestriction(), "has value test not correct" );
                    assertTrue( !a.isCardinalityRestriction(), "cardinality test not correct" );
                    assertTrue( !a.isMinCardinalityRestriction(), "min cardinality test not correct" );
                    assertTrue( !a.isMaxCardinalityRestriction(), "max cardinality test not correct" );

                    OntClass b = m.createClass( NS + "B" );
                    Individual x = m.createIndividual( b );
                    a = a.convertToHasValueRestriction( x );

                    assertTrue( !a.isAllValuesFromRestriction(), "all values from test not correct" );
                    assertTrue( !a.isSomeValuesFromRestriction(), "some values from test not correct" );
                    assertTrue( m_owlLiteLang || a.isHasValueRestriction(), "has value test not correct" );
                    assertTrue( !a.isCardinalityRestriction(), "cardinality test not correct" );
                    assertTrue( !a.isMinCardinalityRestriction(), "min cardinality test not correct" );
                    assertTrue( !a.isMaxCardinalityRestriction(), "max cardinality test not correct" );
                }
            },
            new OntTestCase( "Restriction.convertCardinality", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    ObjectProperty p = m.createObjectProperty( NS + "p" );
                    Restriction a = m.createRestriction( p );

                    assertTrue( !a.isAllValuesFromRestriction(), "all values from test not correct" );
                    assertTrue( !a.isSomeValuesFromRestriction(), "some values from test not correct" );
                    assertTrue( m_owlLiteLang || !a.isHasValueRestriction(), "has value test not correct" );
                    assertTrue( !a.isCardinalityRestriction(), "cardinality test not correct" );
                    assertTrue( !a.isMinCardinalityRestriction(), "min cardinality test not correct" );
                    assertTrue( !a.isMaxCardinalityRestriction(), "max cardinality test not correct" );

                    a = a.convertToCardinalityRestriction( 3 );

                    assertTrue( !a.isAllValuesFromRestriction(), "all values from test not correct" );
                    assertTrue( !a.isSomeValuesFromRestriction(), "some values from test not correct" );
                    assertTrue( m_owlLiteLang || !a.isHasValueRestriction(), "has value test not correct" );
                    assertTrue( a.isCardinalityRestriction(), "cardinality test not correct" );
                    assertTrue( !a.isMinCardinalityRestriction(), "min cardinality test not correct" );
                    assertTrue( !a.isMaxCardinalityRestriction(), "max cardinality test not correct" );
                }
            },
            new OntTestCase( "Restriction.convertMinCardinality", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    ObjectProperty p = m.createObjectProperty( NS + "p" );
                    Restriction a = m.createRestriction( p );

                    assertTrue( !a.isAllValuesFromRestriction(), "all values from test not correct" );
                    assertTrue( !a.isSomeValuesFromRestriction(), "some values from test not correct" );
                    assertTrue( m_owlLiteLang || !a.isHasValueRestriction(), "has value test not correct" );
                    assertTrue( !a.isCardinalityRestriction(), "cardinality test not correct" );
                    assertTrue( !a.isMinCardinalityRestriction(), "min cardinality test not correct" );
                    assertTrue( !a.isMaxCardinalityRestriction(), "max cardinality test not correct" );

                    a = a.convertToMinCardinalityRestriction( 3 );

                    assertTrue( !a.isAllValuesFromRestriction(), "all values from test not correct" );
                    assertTrue( !a.isSomeValuesFromRestriction(), "some values from test not correct" );
                    assertTrue( m_owlLiteLang || !a.isHasValueRestriction(), "has value test not correct" );
                    assertTrue( !a.isCardinalityRestriction(), "cardinality test not correct" );
                    assertTrue( a.isMinCardinalityRestriction(), "min cardinality test not correct" );
                    assertTrue( !a.isMaxCardinalityRestriction(), "max cardinality test not correct" );
                }
            },
            new OntTestCase( "Restriction.convertMaxCardinality", true, true, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    ObjectProperty p = m.createObjectProperty( NS + "p" );
                    Restriction a = m.createRestriction( p );

                    assertTrue( !a.isAllValuesFromRestriction(), "all values from test not correct" );
                    assertTrue( !a.isSomeValuesFromRestriction(), "some values from test not correct" );
                    assertTrue( m_owlLiteLang || !a.isHasValueRestriction(), "has value test not correct" );
                    assertTrue( !a.isCardinalityRestriction(), "cardinality test not correct" );
                    assertTrue( !a.isMinCardinalityRestriction(), "min cardinality test not correct" );
                    assertTrue( !a.isMaxCardinalityRestriction(), "max cardinality test not correct" );

                    a = a.convertToMaxCardinalityRestriction( 3 );

                    assertTrue( !a.isAllValuesFromRestriction(), "all values from test not correct" );
                    assertTrue( !a.isSomeValuesFromRestriction(), "some values from test not correct" );
                    assertTrue( m_owlLiteLang || !a.isHasValueRestriction(), "has value test not correct" );
                    assertTrue( !a.isCardinalityRestriction(), "cardinality test not correct" );
                    assertTrue( !a.isMinCardinalityRestriction(), "min cardinality test not correct" );
                    assertTrue( a.isMaxCardinalityRestriction(), "max cardinality test not correct" );
                }
            },
            new OntTestCase( "OntClass.listInstances", true, true, true ) {
                @Override
                public void ontTest( OntModel m ) {
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
                public void ontTest( OntModel m ) {
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
                public void ontTest( OntModel m ) {
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

                    assertNotNull( C.listDeclaredProperties( true ).next(), "declared property should be an ont prop" );
                    assertNotNull( C.listDeclaredProperties( false ).next(), "declared property should be an ont prop" );
                }
            },
            new OntTestCase( "DataRange.oneOf", true, false, false ) {
                @Override
                public void ontTest( OntModel m ) {
                    Literal x = m.createTypedLiteral( 42 );
                    Literal y = m.createTypedLiteral( true );
                    Literal z = m.createTypedLiteral( "life" );
                    RDFList lits = m.createList( new RDFNode[] {x,y} );

                    DataRange d0 = m.createDataRange( lits );

                    assertTrue( d0.hasOneOf( x ), "datarange should contain x" );
                    assertTrue( d0.hasOneOf( y ), "datarange should contain y" );
                    assertFalse( d0.hasOneOf( z ), "datarange should not contain z" );

                    d0.removeOneOf( z );
                    assertTrue( d0.hasOneOf( x ), "datarange should contain x" );
                    assertTrue( d0.hasOneOf( y ), "datarange should contain y" );
                    assertFalse( d0.hasOneOf( z ), "datarange should not contain z" );

                    d0.removeOneOf( x );
                    assertFalse( d0.hasOneOf( x ), "datarange should not contain x" );
                    assertTrue( d0.hasOneOf( y ), "datarange should contain y" );
                    assertFalse( d0.hasOneOf( z ), "datarange should not contain z" );

                    d0.addOneOf( z );
                    assertEquals( 2, d0.getOneOf().size(), "datarange should be size 2" );
                    iteratorTest( d0.listOneOf(), new Object[] {y,z} );

                    d0.setOneOf( m.createList( new RDFNode[] {x} ) );
                    iteratorTest( d0.listOneOf(), new Object[] {x} );
                }
            },

            // Removal

            new OntTestCase( "Remove intersection", true, true, false )  {
                @Override
                protected void ontTest(OntModel m) {
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
                protected void ontTest(OntModel m) {
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
