/******************************************************************
 * File:        TransitiveEngine.java
 * Created by:  Dave Reynolds
 * Created on:  23-Jun-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: TransitiveEngine.java,v 1.2 2003-06-23 13:54:29 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.transitiveReasoner;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

import java.util.*;

/**
 * Uses two transitive graph caches to store a subclass and a subproperty
 * lattice and use them within a larger inference graph.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2003-06-23 13:54:29 $
 */
public class TransitiveEngine {
    
    /** The precomputed cache of the subClass graph */
    protected TransitiveGraphCache subClassCache;
    
    /** The precomputed cache of the subProperty graph */
    protected TransitiveGraphCache subPropertyCache;
    
    /** The base data set from which the caches can be rebuilt */
    protected Finder data;
    
    /** The set of predicates which are aliases for subClassOf */
    protected static HashSet subClassAliases;
    
    /** The set of predicates which are aliases for subPropertyOf */
    protected static HashSet subPropertyAliases;
    
    /** Classification flag: not relevant to this engine */
    private static final int NOT_RELEVANT = 0;
    
    /** Classification flag: simple or indirect subClass */
    private static final int SUBCLASS = 1;
    
    /** Classification flag: simple subProperty */
    private static final int SUBPROPERTY = 2;
    
    /** Classification flag: subProperty of subClass */
    private static final int REBUILD_SUBCLASS = 4;
    
