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

package org.apache.jena.ontapi;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.ontapi.impl.UnionGraphImpl;
import org.apache.jena.ontapi.impl.repositories.DocumentGraphRepository;
import org.apache.jena.ontapi.testutils.MiscUtils;
import org.apache.jena.ontapi.testutils.RDFIOTestUtils;
import org.apache.jena.ontapi.utils.Graphs;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
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
        Assertions.assertEquals(List.of(g1), repository.loadedGraphs().toList());

        Graph g3 = repository.get("X");
        Assertions.assertEquals(163, g3.size());
        Assertions.assertInstanceOf(UnionGraph.class, g3);
        Assertions.assertFalse(((UnionGraph) g3).hasSubGraph());
        Graph g4 = repository.get("builtins-rdfs.rdf");
        Assertions.assertSame(g3, g4);
        Assertions.assertEquals(Set.of(g1, g3), repository.loadedGraphs().collect(Collectors.toSet()));

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

    @Test
    public void testAddMappings() {
        var mappings = RDFIOTestUtils.readStringAsModel(
                """
                        PREFIX lm: <http://jena.hpl.hp.com/2004/08/location-mapping#>
                        
                        [] lm:mapping
                            [ lm:name "vocab1" ; lm:altName "file:ontologies/vocab1.ttl" ] ,
                            [ lm:prefix "vocab-prefix/" ; lm:altPrefix "builtins-" ] ,
                            [ lm:prefix "bad-prefix/" ; lm:altName "file:ontologies/prefix.ttl" ] .
                        """, "ttl"
        );
        DocumentGraphRepository repository = GraphRepository.createGraphDocumentRepositoryMem()
                .addMappings(mappings);

        Assertions.assertTrue(repository.contains("vocab1"));
        Assertions.assertTrue(repository.contains("file:ontologies/vocab1.ttl"));
        Assertions.assertTrue(repository.contains("vocab-prefix/rdfs.rdf"));
        Assertions.assertFalse(repository.contains("bad-prefix/rdfs.rdf"));
        Assertions.assertFalse(repository.contains("file:ontologies/prefix.ttl"));
        Assertions.assertEquals(2, repository.count());

        Graph g = repository.get("vocab-prefix/rdfs.rdf");
        Assertions.assertEquals(163, g.size());
        Assertions.assertEquals(3, repository.count());
    }

    @Test
    public void testAddPrefixMappingGet() {
        DocumentGraphRepository repository = GraphRepository.createGraphDocumentRepositoryMem()
                .addPrefixMapping("vocab/", "builtins-");

        Assertions.assertTrue(repository.contains("vocab/rdfs.rdf"));
        Assertions.assertEquals(0, repository.count());

        Graph g1 = repository.get("vocab/rdfs.rdf");
        Assertions.assertEquals(163, g1.size());
        Assertions.assertEquals(
                List.of("vocab/rdfs.rdf"),
                repository.ids().sorted().toList());
        Assertions.assertEquals(1, repository.count());

        Graph g2 = repository.get("builtins-rdfs.rdf");
        Assertions.assertSame(g1, g2);
        Assertions.assertEquals(
                List.of("builtins-rdfs.rdf", "vocab/rdfs.rdf"),
                repository.ids().sorted().toList());
        Assertions.assertEquals(2, repository.count());
    }

    @Test
    public void testExactMappingWinsWhenIdAlsoMatchesPrefixMapping() {
        DocumentGraphRepository repository = GraphRepository.createGraphDocumentRepositoryMem()
                .addPrefixMapping("vocab/", "builtins-")
                .addMapping("vocab/rdfs.rdf", "builtins-owl.rdf");

        Assertions.assertTrue(repository.contains("vocab/rdfs.rdf"));
        Assertions.assertTrue(repository.contains("vocab/owl.rdf"));

        Graph g = repository.get("vocab/rdfs.rdf");
        Assertions.assertEquals(159, g.size());
        Assertions.assertSame(g, repository.get("builtins-owl.rdf"));

        Graph sibling = repository.get("vocab/owl.rdf");
        Assertions.assertEquals(159, sibling.size());
        Assertions.assertSame(sibling, repository.get("builtins-owl.rdf"));
    }

    @Test
    public void testAddMappingRemovesOrphanLoadedGraphAfterRemap() {
        DocumentGraphRepository repository = GraphRepository.createGraphDocumentRepositoryMem()
                .addPrefixMapping("vocab/", "builtins-");

        Graph prefixGraph = repository.get("vocab/rdfs.rdf");
        Assertions.assertEquals(163, prefixGraph.size());
        Assertions.assertEquals(List.of(prefixGraph), repository.loadedGraphs().toList());

        repository.addMapping("vocab/rdfs.rdf", "builtins-owl.rdf");
        Assertions.assertFalse(repository.loadedGraphs().toList().contains(prefixGraph));
        // addMapping only changes the source binding. The replacement graph is loaded lazily by get(...).
        Assertions.assertEquals(List.of(), repository.loadedGraphs().toList());

        Graph exactGraph = repository.get("vocab/rdfs.rdf");
        Assertions.assertEquals(159, exactGraph.size());
        Assertions.assertEquals(List.of(exactGraph), repository.loadedGraphs().toList());
    }

    @Test
    public void testLoadedGraphsReturnsDistinctGraphs() {
        DocumentGraphRepository repository = GraphRepository.createGraphDocumentRepositoryMem();
        Graph graph = GraphMemFactory.createDefaultGraph();

        repository.put("first.ttl", graph);
        repository.put("second.ttl", graph);

        Assertions.assertEquals(List.of(graph), repository.loadedGraphs().toList());
    }

    @Test
    public void testAddPrefixMappingLongestLocationPrefixWins() {
        DocumentGraphRepository repository = GraphRepository.createGraphDocumentRepositoryMem()
                .addPrefixMapping("vocab/", "builtins-owl")
                .addPrefixMapping("vocab/rdfs", "builtins-rdfs");

        Graph g = repository.get("vocab/rdfs.rdf");
        Assertions.assertEquals(163, g.size());
        Assertions.assertSame(g, repository.get("builtins-rdfs.rdf"));
    }

    @Test
    public void testAddPrefixMappingUsesLocationMapperPrefixSelection(@TempDir Path dir) throws Exception {
        Path longLocationPrefix = Files.createDirectories(dir.resolve("some-very-long-location-prefix"));
        Path shortLocationPrefix = Files.createDirectories(dir.resolve("x"));
        Path document = Files.createDirectories(longLocationPrefix.resolve("rdfs")).resolve("schema.ttl");
        Files.writeString(document, "@prefix : <http://example.com/test#> . :s :p :o .");
        String longLocation = longLocationPrefix.toUri().toString();
        String shortLocation = shortLocationPrefix.toUri().toString();
        if (!longLocation.endsWith("/")) {
            longLocation += "/";
        }
        if (!shortLocation.endsWith("/")) {
            shortLocation += "/";
        }

        DocumentGraphRepository repository = GraphRepository.createGraphDocumentRepositoryMem()
                .addPrefixMapping("vocab/", longLocation)
                .addPrefixMapping("vocab/rdfs/", shortLocation);

        Graph g = repository.get("vocab/rdfs/schema.ttl");
        Assertions.assertEquals(1, g.size());
        Assertions.assertSame(g, repository.get(document.toUri().toString()));
    }

    @Test
    public void testAddPrefixMappingRemoveAndClear() {
        DocumentGraphRepository repository = GraphRepository.createGraphDocumentRepositoryMem()
                .addPrefixMapping("vocab/", "builtins-");

        Graph g = repository.get("vocab/rdfs.rdf");
        Assertions.assertEquals(163, g.size());
        Assertions.assertTrue(repository.contains("vocab/rdfs.rdf"));

        Assertions.assertSame(g, repository.remove("vocab/rdfs.rdf"));
        Assertions.assertFalse(repository.contains("vocab/rdfs.rdf"));
        Assertions.assertTrue(repository.contains("vocab/owl.rdf"));
        Assertions.assertEquals(0, repository.count());

        repository.addPrefixMapping("vocab/", "builtins-");
        Assertions.assertTrue(repository.contains("vocab/rdfs.rdf"));
        repository.clear();
        Assertions.assertFalse(repository.contains("vocab/rdfs.rdf"));
        Assertions.assertEquals(0, repository.count());
    }

    @Test
    public void testRemovePrefixMapping() {
        DocumentGraphRepository repository = GraphRepository.createGraphDocumentRepositoryMem()
                .addPrefixMapping("vocab/", "builtins-");

        Graph g = repository.get("vocab/rdfs.rdf");
        Assertions.assertEquals(163, g.size());
        Assertions.assertTrue(repository.contains("vocab/owl.rdf"));

        Assertions.assertEquals("builtins-", repository.removePrefixMapping("vocab/"));
        Assertions.assertNull(repository.removePrefixMapping("vocab/"));
        Assertions.assertFalse(repository.contains("vocab/owl.rdf"));

        Assertions.assertTrue(repository.contains("vocab/rdfs.rdf"));
        Assertions.assertSame(g, repository.get("vocab/rdfs.rdf"));
    }

    @Test
    public void testRemoveExactMappingKeepsMatchingPrefixMapping() {
        DocumentGraphRepository repository = GraphRepository.createGraphDocumentRepositoryMem()
                .addPrefixMapping("vocab/", "builtins-");

        Graph prefixGraph = repository.get("vocab/rdfs.rdf");
        Assertions.assertEquals(163, prefixGraph.size());

        repository.addMapping("vocab/rdfs.rdf", "builtins-owl.rdf");
        Graph exactGraph = repository.get("vocab/rdfs.rdf");
        Assertions.assertEquals(159, exactGraph.size());

        Assertions.assertSame(exactGraph, repository.remove("vocab/rdfs.rdf"));
        Assertions.assertFalse(repository.contains("vocab/rdfs.rdf"));
        Assertions.assertTrue(repository.contains("vocab/owl.rdf"));
        Assertions.assertThrows(RuntimeException.class, () -> repository.get("vocab/rdfs.rdf"));
    }

    @Test
    public void testRemovePutGraphKeepsMatchingPrefixMapping() {
        DocumentGraphRepository repository = GraphRepository.createGraphDocumentRepositoryMem()
                .addPrefixMapping("vocab/", "builtins-");

        Graph graph = repository.get("vocab/rdfs.rdf");
        Assertions.assertEquals(163, graph.size());

        Graph replacement = GraphMemFactory.createDefaultGraph();
        Assertions.assertSame(graph, repository.put("vocab/rdfs.rdf", replacement));

        Assertions.assertSame(replacement, repository.remove("vocab/rdfs.rdf"));
        Assertions.assertFalse(repository.contains("vocab/rdfs.rdf"));
        Assertions.assertTrue(repository.contains("vocab/owl.rdf"));
        Assertions.assertThrows(RuntimeException.class, () -> repository.get("vocab/rdfs.rdf"));
    }

    @Test
    public void testAddPrefixMappingRestoresRemovedExactId() {
        DocumentGraphRepository repository = GraphRepository.createGraphDocumentRepositoryMem()
                .addPrefixMapping("vocab/", "builtins-")
                .addMapping("vocab/rdfs.rdf", "builtins-owl.rdf");

        Graph exactGraph = repository.get("vocab/rdfs.rdf");
        Assertions.assertEquals(159, exactGraph.size());
        Assertions.assertSame(exactGraph, repository.remove("vocab/rdfs.rdf"));
        Assertions.assertFalse(repository.contains("vocab/rdfs.rdf"));

        repository.addPrefixMapping("vocab/", "builtins-");
        Assertions.assertTrue(repository.contains("vocab/rdfs.rdf"));
        Assertions.assertEquals(163, repository.get("vocab/rdfs.rdf").size());
    }

    @Test
    public void testAddMappingRestoresRemovedExactId() {
        DocumentGraphRepository repository = GraphRepository.createGraphDocumentRepositoryMem()
                .addPrefixMapping("vocab/", "builtins-")
                .addMapping("vocab/rdfs.rdf", "builtins-owl.rdf");

        Graph exactGraph = repository.get("vocab/rdfs.rdf");
        Assertions.assertEquals(159, exactGraph.size());
        Assertions.assertSame(exactGraph, repository.remove("vocab/rdfs.rdf"));
        Assertions.assertFalse(repository.contains("vocab/rdfs.rdf"));

        repository.addMapping("vocab/rdfs.rdf", "builtins-rdfs.rdf");
        Assertions.assertTrue(repository.contains("vocab/rdfs.rdf"));
        Assertions.assertEquals(163, repository.get("vocab/rdfs.rdf").size());
    }
}
