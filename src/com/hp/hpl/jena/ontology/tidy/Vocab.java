/*
 * (c) Copyright 2003 Hewlett-Packard Development Company, LP  
 * [see end of file]
 */

package com.hp.hpl.jena.ontology.tidy;

import com.hp.hpl.jena.graph.*;

/**
 * The Vocabulary for OWL tidy's internal data
 * structures.
 * @author jjc
 *
 */
class Vocab {
	static final String NameSpace = "http://jena.hpl.hp.com/schemas/tidy#";
    static final Node category = Node.createURI(NameSpace+"category");
    static final Node objectOfTriple = Node.createURI(NameSpace+"objectOfTriple");
	static final Node firstPart = Node.createURI(NameSpace+"firstPart");
	static final Node secondPart = Node.createURI(NameSpace+"secondPart");
	static final Node cyclicState = Node.createURI(NameSpace+"cyclicState");
	static final Node disjointState = Node.createURI(NameSpace+"disjointState");
	
	// This gives a number which identifies the clique
	// This is functional for blank nodes, and multivalued for URI nodes.
	
	static final Node inDisjointClique = Node.createURI(NameSpace+"inDisjointClique");
    
    // mimics owlDisjointWith except is symmetric
    static final Node disjointWith = Node.createURI(NameSpace + "disjointWith");

    // transitive closure of owlDisjointWith
    // special case on URI nodes
	static final Node transDisjointWith = Node.createURI(NameSpace+"transDisjointWith");
    
}


/*
 * (c) Copyright 2003 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
