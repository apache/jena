/******************************************************************
 * File:        FBRuleInfGraph.java
 * Created by:  Dave Reynolds
 * Created on:  28-May-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: FBRuleInfGraph.java,v 1.1 2003-05-29 16:44:57 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.reasoner.rulesys.impl.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.graph.*;
import java.util.*;

import com.hp.hpl.jena.util.iterator.*;
//import org.apache.log4j.Logger;

/**
 * An inference graph that uses a mixture of forward and backward
 * chaining rules. The forward rules can create direct deductions from
 * the source data and schema and can also create backward rules. A
 * query is answered by consulting the union of the raw data, the forward
 * derived results and any relevant backward rules (whose answers are tabled
 * for future reference).
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2003-05-29 16:44:57 $
 */
public class FBRuleInfGraph  extends BasicForwardRuleInfGraph implements BackwardRuleInfGraphI {
    
    /** Single context for the reasoner, used when passing information to builtins */
    protected BBRuleContext context;
     
    /** A finder that searches across the data, schema, axioms and forward deductions*/
    protected Finder dataFind;

//  =======================================================================
//  Constructors

    /**
     * Constructor.
     * @param reasoner the reasoner which created this inf graph instance
     * @param rules the rules to process
     * @param schema the (optional) schema graph to be included
     */
    public FBRuleInfGraph(Reasoner reasoner, List rules, Graph schema) {
        super(reasoner, rules, schema);
    }

    /**
     * Constructor.
     * @param reasoner the reasoner which created this inf graph instance
     * @param rules the rules to process
     * @param schema the (optional) schema graph to be included
     * @param data the data graph to be processed
     */
    public FBRuleInfGraph(Reasoner reasoner, List rules, Graph schema, Graph data) {
        super(reasoner, rules, schema, data);
    }
    
//  =======================================================================
//   Interface between infGraph and the goal processing machinery

    /**
     * Match a pattern just against the stored data (raw data, schema,
     * axioms) but no derivation.
     */
    public ExtendedIterator findDataMatches(TriplePattern pattern) {
        return dataFind.find(pattern);
    }
            
    /**
     * Process a call to a builtin predicate
     * @param clause the Functor representing the call
     * @param env the BindingEnvironment for this call
     * @param rule the rule which is invoking this call
     * @return true if the predicate succeeds
     */
    public boolean processBuiltin(Object clause, Rule rule, BindingEnvironment env) {
        if (clause instanceof Functor) {
            context.setEnv(env);
            context.setRule(rule);
            return((Functor)clause).evalAsBodyClause(context);
        } else {
            throw new ReasonerException("Illegal builtin predicate: " + clause + " in rule " + rule);
        }
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