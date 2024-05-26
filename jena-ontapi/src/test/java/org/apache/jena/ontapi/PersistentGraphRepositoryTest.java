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

package org.apache.jena.ontapi;

import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.ontapi.impl.repositories.PersistentGraphRepository;
import org.apache.jena.ontapi.utils.Graphs;
import org.apache.jena.shared.DoesNotExistException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PersistentGraphRepositoryTest {

    @Test
    public void testGetIdsRemove() {
        var maker = new TestMemGraphMaker();
        var repo = new PersistentGraphRepository(maker);
        Assertions.assertThrows(DoesNotExistException.class, () -> repo.get("/graph"));
        var graph = maker.createGraph("/graph");
        Assertions.assertSame(graph, repo.get("/graph"));
        Assertions.assertEquals(List.of("/graph"), repo.ids().toList());

        Assertions.assertSame(graph, repo.remove("/graph"));
        Assertions.assertEquals(List.of(), repo.ids().toList());

        // restore graph
        Assertions.assertSame(graph, repo.get("/graph"));
        Assertions.assertEquals(List.of("/graph"), repo.ids().toList());

        maker.removeGraph("/graph");
        repo.remove("/graph");
        Assertions.assertThrows(DoesNotExistException.class, () -> repo.get("/graph"));
    }

    @Test
    public void testPutIdsGet() {
        var maker = new TestMemGraphMaker();
        var repo = new PersistentGraphRepository(maker);
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                repo.put("/graph", GraphMemFactory.createDefaultGraph())
        );
        Assertions.assertNull(repo.put("/graph", maker.createGraph("/graph")));
        Assertions.assertNotNull(repo.put("/graph", maker.openGraph("/graph")));
        Assertions.assertEquals(List.of("/graph"), repo.ids().toList());

        var u = (UnionGraph)OntModelFactory.createModel(maker.getGraphOrNull("/graph")).getGraph();
        repo.put("/graph", u);
        Assertions.assertSame(u, repo.get("/graph"));
        Assertions.assertEquals(List.of("/graph"), repo.ids().toList());
    }

    @Test
    public void testLoadAll() {
        var maker = new TestMemGraphMaker();
        maker.createGraph("g1");
        maker.createGraph("g2");
        var repo1 = new PersistentGraphRepository(maker);
        Assertions.assertEquals(Set.of("g1", "g2"), repo1.ids().collect(Collectors.toSet()));

        var repo2 = new PersistentGraphRepository(maker);
        Assertions.assertEquals(Set.of("g1", "g2"), repo2.ids().collect(Collectors.toSet()));

        // remap
        Graphs.createOntologyHeaderNode(maker.openGraph("g2"), "g3");
        var repo3 = new PersistentGraphRepository(maker);
        Assertions.assertEquals(Set.of("g1", "g3"), repo3.ids().collect(Collectors.toSet()));
    }
}
