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

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.*;
import java.util.Iterator;

/**
 * A base level implementation of the InfGraph interface.
 */
public abstract class BaseInfGraph extends GraphBase implements InfGraph {

    /** The Reasoner instance which performs all inferences and Tbox lookups */
    protected Reasoner reasoner;

    /** The graph of raw data which is being reasoned over */
    protected FGraph fdata;

    /** Flag, if set to true then derivations are recorded */
    protected boolean recordDerivations;

    /** Flag to record if the preparation call has been made and so the graph is ready for queries */
    private volatile boolean isPrepared = false;

    /** version count */
    protected volatile int version = 0;
    
    /**
         Inference graphs share the prefix-mapping of their underlying raw graph.
     	@see com.hp.hpl.jena.graph.Graph#getPrefixMapping()
    */
    @Override
    public PrefixMapping getPrefixMapping()
        { return getRawGraph().getPrefixMapping(); }

    /**
     * Constructor
     * @param data the raw data file to be augmented with entailments
     * @param reasoner the engine, with associated tbox data, whose find interface
     * can be used to extract all entailments from the data.
     */
    public BaseInfGraph(Graph data, Reasoner reasoner) {
        super( );
        this.fdata = new FGraph( data );
        this.reasoner = reasoner;
        }

    /**
        Answer the InfCapabilities of this InfGraph.
     */
    @Override
    public Capabilities getCapabilities() {
        if (capabilities == null) {
            return getReasoner().getGraphCapabilities();
        } else {
            return capabilities;
        }
    }

    /**
        An InfCapabilities notes that size may not be accurate, and some
        triples may be irremovable.

        TODO accomodate the properties of the base graph, too.
    */
    public static class InfCapabilities extends AllCapabilities
        {
        @Override
        public boolean sizeAccurate() { return false; }
        @Override
        public boolean deleteAllowed( boolean every ) { return !every; }
        @Override
        public boolean iteratorRemoveAllowed() { return false; }
        @Override
        public boolean findContractSafe() { return false; }
        }

    /**
        An InfCapabilities notes that size may not be accurate, and some
        triples may be irremovable.

        TODO accomodate the properties of the base graph, too.
    */
    public static class InfFindSafeCapabilities extends InfCapabilities
        {
        @Override
        public boolean findContractSafe() { return true; }
        }

    /**
        @deprecated Bulk update operations are going to be removed.  
        @see GraphUtil for convenience helpers.
    */
    
    @Override
    @Deprecated
    public BulkUpdateHandler getBulkUpdateHandler()
        {
        if (bulkHandler == null) bulkHandler = new InfBulkUpdateHandler( this );
        return bulkHandler;
        }

    /**
        InfBulkUpdateHandler - a bulk update handler specialised for inference
        graphs by code for <code>removeAll()</code>.
    */
    static class InfBulkUpdateHandler extends SimpleBulkUpdateHandler
    	{
        public InfBulkUpdateHandler( BaseInfGraph  graph )
            { super(graph); }

        @Override
        @Deprecated
        public void remove( Node s, Node p, Node o )
            {
            BaseInfGraph g = (BaseInfGraph) graph;
            g.getRawGraph().remove( s, p, o );
            g.discardState();
            g.rebind();
            manager.notifyEvent( graph, GraphEvents.remove( s, p, o ) );
            }

        @Override
        @Deprecated
        public void removeAll()
            {
            BaseInfGraph g = (BaseInfGraph) graph;
            g.getRawGraph().clear();
            g.discardState();
            g.rebind();
            g.getEventManager().notifyEvent( g, GraphEvents.removeAll );
            }
    	}
    
    @Override
    public void remove( Node s, Node p, Node o )
    {
        getRawGraph().remove( s, p, o );
        discardState();
        rebind();
        getEventManager().notifyEvent( this, GraphEvents.remove( s, p, o ) );
    }
    
    @Override
    public void clear()
    {
        getRawGraph().clear() ;
        discardState();
        rebind();
        getEventManager().notifyEvent( this, GraphEvents.removeAll );
    }

    
    @Override
    public TransactionHandler getTransactionHandler()
        { return new InfTransactionHandler( this ); }

    public static class InfTransactionHandler
        extends TransactionHandlerBase implements TransactionHandler
        {
        protected final BaseInfGraph base;

        public InfTransactionHandler( BaseInfGraph base )
            { this.base = base; }

        @Override
        public boolean transactionsSupported()
            { return getBaseHandler().transactionsSupported(); }

        protected TransactionHandler getBaseHandler()
            { return base.getRawGraph().getTransactionHandler(); }

        @Override
        public void begin()
            { getBaseHandler().begin(); }

        @Override
        public void abort()
            { getBaseHandler().abort();
            base.rebind(); }

        @Override
        public void commit()
            { getBaseHandler().commit(); }
        }

    /**
     	discard any state that depends on the content of fdata, because
     	it's just been majorly trashed, solid gone.
    */
    protected void discardState()
        {}

    /**
     * Return the raw RDF data Graph being processed (i.e. the argument
     * to the Reasonder.bind call that created this InfGraph).
     */
    @Override
    public Graph getRawGraph() {
        return fdata.getGraph();
    }

