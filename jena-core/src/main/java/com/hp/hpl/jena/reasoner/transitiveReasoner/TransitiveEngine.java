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

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

import java.util.*;

/**
 * Uses two transitive graph caches to store a subclass and a subproperty
 * lattice and use them within a larger inference graph.
 */
public class TransitiveEngine {
    
    /** The precomputed cache of the subClass graph */
    protected TransitiveGraphCache subClassCache;
    
    /** The precomputed cache of the subProperty graph */
    protected TransitiveGraphCache subPropertyCache;
    
    /** The base data set from which the caches can be rebuilt */
    protected Finder data;
    
    /** True if the internal data structures have been initialized */
    protected boolean isPrepared = false;
    
    /** The set of predicates which are aliases for subClassOf */
    protected static HashSet<Node> subClassAliases;
    
    /** The set of predicates which are aliases for subPropertyOf */
    protected static HashSet<Node> subPropertyAliases;
    
    /** Classification flag: not relevant to this engine */
    private static final int NOT_RELEVANT = 1;
    
    /** Classification flag: simple or indirect subClass */
    private static final int SUBCLASS = 2;
    
    /** Classification flag: simple subProperty */
    private static final int SUBPROPERTY = 4;
    
    /** Mask for the lattice update cases */
//    private static final int UPDATE_MASK = SUBCLASS | SUBPROPERTY;
    private static final int UPDATE_MASK = SUBCLASS | SUBPROPERTY | NOT_RELEVANT;
    
    /** Classification flag: subProperty of subClass */
    private static final int REBUILD_SUBCLASS = 8;
    
    /** Classification flag: subProperty of subProperty */
    private static final int REBUILD_SUBPROPERTY = 16;
    
    /** The direct (minimal) version of the subPropertyOf property */
    public static Node directSubPropertyOf;
    
    /** The direct (minimal) version of the subClassOf property */
    public static Node directSubClassOf;
    
    /** The normal subPropertyOf property */
    public static Node subPropertyOf;
    
    /** The normal subClassOf property */
    public static Node subClassOf;
    
    // Static initializer
    static {
        directSubPropertyOf = TransitiveReasoner.directSubPropertyOf;
        directSubClassOf    = TransitiveReasoner.directSubClassOf;
        subPropertyOf = RDFS.subPropertyOf.asNode();
        subClassOf = RDFS.subClassOf.asNode();
    }
   
    /**
     * Constructor.
     * @param subClassCache pre-initialized subclass TGC
     * @param subPropertyCache pre-initialized subproperty TGC
     */
    public TransitiveEngine(TransitiveGraphCache subClassCache,
                             TransitiveGraphCache subPropertyCache) {
         this.subClassCache = subClassCache;
         this.subPropertyCache = subPropertyCache;
    }
   
    /**
     * Constructor.
     * @param tengine an instance of TransitiveEngine to be cloned
     */
    public TransitiveEngine(TransitiveEngine tengine) {
         this.subClassCache = tengine.getSubClassCache().deepCopy();
         this.subPropertyCache = tengine.getSubPropertyCache().deepCopy();
    }
    
    /**
     * Prepare the engine by inserting any new data not already included
     * in the existing caches.
     * @param baseData the base dataset on which the initial caches were based, could be null
     * @param newData a dataset to be added to the engine, not known to be already
     *                 included in the caches from construction time
     * @return a concatenation of the inserted data and the original data
     */
    public Finder insert(Finder baseData, FGraph newData) {
        Graph newDataG = newData.getGraph();
        if (baseData != null) {
            data = FinderUtil.cascade(baseData, newData);
        } else {
            data = newData;
        }
        if ((TransitiveEngine.checkOccuranceUtility(subPropertyOf, newDataG, subPropertyCache) ||
               TransitiveEngine.checkOccuranceUtility(subClassOf, newDataG, subPropertyCache))) {
             subClassCache = new TransitiveGraphCache(directSubClassOf, subClassOf);
             subPropertyCache = new TransitiveGraphCache(directSubPropertyOf, subPropertyOf);
             TransitiveEngine.cacheSubPropUtility(data, subPropertyCache);
             TransitiveEngine.cacheSubClassUtility(data, subPropertyCache, subClassCache);
         }        
         return data;    
    }

    /**
     * Return the cache of the subclass lattice.
     */
    public TransitiveGraphCache getSubClassCache() {
        return subClassCache;
    }

    /**
     * Return the cache of the subclass lattice.
     */
    public TransitiveGraphCache getSubPropertyCache() {
        return subPropertyCache;
    }
    
