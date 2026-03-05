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

package org.apache.jena.rdflink.dataset;

import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.adapter.QueryExecBuilderProvider;
import org.apache.jena.sparql.adapter.SparqlAdapterRegistry;
import org.apache.jena.sparql.adapter.UpdateExecBuilderProvider;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.sparql.system.InitARQ;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sys.JenaSubsystemLifecycle;

/**
 * Initialize adapters for {@link DatasetGraphOverRDFLink}.
 */
public class InitDatasetGraphOverRDFLink implements JenaSubsystemLifecycle {

    @Override
    public void start() {
        init();
    }

    @Override
    public void stop() {}

    /**
     * Must initialize after {@link InitARQ} because otherwise ARQ would always take precedence.
     * Must also initialize before certain extensions such as execution tracking.
     */
    @Override
    public int level() {
        return 40 ;
    }

    private static boolean initialized = false;

    public synchronized static void init() {
        if (!initialized) {
            initialized = true;

            SparqlAdapterRegistry.addProvider(QueryExecBuilderProviderOverRDFLink.get());
            SparqlAdapterRegistry.addProvider(UpdateExecBuilderProviderOverRDFLink.get());
        }
    }

    public static class QueryExecBuilderProviderOverRDFLink implements QueryExecBuilderProvider {
        private static final QueryExecBuilderProviderOverRDFLink INSTANCE = new QueryExecBuilderProviderOverRDFLink();
        public static QueryExecBuilderProviderOverRDFLink get() { return INSTANCE; }

        private QueryExecBuilderProviderOverRDFLink() {}

        @Override
        public boolean accept(DatasetGraph dsg, Context context) {
            return dsg instanceof DatasetGraphOverRDFLink;
        }

        @Override
        public QueryExecBuilder create(DatasetGraph dsg, Context context) {
            DatasetGraphOverRDFLink d = (DatasetGraphOverRDFLink)dsg;
            return new QueryExecBuilderOverRDFLink(new RDFLinkCreatorAdapter(d), dsg).context(context);
        }
    }

    public static class UpdateExecBuilderProviderOverRDFLink implements UpdateExecBuilderProvider {
        private static final UpdateExecBuilderProviderOverRDFLink INSTANCE = new UpdateExecBuilderProviderOverRDFLink();
        public static UpdateExecBuilderProviderOverRDFLink get() { return INSTANCE; }

        private UpdateExecBuilderProviderOverRDFLink() {}

        @Override
        public boolean accept(DatasetGraph dsg, Context context) {
            return dsg instanceof DatasetGraphOverRDFLink;
        }

        @Override
        public UpdateExecBuilder create(DatasetGraph dsg, Context context) {
            DatasetGraphOverRDFLink d = (DatasetGraphOverRDFLink)dsg;
            return new UpdateExecBuilderOverRDFLink(new RDFLinkCreatorAdapter(d), dsg).context(context);
        }
    }

    /** {@code Creator<RDFLink>} implementation that delegates to {@link DatasetGraphOverRDFLink#newLink()}. */
    private static class RDFLinkCreatorAdapter
        implements Creator<RDFLink>
    {
        private DatasetGraphOverRDFLink dsg;

        public RDFLinkCreatorAdapter(DatasetGraphOverRDFLink dsg) {
            super();
            this.dsg = dsg;
        }

        // public DatasetGraph getDataset() { return dsg; }

        @Override public RDFLink create() { return dsg.newLink(); }
    }
}
