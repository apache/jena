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

package org.apache.jena.sparql.modify;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphOne;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.update.UpdateException;
import org.junit.Test;

/**
 * Tests of SILENT. The tests are written to work aginst {@link DatasetGraphOne}
 * which has the characteristic that it does not support named graphs.
 * <p>
 * Normally, datasets behave as if all named graphs exists for adding/copying/moving
 * into graphs without needing create.
 * <p>
 * JENA-2293
 */
public class TestUpdateSilent {

    private void test(String updateRequest) {
        // A characteristic of DatasetGraphOne is that it has no
        // named graphs and they can't be added.
        DatasetGraph dataset = DatasetGraphOne.create(GraphFactory.createGraphMem());
        UpdateExec.dataset(dataset).update(updateRequest).build().execute();
    }

    @Test(expected = UpdateException.class)
    public void LOAD_toNonExistingGraph() {
        test("LOAD <file:testing/Update/empty.nt> INTO GRAPH <http://example/no-such-graph>");
    }

    @Test
    public void LOAD_SILENT_toNonExistingGraph() {
        test("LOAD SILENT <file:testing/Update/empty.nt> INTO GRAPH <http://example/no-such-graph>");
    }


    @Test(expected = UpdateException.class)
    public void CLEAR_nonExistingGraph() {
        test("CLEAR GRAPH <http://example/no-such-graph>");
    }

    @Test
    public void CLEAR_SILENT_nonExistingGraph() {
        test("CLEAR SILENT GRAPH <http://example/no-such-graph>");
    }

    @Test(expected = UpdateException.class)
    public void CREATE_nonExistingGraph() {
        //The target is DatasetGraphOne which does not provide named graphs.
        test("CREATE GRAPH <file:testing/Update/empty.nt>");
    }

    @Test
    public void CREATE_SILENT_nonExistingGraph() {
        test("CREATE SILENT GRAPH <file:testing/Update/empty.nt>");
    }

    @Test
    public void DROP_ofNonExistingGraph() {
        // DROP non-existent is not an error.
        test("DROP GRAPH <http://example/no-such-graph>");
    }

    @Test
    public void DROP_SILENT_ofNonExistingGraph() {
        test("DROP SILENT GRAPH <http://example/no-such-graph>");
    }

    @Test(expected = UpdateException.class)
    public void COPY_toNonExistingGraph() {
        test("COPY DEFAULT TO <http://example/no-such-graph>");
    }

    @Test
    public void COPY_SILENT_toNonExistingGraph() {
        test("COPY SILENT DEFAULT TO <http://example/no-such-graph>");
    }

    @Test(expected = UpdateException.class)
    public void COPY_fromNonExistingGraph() {
        test("COPY <http://example/no-such-graph> TO DEFAULT");
    }

    @Test
    public void COPY_SILENT_fromNonExistingGraph() {
        test("COPY SILENT <http://example/no-such-graph> TO DEFAULT");
    }

    @Test(expected = UpdateException.class)
    public void MOVE_toNonExistingGraph_isError() {
        test("MOVE DEFAULT TO <http://example/no-such-graph>");
    }

    @Test
    public void MOVE_SILENT_toNonExistingGraph() {
        test("MOVE SILENT DEFAULT TO <http://example/no-such-graph>");
    }

    @Test(expected = UpdateException.class)
    public void MOVE_fromNonExistingGraph() {
        test("MOVE <http://example/no-such-graph> TO DEFAULT");
    }

    @Test
    public void MOVE_SILENT_fromNonExistingGraph() {
        test("MOVE SILENT <http://example/no-such-graph> TO DEFAULT");
    }

    @Test(expected = UpdateException.class)
    public void ADD_toNonExistingGraph() {
        test("ADD DEFAULT TO <http://example/no-such-graph>");
    }

    @Test
    public void ADD_SILENT_toNonExistingGraph() {
        test("ADD SILENT DEFAULT TO <http://example/no-such-graph>");
    }

    @Test(expected = UpdateException.class)
    public void ADD_fromNonExistingGraph() {
        test("ADD <http://example/no-such-graph> TO DEFAULT");
    }

    @Test
    public void ADD_SILENT_fromNonExistingGraph() {
        test("ADD SILENT <http://example/no-such-graph> TO DEFAULT");
    }
}
