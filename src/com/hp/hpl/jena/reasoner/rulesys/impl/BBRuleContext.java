/******************************************************************
 * File:        BBRuleContext.java
 * Created by:  Dave Reynolds
 * Created on:  05-May-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: BBRuleContext.java,v 1.2 2003-05-30 16:26:13 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

/**
 * Implementation of RuleContext for use in the backward chaining
 * interpreter. The RuleContext allows builtin predicates to 
 * interpret variable bindings to access the static triple data.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2003-05-30 16:26:13 $
 */
public class BBRuleContext implements RuleContext {
    
    /** The binding environment which represents the state of the current rule execution. */
    protected BindingEnvironment env;
    
    /** The rule current being executed. */
    protected Rule rule;
    
    /** The enclosing inference graph. */
    protected InfGraph graph;
    
    /** The set of ground triples to be searched by the find operations */
    protected Finder searchpath;
    
    /**
     * Construct an empty context. It can't be used until
     * the rule and environment have been set.
     */
    public BBRuleContext(InfGraph graph, Finder searchpath) {
        this.graph = graph;
        this.searchpath = searchpath;
    }

    /**
     * @see com.hp.hpl.jena.reasoner.rulesys.RuleContext#contains(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node)
     */
    public boolean contains(Node s, Node p, Node o) {
        ClosableIterator i = find(s, p, o);
        boolean result = i.hasNext();
        i.close();
        return result;
    }

    /**
     * @see com.hp.hpl.jena.reasoner.rulesys.RuleContext#contains(com.hp.hpl.jena.graph.Triple)
     */
    public boolean contains(Triple t) {
        return contains(t.getSubject(), t.getPredicate(), t.getObject());
    }

    /**
     * @see com.hp.hpl.jena.reasoner.rulesys.RuleContext#find(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node)
     */
    public ClosableIterator find(Node s, Node p, Node o) {
        return searchpath.find(new TriplePattern(s, p, o));
    }

    /**
     * @see com.hp.hpl.jena.reasoner.rulesys.RuleContext#getEnv()
     */
    public BindingEnvironment getEnv() {
        return env;
    }

    /**
     * Set the binding environment for the this context
     */
    public void setEnv(BindingEnvironment env) {
        this.env = env;
    }
    
    /**
     * @see com.hp.hpl.jena.reasoner.rulesys.RuleContext#getGraph()
     */
    public InfGraph getGraph() {
        return graph;
    }

    /**
     * @see com.hp.hpl.jena.reasoner.rulesys.RuleContext#getRule()
     */
    public Rule getRule() {
        return rule;
    }

    /**
     * @see com.hp.hpl.jena.reasoner.rulesys.RuleContext#setRule(com.hp.hpl.jena.reasoner.rulesys.Rule)
     */
    public void setRule(Rule rule) {
        this.rule = rule;
    }

}



/*
    (c) Copyright Hewlett-Packard Company 2003
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