/******************************************************************
 * File:        BaseInfGraph.java
 * Created by:  Dave Reynolds
 * Created on:  18-Jan-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: BaseInfGraph.java,v 1.3 2003-04-11 09:57:55 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * A base level implementation of the InfGraph interface.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.3 $ on $Date: 2003-04-11 09:57:55 $
 */
public abstract class BaseInfGraph extends GraphBase implements InfGraph {

    /** The Reasoner instance which performs all inferences and Tbox lookups */
    protected Reasoner reasoner;
    
    /** The graph of raw data which is being reasoned over */
    protected FGraph fdata;

    /**
     * Constructor
     * @param data the raw data file to be augmented with entailments
     * @param reasoner the engine, with associated tbox data, whose find interface
     * can be used to extract all entailments from the data.
     */
    public BaseInfGraph(Graph data, Reasoner reasoner) {
        this.fdata = new FGraph(data);
        this.reasoner = reasoner;
    }
        
    /**
     * Return the raw RDF data Graph being processed (i.e. the argument
     * to the Reasonder.bind call that created this InfGraph).
     */
    public Graph getRawGraph() {
        return fdata.getGraph();
    }
    
    /**
     * Return the Reasoner which is being used to answer queries to this graph.
     */
    public Reasoner getReasoner() {
        return reasoner;
    }

    /**
     * Test a global boolean property of the graph. This might included
     * properties like consistency, OWLSyntacticValidity etc.
     * It remains to be seen what level of generality is needed here. We could
     * replace this by a small number of specific tests for common concepts.
     * @param property the URI of the property to be tested 
     * @return a Node giving the value of the global property, this may 
     * be a boolean literal, some other literal value (e.g. a size).
     */    
    public Node getGlobalProperty(Node property) {
        throw new ReasonerException("Global property not implemented: " + property);
    }
    
    /**
     * A convenience version of getGlobalProperty which can only return
     * a boolean result.
     */
    public boolean testGlobalProperty(Node property) {
        Node resultNode = getGlobalProperty(property);
        if (resultNode.isLiteral()) {
            Object result = resultNode.getLiteral().getValue();
            if (result instanceof Boolean) {
                return ((Boolean)result).booleanValue();
            }
        }
        throw new ReasonerException("Global property test returned non-boolean value" +
                                     "\nTest was: " + property +
                                     "\nResult was: " + resultNode);
    }
    
    /**
     * Test the consistency of the bound data. This normally tests
     * the validity of the bound instance data against the bound
     * schema data. 
     * @return a ValidityReport structure
     */
    public ValidityReport validate() {
        return new StandardValidityReport();
    }
    
   /**
     * An extension of the Graph.find interface which allows the caller to 
     * encode complex expressions in RDF and then refer to those expressions
     * within the query triple. For example, one might encode a class expression
     * and then ask if there are any instances of this class expression in the
     * InfGraph.
     * @param subject the subject Node of the query triple, may be a Node in 
     * the graph or a node in the parameter micro-graph or null
     * @param property the property to be retrieved or null
     * @param object the object Node of the query triple, may be a Node in 
     * the graph or a node in the parameter micro-graph.    
     * @param param a small graph encoding an expression which the subject and/or
     * object nodes refer.
     */
    public ExtendedIterator find(Node subject, Node property, Node object, Graph param) {
        return find(subject, property, object);
    }
    
    /** 
     * Returns an iterator over Triples.
     */
    public ExtendedIterator find(TripleMatch m) {
        return find(m.getSubject(), m.getPredicate(), m.getObject())
             .filterKeep(new TripleMatchFilter(m));
    }
    
    /** 
     * Returns an iterator over Triples.
     */
    public ExtendedIterator find(Node subject, Node property, Node object) {
        return findWithContinuation(new TriplePattern(subject, property, object), fdata);
     }

    /**
     * Basic pattern lookup interface.
     * @param pattern a TriplePattern to be matched against the data
     * @return a ExtendedIterator over all Triples in the data set
     *  that match the pattern
     */
    public ExtendedIterator find(TriplePattern pattern) {
        return findWithContinuation(pattern, fdata);
    }
    
    /**
     * Extended find interface used in situations where the implementator
     * may or may not be able to answer the complete query. It will
     * attempt to answer the pattern but if its answers are not known
     * to be complete then it will also pass the request on to the nested
     * Finder to append more results.
     * @param pattern a TriplePattern to be matched against the data
     * @param continuation either a Finder or a normal Graph which
     * will be asked for additional match results if the implementor
     * may not have completely satisfied the query.
     */
    abstract public ExtendedIterator findWithContinuation(TriplePattern pattern, Finder continuation);
    
    /** 
        returns this Graph's reifier. Each call on a given Graph gets the same
        Reifier object.
    */
    public Reifier getReifier() {
        return null;
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

