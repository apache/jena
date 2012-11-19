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

package com.hp.hpl.jena.rdf.model.test;

import junit.framework.TestSuite ;

import com.hp.hpl.jena.graph.compose.Union ;
import com.hp.hpl.jena.rdf.model.InfModel ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.impl.ModelCom ;
import com.hp.hpl.jena.reasoner.InfGraph ;
import com.hp.hpl.jena.reasoner.Reasoner ;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner ;
import com.hp.hpl.jena.reasoner.rulesys.Rule ;
import com.hp.hpl.jena.shared.PrefixMapping ;

/**
    Tests the ModelFactory code. Very skeletal at the moment. It's really
    testing that the methods actually exists, but it doesn't check much in
    the way of behaviour.
*/

public class TestModelFactory extends ModelTestBase
    {
    public TestModelFactory(String name)
        { super(name); }
        
    public static TestSuite suite()
        { return new TestSuite( TestModelFactory.class ); }   
        
    /**
        Test that ModelFactory.createDefaultModel() exists. [Should check that the Model
        is truly a "default" model.]
     */
    public void testCreateDefaultModel()
        { ModelFactory.createDefaultModel().close(); }    

    public void testGetDefaultPrefixMapping()
        {
        assertSame( ModelCom.getDefaultModelPrefixes(), ModelFactory.getDefaultModelPrefixes() );
        }
    
    public void testSetDefaultPrefixMapping()
        {
        PrefixMapping original = ModelCom.getDefaultModelPrefixes();
        PrefixMapping pm = PrefixMapping.Factory.create();
        ModelFactory.setDefaultModelPrefixes( pm );
        assertSame( pm, ModelCom.getDefaultModelPrefixes() );
        assertSame( pm, ModelFactory.getDefaultModelPrefixes() );
        ModelCom.setDefaultModelPrefixes( original );
        }
    
    public void testCreateInfModel() 
        {
        String rule = "-> (eg:r eg:p eg:v).";
        Reasoner r = new GenericRuleReasoner( Rule.parseRules(rule) );
        InfGraph ig = r.bind( ModelFactory.createDefaultModel().getGraph() );
        InfModel im = ModelFactory.createInfModel(ig);
        assertInstanceOf( InfModel.class, im );
        assertEquals( 1, im.size() );
        }
    
    /**
         test that a union model is a model over the union of the two underlying
         graphs. (We don't check that Union works - that's done in the Union
         tests, we hope.)
    */
    public void testCreateUnion()
        {
        Model m1 = ModelFactory.createDefaultModel();
        Model m2 = ModelFactory.createDefaultModel();
        Model m = ModelFactory.createUnion( m1, m2 );
        assertInstanceOf( Union.class, m.getGraph() );
        assertSame( m1.getGraph(), ((Union) m.getGraph()).getL() );
        assertSame( m2.getGraph(), ((Union) m.getGraph()).getR() );
        }
    
    public void testAssembleModelFromModel()
        {
        // TODO Model ModelFactory.assembleModelFrom( Model singleRoot )
        }
    
    public void testFindAssemblerRoots()
        {
        // TODO Set ModelFactory.findAssemblerRoots( Model m )
        }

    public void testAssmbleModelFromRoot()
        {
        // TODO Model assembleModelFrom( Resource root )
        }
    }
