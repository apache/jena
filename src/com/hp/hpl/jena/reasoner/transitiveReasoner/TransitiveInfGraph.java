/******************************************************************
 * File:        TransitiveInfGraph.java
 * Created by:  Dave Reynolds
 * Created on:  02-Feb-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: TransitiveInfGraph.java,v 1.10 2003-06-18 08:00:11 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.transitiveReasoner;

import com.hp.hpl.jena.reasoner.BaseInfGraph;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.UniqueExtendedIterator;

import java.util.HashSet;

/**
 * Implementation of InfGraph used by the TransitiveReasoner.
 * This is returned by the TransitiveReasoner when a data graph
 * (together with an optional schema) has been bound.
 * 
 * <p>The cached property and class graphs are calculated by the
 * reasoner when the schema is bound. If the data graph does not
 * include schema information then the caches generated at 
 * schema binding stage are reused here. Otherwise the caches
 * are regenerated.</p>
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.10 $ on $Date: 2003-06-18 08:00:11 $
 */
public class TransitiveInfGraph extends BaseInfGraph {

    /** The precomputed cache of the subClass graph */
    protected TransitiveGraphCache subClassCache;
    
    /** The precomputed cache of the subProperty graph */
    protected TransitiveGraphCache subPropertyCache;
    
    /** The graph registered as the schema, if any */
    protected Finder tbox = null;
    
    /** The set of predicates handled by this InfGraph */
    protected static HashSet specialPredicates;
    
    /**
     * Constructor. Called by the TransitiveReasoner when it
     * is bound to a data graph.
     * @param reasoner the parent instance of the transitive reasoner,
     * including any precomputed class and property caches
     * @param data the data graph being bound in.
     */
    public TransitiveInfGraph(Graph data, TransitiveReasoner reasoner) {
        super(data, reasoner);
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
        // Initialize the predicate switch table.
        specialPredicates = new HashSet();
        specialPredicates.add(TransitiveReasoner.directSubClassOf);
        specialPredicates.add(TransitiveReasoner.directSubPropertyOf);
        specialPredicates.add(TransitiveReasoner.subPropertyOf);
        specialPredicates.add(TransitiveReasoner.subClassOf);
        
        // Initially just point to the reasoner's precached information
        this.subClassCache = ((TransitiveReasoner)reasoner).getSubClassCache();
        this.subPropertyCache = ((TransitiveReasoner)reasoner).getSubPropertyCache();
        this.tbox = ((TransitiveReasoner)reasoner).getTbox();

        // But need to check if the data graph defines schema data as well
        Graph data = fdata.getGraph();
        if ((TransitiveReasoner.checkOccurance(TransitiveReasoner.subPropertyOf, data, subPropertyCache) ||
              TransitiveReasoner.checkOccurance(TransitiveReasoner.subClassOf, data, subPropertyCache))) {
            // Need to include data in the tbox so create a new reasoner which
            // become the parent of this InfGraph
            if (tbox != null) {
                tbox = FinderUtil.cascade(tbox, fdata);
            } else {
                tbox = fdata;
            }
            subClassCache = new TransitiveGraphCache(TransitiveReasoner.directSubClassOf, TransitiveReasoner.subClassOf);
            subPropertyCache = new TransitiveGraphCache(TransitiveReasoner.directSubPropertyOf, TransitiveReasoner.subPropertyOf);
            TransitiveReasoner.cacheSubProp(tbox, subPropertyCache);
            TransitiveReasoner.cacheSubClass(tbox, subPropertyCache, subClassCache);
        }            
        // Cache the closures of subPropertyOf because these are likely to be
        // small and accessed a lot
        subPropertyCache.setCaching(true);
        
        isPrepared = true;
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
        Node predicate = pattern.getPredicate();
        Finder resultF = null;
        if (predicate.isVariable()) {
            // Want everything in the cache, the tbox and the continuation
            resultF = FinderUtil.cascade(subPropertyCache, subClassCache, tbox, continuation);
        } else if (specialPredicates.contains(predicate)) {
            if (predicate.equals(TransitiveReasoner.directSubPropertyOf) || 
                 predicate.equals(TransitiveReasoner.subPropertyOf)) {
                resultF = subPropertyCache;
            } else {
                resultF = subClassCache;
            }
        } else {
            resultF = FinderUtil.cascade(continuation, tbox);
        }
        return new UniqueExtendedIterator(resultF.find(pattern));
    }
   
    /** 
     * Returns an iterator over Triples.
     */
    public ExtendedIterator find(Node subject, Node property, Node object) {
        return findWithContinuation(new TriplePattern(subject, property, object), fdata);
    }

    /**
     * Basic pattern lookup interface.
     * @param pattern a TriplePattern to be matched against the data
     * @return a ExtendedIterator over all Triples in the data set
     *  that match the pattern
     */
    public ExtendedIterator find(TriplePattern pattern) {
        return findWithContinuation(pattern, fdata);
    }
        
    /**
     * Add one triple to the data graph, run any rules triggered by
     * the new data item, recursively adding any generated triples.
     * TODO Will not correctly handle subPropertyOf subClassOf/subPropertyOf
     */
    public synchronized void add(Triple t) {
        if (!isPrepared) prepare();
        Node predicate = t.getPredicate();
        if (specialPredicates.contains(predicate)) {
            if (predicate.equals(TransitiveReasoner.directSubClassOf)  
            || predicate.equals(TransitiveReasoner.subClassOf)) {
                subClassCache.addRelation(t.getSubject(), t.getObject());
            } else {
                subPropertyCache.addRelation(t.getSubject(), t.getObject());
            }
        }
        fdata.getGraph().add(t);
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
     * Removes the triple t (if possible) from the set belonging to this graph.
     * TODO: This will not work on subPropertyOf subClass/subPropertyOf yet. 
     */   
    public void delete(Triple t) {
        if (isPrepared) {
            Node predicate = t.getPredicate();
            if (specialPredicates.contains(predicate)) {
                if (predicate.equals(TransitiveReasoner.directSubClassOf)  
                || predicate.equals(TransitiveReasoner.subClassOf)) {
                    subClassCache.removeRelation(t.getSubject(), t.getObject());
                } else {
                    subPropertyCache.removeRelation(t.getSubject(), t.getObject());
                }
            }
        }
        fdata.getGraph().delete(t);
    }

}
