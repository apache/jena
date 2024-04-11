/**
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

package org.apache.jena.sparql.core;

import static org.junit.Assert.assertEquals;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Test cases that operate on the query execution level (whole query). */
public abstract class AbstractTestQueryExec {
    protected abstract Dataset createDataset() ;
    protected abstract void releaseDataset(Dataset ds) ;

    protected Dataset  dataset ;

    @Before public void before()
    {
        dataset = createDataset() ;
    }

    @After public void after() {
        releaseDataset(dataset) ;
    }

    /* join skew tests based on unbound values. See also: AbstractTestInnerJoin */

    /** GH-2412 */
    @Test
    public void join_skew_01() {
        testCount("SELECT * { VALUES ?x { 0 } { VALUES ?x { UNDEF } FILTER(bound(?x)) } }", 0, dataset);
    }

    /** GH-2412 */
    @Test
    public void join_skew_02() {
        testCount("SELECT * { BIND(0 AS ?x) { BIND(coalesce() AS ?x) FILTER(bound(?x)) } }", 0, dataset);
    }

    /**
     * Execute the given query on the given dataset and assert that the number of result rows
     * matches the expected value.
     *
     * This method has package visibility because it is also called from {@link AbstractTestDynamicDataset}.
     */
    static void testCount(String queryString, int expected, Dataset ds)
    {
        Query query = QueryFactory.create(queryString) ;
        QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
        ResultSet rs = qExec.execSelect() ;
        int n = ResultSetFormatter.consume(rs) ;
        assertEquals(expected, n) ;
    }
}
