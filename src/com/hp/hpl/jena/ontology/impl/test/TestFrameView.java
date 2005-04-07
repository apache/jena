/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            04-Apr-2005
 * Filename           $RCSfile: TestFrameView.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2005-04-07 16:47:40 $
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
 * @version Release @release@ ($Id: TestFrameView.java,v 1.3 2005-04-07 16:47:40 ian_dickinson Exp $)
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

