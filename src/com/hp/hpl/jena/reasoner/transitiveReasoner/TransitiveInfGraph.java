/******************************************************************
 * File:        TransitiveInfGraph.java
 * Created by:  Dave Reynolds
 * Created on:  02-Feb-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: TransitiveInfGraph.java,v 1.5 2003-04-29 16:54:12 der Exp $
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
 * @version $Revision: 1.5 $ on $Date: 2003-04-29 16:54:12 $
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
        
        // Initialize the predicate switch table.
        specialPredicates = new HashSet();
        specialPredicates.add(TransitiveReasoner.directSubClassOf);
        specialPredicates.add(TransitiveReasoner.directSubPropertyOf);
        specialPredicates.add(TransitiveReasoner.subPropertyOf);
        specialPredicates.add(TransitiveReasoner.subClassOf);
        
        // Initially just point to the reasoner's precached information
        this.subClassCache = reasoner.subClassCache;
        this.subPropertyCache = reasoner.subPropertyCache;
        this.tbox = reasoner.tbox;

        // But need to check if the data graph defines schema data as well
        if (data != null && 
            (TransitiveReasoner.checkOccurance(TransitiveReasoner.subPropertyOf, data, subPropertyCache) ||
             TransitiveReasoner.checkOccurance(TransitiveReasoner.subClassOf, data, subPropertyCache))) {
            // Need to include data in the tbox so create a new reasoner which
            // become the parent of this InfGraph
            if (tbox != null) {
                tbox = FinderUtil.cascade(tbox, fdata);
            } else {
                tbox = fdata;
            }
            TransitiveReasoner newTR = new TransitiveReasoner();
            this.reasoner = newTR.bindSchema(tbox);
            subClassCache = newTR.subClassCache;
            subPropertyCache = newTR.subPropertyCache;
        }            
        // Cache the closures of subPropertyOf because these are likely to be
        // small and accessed a lot
        subPropertyCache.setCaching(true);
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

}
