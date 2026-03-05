/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.exec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Graph;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.adapter.QueryExecBuilderProvider;
import org.apache.jena.sparql.adapter.SparqlAdapterRegistry;
import org.apache.jena.sparql.adapter.UpdateExecBuilderProvider;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.DatasetGraphWrapper;
import org.apache.jena.sparql.engine.Plan;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineFactoryWrapper;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.ref.QueryEngineRef;
import org.apache.jena.sparql.modify.UpdateEngine;
import org.apache.jena.sparql.modify.UpdateEngineFactory;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.Context;

public class TestSparqlAdapterSystem {

    // Turning off deferred builders should cause all deferred test cases to fail.
    // Reason: With deferred builders turned off,
    //         {Query|Update}ExecBuilder.newBuilder() will return builders for
    //         ARQ's default engines. This bypasses the extra configuration applied
    //         in the deferred builders of this test case class.
    // static { SystemARQ.DeferredExecBuilders = false; }

    /** Custom DatasetGraph type for the reference query engine. */
    private static class DatasetGraphRef extends DatasetGraphWrapper {
        public DatasetGraphRef() { super(DatasetGraphFactory.create()); }
    }

    /** Register a custom SparqlAdapterRegistry with the dsg which
     *  sets the specified query engine when using {Query|Update}ExecBuilders. */
    private static void setQueryEngine(DatasetGraph dsg, QueryEngineFactory qef) {
        SparqlAdapterRegistry adapterReg = new SparqlAdapterRegistry();
        SparqlAdapterRegistry.set(dsg.getContext(), adapterReg);

        QueryEngineRegistry qeReg = new QueryEngineRegistry();
        qeReg.add(qef);

        adapterReg.add(new QueryExecBuilderProvider() {
            @Override public boolean accept(DatasetGraph dsg, Context cxt) { return dsg instanceof DatasetGraphRef; }
            @Override public QueryExecBuilder create(DatasetGraph dsg, Context cxt) {
                return QueryExecDatasetBuilderImpl.create().dataset(dsg)
                    .context(cxt)
                    .set(ARQConstants.registryQueryEngines, qeReg);
            }
        });
        adapterReg.add(new UpdateExecBuilderProvider() {
            @Override public boolean accept(DatasetGraph dsg, Context cxt) { return dsg instanceof DatasetGraphRef; }
            @Override public UpdateExecBuilder create(DatasetGraph dsg, Context cxt) {
                return UpdateExecDatasetBuilderImpl.create().dataset(dsg)
                    .context(cxt)
                    .set(ARQConstants.registryQueryEngines, qeReg);
            }
        });
    }

    @Test
    public void testDeferredDatasetBuilderQuery() {
        DatasetGraph dsg = new DatasetGraphRef();
        QueryEngineFactoryWrapperUsageCount qef = new QueryEngineFactoryWrapperUsageCount(QueryEngineRef.getFactory());
        setQueryEngine(dsg, qef);
        dsg.getDefaultGraph().add(SSE.parseTriple("(:s :p :o)"));

        Graph graph = QueryExec.newBuilder()
            .dataset(dsg)
            .query("CONSTRUCT WHERE { ?s ?p ?o }")
            .construct();
        // RDFDataMgr.write(System.out, graph, RDFFormat.TURTLE_PRETTY);

        assertEquals(1, graph.size());
        assertEquals(1, qef.getAcceptCount());
        assertEquals(1, qef.getCreateCount());
    }

    @Test
    public void testDirectDatasetBuilderQuery() {
        DatasetGraph dsg = new DatasetGraphRef();
        QueryEngineFactoryWrapperUsageCount qef = new QueryEngineFactoryWrapperUsageCount(QueryEngineRef.getFactory());
        setQueryEngine(dsg, qef);
        dsg.getDefaultGraph().add(SSE.parseTriple("(:s :p :o)"));

        Graph graph = QueryExec.dataset(dsg)
            .query("CONSTRUCT WHERE { ?s ?p ?o }")
            .construct();

        assertEquals(1, graph.size());
        assertEquals(1, qef.getAcceptCount());
        assertEquals(1, qef.getCreateCount());
    }

    @Test
    public void testDeferredDatasetBuilderUpdate() {
        DatasetGraph dsg = new DatasetGraphRef();
        QueryEngineFactoryWrapperUsageCount qef = new QueryEngineFactoryWrapperUsageCount(QueryEngineRef.getFactory());
        setQueryEngine(dsg, qef);

        UpdateExec.newBuilder()
            .dataset(dsg)
            .update("INSERT { ?x ?x ?x } WHERE { BIND(<http://www.example.org/x> AS ?x) }")
            .execute();

        assertEquals(1, dsg.getDefaultGraph().size());
        assertEquals(1, qef.getAcceptCount());
        assertEquals(1, qef.getCreateCount());
    }

