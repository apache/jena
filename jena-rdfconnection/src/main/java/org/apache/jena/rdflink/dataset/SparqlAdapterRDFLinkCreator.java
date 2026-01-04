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

package org.apache.jena.rdflink.dataset;

import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.adapter.SparqlAdapter;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.QueryExecDatasetBuilder;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.sparql.exec.UpdateExecDatasetBuilder;

/**
 * Adapter that wraps a source of RDFLinks with builders that mimic those for DatasetGraphs.
 * The adapter thus returns {@link QueryExecDatasetBuilder} and {@link UpdateExecDatasetBuilder} views
 * over the link source.
 *
 * The life cycle of links is as follows:
 * For queries, a link is only created when {@link QueryExecDatasetBuilder#build()} is called. The link is closed
 * when the corresponding QueryExec is closed.
 * For updates, a link is only created during {@link UpdateExec#execute()} and closed when this method completes.
 */
public class SparqlAdapterRDFLinkCreator
    implements SparqlAdapter
{
    private Creator<RDFLink> rdfLinkCreator;
    private DatasetGraph dataset;

    public SparqlAdapterRDFLinkCreator(Creator<RDFLink> rdfLinkCreator, DatasetGraph dataset) {
        super();
        this.rdfLinkCreator = rdfLinkCreator;
        this.dataset = dataset;
    }

    @Override
    public QueryExecBuilder newQuery() {
        return new QueryExecDatasetBuilderOverRDFLink(rdfLinkCreator, dataset);
    }

    @Override
    public UpdateExecBuilder newUpdate() {
        return new UpdateExecDatasetBuilderOverRDFLink(rdfLinkCreator, dataset);
    }
}
