/******************************************************************
 * File:        BasicForwardRuleInfGraph.java
 * Created by:  Dave Reynolds
 * Created on:  30-Mar-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: BasicForwardRuleInfGraph.java,v 1.8 2003-05-15 21:33:35 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.impl.*;
import com.hp.hpl.jena.graph.*;
import java.util.*;

import com.hp.hpl.jena.util.OneToManyMap;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.RDF;

import org.apache.log4j.Logger;

/**
 * An inference graph interface that runs a set of forward chaining
 * rules to conclusion on each added triple and stores the entire
 * result set.
 * <p>
 * This implementation has a horribly inefficient rule chainer built in.
 * Once we have this working generalize this to an interface than
 * can call out to a rule engine and build a real rule engine (e.g. Rete style). </p>
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.8 $ on $Date: 2003-05-15 21:33:35 $
 */
public class BasicForwardRuleInfGraph extends BaseInfGraph {

//=======================================================================
// variables

    /** Map from predicate node to rule + clause, Node_ANY is used for wildcard predicates */
    protected OneToManyMap clauseIndex;
    
    /** Table of derivation records, maps from triple to RuleDerivation */
    protected OneToManyMap derivations;
    
    /** Set for rules being used */
    protected List rules;
    
    /** The set of deduced triples, this is in addition to base triples in the fdata graph */
    protected FGraph deductions;
    
    /** Reference to any schema graph data bound into the parent reasoner */
    protected InfGraph schemaGraph;
    
    /** performance stats - number of rules passing initial trigger */
    int nRulesTriggered = 0;
    
    /** performance stats - number of rules fired */
    long nRulesFired = 0;
    
    /** performance stats - number of rules fired during axiom initialization */
    long nAxiomRulesFired = -1;
    
    /** threshold on the numbers of rule firings allowed in a single operation */
    long nRulesThreshold = DEFAULT_RULES_THRESHOLD;

    /** Flag which, if true, enables tracing of rule actions to logger.info */
    boolean traceOn = false;
        
    /** Default setting for rules threshold */
    public static final long DEFAULT_RULES_THRESHOLD = 500000;
    
    /** True if we have processed the axioms in the rule set */
    boolean processedAxioms = false;
    
    /** log4j logger */
    protected static Logger logger = Logger.getLogger(BasicForwardRuleInfGraph.class);
    
//=======================================================================
// Core methods

    /**
     * Constructor. Creates a new inference graph based on the given rule set. 
     * No data graph is attached at this stage. This is to allow
     * any configuration parameters (such as logging) to be set before the data is added.
     * Note that until the data is added using {@link #bindData bindData} then any operations
     * like add, remove, find will result in errors.
     * 
     * @param reasoner the parent reasoner 
     * @param rules the list of rules to use this time
     */
    public BasicForwardRuleInfGraph(BasicForwardRuleReasoner reasoner, List rules) {
        super(null, reasoner);
        this.rules = rules;
        this.schemaGraph = reasoner.getSchemaGraph();
        buildClauseIndex();
    }    

    /**
     * Constructor. Creates a new inference graph based on the given rule set
     * then processes the initial data graph. No precomputed deductions are loaded.
     * 
     * @param reasoner the parent reasoner 
     * @param rules the list of rules to use this time
     * @param the data graph to be processed
     */
    public BasicForwardRuleInfGraph(BasicForwardRuleReasoner reasoner, List rules, Graph data) {
        this(reasoner, rules);
        rebind(data);
    }

    /**
     * Replace the underlying data graph for this inference graph and start any
     * inferences over again. This is primarily using in setting up ontology imports
     * processing to allow an imports multiunion graph to be inserted between the
     * inference graph and the raw data, before processing.
     * @param data the new raw data graph
     */
    public void rebind(Graph data) {
        fdata = new FGraph( data );
        rebind();
    }
    
    /**
     * Cause the inference graph to reconsult the underlying graph to take
     * into account changes. Normally changes are made through the InfGraph's add and
     * remove calls are will be handled appropriately. However, in some cases changes
     * are made "behind the InfGraph's back" and this forces a full reconsult of
     * the changed data. 
     */
    public void rebind() {
        isPrepared = false;
    }
    
