/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            21-Jun-2003
 * Filename           $RCSfile: TestOntModel.java,v $
 * Revision           $Revision: 1.14 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2004-12-06 13:50:27 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2002, 2003, 2004 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl.test;


// Imports
///////////////
import java.io.*;
import java.util.*;
import java.util.List;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.impl.*;
import com.hp.hpl.jena.ontology.impl.OWLDLProfile;
import com.hp.hpl.jena.ontology.impl.OWLProfile;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;



/**
 * <p>
 * Unit tests on OntModel capabilities.  Many of OntModel's methods are tested by the other
 * abstractions' unit tests.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: TestOntModel.java,v 1.14 2004-12-06 13:50:27 andy_seaborne Exp $
 */
public class TestOntModel 
    extends ModelTestBase
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    public static final String BASE = "http://www.hp.com/test";
    public static final String NS = BASE + "#";
    
    public static final String DOC = "<rdf:RDF" +
                                     "   xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"" +
                                     "   xmlns:owl=\"http://www.w3.org/2002/07/owl#\"" +
                                     "   xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">" +
                                     "  <owl:Class rdf:about=\"http://www.hp.com/test#D\">" +
                                     "    <rdfs:subClassOf>" +
                                     "      <owl:Class rdf:about=\"http://www.hp.com/test#B\"/>" +
                                     "    </rdfs:subClassOf>" +
                                     "  </owl:Class>" +
                                     "  <owl:Class rdf:about=\"http://www.hp.com/test#B\">" +
                                     "    <rdfs:subClassOf rdf:resource=\"http://www.hp.com/test#A\"" +
                                     "       rdf:type=\"http://www.w3.org/2002/07/owl#Class\"/>" +
                                     "  </owl:Class>" +
                                     "  <owl:Class rdf:about=\"http://www.hp.com/test#C\">" +
                                     "    <rdfs:subClassOf rdf:resource=\"http://www.hp.com/test#B\"/>" +
                                     "  </owl:Class>" +
                                     "  <owl:ObjectProperty rdf:about=\"http://www.hp.com/test#p\">" +
                                     "    <rdfs:domain rdf:resource=\"http://www.hp.com/test#A\"/>" +
                                     "    <rdfs:range rdf:resource=\"http://www.hp.com/test#B\"/>" +
                                     "    <rdfs:range rdf:resource=\"http://www.hp.com/test#C\"/>" +
                                     "  </owl:ObjectProperty>" +
                                     "</rdf:RDF>";
    
    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    public TestOntModel( String name ) {
        super( name );
    }
    
    // External signature methods
    //////////////////////////////////

    public void setUp() {
        // ensure the ont doc manager is in a consistent state
        OntDocumentManager.getInstance().reset( true );
    }
    
    
    /** Test writing the base model to an output stream */
    public void testWriteOutputStream() {
        OntModel m = ModelFactory.createOntologyModel();
        
        // set up the model
        OntClass A = m.createClass( NS + "A" );
        OntClass B = m.createClass( NS + "B" );
        OntClass C = m.createClass( NS + "C" );
        OntClass D = m.createClass( NS + "D" );
        
        A.addSubClass( B );
        B.addSubClass( C );
        B.addSubClass( D );
        
        ObjectProperty p = m.createObjectProperty( NS + "p" );
        
        p.addDomain( A );
        p.addRange( B );
        p.addRange( C );
        
        // write to a stream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        m.write( out );

        String s = out.toString();
        ByteArrayInputStream in = new ByteArrayInputStream( s.getBytes() );
        
        // read it back again
        Model mIn1 = ModelFactory.createDefaultModel();
        mIn1.read( in, BASE );
        
        Model mIn2 = ModelFactory.createDefaultModel();
        mIn2.read( new ByteArrayInputStream( DOC.getBytes() ), BASE );
        
        // should be the same
        assertTrue( "InputStream write/read cycle failed (1)", mIn1.isIsomorphicWith( m.getBaseModel() ) );
        assertTrue( "InputStream write/read cycle failed (2)", mIn2.isIsomorphicWith( m.getBaseModel() ) );
    }
    
    public void testGetBaseModelPrefixes()
        {
        OntModel om = ModelFactory.createOntologyModel();
        om.setNsPrefix( "bill", "http://bill.and.ben/flowerpot#" );
        om.setNsPrefix( "grue", "ftp://grue.and.bleen/2000#" );
        assertEquals( om.getNsPrefixMap(), om.getBaseModel().getNsPrefixMap() );    
        }
        
    /**
         The prefixes of an OntModel should be the prefixes of its base model,
         plus any non-clashing ones from the document manager's prefix mapping.
         (which this test assume includes rdfs and daml).
    */
    public void testPrefixDefaulting()
        {
        Model base = ModelFactory.createDefaultModel();
        base.setNsPrefix( "hedgehog", "http://hedgehog.hog/" );
        base.setNsPrefix( "daml", "not:the-DAML-URI/" );
        base.setNsPrefix( "mine", RDF.getURI() );
        OntModel m = new OntModelImpl( OntModelSpec.RDFS_MEM, base );
        PrefixMapping given = m.getDocumentManager().getDeclaredPrefixMapping();
        assertEquals( "http://hedgehog.hog/", m.getNsPrefixURI( "hedgehog" ) );
        assertEquals( "not:the-DAML-URI/", m.getNsPrefixURI( "daml" ) );
        assertEquals( RDF.getURI(), m.getNsPrefixURI( "mine" ) );
        assertEquals( null, m.getNsPrefixURI( "rdf" ) );
        assertEquals( given.getNsPrefixURI( "rdfs" ), m.getNsPrefixURI( "rdfs" ) );
        }
    
    public void testWritesPrefixes()
        {
        OntModel om = ModelFactory.createOntologyModel();
        om.setNsPrefix( "spoo", "http://spoo.spoo.com/spoo#" );
        om.add( statement( om, "ping http://spoo.spoo.com/spoo#pang pilly" ) );
        om.add( statement( om, "gg http://www.daml.org/2001/03/daml+oil#hh ii" ) );
        StringWriter sw = new StringWriter(); 
        om.write( sw );    
        String s = sw.getBuffer().toString();
        assertTrue( s.indexOf( "xmlns:spoo=\"http://spoo.spoo.com/spoo#\"" ) > 0 );
        assertTrue( s.indexOf( "xmlns:daml=\"http://www.daml.org/2001/03/daml+oil#\"" ) > 0 );      
        }
        
    /** Test writing the base model to an output stream */
    public void testWriteWriter() {
        OntModel m = ModelFactory.createOntologyModel();
        
        // set up the model
        OntClass A = m.createClass( NS + "A" );
        OntClass B = m.createClass( NS + "B" );
        OntClass C = m.createClass( NS + "C" );
        OntClass D = m.createClass( NS + "D" );
        
        A.addSubClass( B );
        B.addSubClass( C );
        B.addSubClass( D );
        
        ObjectProperty p = m.createObjectProperty( NS + "p" );
        
        p.addDomain( A );
        p.addRange( B );
        p.addRange( C );
        
        // write to a stream
        StringWriter out = new StringWriter();
        m.write( out );

        String s = out.toString();
        
        // read it back again
        Model mIn1 = ModelFactory.createDefaultModel();
        mIn1.read( new StringReader( s ), BASE );
        
        Model mIn2 = ModelFactory.createDefaultModel();
        mIn2.read( new StringReader( DOC ), BASE );
        
        // should be the same
        assertTrue( "Writer write/read cycle failed (1)", mIn1.isIsomorphicWith( m.getBaseModel() ) );
        assertTrue( "Writer write/read cycle failed (2)", mIn2.isIsomorphicWith( m.getBaseModel() ) );
    }
    
    public void testGetOntology() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createOntology( NS + "s" );
        assertEquals( "Result of get s", s, m.getOntology( NS + "s" ) );
        assertNull( "result of get q", m.getOntology( NS+"q") );
        assertNull( "result of get r", m.getOntology( NS+"r"));
    }
    
   
    public void testGetIndividual() {
        OntModel m = ModelFactory.createOntologyModel();
        OntClass c = m.createClass( NS +"c" );
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createIndividual( NS + "s", c );
        assertEquals( "Result of get s", s, m.getIndividual( NS + "s" ) );
        assertNull( "result of get q", m.getIndividual( NS+"q") );
    }
    
   
    public void testGetOntProperty() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createOntProperty( NS + "s" );
        assertEquals( "Result of get s", s, m.getOntProperty( NS + "s" ) );
        assertNull( "result of get q", m.getOntProperty( NS+"q") );
        assertNull( "result of get r", m.getOntProperty( NS+"r"));
    }
    
   
    public void testGetObjectProperty() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createObjectProperty( NS + "s" );
        assertEquals( "Result of get s", s, m.getObjectProperty( NS + "s" ) );
        assertNull( "result of get q", m.getObjectProperty( NS+"q") );
        assertNull( "result of get r", m.getObjectProperty( NS+"r"));
    }
    
   
    public void testGetTransitiveProperty() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createTransitiveProperty( NS + "s" );
        assertEquals( "Result of get s", s, m.getTransitiveProperty( NS + "s" ) );
        assertNull( "result of get q", m.getTransitiveProperty( NS+"q") );
        assertNull( "result of get r", m.getTransitiveProperty( NS+"r"));
    }
    

    public void testGetSymmetricProperty() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createSymmetricProperty( NS + "s" );
        assertEquals( "Result of get s", s, m.getSymmetricProperty( NS + "s" ) );
        assertNull( "result of get q", m.getSymmetricProperty( NS+"q") );
        assertNull( "result of get r", m.getSymmetricProperty( NS+"r"));
    }   
    
    
    public void testGetInverseFunctionalProperty() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createInverseFunctionalProperty( NS + "s" );
        assertEquals( "Result of get s", s, m.getInverseFunctionalProperty( NS + "s" ) );
        assertNull( "result of get q", m.getInverseFunctionalProperty( NS+"q") );
        assertNull( "result of get r", m.getInverseFunctionalProperty( NS+"r"));
    }
    
    
    public void testGetDatatypeProperty() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createDatatypeProperty( NS + "s" );
        assertEquals( "Result of get s", s, m.getDatatypeProperty( NS + "s" ) );
        assertNull( "result of get q", m.getDatatypeProperty( NS+"q") );
        assertNull( "result of get r", m.getDatatypeProperty( NS+"r"));
    }
    
   
    public void testGetAnnotationProperty() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createAnnotationProperty( NS + "s" );
        assertEquals( "Result of get s", s, m.getAnnotationProperty( NS + "s" ) );
        assertNull( "result of get q", m.getAnnotationProperty( NS+"q") );
        assertNull( "result of get r", m.getAnnotationProperty( NS+"r"));
    }
    
   
    public void testGetOntClass() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource r = m.getResource( NS + "r" );
        Resource r0 = m.getResource( NS + "r0" );
        m.add( r, RDF.type, r0 );
        Resource s = m.createClass( NS + "s" );
        assertEquals( "Result of get s", s, m.getOntClass( NS + "s" ) );
        assertNull( "result of get q", m.getOntClass( NS+"q") );
        assertNull( "result of get r", m.getOntClass( NS+"r"));
    }
    
   
    public void testGetComplementClass() {
        OntModel m = ModelFactory.createOntologyModel();
        OntClass c = m.createClass( NS +"c" );
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createComplementClass( NS + "s", c );
        assertEquals( "Result of get s", s, m.getComplementClass( NS + "s" ) );
        assertNull( "result of get q", m.getComplementClass( NS+"q") );
        assertNull( "result of get r", m.getComplementClass( NS+"r"));
    }
    
   
    public void testGetEnumeratedClass() {
        OntModel m = ModelFactory.createOntologyModel();
        RDFList l = m.createList();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createEnumeratedClass( NS + "s", l );
        assertEquals( "Result of get s", s, m.getEnumeratedClass( NS + "s" ) );
        assertNull( "result of get q", m.getEnumeratedClass( NS+"q") );
        assertNull( "result of get r", m.getEnumeratedClass( NS+"r"));
    }
    
   
    public void testGetUnionClass() {
        OntModel m = ModelFactory.createOntologyModel();
        RDFList l = m.createList();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createUnionClass( NS + "s", l );
        assertEquals( "Result of get s", s, m.getUnionClass( NS + "s" ) );
        assertNull( "result of get q", m.getUnionClass( NS+"q") );
        assertNull( "result of get r", m.getUnionClass( NS+"r"));
    }
    
   
    public void testGetIntersectionClass() {
        OntModel m = ModelFactory.createOntologyModel();
        RDFList l = m.createList();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createIntersectionClass( NS + "s", l );
        assertEquals( "Result of get s", s, m.getIntersectionClass( NS + "s" ) );
        assertNull( "result of get q", m.getIntersectionClass( NS+"q") );
        assertNull( "result of get r", m.getIntersectionClass( NS+"r"));
    }


    public void testGetRestriction() {
        OntModel m = ModelFactory.createOntologyModel();
        Property p = m.createProperty( NS + "p" );
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createRestriction( NS + "s", p );
        assertEquals( "Result of get s", s, m.getRestriction( NS + "s" ) );
        assertNull( "result of get q", m.getRestriction( NS+"q") );
        assertNull( "result of get r", m.getRestriction( NS+"r"));
    }
    
    
    public void testGetHasValueRestriction() {
        OntModel m = ModelFactory.createOntologyModel();
        Property p = m.createProperty( NS + "p" );
        OntClass c = m.createClass( NS + "c" );
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createHasValueRestriction( NS + "s", p, c );
        assertEquals( "Result of get s", s, m.getHasValueRestriction( NS + "s" ) );
        assertNull( "result of get q", m.getHasValueRestriction( NS+"q") );
        assertNull( "result of get r", m.getHasValueRestriction( NS+"r"));
    }
    
    
    public void testGetSomeValuesFromRestriction() {
        OntModel m = ModelFactory.createOntologyModel();
        Property p = m.createProperty( NS + "p" );
        OntClass c = m.createClass( NS + "c" );
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createSomeValuesFromRestriction( NS + "s", p, c );
        assertEquals( "Result of get s", s, m.getSomeValuesFromRestriction( NS + "s" ) );
        assertNull( "result of get q", m.getSomeValuesFromRestriction( NS+"q") );
        assertNull( "result of get r", m.getSomeValuesFromRestriction( NS+"r"));
    }
    
    
    public void testGetAllValuesFromRestriction() {
        OntModel m = ModelFactory.createOntologyModel();
        Property p = m.createProperty( NS + "p" );
        OntClass c = m.createClass( NS + "c" );
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createAllValuesFromRestriction( NS + "s", p, c );
        assertEquals( "Result of get s", s, m.getAllValuesFromRestriction( NS + "s" ) );
        assertNull( "result of get q", m.getAllValuesFromRestriction( NS+"q") );
        assertNull( "result of get r", m.getAllValuesFromRestriction( NS+"r"));
    }
    
    
    public void testGetCardinalityRestriction() {
        OntModel m = ModelFactory.createOntologyModel();
        Property p = m.createProperty( NS + "p" );
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createCardinalityRestriction( NS + "s", p, 1 );
        assertEquals( "Result of get s", s, m.getCardinalityRestriction( NS + "s" ) );
        assertNull( "result of get q", m.getCardinalityRestriction( NS+"q") );
        assertNull( "result of get r", m.getCardinalityRestriction( NS+"r"));
    }
    
    
    public void testGetMinCardinalityRestriction() {
        OntModel m = ModelFactory.createOntologyModel();
        Property p = m.createProperty( NS + "p" );
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createMinCardinalityRestriction( NS + "s", p, 1 );
        assertEquals( "Result of get s", s, m.getMinCardinalityRestriction( NS + "s" ) );
        assertNull( "result of get q", m.getMinCardinalityRestriction( NS+"q") );
        assertNull( "result of get r", m.getMinCardinalityRestriction( NS+"r"));
    }
    
    
    public void testGetMaxCardinalityRestriction() {
        OntModel m = ModelFactory.createOntologyModel();
        Property p = m.createProperty( NS + "p" );
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createMaxCardinalityRestriction( NS + "s", p, 1 );
        assertEquals( "Result of get s", s, m.getMaxCardinalityRestriction( NS + "s" ) );
        assertNull( "result of get q", m.getMaxCardinalityRestriction( NS+"q") );
        assertNull( "result of get r", m.getMaxCardinalityRestriction( NS+"r"));
    }

    public void testGetSubgraphs() {
        OntModel m = ModelFactory.createOntologyModel();
        m.read( "file:testing/ontology/testImport6/a.owl" );
        assertEquals( "Marker count not correct", 4, TestOntDocumentManager.countMarkers( m ) );
        
        List subs = m.getSubGraphs();
        
        assertEquals( "n subgraphs should be ", 3, subs.size() );
        
        boolean isGraph = true;
        for (Iterator i = subs.iterator(); i.hasNext(); ) {
            Object x = i.next();
            if (!(x instanceof Graph)) {
                isGraph = false;
            }
        }
        assertTrue( "All sub-graphs should be graphs", isGraph );
        
    }
    
    
    public void testListImportURIs() {
        OntModel m = ModelFactory.createOntologyModel();
        m.read( "file:testing/ontology/testImport6/a.owl" );
        Collection c = m.listImportedOntologyURIs();
        
        assertEquals( "Should be two non-closed import URI's", 2, c.size() );
        assertTrue( "b should be imported ", c.contains( "file:testing/ontology/testImport6/b.owl" ));
        assertFalse( "c should not be imported ", c.contains( "file:testing/ontology/testImport6/c.owl" ));
        assertTrue( "d should be imported ", c.contains( "file:testing/ontology/testImport6/d.owl" ));

        c = m.listImportedOntologyURIs( true );
        
        assertEquals( "Should be two non-closed import URI's", 3, c.size() );
        assertTrue( "b should be imported ", c.contains( "file:testing/ontology/testImport6/b.owl" ));
        assertTrue( "c should be imported ", c.contains( "file:testing/ontology/testImport6/c.owl" ));
        assertTrue( "d should be imported ", c.contains( "file:testing/ontology/testImport6/d.owl" ));
    } 
    
    public void testListImportedModels() {
        OntModel m = ModelFactory.createOntologyModel();
        m.read( "file:testing/ontology/testImport6/a.owl" );
        assertEquals( "Marker count not correct", 4, TestOntDocumentManager.countMarkers( m ) );
        
        List importModels = new ArrayList();
        for (Iterator j = m.listImportedModels(); j.hasNext(); importModels.add( j.next() ));
        
        assertEquals( "n import models should be ", 3, importModels.size() );
        
        boolean isOntModel = true;
        int nImports = 0;
        
        for (Iterator i = importModels.iterator(); i.hasNext(); ) {
            Object x = i.next();
            if (!(x instanceof OntModel)) {
                isOntModel = false;
            }
            else {
                // count the number of imports of each sub-model
                OntModel mi = (OntModel) x;
                nImports += mi.listImportedOntologyURIs().size();
            }
        }
        
        assertTrue( "All import models should be OntModels", isOntModel );
        assertEquals( "Wrong number of sub-model imports", 2, nImports );
    }
    
    public void testGetImportedModel() {
        OntModel m = ModelFactory.createOntologyModel();
        m.read( "file:testing/ontology/testImport6/a.owl" );
        
        OntModel m0 = m.getImportedModel( "file:testing/ontology/testImport6/b.owl" );
        OntModel m1 = m.getImportedModel( "file:testing/ontology/testImport6/c.owl" );
        OntModel m2 = m.getImportedModel( "file:testing/ontology/testImport6/d.owl" );
        OntModel m3 = m.getImportedModel( "file:testing/ontology/testImport6/b.owl" )
                       .getImportedModel( "file:testing/ontology/testImport6/c.owl" );
        OntModel m4 = m.getImportedModel( "file:testing/ontology/testImport6/a.owl" );
        
        assertNotNull( "Import model b should not be null", m0 );
        assertNotNull( "Import model c should not be null", m1 );
        assertNotNull( "Import model d should not be null", m2 );
        assertNotNull( "Import model b-c should not be null", m3 );
        assertNull( "Import model a should be null", m4 );
    }
    
    /**
     * Test that the supports checks that are defined in the OWL full profile are not
     * missing in the DL and Lite profiles, unless by design. 
     * Not strictly a model test, but it has to go somewhere */
    public void testProfiles() {
        List notInDL = Arrays.asList( new Class[] {} );
        List notInLite = Arrays.asList( new Class[] {DataRange.class, HasValueRestriction.class} );
        
        Map fullProfileMap = new OWLProfileExt().getSupportsMap();
        Map dlProfileMap = new OWLDLProfileExt().getSupportsMap();
        Map liteProfileMap = new OWLLiteProfileExt().getSupportsMap();
        
        for (Iterator i = fullProfileMap.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry kv = (Map.Entry) i.next();
            Class c = (Class) kv.getKey();
            assertTrue( "Key in OWL DL profile: " + c.getName(), dlProfileMap.containsKey( c ) || notInDL.contains( c ));
            assertTrue( "Key in OWL lite profile: " + c.getName(), liteProfileMap.containsKey( c ) || notInLite.contains( c ));
        }
    }
    
    
    /**
        Added by kers to ensure that bulk update works; should really be a test
        of the ontology Graph using AbstractTestGraph, but that fails because there
        are too many things that don't pass those tests.
    <p>
        <b>Yet</b>.
    */
    public void testBulkAddWorks()
        {
        OntModel om1= ModelFactory.createOntologyModel();
        OntModel om2 = ModelFactory.createOntologyModel();
        om1.add( om2 );
        }
        
    public void testChecker1() {
        String NS = "http://example.org/test#";
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_LITE_MEM, null );
        m.createClass( NS + "A" );
        
        // note, just checking the process not the coverage of the syntax checker
        List probs = new ArrayList();
        assertEquals( "Should be owl lite", OWL.LITE_LANG, m.getOWLLanguageLevel( null ));
        assertEquals( "Should be owl lite", OWL.LITE_LANG, m.getOWLLanguageLevel( probs ));
        assertTrue( "should be no problems", probs.isEmpty() );
    }
    
    public void testChecker2() {
        String NS = "http://example.org/test#";
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_DL_MEM, null );
        m.createClass( NS + "A" );
        
        // note, just checking the process not the coverage of the syntax checker
        List probs = new ArrayList();
        assertEquals( "Should be owl lite", OWL.LITE_LANG, m.getOWLLanguageLevel( null ));
        assertEquals( "Should be owl lite", OWL.LITE_LANG, m.getOWLLanguageLevel( probs ));
        assertTrue( "should be no problems", probs.isEmpty() );
    }
    
    public void testChecker3() {
        String NS = "http://example.org/test#";
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_LITE_MEM, null );
        
        // contains non owl-lite class exprs
        m.read( "file:testing/ontology/owl/ClassExpression/test-boolean.rdf");
        
        // note, just checking the process not the coverage of the syntax checker
        List probs = new ArrayList();
        //assertEquals( "Should be owl DL", OWL.DL_LANG, m.getOWLLanguageLevel( null ));
        assertEquals( "Should be owl DL", OWL.DL_LANG, m.getOWLLanguageLevel( probs ));
        assertTrue( "should be some problems", !probs.isEmpty() );
    }
    
    public void testChecker4() {
        String NS = "http://example.org/test#";
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_DL_MEM, null );
        
        // contains non owl-lite class exprs
        m.read( "file:testing/ontology/owl/ClassExpression/test-boolean.rdf");
        
        // note, just checking the process not the coverage of the syntax checker
        List probs = new ArrayList();
        assertEquals( "Should be owl DL", OWL.DL_LANG, m.getOWLLanguageLevel( null ));
        assertEquals( "Should be owl DL", OWL.DL_LANG, m.getOWLLanguageLevel( probs ));
        assertTrue( "should be no problems", probs.isEmpty() );
    }
    
    public void testChecker5() {
        String NS = "http://example.org/test#";
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_LITE_MEM, null );
        
        OntClass A = m.createClass( NS + "A" );
        ObjectProperty p = m.createObjectProperty( NS + "p" );
        Resource B = m.createResource( NS + "B" );
        m.createAllValuesFromRestriction( null, p, B );
        
        // note, just checking the process not the coverage of the syntax checker
        List probs = new ArrayList();
        assertEquals( "Should be owl Full", OWL.FULL_LANG, m.getOWLLanguageLevel( null ));
        assertEquals( "Should be owl Full", OWL.FULL_LANG, m.getOWLLanguageLevel( probs ));
        assertTrue( "should be problems", !probs.isEmpty() );
    }
    
    public void testChecker6() {
        String NS = "http://example.org/test#";
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_DL_MEM, null );
        
        OntClass A = m.createClass( NS + "A" );
        ObjectProperty p = m.createObjectProperty( NS + "p" );
        Resource B = m.createResource( NS + "B" );
        m.createAllValuesFromRestriction( null, p, B );
        
        // note, just checking the process not the coverage of the syntax checker
        List probs = new ArrayList();
        assertEquals( "Should be owl Full", OWL.FULL_LANG, m.getOWLLanguageLevel( null ));
        assertEquals( "Should be owl Full", OWL.FULL_LANG, m.getOWLLanguageLevel( probs ));
        assertTrue( "should be problems", !probs.isEmpty() );
    }
    
    public void testChecker7() {
        String NS = "http://example.org/test#";
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM, null );
        
        OntClass A = m.createClass( NS + "A" );
        ObjectProperty p = m.createObjectProperty( NS + "p" );
        Resource B = m.createResource( NS + "B" );
        m.createAllValuesFromRestriction( null, p, B );
        
        // note, just checking the process not the coverage of the syntax checker
        List probs = new ArrayList();
        assertEquals( "Should be owl Full", OWL.FULL_LANG, m.getOWLLanguageLevel( null ));
        assertEquals( "Should be owl Full", OWL.FULL_LANG, m.getOWLLanguageLevel( probs ));
        assertTrue( "should be problems", !probs.isEmpty() );
    }
    
    public void testChecker8() {
        boolean ex = false;
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.DAML_MEM, null );
        
        try {
            m.getOWLLanguageLevel( null );
        }
        catch (OntologyException e) {
            ex = true;
        }
        
        assertTrue( "Should have raised exception to get owl lang level on non-owl model", ex );
    }
    
    
    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

    protected class OWLProfileExt extends OWLProfile
    {
        public Map getSupportsMap() {
            return getCheckTable();
        }
    }

    protected class OWLDLProfileExt extends OWLDLProfile
    {
        public Map getSupportsMap() {
            return getCheckTable();
        }
    }

    protected class OWLLiteProfileExt extends OWLLiteProfile
    {
        public Map getSupportsMap() {
            return getCheckTable();
        }
    }
}


/*
    (c) Copyright 2002, 2003, 2004 Hewlett-Packard Development Company, LP
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

