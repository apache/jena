/******************************************************************
 * File:        RETEEngine.java
 * Created by:  Dave Reynolds
 * Created on:  09-Jun-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: RETEEngine.java,v 1.1 2003-06-09 16:43:24 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.graph.*;
import java.util.*;

import com.hp.hpl.jena.util.OneToManyMap;

import org.apache.log4j.Logger;

/**
 * A RETE version of the the forward rule system engine. It neeeds to reference
 * an enclosing ForwardInfGraphI which holds the raw data and deductions.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2003-06-09 16:43:24 $
 */
public class RETEEngine {
    
    /** The parent InfGraph which is employing this engine instance */
    protected ForwardRuleInfGraphI infGraph;
    
    /** Set of rules being used */
    protected List rules;
    
    /** Map from predicate node to rule + clause, Node_ANY is used for wildcard predicates */
    protected OneToManyMap clauseIndex;
    
    /** List of predicates used in rules to assist in fast data loading */
    protected HashSet predicatesUsed;
    
    /** Flag, if true then there is a wildcard predicate in the rule set so that selective insert is not useful */
    protected boolean wildcardRule;
     
    /** Set to true to flag that derivations should be logged */
    protected boolean recordDerivations;
    
    /** performance stats - number of rules fired */
    long nRulesFired = 0;
    
    /** performance stats - number of rules fired during axiom initialization */
    long nAxiomRulesFired = -1;
    
    /** True if we have processed the axioms in the rule set */
    boolean processedAxioms = false;
    
    /** log4j logger */
    protected static Logger logger = Logger.getLogger(FRuleEngine.class);
    
//  =======================================================================
//  Constructors

    /**
     * Constructor.
     * @param parent the F or FB infGraph that it using this engine, the parent graph
     * holds the deductions graph and source data.
     * @param rules the rule set to be processed
     */
    public RETEEngine(ForwardRuleInfGraphI parent, List rules) {
        infGraph = parent;
        this.rules = rules;
    }

    /**
     * Constructor. Build an empty engine to which rules must be added
     * using setRuleStore().
     * @param parent the F or FB infGraph that it using this engine, the parent graph
     * holds the deductions graph and source data.
     */
    public RETEEngine(ForwardRuleInfGraphI parent) {
        infGraph = parent;
    }
    
//  =======================================================================
//  Control methods

    /**
     * Process all available data. This should be called once a deductions graph
     * has be prepared and loaded with any precomputed deductions. It will process
     * the rule axioms and all relevant existing exiting data entries.
     * @param ignoreBrules set to true if rules written in backward notation should be ignored
     */
    public void init(boolean ignoreBrules) {
        buildClauseIndex(ignoreBrules);
        findAndProcessAxioms();
        nAxiomRulesFired = nRulesFired;
        logger.debug("Axioms fired " + nAxiomRulesFired + " rules");
        fastInit();
    }
    
    /**
     * Process all available data. This version expects that all the axioms 
     * have already be preprocessed and the clause index already exists.
     */
    public void fastInit() {
        if (infGraph.getRawGraph() == null) return; 
        // Create the reasoning context
        BFRuleContext context = new BFRuleContext(infGraph);
        // Insert the data
        if (wildcardRule) {
            for (Iterator i = infGraph.getRawGraph().find(null, null, null); i.hasNext(); ) {
                context.addTriple((Triple)i.next());
            }
        } else {
            for (Iterator p = predicatesUsed.iterator(); p.hasNext(); ) {
                Node predicate = (Node)p.next();
                for (Iterator i = infGraph.getRawGraph().find(null, predicate, null); i.hasNext(); ) {
                    context.addTriple((Triple)i.next());
                }
            }
        }
        // Run the engine
        addSet(context);
    }

    /**
     * Add one triple to the data graph, run any rules triggered by
     * the new data item, recursively adding any generated triples.
     */
    public synchronized void add(Triple t) {
        BFRuleContext context = new BFRuleContext(infGraph);
        context.addTriple(t);
        addSet(context);
    }
    
    /**
     * Return the number of rules fired since this rule engine instance
     * was created and initialized
     */
    public long getNRulesFired() {
        return nRulesFired;
    }
    
    /**
     * Return true if the internal engine state means that tracing is worthwhile.
     * It will return false during the axiom bootstrap phase.
     */
    public boolean shouldTrace() {
        return processedAxioms;
    }

    /**
     * Set to true to enable derivation caching
     */
    public void setDerivationLogging(boolean recordDerivations) {
        this.recordDerivations = recordDerivations;
    }
    
    /**
     * Access the precomputed internal rule form. Used when precomputing the
     * internal axiom closures.
     */
    public RuleStore getRuleStore() {
        return new RuleStore(clauseIndex, predicatesUsed, wildcardRule);
    }
    
