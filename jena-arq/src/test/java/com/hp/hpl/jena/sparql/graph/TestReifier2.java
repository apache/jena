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

package com.hp.hpl.jena.sparql.graph;


import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Reifier ;
import com.hp.hpl.jena.graph.test.AbstractTestReifier ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.Property ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.rdf.model.Statement ;
import com.hp.hpl.jena.shared.ReificationStyle ;

public class TestReifier2 extends AbstractTestReifier
{
    public TestReifier2()
    {
        super("Reifier2") ;
    }

    @Override
    public Graph getGraph()
    {
        return new GraphMemSimple2() ;
    }

    @Override
    public Graph getGraph(ReificationStyle style)
    {
        if ( style != ReificationStyle.Standard )
        {}
        return new GraphMemSimple2() ;
    }

    // Standard only.
    @Override public void testStyle() { assertSame( ReificationStyle.Standard, 
                                                    getGraph( ReificationStyle.Standard ).getReifier().getStyle() ); }
    
    // These are tests on other styles.
    @Override public void testIntercept() {}              // "Convenient"
    @Override public void testMinimalExplode() {}         // "Minimal"
    @Override public void testDynamicHiddenTriples() {}   // "Minimal"

//    @Override public void testBulkClearReificationTriples() {}
//    @Override public void testBulkClearReificationTriples2() {}
    
    /*@Test*/ public void testRemoveReification()
    {
        // Test from Benson Margulies : JENA-82
        Model model= ModelFactory.createModelForGraph(getGraph()) ;
        Resource per1 = model.createResource("urn:x:global#per1");
        Resource per2 = model.createResource("urn:x:global#per2");
        Property pred1 = model.createProperty("http://example/ns#prop1");
        Property pred2 = model.createProperty("http://example/ns#prop2") ;
        Statement s1 = model.createStatement(per1, pred1, per2);
        Statement s2 = model.createStatement(per2, pred2, per2);
        
        s1.createReifiedStatement();
        s2.createReifiedStatement();
        
        assertEquals(2, model.listReifiedStatements().toList().size());
        
        Reifier r = new Reifier2(model.getGraph()) ;
        //r = model.getGraph().getReifier() ;
        r.remove(s2.asTriple()) ;
        assertEquals(1, model.listReifiedStatements().toList().size());
    }
    
}
