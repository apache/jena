/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            26-Mar-2003
 * Filename           $RCSfile: TestProperty.java,v $
 * Revision           $Revision: 1.9 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-06-22 19:20:43 $
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
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.vocabulary.RDF;



/**
 * <p>
 * Unit test cases for the Ontology class
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: TestProperty.java,v 1.9 2003-06-22 19:20:43 ian_dickinson Exp $
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

    public OntTestCase[] getTests() {
        return new OntTestCase[] {
            new OntTestCase( "OntProperty.super-property", true, true, true, true ) {
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
                }
            },
            new OntTestCase( "OntProperty.sub-property", true, true, true, true ) {
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
            new OntTestCase( "OntProperty.domain", true, true, true, true ) {
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createOntProperty( NS + "p" );
                    OntResource a = (OntResource) m.getResource( NS + "a" ).as( OntResource.class );
                    OntResource b = (OntResource) m.getResource( NS + "b" ).as( OntResource.class );
                    
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
            new OntTestCase( "OntProperty.range", true, true, true, true ) {
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createOntProperty( NS + "p" );
                    OntResource a = (OntResource) m.getResource( NS + "a" ).as( OntResource.class );
                    OntResource b = (OntResource) m.getResource( NS + "b" ).as( OntResource.class );
                    
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
            new OntTestCase( "OntProperty.equivalentProperty", true, true, true, false ) {
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
            new OntTestCase( "OntProperty.inverseOf", true, true, true, false ) {
                public void ontTest( OntModel m ) throws Exception {
                    Profile prof = m.getProfile();
                    OntProperty p = m.createObjectProperty( NS + "p" );
                    OntProperty q = m.createObjectProperty( NS + "q" );
                    OntProperty r = m.createObjectProperty( NS + "r" );
                    
                    p.addInverseOf( q );
                    assertEquals( "Cardinality should be 1", 1, p.getCardinality( prof.INVERSE_OF() ) );
                    assertEquals( "p should have inverse q", q, p.getInverseOf() );
                    
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
            new OntTestCase( "OntProperty.subproperty.fromFile", true, true, true, true ) {
                public void ontTest( OntModel m ) throws Exception {
                    String lang = m_owlLang ? "owl" : (m_damlLang ? "daml" : "rdfs");
                    String fileName = "file:testing/ontology/" + lang + "/Property/test.rdf";
                    m.read( fileName );

                    OntProperty p = (OntProperty) m.getProperty( NS, "p" ).as( OntProperty.class );
                    OntProperty q = (OntProperty) m.getProperty( NS, "q" ).as( OntProperty.class );
                    
                    iteratorTest( p.listSuperProperties(), new Object[] {q} );
                    iteratorTest( q.listSubProperties(), new Object[] {p} );
                }
            },
            new OntTestCase( "OntProperty.domain.fromFile", true, true, true, true ) {
                public void ontTest( OntModel m ) throws Exception {
                    String lang = m_owlLang ? "owl" : (m_damlLang ? "daml" : "rdfs");
                    String fileName = "file:testing/ontology/" + lang + "/Property/test.rdf";
                    m.read( fileName );

                    OntProperty p = (OntProperty) m.getProperty( NS, "p" ).as( OntProperty.class );
                    OntClass A = (OntClass) m.getResource( NS + "ClassA").as( OntClass.class);
                    
                    assertTrue( "p should have domain A", p.hasDomain( A ) );
                }
            },
            new OntTestCase( "OntProperty.range.fromFile", true, true, true, true ) {
                public void ontTest( OntModel m ) throws Exception {
                    String lang = m_owlLang ? "owl" : (m_damlLang ? "daml" : "rdfs");
                    String fileName = "file:testing/ontology/" + lang + "/Property/test.rdf";
                    m.read( fileName );

                    OntProperty p = (OntProperty) m.getProperty( NS, "p" ).as( OntProperty.class );
                    OntClass B = (OntClass) m.getResource( NS + "ClassB").as( OntClass.class);
                    
                    assertTrue( "p should have domain B", p.hasRange( B ) );
                }
            },
            new OntTestCase( "OntProperty.equivalentProeprty.fromFile", true, true, true, false ) {
                public void ontTest( OntModel m ) throws Exception {
                    String lang = m_owlLang ? "owl" : (m_damlLang ? "daml" : "rdfs");
                    String fileName = "file:testing/ontology/" + lang + "/Property/test.rdf";
                    m.read( fileName );

                    OntProperty p = (OntProperty) m.getProperty( NS, "p" ).as( OntProperty.class );
                    OntProperty r = (OntProperty) m.getProperty( NS, "r" ).as( OntProperty.class );
                    
                    assertTrue( "p should have equiv prop r", p.hasEquivalentProperty( r ) );
                }
            },
            new OntTestCase( "OntProperty.inversePropertyOf.fromFile", true, true, true, false ) {
                public void ontTest( OntModel m ) throws Exception {
                    String lang = m_owlLang ? "owl" : (m_damlLang ? "daml" : "rdfs");
                    String fileName = "file:testing/ontology/" + lang + "/Property/test.rdf";
                    m.read( fileName );

                    OntProperty p = (OntProperty) m.getProperty( NS, "p" ).as( OntProperty.class );
                    OntProperty s = (OntProperty) m.getProperty( NS, "s" ).as( OntProperty.class );
                    
                    assertTrue( "p should have inv prop s", p.isInverseOf( s ) );
                }
            },
            
            // type tests
            new OntTestCase( "OntProperty.isFunctionalProperty dt", true, true, true, false ) {
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
            new OntTestCase( "OntProperty.isFunctionalProperty object", true, true, true, false ) {
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
            new OntTestCase( "OntProperty.isDatatypeProperty", true, true, true, false ) {
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
            new OntTestCase( "OntProperty.isObjectProperty", true, true, true, false ) {
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
            new OntTestCase( "OntProperty.isTransitiveProperty", true, true, true, false ) {
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
            new OntTestCase( "OntProperty.isInverseFunctionalProperty", true, true, true, false ) {
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
            new OntTestCase( "OntProperty.isSymmetricProperty", true, true, false, false ) {
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
            new OntTestCase( "OntProperty.convertToFunctionalProperty", true, true, true, false ) {
                public void ontTest( OntModel m ) throws Exception {
                    Property pSimple = m.createProperty( NS, "p" );
                    pSimple.addProperty( RDF.type, RDF.Property );
                    OntProperty p = (OntProperty) pSimple.as( OntProperty.class );
                
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
            new OntTestCase( "OntProperty.convertToDatatypeProperty", true, true, true, false ) {
                public void ontTest( OntModel m ) throws Exception {
                    Property pSimple = m.createProperty( NS, "p" );
                    pSimple.addProperty( RDF.type, RDF.Property );
                    OntProperty p = (OntProperty) pSimple.as( OntProperty.class );
                
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
            new OntTestCase( "OntProperty.convertToObjectProperty", true, true, true, false ) {
                public void ontTest( OntModel m ) throws Exception {
                    Property pSimple = m.createProperty( NS, "p" );
                    pSimple.addProperty( RDF.type, RDF.Property );
                    OntProperty p = (OntProperty) pSimple.as( OntProperty.class );
                
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
            new OntTestCase( "OntProperty.convertToTransitiveProperty", true, true, true, false ) {
                public void ontTest( OntModel m ) throws Exception {
                    Property pSimple = m.createProperty( NS, "p" );
                    pSimple.addProperty( RDF.type, RDF.Property );
                    OntProperty p = (OntProperty) pSimple.as( OntProperty.class );
                
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
            new OntTestCase( "OntProperty.convertToInverseFunctionalProperty", true, true, true, false ) {
                public void ontTest( OntModel m ) throws Exception {
                    Property pSimple = m.createProperty( NS, "p" );
                    pSimple.addProperty( RDF.type, RDF.Property );
                    OntProperty p = (OntProperty) pSimple.as( OntProperty.class );
                
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
            new OntTestCase( "OntProperty.convertToSymmetricProperty", true, true, false, false ) {
                public void ontTest( OntModel m ) throws Exception {
                    Property pSimple = m.createProperty( NS, "p" );
                    pSimple.addProperty( RDF.type, RDF.Property );
                    OntProperty p = (OntProperty) pSimple.as( OntProperty.class );
                
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
            new OntTestCase( "OntProperty.inverse", true, true, true, false ) {
                public void ontTest( OntModel m ) throws Exception {
                    ObjectProperty p = m.createObjectProperty( NS + "p" );
                    ObjectProperty q = m.createObjectProperty( NS + "q" );
                    ObjectProperty r = m.createObjectProperty( NS + "r" );
                    
                    assertFalse( "No inverse of p", p.hasInverse() );
                    
                    q.addInverseOf( p );
                    assertTrue( "Inverse of p", p.hasInverse() );
                    assertEquals( "inverse of p ", q, p.getInverse() );
                    
                    r.addInverseOf( p );
                    iteratorTest( p.listInverse(), new Object[] {q,r} );
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


