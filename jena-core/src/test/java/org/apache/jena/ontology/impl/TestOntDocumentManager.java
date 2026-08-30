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

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntDocumentManager.ReadFailureHandler;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.ProfileRegistry;
import org.apache.jena.ontology.models.ModelMaker;
import org.apache.jena.ontology.models.ModelMakerImpl;
import org.apache.jena.ontology.models.SimpleGraphMaker;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.RDFReaderFImpl;
import org.apache.jena.test.X_RDFReaderF;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.OntDocManagerVocab;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.jena.test.JenaTestLib;

/**
 * <p>
 * Unit tests for document manager
 * </p>
 */
@SuppressWarnings("removal")
public class TestOntDocumentManager
{

    static { JenaTestLib.setup(); }

    static { RDFReaderFImpl.alternative(new X_RDFReaderF()); }

    // Constants
    //////////////////////////////////

    private static Boolean F = Boolean.FALSE;
    private static Boolean T = Boolean.TRUE;

    // Static variables
    //////////////////////////////////

    /** Logger for this class */
    private static Logger log = LoggerFactory.getLogger( TestOntDocumentManager.class );

    public static final Integer cnt( int x ) {return x;}

    /* Data for various combinations of test import conditions */
    public static Object[][] s_testData = new Object[][] {
        // directory to look in             marker count        imports     Filemanager config path (null = default)
        {  "testing/ontology/testImport1",  cnt(1),             T,          null },
        {  "testing/ontology/testImport2",  cnt(2),             T,          null },
        {  "testing/ontology/testImport2",  cnt(1),             F,          null },
        {  "testing/ontology/testImport3",  cnt(3),             T,          null },
        {  "testing/ontology/testImport4",  cnt(2),             T,          null },
        {  "testing/ontology/testImport5",  cnt(2),             T,          "file:testing/ontology/testImport5/ont-policy.rdf" }
    };

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

        // forget any cached models in the model spec
        // TODO remove this once we rationalise modelmakers in the OntModel code
        Set<String> modelNames = new HashSet<>();
        ModelMaker memMaker = OntModelSpec.OWL_MEM.getImportModelMaker();
        for (Iterator<String> i = memMaker.listModels(); i.hasNext(); ) {
            modelNames.add( i.next() );
        }

