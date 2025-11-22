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

package org.apache.jena.sparql.exec;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.lib.Alarm;
import org.apache.jena.atlas.lib.AlarmClock;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.query.QueryExecException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.describe.DescribeHandler;
import org.apache.jena.sparql.core.describe.DescribeHandlerRegistry;
import org.apache.jena.sparql.engine.Plan;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.Timeouts.Timeout;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIteratorWrapper;
import org.apache.jena.sparql.graph.GraphOps;
import org.apache.jena.sparql.modify.TemplateLib;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.ModelUtils;

/** All the SPARQL query result forms at the graph-level. */

public class QueryExecDataset implements QueryExec
{
    public static QueryExecDatasetBuilder newBuilder() { return QueryExecDatasetBuilder.create(); }

    private final Query              query;
    private String                   queryString = null;
    private final QueryEngineFactory qeFactory;
    private final Context            context;
    private final DatasetGraph       dataset;

    private QueryIterator            queryIterator    = null;
    private Plan                     plan             = null;
    private Binding                  initialBinding   = null;

    private boolean                  closed;
    private AtomicReference<TimeoutCallback> expectedCallback = new AtomicReference<>(null);
    private Alarm                    timeout1Alarm    = null;
    private Alarm                    timeout2Alarm    = null;

    // synchronization.
    private final Object             lockTimeout      = new Object();
    private static final long        TIMEOUT_UNSET    = -1;
    private static final long        TIMEOUT_INF      = -2;
    private long                     timeout1         = TIMEOUT_UNSET;
    private long                     timeout2         = TIMEOUT_UNSET;
    private final AlarmClock         alarmClock       = AlarmClock.get();
    private long                     queryStartTime   = -1; // Unset
    private AtomicBoolean            cancelSignal;

    protected QueryExecDataset(Query query, String queryString, DatasetGraph datasetGraph, Context cxt,
                               QueryEngineFactory qeFactory, Timeout timeout,
                               Binding initialToEngine) {
        // Context cxt is already a safe copy.
        this.query = query;
        this.queryString = queryString;
        this.dataset = datasetGraph;
        this.qeFactory = qeFactory;
        this.context = (cxt == null) ? Context.setupContextForDataset(cxt, datasetGraph) : cxt;
        this.timeout1 = timeout.initialTimeoutMillis();
        this.timeout2 = timeout.overallTimeoutMillis();
        // See also query substitution handled in QueryExecBuilder
        this.initialBinding = initialToEngine;

        // Cancel signal may originate from e.g. an update execution.
        this.cancelSignal = Context.getOrSetCancelSignal(context);

        init();
    }

    private void init() {
        Context.setCurrentDateTime(context);
        if ( query != null )
            context.put(ARQConstants.sysCurrentQuery, query);
    }

