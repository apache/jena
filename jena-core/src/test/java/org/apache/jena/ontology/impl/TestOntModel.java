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

import org.apache.jena.graph.Graph;
import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.DataRange;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.HasValueRestriction;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.ontology.impl.OWLProfile.SupportsCheck;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.rulesys.test.TestRuleSystemBugs;
import org.apache.jena.test.JenaTestLib;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Unit tests on OntModel capabilities.  Many of OntModel's methods are tested by the other
 * abstractions' unit tests.
 * </p>
 */
@SuppressWarnings("removal")
public class TestOntModel
{

    static { JenaTestLib.setup(); }

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

    // External signature methods
    //////////////////////////////////

    @BeforeEach
    public void setUp() {
        // ensure the ont doc manager is in a consistent state
        OntDocumentManager.getInstance().reset( true );
    }

    /** Test writing the base model to an output stream */
    @Test
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
        m.write( out, "RDF/XML" );

        String s = out.toString();
        ByteArrayInputStream in = new ByteArrayInputStream( s.getBytes() );

        // read it back again
        Model mIn1 = ModelFactory.createDefaultModel();
        mIn1.read( in, BASE );

        Model mIn2 = ModelFactory.createDefaultModel();
        mIn2.read( new ByteArrayInputStream( DOC.getBytes() ), BASE );

