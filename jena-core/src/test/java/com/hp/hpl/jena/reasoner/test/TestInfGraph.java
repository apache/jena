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

package com.hp.hpl.jena.reasoner.test;

import junit.framework.TestSuite ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.test.AbstractTestGraph ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.reasoner.InfGraph ;

/**
     Needs extending; relys on knowing that the only InfGraph currently used is
     the Jena-provided base. Needs to be made into an abstract test and
     parametrised with the InfGraph being tested (hence getInfGraph).
*/

public class TestInfGraph extends AbstractTestGraph
    {
    public TestInfGraph( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestInfGraph.class ); }
    
    private InfGraph getInfGraph()
        {
        return (InfGraph) ModelFactory.createOntologyModel().getGraph();
        }
    
    @Override
    public Graph getGraph()
        { return getInfGraph(); }
    
    public void testInfGraph()
        {
        InfGraph ig = getInfGraph();
        assertSame( ig.getPrefixMapping(), ig.getRawGraph().getPrefixMapping() );
        }
    
    /**
         Placeholder. Will need revision later.
    */
    public void testInfCapabilities()
        {
        // The default Ontology inference model is RDFS which is safe
        assertTrue( getInfGraph().getCapabilities().findContractSafe() );
        }
    }
