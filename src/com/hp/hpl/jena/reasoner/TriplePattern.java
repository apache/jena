/******************************************************************
 * File:        TriplePattern.java
 * Created by:  Dave Reynolds
 * Created on:  18-Jan-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: TriplePattern.java,v 1.20 2004-06-30 12:58:01 chris-dollin Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.util.HashUtils;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Datastructure which defines a triple pattern as used in simple
 * rules and in find interfaces. 
 * <p>
 * Wildcards are recorded by using Node_RuleVariable entries rather than
 * nulls because they can be named. If a null is specified that is
 * converted to a variable of name "". Note that whilst some engines might simply
 * require Node_Variables the forward engine requires variables represented using
 * the more specialized subclass - Node_RuleVariable.</p>
 * <p>
 * It would make more sense to have TriplePattern subclass Triple
 * but that is final for some strange reason.</p>
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.20 $ on $Date: 2004-06-30 12:58:01 $
 */
public class TriplePattern implements ClauseEntry {

    /** The subject element of the pattern */
    protected Node subject;
    
    /** The predicate element of the pattern */
    protected Node predicate;
    
    /** The object element of the pattern */
    protected Node object;
    
    /**
     * Constructor - builds a pattern from three nodes,
     * use Node_RuleVariables as variables, use a variable
     * with an empty name as a wildcard, can also use null
     * as a wildcard.
     */
    public TriplePattern(Node subject, Node predicate, Node object) {
        this.subject   = normalize(subject);
        this.predicate = normalize(predicate);
        this.object    = normalize(object);
    }
    
    /**
     * Constructor - builds a pattern from a standard triple match.
     * Node that any filter part of the triple match will not be
     * represented within the pattern and will need to be checked
     * for separately.
     */
    public TriplePattern(TripleMatch match) {
        this.subject   = normalize(match.getMatchSubject());
        this.predicate = normalize(match.getMatchPredicate());
        this.object    = normalize(match.getMatchObject());
    }
    
    /**
     * Constructor - builds a dgenerate pattern from a simple triple.
     * This would be much easier if we merged Triples and TriplePatterns!
     */
    public TriplePattern(Triple match) {
        this.subject   = normalize(match.getSubject());
        this.predicate = normalize(match.getPredicate());
        this.object    = normalize(match.getObject());
    }
    
    /**
     * Returns the object.
     * @return Node
     */
    public Node getObject() {
        return object;
    }

    /**
     * Returns the predicate.
     * @return Node
     */
    public Node getPredicate() {
        return predicate;
    }

    /**
     * Returns the subject.
     * @return Node
     */
    public Node getSubject() {
        return subject;
    }

    /**
     * Return the triple pattern as a triple match
     */
    public TripleMatch asTripleMatch() {
        return Triple.createMatch(toMatch(subject), 
                                        toMatch(predicate), 
                                        toMatch(object));
    }

    /**
     * Return the triple pattern as a triple 
     */
    public Triple asTriple() {
        return new Triple(subject,predicate, object);
    }
    
    /**
     * Compare two patterns for compatibility - i.e. potentially unifiable.
     * Two patterns are "compatible" in the sense we mean here
     * if all their ground terms match. A variable in either pattern
     * can match a ground term or a variable in the other. We are not,
     * currently, checking for multiple occurances of the same variable.
     * Functor-valued object literals are treated as a special case which 
     * are only checked for name/arity matching.
     */
    public boolean compatibleWith(TriplePattern pattern) {
        boolean ok = subject.isVariable() || pattern.subject.isVariable() || subject.equals(pattern.subject);
        if (!ok) return false;
        ok =  predicate.isVariable() || pattern.predicate.isVariable() || predicate.equals(pattern.predicate);
        if (!ok) return false;
        if (object.isVariable() || pattern.object.isVariable()) return true;
        // Left with checking compatibility of ground literals
        if (Functor.isFunctor(object) && Functor.isFunctor(pattern.object)) {
            Functor functor = (Functor)object.getLiteral().getValue();
            Functor pFunctor = (Functor)pattern.object.getLiteral().getValue();
            return (functor.getName().equals(pFunctor.getName()) 
                            && functor.getArgs().length == pFunctor.getArgs().length);
        } else {
            return object.sameValueAs(pattern.object);
        } 
    }
    
    /**
     * Test if a pattern is just a variant of this pattern. I.e. it is the same
     * up to variable renaming. This takes into account multiple occurances
     * of the same variable.
     */
    public boolean variantOf(TriplePattern pattern) {
        Map vmap = HashUtils.createMap();
        if ( ! variantOf(subject, pattern.subject, vmap) ) return false;
        if ( ! variantOf(predicate, pattern.predicate, vmap) ) return false;
        if (Functor.isFunctor(object) && Functor.isFunctor(pattern.object)) {
            Functor functor = (Functor)object.getLiteral().getValue();
            Functor pFunctor = (Functor)pattern.object.getLiteral().getValue();
            if ( ! functor.getName().equals(pFunctor.getName()) ) return false;
            Node[] args = functor.getArgs();
            Node[] pargs = pFunctor.getArgs();
            if ( args.length != pargs.length ) return false;
            for (int i = 0; i < args.length; i++) {
                if ( ! variantOf(args[i], pargs[i], vmap) ) return false;
            }
            return true; 
        } else {
            return variantOf(object, pattern.object, vmap);
        } 
    }
    
