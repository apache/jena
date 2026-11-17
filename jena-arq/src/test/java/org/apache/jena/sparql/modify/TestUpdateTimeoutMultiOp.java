/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.jena.sparql.modify;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.Txn;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestUpdateTimeoutMultiOp {
    private static final String TWO_OPS =
            "DELETE { ?s ?p ?o } WHERE { ?s ?p ?o } ;"
                    + "INSERT { ?s ?p ?o } WHERE { GRAPH <http://example/g> { ?s ?p ?o } }";

    @Test
    public void update_timeout_two_operations() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();

        Txn.executeWrite(dsg, () -> {
            dsg.add(SSE.parseQuad("(<http://example/g> :s :p :o)"));
            dsg.getDefaultGraph().add(SSE.parseTriple("(:old :p :o)"));
        });

        Txn.executeWrite(dsg, () ->
                UpdateExec.dataset(dsg)
                        .update(TWO_OPS)
                        .timeout(600, TimeUnit.SECONDS)
                        .execute());

        Txn.executeRead(dsg, () -> {
            assertTrue(dsg.getDefaultGraph().contains(SSE.parseTriple("(:s :p :o)")));
            assertFalse(dsg.getDefaultGraph().contains(SSE.parseTriple("(:old :p :o)")));
        });
    }
}
