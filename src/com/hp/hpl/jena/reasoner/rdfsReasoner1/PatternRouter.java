/******************************************************************
 * File:        PatternRouter.java
 * Created by:  Dave Reynolds
 * Created on:  22-Jan-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: PatternRouter.java,v 1.1 2003-06-23 15:54:23 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rdfsReasoner1;

import com.hp.hpl.jena.reasoner.Finder;
import com.hp.hpl.jena.reasoner.InfGraph;
import com.hp.hpl.jena.reasoner.ReasonerException;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.transitiveReasoner.TransitiveGraphCache;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.graph.*;

import org.apache.log4j.Logger;

import java.util.*;

/**
 * A utility for mapping a TriplePattern to a sequence of operations
 * that could satisfy it. Sources register individual patterns that
 * they can satisfy. Then requesters use the Finder interface to 
 * satisfy a query.
 * 
 * <p>Types of sources that can be registered include TransitiveGraphCaches
 * (which are assumed complete for the predicates they cache), triple stores
 * (via a Finder interface) and simple rewrite rules.</p>
 * 
 * <p>This implementation only supports TGCs and rules. It only indexes on
 * pattern predicates and does a linear search down the rest.<br />
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2003-06-23 15:54:23 $
 */
public class PatternRouter {
    
    /** log4j logger */
    protected static Logger logger = Logger.getLogger(PatternRouter.class);
    
    
    /** A map from pattern predicate to a list of satisfying Finders */
    Map patternIndex = new HashMap();
    
    /**
     * Constructor
     */
    public PatternRouter() {
    }
    
    /**
     * Register a transitive closure cache and a means of satisfying
     * the direct and closed versions of the cached predicate.
     * 
     * @param cache the TransitiveGraphCache
     */
    public void register(TransitiveGraphCache cache) {
        register(new TriplePattern(null, cache.getClosedPredicate(), null), cache);
        register(new TriplePattern(null, cache.getDirectPredicate(), null), cache);
    }
    
    /**
     * Register a backward rewrite rule
     */
    public void register(BRWRule rule) {
        register(rule.getHead(), rule);
    }
    
    /**
     * Register an object against a specific search pattern
     */
    public void register(TriplePattern pattern, Object satisfier) {
        PatternEntry entry = new PatternEntry(pattern, satisfier);
        Node predicate = pattern.getPredicate();
        if (predicate.isVariable()) {
            throw new ReasonerException("PatternRouter can't handle non-ground predicates in patterns: " + pattern);
        }
        HashSet sats = (HashSet)patternIndex.get(predicate);
        if (sats == null) {
            sats = new HashSet();
            patternIndex.put(predicate, sats);
        }
        sats.add(entry);
    }
        
    /**
     * Process a query according to the known routing information.
     * The set of required parameters is redundant but enables different routing
     * tactics to be tried in the future.
     * 
     * @param pattern the query to be processed
     * @param tripleCache a cascade of any generic caches which can supply additional answers
     * @param data the raw data graph being processed
     * @param infGraph link to originating inference graph, may be re-invoked after a pattern rewrite
     */
    public ExtendedIterator find(TriplePattern pattern, Finder tripleCache, Finder data, InfGraph infGraph) {
        return find(pattern, tripleCache, data, infGraph, new HashSet());
    }
    
    /**
     * Process a query according to the known routing information.
     * The set of required parameters is redundant but enables different routing
     * tactics to be tried in the future.
     * 
     * @param pattern the query to be processed
     * @param tripleCache a cascade of any generic caches which can supply additional answers
     * @param data the raw data graph being processed
     * @param infGraph link to originating inference graph, may be re-invoked after a pattern rewrite
     * @param firedRules set of rules which have already been fired and should now be blocked
     */
    public ExtendedIterator find(TriplePattern pattern, Finder tripleCache, Finder data, InfGraph infGraph, HashSet firedRules) {
        ExtendedIterator result = tripleCache.findWithContinuation(pattern, data);
        Node predicate = pattern.getPredicate();
        if (predicate.isVariable()) {
            // Wildcard predicate - this is brute force search across all rules
            for (Iterator i = patternIndex.values().iterator(); i.hasNext();) {
                HashSet sats = (HashSet)i.next();
                result = doFind(sats, result, pattern, tripleCache, data, infGraph, firedRules);
            }
            return result;
        } else {
            HashSet sats = (HashSet)patternIndex.get(predicate);
            return doFind(sats, result, pattern, tripleCache, data, infGraph, firedRules);
        }
    }
   
