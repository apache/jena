/******************************************************************
 * File:        RuleContext.java
 * Created by:  Dave Reynolds
 * Created on:  28-Apr-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: RuleContext.java,v 1.6 2003-06-12 14:13:39 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.reasoner.InfGraph;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.graph.*;

/**
 * Interface used to convey context information from a rule engine
 * to the stack of procedural builtins. This gives access
 * to the triggering rule, the variable bindings and the set of
 * currently known triples. 
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.6 $ on $Date: 2003-06-12 14:13:39 $
 */
public interface RuleContext {
    /**
     * Returns the current variable binding environment for the current rule.
     * @return BindingEnvironment
     */
    public BindingEnvironment getEnv();

    /**
     * Returns the parent inference graph.
     * @return InfGraph
     */
    public InfGraph getGraph();
    
    /**
     * Returns the rule.
     * @return Rule
     */
    public Rule getRule();

    /**
     * Sets the rule.
     * @param rule The rule to set
     */
    public void setRule(Rule rule);
    
    /**
     * Return true if the triple is already in either the graph or the stack.
     * I.e. it has already been deduced.
     */
    public boolean contains(Triple t);
    
    /**
     * Return true if the triple pattern is already in either the graph or the stack.
     * I.e. it has already been deduced.
     */
    public boolean contains(Node s, Node p, Node o);
    
    /**
     * In some formulations the context includes deductions that are not yet
     * visible to the underlying graph but need to be checked for.
     */
    public ClosableIterator find(Node s, Node p, Node o);
    
    /**
     * Assert a new triple in the deduction graph, bypassing any processing machinery.
     */
    public void silentAdd(Triple t);

    /**
     * Assert a new triple in the deduction graph, triggering any consequent processing as appropriate.
     */
    public void add(Triple t);
    
    /**
     * Remove a triple from the deduction graph (and the original graph if relevant).
     */
    public void remove(Triple t);
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
