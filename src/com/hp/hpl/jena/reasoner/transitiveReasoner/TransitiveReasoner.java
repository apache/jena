/******************************************************************
 * File:        TransitiveReasoner.java
 * Created by:  Dave Reynolds
 * Created on:  16-Jan-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: TransitiveReasoner.java,v 1.6 2003-04-15 21:28:39 jeremy_carroll Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.transitiveReasoner;

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import java.util.HashSet;

/**
 * A  simple "reasoner" used to help with API development.
 * <p>This reasoner caches a transitive closure of the subClass and
 * subProperty graphs. The generated infGraph allows both the direct
 * and closed versions of these properties to be retrieved. The cache is
 * built when the tbox is bound in but if the final data graph
 * contains additional subProperty/subClass declarations then the
 * cache has to be rebuilt.</p>
 * <p>
 * The triples in the tbox (if present) will also be included
 * in any query. Any of tbox or data graph are allowed to be null.</p>
 * <p>
 * TODO: Add switch (in configuration code) to turn off meta-level enablement
 * of RDFS processing.</p>
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.6 $ on $Date: 2003-04-15 21:28:39 $
 */
public class TransitiveReasoner implements Reasoner {

    /** The precomputed cache of the subClass graph */
    protected TransitiveGraphCache subClassCache;
    
    /** The precomputed cache of the subProperty graph */
    protected TransitiveGraphCache subPropertyCache;
    
    /** The graph registered as the schema, if any */
    protected Finder tbox = null;
    
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
        directSubPropertyOf = ReasonerRegistry.makeDirect(RDFS.subPropertyOf.getNode());
        directSubClassOf    = ReasonerRegistry.makeDirect(RDFS.subClassOf.getNode());
        subPropertyOf = RDFS.subPropertyOf.getNode();
        subClassOf = RDFS.subClassOf.getNode();
    }
    
    /** Constructor */
    public TransitiveReasoner() {
        subClassCache = new TransitiveGraphCache(directSubClassOf, subClassOf);
        subPropertyCache = new TransitiveGraphCache(directSubPropertyOf, subPropertyOf);
    }
     
    /**
     * Extracts all of the subClass and subProperty declarations from
     * the given schema/tbox and caches the resultant graphs.
     * It can only be used once, can't stack up multiple tboxes this way.
     * This limitation could be lifted - the only difficulty is the need to
     * reprocess all the earlier tboxes if a new subPropertyOf subPropertyOf
     * subClassOf is discovered.
     * @param tbox schema containing the property and class declarations
     */
    public Reasoner bindSchema(Graph tbox) throws ReasonerException {
        return bindSchema(new FGraph(tbox));
    }
    
     
    /**
     * Extracts all of the subClass and subProperty declarations from
     * the given schema/tbox and caches the resultant graphs.
     * It can only be used once, can't stack up multiple tboxes this way.
     * This limitation could be lifted - the only difficulty is the need to
     * reprocess all the earlier tboxes if a new subPropertyOf subPropertyOf
     * subClassOf is discovered.
     * @param tbox schema containing the property and class declarations
     */
    Reasoner bindSchema(Finder tbox) throws ReasonerException {
        if (this.tbox != null) {
            throw new ReasonerException("Attempt to bind multiple rulesets - disallowed for now");
        }
        this.tbox = tbox;
        
        cacheSubProp(tbox, subPropertyCache);
        cacheSubClass(tbox, subPropertyCache, subClassCache);
        
        return this;
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
        
        spCache.cacheAll(graph, subPropertyOf);
        
        // Check for any properties which are subProperties of subProperty
        // and so introduce additional subProperty relations.
        // Each one discovered might reveal indirect subPropertyOf subPropertyOf
        // declarations - hence the double iteration
        boolean foundAny = false;
        boolean foundMore = false;
        HashSet cached = new HashSet();
        do {
            ExtendedIterator subProps 
                = spCache.find(new TriplePattern(null, subPropertyOf, subPropertyOf));
            while (subProps.hasNext()) {
                foundMore = false;
                Triple t = (Triple)subProps.next();
                Node subProp = t.getSubject();
                if (!subProp.equals(subPropertyOf) && !cached.contains(subProp)) {
                    foundAny = true;
                    cached.add(subProp);
                    spCache.cacheAll(graph, subProp);
                    foundMore = true;
                }
            }
        } while (foundMore);
        
        return foundAny;
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

        scCache.cacheAll(graph, subClassOf);       
        
        // Check for any properties which are subProperties of subClassOf
        boolean foundAny = false;
        ExtendedIterator subClasses 
            = spCache.find(new TriplePattern(null, subPropertyOf, subClassOf));
        while (subClasses.hasNext()) {
            foundAny = true;
            Triple t = (Triple)subClasses.next();
            Node subClass = t.getSubject();
            if (!subClass.equals(subClassOf)) {
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
        ExtendedIterator uses
            = graph.find(new StandardTripleMatch(null, prop, null));
        foundOne = uses.hasNext();
        uses.close();
        if (foundOne) return foundOne;
        
        ExtendedIterator propVariants 
           = spCache.find(new TriplePattern(null, subPropertyOf, prop));
        while (propVariants.hasNext() && !foundOne) {
            Triple t = (Triple)propVariants.next();
            Node propVariant = t.getSubject();
            uses = graph.find(new StandardTripleMatch(null, propVariant, null));
            foundOne = uses.hasNext();
            uses.close();
        }
        propVariants.close();
        return foundOne;
    }
    
    /**
     * Merge two graphs. This should be a utility somehwer more sensible
     * @param graph the graph into which data is to be merged
     * @param additions the graph of additional triples
     */
    public static void addGraph(Graph graph, Graph additions) {
        ExtendedIterator it = additions.find(null, null, null);
        while (it.hasNext()) {
            graph.add((Triple)it.next());
        }
        it.close();
    }
    
    /**
     * Attach the reasoner to a set of RDF ddata to process.
     * The reasoner may already have been bound to specific rules or ontology
     * axioms (encoded in RDF) through earlier bindRuleset calls.
     * @param data the RDF data to be processed, some reasoners may restrict
     * the range of RDF which is legal here (e.g. syntactic restrictions in OWL).
     * @return an inference graph through which the data+reasoner can be queried.
     * @throws ReasonerException if the data is ill-formed according to the
     * constraints imposed by this reasoner.
     */
    public InfGraph bind(Graph data) throws ReasonerException {
        return new TransitiveInfGraph(data, this);
    }   
   
    /**
     * Switch on/off drivation logging.
     * If set to true then the InfGraph created from the bind operation will start
     * life with recording of derivations switched on. This is currently only of relevance
     * to rule-based reasoners.
     * <p>
     * Default - false.
     */
    public void setDerivationLogging(boolean logOn) {
        // Irrelevant to this reasoner
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