    /**
     * Process a query according to the known routing information.
     * The set of required parameters is redundant but enables different routing
     * tactics to be tried in the future.
     * 
     * @param sats the set of PatternEntry objects that might be applicable to this pattern
     * @param result the iterator over resuls so far
     * @param pattern the query to be processed
     * @param tripleCache a cascade of any generic caches which can supply additional answers
     * @param data the raw data graph being processed
     * @param infGraph link to originating inference graph, may be re-invoked after a pattern rewrite
     * @param firedRules set of rules which have already been fired and should now be blocked
     */
    private ExtendedIterator doFind(HashSet rules, ExtendedIterator result, 
                                     TriplePattern pattern, Finder tripleCache, 
                                     Finder data, InfGraph infGraph, HashSet firedRules) {
        if (rules != null) {
            // Scan all matches to check for complete solutions
            for (Iterator i = rules.iterator(); i.hasNext(); ) {
                PatternEntry entry =  (PatternEntry) i.next();
                if (entry.completeFor(pattern)) {
                    return entry.fire(pattern, data, infGraph, firedRules);
                }
            }
            // Scan again and accumulate all non-complete solutions
            for (Iterator i = rules.iterator(); i.hasNext(); ) {
                PatternEntry entry =  (PatternEntry) i.next();
                if (entry.shouldFire(pattern)) {
                    result = result.andThen(entry.fire(pattern, data, infGraph, firedRules));
                }
            }
        }
        return result;
    }
    
    /**
     * Printable version of the whole reasoner state.
     * Used during debugging
     */
    public String toString() {
        StringBuffer state = new StringBuffer();
        for (Iterator i = patternIndex.values().iterator(); i.hasNext(); ) {
            HashSet ents = (HashSet)i.next();
            for (Iterator j = ents.iterator(); j.hasNext(); ) {
                state.append(j.next().toString());
                state.append("\n");
            }
        }
        return state.toString();
    }
    
    /**
     * Inner class used to prepresent a pattern indexed entry in the router table
     */
    static class PatternEntry {
        /** The pattern which triggers this entry */
        TriplePattern pattern;
        
        /** The cache/rule which is fired */
        Object action;
        
        /** Constructor */
        PatternEntry(TriplePattern pattern, Object action) {
            this.pattern = pattern;
            this.action = action;
        }

        /**
         * Return true if this entry is a complete solution to the given
         * query and the router need look no further
         */
        public boolean completeFor(TriplePattern query) {
            if (action instanceof BRWRule) {
                return ((BRWRule)action).completeFor(query);
            } else if (action instanceof TransitiveGraphCache) {
                TransitiveGraphCache tgc = (TransitiveGraphCache)action;
                Node prop = query.getPredicate();
                return prop.equals(tgc.getDirectPredicate()) ||
                        prop.equals(tgc.getClosedPredicate());
            }
            return false;
        }
            
        /** Test if this entry should fire against the given query pattern */
        boolean shouldFire(TriplePattern query) {
            return pattern.compatibleWith(query);
        }

        /**
         * Run the action
         * @param query the query to be processed
         * @param data the raw data graph being processed
         * @param infGraph link to originating inference graph, may be re-invoked after a pattern rewrite
         * @param firedRules set of rules which have already been fired and should now be blocked
         */
        public ExtendedIterator fire(TriplePattern query, Finder data, InfGraph infGraph, HashSet firedRules) {
            TriplePattern nquery = query;
            if (nquery.getPredicate().isVariable()) {
                nquery = new TriplePattern(query.getSubject(), pattern.getPredicate(), query.getObject());
            }
            if (action instanceof TransitiveGraphCache) {
                return ((TransitiveGraphCache)action).find(nquery);
            } else if (action instanceof BRWRule) {
                logger.debug("Fire rule: " + action);
                return ((BRWRule)action).execute(nquery, infGraph, data, firedRules);
            } else {
                throw new ReasonerException("Illegal router action entry");
            }
        }
        
        /** Printable string for debugging */
        public String toString() {
            return pattern.toString() + " <- " + action;
        }
        
        /** Equality override */
        public boolean equals(Object o) {
            return o instanceof PatternEntry && 
                    pattern.equals(((PatternEntry)o).pattern) &&
                    action.equals(((PatternEntry)o).action) ;
        }
            
        /** hash function override */
        public int hashCode() {
            return (pattern.hashCode() >> 1) ^ action.hashCode();
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