    @Override
    public void close() {
        closed = true;
        if ( queryIterator != null )
            queryIterator.close();
        if ( plan != null )
            plan.close();
        if ( timeout1Alarm != null )
            alarmClock.cancel(timeout1Alarm);
        if ( timeout2Alarm != null )
            alarmClock.cancel(timeout2Alarm);
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    private void checkNotClosed() {
        if ( closed )
            throw new QueryExecException("HTTP QueryExecution has been closed");
    }

    @Override
    public void abort() {
        cancelSignal.set(true);
        synchronized (lockTimeout) {
            // This is called asynchronously to the execution.
            // synchronized is for coordination with other calls of
            // .abort and with the timeout2 reset code.
            if ( queryIterator != null )
                // we notify the chain of iterators,
                // however, we do *not* close the
                // iterators.
                // That happens after the cancellation
                // is properly over.
                queryIterator.cancel();
        }
    }

    @Override
    public RowSet select() {
        checkNotClosed();
        if ( !query.isSelectType() )
            throw new QueryExecException("Attempt to have RowSet from a " + labelForQuery(query) + " query");
        RowSet rowSet = execute();
        return rowSet;
    }

    private RowSet execute() {
        startQueryIterator();
        List<Var> vars = query.getResultVars().stream().map(Var::alloc).toList();
        return RowSetStream.create(vars, queryIterator);
    }

    private void fixupResultVars() {
        if ( initialBinding != null && ! initialBinding.isEmpty()) {
            //query = query.cloneQuery();
            synchronized(query) {
                query.setQueryResultStar(true);
                //query.resetResultVars();
            }
        }
    }

    // -- Construct
    @Override
    public Graph construct(Graph graph) {
        checkNotClosed();
        try {
            Iterator<Triple> it = constructTriples();
            // Prefixes for result
            insertPrefixesInto(graph);
            GraphOps.addAll(graph, it);
        }
        finally {
            this.close();
        }
        return graph;
    }

    @Override
    public Iterator<Triple> constructTriples() {
        checkNotClosed();
        if ( !query.isConstructType() )
            throw new QueryExecException("Attempt to get a CONSTRUCT model from a " + labelForQuery(query) + " query");
        // This causes there to be no PROJECT around the pattern.
        // That in turn, exposes the initial bindings.

        fixupResultVars();

        startQueryIterator();

        Template template = query.getConstructTemplate();
        return TemplateLib.calcTriples(template.getTriples(), queryIterator);
    }

    // -- Construct Quads

    @Override
    public Iterator<Quad> constructQuads() {
        checkNotClosed();
        if ( !query.isConstructType() )
            throw new QueryExecException("Attempt to get a CONSTRUCT model from a " + labelForQuery(query) + " query");
        // This causes there to be no PROJECT around the pattern.
        // That in turn, exposes the initial bindings.

        fixupResultVars();

        startQueryIterator();

        Template template = query.getConstructTemplate();
        return TemplateLib.calcQuads(template.getQuads(), queryIterator);
    }

    @Override
    public DatasetGraph constructDataset(DatasetGraph dataset) {
        try {
            Iterator<Quad> iter = constructQuads();
            iter.forEachRemaining(dataset::add);
            Iter.close(iter);
            insertPrefixesInto(dataset);
        } finally {
            this.close();
        }
        return dataset;
    }

    // -- Describe

    @Override
    public Graph describe(Graph graph) {
        checkNotClosed();
        Model model = ModelFactory.createModelForGraph(graph);

        if ( !query.isDescribeType() )
            throw new QueryExecException("Attempt to get a DESCRIBE result from a " + labelForQuery(query) + " query");

        // If there was no WhereClause, use an empty pattern (one solution, no
        // columns).
        if ( query.getQueryPattern() == null )
            query.setQueryPattern(new ElementGroup());

        fixupResultVars();

        Set<Node> set = new HashSet<>();

        RowSet rows = execute();

        // Prefixes for result (after initialization)
        insertPrefixesInto(graph);

        // Variables in DESCRIBE
        if ( rows != null ) {
            // Single pass over rows.
            rows.forEachRemaining(row->{
                for ( Var var : rows.getResultVars() ) {
                    Node n = row.get(var);
                    if ( n != null )
                        set.add(n);
                }
            });
        }

        // Any URIs in the DESCRIBE
        if ( query.getResultURIs() != null ) {
            query.getResultURIs().forEach(set::add);
        }

        // DescribeHandlers work on models.

        // Create new handlers for this process.
        List<DescribeHandler> dhList = DescribeHandlerRegistry.get().newHandlerList();
        getContext().put(ARQConstants.sysCurrentDataset, getDataset());
        // Notify start of describe phase
        for ( DescribeHandler dh : dhList )
            dh.start(model, getContext());

        // Do describe for each resource found.
        for ( Node n : set ) {
            RDFNode rdfNode = ModelUtils.convertGraphNodeToRDFNode(n, model);

            if ( rdfNode instanceof Resource ) {
                for ( DescribeHandler dh : dhList ) {
                    dh.describe((Resource)rdfNode);
                }
            } else {
                // Can't describe literals
                continue;
            }
        }

        for ( DescribeHandler dh : dhList )
            dh.finish();

        this.close();
        return graph;
    }

    // ?? Change to iterator from Describe Handlers.
    // (Streaming DESCRIBE isn't important enough to worry about)
    @Override
    public Iterator<Triple> describeTriples() {
        return describe().find();
    }

    @Override
    public boolean ask() {
        checkNotClosed();
        if ( !query.isAskType() )
            throw new QueryExecException("Attempt to have boolean from a " + labelForQuery(query) + " query");

        startQueryIterator();

        boolean r;
        try {
            // Not hasNext because setting timeout1 which applies to getting
            // the first result, not testing for it.
            queryIterator.next();
            r = true;
        } catch (NoSuchElementException ex) {
            r = false;
        } finally {
            this.close();
        }
        return r;
    }

    @Override
    public JsonArray execJson() {
        checkNotClosed();
        if ( !query.isJsonType() )
            throw new QueryExecException("Attempt to get a JSON result from a " + labelForQuery(query) + " query");
        startQueryIterator();
        JsonArray jsonArray = JsonResults.results(queryIterator, query.getJsonMapping());
        List<String> resultVars = query.getResultVars();
        return jsonArray;
    }

    @Override
    public Iterator<JsonObject> execJsonItems() {
        checkNotClosed();
        if ( !query.isJsonType() )
            throw new QueryExecException("Attempt to get a JSON result from a " + labelForQuery(query) + " query");
        startQueryIterator();
        return JsonResults.iterator(queryIterator, query.getJsonMapping());
    }

    private static boolean isTimeoutSet(long x) {
        return x >= 0;
    }

    class TimeoutCallback implements Runnable {
        @Override
        public void run() {
            // Abort query if and only if we are the expected callback.
            // If the first row has appeared, and we are removing timeout1
            // callback, it still may go off so it needs to check here
            // it's still wanted.
            if ( expectedCallback.get() == this ) {
                if ( cancelSignal != null )
                    cancelSignal.set(true);
            }
        }
    }

    private class QueryIteratorTimer2 extends QueryIteratorWrapper {

        public QueryIteratorTimer2(QueryIterator qIter) {
            super(qIter);
        }

        long yieldCount = 0;
        boolean resetDone = false;
        @Override
        protected Binding moveToNextBinding()
        {
            Binding b = super.moveToNextBinding();
            yieldCount++;

            if ( ! resetDone )
            {
                // Sync on calls of .abort.
                // So nearly not needed.
                synchronized(lockTimeout)
                {
                    TimeoutCallback callback = new TimeoutCallback();
                    expectedCallback.set(callback);
                    // Lock against calls of .abort() or of timeout1Callback.

                    // Update/check the volatiles in a careful order.
                    // This cause timeout1 not to call .abort and hence not set isCancelled

                    // But if timeout1 went off after moveToNextBinding, before expectedCallback is set,
                    // then forget the row and cancel the query.
                    if ( cancelSignal.get() )
                        // timeout1 went off after the binding was yielded but
                        // before we got here.
                        throw new QueryCancelledException();
                    if ( timeout1Alarm != null ) {
                        alarmClock.cancel(timeout1Alarm);
                        timeout1Alarm = null;
                    }

                    // Now arm the second timeout, if any.
                    if ( timeout2 > 0 ) {
                        // Set to time remaining.
                        long t = timeout2 - (System.currentTimeMillis()-queryStartTime);
                        // Not first timeout - finite second timeout for remaining time.
                        timeout2Alarm = alarmClock.add(callback, t);
                    }
                    resetDone = true;
                }
            }
            return b;
        }
    }

    protected void execInit() {
        if ( queryStartTime <= -1 )
            queryStartTime = System.currentTimeMillis();
    }

    /** Wrapper for starting the query iterator but also dealing with cancellation */
    private void startQueryIterator() {
        synchronized (lockTimeout) {
            if (cancelSignal.get()) {
                // Fail before starting the iterator if cancelled already
                throw new QueryCancelledException();
            }

            startQueryIteratorActual();

            if (cancelSignal.get()) {
                queryIterator.cancel();

                // Fail now if cancelled already
                throw new QueryCancelledException();
            }
        }
    }

    /** Start the query iterator, setting timeouts as needed. */
    private void startQueryIteratorActual() {
        if ( queryIterator != null ) {
            Log.warn(this, "Query iterator has already been started");
            return;
        }

        execInit();

        /* Timeouts:
         * -1,-1                No timeouts
         * N, same as -1,N      Overall timeout only.  No wrapper needed.
         * N,-1                 Timeout on first row only. Need to cancel on first row.
         * N,M                  First/overall timeout. Need to reset on first row.
         */

        if ( !isTimeoutSet(timeout1) && !isTimeoutSet(timeout2) ) {
            // Case -1,-1
            queryIterator = getPlan().iterator();
            return;
        }

        TimeoutCallback callback = new TimeoutCallback() ;
        expectedCallback.set(callback) ;

        if ( !isTimeoutSet(timeout1) && isTimeoutSet(timeout2) ) {
            // Case -1,N
            timeout2Alarm = alarmClock.add(callback, timeout2) ;
            // Start the query.
            queryIterator = getPlan().iterator();
            // But don't add resetter.
            return ;
        }

        // Case N,-1
        // Case N,M
        // Case isTimeoutSet(timeout1)
        //   Whether timeout2 is set is determined by QueryIteratorTimer2
        //   Subcase 2: ! isTimeoutSet(timeout2)
        // Add timeout to first row.

        timeout1Alarm = alarmClock.add(callback, timeout1) ;

        queryIterator = getPlan().iterator();
        // Add the timeout1->timeout2 resetter wrapper.
        queryIterator = new QueryIteratorTimer2(queryIterator);
    }

    private Plan getPlan() {
        if ( plan == null ) {
            Binding initial = ( initialBinding != null ) ? initialBinding : BindingFactory.root();
            plan = qeFactory.create(query, dataset, initial, getContext());
        }
        return plan;
    }

    private void insertPrefixesInto(Graph graph) {
        try {
            if ( dataset != null ) {
                // Load the models prefixes first
                PrefixMap m = dataset.prefixes();
                m.forEach((prefix, uri) -> graph.getPrefixMapping().setNsPrefix(prefix, uri));
            }
            // Then add the queries (just the declared mappings)
            // so the query declarations override the data sources.
            graph.getPrefixMapping().setNsPrefixes(query.getPrefixMapping());
        } catch (Exception ex) {
            Log.warn(this, "Exception in insertPrefixes: " + ex.getMessage(), ex);
        }
    }

    private void insertPrefixesInto(DatasetGraph dsg) {
        try {
            PrefixMap pmap = dsg.prefixes();

            if ( dataset != null ) {
                // Load the models prefixes first
                pmap.putAll(dataset.prefixes());
            }
            // Then add the queries (just the declared mappings)
            // so the query declarations override the data sources.
            query.getPrefixMapping().getNsPrefixMap().forEach((prefix, uri) -> pmap.add(prefix, uri));
        } catch (Exception ex) {
            Log.warn(this, "Exception in insertPrefixes: " + ex.getMessage(), ex);
        }

    }

    static private String labelForQuery(Query q) {
        if ( q.isSelectType() )     return "SELECT";
        if ( q.isConstructType() )  return "CONSTRUCT";
        if ( q.isDescribeType() )   return "DESCRIBE";
        if ( q.isAskType() )        return "ASK";
        if ( q.isJsonType() )       return "JSON";
        return "<<unknown>>";
    }

    @Override
    public Context getContext() { return context; }

    @Override
    public DatasetGraph getDataset() { return dataset; }

    @Override
    public Query getQuery()     { return query; }

    @Override
    public String getQueryString() {
        if ( queryString == null )
            queryString = query.toString();
        return queryString;
    }

}
