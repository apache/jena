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

package org.apache.jena.sparql.engine;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Node;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.Context;

/** Tests for query setup and execution not covered by tests elsewhere */
public class TestQueryEngine {

    @Test public void fixed_now() {
        String fixedNowStr = "'1970-01-01T00:00:00Z'^^xsd:dateTime";
        String queryString =
                "PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#>\n"+
                "ASK { FILTER ( NOW() = "+fixedNowStr+") }";
        Node fixedNow = SSE.parseNode(fixedNowStr);
        Context context = ARQ.getContext().copy();
        context.set(ARQConstants.sysCurrentTime, fixedNow);
        DatasetGraph dsg = DatasetGraphFactory.empty();

        boolean result = QueryExec.dataset(dsg)
                .query(queryString)
                .context(context)
                .ask();
        assertTrue(result, "NOW() not the expected fixed setting");
    }
}
