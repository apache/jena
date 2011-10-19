/******************************************************************
 * File:        RETETerminal.java
 * Created by:  Dave Reynolds
 * Created on:  09-Jun-2003
 * 
 * (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: RETETerminal.java,v 1.1 2009-06-29 08:55:33 castagna Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.reasoner.rulesys.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The final node in a RETE graph. It runs the builtin guard clauses
 * and then, if the token passes, executes the head operations.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2009-06-29 08:55:33 $
 */
public class RETETerminal implements RETESinkNode {

    /** Context containing the specific rule and parent graph */
    protected RETERuleContext context;
    
    protected static Logger logger = LoggerFactory.getLogger(FRuleEngine.class);
    
    /**
     * Constructor.
     * @param rule the rule which this terminal should fire.
     * @param engine the parent rule engine through which the deductions and recursive network can be reached.
     * @param graph the wider encompasing infGraph needed to for the RuleContext
     */
    public RETETerminal(Rule rule, RETEEngine engine, ForwardRuleInfGraphI graph) {
        context = new RETERuleContext(graph, engine);
        context.rule = rule;
    }
    
    /**
     * Constructor. Used internally for cloning.
     * @param rule the rule which this terminal should fire.
     * @param engine the parent rule engine through which the deductions and recursive network can be reached.
     * @param graph the wider encompasing infGraph needed to for the RuleContext
     */
    protected RETETerminal(RETERuleContext context) {
        this.context = context;
    }
    
    /**
     * Change the engine/graph to which this terminal should deliver its results.
     */
    public void setContext(RETEEngine engine, ForwardRuleInfGraphI graph) {
        Rule rule = context.getRule();
        context = new RETERuleContext(graph, engine);
        context.setRule(rule);
    }
    
    /** 
     * Propagate a token to this node.
     * @param env a set of variable bindings for the rule being processed. 
     * @param isAdd distinguishes between add and remove operations.
     */
    @Override
    public void fire(BindingVector env, boolean isAdd) {
        Rule rule = context.getRule();
        context.setEnv(env);
        
        if (! context.shouldFire(isAdd)) return;

        // Now fire the rule
        context.getEngine().requestRuleFiring(rule, env, isAdd);
    }
    
    /**
     * Clone this node in the network.
     * @param netCopy a map from RETENode to cloned instance
     * @param context the new context to which the network is being ported
     */
    
    @Override
    public RETENode clone(Map<RETENode, RETENode> netCopy, RETERuleContext contextIn) {
        RETETerminal clone = (RETETerminal)netCopy.get(this);
        if (clone == null) {
            RETERuleContext newContext = new RETERuleContext((ForwardRuleInfGraphI)contextIn.getGraph(), contextIn.getEngine());
            newContext.setRule(context.getRule());
            clone = new RETETerminal(newContext);
            netCopy.put(this, clone);
        }
        return clone;
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