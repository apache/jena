/******************************************************************
 * File:        BRWRule.java
 * Created by:  Dave Reynolds
 * Created on:  22-Jan-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: BRWRule.java,v 1.2 2003-02-10 10:14:13 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rdfsReasoner1;

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.util.iterator.*;

import java.util.*;

/**
 * Datastructure to hold a trivial backward rewrite rule.
 * 
 * <p>The rules take the form "pattern &lt;- pattern" where the pattern
 * is is a triple pattern with variables. The head pattern uses the
 * variables s/p/o to refer to the subject/predicate/object parts of the
 * body pattern. Similarly, the body pattern uses s/p/o to refer to
 * the corresponding parts of the query being processed.</p>
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2003-02-10 10:14:13 $
 */
public class BRWRule {

    /** The head of the rule */
    protected TriplePattern head;
    
    /** The body of the rule */
    protected TriplePattern body;
    
    
    /**
     * Constructor
     */
    public BRWRule(TriplePattern head, TriplePattern body) {
        this.head = head;
        this.body = body;
    }
    
    /**
     * Factory method that builds a rule instance by parsing
     * a simple string representation of the form:
     * <pre>
     *   ?s prop foo <- ?a ns:prop _ 
     * </pre>
     * Variables are either _ or ?x, uri's are either simple strings (no spaces)
     * or qnames. The prefix in qnames are restricted to rdf and rdfs.
     * Minimal error checking.
     */
    public static BRWRule makeRule(String rulespec) {
        StringTokenizer tokenizer = new StringTokenizer(rulespec);
        try {
            Node headS = parseNode(tokenizer.nextToken());
            Node headP = parseNode(tokenizer.nextToken());
            Node headO = parseNode(tokenizer.nextToken());
            TriplePattern head = new TriplePattern(headS, headP, headO);
            if (!tokenizer.nextToken().equals("<-"))
                throw new NoSuchElementException();
            Node bodyS = parseNode(tokenizer.nextToken());
            Node bodyP = parseNode(tokenizer.nextToken());
            Node bodyO = parseNode(tokenizer.nextToken());
            TriplePattern body = new TriplePattern(bodyS, bodyP, bodyO);
            return new BRWRule(head, body);
        } catch (NoSuchElementException e) {
            throw new ReasonerException("Illegal BRWRule: " + rulespec);
        }
    }
    
    /**
     * Use the rule to implement the given query. This will
     * instantiate the rule against the query, run the new query
     * against the whole reasoner+rawdata again and then rewrite the
     * results from that query according the rule.
     * @param query the query being processed
     * @param infGraph the parent infGraph that invoked us, will be called recursively
     * @param data the raw data graph which gets passed back to the reasoner as part of the recursive invocation
     * @param firedRules set of rules which have already been fired and should now be blocked
     * @return a ExtendedIterator which aggregates the matches and rewrites them
     * according to the rule
     */
    public ExtendedIterator execute(TriplePattern query, InfGraph infGraph, Finder data, HashSet firedRules) {
        TriplePattern iBody = instantiate(body, query);
        BRWRule iRule = new BRWRule(head, iBody);
        if (firedRules.contains(iRule)) {
            // No additional answers to be found
            return new NiceIterator();
        } 
        firedRules.add(iRule);
        Iterator it = ((RDFSInfGraph) infGraph).findNested(iBody, data, firedRules);
        firedRules.remove(iRule);
        return new RewriteIterator(it, iRule);
    }    

    /**
     * Return true if this rule is a a complete solution to the given
     * query and the router need look no further
     */
    public boolean completeFor(TriplePattern query) {
        return false;
    }
    
    /**
     * instantiate a triple pattern against a query/value
     */
    protected static TriplePattern instantiate(TriplePattern pattern, TriplePattern query) {
        return new TriplePattern( instantiate(pattern.getSubject(), query),
                                   instantiate(pattern.getPredicate(), query),
                                   instantiate(pattern.getObject(), query) );
    }

    /**
     * instantiate a rule body element against a query
     */
    protected static Node instantiate(Node elt, TriplePattern query) {
        if (elt.isVariable()) {
            String var = elt.getName();     // interned so can use simple equality test
            if (var.equals("s")) return query.getSubject();
            if (var.equals("p")) return query.getPredicate();
            if (var.equals("o")) return query.getObject();
        }
        return elt;
    }
    
    /**
     * instantiate a rule body element against a query ground value
     */
    protected static Node instantiate(Node elt, Triple value) {
        if (elt.isVariable()) {
            String var = elt.getName();     // interned so can use simple equality test
            if (var.equals("s")) return value.getSubject();
            if (var.equals("p")) return value.getPredicate();
            if (var.equals("o")) return value.getObject();
        }
        return elt;
    }

    /**
     * Assistant method to makeRule than parses a token as a node.
     */
    public static Node parseNode(String token) {
        if (token.startsWith("?")) {
            return Node.makeVariable(token.substring(1));
        } else if (token.equals("_")) {
            return Node.makeVariable("*");
        } else if (token.indexOf(':') != -1) {
            int split = token.indexOf(':');
            String nsPrefix = token.substring(0, split);
            String localname = token.substring(split+1);
            if (nsPrefix.equalsIgnoreCase("rdf")) {
                return Node.makeURI(RDF.getURI() + localname);
            } else if (nsPrefix.equalsIgnoreCase("rdfs")) {
                return Node.makeURI(RDFS.getURI() + localname);
            } else {
                return Node.makeURI(token);
            }
        } else {
            return Node.makeURI(token);
        }
    }
    
    /**
     * Printable string form
     */
    public String toString() {
        return head.toString() + " <- " + body.toString();
    }
    
        
    /**
     * Returns the body.
     * @return TriplePattern
     */
    public TriplePattern getBody() {
        return body;
    }

    /**
     * Returns the head.
     * @return TriplePattern
     */
    public TriplePattern getHead() {
        return head;
    }
    
    /** Equality override */
    public boolean equals(Object o) {
        return o instanceof BRWRule && 
                head.equals(((BRWRule)o).head) &&
                body.equals(((BRWRule)o).body) ;
    }
        
    /** hash function override */
    public int hashCode() {
        return (head.hashCode() >> 1) ^ body.hashCode();
    }

    /**
     * Inner class. This implements an iterator that uses the rule to rewrite any
     * results from the supplied iterator according to the rule.
     */
    static class RewriteIterator extends BaseExtendedIterator {
        /** The head of the rewrite rule */
        TriplePattern head;
        
        /** 
         * Constructor 
         * @param underlying the iterator whose results are to be rewritten
         * @param rule the BRWRule which defines the rewrite
         */
        public RewriteIterator(Iterator underlying, BRWRule rule) {
            super(underlying);
            this.head = rule.head;
        }
    
        /**
         * @see Iterator#next()
         */
        public Object next() {
            Triple value = (Triple)underlying.next();
            return new Triple( instantiate(head.getSubject(), value),
                                instantiate(head.getPredicate(), value),
                                instantiate(head.getObject(), value) );
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
