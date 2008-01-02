/******************************************************************
 * File:        TestInfModel.java
 * Created by:  Dave Reynolds
 * Created on:  31-Oct-2005
 * 
 * (c) Copyright 2005, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: TestInfModel.java,v 1.4 2008-01-02 12:08:31 andy_seaborne Exp $
 *****************************************************************/

package com.hp.hpl.jena.reasoner.test;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.vocabulary.RDFS;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test machinery in InfModel which is not associated with any 
 * particular reasoner.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.4 $
 */

public class TestInfModel extends TestCase {
    /**
     * Boilerplate for junit
     */ 
    public TestInfModel( String name ) {
        super( name ); 
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite(TestInfModel.class);
    }  

    /**
     * Check interface extensions which had an earlier bug with null handling
     */
    public void testListWithPosits() {
        String NS = PrintUtil.egNS;
        Model data = ModelFactory.createDefaultModel();
        Resource c1 = data.createResource(NS + "C1");
        Resource c2 = data.createResource(NS + "C2");
        Resource c3 = data.createResource(NS + "C3");
        data.add(c2, RDFS.subClassOf, c3);
        Model premise = ModelFactory.createDefaultModel();
        premise.add(c1, RDFS.subClassOf, c2);
        InfModel im = ModelFactory.createInfModel(ReasonerRegistry.getRDFSReasoner(), data);
        TestUtil.assertIteratorValues(this, im.listStatements(c1, RDFS.subClassOf, null, premise),
                new Object[] {
                    data.createStatement(c1, RDFS.subClassOf, c2),
                    data.createStatement(c1, RDFS.subClassOf, c3),
                    data.createStatement(c1, RDFS.subClassOf, c1)
                });
        
        OntModel om = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF, data);
        TestUtil.assertIteratorValues(this, om.listStatements(c1, RDFS.subClassOf, null, premise),
                new Object[] {
                    data.createStatement(c1, RDFS.subClassOf, c2),
                    data.createStatement(c1, RDFS.subClassOf, c3),
                    data.createStatement(c1, RDFS.subClassOf, c1)
                });
    }

}


/*
    (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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
