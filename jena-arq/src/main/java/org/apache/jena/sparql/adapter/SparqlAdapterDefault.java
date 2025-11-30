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

package org.apache.jena.sparql.adapter;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.QueryExecDatasetBuilderImpl;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.sparql.exec.UpdateExecDatasetBuilderImpl;

/**
 * The default adapter that executes SPARQL statements
 * against a DatasetGraph using the default engines.
 */
public class SparqlAdapterDefault
    implements SparqlAdapter
{
    private final DatasetGraph dsg;

    public SparqlAdapterDefault(DatasetGraph dsg) {
        super();
        this.dsg = dsg;
    }

    @Override
    public QueryExecBuilder newQuery() {
        return QueryExecDatasetBuilderImpl.create()
            .dataset(dsg)
            // Execution tracking via pre-configured builder. Alternative: hard code into build() method.
            // .transformExec(QueryExecTransformExecTracking.get())
            ;
    }

    @Override
    public UpdateExecBuilder newUpdate() {
        return UpdateExecDatasetBuilderImpl.create()
            .dataset(dsg)
            // Execution tracking via pre-configured builder. Alternative: hard code into build() method.
            // .transformExec(UpdateExecTransformExecTracking.get())
            ;
    }
}