    /** Classification flag: subProperty of subProperty */
    private static final int REBUILD_SUBPROPERTY = 8;
    
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
        subPropertyOf = RDFS.subPropertyOf.getNode();
        subClassOf = RDFS.subClassOf.getNode();
    }
    
    /**
     * Constructor.
     * @param subClassCache pre-initialized subclass TGC
     * @param subPropertyCache pre-initialized subproperty TGC
     * @param data the dataset which was used to initialize the provided caches, may be null
     */
    public TransitiveEngine(TransitiveGraphCache subClassCache,
                             TransitiveGraphCache subPropertyCache,
                             Finder data) {
         this.subClassCache = subClassCache;
         this.subPropertyCache = subPropertyCache;
         this.data = data;
         if (data instanceof FGraph && ((FGraph)data).getGraph() == null) this.data = null;
    }
    
    /**
     * Prepare the engine by inserting any new data not already included
     * in the existing caches.
     * @param newData a dataset to be added to the engine, not known to be already
     *                 included in the caches from construction time
     */
    public void insert(FGraph newData) {
        Graph newDataG = newData.getGraph();
        if (data == null) {
            data = newData;
        } else {
            if (newData != null) data = FinderUtil.cascade(data, newData);
        }
        if ((TransitiveEngine.checkOccurance(subPropertyOf, newDataG, subPropertyCache) ||
               TransitiveEngine.checkOccurance(subClassOf, newDataG, subPropertyCache))) {
             subClassCache = new TransitiveGraphCache(directSubClassOf, subClassOf);
             subPropertyCache = new TransitiveGraphCache(directSubPropertyOf, subPropertyOf);
             TransitiveEngine.cacheSubProp(data, subPropertyCache);
             TransitiveEngine.cacheSubClass(data, subPropertyCache, subClassCache);
         }            
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
     * Build alias tables for subclass and subproperty.
     */
    private void buildAliasTables() {
        subClassAliases = new HashSet();
        subClassAliases.add(subClassOf);
        subClassAliases.add(directSubClassOf);
        
        subPropertyAliases = new HashSet();
        subPropertyAliases.add(subPropertyOf);
        subPropertyAliases.add(directSubPropertyOf);
        
        Iterator subProps = subPropertyCache.find(new TriplePattern(null, subPropertyOf, subPropertyOf));
        while (subProps.hasNext()) {
            Triple spT = (Triple) subProps.next();
            Node spAlias = spT.getSubject();
            subPropertyAliases.add(spAlias);
            Iterator subClasses = subClassCache.find(new TriplePattern(null, spAlias, subClassOf));
            subClassAliases.add(((Triple)subClasses.next()).getObject());
        }
    }
    
    /**
     * Classify an incoming triple to detect whether it is relevant to this engine.
     * @param t the triple being added
     * @return a classification flag, as specified in the above private properties
     */
    private int triage(Triple t) {
        if (subClassAliases == null) buildAliasTables();
        Node predicate = t.getPredicate();
        if (subClassAliases.contains(predicate)) {
            return SUBCLASS;
        } else if (subPropertyAliases.contains(predicate)) {
            Node target = t.getObject();
            if (subClassAliases.contains(target)) {
                return REBUILD_SUBCLASS;
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
     * Add one triple to caches if it is relevant.
     */
    public synchronized void add(Triple t) {
        int triageClass = triage(t);
        switch (triageClass) {
            case SUBCLASS:
                subClassCache.addRelation(t.getSubject(), t.getObject());
                return;
            case SUBPROPERTY:
                subPropertyCache.addRelation(t.getSubject(), t.getObject());
                return;
            case NOT_RELEVANT:
                return;
        }
        // If we get here we need to some cache rebuilding
        if ((triageClass & REBUILD_SUBPROPERTY) != 0) {
            TransitiveEngine.cacheSubProp(data, subPropertyCache);
        }
        if ((triageClass & REBUILD_SUBCLASS) != 0) {
            TransitiveEngine.cacheSubClass(data, subPropertyCache, subClassCache);
        }
    }
    
    /** 
     * Removes the triple t (if relevant) from the caches.
     */   
    public void delete(Triple t) {
        int triageClass = triage(t);
        switch (triageClass) {
            case SUBCLASS:
                subClassCache.removeRelation(t.getSubject(), t.getObject());
                return;
            case SUBPROPERTY:
                subPropertyCache.removeRelation(t.getSubject(), t.getObject());
                return;
            case NOT_RELEVANT:
                return;
        }
        // If we get here we need to some cache rebuilding
        if ((triageClass & REBUILD_SUBPROPERTY) != 0) {
            subPropertyCache.clear();
            TransitiveEngine.cacheSubProp(data, subPropertyCache);
        }
        if ((triageClass & REBUILD_SUBCLASS) != 0) {
            subClassCache.clear();
            TransitiveEngine.cacheSubClass(data, subPropertyCache, subClassCache);
        }
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
    public static boolean cacheSubClass(Finder graph, TransitiveGraphCache spCache, TransitiveGraphCache scCache) {
        if (graph == null) return false;
    
        scCache.cacheAll(graph, TransitiveReasoner.subClassOf);       
        
        // Check for any properties which are subProperties of subClassOf
        boolean foundAny = false;
        ExtendedIterator subClasses 
            = spCache.find(new TriplePattern(null, TransitiveReasoner.subPropertyOf, TransitiveReasoner.subClassOf));
        while (subClasses.hasNext()) {
            foundAny = true;
            Triple t = (Triple)subClasses.next();
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
    public static boolean checkOccurance(Node prop, Graph graph, TransitiveGraphCache spCache) {
        boolean foundOne = false;
        ExtendedIterator uses  = graph.find( null, prop, null );
        foundOne = uses.hasNext();
        uses.close();
        if (foundOne) return foundOne;
        
        ExtendedIterator propVariants 
           = spCache.find(new TriplePattern(null, TransitiveReasoner.subPropertyOf, prop));
        while (propVariants.hasNext() && !foundOne) {
            Triple t = (Triple)propVariants.next();
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
    public static boolean cacheSubProp(Finder graph, TransitiveGraphCache spCache) {
        if (graph == null) return false;
        
        spCache.cacheAll(graph, TransitiveReasoner.subPropertyOf);
        
        // Check for any properties which are subProperties of subProperty
        // and so introduce additional subProperty relations.
        // Each one discovered might reveal indirect subPropertyOf subPropertyOf
        // declarations - hence the double iteration
        boolean foundAny = false;
        boolean foundMore = false;
        HashSet cached = new HashSet();
        do {
            ExtendedIterator subProps 
                = spCache.find(new TriplePattern(null, TransitiveReasoner.subPropertyOf, TransitiveReasoner.subPropertyOf));
            while (subProps.hasNext()) {
                foundMore = false;
                Triple t = (Triple)subProps.next();
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