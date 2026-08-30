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

package org.apache.jena.ontology.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.test.JenaTestLib;

/**
 * <p>
 * Generic test case for ontology unit testing.
 * </p>
 * <p>
 * JUnit6 counterpart of {@link OntTestBase}. The JUnit3 original was a
 * {@code TestSuite} that built one {@code TestCase} per entry of
 * {@link #getTests}; here the same array becomes one {@link DynamicTest} per
 * entry, so the test count is unchanged. {@code OntTestCase} keeps the
 * constructor and {@code ontTest} contract of the original, so sub-classes
 * carry over unaltered.
 * </p>
 */
@SuppressWarnings("removal")
public abstract class OntTestBase
{
    // Constants
    //////////////////////////////////

    public static final String BASE = "http://jena.hpl.hp.com/testing/ontology";
    public static final String NS = BASE + "#";

    static { JenaTestLib.setup(); }

    // External signature methods
    //////////////////////////////////

    /**
     * One dynamic test per entry of {@link #getTests}. Each entry runs the three
     * language profiles internally, exactly as {@code OntTestCase.runTest()} did
     * under JUnit3, so one entry remains one test.
     */
    @TestFactory
    public Stream<DynamicTest> ontTests() {
        OntTestCase[] tc = getTests();
        if (tc == null)
            return Stream.empty();
        return Arrays.stream( tc )
                     .map( t -> DynamicTest.dynamicTest( t.getName(), () -> { t.setUp(); t.runTest(); } ) );
    }

    // Internal implementation methods
    //////////////////////////////////

    /** Return the array of tests for the suite */
    protected  OntTestCase[] getTests() {
        return null;
    }

    //==============================================================================
    // Inner class definitions
    //==============================================================================

    protected abstract class OntTestCase
    {
        protected boolean m_inOWL;
        protected boolean m_inOWLLite;
        protected boolean m_inRDFS;
        protected String m_langElement;
        protected String m_name;
        protected boolean m_owlLang = true;
        protected boolean m_owlLiteLang = false;
        protected boolean m_rdfsLang = false;

        public OntTestCase( String langElement , boolean inOWL , boolean inOWLLite , boolean inRDFS  ) {
            m_name = "Ontology API test " + langElement;
            m_langElement = langElement;
            m_inOWL = inOWL;
            m_inOWLLite = inOWLLite;
            m_inRDFS = inRDFS;
        }

        /** The name this case ran under in the JUnit3 suite. */
        public String getName() {
            return m_name;
        }

        public void runTest()
            throws Exception
        {
            // we don't want inferencing for these unit tests
            runTest( getOntModel( OntModelSpec.OWL_MEM ), m_inOWL );

            m_owlLiteLang = true;

            runTest( getOntModel( OntModelSpec.OWL_LITE_MEM ), m_inOWLLite );

            // now RDFS

            m_rdfsLang = true;
            runTest( getOntModel( OntModelSpec.RDFS_MEM ), m_inRDFS);
        }

        /** Test execution worker */
        protected void runTest( OntModel m, boolean inModel )
            throws Exception
        {
            boolean profileEx = false;

            try {
                ontTest( m );
            }
            catch (ProfileException e) {
                profileEx = true;
            }

            assertEquals( inModel, !profileEx, "language element " + m_langElement + " was " + (inModel ? "" : "not") + " expected in model " + m.getProfile().getLabel() );
        }

        /** Does the work in the test sub-class */
        protected abstract void ontTest( OntModel m ) throws Exception;

        /** Test that an iterator delivers the expected values */
        protected void iteratorTest( Iterator<?> i, Object[] expected ) {
            OntTestUtil.assertIteratorValues( i, expected );
        }

        public void setUp() {
            // ensure the ont doc manager is in a consistent state
            OntDocumentManager.getInstance().reset( true );
        }

        protected boolean owlFull() {
            return m_owlLang && !m_owlLiteLang;
        }

        /** Answer true if an iterator contains a given value */
        protected boolean iteratorContains( Iterator<?> i, Object target ) {
            boolean found = false;
            while (i.hasNext()) {
                found = i.next().equals( target ) || found;
            }
            return found;
        }

        /** Create the model, and call the model add axioms hook before returning */
        protected OntModel getOntModel( OntModelSpec spec ) {
            OntModel m = ModelFactory.createOntologyModel( spec );
            addAxioms( m );
            return m;
        }

        /** Add setup axioms to a new empty OntModel */
        protected void addAxioms( OntModel m ) {
            // default is no-op
        }
    }
}
