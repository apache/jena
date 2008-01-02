/*
 	(c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestOntModelSpec.java,v 1.3 2008-01-02 12:08:39 andy_seaborne Exp $
*/

package com.hp.hpl.jena.ontology.impl.test;

import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;

public class TestOntModelSpec extends ModelTestBase
    {
    public TestOntModelSpec( String name )
        { super( name ); }

    public void testEqualityAndDifference()
        {
        testEqualityAndDifference( OntModelSpec.OWL_MEM );
        testEqualityAndDifference( OntModelSpec.OWL_MEM_RDFS_INF );
        testEqualityAndDifference( OntModelSpec.OWL_MEM_RULE_INF );
        testEqualityAndDifference( OntModelSpec.OWL_MEM_TRANS_INF );
        testEqualityAndDifference( OntModelSpec.OWL_MEM_MICRO_RULE_INF );
        testEqualityAndDifference( OntModelSpec.OWL_MEM_MINI_RULE_INF );
        testEqualityAndDifference( OntModelSpec.OWL_DL_MEM );
        testEqualityAndDifference( OntModelSpec.OWL_DL_MEM_RDFS_INF );
        testEqualityAndDifference( OntModelSpec.OWL_DL_MEM_RULE_INF );
        testEqualityAndDifference( OntModelSpec.OWL_DL_MEM_TRANS_INF );
        testEqualityAndDifference( OntModelSpec.OWL_LITE_MEM );
        testEqualityAndDifference( OntModelSpec.OWL_LITE_MEM_TRANS_INF );
        testEqualityAndDifference( OntModelSpec.OWL_LITE_MEM_RDFS_INF );
        testEqualityAndDifference( OntModelSpec.OWL_LITE_MEM_RULES_INF );
        testEqualityAndDifference( OntModelSpec.RDFS_MEM );
        testEqualityAndDifference( OntModelSpec.RDFS_MEM_TRANS_INF );
        testEqualityAndDifference( OntModelSpec.RDFS_MEM_RDFS_INF );
        }
    
    private void testEqualityAndDifference( OntModelSpec os )
        {
        assertEquals( os, new OntModelSpec( os ) );
        assertDiffer( os, OntModelSpec.DAML_MEM );
        }
    
    public void testAssembleRoot()
        {
        // TODO OntModelSpec.assemble( Resource root )
        }
    
    public void testAssembleModel()
        {
        // TODO OntModelSpec.assemble( Model model )
        }
    }

/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
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
