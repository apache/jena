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

package org.apache.jena.tdb2.solver.index;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.lib.ListUtils;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.system.Txn;
import org.junit.jupiter.api.function.Executable;

/**
 * JUnit test case executable to compare two datasets w.r.t.
 * their result of a select query.
 */
public class GraphCompareSelectResultExecutable
    implements Executable
{
    private static PrintStream out = System.err;

    private String       testLabel;
    private Query        query;
    private DatasetGraph referenceDsg;
    private DatasetGraph testDsg;

    public GraphCompareSelectResultExecutable(String testLabel, Query query, DatasetGraph referenceDsg, DatasetGraph testDsg) {
        super();
        this.testLabel = testLabel;
        this.query = query;
        this.referenceDsg = referenceDsg;
        this.testDsg = testDsg;
    }

    public String getTestLabel() {
        return testLabel;
    }

    public Query getQuery() {
        return query;
    }

    /**
     * Assert that graph.find() returned the same set of quads for the given pattern.
     * Duplicates are ignored.
     */
    @Override
    public void execute() throws Throwable {
        Table expectedTable = Txn.calculateRead(referenceDsg,
            () -> QueryExec.dataset(referenceDsg).query(query).table());
        List<Binding> expectedList = new ArrayList<>();
        expectedTable.rows().forEachRemaining(expectedList::add);

        Table actualTable = Txn.calculateRead(testDsg,
            () -> QueryExec.dataset(testDsg).query(query).table());
        List<Binding> actualList = new ArrayList<>();
        actualTable.rows().forEachRemaining(actualList::add);

        boolean b = ListUtils.equalsUnordered(expectedList, actualList);
        if ( ! b ) {
            out.println("Fail: find(" + query + ")");
            out.println("Expected: " + expectedList + ", Actual: " + actualList);
        }

        assertTrue(b, () -> getTestLabel());
    }

    @Override
    public String toString() {
        return getTestLabel() + " " + getQuery();
    }
}
