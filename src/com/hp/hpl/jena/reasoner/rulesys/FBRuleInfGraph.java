/******************************************************************
 * File:        FBRuleInfGraph.java
 * Created by:  Dave Reynolds
 * Created on:  28-May-2003
 * 
 * (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: FBRuleInfGraph.java,v 1.48 2005-02-18 15:33:35 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.reasoner.rulesys.impl.*;
import com.hp.hpl.jena.reasoner.transitiveReasoner.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.shared.impl.JenaParameters;
import com.hp.hpl.jena.graph.*;

import java.util.*;

//import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.util.OneToManyMap;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An inference graph that uses a mixture of forward and backward
 * chaining rules. The forward rules can create direct deductions from
 * the source data and schema and can also create backward rules. A
 * query is answered by consulting the union of the raw data, the forward
 * derived results and any relevant backward rules (whose answers are tabled
 * for future reference).
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.48 $ on $Date: 2005-02-18 15:33:35 $
 */
public class FBRuleInfGraph  extends BasicForwardRuleInfGraph implements BackwardRuleInfGraphI, Filter {
    
    /** Single context for the reasoner, used when passing information to builtins */
    protected BBRuleContext context;
     
    /** A finder that searches across the data, schema, axioms and forward deductions*/
    protected Finder dataFind;
    
    /** The core backward rule engine which includes all the memoized results */
    protected LPBRuleEngine bEngine;
    
    /** The original rule set as supplied */
    protected List rawRules;
    
    /** The rule list after possible extension by preprocessing hooks */
    protected List rules;
    
    /** Static switch from Basic to RETE implementation of the forward component */
    public static boolean useRETE = true;

    /** Flag, if true then subClass and subProperty lattices will be optimized using TGCs */
    protected boolean useTGCCaching = false;
    
    /** Flag, if true then find results will be filtered to remove functors and illegal RDF */
    public boolean filterFunctors = true;
    
    /** Optional precomputed cache of the subClass/subproperty lattices */
    protected TransitiveEngine transitiveEngine;
    
    /** Optional list of preprocessing hooks  to be run in sequence during preparation time */
    protected List preprocessorHooks;
    
    /** Cache of temporary property values inferred through getTemp calls */
    protected TempNodeCache tempNodecache;
    
    /** Table of temp nodes which should be hidden from output listings */
    protected Set hiddenNodes;
    
    static Log logger = LogFactory.getLog(FBRuleInfGraph.class);

//  =======================================================================
//  Constructors

    /**
     * Constructor.
     * @param reasoner the reasoner which created this inf graph instance
     * @param schema the (optional) schema graph to be included
     */
    public FBRuleInfGraph(Reasoner reasoner, Graph schema) {
        super(reasoner, schema);
        constructorInit(schema);    
    }

    /**
     * Constructor.
     * @param reasoner the reasoner which created this inf graph instance
     * @param rules the rules to process
     * @param schema the (optional) schema graph to be included
     */
    public FBRuleInfGraph(Reasoner reasoner, List rules, Graph schema) {
        super(reasoner, rules, schema);
        this.rawRules = rules;
        constructorInit(schema);    
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
        this.rawRules = rules;  
        constructorInit(schema);    
    }

    /**
     * Common pieces of initialization code which apply in all constructor cases.
     */
    private void constructorInit(Graph schema) {
        initLP(schema);  
        tempNodecache = new TempNodeCache(this);
        if (JenaParameters.enableFilteringOfHiddenInfNodes) {
            hiddenNodes = new HashSet();
            if (schema != null && schema instanceof FBRuleInfGraph) {
                hiddenNodes.addAll(((FBRuleInfGraph)schema).hiddenNodes);
            }
        }
    }
    
    /**
     * Instantiate the forward rule engine to use.
     * Subclasses can override this to switch to, say, a RETE imlementation.
     * @param rules the rule set or null if there are not rules bound in yet.
     */
    protected void instantiateRuleEngine(List rules) {
        if (rules != null) {
            if (useRETE) {
                engine = new RETEEngine(this, rules);
            } else {
                engine = new FRuleEngine(this, rules);
            }
        } else {
            if (useRETE) {
                engine = new RETEEngine(this);
            } else {
                engine = new FRuleEngine(this);
            }
        }
    }

