/******************************************************************
 * File:        RuleContext.java
 * Created by:  Dave Reynolds
 * Created on:  15-Apr-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: BFRuleContext.java,v 1.3 2003-05-30 16:26:14 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.graph.*;
import java.util.*;
import org.apache.log4j.Logger;

/**
 * An implementation of the generic RuleContext interface used by
 * the basic forward (BF) rule engine. This provides additional
 * methods specific to the functioning of that engine.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.3 $ on $Date: 2003-05-30 16:26:14 $
 */
public class BFRuleContext implements RuleContext {
    /** The binding environment which represents the state of the current rule execution. */
    protected BindingStack env;
    
    /** The rule current being executed. */
    protected Rule rule;
    
    /** The enclosing inference graph. */
    protected ForwardRuleInfGraphI graph;
    
    /** A stack of triples which have been added to the graph but haven't yet been processed. */
    protected List stack;
    
    /** A temporary list of Triples which will be added to the stack and triples at the end of a rule scan */
    protected List pending;

    /** A searchable index into the pending triples */
    protected Graph pendingCache;
    
    /** log4j logger */
    protected static Logger logger = Logger.getLogger(BFRuleContext.class);
    
    /**
     * Constructor.
     * @param graph the inference graph which owns this context.
     */
    public BFRuleContext(ForwardRuleInfGraphI graph) {
        this.graph = graph;
        env = new BindingStack();
        stack = new ArrayList();
        pending = new ArrayList();
        pendingCache = new GraphMem();
    }
    
    /**
     * Returns the current variable binding environment for the current rule.
     * @return BindingEnvironment
     */
    public BindingEnvironment getEnv() {
        return env;
    }
    
    /**
     * Variant of the generic getEnv interface specific to the basic
     * forward rule system.
     * Returns the current variable binding environment for the current rule.
     * @return BindingStack
     */
    public BindingStack getEnvStack() {
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
     * Add a triple to the stack of triples to waiting to be processed by the rule engine.
     */
    public void addTriple(Triple t) {
        if (graph.shouldTrace()) {
            if (rule != null) {
                logger.debug("Adding to stack (" + rule.toShortString() + "): " + PrintUtil.print(t));
            } else {
                logger.debug("Adding to stack : " + PrintUtil.print(t));
            }
        }
        stack.add(t);
    }
    
    /**
     * Add a triple to a temporary "pending" store, ready to be added to added to the
     * deductions graph and the processing stack later.
     * <p>This is needed to prevent concurrrent modification exceptions which searching
     * the deductions for matches to a given rule.
     */
    public void addPending(Triple t) {
        if (graph.shouldTrace()) {
            if (rule != null) {
                logger.debug("Adding to pending (" + rule.toShortString() + "): " + PrintUtil.print(t));
            } else {
                logger.debug("Adding to pending : " + PrintUtil.print(t));
            }
        }
        pending.add(t);
        //pendingCache.add(t);
    }
            
    /**
     * Take all the pending triples and add them to both the given graph and
     * to the processing stack.
     * @param deductions the graph to which the pending triples should be added
     */
    public void flushPending(Graph deductions) {
        for (Iterator i = pending.iterator(); i.hasNext(); ) {
            Triple t = (Triple)i.next();
            stack.add(t);
            deductions.add(t);
            i.remove();
            // pendingCache.delete(t);
        }
        pending.clear();
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
        // Can't use stackCache.contains because that does not do semantic equality
        return find(s, p, o).hasNext();
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
     * Return the next triple to be added to the graph, removing it from
     * the stack.
     * @return the Triple or null if there are no more
     */
    public Triple getNextTriple() {
        if (stack.size() > 0) {
            Triple t = (Triple)stack.remove(stack.size() - 1);
            return t;
        } else {
            return null;
        } 
    }
    
    /**
     * Reset the binding environemnt back to empty
     */
    public void resetEnv() {
        env.reset();
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