    /**
     * Set the internal rule from from a precomputed state.
     */
    public void setRuleStore(RuleStore ruleStore) {
        RuleStore rs = (RuleStore)ruleStore;
        clauseIndex = rs.clauseIndex;
        predicatesUsed = rs.predicatesUsed;
        wildcardRule = rs.wildcardRule;
    }
    
//  =======================================================================
//  Internal methods

    /**
     * Add a set of new triple to the data graph, run any rules triggered by
     * the new data item, recursively adding any generated triples.
     * Technically the triples having been physically added to either the
     * base or deduction graphs and the job of this function is just to
     * process the stack of additions firing any relevant rules.
     * @param context a context containing a set of new triples to be added
     */
    public void addSet(BFRuleContext context) {
//        Triple t;
//        while ((t = context.getNextTriple()) != null) {
//            if (infGraph.shouldTrace()) {
//                logger.info("Processing: " + PrintUtil.print(t));
//            }
//            // Check for rule triggers
//            HashSet firedRules = new HashSet();
//            Iterator i1 = clauseIndex.getAll(t.getPredicate());
//            Iterator i2 = clauseIndex.getAll(Node.ANY);
//            Iterator i = new ConcatenatedIterator(i1, i2);
//            while (i.hasNext()) {
//                ClausePointer cp = (ClausePointer) i.next();
//                if (firedRules.contains(cp.rule)) continue;
//                context.resetEnv();
//                TriplePattern trigger = (TriplePattern) cp.rule.getBodyElement(cp.index);
//                if (match(trigger, t, context.getEnvStack())) {
//                    context.setRule(cp.rule);
//                    if (matchRuleBody(cp.index, context)) {
//                        firedRules.add(cp.rule);
//                        nRulesFired++;
//                    }
//                }
//            }
//        }
    }
    
    /**
     * Compile a list of rules into the internal rule store representation.
     * @param rules the list of Rule objects
     * @param ignoreBrules set to true if rules written in backward notation should be ignored
     * @return an object that can be installed into the engine using setRuleStore.
     */
    public static RuleStore compile(List rules, boolean ignoreBrules) {
        OneToManyMap clauseIndex = new OneToManyMap();
        HashSet predicatesUsed = new HashSet();
        boolean wildcardRule = false;
            
        for (Iterator i = rules.iterator(); i.hasNext(); ) {
            Rule r = (Rule)i.next();
            if (ignoreBrules && r.isBackward()) continue;
            Object[] body = r.getBody();
            for (int j = 0; j < body.length; j++) {
                if (body[j] instanceof TriplePattern) {
                    Node predicate = ((TriplePattern) body[j]).getPredicate();
                    ClausePointer cp = new ClausePointer(r, j);
                    if (predicate.isVariable()) {
                        clauseIndex.put(Node.ANY, cp);
                        wildcardRule = true;
                    } else {
                        clauseIndex.put(predicate, cp);
                        if (! wildcardRule) {
                            predicatesUsed.add(predicate);
                        }
                    }
                }
            }
        }
            
        if (wildcardRule) predicatesUsed = null;
        return new RuleStore(clauseIndex, predicatesUsed, wildcardRule);
    }
    
    /**
     * Index the rule clauses by predicate.
     * @param ignoreBrules set to true if rules written in backward notation should be ignored
     */
    protected void buildClauseIndex(boolean ignoreBrules) {
        if (clauseIndex == null) {
            setRuleStore(compile(rules, ignoreBrules));
        }
    }
    
    /**
     * Scan the rules for any axioms and insert those
     */
    protected void findAndProcessAxioms() {
        BFRuleContext context = new BFRuleContext(infGraph);
        for (Iterator i = rules.iterator(); i.hasNext(); ) {
            Rule r = (Rule)i.next();
            if (r.bodyLength() == 0) {
                // An axiom
                for (int j = 0; j < r.headLength(); j++) {
                    Object head = r.getHeadElement(j);
                    if (head instanceof TriplePattern) {
                        TriplePattern h = (TriplePattern) head;
                        Triple t = new Triple(h.getSubject(), h.getPredicate(), h.getObject());
                        context.addTriple(t);
                        infGraph.getDeductionsGraph().add(t);
                    }
                }
            }
        }
        addSet(context);
        processedAxioms = true;
    }
    