        // should be the same
        assertTrue( mIn1.isIsomorphicWith( m.getBaseModel() ), "InputStream write/read cycle failed (1)" );
        assertTrue( mIn2.isIsomorphicWith( m.getBaseModel() ), "InputStream write/read cycle failed (2)" );
    }

    @Test
    public void testGetBaseModelPrefixes() {
        OntModel om = ModelFactory.createOntologyModel();
        om.setNsPrefix( "bill", "http://bill.and.ben/flowerpot#" );
        om.setNsPrefix( "grue", "ftp://grue.and.bleen/2000#" );
        assertEquals( om.getNsPrefixMap(), om.getBaseModel().getNsPrefixMap() );
    }

    /**
     * The default namespace pefix of a non-base-model should not manifest as
     * the default namespace prefix of the base model or the Ont model.
     */
    @Test
    public void testPolyadicPrefixMapping() {
        final String IMPORTED_NAMESPACE = "http://imported#";
        final String LOCAL_NAMESPACE = "http://local#";
        Model importedModel = ModelFactory.createDefaultModel();
        importedModel.setNsPrefix( "", IMPORTED_NAMESPACE );
        OntModel ontModel = ModelFactory.createOntologyModel();
        ontModel.setNsPrefix( "", LOCAL_NAMESPACE );
        ontModel.addSubModel( importedModel );
        assertNull( ontModel.getNsURIPrefix( IMPORTED_NAMESPACE ) );
    }

    @Test
    public void testWritesPrefixes() {
        OntModel om = ModelFactory.createOntologyModel();
        om.setNsPrefix( "spoo", "http://spoo.spoo.com/spoo#" );
        om.add( ModelTestLib.statement( om, "ping http://spoo.spoo.com/spoo#pang pilly" ) );
        om.add( ModelTestLib.statement( om, "gg " + OWL.getURI() + "hh ii" ) );
        StringWriter sw = new StringWriter();
        om.write( sw , "RDF/XML");
        String s = sw.getBuffer().toString();
        assertTrue( s.indexOf( "xmlns:spoo=\"http://spoo.spoo.com/spoo#\"" ) > 0 );
        assertTrue( s.indexOf( "xmlns:owl=\"" + OWL.getURI() + "\"" ) > 0 );
    }

    /** Test writing the base model to an output stream */
    @Test
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
        m.write( out, "RDF/XML");

        String s = out.toString();

        // read it back again
        Model mIn1 = ModelFactory.createDefaultModel();
        mIn1.read( new StringReader( s ), BASE );

        Model mIn2 = ModelFactory.createDefaultModel();
        mIn2.read( new StringReader( DOC ), BASE );

        // should be the same
        assertTrue( mIn1.isIsomorphicWith( m.getBaseModel() ), "Writer write/read cycle failed (1)" );
        assertTrue( mIn2.isIsomorphicWith( m.getBaseModel() ), "Writer write/read cycle failed (2)" );
    }

    @Test
    public void testGetOntology() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createOntology( NS + "s" );
        assertEquals( s, m.getOntology( NS + "s" ), "Result of get s" );
        assertNull( m.getOntology( NS+"q"), "result of get q" );
        assertNull( m.getOntology( NS+"r"), "result of get r");
    }

    @Test
    public void testGetIndividual() {
        OntModel m = ModelFactory.createOntologyModel();
        OntClass c = m.createClass( NS +"c" );
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createIndividual( NS + "s", c );
        assertEquals( s, m.getIndividual( NS + "s" ), "Result of get s" );
        assertNull( m.getIndividual( NS+"q"), "result of get q" );
    }

    /** User requested: allow null arguments when creating individuals */
    @Test
    public void testCreateIndividual() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        Resource i0 = m.createIndividual( OWL.Thing );
        Resource i1 = m.createIndividual( null );
        Resource i2 = m.createIndividual( NS + "i2", OWL.Thing );
        Resource i3 = m.createIndividual( NS + "i3", null );
        Resource i4 = m.createIndividual( null, OWL.Thing );
        Resource i5 = m.createIndividual( null, null );

        assertNotNull( i0 );
        assertNotNull( i1 );
        assertNotNull( i2 );
        assertNotNull( i3 );
        assertNotNull( i4 );
        assertNotNull( i5 );
    }

    @Test
    public void testGetOntProperty() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createOntProperty( NS + "s" );
        assertEquals( s, m.getOntProperty( NS + "s" ), "Result of get s" );
        assertNull( m.getOntProperty( NS+"q"), "result of get q" );
        assertNull( m.getOntProperty( NS+"r"), "result of get r");
    }

    @Test
    public void testGetObjectProperty() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createObjectProperty( NS + "s" );
        assertEquals( s, m.getObjectProperty( NS + "s" ), "Result of get s" );
        assertNull( m.getObjectProperty( NS+"q"), "result of get q" );
        assertNull( m.getObjectProperty( NS+"r"), "result of get r");
    }

    @Test
    public void testGetTransitiveProperty() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createTransitiveProperty( NS + "s" );
        assertEquals( s, m.getTransitiveProperty( NS + "s" ), "Result of get s" );
        assertNull( m.getTransitiveProperty( NS+"q"), "result of get q" );
        assertNull( m.getTransitiveProperty( NS+"r"), "result of get r");
    }

    @Test
    public void testGetSymmetricProperty() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createSymmetricProperty( NS + "s" );
        assertEquals( s, m.getSymmetricProperty( NS + "s" ), "Result of get s" );
        assertNull( m.getSymmetricProperty( NS+"q"), "result of get q" );
        assertNull( m.getSymmetricProperty( NS+"r"), "result of get r");
    }

    @Test
    public void testGetInverseFunctionalProperty() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createInverseFunctionalProperty( NS + "s" );
        assertEquals( s, m.getInverseFunctionalProperty( NS + "s" ), "Result of get s" );
        assertNull( m.getInverseFunctionalProperty( NS+"q"), "result of get q" );
        assertNull( m.getInverseFunctionalProperty( NS+"r"), "result of get r");
    }

    @Test
    public void testGetDatatypeProperty() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createDatatypeProperty( NS + "s" );
        assertEquals( s, m.getDatatypeProperty( NS + "s" ), "Result of get s" );
        assertNull( m.getDatatypeProperty( NS+"q"), "result of get q" );
        assertNull( m.getDatatypeProperty( NS+"r"), "result of get r");
    }

    @Test
    public void testGetAnnotationProperty() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createAnnotationProperty( NS + "s" );
        assertEquals( s, m.getAnnotationProperty( NS + "s" ), "Result of get s" );
        assertNull( m.getAnnotationProperty( NS+"q"), "result of get q" );
        assertNull( m.getAnnotationProperty( NS+"r"), "result of get r");
    }

    @Test
    public void testGetOntResource() {
        OntModel m = ModelFactory.createOntologyModel();
        OntResource r0 = m.getOntResource( NS + "a" );
        assertNull( r0 );
        OntResource r1 = m.createOntResource( NS + "aaa" );
        JenaTestLib.assertInstanceOf( OntResource.class, r1 );
        Resource r2a = m.getResource( NS + "a" );
        Resource r2b = m.getResource( NS + "b" );
        Property p = m.getProperty( NS + "p" );
        m.add( r2a, p, r2b );
        r0 = m.getOntResource( NS + "a" );
        JenaTestLib.assertInstanceOf( OntResource.class, r0 );
        OntResource r3 = m.getOntResource( r2b );
        JenaTestLib.assertInstanceOf( OntResource.class, r3 );
    }

    @Test
    public void testGetOntClass() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource r = m.getResource( NS + "r" );
        Resource r0 = m.getResource( NS + "r0" );
        m.add( r, RDF.type, r0 );
        Resource s = m.createClass( NS + "s" );
        assertEquals( s, m.getOntClass( NS + "s" ), "Result of get s" );
        assertNull( m.getOntClass( NS+"q"), "result of get q" );
        assertNull( m.getOntClass( NS+"r"), "result of get r");
    }

    @Test
    public void testGetComplementClass() {
        OntModel m = ModelFactory.createOntologyModel();
        OntClass c = m.createClass( NS +"c" );
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createComplementClass( NS + "s", c );
        assertEquals( s, m.getComplementClass( NS + "s" ), "Result of get s" );
        assertNull( m.getComplementClass( NS+"q"), "result of get q" );
        assertNull( m.getComplementClass( NS+"r"), "result of get r");
    }

    @Test
    public void testGetEnumeratedClass() {
        OntModel m = ModelFactory.createOntologyModel();
        RDFList l = m.createList();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createEnumeratedClass( NS + "s", l );
        assertEquals( s, m.getEnumeratedClass( NS + "s" ), "Result of get s" );
        assertNull( m.getEnumeratedClass( NS+"q"), "result of get q" );
        assertNull( m.getEnumeratedClass( NS+"r"), "result of get r");
    }

    @Test
    public void testGetUnionClass() {
        OntModel m = ModelFactory.createOntologyModel();
        RDFList l = m.createList();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createUnionClass( NS + "s", l );
        assertEquals( s, m.getUnionClass( NS + "s" ), "Result of get s" );
        assertNull( m.getUnionClass( NS+"q"), "result of get q" );
        assertNull( m.getUnionClass( NS+"r"), "result of get r");
    }

    @Test
    public void testGetIntersectionClass() {
        OntModel m = ModelFactory.createOntologyModel();
        RDFList l = m.createList();
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createIntersectionClass( NS + "s", l );
        assertEquals( s, m.getIntersectionClass( NS + "s" ), "Result of get s" );
        assertNull( m.getIntersectionClass( NS+"q"), "result of get q" );
        assertNull( m.getIntersectionClass( NS+"r"), "result of get r");
    }

    @Test
    public void testGetRestriction() {
        OntModel m = ModelFactory.createOntologyModel();
        Property p = m.createProperty( NS + "p" );
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createRestriction( NS + "s", p );
        assertEquals( s, m.getRestriction( NS + "s" ), "Result of get s" );
        assertNull( m.getRestriction( NS+"q"), "result of get q" );
        assertNull( m.getRestriction( NS+"r"), "result of get r");
    }

    @Test
    public void testGetHasValueRestriction() {
        OntModel m = ModelFactory.createOntologyModel();
        Property p = m.createProperty( NS + "p" );
        OntClass c = m.createClass( NS + "c" );
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createHasValueRestriction( NS + "s", p, c );
        assertEquals( s, m.getHasValueRestriction( NS + "s" ), "Result of get s" );
        assertNull( m.getHasValueRestriction( NS+"q"), "result of get q" );
        assertNull( m.getHasValueRestriction( NS+"r"), "result of get r");
    }

    @Test
    public void testGetSomeValuesFromRestriction() {
        OntModel m = ModelFactory.createOntologyModel();
        Property p = m.createProperty( NS + "p" );
        OntClass c = m.createClass( NS + "c" );
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createSomeValuesFromRestriction( NS + "s", p, c );
        assertEquals( s, m.getSomeValuesFromRestriction( NS + "s" ), "Result of get s" );
        assertNull( m.getSomeValuesFromRestriction( NS+"q"), "result of get q" );
        assertNull( m.getSomeValuesFromRestriction( NS+"r"), "result of get r");
    }

    @Test
    public void testGetAllValuesFromRestriction() {
        OntModel m = ModelFactory.createOntologyModel();
        Property p = m.createProperty( NS + "p" );
        OntClass c = m.createClass( NS + "c" );
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createAllValuesFromRestriction( NS + "s", p, c );
        assertEquals( s, m.getAllValuesFromRestriction( NS + "s" ), "Result of get s" );
        assertNull( m.getAllValuesFromRestriction( NS+"q"), "result of get q" );
        assertNull( m.getAllValuesFromRestriction( NS+"r"), "result of get r");
    }

    @Test
    public void testGetCardinalityRestriction() {
        OntModel m = ModelFactory.createOntologyModel();
        Property p = m.createProperty( NS + "p" );
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createCardinalityRestriction( NS + "s", p, 1 );
        assertEquals( s, m.getCardinalityRestriction( NS + "s" ), "Result of get s" );
        assertNull( m.getCardinalityRestriction( NS+"q"), "result of get q" );
        assertNull( m.getCardinalityRestriction( NS+"r"), "result of get r");
    }

    @Test
    public void testGetMinCardinalityRestriction() {
        OntModel m = ModelFactory.createOntologyModel();
        Property p = m.createProperty( NS + "p" );
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createMinCardinalityRestriction( NS + "s", p, 1 );
        assertEquals( s, m.getMinCardinalityRestriction( NS + "s" ), "Result of get s" );
        assertNull( m.getMinCardinalityRestriction( NS+"q"), "result of get q" );
        assertNull( m.getMinCardinalityRestriction( NS+"r"), "result of get r");
    }

    @Test
    public void testGetMaxCardinalityRestriction() {
        OntModel m = ModelFactory.createOntologyModel();
        Property p = m.createProperty( NS + "p" );
        Resource r = m.getResource( NS + "r" );
        m.add( r, RDF.type, r );
        Resource s = m.createMaxCardinalityRestriction( NS + "s", p, 1 );
        assertEquals( s, m.getMaxCardinalityRestriction( NS + "s" ), "Result of get s" );
        assertNull( m.getMaxCardinalityRestriction( NS+"q"), "result of get q" );
        assertNull( m.getMaxCardinalityRestriction( NS+"r"), "result of get r");
    }

    @Test
    public void testGetSubgraphs() {
        OntModel m = ModelFactory.createOntologyModel();
        m.read( "file:testing/ontology/testImport6/a.owl" );
        assertEquals( 4, TestOntDocumentManager.countMarkers( m ), "Marker count not correct" );

        List<Graph> subs = m.getSubGraphs();

        assertEquals( 3, subs.size(), "n subgraphs should be " );
    }

    private static boolean hasImport(Collection<String> c, String x) {
        String x2 = x.substring("file:".length());
        return c.stream().anyMatch(elt->elt.endsWith(x2));
    }

    @Test
    public void testListImportURIs() {
        OntModel m = ModelFactory.createOntologyModel();
        m.read( "file:testing/ontology/testImport6/a.owl" );
        Collection<String> c = m.listImportedOntologyURIs();

        assertEquals( 2, c.size(), "Should be two non-closed import URI's" );
        assertTrue( hasImport(c, "file:testing/ontology/testImport6/b.owl"), "b should be imported ");
        assertFalse( hasImport(c, "file:testing/ontology/testImport6/c.owl"), "c should not be imported ");
        assertTrue( hasImport(c, "file:testing/ontology/testImport6/d.owl"), "d should be imported ");

        c = m.listImportedOntologyURIs( true );

        assertEquals( 3, c.size(), "Should be two non-closed import URI's" );
        assertTrue( hasImport(c, "file:testing/ontology/testImport6/b.owl" ), "b should be imported ");
        assertTrue( hasImport(c, "file:testing/ontology/testImport6/c.owl" ), "c should be imported ");
        assertTrue( hasImport(c, "file:testing/ontology/testImport6/d.owl" ), "d should be imported ");
    }

    /** Some tests for listing properties. See also {@link TestListSyntaxCategories} */

    @Test
    public void testListOntProperties0() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        ObjectProperty op = m.createObjectProperty( NS + "op" );
        DatatypeProperty dp = m.createDatatypeProperty( NS + "dp" );
        AnnotationProperty ap = m.createAnnotationProperty( NS + "ap" );
        OntProperty ontp = m.createOntProperty( NS + "ontp" );
        Property rdfp = m.createProperty( NS + "rdfp" );
        rdfp.addProperty( RDF.type, RDF.Property );

        // no rdf:type entailment, so we don't find most properties ...

        assertFalse( iteratorContains( m.listOntProperties(), op ) );
        assertFalse( iteratorContains( m.listOntProperties(), dp ) );
        assertFalse( iteratorContains( m.listOntProperties(), ap ) );
        assertTrue( iteratorContains( m.listOntProperties(), ontp ) );
        assertTrue( iteratorContains( m.listOntProperties(), rdfp ) );
    }

    @Test
    public void testListOntProperties1() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_MICRO_RULE_INF);
        ObjectProperty op = m.createObjectProperty( NS + "op" );
        DatatypeProperty dp = m.createDatatypeProperty( NS + "dp" );
        AnnotationProperty ap = m.createAnnotationProperty( NS + "ap" );
        OntProperty ontp = m.createOntProperty( NS + "ontp" );
        Property rdfp = m.createProperty( NS + "rdfp" );
        rdfp.addProperty( RDF.type, RDF.Property );

        assertTrue( iteratorContains( m.listOntProperties(), op ) );
        assertTrue( iteratorContains( m.listOntProperties(), dp ) );

        // note that owl:AnnotationProperty is an rdf:Property in OWL Full
        assertTrue( iteratorContains( m.listOntProperties(), ap ) );
        assertTrue( iteratorContains( m.listOntProperties(), ontp ) );
        assertTrue( iteratorContains( m.listOntProperties(), rdfp ) );
    }

    @Test
    public void testListOntProperties2() {
        OntModelSpec owlDLReasoner = new OntModelSpec( OntModelSpec.OWL_DL_MEM );
        owlDLReasoner.setReasoner( OntModelSpec.OWL_MEM_MICRO_RULE_INF.getReasoner() );
        OntModel m = ModelFactory.createOntologyModel( owlDLReasoner );
        ObjectProperty op = m.createObjectProperty( NS + "op" );
        DatatypeProperty dp = m.createDatatypeProperty( NS + "dp" );
        AnnotationProperty ap = m.createAnnotationProperty( NS + "ap" );
        OntProperty ontp = m.createOntProperty( NS + "ontp" );
        Property rdfp = m.createProperty( NS + "rdfp" );
        rdfp.addProperty( RDF.type, RDF.Property );

        assertTrue( iteratorContains( m.listOntProperties(), op ) );
        assertTrue( iteratorContains( m.listOntProperties(), dp ) );

        // note that owl:AnnotationProperty not an rdf:Property in OWL DL
        assertFalse( iteratorContains( m.listOntProperties(), ap ) );
        assertTrue( iteratorContains( m.listOntProperties(), ontp ) );
        assertTrue( iteratorContains( m.listOntProperties(), rdfp ) );
    }

    @Test
    public void testListAllOntProperties0() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        ObjectProperty op = m.createObjectProperty( NS + "op" );
        DatatypeProperty dp = m.createDatatypeProperty( NS + "dp" );
        AnnotationProperty ap = m.createAnnotationProperty( NS + "ap" );
        OntProperty ontp = m.createOntProperty( NS + "ontp" );
        Property rdfp = m.createProperty( NS + "rdfp" );
        rdfp.addProperty( RDF.type, RDF.Property );

        // no rdf:type entailment, so we don't find most properties ...

        assertTrue( iteratorContains( m.listAllOntProperties(), op ) );
        assertTrue( iteratorContains( m.listAllOntProperties(), dp ) );
        assertTrue( iteratorContains( m.listAllOntProperties(), ap ) );
        assertTrue( iteratorContains( m.listAllOntProperties(), ontp ) );
        assertTrue( iteratorContains( m.listAllOntProperties(), rdfp ) );
    }

    @Test
    public void testListObjectProperties0() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        ObjectProperty op = m.createObjectProperty( NS + "op" );
        DatatypeProperty dp = m.createDatatypeProperty( NS + "dp" );
        AnnotationProperty ap = m.createAnnotationProperty( NS + "ap" );
        OntProperty ontp = m.createOntProperty( NS + "ontp" );
        Property rdfp = m.createProperty( NS + "rdfp" );
        rdfp.addProperty( RDF.type, RDF.Property );

        // no rdf:type entailment, so we don't find most properties ...

        assertTrue( iteratorContains( m.listObjectProperties(), op ) );
        assertFalse( iteratorContains( m.listObjectProperties(), dp ) );
        assertFalse( iteratorContains( m.listObjectProperties(), ap ) );
        assertFalse( iteratorContains( m.listObjectProperties(), ontp ) );
        assertFalse( iteratorContains( m.listObjectProperties(), rdfp ) );
    }

    @Test
    public void testListDatatypeProperties0() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        ObjectProperty op = m.createObjectProperty( NS + "op" );
        DatatypeProperty dp = m.createDatatypeProperty( NS + "dp" );
        AnnotationProperty ap = m.createAnnotationProperty( NS + "ap" );
        OntProperty ontp = m.createOntProperty( NS + "ontp" );
        Property rdfp = m.createProperty( NS + "rdfp" );
        rdfp.addProperty( RDF.type, RDF.Property );

        // no rdf:type entailment, so we don't find most properties ...

        assertFalse( iteratorContains( m.listDatatypeProperties(), op ) );
        assertTrue( iteratorContains( m.listDatatypeProperties(), dp ) );
        assertFalse( iteratorContains( m.listDatatypeProperties(), ap ) );
        assertFalse( iteratorContains( m.listDatatypeProperties(), ontp ) );
        assertFalse( iteratorContains( m.listDatatypeProperties(), rdfp ) );
    }

    @Test
    public void testListAnnotationProperties0() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        ObjectProperty op = m.createObjectProperty( NS + "op" );
        DatatypeProperty dp = m.createDatatypeProperty( NS + "dp" );
        AnnotationProperty ap = m.createAnnotationProperty( NS + "ap" );
        OntProperty ontp = m.createOntProperty( NS + "ontp" );
        Property rdfp = m.createProperty( NS + "rdfp" );
        rdfp.addProperty( RDF.type, RDF.Property );

        // no rdf:type entailment, so we don't find most properties ...

        assertFalse( iteratorContains( m.listAnnotationProperties(), op ) );
        assertFalse( iteratorContains( m.listAnnotationProperties(), dp ) );
        assertTrue( iteratorContains( m.listAnnotationProperties(), ap ) );
        assertFalse( iteratorContains( m.listAnnotationProperties(), ontp ) );
        assertFalse( iteratorContains( m.listAnnotationProperties(), rdfp ) );
    }

    @Test
    public void testListSubModels0() {
        OntModel m = ModelFactory.createOntologyModel();
        m.read( "file:testing/ontology/testImport6/a.owl" );
        assertEquals( 4, TestOntDocumentManager.countMarkers( m ), "Marker count not correct" );

        List<OntModel> importModels = new ArrayList<>();
        for (Iterator<OntModel> j = m.listSubModels(); j.hasNext(); ) {
            importModels.add( j.next() );
        }

        assertEquals( 3, importModels.size(), "n import models should be " );

        int nImports = 0;

        for ( OntModel x : importModels )
        {
            // count the number of imports of each sub-model
            nImports += x.countSubModels();
        }
        // listSubModels' default behaviour is *not* to include imports of sub-models
        assertEquals( 0, nImports, "Wrong number of sub-model imports" );
    }

    @Test
    public void testListSubModels1() {
        OntModel m = ModelFactory.createOntologyModel();
        m.read( "file:testing/ontology/testImport6/a.owl" );
        assertEquals( 4, TestOntDocumentManager.countMarkers( m ), "Marker count not correct" );

        List<OntModel> importModels = new ArrayList<>();
        for (Iterator<OntModel> j = m.listSubModels( true ); j.hasNext(); ) {
            importModels.add( j.next() );
        }

        assertEquals( 3, importModels.size(), "n import models should be " );

        int nImports = 0;

        for ( OntModel x : importModels )
        {
            // count the number of imports of each sub-model
            nImports += x.countSubModels();
        }
        assertEquals( 2, nImports, "Wrong number of sub-model imports" );
    }

    @Test
    public void testGetImportedModel() {
        OntModel m = ModelFactory.createOntologyModel();
        m.read( "file:testing/ontology/testImport6/a.owl" );

        OntModel m0 = m.getImportedModel( "file:testing/ontology/testImport6/b.owl" );
        OntModel m1 = m.getImportedModel( "file:testing/ontology/testImport6/c.owl" );
        OntModel m2 = m.getImportedModel( "file:testing/ontology/testImport6/d.owl" );
        OntModel m3 = m.getImportedModel( "file:testing/ontology/testImport6/b.owl" )
                       .getImportedModel( "file:testing/ontology/testImport6/c.owl" );
        OntModel m4 = m.getImportedModel( "file:testing/ontology/testImport6/a.owl" );

        assertNotNull( m0, "Import model b should not be null" );
        assertNotNull( m1, "Import model c should not be null" );
        assertNotNull( m2, "Import model d should not be null" );
        assertNotNull( m3, "Import model b-c should not be null" );
        assertNull( m4, "Import model a should be null" );
    }

    /**
     * Test that the supports checks that are defined in the OWL full profile are not
     * missing in the DL and Lite profiles, unless by design.
     * Not strictly a model test, but it has to go somewhere */
    @Test
    public void testProfiles() {
        List<Class<?>> notInDL = Arrays.asList( new Class<?>[] {} );
        List<Class<?>> notInLite = Arrays.asList( new Class<?>[] {DataRange.class, HasValueRestriction.class} );

        Map<Class<?>, SupportsCheck> fullProfileMap = new OWLProfileExt().getSupportsMap();
        Map<Class<?>, SupportsCheck> dlProfileMap = new OWLDLProfileExt().getSupportsMap();
        Map<Class<?>, SupportsCheck> liteProfileMap = new OWLLiteProfileExt().getSupportsMap();

        for ( Map.Entry<Class<?>, SupportsCheck> entry : fullProfileMap.entrySet() )
        {
            Class<?> c = entry.getKey();
            assertTrue( dlProfileMap.containsKey( c ) || notInDL.contains( c ), "Key in OWL DL profile: " + c.getName() );
            assertTrue( liteProfileMap.containsKey( c ) || notInLite.contains( c ), "Key in OWL lite profile: " + c.getName() );
        }
    }

    /**
        Added by kers to ensure that bulk update works; should really be a test
        of the ontology Graph using AbstractTestGraph, but that fails because there
        are too many things that don't pass those tests.
    <p>
        <b>Yet</b>.
    */
    @Test
    public void testBulkAddWorks()
        {
        OntModel om1= ModelFactory.createOntologyModel();
        OntModel om2 = ModelFactory.createOntologyModel();
        om1.add( om2 );
        }

    @Test
    public void testRead() {
        String base0 = "http://example.com/test0";
        String ns0 = base0 + "#";
        String base1 = "http://example.com/test1";
        String ns1 = base1 + "#";

        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        m.getDocumentManager().reset();
        m.getDocumentManager().addAltEntry( base0, "file:testing/ontology/relativenames.rdf" );
        m.read( base0, "RDF/XML" );
        assertNotNull( m.getOntClass( ns0 + "A" ), "Should be a class ns0:A" );
        assertNull( m.getOntClass( ns1 + "A" ), "Should not be a class ns1:A" );

        m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        m.getDocumentManager().reset();
        m.getDocumentManager().addAltEntry( base0, "file:testing/ontology/relativenames.rdf" );
        m.read( base0, base1, "RDF/XML" );
        assertNull( m.getOntClass( ns0 + "A" ), "Should not be a class ns0:A" );
        assertNotNull( m.getOntClass( ns1 + "A" ), "Should be a class ns1:A" );
    }

    @Test
    public void testListDataRange() {
        String base = "http://jena.hpl.hp.com/test#";
        String doc =
                 "<?xml version='1.0'?>"
                + "<!DOCTYPE owl ["
                + "      <!ENTITY rdf  'http://www.w3.org/1999/02/22-rdf-syntax-ns#' >"
                + "      <!ENTITY rdfs 'http://www.w3.org/2000/01/rdf-schema#' >"
                + "      <!ENTITY xsd  'http://www.w3.org/2001/XMLSchema#' >"
                + "      <!ENTITY owl  'http://www.w3.org/2002/07/owl#' >"
                + "      <!ENTITY dc   'http://purl.org/dc/elements/1.1/' >"
                + "      <!ENTITY base  'http://jena.hpl.hp.com/test' >"
                + "    ]>"
                + "<rdf:RDF"
                + "   xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'"
                + "   xmlns:owl='http://www.w3.org/2002/07/owl#'>"
                + "  <owl:DataRange>"
                + "    <owl:oneOf>"
                + "      <rdf:List>"
                + "        <rdf:first rdf:datatype='&xsd;integer'>0</rdf:first>"
                + "        <rdf:rest rdf:resource='&rdf;nil' />"
                + "      </rdf:List>"
                + "    </owl:oneOf>"
                + "  </owl:DataRange>"
                + "</rdf:RDF>";

        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
        m.read(new StringReader(doc), base);

        Iterator<DataRange> i = m.listDataRanges();
        assertTrue( i.hasNext(), "Should be at least one DataRange" );
        Object dr = i.next();
        JenaTestLib.assertInstanceOf( DataRange.class, dr );
        assertFalse( i.hasNext(), "Should no more DataRange" );
    }

    @Test
    public void testListHierarchyRoots0() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        assertFalse( m.listHierarchyRootClasses().hasNext() );
        m = ModelFactory.createOntologyModel( OntModelSpec.RDFS_MEM );
        assertFalse( m.listHierarchyRootClasses().hasNext() );
    }

    @Test
    public void testListHierarchyRoots1() {
        String doc =
                  "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>. "
                + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>. "
                + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#>. "
                + "@prefix owl: <http://www.w3.org/2002/07/owl#>. "
                + "@prefix : <" + NS + ">. "
                + ":A a owl:Class. "
               ;

        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
        m.read( new StringReader(doc), NS, "N3" );

        OntClass a = m.getOntClass(NS+"A");
        OntTestUtil.assertIteratorValues(m.listHierarchyRootClasses(),
                                       new Object[] {a} );
    }

    @Test
    public void testListHierarchyRoots2() {
        String doc =
                  "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>. "
                + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>. "
                + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#>. "
                + "@prefix owl: <http://www.w3.org/2002/07/owl#>. "
                + "@prefix : <" + NS + ">. "
                + ":A a owl:Class. "
               ;

        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, null);
        m.read( new StringReader(doc), NS, "N3" );

        OntClass a = m.getOntClass(NS+"A");
        OntTestUtil.assertIteratorValues(m.listHierarchyRootClasses(),
                                       new Object[] {a} );
    }

    @Test
    public void testListHierarchyRoots3() {
        String doc =
                  "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>. "
                + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>. "
                + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#>. "
                + "@prefix owl: <http://www.w3.org/2002/07/owl#>. "
                + "@prefix : <" + NS + ">. "
                + ":A a owl:Class. "
                + ":B a owl:Class; rdfs:subClassOf :A . "
               ;

        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MINI_RULE_INF, null);
        m.read( new StringReader(doc), NS, "N3" );

        OntClass a = m.getOntClass(NS+"A");
        OntTestUtil.assertIteratorValues(m.listHierarchyRootClasses(),
                                       new Object[] {a} );
    }

    @Test
    public void testListHierarchyRoots4() {
        String doc =
                  "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>. "
                + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>. "
                + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#>. "
                + "@prefix owl: <http://www.w3.org/2002/07/owl#>. "
                + "@prefix : <" + NS + ">. "
                + ":A a rdfs:Class. "
                + ":C a rdfs:Class. "
                + ":B a rdfs:Class; rdfs:subClassOf :A . "
               ;

        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF, null);
        m.read( new StringReader(doc), NS, "N3" );

        OntClass a = m.getOntClass(NS+"A");
        OntClass c = m.getOntClass(NS+"C");
        OntTestUtil.assertIteratorValues(m.listHierarchyRootClasses(),
                                       new Object[] {a,c} );
    }

    /* Auto-loading of imports is off by default */
    @Test
    public void testLoadImports0() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        Resource a = m.getResource( "file:testing/ontology/testImport3/a.owl" );
        Resource b = m.getResource( "file:testing/ontology/testImport3/b.owl" );
        m.add( a, m.getProfile().IMPORTS(), b );

        // not dymamically imported by default
        assertEquals( 0, TestOntDocumentManager.countMarkers( m ), "Marker count not correct" );

        assertFalse( m.hasLoadedImport( "file:testing/ontology/testImport3/c.owl" ), "c should not be imported" );
        assertFalse( m.hasLoadedImport( "file:testing/ontology/testImport3/b.owl" ), "b should not be imported" );

        m.loadImports();

        assertEquals( 2, TestOntDocumentManager.countMarkers( m ), "Marker count not correct" );

        assertTrue( m.hasLoadedImport( "file:testing/ontology/testImport3/c.owl" ), "c should be imported" );
        assertTrue( m.hasLoadedImport( "file:testing/ontology/testImport3/b.owl" ), "b should be imported" );
    }

    /* Auto-loading of imports = on */
    @Test
    public void testLoadImports1() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        Resource a = m.getResource( "file:testing/ontology/testImport3/a.owl" );
        Resource b = m.getResource( "file:testing/ontology/testImport3/b.owl" );

        m.setDynamicImports( true );
        m.add( a, m.getProfile().IMPORTS(), b );

        assertEquals( 2, TestOntDocumentManager.countMarkers( m ), "Marker count not correct" );

        assertTrue( m.hasLoadedImport( "file:testing/ontology/testImport3/c.owl" ), "c should be imported" );
        assertTrue( m.hasLoadedImport( "file:testing/ontology/testImport3/b.owl" ), "b should be imported" );

        // this should have no effect
        m.loadImports();

        assertEquals( 2, TestOntDocumentManager.countMarkers( m ), "Marker count not correct" );

        assertTrue( m.hasLoadedImport( "file:testing/ontology/testImport3/c.owl" ), "c should be imported" );
        assertTrue( m.hasLoadedImport( "file:testing/ontology/testImport3/b.owl" ), "b should be imported" );
    }

    /** Test that resources are attached to the right sub-models when importing */
    @Test
    public void testLoadImports2() {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
        ontModel.read("file:testing/ontology/testImport8/a.owl");

        String NSa = "http://incubator.apache.org/jena/2011/10/testont/a#";
        String NSb = "http://incubator.apache.org/jena/2011/10/testont/b#";

        OntClass A = ontModel.getOntClass(NSa + "A");
        assertTrue( ontModel.isInBaseModel(A));

        OntClass B = ontModel.getOntClass(NSb + "B");
        assertFalse( ontModel.isInBaseModel(B));

        assertTrue( ontModel.isInBaseModel(ontModel.createStatement(A, RDF.type, OWL.Class)));
        assertFalse( ontModel.isInBaseModel(ontModel.createStatement(B, RDF.type, OWL.Class)));

    }

    /** Test getting conclusions after loading imports */
    @Test
    public void testAddImports0() {
        OntModel base = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );

        base.createClass( NS + "A" );
        base.createClass( NS + "B" );

        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_MICRO_RULE_INF, base );

        OntClass a = m.getOntClass( NS + "A" );
        OntClass b = m.getOntClass( NS + "B" );

        // nothing is known about a and b yet
        assertFalse( a.hasSubClass( b ) );

        // add some ontology data
        OntModel imp = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        imp.add( b, RDFS.subClassOf, a );

        m.addSubModel( imp, true );
        assertTrue( a.hasSubClass( b ) );
    }

    @Test
    public void testAddImports1() {
        String ns = "http://jena.hpl.hp.com/2003/03/testont";
        OntModel base = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );

        OntDocumentManager odm = OntDocumentManager.getInstance();
        odm.addAltEntry( ns + "#a", "file:testing/ontology/testImport7/a.owl" );

        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_MICRO_RULE_INF, base );

        Ontology oo = base.createOntology( ns );
        oo.addImport( base.createResource( ns + "#a") );

        // nothing is known about a and b yet
        Resource a = m.getResource( ns + "#A" );
        Resource c = m.getResource( ns + "#C" );
        assertFalse( m.contains( c, RDFS.subClassOf, a ) );

        // when we load the imports, the odm must kick the reasoner with a rebind()
        m.getDocumentManager().loadImports( m );
        assertTrue( m.contains( c, RDFS.subClassOf, a ) );
    }

    /**
     * AddSubModel variant 2: base = no inf, import = no inf
     */
    @Test
    public void testaddSubModel0() {
        OntModel m0 = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        OntModel m1 = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );

        OntClass c = m1.createClass( NS + "c" );

        assertFalse( m0.containsResource( c ) );

        m0.addSubModel( m1 );
        assertTrue( m0.containsResource( c ) );

        m0.removeSubModel( m1 );
        assertFalse( m0.containsResource( c ) );
    }

    /**
     * AddSubModel variant 2: base = inf, import = no inf
     */
    @Test
    public void testaddSubModel1() {
        OntDocumentManager.getInstance().setProcessImports( false );
        OntDocumentManager.getInstance().addAltEntry( "http://www.w3.org/TR/2003/CR-owl-guide-20030818/wine",
        "file:testing/ontology/owl/Wine/wine.owl" );
        OntModel m0 = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
        OntModel m1 = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

        String namespace = "http://www.w3.org/TR/2003/CR-owl-guide-20030818/wine";
        String classURI = namespace + "#Wine";
        m1.read(namespace);
        OntClass c = m1.getOntClass(classURI);

        assertFalse(m0.containsResource(c));
        m0.addSubModel(m1);
        assertTrue(m0.containsResource(c));
        m0.removeSubModel(m1);
        assertFalse(m0.containsResource(c));
    }

    /**
     * Variant 3: base = no inf, import = inf
     */
    @Test
    public void testaddSubModel3() {
        OntModel m0 = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        OntModel m1 = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_RDFS_INF );

        OntClass c = m1.createClass( NS + "c" );

        assertFalse( m0.containsResource( c ) );

        m0.addSubModel( m1 );
        assertTrue( m0.containsResource( c ) );

        m0.removeSubModel( m1 );
        assertFalse( m0.containsResource( c ) );
    }

    /**
     * Variant 4: base = inf, import = inf
     */
    @Test
    public void testaddSubModel4() {
        OntModel m0 = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_RDFS_INF );
        OntModel m1 = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_RDFS_INF );

        OntClass c = m1.createClass( NS + "c" );

        assertFalse( m0.containsResource( c ) );

        m0.addSubModel( m1 );
        assertTrue( m0.containsResource( c ) );

        m0.removeSubModel( m1 );
        assertFalse( m0.containsResource( c ) );
    }

    /** Remove a sub model (imported model) */
    @Test
    public void testremoveSubModel0() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM, null );
        m.read( "file:testing/ontology/testImport3/a.owl" );

        assertEquals( 2, m.getSubGraphs().size() );

        for (Iterator<OntModel> it = m.listSubModels(); it.hasNext();) {
                m.removeSubModel( it.next() );
        }

        assertEquals( 0, m.getSubGraphs().size() );
    }

    /** Getting the deductions model of an OntModel
     * see also {@link TestRuleSystemBugs#testOntModelGetDeductions()}
     * <p>ijd: Feb 6th, 2008 - this test has been disabled for
     * the time being, since it is not correct as written. However,
     * I'm not removing or changing it just yet, since it is showing up
     * an infelicity in the rule engine that Dave will investigate
     * at some future date.</p>
     * */
    public void xxtestGetDeductionsModel0() {
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_MICRO_RULE_INF );
        OntClass a = m.createClass( NS + "A" );
        OntClass b = m.createClass( NS + "B" );
        OntClass c = m.createClass( NS + "C" );

        b.addSubClass( c );

        // we see the entailments only in the deductions model
        Model dm = m.getDeductionsModel();
        assertTrue( dm.contains( OWL.Nothing, RDFS.subClassOf, a ) );
        assertTrue( dm.contains( OWL.Nothing, RDFS.subClassOf, c ) );

        a.addSubClass( b );

        assertTrue( a.hasSubClass( c ));

        dm = m.getDeductionsModel();

        assertFalse( dm.contains( OWL.Nothing, RDFS.subClassOf, a ) );
        assertTrue( dm.contains( OWL.Nothing, RDFS.subClassOf, c ) );
    }

    /**
     * Test that using closed models in imports does not raise an exception
     */
    @Test
    public void testImportClosedModel() {
        String SOURCEA=
            "<rdf:RDF" +
            "    xmlns:rdf          ='http://www.w3.org/1999/02/22-rdf-syntax-ns#'" +
            "    xmlns:owl          ='http://www.w3.org/2002/07/owl#'" +
            "    xml:base           ='http://example.com/a#'" +
            ">" +
            "  <owl:Ontology>" +
            "          <owl:imports rdf:resource='http://example.com/b' />" +
            "  </owl:Ontology>" +
            "</rdf:RDF>";

        OntDocumentManager.getInstance().addAltEntry( "http://example.com/b", "file:testing/ontology/relativenames.rdf" );

        OntModel a0 = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        a0.read( new StringReader( SOURCEA ), null );
        long a0count = a0.size();

        // key step - close a model which is now in the ODM cache
        OntDocumentManager.getInstance().getModel( "http://example.com/b" ).close();

        // this line threw an exception before the bug was fixed
        OntModel a1 = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        a1.read( new StringReader( SOURCEA ), null );

        // for completeness, check that we have read the same contents
        assertEquals( a0count, a1.size(), "Models should be same size" );
    }

    /**
     * OntModel read should do content negotiation if no base URI is given
     */
    @Test
    public void testReadConneg0() {
        final boolean[] acceptHeaderSet = new boolean[] {false};

        // because ModelCom has private fields it references directly, we have to mock
        // a lot more pieces that I would prefer
        OntModel m = new OntModelImpl(OntModelSpec.OWL_MEM) {
            @Override
            protected Model readDelegate( String url ) {
                acceptHeaderSet[0] = true;
                return super.readDelegate( url );
            }

            /** Allow pseudo-conneg even on file: uri's */
            @Override
            public boolean ignoreFileURI( String url ) {
                return false;
            }
        };

        assertFalse( acceptHeaderSet[0] );
        m.read( "file:testing/ontology/owl/Property/test.rdf" );
        assertTrue( acceptHeaderSet[0] );

    }

    /** No conneg for file: uri's normally */
    @Test
    public void testReadConneg1() {
        final boolean[] acceptHeaderSet = new boolean[] {false};

        // because ModelCom has private fields it references directly, we have to mock
        // a lot more pieces that I would prefer
        OntModel m = new OntModelImpl(OntModelSpec.OWL_MEM) {
            @Override
            protected Model readDelegate( String url ) {
                acceptHeaderSet[0] = true;
                return super.readDelegate( url );
            }
        };

        assertFalse( acceptHeaderSet[0] );
        m.read( "file:testing/ontology/owl/Property/test.rdf" );
        assertFalse( acceptHeaderSet[0] );

    }

    /** With RDF/XML syntax specified, conneg */
    @Test
    public void testReadConneg2() {
        final boolean[] acceptHeaderSet = new boolean[] {false};

        // because ModelCom has private fields it references directly, we have to mock
        // a lot more pieces that I would prefer
        OntModel m = new OntModelImpl(OntModelSpec.OWL_MEM) {
            @Override
            protected Model readDelegate( String url, String lang ) {
                acceptHeaderSet[0] = true;
                return super.readDelegate( url, lang );
            }

            /** Allow pseudo-conneg even on file: uri's */
            @Override
            public boolean ignoreFileURI( String url ) {
                return false;
            }
        };

        assertFalse( acceptHeaderSet[0] );
        m.read( "file:testing/ontology/owl/Property/test.rdf", "RDF/XML" );
        assertTrue( acceptHeaderSet[0] );

    }

    /** With a base URI, no conneg */
    @Test
    public void testReadConneg3() {
        final boolean[] acceptHeaderSet = new boolean[] {false};

        // because ModelCom has private fields it references directly, we have to mock
        // a lot more pieces that I would prefer
        OntModel m = new OntModelImpl(OntModelSpec.OWL_MEM) {
            @Override
            protected Model readDelegate( String url, String lang ) {
                acceptHeaderSet[0] = true;
                return super.readDelegate( url, lang );
            }

            /** Allow pseudo-conneg even on file: uri's */
            @Override
            public boolean ignoreFileURI( String url ) {
                return false;
            }
        };

        assertFalse( acceptHeaderSet[0] );
        m.read( "file:testing/ontology/owl/Property/test.rdf", "http://foo.com", "RDF/XML" );
        assertFalse( acceptHeaderSet[0] );

    }

    // Internal implementation methods
    //////////////////////////////////

    /**
     * Answer true iff an iterator contains a given value.
     */
    private boolean iteratorContains( Iterator<?> i, Object x ) {
        boolean found = false;
        while (i.hasNext()) {
            found = i.next().equals( x ) || found;
        }
        return found;
    }

    //==============================================================================
    // Inner class definitions
    //==============================================================================

    protected class OWLProfileExt extends OWLProfile
    {
        public Map<Class<?>, SupportsCheck> getSupportsMap() {
            return getCheckTable();
        }
    }

    protected class OWLDLProfileExt extends OWLDLProfile
    {
        public Map<Class<?>, SupportsCheck> getSupportsMap() {
            return getCheckTable();
        }
    }

    protected class OWLLiteProfileExt extends OWLLiteProfile
    {
        public Map<Class<?>, SupportsCheck> getSupportsMap() {
            return getCheckTable();
        }
    }
}
