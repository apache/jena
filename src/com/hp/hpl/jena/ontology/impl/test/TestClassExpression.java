/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            27-May-2003
 * Filename           $RCSfile: TestClassExpression.java,v $
 * Revision           $Revision: 1.12 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-05-28 16:26:21 $
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
import java.util.ArrayList;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.RDFNode;

import junit.framework.*;


/**
 * <p>
 * Unit tests for OntClass and other class expressions.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: TestClassExpression.java,v 1.12 2003-05-28 16:26:21 ian_dickinson Exp $
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

    public OntTestCase[] getTests() {
        return new OntTestCase[] {
            new OntTestCase( "OntClass.super-class", true, true, true ) {
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
                    assertTrue( "A shuold not have super-class B", !A.hasSuperClass( B ) );
                }
            },
            new OntTestCase( "OntClass.sub-class", true, true, true ) {
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
                    assertTrue( "A shuold not have sub-class B", !A.hasSubClass( B ) );
                }
            },
            new OntTestCase( "OntClass.equivalentClass", true, true, true ) {
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
                }
            },
            new OntTestCase( "OntClass.disjointWith", true, false, true ) {
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
                }
            },
			new OntTestCase( "EnumeratedClass.oneOf", true, false, true ) {
				public void ontTest( OntModel m ) throws Exception {
					Profile prof = m.getProfile();
					EnumeratedClass A = m.createEnumeratedClass( NS + "A", null );
					OntResource a = (OntResource) m.getResource( NS + "a" ).as( OntResource.class );
					OntResource b = (OntResource) m.getResource( NS + "b" ).as( OntResource.class );
                    
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
				}
			},
			new OntTestCase( "IntersectionClass.intersectionOf", true, true, true ) {
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
                    
					A.setOperands( m.createList( new RDFNode[] {C} ) );
					assertEquals( "Cardinality should be 1", 1, A.getCardinality( prof.INTERSECTION_OF() ) );
					assertEquals( "Size should be 1", 1, A.getOperands().size() );
					assertTrue( "A should have C in the intersection", A.hasOperand( C ) );
					assertTrue( "A should not have B in the intersection", !A.hasOperand( B ) );
				}
			},
			new OntTestCase( "UnionClass.unionOf", true, false, true ) {
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
                    
					A.setOperands( m.createList( new RDFNode[] {C} ) );
					assertEquals( "Cardinality should be 1", 1, A.getCardinality( prof.UNION_OF() ) );
					assertEquals( "Size should be 1", 1, A.getOperands().size() );
					assertTrue( "A should have C in the union", A.hasOperand( C ) );
					assertTrue( "A should not have B in the union", !A.hasOperand( B ) );
				}
			},
			new OntTestCase( "ComplementClass.complementOf", true, false, true ) {
				public void ontTest( OntModel m ) throws Exception {
					Profile prof = m.getProfile();
					ComplementClass A = m.createComplementClass( NS + "A", null );
					OntClass B = m.createClass( NS + "B" );
					OntClass C = m.createClass( NS + "C" );
					boolean ex = false;
                    
					try { A.addOperand( B ); } catch (UnsupportedOperationException e) {ex = true;}
					assertTrue( "Should fail to add to a complement", ex );
                    
					ex = false;
					try { A.addOperands( new ArrayList().iterator() ); } catch (UnsupportedOperationException e) {ex = true;}
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
				}
			},
            
            // from file
            new OntTestCase( "OntClass.subclass.fromFile", true, true, true ) {
                public void ontTest( OntModel m ) throws Exception {
                    String fileName = m_owlLang ? "file:testing/ontology/owl/ClassExpression/test.rdf" : "file:testing/ontology/daml/ClassExpression/test.rdf";
                    m.read( fileName );

                    OntClass A = m.createClass( NS + "ClassA" );
                    OntClass B = m.createClass( NS + "ClassB" );
                    
                    iteratorTest( A.listSuperClasses(), new Object[] {B} );
                    iteratorTest( B.listSubClasses(), new Object[] {A} );
                }
            },
            new OntTestCase( "OntClass.equivalentClass.fromFile", true, true, true ) {
                public void ontTest( OntModel m ) throws Exception {
                    String fileName = m_owlLang ? "file:testing/ontology/owl/ClassExpression/test.rdf" : "file:testing/ontology/daml/ClassExpression/test.rdf";
                    m.read( fileName );

                    OntClass A = m.createClass( NS + "ClassA" );
                    OntClass C = m.createClass( NS + "ClassC" );
                    
                    assertTrue( "A should be equiv to C", A.hasEquivalentClass( C ) );
                }
            },
            new OntTestCase( "OntClass.disjoint.fromFile", true, false, true ) {
                public void ontTest( OntModel m ) throws Exception {
                    String fileName = m_owlLang ? "file:testing/ontology/owl/ClassExpression/test.rdf" : "file:testing/ontology/daml/ClassExpression/test.rdf";
                    m.read( fileName );

                    OntClass A = m.createClass( NS + "ClassA" );
                    OntClass D = m.createClass( NS + "ClassD" );
                    
                    assertTrue( "A should be disjoint with D", A.isDisjointWith( D ) );
                }
            },
        };
    }
    
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

