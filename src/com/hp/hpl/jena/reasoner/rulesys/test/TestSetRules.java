/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TestSetRules.java,v 1.1 2004-07-30 15:16:03 chris-dollin Exp $
*/
package com.hp.hpl.jena.reasoner.rulesys.test;

import java.util.*;

import junit.framework.TestSuite;

import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.rulesys.impl.BaseRuleReasonerFactory;
import com.hp.hpl.jena.reasoner.transitiveReasoner.TransitiveReasoner;
import com.hp.hpl.jena.reasoner.transitiveReasoner.TransitiveReasonerFactory;

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
    
    public void testRuleFactories()
        {
        assertHasParent( GenericRuleReasonerFactory.class, BaseRuleReasonerFactory.class );
        assertHasParent( RDFSRuleReasonerFactory.class, BaseRuleReasonerFactory.class );
        assertHasParent( OWLMicroReasonerFactory.class, BaseRuleReasonerFactory.class );
        }
    
    public void testGenericSetRules()
        { testFactory( new GenericRuleReasonerFactory() ); }
    
    public void testRDFSSetRules()
        { testFactory( new RDFSRuleReasonerFactory() ); }

    public void testOwlMicroSetRules()
        { testFactory( new OWLMicroReasonerFactory() ); }
    
    private void testFactory( RuleReasonerFactory grf )
        {
        FBRuleReasoner base = (FBRuleReasoner) grf.create( null );
        grf.setRules( rules );
        FBRuleReasoner gr = (FBRuleReasoner) grf.create( null );
        assertEquals( append( base.getRules(), rules ), gr.getRules() );
        }

    public List append( List L, List R )
        { List result = new ArrayList( L );
        result.addAll( R );
        return result; }
    }


/*
    (c) Copyright 2004, Hewlett-Packard Development Company, LP
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