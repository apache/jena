/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.graph.*;

import java.util.*;

import com.hp.hpl.jena.util.OneToManyMap;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A RETE version of the the forward rule system engine. It neeeds to reference
 * an enclosing ForwardInfGraphI which holds the raw data and deductions.
 */
public class RETEEngine implements FRuleEngineI {
    
    /** The parent InfGraph which is employing this engine instance */
    protected ForwardRuleInfGraphI infGraph;
    
    /** Set of rules being used */
    protected List<Rule> rules;
    
    /** Map from predicate node to clause processor, Node_ANY is used for wildcard predicates */
    protected OneToManyMap<Node, RETENode> clauseIndex;
    
    /** Queue of newly added triples waiting to be processed */
    protected List<Triple> addsPending = new ArrayList<>();
    
    /** A HashSet of pending triples for faster lookup */
    protected HashSet<Triple> addsHash = new HashSet<>();
    
    
    /** Queue of newly deleted triples waiting to be processed */
    protected List<Triple> deletesPending = new ArrayList<>();
    
    
    /** The conflict set of rules waiting to fire */
    protected RETEConflictSet conflictSet;
    
    /** Map from predicates used in the rules to object value they are used with, can be Node.ANY */
//    protected Map<Node, Node> predicatePatterns;
    protected OneToManyMap<Node, Node> predicatePatterns;
    
    /** Flag, if true then there is a wildcard predicate in the rule set so that selective insert is not useful */
    protected boolean wildcardRule;
     
    /** Set to true to flag that derivations should be logged */
    protected boolean recordDerivations;
    
    /** performance stats - number of rules fired */
    long nRulesFired = 0;
    
    /** True if we have processed the axioms in the rule set */
    boolean processedAxioms = false;
    
    /** True if all the rules are monotonic, so we short circuit the conflict set processing */
    boolean isMonotonic = true;
    
    protected static Logger logger = LoggerFactory.getLogger(FRuleEngine.class);
    
//  =======================================================================
//  Constructors

