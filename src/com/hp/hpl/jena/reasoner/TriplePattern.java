/******************************************************************
 * File:        TriplePattern.java
 * Created by:  Dave Reynolds
 * Created on:  18-Jan-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: TriplePattern.java,v 1.4 2003-04-15 21:20:27 jeremy_carroll Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Datastructure which defines a triple pattern as used in simple
 * rules and in find interfaces. 
 * <p>
 * Wildcards are recorded by using Node_Variable entries rather than
 * nulls because they can be named. If a null is specified that is
 * converted to a variable of name "".</p>
 * <p>
 * It would make more sense to have TriplePattern subclass Triple
 * but that is final for some strange reason.</p>
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.4 $ on $Date: 2003-04-15 21:20:27 $
 */
public class TriplePattern {

    /** The subject element of the pattern */
    protected Node subject;
    
    /** The predicate element of the pattern */
    protected Node predicate;
    
    /** The object element of the pattern */
    protected Node object;
    
    /** A cached, preconstructed wildcard node */
    public static final Node nodeWildcard = Node.createVariable("*");
    
    /**
     * Constructor - builds a pattern from three nodes,
     * use Node_Variables as variables, use a variable
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
        return new StandardTripleMatch(toMatch(subject), 
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
     * Compare two patterns for compatibility.
     * Two patterns are "compatible" in the sense we mean here
     * if all their ground terms match. A variable in either pattern
     * can match a ground term or a variable in the other. We are not,
     * current checking for multiple occurances of the same variable.
     */
    public boolean compatibleWith(TriplePattern pattern) {
        return (subject.isVariable() || pattern.subject.isVariable() || subject.equals(pattern.subject))
            && (predicate.isVariable() || pattern.predicate.isVariable() || predicate.equals(pattern.predicate))
            && (object.isVariable() || pattern.object.isVariable() || object.equals(pattern.object));
    }
    
    /**
     * Compare two patterns and return true if arg is a more
     * specific (more grounded) version of this one.
     * This implies compatibleWith.
     */
    public boolean subsumes(TriplePattern arg) {
        return (subject.isVariable()  || subject.equals(arg.subject))
            && (predicate.isVariable() || predicate.equals(arg.predicate))
            && (object.isVariable() || object.equals(arg.object));
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
     * Convert any null wildcards to Node_Variable wildcards.
     */
    private static Node normalize(Node node) {
        return node == null ? nodeWildcard : node;
    }
            
    /**
     * Convert any Node_Variable wildcards to null. This loses
     * the variable named but is used when converting a singleton
     * pattern to a TripleMtch
     */
    private static Node toMatch(Node node) {
        return node.isVariable() ? null : node;
    }
    
    /** Equality override */
    public boolean equals(Object o) {
        return o instanceof TriplePattern && 
                subject.equals(((TriplePattern)o).subject) &&
                predicate.equals(((TriplePattern)o).predicate) &&
                object.equals(((TriplePattern)o).object);
    }
        
    /** hash function override */
    public int hashCode() {
        return (subject.hashCode() >> 1) ^ predicate.hashCode() ^ (object.hashCode() << 1);
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

