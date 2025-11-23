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

package org.apache.jena.sparql.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.main.OpExecutorFactory;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.util.Context;

public class ExecutionContext implements FunctionEnv
{
    private static final boolean TrackAllIterators = false;

    private Context context       = null;
    private DatasetGraph dataset  = null;

    // Iterator tracking
    private final Collection<QueryIterator> openIterators;
    // Tracking all iterators leads to a build up of state,
    private Collection<QueryIterator> allIterators     = null;
    private Graph activeGraph           = null;
    private OpExecutorFactory executor  = null;
    private final AtomicBoolean cancelSignal;

    /** Clone */
    public static ExecutionContext copy(ExecutionContext other) {
        return new ExecutionContext(other);
    }

    /** @deprecated Use {@link #copy(ExecutionContext)} */
    @Deprecated
    public ExecutionContext(ExecutionContext other) {
        this.context = other.context;
        this.dataset = other.dataset;
        this.openIterators = other.openIterators;
        this.allIterators = other.allIterators;
        this.activeGraph = other.activeGraph;
        this.executor = other.executor;
        this.cancelSignal = other.cancelSignal;
    }

    /** Create ExecutionContext from {@link FunctionEnv} */
    public static ExecutionContext fromFunctionEnv(FunctionEnv functionEnv) {
        return new ExecutionContext(functionEnv);
    }

    private ExecutionContext(FunctionEnv other) {
        this.context = other.getContext();
        this.dataset = other.getDataset();
        this.openIterators = new ArrayList<>();
        if ( TrackAllIterators )
            this.allIterators  = new ArrayList<>();
        else
            this.allIterators  = null;
        this.activeGraph = other.getActiveGraph();
        this.executor = QC.getFactory(context);
        this.cancelSignal = Context.getCancelSignal(context);
    }

    /** Clone and change active graph - shares tracking */
    public static ExecutionContext copyChangeActiveGraph(ExecutionContext other, Graph activeGraph) {
        return new ExecutionContext(other, activeGraph);
    }

    /**
     * Clone and change active graph - shares tracking
     * @deprecated Use {@link #copyChangeActiveGraph(ExecutionContext, Graph)}.
     */
    @Deprecated
    public ExecutionContext(ExecutionContext other, Graph activeGraph) {
        this(other);
        this.activeGraph = activeGraph;
    }

    /**
     * ExecutionContext for normal execution over a dataset, with defaults for
     * {@link Context} and {@link OpExecutorFactory}.
     */
    public static ExecutionContext create(DatasetGraph dataset) {
        Context cxt = ARQ.getContext().copy();
        return create(dataset, cxt);
    }

    /**
     * ExecutionContext for normal execution over a dataset,
     * with default for {@link OpExecutorFactory}.
     */
    public static ExecutionContext create(DatasetGraph dataset, Context context) {
        Objects.requireNonNull(dataset, "dataset is null in call to ExecutionContext create(DatasetGraph dataset, Context context)");
        Graph dftGraph = dataset.getDefaultGraph();
        return create(dataset, dftGraph, context);
    }

    /**
     * ExecutionContext for execution without a dataset,
     * with default for {@link OpExecutorFactory}.
     */
    public static ExecutionContext create(Context context) {
        return create(null, null, context);
    }


    /**
     * ExecutionContext for normal execution over a dataset, with defaults for
     * {@link Context} and {@link OpExecutorFactory}.
     */
    public static ExecutionContext create(DatasetGraph dataset, Graph activeGraph) {
        Context cxt = ARQ.getContext().copy();
        return create(dataset, activeGraph, cxt);
    }

    /**
     * ExecutionContext for normal execution over a dataset.
     */
    public static ExecutionContext create(DatasetGraph dataset, Graph activeGraph, Context context) {
        return new ExecutionContext(context,
                                    activeGraph, dataset,
                                    QC.getFactory(context),
                                    Context.getCancelSignal(context));
    }

    /**
     * ExecutionContext for normal execution over a graph, with defaults for
     * {@link Context} and {@link OpExecutorFactory}.
     */
    public static ExecutionContext createForGraph(Graph graph) {
        Context cxt = ARQ.getContext().copy();
        return createForGraph(graph, cxt);
    }

    /**
     * ExecutionContext for normal execution over a graph.
     */
    public static ExecutionContext createForGraph(Graph graph, Context cxt) {
        DatasetGraph dsg = (graph == null) ? null : DatasetGraphFactory.wrap(graph);
        return create(dsg, graph, cxt);
    }

    private ExecutionContext(Context params, Graph activeGraph, DatasetGraph dataset, OpExecutorFactory factory, AtomicBoolean cancelSignal) {
        this.context = params;
        this.dataset = dataset;
        this.openIterators = new ArrayList<>();
        if ( TrackAllIterators )
            this.allIterators  = new ArrayList<>();
        this.activeGraph = activeGraph;
        this.executor = factory;
        this.cancelSignal = cancelSignal;
    }

    @Override
    public Context getContext() {
        return context;
    }

    public AtomicBoolean getCancelSignal() {
        return cancelSignal;
    }

    /** Check the cancel signal and throw {@link QueryCancelledException}} if it is true. */
    public void checkCancelSignal() {
      if ( cancelSignal != null && cancelSignal.get() )
          throw new QueryCancelledException();
    }

    public void openIterator(QueryIterator qIter) {
        openIterators.add(qIter);
        if ( allIterators != null )
            allIterators.add(qIter);
    }

    public void closedIterator(QueryIterator qIter) {
        openIterators.remove(qIter);
    }

    public Iterator<QueryIterator> listOpenIterators() {
        return openIterators.iterator();
    }

    public Iterator<QueryIterator> listAllIterators() {
        if ( allIterators == null )
            return Iter.nullIterator();
        return allIterators.iterator();
    }

    public OpExecutorFactory getExecutor() {
        return executor;
    }

    /** Setter for the policy for algebra expression evaluation - use with care */
    public void setExecutor(OpExecutorFactory executor) {
        this.executor = executor;
    }

    @Override
    public DatasetGraph getDataset()  { return dataset; }

    /** Return the active graph (the one matching is against at this point in the query.
     * May be null if unknown or not applicable - for example, doing quad store access or
     * when sorting
     */
    @Override
    public Graph getActiveGraph()     { return activeGraph; }
}
