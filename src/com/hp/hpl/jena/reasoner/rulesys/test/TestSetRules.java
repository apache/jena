/*
  (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TestSetRules.java,v 1.8 2005-02-21 12:18:14 andy_seaborne Exp $
*/
package com.hp.hpl.jena.reasoner.rulesys.test;

import java.util.*;

import junit.framework.TestSuite;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.ModelSpecImpl;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.rulesys.impl.WrappedReasonerFactory;

/**
     TestSetRules - tests to bring setRules into existence on RuleReasonerFactory.     
     @author kers
*/
public class TestSetRules extends ModelTestBase
    {

    public TestSetRules( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestSetRules.class ); }

    static final List rules = Rule.parseRules( "[name: (?s owl:foo ?p) -> (?s ?p ?a)]" );
    
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
        List done = new ArrayList();
        static final Model capabilities = modelWithStatements( "this isA Capability" );
        static final String uri = "eg:mockURI";
        static final Reasoner reasoner = new GenericRuleReasoner( rules );
        
        public void addRules( List rules )
            { assertEquals( TestSetRules.rules, rules );
            done.add( "addRules" ); }
    
        public Reasoner create(Resource configuration)
            { done.add( "create" );
            return reasoner; }
    
        public Model getCapabilities()
            { done.add( "capabilities" );
            return capabilities; }
    
        public String getURI()
            { done.add( "uri" );
            return uri; }
        }
    
    private static ReasonerFactory wrap( final ReasonerFactory rrf )
        {
        return new WrappedReasonerFactory( rrf, ModelSpecImpl.emptyResource );
        }
    
    }


/*
    (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
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