    /**
     * Set the closure caching flags.
     * @param cacheSP true if caching of subPropertyOf closure is wanted
     * @param cacheSC true if caching of subClassOf closure is wanted
     */
    public void setCaching(boolean cacheSP, boolean cacheSC) {
        subPropertyCache.setCaching(cacheSP);
        subClassCache.setCaching(cacheSC);
    }
    
    /**
     * Build alias tables for subclass and subproperty.
     */
    private void prepare() {
        if (isPrepared) return;
        subClassAliases = new HashSet<>();
        subClassAliases.add(subClassOf);
        subClassAliases.add(directSubClassOf);
        
        subPropertyAliases = new HashSet<>();
        subPropertyAliases.add(subPropertyOf);
        subPropertyAliases.add(directSubPropertyOf);
        
        Iterator<Triple> subProps = subPropertyCache.find(new TriplePattern(null, subPropertyOf, subPropertyOf));
        while (subProps.hasNext()) {
            Triple spT = subProps.next();
            Node spAlias = spT.getSubject();
            subPropertyAliases.add(spAlias);
            Iterator<Triple> subClasses = subPropertyCache.find(new TriplePattern(null, spAlias, subClassOf));
            while (subClasses.hasNext()) {
                subClassAliases.add(subClasses.next().getObject());
            }
        }
        isPrepared = true;
    }
    
    /**
     * Classify an incoming triple to detect whether it is relevant to this engine.
     * @param t the triple being added
     * @return a classification flag, as specified in the above private properties
     */
    private int triage(Triple t) {
        if (!isPrepared) prepare();
        Node predicate = t.getPredicate();
        if (subClassAliases.contains(predicate)) {
            return SUBCLASS;
        } else if (subPropertyAliases.contains(predicate)) {
            Node target = t.getObject();
            if (subClassAliases.contains(target)) {
                return REBUILD_SUBCLASS | SUBPROPERTY;
            } else if (subPropertyAliases.contains(target)) {
                return REBUILD_SUBCLASS | REBUILD_SUBPROPERTY;
            } else {
                return SUBPROPERTY;
            }
        } else {
            return NOT_RELEVANT;
        }
        
    }
    
    /**
     * Return a Finder instance appropriate for the given query.
     */
    public Finder getFinder(TriplePattern pattern, Finder continuation) {
        if (!isPrepared) prepare();
        Node predicate = pattern.getPredicate();
        if (predicate.isVariable()) {
            // Want everything in the cache, the tbox and the continuation
            return FinderUtil.cascade(subPropertyCache, subClassCache, continuation);
        } else if (subPropertyAliases.contains(predicate)) {
            return subPropertyCache;
        } else if (subClassAliases.contains(predicate)) {
            return subClassCache;
        } else {
            return continuation;
        }
    }
    
    /**
     * Add one triple to caches if it is relevant.
     * @return true if the triple affected the caches
     */
    public synchronized boolean add(Triple t) {
        int triageClass = triage(t);
        switch (triageClass & UPDATE_MASK) {
            case SUBCLASS:
                subClassCache.addRelation(t);
                break;
            case SUBPROPERTY:
                subPropertyCache.addRelation(t);
                break;
            case NOT_RELEVANT:
                return false;
        }
        // If we get here we might need to some cache rebuilding
        if ((triageClass & REBUILD_SUBPROPERTY) != 0) {
            TransitiveEngine.cacheSubPropUtility(data, subPropertyCache);
            isPrepared = false;
        }
        if ((triageClass & REBUILD_SUBCLASS) != 0) {
            TransitiveEngine.cacheSubClassUtility(data, subPropertyCache, subClassCache);
            isPrepared = false;
        }
        return true;
    }
    
    /** 
     * Removes the triple t (if relevant) from the caches.
     * @return true if the triple affected the caches
     */   
    public synchronized boolean delete(Triple t) {
        int triageClass = triage(t);
        switch (triageClass & UPDATE_MASK) {
            case SUBCLASS:
                subClassCache.removeRelation(t);
                break;
            case SUBPROPERTY:
                subPropertyCache.removeRelation(t);
                break;
            case NOT_RELEVANT:
                return false;
        }
        // If we get here we might need to some cache rebuilding
        if ((triageClass & REBUILD_SUBPROPERTY) != 0) {
            subPropertyCache.clear();
            TransitiveEngine.cacheSubPropUtility(data, subPropertyCache);
            isPrepared = false;
        }
        if ((triageClass & REBUILD_SUBCLASS) != 0) {
            subClassCache.clear();
            TransitiveEngine.cacheSubClassUtility(data, subPropertyCache, subClassCache);
            isPrepared = false;
        }
        return true;
    }

    
    /**
     * Test if there are any usages of prop within the given graph.
     * This includes indirect usages incurred by subProperties of prop.
     * 
     * @param prop the property to be checked for
     * @param graph the graph to be check
     * @return true if there is a triple using prop or one of its sub properties
     */
    public boolean checkOccurance(Node prop, Graph graph) {
        return checkOccuranceUtility(prop, graph, subPropertyCache);
    }