    /**
     * Initialize the LP engine, based on an optional schema graph.
     */    
    private void initLP(Graph schema) {
        if (schema != null && schema instanceof FBRuleInfGraph) {
            LPRuleStore newStore = new LPRuleStore();
            newStore.addAll(((FBRuleInfGraph)schema).bEngine.getRuleStore());
            bEngine = new LPBRuleEngine(this, newStore);
        } else {
            bEngine = new LPBRuleEngine(this);
        }
    }
    
    /**
     * Instantiate the optional caches for the subclass/suproperty lattices.
     * Unless this call is made the TGC caching will not be used.
     */
    public void setUseTGCCache() {
        useTGCCaching = true;
        if (schemaGraph != null) {
            transitiveEngine = new TransitiveEngine(((FBRuleInfGraph)schemaGraph).transitiveEngine);
        } else {
            transitiveEngine = new TransitiveEngine(
                new TransitiveGraphCache(ReasonerVocabulary.directSubClassOf.asNode(), RDFS.subClassOf.asNode()),
                new TransitiveGraphCache(ReasonerVocabulary.directSubPropertyOf.asNode(), RDFS.subPropertyOf.asNode()));
        }
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
    public boolean processBuiltin(ClauseEntry clause, Rule rule, BindingEnvironment env) {
        throw new ReasonerException("Internal error in FBLP rule engine, incorrect invocation of building in rule " + rule);
        // TODO: Remove 
//        if (clause instanceof Functor) {
//            context.setEnv(env);
//            context.setRule(rule);
//            return((Functor)clause).evalAsBodyClause(context);
//        } else {
//            throw new ReasonerException("Illegal builtin predicate: " + clause + " in rule " + rule);
//        }
    }
    
    /**
     * Adds a new Backward rule as a rusult of a forward rule process. Only some
     * infgraphs support this.
     */
    public void addBRule(Rule brule) {
        logger.debug("Adding rule " + brule);
        bEngine.addRule(brule);
        bEngine.reset();
    }
       
    /**
     * Deletes a new Backward rule as a rules of a forward rule process. Only some
     * infgraphs support this.
     */
    public void deleteBRule(Rule brule) {
        logger.debug("Deleting rule " + brule);
        bEngine.deleteRule(brule);
        bEngine.reset();
    }
    
    /**
     * Adds a set of new Backward rules
     */
    public void addBRules(List rules) {
        for (Iterator i = rules.iterator(); i.hasNext(); ) {
            Rule rule = (Rule)i.next();
//            logger.debug("Adding rule " + rule);
            bEngine.addRule(rule);
        }
        bEngine.reset();
    }
    
    /**
     * Return an ordered list of all registered backward rules. Includes those
     * generated by forward productions.
     */
    public List getBRules() {
        return bEngine.getAllRules();
    }
    
    /**
     * Return the originally supplied set of rules, may be a mix of forward
     * and backward rules.
     */
    public List getRules() {
        return rules;
    }
      
    /**
     * Set a predicate to be tabled/memoized by the LP engine. 
     */
    public void setTabled(Node predicate) {
        bEngine.tablePredicate(predicate);
        if (traceOn) {
            logger.info("LP TABLE " + predicate);
        }
    }
    
    /**
     * Return a compiled representation of all the registered
     * forward rules.
     */
    private Object getForwardRuleStore() {
        return engine.getRuleStore();
    }
    
    /**
     * Add a new deduction to the deductions graph.
     */
    public void addDeduction(Triple t) {
        getDeductionsGraph().add(t);
        if (useTGCCaching) {
            transitiveEngine.add(t);
        }
    }

    /**
     * Retrieve or create a bNode representing an inferred property value.
     * @param instance the base instance node to which the property applies
     * @param prop the property node whose value is being inferred
     * @param pclass the (optional, can be null) class for the inferred value.
     * @return the bNode representing the property value 
     */
    public Node getTemp(Node instance, Node prop, Node pclass) {
        return tempNodecache.getTemp(instance, prop, pclass);
    }
   
//  =======================================================================
//  Core inf graph methods
    
    /**
     * Add a new rule to the rule set. This should only be used by implementations
     * of RuleProprocessHook (which are called during rule system preparation phase).
     * If called at other times the rule won't be correctly transferred into the
     * underlying engines.
     */
    public void addRuleDuringPrepare(Rule rule) {
        if (rules == rawRules) {
            // Ensure the original is preserved in case we need to do a restart
            if (rawRules instanceof ArrayList) {
                rules = (ArrayList) ((ArrayList)rawRules).clone();
            } else {
                rules = new ArrayList(rawRules);
            }
            // Rebuild the forward engine to use the cloned rules
            instantiateRuleEngine(rules);
        }
        rules.add(rule);
    }
    
    /**
     * Add a new preprocessing hook defining an operation that
     * should be run when the preparation phase is underway.
     */
    public void addPreprocessingHook(RulePreprocessHook hook) {
        if (preprocessorHooks == null) {
            preprocessorHooks = new ArrayList();
        }
        preprocessorHooks.add(hook);
    }
    
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
            
            // Restore the original pre-hookProcess rules
            rules = rawRules;
            
            // Is there any data to bind in yet?
            Graph data = null;
            if (fdata != null) data = fdata.getGraph();
            
            // initilize the deductions graph
            fdeductions = new FGraph( new GraphMem() );
            dataFind = (data == null) ? fdeductions :  FinderUtil.cascade(fdeductions, fdata);
            Finder dataSource = fdata;
            
            // Initialize the optional TGC caches
            if (useTGCCaching) {
                if (schemaGraph != null) {
                    // Check if we can just reuse the copy of the raw 
                    if (
                        (transitiveEngine.checkOccurance(TransitiveReasoner.subPropertyOf, data) ||
                         transitiveEngine.checkOccurance(TransitiveReasoner.subClassOf, data) ||
                         transitiveEngine.checkOccurance(RDFS.domain.asNode(), data) ||
                         transitiveEngine.checkOccurance(RDFS.range.asNode(), data) )) {
                
                        // The data graph contains some ontology knowledge so split the caches
                        // now and rebuild them using merged data
                        transitiveEngine.insert(((FBRuleInfGraph)schemaGraph).fdata, fdata);
                    }     
                } else {
                    if (data != null) {
                        transitiveEngine.insert(null, fdata);
                    }
                }
                // Insert any axiomatic statements into the caches
                for (Iterator i = rules.iterator(); i.hasNext(); ) {
                    Rule r = (Rule)i.next();
                    if (r.bodyLength() == 0) {
                        // An axiom
                        for (int j = 0; j < r.headLength(); j++) {
                            Object head = r.getHeadElement(j);
                            if (head instanceof TriplePattern) {
                                TriplePattern h = (TriplePattern) head;
                                transitiveEngine.add(h.asTriple());
                            }
                        }
                    }
                }

                transitiveEngine.setCaching(true, true);
//                dataFind = FinderUtil.cascade(subClassCache, subPropertyCache, dataFind);
                dataFind = FinderUtil.cascade(dataFind, transitiveEngine.getSubClassCache(), transitiveEngine.getSubPropertyCache());
                
                // Without the next statement then the transitive closures are not seen by the forward rules
                dataSource = FinderUtil.cascade(dataSource, transitiveEngine.getSubClassCache(), transitiveEngine.getSubPropertyCache());
            }
            
            // Make sure there are no Brules left over from pior runs
            bEngine.deleteAllRules();

            // Call any optional preprocessing hook
            if (preprocessorHooks != null && preprocessorHooks.size() > 0) {
                Graph inserts = new GraphMem();
                for (Iterator i = preprocessorHooks.iterator(); i.hasNext(); ) {
                    RulePreprocessHook hook = (RulePreprocessHook)i.next();
                    hook.run(this, dataFind, inserts);
                }
                if (inserts.size() > 0) {
                    FGraph finserts = new FGraph(inserts);
                    dataSource = FinderUtil.cascade(fdata, finserts);
                    dataFind = FinderUtil.cascade(dataFind, finserts);
                }
            }
            
            boolean rulesLoaded = false;
            if (schemaGraph != null) {
                Graph rawPreload = ((InfGraph)schemaGraph).getRawGraph();
                if (rawPreload != null) {
                    dataFind = FinderUtil.cascade(dataFind, new FGraph(rawPreload));
                }
                rulesLoaded = preloadDeductions(schemaGraph);
            }
            if (rulesLoaded) {
                engine.fastInit(dataSource);
            } else {
                // No preload so do the rule separation
                addBRules(extractPureBackwardRules(rules));
                engine.init(true, dataSource);
            }
            // Prepare the context for builtins run in backwards engine
            context = new BBRuleContext(this);
            
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
     * are logged out to the Log at "INFO" level.
     */
    public void setTraceOn(boolean state) {
        super.setTraceOn(state);
        bEngine.setTraceOn(state);
    }

    /**
     * Set to true to enable derivation caching
     */
    public void setDerivationLogging(boolean recordDerivations) {
        this.recordDerivations = recordDerivations;
        engine.setDerivationLogging(recordDerivations);
        bEngine.setDerivationLogging(recordDerivations);
        if (recordDerivations) {
            derivations = new OneToManyMap();
        } else {
            derivations = null;
        }
    }
   
    /**
     * Set to true to cause functor-valued literals to be dropped from rule output.
     * Default is true.
     */
    public void setFunctorFiltering(boolean param) {
        filterFunctors = param;
    }
    
    /**
     * Return the number of rules fired since this rule engine instance
     * was created and initialized. The current implementation only counts
     * forward rules and does not track dynamic backward rules needed for
     * specific queries.
     */
    public long getNRulesFired() {
        return engine.getNRulesFired();
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
        checkOpen();
        if (!isPrepared) prepare();
        ExtendedIterator result = new UniqueExtendedIterator(bEngine.find(pattern));
        if (continuation != null) {
            result = result.andThen(continuation.find(pattern));
        }
        if (filterFunctors) {
//            return result.filterDrop(Functor.acceptFilter);
            return result.filterDrop(this);
        } else {
            return result;
        }
    }
    
    /**
     * Internal variant of find which omits the filters which block illegal RDF data.
     * @param pattern a TriplePattern to be matched against the data
     */
    public ExtendedIterator findFull(TriplePattern pattern) {
        checkOpen();
        if (!isPrepared) prepare();
        return new UniqueExtendedIterator(bEngine.find(pattern));
    }
   
    /** 
     * Returns an iterator over Triples.
     * This implementation assumes that the underlying findWithContinuation 
     * will have also consulted the raw data.
     */
    public ExtendedIterator graphBaseFind(Node subject, Node property, Node object) {
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
     * Add one triple to the data graph, run any rules triggered by
     * the new data item, recursively adding any generated triples.
     */
    public synchronized void performAdd(Triple t) {
        fdata.getGraph().add(t);
        if (useTGCCaching) {
            if (transitiveEngine.add(t)) isPrepared = false;
        }
        if (isPrepared) {
            engine.add(t);
        }
        bEngine.reset();
    }

    /** 
     * Removes the triple t (if possible) from the set belonging to this graph. 
     */   
    public void performDelete(Triple t) {
        boolean removeIsFromBase = fdata.getGraph().contains(t);
        fdata.getGraph().delete(t);
        if (useTGCCaching) {
            if (transitiveEngine.delete(t)) {
                if (isPrepared) {
                    bEngine.deleteAllRules();
                }
                isPrepared = false;
            }
        } 
        // Full incremental remove processing requires reference counting
        // of all deductions. It's not clear the cost of maintaining the
        // reference counts is worth it so the current implementation
        // forces a recompute if any external deletes are performed.
        if (isPrepared) {
            bEngine.deleteAllRules();
            isPrepared = false;
            // Re-enable the code below when/if ref counting is added and remove above
            // if (removeIsFromBase) engine.delete(t);
        }
        bEngine.reset();
    }
    
    /**
     * Return a new inference graph which is a clone of the current graph
     * together with an additional set of data premises. Attempts to the replace
     * the default brute force implementation by one that can reuse some of the
     * existing deductions.
     */
    public InfGraph cloneWithPremises(Graph premises) {
        prepare();
        FBRuleInfGraph graph = new FBRuleInfGraph(getReasoner(), rawRules, this);
        if (useTGCCaching) graph.setUseTGCCache();
        graph.setDerivationLogging(recordDerivations);
        graph.setTraceOn(traceOn);
        // Implementation note:  whilst current tests pass its not clear that 
        // the nested passing of FBRuleInfGraph's will correctly handle all
        // cases of indirectly bound schema data. If we do uncover a problem here
        // then either include the raw schema in a Union with the premises or
        // revert of a more brute force version. 
        graph.rebind(premises);
        return graph;
    }
    
    /** 
     * Free all resources, any further use of this Graph is an error.
     */
    public void close() {
        if (!closed) {
            bEngine.halt();        
            bEngine = null;
            transitiveEngine = null;
            super.close();
        }
    }
    
//  =======================================================================
//  Generalized validation machinery. Assumes rule set has special validation
//  rules that can be turned on.
   
    /**
     * Test the consistency of the bound data. This normally tests
     * the validity of the bound instance data against the bound
     * schema data. 
     * @return a ValidityReport structure
     */
    public ValidityReport validate() {
        checkOpen();
        StandardValidityReport report = new StandardValidityReport();
        // Switch on validation
        Triple validateOn = new Triple(Node.createAnon(), 
                                ReasonerVocabulary.RB_VALIDATION.asNode(),
                                Functor.makeFunctorNode("on", new Node[] {}));
        // We sneak this switch directly into the engine to avoid contaminating the
        // real data - this is only possible only the forward engine has been prepared
//      add(validateOn);
        if (!isPrepared) {
            prepare();
        }
        engine.add(validateOn); 
        // Look for all reports
        TriplePattern pattern = new TriplePattern(null, ReasonerVocabulary.RB_VALIDATION_REPORT.asNode(), null);
        for (Iterator i = findFull(pattern); i.hasNext(); ) {
            Triple t = (Triple)i.next();
            Node rNode = t.getObject();
            boolean foundReport = false;
            if (rNode.isLiteral()) {
                Object rVal = rNode.getLiteral().getValue();
                if (rVal instanceof Functor) {
                    Functor rFunc = (Functor)rVal;
                    foundReport = true;
                    StringBuffer description = new StringBuffer();
                    String nature = rFunc.getName();
                    String type = rFunc.getArgs()[0].toString();
                    String text = rFunc.getArgs()[1].toString();
                    description.append( text + "\n");
                    description.append( "Culprit = " + PrintUtil.print(t.getSubject()) +"\n");
                    for (int j = 2; j < rFunc.getArgLength(); j++) {
                        description.append( "Implicated node: " + PrintUtil.print(rFunc.getArgs()[j]) + "\n");
                    }
                    Node culpritN = t.getSubject();
                    RDFNode culprit = null;
                    if (culpritN.isURI()) {
                        culprit = ResourceFactory.createResource(culpritN.getURI());
                    }
                    report.add(nature.equalsIgnoreCase("error"), type, description.toString(), culprit);
                }
            }
        }
//        // Debug
//        Node ia = Node.createURI("http://jena.hpl.hp.com/testing/reasoners/owl#ia");
//        System.out.println("Types of ia");
//        PrintUtil.printOut(findFull(new TriplePattern(ia, RDF.Nodes.type, null)));
//        System.out.println("different froms");
//        PrintUtil.printOut(findFull(new TriplePattern(null, OWL.differentFrom.asNode(), null)));
//        System.out.println("ia same as");
//        PrintUtil.printOut(findFull(new TriplePattern(ia, OWL.sameIndividualAs.asNode(), null)));
//        // end
        return report;
    }

//  =======================================================================
//  Helper methods

    /**
     * Scan the initial rule set and pick out all the backward-only rules with non-null bodies,
     * and transfer these rules to the backward engine. 
     */
    private static List extractPureBackwardRules(List rules) {
        List bRules = new ArrayList();
        for (Iterator i = rules.iterator(); i.hasNext(); ) {
            Rule r = (Rule)i.next();
            if (r.isBackward() && r.bodyLength() > 0) {
                bRules.add(r);
            }
        }
        return bRules;
    }

    /**
     * Adds a set of precomputed triples to the deductions store. These do not, themselves,
     * fire any rules but provide additional axioms that might enable future rule
     * firing when real data is added. Used to implement bindSchema processing
     * in the parent Reasoner.
     * @return true if the preload was able to load rules as well
     */
    protected boolean preloadDeductions(Graph preloadIn) {
        Graph d = fdeductions.getGraph();
        FBRuleInfGraph preload = (FBRuleInfGraph)preloadIn;
        // If the rule set is the same we can reuse those as well
        if (preload.rules == rules) {
            // Load raw deductions
            for (Iterator i = preload.getDeductionsGraph().find(null, null, null); i.hasNext(); ) {
                d.add((Triple)i.next());
            }
            // Load backward rules
            addBRules(preload.getBRules());
            // Load forward rules
            engine.setRuleStore(preload.getForwardRuleStore());
            // Add access to raw data
            return true;
        } else {
            return false;
        }
    }
   
    /**
     * Called to flag that a node should be hidden from external queries.
     */
    public void hideNode(Node n) {
        if (! JenaParameters.enableFilteringOfHiddenInfNodes) return;
        if (hiddenNodes == null) {
            hiddenNodes = new HashSet();
        }
        synchronized (hiddenNodes) {
            hiddenNodes.add(n);
        }
    }
    
//  =======================================================================
//  Support for LP engine profiling
    
    /**
     * Reset the LP engine profile.
     * @param enable it true then profiling will continue with a new empty profile table,
     * if false profiling will stop all current data lost.
     */
    public void resetLPProfile(boolean enable) {
        bEngine.resetProfile(enable);
    }
    
    /**
     * Print a profile of LP rules used since the last reset.
     */
    public void printLPProfile() {
        bEngine.printProfile();
    }
    
//  =======================================================================
//  Implement Filter signature
 
    /**
     * Post-filter query results to hide unwanted
     * triples from the glare of publicity. Unwanted triples
     * are triples with Functor literals and triples with hidden nodes
     * as subject or object.
     */
    public boolean accept(Object tin) {
        Triple t = (Triple)tin;
        
        if (((Triple)t).getSubject().isLiteral()) return true;
        
        if (JenaParameters.enableFilteringOfHiddenInfNodes && hiddenNodes != null) {
            if (hiddenNodes.contains(t.getSubject()) || hiddenNodes.contains(t.getObject())) {
                return true;
            }
        }
        
        if (filterFunctors) {
            if (Functor.isFunctor(t.getObject())) {
                return true;
            }
        }
        
        return false;

     }
     
//  =======================================================================
//   Inner classes

    /**
     * Structure used to wrap up pre-processed/compiled rule sets.
     */
    public static class RuleStore {
        
        /** The raw rules */
        protected List rawRules;
        
        /** The indexed store used by the forward chainer */
        protected Object fRuleStore;
        
        /** The separated backward rules */
        protected List bRules;
        
        /** 
         * Constructor.
         */
        public RuleStore(List rawRules, Object fRuleStore, List bRules) {
            this.rawRules = rawRules;
            this.fRuleStore = fRuleStore;
            this.bRules = bRules;
        }
        
    }
   
    
}


/*
    (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
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