        for ( String mn : modelNames )
        {
            memMaker.removeModel( mn );
        }
    }

    @Test
    public void testConstruct0() {
        OntDocumentManager m = new OntDocumentManager();
        assertNotNull( m );
        assertEquals( m.getMetadataSearchPath(), OntDocumentManager.DEFAULT_METADATA_PATH );
    }

    @Test
    public void testConstruct1() {
        OntDocumentManager mgr = new OntDocumentManager( "" );
        assertTrue( !mgr.listDocuments().hasNext(), "Should be no specification loaded" );
    }

    @Test
    public void testConstruct2() {
        // make sure we don't fail on null
        OntDocumentManager mgr = new OntDocumentManager( (String) null );
        assertTrue( !mgr.listDocuments().hasNext(), "Should be no specification loaded" );
    }

    @Test
    public void testConstruct3() {
        Model m = ModelFactory.createDefaultModel();
        Resource r = m.createResource();
        r.addProperty( RDF.type, OntDocManagerVocab.OntologySpec );
        r.addProperty( OntDocManagerVocab.publicURI, m.createResource("http://example.com/foo") );
        r.addProperty( OntDocManagerVocab.altURL, m.createResource("file:local.rdf") );

        OntDocumentManager mgr = new OntDocumentManager( m );
        assertEquals( "file:local.rdf", mgr.doAltURLMapping( "http://example.com/foo" ), "cache URL not correct");
    }

    @Test
    public void testInitialisation() {
        OntDocumentManager mgr = new OntDocumentManager( "ont-policy-test.rdf" );

        assertTrue( mgr.listDocuments().hasNext(), "Should be at least one specification loaded" );
        assertNotNull( mgr.doAltURLMapping( "http://www.w3.org/2002/07/owl" ), "cache URL for owl should not be null");
        assertEquals( "file:vocabularies/owl.owl", mgr.doAltURLMapping( "http://www.w3.org/2002/07/owl" ), "cache URL for owl not correct");
    }

    @Test
    public void testGetInstance() {
        OntDocumentManager odm = OntDocumentManager.getInstance();
        assertNotNull( odm );

        OntDocumentManager odm2 = OntDocumentManager.getInstance();
        assertSame( odm, odm2 );
    }

    @Test
    public void testSetMetadataSearchPath() {
        OntDocumentManager odm = new OntDocumentManager( "ont-policy-test.rdf" );
        assertEquals( "ont-policy-test.rdf", odm.getMetadataSearchPath() );
        assertTrue( odm.listDocuments().hasNext() );
        assertEquals( "ont-policy-test.rdf", odm.getLoadedPolicyURL() );

        odm.setMetadataSearchPath( "file:notexist.rdf", false );
        assertTrue( odm.listDocuments().hasNext() );
        assertNull( odm.getLoadedPolicyURL() );

        odm.setMetadataSearchPath( "file:notexist.rdf", true );
        assertFalse( odm.listDocuments().hasNext() );
        assertNull( odm.getLoadedPolicyURL() );

        odm.setMetadataSearchPath( "ont-policy-test.rdf", false );
        assertTrue( odm.listDocuments().hasNext() );
        assertEquals( "ont-policy-test.rdf", odm.getLoadedPolicyURL() );
    }

    @Test
    public void testConfigure0() {
        Model m = ModelFactory.createDefaultModel();
        Resource r = m.createResource();
        r.addProperty( RDF.type, OntDocManagerVocab.OntologySpec );
        r.addProperty( OntDocManagerVocab.publicURI, m.createResource("http://example.com/foo") );
        r.addProperty( OntDocManagerVocab.altURL, m.createResource("file:local.rdf") );

        OntDocumentManager odm = new OntDocumentManager( "ont-policy-test.rdf" );
        OntTestUtil.assertIteratorLength( odm.listDocuments(), 3 );

        odm.configure( m, false );
        OntTestUtil.assertIteratorLength( odm.listDocuments(), 4 );
    }

    @Test
    public void testConfigure1() {
        Model m = ModelFactory.createDefaultModel();
        Resource r = m.createResource();
        r.addProperty( RDF.type, OntDocManagerVocab.OntologySpec );
        r.addProperty( OntDocManagerVocab.publicURI, m.createResource("http://example.com/foo") );
        r.addProperty( OntDocManagerVocab.altURL, m.createResource("file:local.rdf") );

        OntDocumentManager odm = new OntDocumentManager( "ont-policy-test.rdf" );
        OntTestUtil.assertIteratorLength( odm.listDocuments(), 3 );

        odm.configure( m );
        OntTestUtil.assertIteratorLength( odm.listDocuments(), 1 );
    }

    @Test
    public void testConfigure2() {
        // create a simple policy
        Model m = ModelFactory.createDefaultModel();
        Resource policy = m.createResource();
        m.add( policy, RDF.type, OntDocManagerVocab.DocumentManagerPolicy );
        m.addLiteral( policy, OntDocManagerVocab.cacheModels, false );

        OntDocumentManager mgr = new OntDocumentManager( (String) null );
        assertTrue( mgr.getCacheModels() );
        mgr.configure( m );
        assertFalse( mgr.getCacheModels(), "Docmgr configure() should have updated cache models flag" );
    }

    @Test
    public void testReset() {
        OntDocumentManager mgr = new OntDocumentManager( (String) null );

        assertTrue( mgr.getProcessImports() );
        mgr.setProcessImports( false );
        assertFalse( mgr.getProcessImports() );
        mgr.reset();
        assertTrue( mgr.getProcessImports() );

        mgr.setMetadataSearchPath( "file:foo.xml", true );
        assertEquals( "file:foo.xml", mgr.getMetadataSearchPath() );
        mgr.reset();
        assertEquals( "file:foo.xml", mgr.getMetadataSearchPath() );

        assertTrue( mgr.getCacheModels() );
        mgr.setCacheModels(false );
        assertFalse( mgr.getCacheModels() );
        mgr.reset();
        assertTrue( mgr.getCacheModels() );
    }

    @Test
    public void testDoAltMapping() {
        OntDocumentManager odm = new OntDocumentManager( "ont-policy-test.rdf" );
        assertEquals( "file:vocabularies/owl.owl", odm.doAltURLMapping( "http://www.w3.org/2002/07/owl" ));
        assertEquals( "http://example.com/nocache", odm.doAltURLMapping( "http://example.com/nocache" ));
    }

    @Test
    public void testAddModel0() {
        OntDocumentManager odm = OntDocumentManager.getInstance();
        Model m = ModelFactory.createDefaultModel();
        String uri = "http://example.com/test#m";
        assertNull( odm.getModel( uri ));
        odm.addModel( uri, m );
        assertSame( m, odm.getModel(uri));
    }

    @Test
    public void testAddModel1() {
        OntDocumentManager odm = OntDocumentManager.getInstance();
        Model m0 = ModelFactory.createDefaultModel();
        Model m1 = ModelFactory.createDefaultModel();
        String uri = "http://example.com/test#m";
        assertNull( odm.getModel( uri ));
        odm.addModel( uri, m0 );

        // add duplicate with no replace
        odm.addModel( uri, m1 );
        assertSame( m0, odm.getModel(uri));

        // add duplicate with replace
        odm.addModel( uri, m1, true );
        assertSame( m1, odm.getModel(uri));
    }

    @Test
    public void testClearCache0() {
        OntDocumentManager odm = OntDocumentManager.getInstance();
        Model m = ModelFactory.createDefaultModel();
        String uri = "http://example.com/test#m";
        assertNull( odm.getModel( uri ));
        odm.addModel( uri, m );
        odm.clearCache();
        assertSame( null, odm.getModel(uri));
    }

    /**
     * Ensure that sub-model imports are not re-used after clearing the cache.
     */
    @Test
    public void testClearCache1() {
        OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
        spec.getDocumentManager().reset();
        spec.getDocumentManager().setCacheModels(false);
        spec.getDocumentManager().addAltEntry( "http://incubator.apache.org/jena/2011/10/testont/a",
                                               "file:testing/ontology/testImport8/a.owl" );

        OntModel m0 = ModelFactory.createOntologyModel(spec, null);
        m0.read( "http://incubator.apache.org/jena/2011/10/testont/a" );
        Model subModel0 = m0.listSubModels().next();
        long count0 = subModel0.size();

        OntClass ontClass = m0.getOntClass( "http://incubator.apache.org/jena/2011/10/testont/a#A" );
        subModel0.add( ontClass, RDF.type, OWL.DeprecatedClass );
        assertEquals( count0 + 1, subModel0.size() );

        // TODO this workaround to be removed
        SimpleGraphMaker sgm = (SimpleGraphMaker) ((ModelMakerImpl) spec.getImportModelMaker()).getGraphMaker();
        List<String> toGo = new ArrayList<>();
        for (Iterator<String> i = sgm.listGraphs(); i.hasNext(); toGo.add( i.next() )) {/**/}
        for (Iterator<String> i = toGo.iterator(); i.hasNext(); sgm.removeGraph( i.next() )) {/**/}
        spec.getDocumentManager().clearCache();

        OntModel m1 = ModelFactory.createOntologyModel(spec, null);
        m1.read( "http://incubator.apache.org/jena/2011/10/testont/a" );
        Model subModel1 = m1.listSubModels().next();
        assertNotSame( m1, m0 );
        assertNotSame( subModel1, subModel0 );
        assertEquals( count0, subModel1.size() );
    }

    @Test
    public void testForget() {
        OntDocumentManager odm = new OntDocumentManager( "ont-policy-test.rdf" );
        assertEquals( "file:vocabularies/owl.owl", odm.doAltURLMapping( "http://www.w3.org/2002/07/owl" ) );
        OntModel m = ModelFactory.createOntologyModel();
        odm.addModel( "http://www.w3.org/2002/07/owl#", m );
        assertNotNull( odm.getModel( "http://www.w3.org/2002/07/owl#" ));

        odm.forget( "http://www.w3.org/2002/07/owl#" );
        odm.forget( "http://www.w3.org/2002/07/owl" );

        assertEquals( "http://www.w3.org/2002/07/owl", odm.doAltURLMapping( "http://www.w3.org/2002/07/owl" ) );
        assertNull( odm.getModel( "http://www.w3.org/2002/07/owl#" ));
    }

    @Test
    public void testGetOntology() {
        OntDocumentManager odm = new OntDocumentManager( "ont-policy-test.rdf" );
        OntModel m = odm.getOntology( "http://www.w3.org/2002/07/owl", OntModelSpec.OWL_MEM );
        assertNotNull( m );
        assertSame( odm, m.getDocumentManager() );
        OntModel m1 = odm.getOntology( "http://www.w3.org/2002/07/owl", OntModelSpec.OWL_MEM );
        assertSame( m, m1 );
    }

    @Test
    public void testProcessImports() {
        OntDocumentManager odm = new OntDocumentManager( "ont-policy-test.rdf" );
        assertTrue( odm.getProcessImports() );
        odm.setProcessImports( false );
        assertFalse( odm.getProcessImports() );
    }

    @Test
    public void testCacheModels() {
        OntDocumentManager odm = new OntDocumentManager( "ont-policy-test.rdf" );
        assertTrue( odm.getCacheModels() );
        odm.setCacheModels( false );
        assertFalse( odm.getCacheModels() );
    }

    @Test
    public void testManualAssociation() {
        OntDocumentManager odm = new OntDocumentManager( (String) null );

        odm.addAltEntry( "http://www.w3.org/2002/07/owl", "file:foo.bar" );
        assertEquals( "file:foo.bar", odm.doAltURLMapping( "http://www.w3.org/2002/07/owl" ), "Failed to retrieve cache location" );
    }

    @Test
    public void testRelativeNames() {
        OntModel m = ModelFactory.createOntologyModel();
        m.getDocumentManager().addAltEntry(
            "http://jena.hpl.hp.com/testing/ontology/relativenames",
            "file:testing/ontology/relativenames.rdf");

        m.read("http://jena.hpl.hp.com/testing/ontology/relativenames");
        assertTrue( m.getResource("http://jena.hpl.hp.com/testing/ontology/relativenames#A").canAs(OntClass.class));
        assertFalse( m.getResource("file:testing/ontology/relativenames.rdf#A").canAs(OntClass.class));
    }

    @Test
    public void testIgnoreImport() {
        OntDocumentManager odm = new OntDocumentManager();
        OntTestUtil.assertIteratorLength( odm.listIgnoredImports(), 0 );

        odm.addIgnoreImport( "file:testing/ontology/testImport3/c.owl" );
        OntTestUtil.assertIteratorLength( odm.listIgnoredImports(), 1 );
        assertTrue( odm.ignoringImport( "file:testing/ontology/testImport3/c.owl"));
        assertFalse( odm.ignoringImport( "file:testing/ontology/foo.owl"));

        OntModelSpec spec = new OntModelSpec( null, odm, null, ProfileRegistry.OWL_LANG );
        OntModel m = ModelFactory.createOntologyModel( spec, null );
        assertNotNull( m, "Ontology model should not be null" );

        m.read( "file:testing/ontology/testImport3/a.owl" );
        assertEquals( 2, countMarkers( m ), "Marker count not correct");

        odm.removeIgnoreImport( "file:testing/ontology/testImport3/c.owl" );
        OntTestUtil.assertIteratorLength( odm.listIgnoredImports(), 0 );
        assertFalse( odm.ignoringImport( "file:testing/ontology/testImport3/c.owl"));
    }

    /** Simple case: a imports b, b imports c, remove c */
    @Test
    public void testUnloadImport1() {
        OntModel m = ModelFactory.createOntologyModel();
        m.read( "file:testing/ontology/testImport3/a.owl" );
        assertEquals( 3, countMarkers( m ), "Marker count not correct" );

        assertTrue( m.hasLoadedImport( "file:testing/ontology/testImport3/c.owl" ), "c should be imported" );
        m.getDocumentManager().unloadImport( m, "file:testing/ontology/testImport3/c.owl" );
        assertEquals( 2, countMarkers( m ), "Marker count not correct" );
        assertFalse( m.hasLoadedImport( "file:testing/ontology/testImport3/c.owl" ), "c should not be imported" );
    }

    /** case 2: a imports b, b imports c, remove b */
    @Test
    public void testUnloadImport2() {
        OntModel m = ModelFactory.createOntologyModel();
        m.read( "file:testing/ontology/testImport3/a.owl" );
        assertEquals( 3, countMarkers( m ), "Marker count not correct" );

        assertTrue( m.hasLoadedImport( "file:testing/ontology/testImport3/c.owl" ), "c should be imported" );
        assertTrue( m.hasLoadedImport( "file:testing/ontology/testImport3/b.owl" ), "b should be imported" );
        m.getDocumentManager().unloadImport( m, "file:testing/ontology/testImport3/b.owl" );
        assertEquals( 1, countMarkers( m ), "Marker count not correct" );
        assertFalse( m.hasLoadedImport( "file:testing/ontology/testImport3/c.owl" ), "c should not be imported" );
        assertFalse( m.hasLoadedImport( "file:testing/ontology/testImport3/b.owl" ), "b should not be imported" );
    }

    /** case 3: a imports b, b imports c, a imports d, d imports c, remove b */
    @Test
    public void testUnloadImport3() {
        OntModel m = ModelFactory.createOntologyModel();
        m.read( "file:testing/ontology/testImport6/a.owl" );
        assertEquals( 4, countMarkers( m ), "Marker count not correct" );

        assertTrue( m.hasLoadedImport( "file:testing/ontology/testImport6/c.owl" ), "c should be imported" );
        assertTrue( m.hasLoadedImport( "file:testing/ontology/testImport6/b.owl" ), "b should be imported" );
        assertTrue( m.hasLoadedImport( "file:testing/ontology/testImport6/d.owl" ), "d should be imported" );
        m.getDocumentManager().unloadImport( m, "file:testing/ontology/testImport6/b.owl" );
        assertEquals( 3, countMarkers( m ), "Marker count not correct" );
        assertTrue( m.hasLoadedImport( "file:testing/ontology/testImport6/c.owl" ), "c should be imported" );
        assertTrue( m.hasLoadedImport( "file:testing/ontology/testImport6/d.owl" ), "d should be imported" );
        assertFalse( m.hasLoadedImport( "file:testing/ontology/testImport6/b.owl" ), "b should not be imported" );
    }

    @Test
    public void testDynamicImports1() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource a = m.getResource( "file:testing/ontology/testImport3/a.owl" );
        Resource b = m.getResource( "file:testing/ontology/testImport3/b.owl" );
        m.add( a, m.getProfile().IMPORTS(), b );

        // not dymamically imported by default
        assertEquals( 0, countMarkers( m ), "Marker count not correct" );

        assertFalse( m.hasLoadedImport( "file:testing/ontology/testImport3/c.owl" ), "c should not be imported" );
        assertFalse( m.hasLoadedImport( "file:testing/ontology/testImport3/b.owl" ), "b should not be imported" );
    }

    @Test
    public void testDynamicImports2() {
        OntModel m = ModelFactory.createOntologyModel();
        Resource a = m.getResource( "file:testing/ontology/testImport3/a.owl" );
        Resource b = m.getResource( "file:testing/ontology/testImport3/b.owl" );

        m.setDynamicImports( true );

        m.add( a, m.getProfile().IMPORTS(), b );

        // dynamically imported
        assertEquals( 2, countMarkers( m ), "Marker count not correct" );

        assertTrue( m.hasLoadedImport( "file:testing/ontology/testImport3/c.owl" ), "c should be imported" );
        assertTrue( m.hasLoadedImport( "file:testing/ontology/testImport3/b.owl" ), "b should be imported" );
    }

    @Test
    public void testDynamicImports3() {
        OntModel m = ModelFactory.createOntologyModel();
        m.read( "file:testing/ontology/testImport3/a.owl" );
        assertEquals( 3, countMarkers( m ), "Marker count not correct" );

        assertTrue( m.hasLoadedImport( "file:testing/ontology/testImport3/c.owl" ), "c should be imported" );
        assertTrue( m.hasLoadedImport( "file:testing/ontology/testImport3/b.owl" ), "b should be imported" );

        m.setDynamicImports( true );

        Resource a = m.getResource( OntResolve.resolve("file:testing/ontology/testImport3/a.owl") );
        Resource b = m.getResource( OntResolve.resolve("file:testing/ontology/testImport3/b.owl") );
        m.remove( m.createStatement( a, m.getProfile().IMPORTS(), b ) );

        assertEquals( 1, countMarkers( m ), "Marker count not correct" );
        assertFalse( m.hasLoadedImport( "file:testing/ontology/testImport3/c.owl" ), "c should not be imported" );
        assertFalse( m.hasLoadedImport( "file:testing/ontology/testImport3/b.owl" ), "b should not be imported" );
    }

    @Test
    public void testSearchPath() {
        OntDocumentManager o1 = new OntDocumentManager( "ont-policy-test.rdf" );
        assertEquals( "ont-policy-test.rdf", o1.getLoadedPolicyURL(), "Did not return correct loaded search path" );

        OntDocumentManager o2 = new OntDocumentManager( "ont-policy-test.notexist.rdf;ont-policy-test.rdf" );
        assertEquals( "ont-policy-test.rdf", o2.getLoadedPolicyURL(), "Did not return correct loaded search path" );

        OntDocumentManager o3 = new OntDocumentManager( (String) null );
        assertNull( o3.getLoadedPolicyURL(), "Most recent policy should be null" );

        o3.setMetadataSearchPath( "ont-policy-test.rdf", true );
        assertEquals( "ont-policy-test.rdf", o2.getLoadedPolicyURL(), "Did not return correct loaded search path" );

        o3.setMetadataSearchPath( "ont-policy-test.notexist.rdf", true );
        assertNull( o3.getLoadedPolicyURL(), "Most recent policy should be null" );
    }

    @Test
    public void testReadFailHandler0() {
        OntDocumentManager o1 = new OntDocumentManager( "ont-policy-test.rdf" );
        assertNull( o1.getReadFailureHandler() );

        OntDocumentManager.ReadFailureHandler rfh = new OntDocumentManager.ReadFailureHandler() {
            @Override
            public void handleFailedRead( String url, Model model, Exception e ) {/**/}};

        o1.setReadFailureHandler( rfh );
        assertSame( rfh, o1.getReadFailureHandler() );
    }

    /**
     * Test the read-fail handler hook.
     * Test updated to use the domain "example.invalid", not example.com, since .invalid
     * is designed for domain names that are sure to be invalid. See
     * <a href="http://tools.ietf.org/html/rfc2606#section-2">tools.ietf.org/html/rfc2606#section-2</a>
     */
    @Test
    public void testReadFailHandler1() {
        OntDocumentManager o1 = new OntDocumentManager( "ont-policy-test.rdf" );

        TestFailHandler rfh = new TestFailHandler();
        o1.setReadFailureHandler( rfh );

        // trigger the odm to read a non-existant source
        String source = "@prefix owl: <http://www.w3.org/2002/07/owl#> . <> a owl:Ontology; owl:imports <http://example.invalid/not/exist>. ";
        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_MEM );
        spec.setDocumentManager(  o1 );
        OntModel m = ModelFactory.createOntologyModel( spec );
        m.read( new StringReader( source ), "http://example.com/foo#", "N3" );

        assertTrue( rfh.m_seen );
    }

    @Test
    public void testReadHook0() {
        TestReadHook rh = new TestReadHook( false );
        OntDocumentManager o1 = new OntDocumentManager( "ont-policy-test.rdf" );
        o1.setReadHook( rh );
        o1.reset();

        String source =
                "@prefix owl: <http://www.w3.org/2002/07/owl#> ."
                + " <> a owl:Ontology;"
                + " owl:imports <file:testing/ontology/testImport3/a.owl>. ";

        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_MEM );
        spec.setDocumentManager(  o1 );
        OntModel m = ModelFactory.createOntologyModel( spec );
        m.read( new StringReader( source ), "http://example.com/foo#", "N3" );

        assertEquals( 3, rh.m_before, "Wrong number of calls to before load hook" );
        assertEquals( 3, rh.m_after, "Wrong number of calls to after load hook" );
    }

    @Test
    public void testReadHook1() {
        TestReadHook rh = new TestReadHook( true );
        OntDocumentManager o1 = new OntDocumentManager( "ont-policy-test.rdf" );
        o1.setReadHook( rh );
        o1.reset();

        String source = "@prefix owl: <http://www.w3.org/2002/07/owl#> . <> a owl:Ontology; owl:imports <file:testing/ontology/testImport3/a.owl>. ";

        OntModelSpec spec = new OntModelSpec( OntModelSpec.OWL_MEM );
        spec.setDocumentManager(  o1 );
        OntModel m = ModelFactory.createOntologyModel( spec );
        m.read( new StringReader( source ), "http://example.com/foo#", "N3" );

        assertEquals( 1, rh.m_before, "Wrong number of calls to before load hook" );
        assertEquals( 1, rh.m_after, "Wrong number of calls to after load hook" );
    }

    /* count the number of marker statements in the combined model */
    public static int countMarkers( Model m ) {
        int count = 0;

        Resource marker = m.getResource( "http://jena.hpl.hp.com/2003/03/testont#Marker" );
        for (StmtIterator i = m.listStatements( null, RDF.type, marker ); i.hasNext();  ) {
            count++;
            i.next();
        }

        return count;
    }

    /**
     * One dynamic test per row of {@code s_testData}. The JUnit3 suite() added
     * the fixed test cases (now ordinary @Test methods) plus one
     * DocManagerImportTest per row, so one row remains one test.
     */
    @TestFactory
    public Stream<DynamicTest> docManagerImportTests() {
        return Stream.of( s_testData )
                     .map( row -> new DocManagerImportTest( (String) row[0], ((Integer) row[1]).intValue(),
                                                            ((Boolean) row[2]).booleanValue(), (String) row[3] ) )
                     .map( tc -> DynamicTest.dynamicTest( tc.getName(), tc::runTest ) );
    }

    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

    /**
     * Document manager imports test case. Each test case starts with a root model (always a.owl in some
     * directory), and loads the model. Depending on the model contents, and the settings of the doc
     * manager, other models will be loaded. Each model is set to contain a fixed number of marker
     * statements of the form:
     * <pre>
     *   <Marker rdf:ID="a0" />
     * </pre>
     * the test for having correctly loaded the models is to count the markers and compare to the predicted
     * total.
     */
    static class DocManagerImportTest
    {
        String m_name;
        String m_dir;
        int m_count;
        String m_path;
        boolean m_processImports;

        /* constuctor */
        DocManagerImportTest( String dir, int count, boolean processImports, String path ) {
            m_name = dir;
            m_dir = dir;
            m_count = count;
            m_path = path;
            m_processImports = processImports;
        }

        // external contract methods

        /** The name this case ran under in the JUnit3 suite. */
        public String getName() {
            return m_name;
        }

        public void runTest() {
            OntDocumentManager dm = new OntDocumentManager();

            // adjust the doc manager properties according to the test setup
            dm.setProcessImports( m_processImports );
            if (m_path != null) {
                dm.setMetadataSearchPath( m_path, true );
            }

            // now load the model - we always start from a.owl in the given directory
            OntModelSpec spec = new OntModelSpec( null, dm, null, ProfileRegistry.OWL_LANG );
            OntModel m = ModelFactory.createOntologyModel( spec, null );
            assertNotNull( m, "Ontology model should not be null" );

            String filename = "file:" + m_dir + "/a.owl";

            try {
                m.read(filename);
            } catch (Throwable ex) {
                m.read(filename);
            }
            assertEquals( m_count, countMarkers( m ), "Marker count not correct: "+filename);
        }
    }

    static class TestFailHandler
        implements ReadFailureHandler
    {
        public boolean m_seen = false;
        @Override
        public void handleFailedRead( String url, Model model, Exception e ) {
            m_seen = true;
            log.debug( "Seeing failed read of " + url, e );
        }

    }

    static class TestReadHook
        implements OntDocumentManager.ReadHook
    {
        private int m_before = 0;
        private int m_after = 0;
        private boolean m_renaming = false;

        TestReadHook( boolean renaming ) {
            m_renaming = renaming;
        }

        @Override
        public void afterRead( Model model, String source, OntDocumentManager odm ) {
            m_after++;
        }

        @Override
        public String beforeRead( Model model, String source, OntDocumentManager odm ) {
            if (m_renaming) {
                // local rewrite of the source file, which could be used e.g. to
                // get the source from a .jar file
                m_before++;
                return "file:testing/ontology/testImport3/c.owl";
            }
            else {
                m_before++;
                return source;
            }
        }

    }
}
