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
import junit.framework.TestCase;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.test.TestUtil;




/**
 * <p>
 * Unit-tests for frame-like views of OWL and RDFS-classes, especially listDeclaredProperties
 * </p>
 */
public class TestFrameView
    extends TestCase
{
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

    @Override
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

    @Override
    public void tearDown() {
        /* assistance with monitoring space leak
        System.gc();
        System.gc();
        Runtime r = Runtime.getRuntime();
        System.out.println( getName() + 
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

    public void testLDP_noinfA_nodirect() {
        TestUtil.assertIteratorValues( this, noinfA.listDeclaredProperties( false ),
                                       new Object[] {noinfPa, noinfQa, noinfG, noinfQb} );
    }

    public void testHasDP_noinfA_nodirect() {
        // we only need a small number of tests on hasDP because it's the
        // main componenet of listDP
        assertTrue( noinfA.hasDeclaredProperty( noinfPa, false ) );
        assertFalse( noinfA.hasDeclaredProperty( noinfPb, false ) );
    }

    public void testLDP_noinfA_direct() {
        TestUtil.assertIteratorValues( this, noinfA.listDeclaredProperties( true ),
                                       new Object[] {noinfPa, noinfQa, noinfG, noinfQb} );
    }

    public void testLDP_infA_nodirect() {
        TestUtil.assertIteratorValues( this, infA.listDeclaredProperties( false ),
                                       new Object[] {infPa, infQa, infQb, noinfG} );
    }

    public void testLDP_infA_direct() {
        TestUtil.assertIteratorValues( this, infA.listDeclaredProperties( true ),
                                       new Object[] {infPa, infQa, infQb, noinfG} );
    }

    public void testLDP_noinfB_nodirect() {
        TestUtil.assertIteratorValues( this, noinfB.listDeclaredProperties( false ),
                                       new Object[] {noinfPa, noinfPb, noinfQa, noinfG, noinfQb} );
    }

    public void testLDP_noinfB_direct() {
        TestUtil.assertIteratorValues( this, noinfB.listDeclaredProperties( true ),
                                       new Object[] {noinfPb} );
    }

    public void testLDP_infB_nodirect() {
        TestUtil.assertIteratorValues( this, infB.listDeclaredProperties( false ),
                                       new Object[] {infPa, infPb, infQa, infQb, infG} );
    }

    public void testLDP_infB_direct() {
        TestUtil.assertIteratorValues( this, infB.listDeclaredProperties( true ),
                                       new Object[] {infPb} );
    }

    public void testLDP_noinfC_nodirect() {
        // note that qB appears in the results because without inference it looks like a global
        TestUtil.assertIteratorValues( this, noinfC.listDeclaredProperties( false ),
                                       new Object[] {noinfPa, noinfPb, noinfPc, noinfQa, noinfG, noinfQb} );
    }

    public void testLDP_noinfC_direct() {
        TestUtil.assertIteratorValues( this, noinfC.listDeclaredProperties( true ),
                                       new Object[] {noinfPc} );
    }

    public void testLDP_infC_nodirect() {
        TestUtil.assertIteratorValues( this, infC.listDeclaredProperties( false ),
                                       new Object[] {infPa, infPb, infPc, infQa, infQb, infG} );
    }

    public void testLDP_infC_direct() {
        TestUtil.assertIteratorValues( this, infC.listDeclaredProperties( true ),
                                       new Object[] {infPc} );
    }


    public void testLDP_noinfAnn_nodirect() {
        // note that qB appears in the results because without inference it looks like a global
        TestUtil.assertIteratorValues( this, noinfAnn.listDeclaredProperties( false ),
                                       new Object[] {noinfPann, noinfG, noinfQb} );
    }

    public void testLDP_noinfAnn_direct() {
        TestUtil.assertIteratorValues( this, noinfAnn.listDeclaredProperties( true ),
                                       new Object[] {noinfPann, noinfG, noinfQb} );
    }

    public void testLDP_infAnn_nodirect() {
        TestUtil.assertIteratorValues( this, infAnn.listDeclaredProperties( false ),
                                       new Object[] {noinfPann, noinfG} );
    }

    public void testLDP_infAnn_direct() {
        TestUtil.assertIteratorValues( this, infAnn.listDeclaredProperties( true ),
                                       new Object[] {noinfPann, noinfG} );
    }


    public void testLDP_noinfUnion_nodirect() {
        TestUtil.assertIteratorValues( this, noinfUnion1.listDeclaredProperties( false ),
                new Object[] {noinfG, noinfQb} );
        TestUtil.assertIteratorValues( this, noinfUnion2.listDeclaredProperties( false ),
                new Object[] {noinfG, noinfQb} );
    }

    public void testLDP_infUnion_nodirect() {
        TestUtil.assertIteratorValues( this, infUnion1.listDeclaredProperties( false ),
                new Object[] {infPunion, infG} );
        TestUtil.assertIteratorValues( this, infUnion2.listDeclaredProperties( false ),
                new Object[] {infPunion, infG} );
    }

    public void testLDP_noinfIntersect_nodirect() {
        TestUtil.assertIteratorValues( this, noinfIntersect1.listDeclaredProperties( false ),
                new Object[] {noinfG, noinfQb} );
        TestUtil.assertIteratorValues( this, noinfIntersect2.listDeclaredProperties( false ),
                new Object[] {noinfG, noinfQb} );
    }

    public void testLDP_infIntersect_nodirect() {
        TestUtil.assertIteratorValues( this, infIntersect1.listDeclaredProperties( false ),
                new Object[] {infG} );
        TestUtil.assertIteratorValues( this, infIntersect2.listDeclaredProperties( false ),
                new Object[] {infG} );
    }

    // OntProperty.listDeclaringProperties() tests ...

    public void testLDC_noinfPa_nodirect() {
        TestUtil.assertIteratorValues( this, noinfPa.listDeclaringClasses( false ),
                new Object[] {noinfA, noinfB, noinfC} );
    }

    public void testLDC_infPa_nodirect() {
        TestUtil.assertIteratorValues( this, infPa.listDeclaringClasses( false ),
                new Object[] {infA, infB, infC} );
    }

    public void testLDC_noinfPb_nodirect() {
        TestUtil.assertIteratorValues( this, noinfPb.listDeclaringClasses( false ),
                new Object[] {noinfB, noinfC} );
    }

    public void testLDC_infPb_nodirect() {
        TestUtil.assertIteratorValues( this, infPb.listDeclaringClasses( false ),
                new Object[] {infC, infB} );
    }

    public void testLDC_noinfPc_nodirect() {
        TestUtil.assertIteratorValues( this, noinfPc.listDeclaringClasses( false ),
                new Object[] {noinfC} );
    }

    public void testLDC_infPc_nodirect() {
        TestUtil.assertIteratorValues( this, infPc.listDeclaringClasses( false ),
                new Object[] {infC} );
    }

    public void testLDC_noinfPa_direct() {
        TestUtil.assertIteratorValues( this, noinfPa.listDeclaringClasses( true ),
                new Object[] {noinfA} );
    }

    public void testLDC_infPa_direct() {
        TestUtil.assertIteratorValues( this, infPa.listDeclaringClasses( true ),
                new Object[] {infA} );
    }

    public void testLDC_noinfPb_direct() {
        TestUtil.assertIteratorValues( this, noinfPb.listDeclaringClasses( true ),
                new Object[] {noinfB} );
    }

    public void testLDC_infPb_direct() {
        TestUtil.assertIteratorValues( this, infPb.listDeclaringClasses( true ),
                new Object[] {infB} );
    }

    public void testLDC_noinfPc_direct() {
        TestUtil.assertIteratorValues( this, noinfPc.listDeclaringClasses( true ),
                new Object[] {noinfC} );
    }

    public void testLDC_infPc_direct() {
        TestUtil.assertIteratorValues( this, infPc.listDeclaringClasses( true ),
                new Object[] {infC} );
    }

    public void testLDC_noinfG_direct() {
        TestUtil.assertIteratorValues( this, noinfG.listDeclaringClasses( true ),
                new Object[] {noinfA, noinfAnn, noinfUnion1, noinfUnion2, mNoInf.getOntClass(NS+"Joint"),noinfIntersect1,noinfIntersect2}, 2 );
    }

    public void testLDC_infG_direct() {
        TestUtil.assertIteratorValues( this, infG.listDeclaringClasses( true ),
                new Object[] {infA, infAnn, mNoInf.getOntClass(NS+"Joint"),noinfIntersect1,noinfIntersect2}, 1 );
    }

    public void testLDC_noinfG_nodirect() {
        TestUtil.assertIteratorValues( this, noinfG.listDeclaringClasses( false ),
                new Object[] {noinfA, noinfB, noinfC, noinfUnion1, noinfUnion2, noinfAnn, mNoInf.getOntClass(NS+"Joint"),noinfIntersect1,noinfIntersect2}, 2 );
    }

    public void testLDC_infG_nodirect() {
        TestUtil.assertIteratorValues( this, infG.listDeclaringClasses( false ),
                new Object[] {infA, infB, infC, infAnn, noinfUnion1, noinfUnion2, mNoInf.getOntClass(NS+"Joint"),noinfIntersect1,noinfIntersect2}, 2 );
    }


    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
