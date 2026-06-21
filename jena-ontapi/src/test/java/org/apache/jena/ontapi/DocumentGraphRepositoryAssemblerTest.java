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

import org.apache.jena.assembler.Assembler;
import org.apache.jena.ontapi.impl.repositories.DocumentGraphRepository;
import org.apache.jena.ontapi.testutils.RDFIOTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class DocumentGraphRepositoryAssemblerTest {

    @Test
    public void testAssemblerWithNoGraphs() {
        var m = RDFIOTestUtils.readStringAsModel(
                """
                        PREFIX : <http://ex.com#>
                        PREFIX oa: <https://jena.apache.org/ontapi/Assembler#>
                        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                        :repo a oa:DocumentGraphRepository .
                        """, "ttl"
        );
        var root = m.getResource("http://ex.com#repo");
        var result = Assembler.general().open(root);
        Assertions.assertInstanceOf(DocumentGraphRepository.class, result);
        var repo = (DocumentGraphRepository) result;
        Assertions.assertEquals(0, repo.count());
    }

    @Test
    public void testAssemblerWithTwoGraphs() {
        var m = RDFIOTestUtils.readStringAsModel(
                """
                        PREFIX : <http://ex.com#>
                        PREFIX oa: <https://jena.apache.org/ontapi/Assembler#>
                        PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                        :repo a oa:DocumentGraphRepository ;
                            oa:graph :g1, :g2 .
                        
                        :g1 a oa:Graph ;
                            oa:graphIRI "vocab1" ;
                            oa:graphLocation "file:ontologies/vocab1.ttl" .
                        
                        :g2 a oa:Graph ;
                            oa:graphIRI "vocab2" ;
                            oa:graphLocation "file:ontologies/vocab2.ttl" .
                        """, "ttl"
        );
        var root = m.getResource("http://ex.com#repo");
        var result = Assembler.general().open(root);
        Assertions.assertInstanceOf(DocumentGraphRepository.class, result);
        var repo = (DocumentGraphRepository) result;
        Assertions.assertEquals(4, repo.count());
        Assertions.assertEquals(
                Set.of("vocab1", "vocab2", "file:ontologies/vocab1.ttl", "file:ontologies/vocab2.ttl"),
                new HashSet<>(repo.ids().toList())
        );
    }

    @Test
    public void testAssemblerWithLocationMappings() {
        var m = RDFIOTestUtils.readStringAsModel(
                """
                        PREFIX : <http://ex.com#>
                        PREFIX oa: <https://jena.apache.org/ontapi/Assembler#>
                        PREFIX ja: <http://jena.hpl.hp.com/2005/11/Assembler#>
                        PREFIX lm: <http://jena.hpl.hp.com/2004/08/location-mapping#>
                        :repo a oa:DocumentGraphRepository ;
                            oa:locationMappings :mappingModel .
                        :mappingModel a ja:MemoryModel ;
                            ja:content [
                                ja:literalContent '''
                                    PREFIX lm: <http://jena.hpl.hp.com/2004/08/location-mapping#>
                                    [] lm:mapping
                                        [ lm:name "vocab1" ; lm:altName "file:ontologies/vocab1.ttl" ] ,
                                        [ lm:prefix "vocab-prefix/" ; lm:altPrefix "builtins-" ] ,
                                        [ lm:prefix "bad-prefix/" ; lm:altName "file:ontologies/prefix.ttl" ] .
                                ''' ;
                                ja:contentEncoding "TTL"
                            ] .
                        """, "ttl"
        );
        var root = m.getResource("http://ex.com#repo");
        var result = Assembler.general().open(root);
        Assertions.assertInstanceOf(DocumentGraphRepository.class, result);
        var repo = (DocumentGraphRepository) result;
        Assertions.assertEquals(2, repo.count());
        Assertions.assertTrue(repo.contains("vocab-prefix/rdfs.rdf"));
        Assertions.assertFalse(repo.contains("bad-prefix/rdfs.rdf"));
        Assertions.assertEquals(
                Set.of("vocab1", "file:ontologies/vocab1.ttl"),
                new HashSet<>(repo.ids().toList())
        );
    }

    @Test
    public void testAssemblerWithLocationMappingsFromFile(@TempDir Path dir) throws Exception {
        Path mappings = Files.createTempFile(dir, "location-mapping", ".ttl");
        Files.writeString(mappings,
                """
                        PREFIX lm: <http://jena.hpl.hp.com/2004/08/location-mapping#>
                        [] lm:mapping
                            [ lm:name "vocab1" ; lm:altName "file:ontologies/vocab1.ttl" ] ,
                            [ lm:prefix "vocab-prefix/" ; lm:altPrefix "builtins-" ] .
                        """
        );
        var m = RDFIOTestUtils.readStringAsModel(
                """
                        PREFIX : <http://ex.com#>
                        PREFIX oa: <https://jena.apache.org/ontapi/Assembler#>
                        PREFIX ja: <http://jena.hpl.hp.com/2005/11/Assembler#>
                        :repo a oa:DocumentGraphRepository ;
                            oa:locationMappings :mappingModel .
                        :mappingModel a ja:MemoryModel ;
                            ja:externalContent <%s> .
                        """.formatted(mappings.toUri()), "ttl"
        );
        var root = m.getResource("http://ex.com#repo");
        var result = Assembler.general().open(root);
        Assertions.assertInstanceOf(DocumentGraphRepository.class, result);
        var repo = (DocumentGraphRepository) result;
        Assertions.assertEquals(2, repo.count());
        Assertions.assertTrue(repo.contains("vocab1"));
        Assertions.assertTrue(repo.contains("vocab-prefix/rdfs.rdf"));
        Assertions.assertEquals(
                Set.of("vocab1", "file:ontologies/vocab1.ttl"),
                new HashSet<>(repo.ids().toList())
        );
    }

    @Test
    public void testAssemblerWithGraphsAndLocationMappings() {
        var m = RDFIOTestUtils.readStringAsModel(
                """
                        PREFIX : <http://ex.com#>
                        PREFIX oa: <https://jena.apache.org/ontapi/Assembler#>
                        PREFIX ja: <http://jena.hpl.hp.com/2005/11/Assembler#>
                        PREFIX lm: <http://jena.hpl.hp.com/2004/08/location-mapping#>
                        :repo a oa:DocumentGraphRepository ;
                            oa:graph :g1, :g2, :g3 ;
                            oa:locationMappings :mappingModel .
                        :mappingModel a ja:MemoryModel ;
                            ja:content [
                                ja:literalContent '''
                                    PREFIX lm: <http://jena.hpl.hp.com/2004/08/location-mapping#>
                                    [] lm:mapping
                                        [
                                            lm:name "vocab3" ;
                                            lm:altName "builtins-rdfs.rdf"
                                        ] ,
                                        [
                                            lm:prefix "https://example.com/ontologies/" ;
                                            lm:altPrefix "builtins-"
                                        ] .
                                ''' ;
                                ja:contentEncoding "TTL"
                            ] .
                        :g1 a oa:Graph ;
                            oa:graphIRI "vocab1" ;
                            oa:graphLocation "builtins-owl.rdf" .
                        :g2 a oa:Graph ;
                            oa:graphIRI "vocab2" ;
                            oa:graphLocation "builtins-rdfs.rdf" .
                        :g3 a oa:Graph ;
                            oa:graphIRI "pizza" ;
                            oa:graphLocation "pizza.ttl" .
                        """, "ttl"
        );
        var root = m.getResource("http://ex.com#repo");
        var result = Assembler.general().open(root);
        Assertions.assertInstanceOf(DocumentGraphRepository.class, result);
        var repo = (DocumentGraphRepository) result;

        Assertions.assertEquals(
                Set.of("vocab1", "vocab2", "vocab3", "pizza", "builtins-owl.rdf", "builtins-rdfs.rdf", "pizza.ttl"),
                new HashSet<>(repo.ids().toList())
        );
        Assertions.assertTrue(repo.contains("https://example.com/ontologies/rdfs.rdf"));
        Assertions.assertEquals(7, repo.count());

        Assertions.assertEquals(159, repo.get("vocab1").size());
        Assertions.assertEquals(163, repo.get("vocab2").size());
        Assertions.assertEquals(163, repo.get("vocab3").size());
        Assertions.assertTrue(repo.get("pizza").size() > 1_000);
        Assertions.assertSame(repo.get("pizza"), repo.get("pizza.ttl"));
        Assertions.assertSame(repo.get("vocab2"), repo.get("https://example.com/ontologies/rdfs.rdf"));
    }
}
