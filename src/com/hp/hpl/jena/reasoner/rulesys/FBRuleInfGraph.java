/******************************************************************
 * File:        FBRuleInfGraph.java
 * Created by:  Dave Reynolds
 * Created on:  28-May-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: FBRuleInfGraph.java,v 1.2 2003-05-30 16:26:13 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.reasoner.rulesys.impl.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.graph.*;
import java.util.*;

import com.hp.hpl.jena.util.iterator.*;
import org.apache.log4j.Logger;

/**
 * An inference graph that uses a mixture of forward and backward
 * chaining rules. The forward rules can create direct deductions from
 * the source data and schema and can also create backward rules. A
 * query is answered by consulting the union of the raw data, the forward
 * derived results and any relevant backward rules (whose answers are tabled
 * for future reference).
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2003-05-30 16:26:13 $
 */
public class FBRuleInfGraph  extends BasicForwardRuleInfGraph implements BackwardRuleInfGraphI {
    
    /** Single context for the reasoner, used when passing information to builtins */
    protected BBRuleContext context;
     
    /** A finder that searches across the data, schema, axioms and forward deductions*/
    protected Finder dataFind;
    
    /** The core backward rule engine which includes all the memoized results */
    protected BRuleEngine bEngine;
    
    /** The original rule set as supplied */
    protected List rules;
    
    /** log4j logger*/
    static Logger logger = Logger.getLogger(FBRuleInfGraph.class);

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
        this.rules = rules;
        bEngine = new BRuleEngine(this);
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
        this.rules = rules;        bEngine = new BRuleEngine(this);
    }
    
//  =======================================================================
//   Interface between infGraph and the goal processing machinery

    
    /**
     * Search the combination of data and deductions graphs for the given triple pattern.
     * This may different from the normal find operation in the base of hybrid reasoners
     * where we are side-stepping the backward deduction step.
     */
    public ExtendedIterator findDataMatches(Node subject, Node predicate, Node object) {
        return dataFind.find(new TriplePattern(subject, predicate, object));
    }
    
    /**
     * Search the combination of data and deductions graphs for the given triple pattern.
     * This may different from the normal find operation in the base of hybrid reasoners
     * where we are side-stepping the backward deduction step.
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
    
    /**
     * Adds a new Backward rule as a rules of a forward rule process. Only some
     * infgraphs support this.
     */
    public void addBRule(Rule brule) {
        logger.debug("Adding rule " + brule);
        bEngine.addRule(brule);
        bEngine.reset();
    }
    
//  =======================================================================
//  Core inf graph methods
    
    /**
     * Perform any initial processing and caching. This call is optional. Most
     * engines either have negligable set up work or will perform an implicit
     * "prepare" if necessary. The call is provided for those occasions where
     * substantial preparation work is possible (e.g. running a forward chaining
     * rule system) and where an application might wish greater control over when
     * this prepration is done.
     */
    public void prepare() {
        if (!isPrepared) {
            isPrepared = true;
            // initilize the deductions graph
            deductions = new FGraph( new GraphMem() );
            dataFind = (fdata == null) ? deductions :  FinderUtil.cascade(deductions, fdata);
            if (schemaGraph != null) {
                preloadDeductions(schemaGraph);
            }
            extractPureBackwardRules();
            engine.init(true);
            context = new BBRuleContext(this, dataFind);
        }
    }
    
    /**
     * Cause the inference graph to reconsult the underlying graph to take
     * into account changes. Normally changes are made through the InfGraph's add and
     * remove calls are will be handled appropriately. However, in some cases changes
     * are made "behind the InfGraph's back" and this forces a full reconsult of
     * the changed data. 
     */
    public void rebind() {
        if (bEngine != null) bEngine.reset();
        isPrepared = false;
    }
    
    /**
     * Set the state of the trace flag. If set to true then rule firings
     * are logged out to the Logger at "INFO" level.
     */
    public void setTraceOn(boolean state) {
        super.setTraceOn(state);
        bEngine.setTraceOn(state);
    }
   
    /**
     * Extended find interface used in situations where the implementator
     * may or may not be able to answer the complete query. It will
     * attempt to answer the pattern but if its answers are not known
     * to be complete then it will also pass the request on to the nested
     * Finder to append more results.
     * @param pattern a TriplePattern to be matched against the data
     * @param continuation either a Finder or a normal Graph which
     * will be asked for additional match results if the implementor
     * may not have completely satisfied the query.
     */
    public ExtendedIterator findWithContinuation(TriplePattern pattern, Finder continuation) {
        if (!isPrepared) prepare();
        
        if (continuation == null) {
            return WrappedIterator.create( new TopGoalIterator(bEngine, pattern) );
        } else {
            return WrappedIterator.create( new TopGoalIterator(bEngine, pattern) )
                            .andThen(continuation.find(pattern));
        }
    }
   
    /** 
     * Returns an iterator over Triples.
     * This implementation assumes that the underlying findWithContinuation 
     * will have also consulted the raw data.
     */
    public ExtendedIterator find(Node subject, Node property, Node object) {
        return findWithContinuation(new TriplePattern(subject, property, object), null);
    }

    /**
     * Basic pattern lookup interface.
     * This implementation assumes that the underlying findWithContinuation 
     * will have also consulted the raw data.
     * @param pattern a TriplePattern to be matched against the data
     * @return a ExtendedIterator over all Triples in the data set
     *  that match the pattern
     */
    public ExtendedIterator find(TriplePattern pattern) {
        return findWithContinuation(pattern, null);
    }

    /**
     * Flush out all cached results. Future queries have to start from scratch.
     */
    public void reset() {
        bEngine.reset();
        isPrepared = false;
    }

    /**
     * Scan the initial rule set and pick out all the backward-only rules with non-null bodies,
     * and transfer these rules to the backward engine. 
     */
    private void extractPureBackwardRules() {
        for (Iterator i = rules.iterator(); i.hasNext(); ) {
            Rule r = (Rule)i.next();
            if (r.isBackward() && r.bodyLength() > 0) {
                bEngine.addRule(r);
            }
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