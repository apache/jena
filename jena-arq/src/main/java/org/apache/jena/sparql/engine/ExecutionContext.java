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
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.main.OpExecutor;
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
    public ExecutionContext(ExecutionContext other) {
        this.context = other.context;
        this.dataset = other.dataset;
        this.openIterators = other.openIterators;
        this.allIterators = other.allIterators;
        this.activeGraph = other.activeGraph;
        this.executor = other.executor;
        this.cancelSignal = other.cancelSignal;
    }

    /** Clone and change active graph - shares tracking */
    public ExecutionContext(ExecutionContext other, Graph activeGraph) {
        this(other);
        this.activeGraph = activeGraph;
    }

    /** Setup with defaults of global settings */
    public ExecutionContext(DatasetGraph dataset) {
        this(dataset, QC.getFactory(ARQ.getContext()));
    }

    /** Setup with defaults of global settings but explicit {@link OpExecutor} factory. */
    public ExecutionContext(DatasetGraph dataset, OpExecutorFactory factory) {
        this(ARQ.getContext().copy(), dataset.getDefaultGraph(), dataset, factory);
    }

    public ExecutionContext(Context params, Graph activeGraph, DatasetGraph dataset, OpExecutorFactory factory) {
        this(params, activeGraph, dataset, factory, cancellationSignal(params));
    }

    private static AtomicBoolean cancellationSignal(Context cxt) {
        if ( cxt == null )
            return null;
        try {
            return cxt.get(ARQConstants.symCancelQuery);
        } catch (ClassCastException ex) {
            Log.error(ExecutionContext.class, "Class cast exception: Expected AtomicBoolean for cancel control: "+ex.getMessage());
            return null;
        }
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
    public Context getContext()       { return context; }

    public AtomicBoolean getCancelSignal()       { return cancelSignal; }

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
            return null;
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
