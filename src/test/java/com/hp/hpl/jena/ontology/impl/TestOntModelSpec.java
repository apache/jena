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

package com.hp.hpl.jena.ontology.impl;

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
