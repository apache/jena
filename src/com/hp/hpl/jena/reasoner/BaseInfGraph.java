/******************************************************************
 * File:        BaseInfGraph.java
 * Created by:  Dave Reynolds
 * Created on:  18-Jan-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: BaseInfGraph.java,v 1.20 2003-08-25 08:31:08 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.compose.Union;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.util.iterator.*;
import java.util.Iterator;

/**
 * A base level implementation of the InfGraph interface.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.20 $ on $Date: 2003-08-25 08:31:08 $
 */
public abstract class BaseInfGraph extends GraphBase implements InfGraph {

    /** The Reasoner instance which performs all inferences and Tbox lookups */
    protected Reasoner reasoner;
    
    /** The graph of raw data which is being reasoned over */
    protected FGraph fdata;

    /** Flag, if set to true then derivations are recorded */
    protected boolean recordDerivations;
    
    /** Flag to record if the preparation call has been made and so the graph is ready for queries */
    protected boolean isPrepared = false;

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
     * Replace the underlying data graph for this inference graph and start any
     * inferences over again. This is primarily using in setting up ontology imports
     * processing to allow an imports multiunion graph to be inserted between the
     * inference graph and the raw data, before processing.
     * @param data the new raw data graph
     */
    public void rebind(Graph data) {
        fdata = new FGraph(data);
        isPrepared = false;
    }
    
    /**
     * Cause the inference graph to reconsult the underlying graph to take
     * into account changes. Normally changes are made through the InfGraph's add and
     * remove calls are will be handled appropriately. However, in some cases changes
     * are made "behind the InfGraph's back" and this forces a full reconsult of
     * the changed data. 
     */
    public void rebind() {
        isPrepared = false;
    }
    
    /**
     * Reset any internal caches. Some systems, such as the tabled backchainer, 
     * retain information after each query. A reset will wipe this information preventing
     * unbounded memory use at the expense of more expensive future queries. A reset
     * does not cause the raw data to be reconsulted and so is less expensive than a rebind.
     */
    public void reset() {
    }
    
    /**
     * Perform any initial processing and caching. This call is optional. Most
     * engines either have negligable set up work or will perform an implicit
     * "prepare" if necessary. The call is provided for those occasions where
     * substantial preparation work is possible (e.g. running a forward chaining
     * rule system) and where an application might wish greater control over when
     * this prepration is done.
     */
    public void prepare() {
        // Default is to do no preparation
        isPrepared = true;
    }
    
    /**
     * Returns a derivations graph. The rule reasoners typically create a 
     * graph containing those triples added to the base graph due to rule firings.
     * In some applications it can useful to be able to access those deductions
     * directly, without seeing the raw data which triggered them. In particular,
     * this allows the forward rules to be used as if they were rewrite transformation
     * rules.
     * @return the deductions graph, if relevant for this class of inference
     * engine or null if not.
     */
    public Graph getDeductionsGraph() {
        return null;
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
        checkOpen();
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
        return cloneWithPremises(param).find(subject, property, object);
    }
    
    /** 
     * Returns an iterator over Triples.
     */
    public ExtendedIterator find(TripleMatch m) {
        return find(m.getMatchSubject(), m.getMatchPredicate(), m.getMatchObject())
             .filterKeep(new TripleMatchFilter(m.asTriple()));
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
     * Returns an iterator over Triples.
     * This implementation assumes that the underlying findWithContinuation 
     * will have also consulted the raw data.
     */
    public ExtendedIterator find(Node subject, Node property, Node object) {
        checkOpen();
        return findWithContinuation(new TriplePattern(subject, property, object), fdata);
    }

    /**
     * Basic pattern lookup interface.
     * This implementation assumes that the underlying findWithContinuation 
     * will have also consulted the raw data.
     * @param pattern a TriplePattern to be matched against the data
     * @return a ExtendedIterator over all Triples in the data set
     *  that match the pattern
     */
    public ExtendedIterator find(TriplePattern pattern) {
        checkOpen();
        return findWithContinuation(pattern, fdata);
    }
    
    /**
     * Test if the graph contains the given triple.
     * Overridden in order to implement semantic instead of syntactic
     * equivalance.
     */
    public boolean contains(Triple t) {
        checkOpen();
        ClosableIterator i = find(t.getSubject(), t.getPredicate(), t.getObject());
        boolean contained =  i.hasNext();
        i.close();
        return contained;
    }
    
    /**
     * Test if the graph contains the given triple.
     * Overridden in order to implement semantic instead of syntactic
     * equivalance.
     */
    public boolean contains(Node s, Node p, Node o) {
        checkOpen();
        ClosableIterator i = find(s, p, o);
        boolean contained =  i.hasNext();
        i.close();
        return contained;
    }
    
    /**
     * Switch on/off drivation logging
     */
    public void setDerivationLogging(boolean logOn) {
        recordDerivations = logOn;
    }
   
    /**
     * Return the derivation of the given triple (which is the result of
     * some previous find operation).
     * Not all reasoneers will support derivations.
     * @return an iterator over Derivation records or null if there is no derivation information
     * available for this triple.
     */
    public Iterator getDerivation(Triple triple) {
        return null;
    }

    /**
     * Return the number of triples in the just the base graph
     */
    public int size() {
        checkOpen();
        return fdata.getGraph().size();
    }
    
    /**
        Answer true iff this graph is empty. [Used to be in QueryHandler, but moved in
        here because it's a more primitive operation.]
    */
    public boolean isEmpty() {
        return fdata.getGraph().isEmpty();
    }
    
    /** 
     * Free all resources, any further use of this Graph is an error.
     */
    public void close() {
        if (!closed) {
            fdata.getGraph().close();
            fdata = null;
            super.close();
        }
    }
        
    /**
     * Add one triple to the data graph, run any rules triggered by
     * the new data item, recursively adding any generated triples.
     */
    public synchronized void performAdd(Triple t) {
        if (!isPrepared) prepare();
        fdata.getGraph().add(t);
    }
    
    /**
     * Returns the bitwise or of ADD, DELETE, SIZE and ORDERED,
     * to show the capabilities of this implementation of Graph.
     * So a read-only graph that finds in an unordered fashion,
     * but can tell you how many triples are in the graph returns
     * SIZE.
     */
    public int capabilities() {
        return ADD | DELETE | SIZE;
    }
    
    /** 
     * Removes the triple t (if possible) from the set belonging to this graph. 
     */   
    public void performDelete(Triple t) {
        if (!isPrepared) prepare();
        fdata.getGraph().delete(t);
    }

    /**
     * Return the schema graph, if any, bound into this inference graph.
     */
    public abstract Graph getSchemaGraph();
    
    /**
     * Return a new inference graph which is a clone of the current graph
     * together with an additional set of data premises. The default
     * implementation loses ALL partial deductions so far. Some subclasses
     * may be able to a more efficient job.
     */
    public InfGraph cloneWithPremises(Graph premises) {
        return getReasoner().bindSchema(getSchemaGraph()).bind(new Union(getRawGraph(), premises));
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

