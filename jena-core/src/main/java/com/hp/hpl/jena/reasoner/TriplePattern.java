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

package com.hp.hpl.jena.reasoner;

import java.util.Map;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.rulesys.ClauseEntry;
import com.hp.hpl.jena.reasoner.rulesys.Functor;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.util.CollectionFactory;
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
            Functor functor = (Functor)object.getLiteralValue();
            Functor pFunctor = (Functor)pattern.object.getLiteralValue();
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
        Map<Node, Node> vmap = CollectionFactory.createHashedMap();
        if ( ! variantOf(subject, pattern.subject, vmap) ) return false;
        if ( ! variantOf(predicate, pattern.predicate, vmap) ) return false;
        if (Functor.isFunctor(object) && Functor.isFunctor(pattern.object)) {
            Functor functor = (Functor)object.getLiteralValue();
            Functor pFunctor = (Functor)pattern.object.getLiteralValue();
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
    private boolean variantOf(Node n, Node p, Map<Node, Node> vmap) {
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
            Node[] args = ((Functor)object.getLiteralValue()).getArgs();
            for ( Node arg : args )
            {
                if ( Functor.isFunctor( arg ) )
                {
                    return false;
                }
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
            return ((Functor)object.getLiteralValue()).isGround();
        }
        return true;
    }
    
    /**
     * Printable string
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
    public boolean sameAs(Object o) {
        if (! (o instanceof TriplePattern) ) return false;
        TriplePattern other = (TriplePattern) o;
        return Node_RuleVariable.sameNodeAs(subject, other.subject) && Node_RuleVariable.sameNodeAs(predicate, other.predicate) && Node_RuleVariable.sameNodeAs(object, other.object);
    }
    
}
