/******************************************************************
 * File:        BBRuleContext.java
 * Created by:  Dave Reynolds
 * Created on:  05-May-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: BBRuleContext.java,v 1.8 2003-08-27 13:09:19 andy_seaborne Exp $
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
 * @version $Revision: 1.8 $ on $Date: 2003-08-27 13:09:19 $
 */
public class BBRuleContext implements RuleContext {
    
    /** The binding environment which represents the state of the current rule execution. */
    protected BindingEnvironment env;
    
    /** The rule current being executed. */
    protected Rule rule;
    
    /** The enclosing inference graph. */
    protected BackwardRuleInfGraphI graph;
    
    /**
     * Construct an empty context. It can't be used until
     * the rule and environment have been set.
     */
    public BBRuleContext(BackwardRuleInfGraphI graph) {
        this.graph = graph;
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
        return graph.findDataMatches(new TriplePattern(s, p, o));
//        return searchpath.find(new TriplePattern(s, p, o));
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
    
    /**
     * Assert a new triple in the deduction graph, bypassing any processing machinery.
     */
    public void silentAdd(Triple t) {
        ((SilentAddI)graph).silentAdd(t);
    }

    /**
     * Assert a new triple in the deduction graph, triggering any consequent processing as appropriate.
     * In the backward case there no immediate consequences so this is equivalent to a silentAdd.
     */
    public void add(Triple t) {
        ((SilentAddI)graph).silentAdd(t);
    }

    /**
     * Remove a triple from the deduction graph (and the original graph if relevant).
     */
    public void remove(Triple t) {
        graph.delete(t);
    }

    /**
     * Retrieve or create a bNode representing an inferred property value.
     * This is currently only available on backward contexts and not part of the 
     * normal RuleContext interface.
     * @param instance the base instance node to which the property applies
     * @param prop the property node whose value is being inferred
     * @param pclass the (optional, can be null) class for the inferred value.
     * @return the bNode representing the property value 
     */
    public Node getTemp(Node instance, Node prop, Node pclass) {
        return graph.getTemp(instance, prop, pclass);
    }

}



/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
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