    /**
     * Constructor.
     * @param parent the F or FB infGraph that it using this engine, the parent graph
     * holds the deductions graph and source data.
     * @param rules the rule set to be processed
     */
    public RETEEngine(ForwardRuleInfGraphI parent, List<Rule> rules) {
        infGraph = parent;
        this.rules = rules;
        // Check if this is a monotonic rule set
        isMonotonic = true;
        for ( Rule r : rules )
        {
            if ( !r.isMonotonic() )
            {
                isMonotonic = false;
                break;
            }
        }
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
     * @param inserts the set of triples to be processed, normally this is the
     * raw data graph but may include additional deductions made by preprocessing hooks
     */
    @Override
    public void init(boolean ignoreBrules, Finder inserts) {
        compile(rules, ignoreBrules);
        findAndProcessAxioms();
        fastInit(inserts);
    }
    
    /**
     * Process all available data. This version expects that all the axioms 
     * have already be preprocessed and the clause index already exists.
     * @param inserts the set of triples to be processed, normally this is the
     * raw data graph but may include additional deductions made by preprocessing hooks
     */
    @Override
    public void fastInit(Finder inserts) {
        conflictSet = new RETEConflictSet(new RETERuleContext(infGraph, this), isMonotonic);
        // Below is used during testing to ensure that all ruleset work (if less efficiently) if marked as non-monotonic
//        conflictSet = new RETEConflictSet(new RETERuleContext(infGraph, this), false);
        findAndProcessActions();
        if (infGraph.getRawGraph() != null) {
            // Insert the data
            if (wildcardRule) {
                for (Iterator<Triple> i = inserts.find(new TriplePattern(null, null, null)); i.hasNext(); ) {
                    addTriple( i.next(), false );
                }
            } else {
                for (Map.Entry<Node, Node> ent : predicatePatterns.entrySet()) {
//                    System.out.println("FastInit: " + ent.getKey() + " = " + ent.getValue());
                    for (Iterator<Triple> i = inserts.find(new TriplePattern(null, ent.getKey(), ent.getValue())); i.hasNext(); ) {
                        Triple t = i.next();
                        addTriple(t, false);
                    }
                }
            }
        }
        // Run the engine
        runAll();
    }

    /**
     * Add one triple to the data graph, run any rules triggered by
     * the new data item, recursively adding any generated triples.
     */
    @Override
    public synchronized void add(Triple t) {
        addTriple(t, false);
        runAll();
    }
    
    /**
     * Remove one triple to the data graph.
     * @return true if the effects could be correctly propagated or
     * false if not (in which case the entire engine should be restarted).
     */
    @Override
    public synchronized boolean delete(Triple t) {
        deleteTriple(t, false);
        runAll();
        return true;
    }
    
    /**
     * Return the number of rules fired since this rule engine instance
     * was created and initialized
     */
    @Override
    public long getNRulesFired() {
        return nRulesFired;
    }
    
    /**
     * Return true if the internal engine state means that tracing is worthwhile.
     * It will return false during the axiom bootstrap phase.
     */
    @Override
    public boolean shouldTrace() {
        return true;
//        return processedAxioms;
    }

    /**
     * Set to true to enable derivation caching
     */
    @Override
    public void setDerivationLogging(boolean recordDerivations) {
        this.recordDerivations = recordDerivations;
    }
    
    /**
     * Access the precomputed internal rule form. Used when precomputing the
     * internal axiom closures.
     */
    @Override
    public Object getRuleStore() {
        return new RuleStore(clauseIndex, predicatePatterns, wildcardRule, isMonotonic);
    }
    
    /**
     * Set the internal rule from from a precomputed state.
     */
    @Override
    public void setRuleStore(Object ruleStore) {
        RuleStore rs = (RuleStore)ruleStore;
        predicatePatterns = rs.predicatePatterns;
        wildcardRule = rs.wildcardRule;
        isMonotonic = rs.isMonotonic;
        
        // Clone the RETE network to this engine
        RETERuleContext context = new RETERuleContext(infGraph, this);
        Map<RETENode, RETENode> netCopy = new HashMap<>();
        clauseIndex = new OneToManyMap<>();
        for ( Map.Entry<Node, RETENode> entry : rs.clauseIndex.entrySet() )
        {
            clauseIndex.put( entry.getKey(), entry.getValue().clone( netCopy, context ) );
        }
    }
    
    /**
     * Add a rule firing request to the conflict set.
     */
    public void requestRuleFiring(Rule rule, BindingEnvironment env, boolean isAdd) {
        conflictSet.add(rule, env, isAdd);
    }
    
//  =======================================================================
//  Compiler support  

    /**
     * Compile a list of rules into the internal rule store representation.
     * @param rules the list of Rule objects
     * @param ignoreBrules set to true if rules written in backward notation should be ignored
     */
    public void compile(List<Rule> rules, boolean ignoreBrules) {
        clauseIndex = new OneToManyMap<>();
        predicatePatterns = new OneToManyMap<>();
        wildcardRule = false;

        for ( Rule rule : rules )
        {
            if ( ignoreBrules && rule.isBackward() )
            {
                continue;
            }

            int numVars = rule.getNumVars();
            boolean[] seenVar = new boolean[numVars];
            RETESourceNode prior = null;

            for ( int i = 0; i < rule.bodyLength(); i++ )
            {
                Object clause = rule.getBodyElement( i );
                if ( clause instanceof TriplePattern )
                {
                    // Create the filter node for this pattern
                    ArrayList<Node> clauseVars = new ArrayList<>( numVars );
                    RETEClauseFilter clauseNode =
                        RETEClauseFilter.compile( (TriplePattern) clause, numVars, clauseVars );
                    Node predicate = ( (TriplePattern) clause ).getPredicate();
                    Node object = ( (TriplePattern) clause ).getObject();
                    if ( predicate.isVariable() )
                    {
                        clauseIndex.put( Node.ANY, clauseNode );
                        wildcardRule = true;
                    }
                    else
                    {
                        clauseIndex.put( predicate, clauseNode );
                        if ( !wildcardRule )
                        {
                            if ( object.isVariable() )
                            {
                                object = Node.ANY;
                            }
                            if ( Functor.isFunctor( object ) )
                            {
                                object = Node.ANY;
                            }
                            recordPredicatePattern( predicate, object );
                        }
                    }

                    // Create list of variables which should be cross matched between the earlier clauses and this one
                    ArrayList<Byte> matchIndices = new ArrayList<>( numVars );
                    for ( Iterator<Node> iv = clauseVars.iterator(); iv.hasNext(); )
                    {
                        int varIndex = ( (Node_RuleVariable) iv.next() ).getIndex();
                        if ( seenVar[varIndex] )
                        {
                            matchIndices.add( new Byte( (byte) varIndex ) );
                        }
                        seenVar[varIndex] = true;
                    }

                    // Build the join node
                    if ( prior == null )
                    {
                        // First clause, no joins yet
                        prior = clauseNode;
                    }
                    else
                    {
                        RETEQueue leftQ = new RETEQueue( matchIndices );
                        RETEQueue rightQ = new RETEQueue( matchIndices );
                        leftQ.setSibling( rightQ );
                        rightQ.setSibling( leftQ );
                        clauseNode.setContinuation( rightQ );
                        prior.setContinuation( leftQ );
                        prior = leftQ;
                    }
                }
            }

            // Finished compiling a rule - add terminal 
            if ( prior != null )
            {
                RETETerminal term = createTerminal( rule );
                prior.setContinuation( term );
            }

        }
            
        if (wildcardRule) predicatePatterns = null;
    }    
    
    /**
     * Helper. Record a new predicate/value pattern. Ensure there is either
     * one wildcard or a number of specific patterns
     */
    private void recordPredicatePattern(Node predicate, Node value) {
        if (predicatePatterns.contains(predicate, Node.ANY)) {
            // already fully covered
        } else if (value.equals(Node.ANY)) {
            predicatePatterns.remove(predicate);
            predicatePatterns.put(predicate, value);
        } else {
            // TODO possibly introduce a threshold here for the number of patterns allowed before generalizing
            predicatePatterns.put(predicate, value);
        }
        return;
    }
    
    /**
     * Create a terminal node for the RETE network. Normally this is RETETerminal
     * but some people have experimented with alternative delete handling by
     * creating RETETerminal subclasses so this hook simplifies use of that
     * approach.
     */
    protected RETETerminal createTerminal(Rule rule) {
        return new RETETerminal(rule, this, infGraph);
    }
    
//  =======================================================================
//  Internal implementation methods

    /**
     * Add a new triple to the network. 
     * @param triple the new triple
     * @param deduction true if the triple has been generated by the rules and so should be 
     * added to the deductions graph.
     */
    public synchronized void addTriple(Triple triple, boolean deduction) {
        if (infGraph.shouldTrace()) {
            logger.debug("Add triple: " + PrintUtil.print(triple));
        }
        if (deletesPending.size() > 0) deletesPending.remove(triple);
        if (!addsHash.contains(triple)) {      // Experimental, not sure why it wasn't done before
            addsPending.add(triple);
            addsHash.add(triple);
        }
        if (deduction) {
            infGraph.addDeduction(triple);
        }
    }

    /**
     * Remove a new triple from the network. 
     * @param triple the new triple
     * @param deduction true if the remove has been generated by the rules 
     */
    public synchronized void deleteTriple(Triple triple, boolean deduction) {
        addsPending.remove(triple);
        addsHash.remove(triple);
        deletesPending.add(triple);
        if (deduction) {
            infGraph.getCurrentDeductionsGraph().delete(triple);
            Graph raw = infGraph.getRawGraph();
            // deduction retractions should not remove asserted facts, so commented out next line
            // raw.delete(triple);
            if (raw.contains(triple)) {
                // Built in a graph which can't delete this triple
                // so block further processing of this delete to avoid loops
                deletesPending.remove(triple);
            }
        }
    }
    
    /**
     * Increment the rule firing count, called by the terminal nodes in the
     * network.
     */
    protected void incRuleCount() {
        nRulesFired++;
    }
    
    /**
     * Find the next pending add triple.
     * @return the triple or null if there are none left.
     */
    protected synchronized Triple nextAddTriple() {
        int size = addsPending.size(); 
        if (size > 0) {
            Triple t=addsPending.remove(size - 1);
            addsHash.remove(t);
            return t;
        }
        return null;
    }
    
    /**
     * Find the next pending add triple.
     * @return the triple or null if there are none left.
     */
    protected synchronized Triple nextDeleteTriple() {
        int size = deletesPending.size(); 
        if (size > 0) {
            return deletesPending.remove(size - 1);
        }
        return null;
    }
        
    /**
     * Process the queue of pending insert/deletes until the queues are empty.
     * Public to simplify unit tests - not normally called directly.
     */
    public void runAll() {
        while(true) {
            boolean isAdd = false;
            Triple next = nextDeleteTriple();
            if (next == null) {
                next = nextAddTriple();
                isAdd = true;
            }
            if (next == null) {
                // Nothing more to inject, if this is a non-mon rule set now process one rule from the conflict set
                if (conflictSet.isEmpty()) return;   // Finished
                conflictSet.fireOne();
            } else {
                inject(next, isAdd);
            }
        }
    }
    
    /**
     * Inject a single triple into the RETE network
     */
    private void inject(Triple t, boolean isAdd) {
        if (infGraph.shouldTrace()) {
            logger.debug((isAdd ? "Inserting" : "Deleting") + " triple: " + PrintUtil.print(t));
        }
        Iterator<RETENode> i1 = clauseIndex.getAll(t.getPredicate());
        Iterator<RETENode> i2 = clauseIndex.getAll(Node.ANY);
        Iterator<RETENode> i = WrappedIterator.create(i1).andThen( i2 );
        while (i.hasNext()) {
            RETEClauseFilter cf = (RETEClauseFilter) i.next();
            // firedRules guard in here?
            cf.fire(t, isAdd);
        }
    }
    
    /**
     * This fires a triple into the current RETE network. 
     * This format of call is used in the unit testing but needs to be public
     * because the tester is in another package.
     */
    public void testTripleInsert(Triple t) {
        Iterator<RETENode> i1 = clauseIndex.getAll(t.getPredicate());
        Iterator<RETENode> i2 = clauseIndex.getAll(Node.ANY);
        Iterator<RETENode> i = WrappedIterator.create( i1 ).andThen( i2 );
        while (i.hasNext()) {
            RETEClauseFilter cf = (RETEClauseFilter) i.next();
            cf.fire(t, true);
        }
    }
    
    /**
     * Scan the rules for any axioms and insert those
     */
    protected void findAndProcessAxioms() {
        for ( Rule r : rules )
        {
            if ( r.isAxiom() )
            {
                // An axiom
                RETERuleContext context = new RETERuleContext( infGraph, this );
                context.setEnv( new BindingVector( new Node[r.getNumVars()] ) );
                context.setRule( r );
                if ( context.shouldFire( true ) )
                {
                    RETEConflictSet.execute( context, true );
                }
                /*   // Old version, left during debug and final checks
                for (int j = 0; j < r.headLength(); j++) {
                    Object head = r.getHeadElement(j);
                    if (head instanceof TriplePattern) {
                        TriplePattern h = (TriplePattern) head;
                        Triple t = new Triple(h.getSubject(), h.getPredicate(), h.getObject());
                        addTriple(t, true);
                    }
                }
                */
            }
        }
        processedAxioms = true;
    }
    
    /**
     * Scan the rules for any run actions and run those
     */
    protected void findAndProcessActions() {
        RETERuleContext tempContext = new RETERuleContext(infGraph, this);
        for ( Rule r : rules )
        {
            if ( r.bodyLength() == 0 )
            {
                for ( int j = 0; j < r.headLength(); j++ )
                {
                    Object head = r.getHeadElement( j );
                    if ( head instanceof Functor )
                    {
                        Functor f = (Functor) head;
                        Builtin imp = f.getImplementor();
                        if ( imp != null )
                        {
                            tempContext.setRule( r );
                            tempContext.setEnv( new BindingVector( r.getNumVars() ) );
                            imp.headAction( f.getArgs(), f.getArgLength(), tempContext );
                        }
                        else
                        {
                            throw new ReasonerException(
                                "Invoking undefined Functor " + f.getName() + " in " + r.toShortString() );
                        }
                    }
                }
            }
        }
    }
 
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
        protected OneToManyMap<Node, RETENode> clauseIndex;
    
        /** Map from predicates used in the rules to object value they are used with, can be Node.ANY */
        protected OneToManyMap<Node, Node> predicatePatterns;
    
        /** Flag, if true then there is a wildcard predicate in the rule set so that selective insert is not useful */
        protected boolean wildcardRule;
        
        /** True if all the rules are monotonic, so we short circuit the conflict set processing */
        protected boolean isMonotonic = true;
        
        /** Constructor */
        RuleStore(OneToManyMap<Node, RETENode> clauseIndex, OneToManyMap<Node, Node> predicatesPatterns, boolean wildcardRule, boolean isMonotonic) {
            this.clauseIndex = clauseIndex;
            this.predicatePatterns = predicatesPatterns;
            this.wildcardRule = wildcardRule;
            this.isMonotonic = isMonotonic;
        }
    }

}
