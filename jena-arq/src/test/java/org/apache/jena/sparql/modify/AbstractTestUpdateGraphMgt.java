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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.modify.request.Target;
import org.apache.jena.sparql.modify.request.UpdateAdd;
import org.apache.jena.sparql.modify.request.UpdateCreate;
import org.apache.jena.sparql.modify.request.UpdateDrop;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateException;
import org.junit.Test;

public abstract class AbstractTestUpdateGraphMgt extends AbstractTestUpdateBase
{
    static final Node graphIRI = NodeFactory.createURI("http://example/graph");

    @Test
    public void testCreateDrop1() {
        DatasetGraph gStore = getEmptyDatasetGraph();
        Update u = new UpdateCreate(graphIRI);

        UpdateAction.execute(u, gStore);
        assertTrue(graphEmpty(gStore.getGraph(graphIRI)));

        // With "auto SILENT" then these aren't errors.
        boolean silentMode = true;

        if ( !silentMode ) {
            // try again - should fail (already exists)
            try {
                UpdateAction.execute(u, gStore);
                fail();
            } catch (UpdateException ex) {}
        }

        // Drop it.
        u = new UpdateDrop(graphIRI);
        UpdateAction.execute(u, gStore);
        assertFalse(gStore.containsGraph(graphIRI));

        if ( !silentMode ) {
            // Drop it again. - should fail
            try {
                UpdateAction.execute(u, gStore);
                fail();
            } catch (UpdateException ex) {}
        }

    }

    @Test
    public void testCreateDrop2() {
        DatasetGraph gStore = getEmptyDatasetGraph();
        Update u = new UpdateCreate(graphIRI);
        UpdateAction.execute(u, gStore);

        u = new UpdateCreate(graphIRI, true);
        UpdateAction.execute(u, gStore);
        assertTrue(graphEmpty(gStore.getGraph(graphIRI)));

        u = new UpdateDrop(graphIRI);
        UpdateAction.execute(u, gStore);
        assertFalse(gStore.containsGraph(graphIRI));
        u = new UpdateDrop(graphIRI, true);
        UpdateAction.execute(u, gStore);

    }

    @Test
    public void testCreateDrop3() {
        DatasetGraph gStore = getEmptyDatasetGraph();
        script(gStore, "create-1.ru");
        // JENA-1068
// assertTrue(gStore.containsGraph(graphIRI)) ;
        assertTrue(graphEmpty(gStore.getGraph(graphIRI)));
    }

    @Test
    public void testCreateDrop4() {
        DatasetGraph gStore = getEmptyDatasetGraph();
        gStore.addGraph(graphIRI, GraphFactory.createDefaultGraph());
        script(gStore, "drop-1.ru");
        assertFalse(gStore.containsGraph(graphIRI));
    }

    @Test
    public void testAdd() {
        DatasetGraph gStore = DatasetGraphFactory.create();
        Node g1 = SSE.parseNode(":g1");
        Node g2 = SSE.parseNode(":g2");
        gStore.add(SSE.parseQuad("( :g1 :s :p :o )"));
        // ADD GRAPH :g1 TO GRAPH :g2, not SILENT
        Update add = new UpdateAdd(Target.create(g1), Target.create(g2), false);
        assertFalse(gStore.containsGraph(g2));

        UpdateAction.execute(add, gStore);

        assertTrue(gStore.containsGraph(g2));
    }
}
