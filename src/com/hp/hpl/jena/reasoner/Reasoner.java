/******************************************************************
 * File:        Reasoner.java
 * Created by:  Dave Reynolds
 * Created on:  09-Jan-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: Reasoner.java,v 1.4 2003-03-30 20:53:27 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner;

import com.hp.hpl.jena.graph.Graph;
//import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * The minimal interface to which all reasoners (or reasoner adaptors) conform. 
 * This only supports attaching the reasoner to a set of RDF graphs 
 * which represent the rules or ontologies and instance data. The actual
 * reasoner requests are made through the InfGraph which is generated once
 * the reasoner has been bound to a set of RDF data.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.4 $ on $Date: 2003-03-30 20:53:27 $
 */
public interface Reasoner {
    
    /**
     * This is most commonly used to attach an ontology (a set of tbox 
     * axioms in description logics jargon) to a reasoner. A certain amount
     * of precomputation may be done at this time (e.g. constructing the
     * class lattice). When the reasoner is later applied some instance data
     * these cached precomputations may be reused.
     * <p>In fact this call may be more general than the above 
     * description suggests. Firstly, a reasoner that supports arbitrary rules
     * rather than ontologies may use the same method to bind the reasoner
     * to the specific rule set (encoded in RDF). Secondly, even in the ontology
     * case a given reasoner may not require a strict separation of tbox and
     * abox - it may allow instance data in the tbox and terminology axioms in
     * the abox. </p>
     * <p>A reasoner is free to simply note this set of RDF and merge with any
     * future RDF rather than do processing at this time. </p>
     * @param tbox the ontology axioms or rule set encoded in RDF
     * @return a reasoner instace which can be used to process a data graph,
     * it may be the same instance - bindRuleset is not required to be side-effect free.
     * @throws ReasonerException if the reasoner cannot be
     * bound to a rule set in this way, for example if the underlying engine
     * can only accept a single rule set in this way and one rule set has
     * already been bound in of if the ruleset is illformed.
     */
    public Reasoner bindSchema(Graph tbox) throws ReasonerException;
    
    /**
     * Attach the reasoner to a set of RDF data to process.
     * The reasoner may already have been bound to specific rules or ontology
     * axioms (encoded in RDF) through earlier bindRuleset calls.
     * @param data the RDF data to be processed, some reasoners may restrict
     * the range of RDF which is legal here (e.g. syntactic restrictions in OWL).
     * @return an inference graph through which the data+reasoner can be queried.
     * @throws ReasonerException if the data is ill-formed according to the
     * constraints imposed by this reasoner.
     */
    public InfGraph bind(Graph data) throws ReasonerException;

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

