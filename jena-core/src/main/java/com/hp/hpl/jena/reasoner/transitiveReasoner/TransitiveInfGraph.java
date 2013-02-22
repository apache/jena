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

package com.hp.hpl.jena.reasoner.transitiveReasoner;

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.UniqueFilter;

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
    @Override
    public synchronized void prepare() {
        if (this.isPrepared()) return;
        
        tbox = ((TransitiveReasoner)reasoner).getTbox();
        // Initially just point to the reasoner's precached information
        transitiveEngine = new TransitiveEngine(((TransitiveReasoner)reasoner).getSubClassCache().deepCopy(),
                                                 ((TransitiveReasoner)reasoner).getSubPropertyCache().deepCopy());
                    // The deepCopies reduce the value of precomputing the closure in the reasoner object
                    // but enables people to bind the same reasoner to multiple datasets.
                    // Perhaps need a faster deepcopy
                                                 
        // But need to check if the data graph defines schema data as well
        dataFind = transitiveEngine.insert(tbox, fdata);
        transitiveEngine.setCaching(true, true);
        
        this.setPreparedState(true);
    }

    /**
     * Return the schema graph, if any, bound into this inference graph.
     */
    @Override
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
    @Override public ExtendedIterator<Triple> findWithContinuation(TriplePattern pattern, Finder continuation) {
        checkOpen();
        this.requirePrepared();
        Finder cascade = transitiveEngine.getFinder(pattern, FinderUtil.cascade(tbox, continuation));
        return cascade.find(pattern).filterKeep( new UniqueFilter<Triple>());
    }
   
    /** 
     * Returns an iterator over Triples.
     */
    @Override public ExtendedIterator<Triple> graphBaseFind(Node subject, Node property, Node object) {
        return findWithContinuation(new TriplePattern(subject, property, object), fdata);
    }

    /**
     * Basic pattern lookup interface.
     * @param pattern a TriplePattern to be matched against the data
     * @return a ExtendedIterator over all Triples in the data set
     *  that match the pattern
     */
    @Override public ExtendedIterator<Triple> find(TriplePattern pattern) {
        return findWithContinuation(pattern, fdata);
    }
        
    /**
     * Add one triple to the data graph, run any rules triggered by
     * the new data item, recursively adding any generated triples.
     */
    @Override
    public synchronized void performAdd(Triple t) {
        this.requirePrepared();
        fdata.getGraph().add(t);
        transitiveEngine.add(t);
    }

    /** 
     * Removes the triple t (if possible) from the set belonging to this graph.
     */   
    @Override
    public synchronized void performDelete(Triple t) {
        fdata.getGraph().delete(t);
        if (this.isPrepared()) {
            transitiveEngine.delete(t);
        }
    }
    /**
    Answer the InfCapabilities of this InfGraph.
 */
@Override
public Capabilities getCapabilities()
    {
    if (capabilities == null) capabilities = new InfFindSafeCapabilities();
    return capabilities;
    }

}
