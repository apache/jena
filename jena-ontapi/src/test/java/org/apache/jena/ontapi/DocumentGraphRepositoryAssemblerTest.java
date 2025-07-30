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

import org.apache.jena.assembler.Assembler;
import org.apache.jena.ontapi.impl.repositories.DocumentGraphRepository;
import org.apache.jena.ontapi.testutils.RDFIOTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

public class DocumentGraphRepositoryAssemblerTest {

    @Test
    public void testAssemblerWithNoGraphs() {
        var m = RDFIOTestUtils.readStringAsModel(
                """
                        @prefix : <http://ex.com#> .
                        @prefix oa: <https://jena.apache.org/ontapi/Assembler#> .
                        @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
                        
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
                        @prefix : <http://ex.com#> .
                        @prefix oa: <https://jena.apache.org/ontapi/Assembler#> .
                        @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
                        
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
}