    /**
     * Return the Reasoner which is being used to answer queries to this graph.
     */
    @Override
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
    @Override
    public synchronized void rebind(Graph data) {
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
    @Override
    public synchronized void rebind() {
        version++;
        isPrepared = false;
    }

    /**
     * Reset any internal caches. Some systems, such as the tabled backchainer,
     * retain information after each query. A reset will wipe this information preventing
     * unbounded memory use at the expense of more expensive future queries. A reset
     * does not cause the raw data to be reconsulted and so is less expensive than a rebind.
     */
    @Override
    public void reset() {
        version++;
    }

    /**
     * Perform any initial processing and caching. This call is optional. Most
     * engines either have negligable set up work or will perform an implicit
     * "prepare" if necessary. The call is provided for those occasions where
     * substantial preparation work is possible (e.g. running a forward chaining
     * rule system) and where an application might wish greater control over when
     * this prepration is done.
     */
    @Override
    public synchronized void prepare() {
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
    @Override
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
    @Override
    public Node getGlobalProperty(Node property) {
        throw new ReasonerException("Global property not implemented: " + property);
    }

    /**
     * A convenience version of getGlobalProperty which can only return
     * a boolean result.
     */
    @Override
    public boolean testGlobalProperty(Node property) {
        Node resultNode = getGlobalProperty(property);
        if (resultNode.isLiteral()) {
            Object result = resultNode.getLiteralValue();
            if (result instanceof Boolean) {
                return (Boolean) result;
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
    @Override
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
    @Override
    public ExtendedIterator<Triple> find(Node subject, Node property, Node object, Graph param) {
        return cloneWithPremises(param).find(subject, property, object);
    }

    /**
     * Returns an iterator over Triples.
     *
     * <p>This code used to have the .filterKeep component uncommented. We
     * think this is because of earlier history, before .matches on a literal node
     * was implemented as sameValueAs rather than equals. If it turns out that
     * the filter is needed, it can be commented back in, AND a corresponding
     * filter added to find(Node x 3) -- and test cases, of course.
     */
    @Override
    public ExtendedIterator<Triple> graphBaseFind(TripleMatch m) {
        return graphBaseFind(m.getMatchSubject(), m.getMatchPredicate(), m.getMatchObject())
             // .filterKeep(new TripleMatchFilter(m.asTriple()))
             ;
    }

    /**
     * Returns an iterator over Triples.
     * This implementation assumes that the underlying findWithContinuation
     * will have also consulted the raw data.
     */
    @Override
    public ExtendedIterator<Triple> graphBaseFind(Node subject, Node property, Node object) {
        return findWithContinuation(new TriplePattern(subject, property, object), fdata);
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
    abstract public ExtendedIterator<Triple> findWithContinuation(TriplePattern pattern, Finder continuation);


    /**
     * Basic pattern lookup interface.
     * This implementation assumes that the underlying findWithContinuation
     * will have also consulted the raw data.
     * @param pattern a TriplePattern to be matched against the data
     * @return a ExtendedIterator over all Triples in the data set
     *  that match the pattern
     */
    public ExtendedIterator<Triple> find(TriplePattern pattern) {
        checkOpen();
        return findWithContinuation(pattern, fdata);
    }

    /**
     * Switch on/off drivation logging
     */
    @Override
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
    @Override
    public Iterator<Derivation> getDerivation(Triple triple) {
        return null;
    }

    /**
     * Return the number of triples in the just the base graph
     */
    @Override
    public int graphBaseSize() {
        checkOpen();
        return fdata.getGraph().size();
    }

    /**
        Answer true iff this graph is empty. [Used to be in QueryHandler, but moved in
        here because it's a more primitive operation.]
    */
    @Override
    public boolean isEmpty() {
        return fdata.getGraph().isEmpty();
    }

    /**
     * Free all resources, any further use of this Graph is an error.
     */
    @Override
    public void close() {
        if (!closed) {
            fdata.getGraph().close();
            fdata = null;
            super.close();
        }
    }

    /**
     * Return a version stamp for this graph which can be
     * used to fast-fail concurrent modification exceptions.
     */
    public int getVersion() {
        return version;
    }
    
    /**
     * Add one triple to the data graph, run any rules triggered by
     * the new data item, recursively adding any generated triples.
     */
    @Override
    public synchronized void performAdd(Triple t) {
        version++;
        this.requirePrepared();
        fdata.getGraph().add(t);
    }

    /**
     * Removes the triple t (if possible) from the set belonging to this graph.
     */
    @Override
    public void performDelete(Triple t) {
        version++;
        this.requirePrepared();
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
        MultiUnion union = new MultiUnion();
        Graph raw = getRawGraph();
        union.addGraph( raw );
        union.setBaseGraph( raw );
        union.addGraph( premises );
        Graph schema = getSchemaGraph();
        if (schema != null) {
            if (schema instanceof BaseInfGraph) {
                BaseInfGraph ischema = (BaseInfGraph)schema;
                Graph sschema = ischema.getSchemaGraph();
                if (sschema != null) union.addGraph( sschema );
                Graph rschema = ischema.getRawGraph();
                if (rschema != null) union.addGraph( rschema );
            }
            
        }
        return getReasoner().bind(union);
    }

    /**
         Answer true iff this graph has been through the <code>prepare()</code> step.
         For testing purposes.
     * @return Whether the graph is prepared
    */
    public synchronized boolean isPrepared()
        { return isPrepared;  }

    /**
     * Reset prepared state to false
     */
    protected synchronized void setPreparedState(boolean state) {
        this.isPrepared = state;
    }
    
    /**
     * Checks whether the graph is prepared and calls {@link #prepare()} if it is not
     */
    protected synchronized void requirePrepared() {
        if (!this.isPrepared) this.prepare();
    }
}
