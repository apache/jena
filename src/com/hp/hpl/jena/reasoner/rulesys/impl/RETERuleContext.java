/******************************************************************
 * File:        RETERuleContext.java
 * Created by:  Dave Reynolds
 * Created on:  09-Jun-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: RETERuleContext.java,v 1.1 2003-06-09 16:43:23 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.graph.*;
import java.util.*;
import org.apache.log4j.Logger;

/**
 * An implementation of the generic RuleContext for use in the RETE implementation.
 * The RuleContext is used to supply context information to the builtin operations.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2003-06-09 16:43:23 $
 */
public class RETERuleContext implements RuleContext {
    
    /** The binding environment which represents the state of the current rule execution. */
    protected BindingEnvironment env;
    
    /** The rule current being executed. */
    protected Rule rule;
    
    /** The enclosing inference graph. */
    protected ForwardRuleInfGraphI graph;
    
    /** A stack of triples which have been added to the graph but haven't yet been processed. */
    protected ArrayList addStack = new ArrayList();
    
    /** A stack of triples which have been removed from the graph but haven't yet been processed. */
    protected ArrayList removeStack = new ArrayList();
        
    /** log4j logger */
    protected static Logger logger = Logger.getLogger(BFRuleContext.class);
    
    /**
     * Constructor.
     * @param graph the inference graph which owns this context.
     */
    public RETERuleContext(ForwardRuleInfGraphI graph) {
        this.graph = graph;
    }
    
    /**
     * Returns the current variable binding environment for the current rule.
     * @return BindingEnvironment
     */
    public BindingEnvironment getEnv() {
        return env;
    }

    /**
     * Returns the graph.
     * @return InfGraph
     */
    public InfGraph getGraph() {
        return graph;
    }

    /**
     * Returns the rule.
     * @return Rule
     */
    public Rule getRule() {
        return rule;
    }

    /**
     * Sets the rule.
     * @param rule The rule to set
     */
    public void setRule(Rule rule) {
        this.rule = rule;
    }

    /**
     * Sets both the rule and the current binding environment for this context.
     * @param rule the rule being processed
     * @param env the binding environment so far
     */
    public void setRuleEnv(Rule rule, BindingEnvironment env) {
        this.rule = rule;
        this.env = env;
    }
    
    /**
     * Return true if the triple is already in either the graph or the stack.
     * I.e. it has already been deduced.
     */
    public boolean contains(Triple t) {
        // Can't use stackCache.contains because that does not do semantic equality
        return contains(t.getSubject(), t.getPredicate(), t.getObject());
    }
    
    /**
     * Return true if the triple pattern is already in either the graph or the stack.
     * I.e. it has already been deduced.
     */
    public boolean contains(Node s, Node p, Node o) {
        ClosableIterator it = find(s, p, o);
        boolean result = it.hasNext();
        it.close();
        return result;
    }
    
    /**
     * In some formulations the context includes deductions that are not yet
     * visible to the underlying graph but need to be checked for.
     * However, currently this calls the graph find directly.
     */
    public ClosableIterator find(Node s, Node p, Node o) {
        //return graph.find(s, p, o).andThen(pendingCache.find(s, p, o));
        return graph.findDataMatches(s, p, o);
    }
    
    /**
     * Assert a new triple in the deduction graph, bypassing any processing machinery.
     */
    public void silentAdd(Triple t) {
        ((SilentAddI)graph).silentAdd(t);
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