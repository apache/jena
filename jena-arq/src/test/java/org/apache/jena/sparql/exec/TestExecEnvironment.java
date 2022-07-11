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

import java.util.concurrent.TimeUnit;

import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionDatasetBuilder;
import org.apache.jena.query.QueryExecutionFactory;
import org.junit.Assert;
import org.junit.Test;

public class TestExecEnvironment {

    /** QueryExecutionAdapter.adapt must not return null; see https://github.com/apache/jena/issues/1393 */
    @Test
    public void testQueryExecAdapter() {
        try (QueryExecution qExec = QueryExecutionFactory.create("SELECT * { ?s ?p ?o }", DatasetFactory.empty())) {
            QueryExec qe  = QueryExecAdapter.adapt(qExec);
            Assert.assertNotNull(qe);
        }
    }

    /** Tests QueryExecBuilderAdapter's special logic for passing on the initialTimeout and
     * overallTimeout calls to the delegate. This should succeed without errors. */
    @Test
    public void testQueryExecutionDatasetBuilderWrapping() {
        QueryExecBuilder builder = QueryExecBuilderAdapter.adapt(QueryExecutionDatasetBuilder.create());

        try (QueryExec qe = builder
            .query("SELECT * { ?s ?p ?o }")
            .initialTimeout(1, TimeUnit.SECONDS)
            .timeout(2000)
            .overallTimeout(3, TimeUnit.SECONDS)
            .build()) {
        }
    }
}
