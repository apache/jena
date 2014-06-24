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

package com.hp.hpl.jena.reasoner.rulesys.test;

import java.util.*;

import junit.framework.TestSuite;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.rulesys.impl.WrappedReasonerFactory;

/**
     TestSetRules - tests to bring setRules into existence on RuleReasonerFactory.     
*/
public class TestSetRules extends ModelTestBase
    {

    public TestSetRules( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestSetRules.class ); }

    static final List<Rule> rules = Rule.parseRules( "[name: (?s owl:foo ?p) -> (?s ?p ?a)]" );
    
    public void testRuleReasonerWrapper()
        {
        MockFactory mock = new MockFactory();
        ReasonerFactory wrapped = wrap( mock );
        assertEquals( MockFactory.capabilities, wrapped.getCapabilities() );
        assertEquals( MockFactory.uri, wrapped.getURI() );
        assertEquals( MockFactory.reasoner, wrapped.create( null ) );
        assertEquals( Arrays.asList( new Object[] {"capabilities", "uri", "create"} ),  mock.done );
        }
    
    private static class MockFactory implements ReasonerFactory
        {
        List<String> done = new ArrayList<>();
        static final Model capabilities = modelWithStatements( "this isA Capability" );
        static final String uri = "eg:mockURI";
        static final Reasoner reasoner = new GenericRuleReasoner( rules );
        
        public void addRules( List<Rule> rules )
            { assertEquals( TestSetRules.rules, rules );
            done.add( "addRules" ); }
    
        @Override
        public Reasoner create(Resource configuration)
            { done.add( "create" );
            return reasoner; }
    
        @Override
        public Model getCapabilities()
            { done.add( "capabilities" );
            return capabilities; }
    
        @Override
        public String getURI()
            { done.add( "uri" );
            return uri; }
        }
    
    private static Resource emptyResource = 
        ModelFactory.createDefaultModel().createResource();
    
    private static ReasonerFactory wrap( final ReasonerFactory rrf )
        {
        return new WrappedReasonerFactory( rrf, emptyResource );
        }
    
    }
