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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.junit.Test;


public class TestQueryExecDataset {
    // Most QueryExec testing gets tested via QueryExecution usage

    @Test public void queryExec_substitution_01() {
        QueryExec queryExec = QueryExec.dataset(DatasetGraphFactory.empty())
                .query("SELECT * { ?s ?p ?o }")
                .substitution(Var.alloc("s"), SSE.parseNode(":x"))
                .build();
        Query query = queryExec.getQuery();

        String s = query.toString();
        assertTrue(s.contains("http://example/x"));
    }

    private static final Symbol testSymbol = Symbol.create("a:b:c");

    @Test public void queryExec_substitution_02() {
        QueryExecBuilder builder = QueryExec.dataset(DatasetGraphFactory.empty())
                .query("SELECT * { ?s ?p ?o }")
                .set(testSymbol, "TRUE")
                .substitution(Var.alloc("s"), SSE.parseNode(":x"));

        QueryExec queryExec = builder.build();
        Context cxt = queryExec.getContext();

        assertTrue(cxt.isDefined(testSymbol));
        assertEquals("TRUE", cxt.get(testSymbol));

        builder.set(testSymbol, "FALSE");
        assertEquals("TRUE", cxt.get(testSymbol));

        // Different context.
        Context cxt2 = builder.build().getContext();
        assertEquals("FALSE", cxt2.get(testSymbol));
    }

}