    /**
     * Perform any initial processing and caching. This call is optional. Most
     * engines either have negligable set up work or will perform an implicit
     * "prepare" if necessary. The call is provided for those occasions where
     * substantial preparation work is possible (e.g. running a forward chaining
     * rule system) and where an application might wish greater control over when
     * this prepration is done.
     */
    public synchronized void prepare() {
        isPrepared = true;
        // initilize the deductions with axiomatic data ...
        deductions = new FGraph( new GraphMem() );
        findAndProcessAxioms();
        nAxiomRulesFired = nRulesFired;
        logger.debug("Axioms fired " + nAxiomRulesFired + " rules");
        // ... and with schema data
        if (schemaGraph != null) {
            preloadDeductions(schemaGraph);
        }
        // Create the reasoning context
        Graph data = fdata.getGraph();
        BFRuleContext context = new BFRuleContext(this);
        // Insert the data
        // TODO This could do a set of searches over just the data which might match a rule
        for (Iterator i = data.find(null, null, null); i.hasNext(); ) {
            context.addTriple((Triple)i.next());
        }
        // Run the engine
        addSet(context);
    }

    /**
     * Adds a set of precomputed triples to the deductions store. These do not, themselves,
     * fire any rules but provide additional axioms that might enable future rule
     * firing when real data is added. Used to implement bindSchema processing
     * in the parent Reasoner.
     */
    public void preloadDeductions(Graph preload) {
        Graph d = deductions.getGraph();
        for (Iterator i = preload.find(null, null, null); i.hasNext(); ) {
            d.add((Triple)i.next());
        }
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
        if (!processedAxioms) {
            // This occurs during processing if there are axioms in the rules but we haven't yet
            // bound an actual data graph
            return deductions.findWithContinuation(pattern, continuation);
        } else {
            if (continuation == null) {
                return fdata.findWithContinuation(pattern, deductions);
            } else {
                return fdata.findWithContinuation(pattern, FinderUtil.cascade(deductions, continuation) );
            }
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
     * Add one triple to the data graph, run any rules triggered by
     * the new data item, recursively adding any generated triples.
     */
    public synchronized void add(Triple t) {
        if (!isPrepared) prepare();
        BFRuleContext context = new BFRuleContext(this);
        context.addTriple(t);
        fdata.getGraph().add(t);
        addSet(context);
    }
    
    /**
     * Returns the bitwise or of ADD, DELETE, SIZE and ORDERED,
     * to show the capabilities of this implementation of Graph.
     * So a read-only graph that finds in an unordered fashion,
     * but can tell you how many triples are in the graph returns
     * SIZE.
     */
    public int capabilities() {
        return ADD | SIZE | DELETE;
    }

    /**
     * Return the number of triples in the inferred graph
     */
    public int size() {
        return fdata.getGraph().size() + deductions.getGraph().size();
    }
    
    /** 
     * Removes the triple t (if possible) from the set belonging to this graph. 
     */   
    public void delete(Triple t) {
        if (!isPrepared) prepare();
        if (fdata != null) {
            Graph data = fdata.getGraph();
            if (data != null) {
                data.delete(t);
            }
        }
        deductions.getGraph().delete(t);
    }
    
//=======================================================================
// support for proof traces

    /**
     * Set to true to enable derivation caching
     */
    public void setDerivationLogging(boolean recordDerivations) {
        this.recordDerivations = recordDerivations;
        if (recordDerivations) {
            derivations = new OneToManyMap();
        } else {
            derivations = null;
        }
    }
    
    /**
     * Return the derivation of at triple.
     * The derivation is a List of DerivationRecords
     */
    public Iterator getDerivation(Triple t) {
        return derivations.getAll(t);
    }
    
    /**
     * Change the threshold on the number of rule firings 
     * allowed during a single operation.
     * @param threshold the new cutoff on the number rules firings per external op
     */
    public void setRuleThreshold(long threshold) {
        nRulesThreshold = threshold;
    }
    
    /**
     * Set the state of the trace flag. If set to true then rule firings
     * are logged out to the Logger at "INFO" level.
     */
    public void setTraceOn(boolean state) {
        traceOn = state;
    }
    
    /**
     * Return true if tracing should be acted on - i.e. if traceOn is true
     * and we are past the bootstrap phase.
     */
    public boolean shouldTrace() {
        return traceOn && processedAxioms;
    }
    
//=======================================================================
// Rule engine

    /**
     * Add a set of new triple to the data graph, run any rules triggered by
     * the new data item, recursively adding any generated triples.
     * Technically the triples having been physically added to either the
     * base or deduction graphs and the job of this function is just to
     * process the stack of additions firing any relevant rules.
     * @param context a context containing a set of new triples to be added
     */
    protected void addSet(BFRuleContext context) {
        long cutoff = nRulesFired + nRulesThreshold;
        Triple t;
        while ((t = context.getNextTriple()) != null) {
            if (traceOn && processedAxioms) {
                logger.info("Processing: " + PrintUtil.print(t));
            }
            // Check for rule triggers
            HashSet firedRules = new HashSet();
            Iterator i1 = clauseIndex.getAll(t.getPredicate());
            Iterator i2 = clauseIndex.getAll(Node.ANY);
            Iterator i = new ConcatenatedIterator(i1, i2);
            while (i.hasNext()) {
                ClausePointer cp = (ClausePointer) i.next();
                if (firedRules.contains(cp.rule)) continue;
                context.resetEnv();
                TriplePattern trigger = (TriplePattern) cp.rule.getBodyElement(cp.index);
                if (match(trigger, t, context.getEnvStack())) {
                    nRulesTriggered++;
                    context.setRule(cp.rule);
                    if (matchRuleBody(cp.index, context)) {
                        firedRules.add(cp.rule);
                        nRulesFired++;
                    }
                }
            }
            if (nRulesFired > cutoff) {
                throw new BFRException("Add operation aborted - too many rule firings required, assuming stuck in a reasoning loop."
                                      + "\nWarning - graph may be in an indeterminate state");
            }
        }
    }
    
    /**
     * Return the number of rules fired since this rule engine instance
     * was created and initialized
     */
    public long getNRulesFired() {
        return nRulesFired;
    }
    
    /**
     * Index the rule clauses by predicate.
     */
    protected void buildClauseIndex() {
        clauseIndex = new OneToManyMap();
        
        for (Iterator i = rules.iterator(); i.hasNext(); ) {
            Rule r = (Rule)i.next();
            Object[] body = r.getBody();
            for (int j = 0; j < body.length; j++) {
                if (body[j] instanceof TriplePattern) {
                    Node predicate = ((TriplePattern) body[j]).getPredicate();
                    ClausePointer cp = new ClausePointer(r, j);
                    if (predicate instanceof Node_ANY || predicate instanceof Node_RuleVariable) {
                        clauseIndex.put(Node.ANY, cp);
                    } else {
                        clauseIndex.put(predicate, cp);
                    }
                }
            }
        }
    }
    
    /**
     * Scan the rules for any axioms and insert those
     */
    protected void findAndProcessAxioms() {
        BFRuleContext context = new BFRuleContext(this);
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
                        deductions.getGraph().add(t);
                    }
                }
            }
        }
        addSet(context);
        processedAxioms = true;
    }
    
    /**
     * Match the rest of a set of rule clauses once an initial rule
     * trigger has fired. Carries out any actions as a side effect.
     * @param trigger the index of the clause which has already be successfully matched
     * @param context a context containing a set of new triples to be added
     * @return true if the rule actually fires
     */
    private boolean matchRuleBody(int trigger, BFRuleContext context) {
        Rule rule = context.getRule();
        // Create an ordered list of body clauses to process, best at the end
        Object[] body = rule.getBody();
        int len = body.length;
        ArrayList clauses = new ArrayList(len-1);
        
        if (len <= 1) {
            // No clauses to add, just fall through to clause matcher
        } else if (len == 2) {
            // Only one clause remaining, no reordering necessary
            Object clause = body[trigger == 0 ? 1 : 0];
            if (clause instanceof TriplePattern) {
                clauses.add(clause);
            }
         } else {
            // Pick most bound remaining clause as the best one to go first
            int bestscore = 0;
            int best = -1;
            for (int i = 0; i < len; i++) {
                if (i == trigger) continue; // Skip the clause already processed
                BindingStack env = context.getEnvStack();
                if (body[i] instanceof TriplePattern) {
                    TriplePattern clause = (TriplePattern) body[i];
                    int score = scoreNodeBoundness(clause.getSubject(), env) * 3 +
                                 scoreNodeBoundness(clause.getPredicate(), env) * 2 +
                                 scoreNodeBoundness(clause.getObject(), env) * 3;
                    if (score > bestscore) {
                        bestscore = score;
                        best = i;
                    }
                }
            }
            
            for (int i = 0; i < len; i++) {
                if (i == trigger || i == best) continue;
                if (body[i] instanceof TriplePattern) {
                    clauses.add(body[i]);
                }
            }
            if (best != -1) clauses.add(body[best]);
        }
        
        // Call a recursive clause matcher in the ordered list of clauses   
        boolean matched = matchClauseList(clauses, context);
        if (matched) {
            // We have new deductions stashed which now want to be
            // asserted as deductions and then added to processing stack
            context.flushPending(deductions.getGraph());
        }
        return matched;
    }
    
    /**
     * Match each of a list of clauses in turn. For all bindings for which all
     * clauses match check the remaining clause guards and fire the rule actions.
     * @param clauses the list of clauses to match, start with last clause
     * @param context a context containing a set of new triples to be added
     * @return true if the rule actually fires
     */
    private boolean matchClauseList(List clauses, BFRuleContext context) {
        Rule rule = context.getRule();
        BindingStack env = context.getEnvStack();
        int index = clauses.size() - 1;
        if (index == -1) {
            // Check any non-pattern clauses 
            // TODO refactor this by a complete restructure of the match loop
            for (int i = 0; i < rule.bodyLength(); i++) {
                Object clause = rule.getBodyElement(i);
                if (clause instanceof Functor) {
                    // Fire a built in
                    if (!((Functor)clause).evalAsBodyClause(context)) {
                        return false;       // guard failed
                    }
                }
            }
            // Now fire the rule
            if (traceOn && processedAxioms) {
                logger.info("Fired rule: " + rule.toShortString());
            }
            List matchList = null;
            if (recordDerivations) {
                // Create derivation record
                matchList = new ArrayList(rule.bodyLength());
                for (int i = 0; i < rule.bodyLength(); i++) {
                    Object clause = rule.getBodyElement(i);
                    if (clause instanceof TriplePattern) {
                        matchList.add(instantiate((TriplePattern)clause, env));
                    } 
                }
            }
            for (int i = 0; i < rule.headLength(); i++) {
                Object hClause = rule.getHeadElement(i);
                if (hClause instanceof TriplePattern) {
                    Triple t = instantiate((TriplePattern) hClause, env);
                    if (!t.getSubject().isLiteral()) {
                        // Only add the result if it is legal at the RDF level.
                        // E.g. RDFS rules can create assertions about literals
                        // that we can't record in RDF
                        if ( ! context.contains(t)  ) {
                            context.addPending(t);
                            if (recordDerivations) {
                                derivations.put(t, new RuleDerivation(rule, t, matchList, this));
                            }
                        }
                    }
                } else if (hClause instanceof Functor) {
                    Functor f = (Functor)hClause;
                    Builtin imp = f.getImplementor();
                    if (imp != null) {
                        imp.headAction(f.getBoundArgs(env), context);
                    } else {
                        logger.warn("Invoking undefined Functor " + f.getName() +" in " + rule.toShortString());
                    }
                }
            }
            return true;
        }
        // More clauses left to match ...
        ArrayList clausesCopy = (ArrayList)((ArrayList)clauses).clone();
        TriplePattern clause = (TriplePattern) clausesCopy.remove(index);
        Node objPattern = env.getBinding(clause.getObject());
        if (Functor.isFunctor(objPattern)) {
            // Can't search on functor patterns so leave that as a wildcard
            objPattern = null;
        }
        Iterator i = find(
                            env.getBinding(clause.getSubject()),
                            env.getBinding(clause.getPredicate()),
                            env.getBinding(objPattern));
        boolean foundMatch = false;
        while (i.hasNext()) {
            Triple t = (Triple) i.next();
            // Add the bindings to the environment
            env.push();
            if (match(clause.getPredicate(), t.getPredicate(), env)
                    && match(clause.getObject(), t.getObject(), env)
                    && match(clause.getSubject(), t.getSubject(), env)) {
                foundMatch |= matchClauseList(clausesCopy, context);
            }
            env.unwind();
        }
        return foundMatch;
    }

    /**
     * Score a Node in terms of groundedness - heuristic.
     * Treats a variable as better than a wildcard because it constrains
     * later clauses. Treats rdf:type as worse than any other ground node
     * because that tends to link to lots of expensive rules.
     */
    public static int scoreNodeBoundness(Node n, BindingEnvironment env) {
        if (n instanceof Node_ANY) {
            return 0;
        } else if (n instanceof Node_RuleVariable) {
            Node val = env.getGroundVersion(n);
            if (val == null) {
                return 1;
            } else if (val.equals(RDF.type.asNode())) {
                return 2;
            } else {
                return 3;
            }
        } else {
            return 3;
        }
    }
    
    /**
     * Instantiate a triple pattern against the current environment.
     * This version handles unbound varibles by turning them into bNodes.
     * @param clause the triple pattern to match
     * @param env the current binding environment
     * @return a new, instantiated triple
     */
    public static Triple instantiate(TriplePattern pattern, BindingStack env) {
        Node s = env.getBinding(pattern.getSubject());
        if (s == null) s = Node.createAnon();
        Node p = env.getBinding(pattern.getPredicate());
        if (p == null) p = Node.createAnon();
        Node o = env.getBinding(pattern.getObject());
        if (o == null) o = Node.createAnon();
        return new Triple(s, p, o);
    }
    
    /**
     * Test if a TriplePattern matches a Triple in the given binding
     * environment. If it does then the binding environment is modified
     * the reflect any additional bindings.
     * @return true if the pattern matches the triple
     */
    public static boolean match(TriplePattern pattern, Triple triple, BindingStack env) {
        env.push();
        boolean matchOK = match(pattern.getPredicate(), triple.getPredicate(), env)
                        && match(pattern.getObject(), triple.getObject(), env)
                        && match(pattern.getSubject(), triple.getSubject(), env);
        if (matchOK) {
            env.commit();
            return true;
        } else {
            env.unwind();
            return false;
        }
    }
    
    /**
     * Test if a pattern Node matches a Triple Node in the given binding
     * environment. If it does then the binding environment is modified
     * the reflect any additional bindings.
     * @return true if the pattern matches the node
     */
    public static boolean match(Node pattern, Node node, BindingStack env) {
        if (pattern instanceof Node_RuleVariable) {
            int index = ((Node_RuleVariable)pattern).getIndex();
            return env.bind(index, node);
        } else if (pattern instanceof Node_ANY) {
            return true;
        } else if (Functor.isFunctor(pattern)) {
            if (!Functor.isFunctor(node)) return false;
            Functor patternF = (Functor) pattern.getLiteral().getValue();
            Functor nodeF = (Functor) node.getLiteral().getValue();
            if (!patternF.getName().equals(nodeF.getName())) return false;
            Node[] patternArgs = patternF.getArgs();
            Node[] nodeArgs = nodeF.getArgs();
            if (patternF.isGround()) {
                return Arrays.equals(patternArgs, nodeArgs);
            } else {
                if (patternArgs.length != nodeArgs.length) return false;
                // Compatible functor shapes so bind an embedded variables in the pattern
                env.push();
                boolean matchOK = true;
                for (int i = 0; i < patternArgs.length; i++) {
                    if (!match(patternArgs[i], nodeArgs[i], env)) {
                        matchOK = false;
                        break;
                    }
                }
                if (matchOK) {
                    env.commit();
                    return true;
                } else {
                    env.unwind();
                    return false;
                }
            }
        } else {
            return pattern.sameValueAs(node);
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
     * Generic exception used to report problems with rule semantics
     */
    public static class BFRException extends RuntimeException {
        
        public BFRException(String msg) {
            super(msg);
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
