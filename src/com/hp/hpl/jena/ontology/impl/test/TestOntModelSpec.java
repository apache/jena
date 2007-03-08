/*
 	(c) Copyright 2006 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestOntModelSpec.java,v 1.2 2007-03-08 15:24:27 chris-dollin Exp $
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