    /**
     * Test if one node is a variant of another give a table of variable matches.
     */
    private boolean variantOf(Node n, Node p, Map vmap) {
        if (n instanceof Node_RuleVariable) {
            if (p instanceof Node_RuleVariable) {
                Object nMatch = vmap.get(n);
                if (nMatch == null) {
                    // First match of these pairs
                    vmap.put(n, p);
                    return true;
                } else {
                    return nMatch == p;
                }
            } else {
                return false;
            }
        } else {
            return n.sameValueAs(p);
        }
    }
    
    /**
     * Check a pattern to see if it is legal, used to exclude backchaining goals that
     * could never be satisfied. A legal pattern cannot have literals in the subject or
     * predicate positions and is not allowed nested functors in the object.
     */
    public boolean isLegal() {
        if (subject.isLiteral() || predicate.isLiteral()) return false;
        if (Functor.isFunctor(subject)) return false;
        if (Functor.isFunctor(object)) {
            Node[] args = ((Functor)object.getLiteral().getValue()).getArgs();
            for (int i = 0; i < args.length; i++) {
                if (Functor.isFunctor(args[i])) return false;  
            }
        }
        return true;
    }
    
    /**
     * Compare two patterns and return true if arg is a more
     * specific (more grounded) version of this one.
     * Does not handle functors.
     */
    public boolean subsumes(TriplePattern arg) {
        return (subject.isVariable()  || subject.equals(arg.subject))
            && (predicate.isVariable() || predicate.equals(arg.predicate))
            && (object.isVariable() || object.equals(arg.object));
    }
    
    /**
     * Test if the pattern is ground, contains no variables.
     */
    public boolean isGround() {
        if (subject.isVariable() || predicate.isVariable() || object.isVariable()) return false;
        if (Functor.isFunctor(object)) {
            return ((Functor)object.getLiteral().getValue()).isGround();
        }
        return true;
    }
    
    /**
     * Printable string
     */
    public String toString() {
        return simplePrintString(subject) + 
                " @" + simplePrintString(predicate) + 
                " " + simplePrintString(object);
    }
    
    /**
     * Simplified printable name for a triple
     */
    public static String simplePrintString(Triple t) {
        return simplePrintString(t.getSubject()) + 
                " @" + simplePrintString(t.getPredicate()) + 
                " " + simplePrintString(t.getObject());
    }

    /**
     * Simplified printable name for a node
     */
    public static String simplePrintString(Node n) {
        if (n instanceof Node_URI) {
            String uri = n.getURI();
            int split = uri.lastIndexOf('#');
            if (split == -1) {
                split = uri.lastIndexOf('/');
                if (split == -1) split = -1;
            }
            String ns = uri.substring(0, split+1);
            String prefix = "";
            if (ns.equals(RDF.getURI())) {
                prefix = "rdf:";
            } else if (ns.equals(RDFS.getURI())) {
                prefix = "rdfs:";
            }
            return prefix + uri.substring(split+1);
        } else {
            return n.toString();
        }
    }
            
    /**
     * Convert any null wildcards to Node_RuleVariable wildcards.
     */
    private static Node normalize(Node node) {
        if (node == null || node == Node.ANY) return Node_RuleVariable.WILD;
//        if (node == null) return Node.ANY;
        return node;
    }
            
    /**
     * Convert any Node_RuleVariable wildcards to null. This loses
     * the variable named but is used when converting a singleton
     * pattern to a TripleMtch
     */
    private static Node toMatch(Node node) {
        return node.isVariable() ? null : node;
    }
    
    /** 
     * Equality override - used so that TriplePattern variants (same to within variable renaming) test as equals
     */
    public boolean equals(Object o) {
//        return o instanceof TriplePattern && 
//                subject.equals(((TriplePattern)o).subject) &&
//                predicate.equals(((TriplePattern)o).predicate) &&
//                object.equals(((TriplePattern)o).object);
        return o instanceof TriplePattern &&
                nodeEqual(subject, ((TriplePattern)o).subject) &&
                nodeEqual(predicate, ((TriplePattern)o).predicate) &&
                nodeEqual(object, ((TriplePattern)o).object);
    }
    
    /** Helper - equality override on nodes */
    private boolean nodeEqual(Node n1, Node n2) {
        if ((n1 instanceof Node_RuleVariable) && (n2 instanceof Node_RuleVariable)) {
            return true;
        } else {
            return n1.equals(n2);
        }
    }
        
    /** hash function override */
    public int hashCode() {
        int hash = 0;
        if (!(subject instanceof Node_RuleVariable)) hash ^= (subject.hashCode() >> 1);
        if (!(predicate instanceof Node_RuleVariable)) hash ^= predicate.hashCode();
        if (!(object instanceof Node_RuleVariable)) hash ^= (object.hashCode() << 1);
        return hash;
//        return (subject.hashCode() >> 1) ^ predicate.hashCode() ^ (object.hashCode() << 1);
    }
    
    /**
     * Compare triple patterns, taking into account variable indices.
     * The equality function ignores differences between variables.
     */
    public boolean sameAs(Object o) {
        if (! (o instanceof TriplePattern) ) return false;
        TriplePattern other = (TriplePattern) o;
        return Node_RuleVariable.sameNodeAs(subject, other.subject) && Node_RuleVariable.sameNodeAs(predicate, other.predicate) && Node_RuleVariable.sameNodeAs(object, other.object);
    }
    
}

/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
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