    /**
     * Match each of a list of clauses in turn. For all bindings for which all
     * clauses match check the remaining clause guards and fire the rule actions.
     * @param clauses the list of clauses to match, start with last clause
     * @param context a context containing a set of new triples to be added
     * @return true if the rule actually fires
     */
//    private boolean matchClauseList(List clauses, BFRuleContext context) {
//        Rule rule = context.getRule();
//        BindingStack env = context.getEnvStack();
//        int index = clauses.size() - 1;
//        if (index == -1) {
//            // Check any non-pattern clauses 
//            for (int i = 0; i < rule.bodyLength(); i++) {
//                Object clause = rule.getBodyElement(i);
//                if (clause instanceof Functor) {
//                    // Fire a built in
//                    if (!((Functor)clause).evalAsBodyClause(context)) {
//                        return false;       // guard failed
//                    }
//                }
//            }
//            // Now fire the rule
//            if (infGraph.shouldTrace()) {
//                logger.info("Fired rule: " + rule.toShortString());
//            }
//            List matchList = null;
//            if (recordDerivations) {
//                // Create derivation record
//                matchList = new ArrayList(rule.bodyLength());
//                for (int i = 0; i < rule.bodyLength(); i++) {
//                    Object clause = rule.getBodyElement(i);
//                    if (clause instanceof TriplePattern) {
//                        matchList.add(instantiate((TriplePattern)clause, env));
//                    } 
//                }
//            }
//            for (int i = 0; i < rule.headLength(); i++) {
//                Object hClause = rule.getHeadElement(i);
//                if (hClause instanceof TriplePattern) {
//                    Triple t = instantiate((TriplePattern) hClause, env);
//                    if (!t.getSubject().isLiteral()) {
//                        // Only add the result if it is legal at the RDF level.
//                        // E.g. RDFS rules can create assertions about literals
//                        // that we can't record in RDF
//                        if ( ! context.contains(t)  ) {
//                            context.addPending(t);
//                            if (recordDerivations) {
//                                infGraph.logDerivation(t, new RuleDerivation(rule, t, matchList, infGraph));
//                            }
//                        }
//                    }
//                } else if (hClause instanceof Functor) {
//                    Functor f = (Functor)hClause;
//                    Builtin imp = f.getImplementor();
//                    if (imp != null) {
//                        imp.headAction(f.getBoundArgs(env), context);
//                    } else {
//                        throw new ReasonerException("Invoking undefined Functor " + f.getName() +" in " + rule.toShortString());
//                    }
//                } else if (hClause instanceof Rule) {
//                    Rule r = (Rule)hClause;
//                    if (r.isBackward()) {
//                        infGraph.addBRule(r.instantiate(env));
//                    } else {
//                        throw new ReasonerException("Found non-backward subrule : " + r); 
//                    }
//                }
//            }
//            return true;
//        }
//        // More clauses left to match ...
//        ArrayList clausesCopy = (ArrayList)((ArrayList)clauses).clone();
//        TriplePattern clause = (TriplePattern) clausesCopy.remove(index);
//        Node objPattern = env.getBinding(clause.getObject());
//        if (Functor.isFunctor(objPattern)) {
//            // Can't search on functor patterns so leave that as a wildcard
//            objPattern = null;
//        }
//        Iterator i = infGraph.findDataMatches(
//                            env.getBinding(clause.getSubject()),
//                            env.getBinding(clause.getPredicate()),
//                            env.getBinding(objPattern));
//        boolean foundMatch = false;
//        while (i.hasNext()) {
//            Triple t = (Triple) i.next();
//            // Add the bindings to the environment
//            env.push();
//            if (match(clause.getPredicate(), t.getPredicate(), env)
//                    && match(clause.getObject(), t.getObject(), env)
//                    && match(clause.getSubject(), t.getSubject(), env)) {
//                foundMatch |= matchClauseList(clausesCopy, context);
//            }
//            env.unwind();
//        }
//        return foundMatch;
//    }

//=======================================================================
// Inner classes

    /**
     * Structure used in the clause index to indicate a particular
     * clause in a rule. This is used purely as an internal data
     * structure so we just use direct field access.
     */
    protected static class ClausePointer {
        
        /** The rule containing this clause */
        protected Rule rule;
        
        /** The index of the clause in the rule body */
        protected int index;
        
        /** constructor */
        ClausePointer(Rule rule, int index) {
            this.rule = rule;
            this.index = index;
        }
        
        /** Get the clause pointed to */
        TriplePattern getClause() {
            return (TriplePattern)rule.getBodyElement(index);
        }
    }
    
    /**
     * Structure used to wrap up processed rule indexes.
     */
    public static class RuleStore {
    
        /** Map from predicate node to rule + clause, Node_ANY is used for wildcard predicates */
        protected OneToManyMap clauseIndex;
    
        /** List of predicates used in rules to assist in fast data loading */
        protected HashSet predicatesUsed;
    
        /** Flag, if true then there is a wildcard predicate in the rule set so that selective insert is not useful */
        protected boolean wildcardRule;
        
        /** Constructor */
        RuleStore(OneToManyMap clauseIndex, HashSet predicatesUsed, boolean wildcardRule) {
            this.clauseIndex = clauseIndex;
            this.predicatesUsed = predicatesUsed;
            this.wildcardRule = wildcardRule;
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