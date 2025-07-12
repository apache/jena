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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.sse.SSE;

/** Miscellaneous tests, e.g. from reports. */
public class TestQueryExecution {
    @Test public void lateral_with_join() {
        // GH-2896 LATERAL
        String qsReport = """
            SELECT * {
                BIND( 'x' AS ?xIn )
                LATERAL {
                    VALUES ?x { 1 }
                    { SELECT ?xIn ?xOut { BIND(?xIn AS ?xOut) } }
                }
            }
            """;

        Table expected = SSE.parseTable("(table (row (?xIn 'x') (?x 1) (?xOut 'x') ) )");
        Table actual = QueryExec.dataset(DatasetGraphFactory.empty()).query(qsReport).table();
        assertEquals(expected, actual);
    }

    @Test public void lateral_with_nesting() {
        // GH-2924
        String qsReport = """
            SELECT * {
                BIND(1 AS ?s)
                LATERAL {
                    BIND(?s AS ?x)
                    LATERAL {
                        BIND(?s AS ?y)
                    }
                }
            }
            """;

        Table expected = SSE.parseTable("(table (row (?s 1) (?x 1) (?y 1) ) )");
        Table actual = QueryExec.dataset(DatasetGraphFactory.empty()).query(qsReport).table();
        assertEquals(expected, actual);
    }
}
