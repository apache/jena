/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: WrappedReasonerFactory.java,v 1.2 2004-08-06 13:58:41 chris-dollin Exp $
*/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import java.util.*;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;

/**
    WrappedReasonerFactory - a wrapper round ReasonerFactories that
    allowed rules and schemas to be accumulated that will be applied to any 
    Reasoner it generates.
    
    @author kers
*/
public final class WrappedReasonerFactory extends BaseRuleReasonerFactory 
    implements RuleReasonerFactory
    {
    private final ReasonerFactory factory;
    private List schemas = new ArrayList();
    
    public WrappedReasonerFactory( ReasonerFactory rrf )
        { super();
        this.factory = rrf; }
    
    /**
         Answer a Reasoner created according to the underlying factory, and then 
         loaded with this Wrapper's rules (if the Reasoner is a RuleReasoner) and
         bound to this Wrapper's schemas (in an unspecified order).
     */
    public Reasoner create( Resource configuration )
        { Reasoner result = factory.create( configuration );
        if (result instanceof RuleReasoner) ((RuleReasoner) result).setRules( rules );
        for (int i = 0; i < schemas.size(); i += 1) result.bindSchema( (Graph) schemas.get(i) );
        return result; }
    
    /**
         Answer the capabilities of the underlying ReasonerFactory.
    */
    public Model getCapabilities()
        { return factory.getCapabilities(); }
    
    /**
         Remember this schema. When a Reasoner is created from this factory,
         bind all the remembered schemas.
    */
    public void bindSchema( Graph schema )
    	{ schemas.add( schema ); }
    
    /**
         Answer the URI of the underlying ReasonerFactory. 
    */
    public String getURI()
        { return factory.getURI(); }
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