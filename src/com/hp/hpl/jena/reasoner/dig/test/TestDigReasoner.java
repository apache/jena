/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            11-Sep-2003
 * Filename           $RCSfile: TestDigReasoner.java,v $
 * Revision           $Revision: 1.14 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2004-05-18 14:50:40 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2001, 2002, 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.reasoner.dig.test;



// Imports
///////////////
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.*;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.reasoner.dig.*;
import com.hp.hpl.jena.reasoner.test.TestUtil;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

import junit.framework.*;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.parsers.DocumentBuilder;


/**
 * <p>
 * Unit tests for DIG reasoners 
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version Release @release@ ($Id: TestDigReasoner.java,v 1.14 2004-05-18 14:50:40 ian_dickinson Exp $)
 */
public class TestDigReasoner 
    extends TestCase
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    protected Model m_base;
    
    // Constructors
    //////////////////////////////////

    public TestDigReasoner( String name ) {
        super( name );
    }
    
    
    
    // External signature methods
    //////////////////////////////////
    
    public static TestSuite suite() {
        TestSuite s = new TestSuite( "TestDigReasoner" );
        
        buildConceptLangSuite( "testing/ontology/dig/owl/cl", OntModelSpec.OWL_MEM, s );
        buildBasicQuerySuite( "testing/ontology/dig/owl/basicq", OntModelSpec.OWL_MEM, s );

        // add the standard tests from this class
        s.addTestSuite( TestDigReasoner.class );
        return s;
    }

    
    public void setUp() {
        // ensure the ont doc manager is in a consistent state
        OntDocumentManager.getInstance().reset( true );
    }
    
    public void testAxioms() {
        String NS = "http://example.org/foo#";
        
        DIGReasoner r = (DIGReasoner) ReasonerRegistry.getDIGReasoner();
        DIGReasoner ro = (DIGReasoner) ReasonerRegistry.getDIGReasoner( OWL.NAMESPACE, null );
        DIGReasoner rd = (DIGReasoner) ReasonerRegistry.getDIGReasoner( DAML_OIL.NAMESPACE_DAML, null );
        DIGReasoner roA = (DIGReasoner) ReasonerRegistry.getDIGReasoner( OWL.NAMESPACE, true, null );
        DIGReasoner rdA = (DIGReasoner) ReasonerRegistry.getDIGReasoner( DAML_OIL.NAMESPACE_DAML, true, null );
        
        axiomTestAux( r, OntModelSpec.OWL_MEM, false, false );
        //axiomTestAux( r, OntModelSpec.DAML_MEM, false, false );

        axiomTestAux( ro, OntModelSpec.OWL_MEM, false, false );
        axiomTestAux( rd, OntModelSpec.DAML_MEM, false, false );
        
        axiomTestAux( roA, OntModelSpec.OWL_MEM, true, false );
        axiomTestAux( rdA, OntModelSpec.DAML_MEM, false, true );
    }
    
    private void axiomTestAux( DIGReasoner dr, OntModelSpec baseSpec, boolean owlResult, boolean damlResult ) {
        OntModelSpec spec = new OntModelSpec( baseSpec );
        spec.setReasoner( dr );
        OntModel m = ModelFactory.createOntologyModel( spec, null );
        
        assertEquals( "Result for owl:ObjectProperty", owlResult, m.contains( OWL.ObjectProperty, RDF.type, RDFS.Class ));
        assertEquals( "Result for daml:ObjectProperty", damlResult, m.contains( DAML_OIL.ObjectProperty, RDF.type, RDFS.Class ));
    }
    
    public void testQueryAllConcepts() {
        String NS = "http://example.org/foo#";
        
        DIGReasoner r = (DIGReasoner) ReasonerRegistry.theRegistry().create( DIGReasonerFactory.URI, null );
        
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
        spec.setReasoner( r );
        OntModel m = ModelFactory.createOntologyModel( spec, null );
        m.read( "file:testing/ontology/dig/owl/test1.xml" );
        
        TestUtil.assertIteratorValues( this, m.listClasses(), 
                                       new Resource[] {
                                           m.getResource( NS + "A" ), m.getResource( NS + "B" ), 
                                           m.getResource( NS + "C"),  m.getResource( NS + "D"), 
                                           m.getResource( NS + "E"),  m.getResource( NS + "BB"), 
                                           m.getResource( NS + "F0"), m.getResource( NS + "F1"), m.getResource( NS + "F2"), 
                                       }, 2 );
    }
    
    
    public void testQuerySubsumes1() {
        String NS = "http://example.org/foo#";
        
        DIGReasoner r = (DIGReasoner) ReasonerRegistry.theRegistry().create( DIGReasonerFactory.URI, null );
        
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
        spec.setReasoner( r );
        OntModel m = ModelFactory.createOntologyModel( spec, null );
        m.read( "file:testing/ontology/dig/owl/test1.xml" );
        
        OntClass A = m.getOntClass( NS + "A" );
        OntClass B = m.getOntClass( NS + "B" );
        assertTrue( "A should be a sub-class of B", A.hasSuperClass( B ) );
    }
    
    public void testQuerySubsumes2() {
        String NS = "http://example.org/foo#";
        
        DIGReasoner r = (DIGReasoner) ReasonerRegistry.theRegistry().create( DIGReasonerFactory.URI, null );
        
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
        spec.setReasoner( r );
        OntModel m = ModelFactory.createOntologyModel( spec, null );
        m.read( "file:testing/ontology/dig/owl/test1.xml" );
        
        OntClass C = m.getOntClass( NS + "C" );
        OntClass D = m.getOntClass( NS + "D" );
        assertTrue( "D should be a sub-class of C", D.hasSuperClass( C ) );
    }
    
    public void testQuerySubsumes3() {
        String NS = "http://example.org/foo#";
        
        DIGReasoner r = (DIGReasoner) ReasonerRegistry.theRegistry().create( DIGReasonerFactory.URI, null );
        
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
        spec.setReasoner( r );
        OntModel m = ModelFactory.createOntologyModel( spec, null );
        m.read( "file:testing/ontology/dig/owl/test1.xml" );
        
        OntClass A = m.getOntClass( NS + "A" );
        OntClass C = m.getOntClass( NS + "C" );
        assertFalse( "A should not be a super-class of C", C.hasSuperClass( A ) );
        assertFalse( "C should not be a super-class of A", A.hasSuperClass( C ) );
    }
    
    public void testAncestors() {
        String NS = "http://example.org/foo#";
        
        DIGReasoner r = (DIGReasoner) ReasonerRegistry.theRegistry().create( DIGReasonerFactory.URI, null );
        
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
        spec.setReasoner( r );
        OntModel m = ModelFactory.createOntologyModel( spec, null );
        m.read( "file:testing/ontology/dig/owl/test1.xml" );
        
        OntClass A = m.getOntClass( NS + "A" );
        OntClass B = m.getOntClass( NS + "B" );
        OntClass BB = m.getOntClass( NS + "BB" );
        
        TestUtil.assertIteratorValues( this, A.listSuperClasses(), 
                                       new Resource[] {B,BB} );
    }

    public void testDescendants() {
        String NS = "http://example.org/foo#";
        
        DIGReasoner r = (DIGReasoner) ReasonerRegistry.theRegistry().create( DIGReasonerFactory.URI, null );
        
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
        spec.setReasoner( r );
        OntModel m = ModelFactory.createOntologyModel( spec, null );
        m.read( "file:testing/ontology/dig/owl/test1.xml" );
        
        OntClass A = m.getOntClass( NS + "A" );
        OntClass B = m.getOntClass( NS + "B" );
        OntClass BB = m.getOntClass( NS + "BB" );
        
        TestUtil.assertIteratorValues( this, BB.listSubClasses(), 
                                       new Resource[] {B,A} );
    }

    public void testAllClassHierarchy() {
        String NS = "http://example.org/foo#";
        
        DIGReasoner r = (DIGReasoner) ReasonerRegistry.theRegistry().create( DIGReasonerFactory.URI, null );
        
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
        spec.setReasoner( r );
        OntModel m = ModelFactory.createOntologyModel( spec, null );
        m.read( "file:testing/ontology/dig/owl/test1.xml" );
        
        OntClass A = m.getOntClass( NS + "A" );
        OntClass B = m.getOntClass( NS + "B" );
        OntClass BB = m.getOntClass( NS + "BB" );
        OntClass C = m.getOntClass( NS + "C" );
        OntClass D = m.getOntClass( NS + "D" );
        OntClass E = m.getOntClass( NS + "E" );
        OntClass F0 = m.getOntClass( NS + "F0" );
        OntClass F1 = m.getOntClass( NS + "F1" );
        OntClass F2 = m.getOntClass( NS + "F2" );
        
        TestUtil.assertIteratorValues( this, m.listStatements( null, RDFS.subClassOf, (RDFNode) null ), 
                                       new Statement[] {
                                           m.createStatement( A, RDFS.subClassOf, A ),
                                           m.createStatement( A, RDFS.subClassOf, B ),
                                           m.createStatement( A, RDFS.subClassOf, BB ),
                                           m.createStatement( B, RDFS.subClassOf, B ),
                                           m.createStatement( B, RDFS.subClassOf, BB ),
                                           m.createStatement( BB, RDFS.subClassOf, BB ),
                                           m.createStatement( C, RDFS.subClassOf, C ),
                                           m.createStatement( D, RDFS.subClassOf, D ),
                                           m.createStatement( D, RDFS.subClassOf, C ),
                                           m.createStatement( E, RDFS.subClassOf, C ),
                                           m.createStatement( E, RDFS.subClassOf, E ),
                                           m.createStatement( F0, RDFS.subClassOf, F0 ),
                                           m.createStatement( F1, RDFS.subClassOf, F1 ),
                                           m.createStatement( F2, RDFS.subClassOf, F2 ),
                                       }, 2 );
    }

    public void testQueryDisjoint1() {
        String NS = "http://example.org/foo#";
        
        DIGReasoner r = (DIGReasoner) ReasonerRegistry.theRegistry().create( DIGReasonerFactory.URI, null );
        
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
        spec.setReasoner( r );
        OntModel m = ModelFactory.createOntologyModel( spec, null );
        m.read( "file:testing/ontology/dig/owl/test1.xml" );
        
        OntClass A = m.getOntClass( NS + "A" );
        OntClass C = m.getOntClass( NS + "C" );
        
        assertTrue( "A should be disjoint with C", A.isDisjointWith( C ) );
        assertTrue( "C should be disjoint with A", C.isDisjointWith( A ) );
    }
    

    public void testQueryDisjoint2() {
        String NS = "http://example.org/foo#";
        
        DIGReasoner r = (DIGReasoner) ReasonerRegistry.theRegistry().create( DIGReasonerFactory.URI, null );
        
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
        spec.setReasoner( r );
        OntModel m = ModelFactory.createOntologyModel( spec, null );
        m.read( "file:testing/ontology/dig/owl/test1.xml" );
        
        OntClass F0 = m.getOntClass( NS + "F0" );
        OntClass F1 = m.getOntClass( NS + "F1" );
        
        assertTrue( "F0 should be disjoint with F1", F0.isDisjointWith( F1 ) );
        assertTrue( "F1 should be disjoint with F0", F1.isDisjointWith( F0 ) );
    }
    
    public void testParents() {
        String NS = "http://example.org/foo#";
        
        DIGReasoner r = (DIGReasoner) ReasonerRegistry.theRegistry().create( DIGReasonerFactory.URI, null );
        
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
        spec.setReasoner( r );
        OntModel m = ModelFactory.createOntologyModel( spec, null );
        m.read( "file:testing/ontology/dig/owl/test1.xml" );
        
        OntClass A = m.getOntClass( NS + "A" );
        OntClass B = m.getOntClass( NS + "B" );
        
        // note - direct super class
        TestUtil.assertIteratorValues( this, A.listSuperClasses( true ), 
                                       new Resource[] {B} );
    }

    public void testChildren() {
        String NS = "http://example.org/foo#";
        
        DIGReasoner r = (DIGReasoner) ReasonerRegistry.theRegistry().create( DIGReasonerFactory.URI, null );
        
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
        spec.setReasoner( r );
        OntModel m = ModelFactory.createOntologyModel( spec, null );
        m.read( "file:testing/ontology/dig/owl/test1.xml" );
        
        OntClass B = m.getOntClass( NS + "B" );
        OntClass BB = m.getOntClass( NS + "BB" );
        
        // note direct sub-class
        TestUtil.assertIteratorValues( this, BB.listSubClasses(true), 
                                       new Resource[] {B} );
    }

    public void testEquivalents() {
        String NS = "http://example.org/foo#";
        
        DIGReasoner r = (DIGReasoner) ReasonerRegistry.theRegistry().create( DIGReasonerFactory.URI, null );
        
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
        spec.setReasoner( r );
        OntModel m = ModelFactory.createOntologyModel( spec, null );
        m.read( "file:testing/ontology/dig/owl/test1.xml" );
        
        OntClass F0 = m.getOntClass( NS + "F0" );
        OntClass F2 = m.getOntClass( NS + "F2" );
        OntClass F1 = m.getOntClass( NS + "F1" );
        
        TestUtil.assertIteratorValues( this, F0.listEquivalentClasses(), 
                                       new Resource[] {F2, F0}, 1 );
        TestUtil.assertIteratorValues( this, F2.listEquivalentClasses(), 
                                       new Resource[] {F0, F2}, 1 );
        TestUtil.assertIteratorValues( this, F1.listEquivalentClasses(), 
                                       new Resource[] {F1}, 1 );
    }


    public void testIsEquivalent() {
        String NS = "http://example.org/foo#";
        
        DIGReasoner r = (DIGReasoner) ReasonerRegistry.theRegistry().create( DIGReasonerFactory.URI, null );
        
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
        spec.setReasoner( r );
        OntModel m = ModelFactory.createOntologyModel( spec, null );
        m.read( "file:testing/ontology/dig/owl/test1.xml" );
        
        OntClass F0 = m.getOntClass( NS + "F0" );
        OntClass F2 = m.getOntClass( NS + "F2" );
        OntClass F1 = m.getOntClass( NS + "F1" );
        
        assertTrue( "F0 should be equivalent to F2", F0.hasEquivalentClass( F2 ));
        assertFalse( "F0 should not be equivalent to F1", F1.hasEquivalentClass( F0 ));
    }

    
    public void testRAncestors() {
        String NS = "http://example.org/foo#";
        
        DIGReasoner r = (DIGReasoner) ReasonerRegistry.theRegistry().create( DIGReasonerFactory.URI, null );
        
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
        spec.setReasoner( r );
        OntModel m = ModelFactory.createOntologyModel( spec, null );
        m.read( "file:testing/ontology/dig/owl/test1.xml" );
        
        ObjectProperty p0 = m.getObjectProperty( NS + "p0" );
        ObjectProperty p1 = m.getObjectProperty( NS + "p1" );
        ObjectProperty p2 = m.getObjectProperty( NS + "p2" );
        
        TestUtil.assertIteratorValues( this, p0.listSuperProperties(), 
                                       new Resource[] {p1, p2, p0} );
    }

    public void testRDescendants() {
        String NS = "http://example.org/foo#";
        
        DIGReasoner r = (DIGReasoner) ReasonerRegistry.theRegistry().create( DIGReasonerFactory.URI, null );
        
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
        spec.setReasoner( r );
        OntModel m = ModelFactory.createOntologyModel( spec, null );
        m.read( "file:testing/ontology/dig/owl/test1.xml" );
        
        ObjectProperty p0 = m.getObjectProperty( NS + "p0" );
        ObjectProperty p1 = m.getObjectProperty( NS + "p1" );
        ObjectProperty p2 = m.getObjectProperty( NS + "p2" );
        
        TestUtil.assertIteratorValues( this, p2.listSubProperties(), 
                                       new Resource[] {p1, p0, p2} );
    }

    
    public void testRParents() {
        String NS = "http://example.org/foo#";
        
        DIGReasoner r = (DIGReasoner) ReasonerRegistry.theRegistry().create( DIGReasonerFactory.URI, null );
        
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
        spec.setReasoner( r );
        OntModel m = ModelFactory.createOntologyModel( spec, null );
        m.read( "file:testing/ontology/dig/owl/test1.xml" );
        
        ObjectProperty p0 = m.getObjectProperty( NS + "p0" );
        ObjectProperty p1 = m.getObjectProperty( NS + "p1" );
        //ObjectProperty p2 = m.getObjectProperty( NS + "p2" );
        
        TestUtil.assertIteratorValues( this, p0.listSuperProperties(true), 
                                       new Resource[] {p1} );
    }

    public void testRChildren() {
        String NS = "http://example.org/foo#";
        
        DIGReasoner r = (DIGReasoner) ReasonerRegistry.theRegistry().create( DIGReasonerFactory.URI, null );
        
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
        spec.setReasoner( r );
        OntModel m = ModelFactory.createOntologyModel( spec, null );
        m.read( "file:testing/ontology/dig/owl/test1.xml" );
        
        //ObjectProperty p0 = m.getObjectProperty( NS + "p0" );
        ObjectProperty p1 = m.getObjectProperty( NS + "p1" );
        ObjectProperty p2 = m.getObjectProperty( NS + "p2" );
        
        TestUtil.assertIteratorValues( this, p2.listSubProperties(true), 
                                       new Resource[] {p1} );
    }

    public void testInstances() {
        String NS = "http://example.org/foo#";
        
        DIGReasoner r = (DIGReasoner) ReasonerRegistry.theRegistry().create( DIGReasonerFactory.URI, null );
        
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
        spec.setReasoner( r );
        OntModel m = ModelFactory.createOntologyModel( spec, null );
        m.read( "file:testing/ontology/dig/owl/test1.xml" );
        
        OntClass F0 = m.getOntClass( NS + "F0" );
        Resource i0 = m.getResource( NS + "i0" );
        Resource i1 = m.getResource( NS + "i1" );
        Resource i2 = m.getResource( NS + "i2" );
        Resource q0 = m.getResource( NS + "q0" );
        Resource q1 = m.getResource( NS + "q1" );
        Resource q2 = m.getResource( NS + "q2" );

        TestUtil.assertIteratorValues( this, F0.listInstances(), 
                                       new Resource[] {i0, i1, i2, q0, q2, q1} );
    }

    public void testTypes() {
        String NS = "http://example.org/foo#";
        
        DIGReasoner r = (DIGReasoner) ReasonerRegistry.theRegistry().create( DIGReasonerFactory.URI, null );
        
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
        spec.setReasoner( r );
        OntModel m = ModelFactory.createOntologyModel( spec, null );
        m.read( "file:testing/ontology/dig/owl/test1.xml" );
        
        OntClass F0 = m.getOntClass( NS + "F0" );
        OntClass F2 = m.getOntClass( NS + "F2" );
        Individual i0 = m.getIndividual( NS + "i0" );

        TestUtil.assertIteratorValues( this, i0.listRDFTypes(false), 
                                       new Resource[] {F0, F2}, 1 );
    }

    public void testInstance() {
        String NS = "http://example.org/foo#";
        
        DIGReasoner r = (DIGReasoner) ReasonerRegistry.theRegistry().create( DIGReasonerFactory.URI, null );
        
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
        spec.setReasoner( r );
        OntModel m = ModelFactory.createOntologyModel( spec, null );
        m.read( "file:testing/ontology/dig/owl/test1.xml" );
        
        OntClass F0 = m.getOntClass( NS + "F0" );
        OntClass F1 = m.getOntClass( NS + "F1" );
        Individual i0 = m.getIndividual( NS + "i0" );

        assertTrue( "i0 should be an instance of F0", i0.hasRDFType( F0 ));
        assertFalse( "i0 should not be an instance of F1", i0.hasRDFType( F1 ));
    }

    public void testRoleFillers() {
        String NS = "http://example.org/foo#";
        
        DIGReasoner r = (DIGReasoner) ReasonerRegistry.theRegistry().create( DIGReasonerFactory.URI, null );
        
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
        spec.setReasoner( r );
        OntModel m = ModelFactory.createOntologyModel( spec, null );
        m.read( "file:testing/ontology/dig/owl/test1.xml" );
        
        Individual q0 = m.getIndividual( NS + "q0" );
        Individual q1 = m.getIndividual( NS + "q1" );
        Individual q2 = m.getIndividual( NS + "q2" );
        Property q = m.getProperty( NS + "q" );
        
        TestUtil.assertIteratorValues( this, q0.listPropertyValues( q ), 
                                       new Resource[] {q1, q2}, 0 );
    }

    public void xxtestDebug1() {
        String NS = "http://example.org/foo#";
        
        OntModel base = ModelFactory.createOntologyModel( OntModelSpec.OWL_DL_MEM, null );
        Individual a = base.createIndividual( NS + "a", OWL.Thing );
        Individual b = base.createIndividual( NS + "b", OWL.Thing );
        OntClass A = base.createEnumeratedClass( NS + "A", base.createList( new Resource[] {a,b} ));
        
        DIGReasoner r = (DIGReasoner) ReasonerRegistry.theRegistry().create( DIGReasonerFactory.URI, null );
        
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
        spec.setReasoner( r );
        OntModel m = ModelFactory.createOntologyModel( spec, base );
        
        for (Iterator i = m.listClasses();  i.hasNext(); ) {
            System.err.println( "concept " + i.next() );
        }
    }

    public void xxtestDebug() {
        String NS = "http://example.org/foo#";
        
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_DL_MEM_RULE_INF );
        OntModel m = ModelFactory.createOntologyModel( spec, null );
        m.read( "file:testing/ontology/dig/owl/test1.xml" );
        
        ObjectProperty p2 = m.getObjectProperty( NS + "p2" );
        
        for (StmtIterator i = m.listStatements( null, RDFS.subPropertyOf, p2 );  i.hasNext(); ) {
            System.err.println( "p2 has sub prop " + i.next() );
        }
    }


    // Internal implementation methods
    //////////////////////////////////

    private static void buildConceptLangSuite( String root, OntModelSpec spec, TestSuite s ) {
        int i = 0;
        while (true) {
            File testSource = new File( root + "/test_" + i + ".source.xml" );
            File testTarget = new File( root + "/test_" + i + ".xml" );
            
            if (!testSource.exists()) {
                break;
            }
            else {
                i++;
            }
            
            //s.addTest( new DigTranslationTest( testSource, testTarget, spec ) );
        }
    }
    
    private static void buildBasicQuerySuite( String root, OntModelSpec spec, TestSuite s ) {
        int i = 0;
        while (true) {
            File testSource = new File( root + "/test_" + i + ".source.xml" );
            File testQuery = new File( root + "/test_" + i + ".query.xml" );
            File testTarget = new File( root + "/test_" + i + ".result.xml" );
            
            if (!testSource.exists()) {
                break;
            }
            else {
                i++;
            }
            
            s.addTest( new DigBasicQueryTest( testSource, testTarget, testQuery, spec ) );
        }
    }
    
    
    
    //==============================================================================
    // Inner class definitions
    //==============================================================================

    private static class AbstractDigTest
        extends TestCase
    {
        private boolean debug = true;
        
        public AbstractDigTest( String name ) {
            super( name );
        }
        
        /** This is a simple test that test xml structure isomorphism on elements and attributes */
        protected void xmlEqualityTest( Document source, Document target ) {
            // test both ways round to ensure compatability
            boolean test = xmlEqualityTest( source.getDocumentElement(), target.getDocumentElement() );
            if (debug && !test) {
                PrintWriter out = new PrintWriter( System.err );
                out.println( getName() +  " expected:" );
                new DIGConnection().serialiseDocument( target, out );
                out.println();
                out.println( "Saw:" );
                new DIGConnection().serialiseDocument( source, out );
                out.println();
            }
            assertTrue( "Failed to match source to target documents", test );

            test = xmlEqualityTest( target.getDocumentElement(), source.getDocumentElement() );
            if (debug && !test) {
                PrintWriter out = new PrintWriter( System.err );
                out.println( getName() +  " expected:" );
                new DIGConnection().serialiseDocument( source, out );
                out.println();
                out.println( "Saw:" );
                new DIGConnection().serialiseDocument( target, out );
                out.println();
            }
            assertTrue( "Failed to match target to source documents", test );
        }
    
        private boolean xmlEqualityTest( Element source, Element target ) {
            boolean match = source.getNodeName().equals( target.getNodeName() );
            NodeList children = source.getChildNodes();
            
            for (int i = 0;  match && i < children.getLength(); i++) {
                Node child = children.item( i );
                // we're only looking at structural equivalence - elements and attributes
                if (child instanceof Element) {
                    match = findElementMatch( (Element) child, target );
                }
            }
            
            NamedNodeMap attrs = source.getAttributes();
            
            for (int i = 0;  match && i < attrs.getLength(); i++) {
                match = findAttributeMatch( (Attr) attrs.item( i ), target );
            }
            
            return match;
        }
    
    
        private boolean findElementMatch( Element sourceChild, Element target ) {
            boolean found = false;

            NodeList targetChildren = target.getElementsByTagName( sourceChild.getNodeName() );
        
            for (int i = 0;  !found && i < targetChildren.getLength();  i++) {
                Node targetChild = targetChildren.item( i );
                
                if (targetChild instanceof Element && sourceChild.getNodeName().equals( targetChild.getNodeName() )) {
                    // we have found an element with the same name - see if it matches
                    found = xmlEqualityTest( sourceChild, (Element) targetChild );
                }
            }
        
            return found;
        }
    
    
        private boolean findAttributeMatch( Attr child, Element target ) {
            String chValue = child.getValue();
            String targetValue = target.getAttribute( child.getName() );
            
            return (chValue.startsWith( DIGAdapter.ANON_MARKER ) && targetValue.startsWith( DIGAdapter.ANON_MARKER)) || 
                   chValue.equals( targetValue );
        }
    }
    
    
    private static class DigTranslationTest
        extends AbstractDigTest
    {
        private File m_source;
        private File m_target;
        private OntModelSpec m_spec;
        
        DigTranslationTest( File source, File target, OntModelSpec spec ) {
            super( "DigTranslationTest " + source.getName() );
            m_source = source;
            m_target = target;
            m_spec = spec;
        }
        
        public void runTest()
            throws Exception 
        {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            
            Model m = ModelFactory.createDefaultModel();
            m.read( new FileInputStream( m_source ), null );
            DIGAdapter da = new DIGAdapter( m_spec, m.getGraph() );
            
            Document targetD = builder.parse( m_target );
            Document sourceD = da.translateKbToDig();
            
            // debug da.serialiseDocument( sourceD, new PrintWriter( System.out ));
            
            xmlEqualityTest( sourceD, targetD );
        }
    }
    
    
    private static class DigBasicQueryTest
        extends AbstractDigTest
    {
        private File m_source;
        private File m_target;
        private File m_query;
        private OntModelSpec m_spec;
        
        DigBasicQueryTest( File source, File target, File query, OntModelSpec spec ) {
            super( "BasicQueryTest " + source.getName() );
            m_source = source;
            m_target = target;
            m_query = query;
            m_spec = spec;
        }
        
        public void runTest()
            throws Exception 
        {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            
            Model m = ModelFactory.createDefaultModel();
            m.read( new FileInputStream( m_source ), null );
            DIGAdapter da = new DIGAdapter( m_spec, m.getGraph() );
            
            // upload 
            da.resetKB();
            boolean warn  = !da.uploadKB();
            if (warn) {
                System.err.println( "00 Warning!" );
                for (Iterator i = da.getConnection().getWarnings(); i.hasNext(); ) {
                    System.err.println( i.next() );
                }
                assertFalse( "Should not be upload warnings", warn );
            }
                        
            Document queryD = builder.parse( m_query );
            Document targetD = builder.parse( m_target );

            LogFactory.getLog( getClass() ).debug( "DIG test " + m_source.getPath() );
            Document resultD = da.getConnection().sendDigVerb( queryD, da.getProfile() );
            
            da.getConnection().errorCheck( resultD );
            assertFalse( "Should not be warnings", da.getConnection().warningCheck( resultD ) );
            
            da.close();
            xmlEqualityTest( resultD, targetD );
        }
    }
}


/*
 *  (c) Copyright 2001, 2002, 2003 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
