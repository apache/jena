/******************************************************************
 * File:        RETERuleContext.java
 * Created by:  Dave Reynolds
 * Created on:  09-Jun-2003
 * 
 * (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: RETERuleContext.java,v 1.1 2009-06-29 08:55:33 castagna Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.graph.*;

/**
 * An implementation of the generic RuleContext for use in the RETE implementation.
 * The RuleContext is used to supply context information to the builtin operations.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2009-06-29 08:55:33 $
 */
public class RETERuleContext implements RuleContext {
    
    /** The binding environment which represents the state of the current rule execution. */
    protected BindingEnvironment env;
    
    /** The rule current being executed. */
    protected Rule rule;
    
    /** The enclosing inference graph. */
    protected ForwardRuleInfGraphI graph;
    
    /** The RETE engine associated with the inference graph */
    protected RETEEngine engine;
    
    /**
     * Constructor.
     * @param graph the inference graph which owns this context.
     */
    public RETERuleContext(ForwardRuleInfGraphI graph, RETEEngine engine) {
        this.graph = graph;
        this.engine = engine;
    }
    
    /**
     * Returns the current variable binding environment for the current rule.
     * @return BindingEnvironment
     */
    @Override
    public BindingEnvironment getEnv() {
        return env;
    }

    /**
     * Returns the graph.
     * @return InfGraph
     */
    @Override
    public InfGraph getGraph() {
        return graph;
    }
    
    /**
     * Returns the RETE engine associated with this context.
     */
    public RETEEngine getEngine() {
        return engine;
    }

    /**
     * Returns the rule.
     * @return Rule
     */
    @Override
    public Rule getRule() {
        return rule;
    }

    /**
     * Sets the rule.
     * @param rule The rule to set
     */
    @Override
    public void setRule(Rule rule) {
        this.rule = rule;
    }

    /**
     * Sets the current binding environment for this context.
     * @param env the binding environment so far
     */
    public void setEnv(BindingEnvironment env) {
        this.env = env;
    }
    
    /**
     * Return true if the triple is already in either the graph or the stack.
     * I.e. it has already been deduced.
     */
    @Override
    public boolean contains(Triple t) {
        // Can't use stackCache.contains because that does not do semantic equality
        return contains(t.getSubject(), t.getPredicate(), t.getObject());
    }
    
    /**
     * Return true if the triple pattern is already in either the graph or the stack.
     * I.e. it has already been deduced.
     */
    @Override
    public boolean contains(Node s, Node p, Node o) {
        ClosableIterator<Triple> it = find(s, p, o);
        boolean result = it.hasNext();
        it.close();
        return result;
    }
    
    /**
     * In some formulations the context includes deductions that are not yet
     * visible to the underlying graph but need to be checked for.
     * However, currently this calls the graph find directly.
     */
    @Override
    public ClosableIterator<Triple> find(Node s, Node p, Node o) {
        //return graph.find(s, p, o).andThen(pendingCache.find(s, p, o));
        return graph.findDataMatches(s, p, o);
    }
    
    /**
     * Assert a new triple in the deduction graph, bypassing any processing machinery.
     */
    @Override
    public void silentAdd(Triple t) {
        ((SilentAddI)graph).silentAdd(t);
    }

    /**
     * Remove a triple from the deduction graph (and the original graph if relevant).
     */
    @Override
    public void remove(Triple t) {
        graph.getRawGraph().delete(t);
        engine.deleteTriple(t, true);
    }

    /**
     * Assert a new triple in the deduction graph, triggering any consequent processing as appropriate.
     */
    @Override
    public void add(Triple t) {
        engine.addTriple(t, true);
    }

    /**
     * Check whether the rule should fire in this context.
     */
    public boolean shouldFire(boolean allowUnsafe) {
        // Check any non-pattern clauses 
        for (int i = 0; i < rule.bodyLength(); i++) {
            Object clause = rule.getBodyElement(i);
            if (clause instanceof Functor) {
                // Fire a built in
                if (allowUnsafe) {
                    if (!((Functor)clause).evalAsBodyClause(this)) {
                        // Failed guard so just discard and return
                        return false;
                    }
                } else {
                    // Don't re-run side-effectful clause on a re-run
                    if (!((Functor)clause).safeEvalAsBodyClause(this)) {
                        // Failed guard so just discard and return
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * Check if a rule from the conflict set is still OK to fire.
     * Just checks the non-monotonic guards such as noValue.
     */
    public boolean shouldStillFire() {
        // Check any non-pattern clauses 
        for (int i = 0; i < rule.bodyLength(); i++) {
            Object clause = rule.getBodyElement(i);
            if (clause instanceof Functor) {
                Builtin builtin = ((Functor)clause).getImplementor();
                if (builtin != null && !builtin.isMonotonic()) {
                    if (!((Functor)clause).evalAsBodyClause(this)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
}


/*
    (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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