    /**
     * Caches all subClass declarations, including those that
     * are defined via subProperties of subClassOf. Public to allow other reasoners
     * to use it but not of interest to end users.
     * 
     * @param graph a graph whose declarations are to be cached
     * @param spCache the existing state of the subPropertyOf cache
     * @param scCache the existing state of the subClassOf cache, will be updated
     * @return true if there were new metalevel declarations discovered.
     */
    public static boolean cacheSubClassUtility(Finder graph, TransitiveGraphCache spCache, TransitiveGraphCache scCache) {
        if (graph == null) return false;
    
        scCache.cacheAll(graph, TransitiveReasoner.subClassOf);       
        
        // Check for any properties which are subProperties of subClassOf
        boolean foundAny = false;
        ExtendedIterator<Triple> subClasses 
            = spCache.find(new TriplePattern(null, TransitiveReasoner.subPropertyOf, TransitiveReasoner.subClassOf));
        while (subClasses.hasNext()) {
            foundAny = true;
            Triple t = subClasses.next();
            Node subClass = t.getSubject();
            if (!subClass.equals(TransitiveReasoner.subClassOf)) {
                scCache.cacheAll(graph, subClass);
            }
        }
        
        return foundAny;
    }

    /**
     * Test if there are any usages of prop within the given graph.
     * This includes indirect usages incurred by subProperties of prop.
     * Public to allow other reasoners
     * to use it but not of interest to end users.
     * 
     * @param prop the property to be checked for
     * @param graph the graph to be check
     * @param spCache the subPropertyOf cache to use
     * @return true if there is a triple using prop or one of its sub properties
     */
    private static boolean checkOccuranceUtility(Node prop, Graph graph, TransitiveGraphCache spCache) {
        boolean foundOne = false;
        ExtendedIterator<Triple> uses  = graph.find( null, prop, null );
        foundOne = uses.hasNext();
        uses.close();
        if (foundOne) return foundOne;
        
        ExtendedIterator<Triple> propVariants 
           = spCache.find(new TriplePattern(null, TransitiveReasoner.subPropertyOf, prop));
        while (propVariants.hasNext() && !foundOne) {
            Triple t = propVariants.next();
            Node propVariant = t.getSubject();
            uses = graph.find( null, propVariant, null );
            foundOne = uses.hasNext();
            uses.close();
        }
        propVariants.close();
        return foundOne;
    }

    /**
     * Caches all subPropertyOf declarations, including any meta level
     * ones (subPropertyOf subPropertyOf). Public to allow other reasoners
     * to use it but not of interest to end users.
     * 
     * @param graph a graph whose declarations are to be cached
     * @param spCache the existing state of the subPropertyOf cache, will be updated
     * @return true if there were new metalevel declarations discovered.
     */
    public static boolean cacheSubPropUtility(Finder graph, TransitiveGraphCache spCache) {
        if (graph == null) return false;
        
        spCache.cacheAll(graph, TransitiveReasoner.subPropertyOf);
        
        // Check for any properties which are subProperties of subProperty
        // and so introduce additional subProperty relations.
        // Each one discovered might reveal indirect subPropertyOf subPropertyOf
        // declarations - hence the double iteration
        boolean foundAny = false;
        boolean foundMore = false;
        HashSet<Node> cached = new HashSet<>();
        do {
            ExtendedIterator<Triple> subProps 
                = spCache.find(new TriplePattern(null, TransitiveReasoner.subPropertyOf, TransitiveReasoner.subPropertyOf));
            while (subProps.hasNext()) {
                foundMore = false;
                Triple t = subProps.next();
                Node subProp = t.getSubject();
                if (!subProp.equals(TransitiveReasoner.subPropertyOf) && !cached.contains(subProp)) {
                    foundAny = true;
                    cached.add(subProp);
                    spCache.cacheAll(graph, subProp);
                    foundMore = true;
                }
            }
        } while (foundMore);
        
        return foundAny;
    }

}
