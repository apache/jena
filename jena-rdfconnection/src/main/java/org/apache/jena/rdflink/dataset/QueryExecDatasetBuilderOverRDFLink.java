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
import org.apache.jena.rdflink.dataset.todelete.QueryExecWrapperCloseRDFLink;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.QueryExecDatasetBuilderDeferredBase;

/**
 * A QueryExecBuilder over a creator of RDFLinks.
 * Link creation is deferred: The life cycle of the link is tied to
 * that of the QueryExec created by this builder.
 * This means that the link is created when the QueryExec is built, and
 * is closed when the QueryExec is closed.
 **/
public class QueryExecDatasetBuilderOverRDFLink
    extends QueryExecDatasetBuilderDeferredBase<QueryExecDatasetBuilderOverRDFLink>
{
    private Creator<RDFLink> linkCreator;

    public QueryExecDatasetBuilderOverRDFLink(Creator<RDFLink> linkCreator, DatasetGraph dataset) {
        super();
        this.linkCreator = linkCreator;
        this.dataset = dataset;
    }

    @Override
    protected QueryExecBuilder newActualExecBuilder() {
        RDFLink link = linkCreator.create();
        boolean parseCheck = effectiveParseCheck();
        QueryExecBuilder qeb = link.newQuery()
                .parseCheck(parseCheck)
                .transformExec(qe -> new QueryExecWrapperCloseRDFLink(qe, link));
        return qeb;
    }
}
