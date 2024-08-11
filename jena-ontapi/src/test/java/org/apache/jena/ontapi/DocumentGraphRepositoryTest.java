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

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.ontapi.impl.UnionGraphImpl;
import org.apache.jena.ontapi.impl.repositories.DocumentGraphRepository;
import org.apache.jena.ontapi.testutils.MiscUtils;
import org.apache.jena.ontapi.utils.Graphs;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DocumentGraphRepositoryTest {

    @Test
    public void testLoadResources() {
        DocumentGraphRepository repository =
                GraphRepository.createGraphDocumentRepository(
                                () -> new UnionGraphImpl(GraphMemFactory.createDefaultGraph())
                        )
                        .addMapping("http://www.w3.org/2002/07/owl#", "builtins-owl.rdf")
                        .addMapping("http://www.w3.org/2002/07/owl#", "builtins-owl.rdf")
                        .addMapping("X", "builtins-rdfs.rdf")
                        .addMapping("Y", "builtins-rdfs.rdf");

        Graph g1 = repository.get("http://www.w3.org/2002/07/owl#");
        Assertions.assertEquals(159, g1.size());
        Assertions.assertInstanceOf(UnionGraph.class, g1);
        Assertions.assertFalse(((UnionGraph) g1).hasSubGraph());
        Graph g2 = repository.get("builtins-owl.rdf");
        Assertions.assertSame(g1, g2);
        Assertions.assertEquals(List.of(g1), repository.graphs().toList());

        Graph g3 = repository.get("X");
        Assertions.assertEquals(163, g3.size());
        Assertions.assertInstanceOf(UnionGraph.class, g3);
        Assertions.assertFalse(((UnionGraph) g3).hasSubGraph());
        Graph g4 = repository.get("builtins-rdfs.rdf");
        Assertions.assertSame(g3, g4);
        Assertions.assertEquals(Set.of(g1, g3), repository.graphs().collect(Collectors.toSet()));

        Assertions.assertEquals(
                List.of("X", "Y", "builtins-owl.rdf", "builtins-rdfs.rdf", "http://www.w3.org/2002/07/owl#"),
                repository.ids().sorted().toList());
        Assertions.assertEquals(5, repository.count());

        Assertions.assertSame(g3, repository.remove("X"));

        Assertions.assertEquals(List.of("builtins-owl.rdf", "http://www.w3.org/2002/07/owl#"), repository.ids().sorted().toList());
        Assertions.assertEquals(2, repository.count());

        repository.clear();

        Assertions.assertEquals(0, repository.ids().count());
    }

    @Test
    public void testLoadFiles(@TempDir Path dir) {
        Path file = MiscUtils.save("/builtins-rdfs.rdf", dir);
        DocumentGraphRepository repository = GraphRepository.createGraphDocumentRepositoryMem()
                .addMapping("http://www.w3.org/2002/07/owl#", file.toUri().toString());
        Graph g = repository.get("http://www.w3.org/2002/07/owl#");
        Assertions.assertEquals(163, g.size());
        Assertions.assertTrue(Graphs.isGraphMem(g));
    }
}
