/******************************************************************
 * File:        BaseRule.java
 * Created by:  Dave Reynolds
 * Created on:  26-Jan-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: BaseFRule.java,v 1.2 2003-02-10 10:14:12 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rdfsReasoner1;

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.transitiveReasoner.TransitiveReasoner;
import com.hp.hpl.jena.graph.*;

import java.util.*;

/**
 * Base class for forward rules. Holds a head pattern (which will
 * be matched against the triple store) and an array of body patterns.
 * The body patterns can be instantiated following a successful head
 * match and then processed be descendant class.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2003-02-10 10:14:12 $
 */
public class BaseFRule {

    /** The head triple pattern */
    protected TriplePattern head;
    
    /** The body triple patterns */
    protected TriplePattern[] body;
    
    /**
     * Constructor
     */
    public BaseFRule(TriplePattern head, TriplePattern[] body) {
        this.head = head;
        this.body = body;
    }
    
    /**
     * Constructor
     */
    public BaseFRule(String spec) {
        List patterns = parseTripleSequence(spec);
        head = (TriplePattern) patterns.get(0);
        body = new TriplePattern[patterns.size() - 1];
        for (int i = 1; i < patterns.size(); i++) {
            body[i-1] = (TriplePattern) patterns.get(i);
        }
    }
    
    /**
     * Match the rule against a single triple.
     * Instantiating the variables then firing the consequent action.
     */
    public void bindAndFire(Triple value, RDFSInfGraph reasoner) {
        // special case filter of reflexive subClass/subProp cases
        // somewhat hacky doing it here ...
        if ((value.getPredicate().equals(TransitiveReasoner.subPropertyOf) ||
             value.getPredicate().equals(TransitiveReasoner.subClassOf))
             && value.getSubject().equals(value.getObject())) {
                // skip this case
                return;
        }
        Map bindings = new HashMap();
        matchNode(value.getSubject(), head.getSubject(), bindings);
        matchNode(value.getPredicate(), head.getPredicate(), bindings);
        matchNode(value.getObject(), head.getObject(), bindings);
        // Instantiate the body
        TriplePattern[] newBody = new TriplePattern[body.length];
        for (int i = 0; i < body.length; i++) {
            newBody[i] = new TriplePattern(
                                instantiate(body[i].getSubject(), bindings),
                                instantiate(body[i].getPredicate(), bindings),
                                instantiate(body[i].getObject(), bindings));
        }
        fire(newBody, reasoner);
    }
    
    /**
     * Called when the rule fires.
     * Subclasses should override.
     */
    void fire(TriplePattern[] body, RDFSInfGraph reasoner) {
    }
                    
    /**
     * Match a single node pair and add any new variable binding
     */
    static void matchNode(Node valueNode, Node patternNode, Map bindings) {
        if (patternNode.isVariable()) {
            bindings.put(patternNode.getName(), valueNode);
        }
    }
    
    /**
     * Instantiate a node using the bindings
     */
    static Node instantiate(Node elt, Map bindings) {
        if (elt.isVariable()) {
            Node result = (Node) bindings.get(elt.getName());
            if (result != null) return result;
        }
        return elt;
    }
    
    /**
     * Assistant method to parse a string into a triple 
     */
    public static Triple parseTriple(String spec) {
        StringTokenizer tokenizer = new StringTokenizer(spec);
        try {
            Node s = BRWRule.parseNode(tokenizer.nextToken());
            Node p = BRWRule.parseNode(tokenizer.nextToken());
            Node o = BRWRule.parseNode(tokenizer.nextToken());
            return new Triple(s, p, o);
        } catch (NoSuchElementException e) {
            throw new ReasonerException("Illegal triple: " + spec);
        }
    } 
          
    /**
     * Assistant method to parse a token stream into a triple pattern
     */
    private static TriplePattern parseTriplePattern(StringTokenizer tokenizer) {
        try {
            Node s = BRWRule.parseNode(tokenizer.nextToken());
            Node p = BRWRule.parseNode(tokenizer.nextToken());
            Node o = BRWRule.parseNode(tokenizer.nextToken());
            return new TriplePattern(s, p, o);
        } catch (NoSuchElementException e) {
            throw new ReasonerException("Illegal triple in rule");
        }
    }       
    
    /**
     * Assistant method to parse a string into a sequence oftriple patterns.
     * The patterns may be separated by "<-", "->" or "|" strings.
     * @return a list of TriplePatterns
     */
    public static List parseTripleSequence(String spec) {
        StringTokenizer tokenizer = new StringTokenizer(spec);
        List triples = new ArrayList();
        while (tokenizer.hasMoreElements()) {
            triples.add(parseTriplePattern(tokenizer));
            if (tokenizer.hasMoreElements()) {
                String sep = tokenizer.nextToken();
                if (!sep.equals("|") && !sep.equals("->") && !sep.equals("<-")) {
                    throw new ReasonerException("Illegal FRUle spec: " + spec);
                }
            }
        }
        return triples;
    }       
    /**
     * Returns the head.
     * @return TriplePattern
     */
    public TriplePattern getHead() {
        return head;
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
