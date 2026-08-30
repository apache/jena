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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.jena.test.JenaTestLib;

/**
 * <p>
 * Unit-tests for frame-like views of OWL and RDFS-classes, especially listDeclaredProperties
 * </p>
 */
@SuppressWarnings("removal")
public class TestFrameView
{

    static { JenaTestLib.setup(); }

    // Constants
    //////////////////////////////////

    public static final String BASE = "http://jena.hpl.hp.com/testing/ontology";
    public static final String NS = BASE + "#";

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    OntModel mInf;
    OntModel mNoInf;

    OntClass infA;
    OntClass infB;
    OntClass infC;

    OntClass noinfA;
    OntClass noinfB;
    OntClass noinfC;

    ObjectProperty noinfG;
    ObjectProperty infG;

    ObjectProperty noinfPa;
    ObjectProperty noinfPb;
    ObjectProperty noinfPc;

    ObjectProperty infPa;
    ObjectProperty infPb;
    ObjectProperty infPc;

    ObjectProperty noinfQa;
    ObjectProperty noinfQb;

    ObjectProperty infQa;
    ObjectProperty infQb;

    OntClass infAnn;
    OntClass noinfAnn;
    AnnotationProperty infPann;
    AnnotationProperty noinfPann;

    OntClass infUnion1;
    OntClass infUnion2;
    OntClass noinfUnion1;
    OntClass noinfUnion2;
    ObjectProperty infPunion;
    ObjectProperty noinfPunion;

    OntClass infIntersect1;
    OntClass infIntersect2;
    OntClass noinfIntersect1;
    OntClass noinfIntersect2;
    ObjectProperty infPintersect;
    ObjectProperty noinfPintersect;

    // Constructors
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    @BeforeEach
    public void setUp() {
        OntDocumentManager.getInstance().reset();
        OntDocumentManager.getInstance().clearCache();
        mNoInf = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        mNoInf.read( "file:testing/ontology/owl/list-syntax/test-ldp.rdf" );
        //mInf = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_RULE_INF );
        mInf = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_MICRO_RULE_INF);
        mInf.read( "file:testing/ontology/owl/list-syntax/test-ldp.rdf" );

        infA = mInf.getOntClass( NS + "A" );
        infB = mInf.getOntClass( NS + "B" );
        infC = mInf.getOntClass( NS + "C" );

        noinfA = mNoInf.getOntClass( NS + "A" );
        noinfB = mNoInf.getOntClass( NS + "B" );
        noinfC = mNoInf.getOntClass( NS + "C" );

        noinfG = mNoInf.getObjectProperty( NS + "global" );
        infG = mInf.getObjectProperty( NS + "global" );

        noinfPa = mNoInf.getObjectProperty( NS + "pA" );
        noinfPb = mNoInf.getObjectProperty( NS + "pB" );
        noinfPc = mNoInf.getObjectProperty( NS + "pC" );

        infPa = mInf.getObjectProperty( NS + "pA" );
        infPb = mInf.getObjectProperty( NS + "pB" );
        infPc = mInf.getObjectProperty( NS + "pC" );

        noinfQa = mNoInf.getObjectProperty( NS + "qA" );
        noinfQb = mNoInf.getObjectProperty( NS + "qB" );

        infQa = mInf.getObjectProperty( NS + "qA" );
        infQb = mInf.getObjectProperty( NS + "qB" );

        infAnn = mInf.getOntClass( NS + "HasAnn" );
        noinfAnn = mNoInf.getOntClass( NS + "HasAnn" );
        infPann = mInf.getAnnotationProperty( NS + "ann" );
        noinfPann = mNoInf.getAnnotationProperty( NS + "ann" );

        infUnion1 = mInf.getOntClass( NS + "Union1" );
        infUnion2 = mInf.getOntClass( NS + "Union2" );
        noinfUnion1 = mNoInf.getOntClass( NS + "Union1" );
        noinfUnion2 = mNoInf.getOntClass( NS + "Union2" );
        infPunion = mInf.getObjectProperty( NS + "unionP" );
        noinfPunion = mNoInf.getObjectProperty( NS + "unionP" );

