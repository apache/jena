/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            04-Apr-2005
 * Filename           $RCSfile: TestFrameView.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2005-04-08 17:38:50 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl.test;

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
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version Release @release@ ($Id: TestFrameView.java,v 1.4 2005-04-08 17:38:50 ian_dickinson Exp $)
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

    public void setUp() {
        mNoInf = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
        mNoInf.read( "file:testing/ontology/owl/list-syntax/test-ldp.rdf" );
        mInf = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM_RULE_INF );
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

    public void testLDP_noinfA_nodirect() {
        TestUtil.assertIteratorValues( this, noinfA.listDeclaredProperties( false ),
                                       new Object[] {noinfPa, noinfQa, noinfG, noinfQb} );
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



    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
(c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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

