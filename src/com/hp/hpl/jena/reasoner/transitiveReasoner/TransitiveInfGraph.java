/******************************************************************
 * File:        TransitiveInfGraph.java
 * Created by:  Dave Reynolds
 * Created on:  02-Feb-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: TransitiveInfGraph.java,v 1.15 2003-08-19 20:10:01 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.transitiveReasoner;

import com.hp.hpl.jena.reasoner.BaseInfGraph;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.UniqueExtendedIterator;

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
 * @version $Revision: 1.15 $ on $Date: 2003-08-19 20:10:01 $
 */
public class TransitiveInfGraph extends BaseInfGraph {

    /** The paire of subclass and subproperty lattices */
    protected TransitiveEngine transitiveEngine;
    
    /** The graph registered as the schema, if any */
    protected Finder tbox = null;
    
    /** The combined data and schema finder */
    protected Finder dataFind;
    
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
        tbox = ((TransitiveReasoner)reasoner).getTbox();
        // Initially just point to the reasoner's precached information
        transitiveEngine = new TransitiveEngine(((TransitiveReasoner)reasoner).getSubClassCache(),
                                                 ((TransitiveReasoner)reasoner).getSubPropertyCache());

        // But need to check if the data graph defines schema data as well
        dataFind = transitiveEngine.insert(tbox, fdata);
        transitiveEngine.setCaching(true, true);
        
        isPrepared = true;
    }

    /**
     * Return the schema graph, if any, bound into this inference graph.
     */
    public Graph getSchemaGraph() {
        if (tbox == null) return null;
        if (tbox instanceof FGraph) {
            return ((FGraph)tbox).getGraph();
        } else {
            throw new ReasonerException("Transitive reasoner got into an illegal state");
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
        checkOpen();
        if (!isPrepared) prepare();
        Finder cascade = transitiveEngine.getFinder(pattern, FinderUtil.cascade(tbox, continuation));
        return new UniqueExtendedIterator(cascade.find(pattern));
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
     */
    public synchronized void performAdd(Triple t) {
        if (!isPrepared) prepare();
        fdata.getGraph().add(t);
        transitiveEngine.add(t);
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
     */   
    public void performDelete(Triple t) {
        fdata.getGraph().delete(t);
        if (isPrepared) {
            transitiveEngine.delete(t);
        }
    }

}