        infIntersect1 = mInf.getOntClass( NS + "Intersect1" );
        infIntersect2 = mInf.getOntClass( NS + "Intersect2" );
        noinfIntersect1 = mNoInf.getOntClass( NS + "Intersect1" );
        noinfIntersect2 = mNoInf.getOntClass( NS + "Intersect2" );
        infPintersect = mInf.getObjectProperty( NS + "intersectP" );
        noinfPintersect = mNoInf.getObjectProperty( NS + "intersectP" );
    }

    @AfterEach
    public void tearDown() {
        /* assistance with monitoring space leak
        System.gc();
        System.gc();
        Runtime r = Runtime.getRuntime();
        System.out.println( getClass().getSimpleName() +
                            " memory = " + r.freeMemory() +
                            ", alloc = " + r.totalMemory() +
                            ", % = " + Math.round( 100.0 * (double) r.freeMemory() / (double) r.totalMemory() ));
        */
        mInf.close();
        mInf = null;
        mNoInf.close();
        mNoInf = null;
    }

    // OntClass.listDeclaredProperties() tests ...

    @Test
    public void testLDP_noinfA_nodirect() {
        OntTestUtil.assertIteratorValues(noinfA.listDeclaredProperties( false ),
                                       new Object[] {noinfPa, noinfQa, noinfG, noinfQb} );
    }

    @Test
    public void testHasDP_noinfA_nodirect() {
        // we only need a small number of tests on hasDP because it's the
        // main componenet of listDP
        assertTrue( noinfA.hasDeclaredProperty( noinfPa, false ) );
        assertFalse( noinfA.hasDeclaredProperty( noinfPb, false ) );
    }

    @Test
    public void testLDP_noinfA_direct() {
        OntTestUtil.assertIteratorValues(noinfA.listDeclaredProperties( true ),
                                       new Object[] {noinfPa, noinfQa, noinfG, noinfQb} );
    }

    @Test
    public void testLDP_infA_nodirect() {
        OntTestUtil.assertIteratorValues(infA.listDeclaredProperties( false ),
                                       new Object[] {infPa, infQa, infQb, noinfG} );
    }

    @Test
    public void testLDP_infA_direct() {
        OntTestUtil.assertIteratorValues(infA.listDeclaredProperties( true ),
                                       new Object[] {infPa, infQa, infQb, noinfG} );
    }

    @Test
    public void testLDP_noinfB_nodirect() {
        OntTestUtil.assertIteratorValues(noinfB.listDeclaredProperties( false ),
                                       new Object[] {noinfPa, noinfPb, noinfQa, noinfG, noinfQb} );
    }

    @Test
    public void testLDP_noinfB_direct() {
        OntTestUtil.assertIteratorValues(noinfB.listDeclaredProperties( true ),
                                       new Object[] {noinfPb} );
    }

    @Test
    public void testLDP_infB_nodirect() {
        OntTestUtil.assertIteratorValues(infB.listDeclaredProperties( false ),
                                       new Object[] {infPa, infPb, infQa, infQb, infG} );
    }

    @Test
    public void testLDP_infB_direct() {
        OntTestUtil.assertIteratorValues(infB.listDeclaredProperties( true ),
                                       new Object[] {infPb} );
    }

    @Test
    public void testLDP_noinfC_nodirect() {
        // note that qB appears in the results because without inference it looks like a global
        OntTestUtil.assertIteratorValues(noinfC.listDeclaredProperties( false ),
                                       new Object[] {noinfPa, noinfPb, noinfPc, noinfQa, noinfG, noinfQb} );
    }

    @Test
    public void testLDP_noinfC_direct() {
        OntTestUtil.assertIteratorValues(noinfC.listDeclaredProperties( true ),
                                       new Object[] {noinfPc} );
    }

    @Test
    public void testLDP_infC_nodirect() {
        OntTestUtil.assertIteratorValues(infC.listDeclaredProperties( false ),
                                       new Object[] {infPa, infPb, infPc, infQa, infQb, infG} );
    }

    @Test
    public void testLDP_infC_direct() {
        OntTestUtil.assertIteratorValues(infC.listDeclaredProperties( true ),
                                       new Object[] {infPc} );
    }

    @Test
    public void testLDP_noinfAnn_nodirect() {
        // note that qB appears in the results because without inference it looks like a global
        OntTestUtil.assertIteratorValues(noinfAnn.listDeclaredProperties( false ),
                                       new Object[] {noinfPann, noinfG, noinfQb} );
    }

    @Test
    public void testLDP_noinfAnn_direct() {
        OntTestUtil.assertIteratorValues(noinfAnn.listDeclaredProperties( true ),
                                       new Object[] {noinfPann, noinfG, noinfQb} );
    }

    @Test
    public void testLDP_infAnn_nodirect() {
        OntTestUtil.assertIteratorValues(infAnn.listDeclaredProperties( false ),
                                       new Object[] {noinfPann, noinfG} );
    }

    @Test
    public void testLDP_infAnn_direct() {
        OntTestUtil.assertIteratorValues(infAnn.listDeclaredProperties( true ),
                                       new Object[] {noinfPann, noinfG} );
    }

    @Test
    public void testLDP_noinfUnion_nodirect() {
        OntTestUtil.assertIteratorValues(noinfUnion1.listDeclaredProperties( false ),
                new Object[] {noinfG, noinfQb} );
        OntTestUtil.assertIteratorValues(noinfUnion2.listDeclaredProperties( false ),
                new Object[] {noinfG, noinfQb} );
    }

    @Test
    public void testLDP_infUnion_nodirect() {
        OntTestUtil.assertIteratorValues(infUnion1.listDeclaredProperties( false ),
                new Object[] {infPunion, infG} );
        OntTestUtil.assertIteratorValues(infUnion2.listDeclaredProperties( false ),
                new Object[] {infPunion, infG} );
    }

    @Test
    public void testLDP_noinfIntersect_nodirect() {
        OntTestUtil.assertIteratorValues(noinfIntersect1.listDeclaredProperties( false ),
                new Object[] {noinfG, noinfQb} );
        OntTestUtil.assertIteratorValues(noinfIntersect2.listDeclaredProperties( false ),
                new Object[] {noinfG, noinfQb} );
    }

    @Test
    public void testLDP_infIntersect_nodirect() {
        OntTestUtil.assertIteratorValues(infIntersect1.listDeclaredProperties( false ),
                new Object[] {infG} );
        OntTestUtil.assertIteratorValues(infIntersect2.listDeclaredProperties( false ),
                new Object[] {infG} );
    }

    // OntProperty.listDeclaringProperties() tests ...

    @Test
    public void testLDC_noinfPa_nodirect() {
        OntTestUtil.assertIteratorValues(noinfPa.listDeclaringClasses( false ),
                new Object[] {noinfA, noinfB, noinfC} );
    }

    @Test
    public void testLDC_infPa_nodirect() {
        OntTestUtil.assertIteratorValues(infPa.listDeclaringClasses( false ),
                new Object[] {infA, infB, infC} );
    }

    @Test
    public void testLDC_noinfPb_nodirect() {
        OntTestUtil.assertIteratorValues(noinfPb.listDeclaringClasses( false ),
                new Object[] {noinfB, noinfC} );
    }

    @Test
    public void testLDC_infPb_nodirect() {
        OntTestUtil.assertIteratorValues(infPb.listDeclaringClasses( false ),
                new Object[] {infC, infB} );
    }

    @Test
    public void testLDC_noinfPc_nodirect() {
        OntTestUtil.assertIteratorValues(noinfPc.listDeclaringClasses( false ),
                new Object[] {noinfC} );
    }

    @Test
    public void testLDC_infPc_nodirect() {
        OntTestUtil.assertIteratorValues(infPc.listDeclaringClasses( false ),
                new Object[] {infC} );
    }

    @Test
    public void testLDC_noinfPa_direct() {
        OntTestUtil.assertIteratorValues(noinfPa.listDeclaringClasses( true ),
                new Object[] {noinfA} );
    }

    @Test
    public void testLDC_infPa_direct() {
        OntTestUtil.assertIteratorValues(infPa.listDeclaringClasses( true ),
                new Object[] {infA} );
    }

    @Test
    public void testLDC_noinfPb_direct() {
        OntTestUtil.assertIteratorValues(noinfPb.listDeclaringClasses( true ),
                new Object[] {noinfB} );
    }

    @Test
    public void testLDC_infPb_direct() {
        OntTestUtil.assertIteratorValues(infPb.listDeclaringClasses( true ),
                new Object[] {infB} );
    }

    @Test
    public void testLDC_noinfPc_direct() {
        OntTestUtil.assertIteratorValues(noinfPc.listDeclaringClasses( true ),
                new Object[] {noinfC} );
    }

    @Test
    public void testLDC_infPc_direct() {
        OntTestUtil.assertIteratorValues(infPc.listDeclaringClasses( true ),
                new Object[] {infC} );
    }

    @Test
    public void testLDC_noinfG_direct() {
        OntTestUtil.assertIteratorValues(noinfG.listDeclaringClasses( true ),
                new Object[] {noinfA, noinfAnn, noinfUnion1, noinfUnion2, mNoInf.getOntClass(NS+"Joint"),noinfIntersect1,noinfIntersect2}, 2 );
    }

    @Test
    public void testLDC_infG_direct() {
        OntTestUtil.assertIteratorValues(infG.listDeclaringClasses( true ),
                new Object[] {infA, infAnn, mNoInf.getOntClass(NS+"Joint"),noinfIntersect1,noinfIntersect2}, 1 );
    }

    @Test
    public void testLDC_noinfG_nodirect() {
        OntTestUtil.assertIteratorValues(noinfG.listDeclaringClasses( false ),
                new Object[] {noinfA, noinfB, noinfC, noinfUnion1, noinfUnion2, noinfAnn, mNoInf.getOntClass(NS+"Joint"),noinfIntersect1,noinfIntersect2}, 2 );
    }

    @Test
    public void testLDC_infG_nodirect() {
        OntTestUtil.assertIteratorValues(infG.listDeclaringClasses( false ),
                new Object[] {infA, infB, infC, infAnn, noinfUnion1, noinfUnion2, mNoInf.getOntClass(NS+"Joint"),noinfIntersect1,noinfIntersect2}, 2 );
    }

    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