    @Test
    public void testDirectDatasetBuilderUpdate() {
        DatasetGraph dsg = new DatasetGraphRef();
        QueryEngineFactoryWrapperUsageCount qef = new QueryEngineFactoryWrapperUsageCount(QueryEngineRef.getFactory());
        setQueryEngine(dsg, qef);

        UpdateExec.dataset(dsg)
            .update("INSERT { ?x ?x ?x } WHERE { BIND(<http://www.example.org/x> AS ?x) }")
            .execute();
        // RDFDataMgr.write(System.out, graph, RDFFormat.TURTLE_PRETTY);

        assertEquals(1, dsg.getDefaultGraph().size());
        assertEquals(1, qef.getAcceptCount());
        assertEquals(1, qef.getCreateCount());
    }

    /** Wrapper that counts the calls to acceptX and createX. */
    private static class QueryEngineFactoryWrapperUsageCount extends QueryEngineFactoryForwarding {
        private final AtomicLong acceptCounter = new AtomicLong(0);
        private final AtomicLong createCounter = new AtomicLong(0);

        public QueryEngineFactoryWrapperUsageCount(QueryEngineFactory delegate) {
            super(delegate);
        }

        public long getAcceptCount() {
            return acceptCounter.get();
        }

        public long getCreateCount() {
            return createCounter.get();
        }

        @Override
        public boolean accept(Query query, DatasetGraph dataset, Context context) {
            acceptCounter.incrementAndGet();
            return super.accept(query, dataset, context);
        }

        @Override
        public Plan create(Query query, DatasetGraph dataset, Binding inputBinding, Context context) {
            createCounter.incrementAndGet();
            return super.create(query, dataset, inputBinding, context);
        }

        @Override
        public boolean accept(Op op, DatasetGraph dataset, Context context) {
            acceptCounter.incrementAndGet();
            return super.accept(op, dataset, context);
        }

        @Override
        public Plan create(Op op, DatasetGraph dataset, Binding inputBinding, Context context) {
            createCounter.incrementAndGet();
            return super.create(op, dataset, inputBinding, context);
        }
    }

    /**
     * Delegating wrapper for QueryEngineFactory.
     * Not to be confused with {@link QueryEngineFactoryWrapper} which unwraps dataset graphs.
     */
    private static class QueryEngineFactoryForwarding implements QueryEngineFactory {
        private QueryEngineFactory delegate;

        public QueryEngineFactoryForwarding(QueryEngineFactory delegate) {
            super();
            this.delegate = delegate;
        }

        public QueryEngineFactory getDelegate() {
            return delegate;
        }

        @Override
        public boolean accept(Query query, DatasetGraph dataset, Context context) {
            return getDelegate().accept(query, dataset, context);
        }

        @Override
        public Plan create(Query query, DatasetGraph dataset, Binding inputBinding, Context context) {
            return getDelegate().create(query, dataset, inputBinding, context);
        }

        @Override
        public boolean accept(Op op, DatasetGraph dataset, Context context) {
            return getDelegate().accept(op, dataset, context);
        }

        @Override
        public Plan create(Op op, DatasetGraph dataset, Binding inputBinding, Context context) {
            return getDelegate().create(op, dataset, inputBinding, context);
        }
    }

    /* Unused classes below because UpdateEngineRegistry cannot be overridden. */

    /*
    private static class UpdateEngineFactoryUsageCount extends UpdateEngineFactoryForwarding {
        private final AtomicLong acceptCounter = new AtomicLong(0);
        private final AtomicLong createCounter = new AtomicLong(0);

        public UpdateEngineFactoryUsageCount(UpdateEngineFactory delegate) {
            super(delegate);
        }

        public long getAcceptCount() {
            return acceptCounter.get();
        }

        public long getCreateCount() {
            return createCounter.get();
        }

        @Override
        public boolean accept(DatasetGraph datasetGraph, Context context) {
            acceptCounter.incrementAndGet();
            return super.accept(datasetGraph, context);
        }

        @Override
        public UpdateEngine create(DatasetGraph datasetGraph, Context context) {
            createCounter.incrementAndGet();
            return super.create(datasetGraph, context);
        }
    }

    private static class UpdateEngineFactoryForwarding implements UpdateEngineFactory {
        private UpdateEngineFactory delegate;

        public UpdateEngineFactoryForwarding(UpdateEngineFactory delegate) {
            super();
            this.delegate = delegate;
        }

        public UpdateEngineFactory getDelegate() {
            return delegate;
        }

        @Override
        public boolean accept(DatasetGraph datasetGraph, Context context) {
            return getDelegate().accept(datasetGraph, context);
        }

        @Override
        public UpdateEngine create(DatasetGraph datasetGraph, Context context) {
            return getDelegate().create(datasetGraph, context);
        }
    }
    */
}
